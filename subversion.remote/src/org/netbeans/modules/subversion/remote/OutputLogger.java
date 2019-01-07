/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.SVNNodeKind;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.subversion.remote.versioning.util.OpenInEditorAction;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * 
 */
public class OutputLogger implements ISVNNotifyListener {

    private InputOutput log;
    private boolean ignoreCommand = false;
    private String repositoryRootString;
    private static final RequestProcessor rp = new RequestProcessor("SubversionOutput", 1); // NOI18N
    private boolean writable; // output window is open and can be written into
    /**
     * cache of already opened output windows
     * IOProvider automatically opens OW after its first initialization,
     * in the second call it returns the handle from its cache and OW is not opened again.
     * So if this cache doesn't contain the repository string yet, it will probably mean the OW is automatically opened
     * and in that case it should be closed again. See getLog().
     */
    private static final HashSet<String> openedWindows = new HashSet<>(5);
    private static final Pattern[] filePatterns = new Pattern[] {
        Pattern.compile("[AUCGE ][ UC][ BC][ C] ?(.+)"), //NOI18N
        Pattern.compile("Reverted '(.+)'"), //NOI18N - for commandline
        Pattern.compile("Reverted (.+)"), //NOI18N - for javahl
        Pattern.compile("Sending        (.+)"), //NOI18N
        Pattern.compile("Adding         (.+)") //NOI18N
    };
    
    public static OutputLogger getLogger(FileSystem fileSystem, SVNUrl repositoryRoot) {
        if (repositoryRoot != null) {
            return new OutputLogger(fileSystem, repositoryRoot);
        } else {
            return new NullLogger();
        }
    }
    private AbstractAction action;
    private String lastCompletedMessage;
    private final FileSystem fileSystem;
    
    private OutputLogger(FileSystem fileSystem, SVNUrl repositoryRoot) {
        repositoryRootString = SvnUtils.decodeToString(repositoryRoot);
        this.fileSystem = fileSystem;
    }

    private OutputLogger() {
        fileSystem = null;
    }
    
    @Override
    public void logCommandLine(final String commandLine) {
        rp.post(new Runnable() {
            @Override
            public void run() {                        
                logln(commandLine, false);
                flush();
            }
        });        
    }

    private void flush () {
        if (writable) {
            getLog().getOut().flush();
        }
    }
    
    @Override
    public void logCompleted(final String message) {
        if (message.equals(lastCompletedMessage)) {
            return;
        }
        lastCompletedMessage = message;
        rp.post(new Runnable() {
            @Override
            public void run() {                
                logln(message, ignoreCommand);
                flush();
            }
        });        
    }
    
    @Override
    public void logError(final String message) {
        if (message == null) {
            return;
        }
        rp.post(new Runnable() {
            @Override
            public void run() {                
                logln(message, false);
                flush();
            }
        });            
    }
    
    @Override
    public void logMessage(final String message) {
        rp.post(new Runnable() {
            @Override
            public void run() {                
                logln(message, ignoreCommand);
                flush();
            }
        });
    }
    
    @Override
    public void logRevision(long revision, String path) {
       // logln(" revision " + revision + ", path = '" + path + "'");
    }
    
    @Override
    public void onNotify(VCSFileProxy path, SVNNodeKind kind) {
        //logln(" file " + path + ", kind " + kind);
    }
    
    @Override
    public void setCommand(final ISVNNotifyListener.Command command) {
        rp.post(new Runnable() {
            @Override
            public void run() {        
                ignoreCommand = command == ISVNNotifyListener.Command.INFO ||
                                command == ISVNNotifyListener.Command.STATUS ||
                                command == ISVNNotifyListener.Command.ANNOTATE ||
                                command == ISVNNotifyListener.Command.LOG ||
                                command == ISVNNotifyListener.Command.LS;
            }
        });
    }
         
    public void closeLog() {
        rp.post(new Runnable() {
            @Override
            public void run() {
                if (log != null && writable) {
                    getLog().getOut().flush();
                    getLog().getOut().close();
                }
            }
        });
    }

