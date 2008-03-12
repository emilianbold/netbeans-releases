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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hibernate.framework;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HibernateFrameworkProvider extends WebFrameworkProvider{

    public HibernateFrameworkProvider() {
        super(NbBundle.getMessage(HibernateFrameworkProvider.class, "HibernateFramework_Name"), 
                NbBundle.getMessage(HibernateFrameworkProvider.class, "HibernateFramework_Description"));
    }
    @Override
    public boolean isInWebModule(WebModule wm) {
        return true;
    }

    @Override
    public WebModuleExtender createWebModuleExtender(WebModule wm, ExtenderController controller) {
        final HibernateConfigurationPanel configPanel = new HibernateConfigurationPanel();
        WebModuleExtender webModuleExtender = new WebModuleExtender() {

            @Override
            public void addChangeListener(ChangeListener listener) {
                
            }

            @Override
            public void removeChangeListener(ChangeListener listener) {
                
            }

            @Override
            public JComponent getComponent() {
                return configPanel;
            }

            @Override
            public HelpCtx getHelp() {
                return null;
            }

            @Override
            public void update() {
                
            }

            @Override
            public boolean isValid() {
                return configPanel.isPanelValid();
            }

            @Override
            public Set<FileObject> extend(WebModule webModule) {
                return new TreeSet<FileObject>();
            }
        };
        return webModuleExtender;
    }

    
    @Override
    public File[] getConfigurationFiles(WebModule wm) {
        return new File[]{};
    }

    
}
