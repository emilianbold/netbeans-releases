/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.wizard;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JDialog;
import org.netbeans.module.iep.editor.xsd.nodes.FolderNode;
import org.netbeans.module.iep.editor.xsd.nodes.ProjectNode;
import org.netbeans.module.iep.editor.xsd.nodes.SchemaArtifactTreeModel;
import org.netbeans.module.iep.editor.xsd.nodes.SchemaElementNode;
import org.netbeans.module.iep.editor.xsd.nodes.SchemaFileNode;

/**
 *
 * @author radval
 */
public class SchemaArtifactSelectionDialog {

    public static void main(String[] args) {
        
        FolderNode node = new FolderNode();
        node.setUserObject("By File");
        
        ProjectNode pNode = new ProjectNode();
        pNode.setUserObject("project1");
        node.add(pNode);
        
        SchemaFileNode fNode = new SchemaFileNode("schema1.xsd");
        pNode.add(fNode);
        
        FolderNode elementFolderNode = new FolderNode();
        elementFolderNode.setUserObject("Elements");
        fNode.add(elementFolderNode);
        
        SchemaElementNode eNode = new SchemaElementNode("purchaseOrder");
        eNode.setSelected(true);
        elementFolderNode.add(eNode);
        
        SchemaElementNode eNode1 = new SchemaElementNode("USAddress");
        elementFolderNode.add(eNode1);
        
        SchemaArtifactTreeModel model = new SchemaArtifactTreeModel(node);
        SchemaArtifactSelectionPanel panel = new SchemaArtifactSelectionPanel(model);
        
        JDialog dialog = new JDialog();
        Container container  = dialog.getContentPane();
        container.setLayout(new BorderLayout());
        container.add(panel, BorderLayout.CENTER);
        
        dialog.setSize(new Dimension(400, 600));
        dialog.setVisible(true);
    }
}
