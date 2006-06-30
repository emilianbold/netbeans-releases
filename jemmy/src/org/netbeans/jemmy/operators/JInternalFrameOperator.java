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

import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyInputException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.FrameDriver;
import org.netbeans.jemmy.drivers.InternalFrameDriver;
import org.netbeans.jemmy.drivers.WindowDriver;


import org.netbeans.jemmy.util.EmptyVisualizer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;

import java.beans.PropertyVetoException;

import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JInternalFrame.JDesktopIcon;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;

import javax.swing.event.InternalFrameListener;

import javax.swing.plaf.InternalFrameUI;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

/**
 * Class provides necessary functionality to operate with javax.swing.JInternalFrame component.
 *
 * Some methods can throw WrongInternalFrameStateException exception.
 *
 * <BR><BR>Timeouts used: <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 * ComponentOperator.MouseClickTimeout - time between mouse pressing and releasing <BR>
 * AbstractButtonOperator.PushButtonTimeout - time between button pressing and releasing<BR>
 * JScrollBarOperator.WholeScrollTimeout - time for the whole scrolling <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 * @see WrongInternalFrameStateException
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class JInternalFrameOperator extends JComponentOperator
    implements Outputable, Timeoutable {

    /**
     * Identifier for a "title" property.
     * @see #getDump
     */
    public static final String TITLE_DPROP = "Title";

    /**
     * Identifier for a "state" property.
     * @see #getDump
     */
    public static final String STATE_DPROP = "State";

    /**
     * Identifier for a "normal" value of "state" property.
     * @see #getDump
     */
    public static final String STATE_NORMAL_DPROP_VALUE = "NORMAL";

    /**
     * Identifier for a "closed" value of "state" property.
     * @see #getDump
     */
    public static final String STATE_CLOSED_DPROP_VALUE = "CLOSED";

    /**
     * Identifier for a "iconified" value of "state" property.
     * @see #getDump
     */
    public static final String STATE_ICONIFIED_DPROP_VALUE = "ICONIFIED";

    /**
     * Identifier for a "maximized" value of "state" property.
     * @see #getDump
     */
    public static final String STATE_MAXIMAZED_DPROP_VALUE = "MAXIMIZED";

    /**
     * Identifier for a "resizable" property.
     * @see #getDump
     */
    public static final String IS_RESIZABLE_DPROP = "Resizable";

    /**
     * Identifier for a "selected" property.
     * @see #getDump
     */
    public static final String IS_SELECTED_DPROP = "Selected";

    /**
     * A minimizing button.
     */
    protected JButtonOperator minOper = null;

    /**
     * A maximizing button.
     */
    protected JButtonOperator maxOper = null;

    /**
     * A close button.
     */
    protected JButtonOperator closeOper = null;

    /**
     * A title operator.
     */
    protected ContainerOperator titleOperator = null;
    private TestOut output;
    private Timeouts timeouts;
    private JDesktopIconOperator iconOperator;

    WindowDriver wDriver;
    FrameDriver  fDriver;
    InternalFrameDriver  iDriver;

    /**
     * Constructor.
     * @param b a component
     */
    public JInternalFrameOperator(JInternalFrame b) {
	super(b);
	wDriver = DriverManager.getWindowDriver(getClass());
	fDriver = DriverManager.getFrameDriver(getClass());
	iDriver = DriverManager.getInternalFrameDriver(getClass());
    }

    /**
     * Constructs a JInternalFrameOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JInternalFrameOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JInternalFrame)cont.
             waitSubComponent(new JInternalFrameFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JInternalFrameOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JInternalFrameOperator(ContainerOperator cont, ComponentChooser chooser) {
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
    public JInternalFrameOperator(ContainerOperator cont, String text, int index) {
	this(findOne(cont, text, index));
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
    public JInternalFrameOperator(ContainerOperator cont, String text) {
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
    public JInternalFrameOperator(ContainerOperator cont, int index) {
	this((JInternalFrame)
	     waitComponent(cont, 
			   new JInternalFrameFinder(),
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
    public JInternalFrameOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JInternalframe in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JInternalframe instance or null if component was not found.
     */
    public static JInternalFrame findJInternalFrame(Container cont, ComponentChooser chooser, int index) {
	Component res = findComponent(cont, new JInternalFrameFinder(chooser), index);
	if(res instanceof JInternalFrame) {
	    return((JInternalFrame)res);
	} else if(res instanceof JInternalFrame.JDesktopIcon) {
	    return(((JInternalFrame.JDesktopIcon)res).getInternalFrame());
	} else {
	    return(null);
	}
    }

    /**
     * Searches JInternalframe in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JInternalframe instance or null if component was not found.
     */
    public static JInternalFrame findJInternalFrame(Container cont, ComponentChooser chooser) {
	return(findJInternalFrame(cont, chooser, 0));
    }

    /**
     * Searches JInternalframe by title.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JInternalframe instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JInternalFrame findJInternalFrame(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(findJInternalFrame(cont, 
				  new JInternalFrameByTitleFinder(text, 
								  new DefaultStringComparator(ce, ccs)), 
				  index));
    }

    /**
     * Searches JInternalframe by title.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JInternalframe instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JInternalFrame findJInternalFrame(Container cont, String text, boolean ce, boolean ccs) {
	return(findJInternalFrame(cont, text, ce, ccs, 0));
    }

    /**
     * Searches JInternalFrame object which component lies on.
     * @param comp Component to find JInternalFrame under.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JInternalFrame instance or null if component was not found.
     */
    public static JInternalFrame findJInternalFrameUnder(Component comp, ComponentChooser chooser) {
	return((JInternalFrame)findContainerUnder(comp, new JInternalFrameFinder(chooser)));
    }
    
    /**
     * Searches JInternalFrame object which component lies on.
     * @param comp Component to find JInternalFrame under.
     * @return JInternalFrame instance or null if component was not found.
     */
    public static JInternalFrame findJInternalFrameUnder(Component comp) {
	return(findJInternalFrameUnder(comp, new JInternalFrameFinder()));
    }
    
    /**
     * Waits JInternalframe in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @param index Ordinal component index.
     * @return JInternalframe instance.
     * 
     */
    public static JInternalFrame waitJInternalFrame(final Container cont, final ComponentChooser chooser, final int index) {
	Component res = waitComponent(cont, new JInternalFrameFinder(chooser), index);
	if(res instanceof JInternalFrame) {
	    return((JInternalFrame)res);
	} else if(res instanceof JInternalFrame.JDesktopIcon) {
	    return(((JInternalFrame.JDesktopIcon)res).getInternalFrame());
	} else {
	    throw(new TimeoutExpiredException(chooser.getDescription()));
	}
    }

    /**
     * Waits JInternalframe in container.
     * @param cont Container to search component in.
     * @param chooser a component chooser specifying searching criteria.
     * @return JInternalframe instance.
     * 
     */
    public static JInternalFrame waitJInternalFrame(Container cont, ComponentChooser chooser) {
	return(waitJInternalFrame(cont, chooser, 0));
    }

    /**
     * Waits JInternalframe by title.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JInternalframe instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * 
     */
    public static JInternalFrame waitJInternalFrame(Container cont, String text, boolean ce, boolean ccs, int index) {
	return(waitJInternalFrame(cont, 
				  new JInternalFrameByTitleFinder(text, 
								  new DefaultStringComparator(ce, ccs)), 
				  index));
    }

    /**
     * Waits JInternalframe by title.
     * @param cont Container to search component in.
     * @param text Component text.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JInternalframe instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * 
     */
    public static JInternalFrame waitJInternalFrame(Container cont, String text, boolean ce, boolean ccs) {
	return(waitJInternalFrame(cont, text, ce, ccs, 0));
    }

    public void setOutput(TestOut out) {
	output = out;
	super.setOutput(output.createErrorOutput());
    }

    public TestOut getOutput() {
	return(output);
    }

    public void setTimeouts(Timeouts times) {
	timeouts = times;
	super.setTimeouts(timeouts);
    }

    public Timeouts getTimeouts() {
	return(timeouts);
    }

    /**
     * Iconifies frame.
     * Note: frame should not be iconified and should be iconifiable.
     * @throws WrongInternalFrameStateException
     * 
     */
    public void iconify() {
	output.printLine("Iconify JInternalFrame\n    : " + toStringSource());
	output.printGolden("Iconify JInternalFrame \"" + getTitle() + "\"");
	checkIconified(false);
	makeComponentVisible();
	fDriver.iconify(this);
	if(getVerification()) {
            waitIcon(true);
        }
    }

    /**
     * Deiconifies frame.
     * Note: frame should be iconified.
     * @throws WrongInternalFrameStateException
     * 
     */
    public void deiconify() {
	output.printLine("Deiconify JInternalFrame\n    : " + toStringSource());
	output.printGolden("Deiconify JInternalFrame \"" + getTitle() + "\"");
	checkIconified(true);
	fDriver.deiconify(this);
	if(getVerification()) {
            waitIcon(false);
        }
    }

    /**
     * Maximizes frame.
     * Note: frame should not be iconified.
     * 
     * @throws WrongInternalFrameStateException
     */
    public void maximize() {
	output.printLine("Maximize JInternalFrame\n    : " + toStringSource());
	output.printGolden("Maximize JInternalFrame \"" + getTitle() + "\"");
	checkIconified(false);
	makeComponentVisible();
	fDriver.maximize(this);
	if(getVerification()) {
            waitMaximum(true);
        }
    }

    /**
     * Demaximizes frame.
     * Note: frame should not be iconified.
     * 
     * @throws WrongInternalFrameStateException
     */
    public void demaximize() {
	output.printLine("Demaximize JInternalFrame\n    : " + toStringSource());
	output.printGolden("Demaximize JInternalFrame \"" + getTitle() + "\"");
	checkIconified(false);
	makeComponentVisible();
	fDriver.demaximize(this);
	if(getVerification()) {
            waitMaximum(false);
        }
    }

    /**
     * Moves frame to new location.
     * Note: frame should not be iconified.
     * @param x X coordinate of a new frame location.
     * @param y Y coordinate of a new frame location.
     * @throws WrongInternalFrameStateException
     */
    public void move(int x, int y) {
	checkIconified(false);
	output.printLine("Move JInternalFrame to (" +
			 Integer.toString(x) + "," +
			 Integer.toString(y) + ")" +
			 " position\n    : " + toStringSource());
	output.printGolden("Move " + getTitle() + 
			   " JInternalFrame to (" +
			   Integer.toString(x) + "," +
			   Integer.toString(y) + ")" +
			   " position");
	checkIconified(false);
	wDriver.move(this, x, y);
    }

    /**
     * Resizes frame.
     * Note: frame should not be iconified.
     * @param width New frame width.
     * @param height New frame height.
     * @throws WrongInternalFrameStateException
     */
    public void resize(int width, int height) {
	output.printLine("Resize JInternalFrame to (" +
			 Integer.toString(width) + "," +
			 Integer.toString(height) + ")" +
			 " size\n    : " + toStringSource());
	output.printGolden("Resize " + getTitle() + 
			   " JInternalFrame to (" +
			   Integer.toString(width) + "," +
			   Integer.toString(height) + ")" +
			   " size");
	checkIconified(false);
	wDriver.resize(this, width, height);
    }

    /**
     * Activates frame.
     * Note: frame should not be iconified.
     * @throws WrongInternalFrameStateException
     */
    public void activate() {
	checkIconified(false);
	wDriver.activate(this);
    }

    /**
     * Closes the frame.
     */
    public void close() {
	checkIconified(false);
	wDriver.close(this);
    }

    /**
     * Scrolls to internal frame's rectangle.
     * @param x Horizontal rectangle coordinate
     * @param y Vertical rectangle  coordinate
     * @param width rectangle width
     * @param height rectangle height
     * 
     */
    public void scrollToRectangle(int x, int y, int width, int height) {
	output.printTrace("Scroll desktop pane to make \"" + getTitle() + "\" internal frame visible");
	output.printGolden("Scroll desktop pane to make \"" + getTitle() + "\" internal frame visible");
	makeComponentVisible();
	Component cmp = isIcon() ? iconOperator.getSource() : getSource();
	//try to find JScrollPane under.
	JScrollPane scroll;
	if(isIcon()) {
	    scroll = 
		(JScrollPane)iconOperator.getContainer(new JScrollPaneOperator.
						       JScrollPaneFinder(ComponentSearcher.
									 getTrueChooser("JScrollPane")));
	} else {
	    scroll = 
		(JScrollPane)getContainer(new JScrollPaneOperator.
					  JScrollPaneFinder(ComponentSearcher.
							    getTrueChooser("JScrollPane")));
	}
	if(scroll == null) {
	    return;
	}
	JScrollPaneOperator scroller = new JScrollPaneOperator(scroll);
	scroller.copyEnvironment(this);
	scroller.setVisualizer(new EmptyVisualizer());
	scroller.scrollToComponentRectangle(isIcon() ? iconOperator.getSource() : getSource(), 
					    x, y, width, height);
    }

    /**
     * Scrolls to internal frame's rectangle.
     * @param rect a rectangle to scroll to.
     */
    public void scrollToRectangle(Rectangle rect) {
	scrollToRectangle(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Scrolls to internal frame.
     * 
     */
    public void scrollToFrame() {
	if(isIcon()) {
	    scrollToRectangle(0, 0, iconOperator.getWidth(), iconOperator.getHeight());
	} else {
	    scrollToRectangle(0, 0, getWidth(), getHeight());
	}
    }

    /**
     * Waits for a minimize button inside the title pane.
     * @return a button operator
     */
    public JButtonOperator getMinimizeButton() {
	initOperators();
	return(minOper);
    }

    /**
     * Waits for a maximize button inside the title pane.
     * @return a button operator
     */
    public JButtonOperator getMaximizeButton() {
	initOperators();
	return(maxOper);
    }

    /**
     * Waits for a close button inside the title pane.
     * @return a button operator
     */
    public JButtonOperator getCloseButton() {
	initOperators();
	return(closeOper);
    }

    /**
     * Waits for the title pane.
     * @return a button operator
     */
    public ContainerOperator getTitleOperator() {
	initOperators();
	return(titleOperator);
    }

    /**
     * Creates an operator for an desktop icon.
     * @return an icon operator.
     */
    public JDesktopIconOperator getIconOperator() {
	initOperators();
	return(iconOperator);
    }

    /**
     * Waits for the frame to be iconified or deiconified.
     * @param icon whether the frame needs to be iconified.
     */
    public void waitIcon(final boolean icon) {
        waitState(new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    return(((JInternalFrame)comp).isIcon() == icon);
                }
                public String getDescription() {
                    return("Iconified JInternalFrame");
                }
            });
    }

    /**
     * Waits for the frame to be maximized or demaximized.
     * @param maximum whether the frame needs to be maximized.
     */
    public void waitMaximum(final boolean maximum) {
        waitState(new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    return(((JInternalFrame)comp).isMaximum() == maximum);
                }
                public String getDescription() {
                    return("Maximizied JInternalFrame");
                }
            });
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put(TITLE_DPROP, ((JInternalFrame)getSource()).getTitle());
	String state = STATE_NORMAL_DPROP_VALUE;
	if       (((JInternalFrame)getSource()).isClosed()) {
	    state = STATE_CLOSED_DPROP_VALUE;
	} else if(((JInternalFrame)getSource()).isIcon()) {
	    state = STATE_ICONIFIED_DPROP_VALUE;
	} else if(((JInternalFrame)getSource()).isMaximum()) {
	    state = STATE_MAXIMAZED_DPROP_VALUE;
	}
	result.put(STATE_DPROP, state);
	result.put(IS_RESIZABLE_DPROP, ((JInternalFrame)getSource()).isResizable() ? "true" : "false");
	result.put(IS_SELECTED_DPROP, ((JInternalFrame)getSource()).isSelected() ? "true" : "false");
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JInternalFrame.addInternalFrameListener(InternalFrameListener)</code> through queue*/
    public void addInternalFrameListener(final InternalFrameListener internalFrameListener) {
	runMapping(new MapVoidAction("addInternalFrameListener") {
		public void map() {
		    ((JInternalFrame)getSource()).addInternalFrameListener(internalFrameListener);
		}});}

    /**Maps <code>JInternalFrame.dispose()</code> through queue*/
    public void dispose() {
	runMapping(new MapVoidAction("dispose") {
		public void map() {
		    ((JInternalFrame)getSource()).dispose();
		}});}

    /**Maps <code>JInternalFrame.getContentPane()</code> through queue*/
    public Container getContentPane() {
	return((Container)runMapping(new MapAction("getContentPane") {
		public Object map() {
		    return(((JInternalFrame)getSource()).getContentPane());
		}}));}

    /**Maps <code>JInternalFrame.getDefaultCloseOperation()</code> through queue*/
    public int getDefaultCloseOperation() {
	return(runMapping(new MapIntegerAction("getDefaultCloseOperation") {
		public int map() {
		    return(((JInternalFrame)getSource()).getDefaultCloseOperation());
		}}));}

    /**Maps <code>JInternalFrame.getDesktopIcon()</code> through queue*/
    public JDesktopIcon getDesktopIcon() {
	return((JDesktopIcon)runMapping(new MapAction("getDesktopIcon") {
		public Object map() {
		    return(((JInternalFrame)getSource()).getDesktopIcon());
		}}));}

    /**Maps <code>JInternalFrame.getDesktopPane()</code> through queue*/
    public JDesktopPane getDesktopPane() {
	return((JDesktopPane)runMapping(new MapAction("getDesktopPane") {
		public Object map() {
		    return(((JInternalFrame)getSource()).getDesktopPane());
		}}));}

    /**Maps <code>JInternalFrame.getFrameIcon()</code> through queue*/
    public Icon getFrameIcon() {
	return((Icon)runMapping(new MapAction("getFrameIcon") {
		public Object map() {
		    return(((JInternalFrame)getSource()).getFrameIcon());
		}}));}

    /**Maps <code>JInternalFrame.getGlassPane()</code> through queue*/
    public Component getGlassPane() {
	return((Component)runMapping(new MapAction("getGlassPane") {
		public Object map() {
		    return(((JInternalFrame)getSource()).getGlassPane());
		}}));}

    /**Maps <code>JInternalFrame.getJMenuBar()</code> through queue*/
    public JMenuBar getJMenuBar() {
	return((JMenuBar)runMapping(new MapAction("getJMenuBar") {
		public Object map() {
		    return(((JInternalFrame)getSource()).getJMenuBar());
		}}));}

    /**Maps <code>JInternalFrame.getLayer()</code> through queue*/
    public int getLayer() {
	return(runMapping(new MapIntegerAction("getLayer") {
		public int map() {
		    return(((JInternalFrame)getSource()).getLayer());
		}}));}

    /**Maps <code>JInternalFrame.getLayeredPane()</code> through queue*/
    public JLayeredPane getLayeredPane() {
	return((JLayeredPane)runMapping(new MapAction("getLayeredPane") {
		public Object map() {
		    return(((JInternalFrame)getSource()).getLayeredPane());
		}}));}

    /**Maps <code>JInternalFrame.getTitle()</code> through queue*/
    public String getTitle() {
	return((String)runMapping(new MapAction("getTitle") {
		public Object map() {
		    return(((JInternalFrame)getSource()).getTitle());
		}}));}

    /**Maps <code>JInternalFrame.getUI()</code> through queue*/
    public InternalFrameUI getUI() {
	return((InternalFrameUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JInternalFrame)getSource()).getUI());
		}}));}

    /**Maps <code>JInternalFrame.getWarningString()</code> through queue*/
    public String getWarningString() {
	return((String)runMapping(new MapAction("getWarningString") {
		public Object map() {
		    return(((JInternalFrame)getSource()).getWarningString());
		}}));}

    /**Maps <code>JInternalFrame.isClosable()</code> through queue*/
    public boolean isClosable() {
	return(runMapping(new MapBooleanAction("isClosable") {
		public boolean map() {
		    return(((JInternalFrame)getSource()).isClosable());
		}}));}

    /**Maps <code>JInternalFrame.isClosed()</code> through queue*/
    public boolean isClosed() {
	return(runMapping(new MapBooleanAction("isClosed") {
		public boolean map() {
		    return(((JInternalFrame)getSource()).isClosed());
		}}));}

    /**Maps <code>JInternalFrame.isIcon()</code> through queue*/
    public boolean isIcon() {
	return(runMapping(new MapBooleanAction("isIcon") {
		public boolean map() {
		    return(((JInternalFrame)getSource()).isIcon());
		}}));}

    /**Maps <code>JInternalFrame.isIconifiable()</code> through queue*/
    public boolean isIconifiable() {
	return(runMapping(new MapBooleanAction("isIconifiable") {
		public boolean map() {
		    return(((JInternalFrame)getSource()).isIconifiable());
		}}));}

    /**Maps <code>JInternalFrame.isMaximizable()</code> through queue*/
    public boolean isMaximizable() {
	return(runMapping(new MapBooleanAction("isMaximizable") {
		public boolean map() {
		    return(((JInternalFrame)getSource()).isMaximizable());
		}}));}

    /**Maps <code>JInternalFrame.isMaximum()</code> through queue*/
    public boolean isMaximum() {
	return(runMapping(new MapBooleanAction("isMaximum") {
		public boolean map() {
		    return(((JInternalFrame)getSource()).isMaximum());
		}}));}

    /**Maps <code>JInternalFrame.isResizable()</code> through queue*/
    public boolean isResizable() {
	return(runMapping(new MapBooleanAction("isResizable") {
		public boolean map() {
		    return(((JInternalFrame)getSource()).isResizable());
		}}));}

    /**Maps <code>JInternalFrame.isSelected()</code> through queue*/
    public boolean isSelected() {
	return(runMapping(new MapBooleanAction("isSelected") {
		public boolean map() {
		    return(((JInternalFrame)getSource()).isSelected());
		}}));}

    /**Maps <code>JInternalFrame.moveToBack()</code> through queue*/
    public void moveToBack() {
	runMapping(new MapVoidAction("moveToBack") {
		public void map() {
		    ((JInternalFrame)getSource()).moveToBack();
		}});}

    /**Maps <code>JInternalFrame.moveToFront()</code> through queue*/
    public void moveToFront() {
	runMapping(new MapVoidAction("moveToFront") {
		public void map() {
		    ((JInternalFrame)getSource()).moveToFront();
		}});}

    /**Maps <code>JInternalFrame.pack()</code> through queue*/
    public void pack() {
	runMapping(new MapVoidAction("pack") {
		public void map() {
		    ((JInternalFrame)getSource()).pack();
		}});}

    /**Maps <code>JInternalFrame.removeInternalFrameListener(InternalFrameListener)</code> through queue*/
    public void removeInternalFrameListener(final InternalFrameListener internalFrameListener) {
	runMapping(new MapVoidAction("removeInternalFrameListener") {
		public void map() {
		    ((JInternalFrame)getSource()).removeInternalFrameListener(internalFrameListener);
		}});}

    /**Maps <code>JInternalFrame.setClosable(boolean)</code> through queue*/
    public void setClosable(final boolean b) {
	runMapping(new MapVoidAction("setClosable") {
		public void map() {
		    ((JInternalFrame)getSource()).setClosable(b);
		}});}

    /**Maps <code>JInternalFrame.setClosed(boolean)</code> through queue*/
    public void setClosed(final boolean b) {
	runMapping(new MapVoidAction("setClosed") {
		public void map() throws PropertyVetoException {
		    ((JInternalFrame)getSource()).setClosed(b);
		}});}

    /**Maps <code>JInternalFrame.setContentPane(Container)</code> through queue*/
    public void setContentPane(final Container container) {
	runMapping(new MapVoidAction("setContentPane") {
		public void map() {
		    ((JInternalFrame)getSource()).setContentPane(container);
		}});}

    /**Maps <code>JInternalFrame.setDefaultCloseOperation(int)</code> through queue*/
    public void setDefaultCloseOperation(final int i) {
	runMapping(new MapVoidAction("setDefaultCloseOperation") {
		public void map() {
		    ((JInternalFrame)getSource()).setDefaultCloseOperation(i);
		}});}

    /**Maps <code>JInternalFrame.setDesktopIcon(JDesktopIcon)</code> through queue*/
    public void setDesktopIcon(final JDesktopIcon jDesktopIcon) {
	runMapping(new MapVoidAction("setDesktopIcon") {
		public void map() {
		    ((JInternalFrame)getSource()).setDesktopIcon(jDesktopIcon);
		}});}

    /**Maps <code>JInternalFrame.setFrameIcon(Icon)</code> through queue*/
    public void setFrameIcon(final Icon icon) {
	runMapping(new MapVoidAction("setFrameIcon") {
		public void map() {
		    ((JInternalFrame)getSource()).setFrameIcon(icon);
		}});}

    /**Maps <code>JInternalFrame.setGlassPane(Component)</code> through queue*/
    public void setGlassPane(final Component component) {
	runMapping(new MapVoidAction("setGlassPane") {
		public void map() {
		    ((JInternalFrame)getSource()).setGlassPane(component);
		}});}

    /**Maps <code>JInternalFrame.setIcon(boolean)</code> through queue*/
    public void setIcon(final boolean b) {
	runMapping(new MapVoidAction("setIcon") {
		public void map() throws PropertyVetoException {
		    ((JInternalFrame)getSource()).setIcon(b);
		}});}

    /**Maps <code>JInternalFrame.setIconifiable(boolean)</code> through queue*/
    public void setIconifiable(final boolean b) {
	runMapping(new MapVoidAction("setIconifiable") {
		public void map() {
		    ((JInternalFrame)getSource()).setIconifiable(b);
		}});}

    /**Maps <code>JInternalFrame.setJMenuBar(JMenuBar)</code> through queue*/
    public void setJMenuBar(final JMenuBar jMenuBar) {
	runMapping(new MapVoidAction("setJMenuBar") {
		public void map() {
		    ((JInternalFrame)getSource()).setJMenuBar(jMenuBar);
		}});}

    /**Maps <code>JInternalFrame.setLayer(Integer)</code> through queue*/
    public void setLayer(final Integer integer) {
	runMapping(new MapVoidAction("setLayer") {
		public void map() {
		    ((JInternalFrame)getSource()).setLayer(integer);
		}});}

    /**Maps <code>JInternalFrame.setLayeredPane(JLayeredPane)</code> through queue*/
    public void setLayeredPane(final JLayeredPane jLayeredPane) {
	runMapping(new MapVoidAction("setLayeredPane") {
		public void map() {
		    ((JInternalFrame)getSource()).setLayeredPane(jLayeredPane);
		}});}

    /**Maps <code>JInternalFrame.setMaximizable(boolean)</code> through queue*/
    public void setMaximizable(final boolean b) {
	runMapping(new MapVoidAction("setMaximizable") {
		public void map() {
		    ((JInternalFrame)getSource()).setMaximizable(b);
		}});}

    /**Maps <code>JInternalFrame.setMaximum(boolean)</code> through queue*/
    public void setMaximum(final boolean b) {
	runMapping(new MapVoidAction("setMaximum") {
		public void map() throws PropertyVetoException {
		    ((JInternalFrame)getSource()).setMaximum(b);
		}});}

    /**Maps <code>JInternalFrame.setResizable(boolean)</code> through queue*/
    public void setResizable(final boolean b) {
	runMapping(new MapVoidAction("setResizable") {
		public void map() {
		    ((JInternalFrame)getSource()).setResizable(b);
		}});}

    /**Maps <code>JInternalFrame.setSelected(boolean)</code> through queue*/
    public void setSelected(final boolean b) {
	runMapping(new MapVoidAction("setSelected") {
		public void map() throws PropertyVetoException {
		    ((JInternalFrame)getSource()).setSelected(b);
		}});}

    /**Maps <code>JInternalFrame.setTitle(String)</code> through queue*/
    public void setTitle(final String string) {
	runMapping(new MapVoidAction("setTitle") {
		public void map() {
		    ((JInternalFrame)getSource()).setTitle(string);
		}});}

    /**Maps <code>JInternalFrame.setUI(InternalFrameUI)</code> through queue*/
    public void setUI(final InternalFrameUI internalFrameUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JInternalFrame)getSource()).setUI(internalFrameUI);
		}});}

    /**Maps <code>JInternalFrame.toBack()</code> through queue*/
    public void toBack() {
	runMapping(new MapVoidAction("toBack") {
		public void map() {
		    ((JInternalFrame)getSource()).toBack();
		}});}

    /**Maps <code>JInternalFrame.toFront()</code> through queue*/
    public void toFront() {
	runMapping(new MapVoidAction("toFront") {
		public void map() {
		    ((JInternalFrame)getSource()).toFront();
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Uses InternalframeDriver to get a title pane.
     * @return a title pane.
     */
    protected Container findTitlePane() {
        return((Container)iDriver.getTitlePane(this));
    }

    /**
     * Initiaites suboperators.
     */
    protected void initOperators() {
	iconOperator = new JDesktopIconOperator(((JInternalFrame)getSource()).getDesktopIcon());
	iconOperator.copyEnvironment(this);
	Container titlePane = findTitlePane();
	if(!isIcon() && titlePane != null) {
	    if(titleOperator == null) {
		titleOperator = new ContainerOperator(titlePane);
		int bttCount = 0;
		if(getContainer(new ComponentChooser() {
			public boolean checkComponent(Component comp) {
			    return(comp instanceof JDesktopPane);
			}
			public String getDescription() {
			    return("Desctop pane");
			}
		    }) != null) {
		    minOper = new JButtonOperator(titleOperator, bttCount);
		    bttCount++;
		    if(((JInternalFrame)getSource()).isMaximizable()) {
			maxOper = new JButtonOperator(titleOperator, bttCount);
			bttCount++;
		    } else {
			maxOper = null;
		    }
		} else {
		    minOper = null;
		    maxOper = null;
		}
		if(isClosable()) {
		    closeOper = new JButtonOperator(titleOperator, bttCount);
		} else {
		    closeOper = null;
		}
	    }
	} else {
	    titleOperator = null;
	    minOper = null;
	    maxOper = null;
	    closeOper = null;
	}
    }

    //throw exception if state is wrong
    private void checkIconified(boolean shouldBeIconified) {
	if( shouldBeIconified && !isIcon() ||
	   !shouldBeIconified &&  isIcon()) {
	    throw(new WrongInternalFrameStateException("JInternal frame should " +
						       (shouldBeIconified ? "" : "not") +
						       " be iconified to produce this operation",
						       getSource()));
	}
    }

    private static JInternalFrame findOne(ContainerOperator cont, String text, int index) {
	Component source = waitComponent(cont, 
					 new JInternalFrameByTitleFinder(text, 
									 cont.getComparator()),
					 index);
	if(source instanceof JInternalFrame) {
	    return((JInternalFrame)source);
	} else if(source instanceof JInternalFrame.JDesktopIcon) {
	    return(((JInternalFrame.JDesktopIcon)source).getInternalFrame());
	} else {
	    throw(new TimeoutExpiredException("No internal frame was found"));
	}
    }

    /**
     * Exception can be throwht if as a result of an attempt to produce
     * operation for the frame in incorrect state.
     * Like activate iconified frame, for example.
     */
    public class WrongInternalFrameStateException extends JemmyInputException {
        /**
         * Constructs a JInternalFrameOperator$WrongInternalFrameStateException object.
         * @param message  an exception message.
         * @param comp an internal frame.
         */
	public WrongInternalFrameStateException(String message, Component comp) {
	    super(message, comp);
	}
    }

    /**
     * Allows to find component by title.
     */
    public static class JInternalFrameByTitleFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
        /**
         * Constructs JInternalFrameByTitleFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public JInternalFrameByTitleFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
        /**
         * Constructs JInternalFrameByTitleFinder.
         * @param lb a text pattern
         */
	public JInternalFrameByTitleFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JInternalFrame || comp instanceof JInternalFrame.JDesktopIcon) {
		JInternalFrame frame = null;
		if(comp instanceof JInternalFrame) {
		    frame = (JInternalFrame)comp;
		} else {
		    JDesktopIconOperator io = new JDesktopIconOperator((JInternalFrame.JDesktopIcon)comp);
		    frame = io.getInternalFrame();
		}
		if(frame.getTitle() != null) {
		    return(comparator.equals(frame.getTitle(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("JInternalFrame with title \"" + label + "\"");
	}
    }

    /**
     * Class to operate with javax.swing.JInternalFrame.JDesktopIconOperator component.
     */
    public static class JDesktopIconOperator extends JComponentOperator 
	implements Outputable, Timeoutable {

	private TestOut output;
	private Timeouts timeouts;
	
        /**
         * Constructs JDesktopIconOperator.
         * @param b a component
         */
	public JDesktopIconOperator(JInternalFrame.JDesktopIcon b) {
	    super(b);
	    setOutput(JemmyProperties.getCurrentOutput());
	    setTimeouts(JemmyProperties.getCurrentTimeouts());
	}
	
	public void setOutput(TestOut out) {
	    output = out;
	    super.setOutput(output.createErrorOutput());
	}

	public TestOut getOutput() {
	    return(output);
	}
	
	public void setTimeouts(Timeouts times) {
	    timeouts = times;
	    super.setTimeouts(timeouts);
	}

	public Timeouts getTimeouts() {
	    return(timeouts);
	}

        /**
         * Creates an operator for the correspondent intenal frame.
         * @return an operator.
         */
	public JInternalFrame getInternalFrame() {
	    return((JInternalFrame)getEventDispatcher().
		invokeExistingMethod("getInternalFrame", 
				     null, 
				     null, 
				     output));
	}

        /**
         * Pushs the deiconifying button.
         */
	public void pushButton() {
	    new JButtonOperator(this).push();
	}
    }

    /**
     * Checks component type.
     */
    public static class JInternalFrameFinder implements ComponentChooser {
        ComponentChooser sf = null;
        /**
         * Constructs JInternalFrameFinder.
         * @param sf other searching criteria.
         */
	public JInternalFrameFinder(ComponentChooser sf) {
            this.sf = sf;
	}
        /**
         * Constructs JInternalFrameFinder.
         */
	public JInternalFrameFinder() {
            this(ComponentSearcher.getTrueChooser("JInternalFrame or JInternalFrame.JDesktopIcon"));
	}
        public boolean checkComponent(Component comp) {
            return((comp instanceof JInternalFrame || comp instanceof JInternalFrame.JDesktopIcon) &&
                   sf.checkComponent(comp));
        }
        public String getDescription() {
            return(sf.getDescription());
        }
    }

}
