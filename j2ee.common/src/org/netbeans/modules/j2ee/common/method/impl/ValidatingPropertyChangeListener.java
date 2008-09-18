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

package org.netbeans.modules.j2ee.common.method.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Adamek
 */
public final class ValidatingPropertyChangeListener implements PropertyChangeListener {
    
    private final MethodCustomizerPanel panel;
    private final NotifyDescriptor notifyDescriptor;
    private final boolean checkInterfaces;
    private final Collection<MethodModel> existingMethods;
    private final Set<String> existingMethodsNames; // just for faster validation, if such name exists, more detailed validation follows
    
    public ValidatingPropertyChangeListener(MethodCustomizerPanel panel, NotifyDescriptor notifyDescriptor, Collection<MethodModel> existingMethods) {
        this.panel = panel;
        this.notifyDescriptor = notifyDescriptor;
        this.checkInterfaces = panel.supportsInterfacesChecking();
        this.existingMethods = existingMethods;
        this.existingMethodsNames = new HashSet<String>();
        for (MethodModel methodModel : existingMethods) {
            existingMethodsNames.add(methodModel.getName());
        }
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        validate();
    }
    
    // protected for testing
    protected boolean validate() {
        // method name
        String name = panel.getMethodName();
        if (!Utilities.isJavaIdentifier(name)) {
            setError(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "ERROR_nameNonJavaIdentifier"));  // NOI18N
            return false;
        }
        // return type
        String returnType = panel.getReturnType();
        if ("".equals(returnType)) {  // NOI18N
            setError(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "ERROR_returnTypeInvalid"));  // NOI18N
            return false;
        }
        // interfaces
        if (checkInterfaces) {
            boolean local = panel.hasLocal();
            boolean remote = panel.hasRemote();
            if (!local && !remote) {
                setError(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "ERROR_selectSomeInterface"));  // NOI18N
                return false;
            }
            if (local && remote) {
                setWarning(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "LBL_commonImplForBothInterfaces"));  // NOI18N
            }
        }
        // existing methods
        if (existingMethodsNames.contains(name)) {
            List<MethodModel.Variable> proposedParams = panel.getParameters();
            for (MethodModel methodModel : existingMethods) {
                if (sameParams(proposedParams, methodModel.getParameters())) {
                    setError(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "ERROR_methodExists"));  // NOI18N
                    return false;
                }
            }
        }
        unsetError();
        return true;
    }
    
    private boolean sameParams(List<MethodModel.Variable> proposedParams, List<MethodModel.Variable> existingParams) {
        if (existingParams.size() == proposedParams.size()) {
            for (int i = 0; i < existingParams.size(); i++) {
                String existingType = existingParams.get(i).getType();
                String proposedType = proposedParams.get(i).getType();
                int existingIndex = existingType.lastIndexOf('.');
                int proposedIndex = proposedType.lastIndexOf('.');
                // try to get right result even if comparing String and java.lang.String; compare only simple names
                if (existingIndex == -1 && proposedIndex != -1) {
                    proposedType = proposedType.substring(proposedIndex + 1);
                } else if (existingIndex != -1 && proposedIndex == -1) {
                    existingType = existingType.substring(existingIndex + 1);
                }
                if (!existingType.equals(proposedType)) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
    
    private void setError(String message) {
        notifyDescriptor.setValid(false);
        panel.setError(message);
    }
    
    private void setWarning(String message) {
        notifyDescriptor.setValid(true);
        panel.setWarning(message);
    }
    
    private void unsetError() {
        notifyDescriptor.setValid(true);
        panel.setError("");  // NOI18N
    }
    
}
