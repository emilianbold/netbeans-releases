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
package o.n.m.ruby.qaf;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewRubyProjectNameLocationStepOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.BuildGemAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lukas
 */
public abstract class RubyTestCase extends JellyTestCase {

    private static final Logger LOGGER = Logger.getLogger(RubyTestCase.class.getName());
    private Project project;
    private String projectName;
    private ProjectType projectType;

    /**
     * Enum type to hold project specific settings (like ie. project category
     * label, project template name, etc.)
     */
    protected enum ProjectType {

        RUBY,
        RAILS,
        RR_SAMPLE;

        /**
         * Get project template category name
         *
         * @return category name
         */
        public String getCategory() {
            switch (this) {
                case RUBY:
                case RAILS:
                    //Ruby
                    return Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.ui.wizards.Bundle", "Templates/Project/Ruby");
                case RR_SAMPLE:
                    //Samples
                    return Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Templates/Project/Samples");
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Get project template project type name
         *
         * @return project type name
         */
        public String getProjectTypeName() {
            switch (this) {
                case RUBY:
                    //Ruby Application
                    return Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.ui.wizards.Bundle", "Templates/Project/Ruby/emptyRuby.xml");
                case RAILS:
                    //Ruby on Rails Application
                    return Bundle.getStringTrimmed("org.netbeans.modules.ruby.railsprojects.ui.wizards.Bundle", "Templates/Project/Ruby/railsApp.xml");
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }
    }

    public RubyTestCase(String name) {
        super(name);
        setProjectName(getProjectName());
        setProjectType(getProjectType());
    }

    /**
     * Method responsible for checking and setting up environment for particular
     * test case, mainly for setting up project to be used by test case.<br/>
     *
     * Following logic is used for setting up a project (Note that
     * <code>getProjectName()</code> method is used for getting correct project
     * name):<br/>
     * <ol>
     *  <li>look for a project in <i>projects</i> directory in data directory
     *      and if project is found there then open it in the IDE
     *  </li>
     *  <li>look for a project in <code>getWorkDir().getParentFile().getParentFile()</code>
     *      or in <code>System.getProperty("java.io.tmpdir")</code> directory
     *      (if some parent file does not exist), if project is found then open it
     *      in the IDE</li>
     *  <li>if project is not found then it will be created from scratch</li>
     * </ol>
     *
     * @throws java.lang.Exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (!ProjectType.RR_SAMPLE.equals(getProjectType())) {
            System.out.println("########  TestCase: " + getName() + "  #######"); //NOI18N
            File projectRoot = new File(getDataDir(), "projects/" + getProjectName()); //NOI18N
            if (projectRoot.exists()) {
                project = (Project) ProjectSupport.openProject(new File(getDataDir(), "projects/" + getProjectName()));
            } else {
                projectRoot = new File(getProjectsRootDir(), projectName);
                LOGGER.info("Using project in: " + projectRoot.getAbsolutePath()); //NOI18N
                if (!projectRoot.exists()) {
                    project = createProject(projectName);
                    org.netbeans.junit.ide.ProjectSupport.waitScanFinished();
                } else {
                    ProjectSupport.openProject(projectRoot);
                    FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(projectRoot));
                    assertNotNull("FO cannot be null", fo); //NOI18N
                    project = ProjectManager.getDefault().findProject(fo);
                }
            }
            assertNotNull("Project cannot be null!", project); //NOI18N
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        project = null;
        projectName = null;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            //ignore
        }
    }

    /**
     * Get the name of the project to be used by test case
     *
     * @return name of the project
     */
    protected abstract String getProjectName();

    /**
     * Get a Project instance used by test case
     *
     * @return a Project instance
     */
    protected Project getProject() {
        return project;
    }

    /**
     * Get <code>Node</code> for the project used by test case
     *
     * @return an instance of <code>ProjectRootNode</code>
     */
    protected Node getProjectRootNode() {
        return new Node(ProjectsTabOperator.invoke().tree(), getProjectName());
    }

    /**
     * Project type set for test case, ProjectType.RUBY is used by default
     * Override this method to use different ProjectType
     *
     * @return ProjectType set for test case
     */
    protected ProjectType getProjectType() {
        return ProjectType.RUBY;
    }

    /**
     * Get the name of the sample project's category (ie. Ruby)
     *
     * @return name of the project
     */
    protected String getSamplesCategoryName() {
        return ""; //NOI18N
    }

    /**
     * Helper method to be used by subclasses to create new project according
     * to given parameters. Default server registered in the IDE will be used.
     *
     * @param name project or sample name
     * @param type project type
     * @param javaeeVersion server type, can be null
     * @return created project
     * @throws java.io.IOException
     */
    protected Project createProject(String name) throws IOException {
        // project category & type selection step
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.treeCategories().setComparator(new Operator.DefaultStringComparator(true, true));
        npwo.lstProjects().setComparator(new Operator.DefaultStringComparator(true, true));
        if (ProjectType.RR_SAMPLE.equals(getProjectType())) {
            npwo.selectCategory(getProjectType().getCategory() + "|" + getSamplesCategoryName()); //NOI18N
            npwo.selectProject(name);
            name = getProjectName();
        } else {
            npwo.selectCategory(getProjectType().getCategory());
            npwo.selectProject(getProjectType().getProjectTypeName());
        }
        npwo.next();
        // project name & location selection step
        NewRubyProjectNameLocationStepOperator op = new NewRubyProjectNameLocationStepOperator();
        op.txtProjectName().setText(name);
        if (ProjectType.RR_SAMPLE.equals(getProjectType())) {
            op.txtProjectLocation().setText(getWorkDirPath());
        } else {
            File projectLocation = null;
            projectLocation = getProjectsRootDir();
            op.txtProjectLocation().setText(projectLocation.getAbsolutePath());
        }
        LOGGER.info("Creating project in: " + op.txtProjectLocation().getText()); //NOI18N
        op.finish();
        // Opening Projects
        String openingProjectsTitle = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "LBL_Opening_Projects_Progress");
        waitDialogClosed(openingProjectsTitle);
        // wait project appear in projects view
        Node prjNode = new Node(ProjectsTabOperator.invoke().tree(), name);
        // wait classpath scanning finished
      //  org.netbeans.junit.ide.ProjectSupport.waitScanFinished();
        // get a project instance to return
        Project p = ((org.openide.nodes.Node) prjNode.getOpenideNode()).getLookup().lookup(Project.class);
        assertNotNull("Project instance has not been found", p); //NOI18N
        return p;
    }

    /**
     * Helper method to be used by subclasses to create new file of given
     * <code>fileType</code> from <i>Ruby</i> category
     *
     * @param p project where to create new file
     * @param fileType file type name from ruby category
     */
    protected void createNewRubyFile(Project p, String fileType) {
        // Ruby
        String rubyLabel = Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.ui.wizards.Bundle", "Templates/Project/Ruby");
        createNewFile(p, rubyLabel, fileType);
    }

    /**
     * Helper method to be used by subclasses to create new file of given
     * <code>fileType</code> from <code>fileCategory</code> category
     *
     * @param p project where to create new file
     * @param fileType file type name from ruby category
     */
    protected void createNewFile(Project p, String fileCategory, String fileType) {
        // file category & filetype selection step
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        new EventTool().waitNoEvent(500);
        nfwo.treeCategories().setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.lstFileTypes().setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.cboProject().selectItem(p.toString());
        nfwo.selectCategory(fileCategory);
        nfwo.selectFileType(fileType);
        nfwo.next();
    }

    /**
     * Wait until dialog with title <code>dialogTitle</code> is closed
     *
     * @param dialogTitle title of the dialog to be closed
     */
    protected void waitDialogClosed(String dialogTitle) {
        try {
            // wait at most 60 second until progress dialog dismiss
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 60000); //NOI18N
            new NbDialogOperator(dialogTitle).waitClosed();
        } catch (TimeoutExpiredException e) {
            // ignore when progress dialog was closed before we started to wait for it
        }
    }

