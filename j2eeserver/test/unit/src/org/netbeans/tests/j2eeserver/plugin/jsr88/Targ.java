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
import java.util.*;

/**
 *
 * @author  gfink
 */
public class Targ implements Target {
    
    String name;
    public Targ(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return "Description for " + name;
    }
    
    public String getName() {
        return name;
    }
    
    Map modules = new HashMap();
    public void add(TargetModuleID tmid) {
        modules.put(tmid.toString(), tmid);
    }
    public TargetModuleID getTargetModuleID(String id) {
        return (TargetModuleID) modules.get(id);
    }
    public TargetModuleID[] getTargetModuleIDs() {
        return (TargetModuleID[]) modules.values().toArray(new TargetModuleID[0]);
    }
}
