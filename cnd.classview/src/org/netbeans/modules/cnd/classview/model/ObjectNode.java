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

package org.netbeans.modules.cnd.classview.model;
import java.util.Collection;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.services.CsmFriendResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.classview.PersistentKey;
import org.openide.nodes.*;

import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.classview.actions.GoToDeclarationAction;
import org.netbeans.modules.cnd.classview.actions.MoreDeclarations;

/**
 * @author Vladimir Kvasihn
 */
public abstract class ObjectNode extends BaseNode implements ChangeListener {
    private PersistentKey key;
    
    public ObjectNode(CsmOffsetableDeclaration declaration) {
        this(declaration, Children.LEAF);
    }
    
    public ObjectNode(CsmOffsetableDeclaration declaration, Children children) {
        super(children);
        setObject(declaration);
    }
    
    /** Implements AbstractCsmNode.getData() */
    public CsmObject getCsmObject() {
        return getObject();
    }
    
    public CsmOffsetableDeclaration getObject() {
        return (CsmOffsetableDeclaration) key.getObject();
    }
    
    protected void setObject(CsmOffsetableDeclaration declaration) {
        key = PersistentKey.createKey(declaration);
    }
    
    @Override
    public Action getPreferredAction() {
        return createOpenAction();
    }
    
    private Action createOpenAction() {
        CsmOffsetableDeclaration decl = getObject();
        if (decl != null) {
            return new GoToDeclarationAction(decl);
        }
        return null;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Action action = createOpenAction();
        if (action != null){
            CsmOffsetableDeclaration decl = getObject();
            String name = decl.getUniqueName();
            CsmProject project = decl.getContainingFile().getProject();
            if (project != null){
                Collection<CsmOffsetableDeclaration> arr = project.findDeclarations(name);
                for(CsmFriend friend : CsmFriendResolver.getDefault().findFriends(decl)){
                    if (CsmKindUtilities.isFriendMethod(friend)) {
                        arr.add(friend);
                    }
                }

                if (arr.size() > 1){
                    Action more = new MoreDeclarations(arr);
                    return new Action[] { action, more };
                }
            }
            
            return new Action[] { action };
        }
        return new Action[0];
    }
}
