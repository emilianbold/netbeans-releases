/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.sites;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateCustomizer;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@NbBundle.Messages({"LBL_Name_Initializr=Initializr"
        })
@ServiceProvider(service=SiteTemplateImplementation.class, position=200)
public class SiteInitializr implements SiteTemplateImplementation {

    private SiteTemplateCustomizerImpl customizer = new SiteTemplateCustomizerImpl();
    
    @Override
    public String getName() {
        return Bundle.LBL_Name_Initializr();
    }

    @NbBundle.Messages("SiteInitializr.description=Initializr")
    @Override
    public String getDescription() {
        return Bundle.SiteInitializr_description();
    }

    @Override
    public SiteTemplateCustomizer getCustomizer() {
        return customizer;
    }

    @Override
    public void apply(FileObject p, ProgressHandle handle) {
        try {
            SiteHelper.install(customizer.getURL(), p, handle);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public Collection<String> supportedLibraries() {
        return Collections.emptyList();
    }

    private static class SiteTemplateCustomizerImpl implements SiteTemplateCustomizer {

        private SiteInitializrPanel panel;
        
        public SiteTemplateCustomizerImpl() {
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
        }

        @Override
        public JComponent getComponent() {
            if (panel == null) {
                panel = new SiteInitializrPanel();
            }
            return panel;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public String getErrorMessage() {
            return null;
        }

        @Override
        public String getWarningMessage() {
            return null;
        }
        
        public String getURL() {
            if (panel.getUserChoice() == SiteInitializrPanel.Type.Classic) {
                return "http://www.initializr.com/builder?h5bp-content&modernizr&h5bp-htaccess&jquerymin&h5bp-chromeframe&h5bp-analytics&h5bp-iecond&h5bp-favicon&h5bp-appletouchicons&h5bp-scripts&h5bp-robots&h5bp-humans&h5bp-404&h5bp-adobecrossdomain&h5bp-css&h5bp-csshelpers&h5bp-mediaqueryprint&h5bp-mediaqueries";
            }
            if (panel.getUserChoice() == SiteInitializrPanel.Type.Reponsive) {
                return "http://www.initializr.com/builder?izr-responsive&jquerymin&h5bp-chromeframe&h5bp-analytics&h5bp-iecond&h5bp-favicon&h5bp-appletouchicons&modernizrrespond&h5bp-css&h5bp-csshelpers&h5bp-mediaqueryprint&izr-emptyscript";
            }
            // Bootstrap:
            return "http://www.initializr.com/builder?mode=less&boot-hero&jquerymin&h5bp-chromeframe&h5bp-analytics&h5bp-iecond&h5bp-favicon&h5bp-appletouchicons&modernizrrespond&izr-emptyscript&boot-css&boot-scripts";
        }
    }
    
}
