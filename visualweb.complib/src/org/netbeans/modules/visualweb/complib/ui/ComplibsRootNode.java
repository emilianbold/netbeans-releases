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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.complib.ui;

import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.complib.ComplibServiceProvider;
import org.netbeans.modules.visualweb.complib.ExtensionComplib;
import org.netbeans.modules.visualweb.complib.IdeUtil;
import org.netbeans.modules.visualweb.complib.ComplibServiceProvider.RelatedComplibs;
import org.netbeans.modules.visualweb.complib.api.ComplibEvent;
import org.netbeans.modules.visualweb.complib.api.ComplibListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 * ComplibsRootNode displays a list of embedded complibs for a project.
 * 
 * @author Edwin Goei
 */
public class ComplibsRootNode extends AbstractNode {
//    private static final String COMPLIBS_ROOT_ICON_BASE;
//    static {
//        COMPLIBS_ROOT_ICON_BASE = ComplibsRootNode.class.getPackage().getName()
//                .replace('.', '/')
//                + "/images/libraries.png";
//    }
    private static final String IMAGE_PATH_BADGE = "org/netbeans/modules/visualweb/complib/ui/images/libraries_badge.png"; // NOI18N

    private Project project;

    public ComplibsRootNode(Project project) {
        super(new ComplibsChildren(project));
        this.project = project;

        // Nodes API documentation says to set these items like this
//        setIconBaseWithExtension(COMPLIBS_ROOT_ICON_BASE);
        setName("Complibs");
        setDisplayName(NbBundle.getMessage(ComplibsRootNode.class,
                "ComplibsRootNode.displayName"));
        setShortDescription(NbBundle.getMessage(ComplibsRootNode.class,
                "ComplibsRootNode.tooltip"));
        setValue("propertiesHelpID",
                "projrave_ui_elements_project_nav_node_complib_node");
    }

    public boolean canCopy() {
        return false;
    }

    @Override
    public Action[] getActions(boolean context) {
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new AddComplibAction(project));

