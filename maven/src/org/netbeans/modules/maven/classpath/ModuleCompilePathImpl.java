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

package org.netbeans.modules.maven.classpath;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import org.openide.util.Utilities;

/**
 * 
 * XXX Extremely naive impl.
 * 
 * @author Tomas Stupka
 */
class ModuleCompilePathImpl extends AbstractProjectClassPathImpl implements FlaggedClassPathImplementation {

    private volatile boolean incomplete;
    private final boolean test;

    public ModuleCompilePathImpl(NbMavenProjectImpl proj, boolean test) {
        super(proj);
        this.test = test;
    }
    
    @Override
    URI[] createPath() {
        boolean[] broken = {false};
        URI[] uris = getElements(broken);
        if (incomplete != broken[0]) {
            incomplete = broken[0];
            firePropertyChange(PROP_FLAGS, null, null);
        }
        return uris;
    }

    @Override
    public Set<ClassPath.Flag> getFlags() {
        return incomplete ?
            EnumSet.of(ClassPath.Flag.INCOMPLETE) :
            Collections.<ClassPath.Flag>emptySet();
    }
    

    private URI[] getElements(boolean[] broken) {
        MavenProject project = getProject().getOriginalMavenProject();                
        List<URI> list = new ArrayList<>( project.getArtifacts().size() + 1 );
        list.add( Utilities.toURI(getProject().getProjectWatcher().getOutputDirectory(test)) );
        for(Artifact a : project.getArtifacts()) {
            URI uri = getValidCompileClasspathElement(a, broken);
            if(uri != null) {
                list.add(uri);
            }
        }
        return list.toArray(new URI[list.size()]);
    }

    /**
     * @see org.apache.maven.plugin.compiler.JavaMavenProjectUtils
     */
    private static URI getValidCompileClasspathElement(Artifact a, boolean[] broken) {
        boolean isClassPathElement = a.getArtifactHandler().isAddedToClasspath() && 
                                    (Artifact.SCOPE_COMPILE.equals(a.getScope()) ||
                                     Artifact.SCOPE_PROVIDED.equals(a.getScope()) ||
                                     Artifact.SCOPE_SYSTEM.equals(a.getScope()));
        if(isClassPathElement) {
            if(a.getFile() != null) {
                return Utilities.toURI(a.getFile());
            } else {
                broken[0] = false;
            }
        }
        return null;
    }
    
}
