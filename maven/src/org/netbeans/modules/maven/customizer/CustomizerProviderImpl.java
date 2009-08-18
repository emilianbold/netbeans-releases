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

package org.netbeans.modules.maven.customizer;

import org.netbeans.modules.maven.api.customizer.ModelHandle;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.ProjectProfileHandler;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.execute.UserActionGoalProvider;
import hidden.org.codehaus.plexus.util.IOUtil;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jdom.DefaultJDOMFactory;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.JDOMFactory;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.MavenProjectPropsImpl;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.profile.ProfilesModel;
import org.netbeans.modules.maven.model.profile.ProfilesModelFactory;
import org.netbeans.modules.maven.problems.ProblemReporterImpl;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * maven implementation of CustomizerProvider, handles the general workflow,
 *for panel creation depegates to M2CustomizerPanelProvider instances.
 * @author Milos Kleint 
 */
public class CustomizerProviderImpl implements CustomizerProvider {
    
    private final NbMavenProjectImpl project;
    private ModelHandle handle;
    
    public static final String PROFILES_SKELETON =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //NO18N
"<profilesXml xmlns=\"http://maven.apache.org/PROFILES/1.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +//NO18N
"  xsi:schemaLocation=\"http://maven.apache.org/PROFILES/1.0.0 http://maven.apache.org/xsd/profiles-1.0.0.xsd\">\n" +//NO18N
"</profilesXml>";//NO18N
    // a copy is in maven.model's Utilities.

    private static final String BROKEN_NBACTIONS = "BROKENNBACTIONS";  //NOI18N
    
    public CustomizerProviderImpl(NbMavenProjectImpl project) {
        this.project = project;
    }
    
    public void showCustomizer() {
        showCustomizer( null );
    }
    
    
    public void showCustomizer( String preselectedCategory ) {
        showCustomizer( preselectedCategory, null );
    }
    
