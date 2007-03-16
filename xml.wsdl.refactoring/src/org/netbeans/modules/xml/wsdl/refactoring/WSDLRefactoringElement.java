/*
 * WSDLRefactoringElement.java
 *
 * Created on December 30, 2006, 4:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.refactoring;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.ui.actions.ShowSourceAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Sonali
 */
public class WSDLRefactoringElement extends SimpleRefactoringElementImplementation {
    
    private FileObject source;
    Model model;
    Component comp;
    Node node;
    
    /**
     * Creates a new instance of WSDLRefactoringElement
     */
    public WSDLRefactoringElement(Model model, Referenceable target, Component comp) {
        this.model=model;
        this.comp=comp;
        try {
            if(model instanceof WSDLModel) {
                ModelSource ms = model.getModelSource();
                source = (FileObject) ms.getLookup().lookup(FileObject.class);
                    if(source != null) {
                        DataObject dObj = DataObject.find(source);
                        if(dObj != null && dObj instanceof WSDLDataObject) {
                            this.node = NodesFactory.getInstance().create(comp);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        
    }

    public FileObject getParentFile() {
       if (source == null) {
            assert model != null : "Invalid object, expecting non-null model."; //NOI18N
            source = (FileObject)model.getModelSource().getLookup().lookup(FileObject.class);
            assert source != null : "ModelSource should have FileObject in lookup"; //NOI18N
        }
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
    //     System.out.println("XMLRefactoringElement:: openInEditor called");
         Action preferredAction = SystemAction.get(ShowSourceAction.class);
         String command = (String)preferredAction.getValue(Action.ACTION_COMMAND_KEY);
	 ActionEvent ae = new ActionEvent(node, 0, command);
	 preferredAction.actionPerformed(ae);
     
     }

    public Lookup getLookup() {
        return Lookups.singleton(comp); 
    }
     
}
