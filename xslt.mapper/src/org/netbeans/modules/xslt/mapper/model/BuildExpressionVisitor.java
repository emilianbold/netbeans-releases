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