    public void showCustomizer( String preselectedCategory, String preselectedSubCategory ) {
        project.getLookup().lookup(MavenProjectPropsImpl.class).startTransaction();
        try {
            init();
            OptionListener listener = new OptionListener();
            Lookup context = Lookups.fixed(new Object[] { project, handle});
            Dialog dialog = ProjectCustomizer.createCustomizerDialog("Projects/org-netbeans-modules-maven/Customizer", //NOI18N
                                             context, 
                                             preselectedCategory, listener, null );
            dialog.addWindowListener( listener );
            listener.setDialog(dialog);
            dialog.setTitle( MessageFormat.format(
                    org.openide.util.NbBundle.getMessage(CustomizerProviderImpl.class, "TIT_Project_Properties"),
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName() } ) );
            dialog.setModal(true);
            dialog.setVisible(true);
        } catch (FileNotFoundException ex) {
            if ("No pom file exists.".equals(ex.getMessage())) { //NOI18N
                //#157020
                return;
            }
            Logger.getLogger(CustomizerProviderImpl.class.getName()).log(Level.SEVERE, "Cannot show project customizer", ex);
        } catch (IOException ex) {
            Logger.getLogger(CustomizerProviderImpl.class.getName()).log(Level.SEVERE, "Cannot show project customizer", ex);
        } catch (XmlPullParserException ex) {
            Logger.getLogger(CustomizerProviderImpl.class.getName()).log(Level.SEVERE, "Cannot show project customizer", ex);
        } 
    }
    
    private void init() throws XmlPullParserException, IOException {
        FileObject pom = FileUtil.toFileObject(project.getPOMFile());
        if (pom == null || !pom.isValid()) {
            throw new FileNotFoundException("No pom file exists."); //NOI18N
        }
        ModelSource source = Utilities.createModelSource(pom);
        POMModel model = POMModelFactory.getDefault().getModel(source);
        FileObject profilesFO = project.getProjectDirectory().getFileObject("profiles.xml");
        if (profilesFO != null) {
            source = Utilities.createModelSource(profilesFO);
        } else {
            //the file doesn't exist. what now?
            File file = FileUtil.toFile(project.getProjectDirectory());
            file = new File(file, "profiles.xml"); //NOI18N
            source = Utilities.createModelSourceForMissingFile(file, true, PROFILES_SKELETON, "text/x-maven-profile+xml"); //NOI18N
        }
        ProfilesModel profilesModel = ProfilesModelFactory.getDefault().getModel(source);
        UserActionGoalProvider usr = project.getLookup().lookup(UserActionGoalProvider.class);
        Map<String, ActionToGoalMapping> mapps = new HashMap<String, ActionToGoalMapping>();
        NetbeansBuildActionXpp3Reader reader = new NetbeansBuildActionXpp3Reader();
        ActionToGoalMapping mapping = reader.read(new StringReader(usr.getRawMappingsAsString()));
        mapps.put(M2Configuration.DEFAULT, mapping);
        List<ModelHandle.Configuration> configs = new ArrayList<ModelHandle.Configuration>();
        ModelHandle.Configuration active = null;
        M2ConfigProvider provider = project.getLookup().lookup(M2ConfigProvider.class);
        M2Configuration act = provider.getActiveConfiguration();
        M2Configuration defconfig = provider.getDefaultConfig();
        mapps.put(defconfig.getId(), reader.read(new StringReader(defconfig.getRawMappingsAsString())));
        ModelHandle.Configuration c = ModelHandle.createDefaultConfiguration();
        configs.add(c);
        if (act.equals(defconfig)) {
            active = c;
        }

        for (M2Configuration config : provider.getSharedConfigurations()) {
            mapps.put(config.getId(), reader.read(new StringReader(config.getRawMappingsAsString())));
            c = ModelHandle.createCustomConfiguration(config.getId());
            c.setActivatedProfiles(config.getActivatedProfiles());
            c.setShared(true);
            configs.add(c);
            if (act.equals(config)) {
                active = c;
            }
        }
        for (M2Configuration config : provider.getNonSharedConfigurations()) {
            mapps.put(config.getId(), reader.read(new StringReader(config.getRawMappingsAsString())));
            c = ModelHandle.createCustomConfiguration(config.getId());
            c.setActivatedProfiles(config.getActivatedProfiles());
            c.setShared(false);
            configs.add(c);
            if (act.equals(config)) {
                active = c;
            }
        }
        for (M2Configuration config : provider.getProfileConfigurations()) {
            mapps.put(config.getId(), reader.read(new StringReader(config.getRawMappingsAsString())));
            c = ModelHandle.createProfileConfiguration(config.getId());
            configs.add(c);
            if (act.equals(config)) {
                active = c;
            }
        }
        if (active == null) { //#152706
            active = configs.get(0); //default if current not found..
        }

        handle = ACCESSOR.createHandle(model, profilesModel, 
                project.getOriginalMavenProject(), mapps, configs, active,
                project.getAuxProps());
    }
    
    public static ModelAccessor ACCESSOR = null;

    static {
        // invokes static initializer of ModelHandle.class
        // that will assign value to the ACCESSOR field above
        Class c = ModelHandle.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }    
    
    
    public static abstract class ModelAccessor {
        
        public abstract ModelHandle createHandle(POMModel model, ProfilesModel prof, MavenProject proj, Map<String, ActionToGoalMapping> mapp,
                List<ModelHandle.Configuration> configs, ModelHandle.Configuration active, MavenProjectPropsImpl auxProps);
        
    }
    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener {
        private Dialog dialog;
        
        OptionListener() {
        }
        
        void setDialog(Dialog dlg) {
            dialog = dlg;
        }
        
        // Listening to OK button ----------------------------------------------
        
        public void actionPerformed( ActionEvent e ) {
            if ( dialog != null ) {
                dialog.setVisible(false);
                dialog.dispose();
                try {
                    project.getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            project.getLookup().lookup(MavenProjectPropsImpl.class).commitTransaction();
                            writeAll(handle, project);
                        }
                    });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    //TODO error reporting on wrong model save
                }
            }
        }
        
        // Listening to window events ------------------------------------------
        
        @Override
        public void windowClosed( WindowEvent e) {
            //TODO where to put elsewhere?
            project.getLookup().lookup(MavenProjectPropsImpl.class).cancelTransaction();
            if (handle.getPOMModel().isIntransaction()) {
                handle.getPOMModel().rollbackTransaction();
            }
            assert !handle.getPOMModel().isIntransaction();
            if (handle.getProfileModel().isIntransaction()) {
                handle.getProfileModel().rollbackTransaction();
            }
            assert !handle.getProfileModel().isIntransaction();
        }
        
        @Override
        public void windowClosing(WindowEvent e) {
            if ( dialog != null ) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }

    }
    
    static interface SubCategoryProvider {
        public void showSubCategory(String name);
    }

   public static void writeAll(ModelHandle handle, NbMavenProjectImpl project) throws IOException {
        Utilities.saveChanges(handle.getPOMModel());
        if (handle.isModified(handle.getProfileModel())) {
            Utilities.saveChanges(handle.getProfileModel());
        } else {
            if (handle.getProfileModel().isIntransaction()) {
                handle.getProfileModel().rollbackTransaction();
            }
        }
        if (handle.isModified(handle.getActionMappings())) {
            writeNbActionsModel(project, handle.getActionMappings(), M2Configuration.getFileNameExt(M2Configuration.DEFAULT));
        }
        M2ConfigProvider prv = project.getLookup().lookup(M2ConfigProvider.class);

        if (handle.isModified(handle.getConfigurations())) {
            List<M2Configuration> shared = new ArrayList<M2Configuration>();
            List<M2Configuration> nonshared = new ArrayList<M2Configuration>();
            for (ModelHandle.Configuration mdlConf : handle.getConfigurations()) {
                if (!mdlConf.isDefault() && !mdlConf.isProfileBased()) {
                    M2Configuration c = new M2Configuration(mdlConf.getId(), project);
                    c.setActivatedProfiles(mdlConf.getActivatedProfiles());
                    if (mdlConf.isShared()) {
                        shared.add(c);
                    } else {
                        nonshared.add(c);
                    }
                }
            }
            prv.setConfigurations(shared, nonshared, true);
        }

        //TODO we need to set the configurations for the case of non profile configs
        String id = handle.getActiveConfiguration() != null ? handle.getActiveConfiguration().getId() : M2Configuration.DEFAULT;
        for (M2Configuration m2 : prv.getConfigurations()) {
            if (id.equals(m2.getId())) {
                prv.setActiveConfiguration(m2);
            }
        }
        //save action mappings for configurations..
        for (ModelHandle.Configuration c : handle.getConfigurations()) {
            if (handle.isModified(handle.getActionMappings(c))) {
                writeNbActionsModel(project, handle.getActionMappings(c), M2Configuration.getFileNameExt(c.getId()));
            }

        }
   }

    public static void writeNbActionsModel(final FileObject pomDir, final ActionToGoalMapping mapping, final String path) throws IOException {
        writeNbActionsModel(null, pomDir, mapping, path);
    }

    public static void writeNbActionsModel(final Project project, final ActionToGoalMapping mapping, final String path) throws IOException {
        writeNbActionsModel(project, project.getProjectDirectory(), mapping, path);
    }
    
    private static void writeNbActionsModel(final Project project, final FileObject pomDir, final ActionToGoalMapping mapping, final String path) throws IOException {
        pomDir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                JDOMFactory factory = new DefaultJDOMFactory();
                
                InputStream inStr = null;
                FileLock lock = null;
                OutputStreamWriter outStr = null;
                try {
                    Document doc;
                    FileObject fo = pomDir.getFileObject(path);
                    if (fo == null) {
                        fo = pomDir.createData(path);
                        doc = factory.document(factory.element("actions")); //NOI18N
                    } else {
                        //TODO..
                        inStr = fo.getInputStream();
                        SAXBuilder builder = new SAXBuilder();
                        doc = builder.build(inStr);
                        inStr.close();
                        inStr = null;
                    }
                    lock = fo.lock();
                    NetbeansBuildActionJDOMWriter writer = new NetbeansBuildActionJDOMWriter();
                    String encoding = mapping.getModelEncoding() != null ? mapping.getModelEncoding() : "UTF-8"; //NOI18N
                    outStr = new OutputStreamWriter(fo.getOutputStream(lock), encoding);
                    Format form = Format.getRawFormat().setEncoding(encoding);
                    form = form.setLineSeparator(System.getProperty("line.separator")); //NOI18N
                    @SuppressWarnings("unchecked")
                    List<NetbeansActionMapping> maps = mapping.getActions();
                    //no packaging elements make sense in nbactions files.
                    for (NetbeansActionMapping m : maps) {
                        m.setPackagings(null);
                    }
                    writer.write(mapping, doc, outStr, form);
                } catch (JDOMException exc){
                    //throw (IOException) new IOException("Cannot parse the nbactions.xml by JDOM.").initCause(exc); //NOI18N
                    ProblemReporterImpl impl = project != null ? project.getLookup().lookup(ProblemReporterImpl.class) : null;
                    if (impl != null && !impl.hasReportWithId(BROKEN_NBACTIONS)) {
                        ProblemReport rep = new ProblemReport(ProblemReport.SEVERITY_MEDIUM,
                                NbBundle.getMessage(CustomizerProviderImpl.class, "TXT_Problem_Broken_Actions"),
                                NbBundle.getMessage(CustomizerProviderImpl.class, "DESC_Problem_Broken_Actions", exc.getMessage()),
                                new OpenActions(pomDir.getFileObject(path)));
                        rep.setId(BROKEN_NBACTIONS);
                        impl.addReport(rep);
                    }
                    Logger.getLogger(CustomizerProviderImpl.class.getName()).log(Level.INFO, exc.getMessage(), exc);
                } finally {
                    IOUtil.close(inStr);
                    IOUtil.close(outStr);
                    if (lock != null) {
                        lock.releaseLock();
                    }
                    
                }
            }
        });
    }

    private static class OpenActions extends AbstractAction {

        private FileObject fo;

        OpenActions(FileObject file) {
            putValue(Action.NAME, NbBundle.getMessage(CustomizerProviderImpl.class, "TXT_OPEN_FILE"));
            fo = file;
        }


        public void actionPerformed(ActionEvent e) {
            if (fo != null) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    EditCookie edit = dobj.getCookie(EditCookie.class);
                    edit.edit();
                } catch (DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
}
