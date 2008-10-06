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

package org.netbeans.modules.visualweb.gravy.properties;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComponent;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/**
 * Handles org.openide.explorer.propertysheet.PropertySheetToolbar which
 * represents toolbar in the properties sheet. It includes three sort option
 * toggle buttons, show editable toggle button, customizer button and help
 * button.
 * <p>
 * Usage:<br>
 * <pre>
 *      PropertySheetOperator pso = new PropertySheetOperator("Properties of MyClass");
 *      PropertySheetToolbarOperator psto = new PropertySheetToolbarOperator(pso);
 *      psto.unsorted();
 *      psto.sortByType();
 *      System.out.println("Help enabled="+psto.btHelp().isEnabled());
 * </pre>
 *
 * @deprecated Tool bar is no more present in property sheet.
 */
public class PropertySheetToolbarOperator extends JComponentOperator {
    // in IDE PropertySheetToolbar extends JPanel
    
    /** Component operators */
    private JToggleButtonOperator _btUnsorted;
    private JToggleButtonOperator _btSortByName;
    private JToggleButtonOperator _btSortByType;
    private JToggleButtonOperator _btShowEditable;
    private JButtonOperator _btCustomizer;
    private JButtonOperator _btHelp;
    
    
    /** Waits for PropertySheetToolbar in given container.
     * @param contOper where to find
     * @deprecated Tool bar is no more present in property sheet.
     */
    public PropertySheetToolbarOperator(ContainerOperator contOper) {
        super((JComponent)waitPropertySheetToolbar(contOper));
    }

    /** Gets instance of JToggleButtonOperator of "Unsorted" button.
     * @return JToggleButtonOperator instance
     * @deprecated Tool bar is no more present in property sheet.
     */
    public JToggleButtonOperator btUnsorted() {
        if(_btUnsorted == null) {
            _btUnsorted = new JToggleButtonOperator(this, 0);
        }
        return _btUnsorted;
    }
    
    /** Gets instance of JToggleButtonOperator of "Sort by Name" button.
     * @return JToggleButtonOperator instance
     * @deprecated Tool bar is no more present in property sheet.
     */
    public JToggleButtonOperator btSortByName() {
        if(_btSortByName == null) {
            _btSortByName = new JToggleButtonOperator(this, 1);
        }
        return _btSortByName;
    }
    
    /** Gets instance of JToggleButtonOperator of "Sort by Type" button.
     * @return JToggleButtonOperator instance
     * @deprecated Tool bar is no more present in property sheet.
     */
    public JToggleButtonOperator btSortByType() {
        if(_btSortByType == null) {
            _btSortByType = new JToggleButtonOperator(this, 2);
        }
        return _btSortByType;
    }
    
    /** Gets instance of JToggleButtonOperator of "Show Editable Properties
     * Only" button.
     * @return JToggleButtonOperator instance
     * @deprecated Tool bar is no more present in property sheet.
     */
    public JToggleButtonOperator btShowEditable() {
        if(_btShowEditable == null) {
            _btShowEditable = new JToggleButtonOperator(this, 3);
        }
        return _btShowEditable;
    }
    
    /** Gets instance of JButtonOperator of "Customizer" button.
     * @return JButtonOperator instance
     * @deprecated Tool bar is no more present in property sheet.
     */
    public JButtonOperator btCustomizer() {
        if(_btCustomizer == null) {
            _btCustomizer = new JButtonOperator(this, 0);
        }
        return _btCustomizer;
    }
    
    /** Gets instance of JButtonOperator of "Help" button.
     * @return JButtonOperator instance
     * @deprecated Tool bar is no more present in property sheet.
     */
    public JButtonOperator btHelp() {
        if(_btHelp == null) {
            _btHelp = new JButtonOperator(this, 1);
        }
        return _btHelp;
    }
    
    /** Pushes "Unsorted" button. 
     * @deprecated Tool bar is no more present in property sheet.
     */
    public void unsorted() {
        btUnsorted().push();
    }
    
    /** Pushes "Sort by Name" button. 
     * @deprecated Tool bar is no more present in property sheet.
     */
    public void sortByName() {
        btSortByName().push();
    }
    
    /** Pushes "Sort by Type" button. 
     * @deprecated Tool bar is no more present in property sheet.
     */
    public void sortByType() {
        btSortByType().push();
    }
    
    /** Sets "Show Editable Properties Only" toggle button to desired state.
     * @param show true - select button (show only editable properties);
     *             false - unselect button (show all properties)
     * @deprecated Tool bar is no more present in property sheet.
     */
    public void showEditable(boolean show) {
        btShowEditable().setSelected(show);
    }
    
    /** Pushes "Customizer" button and no block further execution. 
     * @deprecated Tool bar is no more present in property sheet.
     */
    public void customizer() {
        btCustomizer().pushNoBlock();
    }
    
    /** Pushes "Help" button. 
     * @deprecated Tool bar is no more present in property sheet.
     */
    public void help() {
        btHelp().push();
    }
    
    /** Waits for instance of PropertySheetToolbar in a container. */
    private static Component waitPropertySheetToolbar(ContainerOperator contOper) {
        throw new JemmyException("Don't use this! No tool bar is present in property sheet.");
        /*
        ComponentChooser chooser = new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return comp.getClass().getName().indexOf("PropertySheetToolbar") != -1;
            }
            
            public String getDescription() {
                return "org.openide.explorer.propertysheet.PropertySheetToolbar";
            }
        };
        return contOper.waitComponent((Container)contOper.getSource(), chooser);
         */
    }
    
    /** Performs verification by accessing all sub-components 
     * @deprecated Tool bar is no more present in property sheet.
     */    
    public void verify() {
        btCustomizer();
        btHelp();
        btShowEditable();
        btSortByName();
        btSortByType();
        btUnsorted();
    }
}
