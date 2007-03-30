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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Petr Pisl
 */
public abstract class JSFConfigComponentImpl extends AbstractDocumentComponent <JSFConfigComponent>
        implements JSFConfigComponent {
    
    /** Creates a new instance of JSFConfigComponentImp */
    public JSFConfigComponentImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public JSFConfigModelImpl getModel(){
        return (JSFConfigModelImpl)super.getModel();
    }
    protected void populateChildren(List<JSFConfigComponent> children) {
        NodeList nodeList = getPeer().getChildNodes();
        if (nodeList != null){
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node node = nodeList.item(i);
                if (node instanceof Element) {
                    JSFConfigModel model = getModel();
                    JSFConfigComponent comp = (JSFConfigComponent) model.getFactory().create((Element)node, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }
    
    /**
     * Set the value of the text node from the child element with given QName.
     * This method is use to implement mapping of "property" as component attribute.
     * @param propertyName property change event name
     * @param text the string to set value of the child element text node.
     * @param qname QName of the child element to get text from.
     */
    protected void setChildElementText(String propertyName, String text, QName qname) {
        super.setChildElementText(propertyName, text, qname);
        reorderChildren();
    }
    
    protected void appendChild(String propertyName, JSFConfigComponent child) {
        super.appendChild(propertyName, child);
        reorderChildren();
    }
    
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        return stringValue;
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return Collections.EMPTY_LIST;
    }
    
    protected void reorderChildren(){
        NodeList nodes = getPeer().getChildNodes();
        int length = nodes.getLength();
        Integer[] indexes = new Integer[length];
        
        for(int i = 0; i < length; i ++){
            indexes[i] = new Integer(i);
        }
        Arrays.sort(indexes, 0 , indexes.length, new OrderComparator(getSortedListOfLocalNames(), nodes));
        int[] newIndexes = new int[length];
        for (int i = 0; i < length; i++){
            newIndexes[i] = indexes[i].intValue();
        }
        getModel().getAccess().reorderChildren(getPeer(), newIndexes, this);
    }
    
    static public Element createElementNS(JSFConfigModel model,JSFConfigQNames jsfqname) {
        return model.getDocument().createElementNS(jsfqname.getQName(model.getVersion()).getNamespaceURI(), jsfqname.getQualifiedName(model.getVersion()));
    }
    
    private class OrderComparator implements Comparator<Integer>{
        List<String> order;
        NodeList nodes;
        
        public OrderComparator(List<String> order, NodeList nodes){
            this.order = order;
            this.nodes = nodes;
        }
        
        public int compare(Integer arg0, Integer arg1) {
            int result;
            Node node0 = nodes.item(arg0);
            Node node1 = nodes.item(arg1);
            
            int possition0 = order.indexOf(node0.getLocalName());
            int possition1 = order.indexOf(node1.getLocalName());
            
            if (possition0 == possition1){
                result = 0;
            } else if (possition0 < possition1){
                result = -1;
            } else {
                result = 1;
            }
            return result;
        }
    }
    
    
}
