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
package org.netbeans.modules.dlight.perfan.spi;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;

final class SSDCWarmUpTask implements Callable<Boolean> {

    private static final Logger log = DLightLogger.getLogger(SSDCWarmUpTask.class);
    private final String dirName;
    private final ExecutionEnvironment execEnv;

    public SSDCWarmUpTask(ExecutionEnvironment execEnv, String dirName) {
        this.dirName = dirName;
        this.execEnv = execEnv;
    }

    private static void stopIfInterrupted() throws InterruptedException {
        Thread.yield();
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("WarmUp interrupted"); // NOI18N
        }
    }

    public Boolean call() throws Exception {
        boolean status = true;

        log.fine("Prepare PerfanDataCollector. Clean directory " + dirName); // NOI18N
        Future<Integer> rmFuture;
        Integer rmResult = null;

        rmFuture = CommonTasksSupport.rmDir(execEnv, dirName, true, null);

        try {
            rmResult = rmFuture.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
        }

        if (rmResult == null || rmResult.intValue() != 0) {
            log.info("SunStudioDataCollector: unable to delete directory " // NOI18N
                    + execEnv.toString() + ":" + dirName); // NOI18N
            status = false;
        }

        stopIfInterrupted();

        File lockFile = new File(new File(dirName).getParentFile(), "_collector_directory_lock"); // NOI18N
        rmFuture = CommonTasksSupport.rmFile(execEnv, lockFile.getPath(), null);

        try {
            rmResult = rmFuture.get();
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        }

        if (status == false) {
            log.severe("Unable to prepare experiment directory!"); // NOI18N
        }

        return new Boolean(status);
    }
}
