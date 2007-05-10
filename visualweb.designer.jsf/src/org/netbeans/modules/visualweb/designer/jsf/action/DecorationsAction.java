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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.visualweb.designer.jsf.action;


import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.visualweb.designer.jsf.DecorationManager;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import org.netbeans.modules.visualweb.designer.jsf.JsfDesignerPreferences;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;


/**
 * Action switching on/off decorations.
 *
 * @author Peter Zavadsky
 */
public class DecorationsAction extends AbstractAction implements Presenter.Toolbar {


    PropertyChangeListener decorationManagerListener = new DecorationManagerListener(this);

    /** Creates a new instance of DecorationsAction */
    public DecorationsAction() {
        putValue(NAME, NbBundle.getMessage(DecorationsAction.class, "LBL_DecorationsActionName"));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(DecorationsAction.class, "LBL_DecorationsAction"));
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage("org/netbeans/modules/visualweb/designer/resources/decorations.png"))); // NOI18N

        DecorationManager decorationManager = DecorationManager.getDefault();
        decorationManager.addPropertyChangeListener(WeakListeners.propertyChange(decorationManagerListener, decorationManager));
        updateState();
    }


    public void actionPerformed(ActionEvent evt) {
        JsfDesignerPreferences preferences = JsfDesignerPreferences.getInstance();
        boolean showDecorations = preferences.isShowDecorations();
        preferences.setShowDecorations(!showDecorations);
    }

    public Component getToolbarPresenter() {
        if (!isEnabled()) {
            // XXX #6470521 To not show the decorations action when there are no decorations.
            return new JComponent() {};
        }

        return new DecorationsToolbarButton(this);
    }

    private void updateState() {
        setEnabled(DecorationManager.getDefault().getDecorationProviders().length > 0);
    }

    private static class DecorationsToolbarButton extends JToggleButton
    implements /*PropertyChangeListener*/ PreferenceChangeListener {
        public DecorationsToolbarButton(Action action) {
            super();
            
            // XXX Hide action text for the button.
            putClientProperty("hideActionText", Boolean.TRUE); // NOI18N
            setAction(action);

            JsfDesignerPreferences preferences = JsfDesignerPreferences.getInstance();
//            settings.addPropertyChangeListener(WeakListeners.propertyChange(this, settings));
            preferences.addWeakPreferenceChangeListener(this);

            updateState();
        }

//        public void propertyChange(PropertyChangeEvent evt) {
        public void preferenceChange(PreferenceChangeEvent evt) {
//            if (DesignerSettings.PROP_SHOW_DECORATIONS.equals(evt.getPropertyName())) {
            if (JsfDesignerPreferences.PROP_SHOW_DECORATIONS.equals(evt.getKey())) {
                updateState();
            }
        }

        private void updateState() {
            JsfDesignerPreferences settings = JsfDesignerPreferences.getInstance();
            boolean showDecorations = settings.isShowDecorations();
            setSelected(showDecorations);
        }
    } // End of DecoratoinsToolbarButton.


    private static class DecorationManagerListener implements PropertyChangeListener {
        private final DecorationsAction decorationsAction;

        public DecorationManagerListener(DecorationsAction decorationsAction) {
            this.decorationsAction = decorationsAction;
        }
        public void propertyChange(PropertyChangeEvent evt) {
            if (DecorationManager.PROP_DECORATION_PROVIDERS.equals(evt.getPropertyName())) {
                decorationsAction.updateState();
            }
        }
    } // End of DecorationManagerListener.
}
