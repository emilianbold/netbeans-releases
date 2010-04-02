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
package org.netbeans.modules.nativeexecution.support;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.concurrent.Future;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author ak119685
 */
public class HelperUtility {

    private final HashMap<ExecutionEnvironment, String> cache = new HashMap<ExecutionEnvironment, String>();
    private final String pattern;

    protected HelperUtility(String searchPattern) {
        pattern = searchPattern;
    }

    /**
     *
     * @param env
     * @return the ready-to-use remote path for the utility
     * @throws IOException
     */
    public final String getPath(final ExecutionEnvironment env) throws IOException {
        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            throw new IllegalStateException(env.toString() + " is not connected"); // NOI18N
        }

        if (!HostInfoUtils.isHostInfoAvailable(env)) {
            throw new IllegalStateException("No hostinfo for " + env.toString()); // NOI18N
        }

        String result = null;

        synchronized (cache) {
            result = cache.get(env);

            if (result == null) {
                try {
                    HostInfo hinfo = HostInfoUtils.getHostInfo(env);
                    String localFile = getLocalFileLocationFor(env);

                    if (env.isLocal()) {
                        result = localFile;
                    } else {
                        final String fileName = new File(localFile).getName();
                        final String remoteFile = hinfo.getTempDir() + '/' + fileName;

//                        if (!HostInfoUtils.fileExists(env, remoteFile)) {
                            Future<Integer> uploadTask = CommonTasksSupport.uploadFile(localFile, env, remoteFile, 0755, null);
                            Integer uploadResult = uploadTask.get();
                            if (uploadResult != 0) {
                                throw new IOException("Unable to upload " + fileName + " to " + env.getDisplayName()); // NOI18N
                            }
//                        }
                        result = remoteFile;
                    }
                    cache.put(env, result);
                } catch (IOException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }
        }

        return result;
    }

    private String getLocalFileLocationFor(final ExecutionEnvironment env)
            throws ParseException, MissingResourceException {

        InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
        MacroExpander expander = MacroExpanderFactory.getExpander(env);
        String path = expander.expandPredefinedMacros(pattern);

        File file = fl.locate(path, null, false);

        if (file == null || !file.exists()) {
            throw new MissingResourceException(path, null, null);
        }

        return file.getAbsolutePath();
    }
}
