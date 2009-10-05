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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteCategoryID;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteItemID;
import org.netbeans.modules.compapp.casaeditor.api.CasaPalettePlugin;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 *
 * @author rdara
 * @author chikkala
 */
public class CasaPaletteCategoryChildren extends Children.Keys<CasaPaletteCategoryID> {
    private static Logger sLogger = Logger.getLogger(CasaPaletteCategoryChildren.class.getName());
    private ChangeListener mChangeListener = null;

    private Lookup mLookup;

    public CasaPaletteCategoryChildren(Lookup lookup) {
        mLookup = lookup;
        initJbiDefaultComponentInfoChangeListener();
    }
    /**
     * creates and registers the ChangeListener on JBIDefaultComponentInfo and
     * when state changes it refreshes the keys to reload the palette
     */
    private void initJbiDefaultComponentInfoChangeListener() {
        
        mChangeListener = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                sLogger.fine("on jbi comp info state change. refershing keys..."); //NOI18N
                refreshKeys();
            }
            
        };
        // add change listener for JbiDefaultComponentInfo. use weak listener
        JbiDefaultComponentInfo compInfo = JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
        ChangeListener weakListener = WeakListeners.change(mChangeListener, compInfo);
        compInfo.addChangeListener(weakListener);
    }
    /**
     * reloads the palette nodes
     */
    private void refreshKeys() {
        setKeys(new ArrayList<CasaPaletteCategoryID>());
        addNotify();
    }
    /**
     * remove all the palette node keys
     */
    @Override
    protected void removeNotify() {
        setKeys(new ArrayList<CasaPaletteCategoryID>());
    }
    
    
    protected Node[] createNodes(CasaPaletteCategoryID key) {
        return new Node[]{new CasaPaletteCategoryNode(key, mLookup)                };
    }

    @Override
    protected void addNotify() {
        super.addNotify();

        java.util.Map<String, CasaPaletteCategoryID> categoryMap = new HashMap<String, CasaPaletteCategoryID>();
        List<CasaPaletteCategoryID> categories = new ArrayList<CasaPaletteCategoryID>();
        Collection<? extends CasaPalettePlugin> plugins = CasaPalette.getPlugins();
        for (CasaPalettePlugin plugin : plugins) {
            for (CasaPaletteItemID itemID : plugin.getItemIDs()) {
                String categoryName = itemID.getCategory();
                CasaPaletteCategoryID categoryID = categoryMap.get(categoryName);
                if (categoryID == null) {
                    categoryID = new CasaPaletteCategoryID(categoryName);
                    categoryMap.put(categoryName, categoryID);
                    categories.add(categoryID);
                }
                categoryID.addItem(itemID);
            }
        }

        setKeys(categories);
    }
}
