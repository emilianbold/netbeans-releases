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

/*
 * MemberTypesEditor.java
 *
 * Created on December 22, 2005, 12:58 PM
 */

package org.netbeans.modules.xml.schema.ui.basic.editors;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Notation;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class NamespaceEditor  extends StringEditor {
    
    private String typeDisplayName;
    private String property;
    private String initialUri;
    private Collection<String> uris;
    private Collection<Option> options;
    
    public enum Option { None, Declared, Other, TargetNamespace, Local};
    
    /**
     * Creates a new instance of MemberTypesEditor
     */
    public NamespaceEditor(SchemaComponent component, String typeDisplayName, String property) {
        this.typeDisplayName = typeDisplayName;
        this.property = property;
        this.options = new ArrayList<Option>();
        if(component instanceof Schema) {
            Schema schema = (Schema)component;
            this.initialUri = schema.getTargetNamespace();
            this.uris = schema.getPrefixes().values();
            options.add(Option.None);
            options.add(Option.Declared);
            options.add(Option.Other);
        } else if(component instanceof Notation) {
            Notation notation = (Notation)component;
            this.initialUri = notation.getSystemIdentifier();
            this.uris = notation.getModel().getSchema().getPrefixes().values();
            options.add(Option.Declared);
            options.add(Option.Other);
        }
        uris.remove(SchemaModelFactory.getDefault().getPrimitiveTypesModel().
                getSchema().getTargetNamespace());
        if(initialUri!=null) uris.remove(initialUri);
    }
    
    public Component getCustomEditor() {
        final NamespacePanel panel = new NamespacePanel(initialUri,
                uris, options);
        final DialogDescriptor descriptor = new DialogDescriptor(panel,
                NbBundle.getMessage(
                NamespaceEditor.class,"LBL_Custom_Property_Editor_Title",
                typeDisplayName,property),
                true,
                new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    try {
                        setValue(panel.getCurrentSelection());
                    } catch (IllegalArgumentException iae) {
                        ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                                iae.getMessage(), iae.getLocalizedMessage(),
                                null, new java.util.Date());
                        throw iae;
                    }
                }
            }
        }
        );
        // enable/disable the dlg ok button depending selection
        panel.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(
                        NamespacePanel.PROP_VALID_SELECTION)) {
                    descriptor.setValid(((Boolean)evt.getNewValue()).booleanValue());
                }
            }
        });
        panel.checkValidity();
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setPreferredSize(new Dimension(400,300));
        return dlg;
    }
    
    
    public boolean supportsCustomEditor() {
        return true;
    }
}
