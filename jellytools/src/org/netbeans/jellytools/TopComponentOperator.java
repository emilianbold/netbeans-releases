/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools;

import java.awt.Component;
import java.util.Iterator;
import javax.swing.JComponent;
import org.netbeans.jellytools.actions.CloneViewAction;
import org.netbeans.jellytools.actions.DockingAction;
import org.netbeans.jellytools.actions.UndockAction;

import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.Operator;

import org.openide.windows.TopComponent;

/**
 * Represents org.openide.windows.TopComponent. It is IDE wrapper for a lot of
 * panels in IDE which can be docked to a window. TopComponent is for example
 * Filesystems tab in the Explorer, every tab in the Source Editor, every tab
 * in the Output Window, Property sheet and many more.
 *
 * <p>
 * Usage:<br>
 * <pre>
 *      EditorWindowOperator eo = new EditorWindowOperator();
 *      eo.selectPage("TopComponentOperator");
 *      TopComponentOperator tco = new TopComponentOperator(eo, "TopComponentOperator");
 *      tco.dockViewInto("Explorer|"+DockingAction.RIGHT);
 *      Thread.sleep(1000);
 *      tco.undockView();
 *      Thread.sleep(1000);
 *      tco.dockViewInto("Source Editor|"+DockingAction.CENTER);
 *      Thread.sleep(1000);
 *      tco.cloneView();
 *      Thread.sleep(1000);
 *      tco.close();
 * </pre>
 *
 * @author Adam.Sotona@sun.com
 * @author Jiri.Skrivanek@sun.com
 */
public class TopComponentOperator extends JComponentOperator {
    
    static {
        // need to set timeout for the case it was not set previously
        JemmyProperties.getCurrentTimeouts().initDefault("EventDispatcher.RobotAutoDelay", 0);
        DriverManager.setDriver(DriverManager.MOUSE_DRIVER_ID, 
        new MouseRobotDriver(JemmyProperties.getCurrentTimeouts().create("EventDispatcher.RobotAutoDelay"), 
        new Class[] {TopComponentOperator.class}));
    }
    
    /** Waits for index-th TopComponent with given name in specified container.
     * @param contOper container where to search
     * @param topComponentName name of TopComponent (it used to be label of tab)
     * @param index index of TopComponent to be find
     */
    public TopComponentOperator(ContainerOperator contOper, String topComponentName, int index) {
        super((JComponent)waitComponent(contOper, new TopComponentChooser(topComponentName, contOper.getComparator()), index));
        copyEnvironment(contOper);
    }
    
    /** Waits for TopComponent with given name in specified container.
     * @param contOper container where to search
     * @param topComponentName name of TopComponent (it used to be label of tab)
     */
    public TopComponentOperator(ContainerOperator contOper, String topComponentName) {
        this(contOper, topComponentName, 0);
    }
    
    /** Waits for index-th TopComponent in specified container.
     * @param contOper container where to search
     * @param index index of TopComponent to be find
     */
    public TopComponentOperator(ContainerOperator contOper, int index) {
        this(contOper, null, index);
    }
    
    /** Waits for first TopComponent in specified container.
     * @param contOper container where to search
     */
    public TopComponentOperator(ContainerOperator contOper) {
        this(contOper, null, 0);
    }
    
    /** Waits for index-th TopComponent with given name in whole IDE.
     * @param topComponentName name of TopComponent (it used to be label of tab)
     * @param index index of TopComponent to be find
     */
    public TopComponentOperator(String topComponentName, int index) {
        this(waitTopComponent(topComponentName, index));
    }
    
    /** Waits for first TopComponent with given name in whole IDE.
     * @param topComponentName name of TopComponent (it used to be label of tab)
     */
    public TopComponentOperator(String topComponentName) {
        this(topComponentName, 0);
    }
    
    /** Creates new instance from given TopComponent instance.
     * @param topComponent TopComponent instance
     */
/*
    public TopComponentOperator(TopComponent topComponent) {
        super(topComponent);
    }
*/  
  
    /** Creates new instance from given JComponent.
     * This constructor is used in properties.PropertySheetOperator.
     * @param jComponent instance of JComponent
     */
    public TopComponentOperator(JComponent jComponent) {
        super(jComponent);
    }
    
    /** Docks this TopComponent into new host. TopComponent is focused before
     * action is performed.
     * @param newLocationPath path where to dock TopComponent (e.g. "Explorer|Center"
     * or "DockingAction.NEW_SINGLE_FRAME")
     */
    public void dockViewInto(String newLocationPath) {
        makeComponentVisible();
        ((TopComponent)getSource()).requestFocus();
        // need to wait a little
        new EventTool().waitNoEvent(500);
        new DockingAction(newLocationPath).perform();
    }
    
    /** Undocks this TopComponent. TopComponent is focused before
     * action is performed. */
    public void undockView() {
        makeComponentVisible();
        ((TopComponent)getSource()).requestFocus();
        // need to wait a little
        new EventTool().waitNoEvent(500);
        new UndockAction().perform();
    }
    
    /** Clones this TopComponent. TopComponent is focused before
     * action is performed. */
    public void cloneView() {
        makeComponentVisible();
        ((TopComponent)getSource()).requestFocus();
        // need to wait a little
        new EventTool().waitNoEvent(500);
        new CloneViewAction().perform();
    }
    
    /** Closes this TopComponent instance by IDE API call. */
    public void close() {
        // used direct call of IDE API method because CloseViewAction closes
        // active TopComponent and not neccesarily this one
        ((TopComponent)getSource()).close();
    }
    
    /** Finds index-th TopComponent with given name in IDE registry.
     * It takes into account only showing ones.
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @return TopComponent instance or null if noone matching criteria was found
     */
    protected static JComponent findTopComponent(String name, int index) {
        Iterator it=TopComponent.getRegistry().getOpened().iterator();
        ComponentChooser chooser=new TopComponentChooser(name, Operator.getDefaultStringComparator());
        TopComponent c;
        while (it.hasNext()) {
            c=(TopComponent)it.next();
            if (c.isShowing() && chooser.checkComponent(c)) {
                index--;
                if (index<0)
                    return c;
            }
        }
        return null;
    }
    
    /** Waits for index-th TopComponent with given name in IDE registry.
     * It throws JemmyException when TopComponent is not find until timeout
     * expires.
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @return TopComponent instance or throws JemmyException if not found
     * @see #findTopComponent
     */
    protected static JComponent waitTopComponent(final String name, final int index) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                    return findTopComponent(name, index);
                }
                public String getDescription() {
                    return("Wait TopComponent with name="+name+
                           " index="+String.valueOf(index)+" loaded");
                }
            });
            Timeouts times = JemmyProperties.getCurrentTimeouts().cloneThis();
            times.setTimeout("Waiter.WaitingTime", times.getTimeout("ComponentOperator.WaitComponentTimeout"));
            waiter.setTimeouts(times);
            waiter.setOutput(JemmyProperties.getCurrentOutput());
            return((JComponent)waiter.waitAction(null));
        } catch(InterruptedException e) {
            return(null);
        }
    }
    
    /** Chooser to find TopComponent instance by its name.
     * Used in findTopComponent method.
     */
    private static final class TopComponentChooser implements ComponentChooser {
        
        private String _name;
        private StringComparator _comparator;
        
        public TopComponentChooser(String name, StringComparator comparator) {
            _name=name;
            _comparator = comparator;
        }
        
        public boolean checkComponent(Component comp) {
            return (comp instanceof TopComponent) && (_comparator.equals(comp.getName(), _name));
        }
        
        public String getDescription() {
            return "org.openide.windows.TopComponent";
        }
    }
}
