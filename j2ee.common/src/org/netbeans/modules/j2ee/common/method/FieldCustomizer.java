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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.common.method;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.j2ee.common.method.impl.FieldCustomizerPanel;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Adamek
 */
public class FieldCustomizer {
    
    private final Entity entity;
    private final FieldCustomizerPanel panel;
    
    public FieldCustomizer(Entity entity, MethodModel.Variable element, String description, boolean localEnabled, boolean remoteEnabled,
            boolean localGetter, boolean localSetter, boolean remoteGetter, boolean remoteSetter) {
        this.entity = entity;
        this.panel = new FieldCustomizerPanel(element, description, localEnabled, remoteEnabled, localGetter, localSetter, remoteGetter, remoteSetter);
    }
    
    public boolean customizeField() {
        DialogDescriptor notifyDescriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(FieldCustomizer.class, "LBL_AddCmpField"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.PLAIN_MESSAGE,
                null
                );
        panel.addPropertyChangeListener(new Validator(panel, notifyDescriptor, entity));
        return DialogDisplayer.getDefault().notify(notifyDescriptor) == NotifyDescriptor.OK_OPTION;
    }
    
    public MethodModel.Variable getField() {
        return MethodModel.Variable.create(panel.getReturnType(), panel.getMethodName());
    }
    
    public String getDescription() {
        return panel.getDescription();
    }

    public boolean isLocalGetter() {
        return panel.isLocalGetter();
    }

    public boolean isLocalSetter() {
        return panel.isLocalSetter();
    }

    public boolean isRemoteGetter() {
        return panel.isRemoteGetter();
    }

    public boolean isRemoteSetter() {
        return panel.isRemoteSetter();
    }

    private static class Validator implements PropertyChangeListener {
        
        private final FieldCustomizerPanel panel;
        private final NotifyDescriptor notifyDescriptor;
        private final Entity entity;
        
        public Validator(FieldCustomizerPanel panel, NotifyDescriptor notifyDescriptor, Entity entity) {
            this.panel = panel;
            this.notifyDescriptor = notifyDescriptor;
            this.entity = entity;
        }
        
        public void propertyChange(PropertyChangeEvent arg0) {
            validate();
        }
        
        private boolean validate() {
            // method name
            String name = panel.getMethodName();
            if (!Utilities.isJavaIdentifier(name)) {
                setError(NbBundle.getMessage(FieldCustomizer.class, "ERROR_nameNonJavaIdentifier"));
                return false;
            }
            CmpField[] cmpField = entity.getCmpField();
            for (int i = 0; i < cmpField.length; i++) {
                CmpField field = cmpField[i];
                if (name.equals(field.getFieldName())) {
                    setError(NbBundle.getMessage(FieldCustomizer.class, "MSG_Duplicate_Field_Name", name));
                    return false;
                }
            }
            // return type
            String returnType = panel.getReturnType();
            if ("".equals(returnType)) {
                setError(NbBundle.getMessage(FieldCustomizer.class, "ERROR_returnTypeInvalid"));
                return false;
            }
            // interfaces
            if (!panel.isLocalGetter() && !panel.isLocalSetter() && !panel.isRemoteGetter() && !panel.isRemoteSetter()) {
                setError(NbBundle.getMessage(FieldCustomizer.class, "ERROR_selectSomeInterface"));
                return false;
            }
            unsetError();
            return true;
        }
        
        private void setError(String message) {
            notifyDescriptor.setValid(false);
            panel.setError(message);
        }
        
        private void unsetError() {
            notifyDescriptor.setValid(true);
            panel.setError("");
        }

    }
    
}
