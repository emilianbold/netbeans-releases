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
 * MemberTypesEditor.java
 *
 * Created on December 22, 2005, 12:58 PM
 */

package org.netbeans.modules.xml.schema.abe.nodes.properties;

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
import org.netbeans.modules.xml.axi.AXIComponent;
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
    public NamespaceEditor(AXIComponent component, String typeDisplayName, String property) {
        this.typeDisplayName = typeDisplayName;
        this.property = property;
        this.options = new ArrayList<Option>();
        if(component.getPeer() instanceof Schema) {
            Schema schema = (Schema)component.getPeer();
            this.initialUri = schema.getTargetNamespace();
            this.uris = new ArrayList<String>();
            for(String uri: schema.getPrefixes().values()) {
                if(!uris.contains(uri))
                    uris.add(uri);
            }            
            options.add(Option.None);
            options.add(Option.Declared);
            options.add(Option.Other);
        }/* else if(component instanceof Notation) {
            Notation notation = (Notation)component;
            this.initialUri = notation.getSystemIdentifier();
            this.uris = notation.getModel().getSchema().getPrefixes().values();
            options.add(Option.Declared);
            options.add(Option.Other);
        }*/
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
        return dlg;
    }
    
    
    public boolean supportsCustomEditor() {
        return true;
    }
}
