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

package org.netbeans.modules.visualweb.gravy.toolbox;

import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.DNDDriver;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
//import com.sun.rave.toolbox.PaletteContainer;
import org.netbeans.modules.visualweb.gravy.actions.PropertiesAction;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.beans.*;
import java.util.StringTokenizer;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.Bundle;
import javax.swing.*;
import org.netbeans.jemmy.Timeout;

/**
 *
 * @author Alexandre (Shura) Iline (alexandre.iline@sun.com)
 */
public class PaletteContainerOperator extends ContainerOperator {
    
    private static final String PALETTE_TITLE = "Palette";
    JListOperator theTree = null;
    JCheckBoxOperator categoryButton = null;
    private String name;
    static String defaultPaletteCategory = "Basic";
    
    private PaletteContainerOperator(ContainerOperator cont, String name) {
        super(cont, new PaletteContainerChooser(name));
        this.name=name;
    }
    
    /**
     * @param name of the section in Palette
     * @return Container containing CheckBox with section name and JList of the components
     */
    private static Container findPaletteContainer(String name) {
        return new JCheckBoxOperator(new org.netbeans.jellytools.TopComponentOperator(PALETTE_TITLE), name).getParent();
    }
    
    public PaletteContainerOperator(String name) {
        super(findPaletteContainer(name));
        this.name=name;
    }
    
    public static String getDefaultPaletteCategory() {
        return defaultPaletteCategory;
    }
    
    public void showComponents() {
        if (categoryButton == null) {
            categoryButton = new JCheckBoxOperator(this, name);
        }
        categoryButton.changeSelection(true);
    }
    
    public void hideComponents() {
        if (categoryButton == null) {
            categoryButton = new JCheckBoxOperator(this, name);
        }
        categoryButton.changeSelection(false);
    }
    
    public Point getClickPoint(String componentName) {
        JListOperator theTree = getComponentsTree();
        return(theTree.getClickPoint(theTree.findItemIndex(componentName)));
    }
    /**
     * TODO:
     */
    public Point getClickPoint(String componentName, int index) {
        return(getComponentsTree().getClickPoint(
                getComponentsTree().findItemIndex(componentName, index)));
    }
    
    public Point getClickPointOfClass(Class clz) {
        return(getClickPoint(getDisplayNameOfClass(clz)));
    }
    
    public Point getClickPointOfClass(String className) {
        try {
            return(getClickPointOfClass(Class.forName(className)));
        } catch(ClassNotFoundException e) {
            return(getClickPoint(getShortClassName(className)));
        }
    }
    
