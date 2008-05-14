package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class Application_009 extends TestFrame {
    
    private int index = 0;

    public Application_009(int index) {
	super("Application_009/" + Integer.toString(index));

	this.index = index;
	
	setSize(300, 300);

	setLocation(index * 50, index * 50);
    }

    public int getIndex() {
	return(index);
    }

    public static void main(String[] argv) {
	try {
	    (new Application_009(0)).show();
	    Thread.currentThread().sleep(3000);
	    (new Application_009(1)).show();
	    Thread.currentThread().sleep(3000);
	    (new Application_009(2)).show();
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }

}
