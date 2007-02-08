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

package org.netbeans.modules.j2ee.common.method;

import java.util.Collection;
import org.netbeans.modules.j2ee.common.method.impl.MethodCustomizerPanel;
import org.netbeans.modules.j2ee.common.method.impl.ValidatingPropertyChangeListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Martin Adamek
 */
public final class MethodCustomizer {
    
    private final MethodCustomizerPanel panel;
    private final String title;
    private final String prefix;
    private final Collection<MethodModel> existingMethods;
    
    // factory should be used to create instances
    protected MethodCustomizer(String title, MethodModel methodModel, boolean hasLocal, boolean hasRemote, boolean selectLocal, boolean selectRemote,
            boolean hasReturnType, String  ejbql, boolean hasFinderCardinality, boolean hasExceptions, boolean hasInterfaces,
            String prefix, Collection<MethodModel> existingMethods) {
        this.panel = MethodCustomizerPanel.create(methodModel, hasLocal, hasRemote, selectLocal, selectRemote,
                hasReturnType, ejbql, hasFinderCardinality, hasExceptions, hasInterfaces);
        this.title = title;
        this.prefix = prefix;
        this.existingMethods = existingMethods;
    }
    
    /**
     * Opens modal window for method customization.
     * 
     * @return true if OK button on customizer was pressed and changes should be written to source files,
     * false if Cancel button on customizer was pressed or if customizer was cancelled in any other way and
     * nothing should be written to source files.
     */
    public boolean customizeMethod() {
        DialogDescriptor notifyDescriptor = new DialogDescriptor(
                panel, title, true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.PLAIN_MESSAGE,
                null
                );
        panel.addPropertyChangeListener(new ValidatingPropertyChangeListener(panel, notifyDescriptor));
        return DialogDisplayer.getDefault().notify(notifyDescriptor) == NotifyDescriptor.OK_OPTION;
    }
    
    public MethodModel getMethodModel() {
        return MethodModel.create(
                panel.getMethodName(),
                panel.getReturnType(),
                panel.getMethodBody(),
                panel.getParameters(),
                panel.getExceptions(),
                panel.getModifiers()
                );
    }
    
    public boolean finderReturnIsSingle() {
        return false;
    }
    
    public boolean publishToLocal() {
        return panel.hasLocal();
    }
    
    public boolean publishToRemote() {
        return panel.hasRemote();
    }
    
    public String getEjbQL() {
        return null;
        
    }
    
}
