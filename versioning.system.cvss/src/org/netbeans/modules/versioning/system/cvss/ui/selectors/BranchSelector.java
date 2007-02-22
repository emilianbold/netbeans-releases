/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.selectors;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.log.LogCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.openide.util.RequestProcessor;
import org.openide.util.UserCancelException;
import org.openide.util.HelpCtx;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.NodeAcceptor;

import java.util.*;
import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * Allows to select branches for given repository path,
 *
 * @author Petr Kuzel
 */
public final class BranchSelector implements Runnable {

    private CVSRoot root;

    private String module;

    private File file;

    private Node rootNode;
    private BranchNodeChildren rootKids;

    /**
s     * Selects tag or branch for versioned files. Shows modal UI.
     *
     * @param file versioned file or folder
     * @return selected tag or <code>null</code> on cancel.
     */
    public String selectTag(File file) {

        this.file = file;

        return showSelector();
    }

    /**
     * Selects tag or branch for not yet locally checked out files.
     *
     * @param root repository
     * @param module hint where to look for the first cvs_loggable file
     * <ul>
     *   <li><code>null</code> is not allowed
     *   <li><code>"."</code> stays for any modules
     * </ul>
     * @return selected tag or <code>null</code> on cancel.
     */
    public String selectTag(CVSRoot root, String module) {

        this.root = root;
        this.module = module;

        return showSelector();
    }

    private String showSelector() {

        rootKids = new BranchNodeChildren();
        rootNode = new AbstractNode(rootKids);

        // load on background
        RequestProcessor.getDefault().post(this);

        try {
            NodeOperation2 op = new NodeOperation2();
            op.setIconsVisible(false);
            op.setRootVisible(false);
            op.setHelpCtx(new HelpCtx(BranchSelector.class));
            Node[] selected = op.select(org.openide.util.NbBundle.getMessage(BranchSelector.class, "BK2012"),
                                        org.openide.util.NbBundle.getMessage(BranchSelector.class, "BK2013"),
                                        org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSD_BranchSelect"),
                                        rootNode,
                                        org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSN_BranchesTree"),
                                        org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSD_BranchesTree"),
                                        new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    if (nodes.length != 1) return false;
                    return nodes[0].getLookup().lookup(String.class) != null;
                }
            });

