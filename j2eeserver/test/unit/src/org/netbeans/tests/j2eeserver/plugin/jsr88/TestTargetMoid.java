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
