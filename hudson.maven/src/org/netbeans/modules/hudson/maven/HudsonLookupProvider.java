/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.hudson.maven;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.maven.model.CiManagement;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hudson.spi.ProjectHudsonProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author mkleint
 */
public class HudsonLookupProvider implements LookupProvider {

    public Lookup createAdditionalLookup(Lookup baseContext) {
        Project project = baseContext.lookup(Project.class);
        if (project == null) {
            throw new IllegalStateException("Lookup " + baseContext + " does not contain a Project");
        }
//        // if there's more items later, just do a proxy..
        InstanceContent ic = new InstanceContent();
        Provider prov = new Provider(project, ic);
        return prov;
    }

    public static class Provider extends AbstractLookup implements  PropertyChangeListener {
        private Project project;
        private InstanceContent content;
        private ProjectHudsonProvider hudson;
        public Provider(Project proj, InstanceContent cont) {
            super(cont);
            project = proj;
            content = cont;
            checkHudson();
            NbMavenProject.addPropertyChangeListener(project, this);
        }

        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (NbMavenProject.PROP_PROJECT.equals(propertyChangeEvent.getPropertyName())) {
                checkHudson();
            }
        }

        private void checkHudson() {
            NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
            CiManagement cim = prj.getMavenProject().getCiManagement();
            if (cim != null && cim.getSystem() != null && "hudson".equals(cim.getSystem())) {
                if (hudson == null) {
                    hudson = new HudsonProviderImpl(project);
                    content.add(hudson);
                }
            } else {
                if (hudson != null) {
                    content.remove(hudson);
                    hudson = null;
                }
            }
        }
    }

}
