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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.utils.XmlUtil;

/**
 * This is used when ETLDefination/SQLDefination has be created outside of netbeans env
 */
public class OperatorXmlInfoModelX implements IOperatorXmlInfoModel {
    private static final String TAG_CATEGORY = "category";
    private static final String DEFAULT_FOLDER = "ETLOperators";
    private static final String OPERATOR_INFO_RESOURCE_PATH = "resources/operatorInfo.xml";
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
