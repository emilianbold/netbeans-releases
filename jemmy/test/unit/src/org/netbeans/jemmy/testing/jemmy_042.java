package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.*;

import org.netbeans.jemmy.drivers.*;

import java.awt.*;

import javax.swing.*;

public class jemmy_042 extends JemmyTest {
    JMenuBarOperator mbo;
    public int runIt(Object obj) {

	try {

	    (new ClassReference("org.netbeans.jemmy.testing.Application_042")).startApplication();

	    JDialogOperator wino = new JDialogOperator("Application_042");


            getOutput().printLine("Using driver:");
            getOutput().printLine(DriverManager.getMenuDriver(JMenuBarOperator.class).getClass().getName());

            mbo = new JMenuBarOperator(wino);
            getOutput().print("Checking root contents");
            if(!checkItems("", new String[] {"menu0", "menu1"})) {
                finalize();
                return(1);
            }
            getOutput().print("Checking menu0 contents");
            if(!checkItems("menu0", new String[] {"submenu00", "submenu01"})) {
                finalize();
                return(1);
            }
            getOutput().print("Checking menu0|submenu00 contents");
            if(!checkItems("menu0|submenu00", new String[] {"item00"})) {
                finalize();
                return(1);
            }

            mbo.showMenuItem("menu0|submenu00", "|");
            mbo.showMenuItem("menu1|submenu10", "|");
            mbo.showMenuItem("menu0|submenu00|item00", "|");
            mbo.showMenuItem("menu1|submenu10|item10", "|");
            mbo.showMenuItem("menu0|submenu01", "|");
            mbo.showMenuItem("menu1|submenu11", "|");
            mbo.showMenuItem("menu0|submenu01|item01", "|");
            mbo.showMenuItem("menu1|submenu11|item11", "|");
            mbo.closeSubmenus();

            new JComboBoxOperator(wino, 0).selectItem(3);
            new JComboBoxOperator(wino, 1).selectItem(3);


	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

    boolean checkItems(String path, String[] itemTexts) {
        JMenuItemOperator[] items = mbo.showMenuItems(path, "|");
        if(items.length != itemTexts.length) {
            getOutput().printError("Wrong items count.");
            return(false);
        }
        for(int i = 0; i < itemTexts.length; i++) {
            if(!items[i].getText().equals(itemTexts[i])) {
                getOutput().printError("Wrong " + i + "`th item: " + items[i].getText());
                getOutput().printError("Expected:                " + itemTexts[i]);
                return(false);
            }
        }
        return(true);
    }

}
