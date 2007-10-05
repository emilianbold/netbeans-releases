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
import org.netbeans.jemmy.TimeoutExpiredException;

import java.awt.Component;
import java.awt.Container;

import java.util.Hashtable;

import javax.swing.JButton;

/**
 *
 * <BR><BR>Timeouts used: <BR>
 * AbstractButtonOperator.PushButtonTimeout - time between button pressing and releasing<BR>
 * ComponentOperator.WaitComponentTimeout - time to wait button displayed <BR>
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait button enabled <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *
 */

public class JButtonOperator extends AbstractButtonOperator{

    /**
     * Identifier for a "default button" property.
     * @see #getDump
     */
    public static final String IS_DEFAULT_DPROP = "Default button";

    /**
     * Constructor.
     * @param b a component
     */
    public JButtonOperator(JButton b) {
	super(b);
    }

    /**
     * Constructs a JButtonOperator object.
     * @param cont container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JButtonOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JButton)cont.
             waitSubComponent(new JButtonFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JButtonOperator object.
     * @param cont container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JButtonOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont container
     * @param text Button text. 
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JButtonOperator(ContainerOperator cont, String text, int index) {
	this((JButton)
	     waitComponent(cont, 
			   new JButtonFinder(new AbstractButtonOperator.
					     AbstractButtonByLabelFinder(text, 
									 cont.getComparator())),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont container
     * @param text Button text. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JButtonOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont container
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JButtonOperator(ContainerOperator cont, int index) {
	this((JButton)
	     waitComponent(cont, 
			   new JButtonFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont container
     * @throws TimeoutExpiredException
     */
    public JButtonOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JButton instance or null if component was not found.
     */
    public static JButton findJButton(Container cont, ComponentChooser chooser, int index) {
	return((JButton)findAbstractButton(cont, new JButtonFinder(chooser), index));
    }

    /**
     * Searches 0'th JButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JButton instance or null if component was not found.
     */
    public static JButton findJButton(Container cont, ComponentChooser chooser) {
	return(findJButton(cont, chooser, 0));
    }

    /**
     * Searches JButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JButton instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JButton findJButton(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJButton(cont, 
			   new JButtonFinder(new AbstractButtonOperator.
					     AbstractButtonByLabelFinder(text, 
									 new DefaultStringComparator(ce, ccs))), 
			   index));
    }

    /**
     * Searches JButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JButton instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JButton findJButton(Container cont, String text, boolean ce, boolean ccs) {
	return(findJButton(cont, text, ce, ccs, 0));
    }

    /**
     * Waits JButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JButton instance.
     * @throws TimeoutExpiredException
     */
    public static JButton waitJButton(Container cont, ComponentChooser chooser, int index) {
	return((JButton)waitAbstractButton(cont, new JButtonFinder(chooser), index));
    }

    /**
     * Waits 0'th JButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JButton instance.
     * @throws TimeoutExpiredException
     */
    public static JButton waitJButton(Container cont, ComponentChooser chooser) {
	return(waitJButton(cont, chooser, 0));
    }

    /**
     * Waits JButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JButton instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JButton waitJButton(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJButton(cont,  
			   new JButtonFinder(new AbstractButtonOperator.
					     AbstractButtonByLabelFinder(text, 
									 new DefaultStringComparator(ce, ccs))), 
			   index));
    }

    /**
     * Waits JButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JButton instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JButton waitJButton(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJButton(cont, text, ce, ccs, 0));
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.remove(AbstractButtonOperator.IS_SELECTED_DPROP);
	result.put(IS_DEFAULT_DPROP, ((JButton)getSource()).isDefaultButton() ? "true" : "false");
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JButton.isDefaultButton()</code> through queue*/
    public boolean isDefaultButton() {
	return(runMapping(new MapBooleanAction("isDefaultButton") {
		public boolean map() {
		    return(((JButton)getSource()).isDefaultButton());
		}}));}

    /**Maps <code>JButton.isDefaultCapable()</code> through queue*/
    public boolean isDefaultCapable() {
	return(runMapping(new MapBooleanAction("isDefaultCapable") {
		public boolean map() {
		    return(((JButton)getSource()).isDefaultCapable());
		}}));}

    /**Maps <code>JButton.setDefaultCapable(boolean)</code> through queue*/
    public void setDefaultCapable(final boolean b) {
	runMapping(new MapVoidAction("setDefaultCapable") {
		public void map() {
		    ((JButton)getSource()).setDefaultCapable(b);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Prepares the button to click.
     */
    protected void prepareToClick() {
	makeComponentVisible();
    }

    /**
     * Checks component type.
     */
    public static class JButtonFinder extends Finder {
        /**
         * Constructs JButtonFinder.
         * @param sf other searching criteria.
         */
	public JButtonFinder(ComponentChooser sf) {
            super(JButton.class, sf);
	}
        /**
         * Constructs JButtonFinder.
         */
	public JButtonFinder() {
            super(JButton.class);
	}
    }
}
