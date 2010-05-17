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

package org.netbeans.modules.xml.xam.ui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

/**
 * A class that implements a splitter bar.  It is only intended to be used
 * in a SplitterLayout.  Because it is dervied from Panel, a JSplitterBar
 * can do anything a normal panel can do.  However, if you add any
 * components to this panel, the scroll handle will not be accessible.  In a
 * case like this, you need to explicitly add a JSplitterSpace component to the
 * JSplitterBar, guaranteeing a place for the handle to be available.
 * <p/>
 * <p>Use this code at your own risk!  MageLang Institute is not
 * responsible for any damage caused directly or indirctly through
 * use of this code.
 * <p><p>
 * <b>SOFTWARE RIGHTS</b>
 * <p/>
 * MageLang support classes, version 1.0, MageLang Institute
 * <p/>
 * We reserve no legal rights to this code--it is fully in the
 * public domain. An individual or company may do whatever
 * they wish with source code distributed with it, including
 * including the incorporation of it into commerical software.
 * <p/>
 * <p>However, this code cannot be sold as a standalone product.
 * <p/>
 * We encourage users to develop software with this code. However,
 * we do ask that credit is given to us for developing it
 * By "credit", we mean that if you use these components or
 * incorporate any source code into one of your programs
 * (commercial product, research project, or otherwise) that
 * you acknowledge this fact somewhere in the documentation,
 * research report, etc... If you like these components and have
 * developed a nice tool with the output, please mention that
 * you developed it using these components. In addition, we ask that
 * the headers remain intact in our source code. As long as these
 * guidelines are kept, we expect to continue enhancing this
 * system and expect to make other tools available as they are
 * completed.
 * <p/>
 * The MageLang Support Classes Gang:
 *
 * @author <a href="http:www.scruz.net/~thetick">Scott Stanchfield</a>, <a href=http://www.MageLang.com>MageLang Institute</a>
 * @version MageLang Support Classes 1.0, MageLang Insitute, 1997
 * @see SplitterLayout
 * @see JSplitterSpace
 *
 * @author Santhosh Kumar - santhosh@in.fiorano.com
 *
 * @author Jeri Lockhart - jeri.lockhart@sun.com
 * Modified for use in the NbColumnView widget.   Only Horizontal layout is supported.
 * When the user moves the splitter bar to the left, the column that is adjacent to the
 * left maintains its minimum size.
 * When the user moves the splitter bar to the right, the columns to the right of the
 * splitter bar, maintain their widths.
 *
 * mouseDrag() - creates a visible bar to show where the splitter can be dragged.
 * The limit of leftward dragging is the boundary of the min size of the column to the left
 * of the splitter.
 * The limit of rightward dragging is the parent container's right boundary.
 *
 * mouseRelease() - sets the bounds of the actual splitter.  Calls
 * checkOtherComponents().
 *
 * checkOtherComponenets() - For leftward drag:  Make the column to the left of
 * this splitter more  narrow.  Then, set the bounds of the columns and splitters
 * to the right of this splitter so that they are relocated by the correct
 * offset to the left, maintaining their respective widths.
 *
 * For rightward drag:  Make the column to the left of this splitter wider.
 * Then, set the bounds of the columns and splitters to the right of this splitter
 * so that they are relocated by the correct offset towards the right,
 * maintaining their respective widths.
 *
 */
public class JSplitterBar extends JPanel{
    
    static final long serialVersionUID = 1L;
    static final Cursor VERT_CURSOR = new Cursor(Cursor.N_RESIZE_CURSOR);
    static final Cursor HORIZ_CURSOR = new Cursor(Cursor.E_RESIZE_CURSOR);
    static final Cursor DEF_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    private int orientation = SplitterLayout.HORIZONTAL;
    
    private boolean alreadyDrawn = false;
    private Rectangle originalBounds = null;
    private Window wBar;
    private boolean mouseInside = false;
    
    /**
     * Creates a new SpliiterBar
     */
    public JSplitterBar(){
        addMouseMotionListener(new SplitterBarMouseMotionListener(this));
        addMouseListener(new SplitterBarMouseListener(this));
    }
    
