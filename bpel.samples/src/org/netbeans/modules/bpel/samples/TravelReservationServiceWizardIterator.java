/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class TravelReservationServiceWizardIterator extends SampleWizardIterator {
    private static final long serialVersionUID = 1L;
    
    public static TravelReservationServiceWizardIterator createIterator() {
      return new TravelReservationServiceWizardIterator();
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
      return new WizardDescriptor.Panel[] { new TravelReservationServiceWizardPanel() };
    }
    
    protected String[] createSteps() {
      return new String[] { NbBundle.getMessage(TravelReservationServicePanelVisual.class, "MSG_CreateTravelReservatioService")}; // NOI18N
    }
    
    private Set<FileObject> createJ2eeReservationPartnerServicesProjects(FileObject projectDir) throws IOException {
      Set<FileObject> resultSet = new HashSet<FileObject>();
      FileObject j2eeProjectDir = projectDir.createFolder(Util.RESERVATION_PARTNER_SERVICES);

      FileObject j2eeSamples = FileUtil.getConfigFile("org-netbeans-modules-bpel-samples-resources-zip/ReservationPartnerServices.zip");// NOI18N

      Util.unZipFile(j2eeSamples.getInputStream(), j2eeProjectDir);
      resultSet.add(j2eeProjectDir);

      // # 125456 vlv
      // jdk5: j2ee.server.type=JavaEEPlusSIP, J2EE
      // jdk6: j2ee.server.type=GlassFishV1,   J2EE
//    if (System.getProperty("java.version").startsWith("1.5")) {
//      Util.renameInProperties(j2eeProjectDir, /* new */ "j2ee.server.type=J2EE", /* old */ "j2ee.server.type=J2EE");
//    }
      return resultSet;
    }
    
    protected Set<FileObject> createCompositeApplicationProject(FileObject projectDir, String name) throws IOException {
      Set<FileObject> resultSet = createJ2eeReservationPartnerServicesProjects(projectDir);
      FileObject compAppProjectDir = projectDir.createFolder(name);                
      
      FileObject trsCompositeApp = FileUtil.getConfigFile("org-netbeans-modules-bpel-samples-resources-zip/TravelReservationServiceApplication.zip"); // NOI18N

      Util.unZipFile(trsCompositeApp.getInputStream(), compAppProjectDir);
      Util.setProjectName(compAppProjectDir, Util.COMPAPP_PROJECT_CONFIGURATION_NAMESPACE, name, "TravelReservationServiceApplication"); // NOI18N
      
      Util.addJbiModule(compAppProjectDir, getProjectDir());
      resultSet.add(compAppProjectDir);               
      
      return resultSet;
    }
}
