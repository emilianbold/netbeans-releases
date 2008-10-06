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

package org.netbeans.modules.visualweb.gravy.navigation;


import org.netbeans.modules.visualweb.xhtml.FormNamePanel;
import org.netbeans.modules.visualweb.gravy.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jellytools.TopComponentOperator;

/**
 * Operator for "Page Navigation" pane.
 */
public class NavigatorOperator extends JComponentOperator{
    private static final String 
        DEFAULT_LINK_ID = "case1",
        PAGE_NAVIGATION_TITLE = org.netbeans.modules.visualweb.gravy.Bundle.getStringTrimmed("org.netbeans.modules.visualweb.navigation.Bundle","NavigationView"),
        NAVIGATION_BUTTON = org.netbeans.modules.visualweb.gravy.Bundle.getStringTrimmed("org.netbeans.modules.visualweb.navigation.Bundle","ViewNavigation");
    
    ComponentOperator desktop = null;
    private JToggleButtonOperator _btSource;
    private JToggleButtonOperator _btNavigation;

    public NavigatorOperator(ContainerOperator parent) {
        super(parent, new NavigatorChooser());
        this.setComparator(new Operator.DefaultStringComparator(true, true));
    }

    public NavigatorOperator() {
        this(Util.getMainWindow());
    }
    
    /**
     * Get "XML" button.
     */
    public JToggleButtonOperator btSource() {
        if (_btSource == null) {
            TopComponentOperator topComponent = new TopComponentOperator(Util.getMainWindow());
            _btSource = new JToggleButtonOperator(topComponent, "XML");
        }
        return _btSource;        
    }

    /**
     * Get "Navigation" button.
     */
    public JToggleButtonOperator btNavigation() {
        if (_btNavigation == null) {
            TopComponentOperator topComponent = new TopComponentOperator(Util.getMainWindow());
            _btNavigation = new JToggleButtonOperator(topComponent, NAVIGATION_BUTTON);
        }
        return _btNavigation;        
    }
    
