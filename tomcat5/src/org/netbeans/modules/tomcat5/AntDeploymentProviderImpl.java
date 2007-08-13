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

package org.netbeans.modules.tomcat5;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sherold
 */
public class AntDeploymentProviderImpl implements AntDeploymentProvider {
    
    private final TomcatManager tm;
    
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.tomcat5"); // NOI18N
    
    public AntDeploymentProviderImpl(DeploymentManager dm) {
        tm = (TomcatManager)dm;
    }

    public void writeDeploymentScript(OutputStream os, Object moduleType) throws IOException {
        InputStream is = AntDeploymentProviderImpl.class.getResourceAsStream("resources/tomcat-ant-deploy.xml"); // NOI18N
        if (is == null) {
            // this should never happen, but better make sure
            LOGGER.severe("Missing resource resources/tomcat-ant-deploy.xml."); // NOI18N
            return;
        }
        try {
            FileUtil.copy(is, os);
        } finally {
            is.close();
        }
    }

    public File getDeploymentPropertiesFile() {
        TomcatProperties tp = tm.getTomcatProperties();
        File file = tp.getAntDeploymentPropertiesFile();
        if (!file.exists()) {
            // generate the deployment properties file only if it does not exist
            try {
                tp.storeAntDeploymentProperties(file, true);
            } catch (IOException ioe) {
                Logger.getLogger(AntDeploymentProviderImpl.class.getName()).log(Level.INFO, null, ioe);
            }
        }
        return file;
    }
}
