/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */

package org.apache.tools.ant.module;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

import org.openide.ErrorManager;
import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;

import org.apache.tools.ant.module.run.OutputWriterOutputStream;
import org.apache.tools.ant.module.xml.AntProjectSupport;

public class AntModule extends ModuleInstall {

    private static final long serialVersionUID = -8877465721852434693L;

    public static final ErrorManager err = TopManager.getDefault ().getErrorManager ().getInstance ("org.apache.tools.ant.module"); // NOI18N
    
    /*
    public void restored () {
        AntProjectSupport.startFiringProcessor ();
    }
     */

    public void uninstalled () {
        AntProjectSupport.stopFiringProcessor ();
        // #14804:
        OutputWriterOutputStream.detachAllAnnotations();
    }
    
    /*
    public void close () {
        AntProjectSupport.stopFiringProcessor ();
    }
     */

    /** @deprecated no longer used */
    public static final class GlobalJarFileSystem extends JarFileSystem implements Runnable, RepositoryListener {
        private static final long serialVersionUID = -2165058869503900139L;
        // #17476: writeReplace -> null no longer appears to work correctly.
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            err.log("#17476: suppressing old GlobalJarFileSystem");
            // do not call: in.defaultReadObject()
            // Unmount me again:
            RequestProcessor.postRequest(this);
        }
        public void run() {
            err.log("Removing GlobalJarFileSystem from repository");
            listFSs();
            Repository.getDefault().removeFileSystem(this);
            listFSs();
            Repository.getDefault().addRepositoryListener(this);
        }
        private void listFSs() {
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                LinkedList list = new LinkedList();
                Enumeration e = Repository.getDefault().getFileSystems();
                while (e.hasMoreElements()) {
                    FileSystem fs = (FileSystem)e.nextElement();
                    if (fs instanceof GlobalJarFileSystem) {
                        list.add(fs);
                    }
                }
                err.log("GJFSs in repo: " + list);
            }
        }
        public void fileSystemAdded(RepositoryEvent ev) {
            if (ev.getFileSystem() == this) {
                // Was too early before, let's try again.
                RequestProcessor.postRequest(this);
            }
        }
        public void fileSystemRemoved(RepositoryEvent ev) {
            if (ev.getFileSystem() == this) {
                err.log("removing GJFS actually worked");
            }
        }
        public void fileSystemPoolReordered(RepositoryReorderedEvent ev) {
            // ignore
        }
    }

}
