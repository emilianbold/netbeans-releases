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
package org.netbeans.modules.sql.framework.model.impl;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.exception.BaseException;

/**
 * 
 */
public class OperatorCategoryNodeX extends CommonNodeX implements org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoCategory {
    private static final String TAG_OPERATOR = "operator";
    
    private Map operatorNameToNodeMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);

    public OperatorCategoryNodeX(Element catElem) {
        super(catElem);
        createOperators(catElem);
    }

    private void createOperators(Element catElem) {
        NodeList children = catElem.getChildNodes();
        Node node = null;
        Element element = null;
        Object value = null;
        for (int i = 0; i < children.getLength(); i++) {
            node = children.item(i);    
            if (node.getNodeName().equals(TAG_OPERATOR)) {
                element = (Element) node;
                try {
                    OperatorNodeX opNode = new OperatorNodeX(element);
                    // For quick search of oOperatorNode based on operator name.
                    operatorNameToNodeMap.put(opNode.getName(), opNode);
                } catch (BaseException ex){
                    
                }
            } else if (node.getNodeName().equals(TAG_ATTRIBUTE)) {
                element = (Element) node;
                name = element.getAttribute(ATTR_NAME);
                value = this.getAttributeValue(element);
                this.attributes.put(name, value);
            }
        }
    }

    /**
     * Gets the operator list for this category
     * 
     * @return operator list
     */
    public ArrayList getOperatorList() {
        ArrayList list = new ArrayList();
        list.addAll(this.operatorNameToNodeMap.values());
        return list;
    }

    /**
     * Gets the IOperatorXmlInfo instance in this category, if any, corresponding to the
     * given operator name.
     * 
     * @param operatorName name of operator to locate
     * @return IOperatorXmlInfo instance for <code>operatorName</code>, or null if it
     *         does not exist in this category
     */
    public org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo findOperatorXmlInfo(String operatorName) {
        return (org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo) operatorNameToNodeMap.get(operatorName);
    }
}
