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

package org.netbeans.modules.hudson.ui.actions;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.spi.HudsonLogger;
import org.netbeans.modules.hudson.spi.HudsonLogger.HudsonLogSession;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.hudson.spi.ProjectHudsonProvider;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Manages warning/error/stack trace hyperlinking in the Output Window.
 */
class Hyperlinker {

    private static final Logger LOG = Logger.getLogger(Hyperlinker.class.getName());

    private final HudsonLogSession[] sessions;

    public Hyperlinker(HudsonJob job) {
        List<HudsonLogSession> _sessions = new ArrayList<HudsonLogSession>();
        for (HudsonLogger logger : Lookup.getDefault().lookupAll(HudsonLogger.class)) {
            _sessions.add(logger.createSession(job));
        }
        sessions = _sessions.toArray(new HudsonLogSession[_sessions.size()]);
    }

    public void handleLine(String line, OutputWriter stream) {
        for (HudsonLogSession session : sessions) {
            if (session.handle(line, stream)) {
                break;
            }
        }
        // PlainLogger is last and always handles it
    }

    static class PlainLoggerLogic {
        private static final Pattern REMOTE_URL = Pattern.compile("\\b(https?://[^\\s)>]+)");
        private final HudsonJob job;
        /** Looks for errors mentioning workspace files. Prefix captures Maven's [WARNING], Ant's [javac], etc. */
        private final Pattern hyperlinkable;
        PlainLoggerLogic(HudsonJob job, String jobName) {
            this.job = job;
            // XXX support Windows build servers (using backslashes)
            String jobNameQ = Pattern.quote(jobName);
            hyperlinkable = Pattern.compile("\\s*(?:\\[.+\\] )?/.+?/(?:jobs/" + jobNameQ + "/workspace|workspace/" + jobNameQ + // NOI18N
                    ")/([^:]+):(?:\\[?([0-9]+)[:,](?:([0-9]+)[]:])?)? (?:warning: )?(.+)"); // NOI18N
        }
        OutputListener findHyperlink(String line) {
            try {
                Matcher m = hyperlinkable.matcher(line);
                if (m.matches()) {
                    final String path = m.group(1);
                    final int row = m.group(2) != null ? Integer.parseInt(m.group(2)) - 1 : -1;
                    final int col = m.group(3) != null ? Integer.parseInt(m.group(3)) - 1 : -1;
                    final String message = m.group(4);
                    return new Hyperlink(job, path, message, row, col);
                }
                m = REMOTE_URL.matcher(line);
                if (m.matches()) {
                    return new URLHyperlink(new URL(m.group()));
                }
            } catch (MalformedURLException x) {
                LOG.log(Level.FINE, null, x);
            }
            return null;
        }
    }

    @ServiceProvider(service=HudsonLogger.class)
    public static final class PlainLogger implements HudsonLogger {
        public HudsonLogSession createSession(final HudsonJob job) {
            return new HudsonLogSession() {
                final PlainLoggerLogic logic = new PlainLoggerLogic(job, job.getName());
                public boolean handle(String line, OutputWriter stream) {
                    OutputListener link = logic.findHyperlink(line);
                    if (link != null) {
                        try {
                            stream.println(line, link);
                            return true;
                        } catch (IOException x) {
                            LOG.log(Level.INFO, null, x);
                        }
                    }
                    stream.println(line);
                    return true;
                }
            };
        }
    }

    private static class Hyperlink implements OutputListener {

        private final HudsonJob job;
        private final String path;
        private final String message;
        private final int row;
        private final int col;

        public Hyperlink(HudsonJob job, String path, String message, int row, int col) {
            this.job = job;
            this.path = path;
            this.message = message;
            this.row = row;
            this.col = col;
        }

        public void outputLineAction(OutputEvent ev) {
            acted(true);
        }

        public void outputLineSelected(OutputEvent ev) {
            acted(false);
        }

        private void acted(final boolean force) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    FileObject f = null;
                    Project p = ProjectHudsonProvider.getDefault().findAssociatedProject(ProjectHudsonProvider.Association.forJob(job));
                    if (p != null) {
                        String localPath = null;
                        File localRoot = FileUtil.toFile(p.getProjectDirectory());
                        if (localRoot != null) {
                            for (HudsonSCM scm : Lookup.getDefault().lookupAll(HudsonSCM.class)) {
                                localPath = scm.translateWorkspacePath(job, path, localRoot);
                                if (localPath != null) {
                                    LOG.log(Level.FINE, "Translating remote path {0} to {1} using {2}", new Object[] {path, localPath, scm});
                                    break;
                                }
                            }
                        }
                        if (localPath == null) {
                            LOG.fine("Falling back to guess that remote workspace is a project root");
                            localPath = path;
                        }
                        // XXX permit localPath to include ../ segments; for Hg this is reasonable
                        f = p.getProjectDirectory().getFileObject(localPath);
                        LOG.log(Level.FINE, "Tried to find local file in {0} at {1} using {2}", new Object[] {p, f, localPath});
                        // XXX #159829: consider aligning local line number with remote line number somehow
                    }
                    if (f == null) {
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Hyperlinker.class, "Hyperlinker.looking_for", path));
                        f = job.getRemoteWorkspace().findResource(path);
                        LOG.log(Level.FINE, "Tried to find remote file at {0} using {1}", new Object[] {f, path});
                    }
                    if (f == null) {
                        if (force) {
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Hyperlinker.class, "Hyperlinker.not_found", path));
                            Toolkit.getDefaultToolkit().beep();
                        }
                        return;
                    }
                    // XXX could be useful to select this file in the workspace node (see related #159838)
                    StatusDisplayer.getDefault().setStatusText(message);
                    HudsonLogger.Helper.openAt(f, row, col, force);
                }
            });
        }

        public void outputLineCleared(OutputEvent ev) {}

        public @Override String toString() {
            return path + ":" + row + ":" + col + ":" + message; // NOI18N
        }
        
    }

    private static class URLHyperlink implements OutputListener {

        private final URL u;

        URLHyperlink(URL u) {
            this.u = u;
        }

        public void outputLineAction(OutputEvent ev) {
            URLDisplayer.getDefault().showURL(u);
        }

        public void outputLineSelected(OutputEvent ev) {}

        public void outputLineCleared(OutputEvent ev) {}

        public @Override String toString() {
            return u.toString();
        }

    }

}
