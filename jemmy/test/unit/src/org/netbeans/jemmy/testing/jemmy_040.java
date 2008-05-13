package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.EventDispatcher;
import org.netbeans.jemmy.TimeoutExpiredException;

import org.netbeans.jemmy.demo.Demonstrator;

import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.io.PrintWriter;

import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;

public class jemmy_040 extends JemmyTest {
    public int runIt(Object obj) {

	try {

	    try {
		(new ClassReference("org.netbeans.jemmy.testing.Application_040")).startApplication();
	    } catch(ClassNotFoundException e) {
		getOutput().printStackTrace(e);
	    } catch(InvocationTargetException e) {
		getOutput().printStackTrace(e);
	    } catch(NoSuchMethodException e) {
		getOutput().printStackTrace(e);
	    }

	    JFrame win =JFrameOperator.waitJFrame("Application_040", true, true);
	    JFrameOperator wino = new JFrameOperator(win);
	    
	    EventDispatcher.waitQueueEmpty();

	    JMenuBarOperator mbo = new JMenuBarOperator(JMenuBarOperator.findJMenuBar(win));
            mbo.getTimeouts().setTimeout("JMenuOperator.PushMenuTimeout",
                                         600000);
            
            String menuText = "";
            for(int i = 19; i >=0; i--) {
                menuText = menuText + "submenu" + i + "|";
            }
            menuText = menuText + "menuitem";

	    mbo.pushMenu(menuText, "|");

	    JLabelOperator lbo = new JLabelOperator(JLabelOperator.waitJLabel(win, "menu item has been pushed", false, true));
	} catch(TimeoutExpiredException e) {
	    getOutput().printStackTrace(e);
	    finalize();
	    return(1);
	}

	finalize();
	return(0);
    }

}
