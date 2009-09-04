/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.project.deps.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.javacard.api.ProjectKind;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.deps.DependencyKind;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

final class ChooseOriginWizardPanel implements WizardDescriptor.AsynchronousValidatingPanel<Map<String, Object>>, ChangeListener {
    private ChooseOriginPanelVisual component;
    private final WizardDescriptor wiz;
    private final AddDependencyWizardIterator iter;
    static final String PROP_EXP_FILE = "_expFile"; //NOI18N
    static final String PROP_ORIGIN_FILE = "_originFile"; //NOI18N
    static final String PROP_ACTUAL_DEP_KIND = "_actualDepKind"; //NOI18N
    static final String PROP_SIG_FILE = "_sigFile"; //NOI18N
    static final String PROP_SOURCE_ROOT = "_sourceRoot"; //NOI18N
    static final String PROP_INTERMEDIATE_PANEL_KIND = "_intermediatePanelKind"; //NOI18N
    private Map<String, Object> settings;
    private InitialDepKind kind;
    private final ChangeSupport supp = new ChangeSupport(this);
    ChooseOriginWizardPanel(WizardDescriptor wiz, AddDependencyWizardIterator iter) {
        this.wiz = wiz;
        this.iter = iter;
        Parameters.notNull("wiz", wiz); //NOI18N
    }

