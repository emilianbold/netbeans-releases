/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * Created on Jun 7, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;

import javax.swing.Action;

import org.netbeans.modules.xml.refactoring.actions.FindUsagesAction;
import org.netbeans.modules.xml.refactoring.actions.RefactorAction;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.ui.commands.CommonAttributePropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.PropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;



/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DocumentationNode extends WSDLElementNode {

    protected Documentation mWSDLConstruct;
    
    Image ICON  =Utilities.loadImage
         ("org/netbeans/modules/xml/wsdl/ui/view/resources/documentation.png");
    
    private DocumentationPropertyAdapter mPropertyAdapter;

    private static final SystemAction[] ACTIONS = new SystemAction[] {
        SystemAction.get(CutAction.class),
        SystemAction.get(CopyAction.class),
        SystemAction.get(PasteAction.class),
        null,
        SystemAction.get(DeleteAction.class),
        null,
        SystemAction.get(GoToAction.class),
        SystemAction.get(FindUsagesAction.class),
        null,
        SystemAction.get(RefactorAction.class),
        null,
        SystemAction.get(PropertiesAction.class),
    };

    public DocumentationNode(Documentation wsdlConstruct) {
        super(Children.LEAF, wsdlConstruct);
        mWSDLConstruct = wsdlConstruct;
        this.setDisplayName(NbBundle.getMessage(TypesNode.class, "DOCUMENTATION_NODE_NAME"));
        
        this.mPropertyAdapter = new DocumentationPropertyAdapter();
    }
    
    
    @Override
    public Image getIcon(int type) {
        return ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }

    public Object getWSDLConstruct() {
        return mWSDLConstruct;
    }

    @Override
    public Action[] getActions(boolean context) {
        return ACTIONS;
    }
    
    @Override
    protected void refreshAttributesSheetSet() {
        Sheet.Set ss = createPropertiesSheetSet();
        try {
            //value
            Node.Property nameProperty = new BaseAttributeProperty(mPropertyAdapter, String.class, CommonAttributePropertyAdapter.VALUE);
            
            
            nameProperty.setName(NbBundle.getMessage(DocumentationNode.class, "DOC_NODE_Documentation_text"));
            nameProperty.setShortDescription(NbBundle.getMessage(DocumentationNode.class, "DOC_NODE_Documentation_text"));
            ss.put(nameProperty);
            
            
        } catch(Exception ex) {
            mLogger.log(Level.SEVERE, "failed to create property sheet for "+ mWSDLConstruct, ex);
        }
    }
    
    public void nodeValueChanged(PropertyChangeEvent evt) {
//        fire a propertysets change so that property sheet
        //can be refreshed
        this.firePropertySetsChange(new Node.PropertySet[] {}, this.getPropertySets());
    }
    
    public class DocumentationPropertyAdapter extends PropertyAdapter {
        
        public DocumentationPropertyAdapter() {
            super(mWSDLConstruct);
        }
        
        public void setValue(String value) {
            mWSDLConstruct.getModel().startTransaction();
            mWSDLConstruct.setTextContent(value);
                mWSDLConstruct.getModel().endTransaction();
         }
         
         public String getValue() {
             if(mWSDLConstruct.getTextContent() == null) {
                 return "";
             }
             
             return mWSDLConstruct.getTextContent();
         }
         
    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(DocumentationNode.class, "LBL_DocumentationNode_TypeDisplayName");
    }
}

