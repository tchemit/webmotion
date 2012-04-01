/*
 * #%L
 * Webmotion in action
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 Debux
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package org.debux.webmotion.server.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.debux.webmotion.server.WebMotionException;
import org.debux.webmotion.server.WebMotionUtils;
import org.debux.webmotion.server.mapping.Action;
import org.debux.webmotion.server.mapping.ActionRule;
import org.debux.webmotion.server.mapping.Config;
import org.debux.webmotion.server.mapping.Config.Scope;
import org.debux.webmotion.server.mapping.Config.State;
import org.debux.webmotion.server.mapping.ErrorRule;
import org.debux.webmotion.server.mapping.FilterRule;
import org.debux.webmotion.server.mapping.Mapping;
import org.debux.webmotion.server.mapping.FragmentUrl;
import org.nuiton.util.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of parser with use split and pattern to extract the 
 * mapping.
 * 
 * @author jruchaud
 */
public class BasicMappingParser extends MappingParser {

    private static final Logger log = LoggerFactory.getLogger(BasicMappingParser.class);
    
    /** Allowed characted are all alphanumeric characters, all punctuation characters exception following: <pre>!*'();:@&=+$,/?#[]</pre> */
    protected static final String ALLOWED_CHARACTERS = "[\\p{Alnum}\\p{Punct}&&[^!\\*'\\(\\);:@&=+$,\\/\\?#\\[\\]]]";
    protected static Pattern patternParam = Pattern.compile("^(" + ALLOWED_CHARACTERS + "*)=\\{(" + ALLOWED_CHARACTERS + "*)(:)?(.*)?\\}$");
    protected static Pattern patternStaticParam = Pattern.compile("^(" + ALLOWED_CHARACTERS + "*)=(" + ALLOWED_CHARACTERS + "*)$");
    protected static Pattern patternPath = Pattern.compile("^\\{(\\p{Alnum}*)(:)?(.*)?\\}$");

    /**
     * Default contructor
     */
    public BasicMappingParser() {
        super();
    }

    /**
     * Default contructor
     */
    public BasicMappingParser(Config defaultConfig) {
        super(defaultConfig);
    }
    
    @Override
    protected Mapping parse(URL url) {
        Mapping mapping = new Mapping(defaultConfig);
        mapping.setName(url.toExternalForm());
        
        Config config = mapping.getConfig();
        
        try {
            InputStream stream = url.openStream();
            List<String> rules = IOUtils.readLines(stream);

            int section = 0;
            int line = 0;
            for (String rule : rules) {

                rule = rule.trim();
                rule = rule.replaceAll(" +", " ");

                if(rule.startsWith("#") || rule.isEmpty()) {
                    // Comments
                } else if (rule.startsWith("[errors]")) {
                    section = 1;
                } else if (rule.startsWith("[filters]")) {
                    section = 2;
                } else if (rule.startsWith("[actions]")) {
                    section = 3;
                } else if (rule.startsWith("[config]")) {
                    section = 4;
                } else if (rule.startsWith("[extensions]")) {
                    section = 5;

                } else if (section == 1) {
                    // Errors section
                    ErrorRule errorRule = extractSectionErrors(rule);
                    errorRule.setMapping(mapping);
                    errorRule.setLine(line);
                    List<ErrorRule> errorRules = mapping.getErrorRules();
                    errorRules.add(errorRule);

                } else if (section == 2) {
                    // Filters section
                    FilterRule filterRule = extractSectionFilters(rule);
                    filterRule.setMapping(mapping);
                    filterRule.setLine(line);
                    List<FilterRule> filterRules = mapping.getFilterRules();
                    filterRules.add(filterRule);

                } else if (section == 3) {
                    // Actions section
                    ActionRule actionRule = extractSectionActions(rule);
                    actionRule.setMapping(mapping);
                    actionRule.setLine(line);
                    List<ActionRule> actionRules = mapping.getActionRules();
                    actionRules.add(actionRule);

                } else if (section == 5) {
                    // Extension section
                    List<Mapping> extensionMappings = extractSectionExtensions(rule);
                    List<Mapping> extensionsRules = mapping.getExtensionsRules();
                    extensionsRules.addAll(extensionMappings);
                    
                } else if (section == 4 && rule.startsWith(Config.PACKAGE_VIEWS)) {
                    String value = extractConfig(Config.PACKAGE_VIEWS, rule);
                    config.setPackageViews(value);

                } else if (section == 4 && rule.startsWith(Config.PACKAGE_ACTIONS)) {
                    String value = extractConfig(Config.PACKAGE_ACTIONS, rule);
                    config.setPackageActions(value);
                    
                } else if (section == 4 && rule.startsWith(Config.PACKAGE_FILTERS)) {
                    String value = extractConfig(Config.PACKAGE_FILTERS, rule);
                    config.setPackageFilters(value);
                    
                } else if (section == 4 && rule.startsWith(Config.PACKAGE_ERRORS)) {
                    String value = extractConfig(Config.PACKAGE_ERRORS, rule);
                    config.setPackageErrors(value);
                    
                } else if (section == 4 && rule.startsWith(Config.SERVER_ENCODING)) {
                    String value = extractConfig(Config.SERVER_ENCODING, rule);
                    config.setEncoding(value);
                    
                } else if (section == 4 && rule.startsWith(Config.SERVER_ASYNC)) {
                    String value = extractConfig(Config.SERVER_ASYNC, rule);
                    config.setAsync(Boolean.valueOf(value));
                    
                } else if (section == 4 && rule.startsWith(Config.JAVAC_DEBUG)) {
                    String value = extractConfig(Config.JAVAC_DEBUG, rule);
                    config.setJavacDebug(Boolean.valueOf(value));
                    
                } else if (section == 4 && rule.startsWith(Config.SERVER_CONTROLLER_SCOPE)) {
                    String value = extractConfig(Config.SERVER_CONTROLLER_SCOPE, rule);
                    config.setControllerScope(Scope.valueOf(value.toUpperCase()));
                    
                } else if (section == 4 && rule.startsWith(Config.SERVER_MAIN_HANDLER_CLASS)) {
                    String value = extractConfig(Config.SERVER_MAIN_HANDLER_CLASS, rule);
                    config.setMainHandler(value);
                    
                } else if (section == 4 && rule.startsWith(Config.SERVER_ERROR_PAGE)) {
                    String value = extractConfig(Config.SERVER_ERROR_PAGE, rule);
                    config.setErrorPage(State.valueOf(value.toUpperCase()));
                    
                } else if (section == 4 && rule.startsWith(Config.SERVER_LISTENER_CLASS)) {
                    String value = extractConfig(Config.SERVER_LISTENER_CLASS, rule);
                    config.setServerListener(value);
                    
                } else if (section == 4 && rule.startsWith(Config.SERVER_SECRET)) {
                    String value = extractConfig(Config.SERVER_SECRET, rule);
                    config.setSecret(value);
                }
                
                line ++;
            }
        } catch (IOException ioe) {
            throw new WebMotionException("Error to read the file mapping", ioe);
        }
        
        return mapping;
    }
    
