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
 * DialogWaiter.AfterDialogTimeout - time to sleep after dialog has been dispayed <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JDialogOperator extends DialogOperator {

    /**
     * Constructor.
     */
    public JDialogOperator(JDialog w) {
	super(w);
    }

    /**
     * Constructor.
     * Waits for the dialog with "title" subtitle.
     * Uses owner's timeout and output for waiting and to init operator.
     * @param owner Operator pointing on a window owner.
     * @param title
     * @param index
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JDialogOperator(WindowOperator owner, String title, int index) {
	this(waitJDialog(owner,
			 new JDialogSubChooser(new DialogByTitleChooser(title,
									owner.getComparator())), 
			 index));
	copyEnvironment(owner);
    }

    /**
     * Constructor.
     * Waits for the dialog with "title" subtitle.
     * Uses owner's timeout and output for waiting and to init operator.
     * @param owner Operator pointing on a window owner.
     * @param title
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JDialogOperator(WindowOperator owner, String title) {
	this(owner, title, 0);
    }

    /**
     * Constructor.
     * Waits for the index'th dialog between owner's children.
     * Uses owner'th timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JDialogOperator(WindowOperator owner, int index) {
	this((JDialog)
	     waitJDialog(owner, 
			 new JDialogSubChooser(ComponentSearcher.
					       getTrueChooser("Any JDialog")),
			 index));
	copyEnvironment(owner);
    }

    /**
     * Constructor.
     * Waits for the first dialog between owner's children.
     * Uses owner'th timeout and output for waiting and to init operator.
     * @throws TimeoutExpiredException
     */
    public JDialogOperator(WindowOperator owner) {
	this(owner, 0);
    }

    /**
     * Constructor.
     * Waits for the dialog with "title" subtitle.
     * Constructor can be used in complicated cases when
     * output or timeouts should differ from default.
     * @param title
     * @param index
     * @param timeouts 
     * @param output
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JDialogOperator(String title, int index, Operator env) {
	this(waitJDialog(new JDialogSubChooser(new DialogByTitleChooser(title, 
									env.getComparator())), 
			 index, env.getTimeouts(), env.getOutput()));
	copyEnvironment(env);
    }

    /**
     * Constructor.
     * Waits for the dialog with "title" subtitle.
     * Uses current timeouts and output values.
     * @param title
     * @param index
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @see JemmyProperties#getCurrentTimeouts()
     * @see JemmyProperties#getCurrentOutput()
     * @throws TimeoutExpiredException
     */
    public JDialogOperator(String title, int index) {
	this(title, index,
	     ComponentOperator.getEnvironmentOperator());
    }

    /**
     * Constructor.
     * Waits for the dialog with "title" subtitle.
     * Uses current timeouts and output values.
     * @param title
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @see JemmyProperties#getCurrentTimeouts()
     * @see JemmyProperties#getCurrentOutput()
     * @throws TimeoutExpiredException
     */
    public JDialogOperator(String title) {
	this(title, 0);
    }

    /**
     * Constructor.
     * Waits for the index'th dialog.
     * Uses current timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JDialogOperator(int index) {
	this((JDialog)
	     waitJDialog(new JDialogSubChooser(ComponentSearcher.
					       getTrueChooser("Any JDialog")),
			 index,
			 ComponentOperator.getEnvironmentOperator().getTimeouts(),
			 ComponentOperator.getEnvironmentOperator().getOutput()));
	copyEnvironment(ComponentOperator.getEnvironmentOperator());
    }

    /**
     * Constructor.
     * Waits for the first dialog.
     * Uses current timeout and output for waiting and to init operator.
     * @throws TimeoutExpiredException
     */
    public JDialogOperator() {
	this(0);
    }

    /**
     * Searches an index'th dialog.
     */
    public static JDialog findJDialog(ComponentChooser chooser, int index) {
	return((JDialog)DialogWaiter.getDialog(new JDialogSubChooser(chooser), index));
    }

    /**
     * Searches a dialog.
     */
    public static JDialog findJDialog(ComponentChooser chooser) {
	return(findJDialog(chooser, 0));
    }

    /**
     * Searches an index'th dialog by title.
     * @param title Dialog title
     */
    public static JDialog findJDialog(String title, boolean ce, boolean cc, int index) {
	return((JDialog)DialogWaiter.
	       getDialog(new JDialogSubChooser(new DialogByTitleChooser(title, 
									new DefaultStringComparator(ce, cc))), 
			 index));
    }

    /**
     * Searches a dialog by title.
     * @param title Dialog title
     */
    public static JDialog findJDialog(String title, boolean ce, boolean cc) {
	return(findJDialog(title, ce, cc, 0));
    }

    /**
     * Searches an index'th dialog between owner's owned windows.
     * @param owner Window - dialog owner.
     */
    public static JDialog findJDialog(Window owner, ComponentChooser chooser, int index) {
	return((JDialog)DialogWaiter.getDialog(owner, new JDialogSubChooser(chooser), index));
    }

    /**
     * Searches a dialog between owner's owned windows.
     * @param owner Window - dialog owner.
     */
    public static JDialog findJDialog(Window owner, ComponentChooser chooser) {
	return(findJDialog(owner, chooser, 0));
    }

    /**
     * Searches an index'th dialog by title between owner's owned windows.
     * @param owner Window - dialog owner.
     * @param title Dialog title
     */
    public static JDialog findJDialog(Window owner, String title, boolean ce, boolean cc, int index) {
	return((JDialog)DialogWaiter.
	       getDialog(owner, 
			 new JDialogSubChooser(new DialogByTitleChooser(title, 
									new DefaultStringComparator(ce, cc))), 
			 index));
    }

    /**
     * Searches a dialog by title between owner's owned windows.
     * @param owner Window - dialog owner.
     * @param title Dialog title
     */
    public static JDialog findJDialog(Window owner, String title, boolean ce, boolean cc) {
	return(findJDialog(owner, title, ce, cc, 0));
    }

    /**
     * Searches an index'th dialog.
     * @throws TimeoutExpiredException
     */
    public static JDialog waitJDialog(ComponentChooser chooser, int index) {
	return(waitJDialog(chooser, index, 
			   JemmyProperties.getCurrentTimeouts(),
			   JemmyProperties.getCurrentOutput()));
    }

    /**
     * Searches a dialog.
     * @throws TimeoutExpiredException
     */
    public static JDialog waitJDialog(ComponentChooser chooser) {
	return(waitJDialog(chooser, 0));
    }

    /**
     * Searches an index'th dialog by title.
     * @param title Dialog title
     * @throws TimeoutExpiredException
     */
    public static JDialog waitJDialog(String title, boolean ce, boolean cc, int index) {
	return(waitJDialog(new JDialogSubChooser(new DialogByTitleChooser(title, 
									  new DefaultStringComparator(ce, cc))), 
						 index));
    }

    /**
     * Searches a dialog by title.
     * @param title Dialog title
     * @throws TimeoutExpiredException
     */
    public static JDialog waitJDialog(String title, boolean ce, boolean cc) {
	return(waitJDialog(title, ce, cc, 0));
    }

    /**
     * Searches an index'th dialog between owner's owned windows.
     * @param owner Window - dialog owner.
     * @throws TimeoutExpiredException
     */
    public static JDialog waitJDialog(Window owner, ComponentChooser chooser, int index) {
	return(waitJDialog(owner, chooser, index, 
			   JemmyProperties.getCurrentTimeouts(),
			   JemmyProperties.getCurrentOutput()));
    }

    /**
     * Searches a dialog between owner's owned windows.
     * @param owner Window - dialog owner.
     * @throws TimeoutExpiredException
     */
    public static JDialog waitJDialog(Window owner, ComponentChooser chooser) {
	return(waitJDialog(owner, chooser, 0));
    }

    /**
     * Searches an index'th dialog by title between owner's owned windows.
     * @param owner Window - dialog owner.
     * @param title Dialog title
     * @throws TimeoutExpiredException
     */
    public static JDialog waitJDialog(Window owner, String title, boolean ce, boolean cc, int index) {
	return(waitJDialog(owner, new JDialogSubChooser(new DialogByTitleChooser(title, 
										 new DefaultStringComparator(ce, cc))), 
			   index));
    }

    /**
     * Searches a dialog by title between owner's owned windows.
     * @param owner Window - dialog owner.
     * @param title Dialog title
     * @throws TimeoutExpiredException
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

    protected static JDialog waitJDialog(ComponentChooser chooser, int index,
					 Timeouts timeouts, TestOut output) {
	try {
	    DialogWaiter waiter = new DialogWaiter();
	    waiter.setTimeouts(timeouts);
	    waiter.setOutput(output);
	    return((JDialog)waiter.
		   waitDialog(new JDialogSubChooser(chooser), index));
	} catch(InterruptedException e) {
	    output.printStackTrace(e);
	    return(null);
	}
    }

    protected static JDialog waitJDialog(WindowOperator owner, ComponentChooser chooser, int index) {
	return(waitJDialog((Window)owner.getSource(), 
			  chooser, index, 
			  owner.getTimeouts(), owner.getOutput()));
    }

    protected static JDialog waitJDialog(Window owner, ComponentChooser chooser, int index,
					 Timeouts timeouts, TestOut output) {
	try {
	    DialogWaiter waiter = new DialogWaiter();
	    waiter.setTimeouts(timeouts);
	    waiter.setOutput(output);
	    return((JDialog)waiter.
		   waitDialog(owner, new JDialogSubChooser(chooser), index));
	} catch(InterruptedException e) {
	    JemmyProperties.getCurrentOutput().printStackTrace(e);
	    return(null);
	}
    }

    private static class JDialogSubChooser implements ComponentChooser {
	private ComponentChooser chooser;
	public JDialogSubChooser(ComponentChooser c) {
	    super();
	    chooser = c;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JDialog) {
		return(chooser.checkComponent(comp));
	    } else {
		return(false);
	    }
	}
	public String getDescription() {
	    return(chooser.getDescription());
	}
    }
}
