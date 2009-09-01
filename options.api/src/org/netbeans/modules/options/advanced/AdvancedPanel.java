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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.options.advanced;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.options.OptionsPanelControllerAccessor;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class AdvancedPanel extends JPanel {    
    JTabbedPane     tabbedPanel;
    private static final Logger LOGGER = Logger.getLogger(AdvancedPanel.class.getName());
    private LookupListener listener = new LookupListenerImpl();
    private Model model;
    private String subpath;
    private ChangeListener changeListener = new ChangeListener () {
            public void stateChanged(ChangeEvent e) {
                handleTabSwitched();
            }
        };
    
     /*
     * @param subpath path to folder under OptionsDialog folder containing 
     * instances of AdvancedOption class. Path is composed from registration 
     * names divided by slash.
     */
    AdvancedPanel(String subpath) {
        this.subpath = subpath;
        this.model = new Model(subpath, listener);
    }
        
    public void update () {
        int idx = tabbedPanel.getSelectedIndex();
        if (idx != -1) {
            String category = tabbedPanel.getTitleAt(idx);
            model.update (category);        
        }
    }
    
    public void applyChanges () {
        model.applyChanges ();
    }
    
    public void cancel () {
        model.cancel ();
    }
    
    public HelpCtx getHelpCtx () {
        return model.getHelpCtx ((tabbedPanel != null) ? ((JComponent)tabbedPanel.getSelectedComponent ()) : null);
    }
    
    public boolean dataValid () {
        return model.isValid ();
    }
    
    public boolean isChanged () {
        return model.isChanged ();
    }

    public void addModelPropertyChangeListener(PropertyChangeListener listener) {
        model.addPropertyChangeListener(listener);
    }

    public void removeModelPropertyChangeListener(PropertyChangeListener listener) {
        model.removePropertyChangeListener(listener);
    }
    
    public Lookup getLookup () {
        return model.getLookup ();
    }
    
    void init (Lookup masterLookup) {
        // init components
        tabbedPanel = new JTabbedPane();
        tabbedPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AdvancedPanel.class, "AdvancedPanel.tabbedPanel.AD"));
        
        // define layout
        setLayout (new BorderLayout ());
        removeAll(); // #157434 - remove previous tabbedPanel
        add (tabbedPanel, BorderLayout.CENTER);
        initTabbedPane (masterLookup);
    }

    private void initTabbedPane(Lookup masterLookup) {
        tabbedPanel.removeChangeListener(changeListener);
        tabbedPanel.removeAll();
        List<String> categories = model.getCategories();
        tabbedPanel.setVisible(categories.size() > 0);
        for (String category : categories) {
            tabbedPanel.addTab(category, new JLabel(category));
        }
        tabbedPanel.addChangeListener(changeListener);
        handleTabSwitched();
    }
    
    public void setCurrentSubcategory(String path) {
        String subcategoryID = path.indexOf('/') == -1 ? path : path.substring(0, path.indexOf('/'));
        final String subcategorySubpath = path.indexOf('/') == -1 ? null : path.substring(path.indexOf('/')+1);
        LOGGER.fine("Set current subcategory: "+path); // NOI18N
        if(!model.getIDs().contains(subcategoryID)) {
            LOGGER.warning("Subcategory "+subcategoryID+" not found.");  //NOI18N
            return;
        }
        String newDisplayName = model.getDisplayName(subcategoryID);
        String currentDisplayName = getSelectedDisplayName();
        if (!newDisplayName.equals(currentDisplayName)) {
            for (int i = 0; i < tabbedPanel.getComponentCount(); i++) {
                if (tabbedPanel.getTitleAt(i).equals(newDisplayName)) {
                    tabbedPanel.setSelectedIndex(i);
                    break;
                }
            }
        }
        if(subcategorySubpath != null) {
            OptionsPanelControllerAccessor.getDefault().setCurrentSubcategory(model.getController(subcategoryID), subcategorySubpath);
        }
    }
    
    private String getSelectedDisplayName() {
        String categoryDisplayName = null;
        final int selectedIndex = tabbedPanel.getSelectedIndex();
        if (selectedIndex != -1) {
            categoryDisplayName = tabbedPanel.getTitleAt(selectedIndex);
        }
        return categoryDisplayName;
    }

    private void handleTabSwitched() {        
        final int selectedIndex = tabbedPanel.getSelectedIndex() >= 0 ? tabbedPanel.getSelectedIndex() : -1;
        if (selectedIndex != -1) {
            String category = tabbedPanel.getTitleAt(selectedIndex);
            if (tabbedPanel.getSelectedComponent() instanceof JLabel) {
                tabbedPanel.setComponentAt(tabbedPanel.getSelectedIndex(), new JScrollPane(model.getPanel(category)));
                ((JComponent)tabbedPanel.getSelectedComponent()).setBorder (new EmptyBorder(11,11,11,11));
            }
            model.update(category);
            firePropertyChange (OptionsPanelController.PROP_HELP_CTX, null, null);        
        }
    }
    
    private class LookupListenerImpl implements LookupListener {
        public void resultChanged(LookupEvent ev) {
            final Lookup masterLookup = model.getLookup();
            model = new Model(subpath, listener);
            if(SwingUtilities.isEventDispatchThread()) {
                initTabbedPane(masterLookup);
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        initTabbedPane(masterLookup);
                    }
                });
            }
        }        
    }
}
