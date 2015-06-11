/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.ui.project.activity;

import java.awt.event.MouseEvent;
import java.util.MissingResourceException;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import oracle.clouddev.server.profile.activity.client.api.Activity;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.ui.project.LinkLabel;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public final class DeploymentActivityDisplayer extends BuildActivityDisplayer {

    private static final String PROP_NAME = "name"; // NOI18N
    private static final String PROP_APPLICATION = "application"; // NOI18N
    private static final String PROP_TYPE = "type"; // NOI18N
    private static final String PROP_STATUS = "status"; // NOI18N
    private static final String PROP_JOB_NAME = "jobName"; // NOI18N
    private static final String PROP_BUILD_NUMBER = "buildNumber"; // NOI18N
    private static final String PROP_TARGET__TYPE = "target_type"; // NOI18N
    private static final String PROP_TARGET_IDMID = "target_idmId"; // NOI18N
    private static final String PROP_TARGET_SERVICEID = "target_serviceId"; // NOI18N
    private static final String PROP_TARGET_NAME = "target_name"; // NOI18N
    private static final String PROP_TARGET_URL = "target_url"; // NOI18N
    private static final String PROP_TARGET_HOST = "target_host"; // NOI18N
    private static final String STATUS_SUCCESS = "SUCCEEDED"; // NOI18N
    private static final String STATUS_FAILED = "FAILED"; // NOI18N

    public DeploymentActivityDisplayer(Activity activity, ProjectHandle<ODCSProject> projectHandle, int maxWidth) {
        super(activity, projectHandle, maxWidth);
    }

    @Override
    public JComponent getTitleComponent() {
        String application = activity.getProperty(PROP_APPLICATION);
        if (application == null || application.length() == 0) {
            application = activity.getProperty(PROP_NAME);
        }
        String type = activity.getProperty(PROP_TYPE);
        String status = activity.getProperty(PROP_STATUS);
        String bundleKey = (STATUS_SUCCESS.equalsIgnoreCase(status) ? "FMT_Deploy_" : "FMT_Deploy_F_") + type; // NOI18N
        final String configUrl = getDeployConfigURL();
        LinkLabel appLink = new LinkLabel(application) {
            @Override
            public void mouseClicked(MouseEvent e) {
                Utils.openBrowser(configUrl);
            }
        };
        JComponent targetLink = createTargetLink();
        try {
            return createMultipartTextComponent(bundleKey, appLink, targetLink);
        } catch (MissingResourceException ex) {
            return new JLabel(NbBundle.getMessage(DeploymentActivityDisplayer.class, "FMT_Deploy_Unknown", application)); // NOI18N
        }
    }

    @Override
    public JComponent getShortDescriptionComponent() {
        String jobName = activity.getProperty(PROP_JOB_NAME);
        String buildNumber = activity.getProperty(PROP_BUILD_NUMBER);
        return createMultipartTextComponent("FMT_Deploy_Build", createJobLink(jobName), createBuildLink(buildNumber));
    }

    @Override
    public Icon getActivityIcon() {
        // TODO we'd like to have distinct icons for deployments, for now using icons for builds
        String result = activity.getProperty(PROP_STATUS);
        if (STATUS_SUCCESS.equalsIgnoreCase(result)) {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_build_success.png", true); // NOI18N
        } else if (STATUS_FAILED.equalsIgnoreCase(result)) {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_build_failure.png", true); // NOI18N
        } else {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_build_unknown.png", true); // NOI18N
        }
    }

    private JComponent createTargetLink() {
        String targetType = activity.getProperty(PROP_TARGET__TYPE);
        String targetText = null;
        String targetUrl = null;
        if ("JCS-Saas-Local".equalsIgnoreCase(targetType)) { // NOI18N
            targetUrl = activity.getProperty(PROP_TARGET_URL);
            String idmId = activity.getProperty(PROP_TARGET_IDMID);
            String serviceId = activity.getProperty(PROP_TARGET_SERVICEID);
            if (idmId != null && serviceId != null) {
                targetText = NbBundle.getMessage(DeploymentActivityDisplayer.class, "FMT_JCS-Saas_Target", idmId, serviceId); // NOI18N
            }
        } else if ("JCS".equals(targetType)) { // NOI18N
            String targetHost = activity.getProperty(PROP_TARGET_HOST);
            if (targetHost != null) {
                targetUrl = "https://" + targetHost + ":7002/console"; // NOI18N
            }
        }
        if (targetText == null) {
            targetText = activity.getProperty(PROP_TARGET_NAME);
        }
        if (targetUrl == null) {
            return new JLabel(targetText);
        }
        final String targetUrl_f = targetUrl;
        return new LinkLabel(targetText) {
            @Override
            public void mouseClicked(MouseEvent e) {
                Utils.openBrowser(targetUrl_f);
            }
        };
    }

    private String getDeployConfigURL() {
        return projectHandle.getTeamProject().getWebUrl() + "/deployments"; // NOI18N
    }
}
