/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.bridge.impl;

import java.io.IOException;
import java.net.URL;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.apache.tools.ant.taskdefs.Antlib;

/**
 * Antlib subclass used to define custom tasks inside NetBeans.
 * Needs to be a subclass to access {@link Antlib#setClassLoader}
 * and {@link Antlib#setURI}.
 * @author Jesse Glick
 */
public final class NbAntlib extends Antlib {
    
    /**
     * Process antlib.xml definitions.
     * @param p a project to add definitions to
     * @param antlib location of antlib.xml to load
     * @param uri a URI to add definitions in, or null
     * @param l a class loader to load defined classes from
     */
    public static void process(Project p, URL antlib, String uri, ClassLoader l) throws IOException, BuildException {
        ComponentHelper helper = ComponentHelper.getComponentHelper(p);
        helper.enterAntLib(uri);
        Antlib al;
        try {
            UnknownElement antlibElement = new ProjectHelper2().parseUnknownElement(p, antlib);
            al = new NbAntlib(uri, l);
            al.setProject(p);
            al.setLocation(antlibElement.getLocation());
            al.init();
            antlibElement.configure(al);
        } finally {
            helper.exitAntLib();
        }
        al.execute();
    }
    
    private NbAntlib(String uri, ClassLoader l) {
        if (uri != null) {
            setURI(uri);
        }
        setClassLoader(l);
    }
    
}
