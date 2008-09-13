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
 * Contributor(s): thenauradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer.api;

/**
 *
 * @author Anuradha G
 */
public class NBVersionInfo {

    private String groupId;
    private String artifactId;
    private String version;
    private String type;
    private String packaging;
    private String projectName;
    private String classifier;
    private String ProjectDescription;
    private String repoId;
    
    //-----
    private boolean sourcesExists;
    private boolean javadocExists;
    private boolean signatureExists;

    public NBVersionInfo(String repoId,String groupId, String artifactId, String version,
            String type, String packaging, String projectName,String ProjectDescription,String classifier) {
        this.repoId = repoId;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.packaging = packaging;
        this.projectName = projectName;
        this.ProjectDescription = ProjectDescription;
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
        return ProjectDescription;
    }

    public String getClassifier() {
        return classifier;
    }
 
    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version + ":" + repoId;
    }
    
}
