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
import org.netbeans.modules.cnd.makeproject.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class PackagingConfiguration {
    private MakeConfiguration makeConfiguration;
    
    // Types
    private static String[] TYPE_NAMES = {
        getString("SCR4Package"),
        getString("IPSPackage"),
        getString("Tar"),
        getString("Zip"),
    };
    public static final int TYPE_SVR4_PACKAGE = 0;
    public static final int TYPE_IPS_PACKAGE = 1;
    public static final int TYPE_TAR = 2;
    public static final int TYPE_ZIP = 3;
    
    private IntConfiguration type;
    private StringConfiguration files;
    
    // Constructors
    public PackagingConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        type = new IntConfiguration(null, TYPE_ZIP, TYPE_NAMES, null);
        files = new StringConfiguration(null, ""); // NOI18N
    }
    
    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }
    
    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }
    

    public IntConfiguration getType() {
        return type;
    }

    public void setType(IntConfiguration type) {
        this.type = type;
    }

    public StringConfiguration getFiles() {
        return files;
    }

    public void setFiles(StringConfiguration files) {
        this.files = files;
    }
    
    // Clone and assign
    public void assign(PackagingConfiguration conf) {
        setMakeConfiguration(conf.getMakeConfiguration());
        getType().assign(conf.getType());
        getFiles().assign(conf.getFiles());
    }
    
    @Override
    public Object clone() {
        PackagingConfiguration clone = new PackagingConfiguration(getMakeConfiguration());
        clone.setType((IntConfiguration)getType().clone());
        clone.setFiles((StringConfiguration)getFiles().clone());
        return clone;
    }
    
    // Sheet
    public Sheet getGeneralSheet() {
        Sheet sheet = new Sheet();
        Sheet.Set set = new Sheet.Set();
        set.setName("General"); // NOI18N
        set.setDisplayName(getString("GeneralTxt"));
        set.setShortDescription(getString("GeneralHint"));
        set.put(new IntNodeProp(getType(), true, null, getString("PackageTypeTxt"), getString("PackageTypeHint"))); // NOI18N
        set.put(new StringNodeProp(getFiles(), "Files", getString("FilesTxt"), getString("FilesHint"))); // NOI18N
        sheet.put(set);
        return sheet;
    }
    
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(PackagingConfiguration.class, s);
    }
}
