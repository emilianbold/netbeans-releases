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

package org.netbeans.modules.junit;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileUtil;

/**
 * 
 * @author  Marian Petras
 */
abstract class TestAction extends NodeAction { 
    
    /** Creates a new instance of TestAction */
    TestAction() {
    }
    
    public boolean asynchronous() {
        return false;
    }
    
    /**
     * Perform special enablement check in addition to the normal one.
     * 
     *     protected boolean enable (Node[] nodes) {
     *         if (!super.enable(nodes)) {
     *             return false;
     *         }
     *     }
     * 
     *     if (...) {
     *         ...
     *     }
     */
    @Override
    protected boolean enable(Node[] nodes) {
        if (nodes.length == 0) {
            return false;
        }
        
        for (Node node : nodes) {
            DataObject dataObj = node.getCookie(DataObject.class);
            if (dataObj != null) {
                FileObject fileObj = dataObj.getPrimaryFile();
                if ((fileObj == null) || !fileObj.isValid()) {
                    continue;
                }
                
                Project prj = FileOwnerQuery.getOwner(fileObj);
                if ((prj == null) || (getSourceGroup(fileObj, prj) == null)) {
                    continue;
                }

                if (TestUtil.isJavaFile(fileObj) 
                        || (node.getCookie(DataFolder.class) != null)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     */
    private static SourceGroup getSourceGroup(FileObject file, Project prj) {
        Sources src = ProjectUtils.getSources(prj);
        SourceGroup[] srcGrps = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup srcGrp : srcGrps) {
            FileObject rootFolder = srcGrp.getRootFolder();
            if (((file == rootFolder) || FileUtil.isParentOf(rootFolder, file)) 
                    && srcGrp.contains(file)) {
                return srcGrp;
            }
        }
        return null;
    }    
    
    
}
