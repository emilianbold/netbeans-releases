/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 *
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