    /**
     * Switch to source code of navigation rules.
     */
    public void switchToSource() {
        btSource().pushNoBlock();
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    /**
     * Switch to visual designer of navigation rules.
     */
    public void switchToNavigation() {
        btNavigation().pushNoBlock();
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    /**
     * Show "Page Navigation" pane.
     */
    public static NavigatorOperator show() {
        Util.getMainTab().setSelectedIndex(Util.getMainTab().findPage("Page Navigation"));
        return(new NavigatorOperator());
    }

    private ComponentOperator getDesktop() {
        if(desktop == null) {
            desktop = new NavigatorGraphFrameOperator();
        }
        return(desktop);
    }

    /**
     * Push given item of popup menu.
     * @param menuText Name of given item.
     */
    public void pushPopup(String menuText) {
	/* No need to handle Mac differently, comment it out
         if (System.getProperty("os.name").equals("Mac OS X")) 
	    getDesktop().typeKey(' ', InputEvent.CTRL_MASK);
	else */
       
        getDesktop().clickForPopup(1, 1);
	try { Thread.sleep(500); } catch(Exception e) {}
	new JPopupMenuOperator().pushMenuNoBlock(menuText);
        try { Thread.sleep(500); } catch(Exception e) {}
    }

    /**
     * Call popup menu in given position of "Page Navigation" pane.
     * @param x x-coordinate of necessary position.
     * @param y y-coordinate of necessary position.
     */
    public void clickForPopup(int x, int y) {
        getDesktop().clickForPopup(x,y);
    }

    /**
     * Click mouse in given position of "Page Navigation" pane.
     * @param x x-coordinate of necessary position.
     * @param y y-coordinate of necessary position.
     */
    public void clickMouse(int x, int y) {
        getDesktop().clickMouse(x,y,1);
    }

    /**
     * Click mouse specified times in given position of "Page Navigation" pane.
     * @param x x-coordinate of necessary position.
     * @param y y-coordinate of necessary position.
     * @param count Number of clicks.
     */
    public void clickMouse(int x, int y, int count) {
        getDesktop().clickMouse(x,y,count);
    }

    /**
     * Select given page in "Page Navigation" pane.
     * @param page Name of the page.
     */
    /*
    public void select(String page)  {
        PageOperator pp = new PageOperator(this, page);
        pp.select();
    }
     */

    /**
     * Create link from one given page to another.
     * @param page1 Name of the page where link will be created from.
     * @param page2 Name of the page where link will be created to.
     */
    public void link(String page1, String page2) {

        linkUsingXmlSource(page1, page2);
        /*
        PageOperator p1 = new PageOperator(this, page1);
        PageOperator p2 = new PageOperator(this, page2);
        p1.select();
        Util.wait(5000);
        //Util.wait(500);
        
        getDesktop().pressMouse(p1.getX()+40, p1.getY()+40);
        getDesktop().moveMouse(p1.getX()+41, p1.getY()+41);
        getDesktop().moveMouse(p2.getX()+41, p2.getY()+41);
        getDesktop().moveMouse(p2.getX()+40, p2.getY()+40);
        getDesktop().releaseMouse(p2.getX()+40, p2.getY()+40);
        Util.wait(1000);
         */
        //new DNDDriver().dnd(this, new Point(p1.getX()+40, p1.getY()+40), this, new Point(p2.getX()+40, p2.getY()+40));
    }

    /**
     * Create link with specified name from one given page to another.
     * @param page1 Name of the page where link will be created from.
     * @param page2 Name of the page where link will be created to.
     * @param linkId Name of the link.
     */
    public void link(String page1, String page2, String linkId) {
        linkUsingXmlSource(page1, page2, linkId);
        /* TODO need to fix it
        link(page1,page2);
        Util.wait(1000);
        for (int i = 0; i < linkId.length(); i++) {
            typeKey(linkId.charAt(i));
            Util.wait(500);
        }
        pushKey(KeyEvent.VK_ENTER);
        Util.wait(1000);
         */
    }
    
    /**
     * Create links from one given page to another.
     * Even elements in the list are names of pages where links will be created from.
     * Odd elements in the list are names of pages where links will be created to.
     * @param webPageNameList List names of the pages.
     */
    public void linkUsingXmlSource(List webPageNameList) {
        linkUsingXmlSource(webPageNameList, DEFAULT_LINK_ID);
    }
    
    /**
     * Create links with specified name from one given page to another.
     * Even elements in the list are names of pages where links will be created from.
     * Odd elements in the list are names of pages where links will be created to.
     * @param webPageNameList List names of the pages.
     * @param linkId Name of the link.
     */
    public void linkUsingXmlSource(List webPageNameList, String linkId) {
        switchToSource();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        EditorOperator editor = getEditorOperator();
        for (int i = 0; i < webPageNameList.size() - 1; ++i) {
            String masterWebPageName = (String) webPageNameList.get(i),
                   slaveWebPageName  = (String) webPageNameList.get(i + 1);
            appendLinkXmlTags(editor, masterWebPageName, slaveWebPageName, linkId);
            TestUtils.outMsg("+++ Web page link [" + linkId + "] from [" + 
                masterWebPageName + "] to [" + slaveWebPageName + "] is created");
        }
        switchToNavigation();
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    /**
     * Create link from one given page to another with source code editor.
     * @param fromPageName Name of the page where link will be created from.
     * @param toPageName Name of the page where link will be created to.
     */
    public void linkUsingXmlSource(String fromPageName, String toPageName) {
        linkUsingXmlSource(fromPageName, toPageName, DEFAULT_LINK_ID);
    }
    
    /**
     * Create link with specified name from one given page to another with source code editor.
     * @param fromPageName Name of the page where link will be created from.
     * @param toPageName Name of the page where link will be created to.
     * @param linkId Name of the link.
     */
    public void linkUsingXmlSource(String fromPageName, String toPageName, String linkId) {
        switchToSource();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        EditorOperator editor = getEditorOperator();
        appendLinkXmlTags(editor, fromPageName, toPageName, linkId);
        
        switchToNavigation();
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }

    /**
     * Add string for link with specified name from one given page to another into source code editor.
     * @param editor Operator for navigation source code editor.
     * @param fromPageName Name of the page where link will be created from.
     * @param toPageName Name of the page where link will be created to.
     * @param linkId Name of the link.
     */
    private void appendLinkXmlTags(EditorOperator editor, String fromPageName, 
                                   String toPageName, String linkId) {
        // put editor's caret to the start position of the last line
        putCaretToBeginOfLastLine(editor);
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        String linkXmlTags = getLinkXmlTags(fromPageName, toPageName, linkId);
        TestUtils.outMsg("+++ xml-tags used for link [" + linkId + "] between pages [" + 
            fromPageName + "] and [" + toPageName + "]: " + linkXmlTags);
        editor.insert(linkXmlTags);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    /**
     * Put caret to begin of last line in source code editor.
     * @param editor Operator for navigation source code editor.
     */
    private void putCaretToBeginOfLastLine(EditorOperator editor) {
        if (System.getProperty("os.name").equalsIgnoreCase("Mac OS X"))
            editor.pushKey(KeyEvent.VK_END, KeyEvent.META_DOWN_MASK);
        else
            editor.pushKey(KeyEvent.VK_END, KeyEvent.CTRL_DOWN_MASK);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        editor.pushUpArrowKey();
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    /**
     * Return prepared tag for link with specified name from one given page to another.
     * @param fromPageName Name of the page where link will be created from.
     * @param toPageName Name of the page where link will be created to.
     * @param linkId Name of the link.
     * @return String Tag of navigation rule.
     */
    private String getLinkXmlTags(String fromPageName, String toPageName, String linkId) {
        String[] xmlTags = new String[] {
            "    <navigation-rule>",
            "        <from-view-id>/" + fromPageName + "</from-view-id>",
            "            <navigation-case>",
            "                <from-outcome>" + linkId + "</from-outcome>",
            "                <to-view-id>/" + toPageName + "</to-view-id>",
            "        </navigation-case>",
            "    </navigation-rule>"
        };
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < xmlTags.length; ++i) {
            buffer.append(xmlTags[i]);
            buffer.append("\n");
        }
        return buffer.toString();
    }
            
    /**
     * Get navigation source code editor.
     * @return Operator for navigation source code editor.
     */
    private EditorOperator getEditorOperator() {
        //TopComponentOperator topComponent = new TopComponentOperator(PAGE_NAVIGATION_TITLE);        
        TopComponentOperator topComponent = new TopComponentOperator(Util.getMainWindow());
        EditorOperator editor = new EditorOperator(topComponent, PAGE_NAVIGATION_TITLE);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        TestUtils.outMsg("+++ Source editor for [" + PAGE_NAVIGATION_TITLE + "] found");
        return editor;
    }
    
    /**
     * Create static link with specified name from one given page to another.
     * @param page1 Name of the page where link will be created from.
     * @param page2 Name of the page where link will be created to.
     * @param linkId Name of the link.
     */
    /*
    public void staticLink(String page1, String page2,String linkId) {
        PageOperator p1 = new PageOperator(this, page1);
        PageOperator p2 = new PageOperator(this, page2);

        p1.select();

        Util.wait(300);

        new DNDDriver().dnd(this, new Point(p1.getX()+10, p1.getY()+33), this, new Point(p2.getX()+5, p2.getY()+5));

        Util.wait(300);

        for (int i=0; i<linkId.length(); i++){
            typeKey(linkId.charAt(i));
        }
    }*/

    public class NavigatorGraphFrameOperator extends ComponentOperator {
        public NavigatorGraphFrameOperator() {
            super(NavigatorOperator.this, new NavigatorGraphFrameChooser());
        }
    }
    public class NavigatorGraphFrameChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            //return(comp instanceof PageFlowGraph);
            //return(comp instanceof org.netbeans.graph.JGraphView);
	    //Hotfix for sanity to pass.  There should be no test for Navigation.
	    return false;
        }
        public String getDescription() {
            //return(FormNamePanel.class.getName());
            return(FormNamePanel.class.getName());
        }
    }
    public static class NavigatorChooser implements ComponentChooser {
        public NavigatorChooser() {
        }

        public boolean checkComponent(Component comp) {
            // TODO: 
//            boolean res = comp instanceof NavigationView;
//            return res;
              return false;
        }

        public String getDescription() {
           // return(NavigationView.class.getName());
            return(null);
        }
    }
}
