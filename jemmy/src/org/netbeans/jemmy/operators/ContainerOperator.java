/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.operators;

import java.awt.event.ContainerListener;

import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;

/**
 * <BR><BR>Timeouts used: <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait container displayed <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class ContainerOperator extends ComponentOperator
    implements Timeoutable, Outputable {

    private static int POINT_RECT_SIZE = 10;
    
    /**
     * Constructor.
     * @param b Container component.
     */
    public ContainerOperator(Container b) {
	super(b);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public ContainerOperator(ContainerOperator cont, int index) {
	this((Container)waitComponent(cont, 
				      new ContainerFinder(ComponentSearcher.getTrueChooser("Any container")), 
				      index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @throws TimeoutExpiredException
     */
    public ContainerOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches Container in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return Container instance or null if component was not found.
     */
    public static Container findContainer(Container cont, ComponentChooser chooser, int index) {
	return((Container)findComponent(cont, new ContainerFinder(chooser), index));
    }
    
    /**
     * Searches 0'th Container in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return Container instance or null if component was not found.
     */
    public static Container findContainer(Container cont, ComponentChooser chooser) {
	return(findContainer(cont, chooser, 0));
    }
    
    /**
     * Searches Container in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return Container instance or null if component was not found.
     */
    public static Container findContainer(Container cont, int index) {
	return(findContainer(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th Container instance"), index));
    }
    
    /**
     * Searches 0'th Container in container.
     * @param cont Container to search component in.
     * @return Container instance or null if component was not found.
     */
    public static Container findContainer(Container cont) {
	return(findContainer(cont, 0));
    }

    /**
     * Searches Container object which component lies on.
     * @param comp Component to find Container under.
     * @param chooser 
     * @return Container instance or null if component was not found.
     */
    public static Container findContainerUnder(Component comp, ComponentChooser chooser) {
	return((Container)new ComponentOperator(comp).
	       getContainer(new ContainerFinder(chooser)));
    }

    /**
     * Searches Container object which component lies on.
     * @param comp Component to find Container under.
     * @return Container instance or null if component was not found.
     */
    public static Container findContainerUnder(Component comp) {
	return(findContainerUnder(comp, ComponentSearcher.getTrueChooser("Container")));
    }
    
    /**
     * Waits Container in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return Container instance.
     * @throws TimeoutExpiredException
     */
    public static Container waitContainer(Container cont, ComponentChooser chooser, int index) {
	return((Container)waitComponent(cont, new ContainerFinder(chooser), index));
    }
    
    /**
     * Waits 0'th Container in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return Container instance.
     * @throws TimeoutExpiredException
     */
    public static Container waitContainer(Container cont, ComponentChooser chooser) {
	return(waitContainer(cont, chooser, 0));
    }
    
    /**
     * Waits Container in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return Container instance.
     * @throws TimeoutExpiredException
     */
    public static Container waitContainer(Container cont, int index) {
	return(waitContainer(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th Container instance"), index));
    }
    
    /**
     * Waits 0'th Container in container.
     * @param cont Container to search component in.
     * @return Container instance.
     * @throws TimeoutExpiredException
     */
    public static Container waitContainer(Container cont) {
	return(waitContainer(cont, 0));
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>Container.add(Component)</code> through queue*/
    public Component add(final Component component) {
	return((Component)runMapping(new MapAction("add") {
		public Object map() {
		    return(((Container)getSource()).add(component));
		}}));}

    /**Maps <code>Container.add(Component, int)</code> through queue*/
    public Component add(final Component component, final int i) {
	return((Component)runMapping(new MapAction("add") {
		public Object map() {
		    return(((Container)getSource()).add(component, i));
		}}));}

    /**Maps <code>Container.add(Component, Object)</code> through queue*/
    public void add(final Component component, final Object object) {
	runMapping(new MapVoidAction("add") {
		public void map() {
		    ((Container)getSource()).add(component, object);
		}});}

    /**Maps <code>Container.add(Component, Object, int)</code> through queue*/
    public void add(final Component component, final Object object, final int i) {
	runMapping(new MapVoidAction("add") {
		public void map() {
		    ((Container)getSource()).add(component, object, i);
		}});}

    /**Maps <code>Container.add(String, Component)</code> through queue*/
    public Component add(final String string, final Component component) {
	return((Component)runMapping(new MapAction("add") {
		public Object map() {
		    return(((Container)getSource()).add(string, component));
		}}));}

    /**Maps <code>Container.addContainerListener(ContainerListener)</code> through queue*/
    public void addContainerListener(final ContainerListener containerListener) {
	runMapping(new MapVoidAction("addContainerListener") {
		public void map() {
		    ((Container)getSource()).addContainerListener(containerListener);
		}});}

    /**Maps <code>Container.findComponentAt(int, int)</code> through queue*/
    public Component findComponentAt(final int i, final int i1) {
	return((Component)runMapping(new MapAction("findComponentAt") {
		public Object map() {
		    return(((Container)getSource()).findComponentAt(i, i1));
		}}));}

    /**Maps <code>Container.findComponentAt(Point)</code> through queue*/
    public Component findComponentAt(final Point point) {
	return((Component)runMapping(new MapAction("findComponentAt") {
		public Object map() {
		    return(((Container)getSource()).findComponentAt(point));
		}}));}

    /**Maps <code>Container.getComponent(int)</code> through queue*/
    public Component getComponent(final int i) {
	return((Component)runMapping(new MapAction("getComponent") {
		public Object map() {
		    return(((Container)getSource()).getComponent(i));
		}}));}

    /**Maps <code>Container.getComponentCount()</code> through queue*/
    public int getComponentCount() {
	return(runMapping(new MapIntegerAction("getComponentCount") {
		public int map() {
		    return(((Container)getSource()).getComponentCount());
		}}));}

    /**Maps <code>Container.getComponents()</code> through queue*/
    public Component[] getComponents() {
	return((Component[])runMapping(new MapAction("getComponents") {
		public Object map() {
		    return(((Container)getSource()).getComponents());
		}}));}

    /**Maps <code>Container.getInsets()</code> through queue*/
    public Insets getInsets() {
	return((Insets)runMapping(new MapAction("getInsets") {
		public Object map() {
		    return(((Container)getSource()).getInsets());
		}}));}

    /**Maps <code>Container.getLayout()</code> through queue*/
    public LayoutManager getLayout() {
	return((LayoutManager)runMapping(new MapAction("getLayout") {
		public Object map() {
		    return(((Container)getSource()).getLayout());
		}}));}

    /**Maps <code>Container.isAncestorOf(Component)</code> through queue*/
    public boolean isAncestorOf(final Component component) {
	return(runMapping(new MapBooleanAction("isAncestorOf") {
		public boolean map() {
		    return(((Container)getSource()).isAncestorOf(component));
		}}));}

    /**Maps <code>Container.paintComponents(Graphics)</code> through queue*/
    public void paintComponents(final Graphics graphics) {
	runMapping(new MapVoidAction("paintComponents") {
		public void map() {
		    ((Container)getSource()).paintComponents(graphics);
		}});}

    /**Maps <code>Container.printComponents(Graphics)</code> through queue*/
    public void printComponents(final Graphics graphics) {
	runMapping(new MapVoidAction("printComponents") {
		public void map() {
		    ((Container)getSource()).printComponents(graphics);
		}});}

    /**Maps <code>Container.remove(int)</code> through queue*/
    public void remove(final int i) {
	runMapping(new MapVoidAction("remove") {
		public void map() {
		    ((Container)getSource()).remove(i);
		}});}

    /**Maps <code>Container.remove(Component)</code> through queue*/
    public void remove(final Component component) {
	runMapping(new MapVoidAction("remove") {
		public void map() {
		    ((Container)getSource()).remove(component);
		}});}

    /**Maps <code>Container.removeAll()</code> through queue*/
    public void removeAll() {
	runMapping(new MapVoidAction("removeAll") {
		public void map() {
		    ((Container)getSource()).removeAll();
		}});}

    /**Maps <code>Container.removeContainerListener(ContainerListener)</code> through queue*/
    public void removeContainerListener(final ContainerListener containerListener) {
	runMapping(new MapVoidAction("removeContainerListener") {
		public void map() {
		    ((Container)getSource()).removeContainerListener(containerListener);
		}});}

    /**Maps <code>Container.setLayout(LayoutManager)</code> through queue*/
    public void setLayout(final LayoutManager layoutManager) {
	runMapping(new MapVoidAction("setLayout") {
		public void map() {
		    ((Container)getSource()).setLayout(layoutManager);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    private static class ContainerFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public ContainerFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof Container) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }
}
