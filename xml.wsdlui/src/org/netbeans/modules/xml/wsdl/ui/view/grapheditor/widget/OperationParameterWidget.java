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
 * LabelAndTextFieldWidget.java
 *
 * Created on August 21, 2006, 7:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.LabelWidget.Alignment;
import org.netbeans.api.visual.widget.LabelWidget.VerticalAlignment;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.ComboBoxInplaceEditor;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.ComboBoxInplaceEditorProvider;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.WidgetEditCookie;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.MessageNode;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.DisplayObject;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.actions.NewAction;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class OperationParameterWidget extends AbstractWidget<OperationParameter>
        implements DnDHandler {

    private LabelWidget mParameterMessage;
    private LabelWidget mNameLabel;
    private OperationParameter mParameter;
	private WidgetAction editorAction;
    

    /**
     * Creates a new instance of LabelAndTextFieldWidget.
     *
     * @param  scene      the widget Scene.
     * @param  parameter  the corresponding WSDL component.
     * @param  lookup     the Lookup for this widget.
     */
    public OperationParameterWidget(Scene scene, OperationParameter parameter,
            Lookup lookup) {
        super(scene, parameter, lookup);

        mParameter = parameter;
        setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.JUSTIFY, 1));
        mParameterMessage = new LabelWidget (scene);
        mParameterMessage.setBackground (Color.WHITE);
        mParameterMessage.setLabel(getMessageName());
        mParameterMessage.setVerticalAlignment(VerticalAlignment.CENTER);
        mParameterMessage.setAlignment(Alignment.CENTER);
        mParameterMessage.setBorder(WidgetConstants.EMPTY_2PX_BORDER);
        editorAction = ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {

            public void setText(Widget w, String text) {
                if (getWSDLComponent() != null && !getWSDLComponent().getName().equals(text))
                    SharedUtils.locallyRenameRefactor(getWSDLComponent(), text);
            }

            public boolean isEnabled(Widget w) {
                if (getWSDLComponent() != null) {
                    return !isImported() && XAMUtils.isWritable(getWSDLComponent().getModel());
                }
                return false;
            }

            public String getText(Widget w) {
                return mParameter.getName();
            }

        }, null);
        mParameterMessage.getActions().addAction(ActionFactory.createInplaceEditorAction(new ComboBoxInplaceEditorProvider(new ComboBoxInplaceEditor() {
            public boolean isEnabled(Widget widget) {
                if (getWSDLComponent() != null) {
                    return !isImported() && XAMUtils.isWritable(getWSDLComponent().getModel());
                }
                return false;
            }


            public boolean getEditable() {
                return false;
            }

            public ComboBoxModel getModel() {
                Vector<DisplayObject> list = getAllMessages(mParameter.getModel());
                DefaultComboBoxModel model = new DefaultComboBoxModel(list);
                if (getSelectedItem() != null) {
                    if (getSelectedItem() != null) {
                        DisplayObject selectedObject = null;
                        for (DisplayObject dispObj : list) {
                            if (dispObj.getValue().equals(getSelectedItem())) {
                                selectedObject = dispObj;
                                break;
                            }
                        }
                        if (selectedObject != null)
                            model.setSelectedItem(selectedObject);
                    }
                }
                return model;
            }

            public Object getSelectedItem() {
                if (mParameter.getMessage() != null) {
                    return mParameter.getMessage().get();
                }
                return null;
            }

            public void setSelectedItem(Object selectedItem) {
                WSDLModel model = mParameter.getModel();
                boolean newlyCreated = false;
                Message message = null;
                if (selectedItem instanceof DisplayObject) {
                    DisplayObject dispObj = (DisplayObject) selectedItem;
                    Object obj = dispObj.getValue();
                    if (obj instanceof Message) {
                        message = (Message) obj;
                    }
                    NamedComponentReference<Message> mesgRef = getWSDLComponent().getMessage();
                    if (mesgRef != null) {
                        Message msg = mesgRef.get();
                        if (msg != null && msg == message) return;
                    }
                } else if (selectedItem instanceof String) {
                    message = model.getFactory().createMessage();
                    message.setName((String) selectedItem);
                    newlyCreated = true;
                }
                
                
                try {
                    if (model.startTransaction()) {
                        if (newlyCreated) {
                            model.getDefinitions().addMessage(message);
                        }
                        
                        if (message != null) {
                            mParameter.setMessage(mParameter.createReferenceTo(
                                    message, Message.class));
                        }
                    }
                } finally {
                    model.endTransaction();
                }
            }

        },  EnumSet.<InplaceEditorProvider.ExpansionDirection>of (InplaceEditorProvider.ExpansionDirection.RIGHT))));
        
        if (parameter instanceof Fault) {
            Widget holderWidget = new Widget(scene);
            holderWidget.setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.CENTER, 0));
            addChild(holderWidget);
            
            Widget widget = new Widget(scene);
            widget.setLayout(LayoutFactory.createHorizontalFlowLayout(SerialAlignment.JUSTIFY, 2));
            holderWidget.addChild(widget);
            mParameter = parameter;
            mNameLabel = new LabelWidget (scene);
            mNameLabel.setBorder(WidgetConstants.EMPTY_2PX_BORDER);
            mNameLabel.setBackground (Color.WHITE);
            if (mParameter != null) {
                mNameLabel.setLabel (mParameter.getName());
                mNameLabel.getActions().addAction(editorAction);
            }
            Font font = scene.getDefaultFont ().deriveFont (Font.BOLD);
            mNameLabel.setFont (font);
            mNameLabel.setAlignment(Alignment.CENTER);
            widget.addChild (mNameLabel);
            widget.addChild(mParameterMessage);
        } else {
            addChild(mParameterMessage);
        }
        getActions().addAction(new WidgetAction.Adapter() {
            
            @Override
            public State keyPressed (Widget widget, WidgetKeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_F2 || event.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (editorAction == null || mNameLabel == null) return State.CONSUMED;
                    InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                    if (inplaceEditorController.openEditor (mNameLabel)) {
                        return State.createLocked (widget, this);
                    }
                    return State.CONSUMED;
                }
                return State.REJECTED;
            }
        
        });
        getActions().addAction(((PartnerScene) getScene()).getDnDAction());
        getLookupContent().add(new WidgetEditCookie() {
        
            public void edit() {
                InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                inplaceEditorController.openEditor (mNameLabel);
            }
            
            public void close() {
                InplaceEditorProvider.EditorController inplaceEditorController = ActionFactory.getInplaceEditorController (editorAction);
                inplaceEditorController.closeEditor(false);
            }
        
        });
    }
    
    public void setText(String text) {
        mParameterMessage.setLabel(text);
    }
    
    public String getText() {
        return this.mParameterMessage.getLabel();
    }
    
    private String getMessageName() {
        if (getWSDLComponent().getMessage() != null &&
                getWSDLComponent().getMessage().get() != null) {
            return getWSDLComponent().getMessage().get().getName();
        }
        return NbBundle.getMessage(OperationParameterWidget.class,
                "LBL_OperationParamterWidget_NoMessageSelected");
    }
    
    protected boolean isImported() {
        if (getWSDLComponent() != null) {
            return getModel() != getWSDLComponent().getModel();
        }
        return false;
    }
    
    @Override
    protected Node getNodeFilter(Node original) {
        if (isImported()) return new ReadOnlyWidgetFilterNode(original);
        
        return super.getNodeFilter(original);
    }
    
    private Vector<DisplayObject> getAllMessages(WSDLModel model) {
        Vector<DisplayObject> list = new Vector<DisplayObject>();
        
        list.addAll(getAllAvailableMessages(model, model));
        
        for (WSDLModel imported : Utility.getImportedDocuments(model)) {
            list.addAll(getAllAvailableMessages(model, imported));
        }
        
        return list;
        
    }
    
    private static List<DisplayObject> getAllAvailableMessages(WSDLModel source,  WSDLModel document) {
        ArrayList<DisplayObject> portTypesList = new ArrayList<DisplayObject>();

        Definitions definition =  document.getDefinitions();
        
        for (Message message : definition.getMessages()) {
            String name = message.getName();
            String targetNamespace = document.getDefinitions().getTargetNamespace();
            String prefix = Utility.getNamespacePrefix(targetNamespace, source);
            if(name != null) {
                if(prefix != null) {
                    String messageQNameStr = prefix + ":" + name;
                    portTypesList.add(new DisplayObject(messageQNameStr, message));
                } else {
                    portTypesList.add(new DisplayObject(name, message));
                }
            }
        }
        
        return  portTypesList;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    	if (evt.getSource() != getWSDLComponent()) return;
    	if (evt.getPropertyName().equals(OperationParameter.MESSAGE_PROPERTY)) {
    		setText(getMessageName());
    		ActionHelper.selectNode(getWSDLComponent());
            getScene().revalidate();
    	} else if (evt.getPropertyName().equals(OperationParameter.NAME_PROPERTY)) {
            if (mNameLabel != null && mParameter.getName() != null) {
                mNameLabel.setLabel(mParameter.getName());
                getScene().revalidate();
            }
    	}
    	getScene().validate();
    }

    public void dragExit() {
        setBorder(BorderFactory.createEmptyBorder());
    }

    public boolean dragOver(Point scenePoint, WidgetDropTargetDragEvent event) {
        if (isImported()) return false;
        Transferable t = event.getTransferable();
        Node node = Utility.getPaletteNode(t);
        if (node != null) {
            if (!node.getName().startsWith("Message")) {
                node = null;
            }
        } else {
            Node[] nodes = Utility.getNodes(t);
            if (nodes.length == 1) {
                node = nodes[0];
            }
        }
        if (node != null && (node instanceof MessageNode || node.getName().startsWith("Message"))) {
            setBorder(BorderFactory.createLineBorder(WidgetConstants.HIT_POINT_BORDER, 2));
            event.acceptDrag(event.getDropAction());
            return true;
        }

        return false;
    }

    public boolean drop(Point scenePoint, WidgetDropTargetDropEvent event) {
        if (isImported()) return false;
        Transferable t = event.getTransferable();
        Node node = Utility.getPaletteNode(t);
        if (node != null) {
            if (!node.getName().startsWith("Message")) {
                node = null;
            }
        } else {
            Node[] nodes = Utility.getNodes(t);
            if (nodes.length == 1) {
                node = nodes[0];
            }
        }
        if (node != null) {
            setBorder(BorderFactory.createEmptyBorder());
            if (node instanceof MessageNode) { 
                setMessage((MessageNode)node);
            } else {
                WSDLModel model = getWSDLComponent().getModel();
                Message msg = null;
                try {
                    model.startTransaction();
                    msg = model.getFactory().createMessage();
                    msg.setName(generateMessageName());
                    model.getDefinitions().addMessage(msg);

                    Part newPart = model.getFactory().createPart();
                    newPart.setName(NameGenerator.getInstance().generateUniqueMessagePartName(msg));
                    msg.addPart(newPart);
                    
                    getWSDLComponent().setMessage(getWSDLComponent().createReferenceTo(msg, Message.class));
                } finally {
                    if (model.isIntransaction()) {
                        model.endTransaction();
                    }
                }
                ActionHelper.selectNode(msg);
                WidgetEditCookie ec = WidgetHelper.getWidgetLookup(msg, getScene()).lookup(WidgetEditCookie.class);
                if (ec != null) ec.edit();
            }
            return true;
        }
        return false;
    }

    private void setMessage(MessageNode node) {
        Message message = node.getWSDLComponent();
        if (getModel().startTransaction()) {
            try {
            getWSDLComponent().setMessage(getWSDLComponent().createReferenceTo(message, Message.class));
            } finally {
                getModel().endTransaction();
            }
        }
        ActionHelper.selectNode(getWSDLComponent());
        
    }

    public void expandForDragAndDrop() {}

    public boolean isCollapsed() {
        return false;
    }

    @Override
    protected void updateActions(List<Action> actions) {
        super.updateActions(actions);
        ListIterator<Action> liter = actions.listIterator();
        while (liter.hasNext()) {
            Action action = liter.next();
            if (action instanceof NewAction) {
                liter.remove();
            }
        }
    }
    
    private String generateMessageName() {
        String name = ((Operation) getWSDLComponent().getParent()).getName();
        if (getWSDLComponent() instanceof Input) {
            name = NameGenerator.getInstance().generateUniqueInputMessageName(name, getWSDLComponent().getModel());
        } else if (getWSDLComponent() instanceof Output) {
            name = NameGenerator.getInstance().generateUniqueOutputMessageName(name, getWSDLComponent().getModel());
        } else {
            name = NameGenerator.getInstance().generateUniqueFaultMessageName(name, getWSDLComponent().getModel());
            
        }
        
        return name;
    }
}
