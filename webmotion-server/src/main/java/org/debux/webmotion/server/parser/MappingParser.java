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

import java.io.InputStream;
import org.debux.webmotion.server.mapping.Mapping;

/**
 * The interface represents to how parse the mapping file.
 * 
 * @author jruchaud
 */
public interface MappingParser {
    
    /** The absolute path to the mapping file */
    public static String MAPPING_FILE_NAME = "/mapping";
    
    /**
     * Parse a mapping file
     * @param stream mapping file to parse
     * @return the representation of the file
     */
    Mapping parse(InputStream stream);
    
}