/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javadoc.search;

import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;

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
                HtmlBrowser.URLDisplayer.getDefault().showURL(f.getURL());
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
            IndexBuilder index = IndexBuilder.getDefault();
            List[] overviews = index.getIndices();
            if (overviews[0].isEmpty()) {
                displayNames = Collections.EMPTY_LIST;
                associatedInfo = Collections.EMPTY_LIST;
            } else {
                overviews[0].add(0, null);
                overviews[1].add(0, null);
                displayNames = overviews[0];
                associatedInfo = overviews[1];
            }
        }
    }
}
