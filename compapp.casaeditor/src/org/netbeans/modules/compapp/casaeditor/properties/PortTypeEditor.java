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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.casaeditor.properties;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author rdara
 */
public class PortTypeEditor extends PropertyEditorSupport{

    private final static String EMPTY = Constants.EMPTY_STRING;
    
    private String mPropertyName;
    private PortType mPortType;
    private List<PortType> mAllPortTypes;

    
    public enum Option { None, Declared, Other };
    
    
    public PortTypeEditor(
            CasaWrapperModel model, 
            PortType initialPortType, 
            String propertyName) {
        mPropertyName = propertyName;
        mPortType = initialPortType;
        
        mAllPortTypes = model.getPortTypes();
    }
    
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public String getAsText() {
        return mPortType == null ? EMPTY : mPortType.getName();
    }

    public void setAsText(String s) {
        if (EMPTY.equals(s) && getValue() == null) // NOI18N
            return;
    }

    public boolean isPaintable() {
        return false;
    }
    
    protected String getPaintableString() {
        Object value = getValue();
        return value == null ? 
            NbBundle.getMessage(StringEditor.class,"LBL_Null") :        // NOI18N
            getAsText();
    }

    public Component getCustomEditor() {
        final PortTypeEditorPanel panel = new PortTypeEditorPanel(
                mPortType, 
                mAllPortTypes);
        final DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(PortTypeEditorPanel.class, "LBL_PORT_TYPE_Editor"), // NOI18N
                true, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    try {
                        setValue(panel.getCurrentSelection());
                    } catch (IllegalArgumentException iae) {
                        ErrorManager.getDefault().annotate(
                                iae, 
                                ErrorManager.USER,
                                iae.getMessage(), 
                                iae.getLocalizedMessage(),
                                null, 
                                new java.util.Date());
                        throw iae;
                    }
                }
            }
        });
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setPreferredSize(new Dimension(500, 350));
        return dlg;
    }
 
}
