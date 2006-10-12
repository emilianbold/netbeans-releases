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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.AntDeploymentProvider;
import org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;

/**
 * @thief vkraemer
 * @author sherold
 */
public class AntDeploymentProviderImpl implements AntDeploymentProvider {
    
    private SunDeploymentManager dm;
    
    public AntDeploymentProviderImpl(DeploymentManager dm) {
        this.dm = (SunDeploymentManager)dm;
    }

    public void writeDeploymentScript(OutputStream os, Object moduleType) throws IOException {
        InputStream is = AntDeploymentProviderImpl.class.getResourceAsStream("ant-deploy.xml"); // NOI18N
        try {
            FileUtil.copy(is, os);
        } finally {
            is.close();
        }
    }

    public File getDeploymentPropertiesFile() {
//        TomcatProperties tp = tm.getTomcatProperties();
        File file = dm.getAntDeploymentPropertiesFile();
        if (!file.exists()) {
            // generate the deployment properties file only if it does not exist
            try {
                dm.storeAntDeploymentProperties(file, true);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            }
        }
        return file;
        //return dm.getAntDeploymentPropertiesFile();
    }
}
