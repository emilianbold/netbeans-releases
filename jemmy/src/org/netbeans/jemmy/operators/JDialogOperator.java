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
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.DialogWaiter;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Window;

import javax.accessibility.AccessibleContext;

import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;

/**
 * <BR><BR>Timeouts used: <BR>
 * DialogWaiter.WaitDialogTimeout - time to wait dialog displayed <BR>
 * DialogWaiter.AfterDialogTimeout - time to sleep after dialog has been dispayed <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JDialogOperator extends DialogOperator {

    /**
     * Constructor.
     * @param w a component
     */
    public JDialogOperator(JDialog w) {
	super(w);
    }

    /**
     * Constructs a JDialogOperator object.
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     * @param env an operator to copy environment from.
     */
    public JDialogOperator(ComponentChooser chooser, int index, Operator env) {
	this(waitJDialog(new JDialogFinder(chooser),
                         index, 
                         env.getTimeouts(),
                         env.getOutput()));
	copyEnvironment(env);
    }
    
    /**
     * Constructs a JDialogOperator object.
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JDialogOperator(ComponentChooser chooser, int index) {
	this(chooser, index, Operator.getEnvironmentOperator());
    }
    
    /**
     * Constructs a JDialogOperator object.
     * @param chooser a component chooser specifying searching criteria.
     */
    public JDialogOperator(ComponentChooser chooser) {
	this(chooser, 0);
    }
    
    /**
     * Constructs a JDialogOperator object.
     * @param owner window - owner
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JDialogOperator(WindowOperator owner, ComponentChooser chooser, int index) {
	this((JDialog)owner.
             waitSubWindow(new JDialogFinder(chooser),
                           index));
	copyEnvironment(owner);
    }
    
    /**
     * Constructs a JDialogOperator object.
     * @param owner window - owner
     * @param chooser a component chooser specifying searching criteria.
     */
    public JDialogOperator(WindowOperator owner, ComponentChooser chooser) {
	this(owner, chooser, 0);
    }

    /**
     * Constructor.
     * Waits for the dialog with "title" subtitle.
     * Uses owner's timeout and output for waiting and to init operator.
     * @param owner Operator pointing to a window owner.
     * @param title The desired title.
     * @param index Ordinal index.  The first dialog has <code>index</code> 0.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * 
     */
    public JDialogOperator(WindowOperator owner, String title, int index) {
	this(waitJDialog(owner,
			 new JDialogFinder(new DialogByTitleFinder(title,
                                                                    owner.getComparator())), 
			 index));
	copyEnvironment(owner);
    }

    /**
     * Constructor.
     * Waits for the dialog with "title" subtitle.
     * Uses owner's timeout and output for waiting and to init operator.
     * @param owner Operator pointing to a window owner.
     * @param title The desired title.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * 
     */
    public JDialogOperator(WindowOperator owner, String title) {
	this(owner, title, 0);
    }

    /**
     * Constructor.
     * Waits for the index'th dialog between owner's children.
     * Uses owner'th timeout and output for waiting and to init operator.
     * @param owner Operator pointing to a window owner.
     * @param index Ordinal component index.
     * 
     */
    public JDialogOperator(WindowOperator owner, int index) {
	this((JDialog)
	     waitJDialog(owner, 
			 new JDialogFinder(),
			 index));
	copyEnvironment(owner);
    }

    /**
     * Constructor.
     * Waits for the first dialog between owner's children.
     * Uses owner'th timeout and output for waiting and to init operator.
     * @param owner Operator pointing to a window owner.
     * 
     */
    public JDialogOperator(WindowOperator owner) {
	this(owner, 0);
    }

    /**
     * Constructor.
     * Waits for the dialog with "title" subtitle.
     * Constructor can be used in complicated cases when
     * output or timeouts should differ from default.
     * @param title a window title
     * @param index Ordinal component index.
     * @param env an operator to copy environment from.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * 
     */
    public JDialogOperator(String title, int index, Operator env) {
	this(new JDialogFinder(new DialogByTitleFinder(title, 
                                                       env.getComparator())), 
             index, env);
    }

    /**
     * Constructor.
     * Waits for the dialog with "title" subtitle.
     * Uses current timeouts and output values.
     * @param title a window title
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @see JemmyProperties#getCurrentTimeouts()
     * @see JemmyProperties#getCurrentOutput()
     * 
     */
    public JDialogOperator(String title, int index) {
	this(title, index,
	     ComponentOperator.getEnvironmentOperator());
    }

    /**
     * Constructor.
     * Waits for the dialog with "title" subtitle.
     * Uses current timeouts and output values.
     * @param title a window title
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @see JemmyProperties#getCurrentTimeouts()
     * @see JemmyProperties#getCurrentOutput()
     * 
     */
    public JDialogOperator(String title) {
	this(title, 0);
    }

    /**
     * Constructor.
     * Waits for the index'th dialog.
     * Uses current timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * 
     */
    public JDialogOperator(int index) {
	this((JDialog)
	     waitJDialog(new JDialogFinder(),
			 index,
			 ComponentOperator.getEnvironmentOperator().getTimeouts(),
			 ComponentOperator.getEnvironmentOperator().getOutput()));
	copyEnvironment(ComponentOperator.getEnvironmentOperator());
    }

    /**
     * Constructor.
     * Waits for the first dialog.
     * Uses current timeout and output for waiting and to init operator.
     * 
     */
    public JDialogOperator() {
	this(0);
    }

    /**
     * Searches an index'th dialog.
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     * @return JDialog instance or null if component was not found.
     */
    public static JDialog findJDialog(ComponentChooser chooser, int index) {
	return((JDialog)DialogWaiter.getDialog(new JDialogFinder(chooser), index));
    }

    /**
     * Searches a dialog.
     * @param chooser a component chooser specifying searching criteria.
     * @return JDialog instance or null if component was not found.
     */
    public static JDialog findJDialog(ComponentChooser chooser) {
	return(findJDialog(chooser, 0));
    }

    /**
     * Searches an index'th dialog by title.
     * @param title Dialog title
     * @param ce Compare exactly. If true, text can be a substring of caption.
     * @param cc Compare case sensitively. If true, both text and caption are 
     * @param index an index between appropriate ones.
     * @return JDialog instance or null if component was not found.
     */
    public static JDialog findJDialog(String title, boolean ce, boolean cc, int index) {
	return((JDialog)DialogWaiter.
	       getDialog(new JDialogFinder(new DialogByTitleFinder(title, 
                                                                    new DefaultStringComparator(ce, cc))), 
			 index));
    }

    /**
     * Searches a dialog by title.
     * @param title Dialog title
     * @param ce Compare exactly. If true, text can be a substring of caption.
     * @param cc Compare case sensitively. If true, both text and caption are 
     * @return JDialog instance or null if component was not found.
     */
    public static JDialog findJDialog(String title, boolean ce, boolean cc) {
	return(findJDialog(title, ce, cc, 0));
    }

    /**
     * Searches an index'th dialog between owner's owned windows.
     * @param owner Window - dialog owner.
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     * @return JDialog instance or null if component was not found.
     */
    public static JDialog findJDialog(Window owner, ComponentChooser chooser, int index) {
	return((JDialog)DialogWaiter.getDialog(owner, new JDialogFinder(chooser), index));
    }

    /**
     * Searches a dialog between owner's owned windows.
     * @param owner Window - dialog owner.
     * @param chooser a component chooser specifying searching criteria.
     * @return JDialog instance or null if component was not found.
     */
    public static JDialog findJDialog(Window owner, ComponentChooser chooser) {
	return(findJDialog(owner, chooser, 0));
    }

    /**
     * Searches an index'th dialog by title between owner's owned windows.
     * @param owner Window - dialog owner.
     * @param title Dialog title
     * @param ce Compare exactly. If true, text can be a substring of caption.
     * @param cc Compare case sensitively. If true, both text and caption are 
     * @param index an index between appropriate ones.
     * @return JDialog instance or null if component was not found.
     */
    public static JDialog findJDialog(Window owner, String title, boolean ce, boolean cc, int index) {
	return((JDialog)DialogWaiter.
	       getDialog(owner, 
			 new JDialogFinder(new DialogByTitleFinder(title, 
                                                                    new DefaultStringComparator(ce, cc))), 
			 index));
    }

    /**
     * Searches a dialog by title between owner's owned windows.
     * @param owner Window - dialog owner.
     * @param title Dialog title
     * @param ce Compare exactly. If true, text can be a substring of caption.
     * @param cc Compare case sensitively. If true, both text and caption are 
     * @return JDialog instance or null if component was not found.
     */
    public static JDialog findJDialog(Window owner, String title, boolean ce, boolean cc) {
	return(findJDialog(owner, title, ce, cc, 0));
    }

    /**
     * Waits an index'th dialog.
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     * @return JDialog instance or null if component was not found.
     * 
     */
    public static JDialog waitJDialog(ComponentChooser chooser, int index) {
	return(waitJDialog(chooser, index, 
			   JemmyProperties.getCurrentTimeouts(),
			   JemmyProperties.getCurrentOutput()));
    }

    /**
     * Waits a dialog.
     * @param chooser a component chooser specifying searching criteria.
     * @return JDialog instance or null if component was not found.
     * 
     */
    public static JDialog waitJDialog(ComponentChooser chooser) {
	return(waitJDialog(chooser, 0));
    }

    /**
     * Waits an index'th dialog by title.
     * @param title Dialog title
     * @param ce Compare exactly. If true, text can be a substring of caption.
     * @param cc Compare case sensitively. If true, both text and caption are 
     * @param index an index between appropriate ones.
     * @return JDialog instance or null if component was not found.
     * 
     */
    public static JDialog waitJDialog(String title, boolean ce, boolean cc, int index) {
	return(waitJDialog(new JDialogFinder(new DialogByTitleFinder(title, 
                                                                      new DefaultStringComparator(ce, cc))), 
						 index));
    }

    /**
     * Waits a dialog by title.
     * @param title Dialog title
     * @param ce Compare exactly. If true, text can be a substring of caption.
     * @param cc Compare case sensitively. If true, both text and caption are 
     * @return JDialog instance or null if component was not found.
     * 
     */
    public static JDialog waitJDialog(String title, boolean ce, boolean cc) {
	return(waitJDialog(title, ce, cc, 0));
    }

    /**
     * Waits an index'th dialog between owner's owned windows.
     * @param owner Window - dialog owner.
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     * @return JDialog instance or null if component was not found.
     * 
     */
    public static JDialog waitJDialog(Window owner, ComponentChooser chooser, int index) {
	return(waitJDialog(owner, chooser, index, 
			   JemmyProperties.getCurrentTimeouts(),
			   JemmyProperties.getCurrentOutput()));
    }

    /**
     * Waits a dialog between owner's owned windows.
     * @param owner Window - dialog owner.
     * @param chooser a component chooser specifying searching criteria.
     * @return JDialog instance or null if component was not found.
     * 
     */
    public static JDialog waitJDialog(Window owner, ComponentChooser chooser) {
	return(waitJDialog(owner, chooser, 0));
    }

    /**
     * Waits an index'th dialog by title between owner's owned windows.
     * @param owner Window - dialog owner.
     * @param title Dialog title
     * @param ce Compare exactly. If true, text can be a substring of caption.
     * @param cc Compare case sensitively. If true, both text and caption are 
     * @param index an index between appropriate ones.
     * @return JDialog instance or null if component was not found.
     * 
     */
    public static JDialog waitJDialog(Window owner, String title, boolean ce, boolean cc, int index) {
	return(waitJDialog(owner, new JDialogFinder(new DialogByTitleFinder(title, 
                                                                             new DefaultStringComparator(ce, cc))), 
			   index));
    }

    /**
     * Waits a dialog by title between owner's owned windows.
     * @param owner Window - dialog owner.
     * @param title Dialog title
     * @param ce Compare exactly. If true, text can be a substring of caption.
     * @param cc Compare case sensitively. If true, both text and caption are 
     * @return JDialog instance or null if component was not found.
     * 
     */
    public static JDialog waitJDialog(Window owner, String title, boolean ce, boolean cc) {
	return(waitJDialog(owner, title, ce, cc, 0));
    }

    /**
     * Searhs for modal dialog currently staying on top.
     * @return dialog or null if no modal dialog is currently
     * displayed.
     */
    public static Dialog getTopModalDialog() {
	return(DialogWaiter.getDialog(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
		    if(comp instanceof Dialog) {
			Dialog dialog = (Dialog)comp;
			if(dialog.isModal()) {
			    Window[] ow = dialog.getOwnedWindows();
			    for(int i = 0; i < ow.length; i++) {
				if(ow[i].isVisible()) {
				    return(false);
				}
			    }
			    return(true);
			}
		    }
		    return(false);
		}
		public String getDescription() {
		    return("Upper modal dialog");
		}
	    }));
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JDialog.getAccessibleContext()</code> through queue*/
    public AccessibleContext getAccessibleContext() {
	return((AccessibleContext)runMapping(new MapAction("getAccessibleContext") {
		public Object map() {
		    return(((JDialog)getSource()).getAccessibleContext());
		}}));}

    /**Maps <code>JDialog.getContentPane()</code> through queue*/
    public Container getContentPane() {
	return((Container)runMapping(new MapAction("getContentPane") {
		public Object map() {
		    return(((JDialog)getSource()).getContentPane());
		}}));}

    /**Maps <code>JDialog.getDefaultCloseOperation()</code> through queue*/
    public int getDefaultCloseOperation() {
	return(runMapping(new MapIntegerAction("getDefaultCloseOperation") {
		public int map() {
		    return(((JDialog)getSource()).getDefaultCloseOperation());
		}}));}

    /**Maps <code>JDialog.getGlassPane()</code> through queue*/
    public Component getGlassPane() {
	return((Component)runMapping(new MapAction("getGlassPane") {
		public Object map() {
		    return(((JDialog)getSource()).getGlassPane());
		}}));}

    /**Maps <code>JDialog.getJMenuBar()</code> through queue*/
    public JMenuBar getJMenuBar() {
	return((JMenuBar)runMapping(new MapAction("getJMenuBar") {
		public Object map() {
		    return(((JDialog)getSource()).getJMenuBar());
		}}));}

    /**Maps <code>JDialog.getLayeredPane()</code> through queue*/
    public JLayeredPane getLayeredPane() {
	return((JLayeredPane)runMapping(new MapAction("getLayeredPane") {
		public Object map() {
		    return(((JDialog)getSource()).getLayeredPane());
		}}));}

    /**Maps <code>JDialog.getRootPane()</code> through queue*/
    public JRootPane getRootPane() {
	return((JRootPane)runMapping(new MapAction("getRootPane") {
		public Object map() {
		    return(((JDialog)getSource()).getRootPane());
		}}));}

    /**Maps <code>JDialog.setContentPane(Container)</code> through queue*/
    public void setContentPane(final Container container) {
	runMapping(new MapVoidAction("setContentPane") {
		public void map() {
		    ((JDialog)getSource()).setContentPane(container);
		}});}

    /**Maps <code>JDialog.setDefaultCloseOperation(int)</code> through queue*/
    public void setDefaultCloseOperation(final int i) {
	runMapping(new MapVoidAction("setDefaultCloseOperation") {
		public void map() {
		    ((JDialog)getSource()).setDefaultCloseOperation(i);
		}});}

    /**Maps <code>JDialog.setGlassPane(Component)</code> through queue*/
    public void setGlassPane(final Component component) {
	runMapping(new MapVoidAction("setGlassPane") {
		public void map() {
		    ((JDialog)getSource()).setGlassPane(component);
		}});}

    /**Maps <code>JDialog.setJMenuBar(JMenuBar)</code> through queue*/
    public void setJMenuBar(final JMenuBar jMenuBar) {
	runMapping(new MapVoidAction("setJMenuBar") {
		public void map() {
		    ((JDialog)getSource()).setJMenuBar(jMenuBar);
		}});}

    /**Maps <code>JDialog.setLayeredPane(JLayeredPane)</code> through queue*/
    public void setLayeredPane(final JLayeredPane jLayeredPane) {
	runMapping(new MapVoidAction("setLayeredPane") {
		public void map() {
		    ((JDialog)getSource()).setLayeredPane(jLayeredPane);
		}});}

    /**Maps <code>JDialog.setLocationRelativeTo(Component)</code> through queue*/
    public void setLocationRelativeTo(final Component component) {
	runMapping(new MapVoidAction("setLocationRelativeTo") {
		public void map() {
		    ((JDialog)getSource()).setLocationRelativeTo(component);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * A method to be used from subclasses.
     * Uses timeouts and output passed as parameters during the waiting.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @param timeouts timeouts to be used during the waiting.
     * @param output an output to be used during the waiting.
     * @return Component instance or null if component was not found.
     */
    protected static JDialog waitJDialog(ComponentChooser chooser, int index,
					 Timeouts timeouts, TestOut output) {
	try {
	    DialogWaiter waiter = new DialogWaiter();
	    waiter.setTimeouts(timeouts);
	    waiter.setOutput(output);
	    return((JDialog)waiter.
		   waitDialog(new JDialogFinder(chooser), index));
	} catch(InterruptedException e) {
	    output.printStackTrace(e);
	    return(null);
	}
    }

    /**
     * A method to be used from subclasses.
     * Uses <code>owner</code>'s timeouts and output during the waiting.
     * @param owner a window - dialog owner.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return Component instance or null if component was not found.
     * @throws TimeoutExpiredException
     */
    protected static JDialog waitJDialog(WindowOperator owner, ComponentChooser chooser, int index) {
	return(waitJDialog((Window)owner.getSource(), 
			  chooser, index, 
			  owner.getTimeouts(), owner.getOutput()));
    }

    /**
     * A method to be used from subclasses.
     * Uses timeouts and output passed as parameters during the waiting.
     * @param owner a window - dialog owner.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @param timeouts timeouts to be used during the waiting.
     * @param output an output to be used during the waiting.
     * @return Component instance or null if component was not found.
     */
    protected static JDialog waitJDialog(Window owner, ComponentChooser chooser, int index,
					 Timeouts timeouts, TestOut output) {
	try {
	    DialogWaiter waiter = new DialogWaiter();
	    waiter.setTimeouts(timeouts);
	    waiter.setOutput(output);
	    return((JDialog)waiter.
		   waitDialog(owner, new JDialogFinder(chooser), index));
	} catch(InterruptedException e) {
	    JemmyProperties.getCurrentOutput().printStackTrace(e);
	    return(null);
	}
    }

    /**
     * Checks component type.
     */
    public static class JDialogFinder extends Finder {
        /**
         * Constructs JDialogFinder.
         * @param sf other searching criteria.
         */
	public JDialogFinder(ComponentChooser sf) {
            super(JDialog.class, sf);
	}
        /**
         * Constructs JDialogFinder.
         */
	public JDialogFinder() {
            super(JDialog.class);
	}
    }
}
