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

package org.netbeans.modules.xml.schema.abe;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.schema.abe.nodes.ABEAbstractNode;
import org.netbeans.modules.xml.schema.abe.nodes.AnyElementNode;
import org.netbeans.modules.xml.schema.abe.nodes.ElementNode;

public class ElementPanel extends ABEBaseDropPanel{
    private static final long serialVersionUID = 7526472295622776147L;
    
    public ElementPanel(InstanceUIContext context,
            AXIContainer element, ContainerPanel parentCompositorPanel) {
        super(context);
        this.parentCompositorPanel=parentCompositorPanel;
        this.axiContainer=element;
        //setBorder(new LineBorder(Color.RED));
        initialize();
        
        _setName(element.getName());
        
        addElementListener();
        
        makeNBNode();
    }
    
    
    private void initialize() {
        initButton();
        setLayout(new BorderLayout());
        setOpaque(false);
        
        startTag = getNewStartTagPanel(this, context);
        add(startTag,BorderLayout.NORTH);
        
        //dont show expand button if not needed
        if(axiContainer.getCompositor() == null){
            expandButton.setVisible(false);
        }
    }
    
    private void addElementListener(){
        axiContainer.addPropertyChangeListener(new ModelEventMediator(this, axiContainer){
            public void _propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(Compositor.PROP_COMPOSITOR)) {
                        //a compositor event
                        if((evt.getOldValue() == null) && (evt.getNewValue() != null)){
                            //new Compositor added
                            addCompositor((Compositor) evt.getNewValue());
                        }else if((evt.getNewValue() == null) && (evt.getOldValue() != null)){
                            //old Compositor removed
                            removeCompositor((Compositor) evt.getOldValue());
                        }
                } else if(evt.getPropertyName().equals(
                        org.netbeans.modules.xml.axi.Element.PROP_TYPE)){
                    startTag.updateAttributes();
                }
            }
        });
    }
    
    private void initButton(){
        expandButton = new ExpandCollapseButton("+");
        expandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleExpandOrCollapse();
            }
        });
    }
    
    
    private void handleExpandOrCollapse(){
        if(!expandButton.isExpanded()){
            //expand
            expandChild();
        }else{
            //collapse
            collapseChild();
        }
    }
    
    public void expandChild(){
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                if(expandButton.isExpanded())
                    expandButton.setText("-");
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                try{
                    if(fadeinPanel != null){
                        //children already added just show
                        fadeinPanel.setVisible(true);
                    }else{
                        //children not added create them and then show
                        createChild(null);
                        setExpanded(true);
                    }
                }finally{
                    //always reset back
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                revalidate();
                repaint();
            }
        });
    }
    
    public void collapseChild(){
        if(fadeinPanel != null){
            //children already added just hide
            fadeinPanel.setVisible(false);
            revalidate();
            repaint();
        }
    }
    
    private void createChild(Compositor compositor) {
        if(compositor != null) {
            CompositorPanel child = new CompositorPanel(getUIContext(),
                    compositor, this);
            append(child);
            return;
        }
        //if no compositor specified, add all from children list
        for(AXIComponent axiComp: axiContainer.getChildren()) {
            if(axiComp instanceof Compositor){
                compositor = (Compositor) axiComp;
                CompositorPanel child = new CompositorPanel(getUIContext(),
                        compositor, this);
                append(child);
            }
        }
    }
    
    Component lastComponent = null;
    SpringLayout childCompositorPanelLayout;
    public void append(ContainerPanel child) {
        if (childCompositorPanel==null) {
            fadeinPanel = new JPanel();
            expandButton.setWatchForComponent(fadeinPanel);
            fadeinPanel.setOpaque(false);
            fadeinPanel.setLayout(new BorderLayout());
            //add a horizontal strut for compensating the expand collapse button
            fadeinPanel.add(Box.createHorizontalStrut((int)
            expandButton.getPreferredSize().getWidth()), BorderLayout.WEST);
            childCompositorPanel = new AutoSizingPanel(context);
            childCompositorPanel.setVerticalScaling(true);
            childCompositorPanel.setOpaque(false);
            childCompositorPanelLayout = new SpringLayout();
            childCompositorPanel.setLayout(childCompositorPanelLayout);
            fadeinPanel.add(childCompositorPanel, BorderLayout.CENTER);
            
            childCompositorPanel.add(child);
            childCompositorPanelLayout.putConstraint(SpringLayout.NORTH, child, 0,
                    SpringLayout.NORTH, childCompositorPanel);
            childCompositorPanelLayout.putConstraint(SpringLayout.WEST, child, 0,
                    SpringLayout.WEST, childCompositorPanel);
            
            add(fadeinPanel,BorderLayout.CENTER);
            lastComponent = child;
        }else{
            childCompositorPanel.add(child);
            childCompositorPanelLayout.putConstraint(SpringLayout.NORTH, child, 0,
                    SpringLayout.SOUTH, lastComponent);
            childCompositorPanelLayout.putConstraint(SpringLayout.WEST, child, 0,
                    SpringLayout.WEST, childCompositorPanel);
            lastComponent = child;
        }
    }
    
    private void removeChild(CompositorPanel component){
        if(childCompositorPanel != null){
            childCompositorPanel.remove(component);
            if(childCompositorPanel.getComponents().length == 0) {
                fadeinPanel.removeAll();
                remove(fadeinPanel);
                fadeinPanel = null;
                childCompositorPanel = null;
                expandButton.setVisible(false);
            }
            revalidate();
            repaint();
        }
    }
    
    
    
    private void addCompositor(Compositor compositor) {
        createChild(compositor);
        expandButton.setVisible(true);
        setExpanded(false);
    }
    
    private void removeCompositor(Compositor compositor) {
        if(childCompositorPanel == null)
            return;
        for(Component comp: childCompositorPanel.getComponents()){
            CompositorPanel cp = (CompositorPanel) comp;
            if(!cp.getAXIParent().getPeer().isInDocumentModel() ||
                cp.getAXIParent().getPeer() == compositor.getPeer()){
                removeChild(cp);
            }
        }
    }
    
    
    public void removeElement() {
        AXIContainer axiContainer = getAXIContainer();
        AXIModel model = axiContainer.getModel();
        if(model != null){
            model.startTransaction();
            try{
                getAXIContainer().getParent().removeChild(getAXIContainer());
            }finally{
                model.endTransaction();
            }
        }
    }
    
    public boolean isExpanded() {
        return fadeinPanel !=null ? fadeinPanel.isVisible() : false;
    }
    
    
    public void setExpanded(boolean value) {
        if (fadeinPanel != null) {
            fadeinPanel.setVisible(value);
        }
    }
    
    public String getName() {
        return name;
    }
    
    
    public void setName(String value) {
        _setName(value);
    }
    
    
    private void _setName(String value) {
        name=value;
        repaint();
    }
    
    
    public AXIContainer getAXIContainer() {
        return axiContainer;
    }
    
    public String getAnnotation() {
        return annotation;
    }
    
    public void setAnnotation(String value) {
        _setAnnotation(value);
    }
    
    private void _setAnnotation(String value) {
        annotation=value;
    }
    
    public TagPanel getStartTagPanel() {
        return startTag;
    }
    
    
    TweenerPanel getTweenerPanel() {
        return tweenerPanel;
    }
    
    
    //Following set of methods needed for the tag size calculation and horizontal bar display logic
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
    
    public Dimension getMinimumSize() {
        int width = 0;
        int height = 0;
        for(Component child: this.getComponents()){
            if(!child.isVisible())
                continue;
            Dimension dim = child.getPreferredSize();
            height += dim.height;// + getInterComponentSpacing();
            int thisW = dim.width ;
            width = width < thisW ? thisW : width;
        }
        if( (fadeinPanel != null) && fadeinPanel.isVisible() ){
            height +=4;
            width += 10;
        }
        return new Dimension(width, height);
    }
    
    
    List<String> getAttributes() {
        return attributes;
    }
    
    
    void addAttribute(String value) {
        attributes.add(value);
        repaint();
    }
    
    public void paintComponent(Graphics g){
        final Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
//        SwingUtilities.invokeLater(new Runnable(){
//            public void run() {
        if(context.getComponentSelectionManager().getSelectedComponentList().contains(startTag))
            drawBoundingBox(g2d);
//            }
//        });
        
    }
    
    public InstanceUIContext getUIContext() {
        return context;
    }
    
    void showNameEditor(boolean firstTime) {
        startTag.showTagNameEditor(firstTime);
    }
    
    protected void makeNBNode() {
        if(getAXIContainer() instanceof AnyElement)
            elementNode = new AnyElementNode((AnyElement) getAXIContainer(), context);
        else
            elementNode = new ElementNode((AbstractElement) getAXIContainer(), context);
        /*if(getAXIContainer().isReadOnly(getAXIContainer().getOriginal().getModel()))
            ((ABEAbstractNode)elementNode).setReadOnly(true);*/
    }
    
    public ABEAbstractNode getNBNode() {
        return elementNode;
    }
    
    protected StartTagPanel getNewStartTagPanel(ElementPanel elementPanel, InstanceUIContext context) {
        return new StartTagPanel(elementPanel, context);
    }
    
    
    ExpandCollapseButton getExpandButton() {
        return expandButton;
    }
    
    private void drawBoundingBox(Graphics2D g2d) {
        if( (fadeinPanel == null) || !fadeinPanel.isVisible() )
            return;
        if( (childCompositorPanel == null) ||
                (childCompositorPanel.getComponents().length < 1))
            return;
        //this refreshes the point detail of the tag
        startTag.getTagShape();
        //Point rightBottomPoint = startTag.getRightBottomPoint();
        Point leftBottomPoint = startTag.getLeftNosePoint();
        Point rightNosePoint = startTag.getRightNosePoint();
        
        Stroke oldStroke = g2d.getStroke();
        Color oldColor = g2d.getColor();
        g2d.setColor(InstanceDesignConstants.XP_ORANGE);
        g2d.setStroke(new BasicStroke(1));
        
        Rectangle rect = fadeinPanel.getBounds();
        Rectangle myrect = getBounds();
        
        int x1, y1, x2, y2;
        //left Top
        x1 = leftBottomPoint.x;
        y1 = leftBottomPoint.y;
        //left Bottom
        x2 = leftBottomPoint.x;
        y2 = y1 + myrect.height - leftBottomPoint.y - 2;
        /*y2 = y1 + (rect.y > leftBottomPoint.y ? rect.y - leftBottomPoint.y
                : leftBottomPoint.y - rect.y) + rect.height;*/
        g2d.drawLine(x1, y1, x2, y2);
        
        //left Bottom
        x1 = x2; y1 = y2;
        //right Bottom
        x2 = (rect.x + rect.width) > rightNosePoint.x ? (rect.x + rect.width): rightNosePoint.x + 5;
        y2 = y1;
        g2d.drawLine(x1, y1, x2, y2);
        
        //right Bottom
        x1 = x2; y1 = y2;
        //right Top
        x2 = x1;
        y2 = rightNosePoint.y;
        g2d.drawLine(x1-1, y1, x2-1, y2);
        
        //right Top
        x1 = x2; y1 = y2;
        //end Point
        x2 = rightNosePoint.x; y2 = rightNosePoint.y;
        g2d.drawLine(x1, y1, x2, y2);
        
        g2d.setStroke(oldStroke);
        g2d.setColor(oldColor);
    }
    
    
    
    
    public ABEBaseDropPanel getUIComponentFor(AXIComponent axiComponent) {
        ABEBaseDropPanel comp = super.getUIComponentFor(axiComponent);
        if(comp == this){
            //startTag panel is everything so just return that.
            return startTag;
        }
        return null;
    }
    
    public ABEBaseDropPanel getChildUIComponentFor(AXIComponent axiComponent){
        //search myself
        ABEBaseDropPanel result = null;
        //search for attribute
        if(axiComponent instanceof AbstractAttribute){
            //pass on the call to start tag panel
            result = startTag.getChildUIComponentFor(axiComponent);
        }else{
            //expand before analysing the content
            expandChild();
            //must be element or seq
            if(childCompositorPanel == null)
                return null;
            
            expandButton.setText("-");
            for(Component comp : childCompositorPanel.getComponents()){
                if(comp instanceof CompositorPanel){
                    
                    result = ((ABEBaseDropPanel)comp).getUIComponentFor(axiComponent);
                    if(result != null)
                        break;
                }
            }
        }
        return result;
    }
    
    public AXIComponent getAXIComponent() {
        return getAXIContainer();
    }
    
    public void accept(UIVisitor visitor) {
        visitor.visit(this);
    }
    
    public ContainerPanel getParentContainerPanel(){
        return this.parentCompositorPanel;
    }
    
////////////////////////////////////////////////////////////////////////////
// Instance members
////////////////////////////////////////////////////////////////////////////
    
    private ContainerPanel parentCompositorPanel;
    private AutoSizingPanel childCompositorPanel;
    private JPanel  fadeinPanel;
    private AXIContainer axiContainer;
    private String name;
    private String annotation;
    private StartTagPanel startTag;
    private JPanel startTagPanel;
    private TweenerPanel tweenerPanel;
    private List<String> attributes=new ArrayList<String>();
    private ExpandCollapseButton expandButton;
    private ABEAbstractNode elementNode;
}
