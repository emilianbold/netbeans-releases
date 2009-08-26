/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.javacard.project.deps;

import java.util.EnumSet;
import java.util.Set;
import org.openide.util.NbBundle;

/**
 * Different flavors of things a project may have a dependency on.
 *
 * @author Tim Boudreau
 */
public enum DependencyKind {
    CLASSIC_LIB,
    EXTENSION_LIB,
    JAVA_PROJECT,
    RAW_JAR,
    CLASSIC_LIB_JAR,
    EXTENSION_LIB_JAR,
    JAR_WITH_EXP_FILE;

    public boolean isProjectDependency() {
        return this == CLASSIC_LIB || this == EXTENSION_LIB;
    }

    public boolean isOriginAFolder() {
        return isProjectDependency();
    }

    public Set<ArtifactKind> supportedArtifacts() {
        switch (this) {
            case CLASSIC_LIB :
            case JAVA_PROJECT :
                return EnumSet.of(ArtifactKind.ORIGIN);
            case EXTENSION_LIB :
            case CLASSIC_LIB_JAR :
                return EnumSet.of(ArtifactKind.ORIGIN, ArtifactKind.SIG_FILE);
            case EXTENSION_LIB_JAR :
            case RAW_JAR :
                return EnumSet.of(ArtifactKind.ORIGIN, ArtifactKind.SOURCES_PATH);
            case JAR_WITH_EXP_FILE :
                return EnumSet.of(ArtifactKind.ORIGIN, ArtifactKind.SOURCES_PATH, ArtifactKind.EXP_FILE);
            default :
                throw new AssertionError();
        }
    }

    public Set<DeploymentStrategy> supportedDeploymentStrategies() {
        switch (this) {
            case EXTENSION_LIB:
            case EXTENSION_LIB_JAR:
            case JAR_WITH_EXP_FILE :
            case CLASSIC_LIB_JAR:
            case CLASSIC_LIB :
                return EnumSet.of(DeploymentStrategy.DEPLOY_TO_CARD, DeploymentStrategy.ALREADY_ON_CARD);
            case RAW_JAR :
                return EnumSet.of(DeploymentStrategy.ALREADY_ON_CARD, DeploymentStrategy.INCLUDE_IN_PROJECT_CLASSES);
            default :
                throw new AssertionError();
        }
    }

    public static DependencyKind parse (String s) {
        return DependencyKind.valueOf(s);
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(DependencyKind.class, name());
    }
}
