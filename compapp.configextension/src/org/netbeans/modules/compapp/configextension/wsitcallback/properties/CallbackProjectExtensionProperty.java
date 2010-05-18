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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.configextension.wsitcallback.properties;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.io.File;
import javax.xml.namespace.QName;
import javax.swing.*;

import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.properties.spi.ExtensionProperty;
import org.netbeans.modules.compapp.configextension.util.StackTraceUtil;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Exceptions;

/**
 * Extension property of Java Callback Project location.
 *
 * @author tli
 * @author jqian
 */
public class CallbackProjectExtensionProperty
        extends ExtensionProperty<String> {

    private static final String PROJECT_DIR = "CallbackProject";
    private static final QName PROJECT_DIR_QNAME = new QName(PROJECT_DIR);
    private CasaNode mNode;

    public CallbackProjectExtensionProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String propertyName,
            String displayName,
            String description) {
        super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                String.class,
                propertyName, displayName, description);
        mNode = node;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (canWrite()) {
            PropertyEditor pChooserEditor = new ProjectChooserEditor();
            try {
                String value = getValue();
                pChooserEditor.setValue(value);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return pChooserEditor;
        } else {
            return super.getPropertyEditor();
        }
    }

    @Override
    public String getValue()
            throws IllegalAccessException, InvocationTargetException {

        CasaComponent component = getComponent();

        String projectDir = component.getAnyAttribute(PROJECT_DIR_QNAME);

        return projectDir == null ? "" : projectDir; // NOI18N
    }

    @Override
    public void setValue(String value)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {

        if (!canWrite() && StackTraceUtil.isCalledBy(
                "org.openide.explorer.propertysheet.PropertyDialogManager", // NOI18N
                "cancelValue")) { // NOI18N
            return;
        }

        CasaComponent component = getComponent();
        CasaWrapperModel model = getModel();
        model.startTransaction();
        try {
            component.setAnyAttribute(PROJECT_DIR_QNAME, value);
        } finally {
            if (model.isIntransaction()) {
                model.endTransaction();
            }
        }
        if (component.getParent() == null) {
            model.addExtensibilityElement(extensionPointComponent,
                    (CasaExtensibilityElement) component);
        }
    }

    class ProjectChooserEditor extends PropertyEditorSupport
            implements ExPropertyEditor {

        private JFileChooser chooser;

        @Override
        public String getAsText() {
            return (String) getValue();
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public java.awt.Component getCustomEditor() {
            chooser = ProjectChooser.projectChooser();

            String value = (String) getValue();
            if (value != null) {
                File file = new File(value);
                if (file.exists()) {
                    chooser.setSelectedFile(file);
                }
            }
            return chooser;
        }

        public void attachEnv(PropertyEnv env) {
            // Disable direct inline text editing.
            env.getFeatureDescriptor().setValue("canEditAsText", false); // NOI18N

            // Add validation.
            env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            env.addVetoableChangeListener(new VetoableChangeListener() {

                public void vetoableChange(PropertyChangeEvent ev)
                        throws PropertyVetoException {
                    if (PropertyEnv.PROP_STATE.equals(ev.getPropertyName())) {
                        // customEditor.validateValue();
                    }
                }
            });

            env.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                    if (PropertyEnv.PROP_STATE.equals(ev.getPropertyName())) {
                        try {
                            setValue(chooser.getSelectedFile().getCanonicalPath());
                        } catch (Exception ex) {
                            // set failed..
                        }
                    }
                }
            });

        }
    }
}