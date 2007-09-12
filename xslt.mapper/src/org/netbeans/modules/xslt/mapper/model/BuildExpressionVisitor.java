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
package org.netbeans.modules.xslt.mapper.model;

import java.util.List;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils.PathItem;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathOperationOrFuntion;
import org.netbeans.modules.xslt.mapper.XPathUtil;
import org.netbeans.modules.xslt.mapper.model.nodes.LiteralCanvasNode;
import org.netbeans.modules.xslt.mapper.model.nodes.Node;
import org.netbeans.modules.xslt.mapper.model.nodes.OperationOrFunctionCanvasNode;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.AbstractNodeVisitor;
import org.netbeans.modules.xslt.mapper.model.targettree.AXIUtils;
import org.netbeans.modules.xslt.mapper.model.targettree.SchemaNode;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.XslComponent;

/**
 *
 * @author Alexey
 */
public class BuildExpressionVisitor extends AbstractNodeVisitor {
    
    private MapperContext myMapperContext;
    private XPathExpression result;
    private XslComponent context;
    
    public static final String UNCONNECTED_INPUT = "$unconnectedInput_";
    
    public BuildExpressionVisitor(MapperContext mapperContext, XslComponent context) {
        myMapperContext = mapperContext;
        this.context = context;
    }
    
    public BuildExpressionVisitor(MapperContext mapperContext) {
        this(mapperContext, null);
    }
    
    public XPathExpression getResult(){
        return this.result;
    }
    
    public void visit(OperationOrFunctionCanvasNode node) {
        result = (XPathOperationOrFuntion) node.getDataObject();
        
        
        
        ((XPathOperationOrFuntion) result).clearChildren();
        
        List<Node> prevNodes = node.getAllPreviousNodes();
        
        for(int n = 0; n <  prevNodes.size(); n++){
            Node nn = prevNodes.get(n);
            if (nn != null) {
                BuildExpressionVisitor visitor = new BuildExpressionVisitor(myMapperContext);
                nn.accept(visitor);
                ((XPathOperationOrFuntion) result).addChild(visitor.getResult());
            }  else {
                
                IMapperNode mn = node.getMapperNode();
                boolean isAccumulative = false;
                if (mn instanceof IMethoidNode){
                    IMethoid methoid = (IMethoid) ((IMethoidNode) mn).getMethoidObject();
                    isAccumulative = methoid.isAccumulative();
                }
                if (!isAccumulative){
                    ((XPathOperationOrFuntion) result).addChild(XPathUtil.createExpression(UNCONNECTED_INPUT + n));
                }
            }
        }
        
    }
    
    public void visit(LiteralCanvasNode node) {
        result = (XPathExpression) node.getDataObject();
    }
    
    public void visit(SchemaNode node) {
        Stylesheet stylesheet = myMapperContext.getXSLModel().getStylesheet();
        AbstractDocumentComponent adc = (AbstractDocumentComponent)stylesheet;
        //
        List<PathItem> path = AXIUtils.prepareSimpleXPath(node);
        String locationPath = AxiomUtils.calculateSimpleXPath(path, adc);
        //
        result = XPathUtil.createExpression(locationPath);
    }
    
    private String getLocationPath(TreeNode node) {
        
        AXIComponent myself = (AXIComponent) node.getDataObject();
        
        TreeNode parent = node.getParent();
        
        
        String name = ((AXIType) myself).getName();
        
        if (parent != null){
            return getLocationPath(parent) +"/" + name;
        } else {
            return "/" + name;
        }
    }
    
    /// dont use it. impl is broken. root cause is being investigated
//    private String getLocationPath(Element e) {
//
//        AXIComponent parent =  c.getParentElement();
//
//        String name = ((AXIType) c).getName();
//
//
//        if (parent != null){
//            return getLocationPath(parent) +"/" + name;
//        } else {
//            return "/" + name;
//        }
//    }
}
