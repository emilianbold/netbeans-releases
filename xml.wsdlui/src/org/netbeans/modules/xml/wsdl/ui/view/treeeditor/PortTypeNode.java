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


package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.xml.namespace.QName;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;

//import org.netbeans.modules.xml.refactoring.actions.FindUsagesAction;
//import org.netbeans.modules.xml.refactoring.actions.RefactorAction;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.CommonAddExtensibilityAttributeAction;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.actions.RemoveAttributesAction;
import org.netbeans.modules.xml.wsdl.ui.commands.ConstraintNamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.BindingAndServiceNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.DocumentationNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.PortTypeOperationNewType;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.openide.ErrorManager;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;



/**
 * @author Ritesh Adval
 *
 */
public class PortTypeNode extends WSDLNamedElementNode<PortType> {

    private PortType mWSDLConstruct;
   
    private static Image ICON  = Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/portType.png");    

    private PortTypePropertyAdapter mPropertyAdapter;

    private static final SystemAction[] ACTIONS = new SystemAction[] {
        SystemAction.get(CutAction.class),
        SystemAction.get(CopyAction.class),
        SystemAction.get(PasteAction.class),
        null,
        SystemAction.get(NewAction.class),
        SystemAction.get(DeleteAction.class),
        null,
        SystemAction.get(CommonAddExtensibilityAttributeAction.class),
        SystemAction.get(RemoveAttributesAction.class),
        null,
        SystemAction.get(GoToAction.class),
        //SystemAction.get(FindUsagesAction.class),
        (SystemAction)RefactoringActionsFactory.whereUsedAction(),
        null,
        (SystemAction)RefactoringActionsFactory.editorSubmenuAction(),
        //SystemAction.get(RefactorAction.class),
        null,
        SystemAction.get(PropertiesAction.class)
    };
    
    public PortTypeNode(PortType wsdlConstruct) {
        super(new GenericWSDLComponentChildren<PortType>(wsdlConstruct), wsdlConstruct, new PortTypeNewTypesFactory());
        mWSDLConstruct = wsdlConstruct;
        
        this.mPropertyAdapter = new PortTypePropertyAdapter();
        super.setNamedPropertyAdapter(this.mPropertyAdapter);
    }
    
    
    @Override
    public Image getIcon(int type) {
        return ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return ACTIONS;
    }

     @Override
    protected Node.Property createAttributeProperty(QName attrQName) {
            Node.Property attrValueProperty = null;
            try {
            String attrName = attrQName.getLocalPart();
             //name
            if(attrName.equals(PortType.NAME_PROPERTY)) { 
                //name
                attrValueProperty = createNameProperty();
            }  else {
                attrValueProperty = super.createAttributeProperty(attrQName);
             }
            
            } catch(Exception ex) {
                mLogger.log(Level.SEVERE, "failed to create property sheet for "+ getWSDLComponent(), ex);
                ErrorManager.getDefault().notify(ex);
            }
             return attrValueProperty;
        }
    
     
      @Override
    protected List<Node.Property> createAlwaysPresentAttributeProperty() throws Exception {
            ArrayList<Node.Property> alwaysPresentAttrProperties = new ArrayList<Node.Property>();
            alwaysPresentAttrProperties.add(createNameProperty());
            
            return alwaysPresentAttrProperties;
    }
    
    
      private Node.Property createNameProperty() throws NoSuchMethodException {
          Node.Property attrValueProperty;
          attrValueProperty = new BaseAttributeProperty(mPropertyAdapter, String.class, PortType.NAME_PROPERTY);
          attrValueProperty.setName(PortType.NAME_PROPERTY);
          attrValueProperty.setDisplayName(NbBundle.getMessage(PortTypeNode.class, "PROP_NAME_DISPLAYNAME"));
          attrValueProperty.setShortDescription(NbBundle.getMessage(PortTypeNode.class, "PORTTYPE_NAME_DESC"));
            
          return attrValueProperty;
      }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(PortTypeNode.class);
    }
    
    
    public class PortTypePropertyAdapter extends ConstraintNamedPropertyAdapter {
        
        public PortTypePropertyAdapter() {
            super(mWSDLConstruct);
        }
         
        @Override
        public boolean isNameExists(String name) {
            WSDLModel document = mWSDLConstruct.getModel();
            return NameGenerator.getInstance().isPortTypeExists(name, document);
        }
    }
    
    public static final class PortTypeNewTypesFactory implements NewTypesFactory{

        public NewType[] getNewTypes(WSDLComponent def) {
            ArrayList<NewType> list = new ArrayList<NewType>();
            list.add(new PortTypeOperationNewType(def));
            list.add(new BindingAndServiceNewType(def));
            if (def.getDocumentation() == null) {
                list.add(new DocumentationNewType(def));
            }
            return list.toArray(new NewType[]{});
        }        

    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(PortTypeNode.class, "LBL_PortTypeNode_TypeDisplayName");
    }
}
