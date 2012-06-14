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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import static org.netbeans.modules.hudson.ui.actions.Bundle.*;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Displays the console from a Hudson build in the Output Window.
 */
public class ShowBuildConsole extends AbstractAction implements Runnable {

    private static final Logger LOG = Logger.getLogger(ShowBuildConsole.class.getName());

    private final HudsonJob job;
    private final String url;
    private final String displayName;

    public ShowBuildConsole(HudsonJobBuild build) {
        this(build.getJob(), build.getUrl(), build.getDisplayName());
    }

    public ShowBuildConsole(HudsonMavenModuleBuild module) {
        this(module.getBuild().getJob(), module.getUrl(), module.getBuildDisplayName());
    }

    @Messages("ShowBuildConsole.label=Show Console")
    private ShowBuildConsole(HudsonJob job, String url, String displayName) {
        this.job = job;
        this.url = url;
        this.displayName = displayName;
        putValue(NAME, ShowBuildConsole_label());
    }

    @Override public void actionPerformed(ActionEvent e) {
        new RequestProcessor(url + "console").post(this); // NOI18N
    }

    @SuppressWarnings("OS_OPEN_STREAM")
    @java.lang.SuppressWarnings("SleepWhileInLoop")
    @Override public void run() {
        Hyperlinker hyperlinker = new Hyperlinker(job);
        LOG.log(Level.FINE, "{0} started", url);
        InputOutput io = IOProvider.getDefault().getIO(displayName, new Action[] {/* XXX abort build button? */});
        io.select();
        /* If any metadata is needed, e.g. whether it is running, could use:
        HudsonJobBuild build = instance.getConnector().getJobBuild(job, buildNumber);
        if (build == null) {
            return;
        }
         */
        int start = 0;
        String urlPrefix = url + "progressiveLog?start="; // NOI18N
        OutputWriter out = io.getOut();
        OutputWriter err = io.getErr();
        boolean running = job.getLastBuild() > job.getLastCompletedBuild(); // XXX should also check that this is in fact the current build
        try {
            while (true) {
                LOG.log(Level.FINE, "{0} polling", url);
                if (out.checkError() || err.checkError() || io.isClosed()) {
                    LOG.log(Level.FINE, "{0} stopped", url);
                    break;
                }
                URLConnection conn = new ConnectionBuilder().job(job).url(urlPrefix + start).
                        header("Accept-Encoding", "gzip"). // NOI18N
                        connection();
                boolean moreData = Boolean.parseBoolean(conn.getHeaderField("X-More-Data")); // NOI18N
                LOG.log(Level.FINE, "{0} retrieving text from {1}", new Object[] {url, start});
                start = conn.getHeaderFieldInt("X-Text-Size", start); // NOI18N
                InputStream is = conn.getInputStream();
                try {
                    InputStream isToUse = is;
                    if ("gzip".equals(conn.getContentEncoding())) { // NOI18N
                        LOG.log(Level.FINE, "{0} using GZIP", url);
                        isToUse = new GZIPInputStream(is);
                    }
                    // XXX safer to check content type on connection, but in fact Stapler sets it to UTF-8
                    BufferedReader r = new BufferedReader(new InputStreamReader(isToUse, "UTF-8")); // NOI18N
                    String line;
                    while ((line = r.readLine()) != null) {
                        OutputWriter stream = line.matches("(?i).*((warn(ing)?|err(or)?)[]:]|failed).*") ? err : out; // NOI18N
                        hyperlinker.handleLine(line, stream);
                    }
                } finally {
                    is.close();
                }
                if (!moreData) {
                    LOG.log(Level.FINE, "{0} EOF", url);
                    if (running) {
                        LOG.fine("was running, will resynchronize");
                        HudsonInstance instance = job.getInstance();
                        if (instance instanceof HudsonInstanceImpl) {
                            ((HudsonInstanceImpl) instance).synchronize(false);
                        }
                    }
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException x) {
                    LOG.log(Level.FINE, "{0} interrupted", url);
                    break;
                }
            }
        } catch (IOException x) {
            LOG.log(Level.INFO, null, x);
        }
        out.close();
        err.close();
    }

}
