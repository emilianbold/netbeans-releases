package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.*;

import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;

import java.lang.reflect.InvocationTargetException;

import org.netbeans.jemmy.demo.Demonstrator;

public class jemmy_025 extends JemmyTest {

    JLabel label;

    public int runIt(Object obj) {

	try {
	    (new ClassReference("org.netbeans.jemmy.testing.Application_025")).startApplication();

	    JFrame win =JFrameOperator.waitJFrame("Application_025", true, true);
	    JFrameOperator wino = new JFrameOperator(win);

	    Demonstrator.setTitle("jemmy_025 test");

	    JSliderOperator[] slos = {
		new JSliderOperator(JSliderOperator.
				    findJSlider(win, 0)), 
		new JSliderOperator(JSliderOperator.
				    findJSlider(win, 1)), 
		new JSliderOperator(JSliderOperator.
				    findJSlider(win, 2)), 
		new JSliderOperator(JSliderOperator.
				    findJSlider(win, 3))};

	    for(int i = 0; i < 4; i++) {
		JSliderOperator nEW = new JSliderOperator(wino, i);
		getOutput().printLine("Old : " + slos[i].getSource().toString());
		getOutput().printLine("New : " + nEW.getSource().toString());
		if(slos[i].getSource() !=
		   nEW.getSource()) {
		    getOutput().printErrLine("Should be the same");
		    finalize();
		    return(1);
		}
	    }

	    label = JLabelOperator.findJLabel(win, "0", true, true);

	    int value;
	    for(int i = 0; i < slos.length; i++) {

		Demonstrator.nextStep("Scroll to maximum.\n Slider : " + slos[i].toString());

		slos[i].setScrollModel(slos[i].PUSH_AND_WAIT_SCROLL_MODEL);
		slos[i].scrollToMaximum();

		checkPosition(slos[i].getMaximum());

		value = (int)(2 * (slos[i].getMaximum() - slos[i].getMinimum()) / 3);

		Demonstrator.nextStep("Scroll to " + Integer.toString(value) + " value.\n Slider : " + slos[i].toString());

		slos[i].setScrollModel(slos[i].CLICK_SCROLL_MODEL);
		slos[i].scrollToValue(value);

		checkPosition(value);

		value = (int)((slos[i].getMaximum() - slos[i].getMinimum()) / 3);

		Demonstrator.nextStep("Scroll to " + Integer.toString(value) + " value.\n Slider : " + slos[i].toString());

		slos[i].setScrollModel(slos[i].PUSH_AND_WAIT_SCROLL_MODEL);
		slos[i].scrollToValue(value);

		checkPosition(value);

		Demonstrator.nextStep("Scroll to minimum.\n Slider : " + slos[i].toString());

		slos[i].setScrollModel(slos[i].CLICK_SCROLL_MODEL);
		slos[i].scrollToMinimum();

		checkPosition(slos[i].getMinimum());
	    }

	    if(!testJSlider(slos[0])) {
		finalize();
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

    private void checkPosition(int position) throws TestCompletedException {
	if(!label.getText().equals(Integer.toString(position))) {
	    throw(new TestCompletedException(1, 
					     "Position = " + 
					     label.getText() + 
					     ", expected " + 
					     Integer.toString(position)));
	} else {
	    getOutput().printLine("Correct position: " + label.getText());
	}
    }

public boolean testJSlider(JSliderOperator jSliderOperator) {
    if(((JSlider)jSliderOperator.getSource()).getExtent() == jSliderOperator.getExtent()) {
        printLine("getExtent does work");
    } else {
        printLine("getExtent does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getExtent());
        printLine(jSliderOperator.getExtent());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getInverted() == jSliderOperator.getInverted()) {
        printLine("getInverted does work");
    } else {
        printLine("getInverted does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getInverted());
        printLine(jSliderOperator.getInverted());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getLabelTable() == null &&
       jSliderOperator.getLabelTable() == null ||
       ((JSlider)jSliderOperator.getSource()).getLabelTable().equals(jSliderOperator.getLabelTable())) {
        printLine("getLabelTable does work");
    } else {
        printLine("getLabelTable does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getLabelTable());
        printLine(jSliderOperator.getLabelTable());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getMajorTickSpacing() == jSliderOperator.getMajorTickSpacing()) {
        printLine("getMajorTickSpacing does work");
    } else {
        printLine("getMajorTickSpacing does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getMajorTickSpacing());
        printLine(jSliderOperator.getMajorTickSpacing());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getMaximum() == jSliderOperator.getMaximum()) {
        printLine("getMaximum does work");
    } else {
        printLine("getMaximum does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getMaximum());
        printLine(jSliderOperator.getMaximum());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getMinimum() == jSliderOperator.getMinimum()) {
        printLine("getMinimum does work");
    } else {
        printLine("getMinimum does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getMinimum());
        printLine(jSliderOperator.getMinimum());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getMinorTickSpacing() == jSliderOperator.getMinorTickSpacing()) {
        printLine("getMinorTickSpacing does work");
    } else {
        printLine("getMinorTickSpacing does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getMinorTickSpacing());
        printLine(jSliderOperator.getMinorTickSpacing());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getModel() == null &&
       jSliderOperator.getModel() == null ||
       ((JSlider)jSliderOperator.getSource()).getModel().equals(jSliderOperator.getModel())) {
        printLine("getModel does work");
    } else {
        printLine("getModel does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getModel());
        printLine(jSliderOperator.getModel());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getOrientation() == jSliderOperator.getOrientation()) {
        printLine("getOrientation does work");
    } else {
        printLine("getOrientation does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getOrientation());
        printLine(jSliderOperator.getOrientation());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getPaintLabels() == jSliderOperator.getPaintLabels()) {
        printLine("getPaintLabels does work");
    } else {
        printLine("getPaintLabels does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getPaintLabels());
        printLine(jSliderOperator.getPaintLabels());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getPaintTicks() == jSliderOperator.getPaintTicks()) {
        printLine("getPaintTicks does work");
    } else {
        printLine("getPaintTicks does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getPaintTicks());
        printLine(jSliderOperator.getPaintTicks());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getPaintTrack() == jSliderOperator.getPaintTrack()) {
        printLine("getPaintTrack does work");
    } else {
        printLine("getPaintTrack does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getPaintTrack());
        printLine(jSliderOperator.getPaintTrack());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getSnapToTicks() == jSliderOperator.getSnapToTicks()) {
        printLine("getSnapToTicks does work");
    } else {
        printLine("getSnapToTicks does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getSnapToTicks());
        printLine(jSliderOperator.getSnapToTicks());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getUI() == null &&
       jSliderOperator.getUI() == null ||
       ((JSlider)jSliderOperator.getSource()).getUI().equals(jSliderOperator.getUI())) {
        printLine("getUI does work");
    } else {
        printLine("getUI does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getUI());
        printLine(jSliderOperator.getUI());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getValue() == jSliderOperator.getValue()) {
        printLine("getValue does work");
    } else {
        printLine("getValue does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getValue());
        printLine(jSliderOperator.getValue());
        return(false);
    }
    if(((JSlider)jSliderOperator.getSource()).getValueIsAdjusting() == jSliderOperator.getValueIsAdjusting()) {
        printLine("getValueIsAdjusting does work");
    } else {
        printLine("getValueIsAdjusting does not work");
        printLine(((JSlider)jSliderOperator.getSource()).getValueIsAdjusting());
        printLine(jSliderOperator.getValueIsAdjusting());
        return(false);
    }
    return(true);
}

}
