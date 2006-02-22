/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;

import org.netbeans.CLIHandler;
import org.netbeans.Module;
import org.openide.ErrorManager;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

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
        Collection handlers = clis.lookup(new Lookup.Template(CLIHandler.class)).allInstances();
        return notifyHandlers(arguments, handlers, WHEN_EXTRA, true, true);
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
        
        
        ArrayList urls = new ArrayList();
        {
            Iterator it = moduleSystem.getManager().getModules().iterator();
            while (it.hasNext()) {
                Module m = (Module)it.next();
                Iterator files = m.getAllJars().iterator();
                while (files.hasNext()) {
                    File f = (File)files.next();
                    try {
                        urls.add(f.toURI().toURL());
                    } catch (MalformedURLException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            }
        }
        
        URLClassLoader loader = new URLClassLoader((URL[])urls.toArray(new URL[0]), getClass().getClassLoader());
        Lookup clis = Lookups.metaInfServices(loader);
        Collection handlers = clis.lookup(new Lookup.Template(CLIHandler.class)).allInstances();
        showHelp(w, handlers, WHEN_EXTRA);
        w.flush();
    }
}
