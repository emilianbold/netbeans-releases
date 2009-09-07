/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.tool;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thp
 */
public class DLightConfigurationUIWrapper {
    private DLightConfiguration dLightConfiguration;
    private boolean custom;
    private String name;
    private List<DLightToolUIWrapper> tools;

    public DLightConfigurationUIWrapper(DLightConfiguration dLightConfiguration, List<DLightTool> allDLightTools) {
        this.dLightConfiguration = dLightConfiguration;
        this.name = dLightConfiguration.getConfigurationName();
        this.custom = false;
        initWrapper(allDLightTools);
    }

    public DLightConfigurationUIWrapper(String name, List<DLightTool> allDLightTools) {
        this.dLightConfiguration = DLightConfigurationManager.getInstance().getDefaultConfiguration();
        this.name = name;
        this.custom = true;
        initWrapper(allDLightTools);
    }

    private void initWrapper(List<DLightTool> allDLightTools) {
        tools = new ArrayList<DLightToolUIWrapper>();
        List<DLightTool> confDlightTools = dLightConfiguration.getToolsSet();
        int i = 0;
        for (DLightTool dlightTool : allDLightTools) {
            tools.add(new DLightToolUIWrapper(dlightTool, inList(dlightTool, confDlightTools)));
        }
    }
    
    private static boolean inList(DLightTool dlightTool, List<DLightTool> list) {
        for (DLightTool dt : list) {
            if (dt.getID().equals(dlightTool.getID())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the custom
     */
    public boolean isCustom() {
        return custom;
    }

    /**
     * @param custom the custom to set
     */
    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    /**
     * @return the dLightConfiguration
     */
    public DLightConfiguration getdLightConfiguration() {
        return dLightConfiguration;
    }

    /**
     * @param dLightConfiguration the dLightConfiguration to set
     */
    public void setdLightConfiguration(DLightConfiguration dLightConfiguration) {
        this.dLightConfiguration = dLightConfiguration;
    }

    /**
     * @return the tools
     */
    public List<DLightToolUIWrapper> getTools() {
        return tools;
    }

    public DLightToolUIWrapper getToolUIWrapper(DLightTool dlightTool) {
        for (DLightToolUIWrapper dt : getTools()) {
            if (dt.getdLightTool().getID().equals(dlightTool.getID())) {
                return dt;
            }
        }
        return null;
    }

    /**
     * @param tools the tools to set
     */
    public void setTools(List<DLightToolUIWrapper> tools) {
        this.tools = tools;
    }
}
