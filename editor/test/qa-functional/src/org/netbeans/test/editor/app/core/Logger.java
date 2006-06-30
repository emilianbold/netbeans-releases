/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.core;

import org.netbeans.test.editor.app.gui.*;
import java.io.Serializable;
import org.netbeans.modules.editor.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.EditorKit;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serializable;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import org.netbeans.test.editor.app.util.Scheduler;
import javax.swing.SwingUtilities;
import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.core.TestAction;

public class Logger implements Serializable {
    
    static final String COMPLETION_ACTION="completion-action";
    
    static final long serialVersionUID = 8269484241745322111L;
    static final String PERFORMING="Performing";
    static final String LOGGING="Logging";
    
    private Vector actions = new Vector();
    private Vector events = new Vector();
    private Vector testActions = new Vector();
    
    /** Property about delay between steps of simulation */
    private int delay = 20;
    /** Property about logging */
    private boolean logging=false;
    /** Property about running gathered actions */
    private boolean performing=false;
    /** Property change support. */
    private PropertyChangeSupport changeSupport;
    public EventLoggingEditorPane editor;
    
    
    public Logger(EventLoggingEditorPane editor){
        this.editor=(EventLoggingEditorPane)editor;
        changeSupport = new PropertyChangeSupport(this);
    }
    /**
     * Start/Restart the logging of actions into this logger. Everybody interested
     * can hook-up listener
     *
     * @see addPropertyChangeListener
     */
    
    public void startLogging() {
        if( logging == true ) return;
        logging = true;
        editor.setLogger(this);
        firePropertyChange(LOGGING, Boolean.FALSE, Boolean.TRUE );
    }
    
    
    /**
     * Stop the logging of actions into this logger. Everybody interested
     * can hook-up listener
     *
     * @see addPropertyChangeListener
     */
    
    public void stopLogging() {
        if( logging == false ) return;
        logging = false;
        editor.setLogger(null);
        firePropertyChange(LOGGING, Boolean.TRUE, Boolean.FALSE );
    }
    
    /**
     * Returns the actual state of logging.
     */
    
    public boolean isLogging() {
        return logging;
    }
    
    /**
     * Start performing of logged actions from beginning. Everybody interested
     * can hook-up listener.
     *
     * @see addPropertyChangeListener
     */
    
    public synchronized void startPerforming() {
        if( performing == true ) return;
        performing = true;
        firePropertyChange(PERFORMING, Boolean.FALSE, Boolean.TRUE );
        System.err.println("creating SimulationPerformer");
        Thread sim = new SimulationPerformer( delay, this );
        sim.start();
    }
    
    /**
     * Stop the logging of actions into this logger. Everybody interested
     * can hook-up listener.
     *
     * @see addPropertyChangeListener
     */
    
    public synchronized void stopPerforming() {
        if( performing == false ) return;
        performing = false;
        firePropertyChange(PERFORMING, Boolean.TRUE, Boolean.FALSE );
    }
    
    /**
     * Returns the actual state of logging.
     */
    
    public synchronized boolean isPerforming() {
        return performing;
    }
    
    /**
     * Forget all the bufferred actions
     */
    
    public void setDelay(int value) {
        delay=value;
    }
    
    public int getDelay() {
        return delay;
    }
    
    public void clear() {
        actions = new Vector();
        events = new Vector();
        testActions = new Vector();
    }
    
    public TestNode[] saveActions( TestStep step ) {
        TestNode[] nodes;
        String name;
        String cmd;
        
        nodes=new TestNode[actions.size()];
        for( int i=0; i < actions.size(); i++ ) {
            name=(String)(actions.get(i));
            if (name.compareTo(COMPLETION_ACTION) != 0) {
                cmd = ((ActionEvent)events.get(i)).getActionCommand();
                if (cmd == null) cmd="";
                nodes[i]=new TestLogAction(name,cmd);
            } else {
                nodes[i]=new TestCompletionAction(name,(String)(events.get(i)));
            }
        }
        step.addNodes(nodes);
        return nodes;
    }
    
/*    public void loadAction(TestLogAction action) {
        actions.add(action.getName());
        events.add(new ActionEvent(editor,ActionEvent.ACTION_PERFORMED,
        action.getCommand()));
    }
 
    public void loadActions(TestStep step) {
        TestLogAction a;
        TestAction ta;
 
        for(int i=0;i < step.getChildCount();i++) {
            a=(TestLogAction)(step.get(i));
            loadAction(a);
        }
   }*/
    
