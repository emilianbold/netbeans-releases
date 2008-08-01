package org.netbeans.jemmy.testing;

import java.util.*;

import javax.swing.*;

import org.netbeans.jemmy.drivers.scrolling.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.JSpinnerOperator.*;

public class jemmy_047 extends JemmyTest {
    public int runIt(Object obj) {
        try {
            Application_047.main(new String[0]);

            JFrameOperator mainFrame = new JFrameOperator("Application_047");

            JSpinnerOperator first = new JSpinnerOperator(mainFrame);

            first.scrollToObject(new Integer(50), ScrollAdjuster.INCREASE_SCROLL_DIRECTION);

            first.scrollToString("11", ScrollAdjuster.DECREASE_SCROLL_DIRECTION);

            try {
                first.scrollToMaximum();
                getOutput().printErrLine("A SpinnerModelException should be thown");
                finalize();
                return(1);
            } catch(SpinnerModelException ex) {
                getOutput().printLine("Expected exception has been thrown");
            }

            try {
                first.scrollToMinimum();
                getOutput().printErrLine("A SpinnerModelException should be thown");
                finalize();
                return(1);
            } catch(SpinnerModelException ex) {
                getOutput().printLine("Expected exception has been thrown");
            }

            DateSpinnerOperator second = new JSpinnerOperator(mainFrame, 1).getDateSpinner();

            if(!second.getSource().equals(new JSpinnerOperator(mainFrame, second.getValue().toString()).getSource())) {
                getOutput().printErrLine("JSpinner was not found by selected value");
                finalize();
                return(1);
            }

            Calendar today = Calendar.getInstance();
            today.set(Calendar.DAY_OF_MONTH, 1);
            today.set(Calendar.MONTH, Calendar.NOVEMBER);
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.set(Calendar.DAY_OF_MONTH, 1);
            tomorrow.set(Calendar.MONTH, Calendar.DECEMBER);
            Calendar yesterday = Calendar.getInstance();
            yesterday.set(Calendar.DAY_OF_MONTH, 1);
            yesterday.set(Calendar.MONTH, Calendar.OCTOBER);

            second.scrollToDate(today.getTime());
            second.scrollToDate(tomorrow.getTime());
            second.scrollToDate(yesterday.getTime());

            try {
                second.getNumberSpinner();
                getOutput().printErrLine("A SpinnerModelException should be thown");
                finalize();
                return(1);
            } catch(SpinnerModelException ex) {
                getOutput().printLine("Expected exception has been thrown");
            }

            ListSpinnerOperator third = new JSpinnerOperator(mainFrame, "one").getListSpinner();

            third.scrollToMaximum();
            third.scrollToMinimum();

            third.scrollToString("two");

            NumberSpinnerOperator fourth = new JSpinnerOperator(mainFrame, 3).getNumberSpinner();

            fourth.scrollToMaximum();
            fourth.scrollToMinimum();

            fourth.scrollToValue(3.01);

            fourth.scrollToValue(new Float(6.99));

	} catch(Exception e) {
	    getOutput().printStackTrace(e);
	    finalize();
	    return(1);
	}
        return(0);
    }
}
