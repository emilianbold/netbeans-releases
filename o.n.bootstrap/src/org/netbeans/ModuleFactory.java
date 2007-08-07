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
 * Software is Nokia. Portions Copyright 2005 Nokia. All Rights Reserved.
 */
package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.util.jar.Manifest;

/**
 * Allows creation of custom modules. The factories are searched in
 * the default lookup (org.openide.util.Lookup.getDefault()). If there is one
 * it is used - if there are more of them arbitrary one is used (so please make
 * sure that there is only one present in the installation). If there is none
 * in the default lookup the system will use an instance of this class.
 *
 * @author David Strupl
 */
public class ModuleFactory {

    /**
     * This method creates a "standard" module. Standard modules can be
     * disabled, reloaded, autoloaded (loaded only when needed).
     * @see StandardModule
     */
    public Module create(File jar, Object history, boolean reloadable,
            boolean autoload, boolean eager, ModuleManager mgr, Events ev)
            throws IOException {
        return new StandardModule(mgr, ev, jar, history, reloadable, autoload, eager);
    }
    /**
     * This method creates a "fixed" module. Fixed modules cannot be
     * realoaded, are always enabled and are typically present on the
     * classpath.
     * @see FixedModule
     */
    public Module createFixed(Manifest mani, Object history,
            ClassLoader loader, ModuleManager mgr, Events ev)
            throws InvalidException {
        return createFixed(mani, history, loader, false, false, mgr, ev);
    }
    /**
     * This method creates a "fixed" module. Fixed modules cannot be
     * realoaded, are always enabled and are typically present on the
     * classpath.
     * @see FixedModule
     * @since 2.7
     */
    public Module createFixed(Manifest mani, Object history, ClassLoader loader, boolean autoload, boolean eager,
            ModuleManager mgr, Events ev) throws InvalidException {
        return new FixedModule(mgr, ev, mani, history, loader, autoload, eager);
    }
    /**
     * Allows specifying different parent classloader of all modules classloaders.
     */
    public ClassLoader getClasspathDelegateClassLoader(ModuleManager mgr, ClassLoader del) {
        return del;
    }
    
    /**
     * If this method returns true the parent the original classpath
     * classloader will be removed from the parent classloaders of a module classloader.
     */
    public boolean removeBaseClassLoader() {
        return false;
    }
    
}
