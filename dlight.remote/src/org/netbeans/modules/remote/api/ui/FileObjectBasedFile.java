/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.api.ui;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author ak119685
 */
/*package*/ class FileObjectBasedFile extends File {
    final ExecutionEnvironment env;

    private final FileObject fo;
    private File[] NO_CHILDREN = new File[0];

    public FileObjectBasedFile(ExecutionEnvironment env, String path) {
        super(path);
        this.fo = null;
        this.env = env;
    }

    public FileObjectBasedFile(ExecutionEnvironment env, FileObject fo) {
        super( fo == null || "".equals(fo.getPath()) ? "/" : fo.getPath()); // NOI18N
        this.fo = fo;
        this.env = env;
    }

    @Override
    public boolean isDirectory() {
        return fo == null ? false : fo.isFolder();
    }

    @Override
    public boolean exists() {
        return fo == null ? false : fo.isValid();
    }

    @Override
    public boolean renameTo(File dest) {
        Future<Integer> result = renameTo(env, getPath(), dest.getPath(), new StringWriter());
        try {
            return result.get() == 0;
        } catch (InterruptedException ex) {

        } catch (ExecutionException ex) {

        }
        return false;
    }

    

    @Override
    public boolean mkdirs() {
        if (fo == null){
            Future<Integer> result = CommonTasksSupport.mkDir(env, getPath(), new StringWriter());
            try {
                return result.get() == 0;

            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean mkdir() {
        return mkdirs();
    }

    @Override
    public boolean canWrite() {
       return false;
    }




    @Override
    public String getPath() {
        return fo == null ? super.getPath() : fo.getPath();
    }

    @Override
    public File getParentFile() {
        if (fo == null) {
            return null;
        }
        FileObject parent = fo.getParent();
        return parent == null ? null : new FileObjectBasedFile(env,parent);
    }

    @Override
    public boolean isFile() {
        return !isDirectory();
    }

    @Override
    public String getAbsolutePath() {
        String res = fo == null ? super.getAbsolutePath() : fo.getPath();
        if (res != null && Utilities.isWindows()) {
            res = res.replace('\\', '/'); // NOI18N
            while (res.startsWith("//")) { // NOI18N
                res = res.substring(1);
            }
        }
        return res;
    }

    @Override
    public File[] listFiles() {
        if (fo == null) {
            return NO_CHILDREN;
        }

        FileObject[] children = fo.getChildren();

        if (children.length == 0) {
            fo.refresh();
            children = fo.getChildren();
        }

        File[] res = new File[children.length];
        int idx = 0;
        for (FileObject child : children) {
            res[idx++] = new FileObjectBasedFile(env, child);
        }

        return res;
    }

    @Override
    public File getCanonicalFile() throws IOException {
        return this;
    }
    private static final String PREFIX = "NATIVEEXECUTOR: "; // NOI18N
   private static final RequestProcessor processor = new RequestProcessor(PREFIX, 50);

    public static Future<Integer> renameTo(final ExecutionEnvironment execEnv, final String sourceDir,
            final String destDir, final Writer error) {
        final FutureTask<Integer> ftask = new FutureTask<Integer>(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                Thread.currentThread().setName(PREFIX + "mv " + sourceDir + " " + destDir);//NOI18N
                return new CommandRunner(execEnv, error, "mv", sourceDir, destDir).call();//NOI18N
            }
        });


        processor.post(ftask);
        return ftask;

    }

    private static class CommandRunner implements Callable<Integer> {

        private final ExecutionEnvironment execEnv;
        private final String cmd;
        private final String[] args;
        private final Writer error;

        public CommandRunner(ExecutionEnvironment execEnv, Writer error, String cmd, String... args) {
            this.execEnv = execEnv;
            this.cmd = cmd;
            this.args = args;
            this.error = error;
        }

        public Integer call() throws Exception {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(cmd).setArguments(args);
            Process p = npb.call();

            int exitStatus = p.waitFor();

            if (exitStatus != 0) {
                if (error != null) {
                    ProcessUtils.writeError(error, p);
                } 
            }

            return exitStatus;
        }
    }

  

}
