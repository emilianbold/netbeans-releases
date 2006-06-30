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
