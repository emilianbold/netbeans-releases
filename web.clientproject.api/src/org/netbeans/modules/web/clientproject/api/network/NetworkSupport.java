/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.api.network;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.web.clientproject.api.network.ui.NetworkErrorPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Helper class for network issues and tasks.
 * @since 1.13
 */
public final class NetworkSupport {

    private static final Logger LOGGER = Logger.getLogger(NetworkSupport.class.getName());


    private NetworkSupport() {
    }

    /**
     * Show network error dialog with possibility to retry the request
     * or open general IDE options where proxy can be configured.
     * <p>
     * See {@link #showNetworkErrorDialog(List)} for more information.
     * @param failedRequest request that failed, never {@code null}
     * @return {@code true} if the request should be downloaded once more, {@code false} otherwise
     * @see #showNetworkErrorDialog(List)
     */
    public static boolean showNetworkErrorDialog(String failedRequest) {
        Parameters.notNull("failedRequest", failedRequest); // NOI18N
        return showNetworkErrorDialog(Collections.singletonList(failedRequest));
    }

    /**
     * Show network error dialog with possibility to retry the requests
     * or open general IDE options where proxy can be configured.
     * <p>
     * Notes:
     * <ul>
     *   <li>If the request is URL (starts with "http://" or "https://"), it is
     *       displayed as a hyperlink.</li>
     *   <li>If the request is longer than {@value NetworkErrorPanel#MAX_REQUEST_LENGTH}
     *       characters, it is truncated (using "...").</li>
     * </ul>
     * @param failedRequests requests that failed, never {@code null}
     * @return {@code true} if the requests should be downloaded once more, {@code false} otherwise
     * @see #showNetworkErrorDialog(String)
     */
    @NbBundle.Messages({
        "NetworkSupport.errorDialog.title=Network error",
        "# {0} - failed URLs",
        "NetworkSupport.errorDialog.text=<html>Network error occured while processing these requests:<br><br>{0}<br><br>Try it again?",
        "NetworkSupport.errorDialog.configureProxy=Configure Proxy..."
    })
    public static boolean showNetworkErrorDialog(List<String> failedRequests) {
        Parameters.notNull("failedRequests", failedRequests); // NOI18N
        if (failedRequests.isEmpty()) {
            throw new IllegalArgumentException("Failed requests must be provided.");
        }
        DialogDescriptor descriptor = new DialogDescriptor(
                new NetworkErrorPanel(failedRequests),
                Bundle.NetworkSupport_errorDialog_title(),
                true,
                DialogDescriptor.YES_NO_OPTION,
                DialogDescriptor.YES_OPTION,
                null);
        JButton configureProxyButton = new JButton(Bundle.NetworkSupport_errorDialog_configureProxy());
        configureProxyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsDisplayer.getDefault().open(OptionsDisplayer.GENERAL);
            }
        });
        descriptor.setAdditionalOptions(new Object[] {configureProxyButton});
        return DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION;
    }

    /**
     * Download the given URL to the target file.
     * <p>
     * This method must be called only in a background thread.
     * @param url URL to be downloaded
     * @param target target file
     * @throws NetworkException if any network error occurs
     * @throws IOException if any error occurs
     * @since 1.22
     */
    public static void download(@NonNull String url, @NonNull File target) throws NetworkException, IOException {
        Parameters.notNull("url", url);
        Parameters.notNull("target", target);
        if (EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Cannot run in UI thread");
        }
        InputStream is;
        try {
            is = new URL(url).openStream();
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            throw new NetworkException(url, ex);
        }
        try {
            copyToFile(is, target);
        } catch (IOException ex) {
            // error => ensure file is deleted
            if (!target.delete()) {
                // nothing we can do about it
            }
            throw ex;
        } finally {
            is.close();
        }
    }

    private static File copyToFile(InputStream is, File target) throws IOException {
        OutputStream os = new FileOutputStream(target);
        try {
            FileUtil.copy(is, os);
        } finally {
            os.close();
        }
        return target;
    }

}
