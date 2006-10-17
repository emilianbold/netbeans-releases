package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.*;

import java.awt.*;

public class JemmyTest extends org.netbeans.jemmy.Test {
    public void finalize() {
        PNGEncoder.captureScreen(System.getProperty("user.dir") +
                                 System.getProperty("file.separator") +
                                 "screen.png");
        JemmyProperties.setCurrentTimeout("QueueTool.LockTimeout", 60000);
	Window win;
	while((win = WindowWaiter.getWindow(new org.netbeans.jemmy.ComponentChooser() {
		public boolean checkComponent(Component comp) {
		    return(comp.isVisible());
		}
		public String getDescription() {
		    return("Any window");
		}
	    })) != null) {
	    (new WindowOperator(win)).close();
	}
	try {
	    Thread.currentThread().sleep(3000);
	} catch(InterruptedException e) {}
    }
    protected void printLine(Object obj) {
	getOutput().printLine(obj.toString());
    }
    protected void printLine(char s) {
	getOutput().printLine(new Character(s).toString());
    }
    protected void printLine(byte s) {
	getOutput().printLine(Byte.toString(s));
    }
    protected void printLine(int s) {
	getOutput().printLine(Integer.toString(s));
    }
    protected void printLine(long s) {
	getOutput().printLine(Long.toString(s));
    }
    protected void printLine(float s) {
	getOutput().printLine(Float.toString(s));
    }
    protected void printLine(double s) {
	getOutput().printLine(Double.toString(s));
    }
    protected void printLine(boolean s) {
	getOutput().printLine(s ? "true" : "false"); // NOI18N
    }
}
