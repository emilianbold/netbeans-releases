/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.test.xml.schema.lib.dom.parser;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.netbeans.test.xml.schema.lib.util.Helpers;

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
