/*
 * BranchConstructor.java
 *
 * Created on 9 јпрель 2007 г., 13:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xslt.mapper.model;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.targettree.AXIUtils;
import org.netbeans.modules.xslt.mapper.model.targettree.SchemaNode;
import org.netbeans.modules.xslt.mapper.model.targettree.StylesheetNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.AttrValueTamplateHolder;
import org.netbeans.modules.xslt.model.AttributeValueTemplate;
import org.netbeans.modules.xslt.model.NamespaceSpec;
import org.netbeans.modules.xslt.model.SequenceConstructor;
import org.netbeans.modules.xslt.model.SequenceElement;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslModel;
/**
 * Constructs missing XSLT components which are required to
 * the specified SchemaNode be presented in the XSLT document.
 *
 * The XSL model transaction is started and ended automatically if recessary.
 */

public class BranchConstructor {
    
    
    
    private boolean transactionStarted = false;
    private boolean exitTranactionOnFinish = true;
    private SchemaNode startFromNode;
    private XsltMapper mapper;
    private XslModel myModel;
    public BranchConstructor(SchemaNode node, XsltMapper mapper) {
        startFromNode = node;
        this.mapper = mapper;
        myModel = mapper.getContext().getXSLModel();
    }
    
    /**
     * Allows preventing end transaction on finish.
     * This method should be used carefully.
     */
    public void exitTranactionOnFinish(boolean newValue) {
        exitTranactionOnFinish = newValue;
    }
    
    public XslComponent construct() {
        XslComponent result = null;
       
        try {
            result = createXslComponent(startFromNode);
        } finally {
            if (exitTranactionOnFinish && myModel != null && transactionStarted) {
                myModel.endTransaction();
            }
        }
        return result;
    }
    
    private XslComponent createXslComponent(TreeNode node) {
        XslComponent result = null;
        //
        if (node instanceof StylesheetNode){
            result = (XslComponent) node.getDataObject();
        } else if (node instanceof SchemaNode){
            TreeNode parent = node.getParent();
            XslComponent parent_xsl = createXslComponent(parent);
            if (parent_xsl != null) {
                //
                // Start transaction if necessary
                if (!transactionStarted && myModel != null) {
                    if (!myModel.isIntransaction()) {
                        transactionStarted = myModel.startTransaction();
                    }
                }
                //
                result = createXslElementOrAttribute(parent_xsl, node.getType(), mapper);
            }
        }
        //
        return result;
    }
    public static XslComponent createXslElementOrAttribute(XslComponent parent, AXIComponent type, XsltMapper mapper){
        assert (parent instanceof SequenceConstructor);
        XslModel model = parent.getModel();
        XslComponent nameHolder = null;
        if (type instanceof AbstractAttribute) {
            nameHolder = model.getFactory().createAttribute();
        } else if (type instanceof AbstractElement){
            nameHolder = model.getFactory().createElement();
        } else {
            assert false : "Cant recognize element type for new XSL element";
        }
        //
        if (nameHolder != null){
            AttributeValueTemplate nameAVT;
            AttributeValueTemplate namespaceAVT = null;
            //
            String name = ((AXIType) type).getName();
            if (AxiomUtils.isUnqualified(type)) {
                nameAVT = ((AttrValueTamplateHolder)nameHolder).
                        createTemplate(name);
                namespaceAVT = ((AttrValueTamplateHolder)nameHolder).
                        createTemplate("");
            } else {
                String namespace = type.getTargetNamespace();
                QName elementQName = new QName(namespace, name);
                nameAVT = ((AttrValueTamplateHolder)nameHolder).
                        createTemplate(elementQName);
            }
            
            int index = calculateNodeIndex(parent, type, mapper);
            //
            if (model.isIntransaction()) {
                //
                ((AttrValueTamplateHolder)nameHolder).setName(nameAVT);
                if (namespaceAVT != null) {
                    ((NamespaceSpec)nameHolder).setNamespace(namespaceAVT);
                }
                //
                ((SequenceConstructor)parent).addSequenceChild(
                        (SequenceElement)nameHolder, index);
            } else {
                model.startTransaction();
                try {
                    //
                    ((AttrValueTamplateHolder)nameHolder).setName(nameAVT);
                    if (namespaceAVT != null) {
                        ((NamespaceSpec)nameHolder).setNamespace(namespaceAVT);
                    }
                    //
                    ((SequenceConstructor)parent).addSequenceChild(
                            (SequenceElement)nameHolder, index);
                } finally {
                    model.endTransaction();
                }
            }
        }
        return nameHolder;
    }
    
    
    
    private static  int calculateNodeIndex(XslComponent parent_xsl, AXIComponent type, XsltMapper mapper) {
        List<XslComponent> xsl_children = parent_xsl.getChildren();
        
        AXIComponent parent_type = AXIUtils.getType(parent_xsl, mapper);
        
        List<AXIComponent> axi_children = AXIUtils.getChildTypes(parent_type);
        
        int type_index = axi_children.indexOf(type);
        if (type_index == -1){
            return 0;
        }
        
        for (int n = 0; n < xsl_children.size(); n++){
            AXIComponent child_type = AXIUtils.getType(xsl_children.get(n), mapper);
            int comp_index = axi_children.indexOf(child_type);
            if (comp_index > type_index){
                return n;
            }
        }
        return xsl_children.size();
        
        
    }
}

