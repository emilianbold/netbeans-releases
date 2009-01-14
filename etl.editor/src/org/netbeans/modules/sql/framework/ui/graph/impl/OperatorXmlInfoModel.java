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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoModel;
import org.openide.filesystems.FileUtil;
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
            org.openide.filesystems.FileObject fo = FileUtil.getConfigFile(folderName);
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

