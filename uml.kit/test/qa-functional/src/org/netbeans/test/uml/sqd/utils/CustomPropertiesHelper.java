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

import org.netbeans.jellytools.properties.Property;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

public class CustomPropertiesHelper {
    
    public CustomPropertiesHelper() {
    }
    
    public final String STEREOTYPES_TTL = "Stereotypes";
    public final String TAGGED_VALUES = "Tagged Values";
    public final String CONSTRAINTS = "Constraints";
    
    
    
    public void addStereotype(String sterName, Property prop){        
        new EventTool().waitNoEvent(1000);                
        prop.openEditor();
        JDialogOperator dlg = new JDialogOperator(STEREOTYPES_TTL);
        new JButtonOperator(dlg, "Add").pushNoBlock();
        new EventTool().waitNoEvent(1000);        
        //JListOperator list = new JListOperator(dlg);
        JTableOperator table = new JTableOperator(dlg);
        table.clickOnCell(table.getRowCount()-1,0, 2);
        new EventTool().waitNoEvent(500);
        //list.clickOnItem(list.getModel().getSize()-1, 1);
        new JComboBoxOperator(dlg).enterText(sterName);
        org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("afterCustomizer_");
        new JButtonOperator(dlg,"OK").pushNoBlock();
        dlg.waitClosed();
    }
    
    
    public void addTaggedValue(String name, String alias, String value, Property prop){
        new EventTool().waitNoEvent(1000);
        prop.openEditor();
        JDialogOperator dlg = new JDialogOperator(TAGGED_VALUES);
        new JButtonOperator(dlg, "Add").pushNoBlock();
        new EventTool().waitNoEvent(1000);        
        //JListOperator list = new JListOperator(dlg);
        JTableOperator table = new JTableOperator(dlg);
        table.clickOnCell(table.getRowCount()-1, 0, 2);
        new EventTool().waitNoEvent(500);
        new JTextFieldOperator(dlg).typeText(name);
        
        table.clickOnCell(table.getRowCount()-1, 1, 2);
        new EventTool().waitNoEvent(500);
        new JTextFieldOperator(dlg).typeText(alias);
        
        table.clickOnCell(table.getRowCount()-1, 2, 2);
        new EventTool().waitNoEvent(500);
        new JTextFieldOperator(dlg).typeText(value);
        
        new JButtonOperator(dlg,"OK").pushNoBlock();
        dlg.waitClosed();
    }
    
    
    
    public void addConstraints(String name, String expression, Property prop){
        new EventTool().waitNoEvent(1000);
        prop.openEditor();
        JDialogOperator dlg = new JDialogOperator(CONSTRAINTS);
        new JButtonOperator(dlg, "Add").pushNoBlock();
        //JListOperator list = new JListOperator(dlg);
        new EventTool().waitNoEvent(1000);        
        JTableOperator table = new JTableOperator(dlg);
        table.clickOnCell(table.getRowCount()-1, 0, 2);
        new EventTool().waitNoEvent(500);
        new JTextFieldOperator(dlg).typeText(name);
        
        table.clickOnCell(table.getRowCount()-1, 1, 2);
        new EventTool().waitNoEvent(500);
        new JTextFieldOperator(dlg).typeText(expression);
        
        new JButtonOperator(dlg,"OK").pushNoBlock();
        dlg.waitClosed();
    }
    
}
