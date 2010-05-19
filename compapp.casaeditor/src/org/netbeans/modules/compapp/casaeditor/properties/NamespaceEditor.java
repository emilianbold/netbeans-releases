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

package org.netbeans.modules.compapp.casaeditor.properties;

import java.awt.Component;
import java.awt.Dialog;
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
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;


/**
 * Modified from XML Schema UI.
 * @author Ajit Bhate
 */
public class NamespaceEditor extends PropertyEditorSupport 
        implements ExPropertyEditor {
    
    private final static String EMPTY = Constants.EMPTY_STRING;
    
    private boolean mLocalNameWritable;
    private boolean mNamespaceWritable;
    private String mPropertyName;
    private QName mInitialURI;
    private Collection<PrefixNamespacePair> mURIs;
    private Collection<Option> mOptions;
    
    public enum Option { None, Declared, Other };
    
    
    public NamespaceEditor(
            CasaWrapperModel model, 
            QName initialQName, 
            String propertyName,
            boolean localNameWritable,
            boolean namespaceWritable) {

        mLocalNameWritable = localNameWritable;
        mNamespaceWritable = namespaceWritable;
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
    
    
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }
    
    @Override
    public String getAsText() {
        Object value = super.getValue();
        return value == null ? EMPTY : super.getAsText();
    }

    @Override
    public void setAsText(String s) {
        if (EMPTY.equals(s) && getValue() == null) // NOI18N
            return;
    }

    @Override
    public boolean isPaintable() {
        return false;
    }
    
    protected String getPaintableString() {
        Object value = getValue();
        return value == null ? 
            NbBundle.getMessage(StringEditor.class,"LBL_Null") :        // NOI18N
            getAsText();
    }

    @Override
    public Component getCustomEditor() {
        final NamespaceEditorPanel panel = new NamespaceEditorPanel(
                mInitialURI, 
                mURIs, 
                mOptions);
        panel.setEditable(mNamespaceWritable, mLocalNameWritable);
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
        
        if (mLocalNameWritable || mNamespaceWritable) {
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
    
    public void attachEnv(PropertyEnv env) {
        // Disable direct inline text editing.
        env.getFeatureDescriptor().setValue("canEditAsText", false); // NOI18N
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
        @Override
        public String toString() {
            return mPrefix + Constants.COLON_STRING + mNamespace;
        }
        @Override
        public boolean equals(Object another) {
            if (this == another) {
                return true;
            }
            if (another != null && another instanceof PrefixNamespacePair) {
                PrefixNamespacePair anotherPair = (PrefixNamespacePair) another;
                if (//getPrefix().equals(anotherPair.getPrefix()) &&
                        getNamespace().equals(anotherPair.getNamespace())) {
                    return true;
                }
            }
            return false;
        }
        @Override
        public int hashCode() {
            int hash = 1;
            hash = hash * 31 + mPrefix.hashCode();
            hash = hash * 31 + mNamespace.hashCode();
            return hash;
        }
    }
}
