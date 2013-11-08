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

package org.netbeans.modules.maven.newproject.idenative;

import java.io.File;
import org.netbeans.modules.maven.newproject.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.validation.adapters.WizardDescriptorAdapter;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Parent;
import org.netbeans.modules.maven.model.pom.Properties;
import static org.netbeans.modules.maven.newproject.idenative.Bundle.LBL_CreateProjectStep2;
import static org.netbeans.modules.maven.newproject.idenative.Bundle.NameFormat;
import org.netbeans.modules.xml.xam.ModelSource;

import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 *
 *@author mkleint
 */
public abstract class IDENativeMavenWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor>, WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {
    private static final String SKELETON = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
        "    <modelVersion>4.0.0</modelVersion>\n" + "</project>";
    private static final long serialVersionUID = 1L;
    
    private transient int index;
    private transient List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    private transient WizardDescriptor wiz;

    private final AtomicBoolean hasNextCalled = new AtomicBoolean(); //#216236
    private final String titlename;
    private final String log;
    private final String packaging;

    public IDENativeMavenWizardIterator(String title, String log, String packaging) {
        this.titlename = title;
        this.log = log;
        this.packaging = packaging;
    }
    
    @Override
    public Set instantiate() throws IOException {
        throw new UnsupportedOperationException("Not supported."); 
    }
    
    @Override
    public Set<FileObject> instantiate (ProgressHandle handle) throws IOException {
        handle.start();
        try {
            handle.progress("Creating directory");
            ProjectInfo vi = new ProjectInfo((String) wiz.getProperty("groupId"), (String) wiz.getProperty("artifactId"), (String) wiz.getProperty("version"), (String) wiz.getProperty("package")); //NOI18N
            String[] splitlog = StringUtils.split(log, ":");
            ArchetypeWizardUtils.logUsage(splitlog[0], splitlog[1], splitlog[2]);
            File projFile = FileUtil.normalizeFile((File) wiz.getProperty(CommonProjectActions.PROJECT_PARENT_FOLDER)); // NOI18N
            projFile.mkdirs();
            handle.progress("Looking for parent project");
            List<ModelOperation<POMModel>> operations = new ArrayList<ModelOperation<POMModel>>();
            //TODO is FOQ too adventurous, maybe just ProjectManager.findProject on parent is better heuristics?
            Project parentProject = FileOwnerQuery.getOwner(org.openide.util.Utilities.toURI(projFile));
            MavenProject parent = null;
            if (parentProject != null) {
                NbMavenProject nbMavenParent = parentProject.getLookup().lookup(NbMavenProject.class);
                if (nbMavenParent != null && "pom".equals(nbMavenParent.getMavenProject().getPackaging())) {
                    parent = nbMavenParent.getMavenProject();
                }
            }
            operations.add(new BasicPropertiesOperation(vi, parent, projFile));

            operations.addAll(getOperations(new Context(projFile, parent, vi)));
            handle.progress("Writing pom.xml");
            ModelSource model = Utilities.createModelSourceForMissingFile(new File(projFile, "pom.xml"), true, SKELETON, "text/x-maven-pom+xml");
            Utilities.performPOMModelOperations(model, operations);

            if (parent != null) {
                handle.progress("Updating parent pom.xml");
                ModelSource pmodel = Utilities.createModelSource(parentProject.getProjectDirectory().getFileObject("pom.xml"));
                Utilities.performPOMModelOperations(pmodel, Collections.singletonList(new AddModuleToParentOperation(parentProject.getProjectDirectory(), projFile)));
            }
            
            handle.progress("Writing additional files");
            afterProjectCreatedActions(new Context(projFile, parent, vi), handle);
            handle.progress("Finishing...");
            return ArchetypeWizardUtils.openProjects(projFile, null);   
        } finally {
            handle.finish();
        }
    }
      
    protected abstract List<ModelOperation<POMModel>> getOperations(Context context);
    
    protected abstract void afterProjectCreatedActions(Context context, ProgressHandle handle);
        
    
    @Override
    @Messages("LBL_CreateProjectStep2=Name and Location")
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        if (titlename != null) {
            wiz.putProperty ("NewProjectWizard_Title", titlename); // NOI18N        
        }
        index = 0;
        ValidationGroup vg = ValidationGroup.create(new WizardDescriptorAdapter(wiz));
        panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        List<String> steps = new ArrayList<String>();
        
