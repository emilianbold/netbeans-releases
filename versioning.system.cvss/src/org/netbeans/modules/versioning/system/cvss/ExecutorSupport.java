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

import org.netbeans.modules.versioning.util.CommandReport;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.CommandDuplicator;
import org.netbeans.modules.versioning.system.cvss.ui.wizards.RootWizard;
import org.netbeans.modules.versioning.system.cvss.ui.UIUtils;
import org.openide.ErrorManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.TaskListener;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.*;
import java.util.List;
import java.io.IOException;
import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import org.openide.xml.XMLUtil;

/**
 * Support class for command executors:
 * <ul>
 *   <li>asynchronously executes command using
 *       one thread per repository thread pool
 *   <li>logs server output to console
 *   <li>supports execution retry on I/O or authentification errors
 *   <li>reliably detects command termination
 * </ul>
 *
 * <p>Static method {@link #prepareBasicCommand} splits command
 * operating over files in multiple repositories as necessary.
 *
 * @author Maros Sandor
 */
public abstract class ExecutorSupport implements CVSListener, ExecutorGroup.Groupable  {
    
    /**
     * CVS server messages that start with one of these patterns won't be displayed in Output.
     * Library needs these messages to prune empty directories, hence this workaround. 
     */ 
    private static final String [] ignoredMessagePrefixes = {"cvs server: Updating", "cvs server: New directory"}; // NOI18N
        
    protected final FileStatusCache       cache;

    /**
     * List of {@link org.netbeans.lib.cvsclient.command.FileInfoContainer} objects that were collected during
     * command execution. This list is meant to be processed by subclasses in the
     * {@link #commandFinished(org.netbeans.modules.versioning.system.cvss.ClientRuntime.Result)} method.
     * It is never cleared after command successfuly finishes.  
     */
    protected List                        toRefresh = new ArrayList(10);
    
    protected final CvsVersioningSystem   cvs;
    protected final Command             cmd;
    private final GlobalOptions         options;
    private RequestProcessor.Task       task;
    private List taskListeners = new ArrayList(2);
    private Throwable                   internalError;
    private boolean                     terminated;
    private boolean                     commandFailed;

    private boolean                     finishedExecution;
    private boolean executed;
    private CommandRunnable             commandRunnable;

    private StringBuffer message = new StringBuffer();
    private ClientRuntime clientRuntime;
    private List errorMessages = new ArrayList();
    private List warningMessages = new ArrayList();

    private ExecutorGroup group;

    /** t9y */
    boolean t9yRetryFlag;

    /**
     * Be non-interactive.
     */
    private boolean nonInteractive;

    /**
     * Creates execution environment for given command.
     * @param cvs
     * @param cmd that has undergone {@link #prepareBasicCommand} splitting.
     * @param options
     */
    protected ExecutorSupport(CvsVersioningSystem cvs, Command cmd, GlobalOptions options) {
        this.cvs = cvs;
        this.cmd = cmd;
        this.options = options;
        cache = cvs.getStatusCache();
    }

    protected void setNonInteractive(boolean nonInteractive) {
        this.nonInteractive = nonInteractive;
    }


    /**
     * Async execution.
     * Returns after enqueing into execution queue i.e.
     * after {@link #commandEnqueued} call.
     */
    public void execute() {
        assert executed == false;
        executed = true;
        if (group == null) {
            group = new ExecutorGroup(getDisplayName());
            group.setNonInteractive(nonInteractive);
        }

        setup();
        executeImpl();
    }

    private void executeImpl() {
        try {
            task = cvs.post(cmd, options, this);
        } catch (Throwable e) {
            internalError = e;
            group.fail();

            String msg = NbBundle.getMessage(ExecutorSupport.class, "BK1003", new Date(), getDisplayName());
            if (clientRuntime != null) {    // it is null if command did not start
                clientRuntime.log(msg + "\n"); // NOI18N
                clientRuntime.logError(e);
            }
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            synchronized(this) {
                finishedExecution = true;
                notifyAll();
            }
            cleanup();
        }
    }
    
    /**
     * Called once, just before the command is sent to CVS for execution.
     */ 
    protected void setup() {
    }

    /**
     * Called once, after the command finishes execution.
     */ 
    protected void cleanup() {
    }
    
    /**
     * Default implementation takes first non-null name:
     * <ul>
     *   <li>group display name
     *   <li>command display name
     *   <li>plain command syntax
     * </ul>
     */
    protected String getDisplayName() {
        String commandName;
        if (group != null) {
            commandName = group.getDisplayName();
        } else {
            commandName = cmd.getDisplayName();
            if (commandName == null) {
                commandName = cmd.getCVSCommand();
            }
        }
        return commandName;
    }

