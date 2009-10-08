/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xslt.project.anttasks;

import java.lang.reflect.Method;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.netbeans.modules.soa.validation.util.LineUtil;

public class CliValidateProjectTask extends Task {
    
    private String mSourceDirectory = null;
    private String mBuildDirectory = null;
    private String mProjectClassPath = null;
    private AntClassLoader mClassLoader = null;
    private Reference mReference = null;
    private String mBuildDependentProjectFilesDirectory;
    private boolean mAllowBuildWithError = false;
    
    public CliValidateProjectTask() {}
    
    public void setBuildDependentProjectDir(String dependentProjectFilesDir) {
        this.mBuildDependentProjectFilesDirectory = dependentProjectFilesDir;
    }
    
    public void setClasspathRef(Reference ref) {
        this.mReference = ref;
    }
    
    public void setBuildDirectory(String buildDir) {
        mBuildDirectory = buildDir;
    }
    
    public void setSourceDirectory(String srcDir) {
        this.mSourceDirectory = srcDir;
    }
    
    public void setRunValidation(String flag) {
        setAllowBuildWithError(flag);
        mAllowBuildWithError = !mAllowBuildWithError;
    }
    
    public String getSourceDirectory() {
        return this.mSourceDirectory;
    }
    
    public void setProjectClassPath(String projectClassPath) {
        this.mProjectClassPath = projectClassPath;
    }
    
    public void setAllowBuildWithError(String flag) {
        if (flag != null) {
            if (flag.equals("false")) {
                mAllowBuildWithError = false;
            } else if (flag.equals("true")) {
                mAllowBuildWithError = true;
            }
        }
    }
    
    @Override
    public void execute() throws BuildException {
        Boolean isErrors = null;
        
        try {
            mClassLoader = new AntClassLoader();
            initClassLoader();
            
            Class antTaskClass = Class.forName("org.netbeans.modules.xslt.project.anttasks.CliValidateProjectDelegate", true, mClassLoader);
            Thread.currentThread().setContextClassLoader(mClassLoader);
            Object validateObj = antTaskClass.newInstance();
            
            Method driver = antTaskClass.getMethod("setBuildDirectory", new Class[] {String.class});
            Object[] param = new Object[] {this.mBuildDirectory};
            driver.invoke(validateObj, param);

            driver = antTaskClass.getMethod("setSourceDirectory", new Class[] {String.class});
            param = new Object[] {this.mSourceDirectory};
            driver.invoke(validateObj, param);

            driver = antTaskClass.getMethod("setAllowBuildWithError", new Class[] {String.class});
            param = new Object[] {"" + this.mAllowBuildWithError};
            driver.invoke(validateObj, param);

            driver = antTaskClass.getMethod("setProjectClassPath", new Class[] {String.class});
            param = new Object[] {this.mProjectClassPath};
            driver.invoke(validateObj, param);

            driver = antTaskClass.getMethod("setBuildDependentProjectDir", new Class[] {String.class});
            param = new Object[] {this.mBuildDependentProjectFilesDirectory};
            driver.invoke(validateObj, param);

            driver = antTaskClass.getMethod("execute", (Class[]) null);
            driver.invoke(validateObj, (Object[]) null);

            driver = antTaskClass.getMethod("isFoundErrors", (Class[]) null);
            isErrors = (Boolean) driver.invoke(validateObj, (Object[]) null);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new BuildException("Exception occured.", e);
        }
        
        if (isErrors != null && isErrors.booleanValue()) {
            if (!mAllowBuildWithError) {
                throw new BuildException(LineUtil.FOUND_VALIDATION_ERRORS);
            }
        }
    }
    
    private void initClassLoader() {
        Path path = new Path(getProject());
        path.setRefid(mReference);
        
        final Path parentPath = new Path(getProject());
        final ClassLoader cl = this.getClass().getClassLoader();
        
        if (cl instanceof AntClassLoader) {
            parentPath.setPath(((AntClassLoader) cl).getClasspath());
            ((AntClassLoader) cl).setParent(null);
            parentPath.add(path);
            path = parentPath;
        }
        
        mClassLoader.setClassPath(path);
        mClassLoader.setParent(null);
        mClassLoader.setParentFirst(false);
    }
}
