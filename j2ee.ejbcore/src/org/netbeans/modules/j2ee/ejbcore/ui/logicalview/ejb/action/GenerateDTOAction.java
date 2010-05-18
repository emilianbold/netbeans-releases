/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
