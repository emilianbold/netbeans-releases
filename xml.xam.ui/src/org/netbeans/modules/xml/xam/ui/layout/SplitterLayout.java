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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

/**
 * SplitterLayout is a layout manager that will layout a container holding
 * other components and SplitterBars.
 *
 * <p>Each component added to a container to be laid out using SplitterLayout
 * must provide a String containing a "weight" for the component.  This
 * weight will be used to determine the initial spacing of all components
 * being laid out.  The weight numbers are arbitrary integers.  The
 * amount of space initially allocated for a component is
 * <pre>
 * (wc/wt) * (size-insets-splitterSize)
 * </pre>
 * <p>where
 * <dl>
 * <dt>wc
 * <dd>the weight number for the component
 * <dt>wt
 * <dd>the total weight of all visible components in the container
 * <dt>size
 * <dd>the space free to display the components
 * <dt>insets
 * <dd>space used by insets in the container
 * <dt>splitterSize
 * <dd>amount of space needed to display SplitterBars
 * </dl>
 *
 * <p>If the container being laid out holds no SplitterBars, SplitterLayout
 * acts like a relational-weight layout manager.  All components are always
 * laid out based on their proportionate weights.
 *
 * <p>If the container being laid out holds some SplitterBars, SplitterLayout
 * will initially size all non JSplitterBar components based on their weights.
 * Any succesive layouts are computed strictly on the locations of the
 * SplitterBars.
 *
 * <p>SplitterLayout can be oriented Horizontally or Vertically.  Any SpliterBars
 * placed in the container will automatically be oriented.
 *
 * <p>If a JSplitterBar has been modified (adding components to it) you will
 * need to add JSplitterSpace components to it.  See JSplitterBar for more
 * details.
 *
 * <p><b>Known Problems</b>:
 * <ul>
 * <li>If there are any SplitterBars contained in the container,
 * it is best to have them between <u>every</u> non-JSplitterBar.
 * Otherwise, once SplitterBars are moved, some components will
 * use their proportional size while others will use the
 * JSplitterBar positions.  (Non-Splitterbars will check the next
 * component to see if it's a JSplitterBar.  If it's not, it uses
 * its proportional size.)  This may eventually be changed...
 * <li>Results of adding new SplitterBars to an existing (and user-
 * interacted) SplitterLayout-laid container might be a bit
 * unpredictable.  The safest way to ensure the container is laid
 * out correctly would be to explicitly set all pre-existing
 * JSplitterBar positions to (0,0).  This will cause the relational
 * layout algorithm to take effect.
 * </ul>
 *
 * <p>Use this code at your own risk!  MageLang Institute is not
 * responsible for any damage caused directly or indirctly through
 * use of this code.
 * <p><p>
 * <b>SOFTWARE RIGHTS</b>
 * <p>
 * MageLang support classes, version 1.0, MageLang Institute
 * <p>
 * We reserve no legal rights to this code--it is fully in the
 * public domain. An individual or company may do whatever
 * they wish with source code distributed with it, including
 * including the incorporation of it into commerical software.
 *
 * <p>However, this code cannot be sold as a standalone product.
 * <p>
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
 * <p>
 * The MageLang Support Classes Gang:
 * @version MageLang Support Classes 1.0, MageLang Insitute, 1997
 * @author <a href="http:www.scruz.net/~thetick">Scott Stanchfield</a>, <a href=http://www.MageLang.com>MageLang Institute</a>
 * @see JSplitterBar
 * @see JSplitterSpace
 *
 * @author Jeri Lockhart - jeri.lockhart@sun.com
 * Modified for use in the NbColumnView widget.
 * When the user moves the splitter bar to the left, the column that is adjacent to the
 * left maintains its minimum size.
 * When the user moves the splitter bar to the right, the columns to the right of the
 * splitter bar, maintain their widths.
 *
 * layoutComponent() - can be called when the components in the container
 * do not yet have their bounds set.  In this case, use the component's
 * preferred size.  JSplitterBar can set the bounds of the components
 * when the user drags a splitter bar.  If the bounds for a component
 * are set, use this size.
 *
 * checkLayoutSize() - is called for both preferredLayoutSize() and
 * minimumLayoutSize().  To calculate the width of the layout, uses the
 * actual width of the component, if present, or uses the preferred width
 * of the component.
 *
 */
public class SplitterLayout implements LayoutManager2, java.io.Serializable {
	/** Aligns components vertically -- SplitterBars will move up/down */
	public static final int VERTICAL   = 0;
	/** Aligns components horizontally -- SplitterBars will move left-right */
	public static final int HORIZONTAL = 1;
	
	static JSplitterBar dragee;
	
	private int lastW=-1, lastH=-1;
	private boolean newComponentAdded;
	
