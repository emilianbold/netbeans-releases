package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;

import java.awt.*;

import org.netbeans.jemmy.util.Dumper;

public class JemmyTest extends org.netbeans.jemmy.Test {
    public void finalize() {
	try {
	    JemmyProperties.setCurrentTimeout("QueueTool.LockTimeout", 60000);
	    Dumper.dumpAll(System.getProperty("user.dir") +
			   System.getProperty("file.separator") +
			   "dump.xml");
	    if(!System.getProperty("java.version").startsWith("1.2")) {
		Object[] params = {System.getProperty("user.dir") +
				   System.getProperty("file.separator") +
				   "screen.png"};
		Class[] classes = {params[0].getClass()};
		new ClassReference("org.netbeans.jemmy.util.PNGEncoder").
		    invokeMethod("captureScreen",
				 params,
				 classes);
	    }
	} catch(java.io.FileNotFoundException e) {
	    getOutput().printStackTrace(e);
	} catch(ClassNotFoundException e) {
	    getOutput().printStackTrace(e);
	} catch(java.lang.reflect.InvocationTargetException e) {
	    getOutput().printStackTrace(e);
	} catch(NoSuchMethodException e) {
	    getOutput().printStackTrace(e);
	} catch(IllegalAccessException e) {
	    getOutput().printStackTrace(e);
	}
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
	getOutput().printLine(new Boolean(s).toString());
    }
}
