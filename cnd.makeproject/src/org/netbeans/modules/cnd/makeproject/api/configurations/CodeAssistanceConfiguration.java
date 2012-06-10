/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.modules.cnd.makeproject.api.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class CodeAssistanceConfiguration implements Cloneable {
    private MakeConfiguration makeConfiguration;
    private BooleanConfiguration buildAnalyzer;
    private StringConfiguration tools;
    private static final String DEFAULT_TOOLS = "gcc:c++:g++:clang:clang++:icc:icpc:ifort:gfortran:g77:g90:g95:cc:CC:ffortran:f77:f90:f95"; //NOI18N
    
    // Constructors
    public CodeAssistanceConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        buildAnalyzer = new BooleanConfiguration(true);
        tools = new StringConfiguration(tools, DEFAULT_TOOLS);
    }

    public boolean getModified() {
        return getBuildAnalyzer().getModified() || getTools().getModified();
    }

    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }

    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }

    // Strip
    public void setBuildAnalyzer(BooleanConfiguration buildAnalyzer) {
        this.buildAnalyzer = buildAnalyzer;
    }

    public BooleanConfiguration getBuildAnalyzer() {
        return buildAnalyzer;
    }

    // Tool
    public void setTools(StringConfiguration tools) {
        this.tools = tools;
    }

    public StringConfiguration getTools() {
        return tools;
    }

    // Clone and assign
    public void assign(CodeAssistanceConfiguration conf) {
        getBuildAnalyzer().assign(conf.getBuildAnalyzer());
        getTools().assign(conf.getTools());
    }

    @Override
    public CodeAssistanceConfiguration clone() {
        CodeAssistanceConfiguration clone = new CodeAssistanceConfiguration(getMakeConfiguration());
        clone.setBuildAnalyzer(getBuildAnalyzer().clone());
        clone.setTools(getTools().clone());
        return clone;
    }

    // Sheet
    public Sheet getGeneralSheet(MakeConfiguration conf) {
        Sheet sheet = new Sheet();
        Sheet.Set set = new Sheet.Set();
        set.setName("CodeAssistance"); // NOI18N
        set.setDisplayName(getString("CodeAssistanceTxt"));
        set.setShortDescription(getString("CodeAssistanceHint"));
        set.put(new BooleanNodeProp(getBuildAnalyzer(), true, "BuildAnalyzer", getString("BuildAnalyzerTxt"), getString("BuildAnalyzerHint"))); // NOI18N
        set.put(new StringNodeProp(getTools(), DEFAULT_TOOLS, "Tools", getString("ToolsTxt2"), getString("ToolsHint2"))); // NOI18N
        sheet.put(set);
        return sheet;
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CodeAssistanceConfiguration.class, s);
    }

    @Override
    public String toString() {
        return "{buildAnalyzer=" + buildAnalyzer + " tools=" + tools + '}'; // NOI18N
    }

}
