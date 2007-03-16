/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.refactoring;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImpl;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.PositionBounds;
import org.openide.util.lookup.Lookups;
import org.openide.util.actions.SystemAction;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.03.16
 */
public class Element extends SimpleRefactoringElementImpl {
    
    Component comp;
    Node node;
    /**
     * Creates a new instance of Element
     */
    public Element(Component comp) {
        this.comp=comp;
        this.node = Util.getDisplayNode(comp);
    }

   
    public Object getComposite() {
       return comp;
    }

    public FileObject getParentFile() {
        FileObject source = (FileObject)comp.getModel().getModelSource().getLookup().lookup(FileObject.class);
        assert source != null : "ModelSource should have FileObject in lookup"; //NOI18N
        
        return source;
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
         //System.out.println("XMLRefactoringElement:: openInEditor called");
              
     }
}
