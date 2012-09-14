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

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Special {@link SiteTemplateImplementation} (not registered in SFS).
 */
public class SiteZip implements SiteTemplateImplementation {

    private static final Logger LOGGER = Logger.getLogger(SiteZip.class.getName());
    private static final String USED_TEMPLATES = "last.templates"; //NOI18N
    private static final String SEPARATOR = "=s e p="; //NOI18N

    private Customizer cust;

    @NbBundle.Messages("SiteZip.name=Archive File")
    @Override
    public String getName() {
        return Bundle.SiteZip_name();
    }

    @Override
    public String getDescription() {
        return null;
    }

    public Customizer getCustomizer() {
        cust = new Customizer();
        return cust;
    }

    @Override
    public boolean isPrepared() {
        return getArchiveFile().isFile();
    }

    @Override
    public void prepare() throws IOException {
        assert !EventQueue.isDispatchThread();
        assert !isPrepared();
        String template = cust.panel.getTemplate();
        assert isRemoteUrl(template) : "Remote URL expected: " + template; //NOI18N
        SiteHelper.download(template, getArchiveFile(), null); // NOI18N
    }

    @Override
    public void apply(AntProjectHelper helper, ProgressHandle handle) throws IOException {
        assert !EventQueue.isDispatchThread();
        if (!isPrepared()) {
            // not correctly prepared, user has to know about it already
            LOGGER.info("Template not correctly prepared, nothing to be applied"); //NOI18N
            return;
        }
        SiteHelper.unzipProjectTemplate(helper, getArchiveFile(), handle);
        registerTemplate(cust.panel.getTemplate());
    }

    @Override
    public Collection<String> supportedLibraries() {
        return SiteHelper.listJsFilenamesFromZipFile(getArchiveFile());
    }

    private File getArchiveFile() {
        String template = cust.panel.getTemplate();
        if (!isRemoteUrl(template)) {
            return new File(template);
        }
        // remote file => calculate hash of its url
        CRC32 crc = new CRC32();
        crc.update(template.getBytes());
        String filename = String.valueOf(crc.getValue()) + ".zip"; // NOI18N
        LOGGER.log(Level.INFO, "Remote URL \"{0}\" set, downloaded to {1}", new Object[] {template, filename}); //NOI18N
        return new File(SiteHelper.getJsLibsDirectory(), filename);
    }

    private boolean isRemoteUrl(String input) {
        return input.toLowerCase().startsWith("http"); // NOI18N
    }

    public static void registerTemplate(File f) {
        String name = f.getAbsolutePath();
        registerTemplate(name);
    }

    public static void registerTemplate(String name) {
        String templates = NbPreferences.forModule(SiteZip.class).get(USED_TEMPLATES, ""); //NOI18N
        templates = name + SEPARATOR + templates.replace(name+SEPARATOR, ""); //NOI18N
        NbPreferences.forModule(SiteZip.class).put(USED_TEMPLATES, templates);
    }

    public static List<String> getUsedTemplates() {
        String templates = NbPreferences.forModule(SiteZip.class).get(USED_TEMPLATES, ""); //NOI18N
        return Arrays.asList(templates.split(SEPARATOR));
    }

    //~ Inner classes

    public static class Customizer {

        private SiteZipPanel panel = new SiteZipPanel(this);
        private ChangeSupport sup = new ChangeSupport(this);
        private String error = ""; //NOI18N

        public void addChangeListener(ChangeListener listener) {
            sup.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            sup.removeChangeListener(listener);
        }

        public JComponent getComponent() {
            return panel;
        }

        @NbBundle.Messages({
            "SiteZip.error.template.missing=Template file must be specified.",
            "SiteZip.error.template.invalid=Template file is invalid (http://... or local file expected)."
        })
        public boolean isValid() {
            String tpl = panel.getTemplate();
            if (tpl.isEmpty()) {
                error = Bundle.SiteZip_error_template_missing();
                return false;
            }
            if (!tpl.startsWith("http")  && !new File(tpl).exists()) { //NOI18N
                error = Bundle.SiteZip_error_template_invalid();
                return false;
            }
            error = null;
            return true;
        }

        public String getErrorMessage() {
            return error;
        }

        public String getWarningMessage() {
            return null;
        }

        void fireChange() {
            sup.fireChange();
        }

    }

}
