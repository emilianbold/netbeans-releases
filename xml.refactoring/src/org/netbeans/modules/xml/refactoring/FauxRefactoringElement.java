/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
         UI.setComponentForRefactoringPreview(null);
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
