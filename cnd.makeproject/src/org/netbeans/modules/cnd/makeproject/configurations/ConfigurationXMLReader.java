/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.configurations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLDocReader;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.xml.sax.Attributes;

/**
 * was: ConfigurationDescriptorHelper
 */
public class ConfigurationXMLReader extends XMLDocReader {

    private static int DEPRECATED_VERSIONS = 26;
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    private final FileObject projectDirectory;
    private final Project project;
    private final static RequestProcessor REQUEST_PROCESSOR = new RequestProcessor("ConfigurationXMLReader", 10);//NOI18N
    private static final boolean TRACE_LONG_LOADING = false;

    public ConfigurationXMLReader(Project project, FileObject projectDirectory) {
        this.project = project;
        this.projectDirectory = projectDirectory;
        // LATER configurationDescriptor = new
    }

    /*
     * was: readFromDisk
     */
    public MakeConfigurationDescriptor read(final String relativeOffset) throws IOException {
        final String tag;
        final FileObject xml;
        // Try first new style file
        FileObject fo = projectDirectory.getFileObject("nbproject/configurations.xml"); // NOI18N
        if (fo == null) {
            // then try old style file....
            tag = CommonConfigurationXMLCodec.PROJECT_DESCRIPTOR_ELEMENT;
            xml = projectDirectory.getFileObject("nbproject/projectDescriptor.xml"); // NOI18N
        } else {
            tag = CommonConfigurationXMLCodec.CONFIGURATION_DESCRIPTOR_ELEMENT;
            xml = fo;
        }

        if (xml == null) {
            displayErrorDialog();
            return null;
        }
        FileObject configurationBaseFO = (project == null) ? projectDirectory : RemoteFileUtil.getProjectSourceBaseFileObject(project);
        if (configurationBaseFO == null) {
            throw new FileNotFoundException("File does not exist"); //NOI18N
        }
        final MakeConfigurationDescriptor configurationDescriptor = new MakeConfigurationDescriptor(projectDirectory, configurationBaseFO);
        Task task = REQUEST_PROCESSOR.post(new NamedRunnable("Reading project configuraion") { //NOI18N

            protected 
            @Override
            void runImpl() {
                try {
                    if (TRACE_LONG_LOADING) {
                        try {
                            Thread.sleep(10000); // to emulate long reading for testing purpose
                        } catch (InterruptedException ex) {
                            ex.printStackTrace(System.err);
                        }
                    }
                    if (_read(relativeOffset, tag, xml, configurationDescriptor) == null) {
                        // TODO configurationDescriptor is broken
                        configurationDescriptor.setState(State.BROKEN);
                    }
                } catch (IOException ex) {
                    configurationDescriptor.setState(State.BROKEN);
                }
            }
        });
        configurationDescriptor.setInitTask(task);
        return configurationDescriptor;
    }

