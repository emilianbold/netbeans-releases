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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.util;

import java.beans.Beans;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
//import javax.portlet.PortletContext;

/**
 * Strategies for finding the current ClassLoader such as
 * <code>Thread.currentThread().getContextClassLoader()</code>
 * do not work during design time, where the notion of the 
 * classpath is more constrained. Please make sure you use 
 * this utility when you need to get hold of the current loader.
 */
public class ClassLoaderFinder {
    
    /**
     * <p>Return the best class loader to use for loading resources.
     * This is normally the thread context class loader but can 
     * overridden using {@link #setCustomClassLoader}. At design-time, if
     * no custom class loader has been set, an attempt will be made to
     * load the class loader stored in the servlet or portlet context
     * attribute <code>com.sun.rave.project.classloader</code>
     * </p>
     * @param fallbackClass If there is no context class loader, fall
     * back to using the class loader for the given object
     * @return A ClassLoader to use for loading resources
     */
    public static ClassLoader getCurrentLoader(Object fallbackClass) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader   = fallbackClass.getClass().getClassLoader();
        }
        return loader;
    }  
}

