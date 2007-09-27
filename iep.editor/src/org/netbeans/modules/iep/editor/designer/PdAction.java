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
