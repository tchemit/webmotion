/*
 * #%L
 * Webmotion server
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 - 2012 Debux
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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.debux.webmotion.server.call.ServerContext;

/**
 * Listener use to initialized the WebMotion ServerContext.
 * 
 * @author julien
 */
public class WebMotionServletContextListener implements ServletContextListener {

    /** Current application context */
    protected ServerContext serverContext;
        
    /** Filter parameter to configure mapping file name by default is mapping */
    protected final static String PARAM_MAPPING_FILE_NAME = "wm.mapping.file.name";
            
    /** Filter parameter to configure excludes path for WebMotion. The value is separated by comma */
    protected final static String PARAM_EXCLUDE_PATHS = "wm.exclude.paths";
            
    @Override
    public void contextInitialized(ServletContextEvent event) {
        serverContext = new ServerContext();
        
        // Get file name mapping in context param
        ServletContext servletContext = event.getServletContext();
        String mappingFileNameParam = servletContext.getInitParameter(PARAM_MAPPING_FILE_NAME);
        if (mappingFileNameParam != null && !mappingFileNameParam.isEmpty()) {
            serverContext.setMappingFileName(mappingFileNameParam);
        }
        
        // Get exclude path in context param
        String excludePathsParam = servletContext.getInitParameter(PARAM_EXCLUDE_PATHS);
        if (excludePathsParam != null && !excludePathsParam.isEmpty()) {
            serverContext.setExcludePaths(excludePathsParam.split(","));
        } else {
             serverContext.setExcludePaths(new String[]{});
        }
        
        serverContext.contextInitialized(servletContext);
        servletContext.setAttribute(ServerContext.ATTRIBUTES_SERVER_CONTEXT, serverContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        serverContext.contextDestroyed();
    }
    
}