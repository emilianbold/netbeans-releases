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


package org.netbeans.test.uml.sqd.utils;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.input.KeyEventDriver;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.actions.DiagramElementAction;
import org.netbeans.test.umllib.exceptions.ElementVerificationException;
import org.netbeans.test.umllib.util.JPopupByPointChooser;


public class GenericContextMenuVerifier {
    
    DiagramElementOperator element = null;
    LinkOperator link = null;
    DiagramOperator diagram = null;
    EventTool eventTool = new EventTool();
    
    public static final int ID_ENABLE=1;
    
    
    public GenericContextMenuVerifier(DiagramElementOperator act, DiagramOperator diagram) {
        element = act;
        link=null;
        this.diagram = diagram;
    }
    public GenericContextMenuVerifier(LinkOperator act, DiagramOperator diagram) {
        element = null;
        link=act;
        this.diagram = diagram;
    }
    
    public boolean verifyElement(String menuPath, boolean enabled){
        boolean res = true;
        //
        if(false && element!=null && element.getType().equals(ElementTypes.COMBINED_FRAGMENT.toString()))
        {
            //select
            try{Thread.sleep(100);}catch(Exception ex){}
            diagram.getDrawingArea().pushKey(KeyEvent.VK_ESCAPE);
            diagram.getDrawingArea().clickMouse(5,20,1);
            element.waitSelection(false);
            for(int i=0;i<diagram.getAllDiagramElements().size();i++)
            {
                diagram.getDrawingArea().pushKey(KeyEvent.VK_TAB);
                try
                {
                    element.waitSelection(true,1000);
                }
                catch(Exception ex)
                {
                    
                }
            }
            element.waitSelection(true,1000);
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        //
        res = checkItemEnabled(menuPath, enabled);
        if (!res){
            throw new ElementVerificationException("First verification step failed/enabling.");
        }
        
        if (!enabled){
            ///??? old code strange
            return true;
        }
        
        res = invokeAction(menuPath);
        if (!res){
            throw new ElementVerificationException("Second verification step failed/invoce action.");
        }
        
        res = checkActionResult();
        
        return res;
    }
    
    
    protected boolean checkItemEnabled(String menuPath, boolean enabled){        
        /*
        JPopupMenuOperator menu = element.getPopup();
        JMenuItemOperator item = menu.showMenuItem(menuPath);
        boolean itemEnabled = item.isEnabled();
        eventTool.waitNoEvent(500);
        Point p = diagram.getDrawingArea().getFreePoint(50);
        diagram.getDrawingArea().clickMouse(p.x, p.y, 1);
        return (itemEnabled==enabled);        
        */
        JPopupMenuOperator menu = null;
        if(false && element!=null && element.getType().equals(ElementTypes.COMBINED_FRAGMENT.toString()))
        {
            try{Thread.sleep(100);}catch(Exception ex){}
            diagram.getDrawingArea().clickMouse();
            element.waitSelection(false);
            for(int i=0;i<diagram.getAllDiagramElements().size();i++)
            {
                diagram.getDrawingArea().pushKey(KeyEvent.VK_TAB);
                try
                {
                    element.waitSelection(true,1000);
                }
                catch(Exception ex)
                {
                    
                }
            }
            element.waitSelection(true,1000);
            //diagram.getDrawingArea().pushKey(KeyEvent.VK_F10,KeyEvent.SHIFT_DOWN_MASK);
            new MouseRobotDriver(new Timeout("autoDelay",50)).moveMouse(diagram.getDrawingArea(),0,0);
            try{Thread.sleep(500);}catch(Exception ex){};
            new KeyEventDriver().pushKey(diagram.getDrawingArea(),KeyEvent.VK_F10,KeyEvent.SHIFT_DOWN_MASK,new Timeout("pushtime",30));
            try{Thread.sleep(500);}catch(Exception ex){};
            org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("afterPush");
            menu=new JPopupMenuOperator(JPopupMenuOperator.waitJPopupMenu((java.awt.Container)(MainWindowOperator.getDefault().getSource()),new JPopupByPointChooser(element.getBoundingRect().getLocation(),diagram.getDrawingArea().getSource(),0)));
        }
        else
        {
            try{Thread.sleep(100);}catch(Exception ex){}
            menu=(element!=null?element:link).getPopup();
        }
        JMenuItemOperator item = menu.showMenuItem(menuPath);
        try{Thread.sleep(100);}catch(Exception ex){}
        org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("afterItem");
        boolean itemEnabled = item.isEnabled();
        if (itemEnabled==enabled)
        {
            diagram.getDrawingArea().pushKey(KeyEvent.VK_ESCAPE);
            if(menu!=null)
            {
                menu.pushKey(KeyEvent.VK_ESCAPE);
                menu.waitComponentVisible(false);
            }
            eventTool.waitNoEvent(500);        
            try{Thread.sleep(100);}catch(Exception ex){}
            return true;
        }
        else throw new ElementVerificationException("Unexpeced enabling status for "+menuPath+", required: "+enabled+", current: "+itemEnabled,ID_ENABLE);
    }
    
    
    protected boolean invokeAction(final String popupPath){
        //new Thread(new Runnable() {
        //    public void run() {
                if(element!=null && element.getType().equals(ElementTypes.COMBINED_FRAGMENT.toString()))
                {
                    /*try{Thread.sleep(100);}catch(Exception ex){}
                    diagram.getDrawingArea().pushKey(KeyEvent.VK_ESCAPE);
                    diagram.getDrawingArea().clickMouse(5,20,1);
                    element.waitSelection(false);
                    for(int i=0;i<diagram.getAllDiagramElements().size();i++)
                    {
                        diagram.getDrawingArea().pushKey(KeyEvent.VK_TAB);
                        try
                        {
                            element.waitSelection(true,1000);
                        }
                        catch(Exception ex)
                        {

                        }
                    }
                    element.waitSelection(true,1000);
                    //diagram.getDrawingArea().pushKey(KeyEvent.VK_F10,KeyEvent.SHIFT_DOWN_MASK);
                    new MouseRobotDriver(new Timeout("autoDelay",50)).moveMouse(diagram.getDrawingArea(),0,0);
                    new KeyEventDriver().pushKey(diagram.getDrawingArea(),KeyEvent.VK_F10,KeyEvent.SHIFT_DOWN_MASK,new Timeout("pushtime",30));
                    JPopupMenuOperator m=new JPopupMenuOperator(JPopupMenuOperator.waitJPopupMenu((java.awt.Container)(MainWindowOperator.getDefault().getSource()),new JPopupByPointChooser(element.getBoundingRect().getLocation(),diagram.getDrawingArea().getSource(),0)));*/
                    JPopupMenuOperator m=element.getPopup();
                    JMenuItemOperator item=m.showMenuItem(popupPath);
                    item.waitComponentShowing(true);
                    try{Thread.sleep(100);}catch(Exception ex){}
                    org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("beforePush");
                    item.pushNoBlock();
                    
                }
                else
                {
                    org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("beforePerforme");
                    new DiagramElementAction(null, popupPath).performPopupNoBlock(element!=null?element:link);
                }
        //    }
        //}).start();
        eventTool.waitNoEvent(500);
        try{Thread.sleep(500);}catch(Exception ex){}
        return true;
    }
    
    
    protected boolean checkActionResult(){
        new org.netbeans.jellytools.actions.Action(null,null,new org.netbeans.jellytools.actions.Action.Shortcut(KeyEvent.VK_ESCAPE)).performShortcut();
        new org.netbeans.jellytools.actions.Action(null,null,new org.netbeans.jellytools.actions.Action.Shortcut(KeyEvent.VK_ESCAPE)).performShortcut();
        try{Thread.sleep(100);}catch(Exception ex){}
        return true;
    }
        
}
