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
import javax.swing.JTabbedPane;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;

/**
 * Handles org.openide.explorer.propertysheet.PropertySheetTab which
 * represents a single tab of properties in a IDE property sheet.
 * PropertySheetTab extends JPanel in IDE.
 * <p>
 * Usage:<br>
 * <pre>
 *      PropertySheetOperator pso = new PropertySheetOperator("Properties of MyClass");
 *      PropertySheetTabOperator psto = new PropertySheetTabOperator(pso);
 *      Property pr = new Property(psto, "Template");
 *      System.out.println("\nProperty name="+pr.getName());
 *      System.out.println("\nProperty value="+pr.getValue());
 *      PropertySheetTabOperator psto = new PropertySheetTabOperator(pso, "Other Properties");
 * </pre>
 *
 * @deprecated Use {@link PropertySheetOperator} instead
 */
public class PropertySheetTabOperator extends ContainerOperator {//JComponentOperator {
    
    /** Waits for PropertySheetTab with given name in specified container. If tab
     * exist and it is not active, it selects it.
     * @param contOper where to find
     * @param tabName name of tab
     * @deprecated Use {@link PropertySheetOperator} instead
     */
    public PropertySheetTabOperator(ContainerOperator contOper, String tabName) {
        super(resolveParent(contOper));
        //super(contOper.getParent());
        //super((Container)contOper.getSource());
        /*super(waitPropertySheetTab(contOper, tabName));
        copyEnvironment(contOper);*/
    }
    
    /** Waits for PropertySheetTab with name "Properties" in specified container. 
     * If tab exist and it is not active, it selects it.
     * @param contOper where to find
     * @deprecated Use {@link PropertySheetOperator} instead
     */
    public PropertySheetTabOperator(ContainerOperator contOper) {
        super(resolveParent(contOper));
        //super(contOper.getParent());
        //super((Container)contOper.getSource());
        /*super(waitPropertySheetTab(contOper, 
                    Bundle.getString("org.openide.nodes.Bundle", "Properties")));
        copyEnvironment(contOper);*/
    }
    
    /** In case contOper is operator of PropertySheet instance we need to 
     * return parent because in Property we search for PropertySheet in that
     * parent.
     */
    private static Container resolveParent(ContainerOperator contOper) {
        if(contOper.getParent() == null) {
            return (Container)contOper.getSource();
        } else {
            return contOper.getParent();
        }
    }

    /** Finds org.openide.explorer.propertysheet.PropertySheetTab in given
     * container. On its parent (JTabbedPane) find a pane with given tabName
     * and select that tab.
     * @return JComponent representing PropertySheetTab, null if not found.
     */
    private static JComponent findPropertySheetTab(ContainerOperator contOper, String tabName) {
        ComponentChooser chooser = new PropertySheetTabChooser();
        ComponentSearcher searcher = new ComponentSearcher((Container)contOper.getSource());
        searcher.setOutput(TestOut.getNullOutput());
        Component comp = searcher.findComponent(chooser);
        if(comp == null) {
            return null;
        }
        JTabbedPaneOperator tabbed = new JTabbedPaneOperator((JTabbedPane)comp.getParent());
        int count = tabbed.getTabCount();
        for(int i=0; i < count; i++) {
            if(contOper.getComparator().equals(tabbed.getTitleAt(i), tabName)) {
                tabbed.selectPage(i);
                return (JComponent)tabbed.getSelectedComponent();
            }
        }
        return null;
    }
    
    /** Waits for PropertySheetTab.
     * @see #findPropertySheetTab()
     */
    private static JComponent waitPropertySheetTab(final ContainerOperator contOper, 
                                                   final String tabName) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                    return findPropertySheetTab(contOper, tabName);
                }
                public String getDescription() {
                    return("Wait PropertySheetTab \""+tabName+"\".");
                }
            });
            waiter.setOutput(JemmyProperties.getCurrentOutput());
            return((JComponent)waiter.waitAction(null));
        } catch(InterruptedException e) {
            throw new JemmyException("Interrupted waiting for PropertySheetTab");
        }
    }
    
    /** Chooser to find any instance of
     * org.openide.explorer.propertysheet.PropertySheetTab.
     */
    private static class PropertySheetTabChooser implements ComponentChooser {
        public PropertySheetTabChooser() {
        }
        
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().equals("org.openide.explorer.propertysheet.SheetTable");
        }
        
        public String getDescription() {
            return "SheetTable";
        }
    }
    
    /** Count number of properties on the tab.
     * @return number or properties
     * @deprecated Use {@link PropertySheetOperator} instead
     */
    public int getCount() {
        throw new JemmyException("Don't use this! No JTabbedPane is used in property sheet.");
        /*// Trying to find non sense name it goes through all properties and
        // returned index represents the count.
        SheetButtonOperator.SheetButtonChooser chooser = 
                new SheetButtonOperator.SheetButtonChooser("Nonsense name @#$%^&", getComparator());
        findComponent((Container)getSource(), chooser);
        return chooser.getIndex()+1;*/
    }
}
