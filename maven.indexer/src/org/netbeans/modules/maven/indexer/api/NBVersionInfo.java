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
 * Contributor(s): thenauradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer.api;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.openide.util.Utilities;

/**
 *
 * @author Anuradha G
 */
public final class NBVersionInfo implements Comparable<NBVersionInfo> {

    private String groupId;
    private String artifactId;
    private String version;
    private String type;
    private String packaging;
    private String projectName;
    private String classifier;
    private String projectDescription;
    private String repoId;
//    private String sha;
    private long lastModified;
    private long size;

    //-----
    private boolean sourcesExists;
    private boolean javadocExists;
    private boolean signatureExists;

    public NBVersionInfo(String repoId,String groupId, String artifactId, String version,
            String type, String packaging, String projectName,String desc,String classifier) {
        this.repoId = repoId;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.packaging = packaging;
        this.projectName = projectName;
        this.projectDescription = desc;
        this.classifier = classifier;
    }

    public String getRepoId() {
        return repoId;
    }

    public boolean isJavadocExists() {
        return javadocExists;
    }

    public void setJavadocExists(boolean javadocExists) {
        this.javadocExists = javadocExists;
    }

    public boolean isSignatureExists() {
        return signatureExists;
    }

    public void setSignatureExists(boolean signatureExists) {
        this.signatureExists = signatureExists;
    }

    public boolean isSourcesExists() {
        return sourcesExists;
    }

    public void setSourcesExists(boolean sourcesExists) {
        this.sourcesExists = sourcesExists;
    }
    
  
    

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getPackaging() {
        return packaging;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public String getClassifier() {
        return classifier;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

//    public String getSha() {
//        return sha;
//    }
//
//    public void setSha(String sha) {
//        this.sha = sha;
//    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version + ":" + repoId;
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof NBVersionInfo)) {
            return false;
        }
        NBVersionInfo other = (NBVersionInfo) obj;
        return toString().equals(other.toString()) && Utilities.compareObjects(type, other.type) && Utilities.compareObjects(classifier, other.classifier);
    }

    @Override public int hashCode() {
        return toString().hashCode();
    }

    public @Override int compareTo(NBVersionInfo o) {
        int c = groupId.compareTo(o.groupId);
        if (c != 0) {
            return c;
        }
        c = artifactId.compareTo(o.artifactId);
        if (c != 0) {
            return c;
        }
        c = version().compareTo(o.version());
        if (c != 0) {
            return -c; // show newest versions first!
        }
        if (type != null && o.type != null) {
            c = type.compareTo(o.type);
            if (c != 0) {
                return c; // show e.g. jar vs. nbm artifacts in some predictable order
            }
        }
        return System.identityHashCode(this) - System.identityHashCode(o); // don't care
    }
    private ComparableVersion version() {
        if (version.matches("RELEASE\\d+(-.+)?")) { // NOI18N
            // Maven considers RELEASE671 to be newer than RELEASE69. Hack up the version here.
            return new ComparableVersion(version.replaceAll("(\\d)", ".$1")); // NOI18N
        } else {
            return new ComparableVersion(version);
        }
    }
    
}