    private ConfigurationDescriptor _read(String relativeOffset,
            String tag, FileObject xml, final MakeConfigurationDescriptor configurationDescriptor) throws IOException {

        boolean success;

        XMLDecoder decoder = new ConfigurationXMLCodec(tag, projectDirectory, configurationDescriptor, relativeOffset);
        registerXMLDecoder(decoder);
        InputStream inputStream = null;
        try {
            inputStream = xml.getInputStream();
            success = read(inputStream, xml.getPath());
        } finally {
            deregisterXMLDecoder(decoder);
            if (inputStream != null) {
                inputStream.close();
            }
        }

        if (!success) {
            displayErrorDialog();
            return null;
        }


        //
        // Now for the auxiliary/private entry
        //

        xml = projectDirectory.getFileObject("nbproject/private/configurations.xml"); // NOI18N
        if (xml != null) {
            // Don't post an error.
            // It's OK to sometimes not have a private config
            XMLDecoder auxDecoder = new AuxConfigurationXMLCodec(tag, configurationDescriptor);
            registerXMLDecoder(auxDecoder);
            inputStream = null;
            try {
                inputStream = xml.getInputStream();
                success = read(inputStream, projectDirectory.getName());
            } finally {
                deregisterXMLDecoder(auxDecoder);
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            if (!success) {
                return null;
            }
        }

        // Read things from private/project.xml
        if (project != null) {
            int activeIndex = ((MakeProject) project).getActiveConfigurationIndexFromPrivateXML();
            if (activeIndex >= 0) {
                configurationDescriptor.getConfs().setActive(activeIndex);
            }
        }

        // Ensure all item configurations have been created (default are not stored in V >= 57)
        Item[] projectItems = configurationDescriptor.getProjectItems();
        for (Configuration configuration : configurationDescriptor.getConfs().getConfigurations()) {
            for (Item item : projectItems) {
                if (item.getItemConfiguration(configuration) == null) {
                    configuration.addAuxObject(new ItemConfiguration(configuration, item));
                }
            }
        }

        attachListeners(configurationDescriptor);
        configurationDescriptor.setState(State.READY);

        // Some samples are generated without generated makefile. Don't mark these 'not modified'. Then
        // the makefiles will be generated before the project is being built
        boolean isMakefileProject = false;
        for (Configuration configuration : configurationDescriptor.getConfs().getConfigurations()) {
            MakeConfiguration makeConfiguration = (MakeConfiguration) configuration;
            if (makeConfiguration.isMakefileConfiguration()) {
                isMakefileProject = true;
                break;
            }
        }
        FileObject makeImpl = projectDirectory.getFileObject("nbproject/Makefile-impl.mk"); // NOI18N
        configurationDescriptor.setModified((!isMakefileProject && makeImpl == null) || relativeOffset != null);

        // Check version and display deprecation warning if too old
        if (configurationDescriptor.getVersion() >= 0 && configurationDescriptor.getVersion() <= DEPRECATED_VERSIONS) {
            final String message = NbBundle.getMessage(ConfigurationXMLReader.class, "OLD_VERSION_WARNING", projectDirectory.getPath()); // NOI18N
            if (CndUtils.isStandalone()) {
                System.err.print(message);
                System.err.println(NbBundle.getMessage(ConfigurationXMLReader.class, "OLD_VERSION_WARNING_AUTO"));
                configurationDescriptor.setModified();
            } else {
                Runnable warning = new Runnable() {

                    @Override
                    public void run() {
                        NotifyDescriptor nd = new NotifyDescriptor(message,
                                NbBundle.getMessage(ConfigurationXMLReader.class, "CONVERT_DIALOG_TITLE"), NotifyDescriptor.YES_NO_OPTION, // NOI18N
                                NotifyDescriptor.QUESTION_MESSAGE,
                                null, NotifyDescriptor.YES_OPTION);
                        Object ret = DialogDisplayer.getDefault().notify(nd);
                        if (ret == NotifyDescriptor.YES_OPTION) {
                            configurationDescriptor.setModified();
                        }
                    }
                };
                SwingUtilities.invokeLater(warning);
            }
        }

        if (configurationDescriptor.isModified()) {
            // Project is modified and will be saved with current version. This includes samples.
            configurationDescriptor.setVersion(CommonConfigurationXMLCodec.CURRENT_VERSION);
        }

        ConfigurationDescriptorProvider.recordMetrics(ConfigurationDescriptorProvider.USG_PROJECT_OPEN_CND, configurationDescriptor);
        return configurationDescriptor;
    }

    private void displayErrorDialog() {
        //String errormsg = NbBundle.getMessage(ConfigurationXMLReader.class, "CANTREADDESCRIPTOR", projectDirectory.getName());
        //DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
    }

    // Attach listeners to all disk folders
    private void attachListeners(final MakeConfigurationDescriptor configurationDescriptor) {
        Task task = REQUEST_PROCESSOR.post(new Runnable() {

            @Override
            public void run() {
                long time = System.currentTimeMillis();
                LOGGER.log(Level.FINE, "Start attach folder listeners");
                String oldName = Thread.currentThread().getName();
                try {
                    //boolean currentState = configurationDescriptor.getModified();
                    Thread.currentThread().setName("Attach listeners to all disk folders"); // NOI18N
                    List<Folder> firstLevelFolders = configurationDescriptor.getLogicalFolders().getFolders();
                    for (Folder f : firstLevelFolders) {
                        if (f.isDiskFolder()) {
                            // need to set modified descriptor for store new/deleted items in the folder
                            f.refreshDiskFolder(true);
                            f.attachListeners();
                        }
                    }
                    //configurationDescriptor.setModified(currentState);
                    LOGGER.log(Level.FINE, "End attach folder listeners, time {0}ms.", (System.currentTimeMillis() - time));
                } finally {
                    // restore thread name - it might belong to the pool
                    Thread.currentThread().setName(oldName);
                }
            }
        });
        // Refresh disk folders in background process
        // revert changes bacause opening project time is increased.
        //task.waitFinished(); // See IZ https://netbeans.org/bugzilla/show_bug.cgi?id=184260
    }

    // interface XMLDecoder
    @Override
    protected String tag() {
        return null;
    }

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) {
    }

    // interface XMLDecoder
    @Override
    public void end() {
    }

    // interface XMLDecoder
    @Override
    public void startElement(String name, Attributes atts) {
    }

    // interface XMLDecoder
    @Override
    public void endElement(String name, String currentText) {
    }
}
