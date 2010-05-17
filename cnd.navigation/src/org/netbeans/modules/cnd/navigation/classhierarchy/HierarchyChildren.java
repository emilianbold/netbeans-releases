/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.cnd.navigation.classhierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.navigation.services.HierarchyModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
public class HierarchyChildren extends Children.Keys<CsmClass> {
    private static Comparator<CsmClass> COMARATOR = new MyComparator();
    
    private CsmClass object;
    private HierarchyModel model;
    private HierarchyChildren parent;
    private boolean isInited = false;
    
    public HierarchyChildren(CsmClass object, HierarchyModel model, HierarchyChildren parent) {
        this.object = object;
        this.model = model;
        this.parent = parent;
    }
    
    public void dispose(){
        if (isInited) {
            isInited = false;
            setKeys(new CsmClass[0]);
        }
    }
    
    private synchronized void resetKeys(){
        if (object.isValid()) {
            Set<CsmClass> set = model.getModel().get(object);
            if (set != null && set.size() > 0) {
                List<CsmClass> list = new ArrayList<CsmClass>(set);
                Collections.sort(list, COMARATOR);
                setKeys(list);
                return;
            }
        }
        setKeys(new CsmClass[0]);
    }
    
    protected Node[] createNodes(CsmClass cls) {
        Node node = null;
        Set<CsmClass> set = model.getModel().get(cls);
        if (set == null || set.size() == 0) {
            node = new HierarchyNode(cls, Children.LEAF, model, false);
        } else {
            if (checkRecursion(cls)) {
                node = new HierarchyNode(cls, Children.LEAF, model, true);
            } else {
                node = new HierarchyNode(cls, model, this);
            }
        }
        return new Node[]{node};
    }
    
    private boolean checkRecursion(CsmClass cls){
        if (cls.equals(object)) {
            return true;
        }
        HierarchyChildren arr = parent;
        while (arr != null){
            if (cls.equals(arr.object)){
                return true;
            }
            arr = arr.parent;
        }
        return false;
    }
    
    @Override
    protected void addNotify() {
        isInited = true;
        resetKeys();
        super.addNotify();
    }
    
    @Override
    protected void removeNotify() {
        super.removeNotify();
        dispose();
    }
    
    private static class MyComparator implements Comparator<CsmClass> {
        public int compare(CsmClass o1, CsmClass o2) {
            String n1 = o1.getName().toString();
            String n2 = o2.getName().toString();
            return n1.compareTo(n2);
        }
    }
}
