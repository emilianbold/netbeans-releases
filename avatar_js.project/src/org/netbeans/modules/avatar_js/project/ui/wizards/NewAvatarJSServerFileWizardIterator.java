/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.avatar_js.project.ui.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin
 */
@TemplateRegistrations({
    @TemplateRegistration(folder = NewAvatarJSServerFileWizardIterator.FOLDER, position = 100, content = "../resources/ServerFile.js.template", scriptEngine = "freemarker", displayName = "#ServerFile.js", iconBase = NewAvatarJSServerFileWizardIterator.JS_ICON_BASE, description = "../resources/ServerFile.html", category = {"simple-files"}),
})
@NbBundle.Messages("ServerFile.js=Server.js")
public class NewAvatarJSServerFileWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {
    
    static final String FOLDER = "Avatar_js";   // NOI18N
    static final String JS_ICON_BASE = "org/netbeans/modules/avatar_js/project/ui/resources/javascript.png";  // NOI18N
    public static final String PARAM_PORT = "port";// NOI18N

    private transient int index;
    private transient WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private transient WizardDescriptor wiz;
    
    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wiz = wizard;
        index = 0;
        panels = createPanels( wiz );
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        this.wiz = null;
        panels = null;
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        FileObject dir = Templates.getTargetFolder( wiz );
        String targetName = Templates.getTargetName( wiz );
        
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wiz );
        
        DataObject dTemplate = DataObject.find( template );
        DataObject dobj = dTemplate.createFromTemplate(df, targetName, createParams());
        FileObject createdFile = dobj.getPrimaryFile();
        
        return Collections.singleton( createdFile );
    }

    private Map<String, ? extends Object> createParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_PORT, wiz.getProperty(WizardSettings.PROP_SERVER_FILE_PORT));
        return params;
    }
    
    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @Override
    public String name() {
        return "";  // NOI18N
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    private WizardDescriptor.Panel<WizardDescriptor>[] createPanels(WizardDescriptor wiz) {
        //Project project = Templates.getProject(wiz);
        return new WizardDescriptor.Panel[] {
            new PanelServerFile()
        };
    }

}
