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

package org.netbeans.modules.mobility.deployment.ftpscp;
import java.io.File;
import org.apache.tools.ant.module.spi.AutomaticExtraClasspathProvider;
import org.openide.modules.InstalledFileLocator;

/**
 * @author Adam Sotona
 */
public class FtpScpClasspathProvider implements AutomaticExtraClasspathProvider {
        
    private File[] path;
    
    public File[] getClasspathItems() {
        if (path == null) synchronized (this) {
            path = new File[] {
                InstalledFileLocator.getDefault().locate("modules/ext/jakarta-oro-2.0.8.jar", "org.netbeans.modules.deployment.ftpscp", false), // NOI18N
                InstalledFileLocator.getDefault().locate("modules/ext/commons-net-1.4.1.jar", "org.netbeans.modules.deployment.ftpscp", false), // NOI18N
            };
        }
        return path;
    }
    
}
