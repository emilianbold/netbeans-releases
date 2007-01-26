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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.JPanel;
import org.netbeans.modules.j2ee.common.method.impl.MethodCustomizerPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Martin Adamek
 */
public final class MethodCustomizer {
    
    private final MethodCustomizerPanel panel;
    private final String prefix;
    private final Collection<MethodModel> existingMethods;

    // factory should be used to create instances
    protected MethodCustomizer(MethodModel methodModel, boolean hasLocal, boolean hasRemote, boolean selectLocal,
            boolean hasReturnType, String  ejbql, boolean hasFinderCardinality, boolean hasExceptions, boolean hasInterfaces,
            String prefix, Collection<MethodModel> existingMethods) {
            panel = new MethodCustomizerPanel(
                    methodModel,
                    hasLocal,
                    hasRemote,
                    selectLocal,
                    hasReturnType,
                    ejbql,
                    hasFinderCardinality,
                    hasExceptions,
                    hasInterfaces
                    );
            this.prefix = prefix;
            this.existingMethods = existingMethods;
    }
    
    public boolean customizeMethod(String title) {
        final NotifyDescriptor notifyDescriptor = new NotifyDescriptor(panel, title,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                null, null
                );
        new JPanel().addPropertyChangeListener(null);
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(MethodCustomizerPanel.OK_ENABLED)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        notifyDescriptor.setValid(((Boolean)newvalue).booleanValue());
                    }
                }
            }
        });
        Object resultValue = DialogDisplayer.getDefault().notify(notifyDescriptor);
        panel.isOK(); // apply possible changes in dialog fields
        return resultValue == NotifyDescriptor.OK_OPTION;
    }
    
    public boolean finderReturnIsSingle() {
        return false;
    }

    public boolean publishToLocal() {
        return true;
    }

    public boolean publishToRemote() {
        return false;
    }
    
    public String getEjbQL() {
        return null;

    }

}
