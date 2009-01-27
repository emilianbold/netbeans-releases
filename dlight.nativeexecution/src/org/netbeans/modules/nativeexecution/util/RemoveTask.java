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
package org.netbeans.modules.nativeexecution.util;

import org.netbeans.modules.nativeexecution.api.NativeTask;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import java.io.File;
import java.util.concurrent.ExecutionException;
import org.openide.util.Exceptions;

/**
 * Implementation of files removing routines
 */
public final class RemoveTask {

    private RemoveTask() {
    }

    /**
     * Removes directory and it's content
     * @param execEnv <tt>ExecutionEnvironment</tt> where to delete directory
     * @param dir directory to remove
     * @param force whether delete read-only files or not
     * @return true on successfull removal
     */
    public static boolean removeDirectory(final ExecutionEnvironment execEnv,
            final String dir, final boolean force) {
        if (execEnv.isLocal()) {
            return deleteLocalDirectory(new File(dir), force);
        }

        String flags = "-r"; // NOI18N

        if (force) {
            flags = flags.concat("f"); // NOI18N
        }

        NativeTask ddt = new NativeTask(execEnv,
                "/bin/rm", // NOI18N
                new String[]{flags, dir});

        ddt.submit();

        Integer result = -1;

        try {
            result = ddt.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result == 0;
    }

    private static boolean deleteLocalDirectory(final File path,
            final boolean force) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteLocalDirectory(files[i], force);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
