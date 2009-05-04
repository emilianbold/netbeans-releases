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

package org.netbeans.modules.php.project.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * @author Tomas Mysik
 */
public class PhpOptionsPanelController extends OptionsPanelController implements ChangeListener {

    private static final String TAB_FOLDER = "org.netbeans.modules.php/options/"; // NOI18N
    private final PhpOptionsPanel phpOptionsPanel = new PhpOptionsPanel();
    private final Collection<? extends AdvancedOption> options;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    // @GuardedBy(this)
    private Map<OptionsPanelController, AdvancedOption> controllers2Options;
    private JTabbedPane pane;
    private boolean changed;

    public PhpOptionsPanelController() {
        options = Lookups.forPath(TAB_FOLDER).lookupAll(AdvancedOption.class);
        phpOptionsPanel.addChangeListener(this);
    }

    private synchronized Map<OptionsPanelController, AdvancedOption> getControllers2Options() {
        if (controllers2Options == null) {
            controllers2Options = new LinkedHashMap<OptionsPanelController, AdvancedOption>();
            for (AdvancedOption o : options) {
                OptionsPanelController c = o.create();
                controllers2Options.put(c, o);
            }
        }

        return controllers2Options;
    }

    private Set<OptionsPanelController> getControllers() {
        return getControllers2Options().keySet();
    }

    @Override
    public void update() {
        for (OptionsPanelController c : getControllers()) {
            c.update();
        }

        phpOptionsPanel.setPhpInterpreter(getPhpOptions().getPhpInterpreter());
        phpOptionsPanel.setOpenResultInOutputWindow(getPhpOptions().isOpenResultInOutputWindow());
        phpOptionsPanel.setOpenResultInBrowser(getPhpOptions().isOpenResultInBrowser());
        phpOptionsPanel.setOpenResultInEditor(getPhpOptions().isOpenResultInEditor());

        phpOptionsPanel.setDebuggerPort(getPhpOptions().getDebuggerPort());
        phpOptionsPanel.setDebuggerSessionId(getPhpOptions().getDebuggerSessionId());
        phpOptionsPanel.setDebuggerStoppedAtTheFirstLine(getPhpOptions().isDebuggerStoppedAtTheFirstLine());

        phpOptionsPanel.setPhpUnit(getPhpOptions().getPhpUnit());

        changed = false;
    }

    @Override
    public void applyChanges() {
        for (OptionsPanelController c : getControllers()) {
            c.applyChanges();
        }

        getPhpOptions().setPhpInterpreter(phpOptionsPanel.getPhpInterpreter());
        getPhpOptions().setOpenResultInOutputWindow(phpOptionsPanel.isOpenResultInOutputWindow());
        getPhpOptions().setOpenResultInBrowser(phpOptionsPanel.isOpenResultInBrowser());
        getPhpOptions().setOpenResultInEditor(phpOptionsPanel.isOpenResultInEditor());

        getPhpOptions().setDebuggerPort(phpOptionsPanel.getDebuggerPort());
        getPhpOptions().setDebuggerSessionId(phpOptionsPanel.getDebuggerSessionId());
        getPhpOptions().setDebuggerStoppedAtTheFirstLine(phpOptionsPanel.isDebuggerStoppedAtTheFirstLine());

        getPhpOptions().setPhpGlobalIncludePath(phpOptionsPanel.getPhpGlobalIncludePath());

        getPhpOptions().setPhpUnit(phpOptionsPanel.getPhpUnit());

        changed = false;
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

        return validateComponent();
    }

    @Override
    public boolean isChanged() {
        for (OptionsPanelController c : getControllers()) {
            if (c.isChanged()) {
                return true;
            }
        }

        if (!phpOptionsPanel.getPhpInterpreter().equals(getPhpOptions().getPhpInterpreter())) {
            return true;
        }
        if (getPhpOptions().isOpenResultInOutputWindow() != phpOptionsPanel.isOpenResultInOutputWindow()) {
            return true;
        }
        if (getPhpOptions().isOpenResultInBrowser() != phpOptionsPanel.isOpenResultInBrowser()) {
            return true;
        }
        if (getPhpOptions().isOpenResultInEditor() != phpOptionsPanel.isOpenResultInEditor()) {
            return true;
        }

        Integer debuggerPort = phpOptionsPanel.getDebuggerPort();
        if (debuggerPort != null && getPhpOptions().getDebuggerPort() != debuggerPort) {
            return true;
        }
        String debuggerSessionId = phpOptionsPanel.getDebuggerSessionId();
        if (debuggerSessionId != null && !getPhpOptions().getDebuggerSessionId().equals(debuggerSessionId)) {
            return true;
        }
        if (getPhpOptions().isDebuggerStoppedAtTheFirstLine() != phpOptionsPanel.isDebuggerStoppedAtTheFirstLine()) {
            return true;
        }

        if (!phpOptionsPanel.getPhpUnit().equals(getPhpOptions().getPhpUnit())) {
            return true;
        }

        if (!getPhpOptions().getPhpGlobalIncludePath().equals(phpOptionsPanel.getPhpGlobalIncludePath())) {
            return true;
        }
        return false;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
         if (pane == null) {
            pane = new JTabbedPane();
            pane.add(NbBundle.getMessage(PhpOptionsPanelController.class, "LBL_GeneralOPtions"), phpOptionsPanel);

            for (Entry<OptionsPanelController, AdvancedOption> e : getControllers2Options().entrySet()) {
                OptionsPanelController controller = e.getKey();
                AdvancedOption option = e.getValue();
                pane.add(option.getDisplayName(), controller.getComponent(controller.getLookup()));
            }
        }
        return pane;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    private PhpOptions getPhpOptions() {
        return PhpOptions.getInstance();
    }

    public void stateChanged(ChangeEvent e) {
        changed();
    }

    private boolean validateComponent() {
        // errors
        Integer debuggerPort = phpOptionsPanel.getDebuggerPort();
        if (debuggerPort == null || debuggerPort < 1) {
            phpOptionsPanel.setError(NbBundle.getMessage(PhpOptionsPanelController.class, "MSG_DebuggerInvalidPort"));
            return false;
        }
        String debuggerSessionId = phpOptionsPanel.getDebuggerSessionId();
        if (debuggerSessionId == null
                || debuggerSessionId.trim().length() == 0
                || debuggerSessionId.contains(" ")) { // NOI18N
            phpOptionsPanel.setError(NbBundle.getMessage(PhpOptionsPanelController.class, "MSG_DebuggerInvalidSessionId"));
            return false;
        }

        // warnings
        // #144680
        String warning = Utils.validatePhpInterpreter(phpOptionsPanel.getPhpInterpreter());
        if (warning != null) {
            phpOptionsPanel.setWarning(warning);
            return true;
        }

        warning = Utils.validatePhpUnit(phpOptionsPanel.getPhpUnit());
        if (warning != null) {
            phpOptionsPanel.setWarning(warning);
            return true;
        }

        PhpUnit.resetVersion();
        PhpUnit phpUnit = new PhpUnit(phpOptionsPanel.getPhpUnit());
        if (!phpUnit.supportedVersionFound()) {
            phpOptionsPanel.setWarning(NbBundle.getMessage(
                    PhpOptionsPanelController.class, "MSG_OldPhpUnit", PhpUnit.getVersions(phpUnit)));
            return true;
        }

        // everything ok
        phpOptionsPanel.setError(" "); // NOI18N
        return true;
    }

    private void changed() {
        if (!changed) {
            changed = true;
            propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
}
