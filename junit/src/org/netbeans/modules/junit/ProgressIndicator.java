/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import org.openide.util.NbBundle;

/** Thread-safe wrapper around JUnitProgress - panel showing progress info
 * and allowing the user to cancel running task. Used in actions creating
 * or executing tests.
 */

class ProgressIndicator implements Runnable {

    private String message;
    private JUnitProgress progressPanel;
    private boolean creatingGUI;

    synchronized boolean isCanceled() {
        return progressPanel != null ? progressPanel.isCanceled() : false;
    }

    void displayStatusText(String statusText) {
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(statusText);
    }

    synchronized void setMessage(final String msg, final boolean displayStatus) {
        if (progressPanel != null)
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    progressPanel.setMessage(msg, displayStatus);
                }
            });
        else
            message = msg;
    }

    synchronized void show() {
        if (progressPanel == null && !creatingGUI) {
            creatingGUI = true;
            java.awt.EventQueue.invokeLater(this);
        }
        else if (progressPanel != null)
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    progressPanel.showMe(true);
                }
            });
    }

    synchronized void hide() {
        if (progressPanel != null)
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    progressPanel.hideMe();
                }
            });
    }

    synchronized public void run() {
        String msg = NbBundle.getMessage(JUnitProgress.class,
                               "LBL_generator_progress_title"); //NOI18N
        progressPanel = new JUnitProgress(msg);
        if (message != null) {
            progressPanel.setMessage(message);
            message = null;
        }
        progressPanel.showMe(true);
    }
}
