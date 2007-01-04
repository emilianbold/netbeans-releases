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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.classview.model;

import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.classview.SmartChangeEvent;
import org.netbeans.modules.cnd.classview.model.CVUtil.FillingDone;
import org.netbeans.modules.cnd.classview.model.CVUtil.LazyClassifierSortedArray;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
public class ClassifierNode  extends ObjectNode {
    private FillingDone inited;
    
    protected ClassifierNode(CsmCompoundClassifier obj, FillingDone init) {
        super(obj, new LazyClassifierSortedArray(obj, init));
        inited = init;
    }
    
    public boolean isInited(){
        synchronized (inited){
            return inited.isFillingDone();
        }
    }

    protected int getWeight() {
        return CLASSIFIER_WEIGHT;
    }
    
    public boolean update(SmartChangeEvent e) {
        if (!isDismissed()) {
            if (isInited()) {
                if( super.update(e) ) {
                    return true;
                }
            } else{
                String uniqueName = getUniqueName();
                if( e.getChangedUniqueNames().contains(uniqueName) ) {
                    CsmOffsetableDeclaration decl = getObject();
                    if (decl != null){
                        setObject(decl);
                    } else {
                        final Children children = getParentNode().getChildren();
                        children.MUTEX.writeAccess(new Runnable(){
                            public void run() {
                                children.remove(new Node[] { ClassifierNode.this });
                            }
                        });
                    }
                } else if( e.getRemovedUniqueNames().contains(uniqueName) ) {
                    final Children children = getParentNode().getChildren();
                    children.MUTEX.writeAccess(new Runnable(){
                        public void run() {
                            children.remove(new Node[] { ClassifierNode.this });
                        }
                    });
                    return true;
                }
            }
        }
        return false;
    }
    
    public void dismiss() {
        setDismissed();
        if (isInited()){
            super.dismiss();
        }
    }
}
