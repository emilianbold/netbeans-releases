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

import javax.swing.JRadioButton;

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

public class JRadioButtonOperator extends JToggleButtonOperator{

    /**
     * Constructor.
     * @param b a component
     */
    public JRadioButtonOperator(JRadioButton b) {
	super(b);
    }

    /**
     * Constructs a JRadioButtonOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JRadioButtonOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JRadioButton)cont.
             waitSubComponent(new JRadioButtonFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JRadioButtonOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JRadioButtonOperator(ContainerOperator cont, ComponentChooser chooser) {
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
     * @throws TimeoutExpiredException
     */
    public JRadioButtonOperator(ContainerOperator cont, String text, int index) {
	this((JRadioButton)
	     waitComponent(cont, 
			   new JRadioButtonFinder(new AbstractButtonOperator.
						  AbstractButtonByLabelFinder(text, 
									      cont.getComparator())),
			   index));
	copyEnvironment(cont);
    }
    
    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param text Button text. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JRadioButtonOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JRadioButtonOperator(ContainerOperator cont, int index) {
	this((JRadioButton)
	     waitComponent(cont, 
			   new JRadioButtonFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @throws TimeoutExpiredException
     */
    public JRadioButtonOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JRadioButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JRadioButton instance or null if component was not found.
     */
    public static JRadioButton findJRadioButton(Container cont, ComponentChooser chooser, int index) {
	return((JRadioButton)findJToggleButton(cont, new JRadioButtonFinder(chooser), index));
    }

    /**
     * Searches 0'th JRadioButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JRadioButton instance or null if component was not found.
     */
    public static JRadioButton findJRadioButton(Container cont, ComponentChooser chooser) {
	return(findJRadioButton(cont, chooser, 0));
    }

    /**
     * Searches JRadioButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JRadioButton instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JRadioButton findJRadioButton(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJRadioButton(cont, 
				new JRadioButtonFinder(new AbstractButtonOperator.
						       AbstractButtonByLabelFinder(text, 
										   new DefaultStringComparator(ce, ccs))), 
				index));
    }

    /**
     * Searches JRadioButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JRadioButton instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JRadioButton findJRadioButton(Container cont, String text, boolean ce, boolean ccs) {
	return(findJRadioButton(cont, text, ce, ccs, 0));
    }

    /**
     * Waits JRadioButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JRadioButton instance.
     * @throws TimeoutExpiredException
     */
    public static JRadioButton waitJRadioButton(Container cont, ComponentChooser chooser, int index) {
	return((JRadioButton)waitJToggleButton(cont, new JRadioButtonFinder(chooser), index));
    }

    /**
     * Waits 0'th JRadioButton in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JRadioButton instance.
     * @throws TimeoutExpiredException
     */
    public static JRadioButton waitJRadioButton(Container cont, ComponentChooser chooser) {
	return(waitJRadioButton(cont, chooser, 0));
    }

    /**
     * Waits JRadioButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JRadioButton instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JRadioButton waitJRadioButton(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJRadioButton(cont, 
				new JRadioButtonFinder(new AbstractButtonOperator.
						       AbstractButtonByLabelFinder(text, 
										   new DefaultStringComparator(ce, ccs))), 
				index));
    }

    /**
     * Waits JRadioButton by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JRadioButton instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JRadioButton waitJRadioButton(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJRadioButton(cont, text, ce, ccs, 0));
    }

    /**
     * Checks component type.
     */
    public static class JRadioButtonFinder extends Finder {
        /**
         * Constructs JRadioButtonFinder.
         * @param sf other searching criteria.
         */
	public JRadioButtonFinder(ComponentChooser sf) {
            super(JRadioButton.class, sf);
	}
        /**
         * Constructs JRadioButtonFinder.
         */
	public JRadioButtonFinder() {
            super(JRadioButton.class);
	}
    }
}