    public void flushLog() {
        rp.post(new Runnable() {
            @Override
            public void run() {        
                getLog();
                flush();
            }
        });        
    }
    
    private void logln(String message, boolean ignore) {
        OpenFileOutputListener ol = null;
        for (Pattern p : filePatterns) {
            Matcher m = p.matcher(message);
            if (m.matches() && m.groupCount() > 0) {
                String path = m.group(1);
                VCSFileProxy f = VCSFileProxySupport.getResource(fileSystem, path);
                if (!f.isDirectory()) {
                    ol = new OpenFileOutputListener(f.normalizeFile(), m.start(1));
                    break;
                }
            }
        }
        log(message + "\n", ol, ignore); // NOI18N
    }

    private void log(String message, OpenFileOutputListener hyperlinkListener, boolean ignore) {
        if(ignore) {
            return;
        }
        if (getLog().isClosed()) {
            if (SvnModuleConfig.getDefault(fileSystem).getAutoOpenOutput()) {
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.log(Level.FINE, "Creating OutputLogger for {0}", repositoryRootString); // NOI18N
                }
                log = IOProvider.getDefault().getIO(repositoryRootString, false);
                try {
                    // HACK (mystic logic) workaround, otherwise it writes to nowhere
                    getLog().getOut().reset();
                } catch (IOException e) {
                    Subversion.LOG.log(Level.SEVERE, null, e);
                }
            } else {
                writable = false;
            }
        }
        if (writable) {
            if (hyperlinkListener != null) {
                try {
                    String prefix = message.substring(0, hyperlinkListener.filePathStartPos);
                    getLog().getOut().write(prefix);
                    String filePath = message.substring(hyperlinkListener.filePathStartPos);
                    getLog().getOut().println(filePath.endsWith("\n") ? filePath.substring(0, filePath.length() - 1) : filePath, hyperlinkListener); //NOI18N
                } catch (IOException e) {
                    getLog().getOut().write(message);
                }
            } else {
                getLog().getOut().write(message);
            }
        }
    }

    /**
     * @return the log
     */
    private InputOutput getLog() {
        writable = true;
        if(log == null) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, "Creating OutputLogger for {0}", repositoryRootString);
            }
            log = IOProvider.getDefault().getIO(repositoryRootString, false);
            if (!openedWindows.contains(repositoryRootString)) {
                // log window has been opened
                writable = SvnModuleConfig.getDefault(fileSystem).getAutoOpenOutput();
                openedWindows.add(repositoryRootString);
                if (!writable) {
                    // close it again
                    log.closeInputOutput();
                }
            }
        }
        return log;
    }

    public Action getOpenOutputAction() {
        if(action == null) {
            action = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    writable = true;
                    getLog().select();
                }
            };
        }
        return action;
    }

    private static class NullLogger extends OutputLogger {
        @Override
        public void logCommandLine(String commandLine) { }
        @Override
        public void logCompleted(String message) { }
        @Override
        public void logError(String message) { }
        @Override
        public void logMessage(String message) { }
        @Override
        public void logRevision(long revision, String path) { }
        @Override
        public void onNotify(VCSFileProxy path, SVNNodeKind kind) { }
        @Override
        public void setCommand(ISVNNotifyListener.Command command) { }
        @Override
        public void closeLog() { }
        @Override
        public void flushLog() { }
    }

    private static class OpenFileOutputListener implements OutputListener {
        private final VCSFileProxy f;
        private final int filePathStartPos;

        public OpenFileOutputListener(VCSFileProxy f, int filePathStartPos) {
            this.f = f;
            this.filePathStartPos = filePathStartPos;
        }

        @Override
        public void outputLineSelected(OutputEvent ev) { }

        @Override
        public void outputLineAction(OutputEvent ev) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, "Opeining file [{0}]", f);           // NOI18N
            }
            new OpenInEditorAction(new VCSFileProxy[] {f}).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, f.getPath()));
        }

        @Override
        public void outputLineCleared(OutputEvent ev) { }

    }

}
