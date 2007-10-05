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
import java.awt.Label;

import java.util.Hashtable;

/**
 * <BR><BR>Timeouts used: <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class LabelOperator extends ComponentOperator {

    /**
     * Identifier for a "text" property.
     * @see #getDump
     */
    public static final String TEXT_DPROP = "Text";

    /**
     * Constructor.
     * @param b a component
     */
    public LabelOperator(Label b) {
	super(b);
    }

    /**
     * Constructs a LabelOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public LabelOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((Label)cont.
             waitSubComponent(new LabelFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a LabelOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public LabelOperator(ContainerOperator cont, ComponentChooser chooser) {
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
    public LabelOperator(ContainerOperator cont, String text, int index) {
	this((Label)waitComponent(cont, 
                                  new LabelByLabelFinder(text, 
                                                         cont.getComparator()),
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
    public LabelOperator(ContainerOperator cont, String text) {
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
    public LabelOperator(ContainerOperator cont, int index) {
	this((Label)
	     waitComponent(cont, 
			   new LabelFinder(),
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
    public LabelOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches Label in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return Label instance or null if component was not found.
     */
    public static Label findLabel(Container cont, ComponentChooser chooser, int index) {
	return((Label)findComponent(cont, new LabelFinder(chooser), index));
    }

    /**
     * Searches Label in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return Label instance or null if component was not found.
     */
    public static Label findLabel(Container cont, ComponentChooser chooser) {
	return(findLabel(cont, chooser, 0));
    }

    /**
     * Searches Label by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return Label instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static Label findLabel(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findLabel(cont, new LabelByLabelFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Searches Label by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return Label instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static Label findLabel(Container cont, String text, boolean ce, boolean ccs) {
	return(findLabel(cont, text, ce, ccs, 0));
    }

    /**
     * Waits Label in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return Label instance.
     * @throws TimeoutExpiredException
     */
    public static Label waitLabel(final Container cont, final ComponentChooser chooser, final int index) {
	return((Label)waitComponent(cont, new LabelFinder(chooser), index));
    }

    /**
     * Waits Label in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return Label instance.
     * @throws TimeoutExpiredException
     */
    public static Label waitLabel(Container cont, ComponentChooser chooser) {
	return(waitLabel(cont, chooser, 0));
    }

    /**
     * Waits Label by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return Label instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static Label waitLabel(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitLabel(cont, new LabelByLabelFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Waits Label by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return Label instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static Label waitLabel(Container cont, String text, boolean ce, boolean ccs) {
	return(waitLabel(cont, text, ce, ccs, 0));
    }

    public Hashtable getDump() {
	Hashtable result = super.getDump();
	if(((Label)getSource()).getText() != null) {
	    result.put(TEXT_DPROP, ((Label)getSource()).getText());
	} else {
	    result.put(TEXT_DPROP, "null");
	}
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>Label.getAlignment()</code> through queue*/
    public int getAlignment() {
	return(runMapping(new MapIntegerAction("getAlignment") {
		public int map() {
		    return(((Label)getSource()).getAlignment());
		}}));}

    /**Maps <code>Label.getText()</code> through queue*/
    public String getText() {
	return((String)runMapping(new MapAction("getText") {
		public Object map() {
		    return(((Label)getSource()).getText());
		}}));}

    /**Maps <code>Label.setAlignment(int)</code> through queue*/
    public void setAlignment(final int i) {
	runMapping(new MapVoidAction("setAlignment") {
		public void map() {
		    ((Label)getSource()).setAlignment(i);
		}});}

    /**Maps <code>Label.setText(String)</code> through queue*/
    public void setText(final String string) {
	runMapping(new MapVoidAction("setText") {
		public void map() {
		    ((Label)getSource()).setText(string);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Allows to find component by LabelByLabelFinder.
     */
    public static class LabelByLabelFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
        /**
         * Constructs LabelByLabelFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public LabelByLabelFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
        /**
         * Constructs LabelByLabelFinder.
         * @param lb a text pattern
         */
	public LabelByLabelFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof Label) {
		if(((Label)comp).getText() != null) {
		    return(comparator.equals(((Label)comp).getText(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("Label with text \"" + label + "\"");
	}
    }

    /**
     * Checks component type.
     */
    public static class LabelFinder extends Finder {
        /**
         * Constructs LabelFinder.
         * @param sf other searching criteria.
         */
	public LabelFinder(ComponentChooser sf) {
            super(Label.class, sf);
	}
        /**
         * Constructs LabelFinder.
         */
	public LabelFinder() {
            super(Label.class);
	}
    }
}
