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

package org.netbeans.api.progress;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.netbeans.progress.spi.RunOffEDTProvider;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Useful static methods
 * @author Tomas Holy
 * @since 1.16
 */
public final class ProgressUtils {
    private static final RunOffEDTProvider PROVIDER = getProvider();
    private static final int DISPLAY_DIALOG_MS = 9450;
    private static final int DISPLAY_WAIT_CURSOR_MS = 50;

    private ProgressUtils() {
    }

    private static RunOffEDTProvider getProvider() {
        RunOffEDTProvider p = Lookup.getDefault().lookup(RunOffEDTProvider.class);
        return p != null ? p : new Trivial();
    }

    /**
     * Runs operation out of event dispatch thread, blocks UI while operation is in progress. First it shows
     * wait cursor after ~50ms elapses, if operation takes longer than ~10s a dialog with Cancel button is shown.
     * <p>
     * This method is supposed to be used by user invoked foreground actions, that are expected to run very fast in vast majority of cases.
     * However, in some rather rare cases (e.g. extensive IO operations in progress), supplied operation may need longer time. In such case
     * this method first displays wait cursor and if operation takes even more time it displays dialog allowing to cancel operation.
     * DO NOT use this method for operations that may take long time under normal circumstances!
     * @param operation operation to perform
     * @param operationDescr text shown in dialog
     * @param cancelOperation set to true if user canceled the operation
     * @param waitForCanceled true if method should wait until canceled task is finished (if it is not finished in 1s ISE is thrown)
     */
    public static void runOffEventDispatchThread(Runnable operation, String operationDescr, AtomicBoolean cancelOperation, boolean waitForCanceled) {
        PROVIDER.runOffEventDispatchThread(operation, operationDescr, cancelOperation, waitForCanceled, DISPLAY_WAIT_CURSOR_MS, DISPLAY_DIALOG_MS);
    }

    /**
     * Runs operation out of event dispatch thread, blocks UI while operation is in progress. First it shows
     * wait cursor after <i>waitCursorAfter</i> elapses, if operation takes longer than <i>dialogAfter</i> a dialog with Cancel button is shown.
     * <p>
     * This method is supposed to be used by user invoked foreground actions, that are expected to run very fast in vast majority of cases.
     * However, in some rather rare cases (e.g. extensive IO operations in progress), supplied operation may need longer time. In such case
     * this method first displays wait cursor and if operation takes even more time it displays dialog allowing to cancel operation.
     * DO NOT use this method for operations that may take long time under normal circumstances!
     * @param operation operation to perform
     * @param operationDescr text shown in dialog
     * @param cancelOperation set to true if user canceled the operation
     * @param waitForCanceled true if method should wait until canceled task is finished (if it is not finished in 1s ISE is thrown)
     * @param waitCursorAfter time in ms after which wait cursor is shown
     * @param dialogAfter time in ms after which dialog with "Cancel" button is shown
     */
    public static void runOffEventDispatchThread(Runnable operation, String operationDescr, AtomicBoolean cancelOperation, boolean waitForCanceled, int waitCursorAfter, int dialogAfter) {
        PROVIDER.runOffEventDispatchThread(operation, operationDescr, cancelOperation, waitForCanceled, waitCursorAfter, dialogAfter);
    }

    private static class Trivial implements RunOffEDTProvider {
        private static final RequestProcessor WORKER = new RequestProcessor(ProgressUtils.class.getName());

        public void runOffEventDispatchThread(Runnable operation, String operationDescr, AtomicBoolean cancelOperation, boolean waitForCanceled, int waitCursorAfter, int dialogAfter) {
            if (SwingUtilities.isEventDispatchThread()) {
                Task t = WORKER.post(operation);
                t.waitFinished();
            } else {
                operation.run();
            }
        }
    }
}
