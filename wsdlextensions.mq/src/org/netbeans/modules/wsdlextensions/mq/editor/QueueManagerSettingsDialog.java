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
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.mq.editor;

import java.awt.HeadlessException;

import org.openide.util.NbBundle;


/**
 * Concrete Dialog implementation for Queue Manager advanced settings.
 *
 * @author Noel.Ang@sun.com
 */
final class QueueManagerSettingsDialog extends Dialog {

    QueueManagerSettingsDialog() throws HeadlessException {
        super(NbBundle.getMessage(MqBindingsConfigurationEditorForm.class,
                "QueueManagerSettingsDialog.DialogTitle"
        ),
                new QueueManagerSettingsForm(null,
                        new MqBindingsConfigurationEditorModel()
                )
        ); // NOI18N
    }

    /**
     * This method is called when the "OK" action is performed, signaling to the
     * implementation to perform whatever commit semantic is applicable.
     */
    protected void commit() {
        Form form = getForm();
        form.commit();
    }

    /**
     * This method is called when the "Cancel" action is performed, signaling to
     * the implementation to perform whatever rollback semantic is applicable.
     */
    protected void cancel() {
        Form form = getForm();
        form.revert();
    }

    @Override
    public void setVisible(boolean visible) {
        ((QueueManagerSettingsForm) getForm()).getDefaultFocusComponent().requestFocusInWindow();
        super.setVisible(visible);
    }
}
