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

package org.netbeans.modules.debugger.jpda.ui.models;

import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.ContextProvider;

import org.openide.util.actions.Presenter;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.*;
import java.awt.event.ActionEvent;

/**
 * Provides popup menu for JPDA session nodes: suspend options and language selection. 
 *
 * @author Maros Sandor
 */
public class JPDASessionActionsProvider implements NodeActionsProviderFilter {

    private HashSet         listeners;
    private ContextProvider contextProvider;
    

    public JPDASessionActionsProvider (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        original.performDefaultAction(node);
    }

    public Action [] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {

        if (!(node instanceof Session)) return original.getActions(node);
        Action [] actions;
        try {
            actions = original.getActions(node);
        } catch (UnknownTypeException e) {
            actions = new Action[0];
        }
        List myActions = new ArrayList();
        if (node instanceof Session) {
            Session session = (Session) node;
            myActions.add(new CustomizeSession(session));
            myActions.add(new LanguageSelection(session));
        }
        myActions.addAll(Arrays.asList(actions));
        return (Action[]) myActions.toArray(new Action[myActions.size()]);
    }

    private String localize(String s) {
        return NbBundle.getBundle(JPDASessionActionsProvider.class).getString(s);
    }

    private class LanguageSelection extends AbstractAction implements Presenter.Popup {

        private Session session;

        public LanguageSelection(Session session) {
            this.session = session;
        }

        public void actionPerformed(ActionEvent e) {
        }

        public JMenuItem getPopupPresenter() {
            JMenu displayAsPopup = new JMenu(localize("CTL_Session_Popup_Language"));

            String [] languages = session.getSupportedLanguages();
            String currentLanguage = session.getCurrentLanguage();
            for (int i = 0; i < languages.length; i++) {
                final String language = languages[i];
                JRadioButtonMenuItem langItem = new JRadioButtonMenuItem(new AbstractAction(language) {
                    public void actionPerformed(ActionEvent e) {
                        session.setCurrentLanguage(language);
                    }
                });
                if (currentLanguage.equals(language)) langItem.setSelected(true);
                displayAsPopup.add(langItem);
            }
            return displayAsPopup;
        }
    }

    private class CustomizeSession extends AbstractAction implements Presenter.Popup {

        private Session session;

        public CustomizeSession(Session session) {
            this.session = session;
        }

        public void actionPerformed(ActionEvent e) {
        }

        public JMenuItem getPopupPresenter() {
            JMenu displayAsPopup = new JMenu 
                (localize ("CTL_Session_Resume_Threads"));

            JRadioButtonMenuItem resumeAllItem = new JRadioButtonMenuItem (
                new AbstractAction (localize ("CTL_Session_Resume_All_Threads")
            ) {
                public void actionPerformed (ActionEvent e) {
                    JPDADebugger dbg = (JPDADebugger) contextProvider.
                        lookupFirst (null, JPDADebugger.class);
                    dbg.setSuspend (JPDADebugger.SUSPEND_ALL);
                }
            });
            JRadioButtonMenuItem resumeCurrentItem = new JRadioButtonMenuItem (
                new AbstractAction (localize ("CTL_Session_Resume_Current_Thread")
            ) {
                public void actionPerformed(ActionEvent e) {
                    JPDADebugger dbg = (JPDADebugger) contextProvider.
                        lookupFirst (null, JPDADebugger.class);
                    dbg.setSuspend (JPDADebugger.SUSPEND_EVENT_THREAD);
                }
            });

            JPDADebugger dbg = (JPDADebugger) contextProvider.lookupFirst
                (null, JPDADebugger.class);
            if (dbg.getSuspend() == JPDADebugger.SUSPEND_ALL) 
                resumeAllItem.setSelected(true);
            else resumeCurrentItem.setSelected(true);

            displayAsPopup.add(resumeAllItem);
            displayAsPopup.add(resumeCurrentItem);
            return displayAsPopup;
        }
    }

    public void addModelListener(ModelListener l) {
        HashSet newListeners = (listeners == null) ? new HashSet() : (HashSet) listeners.clone();
        newListeners.add(l);
        listeners = newListeners;
    }

    public void removeModelListener(ModelListener l) {
        if (listeners == null) return;
        HashSet newListeners = (HashSet) listeners.clone();
        newListeners.remove(l);
        listeners = newListeners;
    }

}
