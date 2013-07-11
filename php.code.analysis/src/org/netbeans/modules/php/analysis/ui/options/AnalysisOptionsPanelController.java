/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.analysis.ui.options;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(
    location=UiUtils.OPTIONS_PATH,
    id=AnalysisOptionsPanelController.OPTIONS_SUB_PATH,
    displayName="#AnalysisOptionsPanel.name",
//    toolTip="#LBL_OptionsTooltip"
    position=157
)
public class AnalysisOptionsPanelController extends OptionsPanelController implements ChangeListener {

    public static final String OPTIONS_SUB_PATH = "CodeAnalysis"; // NOI18N
    public static final String OPTIONS_PATH = UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUB_PATH; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    // @GuardedBy("EDT")
    private AnalysisOptionsPanel analysisOptionsPanel = null;
    private volatile boolean changed = false;


    @Override
    public void update() {
        assert EventQueue.isDispatchThread();
        getAnalysisOptionsPanel().setCodeSnifferPath(getAnalysisOptions().getCodeSnifferPath());
        getAnalysisOptionsPanel().setCodeSnifferStandard(getAnalysisOptions().getCodeSnifferStandard());
        getAnalysisOptionsPanel().setMessDetectorPath(getAnalysisOptions().getMessDetectorPath());
        getAnalysisOptionsPanel().setMessDetectorRuleSets(getAnalysisOptions().getMessDetectorRuleSets());

        changed = true;
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getAnalysisOptions().setCodeSnifferPath(getAnalysisOptionsPanel().getCodeSnifferPath());
                getAnalysisOptions().setCodeSnifferStandard(getAnalysisOptionsPanel().getCodeSnifferStandard());
                getAnalysisOptions().setMessDetectorPath(getAnalysisOptionsPanel().getMessDetectorPath());
                getAnalysisOptions().setMessDetectorRuleSets(getAnalysisOptionsPanel().getMessDetectorRuleSets());

                changed = false;
            }
        });
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isValid() {
        assert EventQueue.isDispatchThread();
        AnalysisOptionsPanel panel = getAnalysisOptionsPanel();
        ValidationResult result = new AnalysisOptionsValidator()
                .validateCodeSniffer(panel.getCodeSnifferPath(), panel.getCodeSnifferStandard())
                .validateMessDetector(panel.getMessDetectorPath(), panel.getMessDetectorRuleSets())
                .getResult();
        // errors
        if (result.hasErrors()) {
            panel.setError(result.getErrors().get(0).getMessage());
            return false;
        }
        // warnings
        if (result.hasWarnings()) {
            panel.setWarning(result.getWarnings().get(0).getMessage());
            return true;
        }
        // everything ok
        panel.setError(" "); // NOI18N
        return true;
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        assert EventQueue.isDispatchThread();
        return getAnalysisOptionsPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.analysis.ui.options.AnalysisOptionsPanelController"); // NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (changed) {
            propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private AnalysisOptionsPanel getAnalysisOptionsPanel() {
        assert EventQueue.isDispatchThread();
        if (analysisOptionsPanel == null) {
            analysisOptionsPanel = new AnalysisOptionsPanel();
            analysisOptionsPanel.addChangeListener(this);
        }
        return analysisOptionsPanel;
    }

    private AnalysisOptions getAnalysisOptions() {
        return AnalysisOptions.getInstance();
    }

}
