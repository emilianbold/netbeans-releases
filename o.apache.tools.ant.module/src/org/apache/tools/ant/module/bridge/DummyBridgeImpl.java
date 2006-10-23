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

package org.apache.tools.ant.module.bridge;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import org.apache.tools.ant.module.AntModule;
import org.netbeans.api.progress.ProgressHandle;
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
    
    public Enumeration<String> getAttributes() {
        return Enumerations.empty();
    }
    
    public Class getElementType(String name) {
        throw new IllegalStateException();
    }
    
    public Enumeration<String> getNestedElements() {
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
    
    public boolean run(File buildFile, List<String> targets, InputStream in, OutputWriter out, OutputWriter err, Map<String,String> properties, int verbosity, String displayName, Runnable interestingOutputCallback, ProgressHandle handle) {
        err.println(NbBundle.getMessage(DummyBridgeImpl.class, "ERR_cannot_run_target"));
        problem.printStackTrace(err);
        return false;
    }

    public void stop(Thread process) {
        // do nothing
    }
    
}
