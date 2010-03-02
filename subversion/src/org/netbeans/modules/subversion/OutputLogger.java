/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.subversion;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.versioning.util.OpenInEditorAction;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class OutputLogger implements ISVNNotifyListener {

    private InputOutput log;
    private boolean ignoreCommand = false;
    private String repositoryRootString;
    private static final RequestProcessor rp = new RequestProcessor("SubversionOutput", 1);
    private boolean writable; // output window is open and can be written into
    /**
     * cache of already opened output windows
     * IOProvider automatically opens OW after its first initialization,
     * in the second call it returns the handle from its cache and OW is not opened again.
     * So if this cache doesn't contain the repository string yet, it will probably mean the OW is automatically opened
     * and in that case it should be closed again. See getLog().
     */
    private static final HashSet<String> openedWindows = new HashSet<String>(5);
    private static final Pattern filePattern = Pattern.compile("[AUCGE ][ UC][ BC] (.+)"); //NOI18N
    
    public static OutputLogger getLogger(SVNUrl repositoryRoot) {
        if (repositoryRoot != null) {
            return new OutputLogger(repositoryRoot);
        } else {
            return new NullLogger();
        }
    }
    private AbstractAction action;
    
    private OutputLogger(SVNUrl repositoryRoot) {
        repositoryRootString = repositoryRoot.toString();
    }

    private OutputLogger() {
    }
    
    public void logCommandLine(final String commandLine) {
        rp.post(new Runnable() {
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
    
    public void logCompleted(final String message) {
        rp.post(new Runnable() {
            public void run() {                
                logln(message, ignoreCommand);
                flush();
            }
        });        
    }
    
    public void logError(final String message) {
        rp.post(new Runnable() {
            public void run() {                
                logln(message, false);
                flush();
            }
        });            
    }
    
    public void logMessage(final String message) {
        rp.post(new Runnable() {
            public void run() {                
                logln(message, ignoreCommand);
                flush();
            }
        });
    }
    
    public void logRevision(long revision, String path) {
       // logln(" revision " + revision + ", path = '" + path + "'");
    }
    
    public void onNotify(File path, SVNNodeKind kind) {
        //logln(" file " + path + ", kind " + kind);
    }
    
    public void setCommand(final int command) {
        rp.post(new Runnable() {
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
            public void run() {        
                getLog();
                flush();
            }
        });        
    }
    
    private void logln(String message, boolean ignore) {
        OpenFileOutputListener ol = null;
        Matcher m = filePattern.matcher(message);
        if (m.matches() && m.groupCount() > 0) {
            String path = m.group(1);
            File f = new File(path);
            if (!f.isDirectory()) {
                ol = new OpenFileOutputListener(FileUtil.normalizeFile(f), m.start(1));
            }
        }
        log(message + "\n", ol, ignore); // NOI18N
    }

    private void log(String message, OpenFileOutputListener hyperlinkListener, boolean ignore) {
        if(ignore) {
            return;
        }
        if (getLog().isClosed()) {
            if (SvnModuleConfig.getDefault().getAutoOpenOutput()) {
                Subversion.LOG.fine("Creating OutputLogger for " + repositoryRootString);
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
            Subversion.LOG.fine("Creating OutputLogger for " + repositoryRootString);
            log = IOProvider.getDefault().getIO(repositoryRootString, false);
            if (!openedWindows.contains(repositoryRootString)) {
                // log window has been opened
                writable = SvnModuleConfig.getDefault().getAutoOpenOutput();
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
                public void actionPerformed(ActionEvent e) {
                    writable = true;
                    getLog().select();
                }
            };
        }
        return action;
    }

    private static class NullLogger extends OutputLogger {

        public void logCommandLine(String commandLine) {
        }

        public void logCompleted(String message) {
        }

        public void logError(String message) {
        }

        public void logMessage(String message) {
        }

        public void logRevision(long revision, String path) {
        }

        public void onNotify(File path, SVNNodeKind kind) {
        }

        public void setCommand(int command) {
        }

        public void closeLog() {
        }

        public void flushLog() {
        }
    }

    private static class OpenFileOutputListener implements OutputListener {
        private final File f;
        private final int filePathStartPos;

        public OpenFileOutputListener(File f, int filePathStartPos) {
            this.f = f;
            this.filePathStartPos = filePathStartPos;
        }

        @Override
        public void outputLineSelected(OutputEvent ev) { }

        @Override
        public void outputLineAction(OutputEvent ev) {
            new OpenInEditorAction(new File[] {f}).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, f.getAbsolutePath()));
        }

        @Override
        public void outputLineCleared(OutputEvent ev) { }

    }

}
