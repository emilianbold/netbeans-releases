/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
