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


package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.swing.Action;
import javax.xml.namespace.QName;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;

//import org.netbeans.modules.xml.refactoring.actions.FindUsagesAction;
//import org.netbeans.modules.xml.refactoring.actions.RefactorAction;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.commands.ConstraintNamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.NamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.cookies.CreateBindingFromOperationCookie;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.model.StringAttribute;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.property.ParameterOrderPropertyEditor;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.DocumentationNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementNewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.FaultNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.InputNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.OutputNewType;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.openide.ErrorManager;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;


/**
 *
 * @author Ritesh Adval
 *
 */
public abstract class OperationNode<T extends Operation> extends WSDLExtensibilityElementNode<T> {
    
    
    /** Icon for the Ip msg button.    */
    private static Image ICON  = Utilities.loadImage
             ("org/netbeans/modules/xml/wsdl/ui/view/resources/operation.png");
    
    protected Operation mWSDLConstruct;
    
    private OperationPropertyAdapter mPropertyAdapter;

    private static final SystemAction[] ACTIONS = new SystemAction[]{
        SystemAction.get(CutAction.class),
        SystemAction.get(CopyAction.class),
        SystemAction.get(PasteAction.class),
        null,
        SystemAction.get(NewAction.class),
        SystemAction.get(DeleteAction.class),
//        null,
//        SystemAction.get(CreateBindingFromOperationAction.class),
        null,
        SystemAction.get(GoToAction.class),
        //SystemAction.get(FindUsagesAction.class),
        (SystemAction)RefactoringActionsFactory.whereUsedAction(),
        null,
        (SystemAction)RefactoringActionsFactory.editorSubmenuAction(),
        null,
        SystemAction.get(PropertiesAction.class)
    };

    public OperationNode(T wsdlConstruct) {
        super(wsdlConstruct, new OperationNewTypesFactory());
        mWSDLConstruct = wsdlConstruct;
        
        
        this.mPropertyAdapter = new OperationPropertyAdapter();
        super.setNamedPropertyAdapter(this.mPropertyAdapter);
        getLookupContents().add(new CreateBindingFromOperationCookie(mWSDLConstruct));
    }
    