        String sharedCompLibs = System.getProperty("complib.sharedCompLibs");
        if ("true".equalsIgnoreCase(sharedCompLibs)) {
            actions.add(new SharedComplibAction());
        }
        return actions.toArray(new Action[0]);
    }

    @Override
    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    private static Image computeIcon(boolean opened, int type) {
        Node iconDelegate = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
        Image image = opened ? iconDelegate.getOpenedIcon(type) : iconDelegate.getIcon(type);
        Image badge = ImageUtilities.loadImage(IMAGE_PATH_BADGE, true); // NOI18N
        return ImageUtilities.mergeImages(image, badge, 7, 7);
    }


    private static class ComplibsChildren extends Children.Keys implements
            ComplibListener {
        private static final ComplibServiceProvider csp = ComplibServiceProvider
                .getInstance();

        private Project project;

        ComplibsChildren(Project project) {
            this.project = project;
        }

        protected void addNotify() {
            csp.addComplibListener(this);
            setKeys(getKeys());
        }

        protected void removeNotify() {
            csp.removeComplibListener(this);
            setKeys(Collections.EMPTY_SET);
        }

        protected Node[] createNodes(Object obj) {
            ExtensionComplib complib = (ExtensionComplib) obj;
            Node node = new ComplibNode(complib, project);
            return new Node[] { node };
        }

        private List getKeys() {
            Set<ExtensionComplib> prjComplibs = csp
                    .getComplibsForProject(project);
            return new ArrayList<ExtensionComplib>(prjComplibs);
        }

        public void paletteChanged(ComplibEvent event) {
            setKeys(getKeys());
        }
    }

    private static class ComplibNode extends AbstractNode {
        private static final String COMPLIB_ICON_BASE;
        static {
            COMPLIB_ICON_BASE = ComplibNode.class.getPackage().getName()
                    .replace('.', '/')
                    + "/images/library.png";
        }

        private Project project;

        private ExtensionComplib complib;

        public ComplibNode(ExtensionComplib complib, Project project) {
            super(Children.LEAF);
            this.complib = complib;
            this.project = project;

            // Nodes API documentation says to set these items like this
            setIconBaseWithExtension(COMPLIB_ICON_BASE);
            setName("complib");
            setDisplayName(complib.getVersionedTitle());
        }

        public Action[] getActions(boolean context) {
            return new Action[] { new UpgradeComplibAction(complib, project),
                    new ReplaceComplibAction(complib, project),
                    new RemoveComplibAction(complib, project) };
        }

        public boolean canCopy() {
            return false;
        }

        @Override
        protected Sheet createSheet() {
            Sheet sheet = Sheet.createDefault();
            Sheet.Set set = Sheet.createPropertiesSet();

            Property prop = getProperty("title", complib.getTitle());
            set.put(prop);
            prop = getProperty("namespaceUri", complib.getIdentifier()
                    .getNamespaceUriString());
            set.put(prop);
            prop = getProperty("version", complib.getIdentifier()
                    .getVersionString());
            set.put(prop);

            sheet.put(set);
            return sheet;
        }

        private Property getProperty(String propName, final String value) {
            String msg = NbBundle.getMessage(ComplibNode.class,
                    "ComplibsRootNode." + propName);
            Property prop = new PropertySupport.ReadOnly(propName,
                    String.class, msg, null) {
                public Object getValue() {
                    return value;
                };
            };
            return prop;
        }
    }

    private static class AddComplibAction extends AbstractAction {
        protected static final String NEW_COMPLIB = "NEW_COMPLIB";

        protected static final ComplibServiceProvider csp = ComplibServiceProvider
                .getInstance();

        protected Project project;

        public AddComplibAction(Project project) {
            super(NbBundle.getMessage(AddComplibAction.class,
                    "ComplibsRootNode.addComplibAction"));
            this.project = project;
        }

        public void actionPerformed(ActionEvent e) {
            JButton addBtn = new JButton();
            String addBtnMsg = NbBundle.getMessage(ComplibsRootNode.class,
                    "ComplibsRootNode.addComplibBtn");
            Mnemonics.setLocalizedText(addBtn, addBtnMsg);
            addBtn.getAccessibleContext().setAccessibleDescription(addBtnMsg);
            addBtn.setEnabled(false);
            Object[] options = new Object[] { addBtn,
                    DialogDescriptor.CANCEL_OPTION };
            ComplibChooser panel = new ComplibChooser(addBtn, project);
            String dialogTitle = NbBundle.getMessage(ComplibsRootNode.class,
                    "ComplibsRootNode.addComplibTitle");
            DialogDescriptor desc = new DialogDescriptor(panel, dialogTitle,
                    true, options, options[0], DialogDescriptor.DEFAULT_ALIGN,
                    null, null);

            desc.setHelpCtx(new HelpCtx(
                    "projrave_ui_elements_dialogs_add_complib")); // NOI18N

            Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.setVisible(true);
            if (desc.getValue() == options[0]) {
                ArrayList<ExtensionComplib> newComplibs = panel
                        .getSelectedComplibs();
                for (ExtensionComplib complib : newComplibs) {
                    try {
                        csp.addProjectComplib(project, complib);
                    } catch (Exception ex) {
                        IdeUtil
                                .logError(
                                        "Unable to embedded a complib into project",
                                        ex);
                    }
                }
            }
            dlg.dispose();
        }
    }

    private static class ReplaceComplibAction extends AbstractAction implements
            Presenter.Popup {
        protected static final String NEW_COMPLIB = "NEW_COMPLIB";

        protected static final ComplibServiceProvider csp = ComplibServiceProvider
                .getInstance();

        protected Project project;

        protected ExtensionComplib origComplib;

        public ReplaceComplibAction(ExtensionComplib origComplib,
                Project project) {
            this.project = project;
            this.origComplib = origComplib;
        }

        public JMenuItem getPopupPresenter() {
            String msg = NbBundle.getMessage(ReplaceComplibAction.class,
                    "ComplibsRootNode.replaceComplib");
            JMenu submenu = new JMenu(msg);

            RelatedComplibs relatedComplibs = csp
                    .getRelatedComplibs(origComplib);

            List<ExtensionComplib> olderComplibs = relatedComplibs
                    .getOlderComplibs();
            if (olderComplibs.isEmpty()) {
                msg = NbBundle.getMessage(ReplaceComplibAction.class,
                        "ComplibsRootNode.noOlderComplibs");
                JMenuItem item = new JMenuItem(msg);
                item.setEnabled(false);
                item.setToolTipText(getComplibManagerToolTip());
                submenu.add(item);
            } else {
                for (ExtensionComplib iComplib : olderComplibs) {
                    ReplaceComplibAction action = new ReplaceComplibAction(
                            origComplib, project);
                    String versionedTitle = iComplib.getVersionedTitle();
                    action.putValue(NAME, versionedTitle);
                    action.putValue(NEW_COMPLIB, iComplib);
                    action.putValue(SHORT_DESCRIPTION,
                            getComplibManagerToolTip());
                    JMenuItem item = new JMenuItem(action);
                    submenu.add(item);
                }
            }

            return submenu;
        }

        public void actionPerformed(ActionEvent e) {
            ExtensionComplib newComplib = getNewComplib(e);
            if (newComplib == null) {
                return;
            }

            // Confirmation dialog
            String msg = NbBundle.getMessage(ReplaceComplibAction.class,
                    "ComplibsRootNode.confirmReplaceComplib");
            msg = MessageFormat.format(msg, origComplib.getVersionedTitle(),
                    newComplib.getVersionedTitle());
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg,
                    NotifyDescriptor.OK_CANCEL_OPTION);
            nd.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
            Object result = DialogDisplayer.getDefault().notify(nd);
            if (NotifyDescriptor.OK_OPTION == result) {
                try {
                    csp.replaceProjectComplib(project, origComplib, newComplib);
                } catch (Exception ex) {
                    IdeUtil
                            .logError(
                                    "Unable to replace embedded complib in project",
                                    ex);
                }
            }
        }

        static ExtensionComplib getNewComplib(ActionEvent e) {
            Object source = e.getSource();
            if (!(source instanceof JMenuItem)) {
                return null;
            }
            JMenuItem item = (JMenuItem) source;

            Action action = item.getAction();
            if (!(action instanceof ReplaceComplibAction)) {
                return null;
            }
            ReplaceComplibAction addAction = (ReplaceComplibAction) action;

            ExtensionComplib newComplib = (ExtensionComplib) addAction
                    .getValue(NEW_COMPLIB);
            if (newComplib == null) {
                return null;
            }
            return newComplib;
        }

        static String getComplibManagerToolTip() {
            return NbBundle.getMessage(ReplaceComplibAction.class,
                    "ComplibsRootNode.useComplibManager");
        }
    }

    private static class UpgradeComplibAction extends ReplaceComplibAction {

        public UpgradeComplibAction(ExtensionComplib origComplib,
                Project project) {
            super(origComplib, project);
        }

        public JMenuItem getPopupPresenter() {
            String msg = NbBundle.getMessage(UpgradeComplibAction.class,
                    "ComplibsRootNode.upgradeComplib");
            JMenu submenu = new JMenu(msg);

            RelatedComplibs relatedComplibs = csp
                    .getRelatedComplibs(origComplib);
            List<ExtensionComplib> newerComplibs = relatedComplibs
                    .getNewerComplibs();
            if (newerComplibs.isEmpty()) {
                msg = NbBundle.getMessage(ReplaceComplibAction.class,
                        "ComplibsRootNode.noUpgradableComplibs");
                JMenuItem item = new JMenuItem(msg);
                item.setEnabled(false);
                item.setToolTipText(getComplibManagerToolTip());
                submenu.add(item);
                return submenu;
            }

            for (ExtensionComplib iComplib : newerComplibs) {
                UpgradeComplibAction action = new UpgradeComplibAction(
                        origComplib, project);
                String versionedTitle = iComplib.getVersionedTitle();
                action.putValue(NAME, versionedTitle);
                action.putValue(NEW_COMPLIB, iComplib);
                action.putValue(SHORT_DESCRIPTION, getComplibManagerToolTip());
                JMenuItem item = new JMenuItem(action);
                submenu.add(item);
            }
            return submenu;
        }

        public void actionPerformed(ActionEvent e) {
            ExtensionComplib newComplib = getNewComplib(e);
            if (newComplib == null) {
                return;
            }

            // Confirmation dialog
            String msg = NbBundle.getMessage(UpgradeComplibAction.class,
                    "ComplibsRootNode.confirmUpgradeComplib");
            msg = MessageFormat.format(msg, origComplib.getVersionedTitle(),
                    newComplib.getVersionedTitle());
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg,
                    NotifyDescriptor.OK_CANCEL_OPTION);
            Object result = DialogDisplayer.getDefault().notify(nd);
            if (NotifyDescriptor.OK_OPTION == result) {
                try {
                    csp.replaceProjectComplib(project, origComplib, newComplib);
                } catch (Exception ex) {
                    IdeUtil
                            .logError(
                                    "Unable to upgrade embedded complib in project",
                                    ex);
                }
            }
        }
    }

    private static class RemoveComplibAction extends AbstractAction {
        private ExtensionComplib complib;

        private Project project;

        public RemoveComplibAction(ExtensionComplib complib, Project project) {
            super(NbBundle.getMessage(RemoveComplibAction.class,
                    "ComplibsRootNode.removeComplibAction"));
            this.complib = complib;
            this.project = project;
        }

        public void actionPerformed(ActionEvent e) {
            // Confirmation dialog
            String msg = NbBundle.getMessage(RemoveComplibAction.class,
                    "ComplibsRootNode.confirmRemoveComplib");
            msg = MessageFormat.format(msg, complib.getVersionedTitle());
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg,
                    NotifyDescriptor.OK_CANCEL_OPTION);
            nd.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
            Object result = DialogDisplayer.getDefault().notify(nd);
            if (NotifyDescriptor.OK_OPTION == result) {
                try {
                    ComplibServiceProvider.getInstance()
                            .removeComplibFromProject(project, complib);
                } catch (IOException ex) {
                    IdeUtil.logError(
                            "Unable to remove embedded complib from project",
                            ex);
                }

            }
        }
    }
}
