/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.License;
import org.apache.maven.model.Organization;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=CreateFromTemplateAttributesProvider.class, projectType="org-netbeans-modules-maven")
public class TemplateAttrProvider implements CreateFromTemplateAttributesProvider {

    private final Project project;
    
    public TemplateAttrProvider(Project prj) {
        project = prj;
    }
    
    public @Override Map<String,?> attributesFor(DataObject template, DataFolder target, String name) {
        Map<String,String> values = new TreeMap<String,String>();
        String license = project.getLookup().lookup(AuxiliaryProperties.class).get(Constants.HINT_LICENSE, true); //NOI18N
        MavenProject mp = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
        if (license == null) {
            // try to match the project's license URL and the mavenLicenseURL attribute of license template
            List<License> lst = mp.getLicenses();
            if (!lst.isEmpty()) {
                String url = lst.get(0).getUrl();
                FileObject licenses = FileUtil.getConfigFile("Templates/Licenses"); //NOI18N
                if (url != null && licenses != null) {
                    for (FileObject fo : licenses.getChildren()) {
                        String str = (String)fo.getAttribute("mavenLicenseURL"); //NOI18N
                        if (str != null && str.equalsIgnoreCase(url)) {
                            if (fo.getName().startsWith("license-")) { // NOI18N
                                license = fo.getName().substring("license-".length()); //NOI18N
                            } else {
                                Logger.getLogger(TemplateAttrProvider.class.getName()).log(Level.WARNING, "Bad license file name {0} (expected to start with ''license-'' prefix)", fo.getName());
                            }
                            break;
                        }
                    }
                }
            }
        }
        if (license != null) {
            values.put("license", license); // NOI18N
        }

        Organization organization = mp.getOrganization();
        if (organization != null) {
            String organizationName = organization.getName();
            if (organizationName != null) {
                values.put("organization", organizationName); // NOI18N
            }
        }

        FileEncodingQueryImplementation enc = project.getLookup().lookup(FileEncodingQueryImplementation.class);
        Charset charset = enc.getEncoding(target.getPrimaryFile());
        String encoding = (charset != null) ? charset.name() : null;
        if (encoding != null) {
            values.put("encoding", encoding); // NOI18N
        }

        ProjectInformation pi = ProjectUtils.getInformation(project);
        values.put("name", mp.getArtifactId()); // NOI18N
        values.put("displayName", pi.getDisplayName()); // NOI18N

        if (values.size() > 0) {
            return Collections.singletonMap("project", values); // NOI18N
        } else {
            return null;
        }
    }
}
