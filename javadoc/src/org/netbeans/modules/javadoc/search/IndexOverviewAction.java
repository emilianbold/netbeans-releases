/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javadoc.search;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.parser.*;

import org.openide.ErrorManager;
import org.openide.TopManager;
import org.openide.awt.Actions;
import org.openide.awt.JInlineMenu;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;

/**
 * @author Jesse Glick
 */
public class IndexOverviewAction extends SystemAction implements Presenter.Menu, Presenter.Popup {
    
    public void actionPerformed(ActionEvent ev) {
        // do nothing -- should never be called
    }
    
    public String getName() {
        return NbBundle.getMessage(IndexOverviewAction.class, "CTL_INDICES_MenuItem");
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/javadoc/resources/JavaDoc.gif"; // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("javadoc.search"); // NOI18N
    }
    
    public JMenuItem getMenuPresenter() {
        return new SpecialSubMenu(this, new ActSubMenuModel(), false);
    }
    
    public JMenuItem getPopupPresenter() {
        return new SpecialSubMenu(this, new ActSubMenuModel(), true);
    }
    
    /** Special submenu which notifies model when it is added as a component.
     */
    private static final class SpecialSubMenu extends Actions.SubMenu {
        
        private final ActSubMenuModel model;

        SpecialSubMenu(SystemAction action, ActSubMenuModel model, boolean popup) {
            super(action, model, popup);
            this.model = model;
        }
        
        public void addNotify() {
            model.addNotify();
            super.addNotify();
            setEnabled(model.getCount() > 0);
        }
        
        // removeNotify not useful--might be called before action is invoked
        
    }
    
    /** Model to use for the submenu.
     */
    private static final class ActSubMenuModel implements Actions.SubMenuModel {
        
        private List displayNames; // List<String>
        // index.html files:
        private List associatedInfo; // List<FileObject>
        
        private Set listeners = new HashSet(); // Set<ChangeListener>
        
        public int getCount() {
            return displayNames != null ? displayNames.size() : 0;
        }
        
        public String getLabel(int index) {
            return (String)displayNames.get(index);
        }
        
        public HelpCtx getHelpCtx(int index) {
            return HelpCtx.DEFAULT_HELP; // could add something special here, or new HelpCtx(IndexOverviewAction.class)
        }
        
        public void performActionAt(int index) {
            FileObject f = (FileObject)associatedInfo.get(index);
            try {
                TopManager.getDefault().showUrl(f.getURL());
            } catch (FileStateInvalidException fsie) {
                ErrorManager.getDefault().notify(fsie);
            }
        }
        
        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        /** You may use this is you have attached other listeners to things that will affect displayNames, for example. */
        private synchronized void fireStateChanged() {
            if (listeners.size() == 0) return;
            ChangeEvent ev = new ChangeEvent(this);
            Iterator it = listeners.iterator();
            while (it.hasNext())
                ((ChangeListener)it.next()).stateChanged(ev);
        }
        
        void addNotify() {
            displayNames = new ArrayList();
            associatedInfo = new ArrayList();
            Enumeration e = FileSystemCapability.DOC.fileSystems();
            String[] names = {
                "overview-summary.html", // NOI18N
                "api/overview-summary.html", // NOI18N
                "index.html", // NOI18N
                "api/index.html", // NOI18N
                "index.htm", // NOI18N
                "api/index.htm", // NOI18N
            };
            while (e.hasMoreElements()) {
                FileSystem fs = (FileSystem)e.nextElement();
                FileObject index = null;
                for (int i = 0; i < names.length; i++) {
                    if ((index = fs.findResource(names[i])) != null) {
                        break;
                    }
                }
                if (index != null) {
                    // Try to find a title.
                    final String[] title = new String[1];
                    try {
                        Reader r = new InputStreamReader(index.getInputStream());
                        try {
                            class TitleParser extends Parser {
                                public TitleParser() throws IOException {
                                    super(DTD.getDTD("html32")); // NOI18N
                                }
                                protected void handleTitle(char[] text) {
                                    title[0] = new String(text);
                                }
                            }
                            new TitleParser().parse(r);
                        } finally {
                            r.close();
                        }
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                    }
                    JavaDocFSSettings fss = JavaDocFSSettings.getSettingForFS(fs);
                    if (title[0] != null && fss != null) {
                        title[0] = fss.getSearchTypeEngine().getOverviewTitleBase(title[0]);
                    }
                    if (displayNames.isEmpty()) {
                        displayNames.add(null);
                        associatedInfo.add(null);
                    }
                    displayNames.add(title[0] != null ? title[0] : fs.getDisplayName());
                    associatedInfo.add(index);
                }
            }
        }
        
    }
    
}
