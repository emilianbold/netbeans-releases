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

package org.netbeans.test.web.performance;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author ms113234
 */
public class Macros {
    /**
     * Starts bundled Tomcat server, returns the Tomcat's node.
     * @return Tomcat's node
     */
    public static J2eeServerNode startBundledTomact() {
        RuntimeTabOperator.invoke();
        J2eeServerNode serverNode = new J2eeServerNode("Bundled Tomcat");
        serverNode.start();
        return serverNode;
    }
    
    /**
     * Stops bundled Tomcat server, returns the Tomcat's node.
     * @return Tomcat's node
     */
    public static Node stopBundledTomact() {
        RuntimeTabOperator.invoke();
        J2eeServerNode serverNode = new J2eeServerNode("Bundled Tomcat");
        serverNode.stop();
        return serverNode;
    }
}
