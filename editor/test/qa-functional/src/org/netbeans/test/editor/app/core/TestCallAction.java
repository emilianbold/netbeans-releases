/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.core;

import org.netbeans.test.editor.app.gui.*;
import java.beans.*;
import javax.swing.SwingUtilities;
import org.netbeans.test.editor.app.util.Scheduler;
import org.netbeans.test.editor.app.core.TestAction;
import org.w3c.dom.Element;

import java.util.Vector;
import java.util.Collection;
/**
 *
 * @author  ehucka
 * @version
 */
public class TestCallAction extends TestAction
/*implements PropertyChangeListener */{
    
    public static final String INPUT="Input";
    public static final String OUTPUT="Output";
    public static final String TOCALL="ToCall";
    public static final String TOSET="ToSet";
    public static final String ENABLE="Enable";
    public static final String REPEAT="Repeat";
    public static final String LOGGERDELAY="Logger_delay";
    
    private String input,output,toCall,toSet;
    private int repeat,loggerDelay;
    
    private boolean enable;
    /** Creates new TestCallAction */
    
    public TestCallAction(int num) {
        this("call"+Integer.toString(num));
    }
    
    public TestCallAction(String name) {
        super(name);
        input="";
        output="";
        toCall="";
        toSet="";
        enable=true;
        loggerDelay=50;
        repeat=1;
    }
    
    public TestCallAction(Element node) {
        super(node);
//        System.err.println("node:" + node);
        input = Test.loadString(node, INPUT);
        output = Test.loadString(node, OUTPUT);
        toCall = Test.loadString(node, TOCALL);
        toSet = Test.loadString(node, TOSET);
        enable = node.getAttribute(ENABLE).equals("true");
        repeat = Integer.parseInt(node.getAttribute(REPEAT));
        loggerDelay = Integer.parseInt(node.getAttribute(LOGGERDELAY));
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        node = Test.saveString(node, INPUT, input);
        node = Test.saveString(node, OUTPUT, output);
        node = Test.saveString(node, TOCALL, toCall);
        node = Test.saveString(node, TOSET, toSet);
        node.setAttribute(ENABLE, enable ? "true" : "false");
        node.setAttribute(REPEAT, Integer.toString(repeat));
        node.setAttribute(LOGGERDELAY, Integer.toString(loggerDelay));
        return node;
    }
    
    public void setRepeat(int i) {
        int oldValue = repeat;
        repeat = i;
        firePropertyChange (REPEAT,new Integer(oldValue),new Integer(repeat));
    }
    
    public void setRep(String s) {
        Integer oldValue = new Integer(repeat);
        try {
            repeat = Integer.parseInt(s);
        } catch (Exception e) {
            Main.log("Bad number format for repeat value");
        }
        firePropertyChange (REPEAT,oldValue,null);
    }
    
    public int getRepeat() {
        return repeat;
    }
    
    public String getRep() {
        return Integer.toString(repeat);
    }
    
    public void setLoggerDelay(int value) {
        int oldValue = repeat;
        loggerDelay = value;
        firePropertyChange (LOGGERDELAY,new Integer(oldValue),new Integer(loggerDelay));
    }
    
    public int getLoggerDelay() {
        return loggerDelay;
    }
    
    public void setDelay(String s) {
        try {
            setLoggerDelay(Integer.parseInt(s));
        } catch (Exception e) {
            Main.log("Bad number format for repeat value");
        }
    }
    
    public String getDelay() {
        return Integer.toString(getLoggerDelay());
    }
    
    public void setInput(String value) {
        String oldValue = input;
        input = value;
        firePropertyChange (INPUT, oldValue, input);
    }
    
    public String getInput() {
        return input;
    }
    
    public void setOutput(String value) {
        String oldValue = output;
        output = value;
        firePropertyChange (OUTPUT, oldValue, output);
    }
    
    public String getOutput() {
        return output;
    }
    
    public void setToCall(String value) {
        String oldValue = toCall;
        toCall = value;
        firePropertyChange (TOCALL, oldValue, toCall);
    }
    
    public String getToCall() {
        return toCall;
    }
    
    public void setToSet(String value) {
        String oldValue = toSet;
        toSet = value;
        firePropertyChange (TOSET, oldValue, toSet);
    }
    
    public String getToSet() {
        return toSet;
    }
    public void setEnabled(boolean value) {
        boolean oldValue = enable;
        enable = value;
        firePropertyChange (ENABLE, new Boolean(oldValue), new Boolean(enable));
    }
    
    public boolean isEnable() {
        return enable;
    }
    
    public String[] getToCalls() {
        TestStep st;
        int stepCount=0;
        for(int i=0;i < owner.getChildCount();i++) {
            
            if (owner.get(i) instanceof TestStep) {
                st=(TestStep)(owner.get(i));
                if (st.isToCall())
                    stepCount++;
            }
        }
        String[] ret=new String[stepCount];
        stepCount=0;
        for(int i=0;i < owner.getChildCount();i++) {
            if (owner.get(i) instanceof TestStep) {
                st=(TestStep)(owner.get(i));
                if (st.isToCall())
                    ret[stepCount++]=st.getName();
            }
        }
        return ret;
    }
    
    public String[] getToSets() {
        TestStep st;
        int stepCount=0;
        for(int i=0;i < owner.getChildCount();i++) {
            
            if (owner.get(i) instanceof TestStep) {
                st=(TestStep)(owner.get(i));
                if (!st.isToCall())
                    stepCount++;
            }
        }
        String[] ret=new String[stepCount];
        stepCount=0;
        for(int i=0;i < owner.getChildCount();i++) {
            if (owner.get(i) instanceof TestStep) {
                st=(TestStep)(owner.get(i));
                if (!st.isToCall())
                    ret[stepCount++]=st.getName();
            }
        }
        return ret;
    }
    
    private TestStep readStepToCall(String toCall) {
        for(int i=0;i < owner.getChildCount();i++) {
            TestNode n = owner.get(i);
            if (n == null)
                System.err.println("Node: "+toCall+" got from owner is null!");
            else {
                if (n.getName().equals(toCall) && n instanceof TestStep) {
                    return (TestStep)n;
                }
            };
        }
        return null;
    }
    
    public void grabInput() {
        Scheduler.getDefault().addTask(new Thread() {
            public void run() {
                String old=input;
                //        Main.editor.lock(getLogger());
                input=Main.editor.getText();
                //        Main.editor.unlock(getLogger());
                firePropertyChange (INPUT,old ,input );
            }
        });
    }
    
    public void grabOutput() {
        Scheduler.getDefault().addTask(new Thread() {
            public void run() {
                String old=output;
                //        Main.editor.lock(getLogger());
                output=Main.editor.getText();
                //        Main.editor.unlock(getLogger());
                firePropertyChange (INPUT,old ,output );
            }
        });
    }
    
    public void perform() {
        if (!enable) return;
        Main.log("Call action: "+name+" starts performing.");
        isPerforming=true;
        Scheduler.getDefault().addTask(new Thread() {
            public void run() {
                if (!enable) return;
                TestStep call;
                TestStep set;
                
                Main.editor.grabFocus();
                Main.editor.requestFocus();
                call=readStepToCall(toCall);
/*                if (call == null) {
                    Main.log("Call action "+name+": bad call step name: "+toCall);
                    return;
                }*/
                set=readStepToCall(toSet);
                if (set != null) {
                    set.perform();
                }
                //        Main.editor.lock(getLogger());
		if (call != null) {
		    getLogger().setDelay(getLoggerDelay());
		    getLogger().clear();
		    for(int i=0;i < repeat;i++) {
			getLogger().loadActions(call);
		    }
		    Main.editor.setText(input);
		    getLogger().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(final java.beans.PropertyChangeEvent p1) {
			    if (p1.getPropertyName().compareTo(Logger.PERFORMING) == 0) {
				if (!((Boolean)(p1.getNewValue())).booleanValue()) {
				    String content=Main.editor.getText();
				    Main.log("Call action: "+name+" finished performing");
				    if (content.compareTo(output) != 0 && !Test.isTesting())
					Main.log("Call action: "+name+" error in comparation outputs*********************");
				    getLogger().removePropertyChangeListener(this);
				    //                Main.editor.unlock(getLogger());
				    isPerforming=false;
				}
			    }
			}});
			getLogger().startPerforming();
		} else {
		    isPerforming = false;
		}
            }
        });
    }
    
    private static final long TIMEOUT = 60 * 1000;
    
    public void performAndWait() {
        perform();
	
	long time = System.currentTimeMillis();
	
	while (isPerforming) {
	    long actualTime = System.currentTimeMillis();
	    
	    if ((actualTime - time) > TIMEOUT) {
		return;
	    }
            Thread.yield();
	}
    }
    
    public Vector getPerformedActions() {
        TestStep set = readStepToCall(toSet);
        TestStep call = readStepToCall(toCall);
        Collection setActions = set == null ? new Vector(0) : set.getChildNodes();
        Collection callActions = call == null ? new Vector(0) : call.getChildNodes();
        Vector res = new Vector(setActions);
        
        res.addAll(callActions);
        return res;
    }
    
    public void stop() {
        getLogger().stopPerforming();
    }
    
}
