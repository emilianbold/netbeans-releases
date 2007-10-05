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
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.FrameWaiter;

import java.awt.Component;
import java.awt.Container;

import javax.accessibility.AccessibleContext;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;

/**
 * <BR><BR>Timeouts used: <BR>
 * FrameWaiter.WaitFrameTimeout - time to wait frame displayed <BR>
 * FrameWaiter.AfterFrameTimeout - time to sleep after frame has been dispayed <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JFrameOperator extends FrameOperator {

    /**
     * Constructor.
     * @param w window
     */
    public JFrameOperator(JFrame w) {
	super(w);
    }

    /**
     * Constructs a JFrameOperator object.
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     * @param env an operator to copy environment from.
     */
    public JFrameOperator(ComponentChooser chooser, int index, Operator env) {
	this((JFrame)waitFrame(new JFrameFinder(chooser),
                               index, 
                               env.getTimeouts(),
                               env.getOutput()));
	copyEnvironment(env);
    }

    /**
     * Constructs a JFrameOperator object.
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JFrameOperator(ComponentChooser chooser, int index) {
	this(chooser, index, Operator.getEnvironmentOperator());
    }

    /**
     * Constructs a JFrameOperator object.
     * @param chooser a component chooser specifying searching criteria.
     */
    public JFrameOperator(ComponentChooser chooser) {
	this(chooser, 0);
    }

    /**
     * Constructor.
     * Waits for the frame with "title" subtitle.
     * Constructor can be used in complicated cases when
     * output or timeouts should differ from default.
     * @param title a window title
     * @param index Ordinal component index.
     * @param env an operator to copy environment from.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * 
     */
    public JFrameOperator(String title, int index, Operator env) {
	this(new JFrameFinder(new FrameByTitleFinder(title, 
                                                     env.getComparator())),
             index, env);
    }

    /**
     * Constructor.
     * Waits for the frame with "title" subtitle.
     * Uses current timeouts and output values.
     * @param title a window title
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @see JemmyProperties#getCurrentTimeouts()
     * @see JemmyProperties#getCurrentOutput()
     * 
     */
    public JFrameOperator(String title, int index) {
	this(title, index,
	     ComponentOperator.getEnvironmentOperator());
    }

    /**
     * Constructor.
     * Waits for the frame with "title" subtitle.
     * Uses current timeouts and output values.
     * @param title a window title
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @see JemmyProperties#getCurrentTimeouts()
     * @see JemmyProperties#getCurrentOutput()
     * 
     */
    public JFrameOperator(String title) {
	this(title, 0);
    }

    /**
     * Constructor.
     * Waits for the index'th frame.
     * Uses current timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * 
     */
    public JFrameOperator(int index) {
	this((JFrame)
	     (JFrame)waitFrame(new JFrameFinder(),
			       index,
			       ComponentOperator.getEnvironmentOperator().getTimeouts(),
			       ComponentOperator.getEnvironmentOperator().getOutput()));
	copyEnvironment(ComponentOperator.getEnvironmentOperator());
    }

    /**
     * Constructor.
     * Waits for the first frame.
     * Uses current timeout and output for waiting and to init operator.
     * 
     */
    public JFrameOperator() {
	this(0);
    }

    /**
     * Searches an index'th frame.
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     * @return JFrame instance or null if component was not found.
     */
    public static JFrame findJFrame(ComponentChooser chooser, int index) {
	return((JFrame)FrameWaiter.getFrame(new JFrameFinder(chooser), index));
    }

    /**
     * Searches a frame.
     * @param chooser a component chooser specifying searching criteria.
     * @return JFrame instance or null if component was not found.
     */
    public static JFrame findJFrame(ComponentChooser chooser) {
	return(findJFrame(chooser, 0));
    }

    /**
     * Searches an index'th frame by title.
     * @param title Frame title
     * @param ce Compare exactly. If true, text can be a substring of caption.
     * @param cc Compare case sensitively. If true, both text and caption are 
     * @param index an index between appropriate ones.
     * @return JFrame instance or null if component was not found.
     */
    public static JFrame findJFrame(String title, boolean ce, boolean cc, int index) {
	return((JFrame)FrameWaiter.
	       getFrame(new JFrameFinder(new FrameByTitleFinder(title, 
                                                                 new DefaultStringComparator(ce, cc))), 
			index));
    }

    /**
     * Searches a frame by title.
     * @param title Frame title
     * @param ce Compare exactly. If true, text can be a substring of caption.
     * @param cc Compare case sensitively. If true, both text and caption are 
     * @return JFrame instance or null if component was not found.
     */
    public static JFrame findJFrame(String title, boolean ce, boolean cc) {
	return(findJFrame(title, ce, cc, 0));
    }

    /**
     * Waits an index'th frame.
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     * @return JFrame instance or null if component was not found.
     * 
     */
    public static JFrame waitJFrame(ComponentChooser chooser, int index) {
	return((JFrame)waitFrame(new JFrameFinder(chooser), index,
				 JemmyProperties.getCurrentTimeouts(),
				 JemmyProperties.getCurrentOutput()));
    }

    /**
     * Waits a frame.
     * @param chooser a component chooser specifying searching criteria.
     * @return JFrame instance or null if component was not found.
     * 
     */
    public static JFrame waitJFrame(ComponentChooser chooser) {
	return(waitJFrame(chooser, 0));
    }

    /**
     * Waits an index'th frame by title.
     * @param title Frame title
     * @param ce Compare exactly. If true, text can be a substring of caption.
     * @param cc Compare case sensitively. If true, both text and caption are 
     * @param index an index between appropriate ones.
     * @return JFrame instance or null if component was not found.
     * 
     */
    public static JFrame waitJFrame(String title, boolean ce, boolean cc, int index) {
	try {
	    return((JFrame)(new FrameWaiter()).
		   waitFrame(new JFrameFinder(new 
                                              FrameByTitleFinder(title, 
                                                                  new DefaultStringComparator(ce, cc))), 
			     index));
	} catch(InterruptedException e) {
	    JemmyProperties.getCurrentOutput().printStackTrace(e);
	    return(null);
	}
    }

    /**
     * Waits a frame by title.
     * @param title Frame title
     * @param ce Compare exactly. If true, text can be a substring of caption.
     * @param cc Compare case sensitively. If true, both text and caption are 
     * @return JFrame instance or null if component was not found.
     * 
     */
    public static JFrame waitJFrame(String title, boolean ce, boolean cc) {
	return(waitJFrame(title, ce, cc, 0));
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JFrame.getAccessibleContext()</code> through queue*/
    public AccessibleContext getAccessibleContext() {
	return((AccessibleContext)runMapping(new MapAction("getAccessibleContext") {
		public Object map() {
		    return(((JFrame)getSource()).getAccessibleContext());
		}}));}

    /**Maps <code>JFrame.getContentPane()</code> through queue*/
    public Container getContentPane() {
	return((Container)runMapping(new MapAction("getContentPane") {
		public Object map() {
		    return(((JFrame)getSource()).getContentPane());
		}}));}

    /**Maps <code>JFrame.getDefaultCloseOperation()</code> through queue*/
    public int getDefaultCloseOperation() {
	return(runMapping(new MapIntegerAction("getDefaultCloseOperation") {
		public int map() {
		    return(((JFrame)getSource()).getDefaultCloseOperation());
		}}));}

    /**Maps <code>JFrame.getGlassPane()</code> through queue*/
    public Component getGlassPane() {
	return((Component)runMapping(new MapAction("getGlassPane") {
		public Object map() {
		    return(((JFrame)getSource()).getGlassPane());
		}}));}

    /**Maps <code>JFrame.getJMenuBar()</code> through queue*/
    public JMenuBar getJMenuBar() {
	return((JMenuBar)runMapping(new MapAction("getJMenuBar") {
		public Object map() {
		    return(((JFrame)getSource()).getJMenuBar());
		}}));}

    /**Maps <code>JFrame.getLayeredPane()</code> through queue*/
    public JLayeredPane getLayeredPane() {
	return((JLayeredPane)runMapping(new MapAction("getLayeredPane") {
		public Object map() {
		    return(((JFrame)getSource()).getLayeredPane());
		}}));}

    /**Maps <code>JFrame.getRootPane()</code> through queue*/
    public JRootPane getRootPane() {
	return((JRootPane)runMapping(new MapAction("getRootPane") {
		public Object map() {
		    return(((JFrame)getSource()).getRootPane());
		}}));}

    /**Maps <code>JFrame.setContentPane(Container)</code> through queue*/
    public void setContentPane(final Container container) {
	runMapping(new MapVoidAction("setContentPane") {
		public void map() {
		    ((JFrame)getSource()).setContentPane(container);
		}});}

    /**Maps <code>JFrame.setDefaultCloseOperation(int)</code> through queue*/
    public void setDefaultCloseOperation(final int i) {
	runMapping(new MapVoidAction("setDefaultCloseOperation") {
		public void map() {
		    ((JFrame)getSource()).setDefaultCloseOperation(i);
		}});}

    /**Maps <code>JFrame.setGlassPane(Component)</code> through queue*/
    public void setGlassPane(final Component component) {
	runMapping(new MapVoidAction("setGlassPane") {
		public void map() {
		    ((JFrame)getSource()).setGlassPane(component);
		}});}

    /**Maps <code>JFrame.setJMenuBar(JMenuBar)</code> through queue*/
    public void setJMenuBar(final JMenuBar jMenuBar) {
	runMapping(new MapVoidAction("setJMenuBar") {
		public void map() {
		    ((JFrame)getSource()).setJMenuBar(jMenuBar);
		}});}

    /**Maps <code>JFrame.setLayeredPane(JLayeredPane)</code> through queue*/
    public void setLayeredPane(final JLayeredPane jLayeredPane) {
	runMapping(new MapVoidAction("setLayeredPane") {
		public void map() {
		    ((JFrame)getSource()).setLayeredPane(jLayeredPane);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Checks component type.
     */
    public static class JFrameFinder extends Finder {
        /**
         * Constructs JFrameFinder.
         * @param sf other searching criteria.
         */
	public JFrameFinder(ComponentChooser sf) {
            super(JFrame.class, sf);
	}
        /**
         * Constructs JFrameFinder.
         */
	public JFrameFinder() {
            super(JFrame.class);
	}
    }
}
