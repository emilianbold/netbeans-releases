/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.properties;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.nodes.MessagePartNode;
import org.netbeans.modules.bpel.nodes.MessageTypeNode;
import org.netbeans.modules.bpel.nodes.PrimitiveTypeNode;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
//import org.netbeans.modules.xml.schema.ui.nodes.schema.GlobalComplexTypeNode;
//import org.netbeans.modules.xml.schema.ui.nodes.schema.GlobalElementNode;
//import org.netbeans.modules.xml.schema.ui.nodes.schema.GlobalSimpleTypeNode;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Keeps constants for identification of the BPEL Diagram elements and theirs properties.
 *
 * @author nk160297
 */
public interface Constants {
    
    String BOUNDED_ATTRIBUTE_NAME = "BOUNDED_ATTRIBUTE_NAME"; // NOI18N
    String BOUNDED_ELEMENT_CLASS = "BOUNDED_ELEMENT_CLASS"; // NOI18N
    String PROPERTY_TYPE_ATTRIBUTE = "PropertyTypeAttribute"; // NOI18N
    String PROPERTY_NODE_ATTRIBUTE = "PropertyNodeAttribute"; // NOI18N
    
    String COLON = ":"; // NOI18N
    String INVALID = NbBundle.getMessage(FormBundle.class, "LBL_Invalid"); // NOI18N
    String MISSING = NbBundle.getMessage(FormBundle.class, "LBL_Missing"); // NOI18N
    String NOT_ASSIGNED = NbBundle.getMessage(FormBundle.class, "LBL_Not_Assigned"); // NOI18N
    
    // The delay between a field input event and the start of fast validation
    int INPUT_VALIDATION_DELAY = 400;
    
    public static StereotypeFilter CORRELATION_PROPERTY_STEREO_TYPE_FILTER 
            = new StereotypeFilter(/*VariableStereotype.GLOBAL_TYPE
                        , VariableStereotype.GLOBAL_SIMPLE_TYPE
                        , VariableStereotype.GLOBAL_COMPLEX_TYPE
                        , VariableStereotype.PRIMITIVE_TYPE
                        , VariableStereotype.GLOBAL_ELEMENT*/
                        VariableStereotype.PRIMITIVE_TYPE
            );
    
    public static StereotypeFilter CORRELATION_PROPERTY_ALIAS_STEREO_TYPE_FILTER 
            = new StereotypeFilter(
                        VariableStereotype.MESSAGE,
                        VariableStereotype.MESSAGE_PART
            );
    
    enum StandardImportType {
        IMPORT_WSDL("wsdl", Import.WSDL_IMPORT_TYPE),  // NOI18N
        IMPORT_SCHEMA("xsd", Import.SCHEMA_IMPORT_TYPE),  // NOI18N
        IMPORT_UNKNOWN(null, null);
        
        private String importType;
        private String fileExt;
        private String myChooserDescription;
        private String myDisplayName;
        
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
        
        public String getDisplayName() {
            if (myDisplayName == null) {
                myDisplayName = NbBundle.getMessage(
                        PropertyType.class, this.toString());
            }
            return myDisplayName;
        }
        
        public String getChooserDescription() {
            if (myChooserDescription == null) {
                myChooserDescription = NbBundle.getMessage(
                        PropertyType.class, this.toString() + "_CHOOSER"); // NOI18N
            }
            return myChooserDescription;
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
        MESSAGE_PART,
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
            } else if (type instanceof Part) {
                return VariableStereotype.MESSAGE_PART;
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
                    case MESSAGE_PART:
                        nodeClasses.add(MessagePartNode.class);
                        break;
                    case GLOBAL_ELEMENT:
//                        nodeClasses.add(GlobalElementNode.class);
                        break;
                    case GLOBAL_TYPE:
                        // nodeClasses.add(PrimitiveSimpleTypesNode.class);
//                        nodeClasses.add(GlobalSimpleTypeNode.class);
//                        nodeClasses.add(GlobalComplexTypeNode.class);
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
    
    enum AlarmType {
        INVALID(Constants.INVALID),
        NOT_ASSIGNED(Constants.NOT_ASSIGNED),
        FOR_TIME,
        UNTIL_TIME;
        
        private String myDisplayName;
        
        private AlarmType() {
        }
        
        private AlarmType(String displayName) {
            myDisplayName = displayName;
        }
        
        public String getDisplayName() {
            if (myDisplayName == null) {
                myDisplayName = NbBundle.getMessage(
                        FormBundle.class, this.toString());
            }
            return myDisplayName;
        }
        
        /**
         * Returns enumeration via its string representation.
         * @param str string that represent enumeration.
         * @return enumeration.
         */
        public static AlarmType forString(String str) {
            if ( str == null || str.length() == 0){
                return AlarmType.NOT_ASSIGNED;
            }
            AlarmType[] values = AlarmType.values();
            for (AlarmType alarmPickType : values) {
                if (alarmPickType.getDisplayName().equals(str)) {
                    return alarmPickType;
                }
            }
            return AlarmType.INVALID;
        }
    }
    
    enum AlarmEventType {
        INVALID(Constants.INVALID),
        NOT_ASSIGNED(Constants.NOT_ASSIGNED),
        FOR_TIME,
        UNTIL_TIME,
        REPEAT_TIME,
        FOR_REPEAT_TIME,
        UNTIL_REPEAT_TIME;
        
        private String myDisplayName;
        
        private AlarmEventType() {
        }
        
        private AlarmEventType(String displayName) {
            myDisplayName = displayName;
        }
        
        public String getDisplayName() {
            if (myDisplayName == null) {
                myDisplayName = NbBundle.getMessage(
                        FormBundle.class, this.toString());
            }
            return myDisplayName;
        }
        
        /**
         * Returns enumeration via its string representation.
         * @param str string that represent enumeration.
         * @return enumeration.
         */
        public static AlarmEventType forString(String str) {
            if ( str == null || str.length() == 0){
                return AlarmEventType.NOT_ASSIGNED;
            }
            AlarmEventType[] values = AlarmEventType.values();
            for (AlarmEventType alarmEventType : values) {
                if (alarmEventType.getDisplayName().equals(str)) {
                    return alarmEventType;
                }
            }
            return AlarmEventType.INVALID;
        }
    }
}
