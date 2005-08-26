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

package org.apache.tools.ant.module.bridge;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.module.AntModule;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Enumerations;
import org.openide.windows.OutputWriter;

/**
 * Used when the real Ant class loader cannot be initialized for some reason.
 * @author Jesse Glick
 */
final class DummyBridgeImpl implements BridgeInterface, IntrospectionHelperProxy {
    
    private final Throwable problem;
    
    public DummyBridgeImpl(Throwable problem) {
        this.problem = problem;
        AntModule.err.notify(ErrorManager.INFORMATIONAL, problem);
    }
    
    public String getAntVersion() {
        return NbBundle.getMessage(DummyBridgeImpl.class, "ERR_ant_not_loadable", problem);
    }
    
    public boolean isAnt16() {
        return false;
    }
    
    public IntrospectionHelperProxy getIntrospectionHelper(Class clazz) {
        return this;
    }
    
    public Class getAttributeType(String name) {
        throw new IllegalStateException();
    }
    
    public Enumeration getAttributes() {
        return Enumerations.empty();
    }
    
    public Class getElementType(String name) {
        throw new IllegalStateException();
    }
    
    public Enumeration getNestedElements() {
        return Enumerations.empty();
    }
    
    public boolean supportsCharacters() {
        return false;
    }
    
    public boolean toBoolean(String val) {
        return Boolean.valueOf(val).booleanValue();
    }
    
    public String[] getEnumeratedValues(Class c) {
        return null;
    }
    
    public boolean run(File buildFile, List targets, InputStream in, OutputWriter out, OutputWriter err, Properties properties, int verbosity, String displayName, Runnable interestingOutputCallback) {
        err.println(NbBundle.getMessage(DummyBridgeImpl.class, "ERR_cannot_run_target"));
        problem.printStackTrace(err);
        return false;
    }

    public void stop(Thread process) {
        // do nothing
    }
    
}
