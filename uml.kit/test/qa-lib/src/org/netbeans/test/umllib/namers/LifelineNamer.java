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



package org.netbeans.test.umllib.namers;

import java.awt.event.KeyEvent;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.test.umllib.EditControlOperator;
import org.netbeans.test.umllib.SetName;

public class LifelineNamer  implements SetName {
    
    public LifelineNamer() {
    }
    
    
    /**
     * names lifeline
     * if there is no ':' in name name consider as lifeline name
     */
    public void setName(ComponentOperator drawingArea, int x, int y, String name) {
        int semicolonPos = name.indexOf(':');
        String lineName = "";
        lineName = name;
        String classifierName = "";
        if (semicolonPos>0){
            lineName = name.substring(0,semicolonPos);
        }
        //if (semicolonPos<name.length()-1){
        if (semicolonPos>-1){
            classifierName = name.substring(semicolonPos+1);
        }        
        new EventTool().waitNoEvent(1000);            
        try{Thread.sleep(100);}catch(Exception ex){}
        drawingArea.clickMouse(x, y, 1);
        try{Thread.sleep(100);}catch(Exception ex){}
        new EventTool().waitNoEvent(1000);            
        
        EditControlOperator ec=null;
        /*try
        {
            ec=new EditControlOperator();
        }
        catch(org.netbeans.jemmy.TimeoutExpiredException ex)
        {*/
            drawingArea.pushKey(KeyEvent.VK_F2);
            try{Thread.sleep(100);}catch(Exception ex2){}
            ec=new EditControlOperator();
        //}
        //remove old and shifts to left
        for(int i=ec.getTextFieldOperator().getText().length()+3;i>=0;i--)
        {
            ec.getTextFieldOperator().pushKey(KeyEvent.VK_BACK_SPACE);
        }
        
        for(int i=0;i<lineName.length();i++) {
            ec.getTextFieldOperator().typeKey(lineName.charAt(i));
            new Timeout("",100).sleep();
        }
        if(semicolonPos>-1)ec.getTextFieldOperator().typeKey(':');
        for(int i=0;i<classifierName.length();i++) {            
            ec.getTextFieldOperator().typeKey(classifierName.charAt(i));
            new Timeout("",100).sleep();
        }
        
        new EventTool().waitNoEvent(1000);            
                
        ec.getTextFieldOperator().typeKey('\n');
        
        new Timeout("",500).sleep();
    }
}
