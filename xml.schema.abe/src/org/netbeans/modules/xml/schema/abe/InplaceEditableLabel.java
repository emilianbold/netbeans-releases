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
 * InplaceEditableLabel.java
 *
 * Created on June 9, 2006, 3:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author girix
 */
public class InplaceEditableLabel extends JLabel{
    private static final long serialVersionUID = 7526472295622776147L;
    boolean editMode = false;
    public static final String PROPERTY_MODE_CHANGE = "PROPERTY_MODE_CHANGE";
    public enum Mode{
        EDIT, NORMAL
    };
    boolean mouseOverMe = false;
    boolean altKeyPressed = false;
    private Color mouseOverColor;
    private JComponent editorComponent;
    JPanel glass;
    //NBGlassPaneAccessSupport editorSupport;
    /** Creates a new instance of InplaceEditableLabel */
    public InplaceEditableLabel() {
        super();
        setForeground(Color.BLUE);
        setMouseOverColor(getForeground());
        setToolTipText(NbBundle.getMessage(InplaceEditableLabel.class,
                "TTP_DEFAULT_INPLACE_EDIT"));
        initialize();
    }
    
    public InplaceEditableLabel(String str){
        this();
        super.setText(str);
    }
    
    public JScrollPane getScrollPane() {
        return ((ABEBaseDropPanel) this.getParent()).context.getInstanceDesignerScrollPane();
    }
    
