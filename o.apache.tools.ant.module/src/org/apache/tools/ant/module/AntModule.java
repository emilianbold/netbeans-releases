/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */

package org.apache.tools.ant.module;

import java.io.*;
import java.util.Enumeration;

import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;

// More or less copied from org.netbeans.modules.apisupport.APIModule, which does a similar thing.
// In the future, can be just a single layer entry...

public class AntModule extends ModuleInstall {

    private static final long serialVersionUID = -8877465721852434693L;

    private static final String PROP_INSTALL_COUNT = "installCount";
    
    public void installed () {
        restored ();
    }
    
    private static FileSystem createJarFS (File zipfile) throws Exception {
        JarFileSystem fs = new JarFileSystem ();
        fs.setJarFile (zipfile);
        fs.setHidden (true);
        FileSystemCapability capab = fs.getCapability ();
        if (capab instanceof FileSystemCapability.Bean) {
            FileSystemCapability.Bean bean = (FileSystemCapability.Bean) capab;
            bean.setCompile (false);
            bean.setExecute (false);
            bean.setDebug (false);
            bean.setDoc (true);
        } else {
            System.err.println ("Warning: JarFileSystem had strange capability: " + capab);
        }
        return fs;
    }

    public void restored () {
        // Mount docs, or remount if project was discarded:
        Integer count = (Integer) getProperty (PROP_INSTALL_COUNT);
        int icount = count == null ? 1 : count.intValue () + 1;
        putProperty (PROP_INSTALL_COUNT, new Integer (icount));
        // 1: first install (project is discarded anyway)
        // 2: first restore as actual user
        // 3: next restore (project settings incl. Repository loaded)
        if (icount <= 2) {
            final File f = findAPIDocs ();
            if (f != null) {
                try {
                    final FileSystem fs = createJarFS (f);
                    // Mount docs in Documentation Repository:
                    final Repository repo = TopManager.getDefault ().getRepository ();
                    final boolean[] skip = { false };
                    if (repo.findFileSystem (fs.getSystemName ()) == null) {
			repo.addFileSystem (fs);
			// Don't ask: bug/misdesign in Projects module necessitates this.
                        repo.addRepositoryListener (new RepositoryListener () {
                            public void fileSystemRemoved (RepositoryEvent ev) {
                                if (skip[0]) return;
                                if (ev.getFileSystem () == fs) {
                                    RequestProcessor.postRequest (new Runnable () {
                                            public void run () {
                                                if (repo.findFileSystem (fs.getSystemName ()) == null) {
                                                    try {
                                                        FileSystem fs2 = createJarFS (f);
                                                        repo.addFileSystem (fs2);
                                                        skip[0] = true;
                                                    } catch (Exception ioe) {
                                                        ioe.printStackTrace ();
                                                    }
                                                }
                                            }
                                        });
                                }
                            }
                            public void fileSystemAdded (RepositoryEvent ev) {
                            }
                            public void fileSystemPoolReordered (RepositoryReorderedEvent ev) {
                            }
                        });
                    } else {
                        System.err.println ("Note: ant-api.zip was already present in Repository.");
		    }
                } catch (Exception e) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                        e.printStackTrace ();
                }
            } else {
                System.err.println("Note: ant-api.zip not found to add to Javadoc, ignoring...");
            }
        }
    }

    public void uninstalled () {
        // Unmount docs (AutoUpdate should handle actually removing the file):
        File fo = findAPIDocs ();
        if (fo != null) {
            Repository repo = TopManager.getDefault ().getRepository ();
            Enumeration e = repo.fileSystems ();
            while (e.hasMoreElements ()) {
                Object o = e.nextElement ();
                if (o instanceof JarFileSystem) {
                    JarFileSystem jfs = (JarFileSystem) o;
                    try {
                        if (fo.getCanonicalPath ().equals (jfs.getJarFile ().getCanonicalPath ())) {
                            repo.removeFileSystem (jfs);
                            break;
                        }
                    } catch (IOException ioe) {
                        if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                            ioe.printStackTrace ();
                    }
                }
            }
        } else {
            System.err.println("Note: ant-api.zip not found to remove from Javadoc, ignoring...");
        }
    }

    static File findAPIDocs () {
        try {
            String suffix = "docs" + File.separator + "ant-api.zip";
            String user = System.getProperty ("netbeans.user");
            if (user != null) {
                File f = new File (user, suffix);
                if (f.exists ()) return f.getCanonicalFile ();
            }
            String home = System.getProperty ("netbeans.home");
            if (home != null) {
                File f = new File (home, suffix);
                if (f.exists ()) return f.getCanonicalFile ();
            }
        } catch (IOException ioe) {
            TopManager.getDefault ().notifyException (ioe);
        }
        return null;
    }

    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal (in);
        putProperty (PROP_INSTALL_COUNT, in.readObject ());
    }

    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal (out);
        out.writeObject (getProperty (PROP_INSTALL_COUNT));
    }

}