    public Component getComponent() {
        if (component == null) {
            component = new ChooseOriginPanelVisual(wiz);
            component.addChangeListener(WeakListeners.change(this, component));
        }
        component.setDepKind(kind);
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid() {
        boolean result = kind != null && component != null && component.valid();
        return result;
    }

    public final void addChangeListener(ChangeListener l) {
        supp.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        supp.removeChangeListener(l);
    }

    public void readSettings(Map<String, Object> settings) {
        this.settings = settings;
        InitialDepKind k = (InitialDepKind) settings.get(ChooseDependencyKindWizardPanel.PROP_DEP_KIND);
        this.kind = k;
        if (component != null) {
            component.setDepKind(k);
        }
    }
    public void storeSettings(Map<String, Object> settings) {
        this.settings = settings;
        if (component != null) {
            File origin = component.getOriginFile();
            File sources = component.getSourceFile();
            if (origin != null) {
                settings.put(PROP_ORIGIN_FILE, origin);
            } else {
                settings.remove(PROP_ORIGIN_FILE);
            }
            if (sources != null) {
                settings.put(PROP_SOURCE_ROOT, sources);
            } else {
                settings.remove(PROP_SOURCE_ROOT);
            }
        }
    }

    public void stateChanged(ChangeEvent e) {
        supp.fireChange();
    }
    private static final String MANIFEST_APP_TYPE = "Application-Type"; //NOI18N

    public void validate() throws WizardValidationException {
        assert !EventQueue.isDispatchThread();
        //Here we do the heavy lifting.  Validate that the files are real and
        //correctly directories or files.  Then actually read the JAR files
        //and check manifests, or look up the projects and check the project
        //type
        assert component != null;
        assert kind != null;
        assert settings != null;
        File origin = component.getOriginFile();
        Project target = (Project) settings.get(AddDependencyWizardIterator.PROP_TARGET_PROJECT);
        assert target != null;
        ProjectKind pkind = target.getLookup().lookup(ProjectKind.class);
        if (origin == null || !origin.exists()) {
            throw new WizardValidationException(component, "Bad Jar file: " + origin, //NOI18N
                    NbBundle.getMessage(ChooseOriginWizardPanel.class,
                    "ERR_NON_EXISTENT_JAR_FILE", origin == null ? "\"\"" : origin.getName())); //NOI18N
        }
        boolean shouldBeFolder = kind == InitialDepKind.PROJECT;
        if (shouldBeFolder != origin.isDirectory()) {
            throw new WizardValidationException(component, "Bad Jar file: " + origin.getPath(), //NOI18N
                    NbBundle.getMessage(ChooseOriginWizardPanel.class, shouldBeFolder ? "ERR_NOT_FOLDER" : "ERR_NOT_FILE", //NOI18N
                    origin.getName()));
        }
        DependencyKind realKind = null;
        switch (kind) {
            case JAR_FILE:
                realKind = DependencyKind.RAW_JAR;
                try {
                    JarFile jar = new JarFile(origin);
                    try {
                        Manifest m = jar.getManifest();
                        if (m == null) {
                            throw new WizardValidationException(component,
                                    "No Application-Type entry in main section of manifest for " + //NOI18N
                                    origin.getAbsolutePath(), NbBundle.getMessage(ChooseOriginWizardPanel.class,
                                    "ERR_MISSING_MANIFEST", origin.getPath())); //NOI18N
                        }
                        String libType = m.getMainAttributes().getValue(MANIFEST_APP_TYPE);
                        if (libType == null) {
                            realKind = DependencyKind.RAW_JAR;
                        } else if ("classic-lib".equals(libType)) {
                            realKind = DependencyKind.CLASSIC_LIB_JAR;
                        } else if ("extension-lib".equals(libType)) {
                            if (pkind != null && pkind.isClassic()) {
                                throw new WizardValidationException(component,
                                        "Classic -> Extended dep not allowed: " + libType, NbBundle.getMessage(ChooseOriginWizardPanel.class, //NOI18N
                                        "ERR_CLASSIC_TO_EXT_DEPENDENCY")); //NOI18N
                                }
                            realKind = DependencyKind.EXTENSION_LIB_JAR;
                        }
                        //force further validation - try to trigger an IOE if
                        //JAR is corrupted
                        for (JarEntry e : NbCollections.iterable(jar.entries())) {
                            e.getName();
                        }
                    } catch (IOException ex) {
                        WizardValidationException e = new WizardValidationException(component, ex.getMessage(),
                                NbBundle.getMessage(ChooseOriginWizardPanel.class, "ERR_BAD_JAR_FILE")); //NOI18N
                        e.initCause(ex);
                        throw e;
                    } finally {
                        jar.close();
                    }
                } catch (IOException ex) {
                    WizardValidationException e = new WizardValidationException(component, ex.getMessage(),
                            NbBundle.getMessage(ChooseOriginWizardPanel.class, "ERR_BAD_JAR_FILE")); //NOI18N
                    e.initCause(ex);
                    throw e;
                }
                break;
            case PROJECT:
                realKind = DependencyKind.JAVA_PROJECT;
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(origin));
                Project p = FileOwnerQuery.getOwner(fo);
                if (p == null) {
                    throw new WizardValidationException(component, "not a project: " + origin.getAbsolutePath(), //NOI18N
                            NbBundle.getMessage(ChooseOriginWizardPanel.class, "ERR_NOT_PROJECT", origin.getName()));
                }
                if (!p.getProjectDirectory().equals(fo)) {
                    throw new WizardValidationException(component, "not a project root: " + origin.getAbsolutePath(), //NOI18N
                            NbBundle.getMessage(ChooseOriginWizardPanel.class, "ERR_NOT_PROJECT_ROOT", origin.getName())); //NOI18N
                }
                if (target.getProjectDirectory().equals(p.getProjectDirectory())) {
                    throw new WizardValidationException(component, "Adding a project to itself: " + target, //NOI18N
                            NbBundle.getMessage(ChooseOriginWizardPanel.class, "ERR_PROJECT_CANNOT_DEPEND_ON_SELF")); //NOI18N
                }
                //XXX check for circular dependencies if target is library project
                JCProject jp = p.getLookup().lookup(JCProject.class);
                if (jp != null) {
                    if (pkind != null && (pkind.isClassic() && !jp.kind().isClassic())) {
                        throw new WizardValidationException(component,
                                "Classic -> Extended dep not allowed", NbBundle.getMessage(ChooseOriginWizardPanel.class, //NOI18N
                                "ERR_CLASSIC_TO_EXT_DEPENDENCY")); //NOI18N
                    }
                    switch (jp.kind()) {
                        case CLASSIC_APPLET:
                        case EXTENDED_APPLET:
                        case WEB:
                            //Throw an exception - interdependencies are not allowed
                            //between application -> application, only application -> library or
                            //library -> library
                            ProjectInformation info = jp.getLookup().lookup(ProjectInformation.class);
                            String name = info == null ? origin.getName() : info.getDisplayName();
                            throw new WizardValidationException(component, "not a library project: " + origin.getAbsolutePath(), //NOI18N
                                    NbBundle.getMessage(ChooseOriginWizardPanel.class, "ERR_NOT_LIBRARY_PROJECT", name));
                        case CLASSIC_LIBRARY:
                            realKind = DependencyKind.CLASSIC_LIB;
                            break;
                        case EXTENSION_LIBRARY:
                            realKind = DependencyKind.EXTENSION_LIB;
                            break;
                        default:
                            throw new AssertionError();
                    }
                } else {
                    AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
                    if (prov == null) {
                        ProjectInformation info = p.getLookup().lookup(ProjectInformation.class);
                        String name = info == null ? origin.getName() : info.getDisplayName();
                        throw new WizardValidationException(component, "Not a java project" + origin.getAbsolutePath(), //NOI18N
                                NbBundle.getMessage(ChooseOriginWizardPanel.class, "ERR_NOT_JAVA_PROJECT", name)); //NOI18N
                    }
                }
        }
        assert realKind != null : "Kind not found"; //NOI18N
        settings.put(PROP_ACTUAL_DEP_KIND, realKind);
        switch (realKind) {
            case CLASSIC_LIB_JAR:
            case EXTENSION_LIB_JAR:
                settings.put(PROP_INTERMEDIATE_PANEL_KIND, IntermediatePanelKind.SIG_FILE);
                iter.setIntermediatePanelKind(IntermediatePanelKind.SIG_FILE);
                break;

            case JAVA_PROJECT:
            case RAW_JAR:
                settings.put(PROP_INTERMEDIATE_PANEL_KIND, IntermediatePanelKind.EXP_FILE);
                iter.setIntermediatePanelKind(IntermediatePanelKind.EXP_FILE);
                break;

            default:
                settings.remove(PROP_INTERMEDIATE_PANEL_KIND);
                iter.setIntermediatePanelKind(null);
        }
    }
    
    public void prepareValidation() {
        //do nothing
    }
}