	private static final long serialVersionUID = -8658291919501921765L;
	private boolean fill = true;		// false - use preferred size, 
										// instead of weights
        private Dimension originalPreferredSize;
	
	
	public SplitterLayout() {
	}
	
	/** Create a new SplitterLayout
	 * @param orientation -- VERTICAL or HORIZONTAL
	 * @param fill - expand to fill target or use preferred size of components
	 */
	public SplitterLayout( boolean fill) {
		setFill(fill);
	}
	/** Adds a component w/ constraints to the layout.  This should only
	 * be called by java.awt.Container's add method.
	 */
	public final void addLayoutComponent(Component comp, Object constraints) {
//            //System.out.println("addLayoutContainer(Component, Object) comp " + comp + ", constraints " + constraints);
		if (constraints == null) constraints = "1";
		if (constraints instanceof Integer) {
			newComponentAdded = true;
		} else
			addLayoutComponent((String)constraints, comp);
	}
	/** Adds a component w/ a String constraint to the layout.  This should
	 * only be called by java.awt.Container's add method.
	 */
	public final void addLayoutComponent(String name, Component comp) {
//            //System.out.println("addLayoutComponent(String, Component) name " + name + ", comp "+ comp);
		newComponentAdded = true;
                
	}
	
	
	// preferred and min layout size
	public final Dimension checkLayoutSize(Container target, boolean getPrefSize) {
//            //System.out.println("checkLayoutSize getPrefSize: " + getPrefSize);
		Dimension dim = new Dimension(0, 0);
		Component c[] = target.getComponents();
		
		Dimension d = null;
		for(int i = 0; i < c.length; i++) {
			if (c[i].isVisible()) {
				if (getPrefSize || (c[i] instanceof JSplitterBar)) {
					d = c[i].getPreferredSize();
				}
				else {
					d = c[i].getMinimumSize();
				}
				dim.height = Math.max(d.height, dim.height);
				dim.width += d.width;
//				//System.out.println("checkLayoutSize comp #" + i + "  d.height: "  + d.height + ", dim.height: " + dim.height);
//				//System.out.println("checkLayoutSize comp #" + i + "  d.width: "  + d.width + ", dim.width: " + dim.width);
			}
		}
		
		Insets insets = target.getInsets();
//		//System.out.println("checkLayoutSize insets " + insets);
		dim.width += insets.left + insets.right;
		dim.height += insets.top + insets.bottom;
//		//System.out.println("checkLayoutSize returning dim " + dim);
		return dim;
	}
	/** Tells the caller that we prefer to be centered */
	public final float getLayoutAlignmentX(Container parent) {return 0.5f;}
	/** Tells the caller that we prefer to be centered */
	public final float getLayoutAlignmentY(Container parent) {return 0.5f;}
	/** Does not have any effect (overridden to null the effect) */
	public final void  invalidateLayout(Container target)     {}
	
	
	
