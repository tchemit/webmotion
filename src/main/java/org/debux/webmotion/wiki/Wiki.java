/*
 * #%L
 * Webmotion in test
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
package org.debux.webmotion.wiki;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.debux.webmotion.server.WebMotionAction;
import org.debux.webmotion.server.call.HttpContext;
import org.debux.webmotion.server.call.Render;
import org.debux.webmotion.wiki.service.WikiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jruchaud
 */
public class Wiki extends WebMotionAction {

    private static final Logger log = LoggerFactory.getLogger(Wiki.class);

    protected WikiService service;

    public Wiki() {
        this(new WikiService());
    }
    
    public Wiki(WikiService service) {
        this.service = service;
    }
    
    public Render display(String nameSpace, String pageName) {
        String url = pageName;
        if(nameSpace != null) {
            url = nameSpace + "/" + url;
        }
        
        return renderView("wiki.jsp", "url", url);
    }

    public Render include(String nameSpace, String pageName) throws Exception {
        String content = service.evalContent(nameSpace, pageName);
        if(content != null) {
            return renderContent(content, "text/html");
        } else {
            return edit(nameSpace, pageName);
        }
    }
    
    public Render preview(String type, String content) throws Exception {
        String generated = service.generate(type, content);
        return renderContent(generated, "text/html");
    }
    
    public Render edit(String nameSpace, String pageName) throws Exception {
        String url = pageName;
        if(nameSpace != null) {
            url = nameSpace + "/" + url;
        }
        
        String type = service.getType(nameSpace, pageName);
        
        return renderView("edit.jsp", 
                            "url", url,
                            "nameSpace", nameSpace,
                            "pageName", pageName,
                            "type", type);
    }

    public Render content(String nameSpace, String pageName) throws Exception {
        String content = service.getContent(nameSpace, pageName);
        
        if(content != null) {
            return renderContent(content, "text/plain");
        } else {
            return renderContent("", "text/plain");
        }
    }
    
    public Render save(String nameSpace, String pageName, String type, String content) throws Exception {
        service.save(nameSpace, pageName, type, content);
        if(nameSpace == null) {
            return renderAction("display/" + pageName);
        } else {
            return renderAction("display/" + nameSpace + "/" + pageName);
        }
    }
    
    public Render media(String nameSpace, String mediaName) throws Exception {
        File file = service.getMedia(nameSpace, mediaName);
        return renderStream(new FileInputStream(file), null);
    }
    
    public Render upload(String nameSpace, String mediaName, File file) throws Exception {
        service.uploadMedia(nameSpace, mediaName, file);
        return renderAction("display/index");
    }
    
    public Render sitemap() throws Exception {
        Map<String, List<String>> siteMap = service.getSiteMap();
        return renderView("map.jsp",
                "map", siteMap,
                "action", "display");
    }
    
    public Render sitemaptxt() throws Exception {
        List<String> result = new ArrayList<String>();
        
        HttpContext context = getContext();
        HttpServletRequest request = context.getRequest();
        String url = request.getRequestURL().toString();
        String servletPath = request.getServletPath();
        String path = StringUtils.substringBefore(url, servletPath);
        
        Map<String, List<String>> siteMap = service.getSiteMap();
        
        for (Map.Entry<String, List<String>> entry : siteMap.entrySet()) {
            String namespace = entry.getKey();
            List<String> pages = entry.getValue();
            
            for (String page : pages) {
                if(namespace == null) {
                   result.add(path + "/deploy/display/" + page);
                } else {
                   result.add(path + "/deploy/display/" + namespace + "/" + page);
                }
            }
        }
        
        return renderView("sitemaptxt.jsp",
                "map", result,
                "action", "display");
    }
    
    public Render mediamap() throws Exception {
        Map<String, List<String>> mediaMap = service.getMediaMap();
        return renderView("map.jsp",
                "map", mediaMap,
                "action", "media");
    }
    
}
