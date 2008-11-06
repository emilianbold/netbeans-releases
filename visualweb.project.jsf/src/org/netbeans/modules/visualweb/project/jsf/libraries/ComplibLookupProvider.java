/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package org.netbeans.modules.visualweb.project.jsf.libraries;

import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.complib.api.ComplibService;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Code used to initialize and clean up complibs associated with a project.
 * 
 * @author Edwin Goei
 */
@LookupProvider.Registration(projectType="org-netbeans-modules-web-project")
public class ComplibLookupProvider implements LookupProvider {

    public static class ComplibProjectOpenedHook extends ProjectOpenedHook {

        private Project project;

        private ComplibProjectOpenedHook(Project project) {
            this.project = project;
        }

        @Override
        protected void projectOpened() {
            ComplibService complibService = Lookup.getDefault().lookup(ComplibService.class);
            if (complibService != null) {
                complibService.initProjectComplibs(project);
            }
        }

        @Override
        protected void projectClosed() {
            ComplibService complibService = Lookup.getDefault().lookup(ComplibService.class);
            if (complibService != null) {
                complibService.cleanUpProjectComplibs(project);
            }
        }

    }

    public Lookup createAdditionalLookup(Lookup baseContext) {
        Project project = baseContext.lookup(Project.class);
        if (project == null) {
            assert false : "Unable to derive Project";
            return Lookup.EMPTY;
        }

        ComplibProjectOpenedHook projectOpenedHook = new ComplibProjectOpenedHook(project);
        return Lookups.fixed(projectOpenedHook);
    }
}
