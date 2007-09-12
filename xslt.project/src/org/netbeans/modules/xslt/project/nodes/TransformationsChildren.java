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
