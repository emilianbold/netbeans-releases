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

import org.openide.util.Parameters;

/**
 * Represents one dependency of a Java Card project, as defined in the
 * project.xml.  May be another project,
 * a library JAR with a .exp file or a raw JAR, classic applet JAR,
 * classic library JAR, extension library JAR, or extended applet JAR.
 * Each dependency has a deployment strategy which determines what the build
 * script should do with it on deploy, a set of supported artifact kinds,
 * and property names for each artifact kind.
 * <p/>
 * An instance of Dependency represents only the info in project.xml.  To
 * resolve the files associated with a dependency, see ResolvedDependency
 * and ResolvedDependencies.  These will actually read from the project.properties
 * and allow getting/setting of targets, etc.
 *
 * @author Tim Boudreau
 */
public final class Dependency {
    private final DependencyKind kind;
    private final DeploymentStrategy strategy;
    private final String id;
    protected static final String DEP_PROPERTY_PREFIX = "dependency."; //NOI18N

    public Dependency(String id, DependencyKind kind, DeploymentStrategy strategy) {
        Parameters.notNull ("id", id); //NOI18N
        Parameters.notNull("kind", kind); //NOI18N
        Parameters.notNull("strategy", strategy); //NOI18N
        Parameters.notEmpty("id", id); //NOI18N
        this.id = id;
        this.kind = kind;
        this.strategy = strategy;
    }

    public static Dependency convert (Dependency orig, DependencyKind newKind) {
        return new Dependency (orig.getID(), newKind, orig.getDeploymentStrategy());
    }

    @Override
    public String toString() {
        return super.toString() + "[" + id + " : " + kind + " : " + strategy + "]";
    }

    public String getPropertyName (ArtifactKind kind) {
        switch (kind) {
            case SOURCES_PATH :
                return getSourceLocationPropertyName();
            case ORIGIN :
                return getOriginPropertyName();
            case EXP_FILE :
                return getExpFilePropertyName();
            case SIG_FILE :
                if (getKind().supportedArtifacts().contains(kind)) {
                    return getSigFilePropertyName();
                } else {
                    throw new IllegalArgumentException (getKind() + " does not " + //NOI18N
                            "support " + kind + " artifacts"); //NOI18N
                }
            default :
                throw new AssertionError();
        }
    }

    private String getOriginPropertyName() {
        return DEP_PROPERTY_PREFIX + id + '.' + "origin"; //NOI18N
    }

    public String getDeploymentStrategyPropertyName() {
        return DEP_PROPERTY_PREFIX + id + '.' + "deployment"; //NOI18N
    }

    private String getSourceLocationPropertyName() {
        return DEP_PROPERTY_PREFIX + getID() + '.' + "sourcepath"; //NOI18N
    }

    private String getExpFilePropertyName() {
        return DEP_PROPERTY_PREFIX +  getID() + ".expfile"; //NOI18N
    }

    private String getSigFilePropertyName() {
        return DEP_PROPERTY_PREFIX + '.' + getID() + ".sigfile"; //NOI18N
    }

    public DependencyKind getKind() {
        return kind;
    }

    public DeploymentStrategy getDeploymentStrategy() {
        return strategy;
    }

    public final String getID() {
        return id;
    }

    public Dependency copy() {
        return new Dependency(id, kind, strategy);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Dependency &&
                ((Dependency) o).getID().equals(getID());/* &&
                ((Dependency) o).getKind().equals(getKind()) &&
                ((Dependency) o).getDeploymentStrategy().equals(getDeploymentStrategy()); */
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 1951 * hash + (kind.hashCode());
        return hash;
    }
}
