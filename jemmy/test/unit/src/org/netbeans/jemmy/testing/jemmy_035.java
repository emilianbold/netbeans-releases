package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.demo.Demonstrator;

import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.*;

import java.awt.Adjustable;
import java.awt.Button;
import java.awt.ScrollPane;
import java.awt.Component;

import java.io.PrintWriter;

import javax.swing.JFrame;

import java.lang.reflect.InvocationTargetException;

public class jemmy_035 extends JemmyTest {

    private boolean checkInside(Component comp, ComponentOperator area, 
				int x, int y, int width, int height) {
	double compLeft = comp.getLocationOnScreen().getX() + x;
	double compTop = comp.getLocationOnScreen().getY() + y;
	double compRight = compLeft + width;
	double compBottom = compTop + height;
	double areaLeft = area.getSource().getLocationOnScreen().getX();
	double areaTop = area.getSource().getLocationOnScreen().getY();
	double areaRight = areaLeft + area.getSource().getWidth();
	double areaBottom = areaTop + area.getSource().getHeight();
	return(compLeft >= areaLeft && compRight <= areaRight &&
	       compTop >= areaTop && compBottom <=areaBottom);
    }

    private boolean checkInside(Component comp, ComponentOperator area) {
	return(checkInside(comp, area, 0, 0, comp.getWidth(), comp.getHeight()));
    }

    public int runIt(Object obj) {

	try {

	    try {
		(new ClassReference("org.netbeans.jemmy.testing.Application_035")).startApplication();
	    } catch(ClassNotFoundException e) {
		getOutput().printStackTrace(e);
	    } catch(InvocationTargetException e) {
		getOutput().printStackTrace(e);
	    } catch(NoSuchMethodException e) {
		getOutput().printStackTrace(e);
	    }

	    ComponentOperator.setDefaultComponentVisualizer(new EmptyVisualizer());

	    JFrame win =JFrameOperator.waitJFrame("Application_035", true, true);

	    Button butt00 = ButtonOperator.findButton(win, "00", true, true);
	    Button butt04 = ButtonOperator.findButton(win, "04", true, true);
	    Button butt11 = ButtonOperator.findButton(win, "11", true, true);
	    Button butt22 = ButtonOperator.findButton(win, "22", true, true);
	    Button butt24 = ButtonOperator.findButton(win, "24", true, true);
	    Button butt33 = ButtonOperator.findButton(win, "33", true, true);
	    Button butt44 = ButtonOperator.findButton(win, "44", true, true);
	    Button butt42 = ButtonOperator.findButton(win, "42", true, true);
	    Button butt40 = ButtonOperator.findButton(win, "40", true, true);

	    ScrollPaneOperator scroller = new ScrollPaneOperator(new JFrameOperator(win));

	    if(scroller.getSource() != ScrollPaneOperator.findScrollPaneUnder(butt00)) {
		getOutput().printErrLine("One of two methods: findScrollPane or findUnder does not work correcly");
		return(1);
	    }

	    Demonstrator.setTitle("jemmy_035 test");

	    Demonstrator.nextStep("Set scroller values to maximum");

	    scroller.setValues(scroller.getHAdjustable().getMaximum(), 
			       scroller.getVAdjustable().getMaximum());

	    if(!checkInside(butt44, scroller)) {
		getOutput().printErrLine("44 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Set scroller values to (0, 0)");

	    scroller.setValues(0, 0);

	    if(!checkInside(butt00, scroller)) {
		getOutput().printErrLine("00 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to the center of \"22\" button");

	    scroller.scrollToComponentPoint(butt22, 
					    butt22.getWidth() / 2,
					    butt22.getHeight() / 2);

	    if(!checkInside(butt22, scroller, 
			    butt22.getWidth() / 2 - 1,
			    butt22.getHeight() / 2 - 1,
			    2,
			    2)) {
		getOutput().printErrLine("22 button center is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to right");

	    scroller.scrollToRight();

	    if(!checkInside(butt24, scroller, 
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

	    if(!checkInside(butt22, scroller, x22, y22, w22, h22)) {
		getOutput().printErrLine("22 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to bottom");

	    scroller.scrollToBottom();

	    int x42 = 10;
	    int y42 = 10;
	    int w42 = butt42.getWidth() - 20;
	    int h42 = butt42.getHeight() - 20;
	    if(!checkInside(butt42, scroller, x42, y42, w42, h42)) {
		getOutput().printErrLine("42 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to left");

	    scroller.scrollToLeft();

	    if(!checkInside(butt40, scroller)) {
		getOutput().printErrLine("40 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to top");

	    scroller.scrollToTop();

	    if(!checkInside(butt00, scroller)) {
		getOutput().printErrLine("00 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to center");

	    scroller.scrollToValues(0.5, 0.5);

	    if(!checkInside(butt22, scroller,
			    butt22.getWidth() / 2 - 1,
			    butt22.getHeight() / 2 - 1,
                            1,
                            1)) {
                                
		getOutput().printErrLine("22 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to \"11\" button");

	    scroller.scrollToComponent(butt11);


	    if(!checkInside(butt11, scroller)) {
		getOutput().printErrLine("11 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to \"33\" button");

	    scroller.scrollToComponent(butt33);

	    if(!checkInside(butt33, scroller)) {
		getOutput().printErrLine("33 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.nextStep("Scroll to horizontal center");

	    scroller.scrollToHorizontalValue(0.5);

	    Demonstrator.nextStep("Scroll to vertical center");

	    scroller.scrollToVerticalValue(0.5);

	    if(!checkInside(butt22, scroller)) {
		getOutput().printErrLine("22 button is not inside scroll area!");
		return(1);
	    }

	    Demonstrator.showFinalComment("Test passed");

	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

}
