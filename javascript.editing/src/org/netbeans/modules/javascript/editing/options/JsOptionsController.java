/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.javascript.editing.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.javascript.editing.BrowserVersion;
import org.netbeans.modules.javascript.editing.SupportedBrowsers;
import org.netbeans.modules.javascript.editing.spi.JSPreferencesPanel;
import org.netbeans.modules.javascript.editing.spi.JSPreferencesPanelProvider;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
@OptionsPanelController.SubRegistration(
    displayName="#JsOptions.displayName",
    keywords="#KW_JavaScriptOptions",
    keywordsCategory="Advanced/JavaScript"
//    toolTip="#JsOptions.tooltip"
)
public class JsOptionsController extends OptionsPanelController {

    private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    private List<JSPreferencesPanel> preferencesPanels = new LinkedList<JSPreferencesPanel>();

    /** <i>GuardedBy("this")</i> */
    private JPanel panel;

    private boolean changed;

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getComponent();
    }

    @Override
    public void update() {
        for( JSPreferencesPanel panel : preferencesPanels ){
            panel.load();
        }
    }

    @Override
    public void applyChanges() {
        for( JSPreferencesPanel panel : preferencesPanels ){
            panel.store();
        }
        changed = false;
    }

    @Override
    public void cancel() {
        // nothing to do
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public boolean isValid() {
        return true; // always valid
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertySupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertySupport.removePropertyChangeListener(l);
    }

    void changed() {
        if (!changed) {
            changed = true;
            propertySupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        propertySupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private synchronized JPanel getComponent() {
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel,javax.swing.BoxLayout.Y_AXIS));
            preferencesPanels.add(new BrowserPanel(this));
            for ( JSPreferencesPanelProvider provider : getJSPreferencesPanelProviders() ) {
                preferencesPanels.add(provider.getPanel());
            }
            for( JSPreferencesPanel prefPanel : preferencesPanels ){
                panel.add(prefPanel);
            }
        }
        return panel;
    }

    /**
     * The accessor pattern class.
     */
    public abstract static class Accessor {

        /** The default accessor. */
        public static Accessor DEFAULT;

        static {
            // invokes static initializer of ReaderManager.class
            // that will assign value to the DEFAULT field above
            Class c = SupportedBrowsers.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException ex) {
                assert false : ex;
            }
        }

        public abstract void setSupported(SupportedBrowsers supported, EnumSet<BrowserVersion> versions);

        public abstract void setLanguageVersion(SupportedBrowsers supported, int version);
    }
    
        /**
     * Moved this out of Page.java so that WebFolderListener also has an opportunity  to
     * access the providers so that it can listen and decide wether or not to update
     * contents should be updated given a page.
     **/
    public static final Collection<? extends JSPreferencesPanelProvider> getJSPreferencesPanelProviders() {
        Lookup.Template<JSPreferencesPanelProvider> templ = new Lookup.Template<JSPreferencesPanelProvider>(JSPreferencesPanelProvider.class);
        final Lookup.Result<JSPreferencesPanelProvider> result = Lookup.getDefault().lookup(templ);
        Collection<? extends JSPreferencesPanelProvider> impls = result.allInstances();
        return impls;
    }
}
