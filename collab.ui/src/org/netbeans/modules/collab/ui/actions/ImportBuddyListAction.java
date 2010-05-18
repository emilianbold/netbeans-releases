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
package org.netbeans.modules.collab.ui.actions;

import java.io.*;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.NodeAction;

import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.*;

public class ImportBuddyListAction extends NodeAction {
    public static final String defaultContactsExtension = ".iim";
    public static final String CONTACT_FILE_GROUP_SUFFIX = "~~~";
    public static final String DEFAULT_CONTACT_FOLDER = NbBundle.getMessage(
            ImportBuddyListAction.class, "ImportBuddyListAction_DefaultContactList"
        ); // NOI18N    

    protected boolean enable(Node[] nodes) {
        CollabManager manager = CollabManager.getDefault();

        return manager != null;
    }

    public String getName() {
        return NbBundle.getMessage(ImportBuddyListAction.class, "LBL_ImportBuddyListAction_Name");
    }

    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return true;
    }

    protected void performAction(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            CollabSession session = ((SessionNode) nodes[i]).getCollabSession();
            importContacts(session);
        }
    }

    final public void importContacts(CollabSession session) {
        File file = null;

        try {
            file = StickyFileChooser.chooseFile(
                    CollabExplorerPanel.getInstance(), defaultContactsExtension,
                    NbBundle.getMessage(ImportBuddyListAction.class, "ContactList_fileType"),
                    NbBundle.getMessage(ImportBuddyListAction.class, "ImportDialog_title"),
                    NbBundle.getMessage(ImportBuddyListAction.class, "ImportDialog_btnTxt"),
                    NbBundle.getMessage(ImportBuddyListAction.class, "ImportDialog_btnTxt_M")
                );
        } catch (Exception e) {
            Debug.debugNotify(e);
        } finally {
            if (file == null) {
                return;
            }

            if (!file.isFile()) {
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        NbBundle.getMessage(ImportBuddyListAction.class, "Error_while_opening_file"),
                        NotifyDescriptor.INFORMATION_MESSAGE
                    );
                DialogDisplayer.getDefault().notify(d);
            } else {
                try {
                    String uid;
                    String group = DEFAULT_CONTACT_FOLDER;
                    BufferedReader in;

                    in = new BufferedReader(new FileReader(file));

                    while ((uid = in.readLine()) != null) {
                        if (uid.startsWith(CONTACT_FILE_GROUP_SUFFIX)) {
                            group = uid.substring(3);

                            if (session.getContactGroup(group) == null) {
                                session.createContactGroup(group);
                            }
                        } else {
                            //String user = StringUtility.getLocalPartFromAddress(uid);
                            //String server = StringUtility.getDomainFromAddress(uid, null);
                            if (session.getContactGroup(group).getContact(uid) == null) {
                                session.getContactGroup(group).addContact(session.getPrincipal(uid));
                                session.getPrincipal(uid).subscribe();
                            }
                        }
                    }

                    in.close();
                } catch (Exception e) {
                    Debug.errorManager.notify(e);

                    return;
                }
            }
        }

        //	Debug.debugNotify(new Exception("TAF: Complete impl in ImportBuddyListAction"));
    }
}
