/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.ide;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import org.netbeans.modules.tomcat5.TomcatManager;


/**
 * Tomcat's implementation of the J2eePlatformFactory.
 *
 * @author Stepan Herold
 */
public class TomcatPlatformFactory extends J2eePlatformFactory {
    
    /** Creates a new instance of TomcatPlatformFactory */
    public TomcatPlatformFactory() {
    }
    
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        assert TomcatManager.class.isAssignableFrom(dm.getClass()) : this + " cannot create platform for unknown deployment manager:" + dm;
        return ((TomcatManager)dm).getTomcatPlatform();
    }
}