            Node node = selected[0];
            String branch = (String) node.getLookup().lookup(String.class);
            return branch;
        } catch (UserCancelException e) {
            return null;
        }
    }

    /** Background runnable*/
    public void run() {

        GlobalOptions gtx = CvsVersioningSystem.createGlobalOptions();
        if (root != null) {
            gtx.setCVSRoot(root.toString());  // XXX why is it needed? Client already knows, who is definitive source of cvs root?
        }
        File checkoutFolder = null;
        try {

            File[] files;
            File localPath;
            if (file == null) {
                // netbeans.org rlog does not work, we need to create some sort of fake log command

                checkoutFolder = Kit.createTmpFolder();
                if (checkoutFolder == null) {
                    error(org.openide.util.NbBundle.getMessage(BranchSelector.class, "BK2015"));
                    return;
                }

                CheckoutCommand checkout = new CheckoutCommand();
                checkout.setRecursive(false);

                // non recursive operation doe snot work with "." module
                // #58208 so here a random one is choosen
                if (".".equals(module)) {  // NOI18N
                    Client client = Kit.createClient(root);
                    List l = ModuleSelector.listRepositoryPath(client, root, "");  // NOI18N
                    Iterator it = l.iterator();
                    int max = l.size();
                    int counter = max;
                    Random random = new Random();
                    while (counter-- > 0) {
                        int rnd = random.nextInt(max);
                        String path = (String) l.get(rnd);
                        if ("CVSROOT".equals(path)) continue;  // NOI18N
                        module = path;
                        break;
                    }
                }
                checkout.setModule(module);
                File[] checkoutFiles = new File[] {checkoutFolder};
                checkout.setFiles(checkoutFiles);

                Client client = Kit.createClient(root);
                client.setLocalPath(checkoutFolder.getAbsolutePath());
                client.executeCommand(checkout, gtx);

                files = checkoutFolder.listFiles();
                localPath = checkoutFolder;

                // seek for the first non-administrative file
                for (int i = 0; i<files.length; i++) {
                    if (files[i].isFile()) continue;
                    if ("CVSROOT".equals(files[i].getName())) continue;  // NOI18N
                    files = files[i].listFiles();
                    break;
                }

            } else {
                if (file.isDirectory()) {
                    files = file.listFiles();
                    localPath = file;
                } else {
                    files = new File[] {file};
                    localPath = file.getParentFile();
                }
            }

            List logFiles = new ArrayList(files.length);
            fillLogFiles(files, logFiles);
            if (logFiles.isEmpty()) {
                tagsLoaded(Collections.EMPTY_SET, Collections.EMPTY_SET);
                return;
            }

            // extract tags using log
            LogCommand log = new LogCommand();
            log.setHeaderOnly(true);
            File[] cmdFiles = (File[]) logFiles.toArray(new File[logFiles.size()]);
            log.setFiles(cmdFiles);
            if (root == null) {
                for (int i = 0; i<cmdFiles.length; i++) {
                    try {
                        root = CVSRoot.parse(Utils.getCVSRootFor(cmdFiles[i]));  // raises exception
                        break;
                    } catch (IOException e) {
                        ErrorManager err = ErrorManager.getDefault();
                        err.annotate(e, "Can not find CVSROOT for " + cmdFiles[i]);  // NOI18N
                        err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
            Client client = Kit.createClient(root);
            
            final Set tags = new TreeSet();
            final Set branches = new TreeSet();
            EventManager mgr = client.getEventManager();
            mgr.addCVSListener(new CVSListener() {

                public void messageSent(MessageEvent e) {
                }
                public void messageSent(BinaryMessageEvent e) {
                }
                public void fileAdded(FileAddedEvent e) {
                }
                public void fileToRemove(FileToRemoveEvent e) {
                }
                public void fileRemoved(FileRemovedEvent e) {
                }
                public void fileUpdated(FileUpdatedEvent e) {
                }
                public void fileInfoGenerated(FileInfoEvent e) {
                    LogInformation info = (LogInformation) e.getInfoContainer();
                    List symNames = info.getAllSymbolicNames();
                    Iterator it = symNames.iterator();
                    while (it.hasNext()) {
                        LogInformation.SymName name = (LogInformation.SymName) it.next();
                        if (name.isBranch()) {
                            branches.add(name.getName());
                        } else {
                            tags.add(name.getName());
                        }
                    }
                }
                public void commandTerminated(TerminationEvent e) {
                }
                public void moduleExpanded(ModuleExpansionEvent e) {
                }
            });
            
            client.setLocalPath(localPath.getAbsolutePath());
            client.executeCommand(log, gtx);
            tagsLoaded(branches, tags);
            Kit.deleteRecursively(checkoutFolder);
        } catch (CommandException e) {
            error(org.openide.util.NbBundle.getMessage(BranchSelector.class, "BK2016"));
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, org.openide.util.NbBundle.getMessage(BranchSelector.class, "BK2016"));
            err.notify(e);
        } catch (AuthenticationException e) {
            error(org.openide.util.NbBundle.getMessage(BranchSelector.class, "BK2016"));
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, org.openide.util.NbBundle.getMessage(BranchSelector.class, "BK2016"));
            err.notify(e);
        } finally {
            Kit.deleteRecursively(checkoutFolder);
        }
    }

    /**
     * Try to recursively locate at least one versioned file.
     */
    private void fillLogFiles(File[] files, List logFiles) {

        if (files == null) {
            return;
        }

        if (logFiles.isEmpty() == false) {
            return;
        }

        for (int i = 0; i<files.length; i++) {
            if (files[i].isFile()) {
                FileInformation info = CvsVersioningSystem.getInstance().getStatusCache().getStatus(files[i]);
                if ((info.getStatus() & FileInformation.STATUS_IN_REPOSITORY) != 0) {
                    logFiles.add(files[i]);
                }
            }
        }

        for (int i = 0; i<files.length; i++) {
            if (files[i].isDirectory()) {
                fillLogFiles(files[i].listFiles(), logFiles);  // RESURSION
            }
        }

    }

    /**
     * @param tags contains ModuleListInformation
     */
    private void tagsLoaded(Collection branches, Collection tags) {
        rootKids.setBranches(branches);
        rootKids.setTags(tags);
    }

    private void error(String msg) {
        rootNode.setDisplayName(msg);
    }

}
