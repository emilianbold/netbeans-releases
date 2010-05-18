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
    private final Part part;
    
    public PartTypeChooserWidget(Scene scene, Part part) {
        super(scene);
        
        this.part = part;
        
        partTypeLabel = new LabelWidget(scene, 
                MessagesUtils.getPartTypeOrElementString(part));
        partTypeLabel.setFont(scene.getDefaultFont());
        
        showPartTypeChooserButton = new ButtonWidget(scene, "...", true);
        showPartTypeChooserButton.setMargin(new Insets(1, 2, 1, 2));
        showPartTypeChooserButton.setActionListener(this);
        
        addChild(partTypeLabel);
        addChild(showPartTypeChooserButton);
        
        setLayout(new LeftRightLayout(8));
        setBorder(BORDER);
        
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
                dialog.getAccessibleContext().setAccessibleDescription(descriptor.getTitle());
                dialog.setVisible(true);
                dialog.toFront();
            }
        }
    }
    
    
    public static final Border BORDER = new FilledBorder(
            new Insets(0, 0, 0, 0), new Insets(1, 8, 1, 1), null, Color.WHITE);

    void typeOrElementChanged() {
        partTypeLabel.setLabel(MessagesUtils.getPartTypeOrElementString(part));
    }
    
}
