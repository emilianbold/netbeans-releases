/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.test.xml.schema.core.lib.dom.parser;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.netbeans.test.xml.schema.core.lib.util.Helpers;

/**
 *
 * @author ca@netbeans.org
 */
public class ExtraNodeInfo {
    
    private String m_strComponentName;
    private int m_lineNmb;
    private Node m_node;
    private boolean m_bGlobal;
    
    public ExtraNodeInfo(int lineNmb, String componentName, Node node, boolean bGlobal) {
        m_bGlobal = bGlobal;
        m_lineNmb = lineNmb;
        m_strComponentName = componentName;
        m_node = node;
    }
    
    public String getComponentName() {
        return m_strComponentName;
    }
    
    public int getLineNmb() {
        return m_lineNmb;
    }
    
    public String getColumnViewName() {
        String strName = getNamedAttrValue("name");
        
        if (strName != null) return strName;

        strName = getNamedAttrValue("ref");
        if (strName != null) {
            return Helpers.getUnqualifiedName(strName); // + " (->)";
        }
        
        if (m_strComponentName.equals("enumeration")) {
            strName = "\"" + getNamedAttrValue("value") + "\"";
        } else if (m_strComponentName.equals("whiteSpace")) {
            strName = "whitespace";
        }
        
        if (strName != null) return strName;
        
        strName = getNamedAttrValue("value");
        
        if (strName != null) return strName;
        
        strName = m_strComponentName;
        
        return strName;
    }
    
    public String getNamedAttrValue(String attrName) {
        String strAttrValue = null;
        
        NamedNodeMap map = m_node.getAttributes();
        
        String strName = "";
        if (map != null) {
            Node attr = map.getNamedItem(attrName);
            if (attr != null) {
                strAttrValue = attr.getNodeValue();
            }
        }

        return strAttrValue;
    }
    
    public boolean isGlobal() {
        return m_bGlobal;
    }
    
    public String getParentColumnViewName() {
        Node parentNode = m_node.getParentNode();
        ExtraNodeInfo sn = (ExtraNodeInfo) parentNode.getUserData("");
        return sn.getColumnViewName();
    }
    
    public int getLineNumber() {
        return m_lineNmb;
    }
    
    public static ExtraNodeInfo getExtraNodeInfo(Node node) {
        return (ExtraNodeInfo) node.getUserData("");        
    }
}
