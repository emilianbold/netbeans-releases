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
 * LinkPaletteOperator.java
 *
 * Created on February 15, 2005, 6:42 PM
 */

package org.netbeans.test.umllib;

import java.awt.Component;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.palette.context.PaletteButton;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPalette;
import org.netbeans.modules.uml.drawingarea.palette.context.ComboButton;
import org.netbeans.modules.uml.drawingarea.palette.context.ComboButton.ArrowButton;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteModel;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;

import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.util.LibProperties;


/**
 * This is operator for link palette
 * @author Sherry Zhou
 */
public class LinkPaletteOperator extends ContainerOperator {

    private PaletteButton paletteButton;
    private LinkTypes linkElementType;
    public static final long WAIT_LINK_TIMEOUT = 30000;

    static {
        Timeouts.initDefault("LinkPaletteOperator.WaitLinkTime", WAIT_LINK_TIMEOUT);
        
    }
    
    public LinkPaletteOperator(DiagramElementOperator diagramElement) {
         this(diagramElement.getGraphObject());    
     }

     public LinkPaletteOperator(Widget element) {
         super(waitForLinkPalette(element)); 
         setLinkIndex();
     }
     
    public Point getClickPoint(JComponentOperator linkButton) {
        return new Point(linkButton.getCenterX(), linkButton.getCenterY());
    }

    public static ContextPalette waitForLinkPalette(final Widget element) {
        try {
            Waiter w = new Waiter(new Waitable() {
                
                public Object actionProduced(Object obj) {
                    return findLinkPalette(element);
                }
                public String getDescription() {
                    return("Wait for ContextPalette");
                }
            });

            Timeouts t = JemmyProperties.getCurrentTimeouts();
            t.setTimeout("Waiter.WaitingTime", t.getTimeout("LinkPaletteOperator.WaitLinkTime")); 
            return (ContextPalette) w.waitAction(null);
        } catch (InterruptedException ie) {
            return null;
        }
    }

    public static ContextPalette findLinkPalette(Widget element) {    
        return (ContextPalette)MainWindowOperator.getDefault().findSubComponent(new LinkPaletteChooser(element));
    }

    public static ContextPalette findLinkPalette(ContainerOperator containter, Widget element) {    
        return (ContextPalette)findLinkPalette(containter, element, 0);
    }
    
    public static ContextPalette findLinkPalette(ContainerOperator containter, Widget element, int index ) {    
        return (ContextPalette)containter.findSubComponent(new LinkPaletteChooser(element), index);
    }
      
    public JComponentOperator getAssociationArrowButton(){
        return new JComponentOperator(this, new ArrowButtonChooser(), 0);
    }
     
