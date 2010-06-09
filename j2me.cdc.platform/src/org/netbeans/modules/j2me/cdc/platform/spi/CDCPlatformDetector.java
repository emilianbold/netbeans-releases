/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2me.cdc.platform.spi;

import java.io.IOException;

import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.platform.platformdefinition.PlatformConvertor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

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
     * Installs platform detected using CDCPlatformDetector.detectPlatform method
     * @param platform CDCPlatform as a result of detection
     * @param systemName name of platform, this name should not exist, otherwise IllegalStateException will be thrown
     * @throws IOException when some I/O issue occur
     */
    public final void  installPlatform(CDCPlatform platform, String systemName) throws IOException {
        FileObject platformsFolder = FileUtil.getConfigFile(
                "Services/Platforms/org-netbeans-api-java-Platform"); //NOI18N
        if (platformsFolder.getFileObject(systemName,"xml")!=null) {   //NOI18N
            String msg = NbBundle.getMessage(CDCPlatformDetector.class,"ERROR_InvalidName");
            throw (IllegalStateException)ErrorManager.getDefault().annotate(
                new IllegalStateException(msg), ErrorManager.USER, null, msg,null, null);
        }
        PlatformConvertor.create(platform, DataFolder.findFolder(platformsFolder),systemName);
    }

    /**
     * @return configurator for platform tools or null if none is available
     */
    public CDCPlatformConfigurator getConfigurator(FileObject installedFolder){
        return null;
    }    
}
