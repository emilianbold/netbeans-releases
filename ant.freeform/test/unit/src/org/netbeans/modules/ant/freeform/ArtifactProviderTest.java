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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;

/**
 * Test {@link ArtifactProvider}.
 * @author Jesse Glick
 */
public class ArtifactProviderTest extends TestBase {
    
    public ArtifactProviderTest(String name) {
        super(name);
    }
    
    public void testBuildArtifact() throws Exception {
        File mainJar = simple.helper().resolveFile("build/simple-app.jar");
        AntArtifact aa = AntArtifactQuery.findArtifactFromFile(mainJar);
        assertNotNull("have artifact for " + mainJar, aa);
        verifyArtifact(aa);
        // Could be different instances returned each time, so check each one:
        aa = AntArtifactQuery.findArtifactByID(simple, "jar");
        assertNotNull("found artifact by ID", aa);
        verifyArtifact(aa);
        AntArtifact[] aas = AntArtifactQuery.findArtifactsByType(simple, "jar");
        assertEquals("found one 'jar' artifact", 1, aas.length);
        verifyArtifact(aas[0]);
    }
    
    private void verifyArtifact(AntArtifact aa) {
        assertEquals("right project", simple, aa.getProject());
        assertEquals("right location", URI.create("build/simple-app.jar"), aa.getArtifactLocation());
        assertEquals("right target", "jar", aa.getTargetName());
        assertEquals("right clean target", "clean", aa.getCleanTargetName());
        // ID should be target name if that does not cause a conflict
        assertEquals("right ID", "jar", aa.getID());
        assertEquals("right type", "jar", aa.getType());
        assertEquals("right script", simple.helper().resolveFile("build.xml"), aa.getScriptLocation());
    }
    
}
