/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject.ui.customizer;

import java.util.Set;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.WeakSet;

/**
 *
 * @author Alexabder Simon
 */
public class MakeContext {

    public static enum Kind {
        Project,
        Folder,
        Item
    }

    public static interface Savable {
        void save();
    }

    private final Kind kind;
    private final Project project;
    private final ExecutionEnvironment env;
    private final Configuration[] selectedConfigurations;
    private SharedItemConfiguration item;
    private Folder folder;
    private JPanel container;
    private ConfigurationDescriptor configurationDescriptor;
    private Set<Savable> listeners = new WeakSet<Savable>();

    public MakeContext(Kind kind, Project project, ExecutionEnvironment env, Configuration[] selectedConfigurations){
        this.project = project;
        this.kind = kind;
        this.env = env;
        this.selectedConfigurations = selectedConfigurations;
    }

    public MakeContext setPanel(JPanel container) {
        this.container = container;
        return this;
    }

    public MakeContext setConfigurationDescriptor(ConfigurationDescriptor configurationDescriptor) {
        this.configurationDescriptor = configurationDescriptor;
        return this;
    }

    public MakeContext setFolder(Folder folder) {
        this.folder = folder;
        return this;
    }

    /*package*/MakeContext setSharedItem(SharedItemConfiguration item) {
        this.item = item;
        return this;
    }
    /**
     * @return the kind
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * @return the project
     */
    public Project getProject() {
        return project;
    }

    /**
     * @return the env
     */
    public ExecutionEnvironment getEnv() {
        return env;
    }

    /**
     * @return the selectedConfigurations
     */
    public Configuration[] getSelectedConfigurations() {
        return selectedConfigurations;
    }

    /**
     * @return the item
     */
    /*package*/ SharedItemConfiguration getItem() {
        return item;
    }

    /**
     * @return the folder
     */
    /*package*/ Folder getFolder() {
        return folder;
    }

    /**
     * @return the container
     */
    public JPanel getContainer() {
        return container;
    }

    /**
     * @return the configurationDescriptor
     */
    public ConfigurationDescriptor getConfigurationDescriptor() {
        return configurationDescriptor;
    }

    public void registerSavable(Savable listener){
        listeners.add(listener);
    }

    public void save() {
        for(Savable listener : listeners) {
            listener.save();
        }
    }

    public boolean isCompilerConfiguration(){
        return ((MakeConfiguration) selectedConfigurations[0]).isCompileConfiguration();
    }

    public PredefinedToolKind getItemTool() {
        PredefinedToolKind tool = PredefinedToolKind.UnknownTool;
        int compilerSet = -1;

        for (int i = 0; i < selectedConfigurations.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration) selectedConfigurations[i];
            int compilerSet2 = makeConfiguration.getCompilerSet().getValue();
            ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration);
            if (itemConfiguration == null) {
                continue;
            }
            PredefinedToolKind tool2 = itemConfiguration.getTool();
            if (tool == PredefinedToolKind.UnknownTool && compilerSet == -1) {
                tool = tool2;
                compilerSet = compilerSet2;
            }
            if (tool != tool2 || compilerSet != compilerSet2) {
                tool = PredefinedToolKind.UnknownTool;
                break;
            }

            if ((isCompilerConfiguration() && !makeConfiguration.isCompileConfiguration()) ||
                (!isCompilerConfiguration() && makeConfiguration.isCompileConfiguration())) {
                tool = PredefinedToolKind.UnknownTool;
                break;
            }
        }
        return tool;
    }

    public boolean isQtMode() {
        boolean isQtMode = false;
        for (int i = 0; i < selectedConfigurations.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration) selectedConfigurations[i];
            isQtMode |= makeConfiguration.isQmakeConfiguration();
        }
        return isQtMode;
    }

}
