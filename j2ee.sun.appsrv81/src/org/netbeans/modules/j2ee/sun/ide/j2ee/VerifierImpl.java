/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
/*
 * VerifierImpl.java
 *
 * Created on December 8, 2004, 2:54 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.OutputStream;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.FileOwnerQuery;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.glassfish.eecommon.api.VerifierSupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

import org.netbeans.modules.j2ee.sun.api.InstrumentAVK; 
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;

/**
 *
 * @author ludo
 */
public  class VerifierImpl extends org.netbeans.modules.j2ee.deployment.plugins.spi.VerifierSupport {
    
    /** Creates a new instance of VerifierImpl */
    public VerifierImpl() {
    }
    
    /**
     * Verify the provided target J2EE module or application, including both
     * standard J2EE and platform specific deployment info.  The provided
     * service could include invoking its own specific UI displaying of verification
     * result. In this case, the service could have limited or no output to logger stream.
     *
     * @param target The an archive, directory or file to verify.
     * @param logger Log stream to write verification output to.
     * @exception ValidationException if the target fails the validation.
     */
    public void verify(FileObject target, OutputStream logger) throws ValidationException {
        //System.out.println("In Verifier...."+target);
        final String jname = FileUtil.toFile(target).getAbsolutePath();
        DeploymentManager dm = getAssociatedSunDM(target);
        SunDeploymentManagerInterface sdm = (SunDeploymentManagerInterface)dm;
        InstrumentAVK avkSupport = getAVKImpl();
        if((avkSupport != null) && (dm != null) && sdm.isLocal()){
            J2eeModuleProvider modProvider = getModuleProvider(target);
            boolean verificationType = avkSupport.createAVKSupport(dm, modProvider);
            if(verificationType){ 
                VerifierSupport.launchVerifier(jname, logger,sdm.getPlatformRoot());
            }
        }else{
            VerifierSupport.launchVerifier(jname,logger,sdm.getPlatformRoot());
        }   
    }
    
    private DeploymentManager getAssociatedSunDM(FileObject target){
        DeploymentManager dm = null;
        J2eeModuleProvider modProvider = getModuleProvider(target);
        if (modProvider != null){
            InstanceProperties serverName = modProvider.getInstanceProperties();
            dm = serverName.getDeploymentManager();
        }
        return dm;
    }

    private InstrumentAVK getAVKImpl(){
        InstrumentAVK avkSupport = AVKLayerUtil.getAVKImplemenation();
        return avkSupport;
    }
    
    private J2eeModuleProvider getModuleProvider(FileObject target){
        J2eeModuleProvider modProvider = null;
        Project holdingProj = FileOwnerQuery.getOwner(target);
        if (holdingProj != null){
            modProvider = (J2eeModuleProvider) holdingProj.getLookup().lookup(J2eeModuleProvider.class);
        }
        return modProvider;
    }

}

