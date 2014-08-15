/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.makeproject.launchers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.ProjectMetadataFactory;
import org.netbeans.modules.cnd.utils.ui.UIGesturesSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mtishkov
 */
@ServiceProvider(service = ProjectMetadataFactory.class, path = "Projects/org-netbeans-modules-cnd-makeproject/" + ProjectMetadataFactory.LAYER_PATH, position = 100)
public class LaunchersProjectMetadataFactory implements ProjectMetadataFactory {

    public static final String NAME = "launcher.properties"; //NOI18N
    private static final String USG_CND_LAUNCHERS = "USG_CND_LAUNCHERS"; //NOI18N

    @Override
    public void read(FileObject projectDir) {
        if (projectDir == null || !projectDir.isValid()) {
            //there could be the situation when nbproject is invalid, for example when
            //project is remote and it was opened, no cache exists and opening IDE no connection is established
            return;
        }
        FileObject nbproject = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        FileChangeListenerImpl fileChangeListener = new FileChangeListenerImpl(projectDir);
        if (nbproject != null && nbproject.isValid()) {
            nbproject.addFileChangeListener(fileChangeListener);
        }
        initListeners(fileChangeListener, projectDir);
        reload(projectDir);


    }
    
    private void initListeners(FileChangeListener fileChangeListener, FileObject projectDir) {
        FileObject nbproject = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        FileObject publicLaunchers = nbproject.getFileObject(NAME);
        if (publicLaunchers != null) {
            publicLaunchers.removeFileChangeListener(fileChangeListener);
            publicLaunchers.addFileChangeListener(fileChangeListener);
        }        
        final FileObject privateNbFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
        if (privateNbFolder != null && privateNbFolder.isValid()) {
            privateNbFolder.removeFileChangeListener(fileChangeListener);
            privateNbFolder.addFileChangeListener(fileChangeListener);
            FileObject privateLaunchers = privateNbFolder.getFileObject(NAME);
            if (privateLaunchers != null) {
                privateLaunchers.removeFileChangeListener(fileChangeListener);
                privateLaunchers.addFileChangeListener(fileChangeListener);
                LaunchersRegistryFactory.getInstance(projectDir).setPrivateLaucnhersListener(fileChangeListener);  //for debugging purposes only
            }
        }
    }

    @Override
    public void write(FileObject projectDir) {
    }

    private static void reload(FileObject projectDir) {
        LaunchersRegistry launchersRegistry = LaunchersRegistryFactory.getInstance(projectDir);
        Properties properties = new Properties();
        final FileObject nbProjectFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        if (nbProjectFolder == null || !nbProjectFolder.isValid()) {  // LaunchersRegistry shouldn't be updated in case the project has been deleted.
            return;
        }
        FileObject publicLaunchers = nbProjectFolder.getFileObject(NAME);
        final FileObject privateNbFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
        FileObject privateLaunchers = null;
        if (privateNbFolder != null && privateNbFolder.isValid()) {
            privateLaunchers = privateNbFolder.getFileObject(NAME);
        }
        try {
            if (publicLaunchers != null && publicLaunchers.isValid()) {
                final InputStream inputStream = publicLaunchers.getInputStream();
                properties.load(inputStream);
                inputStream.close();
            }
            if (privateLaunchers != null && privateLaunchers.isValid()) {
                final InputStream inputStream = privateLaunchers.getInputStream();
                properties.load(inputStream);
                inputStream.close();
            }
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        }
        if (launchersRegistry.load(properties)) {
            UIGesturesSupport.submit(USG_CND_LAUNCHERS, launchersRegistry.getLaunchers().size());
        }
    }

    private class FileChangeListenerImpl implements FileChangeListener {

        private final FileObject projectDir;

        public FileChangeListenerImpl(FileObject projectDir) {
            this.projectDir = projectDir;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            //private folder could be created here, in this case: let's listen 
            initListeners(this, projectDir);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            //listen for the new file: attach listener to the file to listen for changes
            initListeners(this, projectDir);
            reload(projectDir);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            reload(projectDir);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            fe.getFile().removeFileChangeListener(this);
            reload(projectDir);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reload(projectDir);
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }
}
