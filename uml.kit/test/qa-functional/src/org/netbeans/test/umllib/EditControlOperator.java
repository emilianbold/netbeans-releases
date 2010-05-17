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


/*
 * EditControlOperator.java
 *
 */

package org.netbeans.test.umllib;
import java.awt.Frame;
import javax.swing.JComponent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.modules.uml.ui.controls.editcontrol.EditControlImpl;

/**
 * class handles current EditControl
 * @author psb
 */
public class EditControlOperator extends JComponentOperator {
    
    static{
        DriverManager.setKeyDriver(new KeyRobotDriver(new Timeout("autoDelay",50)));     
    }
    
   
    
    /** 
     * Creates a new instance of first visible EditControlOperator 
     */
    public EditControlOperator() {
        this(MainWindowOperator.getDefault());
    }

    public EditControlOperator(ContainerOperator cont) {
        super(cont, new EditControlChooser());
    }
        /**
     * Creates a new instance of EditControlOperator
     * use case insensitive an
     * @param text in panel
     */
     public EditControlOperator(String text) {
       super(MainWindowOperator.getDefault(), new EditControlChooserByText(text));
        
    }
    /**
     * Creates a new instance of EditControlOperator 
     * @param text in panel
     * @param ce -compare exactly
     * @param cs - case sensitive
     */
     public EditControlOperator(String text, boolean ce, boolean cs) {
       super(MainWindowOperator.getDefault(), new EditControlChooserByText(text,ce,cs));  
    }
   /**
     * Find textfield in edit control panel
     * we assume only one textfield in panel for now
     * @return JTextFieldOperator for first textfield in edit control panel
     */
    public JTextFieldOperator getTextFieldOperator() {
        return new JTextFieldOperator(this);
    }
   /**
     * Find textfield in edit control panel
     * we assume only one textfield in panel for now
     * @return JTextFieldOperator for first textfield in edit control panel
     */
    public JTextAreaOperator getTextAreaOperator() {
        return new JTextAreaOperator(this);
    }
    
    /**
     * TBD - check if there is TextAre instead of textfield
     * @param text 
     */
    public void typeText(String text){
        getTextFieldOperator().typeText(text);
        getTextFieldOperator().typeKey('\n');
    }
 
};


/**
 * Findder for Edit Control panel (based on current realization)
 */
class EditControlChooser implements ComponentChooser
{
    private String frameTitle=MainWindowOperator.getDefault().getTitle();
     /**
      * chooser with all default values
      */
     EditControlChooser(){}
     //
    /**
     * 
     * @param comp 
     * @return 
     */
     public boolean checkComponent(java.awt.Component comp)
     {
          javax.swing.JRootPane cmp=(javax.swing.JRootPane)comp;
         JComponent ec=JComponentOperator.findJComponent(cmp,new checkETEditControlObject());
        return ec!=null && cmp.isShowing() && ((Frame)(comp.getParent())).getTitle().equals(frameTitle);
     }
    /**
     * 
     * @return 
     */
     public String	getDescription()
     {
         return "Try to find UML Edit Control Panel.";
     }
}
class EditControlChooserByText implements ComponentChooser
{
    private String text=null;
    private boolean cs=false;
    private boolean ce=false;
    private String frameTitle=MainWindowOperator.getDefault().getTitle();
   
    /**
     * find specific panel with param name
     * @param txt 
     */
     EditControlChooserByText(String txt)
     {
        text=txt;
     }
    /**
     * 
     * @param txt 
     * @param e 
     * @param s 
     */
     EditControlChooserByText(String txt, boolean e, boolean s)
     {
         ce=e;
         cs=s;
        text=txt;
     }
     //
    /**
     * 
     * @param comp 
     * @return 
     */
     public boolean checkComponent(java.awt.Component comp)
     {
        javax.swing.JRootPane cmp=(javax.swing.JRootPane)comp;
        JComponent ec=JComponentOperator.findJComponent(cmp,new checkETEditControlObject());
         return ec!=null  && ((Frame)(comp.getParent())).getTitle().equals(frameTitle) && JTextFieldOperator.findJTextField(cmp,text,ce,cs)!=null;
     }
    /**
     * 
     * @return 
     */
     public String	getDescription()
     {
         return "Try to find UML Edit Control panel.";
     }
}

//-------------------
class checkETEditControlObject implements ComponentChooser
{
    /**
     * 
     * @param comp 
     * @return 
     */
    public boolean checkComponent(java.awt.Component comp)
    {
        return comp.isShowing() && (comp instanceof EditControlImpl);
    }
    /**
     * 
     * @return 
     */
     public String	getDescription()
     {
         return "Try to find showing ET EditControl Object.";
     }
}
