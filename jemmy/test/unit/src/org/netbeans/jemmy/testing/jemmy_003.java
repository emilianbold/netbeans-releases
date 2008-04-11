package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.EventDispatcher;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;

import org.netbeans.jemmy.accessibility.AccessibleDescriptionChooser;
import org.netbeans.jemmy.accessibility.AccessibleNameChooser;

import org.netbeans.jemmy.demo.Demonstrator;

import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JProgressBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.io.PrintWriter;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolTip;

public class jemmy_003 extends JemmyTest {
    public int runIt(Object obj) {

	try {
	    (new ClassReference("org.netbeans.jemmy.testing.Application_003")).startApplication();

	    JemmyProperties.push();

	    JemmyProperties.getCurrentTimeouts().loadDebugTimeouts();
	    JemmyProperties.getCurrentTimeouts().print(JemmyProperties.getCurrentOutput().getOutput());

	    EventDispatcher.waitQueueEmpty();

	    JFrame win =JFrameOperator.waitJFrame("Application_003", true, true);
	    JFrameOperator wino = new JFrameOperator(win);

	    Demonstrator.setTitle("jemmy_003 test");

	    JLabelOperator lbo = new JLabelOperator(wino, "Button has not been pushed yet");
            JProgressBarOperator progress = new JProgressBarOperator(wino);

	    for(int i = 1; i < 4; i++) {
		for(int j = 1; j < 4; j++) {
		    String bText = Integer.toString(i) + "-" + Integer.toString(j);
		    Demonstrator.nextStep("Push button " + bText);
		    JButtonOperator bo = new JButtonOperator((JButton)JButtonOperator.findJComponent(win, bText, false, true));
		    AbstractButtonOperator abo = new AbstractButtonOperator(wino, i*4 + j);
		    JButtonOperator bo2 = new JButtonOperator(wino, i*4 + j);
		    if(abo.getSource() != bo.getSource() ||
		       bo2.getSource() != bo.getSource()) {
			getOutput().printError("Wrong");
			getOutput().printErrLine(bo.getSource().toString());
			getOutput().printErrLine(abo.getSource().toString());
			getOutput().printErrLine(bo2.getSource().toString());
			finalize();
			return(1);
		    }
		    JToolTip tt = bo.showToolTip();
		    if(!tt.getTipText().equals(bText + " button")) {
			getOutput().printLine("Wrong tip text: " + tt.getTipText());
			getOutput().printLine("Expected      : " + bText + " button");
			finalize();
			return(1);
		    }
		    bo.push();
		    lbo.waitText("Button \"" + bText + "\" has been pushed");
                    progress.waitValue(bText);
                    progress.waitValue(i*4 + j + 1);
		}
	    }
	    final JButtonOperator bbo = new JButtonOperator(wino, "0-0");
            new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            if(!System.getProperty("java.version").startsWith("1.2")) {
                                bbo.getAccessibleContext().
                                    setAccessibleDescription("A button to check different finding approaches");
                            }
                            bbo.setText("New Text");
                        } catch(InterruptedException e) {
                        }
                    }
                }).start();
            bbo.waitText("New Text");
            if(bbo.getSource() != wino.
               findSubComponent(new AbstractButtonOperator.
                                AbstractButtonByLabelFinder("New Text")) ||
               !System.getProperty("java.version").startsWith("1.2") &&
               bbo.getSource() != wino.
               findSubComponent(new AccessibleNameChooser("New Text")) ||
               !System.getProperty("java.version").startsWith("1.2") &&
               bbo.getSource() != wino.
               waitSubComponent(new AccessibleNameChooser("New Text")) ||
               !System.getProperty("java.version").startsWith("1.2") &&
               bbo.getSource() != 
               new JButtonOperator(wino, 
                                   new AccessibleDescriptionChooser("A button to check different finding approaches")).getSource()) {
                getOutput().printLine("Wrong button found");
                finalize();
                return(1);
            }


	    Demonstrator.showFinalComment("Test passed");

	} catch(Exception e) {
	    getOutput().printStackTrace(e);
	    finalize();
	    JemmyProperties.pop();
	    return(1);
	}

	finalize();
	JemmyProperties.pop();
	return(0);
    }

}
