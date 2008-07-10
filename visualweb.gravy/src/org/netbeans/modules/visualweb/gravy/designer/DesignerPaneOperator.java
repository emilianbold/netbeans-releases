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

package org.netbeans.modules.visualweb.gravy.designer;

import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.designer.*;
import org.netbeans.modules.visualweb.designer.jsf.*;
import org.netbeans.modules.visualweb.designer.jsf.ui.*;
import org.netbeans.modules.visualweb.gravy.properties.*;
import java.awt.*;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.*;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.jellytools.Bundle;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import com.sun.rave.designtime.DesignBean;
import org.netbeans.modules.visualweb.gravy.RaveWindowOperator;
import org.openide.windows.TopComponent;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.modules.visualweb.api.designer.DomProvider;

/**
 * This class implements test functionality for the Design View of a project web-page.
 */
public class DesignerPaneOperator extends JComponentOperator {
    private static final String
            DESIGNER_BUTTON_DESIGN = org.netbeans.modules.visualweb.gravy.Bundle.getStringTrimmed("org.netbeans.modules.visualweb.project.jsfloader.Bundle", "CTL_DesignerTabCaption"),//"Design"
            DESIGNER_BUTTON_JSP = org.netbeans.modules.visualweb.gravy.Bundle.getStringTrimmed("org.netbeans.modules.visualweb.project.jsfloader.Bundle","CTL_JspTabCaption"),//"JSP",
            DESIGNER_BUTTON_JAVA = org.netbeans.modules.visualweb.gravy.Bundle.getStringTrimmed("org.netbeans.modules.visualweb.project.jsfloader.Bundle","CTL_JavaTabCaption");//"Java";
    
    protected static String defaultPalette="JSFSTD";
    protected String currentPalette=defaultPalette;
    
    /**
     * Creates a new instance of this class
     * @param cont an object ContainerOperator related to container, which 
     * includes window of web-page.
     */
    public DesignerPaneOperator(ContainerOperator cont) {
        super(cont, new DesignerPaneChooser());
    }
    
    /**
     * Creates a new instance of this class
     * @param cont an object ContainerOperator related to container, which 
     * includes window of web-page.
     * @param index a number of desired component if container includes several
     * similar components
     */
    public DesignerPaneOperator(ContainerOperator cont, int index) {
        super(cont, new DesignerPaneChooser(), index);
    }
    
    /**
     * Creates a new instance of this class
     */
    public DesignerPaneOperator() {
        this(RaveWindowOperator.getDefaultRave());
    }
    
    /**
     * Returns a Design View of a project web-page.
     * @return an object ContainerOperator
     */
    public ContainerOperator getDesignerView() {
        return(new ContainerOperator(getContainer(new Operator.Finder(JsfTopComponent.class))));
    }
    
    /**
     * Returns a center coordinate of component, placed on Design View.
     * @param comp_id a component identifier
     * @return an object Point
     */
    public Point getComponentCenter(String comp_id) {
        CssBox box = getCssBox(getDesignBean(comp_id));
        if (box != null) {
            return (new Point(box.getAbsoluteX(), box.getAbsoluteY()));
        }
        return null;
    }
    
    /**
     * Returns a coordinate of component top-left corner, placed on Design View.
     * @param componentID a component identifier
     * @return an object Point
     */
    public Point getComponentLocation(String componentID) {
        Rectangle compRect = getComponentRectangle(componentID);
        if (compRect != null) {
            return (compRect.getLocation());
        }
        return null;
    }
    
    /**
     * Returns measurements of a component, placed on Design View.
     * @param componentID a component identifier
     * @return an object Rectangle
     */
    public Rectangle getComponentRectangle(String componentID) {
        CssBox box = getCssBox(getDesignBean(componentID));
        if (box != null) {
            return (box.getExtentsRectangle());
        }
        return null;
    }
    
    private DesignBean getDesignBean(String componentID) {
        if ((componentID == null) || (componentID.length() < 1)) return null;
        
        DesignerPane designerPane = (DesignerPane) getSource();
//        DesignBean bean =
//                designerPane.getWebForm().getModel().getLiveUnit().getBeanByName(
//                componentID);
//        org.openide.filesystems.FileObject jspFileObject = designerPane.getWebForm().getJspDataObject().getPrimaryFile();
        WebForm webForm = designerPane.getWebForm();
        DomProvider domProvider = webForm.getDomProvider();
        JsfForm jsfForm = JsfForm.findJsfFormForDomProvider(domProvider);
        org.openide.filesystems.FileObject jspFileObject = jsfForm.getJspDataObject().getPrimaryFile();
        org.netbeans.modules.visualweb.insync.models.FacesModel facesModel = org.netbeans.modules.visualweb.insync.models.FacesModel.getInstance(jspFileObject);
        DesignBean bean = facesModel.getLiveUnit().getBeanByName(componentID);
        return bean;
    }
    
