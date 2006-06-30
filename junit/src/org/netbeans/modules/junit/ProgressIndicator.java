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

package org.netbeans.modules.junit;

import java.awt.EventQueue;
import java.lang.reflect.Method;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/** Thread-safe wrapper around JUnitProgress - panel showing progress info
 * and allowing the user to cancel running task. Used in actions creating
 * or executing tests.
 *
 * @author  Tomas Pavek
 * @author  Ondrej Rypacek
 * @author  Marian Petras
 */
class ProgressIndicator {

    /**
     * initial message to be used when GUI is created.
     * It is only used if setMessage(...) is called sooner than show().
     */
    private String initialMessage;
    private JUnitProgress progressPanel;
    /** <code>true</code> if GUI (dialog) creation has passed or is scheduled */
    private boolean guiCreationScheduled;

    synchronized boolean isCanceled() {
        return progressPanel != null ? progressPanel.isCanceled() : false;
    }

    void displayStatusText(String statusText) {
        StatusDisplayer.getDefault().setStatusText(statusText);
    }

    /**
     * Sets a message to be displayed in the progress GUI.
     * If the GUI already exists (or is scheduled to be created), this method
     * will cause the message in the GUI to be changed to the given text.
     * If the GUI neither exists nor is scheduled, this method just remembers
     * the message so that it will used when the GUI is created.
     */
    synchronized void setMessage(final String msg, final boolean displayStatus) {
        if (guiCreationScheduled) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    progressPanel.setMessage(msg, displayStatus);
                }
            });
        } else {
            /*
             * Set an initial message to be used when GUI is about to be
             * created:
             */
            initialMessage = msg;
        }
    }

    synchronized void show() {
        if (!guiCreationScheduled) {
            sendToAwtQueue("createAndShowDialog");                      //NOI18N
            guiCreationScheduled = true;
        } else {
            sendToAwtQueue("showDialog");                               //NOI18N
        }
    }

    synchronized void hide() {
        if (guiCreationScheduled) {
            sendToAwtQueue("hideDialog");                               //NOI18N
        }
        StatusDisplayer.getDefault().setStatusText("");                 //NOI18N
    }

    /**
     */
    synchronized void createAndShowDialog() {
        String msg = NbBundle.getMessage(ProgressIndicator.class,
                               "LBL_generator_progress_title"); //NOI18N
        progressPanel = new JUnitProgress(msg);
        
        if (initialMessage != null) {
            progressPanel.setMessage(initialMessage);
            initialMessage = null;
        }
        
        showDialog();
    }

    /**
     */
    synchronized void showDialog() {
        progressPanel.showMe(true);
    }

    /**
     */
    synchronized void hideDialog() {
        progressPanel.hideMe();
    }

    /**
     */
    private void sendToAwtQueue(String methodName) {
        final Method method;
        try {
            method = getClass().getDeclaredMethod(methodName, new Class[0]);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            return;
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    method.invoke(ProgressIndicator.this, (Object[]) null);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
                }
            }
        });
    }

}
