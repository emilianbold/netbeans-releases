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


package org.netbeans.core.windows.actions;


import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;

import org.openide.cookies.SaveCookie;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;


/**
 * Utility class for creating contextual actions for window system
 * and window action handlers.
 *
 * @author  Peter Zavadsky
 */
public abstract class ActionUtils {

    private ActionUtils() {}
    
    public static Action[] createDefaultPopupActions(TopComponent tc) {
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
        int kind = mode != null ? mode.getKind() : Constants.MODE_KIND_EDITOR;
        
        List actions = new ArrayList();
        if(kind == Constants.MODE_KIND_EDITOR) {
            actions.add(new CloseAllDocumentsAction());
            actions.add(null); // Separator
            actions.add(new SaveDocumentAction(tc));
            actions.add(new CloneDocumentAction(tc));
            actions.add(null); // Separator
        }

        actions.add(new CloseWindowAction(tc));
        actions.add(new MaximizeWindowAction(tc));
        return (Action[])actions.toArray(new Action[0]);
    }
    
    
    /** */
    private static class CloseAllDocumentsAction extends AbstractAction {
        public CloseAllDocumentsAction() {
            putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class, "LBL_CloseAllDocumentsAction"));
        }
        
        public void actionPerformed(ActionEvent evt) {
            closeAllDocuments();
        }
    } // End of class CloseAllDocumentsAction.
    
    private static class CloseWindowAction extends AbstractAction {
        private final TopComponent tc;
        public CloseWindowAction(TopComponent tc) {
            this.tc = tc;
            putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class, "LBL_CloseWindowAction"));
        }
        
        public void actionPerformed(ActionEvent evt) {
            closeWindow(tc);
        }
    } // End of class CloseWindowAction.
    
    private static class SaveDocumentAction extends AbstractAction {
        private final TopComponent tc;
        public SaveDocumentAction(TopComponent tc) {
            this.tc = tc;
            putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class, "LBL_SaveDocumentAction"));
            setEnabled(getSaveCookie(tc) != null);
        }
        
        public void actionPerformed(ActionEvent evt) {
            saveDocument(tc);
        }
    } // End of class SaveDocumentAction.
    
    private static class CloneDocumentAction extends AbstractAction {
        private final TopComponent tc;
        public CloneDocumentAction(TopComponent tc) {
            this.tc = tc;
            putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class, "LBL_CloneDocumentAction"));
            setEnabled(tc instanceof TopComponent.Cloneable);
        }
        
        public void actionPerformed(ActionEvent evt) {
            cloneWindow(tc);
        }
    } // End of class CloneDocumentAction.
    
    private static class MaximizeWindowAction extends AbstractAction {
        private final TopComponent tc;
        public MaximizeWindowAction(TopComponent tc) {
            this.tc = tc;
            if(getModeToMaximize(tc) == null) {
                putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class, "LBL_RestoreWindowAction"));
            } else {
                putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class, "LBL_MaximizeWindowAction"));
            }
        }
        
        public void actionPerformed(ActionEvent evt) {
            maximizeWindow(tc);
        }
    } // End of class MaximizeAction.

    
    // Utility methods >>
    static void closeAllDocuments() {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        Set tcs = new HashSet();
        for(Iterator it = wm.getModes().iterator(); it.hasNext(); ) {
            ModeImpl mode = (ModeImpl)it.next();
            if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
                tcs.addAll(mode.getOpenedTopComponents());
            }
        }
        
        for(Iterator it = tcs.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            tc.close();
        }
    }
    
    static void closeWindow(TopComponent tc) {
        tc.close();
    }
    
    private static void saveDocument(TopComponent tc) {
        SaveCookie sc = getSaveCookie(tc);
        if(sc != null) {
            try {
                sc.save();
            } catch(java.io.IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
    }
    
    private static SaveCookie getSaveCookie(TopComponent tc) {
        Lookup lookup = tc.getLookup();
        Object obj = lookup.lookup(SaveCookie.class);
        if(obj instanceof SaveCookie) {
            return (SaveCookie)obj;
        }
        
        return null;
    }

    static void cloneWindow(TopComponent tc) {
        if(tc instanceof TopComponent.Cloneable) {
            TopComponent clone = ((TopComponent.Cloneable)tc).cloneComponent();
            clone.open();
            clone.requestActive();
        }
    }
    
    private static void maximizeWindow(TopComponent tc) {
        WindowManagerImpl.getInstance().setMaximizedMode(getModeToMaximize(tc));
    }
    
    private static ModeImpl getModeToMaximize(TopComponent tc) {
         WindowManagerImpl wm = WindowManagerImpl.getInstance();
         ModeImpl mode = (ModeImpl)wm.findMode(tc);
         ModeImpl maximizedMode = wm.getMaximizedMode();
         
         if(mode == maximizedMode) {
             return null;
         } else {
             return mode;
         }
    }
    // Utility methods <<
}

