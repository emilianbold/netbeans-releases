/*
 * WhereUsedElement.java
 *
 * Created on December 30, 2006, 4:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.refactoring;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.refactoring.spi.AnalysisUtilities;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Sonali
 */
public class FauxRefactoringElement extends SimpleRefactoringElementImplementation {
    
    Node node;
    FileObject targetFobj;
    Referenceable ref;
    String label, name, type;
    
    
    public FauxRefactoringElement(Referenceable ref, String refactoringType) {
        this.ref = ref;
        Component targetComponent = ref instanceof Component ? (Component) ref : ref instanceof DocumentModel ? ((DocumentModel) ref).getRootComponent() :null;
        assert targetComponent != null : "target is not Component or DocumentModel";
        targetFobj = SharedUtils.getFileObject(targetComponent);
        type = refactoringType; 
        //node = AnalysisUtilities.getDisplayNode(ref); 
        name = SharedUtils.getName(ref); 
        label = " <b>" + refactoringType + " " + name + "</b>";
    }
    
        

    public String getText() {
        return SharedUtils.getName(ref);
    }

    public String getDisplayText() {
       return label;
      
       
    }

    public void performChange() {
    }

    public Lookup getLookup() {
        return Lookups.singleton(this);
    }

    public FileObject getParentFile() {
        return targetFobj;
    }
    
    

    public PositionBounds getPosition() {
        Model mod= SharedUtils.getModel(ref);
        Document doc=    ((AbstractDocumentModel)mod).getBaseDocument();    
            Position start = doc.getStartPosition();
            Position end = doc.getEndPosition();
            DataObject dob = null;
        try {
            FileObject source = mod.getModelSource().getLookup().lookup(FileObject.class);
            
               dob = DataObject.find(source);
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
            
       CloneableEditorSupport ces = SharedUtils.findCloneableEditorSupport(dob);
       PositionRef ref1 = ces.createPositionRef(start.getOffset(), Bias.Forward);
       PositionRef ref2 = ces.createPositionRef(end.getOffset(), Bias.Forward);
       PositionBounds bounds = new PositionBounds(ref1, ref2);
       return bounds; 
    }
    
     public void showPreview() {
         //UI.setComponentForRefactoringPreview(null);
     }
     
     public void openInEditor(){
         //System.out.println("openInEditor::called");
     }
     
     public void setEnabled(boolean enabled){
      
     }
         
     public String getRefactoringType() {
         return type;
     }
     
     public Referenceable getTarget(){
         return ref;
         
     }
    
    
}
