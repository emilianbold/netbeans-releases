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
package org.netbeans.modules.bpel.samples;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;

public class TravelReservationServiceWizardIterator extends SampleWizardIterator {
    private static final long serialVersionUID = 1L;
    
    public static TravelReservationServiceWizardIterator createIterator() {
        return new TravelReservationServiceWizardIterator();
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new TravelReservationServiceWizardPanel(),
        };
    }
    
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(TravelReservationServicePanelVisual.class, "MSG_CreateTravelReservatioService"),
        };
    }
    
    private Set<FileObject> createJ2eeReservationPartnerServicesProjects(FileObject targetProjectDir) throws FileNotFoundException , IOException {
        assert targetProjectDir != null : "targetProjectDir for ReservationPartnerServices project is null";
        Set<FileObject> resultSet = new HashSet<FileObject>();

        final FileObject j2eeReservationProjectDir = targetProjectDir.createFolder(SoaSampleProjectProperties.RESERVATION_PARTNER_SERVICES);

        FileObject j2eeSamples = Repository.getDefault().
                getDefaultFileSystem().findResource("org-netbeans-modules-bpel-samples-resources-zip/ReservationPartnerServices.zip");// NOI18N

        SoaSampleUtils.unZipFile(j2eeSamples.getInputStream(), j2eeReservationProjectDir);
        resultSet.add(j2eeReservationProjectDir);

        return resultSet;
    }
    
    private Set<FileObject> createTRSCompositeApplicationProject(FileObject targetProjectDir, String name)
    throws FileNotFoundException, IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        
        FileObject compApptargetProjectDir = targetProjectDir.createFolder(name);                
        assert compApptargetProjectDir != null : "targetProjectDir for TRSCompositeApplicationProject project is null";
        
        FileObject trsCompositeApp = Repository.getDefault().
                getDefaultFileSystem().findResource("org-netbeans-modules-bpel-samples-resources-zip/TravelReservationServiceApplication.zip");// NOI18N
        SoaSampleUtils.unZipFile(trsCompositeApp.getInputStream(), compApptargetProjectDir);
                
        SoaSampleUtils.setProjectName(compApptargetProjectDir, 
                SoaSampleProjectProperties.COMPAPP_PROJECT_CONFIGURATION_NAMESPACE,
                name, "TravelReservationServiceApplication");                
        
        // add JbiModule
        Project compAppProject = ProjectManager.getDefault().findProject(compApptargetProjectDir);
        SoaSampleUtils.addJbiModule(compAppProject, getProject());
        
        resultSet.add(compApptargetProjectDir);               
        
        return resultSet;
    }
    
    public Set<FileObject> instantiate() throws IOException {
        Set<FileObject> resultSet = super.instantiate();
        
        FileObject dirParent = getProject().getProjectDirectory().getParent();
        try {
            Set<FileObject> jeeSet = createJ2eeReservationPartnerServicesProjects(dirParent);
            resultSet.addAll(jeeSet);

            String name = (String) wiz.getProperty(NAME) + "Application";
            Set<FileObject> compositeSet = createTRSCompositeApplicationProject(dirParent, name);
            resultSet.addAll(compositeSet);
            
        } catch(FileNotFoundException fnfe) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,fnfe);
        } catch(IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,ioe);
        }
        ProjectChooser.setProjectsFolder(FileUtil.toFile(dirParent.getParent()));
        return resultSet;
    }
}
