/*
 * Utilities.java
 *
 * Created on September 24, 2002, 11:11 AM
 */

package gui;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import org.netbeans.jemmy.JemmyException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 *
 * @author  jb105785
 */
public class Utilities {
    
    /** Creates a new instance of Utilities */
    public Utilities() {
    }

    /** Mounts <userdir>/sampledir through API
     * @return absolute path of mounted dir
     */
    public static boolean mountSampledir() {
        String userdir = System.getProperty("netbeans.user"); // NOI18N
        String mountPoint = userdir+File.separator+"sampledir"; // NOI18N
        mountPoint = mountPoint.replace('\\', '/');
        FileSystem fs = Repository.getDefault().findFileSystem(mountPoint);
        if (fs == null) {
            try {
                LocalFileSystem lfs= new LocalFileSystem();
                lfs.setRootDirectory(new File(mountPoint));
                Repository.getDefault().addFileSystem(lfs);
                return true;
            } catch (IOException ioe) {
                throw new JemmyException("Mounting FS: "+mountPoint+" failed.", ioe);
            } catch (PropertyVetoException pve) {
                throw new JemmyException("Mounting FS: "+mountPoint+" failed.", pve);
            }
        }
        return true;
    }
    
    
    public static void delete(String file) {
        FileObject fileObject = Repository.getDefault().findResource(file);
        if (fileObject==null) return;
        try {
            DataObject.find(fileObject).delete();
        } catch (java.io.IOException e) {
        }
    }
}
