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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.refactoring.ui.tree;

import java.beans.BeanInfo;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Becicka
 */
public class WSDLTreeElement implements TreeElement { 
    
    RefactoringElement element = null;
    Component comp;
    Node node = null;
  
       
    WSDLTreeElement(RefactoringElement element) {
        this.element = element;
        this.comp = (WSDLComponent)element.getComposite();
                
        assert comp instanceof WSDLComponent:"This TreeElement handles WSDLComponents only";
        
        try {
            Model model = comp.getModel();
            if(model instanceof WSDLModel) {
                ModelSource ms = model.getModelSource();
                FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
                    if(fo != null) {
                        DataObject dObj = DataObject.find(fo);
                        if(dObj != null && dObj instanceof WSDLDataObject) {
                            node = NodesFactory.getInstance().create(comp);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        
        //TO DO if the node is null??? 
   }
    
    WSDLTreeElement(Object element) {
        this.comp = (WSDLComponent)element;
       
        try {
            Model model = comp.getModel();
            if(model instanceof WSDLModel) {
                ModelSource ms = model.getModelSource();
                FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
                    if(fo != null) {
                        DataObject dObj = DataObject.find(fo);
                        if(dObj != null && dObj instanceof WSDLDataObject) {
                            node = NodesFactory.getInstance().create(comp);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public TreeElement getParent(boolean isLogical) {
        TreeElement result = null;
            if(comp.getParent() != null ) {
                return TreeElementFactory.getTreeElement(comp.getParent());
            } else {
                FileObject fo = (FileObject) comp.getModel().getModelSource().getLookup().lookup(FileObject.class);
                return TreeElementFactory.getTreeElement(fo);
            }
                 
     }
                
         
 
    public Icon getIcon() {
         return new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
       
        
    }

    public String getText(boolean isLogical) {
        if(element != null ) {
            String htmlDisplayName = node.getHtmlDisplayName();
            String usageTreeNodeLabel =
                            MessageFormat.format(
                            NbBundle.getMessage(
                            WSDLTreeElement.class,
                            "LBL_Usage_Node"),
                            new Object[] {
                        node.getName(),
                        node.getShortDescription(),  // comp type
                        htmlDisplayName==null?"":htmlDisplayName
                    });
            return usageTreeNodeLabel;
        
        } else 
            return node.getName();
       
    }

    public Object getUserObject() {
         if(element != null)
            return element;
        else
            return comp;
    
    }
}
