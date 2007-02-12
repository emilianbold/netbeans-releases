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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.common.method.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
    
    public ValidatingPropertyChangeListener(MethodCustomizerPanel panel, NotifyDescriptor notifyDescriptor) {
        this.panel = panel;
        this.notifyDescriptor = notifyDescriptor;
        this.checkInterfaces = panel.supportsInterfacesChecking();
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        validate();
    }
    
    // protected for testing
    protected boolean validate() {
        // method name
        String name = panel.getMethodName();
        if (!Utilities.isJavaIdentifier(name)) {
            setError(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "ERROR_nameNonJavaIdentifier"));
            return false;
        }
        // return type
        String returnType = panel.getReturnType();
        if ("".equals(returnType)) {
            setError(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "ERROR_returnTypeInvalid"));
            return false;
        }
        // interfaces
        if (checkInterfaces) {
            boolean local = panel.hasLocal();
            boolean remote = panel.hasRemote();
            if (!local && !remote) {
                setError(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "ERROR_selectSomeInterface"));
                return false;
            }
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
