/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.encoder.ui.basic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;

/**
 * The encoding action that is displayed in the popup menu of an XSD document.
 *
 * @author Jun Xu
 */
public class EncodingAction extends NodeAction {
    private static final long serialVersionUID = 1L;
    private static final ResourceBundle _bundle = ResourceBundle.getBundle(
            "org/netbeans/modules/encoder/ui/basic/Bundle"); //NOI18N
    
    protected void performAction(Node[] node) {
        assert false : _bundle.getString(
                "encoding_action.exp.should_never_be_called"); //NOI18N
    }

    protected boolean enable(Node[] node) {
        return true;
    }

    public String getName() {
        return _bundle.getString("encoding_action.lbl.action_name"); //NOI18N
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return new LazyMenu(getName());
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    /** List of system actions to be displayed within this one's toolbar or submenu. */
    private static final SystemAction[] grouped() {
        try {
            FileObject fo = FileUtil.getConfigRoot().getFileSystem().findResource(
                    "Loaders/text/x-schema+xml/Actions/encoding"); //NOI18N
            DataFolder df = DataFolder.findFolder(fo);
            DataObject[] dataObjs = df.getChildren();
            if (dataObjs == null || dataObjs.length == 0) {
                return new SystemAction[0];
            }
            List<SystemAction> actionList = new ArrayList<SystemAction>();
            for (int i = 0; i < dataObjs.length; i++) {
                if (dataObjs[i] instanceof InstanceDataObject) {
                    InstanceDataObject dataObj = (InstanceDataObject) dataObjs[i];
                    try {
                        if (dataObj.instanceOf(SystemAction.class)) {
                            actionList.add((SystemAction)dataObj.instanceCreate());
                        } else if (dataObj.instanceOf(JSeparator.class)) {
                            actionList.add(null);
                        }
                    } catch (ClassNotFoundException ex) {
                        ErrorManager.getDefault().notify(ex);
                        return new SystemAction[0];
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                        return new SystemAction[0];
                    }
                }
            }
            return actionList.toArray(new SystemAction[0]);
        } catch (FileStateInvalidException e) {
            return new SystemAction[0];
        }
    }

    /**
     * Avoids constructing submenu until it will be needed.
     */
    protected class LazyMenu extends JMenu {
        private final static long serialVersionUID = 1L;

        public LazyMenu(String name) {
            super(name);
        }

        @Override
        public JPopupMenu getPopupMenu() {
            //TODO:
            //Hack: always remove all items so sub menus can be recomputed.
            removeAll();
            if (getItemCount() == 0) {
                SystemAction[] grouped = grouped();
                for (int i = 0; i < grouped.length; i++) {
                    SystemAction action = grouped[i];
                    if (action == null) {
                        addSeparator();
                    } else if (action instanceof Presenter.Popup) {
                        //TODO
                        //Hack: always force the enabled to be refreshed no
                        //matter the model has been changed or not.  The hack was
                        //created because so far don't know in what condition
                        //the resultchanged() event is fired.
                        action.setEnabled(true);
                        add(((Presenter.Popup)action).getPopupPresenter());
                    } else {
                        assert false : _bundle.getString(
                                "encoding_action.exp.had_no_popup") + action; //NOI18N
                    }
                }
            }
            return super.getPopupMenu();
        }
    }
}
