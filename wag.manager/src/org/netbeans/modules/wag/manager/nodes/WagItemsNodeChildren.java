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
package org.netbeans.modules.wag.manager.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.modules.wag.manager.model.WagItems;
import org.netbeans.modules.wag.manager.model.WagService;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

public class WagItemsNodeChildren<T extends WagItems, C extends WagItems>
        extends Children.Keys<Object> implements PropertyChangeListener {

    private static final String LOADING_KEY = "Loading...";    //NOI18N

    protected T wagItems;

    public WagItemsNodeChildren(T wagItems) {
        this.wagItems = wagItems;
        wagItems.addPropertyChangeListener(WeakListeners.propertyChange(this, wagItems));
    }

    @Override
    protected void addNotify() {
        updateKeys();
        super.addNotify();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        updateKeys();
    }

    protected void updateKeys() {
        switch (wagItems.getState()) {
            case UNINITIALIZED:
                wagItems.refresh();
                break;
            case LOADING:
                setKeys(Arrays.asList(LOADING_KEY));
                break;
            case INITIALIZED:
                ArrayList<Object> keys = new ArrayList<Object>();
                keys.addAll(wagItems.getItems());
                setKeys(keys);
                break;
            default:
                break;
        }
    }

    @Override
    protected void removeNotify() {
        java.util.List<String> emptyList = Collections.emptyList();
        setKeys(emptyList);
        super.removeNotify();
    }

    protected Node[] createNodes(Object key) {
        if (key instanceof WagItems) {
            return new Node[] {new WagItemsNode((WagItems) key)};
        } else if (key instanceof WagService) {
            return new Node[] {new WagServiceNode((WagService) key)};
        } else if (key instanceof String) {
            if (key.equals(LOADING_KEY)) {
                return getLoadingNode();
            }
        }
        return new Node[0];
    }

    private Node[] getLoadingNode() {
        AbstractNode node = new AbstractNode(Children.LEAF);
        node.setName(NbBundle.getMessage(WagItemsNodeChildren.class, "Loading")); //NOI18N
        node.setIconBaseWithExtension("org/netbeans/modules/wag/manager/resources/wait.gif"); // NOI18N
        return new Node[] { node };
    }
}
