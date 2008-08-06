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
/*
 * UIUtilities.java
 *
 * Created on June 29, 2006, 3:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 *
 * @author girix
 */
public class UIUtilities {
    
    public static String standardizeString(String str){
        str = str.toLowerCase();
        StringBuffer strbuff = new StringBuffer(str);
        strbuff.replace(0, 1, str.substring(0,1).toUpperCase());
        return new String(strbuff);
    }
    
    public static String getConstraintsString(String minOccurs, String maxOccurs) {
        int min;
        int max;
        
        if(minOccurs == null){
            min = 1;
        }else{
            try{
                min = Integer.parseInt(minOccurs);
            }catch(NumberFormatException e){
                min = 1;
            }
        }
        
        if(maxOccurs == null){
            max = 1;
        }else if(maxOccurs.equalsIgnoreCase("unbounded")){ //noi18n
            max = Integer.MAX_VALUE;
        }else{
            try{
                max = Integer.parseInt(maxOccurs);
            }catch(NumberFormatException e){
                max = 1;
            }
        }
        
        String str;
        if(min == max){
            if(max == 1){
                return "";
            }
            return "["+min+".."+max+"]";
        }
        if(min < max){
            String maxStr;
            if(max == Integer.MAX_VALUE)
                maxStr = "*"; //noi18n
            else
                maxStr = ""+max;
            return "["+min+".."+maxStr+"]";
        }
        //min > max?...error case. Should not happen
        return null;
    }
    
    
    
    private static final String LOC_SIMPLE_TYPE = NbBundle.getMessage(UIUtilities.class, "LBL_SIMPLE_TYPE");
    private static final String LOC_COMPLEX_TYPE = NbBundle.getMessage(UIUtilities.class, "LBL_COMPLEX_TYPE");
    private static final String LOC_FROM = NbBundle.getMessage(UIUtilities.class, "LBL_FROM");
    private static final String LOC_BELONGS_TO = NbBundle.getMessage(UIUtilities.class, "LBL_INSTANCE_OF");
    
    public static JLabel getContentModelInfoLabel(AXIComponent component, boolean simpleTypeOnly, boolean withIcon, final InstanceUIContext context) {
        HyperlinkLabel contentModelInfoLabel = new HyperlinkLabel();
        contentModelInfoLabel.setForeground(new Color(139, 139, 139));
        
        Font font = contentModelInfoLabel.getFont();
//        font = new Font(font.getFontName(), Font.PLAIN,
//                InstanceDesignConstants.PROPS_FONT_SIZE);
        font = font.deriveFont((font.getStyle() | java.awt.Font.PLAIN), font.getSize());
        contentModelInfoLabel.setFont(font);
        
        if(component instanceof Element){
            Element element = (Element) component;
            AXIType dt = element.getType();
            
            if(element.isReference()){
                //this is for element reference
                try{
                    Element aelm = ((Element)getSuperDefn(element));
                    while(aelm.isReference())
                        aelm = (Element)aelm.getReferent();
                    contentModelInfoLabel.setText(aelm.getName());
                    contentModelInfoLabel.setIcon(getImageIcon("element.png"));
                    contentModelInfoLabel.setToolTipText(NbBundle.getMessage(UIUtilities.class,
                            "TTP_REFERENCE_TO_GE", element));
                    final AbstractElement tmpaelm = aelm;
                    contentModelInfoLabel.setHyperlinkClickHandler(new HyperlinkLabel.HyperlinkClickHandler(){
                        public void handleClick() {
                            showDefinition(context, tmpaelm, false);
                        }
                        
                    });
                    return contentModelInfoLabel;
                }catch (Throwable e){
                    //might get casting exception so ignore
                    return null;
                }
            }
            
            if(dt == null)
                return null;
            String value = dt.getName();
            if(value == null) {
                contentModelInfoLabel.setText(NbBundle.getMessage(UIUtilities.class, "LBL_ANONYMOUS_TYPE"));                
                return contentModelInfoLabel;
            }
            
            contentModelInfoLabel.setText(value);
            String typeStr = null;
            typeStr = (dt instanceof Datatype) ? LOC_SIMPLE_TYPE : LOC_COMPLEX_TYPE;
            if(withIcon){
                String iconStr = (dt instanceof Datatype) ? "simpletype.png" :
                    "complextype.png";
                contentModelInfoLabel.setIcon(getImageIcon(iconStr));
            }
            String infoStr = " "+LOC_BELONGS_TO+" "+typeStr+": "+value+" ";
            contentModelInfoLabel.setToolTipText(infoStr);
            if(dt instanceof ContentModel){
                final ContentModel cm = (ContentModel) dt;
                contentModelInfoLabel.
                        setHyperlinkClickHandler(new HyperlinkLabel.HyperlinkClickHandler(){
                    public void handleClick() {
                        showDefinition(context, cm, false);
                    }
                    
                });
            }
            return contentModelInfoLabel;
        }
        
        if(!simpleTypeOnly){
            final ContentModel cm = component.getContentModel();
            if(cm != null){
                String type = getContentModelTypeString(cm.getType());
                String locFromStr = (component instanceof AbstractElement) ? LOC_BELONGS_TO : LOC_FROM;
                String infoStr = " "+locFromStr+" "+type+": "+cm.getName()+" ";
                contentModelInfoLabel.setToolTipText(infoStr);
                contentModelInfoLabel.setText(cm.getName());
                if(withIcon)
                    contentModelInfoLabel.setIcon(getContentModelTypeIcon(cm.getType()));
                contentModelInfoLabel.
                        setHyperlinkClickHandler(new HyperlinkLabel.HyperlinkClickHandler(){
                    public void handleClick() {
                        showDefinition(context, cm, false);
                    }
                    
                });
                return contentModelInfoLabel;
            }
        }
        return null;
    }
    
