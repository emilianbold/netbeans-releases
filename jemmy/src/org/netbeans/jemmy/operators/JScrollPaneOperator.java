/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.operators;

import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.util.EmptyVisualizer;

import org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import javax.swing.border.Border;

import javax.swing.plaf.ScrollPaneUI;

/**
 * <BR><BR>Timeouts used: <BR>
 * JScrollBarOperator.OneScrollClickTimeout - time for one scroll click <BR>
 * JScrollBarOperator.WholeScrollTimeout - time for the whole scrolling <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JScrollPaneOperator extends JComponentOperator
    implements Timeoutable, Outputable {

    private static int X_POINT_RECT_SIZE = 6;
    private static int Y_POINT_RECT_SIZE = 4;
    
    private Timeouts timeouts;
    private TestOut output;
    private JScrollBarOperator hScrollBarOper = null;
    private JScrollBarOperator vScrollBarOper = null;
    
    /**
     * Constructor.
     * @param b JScrollPane component.
     */
    public JScrollPaneOperator(JScrollPane b) {
	super(b);
    }

    /**
     * Constructs a JScrollPaneOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JScrollPaneOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JScrollPane)cont.
             waitSubComponent(new JScrollPaneFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JScrollPaneOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JScrollPaneOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JScrollPaneOperator(ContainerOperator cont, int index) {
	this((JScrollPane)waitComponent(cont, 
					new JScrollPaneFinder(), 
					index));
	copyEnvironment(cont);
   }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @throws TimeoutExpiredException
     */
    public JScrollPaneOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JScrollPane in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JScrollPane instance or null if component was not found.
     */
    public static JScrollPane findJScrollPane(Container cont, ComponentChooser chooser, int index) {
	return((JScrollPane)findComponent(cont, new JScrollPaneFinder(chooser), index));
    }
    
    /**
     * Searches 0'th JScrollPane in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JScrollPane instance or null if component was not found.
     */
    public static JScrollPane findJScrollPane(Container cont, ComponentChooser chooser) {
	return(findJScrollPane(cont, chooser, 0));
    }
    
    /**
     * Searches JScrollPane in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JScrollPane instance or null if component was not found.
     */
    public static JScrollPane findJScrollPane(Container cont, int index) {
	return(findJScrollPane(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JScrollPane instance"), index));
    }
    
    /**
     * Searches 0'th JScrollPane in container.
     * @param cont Container to search component in.
     * @return JScrollPane instance or null if component was not found.
     */
    public static JScrollPane findJScrollPane(Container cont) {
	return(findJScrollPane(cont, 0));
    }

    /**
     * Searches JScrollPane object which component lies on.
     * @param comp Component to find JScrollPane under.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JScrollPane instance or null if component was not found.
     */
    public static JScrollPane findJScrollPaneUnder(Component comp, ComponentChooser chooser) {
	return((JScrollPane)findContainerUnder(comp, new JScrollPaneFinder(chooser)));
    }
    
    /**
     * Searches JScrollPane object which component lies on.
     * @param comp Component to find JScrollPane under.
     * @return JScrollPane instance or null if component was not found.
     */
    public static JScrollPane findJScrollPaneUnder(Component comp) {
	return(findJScrollPaneUnder(comp, new JScrollPaneFinder()));
    }
    
    /**
     * Waits JScrollPane in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JScrollPane instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JScrollPane waitJScrollPane(Container cont, ComponentChooser chooser, int index) {
	return((JScrollPane)waitComponent(cont, new JScrollPaneFinder(chooser), index));
    }
    
    /**
     * Waits 0'th JScrollPane in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JScrollPane instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JScrollPane waitJScrollPane(Container cont, ComponentChooser chooser) {
	return(waitJScrollPane(cont, chooser, 0));
    }
    
    /**
     * Waits JScrollPane in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JScrollPane instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JScrollPane waitJScrollPane(Container cont, int index) {
	return(waitJScrollPane(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JScrollPane instance"), index));
    }
    
    /**
     * Waits 0'th JScrollPane in container.
     * @param cont Container to search component in.
     * @return JScrollPane instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JScrollPane waitJScrollPane(Container cont) {
	return(waitJScrollPane(cont, 0));
    }

    /**
     * Sets values for both JScrollBars.
     * @param hValue a value for the horizontal scrollbar.
     * @param vValue a value for the vertical scrollbar.
     */
    public void setValues(int hValue, int vValue) {
	initOperators();
	hScrollBarOper.setValue(hValue);
	vScrollBarOper.setValue(vValue);
    }

    public void setTimeouts(Timeouts timeouts) {
	super.setTimeouts(timeouts);
	this.timeouts = timeouts;
    }

    public Timeouts getTimeouts() {
	return(timeouts);
    }

    public void setOutput(TestOut out) {
	output = out;
	super.setOutput(output.createErrorOutput());
    }

    public TestOut getOutput() {
	return(output);
    }

    /**
     * Scrolls horizontal scroll bar.
     * @param value Value to scroll horizontal scroll bar to.
     * @throws TimeoutExpiredException
     */
    public void scrollToHorizontalValue(int value) {
	output.printTrace("Scroll JScrollPane to " + Integer.toString(value) + " horizontal value \n" +
			  toStringSource());
	output.printGolden("Scroll JScrollPane to " + Integer.toString(value) + " horizontal value");
	initOperators();
	makeComponentVisible();
	if(hScrollBarOper != null && hScrollBarOper.getSource().isVisible()) {
	    hScrollBarOper.scrollToValue(value);
	}
    }

    /**
     * Scrolls horizontal scroll bar.
     * @param proportionalValue Proportional value to scroll horizontal scroll bar to.
     * @throws TimeoutExpiredException
     */
    public void scrollToHorizontalValue(double proportionalValue) {
	output.printTrace("Scroll JScrollPane to " + Double.toString(proportionalValue) + " proportional horizontal value \n" +
			  toStringSource());
	output.printGolden("Scroll JScrollPane to " + Double.toString(proportionalValue) + " proportional horizontal value");
	initOperators();
	makeComponentVisible();
	if(hScrollBarOper != null && hScrollBarOper.getSource().isVisible()) {
	    hScrollBarOper.scrollToValue(proportionalValue);
	}
    }

    /**
     * Scrolls vertical scroll bar.
     * @param value Value to scroll vertical scroll bar to.
     * @throws TimeoutExpiredException
     */
    public void scrollToVerticalValue(int value) {
	output.printTrace("Scroll JScrollPane to " + Integer.toString(value) + " vertical value \n" +
			  toStringSource());
	output.printGolden("Scroll JScrollPane to " + Integer.toString(value) + " vertical value");
	initOperators();
	makeComponentVisible();
	if(vScrollBarOper != null && vScrollBarOper.getSource().isVisible()) {
	    vScrollBarOper.scrollToValue(value);
	}
    }

    /**
     * Scrolls vertical scroll bar.
     * @param proportionalValue Value to scroll vertical scroll bar to.
     * @throws TimeoutExpiredException
     */
    public void scrollToVerticalValue(double proportionalValue) {
	output.printTrace("Scroll JScrollPane to " + Double.toString(proportionalValue) + " proportional vertical value \n" +
			  toStringSource());
	output.printGolden("Scroll JScrollPane to " + Double.toString(proportionalValue) + " proportional vertical value");
	initOperators();
	makeComponentVisible();
	if(vScrollBarOper != null && vScrollBarOper.getSource().isVisible()) {
	    vScrollBarOper.scrollToValue(proportionalValue);
	}
    }

    /**
     * Scrolls both scroll bars.
     * @param valueX Value to scroll horizontal scroll bar to.
     * @param valueY Value to scroll vertical scroll bar to.
     * @throws TimeoutExpiredException
     */
    public void scrollToValues(int valueX, int valueY) {
	scrollToVerticalValue(valueX);
	scrollToHorizontalValue(valueX);
    }

    /**
     * Scrolls both scroll bars.
     * @param proportionalValueX Value to scroll horizontal scroll bar to.
     * @param proportionalValueY Value to scroll vertical scroll bar to.
     * @throws TimeoutExpiredException
     */
    public void scrollToValues(double proportionalValueX, double proportionalValueY) {
	scrollToVerticalValue(proportionalValueX);
	scrollToHorizontalValue(proportionalValueY);
    }

    /**
     * Scrolls pane to top.
     * @throws TimeoutExpiredException
     */
    public void scrollToTop() {
	output.printTrace("Scroll JScrollPane to top\n" +
			  toStringSource());
	output.printGolden("Scroll JScrollPane to top");
	initOperators();
	makeComponentVisible();
	if(vScrollBarOper != null && vScrollBarOper.getSource().isVisible()) {
	    vScrollBarOper.scrollToMinimum();
	}
    }

    /**
     * Scrolls pane to bottom.
     * @throws TimeoutExpiredException
     */
    public void scrollToBottom() {
	output.printTrace("Scroll JScrollPane to bottom\n" +
			  toStringSource());
	output.printGolden("Scroll JScrollPane to bottom");
	initOperators();
	makeComponentVisible();
	if(vScrollBarOper != null && vScrollBarOper.getSource().isVisible()) {
	    vScrollBarOper.scrollToMaximum();
	}
    }

    /**
     * Scrolls pane to left.
     * @throws TimeoutExpiredException
     */
    public void scrollToLeft() {
	output.printTrace("Scroll JScrollPane to left\n" +
			  toStringSource());
	output.printGolden("Scroll JScrollPane to left");
	initOperators();
	makeComponentVisible();
	if(hScrollBarOper != null && hScrollBarOper.getSource().isVisible()) {
	    hScrollBarOper.scrollToMinimum();
	}
    }

    /**
     * Scrolls pane to right.
     * @throws TimeoutExpiredException
     */
    public void scrollToRight() {
	output.printTrace("Scroll JScrollPane to right\n" +
			  toStringSource());
	output.printGolden("Scroll JScrollPane to right");
	initOperators();
	makeComponentVisible();
	if(hScrollBarOper != null && hScrollBarOper.getSource().isVisible()) {
	    hScrollBarOper.scrollToMaximum();
	}
    }

    /**
     * Scrolls pane to rectangle of a component.
     * @param comp a subcomponent defining coordinate system.
     * @param x coordinate
     * @param y coordinate
     * @param width rectangle width
     * @param height rectangle height
     * @throws TimeoutExpiredException
     */
    public void scrollToComponentRectangle(Component comp, int x, int y, int width, int height) {
	initOperators();
	makeComponentVisible();
	if(hScrollBarOper != null && hScrollBarOper.getSource().isVisible()) {
	    hScrollBarOper.scrollTo(new ComponentRectChecker(comp, x, y, width, height, JScrollBar.HORIZONTAL));
	}
	if(vScrollBarOper != null && vScrollBarOper.getSource().isVisible()) {
	    vScrollBarOper.scrollTo(new ComponentRectChecker(comp, x, y, width, height, JScrollBar.VERTICAL));
	}
    }

    /**
     * Scrolls pane to point.
     * @param comp a subcomponent defining coordinate system.
     * @param x coordinate
     * @param y coordinate
     * @throws TimeoutExpiredException
     */
    public void scrollToComponentPoint(Component comp, int x, int y) {
	scrollToComponentRectangle(comp, 
				   x - X_POINT_RECT_SIZE,
				   y - Y_POINT_RECT_SIZE,
				   2 * X_POINT_RECT_SIZE,
				   2 * Y_POINT_RECT_SIZE);
    }

    /**
     * Scrolls pane to component on this pane.
     * Component should lay on the JScrollPane view.
     * @param comp Component to scroll to.
     * @throws TimeoutExpiredException
     */
    public void scrollToComponent(final Component comp) {
        String componentToString = (String)runMapping(
            new Operator.MapAction("comp.toString()") {
                public Object map() {
                    return comp.toString();
                } 
            }
        );
	output.printTrace("Scroll JScrollPane " + toStringSource() + 
			  "\nto component " + componentToString);
	output.printGolden("Scroll JScrollPane to " + comp.getClass().getName() + " component.");
	scrollToComponentRectangle(comp, 0, 0, comp.getWidth(), comp.getHeight());
    }


    /**
     * Returns operator used for horizontal scrollbar.
     * @return an operator for the horizontal scrollbar.
     */
    public JScrollBarOperator getHScrollBarOperator() {
	initOperators();
	return(hScrollBarOper);
    }

    /**
     * Returns operator used for vertical scrollbar.
     * @return an operator for the vertical scrollbar.
     */
    public JScrollBarOperator getVScrollBarOperator() {
	initOperators();
	return(vScrollBarOper);
    }

    /**
     * Checks if component's rectangle is inside view port (no scrolling necessary).
     * @param comp a subcomponent defining coordinate system.
     * @param x coordinate
     * @param y coordinate
     * @param width rectangle width
     * @param height rectangle height
     * @return true if pointed subcomponent rectangle is inside the scrolling area.
     */
    public boolean checkInside(Component comp, int x, int y, int width, int height) {
	Component view = getViewport().getView();
	Point toPoint = SwingUtilities.
	    convertPoint(comp, x, y, getViewport().getView());
	initOperators();
	if(hScrollBarOper != null && hScrollBarOper.getSource().isVisible()) {
	    if(toPoint.x < hScrollBarOper.getValue()) {
		return(false);
	    }
	    if(comp.getWidth() > view.getWidth()) {
		return(toPoint.x > 0);
	    } else {
		return(toPoint.x + comp.getWidth() > 
		       hScrollBarOper.getValue() + view.getWidth());
	    }
	}
	if(vScrollBarOper != null && vScrollBarOper.getSource().isVisible()) {
	    if(toPoint.y < vScrollBarOper.getValue()) {
		return(false);
	    }
	    if(comp.getHeight() > view.getHeight()) {
		return(toPoint.y > 0);
	    } else {
		return(toPoint.y + comp.getHeight() > 
		       vScrollBarOper.getValue() + view.getHeight());
	    }
	}
	return(true);
    }
    
    /**
     * Checks if component is inside view port (no scrolling necessary).
     * @param comp a subcomponent
     * @return true if pointed subcomponent is inside the scrolling area.
     */
    public boolean checkInside(Component comp) {
	return(checkInside(comp, 0, 0, comp.getWidth(), comp.getHeight()));
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JScrollPane.createHorizontalScrollBar()</code> through queue*/
    public JScrollBar createHorizontalScrollBar() {
	return((JScrollBar)runMapping(new MapAction("createHorizontalScrollBar") {
		public Object map() {
		    return(((JScrollPane)getSource()).createHorizontalScrollBar());
		}}));}

    /**Maps <code>JScrollPane.createVerticalScrollBar()</code> through queue*/
    public JScrollBar createVerticalScrollBar() {
	return((JScrollBar)runMapping(new MapAction("createVerticalScrollBar") {
		public Object map() {
		    return(((JScrollPane)getSource()).createVerticalScrollBar());
		}}));}

    /**Maps <code>JScrollPane.getColumnHeader()</code> through queue*/
    public JViewport getColumnHeader() {
	return((JViewport)runMapping(new MapAction("getColumnHeader") {
		public Object map() {
		    return(((JScrollPane)getSource()).getColumnHeader());
		}}));}

    /**Maps <code>JScrollPane.getCorner(String)</code> through queue*/
    public Component getCorner(final String string) {
	return((Component)runMapping(new MapAction("getCorner") {
		public Object map() {
		    return(((JScrollPane)getSource()).getCorner(string));
		}}));}

    /**Maps <code>JScrollPane.getHorizontalScrollBar()</code> through queue*/
    public JScrollBar getHorizontalScrollBar() {
	return((JScrollBar)runMapping(new MapAction("getHorizontalScrollBar") {
		public Object map() {
		    return(((JScrollPane)getSource()).getHorizontalScrollBar());
		}}));}

    /**Maps <code>JScrollPane.getHorizontalScrollBarPolicy()</code> through queue*/
    public int getHorizontalScrollBarPolicy() {
	return(runMapping(new MapIntegerAction("getHorizontalScrollBarPolicy") {
		public int map() {
		    return(((JScrollPane)getSource()).getHorizontalScrollBarPolicy());
		}}));}

    /**Maps <code>JScrollPane.getRowHeader()</code> through queue*/
    public JViewport getRowHeader() {
	return((JViewport)runMapping(new MapAction("getRowHeader") {
		public Object map() {
		    return(((JScrollPane)getSource()).getRowHeader());
		}}));}

    /**Maps <code>JScrollPane.getUI()</code> through queue*/
    public ScrollPaneUI getUI() {
	return((ScrollPaneUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JScrollPane)getSource()).getUI());
		}}));}

    /**Maps <code>JScrollPane.getVerticalScrollBar()</code> through queue*/
    public JScrollBar getVerticalScrollBar() {
	return((JScrollBar)runMapping(new MapAction("getVerticalScrollBar") {
		public Object map() {
		    return(((JScrollPane)getSource()).getVerticalScrollBar());
		}}));}

    /**Maps <code>JScrollPane.getVerticalScrollBarPolicy()</code> through queue*/
    public int getVerticalScrollBarPolicy() {
	return(runMapping(new MapIntegerAction("getVerticalScrollBarPolicy") {
		public int map() {
		    return(((JScrollPane)getSource()).getVerticalScrollBarPolicy());
		}}));}

    /**Maps <code>JScrollPane.getViewport()</code> through queue*/
    public JViewport getViewport() {
	return((JViewport)runMapping(new MapAction("getViewport") {
		public Object map() {
		    return(((JScrollPane)getSource()).getViewport());
		}}));}

    /**Maps <code>JScrollPane.getViewportBorder()</code> through queue*/
    public Border getViewportBorder() {
	return((Border)runMapping(new MapAction("getViewportBorder") {
		public Object map() {
		    return(((JScrollPane)getSource()).getViewportBorder());
		}}));}

    /**Maps <code>JScrollPane.getViewportBorderBounds()</code> through queue*/
    public Rectangle getViewportBorderBounds() {
	return((Rectangle)runMapping(new MapAction("getViewportBorderBounds") {
		public Object map() {
		    return(((JScrollPane)getSource()).getViewportBorderBounds());
		}}));}

    /**Maps <code>JScrollPane.setColumnHeader(JViewport)</code> through queue*/
    public void setColumnHeader(final JViewport jViewport) {
	runMapping(new MapVoidAction("setColumnHeader") {
		public void map() {
		    ((JScrollPane)getSource()).setColumnHeader(jViewport);
		}});}

    /**Maps <code>JScrollPane.setColumnHeaderView(Component)</code> through queue*/
    public void setColumnHeaderView(final Component component) {
	runMapping(new MapVoidAction("setColumnHeaderView") {
		public void map() {
		    ((JScrollPane)getSource()).setColumnHeaderView(component);
		}});}

    /**Maps <code>JScrollPane.setCorner(String, Component)</code> through queue*/
    public void setCorner(final String string, final Component component) {
	runMapping(new MapVoidAction("setCorner") {
		public void map() {
		    ((JScrollPane)getSource()).setCorner(string, component);
		}});}

    /**Maps <code>JScrollPane.setHorizontalScrollBar(JScrollBar)</code> through queue*/
    public void setHorizontalScrollBar(final JScrollBar jScrollBar) {
	runMapping(new MapVoidAction("setHorizontalScrollBar") {
		public void map() {
		    ((JScrollPane)getSource()).setHorizontalScrollBar(jScrollBar);
		}});}

    /**Maps <code>JScrollPane.setHorizontalScrollBarPolicy(int)</code> through queue*/
    public void setHorizontalScrollBarPolicy(final int i) {
	runMapping(new MapVoidAction("setHorizontalScrollBarPolicy") {
		public void map() {
		    ((JScrollPane)getSource()).setHorizontalScrollBarPolicy(i);
		}});}

    /**Maps <code>JScrollPane.setRowHeader(JViewport)</code> through queue*/
    public void setRowHeader(final JViewport jViewport) {
	runMapping(new MapVoidAction("setRowHeader") {
		public void map() {
		    ((JScrollPane)getSource()).setRowHeader(jViewport);
		}});}

    /**Maps <code>JScrollPane.setRowHeaderView(Component)</code> through queue*/
    public void setRowHeaderView(final Component component) {
	runMapping(new MapVoidAction("setRowHeaderView") {
		public void map() {
		    ((JScrollPane)getSource()).setRowHeaderView(component);
		}});}

    /**Maps <code>JScrollPane.setUI(ScrollPaneUI)</code> through queue*/
    public void setUI(final ScrollPaneUI scrollPaneUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JScrollPane)getSource()).setUI(scrollPaneUI);
		}});}

    /**Maps <code>JScrollPane.setVerticalScrollBar(JScrollBar)</code> through queue*/
    public void setVerticalScrollBar(final JScrollBar jScrollBar) {
	runMapping(new MapVoidAction("setVerticalScrollBar") {
		public void map() {
		    ((JScrollPane)getSource()).setVerticalScrollBar(jScrollBar);
		}});}

    /**Maps <code>JScrollPane.setVerticalScrollBarPolicy(int)</code> through queue*/
    public void setVerticalScrollBarPolicy(final int i) {
	runMapping(new MapVoidAction("setVerticalScrollBarPolicy") {
		public void map() {
		    ((JScrollPane)getSource()).setVerticalScrollBarPolicy(i);
		}});}

    /**Maps <code>JScrollPane.setViewport(JViewport)</code> through queue*/
    public void setViewport(final JViewport jViewport) {
	runMapping(new MapVoidAction("setViewport") {
		public void map() {
		    ((JScrollPane)getSource()).setViewport(jViewport);
		}});}

    /**Maps <code>JScrollPane.setViewportBorder(Border)</code> through queue*/
    public void setViewportBorder(final Border border) {
	runMapping(new MapVoidAction("setViewportBorder") {
		public void map() {
		    ((JScrollPane)getSource()).setViewportBorder(border);
		}});}

    /**Maps <code>JScrollPane.setViewportView(Component)</code> through queue*/
    public void setViewportView(final Component component) {
	runMapping(new MapVoidAction("setViewportView") {
		public void map() {
		    ((JScrollPane)getSource()).setViewportView(component);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    private void initOperators() {
	if(hScrollBarOper == null && getHorizontalScrollBar() != null && getHorizontalScrollBar().isVisible()) {
	    hScrollBarOper = new JScrollBarOperator(getHorizontalScrollBar());
	    hScrollBarOper.copyEnvironment(this);
	    hScrollBarOper.setVisualizer(new EmptyVisualizer());
	}
	if(vScrollBarOper == null && getVerticalScrollBar() != null && getVerticalScrollBar().isVisible()) {
	    vScrollBarOper = new JScrollBarOperator(getVerticalScrollBar());
	    vScrollBarOper.copyEnvironment(this);
	    vScrollBarOper.setVisualizer(new EmptyVisualizer());
	}
    }

    private class ComponentRectChecker implements JScrollBarOperator.ScrollChecker {
	Component comp;
	int x;
	int y;
	int width;
	int height;
	int orientation;
	public ComponentRectChecker(Component comp, int x, int y, int width, int height, int orientation) {
	    this.comp = comp;
	    this.x = x;
	    this.y = y;
	    this.width = width;
	    this.height = height;
	    this.orientation = orientation;
	}
	public int getScrollDirection(JScrollBarOperator oper) {
	    Point toPoint = SwingUtilities.
		convertPoint(comp, x, y, getViewport().getView());
	    int to = (orientation == JScrollBar.HORIZONTAL) ? toPoint.x : toPoint.y;
	    int ln = (orientation == JScrollBar.HORIZONTAL) ? width : height;
	    int lv = (orientation == JScrollBar.HORIZONTAL) ? getViewport().getWidth() : getViewport().getHeight();
	    int vl = oper.getValue();
	    if(to < vl) {
		return(ScrollAdjuster.DECREASE_SCROLL_DIRECTION);
	    } else if((to + ln - 1) > (vl + lv) &&
		      to        > vl) {
		return(ScrollAdjuster.INCREASE_SCROLL_DIRECTION);
	    } else {
		return(ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION);
	    }
	}
	public String getDescription() {
	    return("");
	}
    }

    /**
     * Checks component type.
     */
    public static class JScrollPaneFinder extends Finder {
        /**
         * Constructs JScrollPaneFinder.
         * @param sf other searching criteria.
         */
	public JScrollPaneFinder(ComponentChooser sf) {
            super(JScrollPane.class, sf);
	}
        /**
         * Constructs JScrollPaneFinder.
         */
	public JScrollPaneFinder() {
            super(JScrollPane.class);
	}
    }
}