    /**
     * Extract the value in line
     * @param name config name
     * @param line line contains the config
     * @return the value in line
     */
    public String extractConfig(String name, String line) {
        String value = line.substring((name + "=").length());
        return value;
    }
    
    /**
     * Extract in the given line an error rule
     * @param line one line in mapping
     */
    public ErrorRule extractSectionErrors(String line) {
        String[] splitRule = line.split(" ");
        ErrorRule errorRule = new ErrorRule();
        
        String error = extractError(splitRule[0]);
        errorRule.setError(error);
        
        Action action = extractAction(splitRule[1]);
        errorRule.setAction(action);
        
        return errorRule;
    }

    /**
     * Extract in the given line an filter rule
     * @param line one line in mapping
     */
    public FilterRule extractSectionFilters(String line) {
        String[] splitRule = line.split(" ");
        FilterRule filterRule = new FilterRule();
        
        List<String> methods = extractMethods(splitRule[0]);
        filterRule.setMethods(methods);
        
        Pattern pattern = extractPattern(splitRule[1]);
        filterRule.setPattern(pattern);
        
        Action action = extractAction(splitRule[2]);
        filterRule.setAction(action);
        
        return filterRule;
    }

    /**
     * Extract in the given line an action rule
     * @param line one line in mapping
     */
    public ActionRule extractSectionActions(String line) {
        String[] splitRule = line.split(" ");
        ActionRule actionRule = new ActionRule();
        
        List<String> methods = extractMethods(splitRule[0]);
        actionRule.setMethods(methods);
        
        List<FragmentUrl> url = extractUrl(splitRule[1]);
        actionRule.setRuleUrl(url);
        
        List<FragmentUrl> parameters = extractParameters(splitRule[1]);
        actionRule.setRuleParameters(parameters);
        
        Action action = extractAction(splitRule[2]);
        actionRule.setAction(action);
        
        if(splitRule.length >= 4) {
            Map<String, String[]> defaultParameters = extractDefaultParameters(splitRule[3]);
            actionRule.setDefaultParameters(defaultParameters);
            
        }
        return actionRule;
    }
    
    /**
     * Extract in the given line an extension rule
     * @param line one line in mapping
     */
    protected List<Mapping> extractSectionExtensions(String line) {
        String[] splitRule = line.split(" ");
        String path = splitRule[0];
        String name = splitRule[1];

        List<Mapping> mappings = new ArrayList<Mapping>();
        
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            List<URL> resources = Resource.getResources(name, classLoader);
            for (URL resource : resources) {
                BasicMappingParser parser = new BasicMappingParser(defaultConfig);
                
                Mapping extensionMapping = parser.parse(resource);
                extensionMapping.setExtensionPath(path);
                mappings.add(extensionMapping);
            }

        } catch (IOException ex) {
            throw new WebMotionException("Error during read the file mapping with path in " + name, ex);
        }

