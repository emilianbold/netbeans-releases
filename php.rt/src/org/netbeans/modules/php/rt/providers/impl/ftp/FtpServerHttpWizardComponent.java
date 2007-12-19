/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.rt.providers.impl.ftp;

import java.util.logging.Logger;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.providers.impl.WebServerHttpWizardComponent;
import org.netbeans.modules.php.rt.ui.AddHostWizard;

/**
 *
 * @author avk
 */
public class FtpServerHttpWizardComponent extends WebServerHttpWizardComponent{

    private static Logger LOGGER = Logger.getLogger(
            FtpServerHttpWizardComponent.class.getName());

    public FtpServerHttpWizardComponent( FtpServerHttpWizardPanel panel ) {
        super(panel);
    }

    @Override
    protected void read(AddHostWizard wizard) {
        HostImpl impl = (HostImpl) wizard
                .getProperty(FtpUiConfigProvider.HOST);
        if (impl != null){
            setDomain( (String)impl.getProperty(HostImpl.DOMAIN) );
            setPort( (String)impl.getProperty(HostImpl.PORT) );
            setBaseDirectory( (String)impl.getProperty(HostImpl.BASE_DIRECTORY_PATH) );
        }
        
        doContentValidation();
    }

    @Override
    protected void store(AddHostWizard wizard) {
        FtpServerProvider provider = (FtpServerProvider)wizard.getCurrentProvider();
        
        HostImpl host = (HostImpl) wizard
                .getProperty(FtpUiConfigProvider.HOST);
        
        FtpHostImpl impl = null;
        if (host instanceof FtpHostImpl){
            impl = (FtpHostImpl)host;
        }
        
        if (impl == null){
            String name = (String)wizard.getProperty(AddHostWizard.NAME);
            impl = new FtpHostImpl(name, provider);
        }

        impl.setProperty(HostImpl.DOMAIN, getDomain());
        impl.setProperty(HostImpl.PORT, getPort());
        impl.setProperty(HostImpl.BASE_DIRECTORY_PATH, getBaseDirectory());
        
        wizard.putProperty( FtpUiConfigProvider.HOST, impl );
    }

    @Override
    protected void setDefaults() {
        // do nothing
    }

}
