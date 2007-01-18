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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;


import javax.lang.model.element.Element;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
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
        //TODO: RETOUCHE
//            Element mE = getMemberElement(nodes[0]);
//            DTOHelper dtoHelp = new DTOHelper(mE);
//            DTOGenerator dtoGen = new DTOGenerator();
//            try{
//                dtoGen.generateDTO(dtoHelp, null, false);
//            }catch(java.io.IOException ex){
//                ErrorManager.getDefault().notify(ex);
//            }
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length < 1) {
            return false;
        }
        EjbMethodController ejbMethodController;
        Element feature = getMemberElement(nodes[0]);
        if (feature == null) {
            return false;
        }
        //TODO: RETOUCHE
        return false;
//        return nodes.length == 1 &&
//                isMemberElement(nodes[0]) &&
//                (ejbMethodController = EjbMethodController.create(feature)) != null &&
//                ejbMethodController instanceof EntityMethodController &&
//                ((EntityMethodController) ejbMethodController).isCMP();
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
        
    private Element getMemberElement(Node node) {
        //TODO: RETOUCHE element in lookup
        return null;
//        return (Feature) node.getLookup().lookup(Feature.class);
    }
    
    private boolean isMemberElement(Node node){
        return getMemberElement(node) != null;
    }
}
