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
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
//import org.netbeans.modules.xml.xam.dom.DocumentComponent;



import java.beans.BeanInfo;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.bpel.model.api.BpelEntity;

import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.bpel.refactoring.Util;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation;
import org.netbeans.modules.xml.xam.Component;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.bpel.editors.api.nodes.FactoryAccess;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.03.16
 */
public class Element implements RefactoringElementImplementation, TreeElement {
    
    public Element(Component component) {
        this.component = component;
        this.node = FactoryAccess.getRefactoringNodeFactory().createNode(component);
    }
/*   
    Element(RefactoringElement element) {
        this((BpelEntity) element.getLookup().lookup(BpelEntity.class));
        this.element = element;
    }
*/    
    public Lookup getLookup() {
       return Lookups.singleton(component);
    }

    public FileObject getParentFile() {
        FileObject source = (FileObject)component.getModel().getModelSource().getLookup().lookup(FileObject.class);
        assert source != null : "ModelSource should have FileObject in lookup"; //NOI18N
        
        return source;
    }

    public TreeElement getParent(boolean isLogical) {
         TreeElement result = null;

         if (component.getParent() != null)
             return TreeElementFactory.getTreeElement(component.getParent());
         else {
             FileObject fo = (FileObject) component.getModel().getModelSource().getLookup().lookup(FileObject.class);
             return TreeElementFactory.getTreeElement(fo);
         }
    }

    public String getText() {
        return node.getName();
    }

    public String getText(boolean isLogical) {
    /*
        if(element!= null){ 
            String htmlDisplayName = node.getHtmlDisplayName();
            String usageTreeNodeLabel =
                            MessageFormat.format(
                            NbBundle.getMessage(
                            BPELRefactoringTreeElement.class,
                            "LBL_Usage_Node"),
                            new Object[] {
                        node.getName(),
                        node.getShortDescription(),  // component type
                        htmlDisplayName==null?"":htmlDisplayName
                    });
             return usageTreeNodeLabel;
        } else*/
//            return node.getName();
      return getText();
    }

    public String getDisplayText() {
      String text = node.getHtmlDisplayName();

      if (text == null) {
        return "!!!!! === RefactoringNodeFactory returns null === !!!!!"; // todo r
      }
        return text;
    }

    public Icon getIcon() {
       return new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
    }

    public void performChange() {
    }

   public PositionBounds getPosition() {
        return null;
    }
    
         
   public void showPreview() {}

   public void openInEditor() {
//todo m
org.netbeans.modules.bpel.editors.api.utils.Util.goToSource((BpelEntity) component);
              
     }
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public void undoChange() {
    }

    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Object getUserObject() {
//        if(element != null)
//            return element;
//        else 
            return component;
    }

    private boolean enabled = true;
    private int status = NORMAL;

//    private RefactoringElement element;
    private Node node; // todo r
    private Component component;
}
