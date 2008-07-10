package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.*;

import java.awt.*;

import javax.swing.*;

public class jemmy_037 extends JemmyTest {

    public int runIt(Object obj) {

	try {

	    (new ClassReference("org.netbeans.jemmy.testing.Application_037")).startApplication();

	    JFrame win =JFrameOperator.waitJFrame("Application_037", true, true);

	    JFrameOperator fro = new JFrameOperator(win);
	    JTabbedPaneOperator tb = new JTabbedPaneOperator(fro);
	    Thread.sleep(1000);

	    tb.selectPage("Swing");

	    //swing
	    JScrollBarOperator scroll0 = new JScrollBarOperator(fro);
	    scroll0.scrollToMaximum();
	    scroll0.scrollToMinimum();
	    JScrollBarOperator scroll1 = new JScrollBarOperator(fro, 1);
	    scroll1.scrollToMaximum();
	    scroll1.scrollToMinimum();

	    //awt scroll
	    tb.selectPage("AWT");
	    ScrollbarOperator awscroll0 = new ScrollbarOperator(fro);
	    awscroll0.scrollToMaximum();
	    awscroll0.scrollToMinimum();
	    ScrollbarOperator awscroll1 = new ScrollbarOperator(fro, 1);
	    awscroll1.scrollToMaximum();
	    awscroll1.scrollToMinimum();

	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

}
