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
import org.netbeans.editor.ext.*;
import org.netbeans.modules.editor.options.JavaOptions;
import org.openide.options.SystemOption;
import org.netbeans.test.editor.app.util.Scheduler;
import javax.swing.SwingUtilities;

public class Logger implements Serializable {
    
    static final long serialVersionUID = 8269484241745322111L;
    static final String PERFORMING="Performing";
    static final String LOGGING="Logging";
    
    private Vector actions = new Vector();
    private Vector events = new Vector();
    
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
        firePropertyChange(LOGGING, new Boolean( false ), new Boolean( true ) );
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
        firePropertyChange(LOGGING, new Boolean( true ), new Boolean( false ) );
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
        firePropertyChange(PERFORMING, new Boolean( false ), new Boolean( true ) );
        System.err.println("creating SimulationPerformer");
        Thread sim = new SimulationPerformer( delay, (Vector)actions.clone(), (Vector)events.clone(), this );
        
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
        firePropertyChange(PERFORMING, new Boolean( true ), new Boolean( false ) );
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
    }
    
    public void saveActions( TestStep step ) {
        TestNode[] nodes;
        
        nodes=new TestNode[actions.size()];
        for( int i=0; i < actions.size(); i++ ) {
            String cmd = ((ActionEvent)events.get(i)).getActionCommand();
            if (cmd == null) cmd="";
            nodes[i]=new TestLogAction((String)(actions.get(i)),cmd);
        }
        step.addNodes(nodes);
    }
    
    public void loadAction(TestLogAction action) {
        actions.add(action.getName());
        events.add(new ActionEvent(editor,ActionEvent.ACTION_PERFORMED,
        action.getCommand()));
    }
    
    public void loadActions(TestStep step) {
        TestLogAction a;
        
        for(int i=0;i < step.getChildCount();i++) {
            a=(TestLogAction)(step.get(i));
            loadAction(a);
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
//        System.err.println("logged action, logging=" + logging);
        if( logging ) {
            actions.add( a.getValue( Action.NAME ) );
            events.add( evt );
        }
    }
    
    public void performAction(TestLogAction act) {
        Action a = (Action)editor.namesToActions.get(act.getName());
        if (a == null) return;
        ActionEvent evt = new ActionEvent(editor,ActionEvent.ACTION_PERFORMED,
        act.getCommand());
        editor.grabFocus();
        a.actionPerformed(evt);
    }
    
    private void performAction(int index) {
        Action a = (Action)editor.namesToActions.get((String)actions.get(index));
        ActionEvent evt = (ActionEvent)events.get(index);
        editor.grabFocus();
        if (evt.getSource() != editor) {
            throw new IllegalArgumentException("evt.getSource() != editor!");
        }
        a.actionPerformed(evt);
    }
    
    private class SimulationPerformer extends Thread {
        private Vector actions;
        private Vector events;
        private int delay;
        private boolean performing;
        private Logger master;
        
        SimulationPerformer( int delay, Vector actions, Vector events, Logger master ) {
            super();
            this.delay = delay;
            this.actions = actions;
            this.events = events;
            this.master = master;
        }
        
        //        private int howMuchFinished;
        
        public void run() {
            System.err.println("SimulationPerformer started.");
            try {
                JavaOptions opts = (JavaOptions)(SystemOption.findObject(JavaOptions.class));
                int compdelay= opts.getCompletionAutoPopupDelay();
                Main.log("Logger: Starts performing.");
                //              howMuchFinished = 0;
                for( int i=0; i < actions.size(); i++ ) {
                    final int cntr = i;
                    final boolean isLast = (cntr + 1) == actions.size();
                    
                    //                        System.err.println("Putting task: " + cntr + ".");
                        Scheduler.getDefault().addTask(new Thread() {
                            private boolean last = isLast;
                            
                            public void run() {
                                if (!master.isPerforming())
                                    return;
                                performAction(cntr);
                                //                            howMuchFinished++;
                                if (last) {
                                    Main.log("Logger: Stops performing.");
                                    master.stopPerforming();
                                };
                            }
                        });
                };
            } catch (Throwable e) {
                System.out.println("Throwable: " + e);
            };
        }
    }  // SimulationPerformer
}

