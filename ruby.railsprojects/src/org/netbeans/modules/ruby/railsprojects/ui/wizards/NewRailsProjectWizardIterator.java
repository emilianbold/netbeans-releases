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

package org.netbeans.modules.ruby.railsprojects.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.railsprojects.RailsProjectCreateData;
import org.netbeans.modules.ruby.railsprojects.RailsProjectGenerator;
import org.netbeans.modules.ruby.railsprojects.database.RailsDatabaseConnection;
import org.netbeans.modules.ruby.railsprojects.server.RubyServer;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Ruby project.
 *
 * TODO: Disable when no valid Rails install
 */
public class NewRailsProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    /** Wizard descriptor name for using jdbc as database access means */
    static final String JDBC_WN = "useJdbc"; // NOI18N
    /** Wizard descriptor name for the target Rails database */
    static final String RAILS_DB_WN = "railsDatabase"; // NOI18N
    static final String RAILS_DEVELOPMENT_DB = "railsDatabase.development"; // NOI18N
    static final String RAILS_PRODUCTION_DB = "railsDatabase.production"; // NOI18N
    static final String RAILS_TEST_DB = "railsDatabase.test"; // NOI18N
    /** Wizard descriptor name for including Goldspike for WAR deployment */
    static final String GOLDSPIKE_WN = "goldspike"; // NOI18N
    /** Wizard descriptor name for the target Rails server */
    static final String SERVER_INSTANCE = "serverInstance"; //NOI18N
    /** Wizard descriptor name for the Ruby platform */
    static final String PLATFORM = "platform"; //NOI18N
    
    static final int TYPE_APP = 0;
    //static final int TYPE_LIB = 1;
    static final int TYPE_EXT = 2;

    static final String PROP_NAME_INDEX = "nameIndex";      //NOI18N

    private static final long serialVersionUID = 1L;
    
    private int type;

    public NewRailsProjectWizardIterator() {
        this(TYPE_APP);
    }
    
    public NewRailsProjectWizardIterator(int type) {
        this.type = type;
    }

    public static NewRailsProjectWizardIterator existing () {
        return new NewRailsProjectWizardIterator(TYPE_EXT);
    }
    
    private WizardDescriptor.Panel[] createPanels () {
        if (type == TYPE_APP) {
            return new WizardDescriptor.Panel[] {
                    new PanelConfigureProject(this.type),
                    new DatabaseConfigPanel(),
                    new RailsInstallationPanel.Panel()
                };
        } else {
            // No "Configure Rails" panel for create-from-existing: you probably already have
            // Rails (and the Rails-detection would need to also check for vendor-frozen Rails,
            // not just Rails in the gem distro
            return new WizardDescriptor.Panel[] { new PanelConfigureProject(this.type) };
        }
    }
    
    private String[] createSteps() {
        String config = NbBundle.getMessage(NewRailsProjectWizardIterator.class,"LAB_ConfigureProject");
        String database = NbBundle.getMessage(NewRailsProjectWizardIterator.class,"LAB_ConfigureProject");
        String rails = NbBundle.getMessage(NewRailsProjectWizardIterator.class,"LAB_InstallRails");
        if (type == TYPE_APP) {
            return new String[] { config, database, rails };
        } else {
            return new String[] { config };
        }
    }
    
    
    public Set/*<FileObject>*/ instantiate () throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }
        
    public Set/*<FileObject>*/ instantiate (ProgressHandle handle) throws IOException {
        handle.start (4);
        //handle.progress (NbBundle.getMessage (NewRailsProjectWizardIterator.class, "LBL_NewRailsProjectWizardIterator_WizardProgress_ReadingProperties"));
        Set resultSet = new HashSet ();
        File dirF = (File)wiz.getProperty("projdir");        //NOI18N
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        String name = (String)wiz.getProperty("name");        //NOI18N
        //String mainClass = (String)wiz.getProperty("mainClass");        //NOI18N
        handle.progress (NbBundle.getMessage (NewRailsProjectWizardIterator.class, "LBL_NewRailsProjectWizardIterator_WizardProgress_CreatingProject"), 1);

        RakeProjectHelper h = null;
        
//        String database = (String) wiz.getProperty(RAILS_DB_WN); // NOI18N
        Boolean jdbc = (Boolean) wiz.getProperty(JDBC_WN); // NOI18N
        Boolean deploy = (Boolean) wiz.getProperty(GOLDSPIKE_WN); // NOI18N
        RubyServer server = (RubyServer) wiz.getProperty(SERVER_INSTANCE); // NOI18N

        RubyPlatform platform = (RubyPlatform) wiz.getProperty("platform"); // NOI18N
        RailsDatabaseConnection database = (RailsDatabaseConnection) wiz.getProperty(RAILS_DEVELOPMENT_DB);
        RailsProjectCreateData data = new RailsProjectCreateData(platform, dirF, name, type == TYPE_APP,
                database, false, deploy, server.getServerUri());
        h = RailsProjectGenerator.createProject(data);
        handle.progress(2);

//        if (mainClass != null && mainClass.length () > 0) {
//            try {
//                //String sourceRoot = "src"; //(String)j2seProperties.get (RailsProjectProperties.SRC_DIR);
//                FileObject sourcesRoot = h.getProjectDirectory ().getFileObject ("src");        //NOI18N
//                FileObject mainClassFo = getMainClassFO (sourcesRoot, mainClass);
//                assert mainClassFo != null : "sourcesRoot: " + sourcesRoot + ", mainClass: " + mainClass;        //NOI18N
//                // Returning FileObject of main class, will be called its preferred action
//                resultSet.add (mainClassFo);
//            } catch (Exception x) {
//                ErrorManager.getDefault().notify(x);
//            }
//        }
        FileObject dir = FileUtil.toFileObject(dirF);
//        if (type == TYPE_APP || type == TYPE_EXT) {
//            createManifest(dir);
//        }
        handle.progress (3);

        // TODO - check for Java DB and if so configure it

        // Returning FileObject of project diretory. 
        // Project will be open and set as main
//        Integer index = (Integer) wiz.getProperty(PROP_NAME_INDEX);
//        FoldersListSettings.getDefault().setNewApplicationCount(index.intValue());
        resultSet.add (dir);
        handle.progress (NbBundle.getMessage (NewRailsProjectWizardIterator.class, "LBL_NewRailsProjectWizardIterator_WizardProgress_PreparingToOpen"), 4);
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);    
        }
        
        // Open README.rails in the JRuby distribution
        // (unless we're creating a rails app from existing sources - those probably don't need configuration steps)
        //if ((type == TYPE_APP) && (RubyInstallation.getInstance().isJRubySet())) {
        //    String jrubyHome = RubyInstallation.getInstance().getJRubyHome();
        //    if (jrubyHome != null) {
        //        File railsFile = new File(jrubyHome + File.separator + "docs" + File.separator + "README.rails");
        //        FileObject fo = FileUtil.toFileObject(railsFile);
        //        if (fo != null) {
        //            // Open
        //            try {
        //                DataObject dobj = DataObject.find(fo);
        //                EditorCookie cookie = dobj.getCookie(EditorCookie.class);
        //                if (cookie != null) {
        //                    cookie.open();
        //                }
        //            } catch (DataObjectNotFoundException ex) {
        //                ErrorManager.getDefault().notify(ex);
        //            }
        //        }
        //    }
        //}

        // Open database.yml so the user can edit the database
        // (unless we're creating a rails app from existing sources - those probably don't need configuration steps)
        if (type == TYPE_APP) {
            FileObject fo = dir.getFileObject("config/database.yml"); // NOI18N
            if (fo != null) {
                // Open
                try {
                    DataObject dobj = DataObject.find(fo);
                    EditorCookie cookie = dobj.getCookie(EditorCookie.class);
                    if (cookie != null) {
                        cookie.open();
                    }
                } catch (DataObjectNotFoundException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
        
        return resultSet;
    }
    
        
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
        //set the default values of the sourceRoot and the testRoot properties
        this.wiz.putProperty("sourceRoot", new File[0]);    //NOI18N
        this.wiz.putProperty("testRoot", new File[0]);      //NOI18N
    }

    public void uninitialize(WizardDescriptor wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty("projdir",null);           //NOI18N
            this.wiz.putProperty("name",null);          //NOI18N
            this.wiz.putProperty("mainClass",null);         //NOI18N
            this.wiz.putProperty("platform", null);         //NOI18N
            this.wiz = null;
            panels = null;
        }
    }
    
    public String name() {
        return MessageFormat.format (NbBundle.getMessage(NewRailsProjectWizardIterator.class,"LAB_IteratorName"),
            new Object[] {new Integer (index + 1), new Integer (panels.length) });                                
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    // helper methods, finds mainclass's FileObject
    private FileObject getMainClassFO (FileObject sourcesRoot, String mainClass) {
        // replace '.' with '/'
//        mainClass = mainClass.replace ('.', '/'); // NOI18N
//        
//        // ignore unvalid mainClass ???
//        
//        return sourcesRoot.getFileObject (mainClass+ ".java"); // NOI18N
        return sourcesRoot.getFileObject(mainClass);        
    }

    static String getPackageName (String displayName) {
        StringBuffer builder = new StringBuffer ();
        boolean firstLetter = true;
        for (int i=0; i< displayName.length(); i++) {
            char c = displayName.charAt(i);            
            if ((!firstLetter && Character.isJavaIdentifierPart (c)) || (firstLetter && Character.isJavaIdentifierStart(c))) {
                firstLetter = false;
                if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                }                    
                builder.append(c);
            }            
        }
        return builder.length() == 0 ? NbBundle.getMessage(NewRailsProjectWizardIterator.class,"TXT_DefaultPackageName") : builder.toString();
    }
    
//    /**
//     * Create a new application manifest file with minimal initial contents.
//     * @param dir the directory to create it in
//     * @throws IOException in case of problems
//     */
//    private static void createManifest(final FileObject dir) throws IOException {
//        FileObject manifest = dir.createData(MANIFEST_FILE);
//        FileLock lock = manifest.lock();
//        try {
//            OutputStream os = manifest.getOutputStream(lock);
//            try {
//                PrintWriter pw = new PrintWriter(os);
//                pw.println("Manifest-Version: 1.0"); // NOI18N
//                pw.println("X-COMMENT: Main-Class will be added automatically by build"); // NOI18N
//                pw.println(); // safest to end in \n\n due to JRE parsing bug
//                pw.flush();
//            } finally {
//                os.close();
//            }
//        } finally {
//            lock.releaseLock();
//        }
//    }

}
