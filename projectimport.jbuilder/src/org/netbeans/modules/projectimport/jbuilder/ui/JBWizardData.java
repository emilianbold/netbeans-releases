/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.projectimport.jbuilder.ui;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.projectimport.j2seimport.AbstractProject;
import org.netbeans.modules.projectimport.j2seimport.ui.BasicWizardIterator;
import org.openide.filesystems.FileUtil;

/**
 *
 * 
 */
public class JBWizardData extends BasicWizardIterator.BasicDataModel {
    public JBWizardData() {
        super();
    }
    
    private File projectFile = null;
    private File destinationDir = null;
    private Collection<AbstractProject> projectDefinition = Collections.emptySet();
    private boolean includeDependencies;
    
    
    public File getDestinationDir() {
        return (destinationDir != null) ? FileUtil.normalizeFile(destinationDir) : null;
    }
    
    void setDestinationDir(File destination) {
        this.destinationDir = destination;
    }
    
    public File getProjectFile() {
        return (projectFile != null) ? FileUtil.normalizeFile(projectFile) : null;
    }
    
    void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }
    
    public Collection<AbstractProject> getProjectDefinition() {
        return projectDefinition;
    }
    
    void setProjectDefinition(Collection<AbstractProject> projectDefinition) {
        this.projectDefinition = projectDefinition;
    }
    
    public boolean isIncludeDependencies() {
        return includeDependencies;
    }
    
    void setIncludeDependencies(boolean includeDependencies) {
        this.includeDependencies = includeDependencies;
    }
}