    public static org.netbeans.modules.xml.xam.Component getSuperDefn(AXIComponent axiComponent) {
        AXIComponent original = axiComponent.getOriginal();
        if(original == axiComponent){
            if(original instanceof Element){
                Element elm = (Element)original;
                if(elm.isReference())
                    return elm.getReferent();
            }
            if(original instanceof Attribute){
                Attribute attr = (Attribute)original;
                if(attr.isReference())
                    return attr.getReferent();
            }
            return null;
        }
        return original;
    }
    
    
    public static void showDefinition(InstanceUIContext context,
            AXIComponent axiComponent, boolean showSuper){
        if(context != null){
            setBusyCursor(context);
            try{
                FileObject fo = (FileObject) axiComponent.getModel()
                .getSchemaModel().getModelSource().getLookup()
                .lookup(FileObject.class);
                DataObject sdo = null;
                try {
                    sdo = DataObject.find(fo);
                } catch (DataObjectNotFoundException ex) {
                    return;
                }
                if(sdo == null)
                    return;
                ViewComponentCookie vcc = (ViewComponentCookie) sdo
                        .getCookie(ViewComponentCookie.class);
                if(vcc != null){
                    AXIComponent tmp = axiComponent;
                    if(showSuper)
                        tmp = (AXIComponent) getSuperDefn(axiComponent);
                    vcc.view(ViewComponentCookie.View.SUPER,  tmp);
                }
            }finally{
                setDefaultCursor(context);
            }
        }
    }
    
    
    public static String getContentModelTypeString(ContentModel.ContentModelType cmType) {
        switch(cmType){
            case COMPLEX_TYPE:
                return NbBundle.getMessage(UIUtilities.class, "LBL_COMPLEX_TYPE"); //noi8n
            case GROUP:
                return NbBundle.getMessage(UIUtilities.class, "LBL_GROUP"); //noi8n
            case ATTRIBUTE_GROUP:
                return NbBundle.getMessage(UIUtilities.class, "LBL_ATTRIBUTE_GROUP"); //noi8n
        }
        return "";
    }
    
    public static Icon getContentModelTypeIcon(ContentModel.ContentModelType cmType) {
        switch(cmType){
            case COMPLEX_TYPE:
                return new javax.swing.ImageIcon(UIUtilities.class.
                        getResource("/org/netbeans/modules/xml/schema/abe/resources/complextype.png"));
            case GROUP:
                return new javax.swing.ImageIcon(UIUtilities.class.
                        getResource("/org/netbeans/modules/xml/schema/abe/resources/group.png"));
            case ATTRIBUTE_GROUP:
                return new javax.swing.ImageIcon(UIUtilities.class.
                        getResource("/org/netbeans/modules/xml/schema/abe/resources/attributeGroup.png"));
        }
        return null;
    }
    
