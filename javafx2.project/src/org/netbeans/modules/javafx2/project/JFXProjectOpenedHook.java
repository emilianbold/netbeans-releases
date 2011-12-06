/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.awt.Cursor;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.windows.WindowManager;

/**
 *
 * @author Tomas Zezula
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType={"org-netbeans-modules-java-j2seproject"}) // NOI18N
public final class JFXProjectOpenedHook extends ProjectOpenedHook {
//public final class JFXProjectOpenedHook extends ProjectOpenedHook implements TaskListener {

    private static final Logger LOGGER = Logger.getLogger("javafx"); // NOI18N
    
//    private volatile RequestProcessor.Task task;
//    private ProgressHandle progressHandle;

    @Override
    protected synchronized void projectOpened() {
        logUsage(JFXProjectGenerator.PROJECT_OPEN);
        
        // create Default JavaFX platform if necessary
        // #205341
        checkPlatforms();
    }

    @Override
    protected void projectClosed() {
        logUsage(JFXProjectGenerator.PROJECT_CLOSE);
    }

    private static void logUsage(@NonNull final String msg) {
        final LogRecord logRecord = new LogRecord(Level.INFO, msg);
        logRecord.setLoggerName(JFXProjectGenerator.METRICS_LOGGER);
        Logger.getLogger(JFXProjectGenerator.METRICS_LOGGER).log(logRecord);
    }

//    @Override
//    public synchronized void taskFinished(Task task) {
//        task.removeTaskListener(this);
//        this.task = null;
//        
//        progressHandle.finish();
//        progressHandle = null;
//    }

    private void checkPlatforms() {
        if (!JavaFXPlatformUtils.isThereAnyJavaFXPlatform()) {
            
            // I do it in the same thread because otherwise we have problem with "resolve reference" modal dialog.
            // Creation of Deafult JavaFX platform must be finished before J2SEProject checks for broken links after opening.
            switchBusy();
            try {
                JavaFXPlatformUtils.createDefaultJavaFXPlatform();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Can't create Java Platform instance: {0}", ex); // NOI18N
            } finally {
                switchDefault();
            }

//            progressHandle = ProgressHandleFactory.createSystemHandle(
//                    NbBundle.getMessage(PanelOptionsVisual.class, "MSG_Default_Platform_Creation")); // NOI18N
//            progressHandle.start();
            
//            task = RequestProcessor.getDefault().create(new CreatePlatformTask());
//            task.addTaskListener(this);
//            task.schedule(0);
        }
    }

//    private class CreatePlatformTask implements Runnable {
//        @Override
//        public void run() {
//            try {
//                JavaFXPlatformUtils.createDefaultJavaFXPlatform();
//            } catch (Exception ex) {
//                LOGGER.log(Level.WARNING, "Can't create Java Platform instance: {0}", ex); // NOI18N
//            }
//        }
//    }

    private void switchBusy() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        });
    }

    private void switchDefault() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

}
