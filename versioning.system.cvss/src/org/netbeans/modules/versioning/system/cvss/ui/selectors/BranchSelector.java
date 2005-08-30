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

package org.netbeans.modules.versioning.system.cvss.ui.selectors;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.log.LogCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.modules.versioning.system.cvss.ClientRuntime;
import org.openide.util.RequestProcessor;
import org.openide.util.UserCancelException;
import org.openide.util.HelpCtx;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.NodeOperation;
import org.openide.nodes.NodeAcceptor;

import javax.swing.*;
import java.awt.*;
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

    private JList list;

    private CVSRoot root;

    private DialogDescriptor descriptor;

    private String module;
    
    private ProxyDescriptor proxyDescriptor;

    private Node rootNode;
    private BranchNodeChildren rootKids;

    /**
     * Selects tag or branch. Shows modal UI.
     *
     * @param root repository
     * @param module hint where to look for the first cvs_loggable file
     * <ul>
     *   <li><code>null</code> is not allowed
     *   <li><code>"."</code> stays for any modules
     * </ul>
     * @return selected tag or <code>null</code> on cancel.
     */
    public String selectTag(CVSRoot root, String module, ProxyDescriptor proxy) {

        this.root = root;
        this.module = module;
        this.proxyDescriptor = proxy;
        rootKids = new BranchNodeChildren();
        rootNode = new AbstractNode(rootKids);

        // load on background
        RequestProcessor.getDefault().post(this);

        try {
            NodeOperation2 op = new NodeOperation2();
            op.setIconsVisible(false);
            op.setRootVisible(false);
            op.setHelpCtx(new HelpCtx(BranchSelector.class));
            Node[] selected = op.select(org.openide.util.NbBundle.getMessage(BranchSelector.class, "BK2012"), org.openide.util.NbBundle.getMessage(BranchSelector.class, "BK2013"), rootNode, new NodeAcceptor() {
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

        // netbeans.org rlog does not work, we need to create some sort of fake log command

        File checkoutFolder = Kit.createTmpFolder();
        if (checkoutFolder == null) {
            error(org.openide.util.NbBundle.getMessage(BranchSelector.class, "BK2015"));
            return;
        }

        GlobalOptions gtx = new GlobalOptions();
        gtx.setCVSRoot(root.toString());  // XXX why is it needed? Client already knows, who is definitive source of cvs root?
        try {
            CheckoutCommand checkout = new CheckoutCommand();
            checkout.setRecursive(false);

            // non recursive operation doe snot work with "." module
            // #58208 so here a random one is choosen
            if (".".equals(module)) {  // NOI18N
                Client client = Kit.createClient(root, proxyDescriptor);
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
            File[] files = new File[] {checkoutFolder};
            checkout.setFiles(files);

            Client client = Kit.createClient(root, proxyDescriptor);
            client.setLocalPath(checkoutFolder.getAbsolutePath());
            client.executeCommand(checkout, gtx);

            // extract tags using log
            LogCommand log = new LogCommand();
            log.setHeaderOnly(true);

            // seek for the first non-administrative file
            files = checkoutFolder.listFiles();
            for (int i = 0; i<files.length; i++) {
                if (files[i].isFile()) continue;
                if ("CVSROOT".equals(files[i].getName())) continue;  // NOI18N
                files = files[i].listFiles();
                break;
            }
            for (int i = 0; i<files.length; i++) {
                if (files[i].isDirectory()) continue;
                if (".cvsignore".equals(files[i].getName())) continue;  // NOI18N
                File[] logFiles = new File[] {files[i]};
                log.setFiles(logFiles);
                break;
            }
            client = Kit.createClient(root, proxyDescriptor);
            
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
            
            client.setLocalPath(checkoutFolder.getAbsolutePath());
            client.executeCommand(log, gtx);
            tagsLoaded(branches, tags);
            Kit.deleteRecursively(checkoutFolder);
        } catch (CommandException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, org.openide.util.NbBundle.getMessage(BranchSelector.class, "BK2016"));
            err.notify(e);
        } catch (AuthenticationException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, org.openide.util.NbBundle.getMessage(BranchSelector.class, "BK2016"));
            err.notify(e);
        } finally {
            Kit.deleteRecursively(checkoutFolder);
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
