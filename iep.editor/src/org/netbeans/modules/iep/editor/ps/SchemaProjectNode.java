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
package org.netbeans.modules.iep.editor.ps;

import java.beans.BeanInfo;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTree;

import org.netbeans.modules.iep.editor.xsd.nodes.AbstractSchemaArtifactNode;
import org.netbeans.modules.iep.editor.xsd.nodes.DirFileFilter;
import org.netbeans.modules.iep.editor.xsd.nodes.SchemaFileFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author radval
 */
public class SchemaProjectNode extends AbstractSchemaArtifactNode {

    private Node mProjectDelegateNode;
    
    private List<String> mExistingArtificatNames = new ArrayList<String>();
    
    private JTree mTree;
    
    private List<AbstractSchemaArtifactNode> mNodesToBeExpanded = new ArrayList<AbstractSchemaArtifactNode>();
    
    public SchemaProjectNode(Node projectDelegate, 
            List<String> existingArtificatNames,
            JTree tree) {
        super(projectDelegate.getDisplayName());
        this.mProjectDelegateNode = projectDelegate;
        this.mTree = tree;
        //this.setUserObject(projectDelegate.getDisplayName());
        
        this.mIcon = new ImageIcon(this.mProjectDelegateNode.getIcon(BeanInfo.ICON_COLOR_16x16)); 
        this.mExistingArtificatNames = existingArtificatNames;
        
        populateSchemaFiles();
    }
    
    
    private void populateSchemaFiles() {
        if(this.mProjectDelegateNode != null) {
            org.openide.nodes.Children children = this.mProjectDelegateNode.getChildren();
            for (Node child : children.getNodes()) {
                DataObject dobj = child.getCookie(DataObject.class);
                if (dobj != null) {
                    File[] files = recursiveListFiles(FileUtil.toFile(dobj.getPrimaryFile()), new SchemaFileFilter());
                    for (File file : files) {
                        try {
                            FileObject fo = FileUtil.toFileObject(file);
                            DataObject fileDataObject = DataObject.find(fo);
                            SchemaFileNode fileNode = new SchemaFileNode(fileDataObject.getNodeDelegate(), mExistingArtificatNames, mTree);
                            this.add(fileNode);
                            
                            
                            List<AbstractSchemaArtifactNode> nodesToBeExpanded = fileNode.getNodesToBeExpanded();
                            mNodesToBeExpanded.addAll(nodesToBeExpanded);
                            
                            
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            
        }
    }
    
    public List<AbstractSchemaArtifactNode> getNodesToBeExpanded() {
        return this.mNodesToBeExpanded;
    }
    
    private File[] recursiveListFiles(File file, FileFilter filter) {
        List<File> files = new ArrayList<File>();
        File[] filesArr = file.listFiles(filter);
        files.addAll(Arrays.asList(filesArr));
        File[] dirs = file.listFiles(new DirFileFilter());
        for (File dir : dirs) {
            files.addAll(Arrays.asList(recursiveListFiles(dir, filter)));
        }
        return files.toArray(new File[files.size()]);
    }
   
    
}
