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

package org.netbeans.core.multiview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to show in main menu to switch active view.
 * @author mkleint
 */
public class EditorsAction extends AbstractAction 
                                implements Presenter.Menu {
                                    
    public EditorsAction() {
        super(NbBundle.getMessage(EditorsAction.class, "CTL_EditorsAction"));
    }
    
    public void actionPerformed(ActionEvent ev) {
        assert false;// no operation
    }
    
    public JMenuItem getMenuPresenter() {
        JMenu menu = new UpdatingMenu();
        String label = NbBundle.getMessage(EditorsAction.class, "CTL_EditorsAction");
        Mnemonics.setLocalizedText(menu, label);
        return menu;
    }
    
    private static final class UpdatingMenu extends JMenu implements DynamicMenuContent {
        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }

        public JComponent[] getMenuPresenters() {
            Mode mode = WindowManager.getDefault().findMode(CloneableEditorSupport.EDITOR_MODE);
            removeAll();
            if (mode != null) {
                TopComponent tc = mode.getSelectedTopComponent();
                if (tc != null) {
                    setEnabled(true);
                    MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
                    if (handler != null) {
                        final WeakReference handlerRef = new WeakReference(handler);
                        ButtonGroup group = new ButtonGroup();
                        MultiViewPerspective[] pers = handler.getPerspectives();
                        for (int i = 0; i < pers.length; i++) {
                            MultiViewPerspective thisPers = pers[i];
                            final WeakReference persRef = new WeakReference(thisPers);
                            
                            JRadioButtonMenuItem item = new JRadioButtonMenuItem(thisPers.getDisplayName());
                            item.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent event) {
                                    //#88626 prevent a memory leak
                                    MultiViewHandler handler = (MultiViewHandler)handlerRef.get();
                                    MultiViewPerspective thisPers = (MultiViewPerspective)persRef.get();
                                    if (handler != null && thisPers != null) {
                                        handler.requestActive(thisPers);
                                    }
                                }
                            });
                            if (thisPers.getDisplayName().equals(handler.getSelectedPerspective().getDisplayName())) {
                                item.setSelected(true);
                            }
                            group.add(item);
                            add(item);
                        }
                    } else { // handler == null
                        JRadioButtonMenuItem but = new JRadioButtonMenuItem(NbBundle.getMessage(EditorsAction.class, "EditorsAction.source"));
                        but.setSelected(true);
                        add(but);
                    }
                } else { // tc == null
                    setEnabled(false);
                }
            } else { // mode == null
                setEnabled(false);
            }
            return new JComponent[] {this};
        }
        
    }
    
}
