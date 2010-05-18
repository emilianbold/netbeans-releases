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
package org.netbeans.modules.sql.framework.model.impl;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.etl.exception.BaseException;

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
