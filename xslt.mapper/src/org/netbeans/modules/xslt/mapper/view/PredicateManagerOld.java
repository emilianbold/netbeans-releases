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

package org.netbeans.modules.xslt.mapper.view;

import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicField;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicFieldNode;
import org.netbeans.modules.soa.mapper.common.IMapperGroupNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xml.xpath.visitor.AbstractXPathVisitor;
import org.netbeans.modules.xslt.mapper.methoid.Constants;
import org.netbeans.modules.xslt.mapper.model.BuildExpressionVisitor;
import org.netbeans.modules.xslt.mapper.model.MapperContext;
import org.netbeans.modules.xslt.mapper.model.targettree.SchemaNode;
import org.openide.filesystems.FileObject;

/**
 *
 * @author nk160297
 */
public class PredicateManagerOld {
    
    /** Creates a new instance of PredicateManager */
    public PredicateManagerOld() {
    }
    
    /**
     * Provide the special processing to support Predicate elements.
     */
    public static void processAddLink(IMapperLink link, MapperContext context) {
        PredicateLinkParams pr = obtainLinkParams(link);
        //
        BuildExpressionVisitor visitor_ge = new BuildExpressionVisitor(context);
        visitor_ge.visit(pr.sourceSchemaNode);
        XPathExpression xPathExpr = visitor_ge.getResult();
        //
        clearPredicateInputFields(pr.groupNode, pr.mainInputField);
        //
        fillInPredicateInputFields(pr.mainInputField, xPathExpr);
        //
//        System.out.println("Connect the source node \"" + schemaNode.getName() +
//                "\" with the predicate node");
    }
    
    public static void processRemoveLink(IMapperLink link) {
        PredicateLinkParams pr = obtainLinkParams(link);
        //
        clearPredicateInputFields(pr.groupNode, pr.mainInputField);
    }
    
    public static void clearPredicateInputFields(
            IMapperGroupNode predicateNode, IFieldNode mainInputField) {
        IMapperNode mapperNode = predicateNode.getFirstNode();
        while(mapperNode != null) {
            if (mapperNode instanceof IFieldNode) {
                IFieldNode fieldNode = (IFieldNode)mapperNode;
                if (fieldNode.isInput() && !fieldNode.equals(mainInputField)) {
                    mapperNode = predicateNode.getNextNode(fieldNode);
                    predicateNode.removeNode(fieldNode);
                    System.out.println(fieldNode.getName());
                    continue;
                }
            } else if (mapperNode instanceof IMapperGroupNode) {
                clearPredicateInputFields(
                        (IMapperGroupNode)mapperNode, mainInputField);
            }
            mapperNode = predicateNode.getNextNode(mapperNode);
        }
        
    }
    
    public static void fillInPredicateInputFields(
            final IFieldNode mainInputField, XPathExpression xPathExpr) {
        //
        final IMapperGroupNode fieldsOwnerNode = mainInputField.getGroupNode();
        xPathExpr.accept(new AbstractXPathVisitor() {
            public void visit(XPathLocationPath locationPath) {
                LocationStep[] stepArr = locationPath.getSteps();
                IFieldNode addNextAfter = mainInputField;
                for (LocationStep step : stepArr) {
                    String stepText = step.getString();
                    //
                    // Construct new field
                    BasicField newField = new BasicField(stepText,
                            "predicate", // field type
                            "", // tooltip
                            null, // data object
                            true, // is input
                            false, // is output
                            null); // literal updater // NOI18N
                    BasicFieldNode newFieldNode = new BasicFieldNode(newField);
                    fieldsOwnerNode.addNextNode(addNextAfter, newFieldNode);
                    addNextAfter = newFieldNode;
                }
            }
        });
    }
    
    private static PredicateLinkParams obtainLinkParams(IMapperLink link) {
        IMapperNode startNode = link.getStartNode();
        IMapperNode endNode = link.getEndNode();
        //
        // Check if the end node is a predicate
        boolean isPredicate = false;
        IFieldNode endFldNode = null;
        IMethoidNode endMethoidNode = null;
        IMapperGroupNode groupNode = null;
        if (endNode instanceof IFieldNode) {
            endFldNode = (IFieldNode)endNode;
            groupNode = endFldNode.getGroupNode();
            while (groupNode.getGroupNode() != null) {
                groupNode = groupNode.getGroupNode();
            }
            //
            if (groupNode instanceof IMethoidNode) {
                endMethoidNode = (IMethoidNode)groupNode;
                Object methoidObj = endMethoidNode.getMethoidObject();
                if (methoidObj instanceof IMethoid) {
                    Object dataObject = ((IMethoid)methoidObj).getData();
                    if (dataObject instanceof FileObject) {
                        FileObject dataFo = (FileObject)dataObject;
                        Object pridicateObj = dataFo.getAttribute(Constants.IS_PREDICATE);
                        if (pridicateObj != null && pridicateObj instanceof Boolean) {
                            isPredicate = ((Boolean) pridicateObj).booleanValue();
                        }
                    }
                }
            }
        }
        //
        if (!isPredicate) {
            // The target node is not a predicate element.
            return null;
        }
        //
        TreePath treePath = null;
        SchemaNode schemaNode = null;
        if (startNode instanceof IMapperTreeNode) {
            IMapperTreeNode startMapperNode = ((IMapperTreeNode)startNode);
            if (startMapperNode.isSourceTreeNode()) {
                treePath = startMapperNode.getPath();
//                Object nodeObj = startMapperNode.getNodeObject();
                if (treePath != null) {
                    Object pathComp = treePath.getLastPathComponent();
                    if (pathComp instanceof SchemaNode) {
                        schemaNode = (SchemaNode)pathComp;
                    }
                }
            }
        }
        //
        if (schemaNode == null) {
            // The link was connected to a wrong source node.
            // It has to be a SchemaNode from the source tree
            return null;
        }
        //
        if (!Constants.PREDICATE_MAIN_INPUT_TYPE.equals(endFldNode.getTypeName())) {
            // the link was connected to a wrong field.
            // It has to be connected to the specific field with the type "node-set".
            return null;
        }
        //
        PredicateLinkParams pr = new PredicateLinkParams();
        pr.groupNode = groupNode;
        pr.methoidNode = endMethoidNode;
        pr.mainInputField = endFldNode;
        pr.sourceSchemaNode = schemaNode;
        pr.sourceTreePath = treePath;
        //
        return pr;
    }
    
    private static class PredicateLinkParams {
        public IMethoidNode methoidNode;
        public IMapperGroupNode groupNode;
        public IFieldNode mainInputField;
        public TreePath sourceTreePath;
        public SchemaNode sourceSchemaNode;
    }
}
