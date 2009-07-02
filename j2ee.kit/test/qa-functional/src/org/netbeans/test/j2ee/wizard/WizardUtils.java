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
package org.netbeans.test.j2ee.wizard;

import org.netbeans.api.project.Project;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 *
 * @author jungi
 */
public class WizardUtils {
    
    public static final int MODULE_WAR = 0;
    public static final int MODULE_EJB = 1;
    public static final int MODULE_EAR = 2;
    public static final int MODULE_CAR = 3;
    
    public static final int VERSION_1_4 = 0;
    public static final int VERSION_5 = 1;
    
    /** Creates a new instance of WizardUtils */
    private WizardUtils() {
    }
    
    public static NewProjectWizardOperator createNewProject(String category,
            String project) {
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.treeCategories().setComparator(new Operator.DefaultStringComparator(true, true));
        npwo.lstProjects().setComparator(new Operator.DefaultStringComparator(true, true));
        npwo.selectCategory(category);
        npwo.selectProject(project);
        npwo.next();
        return npwo;
    }
    
    public static NewJavaProjectNameLocationStepOperator setProjectNameLocation(
            String name, String location) {
        NewJavaProjectNameLocationStepOperator op = new NewJavaProjectNameLocationStepOperator();
        op.txtProjectName().setText(name);
        op.txtProjectLocation().setText(location);
        return op;
    }
    
    public static NewFileWizardOperator createNewFile(Project p,
            String category, String filetype) {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        new EventTool().waitNoEvent(500);
        nfwo.treeCategories().setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.lstFileTypes().setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.cboProject().selectItem(p.toString());
        nfwo.selectCategory(category);
        nfwo.selectFileType(filetype);
        nfwo.next();
        return nfwo;
    }
    
    public static NewJavaFileNameLocationStepOperator setFileNameLocation(String name,
            String pkg, String srcRoot) {
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(name);
        if (srcRoot != null) {
            op.cboLocation().selectItem(srcRoot);
        }
        op.setPackage(pkg);
        return op;
    }
    
    public static NewJavaProjectNameLocationStepOperator setJ2eeSpecVersion(
            NewJavaProjectNameLocationStepOperator op, int moduleType, String version) {
        op.next();
        JComboBoxOperator jcbo = new JComboBoxOperator(op, 1);
        boolean found = false;
        int i = 0;
        for (; i < jcbo.getItemCount(); i++) {
            Object o = jcbo.getItemAt(i);
            if (o.toString().indexOf(version) > 0) {
                found = true;
                break;
            }
        }
        if (found) {
            jcbo.selectItem(i);
        } else {
            throw new IllegalArgumentException("Version: '" + version + "' was not found.");
        }
        return op;
    }
    
}
