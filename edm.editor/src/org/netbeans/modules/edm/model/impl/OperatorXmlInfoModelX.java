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
package org.netbeans.modules.edm.model.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfo;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfoModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.edm.editor.utils.XmlUtil;

/**
 * This is used when ETLDefination/SQLDefination has be created outside of netbeans env
 */
public class OperatorXmlInfoModelX implements IOperatorXmlInfoModel {
    private static final String TAG_CATEGORY = "category";
    private static final String DEFAULT_FOLDER = "ETLOperators";
    private static final String OPERATOR_INFO_RESOURCE_PATH = "operatorInfo.xml";
    private static final Map modelMap = new HashMap();
    
    private final List categories = new Vector();
    private final Map operatorInfos = new HashMap();

    /** Creates a new instance of OperatorXmlInfoModelX */
    private OperatorXmlInfoModelX() {
        parseOperatorInfo();
    }

    private void parseOperatorInfo(){        
        InputStream is = this.getClass().getResourceAsStream(OPERATOR_INFO_RESOURCE_PATH);
        Element element = XmlUtil.loadXMLFile(new InputStreamReader(is));
        Element catElem = null;
        NodeList categoryNodes = element.getChildNodes();        
        Node node = null;
        OperatorCategoryNodeX category = null;        
        categories.clear();
        operatorInfos.clear();
        for (int i = 0; i < categoryNodes.getLength(); i++) {
            node = categoryNodes.item(i);
            if (!node.getNodeName().equals(TAG_CATEGORY)) {
                continue;
            }
            catElem = (Element) node;            
            category = new OperatorCategoryNodeX(catElem);
            categories.add(category);
        }        
    }
    public List getOperatorCategories(){
        return this.categories;
    }
    
    public IOperatorXmlInfo findOperatorXmlInfo(String operatorName) {
        IOperatorXmlInfo xmlInfo = null;
        int len = this.categories.size();
        for (int i = 0; i < len; i++) {
            OperatorCategoryNodeX node = (OperatorCategoryNodeX) categories.get(i);
            xmlInfo = node.findOperatorXmlInfo(operatorName);
            if (xmlInfo != null) {
                break;
            }
        }
        return xmlInfo;
    }

    public static OperatorXmlInfoModelX getInstance() {
        return getInstance(null);
    }

    public static OperatorXmlInfoModelX getInstance(String operatorFolder) {
        String folder = (operatorFolder == null) ? DEFAULT_FOLDER : operatorFolder;
        String key = folder;
        Object instance = modelMap.get(key);
        if (instance == null) {
            instance = new OperatorXmlInfoModelX();
            modelMap.put(key, instance);
        }

        return (OperatorXmlInfoModelX) instance;
    }

   

    public org.openide.nodes.Node getRootNode() {
		// TODO Auto-generated method stub
		return null;
	}

   
    
   
}
