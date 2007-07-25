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

package org.netbeans.modules.uihandler;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.uihandler.api.Controller;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author Jaroslav Tulach
 */
public class UIHandler extends Handler 
implements ActionListener, Runnable, Callable<JButton> {
    private final boolean exceptionOnly;
    public static final PropertyChangeSupport SUPPORT = new PropertyChangeSupport(Controller.getDefault());
    static final int MAX_LOGS = 1000;
    private static Task lastRecord = Task.EMPTY;
    private static RequestProcessor FLUSH = new RequestProcessor("Flush UI Logs"); // NOI18N
    
    private static boolean exceptionHandler;
    public static void registerExceptionHandler(boolean enable) {
        exceptionHandler = enable;
    }
    
    public UIHandler(boolean exceptionOnly) {
        setLevel(Level.FINEST);
        this.exceptionOnly = exceptionOnly;
    }

    public void publish(LogRecord record) {
        if (exceptionOnly) {
            if (record.getThrown() == null) {
                return;
            }
            if (!exceptionHandler) {
                return;
            }
        }
        
        class WriteOut implements Runnable {
            public LogRecord r;
            public void run() {
                Installer.writeOut(r);
                SUPPORT.firePropertyChange(null, null, null);
                r = null;
            }
        }
        WriteOut wo = new WriteOut();
        wo.r = record;
        lastRecord = FLUSH.post(wo);
    }

    public void flush() {
        waitFlushed();
    }
    
    static final void waitFlushed() {
        try {
            lastRecord.waitFinished(1000);
        } catch (InterruptedException ex) {
            Installer.LOG.log(Level.FINE, null, ex);
        }
    }

    public void close() throws SecurityException {
    }
    
    public void run() {
        Installer.displaySummary("ERROR_URL", true, false,true); // NOI18N
    }

    private JButton button;
    public JButton call() throws Exception {
        if (button == null) {
            button = new JButton();
            Mnemonics.setLocalizedText(button, NbBundle.getMessage(UIHandler.class, "MSG_SubmitButton")); // NOI18N
            button.addActionListener(this);
        }
        return button;
    }

    public void actionPerformed(ActionEvent ev) {
        JComponent c = (JComponent)ev.getSource();
        Window w = SwingUtilities.windowForComponent(c);
        if (w != null) {
            w.dispose();
        } 
        Installer.RP.post(this);
    }
}
