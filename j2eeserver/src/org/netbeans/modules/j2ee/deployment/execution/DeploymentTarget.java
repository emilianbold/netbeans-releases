/*
 * DeploymentTarget.java
 *
 * Created on September 3, 2002, 5:11 PM
 */

package org.netbeans.modules.j2ee.deployment.execution;

import org.openide.nodes.Node;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeComponentProvider;

/**
 *
 * @author  gfink
 */
public interface DeploymentTarget extends Node.Cookie {
    
    public J2eeComponentProvider getComponentProvider();
    
    public ServerString getServer();
    
    public String getConfigID();
    
    public void startClient();
}
