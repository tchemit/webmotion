/*
 * #%L
 * Webmotion server
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 - 2015 Debux
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
package org.debux.webmotion.server;

import java.lang.reflect.Method;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.debux.webmotion.server.call.Call;
import org.debux.webmotion.server.call.Executor;
import org.debux.webmotion.server.call.HttpContext;
import org.debux.webmotion.server.call.ServerContext;
import org.debux.webmotion.server.handler.ExecutorMethodInvokerHandler.RunnableHandler;
import org.debux.webmotion.server.mapping.Config;
import org.debux.webmotion.server.mapping.Mapping;
import org.debux.webmotion.server.mapping.Rule;
import org.debux.webmotion.server.tools.ReflectionUtils;

/**
 * This classe is an action exectuted before and/or after the main action. If
 * the filter method returns a render, the execution will be stopped and the render will be
 * displayed. If the method doProcess is call, the current process will continue.
 * 
 * @author jruchaud
 */
public class WebMotionFilter extends WebMotionController {
    
    /**
     * Default constructor
     */
    public WebMotionFilter() {
    }

    /**
     * Call this method to continue the current execution.
     */
    public void doProcess() {
        Mapping mapping = contextable.getMapping();
        Call call = contextable.getCall();
        doProcess(mapping, call);
    }
    
    /**
     * Call this method to continue the current execution to passing a wrapper 
     * on mapping and call. You can use basic wrapper classes @see MappingWrapper 
     * and @see CallWrapper.
     * @param mapping wrapper on mapping
     * @param call wrapper on call
     */
    public void doProcess(Mapping mapping, Call call) {
        WebMotionHandler handler = contextable.getHandler();
        handler.handle(mapping, call);
    }
    
    /**
     * Chain on filter. The filter is passed with the class name and the method 
     * name separated by point. The doChain method replace the doProcess method 
     * in this case.
     * @param filter the filter to call
     */
    public void doChain(String filter) {
        Mapping mapping = contextable.getMapping();
        Call call = contextable.getCall();
        Rule currentRule = call.getCurrentRule();
        
        HttpContext context = call.getContext();
        ServerContext serverContext = context.getServerContext();
        Map<String, Class<? extends WebMotionController>> globalControllers = serverContext.getGlobalControllers();
        
        String className = StringUtils.substringBeforeLast(filter, ".");
        String methodName = StringUtils.substringAfterLast(filter, ".");
        
        Config config = mapping.getConfig();
        String packageName = config.getPackageActions();
        String fullQualifiedName = null;
        if (packageName == null || packageName.isEmpty()) {
            fullQualifiedName = className;
        } else {
            fullQualifiedName = packageName + "." + className;
        }
        
        try {
            Class<? extends WebMotionController> clazz = globalControllers.get(className);
            if (clazz == null) {
                clazz = (Class<WebMotionController>) Class.forName(fullQualifiedName);
            }

            Method method = ReflectionUtils.getMethod(clazz, methodName);
            if (method == null) {
                throw new WebMotionException("Method not found with name " + methodName + " on class " + fullQualifiedName, currentRule);
            }

            Executor executor = new Executor();
            executor.setClazz(clazz);
            executor.setMethod(method);
            executor.setRule(currentRule);
            
            RunnableHandler handler = (RunnableHandler) contextable.getHandler();
            handler.chainFilter(executor);
            
        } catch (ClassNotFoundException clnfe) {
            throw new WebMotionException("Class not found with name " + fullQualifiedName, clnfe, currentRule);
        }
    }
    
    /**
     * Shortcut to get parameters for action returned by {@see #getAction()} method.
     * @return current parameters for current action
     */
    public Map<String, Object> getParameters() {
        Call call = contextable.getCall();
        Executor executor = call.getExecutor();
        if(executor != null) {
            Map<String, Object> parameters = executor.getParameters();
            return parameters;
        } else {
            return null;
        }
    }

    /**
     * Get the current action, for example you can change parameter to call 
     * action.
     * 
     * @return current action
     */
    public Executor getAction() {
        Call call = contextable.getCall();
        Executor executor = call.getExecutor();
        return executor;
    }
    
}
