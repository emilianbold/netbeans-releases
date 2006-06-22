/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.CLIHandler;
import org.netbeans.Module;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Handler for core.jar options.
 * @author Jaroslav Tulach
 */
public class CLICoreBridge extends CLIHandler {
    /**
     * Create a default handler.
     */
    public CLICoreBridge() {
        super(WHEN_INIT);
    }
    
    protected int cli(Args arguments) {
        Lookup clis = Lookup.getDefault();
        Collection handlers = clis.lookupAll(CLIHandler.class);
        int h = notifyHandlers(arguments, handlers, WHEN_EXTRA, true, true);
        if (h == 0) {
            h = CoreBridge.getDefault().cli(
                arguments.getArguments(),
                arguments.getInputStream(),
                arguments.getOutputStream(),
                arguments.getErrorStream(),
                arguments.getCurrentDirectory()
            );
        }
        return h;
    }

    protected void usage(PrintWriter w) {
        ModuleSystem moduleSystem;
        try {
            moduleSystem = new ModuleSystem(Repository.getDefault().getDefaultFileSystem());
        } catch (IOException ioe) {
            // System will be screwed up.
            throw (IllegalStateException) new IllegalStateException("Module system cannot be created").initCause(ioe); // NOI18N
        }

//        moduleSystem.loadBootModules();
        moduleSystem.readList();
        
        
        ArrayList<URL> urls = new ArrayList<URL>();
        for (Module m : moduleSystem.getManager().getModules()) {
            for (File f : m.getAllJars()) {
                try {
                    urls.add(f.toURI().toURL());
                }
                catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader());
        MainLookup.systemClassLoaderChanged(loader);
        Lookup clis = Lookup.getDefault();
        Collection handlers = clis.lookupAll(CLIHandler.class);
        showHelp(w, handlers, WHEN_EXTRA);
        w.flush();
    }
}
