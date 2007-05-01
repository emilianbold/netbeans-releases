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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoModel;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class OperatorXmlInfoModel implements IOperatorXmlInfoModel {
    private static final String DEFAULT_FOLDER = "ETLOperators";
    private Node rootNode;
    private static final Map modelMap = new HashMap();

    /** Creates a new instance of OperatorXmlInfoModel */
    private OperatorXmlInfoModel(String operatorFolder) {
        String folder = (operatorFolder == null) ? DEFAULT_FOLDER : operatorFolder;
        DataObject rootObj = getRootOperatorGroupObject(folder);
        rootNode = new OperatorCategoryRootNode(rootObj);
    }

    private DataObject getRootOperatorGroupObject(String folderName) {
        try {
            //org.openide.filesystems.FileObject fo = Repository.getDefault().findResource(folderName);
            org.openide.filesystems.FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(folderName);
            if (fo == null) {
                throw new Exception("Folder not found." + folderName);
            }

            return DataObject.find(fo);
        } catch (Exception ex) {
            throw new InternalError("Folder not found: " + folderName);
        }
    }

    public Node getRootNode() {
        return rootNode;
    }

    public IOperatorXmlInfo findOperatorXmlInfo(String operatorName) {
        IOperatorXmlInfo xmlInfo = null;
        Children children = rootNode.getChildren();
        Node[] nodes = children.getNodes();

        for (int i = 0; i < nodes.length; i++) {
            OperatorCategoryNode node = (OperatorCategoryNode) nodes[i];
            xmlInfo = node.findOperatorXmlInfo(operatorName);
            if (xmlInfo != null) {
                break;
            }
        }

        return xmlInfo;
    }

    public static OperatorXmlInfoModel getInstance(String operatorFolder) {
        String folder = (operatorFolder == null) ? DEFAULT_FOLDER : operatorFolder;
        String key = folder;
        Object instance = modelMap.get(key);
        if (instance == null) {
            instance = new OperatorXmlInfoModel(folder);
            modelMap.put(key, instance);
        }

        return (OperatorXmlInfoModel) instance;
    }
}

