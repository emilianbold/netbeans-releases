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

package org.netbeans.modules.j2me.cdc.platform.spi;

import java.io.IOException;

import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.openide.filesystems.FileObject;

/**
 *
 * @author suchys
 */
public abstract class CDCPlatformDetector {
        
    /**
     * @return platform name (human readable)
     */
    public abstract String getPlatformName();
    
    /**
     * @return unique platform type. 
     * Note, the name must be same as name of project plugin
     */
    public abstract String getPlatformType();
    
    /**
     * @param dir where to look for platform
     * @return true if the service recognizes platform
     */
    public abstract boolean accept(FileObject dir);
    
    /**
     * @param dir base folder of platform
     * @return CDCPlatform
     */
    public abstract CDCPlatform detectPlatform(FileObject dir) throws IOException;    

    /**
     * @return configurator for platform tools or null if none is available
     */
    public CDCPlatformConfigurator getConfigurator(FileObject installedFolder){
        return null;
    }    
}