    /**
     * Controls command textual messages logging
     * into output window. By default everything is logged.
     */
    protected boolean logCommandOutput() {
        return true;
    }

    public void joinGroup(ExecutorGroup group) {
        assert executed == false;
        this.group = group;
    }

    public ExecutorGroup getGroup() {
        return group;
    }

    /**
     * Return internal errors.
     * @see #isSuccessful
     */
    public Throwable getFailure() {
        return internalError;
    }

    /**
     * Was the execution cancelled by user?
     */
    public boolean isCancelled() {
        return group.isCancelled();
    }

    /**
     * @return true on no internal error, user cancel nor command fail ("server erroe:")
     */
    public boolean isSuccessful() {
        return internalError == null && group.isCancelled() == false && commandFailed == false;
    }

    /** @return task instance actually used (can change on retry) or null. */
    public RequestProcessor.Task getTask() {
        return task;
    }

    public void messageSent(MessageEvent e) {
        if (e.isError()) {
            String msg = e.getMessage();
            if (msg == null) {
                // null is not too descriptive, pass it's source
                RuntimeException rex = new RuntimeException("Received null MessageEvent from:");  // NOI18N
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                rex.printStackTrace(pw);
                pw.close();
                msg = sw.getBuffer().toString();
            }
            errorMessages.add(msg);
        }
        else if (e.getMessage().startsWith("W ")) { // NOI18N
            warningMessages.add(e.getMessage().substring(2));
        }
        if (e.isTagged()) {
            String s = MessageEvent.parseTaggedMessage(message, e.getMessage());
            if (s != null) {
                clientRuntime.log(s + "\n");  // NOI18N
                message.setLength(0);
            }
        } else {            
            // If waiting for lock command execution looks deadlocked, always propagate
            // E cvs server: [09:38:43] waiting for httpd's lock in /shared/data/ccvs/
            boolean locked = e.getMessage().indexOf("waiting for") != -1;  // NOI18N
            locked &= e.getMessage().indexOf("lock in") != -1; // NOI18N
            if (locked || logCommandOutput()) {
                if (e.getMessage().length() > 0) {  // filter out bogus newlines
                    if (shouldBeDisplayed(e.getMessage())) { 
                        clientRuntime.log(e.getMessage() + "\n");  // NOI18N
                    }
                }
            }
        }
    }

    private boolean shouldBeDisplayed(String message) {
        for (int i = 0; i < ignoredMessagePrefixes.length; i++) {
            if (message.startsWith(ignoredMessagePrefixes[i])) return false; 
        }
        return true;
    }

    public void messageSent(BinaryMessageEvent e) {
    }

    public void fileAdded(FileAddedEvent e) {
    }

    public void fileRemoved(FileRemovedEvent e) {
    }

    public void fileUpdated(FileUpdatedEvent e) {
    }

    public void fileToRemove(FileToRemoveEvent e) {
    }

    public void fileInfoGenerated(FileInfoEvent e) {
        assert !terminated;
        FileInfoContainer fic = e.getInfoContainer();
        if (fic.getFile() == null) {
            // this probably indicates a bug in the library but is usually harmless, log it just for reference
            ErrorManager.getDefault().log(ErrorManager.WARNING, org.netbeans.modules.versioning.util.Utils.getStackTrace());
            return;
        }
        if (fic instanceof DefaultFileInfoContainer) {
            DefaultFileInfoContainer dfic = ((DefaultFileInfoContainer) fic);
            dfic.setFile(FileUtil.normalizeFile(dfic.getFile()));
            // filter out duplicate events, see org.netbeans.lib.cvsclient.response.UpdatedResponse.process()
            // ? file.txt, U file.txt and C file.txt can all be fired for a single file in any order 
            for (Iterator i = toRefresh.iterator(); i.hasNext();) {
                FileInfoContainer existing = (FileInfoContainer) i.next();
                if (existing.getFile().equals(fic.getFile())) {
                    String existingType = ((DefaultFileInfoContainer) existing).getType();
                    String newType = dfic.getType();
                    if (importance(newType) <= importance(existingType)) return;
                    i.remove();
                    break;
                }
            }
        }
        toRefresh.add(fic);
    }

    private int importance(String type) {
        return "UC".indexOf(type); // NOI18N
    }

