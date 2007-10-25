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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xslt.project.wizard;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.compapp.projects.base.ui.wizards.NewIcanproProjectWizardIterator;
import org.netbeans.modules.xslt.project.XsltproProjectGenerator;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import static org.netbeans.modules.xslt.project.XsltproConstants.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.openide.loaders.DataObject;

/**
 * Iterator for a wizard that needs to instantiate new xslt object.
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class NewXsltproProjectWizardIterator extends NewIcanproProjectWizardIterator {

    private static final long serialVersionUID = 1L;

    public NewXsltproProjectWizardIterator() {
        super();
    }

    @Override
    public Set instantiate() throws IOException {
        Set set = super.instantiate();
        createTMapFile(set);
        return set;
    }
    
    @Override
    protected void createProject(File dirF, String name, String j2eeLevel) throws IOException {
        XsltproProjectGenerator.createProject(dirF, name);
    }

    @Override
    protected String getDefaultTitle() {
        return NbBundle.getMessage(NewXsltproProjectWizardIterator.class, "LBL_XSLT_Wizard_Title"); //NOI18N   
    }

    @Override
    protected String getDefaultName() {
        return NbBundle.getMessage(NewXsltproProjectWizardIterator.class, "LBL_NPW1_DefaultProjectName"); //NOI18N
    }
    
    private void createTMapFile(Set resultSet) throws IOException {
        
        if (resultSet == null || resultSet.isEmpty()) {
            return;
        }
        
        FileObject fo = null;
        Iterator setIterator = resultSet.iterator();
        while (setIterator.hasNext()) {
            Object obj = setIterator.next();
            if (obj instanceof FileObject) {
                fo = (FileObject)obj;
                break;
            }
        }
        Project p = ProjectManager.getDefault().findProject(fo);
        if (p != null) {
            FileObject srcFo = Util.getProjectSource(p);
            FileObject tMapFo = FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem()
                    .findResource("org-netbeans-xsltpro/transformmap.xml"), //NOI18N
                    srcFo, "transformmap"); //NOI18N
            
            SoaUiUtil.fixEncoding(DataObject.find(tMapFo), srcFo);
        }
    }
}
