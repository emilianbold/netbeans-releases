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
/*
 * MockDDBeanRoot.java
 *
 * Created on February 23, 2004, 6:04 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import javax.enterprise.deploy.model.DDBeanRoot;
import org.netbeans.modules.j2ee.sun.share.MockDeployableObject;
/**
 *
 * @author  vkraemer
 */
public class MockDDBeanRoot extends MockDDBean implements DDBeanRoot {
    
    /** Creates a new instance of MockDDBeanRoot */
    public MockDDBeanRoot() {
    }
 
    public String getFilename() {
        return null;
    }
    
    public String getDDBeanRootVersion() {
        return null;
    }
    
    public javax.enterprise.deploy.model.DeployableObject getDeployableObject() {
        MockDeployableObject mdo = new MockDeployableObject();
        mdo.setDDBeanRoot(this);
        return mdo;
    }
    
    public String getModuleDTDVersion() {
        return null;
    }
    
    public javax.enterprise.deploy.shared.ModuleType getType() {
        return null;
    }
    
}
