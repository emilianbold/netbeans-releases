/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.navigation.includeview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
public class IncludedChildren extends Children.Keys<CsmFile> {
    private static Comparator<CsmFile> COMARATOR = new MyComparator();
    
    private CsmFile object;
    private IncludedChildren parent;
    private IncludedModel model;
    private boolean isInited = false;
    
    public IncludedChildren(CsmFile object, IncludedModel model, IncludedChildren parent) {
        this.object = object;
        this.parent = parent;
        this.model = model;
    }
    
    public void dispose(){
        if (isInited) {
            isInited = false;
            setKeys(new CsmFile[0]);
        }
    }
    
    private synchronized void resetKeys(){
        if (object.isValid()) {
            Set<CsmFile> set = model.getModel().get(object);
            if (set != null && set.size() > 0) {
                List<CsmFile> list = new ArrayList<CsmFile>(set);
                Collections.sort(list, COMARATOR);
                setKeys(list);
                return;
            }
        }
        setKeys(new CsmFile[0]);
    }
    
    protected Node[] createNodes(CsmFile file) {
        Node node = null;
        Set<CsmFile> set = model.getModel().get(file);
        if (set == null || set.size() == 0) {
            node = new IncludeNode(file, Children.LEAF, false);
        } else {
            if (checkRecursion(file)) {
                node = new IncludeNode(file, Children.LEAF, true);
            } else {
                node = new IncludeNode(file, model, this);
            }
        }
        return new Node[]{node};
    }
    
    private boolean checkRecursion(CsmFile file){
        IncludedChildren arr = parent;
        while (arr != null){
            if (file == arr.object){
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
    
    private static class MyComparator implements Comparator<CsmFile> {
        public int compare(CsmFile o1, CsmFile o2) {
            String n1 = o1.getName();
            String n2 = o2.getName();
            return n1.compareTo(n2);
        }
    }
}