    public JComponentOperator getDependenceArrowButton(){
        return new JComponentOperator(this, new ArrowButtonChooser(), 1);
    }
 
//TODO: getLinkButton() won't work until each palette button has name
    public JComponentOperator getLinkButtonByName(Enum linkType) throws NotFoundException {
        String linkName = LibProperties.getCurrentToolName(linkType);
        if (linkType.equals(LinkTypes.IMPLEMENTATION) ||
                linkType.equals(LinkTypes.GENERALIZATION) ||
                linkType.equals(LinkTypes.COMMENT_LINK)) {
            return new JComponentOperator(this, new PaletteButtonByNameChooser(linkName)); 
        } else if (linkType.equals(LinkTypes.ASSOCIATION) ||
                linkType.equals(LinkTypes.ASSOCIATION_CLASS) ||
                linkType.equals(LinkTypes.COMPOSITION) ||
                linkType.equals(LinkTypes.NAVIGABLE_COMPOSITION) ||
                linkType.equals(LinkTypes.AGGREGATION) ||
                linkType.equals(LinkTypes.NAVIGABLE_AGGREGATION) ||
                linkType.equals(LinkTypes.NAVIGABLE_ASSOCIATION)) {
            getAssociationArrowButton().clickMouse();
            Utils.wait(500);
            waitComponentVisible(true);
            JComponentOperator associatePopupContent = new JComponentOperator(this, new ComboButtonChooser(), 0);
            return new JComponentOperator(associatePopupContent, new PaletteButtonByNameChooser(linkName));  
        } else if (linkType.equals(LinkTypes.DEPENDENCY) ||
                linkType.equals(LinkTypes.REALIZE) ||
                linkType.equals(LinkTypes.USAGE) ||
                linkType.equals(LinkTypes.PERMISSION) ||
                linkType.equals(LinkTypes.ABSTRACTION)) {
            getDependenceArrowButton().clickMouse();
            Utils.wait(500);
            waitComponentVisible(true);
            JComponentOperator dependencePopupContent = new JComponentOperator(this, new ComboButtonChooser(), 1);
            return new JComponentOperator(dependencePopupContent, new PaletteButtonByNameChooser(linkName));
        }
        Utils.log("Could not find link button");
        return null;
    }
    
     
    static  Map map = new HashMap();
    public static void setLinkIndex() {  
        map = new HashMap();
        
        //Create class diagram link button mapping
        map.put(LinkTypes.GENERALIZATION, new Integer(0));
        map.put(LinkTypes.IMPLEMENTATION, new Integer(1));
        map.put(LinkTypes.COMMENT_LINK, new Integer(3));
        
        //Index of Association Combo array button
        map.put(LinkTypes.ASSOCIATION, new Integer(0));
        map.put(LinkTypes.AGGREGATION, new Integer(1));
        map.put(LinkTypes.COMPOSITION, new Integer(2));  
        map.put(LinkTypes.NAVIGABLE_ASSOCIATION, new Integer(3));
        map.put(LinkTypes.NAVIGABLE_AGGREGATION, new Integer(4));
        map.put(LinkTypes.NAVIGABLE_COMPOSITION, new Integer(5));
        map.put(LinkTypes.ASSOCIATION_CLASS, new Integer(6));
        
        // Index of dependence combo array button
        map.put(LinkTypes.DEPENDENCY, new Integer(0));
        map.put(LinkTypes.REALIZE, new Integer(1));
        map.put(LinkTypes.USAGE, new Integer(2));
        map.put(LinkTypes.PERMISSION, new Integer(3));
        map.put(LinkTypes.ABSTRACTION, new Integer(4)); 
        
        //Create Sqeuence Diagram link button mapping
        map.put(LinkTypes.SYNC_MESSAGE, new Integer(0));
        map.put(LinkTypes.ASYNC_MESSAGE, new Integer(1));
        map.put(LinkTypes.CREATE_MESSAGE, new Integer(2));
        map.put(LinkTypes.DESTROY_LIFELINE, new Integer(3));
    }
    