    private JViewport findViewportOfPalette() {
        JViewport viewPort = (JViewport)ContainerOperator.findContainerUnder(getSource(),
                new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return (JViewport.class.isInstance(comp));
            }
            public String getDescription() {
                return ("JViewport");
            }
        });
        return viewPort;
    }
    
    public void dndPaletteComponent(String componentName,
            ComponentOperator designer, Point designerPoint) {
        Point clickPoint = getClickPoint(componentName);
        Util.wait(1000);
        dndPaletteComponent(clickPoint, designer, designerPoint);
    }
    
    public void dndComponentOfClass(String componentClassName,
            ComponentOperator designer, Point designerPoint) {
        Point clickPoint = getClickPointOfClass(componentClassName);
        Util.wait(1000);
        dndPaletteComponent(clickPoint, designer, designerPoint);
    }
    
    private void dndPaletteComponent(Point componentClickPoint, ComponentOperator designer,
            Point designerPoint) {
        JViewport viewPort = findViewportOfPalette();
        ComponentOperator component = getComponentsTree();
        int compX = (component.getLocationOnScreen().x - viewPort.getLocationOnScreen().x) +
                componentClickPoint.x;
        int compY = (component.getLocationOnScreen().y - viewPort.getLocationOnScreen().y) +
                componentClickPoint.y;
        
        System.out.println();
        System.out.println("+++ coordinates of component in Palette Panel = [" +
                compX + ", " + compY + "]");
        System.out.println();
        
        viewPort.setViewPosition(new Point(compX, compY));
        Util.wait(1000);
        //TODO Somehow it goes to previous component so need to shift y a bit
        //componentClickPoint = new Point(componentClickPoint.x,componentClickPoint.y+5);
        component.clickMouse(componentClickPoint.x, componentClickPoint.y, 1);
        Util.wait(2000);
        System.out.println();
        System.out.println("+++ Component in Palette Panel should be visible and selected");
        System.out.println("+++ Left-top position in Palette Panel = " + viewPort.getViewPosition());
        System.out.println();
        
        DNDDriver dndDriver = new DNDDriver();
        Util.wait(1000);
        dndDriver.dnd(component, componentClickPoint, designer, designerPoint,
                InputEvent.BUTTON1_MASK, 0);
        Util.wait(3000);
        System.out.println();
        System.out.println("+++ Component should be put from Palette on Designer Pane");
        System.out.println();
        
    }
    
    public JListOperator getComponentsTree() {
        showComponents();
        if(theTree == null) {
            theTree = new JListOperator(this);
        }
        return(theTree);
    }
    
    public void addComponent(String componentName) {
        Point clickPoint = getClickPoint(componentName);
        theTree.clickMouse(clickPoint.x, clickPoint.y, 2);
    }
    
     public void addComponent(String componentName, int index) {
        Point clickPoint = getClickPoint(componentName, index);
        theTree.clickMouse(clickPoint.x, clickPoint.y, 2);
    }
    public void addComponentOfClass(Class clz) {
        Point clickPoint = getClickPointOfClass(clz);
        theTree.clickMouse(clickPoint.x, clickPoint.y, 2);
        TestUtils.wait(2000);
    }
    
    public void addComponentOfClass(String className) {
        Point clickPoint = getClickPointOfClass(className);
        theTree.clickMouse(clickPoint.x, clickPoint.y, 2);
        TestUtils.wait(2000);
    }
    
    public void addComponent(String componentName, DesignerPaneOperator designer, Point location) {
        Point clickPoint = getClickPoint(componentName);
        //getComponentsTree().clickMouse(clickPoint.x, clickPoint.y, 1);
        JListOperator tree = getComponentsTree();
        if (tree.isSelectionEmpty()) {
            tree.selectItem(componentName);
        } else {
            tree.clearSelection();
            tree.selectItem(componentName);
        }
        TestUtils.wait(2000);
        designer.clickMouse(location.x, location.y, 1);
        TestUtils.wait(2000);
        //Second click added because when component added on designer it is not in focus
        designer.clickMouse(1, 100, 1);
        TestUtils.wait(2000);
        designer.clickMouse(location.x+5, location.y+5, 1);
        TestUtils.wait(2000);
    }
    
    /**
     *  Add component which beginning text is same as others.
     *  i.g., Image and Image Hyperlink. It always selects Image Hyperlink when
     *  use addComponent("Image", designer, new Point(x,y))
     *  as Image Hyperlink is listed in front of Image in Palette.
     *  Example: <BR>
     *  <pre>
     *   String  componentName =  "Image" ;
     *   int  index =  1 ;
     *   palette.addComponent(componentName, indes, designer, new Point(x,y));
     *  </pre>
     */
    public void addComponent(String  componentName, int index,  DesignerPaneOperator designer, Point location) {
        Point clickPoint = getClickPoint(componentName, index);
        getComponentsTree().clickMouse(clickPoint.x, clickPoint.y, 1);
        TestUtils.wait(2000);
        designer.clickMouse(location.x, location.y, 1);
        TestUtils.wait(2000);
    }
    
    public void addComponentOfClass(Class clz, DesignerPaneOperator designer, Point location) {
        Point clickPoint = getClickPointOfClass(clz);
        getComponentsTree().clickMouse(clickPoint.x, clickPoint.y, 1);
        designer.clickMouse(location.x, location.y, 1);
        TestUtils.wait(2000);
    }
    
    public void addComponentOfClass(String className, DesignerPaneOperator designer, Point location) {
        
        Point clickPoint = getClickPointOfClass(className);
        getComponentsTree().clickMouse(clickPoint.x, clickPoint.y, 1);
        designer.clickMouse(location.x, location.y, 1);
        TestUtils.wait(2000);
    }
    
