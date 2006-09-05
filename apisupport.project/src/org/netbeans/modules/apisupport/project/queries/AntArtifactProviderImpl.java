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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 * Provide the module JAR as an exported artifact that other projects could import.
 */
public final class AntArtifactProviderImpl implements AntArtifactProvider {
    
    private final NbModuleProject project;
    private final PropertyEvaluator eval;
    private final AntProjectHelper helper;
    
    public AntArtifactProviderImpl(NbModuleProject project, AntProjectHelper helper, PropertyEvaluator eval) {
        this.project = project;
        this.eval = eval;
        this.helper = helper;
    }
    
    public AntArtifact[] getBuildArtifacts() {
        return new AntArtifact[] {
            new NbmAntArtifact(),
        };
    }
    
    private final class NbmAntArtifact extends AntArtifact {
        
        public NbmAntArtifact() {}

        public String getID() {
            return "module"; // NOI18N
        }

        public File getScriptLocation() {
            return helper.resolveFile("build.xml"); // NOI18N
        }

        public String getType() {
            return JavaProjectConstants.ARTIFACT_TYPE_JAR;
        }

        public URI[] getArtifactLocations() {
            String jarloc = eval.evaluate("${cluster}/${module.jar}"); // NOI18N
            File jar = helper.resolveFile(jarloc); // probably absolute anyway, now
            String reldir = PropertyUtils.relativizeFile(project.getProjectDirectoryFile(), jar);
            if (reldir != null) {
                try {
                    return new URI[] {new URI(null, null, reldir, null)};
                } catch (URISyntaxException e) {
                    throw new AssertionError(e);
                }
            } else {
                return new URI[] {jar.toURI()};
            }
            // XXX should it add in class path extensions?
        }
        
        public String getTargetName() {
            return "netbeans"; // NOI18N
        }

        public String getCleanTargetName() {
            return "clean"; // NOI18N
        }

        public Project getProject() {
            return project;
        }

    }
    
}
