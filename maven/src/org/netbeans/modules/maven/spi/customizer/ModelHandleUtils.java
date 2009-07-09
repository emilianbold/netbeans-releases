/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.spi.customizer;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.execute.UserActionGoalProvider;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.profile.ProfilesModel;
import org.netbeans.modules.maven.model.profile.ProfilesModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Some random utility methods to allow post creation modifications of the project model.
 * 
 * @author mkleint
 */
public final class ModelHandleUtils {
    
    private ModelHandleUtils() {}

    //TODO deprecate in favour of o.n.m.maven.model.Utilities + ModelOperation?
    public static ModelHandle createModelHandle(Project prj) throws IOException, XmlPullParserException {
        NbMavenProjectImpl project = prj.getLookup().lookup(NbMavenProjectImpl.class);
        FileObject pom = FileUtil.toFileObject(project.getPOMFile());
        ModelSource source = Utilities.createModelSource(pom);
        POMModel model = POMModelFactory.getDefault().getModel(source);
        FileObject profilesFO = prj.getProjectDirectory().getFileObject("profiles.xml"); //NOI18N
        if (profilesFO != null) {
            source = Utilities.createModelSource(profilesFO);
        } else {
            //the file doesn't exist. what now?
            File file = FileUtil.toFile(prj.getProjectDirectory());
            file = new File(file, "profiles.xml"); //NOI18N
            source = Utilities.createModelSourceForMissingFile(file, true, CustomizerProviderImpl.PROFILES_SKELETON, "text/x-maven-profile+xml"); //NOI18N
        }
        ProfilesModel profilesModel = ProfilesModelFactory.getDefault().getModel(source);
        UserActionGoalProvider usr = project.getLookup().lookup(org.netbeans.modules.maven.execute.UserActionGoalProvider.class);
        ActionToGoalMapping mapping = new NetbeansBuildActionXpp3Reader().read(new StringReader(usr.getRawMappingsAsString()));
        return CustomizerProviderImpl.ACCESSOR.createHandle(model, profilesModel, project.getOriginalMavenProject(),
                Collections.<String, ActionToGoalMapping>singletonMap(M2Configuration.DEFAULT,mapping), null, null, project.getAuxProps());
    }
    
    //TODO deprecate in favour of o.n.m.maven.model.Utilities + ModelOperation?
    public static void writeModelHandle(ModelHandle handle, Project prj) throws IOException {
        NbMavenProjectImpl project = prj.getLookup().lookup(NbMavenProjectImpl.class);
        CustomizerProviderImpl.writeAll(handle, project);
    }
}

