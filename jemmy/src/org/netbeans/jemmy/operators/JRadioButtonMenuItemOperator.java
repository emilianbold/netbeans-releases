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

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyProperties;

import java.awt.Component;

import javax.swing.JRadioButtonMenuItem;

/**
 *
 * <BR><BR>Timeouts used: <BR>
 * JMenuItemOperator.PushMenuTimeout - time between button pressing and releasing<BR>
 * ComponentOperator.WaitComponentTimeout - time to wait button displayed <BR>
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait button enabled <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JRadioButtonMenuItemOperator extends JMenuItemOperator {
    /**
     * Constructor.
     */
    public JRadioButtonMenuItemOperator(JRadioButtonMenuItem item) {
	super(item);
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
    }

    public JRadioButtonMenuItemOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JRadioButtonMenuItem)cont.
             waitSubComponent(new JRadioButtonMenuItemFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    public JRadioButtonMenuItemOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param text Button text. 
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JRadioButtonMenuItemOperator(ContainerOperator cont, String text, int index) {
	this((JRadioButtonMenuItem)waitComponent(cont, 
					      new JRadioButtonMenuItemByLabelFinder(text, 
										 cont.getComparator()),
					      index));
	setTimeouts(cont.getTimeouts());
	setOutput(cont.getOutput());
    }
    
    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param text Button text. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JRadioButtonMenuItemOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JRadioButtonMenuItemOperator(ContainerOperator cont, int index) {
	this((JRadioButtonMenuItem)
	     waitComponent(cont, 
			   new JRadioButtonMenuItemFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @throws TimeoutExpiredException
     */
    public JRadioButtonMenuItemOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //
    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    public static class JRadioButtonMenuItemByLabelFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
	public JRadioButtonMenuItemByLabelFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
	public JRadioButtonMenuItemByLabelFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JRadioButtonMenuItem) {
		if(((JRadioButtonMenuItem)comp).getText() != null) {
		    return(comparator.equals(((JRadioButtonMenuItem)comp).getText(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("JRadioButtonMenuItem with text \"" + label + "\"");
	}
    }

    public static class JRadioButtonMenuItemFinder extends Finder {
	public JRadioButtonMenuItemFinder(ComponentChooser sf) {
            super(JRadioButtonMenuItem.class, sf);
	}
	public JRadioButtonMenuItemFinder() {
            super(JRadioButtonMenuItem.class);
	}
    }
} 
