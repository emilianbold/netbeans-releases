/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.URI;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.apisupport.project.*;

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
            String jarloc = eval.evaluate("${netbeans.dest.dir}/${cluster.dir}/${module.jar}"); // NOI18N
            File jar = helper.resolveFile(jarloc); // probably absolute anyway, now
            return new URI[] {
                // This is a relative URI:
                URI.create(PropertyUtils.relativizeFile(FileUtil.toFile(project.getProjectDirectory()), jar)),
                // XXX should it add in class path extensions?
            };
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
