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


package org.netbeans.test.uml.relationshipdiscovery.utils;

import java.awt.Point;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DiagramToolbarOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.actions.DeleteElementAction;
import org.netbeans.test.umllib.exceptions.ElementVerificationException;
import org.netbeans.test.umllib.exceptions.NotFoundException;


public class BaseRelatioshipRecoveryVerifier {
    
    private DiagramOperator dia = null;
    private String baseNamePrefix="";
    private String relationshipPath="";
    private EventTool eventTool = new EventTool();
    
    private final String DELETE_MENU_PATH = "Edit|Delete";
    
    public final int ID_NOLINK=1;
    
    public BaseRelatioshipRecoveryVerifier(DiagramOperator dia) {
        this.dia = dia;
    }
    
    public BaseRelatioshipRecoveryVerifier(BaseRelatioshipRecoveryVerifier origin) {
        this.dia = origin.dia;
    }
    
    
    public boolean verifyElementsRelationRecovery(ElementTypes elementsType, LinkTypes linkType){
        return verifyElementsRelationRecovery(elementsType, elementsType, linkType);        
    }    
    
    
    public boolean verifyElementsRelationRecovery(ElementTypes elementType1, ElementTypes elementType2, LinkTypes linkType){
        
            String name1=elementType1+"1";
            String name2=elementType2+"2";
        
            if(elementType1.equals(ElementTypes.LIFELINE))name1+=":"+name1;
            if(elementType2.equals(ElementTypes.LIFELINE))name2+=":"+name2;
            
            DiagramElementOperator op1 = addElement(elementType1, name1);            
            DiagramElementOperator op2 = addElement(elementType2, name2); 
            
            LinkOperator lnk = addRelatioship(op1, op2, linkType);
            
            //to be removed later. Was added due to UML bug N
            dia.toolbar().selectDefault();
            
            deleteRelationship(lnk);
            
            recoverRelatioship(new DiagramElementOperator[]{op1, op2});
            
            boolean diagCheck = checkRelationshipOnDiagram(op1, op2, linkType);
            if (!diagCheck){
                return false;
            }
            return true;
    }
    
    
    
    protected DiagramElementOperator addElement(ElementTypes elementType, String elementName) throws NotFoundException{
        Point point = dia.getDrawingArea().getFreePoint(200);            
        DiagramElementOperator el = dia.putElementOnDiagram(elementName, elementType, point.x, point.y);
        eventTool.waitNoEvent(1000);
        try{Thread.sleep(100);}catch(Exception ex){}
        return el;
    }
    
    
    protected LinkOperator addRelatioship(DiagramElementOperator el1, DiagramElementOperator el2, LinkTypes linkType) throws NotFoundException{
        dia.createGenericRelationshipOnDiagram(linkType, el1, el2);                    
        eventTool.waitNoEvent(1000); 
        try{Thread.sleep(100);}catch(Exception ex){}
        return new LinkOperator(el1, el2);        
    }
    
    
    protected void deleteRelationship(LinkOperator lnk) throws NotFoundException{        
        
        new DeleteElementAction().performShortcut(lnk);
        
        JDialogOperator delDlg = new JDialogOperator(Util.DELETE_DLG);
        //new EventTool().waitNoEvent(1000);
        //JCheckBoxOperator chb = new JCheckBoxOperator(delDlg, 0);
        //chb.clickMouse();
        new EventTool().waitNoEvent(500);
        new JButtonOperator(delDlg, Util.YES_BTN).push();        
        //eventTool.waitNoEvent(1000);
    }
    
    
    protected void recoverRelatioship(DiagramElementOperator[] elements) throws NotFoundException{
        for(int i=0; i<elements.length; i++){
            elements[i].addToSelection();
        } 
        eventTool.waitNoEvent(1500);
        dia.toolbar().selectTool(DiagramToolbarOperator.RELATIONSHIP_DISCOVERY_TOOL);
        eventTool.waitNoEvent(1000);        
    }
    
    public boolean checkRelationshipOnDiagram(ElementTypes elementType1, ElementTypes elementType2, LinkTypes linkType)
    {
            DiagramElementOperator op1 = new DiagramElementOperator(dia, elementType1+"1",elementType1);            
            DiagramElementOperator op2 = new DiagramElementOperator(dia, elementType2+"2",elementType2); 
        return checkRelationshipOnDiagram(op1,op2,linkType);
    }
    
    protected boolean checkRelationshipOnDiagram(DiagramElementOperator source, DiagramElementOperator destination, LinkTypes linkType){
        try{
            new LinkOperator(source, destination, linkType);
            return true;
        }catch(Exception e){
            //throw e;
            LinkOperator any=LinkOperator.findLink(source, destination,new LinkOperator.LinkByTypeChooser(LinkTypes.ANY),0);
            String msg="Can't find link "+linkType+" between "+source +" and "+destination+":::"+any+":::"+((any!=null)?any.getType():"");
            throw new ElementVerificationException(msg,ID_NOLINK);
        }
    }
    
    
}
