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
package org.netbeans.modules.refactoring.spi.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.openide.awt.Actions;
import org.openide.awt.JMenuPlus;
import org.openide.awt.Mnemonics;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Martin Matula
 */
public final class RefactoringSubMenuAction extends TextAction implements Presenter.Menu, Presenter.Popup {
    private final boolean showIcons;
    
    public static RefactoringSubMenuAction create(FileObject o) {
        return new RefactoringSubMenuAction(true);
    }
    
    public static JMenu createMenu() {
        RefactoringSubMenuAction action = new RefactoringSubMenuAction(true);
        return (JMenu) action.getMenuPresenter();
    }
    
    /** Creates a new instance of TestMenu */
    RefactoringSubMenuAction(boolean showIcons) {
        super(NbBundle.getMessage(RefactoringSubMenuAction.class, "LBL_Action"));
        this.showIcons = showIcons;
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
    }
    
    public javax.swing.JMenuItem getMenuPresenter() {
        return new SubMenu();
    }
    
    public javax.swing.JMenuItem getPopupPresenter() {
        return getMenuPresenter();
    }
    
    public boolean equals(Object o) {
        return (o instanceof RefactoringSubMenuAction);
    }
    
    public int hashCode() {
        return 1;
    }
    
    private class SubMenu extends JMenuPlus implements LookupListener {
        private ArrayList actions = null;
        private Lookup.Result nodes = null;
        private boolean nodesChanged = true;
        
        public SubMenu() {
            super((String) RefactoringSubMenuAction.this.getValue(Action.NAME));
            if (showIcons)
                setMnemonic(NbBundle.getMessage(RefactoringSubMenuAction.class, "LBL_ActionMnemonic").charAt(0));
        }
        
        /** Gets popup menu. Overrides superclass. Adds lazy menu items creation. */
        public JPopupMenu getPopupMenu() {
            if (actions == null) {
                createMenuItems();
            }
            return super.getPopupMenu();
        }
        
        /** Creates items when actually needed. */
        private void createMenuItems() {
            actions = new ArrayList();
            removeAll();
            FileSystem dfs = Repository.getDefault().getDefaultFileSystem();
            FileObject fo = dfs.findResource("Menu/Refactoring"); // NOI18N
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
                    if (instance instanceof RefactoringGlobalAction) {
                        // if the action is the refactoring action, pass it information
                        // whether it is in editor, popup or main menu
                        actions.add(instance);
                        JMenuItem mi = new JMenuItem();
                        Actions.connect(mi, (Action) instance, true);
                        if (!showIcons)
                            mi.setIcon(null);
                        add(mi);
                    } else if (instance instanceof JSeparator) {
                        add((JSeparator) instance);
                    } else if (instance instanceof Presenter.Popup) {
                        JMenuItem temp = ((Presenter.Popup)instance).getPopupPresenter();
                        if (!showIcons)
                            temp.setIcon(null);
                        add(temp);
                    } else if (instance instanceof Action) {
                        JMenuItem mi = new JMenuItem();
                        Actions.connect(mi, (Action) instance, true);
                        if (!showIcons)
                            mi.setIcon(null);
                        add(mi);
                    }
                }
            }
        }
        
        private void setText(JMenuItem t, Action a) {
            String name = (String) a.getValue(Action.NAME);
            int i = Mnemonics.findMnemonicAmpersand(name);
            
            if (i < 0)
                t.setText(name);
            else
                t.setText(name.substring(0, i) + name.substring(i + 1));
        }
        
        public void resultChanged(org.openide.util.LookupEvent ev) {
            nodesChanged = true;
        }
        
    }
}