    /**
     * Associates this executor with actualy enqueued runnable
     * (ClientRunnable created by ClientRuntime) performing the command.
     *
     * <p>Adds the runnable into group cancelable chain.
     */
    public void commandEnqueued(CommandRunnable commandRunnable) {
        this.commandRunnable = commandRunnable;
        group.enqueued(cvs.getClientRuntime(cmd, options), this);
        group.addCancellable(commandRunnable);
    }

    /**
     * It (re)runs...
     */
    public void commandStarted(CommandRunnable commandRunnable) {
        clientRuntime = cvs.getClientRuntime(cmd, options);
        group.started(clientRuntime);
    }

    public void commandTerminated(TerminationEvent e) {
        try {
            if (e.getSource() instanceof ClientRuntime.Result) {
                assert !terminated;
                terminated = true;
                ClientRuntime.Result result = (ClientRuntime.Result) e.getSource();
                Throwable error = result.getError();
                if (result.isAborted() || Thread.currentThread().isInterrupted()) {
                    toRefresh.clear();
                    return;
                } else if (error != null) {
                    toRefresh.clear();
                    if (error instanceof CommandException) {
                        // TODO internalError = result.getError();?
                        // TODO group.fail();?
                        internalError = error;
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, error);
                        report(NbBundle.getMessage(ExecutorSupport.class, "MSG_CommandFailed_Title"),
                               NbBundle.getMessage(ExecutorSupport.class, "MSG_CommandFailed_Prompt"),
                               Arrays.asList(new String [] { error.getMessage() }), NotifyDescriptor.ERROR_MESSAGE);
                    }
                    else if (!nonInteractive && retryConnection(error)) {
                        terminated = false;
                        String msg = NbBundle.getMessage(ExecutorSupport.class, "BK1004", new Date(), getDisplayName());
                        clientRuntime = cvs.getClientRuntime(cmd, options);
                        clientRuntime.log(msg + "\n"); // NOI18N
                        executeImpl();
                    } else {
                        if (!nonInteractive) {                        
                            String msg = NbBundle.getMessage(ExecutorSupport.class, "BK1005", new Date(), getDisplayName());
                            clientRuntime.log(msg + "\n");  // NOI18N
                        }
                        internalError = result.getError();
                        group.fail();
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, internalError);
                        // TODO ErrorManager.getDefault().notify(ErrorManager.USER, internalError);?
                    }
                } else {  // error == null
                    commandFinished((ClientRuntime.Result) e.getSource());
                    if (cmd.hasFailed()) {
                        commandFailed = true;
                        group.fail();
                        report(NbBundle.getMessage(ExecutorSupport.class, "MSG_CommandFailed_Title"),
                               NbBundle.getMessage(ExecutorSupport.class, "MSG_CommandFailed_Prompt"), 
                               errorMessages, NotifyDescriptor.ERROR_MESSAGE);
                    }
                    if (warningMessages.size() > 0) {
                        report(NbBundle.getMessage(ExecutorSupport.class, "MSG_CommandWarning_Title"), 
                               NbBundle.getMessage(ExecutorSupport.class, "MSG_CommandWarning_Prompt"), 
                               warningMessages, NotifyDescriptor.WARNING_MESSAGE);
                    }
                }
            }
        } finally {
            if (terminated) {
                cleanup();
                synchronized(this) {
                    finishedExecution = true;
                    notifyAll();
                }

                Iterator it;
                synchronized(taskListeners) {
                    it = new ArrayList(taskListeners).iterator();
                }
                while (it.hasNext()) {
                    try {
                        TaskListener listener = (TaskListener) it.next();
                        listener.taskFinished(task);
                    } catch (RuntimeException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }

                group.finished(clientRuntime, this);
            }
        }
    }

    protected void report(String title, String prompt, List<String> messages, int type) {
        if (nonInteractive) return;
        boolean emptyReport = true;
        for (String message : messages) {
            if (message != null && message.length() > 0) {
                emptyReport = false;
                break;
            }
        }
        if (emptyReport) return;
        CommandReport report = new CommandReport(prompt, messages);
        JButton ok = new JButton(NbBundle.getMessage(ExecutorSupport.class, "MSG_CommandReport_OK"));
        NotifyDescriptor descriptor = new NotifyDescriptor(
                report, 
                title, 
                NotifyDescriptor.DEFAULT_OPTION,
                type,
                new Object [] { ok },
                ok);
        DialogDisplayer.getDefault().notify(descriptor);
    }
    
    /** Retry aware task events source*/
    public void addTaskListener(TaskListener l) {
        synchronized(taskListeners) {
            taskListeners.add(l);
        }
    }

    public void removeTaskListener(TaskListener l) {
        synchronized(taskListeners) {
            taskListeners.remove(l);
        }
    }

    /**
     * I/O exception occured give user chance to fix it.
     * It shows dialog allowing to rewise proxy settings.
     */
    private boolean retryConnection(Throwable cause) {

        Throwable initialCause = cause;
        String cvsRoot = getCvsRoot();
        if (cvsRoot == null) return false;
        
        final CVSRoot root;
        try {
            root = CVSRoot.parse(cvsRoot);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        final RootWizard rootWizard = RootWizard.configureRoot(root.toString());
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0,6,6,6));
        StringBuffer reason = new StringBuffer("<ul>");  // NOI18N
        while (cause != null) {
            try {
                String msg = cause.getLocalizedMessage();
                if (msg == null) {
                    msg = cause.getClass().getName();
                } else {
                    msg = XMLUtil.toElementContent(msg);
                }
                reason.append("<li>" + msg + "</li>"); // NOI18N
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            cause = cause.getCause();
        }
        reason.append("</ul>");  // NOI18N
        String msg = NbBundle.getMessage(ExecutorSupport.class, "BK0001", reason.toString(), cvsRoot);
        JLabel label = new JLabel(msg);
        int ex = Math.max((int) (cvsRoot.length() * 1.1), 50);
        UIUtils.computePreferredSize(label, ex);
        panel.add(label, BorderLayout.NORTH);
        panel.add(rootWizard.getPanel(), BorderLayout.CENTER);

        String okMsg = NbBundle.getMessage(ExecutorSupport.class, "CTL_Password_Action_Ok");
        final JButton ok = new JButton(okMsg);
        ok.setEnabled(rootWizard.isValid());
        ok.getAccessibleContext().setAccessibleDescription(okMsg);
        String cancelMsg = NbBundle.getMessage(ExecutorSupport.class, "CTL_Password_Action_Cancel");
        final JButton cancel = new JButton(cancelMsg);
        cancel.getAccessibleContext().setAccessibleDescription(cancelMsg);
        DialogDescriptor descriptor = new DialogDescriptor(
                panel, 
                NbBundle.getMessage(ExecutorSupport.class, "BK0004", getDisplayName()),
                true, 
                new Object [] { ok, cancel }, 
                ok, 
                DialogDescriptor.BOTTOM_ALIGN, 
                null, 
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                    }
                });
        descriptor.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        descriptor.setClosingOptions(null);
        rootWizard.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                ok.setEnabled(rootWizard.isValid());
            }
        });

        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, initialCause);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExecutorSupport.class, "BK0005"));
        dialog.setVisible(true);

        boolean retry = false;
        if (descriptor.getValue() == ok) {
            rootWizard.commit(false);
            retry = true;
        }
        return retry;
    }

    private String getCvsRoot() {
        if (cmd.getGlobalOptions() != null && cmd.getGlobalOptions().getCVSRoot() != null) return cmd.getGlobalOptions().getCVSRoot(); 
        if (options != null && options.getCVSRoot() != null) return options.getCVSRoot();
        try {
            return cvs.detectCvsRoot(cmd);
        } catch (NotVersionedException e) {
        }
        return null;
    }

    protected abstract void commandFinished(ClientRuntime.Result result);

    public void moduleExpanded(ModuleExpansionEvent e) {
    }

    /**
     * Prepares the command for execution by splitting it into one or more separate commands. 
     * The split is necessary if the original command acts on files that are from different repositories or
     * they lie under different filesystem roots.
     * 
     * @param cmd original command to be executed
     * @return array of commands where each command contains only files that have a common parent and are stored under 
     * the same CVS root
     *
     * @see ExecutorGroup
     */ 
    protected static BasicCommand [] prepareBasicCommand(BasicCommand cmd) throws IOException {
        String format = cmd.getDisplayName();
        File [] files = cmd.getFiles();
        if (files == null || files.length < 2) {
            if (format != null) cmd.setDisplayName(MessageFormat.format(format, new Object [] { files == null ? "" : files[0].getName() })); // NOI18N
            return new BasicCommand [] { cmd };
        }
        File [][] fileSets = splitFiles(files);
        if (fileSets.length == 1) {
            String nfiles = NbBundle.getMessage(ExecutorSupport.class, "MSG_ExecutorSupport_CommandFiles", Integer.toString(fileSets[0].length));
            if (format != null) cmd.setDisplayName(MessageFormat.format(format, new Object [] { nfiles }));
            return new BasicCommand [] { cmd };
        }
        BasicCommand [] commands = new BasicCommand[fileSets.length];
        CommandDuplicator cloner = CommandDuplicator.getDuplicator(cmd);
        for (int i = 0; i < fileSets.length; i++) {
            BasicCommand bc = (BasicCommand) cloner.duplicate();
            bc.setFiles(fileSets[i]);
            commands[i] = bc;
            String nfiles = NbBundle.getMessage(ExecutorSupport.class, "MSG_ExecutorSupport_CommandFiles", Integer.toString(fileSets[i].length));
            if (format != null) commands[i].setDisplayName(MessageFormat.format(format, new Object [] { nfiles }));
        }
        return commands;
    }

    /**
     * Splits input files to groups with common CVS root and common local filesystem parent. Files in each group
     * are guaranteed to belong to the same CVS root and lie under one local directory (it may be '/').
     * 
     * @param files files to examine
     * @return File[][] groups of files
     * @throws IOException if a CVS/Root file is unreadable
     */ 
    protected static File[][] splitFiles(File[] files) throws IOException {
        List ret = new ArrayList();
        File [][] aset = splitByCvsRoot(files);
        for (int i = 0; i < aset.length; i++) {
            File [] fileSet = aset[i];
            File [][] splitSet = splitByCommonParent(fileSet);
            for (int j = 0; j < splitSet.length; j++) {
                ret.add(splitSet[j]);
            }
        }
        return (File[][]) ret.toArray(new File[ret.size()][]);
    }

    // XXX actually masks error in cvsclient library
    // command-line cvs works smoothly over multi-cvsrooted
    // workdirs opening new connections as necessary
    protected static File[][] splitByCvsRoot(File [] files) throws IOException {
        Map fileBuckets = new HashMap();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String root = Utils.getCVSRootFor(file);
            Set bucket = (Set) fileBuckets.get(root);
            if (bucket == null) {
                bucket = new HashSet();
                fileBuckets.put(root, bucket);
            }
            bucket.add(file);
        }
        File [][] sets = new File[fileBuckets.size()][];
        int idx = 0;
        for (Iterator i = fileBuckets.values().iterator(); i.hasNext();) {
            Set bucket = (Set) i.next();
            sets[idx++] = (File[]) bucket.toArray(new File[bucket.size()]);
        }
        return sets;
    }
    
    private static File[][] splitByCommonParent(File[] files) {
        Map fileBuckets = new HashMap();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            File parent;
            if (file.isDirectory()) {
                parent = file; 
            } else {
                parent = file.getParentFile();                                
            }
            
            Set fileset = null;
            File commonParent = null;
            for (Iterator j = fileBuckets.keySet().iterator(); j.hasNext();) {
                File key = (File) j.next();
                commonParent = org.netbeans.modules.versioning.util.Utils.getCommonParent(parent, key);
                if (commonParent != null) {
                    fileset = (Set) fileBuckets.get(key);
                    j.remove();
                    break;
                }
            }

            if (commonParent == null) {
                fileset = new HashSet(1);
                commonParent = parent;
            }
            fileset.add(file);
            fileBuckets.put(commonParent, fileset);
        }
        
        File [][] sets = new File[fileBuckets.size()][];
        int idx = 0;
        for (Iterator i = fileBuckets.values().iterator(); i.hasNext();) {
            Set bucket = (Set) i.next();
            sets[idx++] = (File[]) bucket.toArray(new File[bucket.size()]);
        }
        return sets;
    }

    /**
     * Waits until all executors finish.
     * 
     * @param executors array of executors to check
     * @return true if all executors finished successfuly, false otherwise
     */ 
    public static boolean wait(ExecutorSupport[] executors) {
        boolean success = true;
        for (int i = 0; i < executors.length; i++) {
            ExecutorSupport executor = executors[i];
            synchronized(executor) {
                while (!executor.finishedExecution) {
                    try {
                        executor.wait();
                    } catch (InterruptedException e) {
                        // forward interrupt
                        executor.getGroup().cancel();
                    }
                }
            }
            if (executor.isSuccessful() == false) {
                success = false;
            }
        }
        return success;
    }

    /**
     * Notify progress in terms of transmitted/received bytes.
     */
    public void increaseDataCounter(long bytes) {
        group.increaseDataCounter(bytes);
    }

}
