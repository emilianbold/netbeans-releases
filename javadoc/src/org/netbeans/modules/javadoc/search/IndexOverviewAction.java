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

package org.netbeans.modules.javadoc.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
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

        private static final int MAX_ITEMS = 20;
        
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
            List[] data = IndexBuilder.getDefault().getIndices(false);
            if (data == null) {
                // do not block EDT in case inices are not computed yet
                itemHash = 0;
                removeAll();
                add(new MoreReferencesMenuItem());
                return;
            }
            
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
                    for (int i = 0; i < size && i < MAX_ITEMS; i++) {
                        add(new IndexMenuItem((String) names.get(i), (FileObject) indices.get(i)));
                    }
                    add(new MoreReferencesMenuItem());
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
                // Do not use fo.getURL() that is really slow.
                // As FileObject instance is referened by IndexMenuItem, there
                // should not be the identity issue any more
                x += fo.hashCode();
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
        private final FileObject fsRef;
        
        public IndexMenuItem(String display, FileObject index) {
            super(display);
            fsRef = index;
            addActionListener(this);
        }
        
        public void actionPerformed(ActionEvent ev) {
            URL loc = getURL();
            HtmlBrowser.URLDisplayer.getDefault().showURL(loc);
        }
        
        private URL getURL() {
            if (u == null) {
                u = JavadocURLMapper.findURL(fsRef);
            }
            return u;
        }
        
        public HelpCtx getHelpCtx() {
            return IndexOverviewAction.this.getHelpCtx();
        }
        
    }

    private static final class MoreReferencesMenuItem extends JMenuItem implements ActionListener {

        public MoreReferencesMenuItem() {
            Mnemonics.setLocalizedText(this, NbBundle.getMessage(IndexOverviewAction.class, "CTL_MORE_INDICES_MenuItem"));
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            FileObject fsRef = ReferencesPanel.showInWindow();
            URL u = null;
            if (fsRef != null) {
                u = JavadocURLMapper.findURL(fsRef);
            }
            if (u != null) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(u);
            }
        }

    }
    
}
