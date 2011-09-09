/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserve *
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class FXMLTemplateWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {
    
    private transient WizardDescriptor wiz;
    private transient WizardDescriptor.InstantiatingIterator delegateIterator;

    public static WizardDescriptor.InstantiatingIterator<WizardDescriptor> create() {
        return new FXMLTemplateWizardIterator();
    }


    public FXMLTemplateWizardIterator() {
        delegateIterator = JavaTemplates.createJavaTemplateIterator();
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        wiz = wizard;
        delegateIterator.initialize(wizard);
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        delegateIterator.uninitialize(wizard);
    }

    @Override
    public Set instantiate() throws IOException, IllegalArgumentException {
        Set set = new HashSet(3);
        //set.addAll(delegateIterator.instantiate());
        
        FileObject dir = Templates.getTargetFolder(wiz);
        String targetName = Templates.getTargetName(wiz);
        DataFolder df = DataFolder.findFolder(dir);

        FileObject mainTemplate = FileUtil.getConfigFile("Templates/javafx/FXML.java"); // NOI18N
        DataObject dMainTemplate = DataObject.find(mainTemplate);
        String mainName = targetName + NbBundle.getMessage(FXMLTemplateWizardIterator.class, "Templates/javafx/FXML_Main_Suffix"); //NOI18N
        Map<String, String> params = new HashMap<String, String>();
        params.put("fxmlname", targetName); // NOI18N
        DataObject dobj1 = dMainTemplate.createFromTemplate(df, mainName, params); // NOI18N
        set.add(dobj1.getPrimaryFile());

        FileObject xmlTemplate = FileUtil.getConfigFile("Templates/javafx/FXML.fxml"); // NOI18N
        DataObject dXMLTemplate = DataObject.find(xmlTemplate);
        DataObject dobj = dXMLTemplate.createFromTemplate(df, targetName);
        set.add(dobj.getPrimaryFile());
        
        FileObject javaTemplate = FileUtil.getConfigFile("Templates/javafx/FXML2.java"); // NOI18N
        DataObject dJavaTemplate = DataObject.find(javaTemplate);
        DataObject dobj2 = dJavaTemplate.createFromTemplate(df, targetName); // NOI18N
        set.add(dobj2.getPrimaryFile());
        
        return set;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return delegateIterator.current();
    }

    @Override
    public boolean hasNext() {
        return delegateIterator.hasNext();
    }
    
    @Override
    public boolean hasPrevious() {
        return delegateIterator.hasPrevious();
    }
    
    @Override
    public void nextPanel() {
        if (delegateIterator.hasNext()) {
            delegateIterator.nextPanel();
        }
    }
    
    @Override
    public void previousPanel() {
        delegateIterator.previousPanel();
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
        delegateIterator.addChangeListener(l);
    }
    
    @Override
    public String name() {
        return delegateIterator.name();
    }
    
    @Override
    public void removeChangeListener(ChangeListener l) {
        delegateIterator.removeChangeListener(l);
    }

}
