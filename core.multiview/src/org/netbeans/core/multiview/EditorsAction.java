/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.multiview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to show in main menu..
 * @author  mkleint
 */
public class EditorsAction extends AbstractAction 
                                implements Presenter.Menu {
                                    
    private PropertyChangeListener propListener;
    private JMenu menu;
    
    public EditorsAction() {
        putValue(NAME, NbBundle.getMessage(EditorsAction.class, "CTL_EditorsAction"));
        propListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName()) || 
                   TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                    updateState();
                }
           }
        };
        TopComponent.Registry registry = TopComponent.getRegistry();
//        registry.addPropertyChangeListener(WeakListeners.propertyChange(propListener, registry));
        registry.addPropertyChangeListener(propListener);

        // #37529 WindowsAPI to be called from AWT thread only.
        if(SwingUtilities.isEventDispatchThread()) {
            updateState();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateState();
                }
            });
        }        
    }
    
    /** Perform the action. Tries the performer and then scans the ActionMap
     * of selected topcomponent.
     */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        // no operation
    }
    
    public JMenuItem getMenuPresenter() {
        String label = NbBundle.getMessage(EditorsAction.class, "CTL_EditorsAction");
        menu = new JMenu(label);
        Mnemonics.setLocalizedText(menu, label);
        updateMenu();
        return menu;
    }
    
    private void updateState() {
        Mode mode = (Mode)WindowManager.getDefault().findMode("editor"); // NOI18N
        boolean enabled = mode == null ? false : mode.getSelectedTopComponent() != null;
        setEnabled(enabled);
        updateMenu();
    }
    
    private void updateMenu() {
        if (menu != null) {
            menu.removeAll();
            Mode mode = (Mode)WindowManager.getDefault().findMode("editor"); // NOI18N
            if (mode != null) {
                TopComponent tc = mode.getSelectedTopComponent();
                if (tc != null) {
                    menu.setEnabled(true);
                    MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
                    if (handler != null) {
                        ButtonGroup group = new ButtonGroup();
                        MultiViewPerspective[] pers = handler.getPerspectives();
                        for (int i = 0; i < pers.length; i++) {
                            JRadioButtonMenuItem item = new ListeningRadioButtonMenuItem(handler, pers[i]);
                            if (pers[i].getDisplayName().equals(handler.getSelectedPerspective().getDisplayName())) {
                                item.setSelected(true);
                            }
                            group.add(item);
                            menu.add(item);
                        }
                    } else {
                        JRadioButtonMenuItem but = new JRadioButtonMenuItem(NbBundle.getMessage(EditorsAction.class, "EditorsAction.source"));
                        but.setSelected(true);
                        menu.add(but);
                    }
                } else {
                    menu.setEnabled(false);
                }
            } 
        }
    }
    
    
    private class ListeningRadioButtonMenuItem extends JRadioButtonMenuItem implements MultiViewModel.ElementSelectionListener {
        
        private MultiViewModel model;
        
        ListeningRadioButtonMenuItem(/*MultiViewModel mvModel,*/ final MultiViewHandler handler, final MultiViewPerspective  pers) {
            super(pers.getDisplayName());
//            model = mvModel;
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    handler.requestActive(pers);
                }
            });
        }
        
        
        public void addNotify() {
            super.addNotify();
//            model.addElementSelectionListener(this);
        }
        
        public void removeNotify() {
            super.removeNotify();
//            model.removeElementSelectionListener(this);
        }
        
        public void selectionActivatedByButton() {
        }
        
        public void selectionChanged(MultiViewDescription oldOne, MultiViewDescription newOne) {
            if (getName().equals(newOne.getDisplayName())) {
                setSelected(true);
            }
        }
        
    }
    
}
