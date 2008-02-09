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

package org.netbeans.modules.spring.webmvc;

import java.util.regex.Pattern;

/**
 *
 * @author John Baker
 */
public class SpringWebFrameworkValidator {
    
    public static boolean isDispatcherNameValid(String name) {
        return Pattern.matches("\\w+", name);
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
}
