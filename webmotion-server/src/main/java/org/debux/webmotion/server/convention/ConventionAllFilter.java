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
package org.debux.webmotion.server.convention;

import org.debux.webmotion.server.WebMotionFilter;
import org.debux.webmotion.server.render.Render;

/**
 * This class is used to indentify the filters which are handle by convention.
 * The filter match all url path. You have to implement an filter method.
 * 
 * @author julien
 */
public class ConventionAllFilter extends WebMotionFilter {
    
    /**
     * Redefine this method or create a new method with your parameters.
     * @return render returns by the filter otherwise null to return the render from
     * the action.
     */
    public Render filter() {
        return null;
    }
    
}