    void initialize(){
        addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                //add logic to handle edits
                if( ( (e.isShiftDown() && (e.getClickCount() == 1) ) ||
                        (e.getClickCount() > 1)) ){
                    //setEditMode
                    showEditor();
                }else if(drewLine){
                    //call the ctrl click handler
                    if(ctrlClickHandler != null)
                        ctrlClickHandler.handleCtrlClick();
                }else{
                    InplaceEditableLabel.this.getParent().dispatchEvent(e);
                }
            }
            
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                /*mouseOverMe = true;
                if(e.isControlDown()){
                    altKeyPressed = true;
                    //requestFocusInWindow();
                }*/
                repaint();
                InplaceEditableLabel.this.getParent().dispatchEvent(e);
            }
            
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                /*mouseOverMe = false;
                altKeyPressed = false;
                repaint();*/
                InplaceEditableLabel.this.getParent().dispatchEvent(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                if(!drewLine)
                    InplaceEditableLabel.this.getParent().dispatchEvent(e);
            }
            
            public void mousePressed(MouseEvent e) {
                if(!drewLine)
                    InplaceEditableLabel.this.getParent().dispatchEvent(e);
            }
            
        });
    }
    
    boolean drewLine = false;
    protected void paintComponent(Graphics g) {
        drewLine = false;
        if(mouseOverMe && altKeyPressed && (ctrlClickHandler != null) ){
            //draw diff color and underline
            Color origC = getForeground();
            //Font font = getFont();
            //Font font = origF.deriveFont((origF.getStyle() | java.awt.Font.PLAIN), origF.getSize()+2);
            Font font = new Font(getFont().getFontName(), Font.BOLD, getFont().getSize() + 2);
            setForeground(getMouseOverColor());
            //g.setFont(new Font(origF.getName(), Font.BOLD, origF.getSize()));
            Rectangle bounds = g.getClipBounds();
            super.paintComponent(g);
            g.setColor(getMouseOverColor());
            int width = SwingUtilities.computeStringWidth(getFontMetrics(getFont()), getText());
            g.drawLine(bounds.x, bounds.y + bounds.height -1,
                    bounds.x + width,  bounds.y + bounds.height -1);
            setForeground(origC);
            setFont(font);
            drewLine = true;
        }else{
            super.paintComponent(g);
        }
    }
    
    public Color getMouseOverColor() {
        return mouseOverColor;
    }
    
    public void setMouseOverColor(Color mouseOverColor) {
        this.mouseOverColor = mouseOverColor;
    }
    
    public void setText(String str){
        super.setText(str);
        Font font = getFont();
        if(font != null){
            FontMetrics fm = getFontMetrics(font);
            int width = SwingUtilities.computeStringWidth(fm, str);
            setPreferredSize(new Dimension(width, getPreferredSize().height));
        }
        if(editMode){
            //this case will happen if the user selected something in edit mode
            hideEditor();
        }
    }
    
    public void showEditor(){
        firePropertyChange(PROPERTY_MODE_CHANGE, Mode.NORMAL, Mode.EDIT);
        if((editorComponent == null) && (editInfoString == null))
            return;
        
        editMode = true;
        
        //editorSupport = new NBGlassPaneAccessSupport();
        glass = NBGlassPaneAccessSupport.getNBGlassPane(this);
        if(glass == null)
            return;
        if(editorComponent != null){
            Rectangle rect = SwingUtilities.convertRectangle(this.getParent(),
                    this.getBounds(), glass);
            glass.add(editorComponent);
            rect.width = this.getMinimumSize().width * wMagFactor + 5;
            rect.height += 7;
            editorComponent.setBounds(rect);
        }
        addInfoLabel();
        glass.setVisible(true);
        
        
        if(editorComponent != null){
            editorComponent.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    super.keyTyped(e);
                    if(e.getKeyChar() == e.VK_ESCAPE)
                        hideEditor();
                    if(e.getKeyChar() == e.VK_ENTER){
                        if(validInput())
                            hideEditor();
                    }
                }
            });
            editorComponent.addFocusListener(new FocusListener(){
                public void focusLost(FocusEvent e) {
                    if(getParent() instanceof ABEBaseDropPanel){
                        //if focus is lost while the DnD is not complete, ignore hide
                        ABEBaseDropPanel acp = (ABEBaseDropPanel) getParent();
                        if(acp.context.isUserInducedEventMode())
                            return;
                    }
                    hideEditor();
                }
                public void  focusGained(FocusEvent e) {
                }
            });
            
            this.addHierarchyListener(new HierarchyListener() {
                public void hierarchyChanged(HierarchyEvent e) {
                    //remove when the inplace label is taken off
                    try{
                        hideEditor();
                    }catch (Exception ex){
                        //ignore this
                    }
                }
            });
            //dont let the user scroll when the edit mode is on
            getScrollPane().setWheelScrollingEnabled(false);
            editorComponent.requestFocusInWindow();
        }
    }
    
    
    public void hideEditor(){
        editMode = false;
        //restore scroll mode.
        getScrollPane().setWheelScrollingEnabled(true);
        NBGlassPaneAccessSupport.disposeNBGlassPane();
        //refresh all selected components again for focus change.
        ((ABEBaseDropPanel) this.getParent()).context.
                getComponentSelectionManager().refreshFocus();
    }
    
    public void setInlineEditorComponent(JComponent editorComponent){
        this.editorComponent = editorComponent;
    }
    
    
    public Dimension getMaximumSize(){
        return super.getMinimumSize();
    }
    
    public Dimension getMinimumSize(){
        return super.getPreferredSize();
    }
    
    public Dimension getPreferredSize(){
        return super.getMinimumSize();
    }
    
    String editInfoString = null;
    InstanceUIContext context = null;
    public void setEditInfoText(String editInfoString, InstanceUIContext context){
        this.editInfoString = editInfoString;
        this.context = context;
    }
    
    private void addInfoLabel() {
        if(editInfoString == null)
            return;
        JLabel infoLabel = new JLabel(UIUtilities.getImageIcon("bulb.png"), SwingConstants.LEFT);
        infoLabel.setText(editInfoString);
        Component panel = this.getParent();
        Rectangle rect = panel.getBounds();
        rect = SwingUtilities.convertRectangle(panel.getParent(), rect, glass);
        rect.y -= 20;
        Dimension dim = infoLabel.getPreferredSize();
        rect.width = dim.width;
        rect.height = dim.height;
        glass.add(infoLabel);
        infoLabel.setBackground(new Color(255,255,220));
        infoLabel.setOpaque(true);
        infoLabel.setBounds(rect);
    }
    
    int wMagFactor = 2;
    public void setWidthMagnificationFactor(int factor){
        this.wMagFactor = factor;
    }
    
    InputValidator inputValidator;
    String errorMessage;
    public void setInputValidator(InputValidator iv, String errorMessage){
        this.inputValidator = iv;
        this.errorMessage = errorMessage;
    }
    
    private boolean validInput(){
        if(editorComponent instanceof JTextField){
            String str = ((JTextField)editorComponent).getText();
            if( (this.inputValidator != null) && (str != null) ){
                if(!this.inputValidator.isStringValid(str)){
                    UIUtilities.showErrorMessageFor(errorMessage,
                            ((ABEBaseDropPanel) this.getParent()).context, glass, editorComponent);
                    return false;
                }else{
                    UIUtilities.hideGlassMessage(false);
                }
            }
        }
        return true;
    }
    
    CtrlClickHandler ctrlClickHandler;
    public void addCtrlClickHandler(CtrlClickHandler ctrlClickHandler){
        this.ctrlClickHandler = ctrlClickHandler;
    }
    
    public interface CtrlClickHandler{
        public void handleCtrlClick();
    }
}
