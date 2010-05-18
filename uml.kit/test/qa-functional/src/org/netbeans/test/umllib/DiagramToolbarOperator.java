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
 * UMLDiagramToolbarOperator.java
 *
 */

package org.netbeans.test.umllib;

import java.awt.Component;
import java.awt.Container;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.util.Dumper;
import org.netbeans.test.umllib.exceptions.NotFoundException;

/**
 * This class handles all toolbars now. 
 * Later them must be splitted for palettes
 * @author Alexei Mokeev
 * @see qa.uml.util.LibProperties
 */
public class DiagramToolbarOperator {
    private Component mSource = null;
     
    public static final String PRINT_PREVIEW_DIAGRAM_TOOL = "<html>Print Preview the active diagram (Ctrl+Shift+Z)</html>";
    public static final String EXPORT_AS_IMAGE_TOOL = "<html>Export as Image (Ctrl+Shift+X)</html>";
    public static final String SELECT_TOOL = "<html>Select (Ctrl+Shift+S)</html>";
    public static final String PAN_TOOL = "<html>Pan (Ctrl+Alt+Shift+P)</html>";
    public static final String ZOOM_WITH_MARQUEE_TOOL = "<html>Zoom with Marquee (Ctrl+Alt+Shift+Z)</html>";
    public static final String ZOOM_INTERACTIVELY_TOOL = "<html>Zoom Interactively (Ctrl+Alt+Shift+I)</html>";
    public static final String NAVIGATE_LINK_TOOL = "<html>Navigate Link (Ctrl+Alt+Shift+L)</html>";
    public static final String OVERVIEW_WINDOW_TOOL = "<html>Overview Window (F8)</html>";
    public static final String DIAGRAM_SYNCHRONIZATION_TOOL = "<html>Diagram Synchronization (Ctrl+Alt+Shift+R)</html>";
    public static final String SHOW_FRIENDLY_NAMES_TOOL = "<html>Show Friendly Names</html>";
    public static final String RELATIONSHIP_DISCOVERY_TOOL = "<html>Relationship Discovery (Ctrl+Alt+Shift+H)</html>";
    public static final String FIT_TO_WINDOW_TOOL = "<html>Fit to Window (Ctrl+Shift+F)</html>";
    public static final String ZOOM_IN_TOOL = "<html>Zoom In (Alt+)</html>";
    public static final String ZOOM_OUT_TOOL = "<html>Zoom Out (Alt-)</html>";
    public static final String MOVE_FORWARD_TOOL = "<html>Move Forward (Ctrl+Alt+Shift+U)</html>";
    public static final String MOVE_TO_FRONT_TOOL = "<html>Move to Front (Ctrl+Alt+Shift+F)</html>";
    public static final String MOVE_BACKWARD_TOOL = "<html>Move Backward (Ctrl+Alt+Shift+D)</html>";
    public static final String MOVE_TO_BACK_TOOL = "<html>Move to Back (Ctrl+Alt+Shift+B)</html>";
    public static final String HIERARCHICAL_LAYOUT_TOOL = "<html>Hierarchical Layout (Ctrl+Shift+K)</html>";
    public static final String ORTHOGONAL_LAYOUT_TOOL = "<html>Orthogonal Layout (Ctrl+Shift+B)</html>";
    public static final String SYMMETRIC_LAYOUT_TOOL = "<html>Symmetric Layout (Ctrl+Shift+Y)</html>";
    public static final String SEQUENCE_LAYOUT_TOOL = "<html>Sequence Diagram Layout</html>";
    public static final String INCREMENTAL_LAYOUT_TOOL = "<html>Incremental Layout (Ctrl+Shift+I)</html>";
    
    private String defaultToolTooltip = SELECT_TOOL;

     /**
     * Creates a new instance of DiagramToolbarOperator
     * @param diagramOperator 
     */
    public DiagramToolbarOperator(DiagramOperator diagramOperator) {
        this.mSource = diagramOperator.getSource();
    }
    
    /**
     * 
     * @param source 
     */
    public DiagramToolbarOperator(Component source) {
        this.mSource = source;
    }
    