        return mappings;
    }
    
    /**
     * Extract the error
     * @param ruleException 
     */
    protected String extractError(String ruleException) {
        return ruleException;
    }

    /**
     * Extract the action to execute
     * @param ruleAction 
     */
    protected Action extractAction(String ruleAction) {
        Action action = new Action();
        
        String[] split = ruleAction.split(":");
        if (split.length == 1) {
            action.setFullName(split[0]);
            action.setType(Action.Type.ACTION);
        } else {
            String type = split[0];
            if (type.equalsIgnoreCase("ASYNC")) {
                action.setAsync(true);
                action.setType(Action.Type.ACTION);
                
            } else if (type.equalsIgnoreCase("SYNC")) {
                action.setAsync(false);
                action.setType(Action.Type.ACTION);
                
            } else if (type.equalsIgnoreCase("VIEW")) {
                action.setType(Action.Type.VIEW);
                
            } else if (type.equalsIgnoreCase("URL")) {
                action.setType(Action.Type.URL);
            }
            
            action.setFullName(split[1]);
        }
        
        return action;
    }

    /**
     * Extract the http method. * represents all http method else you can put 
     * value GET, POST, UPDATE, DELETE, HEADER and PUT.
     * @param methods 
     */
    protected List<String> extractMethods(String methods) {
        String[] split = methods.split(",");
        return Arrays.asList(split);
    }

    /**
     * Extract the url
     * @param rulePattern 
     */
    protected Pattern extractPattern(String rulePattern) {
        String regex = rulePattern.replaceAll("/\\*/", "/[^/]*/");
        regex = rulePattern.replaceAll("/\\*", "/.*");
        
        regex = "^" + regex + "$";
        Pattern pattern = Pattern.compile(regex);
        return pattern;
    }

    /**
     * Extract the url fragment
     * @param fragment 
     */
    protected List<FragmentUrl> extractUrl(String fragment) {
        log.info("fragment = " + fragment);
        List<FragmentUrl> ruleUrl = new ArrayList<FragmentUrl>();
        String baseUrl = StringUtils.substringBefore(fragment, "?");
        if(!baseUrl.isEmpty()) {
            
            List<String> splitBaseUrl = WebMotionUtils.splitPath(baseUrl);
            log.info("splitBaseUrl = " + splitBaseUrl);

            for(String item : splitBaseUrl) {
                FragmentUrl expression = extractExpression(item, false);
                ruleUrl.add(expression);
            }
        }
        return ruleUrl;
    }

    /**
     * Extract the url fragment
     * @param fragment 
     */
    protected List<FragmentUrl> extractParameters(String fragment) {
        List<FragmentUrl> ruleParameters = new ArrayList<FragmentUrl>();
        String queryString = StringUtils.substringAfter(fragment, "?");
        if(!queryString.isEmpty()) {
            
            String[] splitQueryString = StringUtils.splitPreserveAllTokens(queryString, "&");
            log.info("splitQueryString = " + Arrays.toString(splitQueryString));

            for(String item : splitQueryString) {
                FragmentUrl expression = extractExpression(item, true);
                ruleParameters.add(expression);
            }
        }
        return ruleParameters;
    }

    /**
     * Extract a fragment of url patern
     * @param value the fragment
     * @return memory representation
     */
    protected FragmentUrl extractExpression(String value, boolean isParam) {
        FragmentUrl expression = new FragmentUrl();

        Matcher matcherPath = patternPath.matcher(value);
        Matcher matcherParam = patternParam.matcher(value);
        Matcher matcherStaticParam = patternStaticParam.matcher(value);

        String pattern = null;
        if(matcherPath.find()) {
            expression.setName(matcherPath.group(1));
            pattern = matcherPath.group(3);
            
        } else if(matcherParam.find()) {
            expression.setParam(matcherParam.group(1));
            expression.setName(matcherParam.group(2));
            pattern = matcherParam.group(4);
            
        } else if(matcherStaticParam.find()) {
            expression.setParam(matcherStaticParam.group(1));
            pattern = matcherStaticParam.group(2);

        } else if (isParam) {
            expression.setParam(value);
            
        } else {
            pattern = value;
        }

        if(pattern != null && !pattern.isEmpty()) {
            expression.setPattern(Pattern.compile("^" + pattern + "$"));
        }
        
        return expression;
    }

    /**
     * Extract default parameters if not found the parameter in url
     * @param ruleParameters 
     */
    protected Map<String, String[]> extractDefaultParameters(String ruleParameters) {
        Map<String, String[]> defaultParameters = new HashMap<String, String[]>();
        String[] split = ruleParameters.split("=|,");
        for (int position = 0; position < split.length; position += 2) {
            String key = split[position];
            String[] value = {split[position + 1]};
            defaultParameters.put(key, value);
        }
        return defaultParameters;
    }
}
