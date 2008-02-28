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

import java.util.regex.Pattern;
import org.openide.util.NbBundle;

/**
 *
 * @author John Baker
 */
public class SpringWebFrameworkUtils {
    private static final String DISPATCHER_MAPPING = ".htm"; // NOI18N
    
    public static boolean isDispatcherNameValid(String name) {
        boolean isNameValid = (name.length() > 0); // an empty string for the dispatcher name is not considered invalid
        for (int charPosition = 0; charPosition < name.length(); charPosition++) {
            if (!Character.isUnicodeIdentifierPart(name.toCharArray()[charPosition])) {
                isNameValid = false;
                break;
            }
        }                
        return isNameValid;
    }
    
    public static boolean isDispatcherMappingPatternValid(String pattern){
        // mapping validation based on the Servlet 2.4 specification,section SRV.11.2
        if (pattern.startsWith("*.")){ // NOI18N
            String p = pattern.substring(2);
            if (p.indexOf('.') == -1 && p.indexOf('*') == -1  
                    && p.indexOf('/') == -1 && !p.trim().equals("") && !p.contains(" ") && Pattern.matches("\\w+",p)) { // NOI18N
                return true;
            }
        }
        
        if ((pattern.length() > 3) && pattern.endsWith("/*") && pattern.startsWith("/") && !pattern.contains(" ")) // NOI18N
            return true;
        
        if (pattern.matches("/")){ // NOI18N
            return true;
        }
               
        return false;
    }
    
    /**
     * Replace the default extension used in the Spring bean configuration file template based on the the mapping entered by the user.
     * For the extension to be replaced, this mapping must be an extension, such as *.html
     * @param line, a line of text in the Spring bean configuration template file.
     * @param dispatcherMapping, Dispatcher mapping entered by the user
     * @return line, contains the .htm extension
     */
    public static String replaceExtensionInTemplates(String lineInTemplate, String dispatcherMapping) {            
        if (lineInTemplate.contains(DISPATCHER_MAPPING) && dispatcherMapping.contains("*.")) { // NOI18N
            int indexOfExtensionInTemplate = lineInTemplate.indexOf(DISPATCHER_MAPPING);
            int lastIndexOfExtensionInTemplate = indexOfExtensionInTemplate + DISPATCHER_MAPPING.length();
            int wildCardLocation = dispatcherMapping.indexOf("*") + 1; // NOI18N
            assert (indexOfExtensionInTemplate != -1);
            assert (lastIndexOfExtensionInTemplate != -1);
            assert (wildCardLocation != -1);
            lineInTemplate = lineInTemplate.substring(0, indexOfExtensionInTemplate) + dispatcherMapping.substring(wildCardLocation) + lineInTemplate.substring(lastIndexOfExtensionInTemplate);  
        }        
        return lineInTemplate;
    }
    
    /**
     * When the dispatcher mapping is of the servlet format /app/* then the redirect jsp file also needs updating to include the relative
     * @param lineInTemplate, a line of text in the template
     * @param dispatcherMapping,  the dispatcher mapping entered by the user in the framework section
     * @return returns the revised line
     */
    public static String reviseRedirectJsp(String lineInTemplate, String dispatcherMapping) {
        if ((dispatcherMapping.length() > 3) && dispatcherMapping.endsWith("/*") && dispatcherMapping.startsWith("/") && !dispatcherMapping.contains(" ")) { // NOI18N            
            int indexOfWelcomeFile = lineInTemplate.indexOf("index.htm"); // NOI18N
            if (indexOfWelcomeFile > -1) {
                String path = dispatcherMapping.substring(1, dispatcherMapping.indexOf("*")); // NOI18N
                return lineInTemplate.substring(0, indexOfWelcomeFile) + path + lineInTemplate.substring(indexOfWelcomeFile); // NOI18N      
            }
        }

        return lineInTemplate;
    }
    
    public static String getWelcomePageText() {
        return NbBundle.getMessage(SpringWebFrameworkUtils.class, "MSG_WELCOME_PAGE_TEXT"); 
    }
}
