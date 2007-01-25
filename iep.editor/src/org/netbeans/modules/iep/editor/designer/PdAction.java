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

package org.netbeans.modules.iep.editor.designer;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import com.nwoods.jgo.*;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Hashtable;


// Define an Action that knows about views and supports enabling/disabling
// depending on the current context.
public abstract class PdAction extends AbstractAction {
    private static Map mActionMap = new Hashtable();

    private PlanDesigner mDesigner;

    public PdAction(String name, String shortDescription, PlanDesigner designer) {
        super(name);
        putValue(SHORT_DESCRIPTION, shortDescription);
        init(designer);
    }

    public PdAction(String name, String shortDescription, Icon icon, PlanDesigner designer) {
        super(name, icon);
        putValue(SHORT_DESCRIPTION, shortDescription);
        init(designer);
    }

    private void init(PlanDesigner designer) {
        mDesigner = designer;
        List actionList = (List)mActionMap.get(designer);
        if (actionList == null) {
            actionList = new Vector();
            mActionMap.put(designer, actionList);
        }
        actionList.add(this);
    }

    public PlanDesigner getDesigner() {
        return mDesigner;
    }

    public PdCanvas getCanvas() {
        return getDesigner().getCanvas();
    }

    public String toString() {
        return (String)getValue(NAME);
    }

    // by default each PdAction is disabled if there's no current view
    public boolean canAct() {
        return (getCanvas() != null);
    }

    public void updateEnabled() {
        setEnabled(canAct());
    }


    public void free() {
        List actionList = (List)mActionMap.get(mDesigner);
        if (actionList != null) {
            actionList.remove(this);
        }
        mDesigner = null;
    }

    // keep track of all instances of PdAction

    public static void updateAllActions(PlanDesigner designer) {
        List actionList = (List)mActionMap.get(designer);
        if (actionList != null) {
            for (int i = 0; i < actionList.size(); i++) {
                PdAction act = (PdAction)actionList.get(i);
                act.updateEnabled();
            }
        }
    }

    public static List allActions(PlanDesigner designer) {
        return (List)mActionMap.get(designer);
    }
  
    public static void freeAllActions(PlanDesigner designer) {
        List actionList = (List)mActionMap.get(designer);
        if (actionList != null) {
            for (int i = actionList.size() - 1; i >= 0; i--) {
                actionList.remove(i);
            }
            mActionMap.remove(designer);
        }
    }        

}
