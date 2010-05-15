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

import org.netbeans.modules.iep.editor.designer.actions.PlanDesignViewOpenAction;
import org.netbeans.modules.iep.model.IEPModel;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.windows.OutputWriter;
import org.openide.windows.IOProvider;

//import org.netbeans.modules.xml.refactoring.ui.ModelProvider;


/** A node to represent this object. */
public class PlanNode extends DataNode /*implements ModelProvider*/ {

    private PlanDataObject mObj;
    
    public PlanNode(PlanDataObject obj) {
        this (obj, Children.LEAF);
    }

    public PlanNode(DataObject obj, Children ch) {
        super (obj, ch);
        mObj = (PlanDataObject) obj;
        setIconBaseWithExtension("org/netbeans/modules/iep/editor/eventProcess.png");

        ///ritgetCookieSet().add(new PlanController(this));
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(PlanDesignViewOpenAction.class);
    }
    
//    public Action getPreferredAction () {
//        return SystemAction.get (OpenAction.class);
//    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.iep.editor.PlanNode");   
    }

   
    public void updateNode() {
    }
    
   

   
    private void log(String str) {
        OutputWriter out = IOProvider.getDefault().getStdOut();
        out.println(str);
        out.flush();
    }
    
    public IEPModel getModel() {

          PlanDataObject dobj = (PlanDataObject) getDataObject();
          return dobj.getPlanEditorSupport().getModel();
    
  }
}