    /**
     * 
     * @param buttonTooltip 
     * @throws qa.uml.exceptions.NotFoundException 
     */
    public void selectTool(String buttonTooltip) throws NotFoundException{
        //new MouseRobotDriver(new Timeout("",10)).clickMouse(btn, btn.getCenterXForClick(), btn.getCenterYForClick(), 1, InputEvent.BUTTON1_MASK, 0, new Timeout("",10));
        getAnyButtonByTooltip(buttonTooltip).doClick();
        try{Thread.sleep(100);}catch(Exception ex){}
    }
    /**
     * 
     * @param buttonTooltip 
     * @throws qa.uml.exceptions.NotFoundException 
     */
    public void selectToolNoBlock(String buttonTooltip) throws NotFoundException{
        getAnyButtonByTooltip(buttonTooltip).pushNoBlock();
        //new MouseRobotDriver(new Timeout("",10)).clickMouse(btn, btn.getCenterXForClick(), btn.getCenterYForClick(), 1, InputEvent.BUTTON1_MASK, 0, new Timeout("",10));
        try{Thread.sleep(100);}catch(Exception ex){}
    }
    
    public AbstractButtonOperator getAnyButtonByTooltip(String buttonTooltip) throws NotFoundException{
        ToolbarButtonChooser tch=new ToolbarButtonChooser(buttonTooltip);
        AbstractButton button = AbstractButtonOperator.findAbstractButton((Container)mSource,tch );
        if(button == null) {
            StringWriter sr=new StringWriter();
            PrintWriter pr=new PrintWriter(sr);
            Dumper.dumpComponent(mSource,pr);
            pr.flush();
            throw new NotFoundException(tch.tt+"Toolbar button \"" + buttonTooltip + "\" was not found on toolbar\n"+sr.getBuffer().toString());
        }
        return new AbstractButtonOperator(button);
    }
    /**
     * default tool selection/clicking, usually "select tool"
     */
    public void selectDefault() {
            selectTool(defaultToolTooltip);
    }
    
    /**
     * 
     * @param buttonTooltip 
     * @throws qa.uml.exceptions.NotFoundException 
     * @return 
     */
     public JButton getButtonByTooltip(String buttonTooltip) throws NotFoundException{
        JButton button = JButtonOperator.findJButton((Container)mSource, new ToolbarButtonChooser(buttonTooltip));
        if(button == null) {
            throw new NotFoundException("Toolbar button \"" + buttonTooltip + "\" was not found on toolbar");
        }
        return button;
    }

    /**
     * 
     * @param buttonTooltip 
     * @throws qa.uml.exceptions.NotFoundException 
     * @return 
     */
     public JToggleButton getToggleButtonByTooltip(String buttonTooltip) throws NotFoundException{
        JToggleButton button = JToggleButtonOperator.findJToggleButton((Container)mSource, new ToolbarToggleButtonChooser(buttonTooltip));
        if(button == null) {
            throw new NotFoundException("Toolbar button \"" + buttonTooltip + "\" was not found on toolbar");
        }
        return button;
    }

    class ToolbarToggleButtonChooser implements ComponentChooser {
        PrintStream p = null;
        
        /**
         * 
         * @param str 
         */
        ToolbarToggleButtonChooser( String str ) {
            myStr = str;
        }
        
        /**
         * 
         * @param arg0 
         * @return 
         */
        public boolean checkComponent( java.awt.Component arg0 ) {
            if (arg0 instanceof JToggleButton) {
                
                JToggleButton button = (JToggleButton) arg0;
                String tooltip = button.getToolTipText( );
                if (( tooltip != null ) && tooltip.startsWith( myStr )) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * 
         * @return 
         */
        public String getDescription( ) {
            return "Chooser for ToggleButton on diagram pane";
        }
        
        private String myStr;
    }

    class ToolbarButtonChooser implements ComponentChooser {
        PrintStream p = null;
        
         public String tt="";
       
        /**
         * 
         * @param str 
         */
        ToolbarButtonChooser( String str ) {
            myStr = str;
            tt="";
        }
        
        /**
         * 
         * @param arg0 
         * @return 
         */
        public boolean checkComponent( java.awt.Component arg0 ) {
            if (arg0 instanceof AbstractButton) {
                
                AbstractButton button = (AbstractButton) arg0;
                String tooltip = button.getToolTipText( );
                tt+="["+tooltip+"];\n";
                if (( tooltip != null ) && tooltip.startsWith( myStr )) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * 
         * @return 
         */
        public String getDescription( ) {
            return "Chooser for Button on diagram pane";
        }
        
        private String myStr;
    }
    
}
