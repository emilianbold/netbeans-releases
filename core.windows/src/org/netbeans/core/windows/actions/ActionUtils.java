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


package org.netbeans.core.windows.actions;


import java.awt.Container;
import java.awt.event.*;
import java.beans.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.view.ui.slides.SlideController;
import org.openide.actions.SaveAction;
import org.openide.cookies.SaveCookie;
import org.openide.util.*;
import org.openide.util.actions.Presenter;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;


/**
 * Utility class for creating contextual actions for window system
 * and window action handlers.
 *
 * @author  Peter Zavadsky
 */
public abstract class ActionUtils {
    
    private static HashMap<Object, Object> sharedAccelerators = new HashMap<Object, Object>();

    private ActionUtils() {}
    
    public static Action[] createDefaultPopupActions(TopComponent tc) {
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
        int kind = mode != null ? mode.getKind() : Constants.MODE_KIND_EDITOR;
        
        List<Action> actions = new ArrayList<Action>();
        if(kind == Constants.MODE_KIND_EDITOR) {
            actions.add(new CloseAllDocumentsAction(false));
            CloseAllButThisAction allBut = new CloseAllButThisAction(tc);
            if (mode != null && mode.getOpenedTopComponents().size() == 1) {
                allBut.setEnabled(false);
            }
            actions.add(allBut);
            actions.add(null); // Separator
            actions.add(new SaveDocumentAction(tc));
            actions.add(new CloneDocumentAction(tc));
            actions.add(null); // Separator
            actions.add(new CloseWindowAction(tc));
            actions.add(new MaximizeWindowAction(tc));
            actions.add(new UndockWindowAction(tc));
        } else if (kind == Constants.MODE_KIND_VIEW) {
            actions.add(new CloseWindowAction(tc));
            // #82053: don't include maximize action for floating (separate) views
            if (mode.getState() == Constants.MODE_STATE_JOINED) {
                actions.add(new MaximizeWindowAction(tc));
            }
            actions.add(new UndockWindowAction(tc));
        } else if (kind == Constants.MODE_KIND_SLIDING) {
            actions.add(new CloseWindowAction(tc));
            if (mode.getState() == Constants.MODE_STATE_JOINED) {
                actions.add(new MaximizeWindowAction(tc));
            }
            actions.add(new UndockWindowAction(tc));
        }
        
        return actions.toArray(new Action[actions.size()]);
    }
    
    /**** PENDING remove during merge, TabbedListener removed, instead drive directly */
    private static Container slidingContext;
    
    public static void setSlidingContext (Container slidingContext) {
        ActionUtils.slidingContext = slidingContext;
    }
    /******** end of PENDING **********/
    
    /** Auto-hide toggle action */
    public static final class AutoHideWindowAction extends AbstractAction implements Presenter.Popup {
        
        private final SlideController slideController;
        
        private final int tabIndex;
        
        private boolean state;
        
        private JCheckBoxMenuItem menuItem;
        
        public AutoHideWindowAction(SlideController slideController, int tabIndex, boolean initialState) {
            super();
            this.slideController = slideController;
            this.tabIndex = tabIndex;
            this.state = initialState;
            putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class, "LBL_AutoHideWindowAction"));
        }
        
        public HelpCtx getHelpCtx() {
            return null;
        }
        
        /** Chnage boolean state and delegate event to winsys through
         * SlideController (implemented by SlideBar component)
         */
        public void actionPerformed(ActionEvent e) {
            // update state and menu item
            state = !state;
            getMenuItem().setSelected(state);
            // send event to winsys
            slideController.userToggledAutoHide(tabIndex, state);
        }

        public JMenuItem getPopupPresenter() {
            return getMenuItem();
        }
        
        private JCheckBoxMenuItem getMenuItem() {
            if (menuItem == null) {
                menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME), state);
                //#45940 - hardwiring the shortcut UI since the actual shortcut processignb is also
                // hardwired in AbstractTabViewDisplayerUI class.
                // later this should be probably made customizable?
                // -> how to get rid of the parameters passed to the action here then?
                menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_DOWN_MASK));
                menuItem.addActionListener(this);
            }
            return menuItem;
        }
        
    } // End of class AutoHideWindowAction

    private static class SaveDocumentAction extends AbstractAction implements PropertyChangeListener {
        private final TopComponent tc;
        private Action saveAction;
        
        public SaveDocumentAction(TopComponent tc) {
            this.tc = tc;
            putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class, "LBL_SaveDocumentAction"));
            // share key accelerator with org.openide.actions.SaveAction
            saveAction = (Action)SaveAction.get(SaveAction.class);
            putValue(Action.ACCELERATOR_KEY, saveAction.getValue(Action.ACCELERATOR_KEY));
            // fix of 40954 - weak listener instead of hard one
            PropertyChangeListener weakL = WeakListeners.propertyChange(this, saveAction);
            saveAction.addPropertyChangeListener(weakL);
            setEnabled(getSaveCookie(tc) != null);
        }
        
        public void actionPerformed(ActionEvent evt) {
            saveDocument(tc);
        }

        /** Keep accelerator key in sync with org.openide.actions.SaveAction */
        public void propertyChange(PropertyChangeEvent evt) {
            if (Action.ACCELERATOR_KEY.equals(evt.getPropertyName())) {
                putValue(Action.ACCELERATOR_KEY, saveAction.getValue(Action.ACCELERATOR_KEY));
            }
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
    
    // Utility methods >>
    public static void closeAllDocuments () {
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        List<TopComponent> tcs = getOpened(activeTC);
        
        for(TopComponent tc: tcs) {
            tc.close();
        }
    }

    public static void closeAllExcept (TopComponent tc) {
        List<TopComponent> tcs = getOpened(tc);

        for(TopComponent curTC: tcs) {
            if (curTC != tc) {
                curTC.close();
            }
        }
    }

    /** Returns List of opened top components in mode of given TopComponent.
     */
    private static List<TopComponent> getOpened (TopComponent tc) {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        ModeImpl mode = (ModeImpl) wm.findMode(tc);
        List<TopComponent> tcs = new ArrayList<TopComponent>();
        if (mode != null) {
                tcs.addAll(mode.getOpenedTopComponents());
        }
        return tcs;
    }

    static void closeWindow(TopComponent tc) {
        tc.close();
    }
    
    private static void saveDocument(TopComponent tc) {
        SaveCookie sc = getSaveCookie(tc);
        if(sc != null) {
            try {
                sc.save();
            } catch(IOException ioe) {
                Exceptions.printStackTrace(ioe);
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
    
    static void putSharedAccelerator (Object key, Object value) {
        sharedAccelerators.put(key, value);
    }
    
    static Object getSharedAccelerator (Object key) {
        return sharedAccelerators.get(key);
    }

    // Utility methods <<
}