    private CssBox getCssBox(DesignBean designBean) {
        if (designBean == null) return null;
        DesignerPane designerPane = (DesignerPane) getSource();
        /*
        CssBox cssBox = ModelViewMapper.findBox(designerPane.getPageBox(), designBean); 
                                              //designerPane.getWebForm().getMapper().findBox(designBean);
        */
        WebForm webForm = designerPane.getWebForm();
        CssBox cssBox = ModelViewMapper.findBox(webForm.getPane().getPageBox(), 
            webForm.getSelection().getSelectedComponentRootElements()[0]);
        return cssBox;
    }
    
    /**
     * Invokes a popup menu, related to a component, placed on Design View.
     * @param comp_id a component identifier
     */
    public void clickForPopup(String comp_id){
        Point p=getComponentCenter(comp_id);
        clickForPopup(p.x,p.y);
    }

    /**
     * Clicks mouse on a component, placed on Design View.
     * @param comp_id a component identifier
     * @param clickCount a number of clicks
     */
    public void clickMouse(String comp_id, int clickCount){
        Point p=getComponentCenter(comp_id);
        clickMouse(p.x,p.y,clickCount);
    }
    
    /**
     * Selects a component on Design View.
     * @param comp_id a component identifier
     */
    public void select(String comp_id){
        Point p=getComponentCenter(comp_id);
        clickMouse(p.x,p.y,1);
    }
    
