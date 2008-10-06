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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import org.openide.util.ImageUtilities;
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
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/visualweb/designer/resources/decorations.png"))); // NOI18N

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
