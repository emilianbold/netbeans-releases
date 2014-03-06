/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.test.beans.operators;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import static junit.framework.Assert.fail;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.modules.beans.PatternNode;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;


/**
 *
 * @author ssazonov
 */
public class Navigator extends TopComponentOperator {

    private JTreeOperator tree;

    public Navigator() {
        super("Navigator");        
    }
    
    public JTreeOperator getTreeOperator() {
        if(tree==null) {
            tree = new JTreeOperator(this);
        }
        return tree;
    }

    public void setScopeToMember() {
        JComboBoxOperator combo = new JComboBoxOperator(this, 0);
        combo.selectItem("Members");
        tree = null; 
    }

    public void setScopeToBeanPatterns() {
        JComboBoxOperator combo = new JComboBoxOperator(this, 0);
        combo.selectItem("Bean Patterns");
        tree = null;
    }
        
    public String getSelectedPath() {
        return Arrays.toString(getTreeOperator().getSelectionPaths());
    }

    public boolean clickTheNode(String node) {
        TreeNode[] nodePath = getPath(node);
        TreePath treePath   = new TreePath(nodePath);

        try {
            getTreeOperator().clickOnPath(treePath, 2);
            return true;
        } catch (Exception e) {
        }
        return false;
    }
    
    public void waitForString(final String expected) {
        Waitable waitable = new Waitable() {

            @Override
            public Object actionProduced(Object obj) {
                Navigator n = (Navigator) obj;
                String tree = n.getTree();
                if(expected.equals(tree)) {
                    return Boolean.TRUE;
                } else {
                    return null;
                }
            }

            @Override
            public String getDescription() {
                return "waiting for navigator tree";
            }
        };
        Waiter w = new Waiter(waitable);
        try {
            w.getTimeouts().setTimeout("Waiter.WaitingTime", 10000);            
            w.waitAction(this);
        } catch(InterruptedException ie) {
            
        } catch (TimeoutExpiredException tee) {
            fail("Expected tree "+expected+" but found "+this.getTree());
            
        }
    }

    //----------------------------------------------------------
    
    private String getTree() {
        return (getTree((TreeNode) getTreeOperator().getRoot(), "", new StringBuilder())).toString();
    }

    private StringBuilder getTree(TreeNode root, String deep, StringBuilder accumulator) {                        
        String text = root.toString();
        Node findNode = Visualizer.findNode(root);
        if(findNode instanceof PatternNode) {
            PatternNode patternNode = (PatternNode) findNode;
            if(patternNode.getShortDescription()!=null) {
                text = patternNode.getShortDescription();
            } else if(patternNode.getHtmlDisplayName()!=null) {
                text = patternNode.getHtmlDisplayName();
            }
        }
        accumulator.append(deep).append(text).append('\n');
        for (int i = 0; i < root.getChildCount(); i++) {
            getTree(root.getChildAt(i), deep + "__", accumulator);
        }
        return accumulator;
    }

    private TreeNode[] getPath(String node) {        
        Stack<TreeNode> lifo = new Stack<TreeNode>();
        lifo.push((TreeNode) getTreeOperator().getRoot());
        while(lifo.isEmpty()) {
            TreeNode actNode = lifo.pop();
            if(node.equals(actNode.toString())) {
                List<TreeNode> path = new LinkedList<TreeNode>();
                path.add(actNode);
                actNode = actNode.getParent();
                while(actNode!=null) {
                    path.add(0,actNode);
                    actNode = actNode.getParent();
                }
                TreeNode[] res = path.toArray(new TreeNode[path.size()]);                
                return res;
            }
            while(actNode.children().hasMoreElements()) {
                lifo.add((TreeNode)actNode.children().nextElement());
            }            
        }
        return null;
    }
        
    public void printAllComponents() {
        System.out.println("**************************");
        printComp((Container) this.getSource(), "");
        System.out.println("**************************");
    }

    public void printComp(Container c, String s) {
        System.out.println(s + c.getClass().getName());
        if (c instanceof Container) {
            for (Component com : c.getComponents()) {
                printComp((Container) com, s + "__");
            }
        }
    }

