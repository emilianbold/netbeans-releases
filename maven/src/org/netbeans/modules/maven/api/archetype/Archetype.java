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

package org.netbeans.modules.maven.api.archetype;

/**
 * Simple model class to describe a Maven archetype. To be created by ArchetypeProvider 
 * implementations, consumed by the New Maven Project wizard.
 * @author mkleint
 */
public final class Archetype {
    
    private String artifactId;
    private String groupId;
    private String version;
    private String name;
    private String description;
    private String repository;
    public final boolean deletable;
    /**
     * @deprecated has no meaning anymore.
     */
    public @Deprecated final boolean archetypeNg;
    
    /** Creates a new instance of Archetype 
     * @deprecated isArchetypeNg is not used anymore.
     */
    public @Deprecated Archetype(boolean deletable, boolean isArchetypeNg) {
        this.deletable = deletable;
        archetypeNg = isArchetypeNg;
        artifactId = "";
        groupId = "";
        version = "";
    }
    
    public Archetype(boolean deletable) {
        this(deletable, false);
    }
    
    public Archetype() {
        this(true);
    }
    
    
    public String getArtifactId() {
        return artifactId;
    }
    
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getName() {
        //#166884
        if ("${project.artifactId}".equals(name)) { //NOI18N
            return artifactId;
        }
        if (name != null && name.trim().length() == 0) {
            return artifactId;
        }
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * optional property.
     */ 
    public void setRepository(String repo) {
        repository = repo;
    }
    
    /**
     * optional property.
     * @return 
     */
    public String getRepository() {
        return repository;
    }
    
    @Override
    public int hashCode() {
        return getGroupId().trim().hashCode() + 13 * getArtifactId().trim().hashCode() + 23 * getVersion().trim().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Archetype)) {
            return false;
        }
        Archetype ar1 = (Archetype)obj;
        if (ar1 == null) {
            return false;
        }
        boolean gr = ar1.getGroupId().trim().equals(getGroupId().trim());
        if (!gr) {
            return false;
        }
        boolean ar = ar1.getArtifactId().trim().equals(getArtifactId().trim());
        if (!ar) {
            return false;
        }
        boolean ver =  ar1.getVersion().trim().equals(getVersion().trim());
        return ver;
    }

}
