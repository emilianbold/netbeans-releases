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
import java.io.File;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class PhpOptionsPanelController extends OptionsPanelController implements ChangeListener {

    private final PhpOptionsPanel phpOptionsPanel = new PhpOptionsPanel();

    public PhpOptionsPanelController() {
        phpOptionsPanel.addChangeListener(this);
    }

    @Override
    public void update() {
        phpOptionsPanel.setPhpInterpreter(getPhpOptions().getPhpInterpreter());
        phpOptionsPanel.setOpenResultInOutputWindow(getPhpOptions().isOpenResultInOutputWindow());
        phpOptionsPanel.setOpenResultInBrowser(getPhpOptions().isOpenResultInBrowser());
        phpOptionsPanel.setOpenResultInEditor(getPhpOptions().isOpenResultInEditor());

        phpOptionsPanel.setDebuggerPort(getPhpOptions().getDebuggerPort());
        phpOptionsPanel.setDebuggerStoppedAtTheFirstLine(getPhpOptions().isDebuggerStoppedAtTheFirstLine());
    }

    @Override
    public void applyChanges() {
        getPhpOptions().setPhpInterpreter(phpOptionsPanel.getPhpInterpreter());
        getPhpOptions().setOpenResultInOutputWindow(phpOptionsPanel.isOpenResultInOutputWindow());
        getPhpOptions().setOpenResultInBrowser(phpOptionsPanel.isOpenResultInBrowser());
        getPhpOptions().setOpenResultInEditor(phpOptionsPanel.isOpenResultInEditor());

        getPhpOptions().setDebuggerPort(phpOptionsPanel.getDebuggerPort());
        getPhpOptions().setDebuggerStoppedAtTheFirstLine(phpOptionsPanel.isDebuggerStoppedAtTheFirstLine());

        getPhpOptions().setPhpGlobalIncludePath(phpOptionsPanel.getPhpGlobalIncludePath());
    }

    @Override
    public void cancel() {
        // nothing needed? what about some cleanup/gc??
    }

    @Override
    public boolean isValid() {
        return validateComponent();
    }

    @Override
    public boolean isChanged() {
        if (!getPhpOptions().getPhpInterpreter().equals(phpOptionsPanel.getPhpInterpreter())) {
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
        if (getPhpOptions().isDebuggerStoppedAtTheFirstLine() != phpOptionsPanel.isDebuggerStoppedAtTheFirstLine()) {
            return true;
        }

        if (!getPhpOptions().getPhpGlobalIncludePath().equals(phpOptionsPanel.getPhpGlobalIncludePath())) {
            return true;
        }
        return false;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return phpOptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        // what this method is for??? javadoc is really poor...
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        // what this method is for??? javadoc is really poor...
    }

    private PhpOptions getPhpOptions() {
        return PhpOptions.getInstance();
    }

    public void stateChanged(ChangeEvent e) {
        validateComponent();
    }

    private boolean validateComponent() {
        String phpInterpreter = phpOptionsPanel.getPhpInterpreter();
        if (phpInterpreter.length() > 0) {
            File file = new File(phpInterpreter);
            if (!file.isAbsolute()) {
                phpOptionsPanel.setError(NbBundle.getMessage(PhpOptionsPanelController.class, "MSG_PhpNotAbsolutePath"));
                return false;
            }
            if (!file.isFile()) {
                phpOptionsPanel.setError(NbBundle.getMessage(PhpOptionsPanelController.class, "MSG_PhpNotFile"));
                return false;
            }
            if (!file.canRead()) {
                phpOptionsPanel.setError(NbBundle.getMessage(PhpOptionsPanelController.class, "MSG_PhpCannotRead"));
                return false;
            }
        }
        Integer debuggerPort = phpOptionsPanel.getDebuggerPort();
        if (debuggerPort == null || debuggerPort < 1) {
            phpOptionsPanel.setError(NbBundle.getMessage(PhpOptionsPanelController.class, "MSG_DebuggerInvalidPort"));
            return false;
        }
        // everything ok
        phpOptionsPanel.setError(" "); // NOI18N
        return true;
    }
}
