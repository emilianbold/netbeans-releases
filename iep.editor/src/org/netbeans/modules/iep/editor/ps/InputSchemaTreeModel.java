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


package org.netbeans.modules.iep.editor.ps;

import org.netbeans.modules.iep.editor.model.Plan;
import org.netbeans.modules.iep.editor.model.Schema;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
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
    
    public InputSchemaTreeModel(DefaultMutableTreeNode root, Plan plan, TcgComponent component) {
        super(root);
        try {
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
            
        } catch(Exception e) {
            mLog.log(Level.SEVERE, NbBundle.getMessage(InputSchemaTreeModel.class, 
                    "InputSchemaTreeModel.FAIL_TO_BUILD_TREE_MODEL_FOR", component.getTitle()), e);
        }
    }
    
}