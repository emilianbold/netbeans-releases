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
package org.netbeans.modules.bpel.properties;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.editors.api.BpelEditorConstants;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.nodes.GlobalComplexTypeNode;
import org.netbeans.modules.bpel.nodes.GlobalElementNode;
import org.netbeans.modules.bpel.nodes.GlobalSimpleTypeNode;
import org.netbeans.modules.bpel.nodes.MessageTypeNode;
import org.netbeans.modules.bpel.nodes.PrimitiveTypeNode;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Keeps constants for identification of the BPEL Diagram elements and theirs properties.
 *
 * @author nk160297
 */
public interface Constants extends BpelEditorConstants {
    
    StereotypeFilter CORRELATION_PROPERTY_STEREO_TYPE_FILTER 
            = new StereotypeFilter(/*VariableStereotype.GLOBAL_TYPE
                        , VariableStereotype.GLOBAL_SIMPLE_TYPE
                        , VariableStereotype.GLOBAL_COMPLEX_TYPE
                        , VariableStereotype.PRIMITIVE_TYPE
                        , VariableStereotype.GLOBAL_ELEMENT*/
                        VariableStereotype.PRIMITIVE_TYPE
            );
    
    enum StandardImportType {
        IMPORT_WSDL("wsdl", Import.WSDL_IMPORT_TYPE),  // NOI18N
        IMPORT_SCHEMA("xsd", Import.SCHEMA_IMPORT_TYPE),  // NOI18N
        IMPORT_UNKNOWN(null, null);
        
        private String importType;
        private String fileExt;
        
        StandardImportType(String fileExt, String importType) {
            this.fileExt = fileExt;
            this.importType = importType;
        }
        
        public static StandardImportType forName(String soughtTypeName) {
            if (soughtTypeName != null && soughtTypeName.length() > 0) {
                for (StandardImportType importObj : values()) {
                    String typeName = importObj.getImportType();
                    if (soughtTypeName.equals(typeName)) {
                        return importObj;
                    }
                }
            }
            //
            return IMPORT_UNKNOWN;
        }
        
        public static StandardImportType forExt(String fileExt) {
            if (fileExt != null && fileExt.length() > 0) {
                for (StandardImportType importObj : values()) {
                    String ext = importObj.getFileExt();
                    if (fileExt.equals(ext)) {
                        return importObj;
                    }
                }
            }
            //
            return IMPORT_UNKNOWN;
        }
        
        public String getImportType() {
            return importType;
        }
        
        public String getFileExt() {
            return fileExt;
        }
        
    }
    
    enum PropertiesGroups {
        MAIN_SET,
        MESSAGE_SET,
        FAULT_SET, 
        EXPERT_SET;
        
        private String myDisplayName;
        
        public String getDisplayName() {
            if (myDisplayName == null) {
                myDisplayName = NbBundle.getMessage(
                        PropertyType.class, this.toString());
            }
            return myDisplayName;
        }
    }
    
    enum MessageDirection {
        INPUT, 
        OUTPUT, 
        FAULT;
    }
    
    enum VariableStereotype {
        GLOBAL_TYPE,
        GLOBAL_SIMPLE_TYPE,
        GLOBAL_COMPLEX_TYPE,
        PRIMITIVE_TYPE,
        MESSAGE,
        GLOBAL_ELEMENT;
        
        private String myDisplayName;
        
        public String getDisplayName() {
            if (myDisplayName == null) {
                myDisplayName = NbBundle.getMessage(
                        PropertyType.class, this.toString());
            }
            return myDisplayName;
        }
        
        public static VariableStereotype recognizeStereotype(Object type) {
            if (type == null) {
                return null;
            }
            if (type instanceof Message) {
                return VariableStereotype.MESSAGE;
            } else if (type instanceof GlobalType) {
                if (type instanceof GlobalSimpleType) {
                    if (((GlobalSimpleType)type).getModel() == 
                            SchemaModelFactory.getDefault().
                            getPrimitiveTypesModel()) {
                        return PRIMITIVE_TYPE;
                    } else {
                        return GLOBAL_SIMPLE_TYPE;
                    }
                } else if (type instanceof GlobalComplexType) {
                    return GLOBAL_COMPLEX_TYPE;
                } else {
                    return VariableStereotype.GLOBAL_TYPE;
                }
            } else if (type instanceof GlobalElement) {
                return VariableStereotype.GLOBAL_ELEMENT;
            }
            assert false : "type paramether can be one of the following types" +
                    "(or subtype of them): Message, Part, GlobalType, GlobalElement";
            return null;
        }
    }
    
