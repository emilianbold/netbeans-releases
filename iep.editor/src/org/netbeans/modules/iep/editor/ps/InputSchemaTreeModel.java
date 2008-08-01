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


package org.netbeans.modules.iep.editor.ps;

import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;

/**
 * InputSchemaTreeModel.java
 *
 * Created on November 1, 2006, 1:52 PM
 *
 * @author Bing Lu
 */
class InputSchemaTreeModel extends DefaultTreeModel implements SharedConstants {
    private static final Logger mLog = Logger.getLogger(InputSchemaTreeModel.class.getName());
    
    public InputSchemaTreeModel(DefaultMutableTreeNode root, IEPModel model, OperatorComponent component) {
        super(root);
        try {
            List<OperatorComponent> inputs = component.getInputOperatorList();
            Iterator<OperatorComponent> itIn = inputs.iterator();
            while(itIn.hasNext()) {
                OperatorComponent  input = itIn.next();
                if(input != null) {
                    String inputName = input.getDisplayName();
                    DefaultMutableTreeNode inputNode = new DefaultMutableTreeNode(inputName);
                    root.add(inputNode);
                    SchemaComponent outputSchema = input.getOutputSchemaId();
                    if(outputSchema != null) {
                        List<SchemaAttribute> attrs =  outputSchema.getSchemaAttributes();
                        Iterator<SchemaAttribute> attrsIt = attrs.iterator();
                        while(attrsIt.hasNext()) {
                            SchemaAttribute sa = attrsIt.next();
                            AttributeInfo ai = new AttributeInfo(inputName, sa);
                            DefaultMutableTreeNode columnNode = new DefaultMutableTreeNode(ai);
                            inputNode.add(columnNode);
                        }
                    }
                }
            }
            
            List<OperatorComponent> tableInputs = component.getStaticInputTableList();
            Iterator<OperatorComponent> tableInIt = tableInputs.iterator();
            while(tableInIt.hasNext()) {
                OperatorComponent input = tableInIt.next();
                
                if(input != null) {
                    String inputName = input.getDisplayName();
                    DefaultMutableTreeNode inputNode = new DefaultMutableTreeNode(inputName);
                    root.add(inputNode);
                    SchemaComponent outputSchema = input.getOutputSchemaId();
                    if(outputSchema != null) {
                        List<SchemaAttribute> attrs = outputSchema.getSchemaAttributes();
                        Iterator<SchemaAttribute> attrsIt = attrs.iterator();
                        while(attrsIt.hasNext()) {
                            SchemaAttribute sa = attrsIt.next();
                            AttributeInfo ai = new AttributeInfo(inputName, sa);
                            DefaultMutableTreeNode columnNode = new DefaultMutableTreeNode(ai);
                            inputNode.add(columnNode);
                        }
                        
                    }
                }
                
            }
            
            
            /*
            List inputIdList = component.getProperty(INPUT_ID_LIST_KEY).getListValue();
            for(int i = 0, I = inputIdList.size(); i < I; i++) {
                String id = (String)inputIdList.get(i);
                TcgComponent input = plan.getOperatorById(id);
                if(input != null) {
                    String inputName = input.getProperty(NAME_KEY).getStringValue();
                    DefaultMutableTreeNode inputNode = new DefaultMutableTreeNode(inputName);
                    root.add(inputNode);
                    String outputSchemaId = input.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
                    Schema schema = plan.getSchema(outputSchemaId);
                    if(schema != null) {
                        int j = 0;
                        for(int J = schema.getAttributeCount(); j < J; j++) {
                            org.netbeans.modules.iep.editor.model.AttributeMetadata cm = schema.getAttributeMetadata(j);
                            AttributeInfo ai = new AttributeInfo(inputName, cm);
                            DefaultMutableTreeNode columnNode = new DefaultMutableTreeNode(ai);
                            inputNode.add(columnNode);
                        }
                        
                    }
                }
            }
            List staticInputIdList = component.getProperty(STATIC_INPUT_ID_LIST_KEY).getListValue();
            for(int i = 0, I = staticInputIdList.size(); i < I; i++) {
                String id = (String)staticInputIdList.get(i);
                TcgComponent input = plan.getOperatorById(id);
                if(input != null) {
                    String inputName = input.getProperty(NAME_KEY).getStringValue();
                    DefaultMutableTreeNode inputNode = new DefaultMutableTreeNode(inputName);
                    root.add(inputNode);
                    String outputSchemaId = input.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
                    Schema schema = plan.getSchema(outputSchemaId);
                    if(schema != null) {
                        int j = 0;
                        for(int J = schema.getAttributeCount(); j < J; j++) {
                            org.netbeans.modules.iep.editor.model.AttributeMetadata cm = schema.getAttributeMetadata(j);
                            AttributeInfo ai = new AttributeInfo(inputName, cm);
                            DefaultMutableTreeNode columnNode = new DefaultMutableTreeNode(ai);
                            inputNode.add(columnNode);
                        }
                        
                    }
                }
            }
            */
        } catch(Exception e) {
            mLog.log(Level.SEVERE, NbBundle.getMessage(InputSchemaTreeModel.class, 
                    "InputSchemaTreeModel.FAIL_TO_BUILD_TREE_MODEL_FOR", component.getTitle()), e);
        }
    }
    
}