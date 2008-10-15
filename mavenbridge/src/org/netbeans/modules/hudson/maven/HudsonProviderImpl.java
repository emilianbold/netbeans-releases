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

import javax.swing.event.ChangeListener;
import org.apache.maven.model.CiManagement;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hudson.spi.ProjectHudsonProvider;
import org.netbeans.modules.maven.api.NbMavenProject;

/**
 *
 * @author mkleint
 */
public class HudsonProviderImpl implements ProjectHudsonProvider {

    private Project project;

    public HudsonProviderImpl(Project project) {
        this.project = project;
    }

    private CiManagement getCIManag() {
        NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
        CiManagement cim = prj.getMavenProject().getCiManagement();
        return cim;

    }

    public String getServerUrl() {
        CiManagement mag = getCIManag();
        if (mag != null) {
            String url = mag.getUrl();
            int index = url.indexOf("/job/");
            if (index > 0) {
                url = url.substring(0, index);
            }
            return url;
        }
        return "http://localhost";
    }

    public String getName() {
        return getServerUrl();
    }

    public String getJobName() {
        CiManagement mag = getCIManag();
        if (mag != null) {
            String url = mag.getUrl();
            int index = url.indexOf("/job/");
            if (index > 0) {
                url = url.substring(index + "/job/".length());
            }
            return url;
        }
        return "";
    }

    public void addChangeListener(ChangeListener arg0) {
    }

    public void removeChangeListener(ChangeListener arg0) {
    }
}
