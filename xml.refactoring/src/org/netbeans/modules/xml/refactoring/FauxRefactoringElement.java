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
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImpl;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.refactoring.ui.util.AnalysisUtilities;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.PositionBounds;

/**
 *
 * @author Sonali
 */
public class FauxRefactoringElement extends SimpleRefactoringElementImpl {
    
    Usage usage;
    Node node;
    FileObject targetFobj;
    Referenceable ref;
    String name, label;
    
    /** Creates a new instance of WhereUsedElement */
    public FauxRefactoringElement(Referenceable ref) {
        this.ref = ref;
        Component targetComponent = ref instanceof Component ? (Component) ref : ref instanceof DocumentModel ? ((DocumentModel) ref).getRootComponent() :null;
            assert targetComponent != null : "target is not Component or DocumentModel";
            targetFobj = AnalysisUtilities.getFileObject(targetComponent);
                      
            UIHelper helper = RefactoringManager.getInstance().getTargetComponentUIHelper(ref);
            if (helper == null){
                helper = new UIHelper();
            }
            node = AnalysisUtilities.getDisplayNode(ref); 
            name = AnalysisUtilities.getName(ref); 
            label = " <b>" + name + "</b>";
    }

    public String getText() {
        return name;
    }

    public String getDisplayText() {
       return label;
      
       
    }

    public void performChange() {
    }

    public Object getComposite() {
        return ref;
        //return null;
    }

    public FileObject getParentFile() {
        return targetFobj;
    }
    
    

    public PositionBounds getPosition() {
        return null;
    }
    
     public void showPreview() {
         //UI.setComponentForRefactoringPreview(null);
     }
     
     public void openInEditor(){
         //System.out.println("openInEditor::called");
     }
     
     public void setEnabled(boolean enabled){
         usage.setIncludedInRefactoring(enabled);
         //System.out.println("setEnabled called with " + enabled);
     }
         
     
    
}