    // Workaround before Trey set name for each link button 
    public JComponentOperator getLinkButtonByIndex(Enum linkType) throws NotFoundException {
        String linkName =  LibProperties.getCurrentToolName(linkType);
        if (linkType.equals(LinkTypes.IMPLEMENTATION) ||
                linkType.equals(LinkTypes.GENERALIZATION) ||
                linkType.equals(LinkTypes.COMMENT_LINK) ||
                linkType.equals(LinkTypes.SYNC_MESSAGE) ||
                linkType.equals(LinkTypes.ASYNC_MESSAGE) ||
                linkType.equals(LinkTypes.CREATE_MESSAGE) ||
                linkType.equals(LinkTypes.DESTROY_LIFELINE)){
                
            return new JComponentOperator(this, new PaletteButtonByNameChooser(linkName), ((Integer)map.get(linkType)).intValue() ); 
        } else if (linkType.equals(LinkTypes.ASSOCIATION) ||
                linkType.equals(LinkTypes.ASSOCIATION_CLASS) ||
                linkType.equals(LinkTypes.COMPOSITION) ||
                linkType.equals(LinkTypes.NAVIGABLE_COMPOSITION) ||
                linkType.equals(LinkTypes.AGGREGATION) ||
                linkType.equals(LinkTypes.NAVIGABLE_AGGREGATION) ||
                linkType.equals(LinkTypes.NAVIGABLE_ASSOCIATION)) {
            getAssociationArrowButton().clickMouse();
            Utils.wait(500);
            waitComponentVisible(true);
            JComponentOperator associatePopupContent = new JComponentOperator(this, new ComboButtonChooser(), 0);
            return new JComponentOperator(associatePopupContent, new PaletteButtonByNameChooser(linkName), ((Integer)map.get(linkType)).intValue() );  
        } else if (linkType.equals(LinkTypes.DEPENDENCY) ||
                linkType.equals(LinkTypes.REALIZE) ||
                linkType.equals(LinkTypes.USAGE) ||
                linkType.equals(LinkTypes.PERMISSION) ||
                linkType.equals(LinkTypes.ABSTRACTION)) {
            getDependenceArrowButton().clickMouse();
            Utils.wait(500);
            waitComponentVisible(true);
            JComponentOperator dependencePopupContent = new JComponentOperator(this, new ComboButtonChooser(), 1);
            return new JComponentOperator(dependencePopupContent, new PaletteButtonByNameChooser(linkName), ((Integer)map.get(linkType)).intValue() );
        }
        Utils.log("Could not find link button");
        return null;
    }
    
    public static class LinkPaletteChooser implements ComponentChooser {
        Widget element;
       
         
        public LinkPaletteChooser(Widget element) {
            this.element=element;
        }
        
        public boolean checkComponent(Component comp) {
            if (comp instanceof ContextPalette) {
                ContextPalette cp = (ContextPalette) comp;
                ContextPaletteModel model = cp.getModel();
                if (model.getContext().toString().equals(element.toString())) {
                    Utils.log("ContextPalette found");
                    return true;
                } else 
                    return false;
            } else 
                return false;
            
        }
        
        
        public String getDescription() {
            return ("Chooser for ContextPalette");
        }
    }
    
    
    public static class  PaletteButtonByNameChooser implements ComponentChooser {
        String name;

        public PaletteButtonByNameChooser(String name) {
            this.name=name;
        }
//        This won't work until Trey provide PaletteButton.getModel() and PaletteButton.setName()
//        public boolean checkComponent(Component comp) {
//            if (comp instanceof PaletteButton ) {
//                ArrayList<ContextPaletteButtonModel> popupContents = comp.getModel.getChildren();
//                for (ContextPaletteButtonModel curDesc : popupContents) {
//                    Utils.log("palette button name= " + curDesc.getName());
//                    curDesc.getName().equals(name);
//                    return true;             
//                }     
//            } else {
//                return false;
//            }
//        }

   
        public boolean checkComponent(Component comp) {
            if (comp instanceof PaletteButton ) {
                    return true;                   
            } else {
                return false;
            }
        }

        public String getDescription() {
            return ("Chooser for PaletteButton");
        }
    }
    
     public static class  ArrowButtonChooser implements ComponentChooser {
        String toolTip;
       
        public ArrowButtonChooser() {
            super();
        }

        public ArrowButtonChooser(String toolTip) {
            super();
            this.toolTip=toolTip;
        }
        
        public boolean checkComponent(Component comp) {
            if (comp instanceof ArrowButton ) {
                Utils.log("ArrowButton found");
                return true;
            } else {
                return false;
            }
        }

        public String getDescription() {
            return ("Chooser for ArrowButton");
        }
    }
    
    
    public static class  ComboButtonChooser implements ComponentChooser {
        String toolTip;
       
        public ComboButtonChooser() {
            super();
        }

        public ComboButtonChooser(String toolTip) {
            super();
            this.toolTip=toolTip;
        }
        
        public boolean checkComponent(Component comp) {
            if (comp instanceof ComboButton ) {
                Utils.log("ComboButton found");
                return true;
            } else {
                return false;
            }
        }

        public String getDescription() {
            return ("Chooser for ArrowButton");
        }
    }   
}     
