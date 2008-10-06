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
package org.netbeans.modules.cnd.makeproject.api.configurations.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomizerNodeProvider;
import org.openide.util.Lookup;

public class CustomizerRootNodeProvider {

    private static CustomizerRootNodeProvider instance = null;
    private ArrayList<CustomizerNode> customizerNodes = null;

    public static CustomizerRootNodeProvider getInstance() {
        if (instance == null) {
            instance = new CustomizerRootNodeProvider();
        }
        return instance;
    }

    private List<CustomizerNode> getCustomizerNodesRegisteredOldStyle() {
        if (customizerNodes == null) {
            customizerNodes = new ArrayList();
        }
        return customizerNodes;
    }

    public List<CustomizerNode> getCustomizerNodes(boolean advanced) {
        ArrayList<CustomizerNode> ret = new ArrayList<CustomizerNode>();
        List<CustomizerNode> nodes = getCustomizerNodes();
        for (CustomizerNode n : nodes) {
            if (n != null && n.advanced == advanced) {
                ret.add(n);
            }
        }
        return ret;
    }

    public synchronized List<CustomizerNode> getCustomizerNodes() {
        ArrayList<CustomizerNode> list = new ArrayList<CustomizerNode>();
        // Add nodes from providers registerd old-style
        list.addAll(getCustomizerNodesRegisteredOldStyle());
        // Add nodes from providers register via services
        for (CustomizerNodeProvider provider : getCustomizerNodeProviders()) {
            list.add(provider.factoryCreate());
        }
        return list;
    }

    public CustomizerNode getCustomizerNode(String id) {
        List<CustomizerNode> nodes = getCustomizerNodes();
        for (CustomizerNode n : nodes) {
            if (n != null && n.name.equals(id)) {
                return n;
            }
        }
        return null;
    }
    
    public List<CustomizerNode> getCustomizerNodes(String id) {
        ArrayList<CustomizerNode> list = new ArrayList<CustomizerNode>();
        List<CustomizerNode> nodes = getCustomizerNodes();
        for (CustomizerNode n : nodes) {
            if (n != null && n.name.equals(id)) {
                list.add(n);
            }
        }
        return list;
    }

    public synchronized void addCustomizerNode(CustomizerNode node) {
        getCustomizerNodesRegisteredOldStyle().add(node);
    }

    public synchronized void removeCustomizerNode(CustomizerNode node) {
        getCustomizerNodesRegisteredOldStyle().remove(node);
    }

    /*
     * Get list (dynamic) registered via services
     */
    private static Set<CustomizerNodeProvider> getCustomizerNodeProviders() {
        HashSet providers = new HashSet();
        Lookup.Template template = new Lookup.Template(CustomizerNodeProvider.class);
        Lookup.Result result = Lookup.getDefault().lookup(template);
        Collection collection = result.allInstances();
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object caop = iterator.next();
            if (caop instanceof CustomizerNodeProvider) {
                providers.add(caop);
            }
        }
        return providers;
    }
}
