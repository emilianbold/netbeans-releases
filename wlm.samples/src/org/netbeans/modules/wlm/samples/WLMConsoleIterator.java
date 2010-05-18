/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.samples;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.modules.compapp.casaeditor.model.casa.Casa;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaBindingComponentServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaLink;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModelFactory;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceUnits;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author anjeleevich
 */
public class WLMConsoleIterator extends WLMSampleIterator {
    
    public WLMConsoleIterator(String name) {
        super(name);
    }

    public static WizardDescriptor.InstantiatingIterator createWLMConsole() {
        return new WLMConsoleIterator("WLMConsole"); // NOI18N
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        final Set<FileObject> set = new LinkedHashSet<FileObject>();

        File dir = FileUtil.normalizeFile((File) wizard.getProperty(PROJECT_DIR));
        dir.mkdirs();

        FileObject baseFolder = FileUtil.toFileObject(dir);
        FileObject template = Templates.getTemplate(wizard);

        String projectName = (String) wizard.getProperty(PROJECT_NAME);

        String webName = projectName + WEB_SUFFIX;
        String compAppName = projectName + COMP_APP_SUFFIX;

        FileObject webFolder = baseFolder.createFolder(webName);
        FileObject compAppFolder = baseFolder.createFolder(compAppName);

        String templateName = template.getName();

        String templateCompAppName = FOLDER + templateName
                + COMP_APP_SUFFIX + ".zip"; // NOI18N

        unzip(template, webFolder);
        unzip(FileUtil.getConfigFile(templateCompAppName),
                compAppFolder);

        changeName(webFolder, webName, templateName + WEB_SUFFIX);

        FileObject casaFileObject = renameCompApp(compAppFolder, compAppName, 
                templateName + COMP_APP_SUFFIX);

        addModule(compAppFolder, webFolder);

        CasaModel casaModel = CasaModelFactory.getInstance()
                .getModel(Utilities.getModelSource(casaFileObject, true));

        casaModel.startTransaction();
        try {
            Casa rootComponent = casaModel.getRootComponent();
            CasaServiceUnits serviceUnits = rootComponent.getServiceUnits();
            CasaBindingComponentServiceUnit casaBinding = serviceUnits
                    .getBindingComponentServiceUnits().get(0);
            CasaPort casaPort = casaBinding.getPorts().getPorts().get(0);
            CasaLink casaLink = casaPort.getLink();

            String href = casaLink.getHref();
            href = href.replace("WLMConsoleWeb", webName); // NOI18N
            casaLink.setHref(href);
        } finally {
            casaModel.endTransaction();
        }

        DataObject casaDataObject = DataObject.find(casaFileObject);
        SaveCookie saveCookie = casaDataObject.getLookup()
                .lookup(SaveCookie.class);
        
        if (saveCookie != null) {
            saveCookie.save();
        }

        set.add(webFolder);
        set.add(compAppFolder);

        return set;
    }
}
