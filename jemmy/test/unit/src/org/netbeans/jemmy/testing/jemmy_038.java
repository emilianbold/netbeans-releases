package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.*;

import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;

import java.io.PrintWriter;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;

import javax.swing.text.JTextComponent;

import java.lang.reflect.InvocationTargetException;

import org.netbeans.jemmy.demo.Demonstrator;

public class jemmy_038 extends JemmyTest {
    public int runIt(Object obj) {

	try {
	    (new ClassReference("org.netbeans.jemmy.testing.Application_002")).startApplication();

	    JFrame win =JFrameOperator.waitJFrame("Application_002", true, true);

	    JFrameOperator fo = new JFrameOperator(win);

	    fo.activate();
	    fo.resize(400, 400);
	    fo.move(200, 200);
	    fo.maximize();
	    fo.demaximize();
	    fo.iconify();
	    fo.deiconify();
	    fo.close();

	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }
}
