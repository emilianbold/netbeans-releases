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

package org.netbeans.modules.javadoc.search;

import javax.swing.JPopupMenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;

/**
 * Action which shows mounted Javadoc filesystems with known indexes as a submenu,
 * so you can choose a Javadoc set.
 *
 * @author Jesse Glick
 */
public final class IndexOverviewAction extends SystemAction implements Presenter.Menu {
    
    private static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.javadoc.search.IndexOverviewAction.IndexMenu"); // NOI18N
 
    public IndexOverviewAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public void actionPerformed(ActionEvent ev) {
        // do nothing -- should never be called
    }
    
    public String getName() {
        return NbBundle.getMessage(IndexOverviewAction.class, "CTL_INDICES_MenuItem");
    }
    
    protected String iconResource() {
        return null;//"org/netbeans/modules/javadoc/resources/JavaDoc.gif"; // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("javadoc.search"); // NOI18N
    }
    
    public JMenuItem getMenuPresenter() {
        return new IndexMenu();
    }
    
    /**
     * Lazy menu which when added to its parent menu, will begin creating the
     * list of filesystems and finding their titles. When the popup for it
     * is created, it will create submenuitems for each available index.
     */
    private final class IndexMenu extends JMenu implements HelpCtx.Provider, DynamicMenuContent {
        
        private int itemHash = 0;
        
        public IndexMenu() {
            Mnemonics.setLocalizedText(this, IndexOverviewAction.this.getName());
            //setIcon(IndexOverviewAction.this.getIcon());
            // model listening is the only lazy menu procedure that works on macosx
            getModel().addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (getModel().isSelected()) {
                        getPopupMenu2();
                    }
                }
            });
        }
        
        public HelpCtx getHelpCtx() {
            return IndexOverviewAction.this.getHelpCtx();
        }
        
        public JComponent[] getMenuPresenters() {
            return new JComponent[] {this};
        }
        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return items;
        }
        
//        public void addNotify() {
//            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
//                err.log("addNotify");
//            }
//            super.addNotify();
//            IndexBuilder.getDefault();
//        }
        
        public void getPopupMenu2() {
            List[] data = IndexBuilder.getDefault().getIndices();
            int newHash = computeDataHash(data);
            if (newHash != itemHash) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log("recreating popup menu (" + itemHash + " -> " + newHash + ")");
                }
                itemHash = newHash;
                // Probably need to recreate the menu.
                removeAll();
                List names = data[0]; // List<String>
                List indices = data[1]; // List<FileObject>
                int size = names.size();
                if (size != indices.size()) throw new IllegalStateException();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        try {
                            add(new IndexMenuItem((String)names.get(i), (FileObject)indices.get(i)));
                        } catch (FileStateInvalidException e) {
                            err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }
                } else {
                    JMenuItem dummy = new JMenuItem(NbBundle.getMessage(IndexOverviewAction.class, "CTL_no_indices_found"));
                    dummy.setEnabled(false);
                    add(dummy);
                }
            }
        }
        
        private int computeDataHash(List[] data) {
            int x = data[0].hashCode();
            Iterator it = data[1].iterator();
            while (it.hasNext()) {
                FileObject fo = (FileObject)it.next();
                // Just using fo.hashCode() does not work because sometimes the FileObject
                // is collected and recreated randomly, and now has a new hash code...
                try {
                    x += fo.getURL().hashCode();
                } catch (FileStateInvalidException e) {
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            return x;
        }
        
    }

    /**
     * Menu item representing one Javadoc index.
     */
    private final class IndexMenuItem extends JMenuItem implements ActionListener, HelpCtx.Provider {
        
        /** cached url */
        private URL u;
        /** a reference to org.openide.filesystems.FileSystem */
        private final Reference fsRef;
        /** path to index file */
        private String foPath;
        
        public IndexMenuItem(String display, FileObject index) throws FileStateInvalidException {
            super(display);
            fsRef = new WeakReference(index.getFileSystem());
            foPath = index.getPath();
            addActionListener(this);
        }
        
        public void actionPerformed(ActionEvent ev) {
            URL loc = getURL();
            HtmlBrowser.URLDisplayer.getDefault().showURL(loc);
        }
        
        private URL getURL() {
            if (u == null) {
                FileSystem fs = (FileSystem) fsRef.get();
                assert fs != null;
                FileObject index = fs.findResource(foPath);
                assert index != null: foPath;
                u = JavadocURLMapper.findURL(index);
            }
            return u;
        }
        
        public HelpCtx getHelpCtx() {
            return IndexOverviewAction.this.getHelpCtx();
        }
        
    }
    
}