    protected File getProjectsRootDir() throws IOException {
        File f = getWorkDir();
        LOGGER.fine("Working directory is set to: " + f.getAbsolutePath()); //NOI18N
        if (f != null) {
            f = f.getParentFile();
            if (f != null) {
                f = f.getParentFile();
            } else {
                return new File(System.getProperty("java.io.tmpdir")); //NOI18N
            }
        } else {
            return new File(System.getProperty("java.io.tmpdir")); //NOI18N
        }
        return f;
    }

    protected void buildProject() {
        OutputOperator oo = OutputOperator.invoke();
        new BuildGemAction().performPopup(getProjectRootNode());
        oo.getOutputTab(getProjectName()).waitText("BUILD SUCCESSFUL"); //NOI18N
    }

    private void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    private void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    static class ModuleTFFinder extends JComponentByLabelFinder {

        public ModuleTFFinder() {
            // In Module:
            super(Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.templates.Bundle", "LBL_RubyTargetChooserPanelGUI_InModuleName_Label"));
        }
    }

    static class TestedClassTFFinder extends JComponentByLabelFinder {

        public TestedClassTFFinder() {
            // Tested Class:
            super(Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.templates.Bundle", "LBL_RubyTargetChooserPanelGUI_Spec_Class"));
        }
    }

    static class LocationCBFinder extends JComponentByLabelFinder {

        public LocationCBFinder() {
            // Location:
            super(Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.templates.Bundle", "LBL_RubyTargetChooserPanelGUI_jLabel1"));
        }
    }

    static class JComponentByLabelFinder implements ComponentChooser {

        private String label;

        public JComponentByLabelFinder(String label) {
            if (label == null || "".equals(label.trim())) {
                throw new IllegalArgumentException(label + " is not valid label"); //NOI18N
            }
            this.label = label.trim();
        }

        public boolean checkComponent(Component c) {
            if (c instanceof JComponent) {
                JComponent jc = (JComponent) c;
                Object o = jc.getClientProperty("labeledBy"); //NOI18N
                if (o != null && o instanceof JLabel) {
                    JLabel lbl = (JLabel) o;
                    return label.equalsIgnoreCase(lbl.getText().trim());
                }
            }
            return false;
        }

        public String getDescription() {
            return "Find JComponent labeled by given label"; //NOI18N
        }
    }
}
