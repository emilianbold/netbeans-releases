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

import java.awt.Image;
import java.beans.*;
import java.io.*;
import java.util.Enumeration;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;

import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.loader.AntProjectCookieEditor;
import org.apache.tools.ant.module.xml.AntProjectSupport;

// More or less copied from org.netbeans.modules.apisupport.APIModule, which does a similar thing.
// In the future, can be just a single layer entry...

public class AntModule extends ModuleInstall {

    private static final long serialVersionUID = -8877465721852434693L;

    private static final String PROP_INSTALL_COUNT = "installCount"; // NOI18N
    
    public static final ErrorManager err = TopManager.getDefault ().getErrorManager ().getInstance ("org.apache.tools.ant.module"); // NOI18N
    
    public void installed () {
        restored ();
    }
    
    private static FileSystem createJarFS (File zipfile) throws Exception {
        JarFileSystem fs = new GlobalJarFileSystem ();
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
            err.log (ErrorManager.INFORMATIONAL, "Warning: JarFileSystem had strange capability: " + capab);
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
                    if (repo.findFileSystem (fs.getSystemName ()) == null) {
			repo.addFileSystem (fs);
                    } else {
                        err.log (ErrorManager.INFORMATIONAL, "Note: ant-api.zip was already present in Repository.");
		    }
                } catch (Exception e) {
                    err.notify (ErrorManager.INFORMATIONAL, e);
                }
            } else {
                err.log (ErrorManager.INFORMATIONAL, "Note: ant-api.zip not found to add to Javadoc, ignoring...");
            }
        }
        // [PENDING] use Class.forName(...,false,...)
        PropertyEditorManager.registerEditor (AntProjectCookie.class, AntProjectCookieEditor.class);
        // [PENDING] do this more lazily...
        AntProjectSupport.startFiringProcessor ();
    }

    public void uninstalled () {
        // Unmount docs (AutoUpdate should handle actually removing the file):
        Repository repo = TopManager.getDefault ().getRepository ();
        Enumeration e = repo.fileSystems ();
        while (e.hasMoreElements ()) {
            Object o = e.nextElement ();
            if (o instanceof GlobalJarFileSystem) {
                repo.removeFileSystem ((FileSystem) o);
            }
        }
        AntProjectSupport.stopFiringProcessor ();
    }
    
    public void close () {
        AntProjectSupport.stopFiringProcessor ();
    }

    static File findAPIDocs () {
        try {
            String suffix = "docs" + File.separator + "ant-api.zip"; // NOI18N
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
            err.notify (ErrorManager.INFORMATIONAL, ioe);
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
    
    /** Exists only for the sake of its bean info. */
    public static final class GlobalJarFileSystem extends JarFileSystem {
        private static final long serialVersionUID = -2165058869503900139L;
    }
    /** Marks it as global (not project-specific). */
    public static final class GlobalJarFileSystemBeanInfo extends SimpleBeanInfo {
        public BeanDescriptor getBeanDescriptor () {
            BeanDescriptor bd = new BeanDescriptor (GlobalJarFileSystem.class);
            bd.setValue ("global", Boolean.TRUE); // NOI18N
            return bd;
        }
        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (JarFileSystem.class) };
            } catch (IntrospectionException ie) {
                err.notify (ie);
                return null;
            }
        }
        public Image getIcon (int kind) {
            try {
                return Introspector.getBeanInfo (JarFileSystem.class).getIcon (kind);
            } catch (IntrospectionException ie) {
                err.notify (ie);
                return null;
            }
        }
    }

}