        panels.add(new BasicWizardPanel(vg, null, true, false)); //only download archetype (for additional props) when unknown archetype is used.
        steps.add(LBL_CreateProjectStep2());
        for (int i = 0; i < panels.size(); i++) {
            JComponent c = (JComponent) panels.get(i).getComponent();
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[0]));
      }
        
    }
    
    @Override
    public void uninitialize(WizardDescriptor wiz) {
//        wiz.putProperty(CommonProjectActions.PROJECT_PARENT_FOLDER, null); //NOI18N
        wiz.putProperty("name",null); //NOI18N
        this.wiz = null;
        panels = null;
    }
    
    @Messages({"# {0} - index", "# {1} - length", "NameFormat={0} of {1}"})
    public @Override String name() {
        return NameFormat(index + 1, panels.size());
    }
    
    @Override
    public boolean hasNext() {
        hasNextCalled.set(true);
        return hasNextImpl();        
    }
    
    private boolean hasNextImpl() {
        return index < panels.size() - 1;
    }
    
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }
    
    @Override
    public void nextPanel() {
        final boolean hnc = hasNextCalled.getAndSet(false);
        if (!hasNextImpl()) {
            throw new NoSuchElementException( //#216236
                    MessageFormat.format(
                    "index: {0}, panels: {1}, called has next: {2}",
                    index,
                    panels.size(),
                    hnc));
        }
        index++;
    }
    
    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels.get(index);
    }
    
    public @Override void addChangeListener(ChangeListener l) {}
    
    public @Override void removeChangeListener(ChangeListener l) {}

    private class BasicPropertiesOperation implements ModelOperation<POMModel> {
        private final @NullAllowed MavenProject parent;
        private final ProjectInfo vi;
        private final File projectDir;

        public BasicPropertiesOperation(ProjectInfo vi, MavenProject parent, File projectDir) {
            this.vi = vi;
            this.parent = parent;
            this.projectDir = projectDir;
        }

        @Override
        public void performOperation(POMModel model) {
            org.netbeans.modules.maven.model.pom.Project root = model.getProject();
            if (root != null) {
                if (parent != null) {
                    Parent parentpom = model.getFactory().createParent();
                    parentpom.setGroupId(parent.getGroupId());
                    parentpom.setArtifactId(parent.getArtifactId());
                    parentpom.setVersion(parent.getVersion());
                    File pfile = FileUtil.normalizeFile(parent.getFile());
                    String rel = FileUtilities.relativizeFile(projectDir, pfile);
                    if (rel != null) {
                        if ("..".equals(rel) || "../pom.xml".equals(rel)) {
                            
                        } else {
                            parentpom.setRelativePath(rel);
                        }
                    } else {
                        parentpom.setRelativePath("");
                    }
                    root.setPomParent(parentpom);
                    
                }
                if (parent == null || !vi.groupId.equals(parent.getGroupId())) {
                    root.setGroupId(vi.groupId);
                }
                root.setArtifactId(vi.artifactId);
                if (parent == null || !vi.version.equals(parent.getVersion())) {
                    root.setVersion(vi.version);
                }
                root.setPackaging(packaging);
                
                boolean setEncoding = true;
                if (parent != null) {
                    java.util.Properties parentprops = parent.getProperties();
                    if (parentprops != null && parentprops.containsKey("project.build.sourceEncoding")) {
                        setEncoding = false;
                    }
                }
                if (setEncoding) {
                    Properties props = root.getProperties();
                    if (props == null) {
                        props = model.getFactory().createProperties();
                        root.setProperties(props);
                    }
                    props.setProperty("project.build.sourceEncoding", "UTF-8");
                }
            }
        }
    }

    private static class AddModuleToParentOperation implements ModelOperation<POMModel> {
        private final String relPath;

        public AddModuleToParentOperation(FileObject projectDirectory, File projFile) {
            FileObject dir = FileUtil.toFileObject(projFile);
            relPath = FileUtil.getRelativePath(projectDirectory, dir);
        }

        @Override
        public void performOperation(POMModel model) {
            if (relPath != null && model.getProject() != null) {
                List<String> modules = model.getProject().getModules();
                if (modules == null || !modules.contains(relPath)) {
                    model.getProject().addModule(relPath);
                }
            }
        }
    }
    
    public final class Context {
        public final File projectDirectory;
        public final MavenProject parentMavenProject;
        public final ProjectInfo projectInfo;

        private Context(File projectDirectory, MavenProject parentMavenProject, ProjectInfo projectInfo) {
            this.projectDirectory = projectDirectory;
            this.parentMavenProject = parentMavenProject;
            this.projectInfo = projectInfo;
        }
        
    }

}
