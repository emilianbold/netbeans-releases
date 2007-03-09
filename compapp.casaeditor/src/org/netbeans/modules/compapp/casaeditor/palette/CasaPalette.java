/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.palette;

import java.awt.datatransfer.DataFlavor;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.Action;
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
    
    public static enum CASA_CATEGORY_TYPE {
        WSDL_BINDINGS,
        SERVICE_UNITS,
        END_POINTS
    };
    
    public static enum CASA_PALETTE_ITEM_TYPE {
        CONSUME,
        PROVIDE,
        INT_SU,
        EXT_SU,
        UN_KNOWN
    }
    
    public static final DataFlavor CasaPaletteDataFlavor = new DataFlavor( Object.class, "CasaPaletteData" ) {  // NOI18N
    };
    
    private static PaletteController msPaletteController = null;
    private static String CASA_PALETTE_ROOT = "CasaPalette";    // NOI18N
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
