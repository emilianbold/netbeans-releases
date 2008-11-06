/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

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
import org.netbeans.modules.xml.refactoring.spi.RefactoringUtil;
import org.netbeans.modules.xml.refactoring.ui.DeleteRefactoringUI;
import org.netbeans.modules.xml.refactoring.ui.FileRenameRefactoringUI;
import org.netbeans.modules.xml.refactoring.ui.RenameRefactoringUI;
import org.netbeans.modules.xml.refactoring.ui.WhereUsedQueryUI;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
//import org.netbeans.modules.xml.refactoring.ui.CopyRefactoringUI;
import org.netbeans.modules.xml.refactoring.ui.CopyRefactoringUI;
import org.netbeans.modules.xml.refactoring.ui.MoveRefactoringUI;
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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class)
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
         Referenceable ref = SharedUtils.getReferenceable(n);
               
         return ref instanceof Referenceable;        
      
 }

  public void doFindUsages(Lookup lookup) {
    Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
    Node[] n = nodes.toArray(new Node[0]);
    
    Referenceable ref = SharedUtils.getReferenceable(n);
    assert ref != null:"The node's NamedReferenceable should not be null";
    //WhereUsedView wuv = new WhereUsedView(ref);
    WhereUsedQueryUI ui = new WhereUsedQueryUI(ref);
    UI.openRefactoringUI(ui);
  
  }
  
 public boolean canRename(Lookup lookup) {
    Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
         if(nodes.size() !=1){
             return false;
         }
             
         Node[] n = nodes.toArray(new Node[0]);
         Referenceable ref = SharedUtils.getReferenceable(n);
         if ( ref instanceof Model && RefactoringUtil.isWritable((Model)ref) )
             return true;
         if ( ref instanceof Nameable  &&  RefactoringUtil.isWritable(SharedUtils.getModel(ref)) ) {
             return true;
          }
         
     return false;
 }

 public void doRename(Lookup lookup) {
     Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
     Node[] n = nodes.toArray(new Node[0]);
   
     final Referenceable ref = SharedUtils.getReferenceable(n);
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
         Referenceable ref = SharedUtils.getReferenceable(n);
        
        if (ref instanceof Component && ((Component)ref).getParent() == null) {
            return false;
        }
        if( ref instanceof NamedReferenceable && RefactoringUtil.isWritable(SharedUtils.getModel(ref)) && n[0].canDestroy() )
            return true;
        
        return false;
    }
 
 public void doDelete(Lookup lookup){
      Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
      Node[] n = nodes.toArray(new Node[0]);
      
      
      final Referenceable ref = SharedUtils.getReferenceable(n);
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
    
          
  public boolean canMove(Lookup lookup) {
       Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
         if(nodes.size() !=1){
             return false;
         }
       
         Node[] n = nodes.toArray(new Node[0]);
         DataObject dob = n[0].getCookie(DataObject.class);
         if (dob==null || !dob.isValid()) {
             return false;
         }
          
         Referenceable ref = SharedUtils.getReferenceable(n);
         if(ref == null)
             return false;
         if ( ref instanceof Model && RefactoringUtil.isWritable((Model)ref) )
             return true;
         
         return false;
   }


   public void doMove(final Lookup lookup) {
       Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
       Node[] n = nodes.toArray(new Node[0]);
   
       final Referenceable ref = SharedUtils.getReferenceable(n);
       final EditorCookie ec = lookup.lookup(EditorCookie.class);
       RefactoringUI ui = null;
       if(ref instanceof Model) {
           Model model = (Model) ref;
           ui = new MoveRefactoringUI(model);
       } 
           
      if(isFromEditor(ec)){
           TopComponent activetc = TopComponent.getRegistry().getActivated();
           UI.openRefactoringUI(ui, activetc);
      } else {
           UI.openRefactoringUI(ui);
     }     
   }
   
   
   public boolean canCopy(Lookup lookup) {
       Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
         if(nodes.size() !=1){
             return false;
         }
             
         Node[] n = nodes.toArray(new Node[0]);
         Referenceable ref = SharedUtils.getReferenceable(n);
         if(ref == null)
             return false;
         if ( ref instanceof Model && RefactoringUtil.isWritable((Model)ref) )
             return true;
         
         return false;
   }


   public void doCopy(final Lookup lookup) {
       Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
       Node[] n = nodes.toArray(new Node[0]);
   
       final Referenceable ref = SharedUtils.getReferenceable(n);
       final EditorCookie ec = lookup.lookup(EditorCookie.class);
       RefactoringUI ui = null;
       if(ref instanceof Model) {
           Model model = (Model) ref;
           ui = new CopyRefactoringUI(model);
       } 
           
      if(isFromEditor(ec)){
           TopComponent activetc = TopComponent.getRegistry().getActivated();
           UI.openRefactoringUI(ui, activetc);
      } else {
           UI.openRefactoringUI(ui);
     }     
   }
}

