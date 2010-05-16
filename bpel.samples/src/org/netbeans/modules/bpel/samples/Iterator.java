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

import java.io.IOException;
import java.util.Set;

import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.xml.samples.SampleIterator;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.06.14
 */
public class Iterator extends SampleIterator {

    public static Iterator createAsynchronous() {
        return new Iterator("Asynchronous"); // NOI18N
    }

    public static Iterator createSynchronous() {
        return new Iterator("Synchronous"); // NOI18N
    }

    public static Iterator createTravelReservationService() {
        return new TravelReservationServiceIterator();
    }

    public static Iterator createBluePrint1() {
        return new Iterator("BluePrint1"); // NOI18N
    }

    public static Iterator createBluePrint2() {
        return new Iterator("BluePrint2"); // NOI18N
    }

    public static Iterator createBluePrint3() {
        return new Iterator("BluePrint3"); // NOI18N
    }

    public static Iterator createBluePrint4() {
        return new Iterator("BluePrint4"); // NOI18N
    }

    public static Iterator createBluePrint5() {
        return new Iterator("BluePrint5"); // NOI18N
    }

    protected Iterator(String name) {
        super("bpel", name); // NOI18N
    }

    protected void addArtifact(Project project, AntArtifact artifact) {
        new AddProjectAction().addProject(project, artifact);
    }

    // ---------------------------------------------------------------------------
    private static class TravelReservationServiceIterator extends Iterator {

        protected TravelReservationServiceIterator() {
            super("TravelReservationService"); // NOI18N
        }

        protected Set<FileObject> createProjectApp(FileObject folder, String name) throws IOException {
            Set<FileObject> set = super.createProjectApp(folder, name);
            addProject(set, folder, "ReservationPartnerServices", "org-netbeans-modules-bpel-samples-resources-zip/ReservationPartnerServices.zip"); // NOI18N
            return set;
        }
    }
}
