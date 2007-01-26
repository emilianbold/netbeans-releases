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
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.LabelWidget.Alignment;
import org.netbeans.api.visual.widget.LabelWidget.VerticalAlignment;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.ComboBoxInplaceEditor;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.ComboBoxInplaceEditorProvider;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions.HoverActionProvider;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.DisplayObject;
import org.netbeans.modules.xml.xam.Model;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class OperationParameterWidget extends AbstractWidget<OperationParameter>
        {

    private LabelWidget mParameterMessage;
    private OperationParameter mParameter;
    

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
        setOpaque(true);
        mParameter = parameter;
        setLayout(LayoutFactory.createVerticalLayout(SerialAlignment.JUSTIFY, 1));
        mParameterMessage = new LabelWidget (scene);
        mParameterMessage.setOpaque (true);
        mParameterMessage.setBackground (Color.WHITE);
        if (parameter.getMessage() != null &&
                parameter.getMessage().get() != null) {
            mParameterMessage.setLabel(parameter.getMessage().get().getName());
        } else {
            mParameterMessage.setLabel(NbBundle.getMessage(OperationParameterWidget.class,
                    "LBL_OperationParamterWidget_NoMessageSelected"));
        }
        mParameterMessage.setVerticalAlignment(VerticalAlignment.CENTER);
        mParameterMessage.setAlignment(Alignment.CENTER);
        
       
        mParameterMessage.getActions().addAction(ActionFactory.createInplaceEditorAction(new ComboBoxInplaceEditorProvider(new ComboBoxInplaceEditor() {
            public boolean isEnabled(Widget widget) {
                return true;
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
                try {
                    if (model.startTransaction()) {
                        Message message = null;
                        if (selectedItem instanceof DisplayObject) {
                            DisplayObject dispObj = (DisplayObject) selectedItem;
                            Object obj = dispObj.getValue();
                            if (obj instanceof Message) {
                                message = (Message) obj;
                            }
                        } else if (selectedItem instanceof String) {
                            message = model.getFactory().createMessage();
                            message.setName((String) selectedItem);
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

        }, EnumSet.<InplaceEditorProvider.ExpansionDirection>of (InplaceEditorProvider.ExpansionDirection.RIGHT))));
        
        if (parameter instanceof Fault) {
            Widget widget = new Widget(scene);
            widget.setLayout(new OperationParameterLayout(10));
            addChild(widget);
            
            mParameter = parameter;
            LabelWidget mLabel = new LabelWidget (scene);
            mLabel.setOpaque (true);
            mLabel.setBackground (Color.WHITE);
            if (mParameter != null) {
                mLabel.setLabel (mParameter.getName());
                mLabel.getActions().addAction(ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {

                    public void setText(Widget widget, String text) {
                        Model model = mParameter.getModel();
                        try {
                            if (model.startTransaction()) {
                                mParameter.setName(text);
                            }
                        } finally {
                            model.endTransaction();
                        }
                    }

                    public boolean isEnabled(Widget widget) {
                        return true;
                    }

                    public String getText(Widget widget) {
                        return mParameter.getName();
                    }

                }, 
                EnumSet.<InplaceEditorProvider.ExpansionDirection>of (InplaceEditorProvider.ExpansionDirection.LEFT, 
                        InplaceEditorProvider.ExpansionDirection.RIGHT)));
            }
            mLabel.getActions().addAction(HoverActionProvider.getDefault(getScene()).getHoverAction());
            Font font = scene.getDefaultFont ().deriveFont (Font.BOLD);
            mLabel.setFont (font);
            mLabel.setAlignment(Alignment.CENTER);
            widget.addChild (mLabel);
            widget.addChild(mParameterMessage);
        } else {
            addChild(mParameterMessage);
        }

    }
    
    public void setText(String text) {
        mParameterMessage.setLabel(text);
    }
    
    public String getText() {
        return this.mParameterMessage.getLabel();
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
    public void updateContent() {
        String msg = null;
        if (mParameter.getMessage() != null && mParameter.getMessage().get() != null) {
            msg = mParameter.getMessage().get().getName();
        }
        if (msg != null && !msg.equals(getText())) {
            setText(msg);
        }
    }

    public class OperationParameterLayout implements Layout {

        int mGap;
        
        public OperationParameterLayout(int gap) {
            mGap = gap;
        }

        public void justify(Widget widget) {
            Rectangle parentBounds = widget.getClientArea();
            
            List<Widget> children = widget.getChildren();
            
            Widget first = children.get(0);
            Widget second = children.get(1);
            
            Point fPt = first.getLocation();
            Point sPt = second.getLocation();
            
            
            
            int centerOfWidget = parentBounds.width / 2;
            fPt.x = centerOfWidget - (mGap / 2) - first.getBounds().width;
            
            sPt.x = centerOfWidget + (mGap / 2);
            
            first.resolveBounds(fPt, first.getBounds());
            second.resolveBounds(sPt, second.getBounds());
        }

        public void layout(Widget widget) {
            List<Widget> children = widget.getChildren();
            
            Widget first = children.get(0);
            Widget second = children.get(1);
            
            Point sPt = new Point(0, 0);
            sPt.x = first.getPreferredBounds().width + mGap;
            
            
            first.resolveBounds(new Point(), first.getPreferredBounds());
            second.resolveBounds(sPt, second.getPreferredBounds());
        }

        public boolean requiresJustification(Widget widget) {
            // TODO Auto-generated method stub
            return true;
        }

    }
}
