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

package org.netbeans.modules.compapp.casaeditor.palette;

import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.Action;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteItemID;
import org.netbeans.modules.compapp.casaeditor.api.CasaPalettePlugin;
import org.netbeans.modules.compapp.casaeditor.api.InternalProjectTypePalettePlugin;
import org.netbeans.modules.compapp.casaeditor.graph.CasaFactory;
import org.netbeans.modules.compapp.projects.jbi.api.InternalProjectTypePlugin;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Singleton class which provides casa specific palette controller
 * @author rdara
 */
public class CasaPalette {
    
    public static final String CATEGORY_ID_WSDL_BINDINGS = 
            NbBundle.getMessage(CasaPalette.class, "WSDLBindings");  // NOI18N
    public static final String CATEGORY_ID_SERVICE_UNITS = 
            NbBundle.getMessage(CasaPalette.class, "ServiceUnits");  // NOI18N
    public static final String CATEGORY_ID_END_POINTS    = 
            NbBundle.getMessage(CasaPalette.class, "EndPoints");     // NOI18N
    
    public static final CasaPaletteItemID ITEM_ID_CONSUME               = 
            new CasaPaletteItemID(
                CasaBasePlugin.getInstance(),
                CasaPalette.CATEGORY_ID_END_POINTS, 
                NbBundle.getMessage(CasaPalette.class, "Palette_Consume_Title"), // NOI18N
                CasaFactory.getCasaCustomizer().getBOOLEAN_CLASSIC_ENDPOINT_PIN_STYLE() ?
                "org/netbeans/modules/compapp/casaeditor/palette/resources/consumesPaletteClassic.png" : // NOI18N
                "org/netbeans/modules/compapp/casaeditor/palette/resources/consumesPalette.png"); // NOI18N
    public static final CasaPaletteItemID ITEM_ID_PROVIDE               = 
            new CasaPaletteItemID(
                CasaBasePlugin.getInstance(),
                CasaPalette.CATEGORY_ID_END_POINTS, 
                NbBundle.getMessage(CasaPalette.class, "Palette_Provide_Title"), // NOI18N
                CasaFactory.getCasaCustomizer().getBOOLEAN_CLASSIC_ENDPOINT_PIN_STYLE() ?
                "org/netbeans/modules/compapp/casaeditor/palette/resources/providesPaletteClassic.png" : // NOI18N
                "org/netbeans/modules/compapp/casaeditor/palette/resources/providesPalette.png");  // NOI18N
    public static final CasaPaletteItemID ITEM_ID_INTERNAL_SU           = 
            new CasaPaletteItemID(
                CasaBasePlugin.getInstance(),
                CasaPalette.CATEGORY_ID_SERVICE_UNITS, 
                NbBundle.getMessage(CasaPalette.class, "Palette_IntSU_Title"), // NOI18N
                "org/netbeans/modules/compapp/casaeditor/palette/resources/intsu.png"); // NOI18N
    public static final CasaPaletteItemID ITEM_ID_EXTERNAL_SU           = 
            new CasaPaletteItemID(
                CasaBasePlugin.getInstance(),
                CasaPalette.CATEGORY_ID_SERVICE_UNITS, 
                NbBundle.getMessage(CasaPalette.class, "Palette_ExtSU_Title"), // NOI18N
                "org/netbeans/modules/compapp/casaeditor/palette/resources/extsu.png"); // NOI18N

    public static final DataFlavor CasaPaletteDataFlavor = new DataFlavor( 
            CasaPaletteItemID.class, 
            "CasaPaletteData" ) // NOI18N
    {
    };
    
//    private static PaletteController msPaletteController = null;
    private static String CASA_PALETTE_ROOT = "CasaPalette"; // NOI18N
    private static PaletteController palette = null;
    private static CasaPaletteRootNode paletteRoot;
    
    
    private CasaPalette() {
    }
    
    
    public static PaletteController getPalette(Lookup lookup) {
        if ( null == palette ) {
            paletteRoot = new CasaPaletteRootNode(new CasaPaletteCategoryChildren(lookup), lookup);
            paletteRoot.setName(CASA_PALETTE_ROOT);
            palette = PaletteFactory.createPalette( paletteRoot, new MyPaletteActions());
            
            disableCreateCategoryAction((Node) palette.getRoot().lookup(Node.class));
        }
        return palette;
    }
    
    public static Collection<? extends CasaPalettePlugin> getPlugins() {
        List<CasaPalettePlugin> plugins = new ArrayList<CasaPalettePlugin>();
        
        // CASA itself plugs into its own palette
        plugins.add(CasaBasePlugin.getInstance());
        
        // Add all internal project types as palette plugins.
        Collection<? extends InternalProjectTypePlugin> projectPlugins = 
                Lookup.getDefault().lookupAll(InternalProjectTypePlugin.class);
        for (InternalProjectTypePlugin projectTypePlugin : projectPlugins) {
            if (projectTypePlugin.getCategoryName() != null) {
                CasaPalettePlugin palettePlugin = 
                        new InternalProjectTypePalettePlugin(projectTypePlugin);
                plugins.add(palettePlugin);
            }
        }
        
        return plugins;
    }
    
    private static void disableCreateCategoryAction(Node proxyRootNode) {
        ResourceBundle bundle = NbBundle.getBundle(proxyRootNode.getClass());
        if (bundle != null) {
            String createCategoryName = null;
            try {
                createCategoryName = bundle.getString("CTL_CreateCategory"); // NOI18N
            } catch (MissingResourceException e) {
                // Cannot disable the action, it's key name may have changed.
                // We must be aware of this and visibly fail so development
                // can update the key name.
                ErrorManager.getDefault().notify(e);
                return;
            }
            if (createCategoryName != null) {
                disableActionByName(proxyRootNode, createCategoryName);
                for (Node childProxyNode : proxyRootNode.getChildren().getNodes()) {
                    disableActionByName(childProxyNode, createCategoryName);
                }
            }
        }
    }
    
    private static void disableActionByName(Node node, String actionName) {
        for (Action action : node.getActions(true)) {
            if (action != null) {
                String name = (String) action.getValue(Action.NAME);
                if (name != null && name.length() > 0) {
                    if (name.equals(actionName)) {
                        action.setEnabled(false);
                    }
                }
            } 
        }
    }
    
    
    private static class MyPaletteActions extends PaletteActions {
        public Action[] getImportActions() {
            return new Action[0];
        }
        public Action[] getCustomPaletteActions() {
            return new Action[0];
        }
        public Action[] getCustomCategoryActions(Lookup lookup) {
            return new Action[0];
        }
        public Action[] getCustomItemActions(Lookup lookup) {
            return new Action[0];
        }
        public Action getPreferredAction(Lookup lookup) {
            return null;
        }
    }
}
