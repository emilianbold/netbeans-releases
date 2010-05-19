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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.api;

import java.io.File;
import java.net.URI;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.openide.filesystems.FileObject;

/**
 * Type to put in the lookup of Java Card projects, which allows a Card
 * instance to find the JAR to deploy and compute the entire classpath
 * closure to pass to the debugger.  Includes fallback implementation over
 * AntArtifactProvider for projects that do not provide an
 * AntClasspathClosureProvider.
 *
 * @author Tim Boudreau
 */
public abstract class AntClasspathClosureProvider {
    public abstract String getClasspathClosureAsString();
    public abstract File getTargetArtifact();
    public static String getClasspathClosure(Project p) {
        AntClasspathClosureProvider c = p.getLookup().lookup(AntClasspathClosureProvider.class);
        if (c != null) {
            return c.getClasspathClosureAsString();
        } else {
            return findClasspathClosure(p);
        }
    }

    public static File getTargetArtifact (Project p) {
        AntClasspathClosureProvider c = p.getLookup().lookup(AntClasspathClosureProvider.class);
        if (c != null) {
            return c.getTargetArtifact();
        } else {
            return findTargetArtifact(p);
        }
    }

    private static File findTargetArtifact(Project p) {
        AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
        for (AntArtifact a : prov.getBuildArtifacts()) {
            if (JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(a.getType())) {
                for (URI uri : a.getArtifactLocations()) {
                    //XXX check if it is a relative URI
                    try {
                        return new File(uri);
                    } catch (Exception e) {
                        FileObject pdir = p.getProjectDirectory();
                        try {
                            String url = pdir.getURL().toURI().toString();
                            if (!url.endsWith("/") && !uri.toString().startsWith("/")) { //NOI18N
                                url += "/"; //NOI18N
                            }
                            URI fullUri = new URI(url + uri);
                            return new File(fullUri);
                        } catch (Exception ex) {
                            throw new IllegalStateException ("Could not find " + //NOI18N
                                    "target artifact from project in " + //NOI18N
                                    p.getProjectDirectory().getPath() +":" + uri, //NOI18N
                                    ex);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String findClasspathClosure(Project p) {
        ClassPathProvider prov = p.getLookup().lookup(ClassPathProvider.class);
        File jar = findTargetArtifact(p);
        String target = jar.getAbsolutePath();
        //XXX get compile classpath
        return target;
    }
}