    private void checkOtherComponents(){
        //System.out.println("checkOtherComponents start");
        Rectangle currBounds = getBounds();  // get current position
        Component comps[] = getParent().getComponents();
        
        // orientation == HORIZONTAL
        if(currBounds.x<originalBounds.x){ // moved left
            //System.out.println("checkOtherComponents moved left");
            int offset = originalBounds.x - currBounds.x;
            //System.out.println("checkOtherComnponents offset" + offset);
            for (int i = 0; i < comps.length; i++){
                if (comps[i] == this){
                    // make left column more narrow
                    assert i-1>-1:"There should be a column to the left of this splitter";
                    if (i-1 > -1){
                        Rectangle oldB = comps[i-1].getBounds();
                        //System.out.println("checkOtherComponents left col orig bounds " + oldB);
                        comps[i-1].setBounds(oldB.x, oldB.y, oldB.width-offset, oldB.height);
                        //System.out.println("checkOtherComponents make left comp narrower" + (i-1));
                    }
                    // move left  all columns and splitters to the right of this splitter
                    // set all visible
                    for (int k = i+1; k < comps.length; k++){
                        Rectangle old = comps[k].getBounds();
                        //System.out.println("checkOtherComponents " + k + " orig bounds " + old);
                        comps[k].setBounds(old.x-offset, old.y, old.width, old.height);
                        //System.out.println("checkOtherComponents move right comps to the left, set bounds "+ comps[k] + " " + comps[k].getBounds());
                        comps[k].setVisible(true);
                        
                    }
                }
            }
        } // HORIZONTAL -- end moved left
        else if(currBounds.x>originalBounds.x){ // moved right
            //System.out.println("checkOtherComponents moved right");
            int offset = currBounds.x - originalBounds.x;
            //System.out.println("checkOtherComnponents offset" + offset);
            // widen the column to the left of this splitter
            Component left = null;
            int thisIdx = -1;
            for (int i = 0; i < comps.length; i++){
                if(comps[i] == this){
                    thisIdx = i;
                    assert i>0:"There should be a column to the left of this splitter.";
                    if (i-1 > -1){
                        left = comps[i-1];
                        Rectangle old = left.getBounds();
                        left.setBounds(old.x, old.y, old.width+offset, old.height);
                        //System.out.println("checkOtherComponents set bounds "+ (i-1) + " " + left.getBounds());
                    }
                }
            }
            
            // relocate the columns and splitters to the right of this splitter
            if (thisIdx+1 < comps.length) {
                for (int j = thisIdx+1; j < comps.length; j++){
                    Rectangle old = comps[j].getBounds();
                    comps[j].setBounds(old.x+offset, old.y, old.width, old.height);
                    //System.out.println("checkOtherComponents set bounds "+ j + " " + comps[j].getBounds());
                }
            }
        } // HORIZONTAL -- moved right
    } // checkComponents()
    
    
    void mouseDrag(MouseEvent e){
        if (SplitterLayout.dragee == null) {
            SplitterLayout.dragee = this;
        } else if (SplitterLayout.dragee != this) {
            return;
        }
        // The NbColumnView mainParentPanel is in a scrollpane
        Container cp = getParent();         // this is the NbColumnView mainParentPanel
        Container viewport = cp.getParent();
        Rectangle viewRect = null;
        Rectangle columnViewRect = null;
        Component columnView = null;
//        int vBarWidth = 0;
        if (viewport instanceof JViewport){
            JScrollPane scrollPane = (JScrollPane)((JViewport)viewport).getParent();
//            vBarWidth = scrollPane.getVerticalScrollBar().getPreferredSize().width;
//            System.out.println("vBar width " + vBarWidth);
            viewRect = ((JViewport)viewport).getViewRect();
            ////System.out.println("mouseDrag: viewRect " + viewRect);  //
            columnView = ((JViewport)viewport).getParent().getParent();
            if (columnView != null) {
                columnViewRect = columnView.getBounds();
                ////System.out.println("mouseDrag: columnViewRect " +columnViewRect );
            } else {
                // We would get NPE if we continued.
                return;
            }
        } else {
            // We would get NPE if we continued.
            return;
        }
        Component c = getParent();
//        Point fl = c.getLocationOnScreen(); // location of this bar on screen
        while(c.getParent()!=null){
            c = c.getParent();
        }
        ////System.out.println("mouseDrag parent bounds " + c.getBounds()); // Main IDE window org.netbeans.core.windows.view.ui.MainWindow
        if(!alreadyDrawn && c instanceof Frame){
            originalBounds = getBounds();
            wBar = new Window((Frame)c);
            wBar.setBackground(getBackground().darker());
        }
        ////System.out.println("mouseDrag mainParentPanel cp " + cp);
//        Dimension parentDim = cp.getSize();
        Rectangle parentRect = cp.getBounds();  // this could be bigger than the scrollpane
        ////System.out.println("mouseDrag: cp parentRect " + parentRect);
        Point l = getLocationOnScreen();
        Insets insets = cp.getInsets();
        ////System.out.println("mouseDrag insets " + insets);
        parentRect.height -= insets.top+insets.bottom;
//        Rectangle r = getBounds(); // mouse event is relative to this...
        ////System.out.println("mouseDrag l " + l);
        ////System.out.println("mouseDrag e.getX " + e.getX());
        int x = l.x+e.getX();
        ////System.out.println("mouseDrag x " + x);
//        int y = l.y;
        Point viewRectLocation = viewRect.getLocation();
        ////System.out.println("mouseDrag: viewRect location " + viewRectLocation);
        SwingUtilities.convertPointToScreen(viewRectLocation, viewport);
        ////System.out.println("mouseDrag: viewRect screen location " + viewRectLocation);
        Point columnViewRectLocation = columnViewRect.getLocation();
        ////System.out.println("mouseDrag: columnViewRect location " + columnViewRectLocation);
        SwingUtilities.convertPointToScreen(columnViewRectLocation, columnView);
        ////System.out.println("mouseDrag: columnViewRect screen location " + columnViewRectLocation);
        int y = viewRectLocation.y;
        // right boundary is  the right location of the
        //     viewport or the column view minus the width of the vertical scrollbar
        // 
//        System.out.println("JSplitterBar width "+ this.getWidth());
        int rightBoundaryScreenPos = columnViewRectLocation.x + columnViewRect.width - this.getWidth();
        ////System.out.println("mouseDrag view width " +  viewRect.width);
        ////System.out.println("mouseDrag columView width "+ columnViewRect.width);
        ////System.out.println("mouseDrag: rightBoundaryScreenPos "+ rightBoundaryScreenPos);
        
        // don't let x be less than the min size boundary position of the left-adjacent column
        //  there should always be a column to the left of this bar
        int leftBoundaryScreenPos = 0;
        Component left = null;
        Component[] comps = getParent().getComponents();
        for (Component comp:comps){
            if (comp == this){
                Dimension min = left.getMinimumSize();
                Point p = left.getLocationOnScreen();
                // use the larger of the viewport left position or the neighbor's minimum size boundary
                //   (the neighbor may be scrolled to the left and partially hidden)
                ////System.out.println("mouseDrag: left neighbor's boundary " + (p.x+ min.width));
                ////System.out.println("mouseDrag: columnView left " + columnViewRectLocation.x);
                leftBoundaryScreenPos = Math.max(p.x+min.width,columnViewRectLocation.x);
                break;
            }
            left = comp;
        }
        ////System.out.println("mouseDrag leftBoundaryScreenPos " + leftBoundaryScreenPos);
        if (x<leftBoundaryScreenPos){
            x = leftBoundaryScreenPos;
            ////System.out.println("mouseDrag x=leftBoundaryScreenPos");
        }
        else if(x>rightBoundaryScreenPos){
            x = rightBoundaryScreenPos;
            ////System.out.println("mouseDrag x=rightBoundaryScreenPos");
        }
        wBar.setBounds(x, y,
                3 ,
                columnViewRect.height);
        //System.out.println("mouseDrag wBar bounds " + wBar.getBounds());
        if(!alreadyDrawn){
            wBar.setVisible(true);
            alreadyDrawn = true;
        }
    }
    
