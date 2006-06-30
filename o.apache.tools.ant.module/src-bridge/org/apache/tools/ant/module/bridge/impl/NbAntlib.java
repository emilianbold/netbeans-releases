/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
