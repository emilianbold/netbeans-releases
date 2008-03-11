/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.module.iep.editor.xsd.nodes;

import java.beans.BeanInfo;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.ImageIcon;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;

/**
 *
 * @author radval
 */
public class ProjectNode extends AbstractSchemaArtifactNode {

    private Node mProjectDelegateNode;
    
    public ProjectNode(Node projectDelegate) {
        super(projectDelegate.getDisplayName());
        this.mProjectDelegateNode = projectDelegate;
        //this.setUserObject(projectDelegate.getDisplayName());
        
        this.mIcon = new ImageIcon(this.mProjectDelegateNode.getIcon(BeanInfo.ICON_COLOR_16x16)); 
        
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
                            FileNode fileNode = new FileNode(fileDataObject.getNodeDelegate());
                            this.add(fileNode);
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            
        }
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
