/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.openide.ErrorManager;

/**
 * A basic implementation of {@link AntArtifact} which assumes everything of interest
 * is in a fixed location under a standard Ant-based project.
 * @author Jesse Glick
 */
public final class SimpleAntArtifact extends AntArtifact {
    
    private final AntProjectHelper h;
    private final String type;
    private final String locationProperty;
    private final String targetName;
    private final String cleanTargetName;
    
    /**
     * Create an artifact object.
     * @param helper an Ant project helper object
     * @param type the type of artifact, e.g. {@link AntArtifact#TYPE_JAR}
     * @param locationProperty an Ant property name giving the project-relative
     *                         location of the artifact, e.g. <samp>dist.jar</samp>
     * @param targetName the name of an Ant target which will build the artifact,
     *                   e.g. <samp>jar</samp>
     * @param cleanTargetName the name of an Ant target which will delete the artifact
     *                        (and maybe other build products), e.g. <samp>clean</samp>
     */
    public SimpleAntArtifact(AntProjectHelper helper, String type, String locationProperty, String targetName, String cleanTargetName) {
        this.h = helper;
        this.type = type;
        this.locationProperty = locationProperty;
        this.targetName = targetName;
        this.cleanTargetName = cleanTargetName;
    }
    
    public URI getArtifactLocation() {
        String locationResolved = h.evaluate(locationProperty);
        try {
            return new URI(locationResolved);
        } catch (URISyntaxException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return URI.create("BROKEN"); // NOI18N
        }
    }
    
    public String getCleanTargetName() {
        return cleanTargetName;
    }
    
    public File getScriptLocation() {
        return h.resolveFile(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    public String getTargetName() {
        return targetName;
    }
    
    public String getType() {
        return type;
    }
    
    public Project getProject() {
        return AntBasedProjectFactorySingleton.getProjectFor(h);
    }
    
}
