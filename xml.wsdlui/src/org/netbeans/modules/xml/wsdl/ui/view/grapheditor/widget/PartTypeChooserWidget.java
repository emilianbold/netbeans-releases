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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.ElementOrTypeChooserEditorPanel;
import org.netbeans.modules.xml.wsdl.ui.view.ElementOrTypeChooserPanel;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border.FilledBorder;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.LeftRightLayout;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class PartTypeChooserWidget extends Widget implements ActionListener {
    
    private LabelWidget partTypeLabel;
    private ButtonWidget showPartTypeChooserButton;
    private Part part;
    
    public PartTypeChooserWidget(Scene scene, Part part) {
        super(scene);
        
        this.part = part;
        
        partTypeLabel = new LabelWidget(scene, 
                MessagesUtils.getPartTypeOrElementString(part));
        partTypeLabel.setFont(scene.getDefaultFont());
        
        showPartTypeChooserButton = new ButtonWidget(scene, "...");
        showPartTypeChooserButton.setMargin(new Insets(1, 2, 1, 2));
        showPartTypeChooserButton.setActionListener(this);
        
        addChild(partTypeLabel);
        addChild(showPartTypeChooserButton);
        
        setLayout(new LeftRightLayout(8));
        setBorder(BORDER);
        
        PartnerScene pScene = (PartnerScene) getScene();
        String weight = pScene.getWeight(part) + "showPartTypeChooserButton";
        if (!pScene.getObjects().contains(weight)) {
        	pScene.addObject(pScene.getWeight(part) + "showPartTypeChooserButton", showPartTypeChooserButton);
        }
    }
    
    
    ButtonWidget getPartTypeChooserButton() {
        return showPartTypeChooserButton;
    }
    
    public void actionPerformed(ActionEvent event) {
        WSDLModel wsdlModel = part.getModel();
        ModelSource modelSource = wsdlModel.getModelSource();
        FileObject wsdlFile = modelSource.getLookup().lookup(FileObject.class);
        if(wsdlFile != null) {
            Project project = FileOwnerQuery.getOwner(wsdlFile);
            if(project != null) {
                Map<String, String> namespaceToPrefixMap = new HashMap<String, String>();
                Map<String, String> map = ((AbstractDocumentComponent)wsdlModel.getDefinitions()).getPrefixes();
                for (String prefix : map.keySet()) {
                    namespaceToPrefixMap.put(map.get(prefix), prefix);
                }
                
                SchemaComponent comp = part.getElement() == null ? null : part.getElement().get();
                if (comp == null) {
                    comp = part.getType() == null ? null : part.getType().get();
                }
                
                final ElementOrTypeChooserPanel panel = new ElementOrTypeChooserPanel(project, namespaceToPrefixMap, wsdlModel, comp);
                final DialogDescriptor descriptor = new DialogDescriptor(panel , NbBundle.getMessage(ElementOrTypeChooserEditorPanel.class, "ElementOrTypeChooserEditorPanel.Dialog.title"), true, null);
                descriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypePropertyEditor"));
                
                final PropertyChangeListener pcl = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if(evt.getSource()== panel && evt.getPropertyName().
                                equals(ElementOrTypeChooserPanel.PROP_ACTION_APPLY)) {
                            descriptor.setValid(((Boolean) evt.getNewValue()).booleanValue());
                        }
                    }
                };
                panel.addPropertyChangeListener(pcl);
                // dialog's action listener
                ActionListener al = new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if (evt.getSource().equals(DialogDescriptor.OK_OPTION) ||
                                evt.getSource().equals(DialogDescriptor.CANCEL_OPTION) ||
                                evt.getSource().equals(DialogDescriptor.CLOSED_OPTION)) {
                            panel.removePropertyChangeListener(pcl);
                        }
                        if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                            panel.apply();
                            SchemaComponent comp1 = panel.getSelectedSchemaComponent();
                            if (comp1 == null) return;
                            WSDLModel model = part.getModel();
                            if (comp1 instanceof GlobalType) {
                                try {
                                    if (model.startTransaction()) {
                                        Utility.addSchemaImport(comp1, model);
                                        Utility.addNamespacePrefix(comp1.getModel().getSchema(), model, null);
                                        part.setType(model.getDefinitions().createSchemaReference(
                                                (GlobalType) comp1, GlobalType.class));
                                        part.setElement(null);
                                    }
                                } finally {
                                    model.endTransaction();
                                }
                            } else if (comp1 instanceof GlobalElement) {
                                try {
                                    if (model.startTransaction()) {
                                        Utility.addSchemaImport(comp1, model);
                                        Utility.addNamespacePrefix(comp1.getModel().getSchema(), model, null);
                                        part.setElement(model.getDefinitions().createSchemaReference(
                                                (GlobalElement) comp1, GlobalElement.class));
                                        part.setType(null);
                                    }
                                } finally {
                                    model.endTransaction();
                                }
                            }
                            ActionHelper.selectNode(part);

                        }
                    }
                };
                descriptor.setButtonListener(al);
                descriptor.setValid(false);
                Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                dialog.setVisible(true);
                dialog.toFront();
            }
        }
    }
    
    
    public static final Border BORDER = new FilledBorder(
            new Insets(0, 0, 0, 0), new Insets(1, 8, 1, 1), null, Color.WHITE);
    
}
