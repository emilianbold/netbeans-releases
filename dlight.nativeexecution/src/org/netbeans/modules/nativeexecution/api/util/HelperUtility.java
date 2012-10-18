/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.nativeexecution.api.util;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.support.InstalledFileLocatorProvider;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class HelperUtility {

    protected static final java.util.logging.Logger log = Logger.getInstance();
    private final HashMap<ExecutionEnvironment, String> cache = new HashMap<ExecutionEnvironment, String>();
    private final String pattern;
    protected final String codeNameBase;

    public HelperUtility(String searchPattern) {
        this("org.netbeans.modules.dlight.nativeexecution", searchPattern); // NOI18N
    }

    public HelperUtility(String codeNameBase, String searchPattern) {
        this.codeNameBase = codeNameBase;
        pattern = searchPattern;
    }

    /**
     *
     * @param env
     * @return the ready-to-use remote path for the utility
     * @throws IOException
     */
    public final String getPath(final ExecutionEnvironment env) throws IOException {
        HostInfo hinfo;
        try {
            hinfo = HostInfoUtils.getHostInfo(env);
        } catch (CancellationException ex) {
            return null;
        }
        return getPath(env, hinfo);
    }

    public final String getPath(final ExecutionEnvironment env, final HostInfo hinfo) throws IOException {
        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            throw new IllegalStateException(env.toString() + " is not connected"); // NOI18N
        }

        String result;

        synchronized (cache) {
            result = cache.get(env);

            if (result == null) {
                try {
                    String localFile = getLocalFileLocationFor(hinfo);

                    if (localFile == null) {
                        localFile = getLocalFileLocationFor(env);
                    }

                    if (env.isLocal()) {
                        result = localFile;
                    } else {
                        Logger.assertNonUiThread("Potentially long method " + getClass().getName() + ".getPath() is invoked in AWT thread"); // NOI18N

                        final String fileName = new File(localFile).getName();
                        final String remoteFile = hinfo.getTempDir() + '/' + fileName;

                        // Helper utility could be needed at the early stages
                        // Should not use NPB here
                        ConnectionManagerAccessor cmAccess = ConnectionManagerAccessor.getDefault();
                        ChannelSftp channel = (ChannelSftp) cmAccess.openAndAcquireChannel(env, "sftp", true); // NOI18N
                        if (channel == null) {
                            return null;
                        }
                        try {
                            channel.connect();
                            channel.put(localFile, remoteFile);
                            channel.chmod(0700, remoteFile);
                            result = remoteFile;
                        } catch (SftpException ex) {
                            log.log(Level.WARNING, "Failed to upload {0}", fileName); // NOI18N
                            Exceptions.printStackTrace(ex);
                        } finally {
                            cmAccess.closeAndReleaseChannel(env, channel);
                        }
                    }
                    cache.put(env, result);
                } catch (MissingResourceException ex) {
                    return null;
                } catch (IOException ex) {
                    throw ex;
                } catch (Exception ex) {
                    if (ex.getCause() instanceof IOException) {
                        throw (IOException) ex.getCause();
                    }
                    throw new IOException(ex);
                }
            }
        }

        return result;
    }

    protected String getLocalFileLocationFor(final HostInfo hinfo) throws MissingResourceException {
        return null;
    }

    protected String getLocalFileLocationFor(final ExecutionEnvironment env)
            throws ParseException, MissingResourceException {

        InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
        MacroExpander expander = MacroExpanderFactory.getExpander(env);
        String path = expander.expandPredefinedMacros(pattern);

        File file = fl.locate(path, codeNameBase, false);

        if (file == null || !file.exists()) {
            throw new MissingResourceException(path, null, null); //NOI18N
        }

        return file.getAbsolutePath();
    }
}
