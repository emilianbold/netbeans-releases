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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach
 */
public class UIHandler extends Handler 
implements ActionListener, Runnable, Callable<JButton> {
    private final Queue<LogRecord> logs;
    private final boolean exceptionOnly;
    static final PropertyChangeSupport SUPPORT = new PropertyChangeSupport(UIHandler.class);
    private static int MAX_LOGS = 1000;
    
    public UIHandler(Queue<LogRecord> l, boolean exceptionOnly) {
        setLevel(Level.FINEST);
        logs = l;
        this.exceptionOnly = exceptionOnly;
    }

    static void registerCallback(PropertyChangeListener l) {
        SUPPORT.addPropertyChangeListener(l);
    }
    
    
    public void publish(LogRecord record) {
        if (exceptionOnly && record.getThrown() == null) {
            return;
        }
        
        synchronized (UIHandler.class) {
            if (logs.size() > MAX_LOGS) {
                for (int i = 0; i < 100; i++) {
                    logs.poll();
                }
            }
            if (!logs.contains(record)) {
                logs.add(record);
            }
        }
        
        SUPPORT.firePropertyChange(null, null, null);
    }

    public void flush() {
    }

    public void close() throws SecurityException {
    }
    
    public void run() {
        Installer.displaySummary("ERROR_URL", true); // NOI18N
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
