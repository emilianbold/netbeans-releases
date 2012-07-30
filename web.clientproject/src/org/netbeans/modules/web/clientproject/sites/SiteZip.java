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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateCustomizer;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Special {@link SiteTemplateImplementation} (not registered in SFS).
 */
@NbBundle.Messages({"LBL_SiteZip=Archive File",
    "LBL_SiteZip_Error=Template file does not exist"
        })
public class SiteZip implements SiteTemplateImplementation {
    
    private static final String USED_TEMPLATES = "last.templates";
    private static final String SEPARATOR = "=s e p=";

    private Customizer cust;
    
    @Override
    public String getName() {
        return Bundle.LBL_SiteZip();
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public SiteTemplateCustomizer getCustomizer() {
        cust = new Customizer();
        return cust;
    }

    @Override
    public void apply(FileObject projectRoot, ProgressHandle handle) {
        try {
            String template = cust.panel.getTemplate();
            if (!template.startsWith("http")) {
                File templ = new File(template);
                SiteHelper.install(templ, projectRoot, handle);
            } else {
                SiteHelper.install(template, projectRoot, handle);
            }
            registerTemplate(template);
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
    
    static class Customizer implements SiteTemplateCustomizer {

        private SiteZipPanel panel = new SiteZipPanel(this);
        private ChangeSupport sup = new ChangeSupport(this);
        private String error = "";
        
        @Override
        public void addChangeListener(ChangeListener listener) {
            sup.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            sup.removeChangeListener(listener);
        }

        @Override
        public JComponent getComponent() {
            return panel;
        }

        @Override
        public boolean isValid() {
            String tpl = panel.getTemplate();
            if (!tpl.startsWith("http")  && !new File(tpl).exists()) {
                error = Bundle.LBL_SiteZip_Error();
                return false;
            }
            error = "";
            return true;
        }

        @Override
        public String getErrorMessage() {
            return error;
        }

        @Override
        public String getWarningMessage() {
            return "";
        }
        
        void fireChange() {
            sup.fireChange();
        }
    
    }
    
    public static void registerTemplate(File f) {
        String name = f.getAbsolutePath();
        registerTemplate(name);
    }
    
    public static void registerTemplate(String name) {
        String templates = NbPreferences.forModule(SiteZip.class).get(USED_TEMPLATES, "");
        templates = name + SEPARATOR + templates.replaceAll(name+SEPARATOR, "");
        NbPreferences.forModule(SiteZip.class).put(USED_TEMPLATES, templates);
    }
    
    public static List<String> getUsedTemplates() {
        String templates = NbPreferences.forModule(SiteZip.class).get(USED_TEMPLATES, "");
        return Arrays.asList(templates.split(SEPARATOR));
    }
}
