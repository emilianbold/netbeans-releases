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

package org.netbeans.performance.j2se.dialogs;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;

import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 * Abstract class for Test of Property Editors.
 *
 * @author  mmirilovic@netbeans.org
 */
public abstract class PropertyEditors extends PerformanceTestCase {
    
    protected static NbDialogOperator propertiesWindow = null;
    
    protected static JTableOperator tableOperator;
    
    /** Creates a new instance of ValidatePropertyEditors */
    public PropertyEditors(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of ValidatePropertyEditors */
    public PropertyEditors(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void shutdown() {
        if(propertiesWindow!=null && propertiesWindow.isShowing())
            propertiesWindow.close();
    }
    
    public void initialize(){
        propertiesWindow = openPropertySheet();
    }
    
    /** Open property sheet (bean customizer). */
    protected static NbDialogOperator openPropertySheet() {
        String waitDialogTimeout = "DialogWaiter.WaitDialogTimeout";
        long findTimeout = JemmyProperties.getCurrentTimeout(waitDialogTimeout);
        JemmyProperties.setCurrentTimeout(waitDialogTimeout, 3000);
        
        try{
            propertiesWindow = new NbDialogOperator(Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties", new Object[]{new Integer(1),"TestNode"}));
        }catch(org.netbeans.jemmy.TimeoutExpiredException exception){
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        new PropertyEditorsTestSheet();
                    }
                });
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            }
            
            propertiesWindow = new NbDialogOperator(Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties", new Object[]{new Integer(1),"TestNode"}));
        }
        
        JemmyProperties.setCurrentTimeout(waitDialogTimeout, findTimeout);
        
        return propertiesWindow;
    }
    
    protected static Property findProperty(String propertyName, NbDialogOperator propertiesWindow) {
        PropertySheetOperator propertySheet = new PropertySheetOperator(propertiesWindow);
        Property property = new Property(propertySheet, propertyName);
        
        // property.openEditor(); - doesn't work - custom editor is opened without Users Event
        // hack for invoking Custom Editor by pushing shortcut CTRL+SPACE
        tableOperator = propertySheet.tblSheet();
        // Need to request focus before selection because invokeCustomEditor action works
        // only when table is focused
        tableOperator.makeComponentVisible();
        tableOperator.requestFocus();
        tableOperator.waitHasFocus();
        // need to select property first
        ((javax.swing.JTable)tableOperator.getSource()).changeSelection(property.getRow(), 0, false, false);
        //        return new Property(new PropertySheetOperator(propertiesWindow), propertyName);
        return property;
    }
    
    protected static void openPropertyEditor(){
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK)).perform(tableOperator);
    }
    
}
