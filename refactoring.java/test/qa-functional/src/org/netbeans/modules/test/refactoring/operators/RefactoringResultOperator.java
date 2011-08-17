/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.tree.TreeModel;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JSplitPaneOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.modules.refactoring.java.ui.tree.FileTreeElement;
import org.netbeans.modules.refactoring.spi.impl.CheckNode;
import org.netbeans.modules.refactoring.spi.impl.RefactoringPanel;
import org.netbeans.modules.refactoring.spi.impl.RefactoringPanelContainer;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class RefactoringResultOperator extends TopComponentOperator{

    private JButton refresh;
    private JToggleButton collapse;
    private JToggleButton logical;
    private JToggleButton physical;
    private JButton prev;
    private JButton next;
    private JButton cancel;
    private JButton doRefactor;
    
    private RefactoringResultOperator() {
        super(ResourceBundle.getBundle("org.netbeans.modules.refactoring.spi.impl.Bundle").getString("LBL_Usages"));
    }
    
    private RefactoringResultOperator(String windowTitle) {
        super(windowTitle);
    }
    
    public static RefactoringResultOperator getFindUsagesResult() {
        return new RefactoringResultOperator(ResourceBundle.getBundle("org.netbeans.modules.refactoring.spi.impl.Bundle").getString("LBL_Usages"));
    }
    
    public static RefactoringResultOperator getPreview() {
        return new RefactoringResultOperator(ResourceBundle.getBundle("org.netbeans.modules.refactoring.spi.impl.Bundle").getString("LBL_Refactoring"));
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
        if("org.netbeans.modules.refactoring.spi.impl.RefactoringPanelContainer".equals(content.getClass().getName())) return ((RefactoringPanelContainer) content).getCurrentPanel();
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
        System.out.print(source.getClass().getName());
        if(source.getClass().getName().endsWith("JButton")) System.out.print(((JButton)source).getText());
        System.out.println("");
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

    private JPanel getDoRefactoringCancelPanel() {
        JPanel refactoringPanel = getRefactoringPanel();
        ContainerOperator ct = new ContainerOperator(refactoringPanel);
        JSplitPaneOperator splitPane = new JSplitPaneOperator(ct);
        JComponent leftComponent = (JComponent) splitPane.getLeftComponent();
        JPanel panel = (JPanel) leftComponent.getComponent(0);
        return panel;
    }

    private JButton getDoRefactoringButton() {
        JPanel panel = getDoRefactoringCancelPanel();
        return (JButton) panel.getComponent(0);
    }

    private JButton getCancelButton() {
        JPanel panel = getDoRefactoringCancelPanel();
        return (JButton) panel.getComponent(1);
    }
    
    public JTree getPreviewTree() {
        JPanel refactoringPanel = getRefactoringPanel();
        ContainerOperator ct = new ContainerOperator(refactoringPanel);
        JSplitPaneOperator splitPane = new JSplitPaneOperator(ct);
        JComponent leftComponent = (JComponent) splitPane.getLeftComponent();
        JScrollPane jScrollPane = null;
        for (Component component : leftComponent.getComponents()) {
            if (component instanceof JScrollPane) {
                jScrollPane = (JScrollPane) component;
            }
        }
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

    public JButton getDoRefactor() {
        if(doRefactor==null) {
            doRefactor = getDoRefactoringButton();
        }
        return doRefactor;
    }

    public JButton getcancel() {
        if(cancel==null) {
            cancel = getCancelButton();
        }
        return cancel;
    }

    public Set<FileObject> getInvolvedFiles() {
        JTree tree = this.getPreviewTree();
        return browseForFileObjects(tree.getModel());
    }

    private Set<FileObject> browseForFileObjects(TreeModel model) {
        Queue<CheckNode> q = new LinkedList<CheckNode>();
        q.add((CheckNode)model.getRoot());
        Set<FileObject> result = new HashSet<FileObject>();        
        while(!q.isEmpty()) {
            CheckNode node = q.remove();
            Object uo = node.getUserObject();
            if(uo instanceof FileTreeElement) {
                FileTreeElement fileTreeElement = (FileTreeElement) uo;
                Object userObject = fileTreeElement.getUserObject();
                if(userObject instanceof FileObject) {
                    result.add((FileObject)userObject);
                } else {
                    throw new IllegalArgumentException("Object of type FileObject was expected, but got "+userObject.getClass().getName());
                }
                                
            }
            for (int i = 0; i < model.getChildCount(node); i++) {
                q.add((CheckNode)model.getChild(node, i));
            }
        }
        return result;
    }
    

}
