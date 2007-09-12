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

import java.util.Iterator;
import org.netbeans.modules.soa.mapper.common.IMapperGroupNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater.LiteralSubTypeInfo;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.openide.nodes.Node;


/**
 * Handles creation of literal editors for the BPEL editor as well
 * as what to do when the resulting editor value is applied.
 * Whenever support for a new literal type is added, an editor as well
 * as an updater must be created to handle it. Editors are the UI
 * for the visual mapper editor. They allow users to enter a new
 * literal value or modify an existing one. Updaters are responsible
 * for changing the model expression once the user has finished
 * editing the value.
 * 
 * @author Josh Sandusky
 */
public abstract class AbstractLiteralUpdater implements ILiteralUpdater {

    protected XPathNodeExpressionUpdater mProcessor;
    
    public AbstractLiteralUpdater() {
    }
    

    public boolean hasEditor() {
        return true;
    }
    
// TODO reimplement
//    protected void applyLiteral(IFieldNode fieldNode, 
//                                String newValue, 
//                                XPathLiteralNode literalNode) {
//        updateModelLiteral(
//                fieldNode, 
//                literalNode, 
//                false);
//        fieldNode.setLiteralName(newValue);
//        updateToolTip(fieldNode, newValue);
//        if (mProcessor != null) {
//            mProcessor.updateNodeExpression(fieldNode);
//        }
//    }
    
    public void setXPathProcessor(XPathNodeExpressionUpdater processor) {
        mProcessor = processor;
    }
    
    protected void updateToolTip(IFieldNode fieldNode, String newValue) {
        IMethoidNode methoidNode = (IMethoidNode) fieldNode.getGroupNode();
        IMethoid methoid = (IMethoid) methoidNode.getMethoidObject();
        if (methoid.isLiteral()) {
            fieldNode.setToolTipText(newValue);
        }
    }
    
    public void literalUnset(IFieldNode fieldNode) {
// TODO reimplement
//        updateModelLiteral(
//                fieldNode, 
//                (XPathLiteralNode) fieldNode.getNodeObject(), 
//                true);
        fieldNode.setLiteralName(null);
        if (mProcessor != null) {
            mProcessor.updateNodeExpression(fieldNode);
        }
    }
    
    public String getLiteralDisplayText(String literalText) {
        return literalText;
    }
    
    public LiteralSubTypeInfo getLiteralSubType(String freeTextValue) {
        // default case is no special sub-type information
        return null;
    }

// TODO reimplement    
//    private void updateModelLiteral(IFieldNode fieldNode, 
//                                    XPathLiteralNode literalNode, 
//                                    boolean isRemove) {
//        boolean linksNeedRemoving = false;
//        IMapperGroupNode groupNode = fieldNode.getGroupNode();
//        Node groupNodeObject = (Node) groupNode.getNodeObject();
//        if (groupNodeObject instanceof XPathOperatorNode) {
//            XPathOperatorNode operatorNode = (XPathOperatorNode) groupNodeObject;
//            int fieldIndex = MapperUtil.findFieldIndex(groupNode, fieldNode);
//            if (isRemove) {
//                if (literalNode != null) {
//                    fieldNode.setNodeObject(null);
//                    operatorNode.removeInput(literalNode);
//                }
//            } else {
//                linksNeedRemoving = true;
//                fieldNode.setNodeObject(literalNode);
//                operatorNode.addInput(fieldIndex, literalNode);
//            }
//        } else if (groupNodeObject instanceof XPathLiteralNode) {
//            if (isRemove) {
//                groupNode.setNodeObject(null);
//            } else {
//                groupNode.setNodeObject(literalNode);
//            }
//        }
//        
//        if (linksNeedRemoving) {
//            // Now for each link connected to the field node, we remove the
//            // link's starting node's output.
//            for (Iterator iter=fieldNode.getLinks().iterator(); iter.hasNext();) {
//                IMapperLink link = (IMapperLink) iter.next();
//                IMapperNode startNode = link.getStartNode();
//                Node modelNode = MapperUtil.getMapperNodeObject(startNode);
//                if (modelNode instanceof CanvasNode) {
//                    CanvasNode modelCanvasNode = (CanvasNode) modelNode;
//                    modelCanvasNode.removeOutput(groupNodeObject);
//                }
//            }
//        }
//    }
    
    
    public interface XPathNodeExpressionUpdater {
        public void updateNodeExpression(IFieldNode sourceNode);
    }
}
