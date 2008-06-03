/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.test.refactoring.operators;

import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.JViewport;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JSplitPaneOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class FindUsagesResultOperator extends TopComponentOperator{

    private JButton refresh;
    private JToggleButton collapse;
    private JToggleButton logical;
    private JToggleButton physical;
    private JButton prev;
    private JButton next;
    private JButton cancel;
    private JButton doRefactor;
    
    public FindUsagesResultOperator() {
        super(ResourceBundle.getBundle("org.netbeans.modules.refactoring.spi.impl.Bundle").getString("LBL_Usages"));
    }
    
    public FindUsagesResultOperator(String windowTitle) {
        super(windowTitle);
    }
    
    public static FindUsagesResultOperator getFindUsagesResult() {
        return new FindUsagesResultOperator(ResourceBundle.getBundle("org.netbeans.modules.refactoring.spi.impl.Bundle").getString("LBL_Usages"));
    }
    
    public static FindUsagesResultOperator getPreview() {
        return new FindUsagesResultOperator(ResourceBundle.getBundle("org.netbeans.modules.refactoring.spi.impl.Bundle").getString("LBL_Refactoring"));
    }
    
    
    public int getTabCount() {
        JTabbedPane tabbedPane = getTabbedPane();
        if(tabbedPane==null) return 0;
        return tabbedPane.getTabCount();
    }
    
    public void selectTab(String name) {
        JComponent content = getContent();        
        if("org.netbeans.modules.refactoring.spi.impl.RefactoringPanel".equals(content.getClass().getName())) {
            throw new  IllegalArgumentException("There are no tabs");
        } else if(content instanceof JTabbedPane) {
            JTabbedPaneOperator jtpo = new JTabbedPaneOperator((JTabbedPane)content);
            jtpo.selectPage(name);            
        } else {
            throw new  IllegalArgumentException("Wrong structure");
        }
    }
    
    public JTabbedPane getTabbedPane() {
        JComponent component = getContent();
        if(component instanceof JTabbedPane) return (JTabbedPane) component;
        else return null;        
    }
            
    public JPanel getRefactoringPanel() {
        JComponent content = getContent();
        
        if("org.netbeans.modules.refactoring.spi.impl.RefactoringPanel".equals(content.getClass().getName())) return (JPanel) content;
        if(content instanceof JTabbedPane) {
            JTabbedPane tab = (JTabbedPane) content;
            return (JPanel) tab.getSelectedComponent();
        }        
        throw new IllegalArgumentException("Wrong structure "+ content.getClass().getName());
        
    }
    
    private JComponent getContent() {
        Component source = this.getSource();        
        Component[] components = ((JComponent) source).getComponents();
        return (JComponent) components[0];
    }
    
    
    private void dumpChilds(Object root, int indentation,JTreeOperator jto) {
        Object[] children = jto.getChildren(root);
        if(children.length==0) return;
        for (int i = 0; i < children.length; i++) {
            Object child = children[i];
            for (int j = 0; j < indentation; j++) System.out.print(" ");
            System.out.println(child.toString());     
            dumpChilds(child, indentation+4, jto);                           
        }
    }
           
    public void test(Component source,int level,int no) { 
        if(level==0) System.out.println("--------------------------");
        for(int j = 0;j<level;j++) System.out.print("  ");
        System.out.print(no);
        System.out.println(source.getClass().getName());
        if(!(source instanceof JComponent)) return;                    
        Component[] components = ((JComponent) source).getComponents();        
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];            
            test(component, level+1,i);
            
        }                                
        if(level==0) System.out.println("--------------------------");
    }
    
    public JToolBar getJToolbar() {
        JPanel refactoringPanel = getRefactoringPanel();
        ContainerOperator ct = new ContainerOperator(refactoringPanel);
        JSplitPaneOperator splitPane = new JSplitPaneOperator(ct);
        JComponent leftComponent = (JComponent) splitPane.getLeftComponent();
        JToolBar toolbar = (JToolBar) leftComponent.getComponent(0);
        return toolbar;                        
    }
    
    public JTree getPreviewTree() {
        JPanel refactoringPanel = getRefactoringPanel();
        ContainerOperator ct = new ContainerOperator(refactoringPanel);
        JSplitPaneOperator splitPane = new JSplitPaneOperator(ct);
        JComponent leftComponent = (JComponent) splitPane.getLeftComponent();
        JScrollPane jScrollPane = (JScrollPane) leftComponent.getComponent(1);                
        JViewport viewport = jScrollPane.getViewport();        
        return (JTree) viewport.getComponent(0);        
    }
    
    public JButton getRefresh() {
        if(refresh==null) {
            refresh = (JButton) getJToolbar().getComponent(0);
        }
        return refresh;
    }
    
    public JToggleButton getCollapse() {
        if(collapse==null) {
            collapse = (JToggleButton) getJToolbar().getComponent(1);
        }
        return collapse;
    }

    public JToggleButton getLogical() {
        if(logical==null) {
            logical = (JToggleButton) getJToolbar().getComponent(2);
        }
        return logical;
    }

    public JToggleButton getPhysical() {
        if(physical==null) {
            physical = (JToggleButton) getJToolbar().getComponent(3);
        }
        return physical;
    }

    public JButton getPrev() {
        if(prev==null) {
            prev = (JButton) getJToolbar().getComponent(4);
        }
        return prev;
    }

    public JButton getNext() {
        if(next==null) {
            next = (JButton) getJToolbar().getComponent(5);
        }
        return next;
    }
    

}
