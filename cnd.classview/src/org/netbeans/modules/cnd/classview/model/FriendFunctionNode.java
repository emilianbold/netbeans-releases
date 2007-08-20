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

import java.awt.Image;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.nodes.Children;

/**
 *
 * @author Alexander Simon
 */
public class FriendFunctionNode extends ObjectNode {
    
    public FriendFunctionNode(CsmFriendFunction fun) {
        super(fun, Children.LEAF);
        init(fun);
    }
    
    private void init(CsmFriendFunction fun){
        String text = CVUtil.getSignature(fun);
        setName(text);
        setDisplayName(text);
        setShortDescription(text);
    }

    @Override
    public Image getIcon(int param) {
	CsmFriendFunction csmObj = (CsmFriendFunction) getCsmObject();
        return (csmObj == null) ? super.getIcon(param) : CsmImageLoader.getFriendFunctionImage(csmObj);
    }
    
    @Override
    public Image getOpenedIcon(int param) {
        return getIcon(param);
    }

    public void stateChanged(ChangeEvent e) {
        Object o = e.getSource();
        if (o instanceof CsmFriendFunction){
            CsmFriendFunction cls = (CsmFriendFunction)o;
            setObject(cls);
            init(cls);
            fireIconChange();
            fireOpenedIconChange();
        } else if (o != null) {
            System.err.println("Expected CsmFriendFunction. Actually event contains "+o.toString());
        }
    }
}