/*
    public void addComponent(String componentName) {
        getComponentButton(componentName).clickMouse(2);
    }
 
    public void addComponentOfClass(Class clz) {
        getComponentButtonOfClass(clz).clickMouse(2);
    }
 
    public void addComponentOfClass(String className) {
        getComponentButtonOfClass(className).clickMouse(2);
    }
 
    public void addComponent(String componentName, DesignerPaneOperator designer, Point location) {
        //new DNDDriver().dnd(getComponentButton(componentName), new Point(1, 1),
        //designer, location);
        getComponentButton(componentName).clickMouse();
        designer.clickMouse(location.x, location.y, 1);
    }
 
    public void addComponentOfClass(Class clz, DesignerPaneOperator designer, Point location) {
        getComponentButtonOfClass(clz).clickMouse();
        designer.clickMouse(location.x, location.y, 1);
    }
 
    public void addComponentOfClass(String className, DesignerPaneOperator designer, Point location) {
        getComponentButtonOfClass(className).clickMouse();
        designer.clickMouse(location.x, location.y, 1);
    }
 */
    public void placeClip(String clipName, JEditorPaneOperator editor, int caretPosition) {
        Rectangle rect = editor.modelToView(caretPosition);
        Point location = new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
        dndComponentOfClass(clipName, editor, location);
    }
/*
    public void placeClip(String clipName, JEditorPaneOperator editor, int caretPosition) {
        Rectangle rect = editor.modelToView(caretPosition);
        Point location = new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
        new DNDDriver().dnd(getComponentButton(clipName), new Point(1, 1), editor, location);
    }
 */
    public void placeClip(String clipName, JEditorPaneOperator editor) {
        placeClip(clipName, editor, editor.getCaretPosition());
    }
    
    private String getDisplayNameOfClass(Class clz) {
        String name = null;
        try {
            name = java.beans.Introspector.getBeanInfo(clz).getBeanDescriptor().getDisplayName();
        } catch(IntrospectionException e) {
        }
        if(name == null) {
            name = getShortClassName(clz.getName());
        }
        return(name);
    }
    
    private String getShortClassName(String className) {
        String name = null;
        StringTokenizer token = new StringTokenizer(className, ".");
        while(token.hasMoreTokens()) {
            name=token.nextToken();
        }
        return(name);
    }
    
    /**
     *  TODO: need to be updated
     *
     */
    public static void showPalette() {
        Util.getMainMenu().pushMenu("Window|Palette" );
        new QueueTool().waitEmpty();
    }
    
    public static PaletteContainerOperator showPalette(String buttName, String paletteName) {
        new org.netbeans.modules.visualweb.gravy.toolbox.actions.ShowPaletteAction().perform();
        new QueueTool().waitEmpty();
        new JToggleButtonOperator(new ToolBoxOperator(), buttName).push();
        //new JButtonOperator(new ToolBoxOperator(), buttName).push();
        return(new PaletteContainerOperator(paletteName));
    }
    
    public static PaletteContainerOperator showCodeClips() {
        new org.netbeans.modules.visualweb.gravy.toolbox.actions.ShowPaletteAction().perform();
        new QueueTool().waitEmpty();
        new JToggleButtonOperator(new ToolBoxOperator(), Bundle.getStringTrimmed("com.sun.rave.toolbox.Bundle", "CODE_CLIPS")).push();
        return(new PaletteContainerOperator("Samples"));
    }
    
    /**
     * TODO: maked private because doesn't work properly
     */
    private static class PaletteContainerChooser implements ComponentChooser {
        String name;
        public PaletteContainerChooser(String name) {
            this.name = name;
        }
        public boolean checkComponent(Component comp) {
            System.out.println("Class ="+ comp.getClass()+"   Name="+comp.getClass().getName());
            if (comp.getClass().getName().equals("org.netbeans.modules.palette.ui.CategoryDescriptor$1")){
                return true;
            }
            return false;//(comp.getClass().getName().equals("org.netbeans.modules.palette.ui.CategoryButton"));
            
        }
        public String getDescription() {
            return(/*PaletteContainer.class.getName() + */" with \"" + name + "\" name");
        }
    }
}
