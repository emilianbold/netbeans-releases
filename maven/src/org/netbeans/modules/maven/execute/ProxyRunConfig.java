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

package org.netbeans.modules.maven.execute;

import org.netbeans.modules.maven.api.execute.RunConfig;
import java.io.File;
import java.util.List;
import java.util.Properties;
import org.netbeans.api.project.Project;

/**
 *
 * @author mkleint
 */
public class ProxyRunConfig implements RunConfig {

    private RunConfig delegate;
    
    /** Creates a new instance of ProxyRunConfig */
    public ProxyRunConfig(RunConfig delegate) {
        this.delegate = delegate;
    }

    public File getExecutionDirectory() {
        return delegate.getExecutionDirectory();
    }

    public Project getProject() {
        return delegate.getProject();
    }

    public List getGoals() {
        return delegate.getGoals();
    }

    public String getExecutionName() {
        return delegate.getExecutionName();
    }

    public Properties getProperties() {
        return delegate.getProperties();
    }
    
    public  String removeProperty(String key) {
        return delegate.removeProperty(key);
    }

    public  String setProperty(String key, String value) {
        return delegate.setProperty(key, value);
    }
    public void setProperties(Properties properties){
      delegate.setProperties(properties);
    }
    public boolean isShowDebug() {
        return delegate.isShowDebug();
    }

    public boolean isShowError() {
        return delegate.isShowError();
    }

    public Boolean isOffline() {
        return delegate.isOffline();
    }

    public List getActivatedProfiles() {
        return delegate.getActivatedProfiles();
    }
    
    public void setActivatedProfiles(List<String> activeteProfiles) {
        delegate.setActivatedProfiles(activeteProfiles);
    }
    

    public boolean isRecursive() {
        return delegate.isRecursive();
    }

    public boolean isUpdateSnapshots() {
        return delegate.isUpdateSnapshots();
    }
    
    public void setOffline(Boolean off) {
        delegate.setOffline(off);
    }

    public String getTaskDisplayName() {
        return delegate.getTaskDisplayName();
    }

    public boolean isInteractive() {
        return delegate.isInteractive();
    }

    public String getActionName() {
        return delegate.getActionName();
    }
    
}
