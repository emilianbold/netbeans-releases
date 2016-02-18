/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.makeproject.launchers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.ProjectMetadataFactory;
import org.netbeans.modules.cnd.utils.ui.UIGesturesSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Notification;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
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
        if (nbproject != null && nbproject.isValid()) {
            FileChangeListenerImpl fileChangeListener = new FileChangeListenerImpl(projectDir);
            nbproject.addRecursiveListener(fileChangeListener);
            LaunchersRegistryFactory.getInstance(projectDir).setPrivateLaucnhersListener(fileChangeListener);  //for debugging purposes only
        }
        reload(projectDir);
    }
    
    @Override
    public void write(FileObject projectDir) {
    }

    @Messages({
        "illegal.string=Illegal string in the file {0}.\n{1}"
    })
    private static void reload(FileObject projectDir) {
        LaunchersRegistry launchersRegistry = LaunchersRegistryFactory.getInstance(projectDir);
        Properties properties = new Properties();
        final FileObject nbProjectFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        if (nbProjectFolder == null || !nbProjectFolder.isValid()) {  // LaunchersRegistry shouldn't be updated in case the project has been deleted.
            return;
        }
        final FileObject publicLaunchers = nbProjectFolder.getFileObject(NAME);
        final FileObject privateNbFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
        final FileObject privateLaunchers;
        if (privateNbFolder != null && privateNbFolder.isValid()) {
            privateLaunchers = privateNbFolder.getFileObject(NAME);
        } else {
            privateLaunchers = null;
        }
        if (publicLaunchers != null && publicLaunchers.isValid()) {
            try (InputStream inputStream = publicLaunchers.getInputStream()) {
                properties.load(inputStream);
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            } catch (final IllegalArgumentException ex) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(
                                        Bundle.illegal_string(publicLaunchers.getPath(), ex.getMessage()), NotifyDescriptor.ERROR_MESSAGE));
                    }
                });
            }
        }
        if (privateLaunchers != null && privateLaunchers.isValid()) {
            try (InputStream inputStream = privateLaunchers.getInputStream()) {
                properties.load(inputStream);
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            } catch (final IllegalArgumentException ex) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(
                                        Bundle.illegal_string(privateLaunchers.getPath(), ex.getMessage()), NotifyDescriptor.ERROR_MESSAGE));
                    }
                });
            }
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
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            if (fe.getFile().getPath().endsWith(NAME)) {
                reload(projectDir);
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            if (fe.getFile().getPath().endsWith(NAME)) {
                reload(projectDir);
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (fe.getFile().getPath().endsWith(NAME)) {
                reload(projectDir);
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            if (fe.getFile().getPath().endsWith(NAME)) {
                reload(projectDir);
            }
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }
}
