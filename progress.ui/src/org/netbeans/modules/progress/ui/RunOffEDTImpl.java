/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.progress.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.progress.spi.RunOffEDTProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * Default RunOffEDTProvider implementation for ProgressUtils.runOffEventDispatchThread() methods
 * @author Jan Lahoda, Tomas Holy
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.progress.spi.RunOffEDTProvider.class, position = 100)
public class RunOffEDTImpl implements RunOffEDTProvider {

    private static final RequestProcessor WORKER = new RequestProcessor(ProgressUtils.class.getName());
    private static final Map<Class<? extends Runnable>, Integer> OPERATIONS = new WeakHashMap<Class<? extends Runnable>, Integer>();
    private static final int CLEAR_TIME = 100;
    private static final int CANCEL_TIME = 1000;
    private static final int WARNING_TIME = Integer.getInteger("org.netbeans.modules.progress.ui.WARNING_TIME", 10000);
    private static final Logger LOG = Logger.getLogger(RunOffEDTImpl.class.getName());

    public void runOffEventDispatchThread(final Runnable operation, final String operationDescr, final AtomicBoolean cancelOperation, boolean waitForCanceled, int waitCursorTime, int dlgTime) {
        Parameters.notNull("operation", operation);
        Parameters.notNull("cancelOperation", cancelOperation);
        if (!SwingUtilities.isEventDispatchThread()) {
            operation.run();
            return;
        }
        long startTime = System.currentTimeMillis();
        runOffEventDispatchThreadImpl(operation, operationDescr, cancelOperation, waitForCanceled, waitCursorTime, dlgTime);
        int elapsed = (int) (System.currentTimeMillis() - startTime);

        boolean ea = false;
        assert ea = true;
        if (ea) {
            Class<? extends Runnable> clazz = operation.getClass();
            synchronized (OPERATIONS) {
                if (elapsed < CLEAR_TIME) {
                    OPERATIONS.remove(clazz);
                } else {
                    Integer prevElapsed = OPERATIONS.get(operation.getClass());
                    if (prevElapsed != null && elapsed + prevElapsed > WARNING_TIME) {
                        LOG.log(Level.WARNING, "Operation is too slow", new Exception(clazz + " is too slow"));
                    }
                    OPERATIONS.put(clazz, elapsed);
                }
            }
        }
    }

    private void runOffEventDispatchThreadImpl(final Runnable operation, final String operationDescr, final AtomicBoolean cancelOperation, boolean waitForCanceled, int waitCursorTime, int dlgTime) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Dialog> d = new AtomicReference<Dialog>();

        WORKER.post(new Runnable() {

            public void run() {
                if (cancelOperation.get()) {
                    return;
                }
                operation.run();
                latch.countDown();

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        Dialog dd = d.get();
                        if (dd != null) {
                            dd.setVisible(false);
                        }
                    }
                });
            }
        });

        Component glassPane = ((JFrame) WindowManager.getDefault().getMainWindow()).getGlassPane();

        if (waitMomentarily(glassPane, null, waitCursorTime, latch)) {
            return;
        }

        Cursor wait = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

        if (waitMomentarily(glassPane, wait, dlgTime, latch)) {
            return;
        }

        String title = NbBundle.getMessage(RunOffEDTImpl.class, "RunOffAWT.TITLE_Operation");
        String cancelButton = NbBundle.getMessage(RunOffEDTImpl.class, "RunOffAWT.BTN_Cancel");

        DialogDescriptor nd = new DialogDescriptor(operationDescr, title, true, new Object[]{cancelButton}, cancelButton, DialogDescriptor.DEFAULT_ALIGN, null, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancelOperation.set(true);
                d.get().setVisible(false);
            }
        });

        nd.setMessageType(NotifyDescriptor.INFORMATION_MESSAGE);

        d.set(DialogDisplayer.getDefault().createDialog(nd));
        d.get().setVisible(true);

        if (waitForCanceled) {
            try {
                if (!latch.await(CANCEL_TIME, TimeUnit.MILLISECONDS)) {
                    throw new IllegalStateException("Canceled operation did not finish in time.");
                }
            } catch (InterruptedException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }
    }

    private static boolean waitMomentarily(Component glassPane, Cursor wait, int timeout, final CountDownLatch l) {
        Cursor original = glassPane.getCursor();

        try {
            if (wait != null) {
                glassPane.setCursor(wait);
            }

            glassPane.setVisible(true);
            try {
                return l.await(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                LOG.log(Level.FINE, null, ex);
                return true;
            }
        } finally {
            glassPane.setVisible(false);
            glassPane.setCursor(original);
        }
    }
}
