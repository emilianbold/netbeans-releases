/*
 * SchemaRefactoringElement.java
 *
 * Created on December 30, 2006, 4:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.xml.refactoring.spi.RefactoringEngine;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.ui.actions.ShowSourceAction;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.actions.SystemAction;
/**
 *
 * @author Sonali
 */
public class SchemaRefactoringElement  extends SimpleRefactoringElementImplementation {
    
    private FileObject source;
    SchemaComponent comp;
    Node node;
    
    /**
     * Creates a new instance of SchemaRefactoringElement
     */
    public SchemaRefactoringElement(SchemaComponent comp) {
        this.comp=comp;
        CategorizedSchemaNodeFactory nodeFactory = new CategorizedSchemaNodeFactory(comp.getModel(), Lookups.singleton(comp));
        this.node = nodeFactory.createNode(comp);
           
    }

    @Override
    public Lookup getLookup() {
       return Lookups.singleton(comp);
       
    }

    public FileObject getParentFile() {
       FileObject source = (FileObject)comp.getModel().getModelSource().getLookup().lookup(FileObject.class);
       assert source != null : "ModelSource should have FileObject in lookup"; //NOI18N
       return source;
    }

    public void showPreview() {
        //UI.setComponentForRefactoringPreview(null);
     }
    
      
        
    public String getText() {
        return node.getName();
    }

    public String getDisplayText() {
        return node.getHtmlDisplayName();
    }

    public void performChange() {
    }

   public PositionBounds getPosition() {
        return null;
    }
    
         
     public void openInEditor(){
         System.out.println("XMLRefactoringElement:: openInEditor called");
         Action preferredAction = SystemAction.get(ShowSourceAction.class);
         String command = (String)preferredAction.getValue(Action.ACTION_COMMAND_KEY);
	 ActionEvent ae = new ActionEvent(node, 0, command);
	 preferredAction.actionPerformed(ae);
     
     }
     
         
    
}
