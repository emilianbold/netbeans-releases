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
package org.netbeans.modules.xslt.project.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xslt.tmap.model.xsltmap.TransformationDesc;
import org.netbeans.modules.xslt.tmap.model.xsltmap.TransformationUC;
import org.netbeans.modules.xslt.tmap.model.xsltmap.XsltMapModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformationsChildren extends Children.Keys implements FileChangeListener {
    private FileObject projectDir;
    private Project project;
    
    public TransformationsChildren(Project project) {
        super();
        this.project = project;
        this.projectDir = project.getProjectDirectory();
    }

    protected Node[] createNodes(Object key) {
        List<Node> nodes = new ArrayList<Node>();
        if (key instanceof TransformationUC) {
            nodes.add(new TransformationUCNode((TransformationUC)key,
                      new TransformationUCChildren((TransformationUC)key)));
            
//            List<TransformationDesc> descs = ((TransformationUC)key).getTransformationDescs();
//            if (descs != null) {
//                for (TransformationDesc elem : descs) {
//                    nodes.add(new TransformationDescNode(elem));
//                }
//            }
        } else if (key instanceof TransformationDesc) {
            nodes.add(new TransformationDescNode((TransformationDesc)key));
        }
        return nodes.toArray(new Node[nodes.size()]);
    }
    
    private Collection getNodeKeys() {
//        System.out.println("invoked getNodeKeys() !!! ");
        
        if (projectDir == null) {
            return Collections.EMPTY_SET;
        }
        XsltMapModel xsltMapModel;
        try {
            xsltMapModel = XsltMapModel.getDefault(project);
            if (xsltMapModel != null) {
                return xsltMapModel.getTransformationUCs();
// TODO a | r                return xsltMapModel.getTransformationDescs();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            ErrorManager.getDefault().notify(ex);
        }
        return Collections.EMPTY_SET;
    }
    
    protected void addNotify() {
        super.addNotify();
        projectDir.getFileObject("src").addFileChangeListener(this);
        setKeys(getNodeKeys());
    }

    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
        projectDir.getFileObject("src").removeFileChangeListener(this);
        super.removeNotify();
    }

    public void fileFolderCreated(FileEvent fe) {
        setKeys(getNodeKeys());
    }

    public void fileDataCreated(FileEvent fe) {
    }

    public void fileChanged(FileEvent fe) {
        setKeys(getNodeKeys());
    }

    public void fileDeleted(FileEvent fe) {
        setKeys(getNodeKeys());
    }

    public void fileRenamed(FileRenameEvent fe) {
        setKeys(getNodeKeys());
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

}
