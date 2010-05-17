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

package org.netbeans.modules.j2me.cdc.project;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author suchys
 */
public class RemapTask extends Task {
    
    private File baseDir;
    private File source;
    private String targetDir;
    private String targetName;
    private String remappedProp;
    
    public void execute() throws BuildException {
        if (baseDir == null) throw new BuildException("basedir == null");
        if (source == null) throw new BuildException("source == null");
        if (targetDir == null) throw new BuildException("targetdir == null");
        if (targetName == null) throw new BuildException("targetname == null");

        File root = baseDir;
        List<File> roots = Arrays.asList(File.listRoots());
        File parent = source;
        StringBuffer sb = new StringBuffer();
        while( !root.equals(parent) && !roots.contains(parent)){
            sb.insert(0,  parent.getName());
            parent = parent.getParentFile();
            if (!root.equals(parent) && !roots.contains(parent))
                sb.insert(0,  '/');
        }
        if (roots.contains(parent)){
            if (remappedProp != null)
                this.getProject().setNewProperty(remappedProp, "true");
            parent = root;
        }
        File dest = new File(parent, sb.toString());
        
        this.getProject().setNewProperty(targetDir, dest.getAbsolutePath());
        this.getProject().setNewProperty(targetName, sb.toString());   
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public String getTargetDir() {
        return targetDir;
    }

    public void setTargetDir(String targetDir) {
        this.targetDir = targetDir;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getRemappedProp() {
        return remappedProp;
    }

    public void setRemappedProp(String remappedProp) {
        this.remappedProp = remappedProp;
    }
}
