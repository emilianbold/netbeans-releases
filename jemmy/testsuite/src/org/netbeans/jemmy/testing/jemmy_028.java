package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.demo.Demonstrator;

import org.netbeans.jemmy.operators.*;

import org.netbeans.jemmy.util.*;

import java.awt.*;

import java.io.PrintWriter;

import javax.swing.*;
import javax.swing.tree.*;

import java.lang.reflect.InvocationTargetException;

public class jemmy_028 extends JemmyTest {

    public int runIt(Object obj) {

	try {

	    ComponentOperator.
		setDefaultComponentVisualizer(new DefaultVisualizer());
	    ((DefaultVisualizer)ComponentOperator.
	     getDefaultComponentVisualizer()).switchTab(true);
	    ((DefaultVisualizer)ComponentOperator.
	     getDefaultComponentVisualizer()).scroll(true);
	    (new ClassReference("org.netbeans.jemmy.testing.Application_028")).startApplication();

	    JFrame win =JFrameOperator.waitJFrame("Right one", true, true);

	    JTextField trg = ((Application_028)win).getTarget();
	    JTextFieldOperator trgo = new JTextFieldOperator(trg);
	    trgo.clearText();
	    trgo.typeText("Text supposed to be typed");

	    JTextFieldOperator.waitJTextField(win, "Text supposed to be typed", true, true);

	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

}
