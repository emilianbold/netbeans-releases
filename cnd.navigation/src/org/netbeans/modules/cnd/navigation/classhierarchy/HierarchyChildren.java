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

package org.netbeans.modules.cnd.navigation.classhierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
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
    private boolean isInited = false;
    
    public HierarchyChildren(CsmClass object, HierarchyModel model) {
        this.object = object;
        this.model = model;
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
            node = new HierarchyNode(cls, Children.LEAF);
        } else {
            node = new HierarchyNode(cls, model);
        }
        return new Node[]{node};
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
            String n1 = o1.getName();
            String n2 = o2.getName();
            return n1.compareTo(n2);
        }
    }
}
