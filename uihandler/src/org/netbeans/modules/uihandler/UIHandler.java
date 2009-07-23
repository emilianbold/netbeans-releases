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
    /** Maximum allowed size of log file 20MB */
    static final long MAX_LOGS_SIZE = 20L * 1024L * 1024L;
    private static Task lastRecord = Task.EMPTY;
    private static RequestProcessor FLUSH = new RequestProcessor("Flush UI Logs"); // NOI18N
    private static boolean flushOnRecord;
    private final SlownessReporter reporter;

    private static boolean exceptionHandler;
    public static void registerExceptionHandler(boolean enable) {
        exceptionHandler = enable;
    }
    
    public UIHandler(boolean exceptionOnly) {
        setLevel(Level.FINEST);
        this.exceptionOnly = exceptionOnly;
        this.reporter = new SlownessReporter();
    }

    public void publish(LogRecord record) {
        if ((record.getLevel().equals(Level.CONFIG)) &&
                (record.getMessage().startsWith("NotifyExcPanel: "))) {//NOI18N
            Installer.setSelectedExcParams(record.getParameters());
            return;
        }

        if (exceptionOnly) {
            if (record.getThrown() == null) {
                return;
            }
            if (!exceptionHandler) {
                return;
            }
        } else {
            if ((record.getLevel().equals(Level.CONFIG)) && record.getMessage().equals("Slowness detected")){
                byte[] nps = (byte[]) record.getParameters()[0];
                long time = (Long) record.getParameters()[1];
                assert nps != null: "nps param should be not null";
                assert nps.length > 0 : "nps param should not be empty";
                assert time >= 1000 : "1s is minimal reportable time";
                reporter.notifySlowness(Installer.getLogs(), nps, time);
                return;
            }
        }

        class WriteOut implements Runnable {
            public LogRecord r;
            public void run() {
                Installer.writeOut(r);
                SUPPORT.firePropertyChange(null, null, null);
                r = null;
                TimeToFailure.logAction();
            }
        }
        WriteOut wo = new WriteOut();
        wo.r = record;
        lastRecord = FLUSH.post(wo);
        
        if (flushOnRecord) {
            waitFlushed();
        }
    }

    public void flush() {
        waitFlushed();
    }
    
    static final void flushImmediatelly() {
        flushOnRecord = true;
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
        Installer.setSelectedExcParams(null);
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
