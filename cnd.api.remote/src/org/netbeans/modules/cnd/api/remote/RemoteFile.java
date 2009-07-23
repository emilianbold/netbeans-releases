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

//TODO: this is wrong package for this class
package org.netbeans.modules.cnd.api.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Sergey Grinev
 */
public class RemoteFile extends File {

    private final ExecutionEnvironment execEnv;

    public ExecutionEnvironment getExecutionEnvironment() {
        return execEnv;
    }

    public static File create(ExecutionEnvironment execEnv, String pathname) {
        if (execEnv.isLocal()) {
            return new File(pathname);
        } else {
            return new RemoteFile(execEnv, pathname);
        }
    }

    public static Reader createReader(File file) throws FileNotFoundException {
        if (file instanceof RemoteFile) {
            RemoteFile rfile = (RemoteFile) file;

            CommandProvider cmd = Lookup.getDefault().lookup(CommandProvider.class);
            if (rfile.exists() && cmd.run(rfile.getExecutionEnvironment(), "cat " + rfile.getPath(), null) == 0) { //NOI18N
                //TODO: works only for absolute paths and only for short files
                return new StringReader(cmd.getOutput());
            } else {
                throw new FileNotFoundException(rfile.getPath() + " wasn't found on " + rfile.getExecutionEnvironment()); //NOI18N
            }

        } else {
            return new FileReader(file);
        }
    }

    private RemoteFile(ExecutionEnvironment execEnv, String pathname) {
        super(pathname);
        assert execEnv.isRemote(); //TODO: invent smth clever to split up remote ones from local
        this.execEnv = execEnv;
    }

    @Override
    public boolean exists() {
        //TODO: nonono
        return HostInfoProvider.fileExists(execEnv, getPath());
    }

    @Override
    public boolean delete() {
        Future<Integer> task = CommonTasksSupport.rmDir(execEnv, getPath(), true, null);
        try {
            int rc = task.get();
            return (rc == 0);
        } catch (InterruptedException ex) {
            return false;
        } catch (ExecutionException ex) {
            return false;
        }
    }

    @Override
    public File[] listFiles() {
        //TODO: till API review
        CommandProvider provider = Lookup.getDefault().lookup(CommandProvider.class);
        if (provider.run(execEnv, "ls -A1 \"" + getPath() + "\"", null) == 0) { //NOI18N
            String files = provider.getOutput();
            if (files != null) {
                BufferedReader bufferedReader = new BufferedReader(new StringReader(files));
                String line;
                ArrayList<File> lines = new ArrayList<File>();
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        lines.add(new RemoteFile(execEnv, getPath() + "/" + line)); //TODO: windows? //NOI18N
                    }
                    bufferedReader.close();
                } catch (IOException ex) {
                    //hardly can happen during reading string
                    Exceptions.printStackTrace(ex);
                    return null;
                }
                return lines.toArray(new File[lines.size()]);
            }
        }
        return null;
    }

    @Override
    public boolean isDirectory() {
        //TODO: till API review
        CommandProvider provider = Lookup.getDefault().lookup(CommandProvider.class);
        return provider.run(execEnv, "test -d \"" + getPath() + "\"", null) == 0; //NOI18N
    }

    @Override
    public boolean isFile() {
        //TODO: till API review
        CommandProvider provider = Lookup.getDefault().lookup(CommandProvider.class);
        return provider.run(execEnv, "test -f \"" + getPath() + "\"", null) == 0; //NOI18N
    }

    @Override
    public boolean canRead() {
        //TODO: till API review
        CommandProvider provider = Lookup.getDefault().lookup(CommandProvider.class);
        return provider.run(execEnv, "test -r \"" + getPath() + "\"", null) == 0; //NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteFile other = (RemoteFile) obj;
        if ((this.execEnv == null) ? (other.execEnv != null) : !this.execEnv.equals(other.execEnv)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + execEnv.hashCode() + 7;
    }
}