    public static Icon getImageIcon(String iconFileName) {
        return new javax.swing.ImageIcon(UIUtilities.class.
                getResource("/org/netbeans/modules/xml/schema/abe/resources/"
                +iconFileName));
    }
    
    public static void scrollViewTo(Component comp, InstanceUIContext context){
        if(!context.getTopComponent().isShowing())
            return;
        JViewport viewPort = context.getInstanceDesignerScrollPane().getViewport();
        Rectangle rect = comp.getBounds();
        rect = SwingUtilities.convertRectangle(comp.getParent(), rect, viewPort);
        rect.height += 100;
        //rect.width += 100;
        viewPort.scrollRectToVisible(rect);
    }
    
    public static AXIComponent findMatchingAXIComponent(SchemaComponent c) {
        AXIComponent matchingComponent = null;
        AXIModel model = AXIModelFactory.getDefault().getModel(c.getModel());
        assert model != null;
        if (model.getState() == Model.State.VALID) {
            List<SchemaComponent> pathFromRoot = findSchemaComponentsFromRoot(c);
            matchingComponent = findClosestAXIComponent(pathFromRoot, model);
        }
        return matchingComponent;
    }
    
    private static List<SchemaComponent> findSchemaComponentsFromRoot(SchemaComponent s) {
        List<SchemaComponent> components = new ArrayList<SchemaComponent>();
        components.add(0,s);
        SchemaComponent currentSC = s;
        while (currentSC.getParent() != currentSC.getModel().getRootComponent()) {
            currentSC = currentSC.getParent();
            components.add(0,currentSC);
        }
        return components;
    }
    
    private static AXIComponent findClosestAXIComponent(
            List<SchemaComponent> components, AXIModel model) {
        assert !components.isEmpty();
        SchemaComponent root = components.remove(0);
        AXIComponent lastFound = findChildComponent(root, model.getRoot());
        if (lastFound != null) {
            for (SchemaComponent sc: components) {
                AXIComponent newRoot = findChildComponent(sc, lastFound);
                if (newRoot != null) {
                    lastFound = newRoot;
                }
            }
        }
        return lastFound;
    }
    
    private static AXIComponent findChildComponent(SchemaComponent component,
            AXIComponent searchFrom) {
        AXIComponent found = null;
        for (AXIComponent axiComponent:searchFrom.getChildren()) {
            if (axiComponent.getPeer() == component) {
                found = axiComponent;
            }
        }
        return found;
    }
    
    public static void showBulbMessage(String message, InstanceUIContext context){
        showBulbMessageFor(message, context, null);
    }
    
    public static void showBulbMessageFor(String message, InstanceUIContext context,
            Component messageForComp){
        showMessage(UIUtilities.getImageIcon("bulb.png"), null,
                null, message,
                context, null, messageForComp);
    }
    
    public static void showErrorMessage(String message, InstanceUIContext context){
        showErrorMessageFor(message, context, null);
    }
    
    public static void showErrorMessageFor(String message, InstanceUIContext context,
            Component messageForComp){
        showMessage(UIUtilities.getImageIcon("error.png"), Color.RED,
                null, message,
                context, null, messageForComp);
    }
    
    public static void showErrorMessage(String message, InstanceUIContext context, JPanel glass){
        showErrorMessageFor(message, context, glass, null);
    }
    
    public static void showErrorMessageFor(String message,
            InstanceUIContext context, JPanel glass, Component messageForComp){
        showMessage(UIUtilities.getImageIcon("error.png"),
                Color.RED, null, message,
                context, glass, messageForComp);
    }
    
    public static void showMessage(Icon icon, Color color, String message, InstanceUIContext context){
        showMessageFor(icon, color, message, context, null);
    }
    
    public static void showMessageFor(Icon icon, Color color, String message,
            InstanceUIContext context, Component messageForComp){
        showMessage(icon, color, null, message, context, null, null);
    }
    
    
    
