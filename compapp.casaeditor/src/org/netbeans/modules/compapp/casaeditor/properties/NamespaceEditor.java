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

package org.netbeans.modules.compapp.casaeditor.properties;

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
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 * Modified from XML Schema UI.
 * @author Ajit Bhate
 */
public class NamespaceEditor extends PropertyEditorSupport {
    
    private final static String EMPTY = Constants.EMPTY_STRING;
    
    private PropertyQName mPropertySupport;
    private String mPropertyName;
    private QName mInitialURI;
    private Collection<PrefixNamespacePair> mURIs;
    private Collection<Option> mOptions;
    
    public enum Option { None, Declared, Other };
    
    
    public NamespaceEditor(
            PropertyQName propertySupport, 
            CasaWrapperModel model, 
            QName initialQName, 
            String propertyName) {
        mPropertySupport = propertySupport;
        mPropertyName = propertyName;
        mOptions = new ArrayList<Option>();
        mInitialURI = initialQName;
        
        mURIs = new ArrayList<PrefixNamespacePair>();
        Map<String, String> prefixToNamespaceMap = model.getNamespaces();
        for (String prefix : prefixToNamespaceMap.keySet()) {
            mURIs.add(new PrefixNamespacePair(prefix, prefixToNamespaceMap.get(prefix)));
        }
        
        mOptions.add(Option.None);
        mOptions.add(Option.Declared);
        mOptions.add(Option.Other);
        
        if (mInitialURI != null) {
            mURIs.remove(mInitialURI);
        }
    }
    
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public String getAsText() {
        Object value = super.getValue();
        return value == null ? EMPTY : super.getAsText();
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
        final NamespaceEditorPanel panel = new NamespaceEditorPanel(
                mInitialURI, 
                mURIs, 
                mOptions);
        panel.setEditable(mPropertySupport.canWrite());
        final DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(NamespaceEditor.class, "LBL_QNAME_Editor"), // NOI18N
                true,
                new ActionListener() {
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
        }
        );
        
        if (mPropertySupport.canWrite()) {
            // enable/disable the dlg ok button depending selection
            panel.addPropertyChangeListener( new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(NamespaceEditorPanel.PROP_VALID_SELECTION)) {
                        descriptor.setValid(((Boolean)evt.getNewValue()).booleanValue());
                    }
                }
            });
            
            panel.checkValidity();
            
        } else {
            descriptor.setValid(false);
        }
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        //dlg.setPreferredSize(new Dimension(500, 400));
        return dlg;
    }
    
    
    public static class PrefixNamespacePair {
        private String mPrefix;
        private String mNamespace;
        public PrefixNamespacePair(String prefix, String namespace) {
            mPrefix = prefix;
            mNamespace = namespace;
        }
        public String getPrefix() {
            return mPrefix;
        }
        public String getNamespace() {
            return mNamespace;
        }
        public String toString() {
            return mPrefix + Constants.COLON_STRING + mNamespace;
        }
    }
}
