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
 * A basic AntArtifact implementation.
 * @see AntProjectHelper#createSimpleAntArtifact
 * @author Jesse Glick
 */
final class SimpleAntArtifact extends AntArtifact {
    
    private final AntProjectHelper h;
    private final String type;
    private final String locationProperty;
    private final PropertyEvaluator eval;
    private final String targetName;
    private final String cleanTargetName;
    
    /**
     * @see AntProjectHelper#createSimpleAntArtifact
     */
    public SimpleAntArtifact(AntProjectHelper helper, String type, String locationProperty, PropertyEvaluator eval, String targetName, String cleanTargetName) {
        this.h = helper;
        this.type = type;
        this.locationProperty = locationProperty;
        this.eval = eval;
        this.targetName = targetName;
        this.cleanTargetName = cleanTargetName;
    }
    
    public URI getArtifactLocation() {
        String locationResolved = eval.getProperty(locationProperty);
        if (locationResolved == null) {
            return URI.create("file:/UNDEFINED"); // NOI18N
        }
        try {
            // XXX does this handle absolute locations correctly? probably not
            return new URI(null, null, locationResolved, null);
        } catch (URISyntaxException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return URI.create("file:/BROKEN"); // NOI18N
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
    
    public String toString() {
        return "SimpleAntArtifact[helper=" + h + ",type=" + type + ",locationProperty=" + locationProperty + // NOI18N
            ",targetName=" + targetName + ",cleanTargetName=" + cleanTargetName + /*",props=" + eval.getProperties() +*/ "]"; // NOI18N
    }
    
}
