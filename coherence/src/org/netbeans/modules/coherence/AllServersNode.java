/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence;

import java.awt.Dialog;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.actions.NewAction;
import org.openide.actions.OpenLocalExplorerAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class AllServersNode extends AbstractNode {

    private static ResourceBundle bundle = NbBundle.getBundle(AllServersNode.class);
    private static Logger logger = Logger.getLogger(AllServersNode.class.getCanonicalName());

    public AllServersNode() {
        super(new AllServersChildren());
        setIconBaseWithExtension(bundle.getString("ICON_AllServersNode"));
        setName("AllServersNode");
        setDisplayName(bundle.getString("LBL_AllServersNode"));
        setShortDescription(bundle.getString("HINT_AllServersNode"));
    }

    public Action[] getActions(boolean context) {
        Action[] result = new Action[]{
            SystemAction.get(RefreshAllServersAction.class),
            null,
            SystemAction.get(OpenLocalExplorerAction.class),
            null,
            SystemAction.get(NewAction.class),
            null,
            SystemAction.get(ToolsAction.class),
            SystemAction.get(PropertiesAction.class),};
        return result;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass().getPackage().toString());
    }

    public Node cloneNode() {
        return new AllServersNode();
    }

    @Override
    public NewType[] getNewTypes() {
        return new NewType[]{
                    new NewType() {

                        public String getName() {
                            return bundle.getString("LBL_NewServer");
                        }

                        public HelpCtx getHelpCtx() {
                            return new HelpCtx(this.getClass().getPackage().toString());
                        }

                        @Override
                        public void create() throws IOException {
                            String title = bundle.getString("LBL_NewServerDialogTitle");
                            String msg = bundle.getString("MSG_NewServerDialogMsg");

                            NewServerPanel nsp = new NewServerPanel();
                            Dialog d = DialogDisplayer.getDefault().createDialog(new DialogDescriptor(nsp, title, true, nsp.getButtonActionListener()));
                            d.pack();
                            d.setVisible(true);
                            d = null;
                            AllServersNotifier.changed();
                        }
                    }
                };
    }
}
