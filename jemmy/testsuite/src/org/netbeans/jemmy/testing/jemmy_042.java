package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.*;

import org.netbeans.jemmy.drivers.*;

import java.awt.*;

import javax.swing.*;

public class jemmy_042 extends JemmyTest {
    public int runIt(Object obj) {

	try {

	    (new ClassReference("org.netbeans.jemmy.testing.Application_042")).startApplication();

	    JDialogOperator wino = new JDialogOperator("Application_042");


            getOutput().printLine("Using driver:");
            getOutput().printLine(DriverManager.getMenuDriver(JMenuBarOperator.class).getClass().getName());

            JMenuBarOperator mbo = new JMenuBarOperator(wino);
            mbo.showMenuItem("menu0|submenu00", "|");
            mbo.showMenuItem("menu1|submenu10", "|");
            mbo.showMenuItem("menu0|submenu00|item00", "|");
            mbo.showMenuItem("menu1|submenu10|item10", "|");
            mbo.showMenuItem("menu0|submenu01", "|");
            mbo.showMenuItem("menu1|submenu11", "|");
            mbo.showMenuItem("menu0|submenu01|item01", "|");
            mbo.showMenuItem("menu1|submenu11|item11", "|");

            new JComboBoxOperator(wino, 0).selectItem(3);
            new JComboBoxOperator(wino, 1).selectItem(3);


	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

}
