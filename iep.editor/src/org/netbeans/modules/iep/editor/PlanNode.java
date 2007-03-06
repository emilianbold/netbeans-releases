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

package org.netbeans.modules.iep.editor;

import javax.swing.Action;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.actions.OpenAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.OutputWriter;
import org.openide.windows.IOProvider;

import org.netbeans.modules.iep.editor.model.Plan;
import org.netbeans.modules.iep.editor.model.ModelManager;

/** A node to represent this object. */
public class PlanNode extends DataNode {

    private Plan mPlan = null;
    private PlanDataObject mObj;
    
    public PlanNode(PlanDataObject obj) {
        this (obj, Children.LEAF);
    }

    public PlanNode(DataObject obj, Children ch) {
        super (obj, ch);
        mObj = (PlanDataObject) obj;
        setIconBaseWithExtension("org/netbeans/modules/iep/editor/eventProcess.png");

        getCookieSet().add(new PlanController(this));
    }

    public Action getPreferredAction () {
	    return SystemAction.get (OpenAction.class);
    }
    
   
    public void updateNode() {
    }
    
    public void discardPlan() {
        ModelManager.destroyModel(mPlan);
        mPlan = null;
    }

    /**
     * Describe <code>getSchema</code> method here.
     *
     * @return a <code>XMLSchema</code> value
     */
    public Plan getPlan() {
        if (mPlan == null) {
            mPlan = ModelManager.getPlan(mObj);
        }
        return mPlan;
    }

    private void log(String str) {
        OutputWriter out = IOProvider.getDefault().getStdOut();
        out.println(str);
        out.flush();
    }
    
}
