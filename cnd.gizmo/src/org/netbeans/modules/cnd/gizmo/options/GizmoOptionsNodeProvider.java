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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.cnd.gizmo.options;

import java.util.ResourceBundle;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomizerNodeProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.makeproject.api.configurations.CustomizerNodeProvider.class)
public class GizmoOptionsNodeProvider implements CustomizerNodeProvider {

    /**
     * Creates an instance of a customizer node
     */
    private CustomizerNode customizerNode = null;

    @Override
    public CustomizerNode factoryCreate(Lookup lookup) {
        if (customizerNode == null) {
            customizerNode = createProfileNode(lookup);
        }
        return customizerNode;
    }

    public CustomizerNode createProfileNode(Lookup lookup) {
        return new GizmoOptionsCustomizerNode(
                "Profile", // NOI18N
                getString("ProfileNodeTxt"),
                null, lookup);
    }

    private class GizmoOptionsCustomizerNode extends CustomizerNode {

        public GizmoOptionsCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
            super(name, displayName, children, lookup);
        }

        @Override
        public Sheet getSheet(Configuration configuration) {
            GizmoOptionsImpl gizmoOptions = GizmoOptionsImpl.getOptions(configuration);
            return createSheet(gizmoOptions);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("ProjectPropsProfile"); // NOI18N
        }
    }


    private Sheet createSheet(GizmoOptionsImpl gizmoOptions) {
        Sheet sheet = new Sheet();
        Sheet.Set set = new Sheet.Set();

        set.setName("General"); // NOI18N
        set.setDisplayName(getString("GeneralName"));
        set.put(new BooleanNodeProp(gizmoOptions.getProfileOnRun(), true, GizmoOptionsImpl.PROFILE_ON_RUN_PROP, getString("profileonrun_txt"), getString("profileonrun_help"))); // NOI18N
//        set.put(new GizmoIntNodeProp(gizmoOptions.getGizmoConfigurations(), true, GizmoOptionsImpl.CONFIGURATION_PROP, getString("profileConfiguration_txt"), getString("profileConfiguration_help"))); // NOI18N
        set.put(new GizmoStringNodeProp(gizmoOptions.getDlightConfigurationName(), GizmoOptionsImpl.CONFIGURATION_PROP, getString("profileConfiguration_txt"), getString("profileConfiguration_help")));
        sheet.put(set);

//        set = new Sheet.Set();
//        set.setName("Indicators"); // NOI18N
//        set.setDisplayName("To Be Removed from here and down.....");//NOI18N
//        set.put(new IntNodeProp(gizmoOptions.getDataProvider(), true, GizmoOptionsImpl.DATA_PROVIDER_PROP, getString("dataprovider_txt"), getString("dataprovider_help"))); // NOI18N
//        DLightConfiguration gizmoConfiguration = DLightConfigurationManager.getInstance().getConfigurationByName("Gizmo");//NOI18N
//        for (String id : gizmoOptions.getNames()){
//            DLightTool tool = gizmoConfiguration.getToolByID(id);
//            String name = id;
//            if (tool != null) {
//                name = tool.getName();
//            }else{
//                //find tool in default configuratopm
//                name = DLightConfigurationManager.getInstance().getDefaultConfiguration().getToolByID(id).getName();
//            }
//            set.put(new BooleanNodeProp(gizmoOptions.getConfigurationByName(id),
//                    true, id, name, gizmoOptions.getDescriptionByName(id)));
//        }
////        set.put(new BooleanNodeProp(gizmoOptions.getCpu(), true, GizmoOptionsImpl.CPU_PROP, getString("cpu_txt"), getString("cpu_help"))); // NOI18N
////        set.put(new BooleanNodeProp(gizmoOptions.getMemory(), true, GizmoOptionsImpl.MEMORY_PROP, getString("memory_txt"), getString("memory_help"))); // NOI18N
////        set.put(new BooleanNodeProp(gizmoOptions.getSynchronization(), true, GizmoOptionsImpl.SYNCHRONIZATION_PROP, getString("synchronization_txt"), getString("synchronization_help"))); // NOI18N
//        sheet.put(set);
        
        return sheet;
    }

    /** Look up i18n strings here */
    private ResourceBundle bundle;

    protected String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(GizmoOptionsNodeProvider.class);
        }
        return bundle.getString(s);
    }
}
