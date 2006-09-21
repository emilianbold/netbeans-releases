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
 * CustomCLDCPlatformConfigurator.java
 *
 */
package org.netbeans.spi.mobility.cldcplatform;

import java.io.File;

/**
 * CustomCLDCPlatformConfigurator is an SPI for service providing information about some non-standard CLDC platform (SDK, emulator).
 * This interface has to be implemented and registered in module META-INF/services/org.netbeans.spi.mobility.cldcplatform.CustomCLDCPlatformConfigurator
 * #position=xx attribute of the registration is important if two different CustomCLDCPlatformConfigurator implementation recognize the same platform.
 * @author Adam Sotona
 */
public interface CustomCLDCPlatformConfigurator {
    
    /**
     * This method must provide just quick answer if the given folder might be a home for a platform recognized by this configurator.
     * This method is not intended to perform any deep detection.
     * The best way is to just check for any unique files inside.
     * @param platformPath Given platform home directory to query.
     * @return True if this configurator recognizes the folder as possible known platform home directory.
     */
    public boolean isPossiblePlatform(File platformPath);
    
    /**
     * This method is called when the previous for deep detection of the platform.
     * The method should return full platform descriptor or null.
     * @param platformPath Given platform home for the detection.
     * @return CLDCPlatformDescriptor with full information about the platform or null.
     */
    public CLDCPlatformDescriptor getPlatform(File platformPath);
    
    /**
     * Optional method helping the automated platforms detection using Windows registry.
     * If the platform installer stores any keys pointing to the platform installation directory then this method is usefull.
     * Usual pattern is to store such information somewhere under HKEY_LOCAL_MACHINE/Software/&lt;provider name&gt; or HKEY_CURRENT_USER/Software/&lt;provider name&gt;
     * @return Part of the provider name that may help to locate registry key with reference to the platform installation (f.ex.: Nokia)
     */
    public String getRegistryProviderName();
    
}
