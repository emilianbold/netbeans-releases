package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.demo.Demonstrator;

import org.netbeans.jemmy.drivers.*;
import org.netbeans.jemmy.drivers.scrolling.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.*;

import java.awt.Component;

import java.io.PrintWriter;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import java.lang.reflect.InvocationTargetException;

public class jemmy_018 extends JemmyTest {

    private boolean checkInside(Component comp, Component area, 
				int x, int y, int width, int height) {
	double compLeft = comp.getLocationOnScreen().getX() + x;
	double compTop = comp.getLocationOnScreen().getY() + y;
	double compRight = compLeft + width;
	double compBottom = compTop + height;
	double areaLeft = area.getLocationOnScreen().getX();
	double areaTop = area.getLocationOnScreen().getY();
	double areaRight = areaLeft + area.getWidth();
	double areaBottom = areaTop + area.getHeight();
	return(compLeft >= areaLeft && compRight <= areaRight &&
	       compTop >= areaTop && compBottom <=areaBottom);
    }

    private boolean checkInside(Component comp, Component area) {
	return(checkInside(comp, area, 0, 0, comp.getWidth(), comp.getHeight()));
    }

    public int runIt(Object obj) {

	try {

	    try {
		(new ClassReference("org.netbeans.jemmy.testing.Application_018")).startApplication();
	    } catch(ClassNotFoundException e) {
		getOutput().printStackTrace(e);
	    } catch(InvocationTargetException e) {
		getOutput().printStackTrace(e);
	    } catch(NoSuchMethodException e) {
		getOutput().printStackTrace(e);
	    }

	    ComponentOperator.setDefaultComponentVisualizer(new EmptyVisualizer());

	    JFrame win =JFrameOperator.waitJFrame("Application_018", true, true);

	    JButton butt00 = JButtonOperator.findJButton(win, "00", true, true);
	    JButton butt04 = JButtonOperator.findJButton(win, "04", true, true);
	    JButton butt11 = JButtonOperator.findJButton(win, "11", true, true);
	    JButton butt22 = JButtonOperator.findJButton(win, "22", true, true);
	    JButton butt24 = JButtonOperator.findJButton(win, "24", true, true);
	    JButton butt33 = JButtonOperator.findJButton(win, "33", true, true);
	    JButton butt44 = JButtonOperator.findJButton(win, "44", true, true);
	    JButton butt42 = JButtonOperator.findJButton(win, "42", true, true);
	    JButton butt40 = JButtonOperator.findJButton(win, "40", true, true);

	    JScrollPane sp = JScrollPaneOperator.
		findJScrollPane(win, 
				ComponentSearcher.getTrueChooser("Scroll pane"));

	    if(sp != JScrollPaneOperator.findJScrollPaneUnder(butt00)) {
		getOutput().printErrLine("One of two methods: findJScrollPane or findUnder does not work correcly");
		return(1);
	    }

	    JScrollBarOperator hscroll = new JScrollBarOperator(new JFrameOperator(win), 1);
	    getOutput().printLine("Horizontal: " + hscroll.getSource().toString());
	    if(hscroll.getOrientation() != JScrollBar.HORIZONTAL) {
		getOutput().printErrLine("Should be horizontal.");
		return(1);
	    }
	    JScrollBarOperator vscroll = new JScrollBarOperator(new JFrameOperator(win));
	    getOutput().printLine("Vertical: " + vscroll.getSource().toString());
	    if(vscroll.getOrientation() != JScrollBar.VERTICAL) {
		getOutput().printErrLine("Should be vertical.");
		return(1);
	    }
	    
	    JScrollPaneOperator scroller = new JScrollPaneOperator(sp);
	    JScrollPaneOperator scrolle1 = new JScrollPaneOperator(new JFrameOperator(win));
	    getOutput().printLine("Old : " + scroller.getSource().toString());
	    getOutput().printLine("New : " + scrolle1.getSource().toString());
	    if(scrolle1.getSource() != scroller.getSource()) {
		getOutput().printErrLine("Should be the same.");
		return(1);
	    }

	    Demonstrator.setTitle("jemmy_018 test");

	    Demonstrator.nextStep("Set scroller values to maximum");

	    scroller.setValues(scroller.getHorizontalScrollBar().getMaximum(), 
			       scroller.getVerticalScrollBar().getMaximum());

	    if(!checkInside(butt44, sp)) {
		getOutput().printErrLine("44 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Set scroller values to (0, 0)");

	    scroller.setValues(0, 0);

	    if(!checkInside(butt00, sp)) {
		getOutput().printErrLine("00 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to the center of \"22\" button");

	    scroller.scrollToComponentPoint(butt22, 
					    butt22.getWidth() / 2,
					    butt22.getHeight() / 2);

	    //	    try { Thread.currentThread().sleep(10000); } catch (Exception e) {}

	    if(!checkInside(butt22, sp, 
			    butt22.getWidth() / 2 - 1,
			    butt22.getHeight() / 2 - 1,
			    2,
			    2)) {
		getOutput().printErrLine("22 button center is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to right");

	    scroller.scrollToRight();

	    if(!checkInside(butt24, sp, 
			    butt24.getWidth() / 2 - 1,
			    butt24.getHeight() / 2 - 1,
			    2,
			    2)) {
		getOutput().printErrLine("24 button center is not inside scroll area!");
		return(1);
	    }

	    int x22 = 10;
	    int y22 = 10;
	    int w22 = butt22.getWidth() - 20;
	    int h22 = butt22.getHeight() - 20;
	    Demonstrator.nextStep("Scroll to (" + 
				  Integer.toString(x22) + "," +
				  Integer.toString(y22) + "," +
				  Integer.toString(w22) + "," +
				  Integer.toString(h22) + ")" +
				  " rectangle in \"22\" button");

	    scroller.scrollToComponentRectangle(butt22, x22, y22, w22, h22);

	    if(!checkInside(butt22, sp, x22, y22, w22, h22)) {
		getOutput().printErrLine("22 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to bottom");

	    scroller.scrollToBottom();

	    int x42 = 10;
	    int y42 = 10;
	    int w42 = butt42.getWidth() - 20;
	    int h42 = butt42.getHeight() - 20;
	    if(!checkInside(butt42, sp, x42, y42, w42, h42)) {
		getOutput().printErrLine("42 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to left");

	    scroller.scrollToLeft();

	    if(!checkInside(butt40, sp)) {
		getOutput().printErrLine("40 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to top");

	    scroller.scrollToTop();

	    if(!checkInside(butt00, sp)) {
		getOutput().printErrLine("00 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to center");

	    scroller.scrollToValues(0.5, 0.5);

	    if(!checkInside(butt22, sp)) {
		getOutput().printErrLine("22 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to \"11\" button");

	    scroller.scrollToComponent(butt11);


	    if(!checkInside(butt11, sp)) {
		getOutput().printErrLine("11 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to \"33\" button");

	    scroller.scrollToComponent(butt33);

	    if(!checkInside(butt33, sp)) {
		getOutput().printErrLine("33 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to horizontal center");

	    scroller.getHScrollBarOperator().scrollTo(new JScrollBarOperator.ScrollChecker() {
		    public int getScrollDirection(JScrollBarOperator oper) {
			if       (oper.getValue() <
				  ((oper.getMaximum() - 
				   oper.getVisibleAmount()) / 2)) {
			    return(ScrollAdjuster.INCREASE_SCROLL_DIRECTION);
			} else if(oper.getValue() > 
				  ((oper.getMaximum() - 
				    oper.getVisibleAmount()) / 2)) {
			    return(ScrollAdjuster.DECREASE_SCROLL_DIRECTION);
			} else {
			    return(ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION);
			}
		    }
		    public String getDescription() {
			return("");
		    }
		});

	    Demonstrator.nextStep("Scroll to vertical center");

	    scroller.getVScrollBarOperator().scrollTo(new JScrollBarOperator.ScrollChecker() {
		    public int getScrollDirection(JScrollBarOperator oper) {
			if       (oper.getValue() <
				  ((oper.getMaximum() - 
				   oper.getVisibleAmount()) / 2)) {
			    return(ScrollAdjuster.INCREASE_SCROLL_DIRECTION);
			} else if(oper.getValue() > 
				  ((oper.getMaximum() - 
				    oper.getVisibleAmount()) / 2)) {
			    return(ScrollAdjuster.DECREASE_SCROLL_DIRECTION);
			} else {
			    return(ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION);
			}
		    }
		    public String getDescription() {
			return("");
		    }
		});

	    if(!checkInside(butt22, sp)) {
		getOutput().printErrLine("22 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.showFinalComment("Test passed");

	    if(!testJScrollBar(hscroll)) {
		finalize();
		return(1);
	    }

	    if(!testJScrollPane(scroller)) {
		finalize();
		return(1);
	    }

	} catch(JemmyException e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

public boolean testJScrollBar(JScrollBarOperator jScrollBarOperator) {
    if(((JScrollBar)jScrollBarOperator.getSource()).getBlockIncrement() == jScrollBarOperator.getBlockIncrement()) {
        printLine("getBlockIncrement does work");
    } else {
        printLine("getBlockIncrement does not work");
        printLine(((JScrollBar)jScrollBarOperator.getSource()).getBlockIncrement());
        printLine(jScrollBarOperator.getBlockIncrement());
        return(false);
    }
    if(((JScrollBar)jScrollBarOperator.getSource()).getMaximum() == jScrollBarOperator.getMaximum()) {
        printLine("getMaximum does work");
    } else {
        printLine("getMaximum does not work");
        printLine(((JScrollBar)jScrollBarOperator.getSource()).getMaximum());
        printLine(jScrollBarOperator.getMaximum());
        return(false);
    }
    if(((JScrollBar)jScrollBarOperator.getSource()).getMinimum() == jScrollBarOperator.getMinimum()) {
        printLine("getMinimum does work");
    } else {
        printLine("getMinimum does not work");
        printLine(((JScrollBar)jScrollBarOperator.getSource()).getMinimum());
        printLine(jScrollBarOperator.getMinimum());
        return(false);
    }
    if(((JScrollBar)jScrollBarOperator.getSource()).getModel() == null &&
       jScrollBarOperator.getModel() == null ||
       ((JScrollBar)jScrollBarOperator.getSource()).getModel().equals(jScrollBarOperator.getModel())) {
        printLine("getModel does work");
    } else {
        printLine("getModel does not work");
        printLine(((JScrollBar)jScrollBarOperator.getSource()).getModel());
        printLine(jScrollBarOperator.getModel());
        return(false);
    }
    if(((JScrollBar)jScrollBarOperator.getSource()).getOrientation() == jScrollBarOperator.getOrientation()) {
        printLine("getOrientation does work");
    } else {
        printLine("getOrientation does not work");
        printLine(((JScrollBar)jScrollBarOperator.getSource()).getOrientation());
        printLine(jScrollBarOperator.getOrientation());
        return(false);
    }
    if(((JScrollBar)jScrollBarOperator.getSource()).getUI() == null &&
       jScrollBarOperator.getUI() == null ||
       ((JScrollBar)jScrollBarOperator.getSource()).getUI().equals(jScrollBarOperator.getUI())) {
        printLine("getUI does work");
    } else {
        printLine("getUI does not work");
        printLine(((JScrollBar)jScrollBarOperator.getSource()).getUI());
        printLine(jScrollBarOperator.getUI());
        return(false);
    }
    if(((JScrollBar)jScrollBarOperator.getSource()).getUnitIncrement() == jScrollBarOperator.getUnitIncrement()) {
        printLine("getUnitIncrement does work");
    } else {
        printLine("getUnitIncrement does not work");
        printLine(((JScrollBar)jScrollBarOperator.getSource()).getUnitIncrement());
        printLine(jScrollBarOperator.getUnitIncrement());
        return(false);
    }
    if(((JScrollBar)jScrollBarOperator.getSource()).getValue() == jScrollBarOperator.getValue()) {
        printLine("getValue does work");
    } else {
        printLine("getValue does not work");
        printLine(((JScrollBar)jScrollBarOperator.getSource()).getValue());
        printLine(jScrollBarOperator.getValue());
        return(false);
    }
    if(((JScrollBar)jScrollBarOperator.getSource()).getValueIsAdjusting() == jScrollBarOperator.getValueIsAdjusting()) {
        printLine("getValueIsAdjusting does work");
    } else {
        printLine("getValueIsAdjusting does not work");
        printLine(((JScrollBar)jScrollBarOperator.getSource()).getValueIsAdjusting());
        printLine(jScrollBarOperator.getValueIsAdjusting());
        return(false);
    }
    if(((JScrollBar)jScrollBarOperator.getSource()).getVisibleAmount() == jScrollBarOperator.getVisibleAmount()) {
        printLine("getVisibleAmount does work");
    } else {
        printLine("getVisibleAmount does not work");
        printLine(((JScrollBar)jScrollBarOperator.getSource()).getVisibleAmount());
        printLine(jScrollBarOperator.getVisibleAmount());
        return(false);
    }
    return(true);
}

public boolean testJScrollPane(JScrollPaneOperator jScrollPaneOperator) {
    if(((JScrollPane)jScrollPaneOperator.getSource()).getColumnHeader() == null &&
       jScrollPaneOperator.getColumnHeader() == null ||
       ((JScrollPane)jScrollPaneOperator.getSource()).getColumnHeader().equals(jScrollPaneOperator.getColumnHeader())) {
        printLine("getColumnHeader does work");
    } else {
        printLine("getColumnHeader does not work");
        printLine(((JScrollPane)jScrollPaneOperator.getSource()).getColumnHeader());
        printLine(jScrollPaneOperator.getColumnHeader());
        return(false);
    }
    if(((JScrollPane)jScrollPaneOperator.getSource()).getHorizontalScrollBar() == null &&
       jScrollPaneOperator.getHorizontalScrollBar() == null ||
       ((JScrollPane)jScrollPaneOperator.getSource()).getHorizontalScrollBar().equals(jScrollPaneOperator.getHorizontalScrollBar())) {
        printLine("getHorizontalScrollBar does work");
    } else {
        printLine("getHorizontalScrollBar does not work");
        printLine(((JScrollPane)jScrollPaneOperator.getSource()).getHorizontalScrollBar());
        printLine(jScrollPaneOperator.getHorizontalScrollBar());
        return(false);
    }
    if(((JScrollPane)jScrollPaneOperator.getSource()).getHorizontalScrollBarPolicy() == jScrollPaneOperator.getHorizontalScrollBarPolicy()) {
        printLine("getHorizontalScrollBarPolicy does work");
    } else {
        printLine("getHorizontalScrollBarPolicy does not work");
        printLine(((JScrollPane)jScrollPaneOperator.getSource()).getHorizontalScrollBarPolicy());
        printLine(jScrollPaneOperator.getHorizontalScrollBarPolicy());
        return(false);
    }
    if(((JScrollPane)jScrollPaneOperator.getSource()).getRowHeader() == null &&
       jScrollPaneOperator.getRowHeader() == null ||
       ((JScrollPane)jScrollPaneOperator.getSource()).getRowHeader().equals(jScrollPaneOperator.getRowHeader())) {
        printLine("getRowHeader does work");
    } else {
        printLine("getRowHeader does not work");
        printLine(((JScrollPane)jScrollPaneOperator.getSource()).getRowHeader());
        printLine(jScrollPaneOperator.getRowHeader());
        return(false);
    }
    if(((JScrollPane)jScrollPaneOperator.getSource()).getUI() == null &&
       jScrollPaneOperator.getUI() == null ||
       ((JScrollPane)jScrollPaneOperator.getSource()).getUI().equals(jScrollPaneOperator.getUI())) {
        printLine("getUI does work");
    } else {
        printLine("getUI does not work");
        printLine(((JScrollPane)jScrollPaneOperator.getSource()).getUI());
        printLine(jScrollPaneOperator.getUI());
        return(false);
    }
    if(((JScrollPane)jScrollPaneOperator.getSource()).getVerticalScrollBar() == null &&
       jScrollPaneOperator.getVerticalScrollBar() == null ||
       ((JScrollPane)jScrollPaneOperator.getSource()).getVerticalScrollBar().equals(jScrollPaneOperator.getVerticalScrollBar())) {
        printLine("getVerticalScrollBar does work");
    } else {
        printLine("getVerticalScrollBar does not work");
        printLine(((JScrollPane)jScrollPaneOperator.getSource()).getVerticalScrollBar());
        printLine(jScrollPaneOperator.getVerticalScrollBar());
        return(false);
    }
    if(((JScrollPane)jScrollPaneOperator.getSource()).getVerticalScrollBarPolicy() == jScrollPaneOperator.getVerticalScrollBarPolicy()) {
        printLine("getVerticalScrollBarPolicy does work");
    } else {
        printLine("getVerticalScrollBarPolicy does not work");
        printLine(((JScrollPane)jScrollPaneOperator.getSource()).getVerticalScrollBarPolicy());
        printLine(jScrollPaneOperator.getVerticalScrollBarPolicy());
        return(false);
    }
    if(((JScrollPane)jScrollPaneOperator.getSource()).getViewport() == null &&
       jScrollPaneOperator.getViewport() == null ||
       ((JScrollPane)jScrollPaneOperator.getSource()).getViewport().equals(jScrollPaneOperator.getViewport())) {
        printLine("getViewport does work");
    } else {
        printLine("getViewport does not work");
        printLine(((JScrollPane)jScrollPaneOperator.getSource()).getViewport());
        printLine(jScrollPaneOperator.getViewport());
        return(false);
    }
    if(((JScrollPane)jScrollPaneOperator.getSource()).getViewportBorder() == null &&
       jScrollPaneOperator.getViewportBorder() == null ||
       ((JScrollPane)jScrollPaneOperator.getSource()).getViewportBorder().equals(jScrollPaneOperator.getViewportBorder())) {
        printLine("getViewportBorder does work");
    } else {
        printLine("getViewportBorder does not work");
        printLine(((JScrollPane)jScrollPaneOperator.getSource()).getViewportBorder());
        printLine(jScrollPaneOperator.getViewportBorder());
        return(false);
    }
    if(((JScrollPane)jScrollPaneOperator.getSource()).getViewportBorderBounds() == null &&
       jScrollPaneOperator.getViewportBorderBounds() == null ||
       ((JScrollPane)jScrollPaneOperator.getSource()).getViewportBorderBounds().equals(jScrollPaneOperator.getViewportBorderBounds())) {
        printLine("getViewportBorderBounds does work");
    } else {
        printLine("getViewportBorderBounds does not work");
        printLine(((JScrollPane)jScrollPaneOperator.getSource()).getViewportBorderBounds());
        printLine(jScrollPaneOperator.getViewportBorderBounds());
        return(false);
    }
    return(true);
}

}
