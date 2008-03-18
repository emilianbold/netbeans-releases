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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.abe.nodes.ABEAbstractNode;

/**
 *
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public abstract class TagPanel extends ABEBaseDropPanel implements ComponentListener{
    
    private static final int X_OFFSET = 0;
    private static final int Y_OFFSET = 0;
    private static final int X_SHADOW_OFFSET = 4;
    private static final int Y_SHADOW_OFFSET = 4;
    public  static final int BOTTOM_PAD = Y_SHADOW_OFFSET+1;
    
    private ElementPanel elementPanel;
    private boolean hover;
    private String tagName;
    private boolean readonlyTag;
    
    private static final int TAG_HEIGHT = 29;
    
    private int XFUDGE = 3;
    
    protected ArrayList<Component> excludePaintComponentList = new ArrayList<Component>();
    
    
    /**
     *
     *
     */
    public TagPanel(ElementPanel elementPanel, InstanceUIContext context) {
        super(context);
        this.elementPanel=elementPanel;
        initialize();
    }
    
    /**
     *
     *
     */
    private void initialize() {
        setOpaque(false);
        setBackground(Color.WHITE);
        
        int height=InstanceDesignConstants.TAG_FONT.getSize()*2+BOTTOM_PAD;
        setPreferredSize(new Dimension(100,height));
//        setMinimumSize(new Dimension(100,height));
        initMouseListener();
        addContainerListener(new ContainerListener() {
            public void componentAdded(ContainerEvent e) {
                e.getComponent().addComponentListener(TagPanel.this);
                forceSizeRecalculate();
            }
            public void componentRemoved(ContainerEvent e) {
                e.getComponent().removeComponentListener(TagPanel.this);
                forceSizeRecalculate();
            }
        });
    }
    
    
    public void removeElement(){
        if(getElementPanel().getParent() != null)
            getElementPanel().removeElement();
    }
    
    protected void initMouseListener(){
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                mouseClickedActionHandler(e, true);
            }
            public void mouseClicked(MouseEvent e) {
                mouseClickedActionHandler(e, false);
            }
            public void mousePressed(MouseEvent e) {
                mouseClickedActionHandler(e, true);
            }
        });
    }
    
    protected void mouseClickedActionHandler(MouseEvent e, boolean handelPopupOnly){
        if(e.getClickCount() == 1){
            if(e.isPopupTrigger()){
                context.getMultiComponentActionManager().showPopupMenu(e, this);
                return;
            }
            if(handelPopupOnly)
                return;
            //the tag is selected
            if(e.isControlDown())
                context.getComponentSelectionManager().addToSelectedComponents(this);
            else
                context.getComponentSelectionManager().setSelectedComponent(this);
        }
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Accessors and mutators
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     *
     *
     */
    public ElementPanel getElementPanel() {
        return elementPanel;
    }
    
    
    /**
     *
     *
     */
    /*pkg*/ boolean isHover() {
        return hover;
    }
    
    
    /**
     *
     *
     */
    /*pkg*/ void setHover(boolean value) {
        boolean oldHover=hover;
        if (oldHover!=value) {
            hover=value;
            repaint();
        }
    }
    
    
    /**
     *
     *
     */
    public String getTagName() {
        return tagName;
    }
    
    
    /**
     *
     *
     */
    public void setTagName(String value) {
        tagName=value;
    }
    
    
    Color fillTopColor = InstanceDesignConstants.TAG_BG_NORMAL_TOP_GRADIENT_COLOR;
    Color fillBottomColor=InstanceDesignConstants.TAG_BG_NORMAL_BOTTOM_GRADIENT_COLOR;
    
    ////////////////////////////////////////////////////////////////////////////
    // Paint methods
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     *
     *
     */
    public void paintComponent(Graphics g) {
        Graphics2D g2d=(Graphics2D)g;
        
        super.paintComponent(g2d);
        
        fillTopColor = InstanceDesignConstants.TAG_BG_NORMAL_TOP_GRADIENT_COLOR;
        fillBottomColor=InstanceDesignConstants.TAG_BG_NORMAL_BOTTOM_GRADIENT_COLOR;
        
        setDrawParamsForSharedElement(g2d);
        boolean selected = false;
        //set proper colors
        if (isHover()) {
            fillTopColor=InstanceDesignConstants.DARK_BLUE;//Color.WHITE;
            fillBottomColor=InstanceDesignConstants.DARK_BLUE;//InstanceDesignConstants.XP_ORANGE;
        }else if(context.getComponentSelectionManager().isSelected(this)){
            selected = true;
        }
        
        Shape tag = getTagShape();
        Rectangle tagBounds = tag.getBounds();
        
        // Draw shadow
        g.translate(X_SHADOW_OFFSET,Y_SHADOW_OFFSET);
        g2d.setColor(new Color(240,240,240));
        g2d.fill(tag);
        
        g.translate(-1,-1);
        g2d.setColor(new Color(224,224,224));
        g2d.fill(tag);
        
        g.translate(-1,-1);
        g2d.setColor(new Color(192,192,192));
        g2d.fill(tag);
        
        // Draw main tag body
        
        
        g.translate(2,2);
        g.translate(-X_SHADOW_OFFSET,-Y_SHADOW_OFFSET);
        // fill tag
        float x1, y1, x2, y2;
        x1 = x2 = (float) tagBounds.getX();
        y2 = (float) tagBounds.getY();
        for(int i = 1; i<= getRowCount(); i++){
            y1 = y2;
            y2 = y2 + getTagHeight() - BOTTOM_PAD;
            GradientPaint fill = new GradientPaint(x1, y1, fillTopColor, x2, y2, fillBottomColor, false);
            g2d.setPaint(fill);
            g2d.fill(tag);
        }
        
        //draw row separation lines
        int lineY = (int) tagBounds.getY();
        g2d.setColor(fillBottomColor);
        Stroke stroke = new BasicStroke(2f);
        Stroke oldstroke = null;
        oldstroke = g2d.getStroke();
        g2d.setStroke(stroke);
        for(int i = 1; i< getRowCount(); i++){
            lineY = lineY + getTagHeight() - BOTTOM_PAD;
            g2d.drawLine(extremeLeftX+1, lineY, extremeRightX-1, lineY);
        }
        g2d.setStroke(oldstroke);
        
        // draw Outline
        g2d.setColor(InstanceDesignConstants.TAG_OUTLINE_COLOR);
        oldstroke = null;
        if(selected){
            oldstroke = g2d.getStroke();
            stroke = new BasicStroke(2f);
            g2d.setStroke(stroke);
            g2d.setColor(InstanceDesignConstants.XP_ORANGE);
        }
        
        g2d.draw(tag);
        
        if(selected && (oldstroke != null)){
            g2d.setStroke(oldstroke);
        }
        
        
        
        resetDrawParamsForSharedElement(g2d);
    }
    
    
    /**
     *
     *
     */
    protected int getA() {
        // Height could be zero before the component is laid out, so use
        // the preferred height (which should be the final height)
        /*int h = getHeight();
        if (h == 0)
            h = getPreferredSize().height;*/
        int h = TAG_HEIGHT;
        return (h - BOTTOM_PAD)/2;
    }
    
    
    /**
     *
     *
     */
   /* protected Shape getTagShape() {
        Graphics g=getGraphics();
        return getTagShape();
    }*/
    
    int tagNosePointOffset;
    public int getTagNosePointOffset(){
        int h = TAG_HEIGHT - BOTTOM_PAD;
        tagNosePointOffset = h/2;
        return tagNosePointOffset;
    }
    
    
    /**
     *
     *
     */
    private Shape _getTagShape(Graphics g) {
        int h = TAG_HEIGHT - BOTTOM_PAD;
        int xo = X_OFFSET;
        int yo = Y_OFFSET;
        int ym = h/2;
        int a = getA(); // ym-yo;
        int w = getAbsoluteWidth();//(int) getPreferredSize().getWidth() + getA()) - 2*xo;
        
        tagNosePointOffset = ym;
        
        /*
        Starting from the left vertical middle point and moving clockwise:
         
             /-----------------\
            /                   \
            \                   /
             \-----------------/
         
            xo,     ym
            xo+a,   yo
            w-xo-a, yo
            w-xo,   ym
            w-xo-a, h-yo
            xo+a,   h-yo
         */
        
        return new Polygon(
                new int[] { xo, xo+a, w-xo-a, w-xo, w-xo-a, xo+a },
                new int[] { ym, yo,   yo,     ym,   h-yo,   h-yo }, 6);
    }
    
    private int extremeLeftX;
    private int extremeRightX;
    private Point leftBottomPoint;
    private Point rightBottomPoint;
    private Point rightNosePoint;
    private Point leftNosePoint;
    public Shape getTagShape() {
        Rectangle rect = getChildrenAreaUnion();
        //Rectangle realRect = g.getClipBounds();
        int h = TAG_HEIGHT - BOTTOM_PAD;
        int a = getA(); // ym-yo;
        int x = rect.x;//rect.x;
        int y = rect.y - 2;
        int H = this.getHeight() - BOTTOM_PAD;
        int W = rect.width;
        tagNosePointOffset = h/2;
        /*
        Starting from the left vertical middle point and moving clockwise:
         
         (x,y)    /--------------------| (x+W, y)
  (x-a,y+h/2)    /                     |
                 \                     |
(x, y+h),(x, y+h) \                    |
                  |               \----| (x+w1, y+H-h), (x+W, y+H-h)
                  |                \
                  |                / (x+w1+a, y+H-(h/2))
          (x, y+H)|---------------/((x+w1, y+H);
         
         
         */
        
        if(getRowCount() > 1)
            W += 8;
        
        int w1 = W;
        if(getEndSlash() != null){
            w1 = getEndSlash().getBounds().x;
        }
        extremeLeftX = x;
        extremeRightX = x+W;
        leftBottomPoint = new Point(x, y+h);
        rightBottomPoint = new Point(x+w1, y+H);
        rightNosePoint = new Point(x+w1+a, y+H-(h/2));
        leftNosePoint = new Point(x-a, y+h/2);
        return new Polygon(
                new int[] { x-a  , x, x+W, x+W   , x+w1 , x+w1+a   , x+w1, x    , x  , x   },
                new int[] { y+h/2, y, y  , y+H-h , y+H-h, y+H-(h/2), y+H , y+H  , y+h, y+h }, 10);
    }
    
    private int getAbsoluteWidth(){
        int width = 0;
        int finalX = 0;
        for(Component child: getComponents()){
            int x;
            x = child.getBounds().x;
            x += child.getPreferredSize().width;
            if(x > finalX)
                finalX = x;
        }
        finalX += getA();
        return finalX;
    }
    
    private Rectangle getChildrenAreaUnion(){
        Component children[] = getComponents();
        if(children.length <= 0)
            return null;
        Rectangle current;
        current = children[1].getBounds();
        for(Component child: children){
            if(excludePaintComponentList.contains(child))// || !child.isVisible())
                continue;
            current = SwingUtilities.computeUnion(current.x, current.y, current.width,
                    current.height, child.getBounds());
        }
        return current;
    }
    
    
    //sub-class to override
    public int getInterComponentSpacing(){
        return 0;
    }
    
    //Following set of methods needed for the tag size calculation and horizontal bar display logic
    public Dimension _getPreferredSize() {
        int width = 0;
        int maxWidth = 0;
        int propsWidth = 0;
        for(Component child: this.getComponents()){
            if(!child.isVisible())
                continue;
            Dimension dim = child.getPreferredSize();
            width += dim.width + getInterComponentSpacing();
            maxWidth = maxWidth > dim.width ? maxWidth : dim.width;
            if(child instanceof ElementPropertiesPanel)
                propsWidth = dim.width;
        }
        if(getRowCount() > 1){
            width = maxWidth * StartTagPanel.NO_OF_ATTRS_PER_ROW +
                    getInterComponentSpacing() * StartTagPanel.NO_OF_ATTRS_PER_ROW * 2
                    + getA() *2 + propsWidth + 25;
        } else{
            width += maxWidth +  getA() * 2 + 20;
            width += (getElementPanel().getAXIContainer().getName().length() < 3) ?
                10 : 0;
        }
        return new Dimension(width, ((TAG_HEIGHT - BOTTOM_PAD) * getRowCount())+BOTTOM_PAD );
    }
    
    boolean recalculateRequired = true;
    Dimension myDim;
    public Dimension getPreferredSize() {
        if(recalculateRequired){
            myDim = _getPreferredSize();
            recalculateRequired = false;
        }
        return myDim;
    }
    
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
    
    
    public static int getTagHeight(){
        return TAG_HEIGHT;
    }
    
    
    //DND events
    public void drop(DropTargetDropEvent event) {
        setHover(false);
    }
    
    public void dragExit(DropTargetEvent event) {
        setHover(false);
    }
    
    public void dragOver(DropTargetDragEvent event) {
        setHover(true);
    }
    
    public void dragEnter(DropTargetDragEvent event) {
        setHover(true);
    }
    
    public ABEAbstractNode getNBNode(){
        return getElementPanel().getNBNode();
    }
    
    public AXIComponent getAXIComponent() {
        return getElementPanel().getAXIContainer();
    }
    
    public int getRowCount() {
        return 1;
    }
    
    public boolean isReadonlyTag() {
        return readonlyTag;
    }
    
    public void setReadonlyTag(boolean readonlyTag) {
        this.readonlyTag = readonlyTag;
    }
    
    private void setDrawParamsForSharedElement(Graphics2D g2d) {
        AXIContainer acon = getElementPanel().getAXIContainer();
        //ContentModel cm = acon.getContentModel();
        boolean reference = false;
        if(acon instanceof Element)
            reference = ((Element) acon).isReference();
        if(acon.isShared() || reference) {
            fillTopColor = InstanceDesignConstants.TAG_BG_SHARED_TOP_GRADIENT_COLOR;
            fillBottomColor = InstanceDesignConstants.TAG_BG_SHARED_BOTTOM_GRADIENT_COLOR;
        }
        if(acon.isReadOnly()){
            fillTopColor = InstanceDesignConstants.TAG_BG_READONLY_TOP_GRADIENT_COLOR;
            fillBottomColor = InstanceDesignConstants.TAG_BG_READONLY_BOTTOM_GRADIENT_COLOR;
        }
        
    }
    
    private void resetDrawParamsForSharedElement(Graphics2D g2d) {
    }
    
    
    public Point getLeftBottomPoint() {
        return leftBottomPoint;
    }
    
    public Point getRightBottomPoint() {
        return rightBottomPoint;
    }
    
    public Point getRightNosePoint() {
        return rightNosePoint;
    }
    
    public Point getLeftNosePoint(){
        return leftNosePoint;
    }
    
    public JLabel getEndSlash() {
        return null;
    }
    
    public void componentShown(ComponentEvent e) {
        forceSizeRecalculate();
    }
    
    public void componentResized(ComponentEvent e) {
        forceSizeRecalculate();
    }
    
    public void componentMoved(ComponentEvent e) {
        forceSizeRecalculate();
    }
    
    public void componentHidden(ComponentEvent e) {
        forceSizeRecalculate();
    }
    
    public void forceSizeRecalculate(){
        recalculateRequired = true;
    }
}
