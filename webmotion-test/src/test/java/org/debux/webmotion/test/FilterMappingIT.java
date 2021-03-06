/*
 * #%L
 * Webmotion in action
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
package org.debux.webmotion.test;

import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.lang3.SystemUtils;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Test on mapping section filter.
 * 
 * @author julien
 */
public class FilterMappingIT extends AbstractIT {

    private static final Logger log = LoggerFactory.getLogger(FilterMappingIT.class);
    
    @Test
    public void doProcess() throws IOException, URISyntaxException {
        Request request = createRequest("/path/log")
                .addParameter("value", "42")
                .Get();
        
        String result = executeRequest(request);
        AssertJUnit.assertTrue(result, result.contains(
                "value = 42" + SystemUtils.LINE_SEPARATOR + 
                "Before filter" + SystemUtils.LINE_SEPARATOR + 
                "Action log" + SystemUtils.LINE_SEPARATOR + 
                "Action render\n" + 
                "After filter"));
    }
    
    @Test
    public void doChain() throws IOException, URISyntaxException {
        Request request = createRequest("/chain/log")
                .addParameter("value", "42")
                .Get();
        
        String result = executeRequest(request);
        AssertJUnit.assertTrue(result, result.contains(
                "Pass value = 42" + SystemUtils.LINE_SEPARATOR + 
                "Before pass" + SystemUtils.LINE_SEPARATOR + 
                "Chain value = 42" + SystemUtils.LINE_SEPARATOR + 
                "Before chain" + SystemUtils.LINE_SEPARATOR + 
                "Action log" + SystemUtils.LINE_SEPARATOR + 
                "Action render" + SystemUtils.LINE_SEPARATOR + 
                "After chain" + SystemUtils.LINE_SEPARATOR + 
                "After pass"));
    }
    
    @Test
    public void render() throws IOException, URISyntaxException {
        Request request = createRequest("/other/repeat")
                .addParameter("number", "6")
                .Get();
        
        String result = executeRequest(request);
        AssertJUnit.assertTrue(result, result.contains("run : 0\nrun : 1\nrun : 2\nrun : 3\nrun : 4\nrun : 5"));
    }
    
    @Test
    public void renderInvalid() throws IOException, URISyntaxException {
        Request request = createRequest("/other/repeat")
                .addParameter("number", "3")
                .Get();
        
        String result = executeRequest(request);
        AssertJUnit.assertTrue(result, result.contains("Invalid number 3"));
    }
    
    @Test
    public void defaultParametersContact() throws IOException, URISyntaxException {
        Request request = createRequest("/contact/view")
                .Get();
        
        String result = executeRequest(request);
        AssertJUnit.assertTrue(result, result.contains("With slot contact"));
    }
    
    @Test
    public void defaultParametersCompany() throws IOException, URISyntaxException {
        Request request = createRequest("/company/view")
                .Get();
        
        String result = executeRequest(request);
        AssertJUnit.assertTrue(result, result.contains("With slot company"));
    }
    
}
