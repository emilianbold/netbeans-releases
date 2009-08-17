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

package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.io.NullInputStream;

/**
 *
 * @author Vladimir Kvashin
 */
class RfsSyncWorker extends ZipSyncWorker {

    private static Parameters lastParameters;
    private static final boolean allAtOnce = false;
    
    /*package*/ static class Parameters {
        public final File localDir;
        public final String remoteDir;
        public final ExecutionEnvironment executionEnvironment;
        public final PrintWriter out;
        public final PrintWriter err;
        public final File privProjectStorageDir;
        public Parameters(File localDir, String remoteDir, ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, File privProjectStorageDir) {
            this.localDir = localDir;
            this.remoteDir = remoteDir;
            this.executionEnvironment = executionEnvironment;
            this.out = out;
            this.err = err;
            this.privProjectStorageDir = privProjectStorageDir;
        }
    }

    public RfsSyncWorker(File localDir, ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, File privProjectStorageDir) {
        super(localDir, executionEnvironment, out, err, privProjectStorageDir);        
    }

    @Override
    protected Zipper createZipper(File zipFile) {
        return new Zipper(zipFile) {
            @Override
            protected InputStream getFileInputStream(File file) throws FileNotFoundException {
                if (allAtOnce) {
                    return super.getFileInputStream(file);
                } else {
                    return new NullInputStream();
                }
            }

        };
    }

    /** FIXUP: this should be done via ActionHandler.*/
    /*package*/ static Parameters getLastParameters() {
        return lastParameters;
    }

    /** FIXUP: this should be done via ActionHandler.*/
    /*package*/ static void cleanLastParameters() {
        lastParameters = null;
    }

    @Override
    protected void synchronizeImpl(String remoteDir) throws InterruptedException, ExecutionException, IOException {
        lastParameters = new Parameters(localDir, remoteDir, executionEnvironment, out, err, privProjectStorageDir);
        super.synchronizeImpl(remoteDir);
    }

    @Override
    protected TimestampAndSharabilityFilter createFilter() {
        return new Filter(privProjectStorageDir, executionEnvironment) {
        };
    }

    private class Filter extends TimestampAndSharabilityFilter {

        public Filter(File privProjectStorageDir, ExecutionEnvironment executionEnvironment) {
            super(privProjectStorageDir, executionEnvironment);
        }

        @Override
        public boolean acceptImpl(File file) {
            return super.acceptImpl(file);
        }

        @Override
        public void flush() {
            // do nothing, since fake (empty) fies were sent!
        }

    }
}
