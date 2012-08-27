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
package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.spi.BuilderConnector;
import static org.netbeans.modules.hudson.ui.actions.Bundle.*;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * Action to display test failures.
 */
public class ShowFailures extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(ShowFailures.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(ShowFailures.class);
    private final HudsonJobBuild build;
    private final HudsonMavenModuleBuild moduleBuild;

    public ShowFailures(HudsonJobBuild build) {
        this(build, null);
    }

    public ShowFailures(HudsonMavenModuleBuild module) {
        this(module.getBuild(), module);
    }

    @Messages("ShowFailures.label=Show Test Failures")
    private ShowFailures(HudsonJobBuild build,
            HudsonMavenModuleBuild moduleBuild) {

        this.build = build;
        this.moduleBuild = moduleBuild;
        putValue(NAME, ShowFailures_label());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        HudsonInstance hudsonInstance = build.getJob().getInstance();
        if (hudsonInstance instanceof HudsonInstanceImpl) {
            HudsonInstanceImpl hudsonInstanceImpl =
                    (HudsonInstanceImpl) hudsonInstance;
            BuilderConnector builderClient = hudsonInstanceImpl.getBuilderConnector();
            if (moduleBuild != null) {
                builderClient.getFailureDisplayer().showFailures(moduleBuild);
            } else {
                builderClient.getFailureDisplayer().showFailures(build);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        HudsonInstance instance = build.getJob().getInstance();
        if (instance instanceof HudsonInstanceImpl) {
            BuilderConnector builderClient =
                    ((HudsonInstanceImpl) instance).getBuilderConnector();
            return builderClient.getFailureDisplayer() != null;
        }
        return false;
    }
}
