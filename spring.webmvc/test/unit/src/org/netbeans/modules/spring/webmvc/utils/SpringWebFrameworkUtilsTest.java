/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.spring.webmvc.utils;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author John Baker
 */
public class SpringWebFrameworkUtilsTest extends NbTestCase {
    
    public SpringWebFrameworkUtilsTest(String testName) {
        super(testName);
    }

    public void testDispatcherName_NonWordCharacterPattern() throws Exception {       
        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dis*patcher"));
    }
    
//    public void testDispatcherName_EmptyWordCharacterPattern() throws Exception {
//        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid(""));
//    }
    
    public void testDispatcherName_ValidPattern() throws Exception {
        assertTrue(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dispatcher"));
    }  
    
    public void testDispatcherName_ValidAmpersandPattern() throws Exception {
        assertTrue(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dispatcher&amp;"));
    } 
         
    public void testDispatcherName_NonWordUnicodeCharacterPattern() throws Exception {       
        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("あｂ３＿:え"));  // ^ is the invalid character
    }        
    
    public void testDispatcherName_ValidUnicodePattern() throws Exception {
        assertTrue(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("あおうえｂ３＿え"));
    }  
    public void testDispatcherMapping_ExtensionSpacePattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("*.h tm"));
    }
    
    public void testDispatcherMapping_ExtensionNonWordPattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("*.h&tm"));
    }
    
    public void testDispatcherMapping_ServletSpacePattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/a /*"));
    }
    
    public void testDispatcherMapping_PathSpacePattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid(" /"));
    }
    
    public void testDispatcherMapping_InvalidExtensionPattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid( "*."));
    }       
     
    public void testDispatcherMapping_InvalidPathPattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/a"));
    }
    
    public void testDispatcherMapping_ValidPathPattern() throws Exception {
        assertTrue(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/"));
    }
    
    public void testDispatcherMapping_InvalidDefaultServletPattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/a*/"));
    }
    
    public void testDispatcherMapping_ValidDefaultServletPattern() throws Exception {
        assertTrue(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/app/*"));
    }
        
    public void testDispatcherMapping_ValidPattern() throws Exception {
        assertTrue(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("*.htm"));
    }                   
    
    public void testReplaceExtensionInTemplatesExtensionPattern_Positive() throws Exception {      
        assertEquals("index.html", SpringWebFrameworkUtils.replaceExtensionInTemplates("index.htm", "*.html"));
    }

    public void testReplaceExtensionInTemplatesServletPattern_Positive() throws Exception {
        assertEquals("index.htm", SpringWebFrameworkUtils.replaceExtensionInTemplates("index.htm", "/app/*"));
    }

    public void testReplaceExtensionInTemplates_PathPattern_Positive() throws Exception {
        assertEquals("index.htm", SpringWebFrameworkUtils.replaceExtensionInTemplates("index.htm", "/"));
    }
    
    public void testReplaceExtensionInTemplatesExtensionPattern_Negative() throws Exception {
        assertEquals("index.xml", SpringWebFrameworkUtils.replaceExtensionInTemplates("index.htm", "*.xml"));
    }

    public void testReplaceExtensionInTemplatesServletPattern_Negative() throws Exception {
        assertEquals("index.htm", SpringWebFrameworkUtils.replaceExtensionInTemplates("index.htm", "/app/*"));
    }

    public void testReplaceExtensionInTemplates_PathPattern_Negative() throws Exception {
        assertEquals("index.htm", SpringWebFrameworkUtils.replaceExtensionInTemplates("index.htm", "/"));
    }    
    
    public void testReviseRedirectJsp_Servlet() throws Exception {
        System.out.println("resultant line = " + SpringWebFrameworkUtils.reviseRedirectJsp("<% response.sendRedirect(\"index.htm\"); %>", "/app/*"));
        assertTrue ("<% response.sendRedirect(\"app/index.htm\"); %>".equals(SpringWebFrameworkUtils.reviseRedirectJsp("<% response.sendRedirect(\"index.htm\"); %>", "/app/*")));
    }
    
    public void testReviseRedirectJsp_Path() throws Exception {
        assertFalse ("<% response.sendRedirect(\"app/index.htm\"); %>".equals(SpringWebFrameworkUtils.reviseRedirectJsp("<% response.sendRedirect(\"index.htm\"); %>", "/")));
    }
    
    public void testReviseRedirectJsp_Extension() throws Exception {
        assertFalse ("<% response.sendRedirect(\"app/index.htm\"); %>".equals(SpringWebFrameworkUtils.reviseRedirectJsp("<% response.sendRedirect(\"index.htm\"); %>", "*.htm")));
    }
    
    public void testReviseRedirectJsp_WrongLine() throws Exception {
        assertFalse ("<% response.sendRedirect(\"app/index.htm\"); %>".equals(SpringWebFrameworkUtils.reviseRedirectJsp("Foo", "/app/*")));
    }
}
