/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */


package org.netbeans.modules.maven.osgi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * intentional manual implementation of lookupprovider. for packagings other then bundle but with bundle-maven-plugin configuration
 * we want to have some osgi related impls in the lookup.
 * @author mkleint
 */
@LookupProvider.Registration(projectType="org-netbeans-modules-maven")
public class OSGILookupProvider implements LookupProvider {

    public OSGILookupProvider() {
    }
    
    @Override
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final InstanceContent ic = new InstanceContent();
        final Project prj = baseContext.lookup(Project.class);
        assert prj != null;
        final AccessQueryImpl access = new AccessQueryImpl(prj);
        final ForeignClassBundlerImpl bundler = new ForeignClassBundlerImpl(prj);
        final RecommendedTemplates templates = new RecommendedTemplates() {
            @Override
            public String[] getRecommendedTypes() {
                return new String[] {"osgi"};
            }
        };
        NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
        nbprj.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                checkContent(prj, ic, access, bundler, templates);
            }

        });
        checkContent(prj, ic, access, bundler, templates);
        return new AbstractLookup(ic);
    }
    
    private void checkContent(Project prj, InstanceContent ic, AccessQueryImpl access, ForeignClassBundlerImpl bundler, RecommendedTemplates templates) {
        NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
        String effPackaging = nbprj.getPackagingType();
        String[] types = PluginPropertyUtils.getPluginPropertyList(prj, OSGiConstants.GROUPID_FELIX, OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN, "supportedProjectTypes", "supportedProjectType", /*"bundle" would not work for GlassFish parent POM*/null);
        if (types != null) {
            for (String type : types) {
                if (effPackaging.equals(type)) {
                    effPackaging = NbMavenProject.TYPE_OSGI;
                }
            }
        }
        if (NbMavenProject.TYPE_OSGI.equals(effPackaging)) {
            ic.add(access);
            ic.add(bundler);
            ic.add(templates);
        } else {
            ic.remove(access);
            ic.remove(bundler);
            ic.remove(templates);
        }
    }

}
