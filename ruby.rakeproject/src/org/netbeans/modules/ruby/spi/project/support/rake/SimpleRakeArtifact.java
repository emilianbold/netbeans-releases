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

package org.netbeans.modules.ruby.spi.project.support.rake;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.api.project.rake.RakeArtifact;
import org.netbeans.modules.ruby.modules.project.rake.RakeBasedProjectFactorySingleton;
import org.openide.ErrorManager;

/**
 * A basic RakeArtifact implementation.
 * @see RakeProjectHelper#createSimpleRakeArtifact
 * @author Jesse Glick
 */
final class SimpleRakeArtifact extends RakeArtifact {

    private final RakeProjectHelper h;
    private final String type;
    private final String locationProperty;
    private final PropertyEvaluator eval;
    private final String targetName;
    private final String cleanTargetName;
    
    /**
     * @see RakeProjectHelper#createSimpleRakeArtifact
     */
    public SimpleRakeArtifact(RakeProjectHelper helper, String type, String locationProperty, PropertyEvaluator eval, String targetName, String cleanTargetName) {
        this.h = helper;
        this.type = type;
        this.locationProperty = locationProperty;
        this.eval = eval;
        this.targetName = targetName;
        this.cleanTargetName = cleanTargetName;
    }
    
    private URI getArtifactLocation0() {
        String locationResolved = eval.getProperty(locationProperty);
        if (locationResolved == null) {
            return URI.create("file:/UNDEFINED"); // NOI18N
        }
        File locF = new File(locationResolved);
        if (locF.isAbsolute()) {
            return locF.toURI();
        } else {
            // Project-relative path.
            try {
                return new URI(null, null, locationResolved.replace(File.separatorChar, '/'), null);
            } catch (URISyntaxException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return URI.create("file:/BROKEN"); // NOI18N
            }
        }
    }
    
    public URI[] getArtifactLocations() {
        return new URI[]{getArtifactLocation0()};
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
        return RakeBasedProjectFactorySingleton.getProjectFor(h);
    }
    
    public String toString() {
        return "SimpleRakeArtifact[helper=" + h + ",type=" + type + ",locationProperty=" + locationProperty + // NOI18N
            ",targetName=" + targetName + ",cleanTargetName=" + cleanTargetName + /*",props=" + eval.getProperties() +*/ "]"; // NOI18N
    }
    
}
