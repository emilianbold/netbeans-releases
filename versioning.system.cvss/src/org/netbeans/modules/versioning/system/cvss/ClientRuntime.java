/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.event.TerminationEvent;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.BasicCommand;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.command.importcmd.ImportCommand;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.proxy.ProxySocketFactory;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.openide.util.Task;
import org.openide.ErrorManager;
import org.openide.windows.InputOutput;
import org.openide.windows.IOProvider;
import org.openide.windows.OutputListener;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.versioning.util.KeyringSupport;

/**
 * Defines a runtime environment for one CVSRoot. Everytime a command is executed for a new CVSRoot,
 * a new instance of ClientRuntime is created and cached. This objects is then responsible for running
 * commands for that CVS root.
 * 
 * @author Maros Sandor
 */
public class ClientRuntime {

    /**
     * The CVS Root this class manages.
     */ 
    private final String        cvsRoot; 

    /**
     * The CVS Root this class manages without password field.
     */ 
    private String              cvsRootDisplay; 
    
    /**
     * Processor to use when posting commands to given CVSRoot. It has a throughput of 1.
     */ 
    private RequestProcessor    requestProcessor = new RequestProcessor("CVS ClientRuntime", 1, true);

    /**
     * Holds server communication log for associated cvs root.
     */
    private InputOutput log;

    ClientRuntime(String root) {
        cvsRoot = root;
        cvsRootDisplay = root;
        try {
            CVSRoot rt = CVSRoot.parse(cvsRoot);
            if (rt.getPassword() != null) {
                // this is not 100% correct but is straightforward
                int idx = root.indexOf(rt.getPassword());
                if (idx != -1) {
                    cvsRootDisplay = cvsRoot.substring(0, idx - 1) + cvsRoot.substring(idx + rt.getPassword().length());
                }
            }
        } catch (Exception e) {
            // should not happen but is harmless here
        }
        requestProcessor = new RequestProcessor("CVS: " + cvsRootDisplay);  // NOI18N
    }