    @Override
    public String getNameInLayer() {
        return WSDLExtensibilityElements.ELEMENT_PORTTYPE_OPERATION;
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
            if(attrName.equals(Operation.NAME_PROPERTY)) { 
                attrValueProperty = createNameProperty();
            } else if(attrName.equals(Operation.PARAMETER_ORDER_PROPERTY)) { 
                //optional parameterOrder
                attrValueProperty = createParameterOrderProperty();
            } else {
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
        alwaysPresentAttrProperties.add(createParameterOrderProperty());
        return alwaysPresentAttrProperties;
    }
    
    
      private Node.Property createNameProperty() throws NoSuchMethodException {
          Node.Property attrValueProperty;
          attrValueProperty = new BaseAttributeProperty(mPropertyAdapter, String.class, Operation.NAME_PROPERTY);
          attrValueProperty.setName(Operation.NAME_PROPERTY);
          attrValueProperty.setDisplayName(NbBundle.getMessage(OperationNode.class, "PROP_NAME_DISPLAYNAME"));
          attrValueProperty.setShortDescription(NbBundle.getMessage(OperationNode.class, "OPERATION_NAME_DESC"));
          return attrValueProperty;
      }
      
      private Node.Property createParameterOrderProperty() throws NoSuchMethodException {
          Node.Property attrValueProperty;
          attrValueProperty = new ParameterOrderProperty(this, mPropertyAdapter, String.class, Operation.PARAMETER_ORDER_PROPERTY);  //NOI18N 
          attrValueProperty.setName(Operation.PARAMETER_ORDER_PROPERTY);
          attrValueProperty.setDisplayName(NbBundle.getMessage(OperationNode.class, "PROP_PARAMETER_ORDER_DISPLAYNAME"));
          attrValueProperty.setShortDescription(NbBundle.getMessage(OperationNode.class, "OPERATION_PARAM_ORDER_DESC"));
                
          return attrValueProperty;
      }
      
    
    public class OperationPropertyAdapter extends ConstraintNamedPropertyAdapter implements NamedPropertyAdapter {
        
        public OperationPropertyAdapter() {
            super(getWSDLComponent());
        }

        public void setParameterOrder(String paramOrder) {
            String paramOrderNewValue = paramOrder != null ? paramOrder.trim() : null;
            
            if (paramOrderNewValue != null && paramOrderNewValue.length() == 0) {
                paramOrderNewValue = null;
            }

            String paramOrderOldValue = mWSDLConstruct.getAttribute(new StringAttribute(Operation.PARAMETER_ORDER_PROPERTY));

            //if one is null and other is not OR non-null values are unequal, value changed.
            if ((paramOrderOldValue == null ^ paramOrderNewValue == null) || 
                    (paramOrderOldValue != null && paramOrderNewValue != null && !paramOrderNewValue.equals(paramOrderOldValue))) {

                List<String> parts = null;

                if (paramOrderNewValue != null) {
                    parts = new ArrayList<String>();
                    StringTokenizer st = new StringTokenizer(paramOrderNewValue, " ");
                    while(st.hasMoreElements()) {
                        String part = (String) st.nextElement();
                        parts.add(part);
                    }
                }



                getWSDLComponent().getModel().startTransaction();
                mWSDLConstruct.setParameterOrder(parts);
                getWSDLComponent().getModel().endTransaction();
            }



        }

        public String getParameterOrder() {
            if(mWSDLConstruct.getParameterOrder() == null) {
                return "";
            }

            StringBuffer partList = new StringBuffer(10);

            List<String> parts = mWSDLConstruct.getParameterOrder();
            for (String part : parts) {
                partList.append(part);
                partList.append(" ");
            }
            return partList.toString().trim();

        }
         
         @Override
         public boolean isNameExists(String name) {
             return NameGenerator.getInstance().isOperationExists(name, (PortType) mWSDLConstruct.getParent());
         }
    }
    
    
    public class ParameterOrderProperty extends BaseAttributeProperty {
        private OperationNode mOperationNode;
            
        public ParameterOrderProperty(OperationNode node, OperationPropertyAdapter instance, Class valueType, String propertyName) throws NoSuchMethodException {
            super(instance, valueType, propertyName);
            this.mOperationNode = node;
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            return new ParameterOrderPropertyEditor(mOperationNode);
        }
        
        
        
    }
    
    public static final class OperationNewTypesFactory implements NewTypesFactory{

        public NewType[] getNewTypes(WSDLComponent def) {
            
            ArrayList<NewType> list = new ArrayList<NewType>();
            if (def.getDocumentation() == null) {
                list.add(new DocumentationNewType(def));
            }
            Operation operation = (Operation) def;
            if (def instanceof NotificationOperation) {
               if (operation.getOutput() == null) {
                   list.add(new OutputNewType(operation));
               }
            }
            
            if (def instanceof OneWayOperation) {
                if (operation.getInput() == null) {
                    list.add(new InputNewType(operation));
                }
            }
            
            if (def instanceof RequestResponseOperation) {
                if (operation.getInput() == null) {
                    list.add(new InputNewType(operation));
                }
                if (operation.getOutput() == null) {
                    list.add(new OutputNewType(operation));
                }
                list.add(new FaultNewType(def));
            }
            
            if (def instanceof SolicitResponseOperation) {
                if (operation.getOutput() == null) {
                    list.add(new OutputNewType(operation));
                }
                if (operation.getInput() == null) {
                    list.add(new InputNewType(operation));
                }
                list.add(new FaultNewType(def));
            }
            list.addAll(Arrays.asList(new ExtensibilityElementNewTypesFactory(WSDLExtensibilityElements.ELEMENT_PORTTYPE_OPERATION).getNewTypes(def)));
            return list.toArray(new NewType[]{});
        }        

    }
}
