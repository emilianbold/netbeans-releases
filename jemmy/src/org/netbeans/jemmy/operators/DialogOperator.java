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
import org.netbeans.jemmy.DialogWaiter;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;

import java.util.Hashtable;

/**
 * <BR><BR>Timeouts used: <BR>
 * DialogWaiter.WaitDialogTimeout - time to wait dialog displayed <BR>
 * DialogWaiter.AfterDialogTimeout - time to sleep after dialog has been dispayed <BR>
 * ComponentOperator.WaitStateTimeout - time to wait for title <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class DialogOperator extends WindowOperator {

    /**
     * Identifier for a title property.
     * @see #getDump
     */
    public static final String TITLE_DPROP = "Title";

    /**
     * Identifier for a modal property.
     * @see #getDump
     */
    public static final String IS_MODAL_DPROP = "Modal";

    /**
     * Identifier for a resizable property.
     * @see #getDump
     */
    public static final String IS_RESIZABLE_DPROP = "Resizable";

    /**
     * Constructs a DialogOperator object.
     * @param w window
     */
    public DialogOperator(Dialog w) {
	super(w);
    }

    /**
     * Constructs a DialogOperator object.
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     * @param env an operator to copy environment from.
     */
    public DialogOperator(ComponentChooser chooser, int index, Operator env) {
	this(waitDialog(new DialogFinder(chooser),
                        index, 
                        env.getTimeouts(),
			env.getOutput()));
	copyEnvironment(env);
    }

    /**
     * Constructs a DialogOperator object.
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public DialogOperator(ComponentChooser chooser, int index) {
	this(chooser, index, Operator.getEnvironmentOperator());
    }

    /**
     * Constructs a DialogOperator object.
     * @param chooser a component chooser specifying searching criteria.
     */
    public DialogOperator(ComponentChooser chooser) {
	this(chooser, 0);
    }

    /**
     * Constructs a DialogOperator object.
     * @param owner window - owner
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public DialogOperator(WindowOperator owner, ComponentChooser chooser, int index) {
	this((Dialog)owner.
             waitSubWindow(new DialogFinder(chooser),
                           index));
	copyEnvironment(owner);
    }

    /**
     * Constructs a DialogOperator object.
     * @param owner window - owner
     * @param chooser a component chooser specifying searching criteria.
     */
    public DialogOperator(WindowOperator owner, ComponentChooser chooser) {
	this(owner, chooser, 0);
    }

    /**
     * Constructor.
     * Waits for a dialog to show. The dialog is identified as the
     * <code>index+1</code>'th <code>java.awt.Dialog</code> that shows, is
     * owned by the window managed by the <code>WindowOperator</code>
     * <code>owner</code>, and that has the desired title.  Uses owner's
     * timeout and output for waiting and to init this operator.
     * @param owner Operator pointing to a window owner.
     * @param title The desired title.
     * @param index Ordinal index.  The first dialog has <code>index</code> 0.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public DialogOperator(WindowOperator owner, String title, int index) {
	this(waitDialog(owner,
			 new DialogByTitleFinder(title,
                                                 owner.getComparator()), 
			 index));
	copyEnvironment(owner);
    }

    /**
     * Uses owner's timeout and output for waiting and to init operator.
     * Waits for a dialog to show. The dialog is identified as the
     * first <code>java.awt.Dialog</code> that shows, is
     * owned by the window managed by the <code>WindowOperator</code>
     * <code>owner</code>, and that has the desired title.  Uses owner's
     * timeout and output for waiting and to init this operator.
     * @param owner Operator pointing to a window owner.
     * @param title The desired title.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public DialogOperator(WindowOperator owner, String title) {
	this(owner, title, 0);
    }

    /**
     * Constructor.
     * Waits for the index'th dialog between owner's children.
     * Uses owner'th timeout and output for waiting and to init operator.
     * @param owner Operator pointing to a window owner.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public DialogOperator(WindowOperator owner, int index) {
	this((Dialog)
	     waitDialog(owner, 
			new DialogFinder(),
			index));
	copyEnvironment(owner);
    }

    /**
     * Constructor.
     * Waits for the first dialog between owner's children.
     * Uses owner'th timeout and output for waiting and to init operator.
     * @param owner Operator pointing to a window owner.
     * @throws TimeoutExpiredException
     */
    public DialogOperator(WindowOperator owner) {
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
     * @throws TimeoutExpiredException
     */
    public DialogOperator(String title, int index, Operator env) {
	this(new DialogByTitleFinder(title, 
                                     env.getComparator()),
             index,
             env);
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
     * @throws TimeoutExpiredException
     */
    public DialogOperator(String title, int index){
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
     * @throws TimeoutExpiredException
     */
    public DialogOperator(String title) {
	this(title, 0);
    }

    /**
     * Constructor.
     * Waits for the index'th dialog.
     * Uses current timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public DialogOperator(int index) {
	this((Dialog)
	     waitDialog(new DialogFinder(),
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
    public DialogOperator() {
	this(0);
    }

    /**
     * Waits for title. Uses getComparator() comparator.
     * @param title Title to wait for.
     */
    public void waitTitle(final String title) {
	getOutput().printLine("Wait \"" + title + "\" title of dialog \n    : "+
			      toStringSource());
	getOutput().printGolden("Wait \"" + title + "\" title");
	waitState(new DialogByTitleFinder(title, getComparator()));
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
        if(((Dialog)getSource()).getTitle() != null) {
            result.put(TITLE_DPROP, ((Dialog)getSource()).getTitle());
        }
	result.put(IS_MODAL_DPROP, ((Dialog)getSource()).isModal() ? "true" : "false");
	result.put(IS_RESIZABLE_DPROP, ((Dialog)getSource()).isResizable() ? "true" : "false");
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>Dialog.getTitle()</code> through queue*/
    public String getTitle() {
	return((String)runMapping(new MapAction("getTitle") {
		public Object map() {
		    return(((Dialog)getSource()).getTitle());
		}}));}

    /**Maps <code>Dialog.isModal()</code> through queue*/
    public boolean isModal() {
	return(runMapping(new MapBooleanAction("isModal") {
		public boolean map() {
		    return(((Dialog)getSource()).isModal());
		}}));}

    /**Maps <code>Dialog.isResizable()</code> through queue*/
    public boolean isResizable() {
	return(runMapping(new MapBooleanAction("isResizable") {
		public boolean map() {
		    return(((Dialog)getSource()).isResizable());
		}}));}

    /**Maps <code>Dialog.setModal(boolean)</code> through queue*/
    public void setModal(final boolean b) {
	runMapping(new MapVoidAction("setModal") {
		public void map() {
		    ((Dialog)getSource()).setModal(b);
		}});}

    /**Maps <code>Dialog.setResizable(boolean)</code> through queue*/
    public void setResizable(final boolean b) {
	runMapping(new MapVoidAction("setResizable") {
		public void map() {
		    ((Dialog)getSource()).setResizable(b);
		}});}

    /**Maps <code>Dialog.setTitle(String)</code> through queue*/
    public void setTitle(final String string) {
	runMapping(new MapVoidAction("setTitle") {
		public void map() {
		    ((Dialog)getSource()).setTitle(string);
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
    protected static Dialog waitDialog(ComponentChooser chooser, int index,
					 Timeouts timeouts, TestOut output) {
	try {
	    DialogWaiter waiter = new DialogWaiter();
	    waiter.setTimeouts(timeouts);
	    waiter.setOutput(output);
	    return((Dialog)waiter.
		   waitDialog(new DialogFinder(chooser), index));
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
     */
    protected static Dialog waitDialog(WindowOperator owner, ComponentChooser chooser, int index) {
	return(waitDialog((Window)owner.getSource(), 
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
    protected static Dialog waitDialog(Window owner, ComponentChooser chooser, int index,
					 Timeouts timeouts, TestOut output) {
	try {
	    DialogWaiter waiter = new DialogWaiter();
	    waiter.setTimeouts(timeouts);
	    waiter.setOutput(output);
	    return((Dialog)waiter.
		   waitDialog(owner, new DialogFinder(chooser), index));
	} catch(InterruptedException e) {
	    JemmyProperties.getCurrentOutput().printStackTrace(e);
	    return(null);
	}
    }


    /**
     * Checks component type.
     */
    public static class DialogFinder extends Finder {
        /**
         * Constructs DialogFinder.
         * @param sf other searching criteria.
         */
	public DialogFinder(ComponentChooser sf) {
            super(Dialog.class, sf);
	}
        /**
         * Constructs DialogFinder.
         */
	public DialogFinder() {
            super(Dialog.class);
	}
    }

    /**
     * Allows to find component by title.
     */
    public static class DialogByTitleFinder implements ComponentChooser {
	String title;
	StringComparator comparator;
        /**
         * Constructs DialogByTitleFinder.
         * @param t a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public DialogByTitleFinder(String t, StringComparator comparator) {
	    title = t;
	    this.comparator = comparator;
	}
        /**
         * Constructs DialogByTitleFinder.
         * @param t a text pattern
         */
	public DialogByTitleFinder(String t) {
            this(t, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof Dialog) {
		if(((Dialog)comp).isShowing() && ((Dialog)comp).getTitle() != null) {
		    return(comparator.equals(((Dialog)comp).getTitle(), title));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("Dialog with title \"" + title + "\"");
	}
    }
}

