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
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;

import java.awt.Component;
import java.awt.Container;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import java.io.InputStream;
import java.io.IOException;

import java.net.URL;

import java.util.Hashtable;

import javax.swing.JEditorPane;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import javax.swing.text.EditorKit;

/**
 * Class provides basic functions to operate with JEditorPane
 * (selection, typing, deleting)
 *
 * <BR><BR>Timeouts used: <BR>
 * JTextComponentOperator.PushKeyTimeout - time between key pressing and releasing during text typing <BR>
 * JTextComponentOperator.BetweenKeysTimeout - time to sleep between two chars typing <BR>
 * JTextComponentOperator.ChangeCaretPositionTimeout - maximum time to change caret position <BR>
 * JTextComponentOperator.TypeTextTimeout - maximum time to type text <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 * ComponentOperator.WaitFocusTimeout - time to wait component focus <BR>
 * JScrollBarOperator.OneScrollClickTimeout - time for one scroll click <BR>
 * JScrollBarOperator.WholeScrollTimeout - time for the whole scrolling <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JEditorPaneOperator extends JTextComponentOperator {

    /**
     * Identifier for a "content type" property.
     * @see #getDump
     */
    public static final String CONTENT_TYPE_DPROP = "Content type";

    private boolean pageNavigation = false;

    /**
     * Constructor.
     * @param b a component
     */
    public JEditorPaneOperator(JEditorPane b) {
	super(b);
    }

    /**
     * Constructs a JEditorPaneOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JEditorPaneOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JEditorPane)cont.
             waitSubComponent(new JEditorPaneFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JEditorPaneOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JEditorPaneOperator(ContainerOperator cont, ComponentChooser chooser) {
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
    public JEditorPaneOperator(ContainerOperator cont, String text, int index) {
	this((JEditorPane)
	     waitComponent(cont, 
			   new JEditorPaneFinder(new JTextComponentOperator.
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
     * @throws TimeoutExpiredException
     */
    public JEditorPaneOperator(ContainerOperator cont, String text) {
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
    public JEditorPaneOperator(ContainerOperator cont, int index) {
	this((JEditorPane)
	     waitComponent(cont, 
			   new JEditorPaneFinder(),
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
    public JEditorPaneOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JEditorPane in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JEditorPane instance or null if component was not found.
     */
    public static JEditorPane findJEditorPane(Container cont, ComponentChooser chooser, int index) {
	return((JEditorPane)findJTextComponent(cont, new JEditorPaneFinder(chooser), index));
    }

    /**
     * Searches JEditorPane in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JEditorPane instance or null if component was not found.
     */
    public static JEditorPane findJEditorPane(Container cont, ComponentChooser chooser) {
	return(findJEditorPane(cont, chooser, 0));
    }

    /**
     * Searches JEditorPane by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JEditorPane instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JEditorPane findJEditorPane(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJEditorPane(cont, 
			       new JEditorPaneFinder(new JTextComponentOperator.
						     JTextComponentByTextFinder(text, 
										new DefaultStringComparator(ce, ccs))), 
			       index));
    }

    /**
     * Searches JEditorPane by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JEditorPane instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JEditorPane findJEditorPane(Container cont, String text, boolean ce, boolean ccs) {
	return(findJEditorPane(cont, text, ce, ccs, 0));
    }

    /**
     * Waits JEditorPane in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JEditorPane instance.
     * @throws TimeoutExpiredException
     */
    public static JEditorPane waitJEditorPane(Container cont, ComponentChooser chooser, int index) {
	return((JEditorPane)waitJTextComponent(cont, new JEditorPaneFinder(chooser), index));
    }

    /**
     * Waits JEditorPane in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JEditorPane instance.
     * @throws TimeoutExpiredException
     */
    public static JEditorPane waitJEditorPane(Container cont, ComponentChooser chooser) {
	return(waitJEditorPane(cont, chooser, 0));
    }

    /**
     * Waits JEditorPane by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JEditorPane instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JEditorPane waitJEditorPane(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJEditorPane(cont,  
			       new JEditorPaneFinder(new JTextComponentOperator.
						     JTextComponentByTextFinder(text, 
										new DefaultStringComparator(ce, ccs))), 
			       index));
    }

    /**
     * Waits JEditorPane by text.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JEditorPane instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JEditorPane waitJEditorPane(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJEditorPane(cont, text, ce, ccs, 0));
    }

    /**
     * Notifies whether "PageUp" and "PageDown" should be used
     * to change caret position. If can be useful if text takes 
     * some pages.
     * @param yesOrNo whether to use "PageUp" and "PageDown"
     * @deprecated vlue set by this method is not used anymore:
     * all navigating is performed by TextDriver.
     */
    public void usePageNavigationKeys(boolean yesOrNo) {
	pageNavigation = yesOrNo;
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put(CONTENT_TYPE_DPROP, ((JEditorPane)getSource()).getContentType());
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JEditorPane.addHyperlinkListener(HyperlinkListener)</code> through queue*/
    public void addHyperlinkListener(final HyperlinkListener hyperlinkListener) {
	runMapping(new MapVoidAction("addHyperlinkListener") {
		public void map() {
		    ((JEditorPane)getSource()).addHyperlinkListener(hyperlinkListener);
		}});}

    /**Maps <code>JEditorPane.fireHyperlinkUpdate(HyperlinkEvent)</code> through queue*/
    public void fireHyperlinkUpdate(final HyperlinkEvent hyperlinkEvent) {
	runMapping(new MapVoidAction("fireHyperlinkUpdate") {
		public void map() {
		    ((JEditorPane)getSource()).fireHyperlinkUpdate(hyperlinkEvent);
		}});}

    /**Maps <code>JEditorPane.getContentType()</code> through queue*/
    public String getContentType() {
	return((String)runMapping(new MapAction("getContentType") {
		public Object map() {
		    return(((JEditorPane)getSource()).getContentType());
		}}));}

    /**Maps <code>JEditorPane.getEditorKit()</code> through queue*/
    public EditorKit getEditorKit() {
	return((EditorKit)runMapping(new MapAction("getEditorKit") {
		public Object map() {
		    return(((JEditorPane)getSource()).getEditorKit());
		}}));}

    /**Maps <code>JEditorPane.getEditorKitForContentType(String)</code> through queue*/
    public EditorKit getEditorKitForContentType(final String string) {
	return((EditorKit)runMapping(new MapAction("getEditorKitForContentType") {
		public Object map() {
		    return(((JEditorPane)getSource()).getEditorKitForContentType(string));
		}}));}

    /**Maps <code>JEditorPane.getPage()</code> through queue*/
    public URL getPage() {
	return((URL)runMapping(new MapAction("getPage") {
		public Object map() {
		    return(((JEditorPane)getSource()).getPage());
		}}));}

    /**Maps <code>JEditorPane.read(InputStream, Object)</code> through queue*/
    public void read(final InputStream inputStream, final Object object) {
	runMapping(new MapVoidAction("read") {
		public void map() throws IOException {
		    ((JEditorPane)getSource()).read(inputStream, object);
		}});}

    /**Maps <code>JEditorPane.removeHyperlinkListener(HyperlinkListener)</code> through queue*/
    public void removeHyperlinkListener(final HyperlinkListener hyperlinkListener) {
	runMapping(new MapVoidAction("removeHyperlinkListener") {
		public void map() {
		    ((JEditorPane)getSource()).removeHyperlinkListener(hyperlinkListener);
		}});}

    /**Maps <code>JEditorPane.setContentType(String)</code> through queue*/
    public void setContentType(final String string) {
	runMapping(new MapVoidAction("setContentType") {
		public void map() {
		    ((JEditorPane)getSource()).setContentType(string);
		}});}

    /**Maps <code>JEditorPane.setEditorKit(EditorKit)</code> through queue*/
    public void setEditorKit(final EditorKit editorKit) {
	runMapping(new MapVoidAction("setEditorKit") {
		public void map() {
		    ((JEditorPane)getSource()).setEditorKit(editorKit);
		}});}

    /**Maps <code>JEditorPane.setEditorKitForContentType(String, EditorKit)</code> through queue*/
    public void setEditorKitForContentType(final String string, final EditorKit editorKit) {
	runMapping(new MapVoidAction("setEditorKitForContentType") {
		public void map() {
		    ((JEditorPane)getSource()).setEditorKitForContentType(string, editorKit);
		}});}

    /**Maps <code>JEditorPane.setPage(String)</code> through queue*/
    public void setPage(final String string) {
	runMapping(new MapVoidAction("setPage") {
		public void map() throws IOException {
		    ((JEditorPane)getSource()).setPage(string);
		}});}

    /**Maps <code>JEditorPane.setPage(URL)</code> through queue*/
    public void setPage(final URL uRL) {
	runMapping(new MapVoidAction("setPage") {
		public void map() throws IOException {
		    ((JEditorPane)getSource()).setPage(uRL);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Checks component type.
     */
    public static class JEditorPaneFinder extends Finder {
        /**
         * Constructs JEditorPaneFinder.
         * @param sf other searching criteria.
         */
	public JEditorPaneFinder(ComponentChooser sf) {
            super(JEditorPane.class, sf);
	}
        /**
         * Constructs JEditorPaneFinder.
         */
	public JEditorPaneFinder() {
            super(JEditorPane.class);
	}
    }
}