	/** Lays out the components in the specified container by telling
	 * them what their size will be
	 */
	public final void layoutContainer(Container target) {
//            //System.out.println("layoutContainer start");
            //System.out.println("layoutContainer target start preferred size " + target.getPreferredSize());
            if (originalPreferredSize == null){ // save it the first time
                originalPreferredSize = target.getPreferredSize();
            }
//            Thread.dumpStack();
		Component c[] = target.getComponents();
		Insets insets = target.getInsets();
		Dimension dim = target.getSize();
//		//System.out.println("layoutContainer target original size " + dim);
		int top = insets.top;
		int bottom = dim.height - insets.bottom;
		int left = insets.left;
		int right = dim.width - insets.right;
		
		boolean reScaleW = false, reScaleH=false;
//		float scaleW = 0, scaleH = 0;
		
		// if the width/height has changed, scale the splitter bar positions
//                //System.out.println("layoutContainer lastW "+ lastW + ", dim.width " + dim.width);
//                //System.out.println("layoutContainer lastH "+ lastH + ", dim.height " + dim.height);
		if (lastW == -1) {  // save it the first time
			lastW = dim.width;
			lastH = dim.height;
		} else {
			if (lastW != dim.width) {
				reScaleW = true;
//				scaleW = (float)dim.width/(float)lastW;
//                                //System.out.println("layoutContainer scaleW "+ scaleW);
				lastW = dim.width;
			}
			if (lastH != dim.height) {
				reScaleH = true;
//				scaleH = (float)dim.height/(float)lastH;
//                                //System.out.println("layoutContainer scaleH "+ scaleH);
				lastH = dim.height;
			}
		}
//                //System.out.println("layoutContainer reScaleW " + reScaleW);
//                //System.out.println("layoutContainer reScaleH " + reScaleH);
		
		dim.width = right - left;
		dim.height = bottom - top;
		
		
		int x = left;
		int y = top;
		for(int i = 0; i < c.length; i++) {
//			//System.out.println("layoutContainer bounds " + i + " " + c[i].getBounds());
//			//System.out.println("layoutContainer pref size " + i + " " + c[i].getPreferredSize());
//			//System.out.println("layoutContainer " + i + " is visible " + c[i].isVisible());
			if (c[i].isVisible()) {
//				if (c[i] instanceof JSplitterBar) {
//					if (reScaleW) {
//						//System.out.println("layoutContainer reScaleW");
//						Point p = c[i].getLocation();
//						c[i].setLocation((int)(((float)p.x)*scaleW),p.y); // dims set later
//                                                //System.out.println("layoutContainer setLocation " + i + " " + c[i].getLocation());
//					}
//				}
				
				// if the component hasn't been sized, use it's preferred size, else use its bounds
				Dimension prefD = c[i].getPreferredSize();
				Dimension size = c[i].getSize();
				if (size.width == 0){
					c[i].setBounds(x, y, prefD.width, dim.height);
					x += prefD.width;
				}
				else {
                                    // get the ColumnView height
                                    Container scrollpane = target.getParent().getParent();
                                    Container cv = scrollpane.getParent();
                                    int cvHeight = 0;
//                                    int vBarWidth = 0;
                                    if (cv instanceof JPanel){
                                        cvHeight = cv.getSize().height;
                                        
                                    }
                                    if (scrollpane instanceof JScrollPane){
                                        JScrollBar hBar = ((JScrollPane)scrollpane).getHorizontalScrollBar();
                                        //System.out.println("layoutContainer hBar isVisible " + hBar.isVisible());
//                                        JScrollBar vBar = ((JScrollPane)scrollpane).getVerticalScrollBar();
                                        if (hBar.isVisible()){
                                            cvHeight -= hBar.getHeight();
                                        }
//                                        if (vBar.isVisible()){
//                                            //System.out.println("layoutContainer vBar isVisible " + vBar.isVisible());
//                                            vBarWidth = vBar.getWidth();
//                                        }
                                    }
				    c[i].setBounds(x, y, size.width, cvHeight-2);
//					c[i].setBounds(x, y, size.width, dim.height);
				    x += size.width;
				}
                                
//                                //System.out.println("layoutContainer new bounds " + i + " " + c[i].getBounds());
			}
		}
		
                // set new width for container if it's too small
                //  or if the total width of the components is smaller
                // than the container  (because columns have been removed)
                Rectangle lastComp = c[c.length-1].getBounds();
                int totalCompsWidth = lastComp.x + lastComp.width;
                Dimension targetSize = target.getSize();
                //System.out.println("layoutContainer target size " + target.getSize());
                //System.out.println("layoutContainer target pref size " + target.getPreferredSize());
                //System.out.println("layoutContainer lastComp height "+ lastComp.height);
                //System.out.println("layoutContainer target height "+ targetSize.height);
                if (!(targetSize.width == totalCompsWidth && targetSize.height == lastComp.height)){
                    target.setPreferredSize(new Dimension(totalCompsWidth, lastComp.height)); 
                    if (target instanceof JComponent){
                        ((JComponent)target).revalidate();                        
                    }
                }
                //System.out.println("layoutContainer end preferred size " + target.getPreferredSize());
		newComponentAdded = false;
	}
	/** Determines the maximum amount of space that could be used
	 * when laying out the components in the specified container.
	 * @param -- the container being laid out
	 */
	public final Dimension maximumLayoutSize(Container target) {
//		//System.out.println("maximumLayoutSize ");
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	/** Determines the minimum amount of room requested for the layout
	 * of components contained in the specified container.
	 * @param target -- the Container being laid out
	 */
//    public final Dimension minimumLayoutSize(Container target)   {return checkLayoutSize(target, false);}
	public final Dimension minimumLayoutSize(Container target)   {
//		//System.out.println("minimumLayoutSize");
		return checkLayoutSize(target, true);
	}
	// TEMP -- CHECK TO SEE HOW minsize==prefsize seems
	
	/** Determines the preferred amount of room requested for the layout
	 * of components contained in the specified container.
	 * @param target -- the Container being laid out
	 */
	public final Dimension preferredLayoutSize(Container target) {
//		//System.out.println("preferredLayoutSize");
		return checkLayoutSize(target, true);
	}
	/** Removes a component from the layout.  This should
	 * only be called by java.awt.Container's remove method.
	 */
	public final void removeLayoutComponent(Component comp) {
		newComponentAdded = true; // so layout gets re-adjusted
	}
	
	/**
	 *
	 *
	 */
	public void setFill(boolean fill){
		this.fill = fill;
	}
	
	
	
	/**
	 *
	 *
	 */
	public boolean getFill( ){
		return this.fill;
	}
	
	
	/** Returns a String representation of the Layout */
	public final String toString() {
		return getClass().getName();
	}
}
