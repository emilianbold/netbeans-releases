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
package org.netbeans.modules.websvc.jaxwsmodel;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;
/**
 * XXX: This is "workaround" as a temporary fix for 
 * BZ#187145 - [69cat] Projects with a WS client created on one machine will not load on another.
 * This class is introduced in the 7.3 release and should be removed in 
 * future ( post 7.3+ ) release because of the reasons:
 * - real issue with library is fixed in the WSUtils class 
 *   ( jar:nbinst URLs are used instead of absolute file path ).
 * - migration from 7.3 to any future release will not be issued   
 *    
 * @author ads
 *
 */
public class JaxWsModuleInstall extends  ModuleInstall {

    /* (non-Javadoc)
     * @see org.openide.modules.ModuleInstall#restored()
     */
    @Override
    public void restored() {
        Library jaxWsApiLib = LibraryManager.getDefault().getLibrary(
                WSUtils.JAX_WS_ENDORSED);
        if ( jaxWsApiLib == null ){
            return;
        }
        List<URL> urls = jaxWsApiLib.getContent("classpath");       // NOI18N
        boolean isBroken = false;
        try {
            for (URL url : urls) {
                url = FileUtil.getArchiveFile(url);
                if ( url == null ){
                    isBroken = true;
                    break;
                }
                File file = new File(url.toURI());
                file = FileUtil.normalizeFile(file);
                if ( file == null ){
                    isBroken = true;
                    break;
                }
                if ( FileUtil.toFileObject(file) == null){
                    isBroken = true;
                    break;
                }
            }
            if ( isBroken ){
                LibraryManager.getDefault().removeLibrary(jaxWsApiLib);
            }
        }
        catch(URISyntaxException e ){
            Logger.getLogger(JaxWsModuleInstall.class.getName()).log(
                    Level.INFO, null , e);
        }
        catch (IllegalArgumentException e) {
            Logger.getLogger(JaxWsModuleInstall.class.getName()).log(
                    Level.INFO, null , e);
        }
        catch (IOException e) {
            Logger.getLogger(JaxWsModuleInstall.class.getName()).log(
                    Level.INFO, null , e);
        } 

    }
}