    private void ensureValidCommand(File [] files) throws IllegalCommandException {
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                String root = Utils.getCVSRootFor(file);
                if (!root.equals(cvsRoot)) throw new IllegalCommandException("#63547 command includes files from different CVS root.\n Expected: " + cvsRoot + "\nGot:     " + root); // NOI18N
            } catch (IOException e) {
                throw new IllegalCommandException("Missing or invalid CVS/Root for: " + file); // NOI18N
            }
        }
    }

    /**
     * Creates a task that will execute the given command.
     *  
     * @param cmd command to schedule
     * @param globalOptions options to use when running the command
     * @param mgr listener for command events
     * @return RequestProcessor.Task a task ready to execute the command 

     * @throws IllegalCommandException if the command is not valid, e.g. it contains files that cannot be
     * processed by a single command (they do not have a common filesystem root OR their CVS Roots differ) 
     */ 
    public RequestProcessor.Task createTask(Command cmd, GlobalOptions globalOptions, final ExecutorSupport mgr)
            throws IllegalCommandException {
        
        File [] files = getCommandFiles(cmd);
        if ((cmd instanceof CheckoutCommand) == false && !(cmd instanceof RlogCommand)) {    // XXX
            ensureValidCommand(files);
        }

        if (globalOptions.getCVSRoot() == null) {
            globalOptions = (GlobalOptions) globalOptions.clone();
            globalOptions.setCVSRoot(cvsRoot);
        }

        Client client = createClient();
        if ((cmd instanceof RlogCommand)) {    // XXX
        }
        else if ((cmd instanceof CheckoutCommand)) {    // XXX
            BasicCommand bc = (BasicCommand) cmd;
            if (bc.getFiles() != null) {
                String path = bc.getFiles()[0].getAbsolutePath();
                client.setLocalPath(path);
            } else {
                // #67315: use some default working dir
                client.setLocalPath(System.getProperty("user.dir")); // NOI18N
            }
        } else if (cmd instanceof ImportCommand) {
            client.setLocalPath(((ImportCommand)cmd).getImportDirectory());
        } else {
            setLocalDirectory(client, files);
        }

        client.getEventManager().addCVSListener(mgr);
        final CommandRunnable cr = new CommandRunnable(client, globalOptions, cmd, mgr);
        mgr.commandEnqueued(cr);
        RequestProcessor.Task task = requestProcessor.create(cr);
        task.addTaskListener(new TaskListener() {
            public void taskFinished(Task task) {
                try {
                    // There are times when 'commandTerminated()' is not the last method called, therefore I introduced
                    // this event that really marks the very end of a command (thread end)
                    mgr.commandTerminated(new TerminationEvent(new Result(cr)));
                } catch (Throwable e) {
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, e);                    
                } finally {
                    flushLog();
                }
            }
        });
        return task;
    }

    /**
     * Logs given message to associated console. The message
     * is appended at the end.
     */
    public void log(String message) {
        log(message, null);
    }
    
    /**
     * Logs given message to associated console and formats it as a hyperlink. The message
     * is appended at the end.
     */
    public void log(String message, OutputListener hyperlinkListener) {
        openLog();
        if (hyperlinkListener != null) {
            try {
                log.getOut().println(message, hyperlinkListener);
            } catch (IOException e) {
                log.getOut().write(message);
            }
        } else {
            log.getOut().write(message);
        }
    }    

    private void openLog() {
        if (log == null || log.isClosed()) {
            log = IOProvider.getDefault().getIO(cvsRootDisplay, false);
            try {
                // XXX workaround, otherwise it writes to nowhere
                log.getOut().reset();
            } catch (IOException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.notify(e);
            }
            //log.select();
        }
    }

    public void logError(Throwable e) {
        openLog();        
        e.printStackTrace(log.getOut());
    }

    public void logError(String s) {
        openLog();        
        log.getErr().print(s);
    }
    
    /**
     * Makes sure output from this command is visible.
     */ 
    void focusLog() {
        if (log != null) log.select();
    }

    public void flushLog() {
        if (log != null) log.getOut().close();
    }


    private File[] getCommandFiles(Command cmd) {
        if (cmd instanceof AddCommand) {
            AddCommand c = (AddCommand) cmd;
            return c.getFiles();
        } else if (cmd instanceof BasicCommand) {
            BasicCommand c = (BasicCommand) cmd;
            return c.getFiles();
        } else {
            return new File[0];
        }
    }

    private void setLocalDirectory(Client client, File [] files) throws IllegalCommandException {
        if (files.length == 0) {
            return;
        }

        File commonParent;

        if (files[0].isDirectory()) {    // XXX it does not work for checkout
            commonParent = files[0]; 
        } else {
            commonParent = files[0].getParentFile();                                
        }
        
        for (int i = 1; i < files.length; i++) {
            if (!Utils.isParentOrEqual(commonParent, files[i])) {
                for (;;) {
                    commonParent = commonParent.getParentFile();
                    if (commonParent == null) throw new IllegalCommandException("Files do not have common parent!"); // NOI18N
                    if (Utils.isParentOrEqual(commonParent, files[i])) {
                        break;
                    }
                }
            }
        }
        
        // we must not run commands from within folders that are not yet in CVS, try to find the closest uptodate parent
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        for (File versionedCommonParent = commonParent; versionedCommonParent != null; versionedCommonParent = versionedCommonParent.getParentFile()) {
            FileInformation info = cache.getStatus(versionedCommonParent);
            if (info.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE) {
                commonParent = versionedCommonParent;
                break;
            }
        }
        client.setLocalPath(commonParent.getAbsolutePath());
    }

    /**
     * Creates a new Client that will handle CVS operations.
     * 
     * @return a Client instance
     */ 
    private Client createClient() {
        Connection connection = setupConnection(CVSRoot.parse(cvsRoot));
        Client client = new Client(connection, CvsVersioningSystem.getInstance().getAdminHandler());
        client.setUncompressedFileHandler(CvsVersioningSystem.getInstance().getFileHandler());
        client.setGzipFileHandler(CvsVersioningSystem.getInstance().getGzippedFileHandler());
        return client;
    }
    
    /**
     * Sets up connection to a given CVS root including any proxies on route.
     * 
     * @param cvsRoot root to connect to
     * @return Connection object ready to connect to the given CVS root
     * @throws IllegalArgumentException if the 'method' part of the supplied CVS Root is not recognized
     */ 
    public static Connection setupConnection(CVSRoot cvsRoot) throws IllegalArgumentException {

        // for testing porposes allow to use dynamically generated port numbers
        String t9yRoot = System.getProperty("netbeans.t9y.cvs.connection.CVSROOT"); // NOI18N
        CVSRoot patchedCvsRoot = cvsRoot;
        if (t9yRoot != null && t9yRoot.length() > 0) {
            int idx = t9yRoot.indexOf(','); 
            if (idx != -1) {
                System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", t9yRoot.substring(idx + 1)); // NOI18N
                t9yRoot = t9yRoot.substring(0, idx);
            }
            try {
                patchedCvsRoot = CVSRoot.parse(t9yRoot);
                assert patchedCvsRoot.getRepository().equals(cvsRoot.getRepository());
                assert patchedCvsRoot.getHostName() == cvsRoot.getHostName() || patchedCvsRoot.getHostName().equals(cvsRoot.getHostName());
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "CVS.ClientRuntime: using patched CVSROOT " + t9yRoot);  // NOI18N
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().annotate(ex, "While parsing: " + t9yRoot);  // NOI18N
                ErrorManager.getDefault().notify(ex);
            }
        }

        if (cvsRoot.isLocal()) {
            LocalConnection con = new LocalConnection();
            con.setRepository(cvsRoot.getRepository());
            return con;
        }

        ProxySocketFactory factory = ProxySocketFactory.getDefault();

        String method = cvsRoot.getMethod();
        if (CVSRoot.METHOD_PSERVER.equals(method)) {
            PServerConnection con = new PServerConnection(patchedCvsRoot, factory);
            char[] passwordChars = KeyringSupport.read(CvsModuleConfig.PREFIX_KEYRING_KEY, cvsRoot.toString());
            String password;
            if (passwordChars != null) {
                password = new String(passwordChars);
            } else {
                password = PasswordsFile.findPassword(cvsRoot.toString());
                if (password != null) {
                    KeyringSupport.save(CvsModuleConfig.PREFIX_KEYRING_KEY, cvsRoot.toString(), password.toCharArray(), null);
                }
            }
            con.setEncodedPassword(password);
            return con;
        } else if (CVSRoot.METHOD_EXT.equals(method)) {
            CvsModuleConfig.ExtSettings extSettings = CvsModuleConfig.getDefault().getExtSettingsFor(cvsRoot);
            String userName = cvsRoot.getUserName();
            String host = cvsRoot.getHostName();
            if (extSettings.extUseInternalSsh) {
                int port = patchedCvsRoot.getPort();
                port = port == 0 ? 22 : port;  // default port
                String password = new String(extSettings.extPassword);
                if (password == null) {
                    password = "\n";  // NOI18N    user will be asked later on
                }
                SSHConnection sshConnection = new SSHConnection(factory, host, port, userName, password);
                sshConnection.setRepository(cvsRoot.getRepository());
                return sshConnection;
            } else {
                // What do we want to achieve here?
                // It's possible to mimics ordinary cvs or cvsnt behaviour:
                // Ordinary cvs style (CVS_RSH):
                //   command += " $hostname [-l$username] $CVS_SERVER"
                // cvsnt style (CVS_EXT and CVS_RSH):
                //   command += " cvs server"
                // I prefer the cvs style, see issue #62683 for details.

                String command = extSettings.extCommand;
                String cvs_server = System.getenv("CVS_SERVER");
                cvs_server = cvs_server != null? cvs_server + " server": "cvs server";  // NOI18N
                String userOption = ""; // NOI18N
                if ( userName != null ) {
                    userOption = " -l " + userName;  // NOI18N
                }
                command += " " + host + userOption + " " + cvs_server; // NOI18N
                ExtConnection connection = new ExtConnection(command);
                connection.setRepository(cvsRoot.getRepository());
                return connection;
            }
        }
        
        throw new IllegalArgumentException("Unrecognized CVS Root: " + cvsRoot); // NOI18N
    }

    public String toString() {
        return "ClientRuntime queue=" + cvsRootDisplay + " processor=" + requestProcessor;  // NOI18N
    }

    /**
     * Encapsulates result of a finished command. If the command succeeded, the 'error' field is null. If it failed,
     * it contains a throwable, cause of the error.
     */ 
    public static class Result  {
        
        private final CommandRunnable runnable;

        public Result(CommandRunnable runnable) {
            this.runnable = runnable;
        }

        /**
         * Get reason why command has not finished succesfully.
         * For user cancels return an exception too.
         */
        public Throwable getError() {
            return runnable.getFailure();
        }

        /** Has it been stopped by user's cancel? */
        public boolean isAborted() {
            return runnable.isAborted();
        }
    }
}