    class StereotypeFilter {
        
        public static StereotypeFilter ALL = new StereotypeFilter() {
            
            public VariableStereotype[] getAllowedStereotypes() {
                return VariableStereotype.values();
            }
            
            public boolean isStereotypeAllowed(VariableStereotype soughtStereotype) {
                return true;
            }
            
            public boolean isSingleStereotype() {
                return false;
            }
        };
        
        private VariableStereotype[] myAllowedStereotypes;
        
        private StereotypeFilter() {
        }
        
        public StereotypeFilter(VariableStereotype... allowedStereotypes) {
            myAllowedStereotypes = allowedStereotypes;
        }
        
        public VariableStereotype[] getAllowedStereotypes() {
            return myAllowedStereotypes;
        }
        
        public boolean isStereotypeAllowed(VariableStereotype soughtStereotype) {
            for (VariableStereotype stereotype : myAllowedStereotypes) {
                if (stereotype == soughtStereotype) {
                    return true;
                }
            }
            //
            return false;
        }
        
        public boolean isSingleStereotype() {
            return myAllowedStereotypes.length == 1;
        }
        
        public Class<? extends Node>[] getTargetNodeClasses() {
            VariableStereotype[] sTypeArr = getAllowedStereotypes();
            List<Class<? extends Node>> nodeClasses =
                    new ArrayList<Class<? extends Node>>();
            for (VariableStereotype sTape : sTypeArr) {
                switch (sTape) {
                    case MESSAGE:
                        nodeClasses.add(MessageTypeNode.class);
                        break;
                    case GLOBAL_ELEMENT:
                        nodeClasses.add(GlobalElementNode.class);
                        break;
                    case GLOBAL_TYPE:
                        // nodeClasses.add(PrimitiveSimpleTypesNode.class);
                        nodeClasses.add(GlobalSimpleTypeNode.class);
                        nodeClasses.add(GlobalComplexTypeNode.class);
                        break;
                    case PRIMITIVE_TYPE:
                        nodeClasses.add(PrimitiveTypeNode.class);
                }
            }
            //
            return nodeClasses.toArray(new Class[nodeClasses.size()]);
        }
        
        public ArrayList<Class<? extends SchemaComponent>> constructSchemaFilter() {
            VariableStereotype[] sTypeArr = getAllowedStereotypes();
            ArrayList<Class<? extends SchemaComponent>> result =
                    new ArrayList<Class<? extends SchemaComponent>>();
            for (VariableStereotype sTape : sTypeArr) {
                switch (sTape) {
                    case MESSAGE:
                        break;
                    case GLOBAL_ELEMENT:
                        result.add(GlobalElement.class);
                        break;
                    case GLOBAL_TYPE:
                        result.add(GlobalSimpleType.class);
                        result.add(GlobalComplexType.class);
                        break;
                }
            }
            //
            return result.size() == 0 ? null : result;
        }
        
//        ArrayList<Class<? extends SchemaComponent>> childTypes =
//                new ArrayList<Class<? extends SchemaComponent>>();
//        childTypes.add(GlobalSimpleType.class);
//        childTypes.add(SchemaModelReference.class);
        
    }
    
    enum CopyType {
        FROM_EXPRESSION,
        MESSAGE_VARIABLE,
        TO_QUERY,
        PROPERTY,
        PARTNER_LINK;
        
        private String myDisplayName;
        
        public String getDisplayName() {
            if (myDisplayName == null) {
                myDisplayName = NbBundle.getMessage(
                        FormBundle.class, this.toString());
            }
            return myDisplayName;
        }
    }
    
}
