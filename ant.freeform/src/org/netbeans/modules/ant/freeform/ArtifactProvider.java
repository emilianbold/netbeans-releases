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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.ErrorManager;
import org.w3c.dom.Element;

/**
 * Handles <code>&lt;export&gt;</code> elements in project.xml
 * @author Jesse Glick
 */
final class ArtifactProvider implements AntArtifactProvider {
    
    private final FreeformProject project;
    
    public ArtifactProvider(FreeformProject project) {
        this.project = project;
    }

    public AntArtifact[] getBuildArtifacts() {
        Element data = project.getPrimaryConfigurationData();
        List<AntArtifact> artifacts = new ArrayList<AntArtifact>();
        Set<String> ids = new HashSet<String>();
        HashMap<String,FreeformArtifact> uniqueArtifacts = new HashMap<String,FreeformArtifact>();
        for (Element export : Util.findSubElements(data)) {
            if (!export.getLocalName().equals("export")) { // NOI18N
                continue;
            }
            FreeformArtifact artifact = new FreeformArtifact(export);
            
            String artifactKey = artifact.getType() + artifact.getTargetName() + artifact.getScriptLocation().getAbsolutePath();
            FreeformArtifact alreadyHasArtifact = uniqueArtifacts.get(artifactKey);
            if (alreadyHasArtifact != null) {
                alreadyHasArtifact.addLocation(readArtifactLocation(export, project.evaluator()));
                continue;
            } else {
                artifact.addLocation(readArtifactLocation(export, project.evaluator()));
                uniqueArtifacts.put(artifactKey, artifact);
            }
            
            String id = artifact.preferredId();
            if (!ids.add(id)) {
                // Need to uniquify it.
                int counter = 2;
                while (true) {
                    String possibleId = id + counter;
                    if (ids.add(possibleId)) {
                        id = possibleId;
                        break;
                    }
                    counter++;
                }
            }
            artifact.configureId(id);
            artifacts.add(artifact);
        }
        return artifacts.toArray(new AntArtifact[artifacts.size()]);
    }
    
    public static URI readArtifactLocation(Element export, PropertyEvaluator eval) {
        Element locEl = Util.findElement(export, "location", FreeformProjectType.NS_GENERAL); // NOI18N
        assert locEl != null;
        String loc = Util.findText(locEl);
        assert loc != null;
        String locationResolved = eval.evaluate(loc);
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

    private final class FreeformArtifact extends AntArtifact {
        
        private final Element export;
        private String id = null;
        private final Set<URI> locations = new LinkedHashSet<URI>();
        
        public FreeformArtifact(Element export) {
            this.export = export;
        }
        
        public String preferredId() {
            return getTargetName();
        }
        
        public void configureId(String id) {
            assert this.id == null;
            this.id = id;
        }

        public String getType() {
            Element typeEl = Util.findElement(export, "type", FreeformProjectType.NS_GENERAL); // NOI18N
            assert typeEl != null;
            String type = Util.findText(typeEl);
            assert type != null;
            return type;
        }

        public String getTargetName() {
            Element targetEl = Util.findElement(export, "build-target", FreeformProjectType.NS_GENERAL); // NOI18N
            assert targetEl != null;
            String target = Util.findText(targetEl);
            assert target != null;
            return target;
        }

        public String getCleanTargetName() {
            Element targetEl = Util.findElement(export, "clean-target", FreeformProjectType.NS_GENERAL); // NOI18N
            if (targetEl != null) {
                String target = Util.findText(targetEl);
                assert target != null;
                return target;
            } else {
                // Guess based on configured target for 'clean' command, if any.
                String target = null;
                Element genldata = project.getPrimaryConfigurationData();
                Element actionsEl = Util.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
                if (actionsEl != null) {
                    for (Element actionEl : Util.findSubElements(actionsEl)) {
                        if (actionEl.getAttribute("name").equals("clean")) { // NOI18N
                            for (Element actionTargetEl : Util.findSubElements(actionEl)) {
                                if (!actionTargetEl.getLocalName().equals("target")) { // NOI18N
                                    continue;
                                }
                                String possibleTarget = Util.findText(actionTargetEl);
                                assert possibleTarget != null;
                                if (target == null) {
                                    // OK, probably use it (unless there is another target for this command).
                                    target = possibleTarget;
                                } else {
                                    // Oops! >1 target not supported for AntArtifact.
                                    target = null;
                                    break;
                                }
                            }
                            // We found the clean command, use that target if we got it.
                            break;
                        }
                    }
                }
                if (target == null) {
                    // Guess!
                    target = "clean"; // NOI18N
                }
                return target;
            }
        }

        public File getScriptLocation() {
            String loc = null;
            Element scriptEl = Util.findElement(export, "script", FreeformProjectType.NS_GENERAL); // NOI18N
            if (scriptEl != null) {
                String script = Util.findText(scriptEl);
                assert script != null;
                loc = project.evaluator().evaluate(script);
            }
            if (loc == null) {
                // Not configured, or eval failed.
                loc = "build.xml"; // NOI18N
            }
            return project.helper().resolveFile(loc);
        }

        public Project getProject() {
            return project;
        }

        public String getID() {
            assert id != null;
            return id;
        }

        public URI[] getArtifactLocations() {
            return locations.toArray(new URI[locations.size()]);
        }
        
        private void addLocation(URI u) {
            locations.add(u);
        }
        
        public String toString() {
            return "FreeformArtifact[" + project + ":" + id + "]"; // NOI18N
        }

    }

}
