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
import java.awt.Container;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.jellytools.JellyVersion;
import org.netbeans.jellytools.actions.CloneViewAction;
import org.netbeans.jellytools.actions.DockingAction;
import org.netbeans.jellytools.actions.UndockAction;

import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.util.DefaultVisualizer;

import org.openide.windows.TopComponent;

/** Represents org.openide.windows.TopComponent. It is IDE wrapper for a lot of
 * panels in IDE which can be docked to a window. TopComponent is for example
 * Filesystems tab in the Explorer, every tab in the Source Editor, every tab
 * in the Output Window, Property sheet and many more.<br>
 * TopComponentOperator has slightly different behaviour of TopComponent Lookup.
 * TopComponent can be located by TopComponentOperator anywhere inside current
 * workspace or explicitly inside some Container.
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
 * @author Adam.Sotona@sun.com
 * @author Jiri.Skrivanek@sun.com
 */
public class TopComponentOperator extends JComponentOperator {
    
    static {
        // register NbInternalFrameDriver
        DriverManager.setWindowDriver(new NbInternalFrameDriver());
        DriverManager.setFrameDriver(new NbInternalFrameDriver());
        // Checks if you run on correct jemmy version. Writes message to jemmy log if not.
        JellyVersion.checkJemmyVersion();
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
        super(waitTopComponent(contOper, topComponentName, index, null));
        copyEnvironment(contOper);
        makeComponentVisible();
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
  
    /** Creates new instance from given TopComponent.
     * This constructor is used in properties.PropertySheetOperator.
     * @param jComponent instance of JComponent
     */
    public TopComponentOperator(JComponent jComponent) {
        super(jComponent);
        makeComponentVisible();
    }
    
    public ComponentVisualizer getVisualizer() {
        ComponentVisualizer v=super.getVisualizer();
        if (v instanceof DefaultVisualizer)
            ((DefaultVisualizer)v).switchTab(true);
        return v;
    }
    
    /** Docks this TopComponent into new host. TopComponent is focused before
     * action is performed.
     * @param newLocationPath path where to dock TopComponent (e.g. "Explorer|Center"
     * or "DockingAction.NEW_SINGLE_FRAME")
     */
    public void dockViewInto(String newLocationPath) {
        if (!(getSource() instanceof TopComponent)) 
            throw new JemmyException("Trying to call dockViewInto(...) method on non-TopComponent object");
        makeComponentVisible();
        ((TopComponent)getSource()).requestFocus();
        // need to wait a little
        new EventTool().waitNoEvent(500);
        new DockingAction(newLocationPath).perform();
    }
    
    /** Undocks this TopComponent. TopComponent is focused before
     * action is performed. */
    public void undockView() {
        if (!(getSource() instanceof TopComponent)) 
            throw new JemmyException("Trying to call undockView() method on non-TopComponent object");
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
        if (!(getSource() instanceof TopComponent)) 
            throw new JemmyException("Trying to call cloneView() method on non-TopComponent object");
        ((TopComponent)getSource()).requestFocus();
        // need to wait a little
        new EventTool().waitNoEvent(500);
        new CloneViewAction().perform();
    }
    
    /** Closes this TopComponent instance by IDE API call. */
    public void close() {
        // used direct call of IDE API method because CloseViewAction closes
        // active TopComponent and not neccesarily this one
        if (!(getSource() instanceof TopComponent)) 
            throw new JemmyException("Trying to call close() method on non-TopComponent object");
        ((TopComponent)getSource()).close();
    }
    
    /** Finds index-th TopComponent with given name in IDE registry.
     * It takes into account only showing ones.
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @return TopComponent instance or null if noone matching criteria was found
     */
    protected static JComponent findTopComponent(String name, int index) {
        return findTopComponent(null, name,  index, null);
    }
    
    /** Finds index-th TopComponent with given name in IDE registry.
     * It takes into account only showing ones.
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @param subchooser ComponentChooser to determine exact TopComponent
     * @return TopComponent instance or null if noone matching criteria was found
     */
    protected static JComponent findTopComponent(ContainerOperator cont, String name, int index, ComponentChooser subchooser) {
        Object tc[]=TopComponent.getRegistry().getOpened().toArray();
        StringComparator comparator=cont==null?Operator.getDefaultStringComparator():cont.getComparator();
        TopComponent c;
        for (int i=0; i<tc.length; i++) {
            c=(TopComponent)tc[i];
            if (c.isShowing() && comparator.equals(c.getName(), name) && isUnder(cont, c) && (subchooser==null || subchooser.checkComponent(c))) {
                index--;
                if (index<0)
                    return c;
            }
        }
        for (int i=0; i<tc.length; i++) {
            c=(TopComponent)tc[i];
            if ((!c.isShowing()) && isParentShowing(c) && comparator.equals(c.getName(), name) && isUnder(cont, c) && (subchooser==null || subchooser.checkComponent(c))) {
                index--;
                if (index<0)
                    return c;
            }
        }
        return null;
    }

    private static boolean isParentShowing(Component c) {
        while (c!=null) {
            if (c.isShowing()) return true;
            c=c.getParent();
        }
        return false;
    }
    
    private static boolean isUnder(ContainerOperator cont, Component c) {
        if (cont==null) return true;
        Component comp=cont.getSource();
        while (comp!=c && c!=null) c=c.getParent();
        return (comp==c);
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
        return waitTopComponent(null, name, index, null);
    }
    
    /** Waits for index-th TopComponent with given name in IDE registry.
     * It throws JemmyException when TopComponent is not find until timeout
     * expires.
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @param subchooser ComponentChooser to determine exact TopComponent
     * @return TopComponent instance or throws JemmyException if not found
     * @see #findTopComponent
     */
    protected static JComponent waitTopComponent(final ContainerOperator cont, final String name, final int index, final ComponentChooser subchooser) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                    return findTopComponent(cont, name, index, subchooser);
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
}