    void mouseEnter(MouseEvent e){
        if(SplitterLayout.dragee!=null) return;
        setCursor(HORIZ_CURSOR);
        mouseInside = true;
        invalidate();
        validate();
        repaint();
    }
    
    void mouseExit(MouseEvent e){
        if(SplitterLayout.dragee!=null) return;
        setCursor(DEF_CURSOR);
        mouseInside = false;
        invalidate();
        validate();
        repaint();
    }
    
    void mouseRelease(MouseEvent e){
        if(alreadyDrawn){
            if(SplitterLayout.dragee!=this) return;
            SplitterLayout.dragee = null;
            Point p = wBar.getLocationOnScreen();
            SwingUtilities.convertPointFromScreen(p, getParent());
            wBar.setVisible(false);
            wBar.dispose();
            wBar = null;
            alreadyDrawn = false;
            Rectangle r = getBounds(); // mouse event is relative to this...
            r.x += e.getX();
//			//System.out.println("mouseReleased converted point "+ p);
//			//System.out.println("mouseRelease bounds of this spliltter " + r);
            setLocation(p.x, r.y);
            setCursor(DEF_CURSOR);
            
            // check to see if we need to move other splitters and hide other
            // components that are controlled by the layout
            // First -- find what component this one is
            
            checkOtherComponents();
            mouseInside = false;
            invalidate();
            getParent().validate();
            SplitterLayout.dragee = null;
        }
    }
    
    /**
     * Paints the image of a JSplitterBar.  If nothing was added to
     * the JSplitterBar, this image will only be a thin, 3D raised line that
     * will act like a handle for moving the JSplitterBar.
     * If other components were added the JSplitterBar, the thin 3D raised
     * line will onlty appear where JSplitterSpace components were added.
     */
    public void paint(Graphics g){
        super.paint(g);
        g.setColor(getBackground());
//        if(mouseInside)
//            g.setColor(Color.yellow);
//        else
//            g.setColor(Colors.lightSkyBlue3);
        Component c[] = getComponents();
        if(c!=null && c.length>0)
            for(int i = 0; i<c.length; i++){
            if(c[i] instanceof JSplitterSpace){
                // only draw boxes where JSplitterSpace components appear
                Rectangle r = c[i].getBounds();
                g.fill3DRect(r.x+r.width/2-1, r.y+2, 3, r.y+r.height-5, true);
            }
            } else{
            Rectangle r = getBounds();
            g.fill3DRect(r.width/2-1, 2, 3, r.height-5, true);
            }
    }
    
    
    
    /**
     * Called by AWT to update the image produced by the JSplitterBar
     */
    public void update(Graphics g){
        paint(g);
    }
}
