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

import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.classview.Diagnostic;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import java.awt.Image;
import java.util.*;
import org.netbeans.modules.cnd.classview.actions.GoToDeclarationAction;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.nodes.*;
import org.openide.util.Utilities;

import  org.netbeans.modules.cnd.api.model.*;


/**
 * @author Vladimir Kvasihn
 */
public class EnumeratorNode extends BaseNode {

    private static Image image = null;    
    
    public EnumeratorNode(CsmEnumerator enumerator) {
	super(Children.LEAF);
        String name = enumerator.getName();
        setName(name);
        setDisplayName(name);
        setShortDescription(name);
        if( image == null ) {
	    image = CsmImageLoader.getImage(enumerator);
	}
    }

    public Image getIcon(int param) {
	return image;
    }

    /** Implements AbstractCsmNode.getData() */
    public CsmObject getCsmObject() {
	return null;
    }
    
//
// temporarily commented out
//    
//    public Action getPreferredAction() {
//        return createOpenAction();
//    }
//
//    protected Action createOpenAction() {
//        return new GoToDeclarationAction(enumerator);
//    }   
//    
//    public Action[] getActions(boolean context) {
//        return new Action[] { createOpenAction() };
//    }    
    
    public void dismiss() {
        setDismissed();
        //enumerator = null;
        super.dismiss();
    }
    


}

