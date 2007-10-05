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

import javax.swing.Icon;
import javax.swing.JLabel;

import javax.swing.plaf.LabelUI;

/**
 * <BR><BR>Timeouts used: <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 * ComponentOperator.WaitStateTimeout - time to wait for text <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JLabelOperator extends JComponentOperator {

    /**
     * Identifier for a "text" property.
     * @see #getDump
     */
    public static final String TEXT_DPROP = "Text";

    /**
     * Constructor.
     * @param b a component
     */
    public JLabelOperator(JLabel b) {
	super(b);
    }

    /**
     * Constructs a JLabelOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JLabelOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JLabel)cont.
             waitSubComponent(new JLabelFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JLabelOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JLabelOperator(ContainerOperator cont, ComponentChooser chooser) {
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
     * 
     */
    public JLabelOperator(ContainerOperator cont, String text, int index) {
	this((JLabel)waitComponent(cont, 
				   new JLabelByLabelFinder(text, 
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
     * 
     */
    public JLabelOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param index Ordinal component index.
     * 
     */
    public JLabelOperator(ContainerOperator cont, int index) {
	this((JLabel)
	     waitComponent(cont, 
			   new JLabelFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * 
     */
    public JLabelOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JLabel in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JLabel instance or null if component was not found.
     */
    public static JLabel findJLabel(Container cont, ComponentChooser chooser, int index) {
	return((JLabel)findComponent(cont, new JLabelFinder(chooser), index));
    }

    /**
     * Searches JLabel in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JLabel instance or null if component was not found.
     */
    public static JLabel findJLabel(Container cont, ComponentChooser chooser) {
	return(findJLabel(cont, chooser, 0));
    }

    /**
     * Searches JLabel by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JLabel instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JLabel findJLabel(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJLabel(cont, new JLabelByLabelFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Searches JLabel by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JLabel instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JLabel findJLabel(Container cont, String text, boolean ce, boolean ccs) {
	return(findJLabel(cont, text, ce, ccs, 0));
    }

    /**
     * Waits JLabel in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JLabel instance.
     * 
     */
    public static JLabel waitJLabel(final Container cont, final ComponentChooser chooser, final int index) {
	return((JLabel)waitComponent(cont, new JLabelFinder(chooser), index));
    }

    /**
     * Waits JLabel in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JLabel instance.
     * 
     */
    public static JLabel waitJLabel(Container cont, ComponentChooser chooser) {
	return(waitJLabel(cont, chooser, 0));
    }

    /**
     * Waits JLabel by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JLabel instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * 
     */
    public static JLabel waitJLabel(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJLabel(cont, new JLabelByLabelFinder(text, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Waits JLabel by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JLabel instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * 
     */
    public static JLabel waitJLabel(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJLabel(cont, text, ce, ccs, 0));
    }

    /**
     * Waits for text. Uses getComparator() comparator.
     * @param text Text to wait for.
     */
    public void waitText(String text) {
	getOutput().printLine("Wait \"" + text + "\" text in component \n    : "+
			      toStringSource());
	getOutput().printGolden("Wait \"" + text + "\" text");
	waitState(new JLabelByLabelFinder(text, getComparator()));
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	if(((JLabel)getSource()).getText() != null) {
	    result.put(TEXT_DPROP, ((JLabel)getSource()).getText());
	} else {
	    result.put(TEXT_DPROP, "null");
	}
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JLabel.getDisabledIcon()</code> through queue*/
    public Icon getDisabledIcon() {
	return((Icon)runMapping(new MapAction("getDisabledIcon") {
		public Object map() {
		    return(((JLabel)getSource()).getDisabledIcon());
		}}));}

    /**Maps <code>JLabel.getDisplayedMnemonic()</code> through queue*/
    public int getDisplayedMnemonic() {
	return(runMapping(new MapIntegerAction("getDisplayedMnemonic") {
		public int map() {
		    return(((JLabel)getSource()).getDisplayedMnemonic());
		}}));}

    /**Maps <code>JLabel.getHorizontalAlignment()</code> through queue*/
    public int getHorizontalAlignment() {
	return(runMapping(new MapIntegerAction("getHorizontalAlignment") {
		public int map() {
		    return(((JLabel)getSource()).getHorizontalAlignment());
		}}));}

    /**Maps <code>JLabel.getHorizontalTextPosition()</code> through queue*/
    public int getHorizontalTextPosition() {
	return(runMapping(new MapIntegerAction("getHorizontalTextPosition") {
		public int map() {
		    return(((JLabel)getSource()).getHorizontalTextPosition());
		}}));}

    /**Maps <code>JLabel.getIcon()</code> through queue*/
    public Icon getIcon() {
	return((Icon)runMapping(new MapAction("getIcon") {
		public Object map() {
		    return(((JLabel)getSource()).getIcon());
		}}));}

    /**Maps <code>JLabel.getIconTextGap()</code> through queue*/
    public int getIconTextGap() {
	return(runMapping(new MapIntegerAction("getIconTextGap") {
		public int map() {
		    return(((JLabel)getSource()).getIconTextGap());
		}}));}

    /**Maps <code>JLabel.getLabelFor()</code> through queue*/
    public Component getLabelFor() {
	return((Component)runMapping(new MapAction("getLabelFor") {
		public Object map() {
		    return(((JLabel)getSource()).getLabelFor());
		}}));}

    /**Maps <code>JLabel.getText()</code> through queue*/
    public String getText() {
	return((String)runMapping(new MapAction("getText") {
		public Object map() {
		    return(((JLabel)getSource()).getText());
		}}));}

    /**Maps <code>JLabel.getUI()</code> through queue*/
    public LabelUI getUI() {
	return((LabelUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JLabel)getSource()).getUI());
		}}));}

    /**Maps <code>JLabel.getVerticalAlignment()</code> through queue*/
    public int getVerticalAlignment() {
	return(runMapping(new MapIntegerAction("getVerticalAlignment") {
		public int map() {
		    return(((JLabel)getSource()).getVerticalAlignment());
		}}));}

    /**Maps <code>JLabel.getVerticalTextPosition()</code> through queue*/
    public int getVerticalTextPosition() {
	return(runMapping(new MapIntegerAction("getVerticalTextPosition") {
		public int map() {
		    return(((JLabel)getSource()).getVerticalTextPosition());
		}}));}

    /**Maps <code>JLabel.setDisabledIcon(Icon)</code> through queue*/
    public void setDisabledIcon(final Icon icon) {
	runMapping(new MapVoidAction("setDisabledIcon") {
		public void map() {
		    ((JLabel)getSource()).setDisabledIcon(icon);
		}});}

    /**Maps <code>JLabel.setDisplayedMnemonic(char)</code> through queue*/
    public void setDisplayedMnemonic(final char c) {
	runMapping(new MapVoidAction("setDisplayedMnemonic") {
		public void map() {
		    ((JLabel)getSource()).setDisplayedMnemonic(c);
		}});}

    /**Maps <code>JLabel.setDisplayedMnemonic(int)</code> through queue*/
    public void setDisplayedMnemonic(final int i) {
	runMapping(new MapVoidAction("setDisplayedMnemonic") {
		public void map() {
		    ((JLabel)getSource()).setDisplayedMnemonic(i);
		}});}

    /**Maps <code>JLabel.setHorizontalAlignment(int)</code> through queue*/
    public void setHorizontalAlignment(final int i) {
	runMapping(new MapVoidAction("setHorizontalAlignment") {
		public void map() {
		    ((JLabel)getSource()).setHorizontalAlignment(i);
		}});}

    /**Maps <code>JLabel.setHorizontalTextPosition(int)</code> through queue*/
    public void setHorizontalTextPosition(final int i) {
	runMapping(new MapVoidAction("setHorizontalTextPosition") {
		public void map() {
		    ((JLabel)getSource()).setHorizontalTextPosition(i);
		}});}

    /**Maps <code>JLabel.setIcon(Icon)</code> through queue*/
    public void setIcon(final Icon icon) {
	runMapping(new MapVoidAction("setIcon") {
		public void map() {
		    ((JLabel)getSource()).setIcon(icon);
		}});}

    /**Maps <code>JLabel.setIconTextGap(int)</code> through queue*/
    public void setIconTextGap(final int i) {
	runMapping(new MapVoidAction("setIconTextGap") {
		public void map() {
		    ((JLabel)getSource()).setIconTextGap(i);
		}});}

    /**Maps <code>JLabel.setLabelFor(Component)</code> through queue*/
    public void setLabelFor(final Component component) {
	runMapping(new MapVoidAction("setLabelFor") {
		public void map() {
		    ((JLabel)getSource()).setLabelFor(component);
		}});}

    /**Maps <code>JLabel.setText(String)</code> through queue*/
    public void setText(final String string) {
	runMapping(new MapVoidAction("setText") {
		public void map() {
		    ((JLabel)getSource()).setText(string);
		}});}

    /**Maps <code>JLabel.setUI(LabelUI)</code> through queue*/
    public void setUI(final LabelUI labelUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JLabel)getSource()).setUI(labelUI);
		}});}

    /**Maps <code>JLabel.setVerticalAlignment(int)</code> through queue*/
    public void setVerticalAlignment(final int i) {
	runMapping(new MapVoidAction("setVerticalAlignment") {
		public void map() {
		    ((JLabel)getSource()).setVerticalAlignment(i);
		}});}

    /**Maps <code>JLabel.setVerticalTextPosition(int)</code> through queue*/
    public void setVerticalTextPosition(final int i) {
	runMapping(new MapVoidAction("setVerticalTextPosition") {
		public void map() {
		    ((JLabel)getSource()).setVerticalTextPosition(i);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Allows to find component by text.
     */
    public static class JLabelByLabelFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
        /**
         * Constructs JLabelByLabelFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public JLabelByLabelFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
        /**
         * Constructs JLabelByLabelFinder.
         * @param lb a text pattern
         */
	public JLabelByLabelFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JLabel) {
		if(((JLabel)comp).getText() != null) {
		    return(comparator.equals(((JLabel)comp).getText(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("JLabel with text \"" + label + "\"");
	}
    }

    /**
     * Checks component type.
     */
    public static class JLabelFinder extends Finder {
        /**
         * Constructs JLabelFinder.
         * @param sf other searching criteria.
         */
	public JLabelFinder(ComponentChooser sf) {
            super(JLabel.class, sf);
	}
        /**
         * Constructs JLabelFinder.
         */
	public JLabelFinder() {
            super(JLabel.class);
	}
    }
}