    static JLabel infoLabel ;
    static JPanel glassReference;
    protected static void showMessage(Icon icon, Color foreGroundColor, Color backgroundColor,
            String message, InstanceUIContext context, JPanel glass,
            Component messageForComp){
        if(message == null)
            return;
        if( (infoLabel != null) && (infoLabel.getParent() != null) ){
            if(infoLabel.getText().equals(message))
                return;
        }
        infoLabel = null;
        if(icon != null)
            infoLabel = new TranslucentLabel(icon , SwingConstants.LEFT);
        else
            infoLabel = new TranslucentLabel(" ",SwingConstants.LEFT);
        
        Font font = infoLabel.getFont();
        //font = new Font(font.getName(), Font.BOLD, font.getSize());
        font = font.deriveFont((font.getStyle() | java.awt.Font.PLAIN), font.getSize());
        if(foreGroundColor != null)
            infoLabel.setForeground(foreGroundColor);
        else
            infoLabel.setForeground(Color.black);
        
        if(backgroundColor != null)
            infoLabel.setBackground(backgroundColor);
        else
            infoLabel.setBackground(InstanceDesignConstants.LIGHT_YELLOW);
        
        infoLabel.setFont(font);
        infoLabel.setText(message);
        Component panel = (messageForComp != null) ? messageForComp :
            context.getNamespacePanel();
        Rectangle rect = panel.getBounds();
        if(glass == null)
            glass = NBGlassPaneAccessSupport.getNBGlassPane(
                    context.getInstanceDesignerPanel());
        else
            glassReference = glass;
        if(glass == null)
            return;
        rect = SwingUtilities.convertRectangle(panel.getParent(), rect, glass);
        glass.add(infoLabel);
        infoLabel.setOpaque(false);
        if(messageForComp != null){
            rect.y -= 20;
            Dimension dim = infoLabel.getPreferredSize();
            rect.width = dim.width;
            rect.height = dim.height;
        }
        infoLabel.setBounds(rect);
        glass.setVisible(true);
    }
    
    public static void hideGlassMessage(boolean disposeGlass){
        if((infoLabel != null) && (glassReference != null)){
            glassReference.remove(infoLabel);
            glassReference.revalidate();
            glassReference.repaint();
        }
        
        if(disposeGlass)
            NBGlassPaneAccessSupport.forceDisposeNBGlassPane();
    }
    
    public static void hideGlassMessage(){
        hideGlassMessage(true);
    }
    
    public static List<Point> getBrokenTapePoints(Point start, int end,
            int xgap, int ygap, boolean rightHanded){
        List<Point> result = new ArrayList<Point>();
        int begin = start.y;
        int stop = end;
        int current = begin;
        boolean sharpeEdge = true;
        result.add(new Point(start.x, current));
        current += ygap;
        while(current < stop){
            if(sharpeEdge){
                if(rightHanded)
                    result.add(new Point((start.x + xgap), current));
                else
                    result.add(new Point((start.x - xgap), current));
                sharpeEdge = false;
            }else{
                if(rightHanded)
                    result.add(new Point((start.x - xgap), current));
                else
                    result.add(new Point((start.x + xgap), current));
                //result.add(new Point((start.x), current));
                sharpeEdge = true;
            }
            current += ygap;
        }
        result.add(new Point(start.x, end));
        return result;
    }
    
    public static String getUniqueName(String name, AXIComponent elm) {
        int count = 1;
        String result = null;
        String nowName = name;
        if(elm.getChildren().size() < 0)
            return name;
        while(result == null){
            boolean gotResult = true;
            for(AXIComponent child : elm.getChildren()){
                if(child.toString().indexOf(nowName) != -1){
                    nowName = name + count++;
                    gotResult = false;
                    break;
                }
            }
            if(gotResult){
                result = nowName;
                break;
            }
        }
        return result;
    }
    
    public static void setBusyCursor(InstanceUIContext context){
        if(context == null)
            return;
        JFrame NBFRAME = NBGlassPaneAccessSupport.getNBFRAME(context.getTopComponent());
        if(NBFRAME == null)
            return;
        NBFRAME.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }
    
    public static void setDefaultCursor(InstanceUIContext context){
        if(context == null)
            return;
        JFrame NBFRAME = NBGlassPaneAccessSupport.getNBFRAME(context.getTopComponent());
        if(NBFRAME == null)
            return;
        NBFRAME.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
}
