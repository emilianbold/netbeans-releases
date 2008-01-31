/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.php.project.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpProjectType;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.ProjectConfigProvider;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.netbeans.modules.php.rt.utils.PhpProjectSharedConstants;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public final class NewPhpProjectWizardIterator implements InstantiatingIterator {

    public static final String WIZARD_PANEL_ERROR_MESSAGE 
                                        = "WizardPanel_errorMessage";   // NOI18N
    private static final String LBL_WIZARD_STEPS_COUNT = "LBL_WizardStepsCount"; // NOI18N
    public static final String CONTENT_DATA = "WizardPanel_contentData"; // NOI18N
    public static final String SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; // NOI18N
    // steps
    public static final String STEP_PROJECT = "LBL_ProjectTitleName"; // NOI18N
    public static final String STEP_WEB_SERVER = "LBL_ProjectWebServer"; // NOI18N
    // properties
    public static final String PROJECT_DIR = "projdir"; // NOI18N
    public static final String NAME 
            = PhpProjectSharedConstants.PHP_PROJECT_NAME; // NOI18N
    public static final String SOURCE_ROOT = "sourceRoot"; // NOI18N
    public static final String SET_AS_MAIN = "setAsMain"; // NOI18N
    public static final String INDEX_FILE_NAME = "indexFileName"; // NOI18N
    public static final String INDEX_FILE_CREATE = "createIndexFile"; // NOI18N
    public static final String HOST = "host"; // NOI18N
    public static final String COMMAND_LINE = "command"; // NOI18N
    public static final String VERSION = "version"; // NOI18N
    public static final String SOURCE_ENCODING = PhpProject.SOURCE_ENCODING;
    
    // constants
    public static final String CURRENT_FOLDER_PATTERN = "."; // NOI18N
    
    /** invokes <code>this(NewPhpProjectWizardIterator.PROJECT_TYPE_NEW)<code>
     */
    public NewPhpProjectWizardIterator() {
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#initialize(org.openide.WizardDescriptor)
     */
    public void initialize(WizardDescriptor descriptor) {
        myDescriptor = descriptor;

        String[] steps = createSteps();
        for (int i = 0; i < myPanels.length; i++) {
            Component component = myPanels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = component.getName();
            }
            if (component instanceof JComponent) {
                JComponent jComp = (JComponent) component;
                jComp.putClientProperty(SELECTED_INDEX, i);
                jComp.putClientProperty(CONTENT_DATA, steps);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#instantiate()
     */
    public Set instantiate() throws IOException {
        Set<Object> resultSet = new HashSet<Object>(2);
        File dir = FileUtil.normalizeFile((File) getDescriptor().getProperty(PROJECT_DIR));
        String name = (String) getDescriptor().getProperty(NAME);

        AntProjectHelper helper = createProject(dir, name);

        // add Project to created objects set
        FileObject fileObject = FileUtil.toFileObject(dir);
        resultSet.add(fileObject);

        // add sources to created objects set
        File src = createSourceRoot(dir);
        FileObject sourceDir = FileUtil.toFileObject(src);
        if (sourceDir != null) {
            resultSet.add(sourceDir);
        }

        // add to created objects set
        Boolean createIndexFile = (Boolean) getDescriptor().
                getProperty(INDEX_FILE_CREATE);
        if (createIndexFile != null && createIndexFile.booleanValue()) {
            FileObject template = Templates.getTemplate(getDescriptor());
            DataObject dObject = createIndexFile(template, src);
            if (dObject != null) {
                resultSet.add(dObject.getPrimaryFile());
            }
        }

        // Returning set of FileObject of project diretory.
        // Project will be open and set as main
        return resultSet;
    }

    
    private File createSourceRoot(File projectDir) throws IOException{
        Object obj = getDescriptor().getProperty(SOURCE_ROOT);
        File src = null;
        if (obj != null && obj instanceof File) {
            src = (File) obj;
            if (isParentOf(projectDir, src) && !src.exists() ){
                createSourceFolder(src);
            }
        }
        return src;
    }
    
    private void createSourceFolder(File src) throws IOException{
        if (!src.mkdirs()) {
            throw new IOException("Can not create source folder."); // NOI18N
        }
    }
    
    private boolean isParentOf(File folder, File f){
        String folderPath = folder.getAbsolutePath();
        String filePath = f.getAbsolutePath();
        if (filePath.startsWith(folderPath) 
                && !filePath.equals(folderPath))
        {
            return true;
        }
        return false;
    }
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#uninitialize(org.openide.WizardDescriptor)
     */
    public void uninitialize(WizardDescriptor descriptor) {
        descriptor.putProperty(PROJECT_DIR, null);
        descriptor.putProperty(NAME, null);
        descriptor.putProperty(SOURCE_ROOT, null);
        descriptor.putProperty(SET_AS_MAIN, null);
        descriptor.putProperty(INDEX_FILE_NAME, null);
        descriptor.putProperty(INDEX_FILE_CREATE, null);
        descriptor.putProperty(HOST, null);
        descriptor.putProperty(COMMAND_LINE, null);
        descriptor.putProperty(VERSION, null);
        descriptor.putProperty(SOURCE_ENCODING, null);
        
        myPanels = null;
        myDescriptor = null;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#addChangeListener(javax.swing.event.ChangeListener)
     */
    public void addChangeListener(ChangeListener listener) {
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#current()
     */
    public Panel current() {
        return myPanels[myIndex];
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#hasNext()
     */
    public boolean hasNext() {
        return myIndex < myPanels.length - 1;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#hasPrevious()
     */
    public boolean hasPrevious() {
        return myIndex > 0;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#name()
     */
    public String name() {
        return MessageFormat.format(
                NbBundle.getBundle(NewPhpProjectWizardIterator.class).
                    getString(LBL_WIZARD_STEPS_COUNT), 
                new Object[]{(myIndex + 1) + "", (myPanels.length) + ""});
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#nextPanel()
     */
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        myIndex++;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#previousPanel()
     */
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        myIndex--;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#removeChangeListener(javax.swing.event.ChangeListener)
     */
    public void removeChangeListener(ChangeListener listener) {
    }

    private WizardDescriptor getDescriptor() {
        return myDescriptor;
    }

    /**
     * prepares name of index file to create it. 
     * Get's name using WizardDescriptor.getProperty(INDEX_FILE_NAME).
     * If name has the same extension as the one specified in parameter,
     * removes it.
     * 
     * @param plannedExt extension that file will have.
     * 
     * @returns index file name without extension
     */
    private String getIndexFileName(String plannedExt){
        String name = (String) getDescriptor().getProperty(INDEX_FILE_NAME);
        if (name == null || name.length() == 0) {
            return null;
        }
        int index = name.lastIndexOf("."+plannedExt);
        // if name doesn't contain only '.<extension>'
        if (index > 0) {
            return name.substring(0, index);
        }
        return name;
    }
    private DataObject createIndexFile(FileObject template, File sourceDir) 
            throws IOException 
    {
        String indexFileName = getIndexFileName(template.getExt());
        if (indexFileName == null) {
            return null;
        }
        
        
        DataFolder dataFolder 
                = DataFolder.findFolder(getSourceFileObject(sourceDir));
        DataObject dTemplate = DataObject.find(template);
        return dTemplate.createFromTemplate(dataFolder, indexFileName);
    }

    private FileObject getSourceFileObject(File sourceDir) throws IOException {
        if (!sourceDir.exists()){
            createSourceFolder(sourceDir);
        }
        return FileUtil.createData(sourceDir);
    }

    /**
     * The actual return array has only 1 element. So this method is not needed
     * actually. It exists for possible future change quantity of steps in wizard.
     */
    private String[] createSteps() {
        myPanels = new WizardDescriptor.Panel[]{
            new PhpProjectConfigurePanel(),
            new ProviderSpecificPanel()
        };
        String[] steps = new String[]{
            NbBundle.getBundle(NewPhpProjectWizardIterator.class).getString(STEP_PROJECT),
            NbBundle.getBundle(NewPhpProjectWizardIterator.class).getString(STEP_WEB_SERVER)
        };
        return steps;
    }

    private AntProjectHelper createProject(File dir, String name) throws IOException {
        refreshFileSystem(dir);
        if (!dir.mkdirs()) {
            throw new IOException("Can not create project folder."); // NOI18N
        }
        refreshFileSystem(dir);
        AntProjectHelper helper = ProjectGenerator.createProject(FileUtil.toFileObject(dir), PhpProjectType.TYPE);
        configure(helper, name);
        return helper;
    }


    private void configure(AntProjectHelper helper, String name) throws IOException {
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, NAME);
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        helper.putPrimaryConfigurationData(data, true);

        EditableProperties properties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

        /*
         * Set source roots.
         */
        configureSources(helper, properties);

        /*
         * Set Encoding
         */
        configureEncoding(properties);
        /*
         * Save provider and host properties
         */
        configureProvider(properties);
        /*
         * Set version.
         */
        // version selection was removed from wizard. It will be available in customizer
        //properties.setProperty(PhpProject.VERSION, getDescriptor().getProperty(VERSION).toString());
        //String defaultVersion = NbBundle.getBundle(NewPhpProjectWizardIterator.class).getString(DEFAULT_PHP_VERSION);
        //properties.setProperty(PhpProject.VERSION, defaultVersion);

        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, properties);


        properties = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, properties);


        /* delegate server specific configuration to provider */
        configureHost(helper);

        Project project = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        ProjectManager.getDefault().saveProject(project);
    }


    private void configureSources(AntProjectHelper helper, EditableProperties properties) {
        String sourcePath = null;
        
        Object obj = getDescriptor().getProperty(SOURCE_ROOT);
        Logger.getLogger(getClass().getName()).info(">>>>> SRC ROOT "+obj);
        if (obj != null && obj instanceof File) {
            File sourceDir = (File) obj;
            File projectDir = FileUtil.toFile( helper.getProjectDirectory() );

            sourcePath = getRelatedSourcePath(sourceDir, projectDir);
            // do not store anything if no reasonable src specified
        //} else {
        //    sourcePath = NbBundle.getBundle(NewPhpProjectWizardIterator.class)
        //            .getString(DEFAULT_SOURCE_ROOT_DIR);
        }
        properties.setProperty(PhpProject.SRC, sourcePath);
        
    }

    private void configureEncoding(EditableProperties properties) {
        String encoding = null;
        
        Object obj = getDescriptor().getProperty(SOURCE_ENCODING);
        if (obj != null && obj instanceof String){
            encoding = (String)encoding;
        }
        if (encoding == null){
            Charset enc = FileEncodingQuery.getDefaultEncoding();
            encoding = enc.name();
        }
        properties.setProperty(PhpProject.SOURCE_ENCODING, encoding);
    }

    private String getRelatedSourcePath(File sourceFolder, File projectFolder) {
        String sourcePath = sourceFolder.getAbsolutePath();
        String projectPath = projectFolder.getAbsolutePath();
        
        if (projectPath.equalsIgnoreCase(sourcePath)) {
            return CURRENT_FOLDER_PATTERN;
        } else if (sourcePath.startsWith(projectPath + File.separator)) {
            String name = sourcePath.substring(projectPath.length() + 1);
            return name;
        } else {
            return sourcePath;
        }
    }

    private void configureProvider(EditableProperties properties) {
        Object obj = getDescriptor().getProperty(HOST);
        // if host was selected
        if (obj != null) {
            if (obj instanceof HostHolder) {
                Host host = ((HostHolder) obj).getHost();
                WebServerProvider provider = host.getProvider();
                String provider_id = provider.getClass().getCanonicalName();
                properties.setProperty(PhpProject.PROVIDER_ID, provider_id);
                properties.setProperty(WebServerProvider.HOST_ID, host.getId());
            }
            // add CMD support to all projects
            /*
            else {
            obj = getDescriptor().getProperty( COMMAND_LINE );
            if ( obj instanceof String ) {
            properties.setProperty( PhpProject.COMMAND_PATH , (String) obj );
            }
            }
             */
        }
    }

    private void configureHost(AntProjectHelper helper) {
        Object obj = getDescriptor().getProperty(HOST);
        if (obj != null) {
            if (obj instanceof HostHolder) {
                Host host = ((HostHolder) obj).getHost();
                ProjectConfigProvider provider = host.getProvider().getProjectConfigProvider();
                provider.configureProject(helper, getDescriptor());
            }
        }
    }

    private static void refreshFileSystem(final File dir) throws FileStateInvalidException {
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(rootF);
        assert dirFO != null : "At least disk roots must be mounted! " + rootF; // NOI18N
        dirFO.getFileSystem().refresh(false);
    }

    private WizardDescriptor myDescriptor;

    private WizardDescriptor.Panel[] myPanels;

    private int myIndex;

}
