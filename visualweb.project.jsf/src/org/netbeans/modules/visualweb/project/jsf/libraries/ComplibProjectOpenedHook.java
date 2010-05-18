/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package org.netbeans.modules.visualweb.project.jsf.libraries;

import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.complib.api.ComplibService;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Lookup;

/**
 * Code used to initialize and clean up complibs associated with a project.
 *
 * @author Edwin Goei
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-web-project")
public class ComplibProjectOpenedHook extends ProjectOpenedHook {

        private Project project;

        public ComplibProjectOpenedHook(Project project) {
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
