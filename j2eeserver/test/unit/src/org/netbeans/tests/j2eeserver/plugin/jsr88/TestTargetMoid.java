/*
 *                 Sun Public License Notice
 *
 * The contents of thisfile are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.tests.j2eeserver.plugin.jsr88;

import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.model.*;

/**
 *
 * @author  nn136682
 */
public class TestTargetMoid implements TargetModuleID {
    Target target;
    String moduleID;
    TestTargetMoid parent;
    TestTargetMoid[] children;
    ModuleType type;
    
    /** Creates a new instance of TestTargetMoid */
    public TestTargetMoid(Target target, String module, ModuleType type) {
        this.target = target;
        this.moduleID = module.replace('.', '_');
        this.type = type; 
    }
    public TargetModuleID[] getChildTargetModuleID() {
        return children;
    }
    
    public ModuleType getModuleType() { return type; }
    
    public String getModuleID() {
        return moduleID;
    }
    
    public TargetModuleID getParentTargetModuleID() {
        return parent;
    }
    
    public Target getTarget() {
        return target;
    }
    
    public String getWebURL() {
        return null;
    }
    
    public String toString() {
        return "TestPlugin:"+target.getName()+":"+moduleID;
    }
    
    public TestTargetMoid getParent() { return parent; }
    public String getModuleUrl() { return moduleID; }
}
