/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.options;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * Common Controller for all options categories composed by subpanels
 *
 * @author Max Sauer
 */
public class TabbedController extends OptionsPanelController {

    private String tabFolder;
    private Lookup.Result<AdvancedOption> options;
    private Map<String, OptionsPanelController> id2Controller;
    private Map<JComponent, OptionsPanelController> component2Option;
    private final LookupListener lookupListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            readPanels();
        }
    };

    /** pane with sub-panels */
    private JTabbedPane pane;
    /** PropertyChangeSupport and listener to fire changes when switching tabs. */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final ChangeListener tabbedPaneChangeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            pcs.firePropertyChange(OptionsPanelController.PROP_HELP_CTX, null, null);
        }
    };

    /**
     * Creates new instance
     * @param tabFolder layer folder where subpanels (AdvancedOption instances) reside
     */
    public TabbedController(String tabFolder) {
        this.tabFolder = tabFolder;
        readPanels();
        options.addLookupListener(WeakListeners.create(LookupListener.class, lookupListener, options));
    }

    @Override
    public void update() {
        for (OptionsPanelController c : getControllers()) {
            c.update();
        }
    }

    @Override
    public void applyChanges() {
        for (OptionsPanelController c : getControllers()) {
            c.applyChanges();
        }
    }

    @Override
    public void cancel() {
        for (OptionsPanelController c : getControllers()) {
            c.cancel();
        }
    }

    @Override
    public boolean isValid() {
        for (OptionsPanelController c : getControllers()) {
            if (!c.isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isChanged() {
        for (OptionsPanelController c : getControllers()) {
            if (c.isChanged()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (pane == null) {
            pane = new JTabbedPane();
            component2Option = new HashMap<JComponent, OptionsPanelController>();
            for (OptionsPanelController c : getControllers()) {
                JComponent comp = c.getComponent( c.getLookup());
                comp.setBorder(new EmptyBorder(8, 8, 8, 8));
                pane.add( controllers2Options.get(c).getDisplayName(), comp);
                component2Option.put(comp, c);
            }
            pane.addChangeListener(tabbedPaneChangeListener);
        }
        return pane;
    }

    @Override
    public HelpCtx getHelpCtx() {
       return pane != null ? getHelpCtx(pane.getSelectedComponent()) : null;
    }

    private HelpCtx getHelpCtx(Component c) {
        OptionsPanelController o = component2Option.get(c);
        if (o != null) {
            return o.getHelpCtx();
        }
        return new HelpCtx("netbeans.optionsDialog.java");
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
        for (OptionsPanelController c : getControllers()) {
            c.addPropertyChangeListener(l);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
        for (OptionsPanelController c : getControllers()) {
            c.removePropertyChangeListener(l);
        }
    }


    @Override
    protected void setCurrentSubcategory(String path) {
        String subcategoryID = path.indexOf('/') == -1 ? path : path.substring(0, path.indexOf('/'));
        OptionsPanelController controller = id2Controller.get(subcategoryID);
        if (controller == null) {
            return;
        }
        JComponent c = controller.getComponent(controller.getLookup());
        if (c != pane.getSelectedComponent()) {
            pane.setSelectedComponent(c);
        }
    }

    // ------ private impl
    private Map<OptionsPanelController, AdvancedOption> controllers2Options;
    private List<OptionsPanelController> controllers;

    private synchronized Collection<OptionsPanelController> getControllers() {
        if (controllers == null) {
            id2Controller = new HashMap<String, OptionsPanelController>();
            controllers2Options = new LinkedHashMap<OptionsPanelController, AdvancedOption>();
            controllers = new LinkedList<OptionsPanelController>();
            for (Lookup.Item<AdvancedOption> item : options.allItems()) {
                AdvancedOption o = item.getInstance();
                OptionsPanelController c = o.create();
                if (c == null) continue;
                String id = item.getId().substring(item.getId().lastIndexOf('/') + 1);
                id2Controller.put(id, c);
                controllers2Options.put(c, o);
                controllers.add(c);
            }
        }

        return controllers;
    }

    private void readPanels() {
        Lookup lookup = Lookups.forPath(tabFolder);
        options = lookup.lookup(new Lookup.Template<AdvancedOption>( AdvancedOption.class ));
    }
}