    public static class DesignerPaneChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp instanceof org.netbeans.modules.visualweb.designer.DesignerPane);
        }
        public String getDescription() {
            return(DesignerPane.class.getName());
        }
    }

    /**
     * Adds a component on Design View.
     * @param component a component name
     * @param x coordinate x of top-left component corner on Design View
     * @param y coordinate y of top-left component corner on Design View
     */
    public void addComponent(String component, int x, int y) {
        addComponent(component,x,y,null,null);
    }
    
    /**
     * Adds a component on Design View.
     * @param component a component name
     * @param x coordinate x of top-left component corner on Design View
     * @param y coordinate y of top-left component corner on Design View
     * @param id a name of modified component property
     * @param value a value of modified component property
     */
    public void addComponent(String component, int x, int y, String id, String value) {
        addComponentFromPalette(component,x,y,id,value,currentPalette);
    }
    
    /**
     * Sets a component palette used by default.
     * @param palName a palette name
     */
    public static void setDefaultPalette(String palName){
        defaultPalette=palName;
    }
    
    /**
     * Sets a component palette "BraveHeart" as a default palette.
     */
    public static void setDefaultBraveHeartPalette(){
        defaultPalette="Basic";
    }
    
    /**
     * Sets a component palette "Standard" as a default palette.
     */
    public static void setDefaultStandardPalette(){
        defaultPalette="JSFSTD";
    }
    
    /**
     * Sets a current component palette.
     * @param palName a palette name
     */
    public void setPalette(String palName){
        currentPalette=palName;
    }
    
    /**
     * Sets a component palette "BraveHeart" as a current palette.
     */
    public void setBraveHeartPalette(){
        setPalette("Basic");
    }
    
    /**
     * Sets a component palette "Standard" as a current palette.
     */
    public void setStandardPalette(){
        setPalette("JSFSTD");
    }
    
    /**
     * Adds a component on Design View.
     * @param component a component name
     * @param x coordinate x of top-left component corner on Design View
     * @param y coordinate y of top-left component corner on Design View
     * @param id a name of modified component property
     * @param value a value of modified component property
     * @param palName a palette name
     */
    public void addComponentFromPalette(String component, int x, int y, String id, String value, String palName) {
        
        // Wait for the Portfolio to appear
        Util.wait(2000);
        PaletteContainerOperator palette = PaletteContainerOperator.showPalette(Bundle.getStringTrimmed("com.sun.rave.toolbox.Bundle", "COMPONENTS"), palName);
        
        System.out.println("======================================================================");
        System.out.println(palName + " Components");
        System.out.println("======================================================================");
        // workaround for CR 6258411
        Point clickPoint = palette.getClickPoint(component);
        new QueueTool().waitEmpty();
        palette.getComponentsTree().clickMouse(clickPoint.x, clickPoint.y, 1);
        Util.wait(300);
        
        palette.addComponent(component, this, new Point(x, y));
        
        //@todo This is temporary - remove after Rave bug will be fixed
        //select component
        //clickMouse(1,1,1);
        //clickMouse(x,y,1);
        
        Util.wait(1000);
        
        // Change properties
        
        SheetTableOperator props = new SheetTableOperator();
        if (id!=null){
            props.setTextValue("id",id);
            Util.wait(1000);
        }
        if (value!=null){
            props.setTextValue("value",value);
            Util.wait(1000);
        }
        
    }
    
    /**
     * Switch to JSP Editor of web-page.
     */
    public static void switchToJSPSource() {
        switchToSource(DESIGNER_BUTTON_JSP);
    }
    
    /**
     * Switch to Java Editor of web-page.
     */
    public static void switchToJavaSource() {
        switchToSource(DESIGNER_BUTTON_JAVA);
    }
    
    /**
     * Switch to Design View of web-page.
     */
    public static void switchToDesignerPane() {
        switchToSource(DESIGNER_BUTTON_DESIGN);
    }
    
    private static void switchToSource(String toggleButtonText) {
        //org.netbeans.jellytools.TopComponentOperator topComponent =
        //new org.netbeans.jellytools.TopComponentOperator("Page1.jsp");
        //    new org.netbeans.jellytools.TopComponentOperator(Util.getMainWindow());
        //Util.wait(1000);
        JToggleButtonOperator toggleButton = new JToggleButtonOperator(Util.getMainWindow(),
                toggleButtonText);
        Util.wait(1000);
        toggleButton.setSelected(true);
        Util.wait(1000);
    }
    
    /**
     * Extracts and returns a coordinate of a component from a window "Properties".
     * @return an object Point
     */
    public Point getCoords() {
        
        PropertySheetOperator pso = new PropertySheetOperator(Util.getMainWindow());
        PropertySheetTabOperator psto = new PropertySheetTabOperator(pso);
        // set a new comparator which should find the required property by performing
        // a strictly comparison of a property name with a given pattern
        psto.setComparator(new Operator.DefaultStringComparator(true, true));
        
        // Name of the property is style
        Property pr = new Property(psto, "style" /* Bundle.getResourceString("CSS_Style") */);
        String propValue = pr.getValue();
        
        //value of style property (example) = position: absolute; left: 288px; top: 96px
        
        int xLoc = HtmlAttribute.parseInt(propValue.substring(propValue.indexOf("left: ") + 6));
        int yLoc = HtmlAttribute.parseInt(propValue.substring(propValue.indexOf("top: ") + 5));
        //        int xLoc =  new Integer(propValue.substring(propValue.indexOf("left: ")+6,propValue.indexOf("px;"))).intValue();
        //        int yLoc = new Integer(propValue.substring(propValue.indexOf("top: ")+5,propValue.indexOf("px; position:"))).intValue();
        
        return new Point(xLoc, yLoc);
    }

    public void makeComponentVisible() {
        TopComponent theComponent = (TopComponent)findContainerUnder(getSource(), new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return(comp instanceof TopComponent);
            }
            public String getDescription() {
                return("TopComponent");
            }
        });
        
        final TopComponent topComponent = theComponent;
        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    String topComponentString =
                            org.openide.windows.WindowManager.getDefault().findTopComponentID(topComponent);
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
                    System.out.println(topComponentString);
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        new org.netbeans.jellytools.TopComponentOperator(theComponent).makeComponentVisible();
        super.makeComponentVisible();
    }
    
    /**
     * Sets an image to a component via Image customizer.
     * @param componentID a component identifier
     * @param imagePath a path to an image file
     */
     public void setImage(String componentID, String imagePath) {
        clickForPopup(componentID); // this api not working 6/1/06
        Util.wait(500);
        new JPopupMenuOperator().pushMenuNoBlock(Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle", "DesignerMenuItem_SetImage"));
        JDialogOperator dialog = new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle", "DesignerMenuItem_SetImage" ));
        JTextFieldOperator tf_Name = new JTextFieldOperator(dialog, 0);
        tf_Name.setText(imagePath);
        Util.wait(1000);
        new JButtonOperator(dialog, Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle", "OK")).pushNoBlock();
        dialog.waitClosed();
        Util.wait(1000);
    }
     
    /**
     * Sets an image to a component via Image customizer.
     * @param x coordinate x of top-left component corner on Design View
     * @param y coordinate y of top-left component corner on Design View
     * @param imagePath a path to an image file
     */
     public void setImage(int x, int y, String imagePath) {
        clickForPopup(x, y);
        Util.wait(500);
        new JPopupMenuOperator().pushMenuNoBlock(Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle", "DesignerMenuItem_SetImage"));
        JDialogOperator dialog = new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle", "Dialog_ImageCustomizer" ));
        (new JTextFieldOperator(dialog, 0)).enterText(imagePath);
        Util.wait(1000);
        new JButtonOperator(dialog, Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle", "Button_OK")).pushNoBlock();
        dialog.waitClosed();
        Util.wait(1000);
    }
}
