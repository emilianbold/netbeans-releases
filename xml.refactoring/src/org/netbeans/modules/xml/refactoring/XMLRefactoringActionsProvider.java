/*
 * XMLRefactoringActionsProvider.java
 *
 * Created on December 20, 2006, 10:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.refactoring;

import java.util.Collection;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.xml.refactoring.impl.RefactoringUtil;
import org.netbeans.modules.xml.refactoring.ui.j.spi.ui.DeleteRefactoringUI;
import org.netbeans.modules.xml.refactoring.ui.j.spi.ui.FileRenameRefactoringUI;
import org.netbeans.modules.xml.refactoring.ui.j.spi.ui.RenameRefactoringUI;
import org.netbeans.modules.xml.refactoring.ui.j.spi.ui.WhereUsedQueryUI;
import org.netbeans.modules.xml.refactoring.ui.util.AnalysisUtilities;
import org.netbeans.modules.xml.refactoring.ui.views.WhereUsedView;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Sonali
 */
public class XMLRefactoringActionsProvider extends ActionsImplementationProvider {
    
    /** Creates a new instance of XMLRefactoringActionsProvider */
    public XMLRefactoringActionsProvider() {
    }
     public boolean canFindUsages(Lookup lookup) {
         //System.out.println("XMLRefactoringActionsProvider: canFindUsages called");
         Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
         if(nodes.size() !=1){
             return false;
         }
             
         Node[] n = nodes.toArray(new Node[0]);
         Referenceable ref = AnalysisUtilities.getReferenceable(n);
               
         return ref instanceof Referenceable;        
      
 }

  public void doFindUsages(Lookup lookup) {
    Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
    Node[] n = nodes.toArray(new Node[0]);
    
    Referenceable ref = AnalysisUtilities.getReferenceable(n);
    assert ref != null:"The node's NamedReferenceable should not be null";
    WhereUsedView wuv = new WhereUsedView(ref);
    WhereUsedQueryUI ui = new WhereUsedQueryUI(wuv, ref);
    UI.openRefactoringUI(ui);
  
  }
  
 public boolean canRename(Lookup lookup) {
//     System.out.println("my canRename called");
     Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
         if(nodes.size() !=1){
             return false;
         }
             
         Node[] n = nodes.toArray(new Node[0]);
         Referenceable ref = AnalysisUtilities.getReferenceable(n);
         if ( ref instanceof Model && RefactoringUtil.isWritable((Model)ref) )
             return true;
         if ( ref instanceof Nameable  &&  RefactoringUtil.isWritable(RefactorRequest.getModel(ref)) ) {
             return true;
          }
         
     return false;
 }

 public void doRename(Lookup lookup) {
     Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
     System.out.println("my rename impl called");
     Node[] n = nodes.toArray(new Node[0]);
   
     final Referenceable ref = AnalysisUtilities.getReferenceable(n);
     final EditorCookie ec = lookup.lookup(EditorCookie.class);
     RefactoringUI ui = null;
     if(ref instanceof Model) {
         Model model = (Model) ref;
         ui = new FileRenameRefactoringUI(model);
     } else if(ref instanceof Nameable) {
          ui = new RenameRefactoringUI(Nameable.class.cast(ref));
     }
           
     if(isFromEditor(ec)){
           TopComponent activetc = TopComponent.getRegistry().getActivated();
           UI.openRefactoringUI(ui, activetc);
     } else {
           UI.openRefactoringUI(ui);
    } 
        
   
   }  
 
 public boolean canDelete(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if(nodes.size() !=1){
             return false;
         }
             
         Node[] n = nodes.toArray(new Node[0]);
         Referenceable ref = AnalysisUtilities.getReferenceable(n);
        
        if (ref instanceof Component && ((Component)ref).getParent() == null) {
            return false;
        }
        if( ref instanceof NamedReferenceable && RefactoringUtil.isWritable(RefactorRequest.getModel(ref)) && n[0].canDestroy() )
            return true;
        
        return false;
    }
 
 public void doDelete(Lookup lookup){
      Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
      System.out.println("my delete impl called");
      Node[] n = nodes.toArray(new Node[0]);
      
      
      final Referenceable ref = AnalysisUtilities.getReferenceable(n);
      final EditorCookie ec = lookup.lookup(EditorCookie.class);
   
      DeleteRefactoringUI ui = new DeleteRefactoringUI(NamedReferenceable.class.cast(ref));
          
      if(isFromEditor(ec)){
           TopComponent activetc = TopComponent.getRegistry().getActivated();
           UI.openRefactoringUI(ui, activetc);
      } else {
           UI.openRefactoringUI(ui);
      } 
      
 }
 
 private boolean isFromEditor(EditorCookie ec) {
        if (ec != null && ec.getOpenedPanes() != null) {
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (activetc instanceof CloneableEditorSupport.Pane) {
                return true;
            }
        }
        return false;
  }
    
          
   
public class FindUsagesRunnable implements Runnable {
        private Node[] nodes;
        private RefactoringUI ui;
        
        public FindUsagesRunnable(Node[] n) {
            this.nodes=n;
        }
        
        public final void run() {
          
            Referenceable ref = AnalysisUtilities.getReferenceable(nodes);
            assert ref != null:"The node's NamedReferenceable should not be null";
            WhereUsedView wuv = new WhereUsedView(ref);
            WhereUsedQueryUI ui = new WhereUsedQueryUI(wuv, ref);
            UI.openRefactoringUI(ui);
            
        }
  }


}