    /*
     javax.swing.JFrame
     __javax.swing.JRootPane
     ____org.netbeans.core.windows.view.ui.MainWindow$8
     ____javax.swing.JLayeredPane
     ______org.netbeans.modules.quicksearch.QuickSearchPopup
     ________javax.swing.JScrollPane
     __________javax.swing.JViewport
     ____________javax.swing.JList
     ______________javax.swing.CellRendererPane
     __________javax.swing.JScrollPane$ScrollBar
     ____________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ____________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     __________javax.swing.JScrollPane$ScrollBar
     ____________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ____________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ________javax.swing.JPanel
     __________javax.swing.JSeparator
     __________javax.swing.JLabel
     __________javax.swing.JLabel
     __________javax.swing.JSeparator
     __________javax.swing.JLabel
     ______javax.swing.JPanel
     ________javax.swing.JLabel
     ______org.netbeans.core.windows.view.ui.MainWindow$1
     ________org.openide.awt.ToolbarPool
     __________org.netbeans.core.windows.view.ui.toolbars.ToolbarConfiguration$1
     ____________org.netbeans.core.windows.view.ui.toolbars.ToolbarRow
     ______________javax.swing.JLabel
     ______________org.netbeans.core.windows.view.ui.toolbars.ToolbarContainer
     ________________org.openide.awt.Toolbar
     __________________org.netbeans.core.windows.view.ui.toolbars.ToolbarContainer$ToolbarXP
     __________________org.openide.awt.Toolbar$DefaultIconButton
     __________________org.openide.awt.Toolbar$DefaultIconButton
     __________________org.openide.awt.Toolbar$DefaultIconButton
     __________________org.openide.awt.Toolbar$DefaultIconButton
     ______________org.netbeans.core.windows.view.ui.toolbars.ToolbarContainer
     ________________org.openide.awt.Toolbar
     __________________org.netbeans.core.windows.view.ui.toolbars.ToolbarContainer$ToolbarXP
     __________________org.openide.awt.Toolbar$DefaultIconButton
     __________________org.openide.awt.Toolbar$DefaultIconButton
     ______________org.netbeans.core.windows.view.ui.toolbars.ToolbarContainer
     ________________org.openide.awt.Toolbar
     __________________org.netbeans.core.windows.view.ui.toolbars.ToolbarContainer$ToolbarXP
     __________________javax.swing.JPanel
     ____________________org.netbeans.modules.project.ui.actions.ActiveConfigAction$1
     ______________________com.sun.java.swing.plaf.windows.WindowsComboBoxUI$XPComboBoxButton
     ______________________javax.swing.CellRendererPane
     ________________________org.netbeans.modules.project.ui.actions.ActiveConfigAction$ConfigCellRenderer
     __________________javax.swing.JButton
     __________________org.openide.awt.Toolbar$DefaultIconButton
     __________________org.openide.awt.Toolbar$DefaultIconButton
     __________________org.openide.awt.DropDownButton
     __________________org.openide.awt.DropDownButton
     __________________org.openide.awt.DropDownButton
     ______________org.netbeans.core.windows.view.ui.toolbars.ToolbarContainer
     ________________org.openide.awt.Toolbar
     __________________org.netbeans.core.windows.view.ui.toolbars.ToolbarContainer$ToolbarXP
     __________________org.openide.actions.GarbageCollectAction$HeapViewWrapper
     ____________________org.openide.actions.HeapView
     __________________org.openide.awt.Toolbar$DefaultIconButton
     __________________org.openide.awt.Toolbar$DefaultIconButton
     ________javax.swing.JPanel
     __________javax.swing.JLayeredPane
     ____________org.netbeans.core.windows.view.ui.DesktopImpl$1
     ______________org.netbeans.core.windows.view.ui.slides.SlideBarContainer$VisualPanel
     ________________org.netbeans.core.windows.view.ui.slides.SlideBar
     __________________javax.swing.JLabel
     ______________org.netbeans.core.windows.view.ui.slides.SlideBarContainer$VisualPanel
     ________________org.netbeans.core.windows.view.ui.slides.SlideBar
     __________________javax.swing.JLabel
     ______________org.netbeans.core.windows.view.ui.slides.SlideBarContainer$VisualPanel
     ________________org.netbeans.core.windows.view.ui.slides.SlideBar
     __________________javax.swing.JLabel
     ______________org.netbeans.core.windows.view.ui.slides.SlideBarContainer$VisualPanel
     ________________org.netbeans.core.windows.view.ui.slides.SlideBar
     __________________javax.swing.JLabel
     ________________javax.swing.JPanel
     __________________javax.swing.JPanel
     ____________________org.netbeans.modules.progress.ui.StatusLineComponent
     ____________________org.netbeans.core.ui.notifications.FlashingIcon
     ____________________org.netbeans.modules.notifications.FlashingIcon
     ____________________javax.swing.JPanel
     ______________________org.netbeans.modules.editor.impl.StatusLineFactories$3
     ______________________org.netbeans.modules.editor.impl.StatusLineComponent
     ____________________javax.swing.JPanel
     ______________________org.netbeans.modules.editor.impl.StatusLineFactories$3
     ______________________org.netbeans.modules.editor.impl.StatusLineComponent
     ______________org.netbeans.core.windows.view.ui.MultiSplitPane
     ________________org.netbeans.core.windows.view.EditorView$EditorAreaComponent
     __________________org.netbeans.core.windows.view.ui.DefaultSplitContainer$ModePanel
     ____________________org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter
     ______________________javax.swing.JPanel
     ________________________org.netbeans.core.multiview.MultiViewCloneableTopComponent
     __________________________org.netbeans.core.multiview.TabsComponent
     ____________________________javax.swing.JToolBar
     ______________________________javax.swing.JToggleButton
     ______________________________javax.swing.JToggleButton
     ______________________________javax.swing.JPanel
     ________________________________org.netbeans.modules.editor.NbEditorToolBar
     __________________________________javax.swing.JToolBar$Separator
     __________________________________javax.swing.JButton
     __________________________________org.openide.awt.DropDownButton
     __________________________________org.openide.awt.DropDownButton
     __________________________________javax.swing.JToolBar$Separator
     __________________________________javax.swing.JButton
     __________________________________javax.swing.JButton
     __________________________________javax.swing.JButton
     __________________________________javax.swing.JButton
     __________________________________javax.swing.JToggleButton
     __________________________________javax.swing.JToolBar$Separator
     __________________________________javax.swing.JToolBar$1
     __________________________________javax.swing.JToolBar$1
     __________________________________javax.swing.JButton
     __________________________________javax.swing.JToolBar$Separator
     __________________________________javax.swing.JButton
     __________________________________javax.swing.JButton
     __________________________________javax.swing.JToolBar$Separator
     __________________________________javax.swing.JToolBar$1
     __________________________________javax.swing.JToolBar$1
     __________________________________javax.swing.JToolBar$Separator
     __________________________________javax.swing.JToolBar$1
     __________________________________javax.swing.JToolBar$1
     ______________________________javax.swing.JLabel
     ____________________________javax.swing.JLayer
     ______________________________javax.swing.JLayer$DefaultLayerGlassPane
     ______________________________javax.swing.JPanel
     ________________________________org.netbeans.core.spi.multiview.text.MultiViewCloneableEditor
     __________________________________javax.swing.JPanel
     ____________________________________javax.swing.JScrollPane
     ______________________________________javax.swing.JViewport
     ________________________________________org.openide.text.QuietEditorPane
     ______________________________________javax.swing.JScrollPane$ScrollBar
     ________________________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ________________________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ______________________________________javax.swing.JScrollPane$ScrollBar
     ________________________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ________________________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ______________________________________javax.swing.JViewport
     ______________________________________javax.swing.JViewport
     ________________________________________javax.swing.JPanel
     __________________________________________org.netbeans.editor.GlyphGutter
     __________________________________________org.netbeans.modules.editor.bracesmatching.BraceMatchingSidebarComponent
     __________________________________________org.netbeans.modules.editor.fold.ui.CodeFoldingSideBar
     __________________________________________org.netbeans.modules.maven.grammar.effpom.AnnotationBar
     __________________________________________org.netbeans.modules.mercurial.ui.annotate.AnnotationBar
     __________________________________________org.netbeans.modules.versioning.annotate.AnnotationBar
     __________________________________________org.netbeans.modules.git.ui.blame.AnnotationBar
     __________________________________________org.netbeans.modules.subversion.ui.blame.AnnotationBar
     __________________________________________org.netbeans.modules.versioning.ui.diff.DiffSidebar
     ______________________________________javax.swing.JPanel
     ____________________________________javax.swing.JPanel
     ______________________________________org.netbeans.modules.editor.errorstripe.AnnotationView
     ____________________________________javax.swing.JPanel
     ______________________________________org.netbeans.modules.gsf.codecoverage.CoverageSideBar
     ______________________________________org.netbeans.modules.editor.search.SearchNbEditorKit$SearchJPanel
     ______________________________________org.netbeans.modules.editor.breadcrumbs.SideBarFactoryImpl$SideBar
     ________________________________________org.netbeans.modules.editor.breadcrumbs.BreadCrumbComponent
     ________________________________________javax.swing.JButton
     ______________________________________javax.swing.JPanel
     ____________________________________javax.swing.JPanel
     ______________________org.netbeans.swing.tabcontrol.TabDisplayer
     ________________________javax.swing.JPanel
     __________________________org.netbeans.swing.tabcontrol.plaf.TabControlButtonFactory$TimerButton
     __________________________org.netbeans.swing.tabcontrol.plaf.TabControlButtonFactory$TimerButton
     __________________________org.netbeans.swing.tabcontrol.plaf.TabControlButtonFactory$DropDownButton
     __________________________org.netbeans.swing.tabcontrol.plaf.TabControlButtonFactory$MaximizeRestoreButton
     ________________org.netbeans.core.windows.view.ui.MultiSplitPane
     __________________org.netbeans.core.windows.view.ui.DefaultSplitContainer$ModePanel
     ____________________org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter
     ______________________javax.swing.JPanel
     ________________________org.netbeans.modules.project.ui.ProjectTab
     __________________________org.netbeans.modules.project.ui.ProjectTab$ProjectTreeView
     ____________________________javax.swing.JViewport
     ______________________________org.openide.explorer.view.TreeView$ExplorerTree
     ________________________________javax.swing.CellRendererPane
     ____________________________javax.swing.JScrollPane$ScrollBar
     ______________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ______________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ____________________________javax.swing.JScrollPane$ScrollBar
     ______________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ______________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     __________________________org.netbeans.modules.project.ui.NodeSelectionProjectPanel
     ____________________________javax.swing.JButton
     ____________________________javax.swing.JLabel
     ________________________org.netbeans.modules.project.ui.ProjectTab
     __________________________org.netbeans.modules.project.ui.NodeSelectionProjectPanel
     ____________________________javax.swing.JButton
     ____________________________javax.swing.JLabel
     __________________________org.netbeans.modules.project.ui.ProjectTab$ProjectTreeView
     ____________________________javax.swing.JViewport
     ______________________________org.openide.explorer.view.TreeView$ExplorerTree
     ________________________________javax.swing.CellRendererPane
     __________________________________com.sun.java.swing.plaf.windows.WindowsTreeUI$WindowsTreeCellRenderer
     __________________________________org.openide.awt.HtmlRendererImpl
     ____________________________javax.swing.JScrollPane$ScrollBar
     ______________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ______________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ____________________________javax.swing.JScrollPane$ScrollBar
     ______________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ______________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ________________________org.netbeans.core.ide.ServicesTab
     __________________________org.openide.explorer.view.BeanTreeView
     ____________________________javax.swing.JViewport
     ______________________________org.openide.explorer.view.TreeView$ExplorerTree
     ________________________________javax.swing.CellRendererPane
     __________________________________com.sun.java.swing.plaf.windows.WindowsTreeUI$WindowsTreeCellRenderer
     __________________________________org.openide.awt.HtmlRendererImpl
     ____________________________javax.swing.JScrollPane$ScrollBar
     ______________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ______________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ____________________________javax.swing.JScrollPane$ScrollBar
     ______________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ______________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ______________________org.netbeans.swing.tabcontrol.TabDisplayer
     ________________________javax.swing.JPanel
     __________________________org.netbeans.swing.tabcontrol.plaf.TabControlButtonFactory$CloseButton
     ________________________org.netbeans.swing.tabcontrol.plaf.TabControlButtonFactory$SlideGroupButton
     __________________org.netbeans.core.windows.view.ui.DefaultSplitContainer$ModePanel
     ____________________org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter
     ______________________javax.swing.JPanel
     ________________________org.netbeans.modules.navigator.NavigatorTC
     __________________________javax.swing.JPanel
     ____________________________javax.swing.JComboBox
     ______________________________com.sun.java.swing.plaf.windows.WindowsComboBoxUI$XPComboBoxButton
     ______________________________javax.swing.CellRendererPane
     ________________________________com.sun.java.swing.plaf.windows.WindowsComboBoxUI$WindowsComboBoxRenderer
     ____________________________javax.swing.JPanel
     ______________________________org.netbeans.modules.java.navigation.ClassMemberPanelUI$Toolbar
     ________________________________org.netbeans.modules.java.navigation.NoBorderToolBar
     __________________________________javax.swing.JComboBox
     ____________________________________com.sun.java.swing.plaf.windows.WindowsComboBoxUI$XPComboBoxButton
     ____________________________________javax.swing.CellRendererPane
     ______________________________________org.netbeans.modules.java.navigation.base.HistorySupport$HistoryRenderer
     __________________________________javax.swing.JButton
     __________________________javax.swing.JPanel
     ____________________________org.netbeans.modules.java.navigation.ClassMemberPanelUI
     ______________________________org.netbeans.modules.java.navigation.ClassMemberPanelUI$MyBeanTreeView
     ________________________________javax.swing.JViewport
     __________________________________org.openide.explorer.view.TreeView$ExplorerTree
     ____________________________________javax.swing.CellRendererPane
     ________________________________javax.swing.JScrollPane$ScrollBar
     __________________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     __________________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ________________________________javax.swing.JScrollPane$ScrollBar
     __________________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     __________________________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ______________________________org.netbeans.modules.java.navigation.base.TapPanel
     ________________________________org.netbeans.modules.java.navigation.base.FiltersManager$FiltersComponent
     __________________________________org.netbeans.modules.java.navigation.NoBorderToolBar
     ____________________________________javax.swing.JToggleButton
     ____________________________________org.netbeans.modules.java.navigation.base.FiltersManager$Space
     ____________________________________javax.swing.JToggleButton
     ____________________________________org.netbeans.modules.java.navigation.base.FiltersManager$Space
     ____________________________________javax.swing.JToggleButton
     ____________________________________org.netbeans.modules.java.navigation.base.FiltersManager$Space
     ____________________________________javax.swing.JToggleButton
     ____________________________________org.netbeans.modules.java.navigation.base.FiltersManager$Space
     ____________________________________javax.swing.JToggleButton
     ____________________________________javax.swing.JToolBar$Separator
     ____________________________________javax.swing.JToggleButton
     ____________________________________javax.swing.JToolBar$Separator
     ____________________________________javax.swing.JToggleButton
     ____________________________________javax.swing.JToolBar$Separator
     ____________________________________javax.swing.JToggleButton
     ______________________org.netbeans.swing.tabcontrol.TabDisplayer
     ________________________javax.swing.JPanel
     __________________________org.netbeans.swing.tabcontrol.plaf.TabControlButtonFactory$CloseButton
     ________________________org.netbeans.swing.tabcontrol.plaf.TabControlButtonFactory$SlideGroupButton
     ______org.openide.awt.MenuBar
     ________org.openide.awt.MenuBar$LazyMenu
     ________org.openide.awt.MenuBar$LazyMenu
     ________org.openide.awt.MenuBar$LazyMenu
     ________org.openide.awt.MenuBar$LazyMenu
     ________org.openide.awt.MenuBar$LazyMenu
     ________org.openide.awt.MenuBar$LazyMenu
     ________org.openide.awt.MenuBar$LazyMenu
     ________org.openide.awt.MenuBar$LazyMenu
     ________org.openide.awt.MenuBar$LazyMenu
     ________org.openide.awt.MenuBar$LazyMenu
     ________org.openide.awt.MenuBar$LazyMenu
     ________org.openide.awt.MenuBar$LazyMenu
     ________org.openide.awt.MenuBar$LazyMenu
     ________javax.swing.Box$Filler
     ________org.netbeans.modules.quicksearch.QuickSearchComboBar
     __________javax.swing.JPanel
     ____________javax.swing.JLabel
     ____________javax.swing.JScrollPane
     ______________javax.swing.JViewport
     ________________org.netbeans.modules.quicksearch.QuickSearchComboBar$DynamicWidthTA
     ______________javax.swing.JScrollPane$ScrollBar
     ________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ______________javax.swing.JScrollPane$ScrollBar
     ________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     ________________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
     */
    
}
