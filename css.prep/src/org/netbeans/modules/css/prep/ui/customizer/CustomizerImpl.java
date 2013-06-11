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
package org.netbeans.modules.css.prep.ui.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.options.CssPrepOptions;
import org.netbeans.modules.css.prep.preferences.CssPreprocessorPreferences;
import org.netbeans.modules.css.prep.util.BaseCssPreprocessor;
import org.netbeans.modules.css.prep.util.ValidationResult;
import org.netbeans.modules.css.prep.util.Warnings;
import org.netbeans.modules.web.common.spi.CssPreprocessorImplementation;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Pair;

public final class CustomizerImpl implements CssPreprocessorImplementation.Customizer, PropertyChangeListener {

    private final BaseCssPreprocessor cssPreprocessor;
    private final Project project;
    private final CssPreprocessorType type;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private volatile OptionsPanel customizerPanel = null;


    public CustomizerImpl(BaseCssPreprocessor cssPreprocessor, Project project, CssPreprocessorType type) {
        assert cssPreprocessor != null;
        assert project != null;
        assert type != null;

        this.cssPreprocessor = cssPreprocessor;
        this.project = project;
        this.type = type;
    }

    @Override
    public String getDisplayName() {
        return type.getDisplayName();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
        changeSupport.addChangeListener(listener);
        CssPrepOptions.getInstance().addPropertyChangeListener(this);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        CssPrepOptions.getInstance().removePropertyChangeListener(this);
        changeSupport.removeChangeListener(listener);
        getComponent().removeChangeListener(listener);
    }

    @Override
    public synchronized OptionsPanel getComponent() {
        if (customizerPanel == null) {
            CssPreprocessorPreferences preferences = type.getPreferences();
            customizerPanel = new OptionsPanel(type, preferences.isEnabled(project), preferences.getMappings(project));
        }
        assert customizerPanel != null;
        return customizerPanel;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.css.prep.ui.customizer.CustomizerImpl." + type.name()); // NOI18N
    }

    @Override
    public boolean isValid() {
        return !getValidationResult().hasErrors();
    }

    @Override
    public String getErrorMessage() {
        return getValidationResult().getFirstErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return getValidationResult().getFirstWarningMessage();
    }

    @Override
    public void save() throws IOException {
        Warnings.resetWarning(type);
        boolean fire = false;
        CssPreprocessorPreferences preferences = type.getPreferences();
        // configured
        if (getComponent().isConfigured()) {
            // only if true, otherwise do not change!
            preferences.setConfigured(project, true);
        }
        // enabled
        boolean originalEnabled = preferences.isEnabled(project);
        boolean enabled = getComponent().isCompilationEnabled();
        preferences.setEnabled(project, enabled);
        if (enabled != originalEnabled) {
            fire = true;
        }
        // mappings
        List<Pair<String, String>> originalMappings = preferences.getMappings(project);
        List<Pair<String, String>> mappings = getComponent().getMappings();
        preferences.setMappings(project, mappings);
        // #230945
        mappings.removeAll(originalMappings);
        if (!mappings.isEmpty()) {
            fire = true;
        }
        // change?
        if (fire) {
            cssPreprocessor.fireCustomizerChanged(project);
        }
    }

    private ValidationResult getValidationResult() {
        boolean compilationEnabled = getComponent().isCompilationEnabled();
        return type.getPreferencesValidator()
                .validateMappings(compilationEnabled, getComponent().getMappings())
                .validateExecutable(compilationEnabled)
                .getResult();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (type.getExecutablePathPropertyName().equals(evt.getPropertyName())) {
            changeSupport.fireChange();
        }
    }

}
