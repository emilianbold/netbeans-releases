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
package org.netbeans.modules.editor;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;


/**
 *
 * @author Dusan Balek
 */
public final class NbSelectInPopupAction extends SystemAction implements Presenter.Popup {

    private ArrayList actions = null;

    public String getName() {
        return NbBundle.getMessage(NbSelectInPopupAction.class, "Editors/text/base/Popup/org-netbeans-modules-editor-NbSelectInPopupAction.instance"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent ev) {
        // do nothing - should never be called
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public javax.swing.JMenuItem getPopupPresenter() {
        return new SubMenu(getName());
    }

    public class SubMenu extends JMenu {

        public SubMenu(String s){
            super(s);
        }
        
        /** Gets popup menu. Overrides superclass. Adds lazy menu items creation. */
        public JPopupMenu getPopupMenu() {
            JPopupMenu pm = super.getPopupMenu();
            pm.removeAll();
            FileSystem dfs = Repository.getDefault().getDefaultFileSystem();
            FileObject fo = dfs.findResource("Menu/Window/SelectDocumentNode"); // NOI18N
            DataFolder df = DataFolder.findFolder(fo);
                
            if (df != null) {
                DataObject actionObjects[] = df.getChildren();
                for (int i = 0; i < actionObjects.length; i++) {
                    InstanceCookie ic = (InstanceCookie) actionObjects[i].getCookie(InstanceCookie.class);
                    if (ic == null) continue;
                    Object instance;
                    try {
                        instance = ic.instanceCreate();
                    } catch (IOException e) {
                        // ignore
                        e.printStackTrace();
                        continue;
                    } catch (ClassNotFoundException e) {
                        // ignore
                        e.printStackTrace();
                        continue;
                    }
                    if (instance instanceof JSeparator) {
                        pm.add((JSeparator) instance);
                    } else if (instance instanceof Presenter.Popup) {    
                        pm.add(((Presenter.Popup)instance).getPopupPresenter());
                    } else if (instance instanceof Presenter.Menu) {    
                        JMenuItem temp = ((Presenter.Menu)instance).getMenuPresenter();
                        temp.setIcon(null);
                        pm.add(temp);
                    } else if (instance instanceof Action) {                        
                        JMenuItem temp = new JMenuItem((Action)instance);
                        temp.setIcon(null);
                        pm.add(temp);
                    }
                }
            }
            pm.pack();
            return pm;
        }
    }
}
