/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.groovy.grailsproject.actions.LineSnooper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class GrailsProcessRunnable implements Runnable, Cancellable {

    private static final Logger LOGGER = Logger.getLogger(GrailsProcessRunnable.class.getName());

    private final Process process;

    private final LineSnooper snooper;

    private final String taskName;

    private ProgressHandle progressHandle;

    // FIXME snooper will be replaced with output API
    public GrailsProcessRunnable(Process process, LineSnooper snooper, String taskName) {
        this.process = process;
        this.taskName = taskName;
        this.snooper = snooper;
    }

    public final void run() {
        InputOutput io = IOProvider.getDefault().getIO(taskName, true);
        io.select();

        synchronized (this) {
            progressHandle = ProgressHandleFactory.createHandle(taskName, this);
            progressHandle.start();
        }
        try {
            // FIXME will be repaced with output API
            new StreamInputThread(process.getOutputStream(), io.getIn()).start();
            new StreamRedirectThread(process.getErrorStream(), io.getErr()).start();

            if (snooper != null) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                try {
                    String lineString;
                    // while stdout gets filtered through the snooper
                    while ((lineString = reader.readLine()) != null) {
                        snooper.lineFilter(lineString);
                        io.getOut().println(lineString);
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                } finally {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    }
                }
            } else {
                new StreamRedirectThread(process.getInputStream(), io.getOut()).start();
            }

            process.waitFor();
        } catch (InterruptedException ex) {
            LOGGER.log(Level.FINE, "Exiting thread", ex);
            process.destroy();
        } catch (Exception ex) {
            // FIXME localize me
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                "Problem creating Process: " + ex.getLocalizedMessage(),
                NotifyDescriptor.Message.WARNING_MESSAGE));
        } finally {
            finishProgress();
            finish();
        }
    }

    public void finish() {

    }

    public final boolean cancel() {
        process.destroy();
        finishProgress();
        return true;
    }

    private synchronized void finishProgress() {
        if (progressHandle != null) {
            progressHandle.finish();
            progressHandle = null;
        }
    }

}
