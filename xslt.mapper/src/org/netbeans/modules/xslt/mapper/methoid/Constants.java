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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xslt.mapper.methoid;

/**
 *
 * @author nk160297
 */
public interface Constants {

    String XSLT_PALETTE_FOLDER = "XsltPalette"; // NOI18N
    String XSLT_PALETTE_METAINFO = "XPathMetainfo"; // NOI18N
    String METAINFO_REF = "metainfo"; // NOI18N
   
    String XPATH_OPERATOR = "Operator";    // NOI18N
    String XPATH_MAXINPUT = "InputNum";    // NOI18N
    String XPATH_FUNCTION = "Function";    // NOI18N
    String XPATH_BOOLEAN = "Boolean";      // NOI18N
    String XPATH_NUMBER = "Number";        // NOI18N
    String XPATH_STRING = "String";        // NOI18N
    
    String CATEGORY_ICON = "Icon"; // NOI18N
    
    String LITERAL_FLAG = "EditableLiteral"; // NOI18N
    
    String INPUT_PARAM = "InputParam";  // NOI18N
    String INPUT_TYPE = "InputType";  // NOI18N
    String INPUT_TOOLTIP = "InputTooltip";  // NOI18N

    String INPUT_THIS = "InputThis";  // NOI18N
    String THIS_CLASS = "Class";  // NOI18N
    String THIS_TOOLTIP = "InputThisTooltip";  // NOI18N
//    String  = "";  // NOI18N

    
    
    String OUTPUT_PARAM = "OutputParam";  // NOI18N
    String OUTPUT_TYPE = "OutputType";  // NOI18N
    String OUTPUT_TOOLTIP = "OutputTooltip";  // NOI18N
    String OUTPUT_NUM = "OutputNum";  // NOI18N
    
    String ACCUMULATIVE = "Accumulative";  // NOI18N
    String TOOLTIP = "Tooltip";  // NOI18N
    String LOCAL_NAME = "LocalName";  // NOI18N
    
    String BUNDLE_CLASS = "SystemFileSystem.localizingBundle"; // NOI18N
    String FILE_ICON = "SystemFileSystem.icon"; // NOI18N

    String STRING_LITERAL = "string-literal";
    String NUMBER_LITERAL = "number-literal";
    String DURATION_LITERAL = "duration-literal";
    String XPATH_LITERAL = "xpath_expression";
    
    /**
     * The special constant to identify the Predicate element.
     */
    String IS_PREDICATE = "IsPredicate"; // NOI18N
    String PREDICATE_MAIN_INPUT_TYPE = "node-set"; // NOI18N
    
    
    enum LiteralType {
        NUMBER_LITERAL_TYPE("number"),
        STRING_LITERAL_TYPE("string"), 
        XPATH_LITERAL_TYPE("xpath");
        
        private String myName;
        
        private LiteralType(String name) {
            myName = name;
        }
        
        public String getName() {
            return myName;
        }
        
        public static LiteralType findByName(String name) {
            if (name == null || name.length() == 0) {
                return null;
            }
            //
            for (LiteralType type : values()) {
                if (type.getName().equals(name)) {
                    return type;
                }
            }
            //
            return null;
        }
    }
}
