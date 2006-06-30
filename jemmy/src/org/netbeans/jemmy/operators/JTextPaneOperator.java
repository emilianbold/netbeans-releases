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

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JTextPane;

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;

/**
 * <BR><BR>Timeouts used: <BR>
 * JTextComponentOperator.PushKeyTimeout - time between key pressing and releasing during text typing <BR>
 * JTextComponentOperator.BetweenKeysTimeout - time to sleep between two chars typing <BR>
 * JTextComponentOperator.ChangeCaretPositionTimeout - maximum time to chenge caret position <BR>
 * JTextComponentOperator.TypeTextTimeout - maximum time to type text <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 * ComponentOperator.WaitFocusTimeout - time to wait component focus <BR>
 * JScrollBarOperator.OneScrollClickTimeout - time for one scroll click <BR>
 * JScrollBarOperator.WholeScrollTimeout - time for the whole scrolling <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */
public class JTextPaneOperator extends JEditorPaneOperator {

    /**
     * Constructor.
     * @param b a component
     */
    public JTextPaneOperator(JTextPane b) {
	super(b);
    }

    /**
     * Constructs a JTextPaneOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JTextPaneOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JTextPane)cont.
             waitSubComponent(new JTextPaneFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JTextPaneOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JTextPaneOperator(ContainerOperator cont, ComponentChooser chooser) {
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
    public JTextPaneOperator(ContainerOperator cont, String text, int index) {
	this((JTextPane)
	     waitComponent(cont, 
			   new JTextPaneFinder(new JTextComponentOperator.
						 JTextComponentByTextFinder(text, 
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
     */
    public JTextPaneOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param index Ordinal component index.
     */
    public JTextPaneOperator(ContainerOperator cont, int index) {
	this((JTextPane)
	     waitComponent(cont, 
			   new JTextPaneFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     */
    public JTextPaneOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JTextPane in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JTextPane instance or null if component was not found.
     */
    public static JTextPane findJTextPane(Container cont, ComponentChooser chooser, int index) {
	return((JTextPane)findJTextComponent(cont, new JTextPaneFinder(chooser), index));
    }

    /**
     * Searches JTextPane in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JTextPane instance or null if component was not found.
     */
    public static JTextPane findJTextPane(Container cont, ComponentChooser chooser) {
	return(findJTextPane(cont, chooser, 0));
    }

    /**
     * Searches JTextPane by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JTextPane instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JTextPane findJTextPane(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJTextPane(cont, 
			       new JTextPaneFinder(new JTextComponentOperator.
						     JTextComponentByTextFinder(text, 
										new DefaultStringComparator(ce, ccs))), 
			       index));
    }

    /**
     * Searches JTextPane by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JTextPane instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JTextPane findJTextPane(Container cont, String text, boolean ce, boolean ccs) {
	return(findJTextPane(cont, text, ce, ccs, 0));
    }

    /**
     * Waits JTextPane in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JTextPane instance.
     */
    public static JTextPane waitJTextPane(Container cont, ComponentChooser chooser, int index) {
	return((JTextPane)waitJTextComponent(cont, new JTextPaneFinder(chooser), index));
    }

    /**
     * Waits JTextPane in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JTextPane instance.
     */
    public static JTextPane waitJTextPane(Container cont, ComponentChooser chooser) {
	return(waitJTextPane(cont, chooser, 0));
    }

    /**
     * Waits JTextPane by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JTextPane instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JTextPane waitJTextPane(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJTextPane(cont,  
			       new JTextPaneFinder(new JTextComponentOperator.
						     JTextComponentByTextFinder(text, 
										new DefaultStringComparator(ce, ccs))), 
			       index));
    }

    /**
     * Waits JTextPane by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JTextPane instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JTextPane waitJTextPane(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJTextPane(cont, text, ce, ccs, 0));
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JTextPane.addStyle(String, Style)</code> through queue*/
    public Style addStyle(final String string, final Style style) {
	return((Style)runMapping(new MapAction("addStyle") {
		public Object map() {
		    return(((JTextPane)getSource()).addStyle(string, style));
		}}));}

    /**Maps <code>JTextPane.getCharacterAttributes()</code> through queue*/
    public AttributeSet getCharacterAttributes() {
	return((AttributeSet)runMapping(new MapAction("getCharacterAttributes") {
		public Object map() {
		    return(((JTextPane)getSource()).getCharacterAttributes());
		}}));}

    /**Maps <code>JTextPane.getInputAttributes()</code> through queue*/
    public MutableAttributeSet getInputAttributes() {
	return((MutableAttributeSet)runMapping(new MapAction("getInputAttributes") {
		public Object map() {
		    return(((JTextPane)getSource()).getInputAttributes());
		}}));}

    /**Maps <code>JTextPane.getLogicalStyle()</code> through queue*/
    public Style getLogicalStyle() {
	return((Style)runMapping(new MapAction("getLogicalStyle") {
		public Object map() {
		    return(((JTextPane)getSource()).getLogicalStyle());
		}}));}

    /**Maps <code>JTextPane.getParagraphAttributes()</code> through queue*/
    public AttributeSet getParagraphAttributes() {
	return((AttributeSet)runMapping(new MapAction("getParagraphAttributes") {
		public Object map() {
		    return(((JTextPane)getSource()).getParagraphAttributes());
		}}));}

    /**Maps <code>JTextPane.getStyle(String)</code> through queue*/
    public Style getStyle(final String string) {
	return((Style)runMapping(new MapAction("getStyle") {
		public Object map() {
		    return(((JTextPane)getSource()).getStyle(string));
		}}));}

    /**Maps <code>JTextPane.getStyledDocument()</code> through queue*/
    public StyledDocument getStyledDocument() {
	return((StyledDocument)runMapping(new MapAction("getStyledDocument") {
		public Object map() {
		    return(((JTextPane)getSource()).getStyledDocument());
		}}));}

    /**Maps <code>JTextPane.insertComponent(Component)</code> through queue*/
    public void insertComponent(final Component component) {
	runMapping(new MapVoidAction("insertComponent") {
		public void map() {
		    ((JTextPane)getSource()).insertComponent(component);
		}});}

    /**Maps <code>JTextPane.insertIcon(Icon)</code> through queue*/
    public void insertIcon(final Icon icon) {
	runMapping(new MapVoidAction("insertIcon") {
		public void map() {
		    ((JTextPane)getSource()).insertIcon(icon);
		}});}

    /**Maps <code>JTextPane.removeStyle(String)</code> through queue*/
    public void removeStyle(final String string) {
	runMapping(new MapVoidAction("removeStyle") {
		public void map() {
		    ((JTextPane)getSource()).removeStyle(string);
		}});}

    /**Maps <code>JTextPane.setCharacterAttributes(AttributeSet, boolean)</code> through queue*/
    public void setCharacterAttributes(final AttributeSet attributeSet, final boolean b) {
	runMapping(new MapVoidAction("setCharacterAttributes") {
		public void map() {
		    ((JTextPane)getSource()).setCharacterAttributes(attributeSet, b);
		}});}

    /**Maps <code>JTextPane.setLogicalStyle(Style)</code> through queue*/
    public void setLogicalStyle(final Style style) {
	runMapping(new MapVoidAction("setLogicalStyle") {
		public void map() {
		    ((JTextPane)getSource()).setLogicalStyle(style);
		}});}

    /**Maps <code>JTextPane.setParagraphAttributes(AttributeSet, boolean)</code> through queue*/
    public void setParagraphAttributes(final AttributeSet attributeSet, final boolean b) {
	runMapping(new MapVoidAction("setParagraphAttributes") {
		public void map() {
		    ((JTextPane)getSource()).setParagraphAttributes(attributeSet, b);
		}});}

    /**Maps <code>JTextPane.setStyledDocument(StyledDocument)</code> through queue*/
    public void setStyledDocument(final StyledDocument styledDocument) {
	runMapping(new MapVoidAction("setStyledDocument") {
		public void map() {
		    ((JTextPane)getSource()).setStyledDocument(styledDocument);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Checks component type.
     */
    public static class JTextPaneFinder extends Finder {
        /**
         * Constructs JTextPaneFinder.
         * @param sf other searching criteria.
         */
	public JTextPaneFinder(ComponentChooser sf) {
            super(JTextPane.class, sf);
	}
        /**
         * Constructs JTextPaneFinder.
         */
	public JTextPaneFinder() {
            super(JTextPane.class);
	}
    }
}
