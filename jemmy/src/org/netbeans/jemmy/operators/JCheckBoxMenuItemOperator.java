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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.operators;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyProperties;

import java.awt.Component;

import javax.swing.JCheckBoxMenuItem;

/**
 *
 * <BR><BR>Timeouts used: <BR>
 * JMenuItemOperator.PushMenuTimeout - time between button pressing and releasing<BR>
 * ComponentOperator.WaitComponentTimeout - time to wait button displayed <BR>
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait button enabled <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JCheckBoxMenuItemOperator extends JMenuItemOperator {
    /**
     * Constructor.
     * @param item a component.
     */
    public JCheckBoxMenuItemOperator(JCheckBoxMenuItem item) {
	super(item);
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
    }

    /**
     * Constructs a JCheckBoxMenuItemOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JCheckBoxMenuItemOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JCheckBoxMenuItem)cont.
             waitSubComponent(new JCheckBoxMenuItemFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JCheckBoxMenuItemOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JCheckBoxMenuItemOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param text Button text. 
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public JCheckBoxMenuItemOperator(ContainerOperator cont, String text, int index) {
	this((JCheckBoxMenuItem)waitComponent(cont, 
					      new JCheckBoxMenuItemByLabelFinder(text, 
										 cont.getComparator()),
					      index));
	setTimeouts(cont.getTimeouts());
	setOutput(cont.getOutput());
    }
    
    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param text Button text. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public JCheckBoxMenuItemOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param index Ordinal component index.
     */
    public JCheckBoxMenuItemOperator(ContainerOperator cont, int index) {
	this((JCheckBoxMenuItem)
	     waitComponent(cont, 
			   new JCheckBoxMenuItemFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     */
    public JCheckBoxMenuItemOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JCheckBoxMenuItem.getState()</code> through queue*/
    public boolean getState() {
	return(runMapping(new MapBooleanAction("getState") {
		public boolean map() {
		    return(((JCheckBoxMenuItem)getSource()).getState());
		}}));}

    /**Maps <code>JCheckBoxMenuItem.setState(boolean)</code> through queue*/
    public void setState(final boolean b) {
	runMapping(new MapVoidAction("setState") {
		public void map() {
		    ((JCheckBoxMenuItem)getSource()).setState(b);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Allows to find component by text.
     */
    public static class JCheckBoxMenuItemByLabelFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
        /**
         * Constructs JCheckBoxMenuItemByLabelFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public JCheckBoxMenuItemByLabelFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
        /**
         * Constructs JCheckBoxMenuItemByLabelFinder.
         * @param lb a text pattern
         */
	public JCheckBoxMenuItemByLabelFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JCheckBoxMenuItem) {
		if(((JCheckBoxMenuItem)comp).getText() != null) {
		    return(comparator.equals(((JCheckBoxMenuItem)comp).getText(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("JCheckBoxMenuItem with text \"" + label + "\"");
	}
    }

    /**
     * Checks component type.
     */
    public static class JCheckBoxMenuItemFinder extends Finder {
        /**
         * Constructs JCheckBoxMenuItemFinder.
         * @param sf other searching criteria.
         */
	public JCheckBoxMenuItemFinder(ComponentChooser sf) {
            super(JCheckBoxMenuItem.class, sf);
	}
        /**
         * Constructs JCheckBoxMenuItemFinder.
         */
	public JCheckBoxMenuItemFinder() {
            super(JCheckBoxMenuItem.class);
	}
    }
} 
