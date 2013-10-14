/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project.ui.customizer;

import java.io.IOException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2me.project.J2MEProject;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;




/**
 *
 * @author Tomas Zezula
 */
public final class J2MEProjectProperties {
        
    public static final String PLATFORM_ANT_NAME = "platform.ant.name";  //NOI18N
    public static final String PLATFORM_TYPE_J2ME = "j2me"; //NOI18N
    public static String PLATFORM_SDK = "platform.sdk"; //NOI18N


    private final J2MEProject project;

    public J2MEProject getProject() {
        return project;
    }

    public PropertyEvaluator getEvaluator() {
        return project.evaluator();
    }   

    /**
     * Creates a new instance of J2MEProjectProperties
     */
    J2MEProjectProperties(@NonNull final J2MEProject project) {
        this.project = project;
    }

    void collectData() {

    }

    void storeData() {
        
    }
    
    public static boolean isTrue(final String value) {
        return value != null &&
                (value.equalsIgnoreCase("true") ||  //NOI18N
                 value.equalsIgnoreCase("yes") ||   //NOI18N
                 value.equalsIgnoreCase("on"));     //NOI18N
    }
           
    public void store() throws IOException {
    }    
    
    private J2MECompilingPanel compilingPanel = null;
    public J2MECompilingPanel getCompilingPanel() {
        if (compilingPanel == null) {
            compilingPanel = new J2MECompilingPanel(this);
        }
        return compilingPanel;
    }
    
    private J2MEPackagingPanel packagingPanel = null;
    public J2MEPackagingPanel getPackagingPanel() {
        if (packagingPanel == null) {
            packagingPanel = new J2MEPackagingPanel(this);
        }
        return packagingPanel;
    }
    
    private J2MERunPanel runPanel = null;
    public J2MERunPanel getRunPanel() {
        if (runPanel == null) {
            runPanel = new J2MERunPanel(this);
        }
        return runPanel;
    }
    
    private J2MEApplicationPanel applicationPanel = null;
    public J2MEApplicationPanel getApplicationPanel() {
        if (applicationPanel == null) {
            applicationPanel = new J2MEApplicationPanel(this);
        }
        return applicationPanel;
    }
    
    private J2MEDeploymentPanel deploymentPanel = null;
    public J2MEDeploymentPanel getDeploymentPanel() {
        if (deploymentPanel == null) {
            deploymentPanel = new J2MEDeploymentPanel(this);
        }
        return deploymentPanel;
    }
}
