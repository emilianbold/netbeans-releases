/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.BasicCommand;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.CommandDuplicator;
import org.netbeans.modules.versioning.system.cvss.ui.wizards.RootWizard;
import org.netbeans.modules.versioning.system.cvss.ui.UIUtils;
import org.openide.ErrorManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
 *   <li>splits command operating over files
 *       in multiple repositories as necessary
 *   <li>logs server output to console
 *   <li>supports execution retry on I/O or authentification errors
 *   <li>reliably detects command termination
 * </ul>
 * 
 * @author Maros Sandor
 */
public abstract class ExecutorSupport implements CVSListener  {
    
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
    private Throwable                   failure;
    private boolean                     terminated;

    private boolean                     finishedExecution;
    private boolean executed;

    private StringBuffer message = new StringBuffer();
    private ClientRuntime clientRuntime;
    private List errorMessages = new ArrayList();
    private List warningMessages = new ArrayList();

    private ExecutorGroup group;

    protected ExecutorSupport(CvsVersioningSystem cvs, Command cmd, GlobalOptions options) {
        this.cvs = cvs;
        this.cmd = cmd;
        this.options = options;
        cache = cvs.getStatusCache();
    }



    /** Async execution. */
    public void execute() {
        executed = true;
        String msg = NbBundle.getMessage(ExecutorSupport.class, "BK1001", new Date(), getDisplayName());
        String sep = NbBundle.getMessage(ExecutorSupport.class, "BK1000");
        executeImpl("\n" + sep + "\n" + msg + "\n"); // NOI18N
    }

    private void executeImpl(String header) {
        try {
            clientRuntime = cvs.getClientRuntime(cmd, options);
            if (group == null || group.start()) {
                clientRuntime.log(header);
            }
            task = cvs.post(cmd, options, this);
        } catch (Throwable e) {
            failure = e;
            String msg = NbBundle.getMessage(ExecutorSupport.class, "BK1003", new Date(), getDisplayName());
            if (clientRuntime != null) {
                clientRuntime.log(msg + "\n"); // NOI18N
                clientRuntime.logError(e);
            }
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            synchronized(this) {
                finishedExecution = true;
                notifyAll();
            }
        }
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

    /**
     * Notifies the executor that it is a part of
     * given execution chain.
     *
     * <p> Must be called before {@link #execute}
     */
    public void joinGroup(ExecutorGroup group) {
        assert executed == false;
        this.group = group;
    }

    /**
     * Return internal errors
     *
     * <p>XXX Temporary returns CommandException on cmd.hasFailed. 
     */
    public Throwable getFailure() {
        return failure;
    }

    /** @return task instance actually used (can change on retry) or null. */
    public RequestProcessor.Task getTask() {
        return task;
    }

    public void messageSent(MessageEvent e) {
        if (e.isError()) {
            errorMessages.add(e.getMessage());
        }
        else if (e.getMessage().startsWith("W ")) {
            warningMessages.add(e.getMessage().substring(2));
        }
        if (e.isTagged()) {
            String s = MessageEvent.parseTaggedMessage(message, e.getMessage());
            if (s != null) {
                clientRuntime.log(s + "\n");  // NOI18N
                message.setLength(0);
            }
        } else {
            if (logCommandOutput()) {
                clientRuntime.log(e.getMessage() + "\n");  // NOI18N
            }
        }
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
        toRefresh.add(e.getInfoContainer());
    }

    public void commandTerminated(TerminationEvent e) {
        try {
            if (e.getSource() instanceof ClientRuntime.Result) {
                assert !terminated;
                terminated = true;
                ClientRuntime.Result result = (ClientRuntime.Result) e.getSource();
                Throwable error = result.getError();
                if (error != null) {
                    toRefresh.clear();
                    if (result.isAborted()) {
                        failure = result.getError();
                        return;
                    }
                    if (error instanceof CommandException) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, error);
                        report(NbBundle.getMessage(ExecutorSupport.class, "MSG_CommandFailed_Title"),
                               NbBundle.getMessage(ExecutorSupport.class, "MSG_CommandFailed_Prompt"), 
                               Arrays.asList(new Object [] { error.getMessage() }), NotifyDescriptor.ERROR_MESSAGE);
                    }
                    else if (retryConnection(error)) {
                        terminated = false;
                        String msg = NbBundle.getMessage(ExecutorSupport.class, "BK1004", new Date(), getDisplayName());
                        executeImpl(msg + "\n"); // NOI18N
                    } else {
                        String msg = NbBundle.getMessage(ExecutorSupport.class, "BK1005", new Date(), getDisplayName());
                        clientRuntime.log(msg + "\n");  // NOI18N
                        failure = result.getError();
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, failure);
                    }
                } else {
                    if (group == null || group.finished()) {
                        String msg = NbBundle.getMessage(ExecutorSupport.class, "BK1002", new Date(), getDisplayName());
                        clientRuntime.log(msg + "\n"); // NOI18N
                        clientRuntime.focusLog();
                    }
                    commandFinished((ClientRuntime.Result) e.getSource());
                    if (cmd.hasFailed()) {
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
                synchronized(this) {
                    finishedExecution = true;
                    notifyAll();
                }

                Iterator it;
                synchronized(taskListeners) {
                    it = new ArrayList(taskListeners).iterator();
                }
                while (it.hasNext()) {
                    TaskListener listener = (TaskListener) it.next();
                    listener.taskFinished(task);
                }
            }
        }
    }

    private void report(String title, String prompt, List messages, int type) {
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
        panel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
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
        String cancel = NbBundle.getMessage(ExecutorSupport.class, "CTL_Password_Action_Cancel");
        DialogDescriptor descriptor = new DialogDescriptor(
                panel, 
                NbBundle.getMessage(ExecutorSupport.class, "BK0004"),
                true, 
                new Object [] { ok, cancel }, 
                ok, 
                DialogDescriptor.BOTTOM_ALIGN, 
                null, 
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                    }
                }); 
        descriptor.setClosingOptions(null);
        rootWizard.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                ok.setEnabled(rootWizard.isValid());
            }
        });

        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, initialCause);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
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
     */ 
    protected static BasicCommand [] prepareBasicCommand(BasicCommand cmd) throws IOException {
        String format = cmd.getDisplayName();
        File [] files = cmd.getFiles();
        if (files == null || files.length < 2) {
            if (format != null) cmd.setDisplayName(MessageFormat.format(format, new Object [] { files == null ? "" : files[0].getName() }));
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
                commonParent = getCommonParent(parent, key);
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

    private static File getCommonParent(File a, File b) {
        for (;;) {
            if (a.equals(b)) {
                return a;
            } else if (a.getAbsolutePath().length() > b.getAbsolutePath().length()) {
                a = a.getParentFile();
                if (a == null) return null;
            } else {
                b = b.getParentFile();
                if (b == null) return null;
            }
        }
    }

    /**
     * Displays notification to the user if some executors failed.
     * 
     * @param executors array of executors to check
     */ 
    public static void notifyError(ExecutorSupport[] executors) {
        for (int i = 0; i < executors.length; i++) {
            ExecutorSupport executor = executors[i];
            if (executor.getFailure() != null) {
                ErrorManager.getDefault().notify(executor.getFailure());
            }
        }
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
                        // not interested
                    }
                }
            }
            if (executor.getFailure() != null) {
                success = false;
            }
        }
        return success;
    }
}
