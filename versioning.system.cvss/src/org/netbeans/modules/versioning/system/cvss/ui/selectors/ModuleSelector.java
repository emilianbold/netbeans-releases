/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.versioning.system.cvss.ui.selectors;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.checkout.ModuleListInformation;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.openide.nodes.*;
import org.openide.util.UserCancelException;
import org.openide.util.HelpCtx;
import org.openide.filesystems.FileUtil;

import java.util.*;
import java.util.List;
import java.io.File;

/**
 * Prototype impl of defined modules listing.
 *
 * @author Petr Kuzel
 */
public final class ModuleSelector {

    /**
     * Asks user to select which module to checkout. Popups a modal UI,
     * @param root identifies repository
     * @return Set of String, possibly empty
     */
    public Set selectModules(CVSRoot root) {

        // create top level node that categorizes to aliases and raw browser

        Children.Array kids = new Children.Array();
        Client.Factory clientFactory = Kit.createClientFactory(root);
        Node aliasesNode = AliasesNode.create(clientFactory, root);
        Node pathsNode = RepositoryPathNode.create(clientFactory, root, "");  // NOI18N
        kids.add(new Node[] {aliasesNode, pathsNode});
        Node rootNode = new AbstractNode(kids);

        try {
            NodeOperation2 op = new NodeOperation2();
            op.setRootVisible(false);
            op.setHelpCtx(new HelpCtx(ModuleSelector.class));
            Node[] selected = op.select(org.openide.util.NbBundle.getMessage(ModuleSelector.class, "BK2019"), 
                                        org.openide.util.NbBundle.getMessage(ModuleSelector.class, "BK2020"), 
                                        org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSD_ModuleSelect"), 
                                        rootNode, 
                                        org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSN_ModulesTree"), 
                                        org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSD_ModulesTree"), 
                                        new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    boolean ret = nodes.length > 0;
                    for (int i = 0; i < nodes.length; i++) {
                        Node node = nodes[i];
                        String path = (String) node.getLookup().lookup(String.class);
                        ret &= path != null;
                    }
                    return ret;
                }
            });

            Set<String>  modules = new LinkedHashSet<String>();
            for (int i = 0; i < selected.length; i++) {
                Node node = selected[i];
                String path = (String) node.getLookup().lookup(String.class);
                modules.add(path);
            }
            return modules;
        } catch (UserCancelException e) {
            return Collections.EMPTY_SET;
        }
    }

    /*
     * Pupup modal UI and let user select repositpry path.
     *
     * @param root identifies repository
     * @param proxy defines which proxy to use or null
     *        to use one from CVsRootSettings.
     * @return '/' separated path or null on cancel.
     */
    public String selectRepositoryPath(CVSRoot root) {

        Client.Factory clientFactory = Kit.createClientFactory(root);
        Node pathsNode = RepositoryPathNode.create(clientFactory, root, "");  // NOI18N

        try {
            Node[] selected = NodeOperation.getDefault().select(org.openide.util.NbBundle.getMessage(ModuleSelector.class, "BK2021"), org.openide.util.NbBundle.getMessage(ModuleSelector.class, "BK2022"), pathsNode, new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    if (nodes.length == 1) {
                        String path = (String) nodes[0].getLookup().lookup(String.class);
                        return path != null;
                    }
                    return false;
                }
            });

            String path = null;
            if (selected.length == 1) {
                path = (String) selected[0].getLookup().lookup(String.class);
            }
            return path;
        } catch (UserCancelException e) {
            return null;
        }
    }

    private static final String MAGIC_START = ": New directory `"; // NOI18N
    private static final String MAGIC_END = "' -- ignored"; // NOI18N
    
    /**
     * Lists subfolders in given repository folder.
     *
     * @param client engine to be used
     * @param root identifies repository
     * @param path "/" separated repository folder path (e.g. "javacvs/cvsmodule")
     * @return folders never <code>null</code>
     */
    public static List<String> listRepositoryPath(Client client, CVSRoot root, String path) throws CommandException, AuthenticationException {

        final List<String> list = new ArrayList<String>();
        GlobalOptions gtx = CvsVersioningSystem.createGlobalOptions();
        gtx.setCVSRoot(root.toString());
        gtx.setDoNoChanges(true);

        UpdateCommand blindUpdate = new UpdateCommand();
        blindUpdate.setBuildDirectories(true);

        AdminHandler localEnv = new VirtualAdminHandler(root, path); // NOI18N
        client.setAdminHandler(localEnv);
        String tmpDir = System.getProperty("java.io.tmpdir");  // NOI18N
        File tmp = new File(tmpDir);
        tmp = FileUtil.normalizeFile(tmp);
        client.setLocalPath(tmp.getAbsolutePath());
        EventManager mgr = client.getEventManager();
        mgr.addCVSListener(new CVSListener() {
            public void messageSent(MessageEvent e) {
                if (e.isError()) {
                    String message = e.getMessage();
                    if (message.endsWith(MAGIC_END)) {  // NOI18N
                        int start = message.indexOf(MAGIC_START);
                        if (start != -1) {
                            int pathStart = start + MAGIC_START.length();
                            int pathEnd = message.length() - MAGIC_END.length();
                            String path = message.substring(pathStart, pathEnd);
                            list.add(path);
                        }
                    }
                }
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
            }
            public void commandTerminated(TerminationEvent e) {
            }
            public void moduleExpanded(ModuleExpansionEvent e) {
            }
        });
        client.executeCommand(blindUpdate, gtx);

        return list;
    }

    /**
     * Lists defined aliases in given repository.
     *
     * @return list of ModuleListInformation
     */
    public static List<ModuleListInformation> listAliases(Client client, CVSRoot root) throws CommandException, AuthenticationException {

        CheckoutCommand checkout = new CheckoutCommand();
        checkout.setShowModules(true);
        final List<ModuleListInformation> modules = new LinkedList<ModuleListInformation>();
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
                ModuleListInformation moduleList = (ModuleListInformation) e.getInfoContainer();
                modules.add(moduleList);
            }
            public void commandTerminated(TerminationEvent e) {
            }
            public void moduleExpanded(ModuleExpansionEvent e) {
            }
        });

        GlobalOptions gtx = CvsVersioningSystem.createGlobalOptions();
        gtx.setCVSRoot(root.toString());  // XXX why is it needed? Client already knows, who is definitive source of cvs root?
        client.executeCommand(checkout, gtx);

        return modules;
    }
}