    public void loadActions(TestStep step) {
        for(int i=0;i < step.getChildCount();i++) {
            testActions.add(step.get(i));
        }
    }
    /**
     * Add a {@link PropertyChangeListener} to the listener list.
     *
     * @param listener  the <code>PropertyChangeListener</code> to be added
     */
    public synchronized void addPropertyChangeListener( PropertyChangeListener listener ) {
        if( changeSupport == null ) changeSupport = new java.beans.PropertyChangeSupport( this );
        changeSupport.addPropertyChangeListener( listener );
    }
    /**
     * Remove a {@link PropertyChangeListener} from the listener list.
     *
     * @param listener  the <code>PropertyChangeListener</code> to be removed
     */
    public synchronized void removePropertyChangeListener( PropertyChangeListener listener ) {
        if( changeSupport != null ) changeSupport.removePropertyChangeListener( listener );
    }
    /**
     * Fire a {@link PropertyChangeEvent} to each listener.
     *
     * @param propertyName the programmatic name of the property that was changed
     * @param oldValue the old value of the property
     * @param newValue the new value of the property
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (changeSupport != null) changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    
    /**
     * Add an Action/ActionEvent pair to the list of events
     */
    public void logAction( Action a, ActionEvent evt ) {
        if( logging && a.getValue(Action.NAME) != null) {
            actions.add( a.getValue( Action.NAME ) );
            events.add( evt );
        }
    }
    
    /**
     * Add an Action/ActionEvent pair to the list of events
     */
    public void logCompletionAction(String name) {
        actions.add(COMPLETION_ACTION);
        events.add(name);
    }
    
    public void performAction(TestStringAction act) {
        String s=act.getString();
        Action a = (Action)editor.namesToActions.get(TestStringAction.STRINGED_NAME);
        if (a == null) return;
        
        for (int i=0;i < s.length();i++) {
            ActionEvent evt = new ActionEvent(editor,ActionEvent.ACTION_PERFORMED, new String(new char[] {s.charAt(i)}));
            editor.grabFocus();
            a.actionPerformed(evt);
        }
    }
    
    public void performAction(TestCompletionAction act) {
        String c=act.getCommand();
        Action a=editor.getCompletion().getJDCPopupPanel().getActionMap().get(c);
        if (a == null) return;
        editor.grabFocus();
        a.actionPerformed(new ActionEvent(editor,ActionEvent.ACTION_PERFORMED,""));
    }
    
    public void performAction(TestLogAction act) {
        Action a = (Action)editor.namesToActions.get(act.getName());
        if (a == null) return;
        ActionEvent evt = new ActionEvent(editor,ActionEvent.ACTION_PERFORMED,
        act.getCommand());
        editor.grabFocus();
        a.actionPerformed(evt);
    }
    ////////////////////////////////////////////////////////////////////////////
    private void performAction(int index) {
       /* if (index+1 < testActions.size() && testActions.get(index) instanceof TestCompletionAction &&
        testActions.get(index) instanceof TestCompletionAction) {
            new Thread() {
                int time=20;
                public void run() {
                    try {
                        sleep(50);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    System.err.println("CC has index: "+editor.getCompletion().getView().getSelectedIndex());
                }
            }.start();
        }*/
        
        if (editor != Main.frame.getEditor()) {
            System.err.println("Logger Editor isn't same as in MainFrame.");
            System.err.println("Logger.editor="+editor);
            System.err.println("Main.Frame.editor="+Main.frame.getEditor());
            editor = Main.frame.getEditor();
        }
        TestAction ta=(TestAction)(testActions.get(index));
        if (ta instanceof TestLogAction)
            performAction((TestLogAction)ta);
        else if (ta instanceof TestStringAction)
            performAction((TestStringAction)ta);
        else if (ta instanceof TestCompletionAction)
            performAction((TestCompletionAction)ta);
    }
    
    private class SimulationPerformer extends Thread {
        private int delay;
        //private boolean performing;
        private Logger master;
        
        SimulationPerformer( int delay, Logger master ) {
            super();
            this.delay = delay;
            this.master = master;
        }
        
        public void run() {
            System.err.println("SimulationPerformer started.");
            try {
                System.err.println("Logger: Starts performing.");
                for( int i=0; i < testActions.size(); i++ ) {
                    if (!master.isPerforming()) break;
                    final int cntr = i;
                    final boolean isLast = (cntr + 1) == testActions.size();
                    try {
                        sleep(delay);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    
                    Scheduler.getDefault().addTask(new Thread() {
                        private boolean last = isLast;
                        
                        public void run() {
                            if (!master.isPerforming())
                                return;
                            performAction(cntr);
                            if (last) {
                                System.err.println("Logger: Stops performing.");
                                master.stopPerforming();
                            }
                        }
                    });
                    //special timeout for completion-show action
                    if (testActions.get(cntr) instanceof TestLogAction &&
                    ((TestLogAction)(testActions.get(cntr))).getName().compareTo("completion-show") == 0) {
                        int time=20;
                        //wait max two seconds for completion
                        while (!editor.getCompletion().isPaneVisible() && time > 0) {
                            try {
                                sleep(100);
                            } catch (InterruptedException ex) {
                                time=0;
                            }
                            time--;
                        }
                        if (!editor.getCompletion().isPaneVisible()) {
                            System.err.println("Warning: Completion isn't visible after \"completion-show\" action.");
                        }
                    }
                }
            } catch (Throwable e) {
                System.err.println("Throwable: " + e);
            }
        }
    }  // SimulationPerformer
}

