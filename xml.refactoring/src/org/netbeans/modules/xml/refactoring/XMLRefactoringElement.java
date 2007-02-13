/*
 * XMLRefactoringElement.java
 *
 * Created on December 30, 2006, 4:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.refactoring;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImpl;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.xml.refactoring.spi.RefactoringEngine;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.PositionBounds;

/**
 *
 * @author Sonali
 */
public class XMLRefactoringElement extends SimpleRefactoringElementImpl {
    
    Usage usage;
    Node node;
    
    /**
     * Creates a new instance of XMLRefactoringElement
     */
    public XMLRefactoringElement(Usage u) {
        this.usage=u;
        Component comp = usage.getComponent();
        //UIHelper engine = usage.getContainer().getEngine().getUIHelper();
       try {
            node = usage.getContainer().getEngine().getUIHelper().getDisplayNode(comp);
       } catch(NullPointerException e){
           
       }
    }

    public String getText() {
        if(node != null)
            return node.getName();
        else 
            return "";
    }

    public String getDisplayText() {
        if(node != null)
            return node.getHtmlDisplayName();
        else
            return "";
      
       
    }

    public void performChange() {
    }

    public Object getComposite() {
        return usage;
        //return null;
    }

    public FileObject getParentFile() {
        return usage.getContainer().getFileObject();
    }

    public PositionBounds getPosition() {
        return null;
    }
    
     public void showPreview() {
        //UI.setComponentForRefactoringPreview(null);
     }
     
     public void openInEditor(){
         if(node != null ) {   
             Action preferredAction = node.getPreferredAction();
	         if (preferredAction != null) {
		     String command = (String)preferredAction.getValue(Action.ACTION_COMMAND_KEY);
		     ActionEvent ae = new ActionEvent(node, 0, command);
		     preferredAction.actionPerformed(ae);
	        }
         }
     
     }
         
         
     
    
}
