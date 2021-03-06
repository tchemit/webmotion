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
package org.debux.webmotion.server.handler;

import org.debux.webmotion.server.call.Call;
import org.debux.webmotion.server.render.Render;
import org.debux.webmotion.server.mapping.Config;
import org.debux.webmotion.server.mapping.Mapping;
import org.debux.webmotion.server.mapping.Action;
import java.lang.reflect.Method;
import java.util.Map;
import org.debux.webmotion.server.WebMotionController;
import org.debux.webmotion.server.WebMotionException;
import org.debux.webmotion.server.WebMotionHandler;
import org.debux.webmotion.server.tools.HttpUtils;
import org.debux.webmotion.server.call.ServerContext;
import org.debux.webmotion.server.call.Executor;
import org.debux.webmotion.server.mapping.Rule;
import org.debux.webmotion.server.tools.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Find the action class reprensent by name given in mapping. If it directly 
 * mapped on view or url, the executor is null but the render is informed.
 * 
 * You can add global controller in server context.
 * 
 * @author julien
 */
public class ActionMethodFinderHandler extends AbstractHandler implements WebMotionHandler {

    private static final Logger log = LoggerFactory.getLogger(ActionMethodFinderHandler.class);

    /** Global action in server context */
    protected Map<String, Class<? extends WebMotionController>> globalControllers;
    
    @Override
    public void handlerCreated(Mapping mapping, ServerContext context) {
        globalControllers = context.getGlobalControllers();
    }

    @Override
    public void handle(Mapping mapping, Call call) {
        Render render = call.getRender();
        Rule rule = call.getRule();
        // Test if it directly mapped on view or urlor not action found in extension
        if (render == null && rule != null) {
            
            Map<String, Object> rawParameters = call.getRawParameters();
            Action action = rule.getAction();

            String className = action.getClassName();
            className = HttpUtils.replaceDynamicName(className, rawParameters);
            className = ReflectionUtils.capitalizeClass(className);

            Config config = mapping.getConfig();
            String packageName = config.getPackageActions();
            
            String fullQualifiedName =  null;
            if(packageName == null || packageName.isEmpty()) {
                fullQualifiedName =  className;
            } else {
                fullQualifiedName = packageName + "." + className;
            }

            try {
                Class<? extends WebMotionController> clazz = globalControllers.get(className);
                if (clazz == null) {
                    clazz = (Class<WebMotionController>) Class.forName(fullQualifiedName);
                }

                String methodName = action.getMethodName();
                methodName = HttpUtils.replaceDynamicName(methodName, rawParameters);
                Method method = ReflectionUtils.getMethod(clazz, methodName);
                if (method == null) {
                    throw new WebMotionException("Method not found with name " + methodName + " on class " + fullQualifiedName, rule);
                }

                Executor executor = new Executor();
                executor.setClazz(clazz);
                executor.setMethod(method);
                executor.setRule(rule);
                call.setExecutor(executor);

            } catch (ClassNotFoundException clnfe) {
                throw new WebMotionException("Class not found with name " + fullQualifiedName, clnfe, rule);
            }
        }
    }

}
