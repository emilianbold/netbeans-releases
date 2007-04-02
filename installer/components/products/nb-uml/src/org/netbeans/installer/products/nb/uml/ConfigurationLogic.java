/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
package org.netbeans.installer.products.nb.uml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.netbeans.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.netbeans.utils.applications.NetBeansUtils;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String CLUSTER =
            "uml{uml-cluster-version}"; // NOI18N
    private static final String ID =
            "UML"; // NOI18N
    private static final long XMX_VALUE_REQUIRED = 512 * NetBeansUtils.M;
    private static final String MACOSX_QUARTZ_OPTION = 
            "-Dapple.awt.graphics.UseQuartz"; //NOI18N
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(CLUSTER, ID);
    }
    
    public void install(final Progress progress) throws InstallationException {
        // get the list of suitable netbeans ide installations
        List<Dependency> dependencies =
                getProduct().getDependencyByUid(IDE_UID);
        List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File nbLocation = sources.get(0).getInstallationLocation();
        
        
        if (nbLocation != null) {
            progress.setDetail(getString("CL.install.netbeans.conf.uml")); // NOI18N
            try {
                // TODO
                // this option should be change back after UML uninstallation
                long xmx = NetBeansUtils.getJvmMemorySize(nbLocation, NetBeansUtils.MEMORY_XMX);
                if(xmx < XMX_VALUE_REQUIRED) {
                    NetBeansUtils.setJvmMemorySize(nbLocation,
                            NetBeansUtils.MEMORY_XMX,
                            XMX_VALUE_REQUIRED);
                }
                if(SystemUtils.isMacOS()) {
                    // TODO
                    // This option should not be set if JDK6 is used for NB
                    NetBeansUtils.setJvmOption(nbLocation, MACOSX_QUARTZ_OPTION, "false");
                }
            } catch (IOException ex) {
                throw new InstallationException(
                       getString("CL.install.error.netbeans.conf.uml"),
                        ex);
            }
        }
        /////////////////////////////////////////////////////////////////////////////
        super.install(progress);
    }

    public void uninstall(final Progress progress) throws UninstallationException {
        
        // get the list of suitable netbeans ide installations
        List<Dependency> dependencies = 
                getProduct().getDependencyByUid(IDE_UID);
        List<Product> sources = 
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File nbLocation = sources.get(0).getInstallationLocation();
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.netbeans.conf.uml")); // NOI18N
            
            NetBeansUtils.removeJvmOption(nbLocation, MACOSX_QUARTZ_OPTION);
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.netbeans.conf.uml"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        super.uninstall(progress);
    }

}
