/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee.osgi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.CopyOnSave;
import org.netbeans.modules.maven.j2ee.EMGSResolverImpl;
import org.netbeans.modules.maven.j2ee.JPAStuffImpl;
import org.netbeans.modules.maven.j2ee.MavenPersistenceProviderSupplier;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.maven.j2ee.web.EntRefContainerImpl;
import org.netbeans.modules.maven.j2ee.web.MavenWebProjectWebRootProvider;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.modules.maven.j2ee.web.WebReplaceTokenProvider;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Provide additional lookup objects for bundle (OSGI) type (see #179584 for more detail)
 * @author Martin Janicek
 */
@LookupProvider.Registration(projectType = {"org-netbeans-modules-maven/" + NbMavenProject.TYPE_OSGI})
public class OsgiLookupProvider implements LookupProvider, PropertyChangeListener {

    private Project project;
    private InstanceContent ic;
    
    
    @Override
    public Lookup createAdditionalLookup(Lookup baseLookup) {
        project = baseLookup.lookup(Project.class);
        ic = new InstanceContent();
        changeAdditionalLookups();
        
        NbMavenProject.addPropertyChangeListener(project, this);
        
        return new AbstractLookup(ic);
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if (NbMavenProject.PROP_PROJECT.equals(propertyChangeEvent.getPropertyName())) {
            changeAdditionalLookups();
        }
    }
    
    private void changeAdditionalLookups() {
        String packaging = project.getLookup().lookup(NbMavenProject.class).getPackagingType();
        
        ic = new InstanceContent(); // Clear old content
        if (MavenProjectSupport.isBundlePackaging(project, packaging)) {
            WebModuleProviderImpl provider = new WebModuleProviderImpl(project);
            CopyOnSave copyOnSave = provider.getCopyOnSaveSupport();
            try {
                if (copyOnSave != null) {
                    copyOnSave.initialize();
                    ic.add(copyOnSave);
                }
            } catch (FileStateInvalidException ex) {
                ex.printStackTrace();
            }
            
            ic.add(provider);
            ic.add(new WebReplaceTokenProvider(project));
            ic.add(new EntRefContainerImpl(project));
            ic.add(new JPAStuffImpl(project));
            ic.add(new EMGSResolverImpl());
            ic.add(new MavenPersistenceProviderSupplier(project));
            ic.add(new MavenWebProjectWebRootProvider(project));
        }
    }
}
