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

    public void testDispatcherNameEntry_NonWordCharacterPattern() throws Exception {       
        assert(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dis*patcher") == false);
    }
    
//    public void testDispatcherNameEntry_EmptyWordCharacterPattern() throws Exception {
//        assert(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("") == false);
//    }
    
    public void testDispatcherNameEntry_ValidPattern() throws Exception {
        assert(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dispatcher") == true);
    }  
    
    public void testDispatcherNameEntry_ValidAmpersandPattern() throws Exception {
        assert(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dispatcher&amp;") == true);
    } 
         
    public void testDispatcherNameEntry_NonWordUnicodeCharacterPattern() throws Exception {       
        assert(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("あｂ３＿:え") == false);  // ^ is the invalid character
    }        
    
    public void testDispatcherNameEntry_ValidUnicodePattern() throws Exception {
        assert(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("あおうえｂ３＿え") == true);
    }  
    public void testDispatcherMappingEntry_ExtensionSpacePattern() throws Exception {
        assert(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("*.h tm") == false);
    }
    
    public void testDispatcherMappingEntry_ExtensionNonWordPattern() throws Exception {
        assert(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("*.h&tm") == false);
    }
    
    public void testDispatcherMappingEntry_ServletSpacePattern() throws Exception {
        assert(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/a /*") == false);
    }
    
    public void testDispatcherMappingEntry_PathSpacePattern() throws Exception {
        assert(SpringWebFrameworkUtils.isDispatcherMappingPatternValid(" /") == false);
    }
    
    public void testDispatcherMappingEntry_InvalidExtensionPattern() throws Exception {
        assert(SpringWebFrameworkUtils.isDispatcherMappingPatternValid( "*.") == false);
    }       
     
    public void testDispatcherMappingEntry_InvalidPathPattern() throws Exception {
        assert(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/a") == false);
    }
    
    public void testDispatcherMappingEntry_ValidPathPattern() throws Exception {
        assert(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/") == true);
    }
    
    public void testDispatcherMappingEntry_InvalidDefaultServletPattern() throws Exception {
        assert(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/a*/") == false);
    }
    
    public void testDispatcherMappingEntry_ValidDefaultServletPattern() throws Exception {
        assert(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/app/*") == true);
    }
        
    public void testDispatcherMappingEntry_ValidPattern() throws Exception {
        assert(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("*.htm") == true);
    }                   
    
    public void testReplaceExtensionInTemplatesExtensionPattern_Positive() throws Exception {      
        assert ("index.html".equals(SpringWebFrameworkUtils.replaceExtensionInTemplates("index.htm", "*.html")) == true);
    }

    public void testReplaceExtensionInTemplatesServletPattern_Positive() throws Exception {
        assert ("index.htm".equals(SpringWebFrameworkUtils.replaceExtensionInTemplates("index.htm", "/app/*")) == true);
    }

    public void testReplaceExtensionInTemplates_PathPattern_Positive() throws Exception {
        assert ("index.htm".equals(SpringWebFrameworkUtils.replaceExtensionInTemplates("index.htm", "/")) == true);
    }
    
    public void testReplaceExtensionInTemplatesExtensionPattern_Negative() throws Exception {
        assert ("index.htm".equals(SpringWebFrameworkUtils.replaceExtensionInTemplates("index.htm", "*.xml")) == false);
    }

    public void testReplaceExtensionInTemplatesServletPattern_Negative() throws Exception {
        assert ("index/app/*".equals(SpringWebFrameworkUtils.replaceExtensionInTemplates("index.htm", "/app/*")) == false);
    }

    public void testReplaceExtensionInTemplates_PathPattern_Negative() throws Exception {
        assert ("index/".equals(SpringWebFrameworkUtils.replaceExtensionInTemplates("index.htm", "/")) == false);
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
