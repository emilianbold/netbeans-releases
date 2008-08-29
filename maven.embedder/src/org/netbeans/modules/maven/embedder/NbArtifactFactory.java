/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.maven.embedder;

import java.lang.reflect.Field;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

/**
 *
 * @author mkleint
 */
public class NbArtifactFactory implements ArtifactFactory, Contextualizable {

    // TODO: remove, it doesn't know the ones from the plugins
    private ArtifactHandlerManager artifactHandlerManager;
    
    private ArtifactFactory original;
    
    /** Creates a new instance of NbArtifactFactory */
    public NbArtifactFactory() {
        original = new DefaultArtifactFactory();
    }

    public Artifact createArtifact(String groupId, String artifactId, String version, String scope, String type) {
        return createFromOriginal(original.createArtifact(checkValue(groupId), checkValue(artifactId), checkVersion(version), scope, type));
    }

    public Artifact createArtifactWithClassifier(String groupId, String artifactId, String version, String type, String classifier) {
        return createFromOriginal(original.createArtifactWithClassifier(checkValue(groupId), checkValue(artifactId), checkVersion(version), type, classifier));
    }

    public Artifact createDependencyArtifact(String groupId, String artifactId, VersionRange versionRange, String type, String classifier, String scope) {
        return createFromOriginal(original.createDependencyArtifact(checkValue(groupId), checkValue(artifactId), checkVersionRange(versionRange), type, classifier, scope));
    }

    public Artifact createDependencyArtifact(String groupId, String artifactId, VersionRange versionRange, String type, String classifier, String scope, boolean optional) {
        return createFromOriginal(original.createDependencyArtifact(checkValue(groupId), checkValue(artifactId), checkVersionRange(versionRange), type, classifier, scope, optional));
    }

    public Artifact createDependencyArtifact(String groupId, String artifactId, VersionRange versionRange, String type, String classifier, String scope, String inheritedScope) {
        return createFromOriginal(original.createDependencyArtifact(checkValue(groupId), checkValue(artifactId), checkVersionRange(versionRange), type, classifier, scope, inheritedScope));
    }

    public Artifact createDependencyArtifact(String groupId, String artifactId, VersionRange versionRange, String type, String classifier, String scope, String inheritedScope, boolean optional) {
        return createFromOriginal(original.createDependencyArtifact(checkValue(groupId), checkValue(artifactId), checkVersionRange(versionRange), type, classifier, scope, inheritedScope, optional));
    }

    public Artifact createBuildArtifact(String groupId, String artifactId, String version, String packaging) {
        return createFromOriginal(original.createBuildArtifact(checkValue(groupId), checkValue(artifactId), checkVersion(version), packaging));
    }

    public Artifact createProjectArtifact(String groupId, String artifactId, String version) {
        return createFromOriginal(original.createProjectArtifact(checkValue(groupId), checkValue(artifactId), checkVersion(version)));
    }

    public Artifact createParentArtifact(String groupId, String artifactId, String version) {
        return createFromOriginal(original.createParentArtifact(checkValue(groupId), checkValue(artifactId), checkVersion(version)));
    }

    public Artifact createPluginArtifact(String groupId, String artifactId, VersionRange versionRange) {
        return createFromOriginal(original.createPluginArtifact(checkValue(groupId), checkValue(artifactId), checkVersionRange(versionRange)));
    }

    public Artifact createProjectArtifact(String groupId, String artifactId, String version, String scope) {
        return createFromOriginal(original.createProjectArtifact(checkValue(groupId), checkValue(artifactId), checkVersion(version), scope));
    }

    public Artifact createExtensionArtifact(String groupId, String artifactId, VersionRange versionRange) {
        return createFromOriginal(original.createExtensionArtifact(checkValue(groupId), checkValue(artifactId), checkVersionRange(versionRange)));
    }

    public void contextualize(Context context) throws ContextException {
        setField("artifactHandlerManager", artifactHandlerManager);
    }
    
    // this method bypasses DefaultArtifact's validateIdentity() method
    private String checkValue(String in) {
        if (in == null || in.trim().length() == 0) {
            return "error";
        }
        return in;
    }
    
    // this method bypasses DefaultArtifact's validateIdentity() method
    private String checkVersion(String value) {
        if (value == null) {
            return "unknown";
        }
        return value;
    }
    
    // this method bypasses DefaultArtifact's validateIdentity() method
    private VersionRange checkVersionRange(VersionRange range) {
        if (range == null) {
            return VersionRange.createFromVersion("unknown");
        }
        return range;
    }
    
    private Artifact createFromOriginal(Artifact orig) {
        if (orig != null) {
            return new NbArtifact(orig);
        }
        return null;
    }
    
    private void setField(String name, Object value) {
        try {
            Field fld = original.getClass().getDeclaredField(name);
            fld.setAccessible(true);
            fld.set(original, value);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        }
    }
    
}
