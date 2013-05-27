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
package org.netbeans.modules.css.prep.ui.wizard;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.preferences.LessPreferences;
import org.netbeans.modules.css.prep.preferences.LessPreferencesValidator;
import org.netbeans.modules.css.prep.preferences.SassPreferences;
import org.netbeans.modules.css.prep.preferences.SassPreferencesValidator;
import org.netbeans.modules.css.prep.ui.customizer.LessCustomizerPanel;
import org.netbeans.modules.css.prep.ui.customizer.SassCustomizerPanel;
import org.netbeans.modules.css.prep.util.ValidationResult;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class NewFileWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private final CssPreprocessorType type;

    WizardDescriptor wizard;
    private WizardDescriptor.Panel<WizardDescriptor> wizardPanel;
    // used in a background thread in instantiate() method
    private volatile BottomPanel bottomPanel;


    public NewFileWizardIterator(CssPreprocessorType type) {
        assert type != null;
        this.type = type;
    }

    @TemplateRegistration(folder = "Other", content = "../resources/style.less",
            position = 660, displayName = "#NewFileWizardIterator.less.template.displayName")
    @NbBundle.Messages("NewFileWizardIterator.less.template.displayName=Less Source File")
    public static WizardDescriptor.InstantiatingIterator<WizardDescriptor> createLessWizardIterator() {
        return new NewFileWizardIterator(CssPreprocessorType.LESS);
    }

    @TemplateRegistration(folder = "Other", content = "../resources/style.scss",
            position = 670, displayName = "#NewFileWizardIterator.scss.template.displayName")
    @NbBundle.Messages("NewFileWizardIterator.scss.template.displayName=Sassy CSS Source File")
    public static WizardDescriptor.InstantiatingIterator<WizardDescriptor> createSassWizardIterator() {
        return new NewFileWizardIterator(CssPreprocessorType.SASS);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        wizardPanel = createWizardPanel();
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        getBottomPanel().save();

        FileObject dir = Templates.getTargetFolder(wizard);
        FileObject template = Templates.getTemplate(wizard);

        DataFolder dataFolder = DataFolder.findFolder(dir);
        DataObject dataTemplate = DataObject.find(template);
        DataObject createdFile = dataTemplate.createFromTemplate(dataFolder, Templates.getTargetName(wizard));
        return Collections.singleton(createdFile.getPrimaryFile());
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        this.wizard = null;
        wizardPanel = null;
        bottomPanel = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return wizardPanel;
    }

    @Override
    public String name() {
        return ""; // NOI18N
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void nextPanel() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void previousPanel() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getBottomPanel().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getBottomPanel().removeChangeListener(listener);
    }

    private WizardDescriptor.Panel<WizardDescriptor> createWizardPanel() {
        Project project = getProject();
        assert project != null;
        return Templates.buildSimpleTargetChooser(project, getSourceGroups(project))
                .bottomPanel(getBottomPanel())
                .create();
    }

    Project getProject() {
        return Templates.getProject(wizard);
    }

    private SourceGroup[] getSourceGroups(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        return sources.getSourceGroups(Sources.TYPE_GENERIC);
    }

    @CheckForNull
    private BottomPanel getBottomPanel() {
        if (bottomPanel != null) {
            return bottomPanel;
        }
        ValidationResult result;
        switch (type) {
            case LESS:
                result = new LessPreferencesValidator()
                        .validate(getProject())
                        .getResult();
                if (result.hasErrors()
                        || result.hasWarnings()) {
                    // project setup incorrect -> show panel
                    bottomPanel = new LessBottomPanel(getProject());
                }
                break;
            case SASS:
                result = new SassPreferencesValidator()
                        .validate(getProject())
                        .getResult();
                if (result.hasErrors()
                        || result.hasWarnings()) {
                    // project setup incorrect -> show panel
                    bottomPanel = new SassBottomPanel(getProject());
                }
                break;
            default:
                assert false : "Unknown type: " + type;
        }
        if (bottomPanel == null) {
            bottomPanel = BottomPanel.EMPTY;
        }
        return bottomPanel;
    }


    //~ Inner classes

    private interface BottomPanel extends WizardDescriptor.Panel<WizardDescriptor> {

        BottomPanel EMPTY = new EmptyBottomPanel();

        void save() throws IOException;
    }

    private static final class EmptyBottomPanel implements BottomPanel {
        @Override
        public void save() throws IOException {
        }

        @Override
        public Component getComponent() {
            return new JPanel();
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

    }

    private abstract static class BaseBottomPanel implements BottomPanel {

        protected static final String ENABLED = "ENABLED"; // NOI18N
        protected static final String MAPPINGS = "MAPPINGS"; // NOI18N

        protected final Project project;

        volatile WizardDescriptor settings = null;


        public BaseBottomPanel(Project project) {
            assert project != null;
            this.project = project;
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            this.settings = settings;
        }

        @Override
        public final boolean isValid() {
            if (settings == null) {
                // not displayed yet
                return false;
            }
            String error = getValidationError();
            if (error != null) {
                settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error);
                return false;
            }
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N
            return true;
        }

        @CheckForNull
        protected abstract String getValidationError();

    }

    private static final class LessBottomPanel extends BaseBottomPanel {

        // @GuardedBy("EDT")
        private LessCustomizerPanel panel;


        public LessBottomPanel(Project project) {
            super(project);
        }

        @Override
        public LessCustomizerPanel getComponent() {
            assert EventQueue.isDispatchThread();
            if (panel == null) {
                panel = new LessCustomizerPanel();
            }
            return panel;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            super.readSettings(settings);
            getComponent().setLessEnabled(LessPreferences.isEnabled(project));
            getComponent().setMappings(LessPreferences.getMappings(project));
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            settings.putProperty(ENABLED, getComponent().isLessEnabled());
            settings.putProperty(MAPPINGS, getComponent().getMappings());
        }

        @Override
        protected String getValidationError() {
            ValidationResult result = new LessPreferencesValidator()
                    .validate(getComponent().isLessEnabled(), getComponent().getMappings())
                    .getResult();
            String error = result.getFirstErrorMessage();
            if (error == null) {
                error = result.getFirstWarningMessage();
            }
            return error;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            getComponent().addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            getComponent().removeChangeListener(l);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void save() throws IOException {
            LessPreferences.setEnabled(project, (boolean) settings.getProperty(ENABLED));
            LessPreferences.setMappings(project, (List<String>) settings.getProperty(MAPPINGS));
        }

    }

    private static final class SassBottomPanel extends BaseBottomPanel {

        // @GuardedBy("EDT")
        private SassCustomizerPanel panel;


        public SassBottomPanel(Project project) {
            super(project);
        }

        @Override
        public SassCustomizerPanel getComponent() {
            assert EventQueue.isDispatchThread();
            if (panel == null) {
                panel = new SassCustomizerPanel();
            }
            return panel;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            super.readSettings(settings);
            getComponent().setSassEnabled(SassPreferences.isEnabled(project));
            getComponent().setMappings(SassPreferences.getMappings(project));
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            settings.putProperty(ENABLED, getComponent().isSassEnabled());
            settings.putProperty(MAPPINGS, getComponent().getMappings());
        }

        @Override
        protected String getValidationError() {
            ValidationResult result = new SassPreferencesValidator()
                    .validate(getComponent().isSassEnabled(), getComponent().getMappings())
                    .getResult();
            String error = result.getFirstErrorMessage();
            if (error == null) {
                error = result.getFirstWarningMessage();
            }
            return error;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            getComponent().addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            getComponent().removeChangeListener(l);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void save() throws IOException {
            SassPreferences.setEnabled(project, (boolean) settings.getProperty(ENABLED));
            SassPreferences.setMappings(project, (List<String>) settings.getProperty(MAPPINGS));
        }

    }

}
