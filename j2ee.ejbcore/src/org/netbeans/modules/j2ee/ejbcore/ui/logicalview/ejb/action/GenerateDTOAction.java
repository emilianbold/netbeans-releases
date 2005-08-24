/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;


import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.modules.j2ee.ejbcore.patterns.DTOGenerator;
import org.netbeans.modules.j2ee.ejbcore.patterns.DTOHelper;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/** Action sensitive to the node selection that does something useful.
 * Consider using a cookie action instead if you can define what the
 * action is applicable to in terms of cookies.
 * @author blaha
 */
public class GenerateDTOAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
            Feature mE = getMemberElement(nodes[0]);
            DTOHelper dtoHelp = new DTOHelper(mE);
            DTOGenerator dtoGen = new DTOGenerator();
            try{
                dtoGen.generateDTO(dtoHelp, null, false);
            }catch(java.io.IOException ex){
                ErrorManager.getDefault().notify(ex);
            }
    }
    
    protected boolean enable(Node[] nodes) {
        EjbMethodController c;
        return nodes.length == 1 &&
                isMemberElement(nodes[0]) &&
                (c = EjbMethodController.create(getMemberElement(nodes[0]))) != null &&
                c instanceof EntityMethodController &&
                ((EntityMethodController) c).isCMP();
    }
    
    public String getName() {
        return NbBundle.getMessage(GenerateDTOAction.class, "LBL_GenerateDTOAction");
    }
    
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(GenerateDTOAction.class);
    }
    
    protected boolean asynchronous() {
        // performAction(Node[]) should run in event thread
        return false;
    }
        
    private Feature getMemberElement(Node node) {
        return (Feature) node.getLookup().lookup(Feature.class);
    }
    
    private boolean isMemberElement(Node node){
        return getMemberElement(node) != null;
    }
}
