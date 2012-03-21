/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2008-2012 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.testng.maven;

import java.util.Collections;
import java.util.logging.Logger;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Activation;
import org.netbeans.modules.maven.model.pom.ActivationProperty;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.filesystems.FileObject;

/*
<profiles>
<profile>
<id>netbeans-private-xxx</id>
<activation>
<property>
<name>netbeans.testng.action</name>
</property>
</activation>
<build>
<plugins>
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-surefire-plugin</artifactId>
<configuration>
<suiteXmlFiles>
<suiteXmlFile>testng.xml</suiteXmlFile>
</suiteXmlFiles>
</configuration>
</plugin>
</plugins>
</build>
</profile>
</profiles>
 */
/**
 *
 * @author lukas
 */
public class MavenModelUtils {

    private static final Logger LOGGER = Logger.getLogger(MavenModelUtils.class.getName());

    private static final String PROFILE_NAME = "netbeans-private-testng"; //NOI18N

    public static void addProfile(FileObject fo, final String fileName) {
        assert fo != null;
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public void performOperation(POMModel model) {
                if (!State.VALID.equals(model.getState())) {
                    return;
                }
                Profile prof = model.getProject().findProfileById(PROFILE_NAME);
                if (prof != null) {
                    return;
                }
                prof = model.getFactory().createProfile();
                prof.setId(PROFILE_NAME);
                Activation act =  model.getFactory().createActivation();
                ActivationProperty prop = model.getFactory().createActivationProperty();
                prop.setName("netbeans.testng.action"); //NOI18N
                act.setActivationProperty(prop);
                prof.setActivation(act);

                BuildBase base = model.getFactory().createBuildBase();
                Plugin plug = model.getFactory().createPlugin();
                plug.setGroupId(Constants.GROUP_APACHE_PLUGINS);
                plug.setArtifactId(Constants.PLUGIN_SUREFIRE);
                plug.setVersion("2.11"); //NOI18N
                Configuration conf = model.getFactory().createConfiguration();
                POMExtensibilityElement suite = model.getFactory().createPOMExtensibilityElement(
                        POMQName.createQName("suiteXmlFiles", model.getPOMQNames().isNSAware()));//NOI18N
                suite.setChildElementText("suiteXmlFile", fileName, //NOI18N
                        POMQName.createQName("suiteXmlFile", model.getPOMQNames().isNSAware()));//NOI18N
                conf.addExtensibilityElement(suite);
                plug.setConfiguration(conf);
                base.addPlugin(plug);
                prof.setBuildBase(base);
                model.getProject().addProfile(prof);
            }
        };
        Utilities.performPOMModelOperations(fo, Collections.singletonList(operation));
    }
}