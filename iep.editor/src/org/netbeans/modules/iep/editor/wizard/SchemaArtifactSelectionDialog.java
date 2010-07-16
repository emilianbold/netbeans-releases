/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.wizard;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class SchemaArtifactSelectionDialog {

    public static void main(String[] args) {
        
//        FolderNode node = new FolderNode();
//        node.setUserObject("By File");
//        
//        ProjectNode pNode = new ProjectNode();
//        pNode.setUserObject("project1");
//        node.add(pNode);
//        
//        SchemaFileNode fNode = new SchemaFileNode("schema1.xsd");
//        pNode.add(fNode);
//        
//        FolderNode elementFolderNode = new FolderNode();
//        elementFolderNode.setUserObject("Elements");
//        fNode.add(elementFolderNode);
//        
//        SchemaElementNode eNode = new SchemaElementNode("purchaseOrder");
//        eNode.setSelected(true);
//        elementFolderNode.add(eNode);
//        
//        SchemaElementNode eNode1 = new SchemaElementNode("USAddress");
//        elementFolderNode.add(eNode1);
//        
//        SchemaArtifactTreeModel model = new SchemaArtifactTreeModel(node, null);
//        SchemaArtifactSelectionPanel panel = new SchemaArtifactSelectionPanel(model);
//        
//        JDialog dialog = new JDialog();
//        Container container  = dialog.getContentPane();
//        container.setLayout(new BorderLayout());
//        container.add(panel, BorderLayout.CENTER);
//        
//        dialog.setSize(new Dimension(400, 600));
//        dialog.setVisible(true);
    }
    
    public static  List<AXIComponent> showDialog(Project project, List<AXIComponent> existingArtificatNames) {
        List<AXIComponent> artifactNamesList = new ArrayList<AXIComponent>();
        SchemaArtifactSelectionPanel panel = new SchemaArtifactSelectionPanel(existingArtificatNames, project);
        
        String title = NbBundle.getMessage(SchemaArtifactSelectionDialog.class, "SchemaArtifactSelectionDialog.Title");
        String tooltip = NbBundle.getMessage(SchemaArtifactSelectionDialog.class, "SchemaArtifactSelectionDialog.Tooltip");
        
        panel.setToolTipText(tooltip);
        panel.getAccessibleContext().setAccessibleDescription(tooltip);
        
        DialogDescriptor dd = new DialogDescriptor(panel, tooltip, true, null);
        DialogDisplayer dDisplayer = DialogDisplayer.getDefault();
        
        dDisplayer.notify(dd);
        
        //ok is clicked do processing
        artifactNamesList = panel.getSelectedArtifactNames();
        
        return artifactNamesList;
    }
    
}
