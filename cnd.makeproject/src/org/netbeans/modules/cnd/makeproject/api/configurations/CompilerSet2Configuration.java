/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class CompilerSet2Configuration {
    private MakeConfiguration makeConfiguration;
    private StringConfiguration compilerSetName;
    private boolean dirty = false;
    
    // Constructors
    public CompilerSet2Configuration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        String csName = CppSettings.getDefault().getCompilerSetName();
        if (csName == null || csName.length() == 0) {
            // This can happen on Unix!!!!
            if (CompilerSetManager.getDefault().getCompilerSetNames().size() > 0)
                csName = CompilerSetManager.getDefault().getCompilerSet(0).getName();
            else {
                if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS)
                    csName = "Sun"; // NOI18N
                else
                    csName = "GNU"; // NOI18N
            }
        }
        compilerSetName = new StringConfiguration(null, csName);
    }
    
    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }
    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }
    
    // compilerSetName
    public StringConfiguration getCompilerSetName() {
        return compilerSetName;
    }
    
    public void setCompilerSetName(StringConfiguration compilerSetName) {
        this.compilerSetName = compilerSetName;
    }
    
    // ----------------------------------------------------------------------------------------------------
    
    public void setValue(String name) {
        getCompilerSetName().setValue(name);
    }
    
    /*
     * Keep backward compatibility with CompilerSetConfiguration (for now)
     */
    public int getValue() {
        String s = getCompilerSetName().getValue();
	if (s != null) {
            int i = 0;
            for (String csname : CompilerSetManager.getDefault().getCompilerSetNames()) {
                if (s.equals(csname)) {
                    return i;
                }
                i++;
            }
        }
        return 0; // Default
    }
    
    public String getName() {
        return getDisplayName();
    }
    
    public String getDisplayName() {
        return getDisplayName(false);
    }
    
    public String getDisplayName(boolean displayIfNotFound) {
        CompilerSet compilerSet = null;
        String dn = null;
        if (compilerSet == null) {
            compilerSet = CompilerSetManager.getDefault().getCompilerSet(getCompilerSetName().getValue());
        }
        if (compilerSet != null) {
            dn = compilerSet.getName();
        }
        if (dn != null)
            return dn;
        else {
            if (displayIfNotFound)
                return createNotFoundName(getCompilerSetName().getValue());
            else
                return ""; // NOI18N
        }
    }
    
    public String createNotFoundName(String name) {
        return name + " - " + getString("NOT_FOUND"); // NOI18N
    }
    
    // Clone and assign
    public void assign(CompilerSet2Configuration conf) {
        setDirty(getValue() != conf.getValue());
        setMakeConfiguration(conf.getMakeConfiguration());
        getCompilerSetName().assign(conf.getCompilerSetName());
    }
    
    @Override
    public Object clone() {
        CompilerSet2Configuration clone = new CompilerSet2Configuration(getMakeConfiguration());
        clone.setCompilerSetName((StringConfiguration)getCompilerSetName().clone());
        return clone;
    }
    
    
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    
    public boolean getDirty() {
        return dirty;
    }
    
    /*
     * Backward compatibility with old CompilerSetConfiguration (for now)
     */
    public boolean isValid() {
        return CompilerSetManager.getDefault().getCompilerSet(getCompilerSetName().getValue()) != null;
    }
    
    public void setValid() {
        // Nothing
    }
    
    public String getOldName() {
        return getCompilerSetName().getValue();
    }
    
    public String getOption() {
        return getCompilerSetName().getValue();
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CompilerSet2Configuration.class, s);
    }
}
