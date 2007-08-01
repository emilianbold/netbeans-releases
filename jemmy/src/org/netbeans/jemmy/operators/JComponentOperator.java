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
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.beans.VetoableChangeListener;

import java.util.Hashtable;

import javax.accessibility.AccessibleContext;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JRootPane;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;

import javax.swing.border.Border;

import javax.swing.event.AncestorListener;

/**
 * <BR><BR>Timeouts used: <BR>
 * JComponentOperator.WaitToolTipTimeout - time to wait tool tip displayed <BR>
 * JComponentOperator.ShowToolTipTimeout - time to show tool tip <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JComponentOperator extends ContainerOperator
    implements Timeoutable, Outputable{

    /**
     * Identifier for a "tooltip text" property.
     * @see #getDump
     */
    public static final String TOOLTIP_TEXT_DPROP = "Tooltip text";
    public static final String A11Y_DATA = "Accessible data (yes/no)";
    public static final String A11Y_NAME_DPROP = "Accessible name";
    public static final String A11Y_DESCRIPTION_DPROP = "Accessible decription";

    private final static long WAIT_TOOL_TIP_TIMEOUT = 10000;
    private final static long SHOW_TOOL_TIP_TIMEOUT = 0;

    private Timeouts timeouts;
    private TestOut output;

    /**
     * Constructor.
     * @param b a component
     */
    public JComponentOperator(JComponent b) {
	super(b);
    }

    /**
     * Constructs a JComponentOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JComponentOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JComponent)cont.
             waitSubComponent(new JComponentFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JComponentOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JComponentOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JComponentOperator(ContainerOperator cont, int index) {
	this((JComponent)waitComponent(cont, 
				       new JComponentFinder(ComponentSearcher.getTrueChooser("Any JComponent")),
				       index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @throws TimeoutExpiredException
     */
    public JComponentOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JComponent in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JComponent instance or null if component was not found.
     */
    public static JComponent findJComponent(Container cont, ComponentChooser chooser, int index) {
	return((JComponent)findComponent(cont, new JComponentFinder(chooser), index));
    }

    /**
     * Searches 0'th JComponent in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JComponent instance or null if component was not found.
     */
    public static JComponent findJComponent(Container cont, ComponentChooser chooser) {
	return(findJComponent(cont, chooser, 0));
    }

    /**
     * Searches JComponent by tooltip text.
     * @param cont Container to search component in.
     * @param toolTipText Tooltip text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JComponent instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JComponent findJComponent(Container cont, String toolTipText, boolean ce, boolean ccs, int index) {
	return(findJComponent(cont, new JComponentByTipFinder(toolTipText, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Searches JComponent by tooltip text.
     * @param cont Container to search component in.
     * @param toolTipText Tooltip text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JComponent instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JComponent findJComponent(Container cont, String toolTipText, boolean ce, boolean ccs) {
	return(findJComponent(cont, toolTipText, ce, ccs, 0));
    }

    /**
     * Waits JComponent in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JComponent instance or null if component was not found.
     * @throws TimeoutExpiredException
     */
    public static JComponent waitJComponent(Container cont, ComponentChooser chooser, final int index) {
	return((JComponent)waitComponent(cont, new JComponentFinder(chooser), index));
    }

    /**
     * Waits 0'th JComponent in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JComponent instance or null if component was not found.
     * @throws TimeoutExpiredException
     */
    public static JComponent waitJComponent(Container cont, ComponentChooser chooser) {
	return(waitJComponent(cont, chooser, 0));
    }

    /**
     * Waits JComponent by tooltip text.
     * @param cont Container to search component in.
     * @param toolTipText Tooltip text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JComponent instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JComponent waitJComponent(Container cont, String toolTipText, boolean ce, boolean ccs, int index) {
	return(waitJComponent(cont, new JComponentByTipFinder(toolTipText, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Waits JComponent by tooltip text.
     * @param cont Container to search component in.
     * @param toolTipText Tooltip text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JComponent instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JComponent waitJComponent(Container cont, String toolTipText, boolean ce, boolean ccs) {
	return(waitJComponent(cont, toolTipText, ce, ccs, 0));
    }

    static {
	Timeouts.initDefault("JComponentOperator.WaitToolTipTimeout", WAIT_TOOL_TIP_TIMEOUT);
	Timeouts.initDefault("JComponentOperator.ShowToolTipTimeout", SHOW_TOOL_TIP_TIMEOUT);
    }

    public void setTimeouts(Timeouts timeouts) {
	super.setTimeouts(timeouts);
	this.timeouts = timeouts;
    }

    public Timeouts getTimeouts() {
	return(timeouts);
    }

    public void setOutput(TestOut out) {
	output = out;
	super.setOutput(output.createErrorOutput());
    }

    public TestOut getOutput() {
	return(output);
    }

    public int getCenterXForClick() {
	Rectangle rect = getVisibleRect();
	return((int)rect.getX() +
	       (int)rect.getWidth() / 2);
    }

    public int getCenterYForClick() {
	Rectangle rect = getVisibleRect();
	return((int)rect.getY() +
	       (int)rect.getHeight() / 2);
    }

    /**
     * Showes tool tip.
     * @return JToolTip component.
     * @throws TimeoutExpiredException
     */
    public JToolTip showToolTip() {
	enterMouse();
	moveMouse(getCenterXForClick(),
		  getCenterYForClick());
        return(waitToolTip());
    }

    public JToolTip waitToolTip() {
        return((JToolTip)waitComponent(WindowOperator.
                                       waitWindow(new JToolTipWindowFinder(),
                                                  0,
                                                  getTimeouts(),
                                                  getOutput()),
                                       new JToolTipFinder(),
                                       0,
                                       getTimeouts(),
                                       getOutput()));
    }

    /**
     * Looks for a first window-like container.
     * @return either WindowOperator of JInternalFrameOperator
     */
    public ContainerOperator getWindowContainerOperator() {
        Component resultComp;
        if(getSource() instanceof Window) {
            resultComp = getSource();
        } else {
            resultComp = getContainer(new ComponentChooser() {
                    public boolean checkComponent(Component comp) {
                        return(comp instanceof Window ||
                               comp instanceof JInternalFrame);
                    }
                    public String getDescription() {
                        return("");
                    }
                });
        }
        ContainerOperator result;
        if(resultComp instanceof Window) {
            result = new WindowOperator((Window)resultComp);
        } else {
            result = new ContainerOperator((Container)resultComp);
        }
        result.copyEnvironment(this);
        return(result);
    }

    public Hashtable getDump() {
	Hashtable result = super.getDump();
	if(getToolTipText() != null) {
	    result.put(TOOLTIP_TEXT_DPROP, getToolTipText());
	}
        //System.out.println("Dump a11y = " + System.getProperty("jemmy.dump.a11y"));
        if(System.getProperty("jemmy.dump.a11y") != null &&
           System.getProperty("jemmy.dump.a11y").equals("on")) {
            AccessibleContext a11y = ((JComponent)getSource()).getAccessibleContext();
            if(a11y != null) {
                result.put(A11Y_DATA, "yes");
                String accName = (a11y.getAccessibleName()        == null) ? "null" : a11y.getAccessibleName();
                String accDesc = (a11y.getAccessibleDescription() == null) ? "null" : a11y.getAccessibleDescription();
                result.put(A11Y_NAME_DPROP, accName);
                result.put(A11Y_DESCRIPTION_DPROP, accDesc);
            } else {
                result.put(A11Y_DATA, "no");
            }
        }
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JComponent.addAncestorListener(AncestorListener)</code> through queue*/
    public void addAncestorListener(final AncestorListener ancestorListener) {
	runMapping(new MapVoidAction("addAncestorListener") {
		public void map() {
		    ((JComponent)getSource()).addAncestorListener(ancestorListener);
		}});}

    /**Maps <code>JComponent.addVetoableChangeListener(VetoableChangeListener)</code> through queue*/
    public void addVetoableChangeListener(final VetoableChangeListener vetoableChangeListener) {
	runMapping(new MapVoidAction("addVetoableChangeListener") {
		public void map() {
		    ((JComponent)getSource()).addVetoableChangeListener(vetoableChangeListener);
		}});}

    /**Maps <code>JComponent.computeVisibleRect(Rectangle)</code> through queue*/
    public void computeVisibleRect(final Rectangle rectangle) {
	runMapping(new MapVoidAction("computeVisibleRect") {
		public void map() {
		    ((JComponent)getSource()).computeVisibleRect(rectangle);
		}});}

    /**Maps <code>JComponent.createToolTip()</code> through queue*/
    public JToolTip createToolTip() {
	return((JToolTip)runMapping(new MapAction("createToolTip") {
		public Object map() {
		    return(((JComponent)getSource()).createToolTip());
		}}));}

    /**Maps <code>JComponent.firePropertyChange(String, byte, byte)</code> through queue*/
    public void firePropertyChange(final String string, final byte b, final byte b1) {
	runMapping(new MapVoidAction("firePropertyChange") {
		public void map() {
		    ((JComponent)getSource()).firePropertyChange(string, b, b1);
		}});}

    /**Maps <code>JComponent.firePropertyChange(String, char, char)</code> through queue*/
    public void firePropertyChange(final String string, final char c, final char c1) {
	runMapping(new MapVoidAction("firePropertyChange") {
		public void map() {
		    ((JComponent)getSource()).firePropertyChange(string, c, c1);
		}});}

    /**Maps <code>JComponent.firePropertyChange(String, double, double)</code> through queue*/
    public void firePropertyChange(final String string, final double d, final double d1) {
	runMapping(new MapVoidAction("firePropertyChange") {
		public void map() {
		    ((JComponent)getSource()).firePropertyChange(string, d, d1);
		}});}

    /**Maps <code>JComponent.firePropertyChange(String, float, float)</code> through queue*/
    public void firePropertyChange(final String string, final float f, final float f1) {
	runMapping(new MapVoidAction("firePropertyChange") {
		public void map() {
		    ((JComponent)getSource()).firePropertyChange(string, f, f1);
		}});}

    /**Maps <code>JComponent.firePropertyChange(String, int, int)</code> through queue*/
    public void firePropertyChange(final String string, final int i, final int i1) {
	runMapping(new MapVoidAction("firePropertyChange") {
		public void map() {
		    ((JComponent)getSource()).firePropertyChange(string, i, i1);
		}});}

    /**Maps <code>JComponent.firePropertyChange(String, long, long)</code> through queue*/
    public void firePropertyChange(final String string, final long l, final long l1) {
	runMapping(new MapVoidAction("firePropertyChange") {
		public void map() {
		    ((JComponent)getSource()).firePropertyChange(string, l, l1);
		}});}

    /**Maps <code>JComponent.firePropertyChange(String, short, short)</code> through queue*/
    public void firePropertyChange(final String string, final short s, final short s1) {
	runMapping(new MapVoidAction("firePropertyChange") {
		public void map() {
		    ((JComponent)getSource()).firePropertyChange(string, s, s1);
		}});}

    /**Maps <code>JComponent.firePropertyChange(String, boolean, boolean)</code> through queue*/
    public void firePropertyChange(final String string, final boolean b, final boolean b1) {
	runMapping(new MapVoidAction("firePropertyChange") {
		public void map() {
		    ((JComponent)getSource()).firePropertyChange(string, b, b1);
		}});}

    /**Maps <code>JComponent.getAccessibleContext()</code> through queue*/
    public AccessibleContext getAccessibleContext() {
	return((AccessibleContext)runMapping(new MapAction("getAccessibleContext") {
		public Object map() {
		    return(((JComponent)getSource()).getAccessibleContext());
		}}));}

    /**Maps <code>JComponent.getActionForKeyStroke(KeyStroke)</code> through queue*/
    public ActionListener getActionForKeyStroke(final KeyStroke keyStroke) {
	return((ActionListener)runMapping(new MapAction("getActionForKeyStroke") {
		public Object map() {
		    return(((JComponent)getSource()).getActionForKeyStroke(keyStroke));
		}}));}

    /**Maps <code>JComponent.getAutoscrolls()</code> through queue*/
    public boolean getAutoscrolls() {
	return(runMapping(new MapBooleanAction("getAutoscrolls") {
		public boolean map() {
		    return(((JComponent)getSource()).getAutoscrolls());
		}}));}

    /**Maps <code>JComponent.getBorder()</code> through queue*/
    public Border getBorder() {
	return((Border)runMapping(new MapAction("getBorder") {
		public Object map() {
		    return(((JComponent)getSource()).getBorder());
		}}));}

    /**Maps <code>JComponent.getClientProperty(Object)</code> through queue*/
    public Object getClientProperty(final Object object) {
	return((Object)runMapping(new MapAction("getClientProperty") {
		public Object map() {
		    return(((JComponent)getSource()).getClientProperty(object));
		}}));}

    /**Maps <code>JComponent.getConditionForKeyStroke(KeyStroke)</code> through queue*/
    public int getConditionForKeyStroke(final KeyStroke keyStroke) {
	return(runMapping(new MapIntegerAction("getConditionForKeyStroke") {
		public int map() {
		    return(((JComponent)getSource()).getConditionForKeyStroke(keyStroke));
		}}));}

    /**Maps <code>JComponent.getDebugGraphicsOptions()</code> through queue*/
    public int getDebugGraphicsOptions() {
	return(runMapping(new MapIntegerAction("getDebugGraphicsOptions") {
		public int map() {
		    return(((JComponent)getSource()).getDebugGraphicsOptions());
		}}));}

    /**Maps <code>JComponent.getInsets(Insets)</code> through queue*/
    public Insets getInsets(final Insets insets) {
	return((Insets)runMapping(new MapAction("getInsets") {
		public Object map() {
		    return(((JComponent)getSource()).getInsets(insets));
		}}));}

    /**Maps <code>JComponent.getNextFocusableComponent()</code> through queue*/
    public Component getNextFocusableComponent() {
	return((Component)runMapping(new MapAction("getNextFocusableComponent") {
		public Object map() {
		    return(((JComponent)getSource()).getNextFocusableComponent());
		}}));}

    /**Maps <code>JComponent.getRegisteredKeyStrokes()</code> through queue*/
    public KeyStroke[] getRegisteredKeyStrokes() {
	return((KeyStroke[])runMapping(new MapAction("getRegisteredKeyStrokes") {
		public Object map() {
		    return(((JComponent)getSource()).getRegisteredKeyStrokes());
		}}));}

    /**Maps <code>JComponent.getRootPane()</code> through queue*/
    public JRootPane getRootPane() {
	return((JRootPane)runMapping(new MapAction("getRootPane") {
		public Object map() {
		    return(((JComponent)getSource()).getRootPane());
		}}));}

    /**Maps <code>JComponent.getToolTipLocation(MouseEvent)</code> through queue*/
    public Point getToolTipLocation(final MouseEvent mouseEvent) {
	return((Point)runMapping(new MapAction("getToolTipLocation") {
		public Object map() {
		    return(((JComponent)getSource()).getToolTipLocation(mouseEvent));
		}}));}

    /**Maps <code>JComponent.getToolTipText()</code> through queue*/
    public String getToolTipText() {
	return((String)runMapping(new MapAction("getToolTipText") {
		public Object map() {
		    return(((JComponent)getSource()).getToolTipText());
		}}));}

    /**Maps <code>JComponent.getToolTipText(MouseEvent)</code> through queue*/
    public String getToolTipText(final MouseEvent mouseEvent) {
	return((String)runMapping(new MapAction("getToolTipText") {
		public Object map() {
		    return(((JComponent)getSource()).getToolTipText(mouseEvent));
		}}));}

    /**Maps <code>JComponent.getTopLevelAncestor()</code> through queue*/
    public Container getTopLevelAncestor() {
	return((Container)runMapping(new MapAction("getTopLevelAncestor") {
		public Object map() {
		    return(((JComponent)getSource()).getTopLevelAncestor());
		}}));}

    /**Maps <code>JComponent.getUIClassID()</code> through queue*/
    public String getUIClassID() {
	return((String)runMapping(new MapAction("getUIClassID") {
		public Object map() {
		    return(((JComponent)getSource()).getUIClassID());
		}}));}

    /**Maps <code>JComponent.getVisibleRect()</code> through queue*/
    public Rectangle getVisibleRect() {
	return((Rectangle)runMapping(new MapAction("getVisibleRect") {
		public Object map() {
		    return(((JComponent)getSource()).getVisibleRect());
		}}));}

    /**Maps <code>JComponent.grabFocus()</code> through queue*/
    public void grabFocus() {
	runMapping(new MapVoidAction("grabFocus") {
		public void map() {
		    ((JComponent)getSource()).grabFocus();
		}});}

    /**Maps <code>JComponent.isFocusCycleRoot()</code> through queue*/
    public boolean isFocusCycleRoot() {
	return(runMapping(new MapBooleanAction("isFocusCycleRoot") {
		public boolean map() {
		    return(((JComponent)getSource()).isFocusCycleRoot());
		}}));}

    /**Maps <code>JComponent.isManagingFocus()</code> through queue*/
    public boolean isManagingFocus() {
	return(runMapping(new MapBooleanAction("isManagingFocus") {
		public boolean map() {
		    return(((JComponent)getSource()).isManagingFocus());
		}}));}

    /**Maps <code>JComponent.isOptimizedDrawingEnabled()</code> through queue*/
    public boolean isOptimizedDrawingEnabled() {
	return(runMapping(new MapBooleanAction("isOptimizedDrawingEnabled") {
		public boolean map() {
		    return(((JComponent)getSource()).isOptimizedDrawingEnabled());
		}}));}

    /**Maps <code>JComponent.isPaintingTile()</code> through queue*/
    public boolean isPaintingTile() {
	return(runMapping(new MapBooleanAction("isPaintingTile") {
		public boolean map() {
		    return(((JComponent)getSource()).isPaintingTile());
		}}));}

    /**Maps <code>JComponent.isRequestFocusEnabled()</code> through queue*/
    public boolean isRequestFocusEnabled() {
	return(runMapping(new MapBooleanAction("isRequestFocusEnabled") {
		public boolean map() {
		    return(((JComponent)getSource()).isRequestFocusEnabled());
		}}));}

    /**Maps <code>JComponent.isValidateRoot()</code> through queue*/
    public boolean isValidateRoot() {
	return(runMapping(new MapBooleanAction("isValidateRoot") {
		public boolean map() {
		    return(((JComponent)getSource()).isValidateRoot());
		}}));}

    /**Maps <code>JComponent.paintImmediately(int, int, int, int)</code> through queue*/
    public void paintImmediately(final int i, final int i1, final int i2, final int i3) {
	runMapping(new MapVoidAction("paintImmediately") {
		public void map() {
		    ((JComponent)getSource()).paintImmediately(i, i1, i2, i3);
		}});}

    /**Maps <code>JComponent.paintImmediately(Rectangle)</code> through queue*/
    public void paintImmediately(final Rectangle rectangle) {
	runMapping(new MapVoidAction("paintImmediately") {
		public void map() {
		    ((JComponent)getSource()).paintImmediately(rectangle);
		}});}

    /**Maps <code>JComponent.putClientProperty(Object, Object)</code> through queue*/
    public void putClientProperty(final Object object, final Object object1) {
	runMapping(new MapVoidAction("putClientProperty") {
		public void map() {
		    ((JComponent)getSource()).putClientProperty(object, object1);
		}});}

    /**Maps <code>JComponent.registerKeyboardAction(ActionListener, String, KeyStroke, int)</code> through queue*/
    public void registerKeyboardAction(final ActionListener actionListener, final String string, final KeyStroke keyStroke, final int i) {
	runMapping(new MapVoidAction("registerKeyboardAction") {
		public void map() {
		    ((JComponent)getSource()).registerKeyboardAction(actionListener, string, keyStroke, i);
		}});}

    /**Maps <code>JComponent.registerKeyboardAction(ActionListener, KeyStroke, int)</code> through queue*/
    public void registerKeyboardAction(final ActionListener actionListener, final KeyStroke keyStroke, final int i) {
	runMapping(new MapVoidAction("registerKeyboardAction") {
		public void map() {
		    ((JComponent)getSource()).registerKeyboardAction(actionListener, keyStroke, i);
		}});}

    /**Maps <code>JComponent.removeAncestorListener(AncestorListener)</code> through queue*/
    public void removeAncestorListener(final AncestorListener ancestorListener) {
	runMapping(new MapVoidAction("removeAncestorListener") {
		public void map() {
		    ((JComponent)getSource()).removeAncestorListener(ancestorListener);
		}});}

    /**Maps <code>JComponent.removeVetoableChangeListener(VetoableChangeListener)</code> through queue*/
    public void removeVetoableChangeListener(final VetoableChangeListener vetoableChangeListener) {
	runMapping(new MapVoidAction("removeVetoableChangeListener") {
		public void map() {
		    ((JComponent)getSource()).removeVetoableChangeListener(vetoableChangeListener);
		}});}

    /**Maps <code>JComponent.repaint(Rectangle)</code> through queue*/
    public void repaint(final Rectangle rectangle) {
	runMapping(new MapVoidAction("repaint") {
		public void map() {
		    ((JComponent)getSource()).repaint(rectangle);
		}});}

    /**Maps <code>JComponent.requestDefaultFocus()</code> through queue*/
    public boolean requestDefaultFocus() {
	return(runMapping(new MapBooleanAction("requestDefaultFocus") {
		public boolean map() {
		    return(((JComponent)getSource()).requestDefaultFocus());
		}}));}

    /**Maps <code>JComponent.resetKeyboardActions()</code> through queue*/
    public void resetKeyboardActions() {
	runMapping(new MapVoidAction("resetKeyboardActions") {
		public void map() {
		    ((JComponent)getSource()).resetKeyboardActions();
		}});}

    /**Maps <code>JComponent.revalidate()</code> through queue*/
    public void revalidate() {
	runMapping(new MapVoidAction("revalidate") {
		public void map() {
		    ((JComponent)getSource()).revalidate();
		}});}

    /**Maps <code>JComponent.scrollRectToVisible(Rectangle)</code> through queue*/
    public void scrollRectToVisible(final Rectangle rectangle) {
	runMapping(new MapVoidAction("scrollRectToVisible") {
		public void map() {
		    ((JComponent)getSource()).scrollRectToVisible(rectangle);
		}});}

    /**Maps <code>JComponent.setAlignmentX(float)</code> through queue*/
    public void setAlignmentX(final float f) {
	runMapping(new MapVoidAction("setAlignmentX") {
		public void map() {
		    ((JComponent)getSource()).setAlignmentX(f);
		}});}

    /**Maps <code>JComponent.setAlignmentY(float)</code> through queue*/
    public void setAlignmentY(final float f) {
	runMapping(new MapVoidAction("setAlignmentY") {
		public void map() {
		    ((JComponent)getSource()).setAlignmentY(f);
		}});}

    /**Maps <code>JComponent.setAutoscrolls(boolean)</code> through queue*/
    public void setAutoscrolls(final boolean b) {
	runMapping(new MapVoidAction("setAutoscrolls") {
		public void map() {
		    ((JComponent)getSource()).setAutoscrolls(b);
		}});}

    /**Maps <code>JComponent.setBorder(Border)</code> through queue*/
    public void setBorder(final Border border) {
	runMapping(new MapVoidAction("setBorder") {
		public void map() {
		    ((JComponent)getSource()).setBorder(border);
		}});}

    /**Maps <code>JComponent.setDebugGraphicsOptions(int)</code> through queue*/
    public void setDebugGraphicsOptions(final int i) {
	runMapping(new MapVoidAction("setDebugGraphicsOptions") {
		public void map() {
		    ((JComponent)getSource()).setDebugGraphicsOptions(i);
		}});}

    /**Maps <code>JComponent.setDoubleBuffered(boolean)</code> through queue*/
    public void setDoubleBuffered(final boolean b) {
	runMapping(new MapVoidAction("setDoubleBuffered") {
		public void map() {
		    ((JComponent)getSource()).setDoubleBuffered(b);
		}});}

    /**Maps <code>JComponent.setMaximumSize(Dimension)</code> through queue*/
    public void setMaximumSize(final Dimension dimension) {
	runMapping(new MapVoidAction("setMaximumSize") {
		public void map() {
		    ((JComponent)getSource()).setMaximumSize(dimension);
		}});}

    /**Maps <code>JComponent.setMinimumSize(Dimension)</code> through queue*/
    public void setMinimumSize(final Dimension dimension) {
	runMapping(new MapVoidAction("setMinimumSize") {
		public void map() {
		    ((JComponent)getSource()).setMinimumSize(dimension);
		}});}

    /**Maps <code>JComponent.setNextFocusableComponent(Component)</code> through queue*/
    public void setNextFocusableComponent(final Component component) {
	runMapping(new MapVoidAction("setNextFocusableComponent") {
		public void map() {
		    ((JComponent)getSource()).setNextFocusableComponent(component);
		}});}

    /**Maps <code>JComponent.setOpaque(boolean)</code> through queue*/
    public void setOpaque(final boolean b) {
	runMapping(new MapVoidAction("setOpaque") {
		public void map() {
		    ((JComponent)getSource()).setOpaque(b);
		}});}

    /**Maps <code>JComponent.setPreferredSize(Dimension)</code> through queue*/
    public void setPreferredSize(final Dimension dimension) {
	runMapping(new MapVoidAction("setPreferredSize") {
		public void map() {
		    ((JComponent)getSource()).setPreferredSize(dimension);
		}});}

    /**Maps <code>JComponent.setRequestFocusEnabled(boolean)</code> through queue*/
    public void setRequestFocusEnabled(final boolean b) {
	runMapping(new MapVoidAction("setRequestFocusEnabled") {
		public void map() {
		    ((JComponent)getSource()).setRequestFocusEnabled(b);
		}});}

    /**Maps <code>JComponent.setToolTipText(String)</code> through queue*/
    public void setToolTipText(final String string) {
	runMapping(new MapVoidAction("setToolTipText") {
		public void map() {
		    ((JComponent)getSource()).setToolTipText(string);
		}});}

    /**Maps <code>JComponent.unregisterKeyboardAction(KeyStroke)</code> through queue*/
    public void unregisterKeyboardAction(final KeyStroke keyStroke) {
	runMapping(new MapVoidAction("unregisterKeyboardAction") {
		public void map() {
		    ((JComponent)getSource()).unregisterKeyboardAction(keyStroke);
		}});}

    /**Maps <code>JComponent.updateUI()</code> through queue*/
    public void updateUI() {
	runMapping(new MapVoidAction("updateUI") {
		public void map() {
		    ((JComponent)getSource()).updateUI();
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Allows to find component by tooltip.
     */
    public static class JComponentByTipFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
	boolean compareExactly;
	boolean compareCaseSensitive;
        /**
         * Constructs JComponentByTipFinder.
         * @param lb a text pattern
         * @param comparator specifies string comparision algorithm.
         */
	public JComponentByTipFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
        /**
         * Constructs JComponentByTipFinder.
         * @param lb a text pattern
         */
	public JComponentByTipFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JComponent) {
		if(((JComponent)comp).getToolTipText() != null) {
		    return(comparator.equals(((JComponent)comp).getToolTipText(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("JComponent with tool tip \"" + label + "\"");
	}
    }

    /**
     * Checks component type.
     */
    public static class JComponentFinder extends Finder {
        /**
         * Constructs JComponentFinder.
         * @param sf other searching criteria.
         */
	public JComponentFinder(ComponentChooser sf) {
            super(JComponent.class, sf);
	}
        /**
         * Constructs JComponentFinder.
         */
	public JComponentFinder() {
            super(JComponent.class);
	}
    }

    class JToolTipWindowFinder implements ComponentChooser {
        ComponentChooser ppFinder;
	public JToolTipWindowFinder() {
            ppFinder = new ComponentChooser() {
                    public boolean checkComponent(Component comp) {
                        return(comp.isShowing() &&
                               comp.isVisible() &&
                               comp instanceof JToolTip);
                    }
                    public String getDescription() {
                        return("A tool tip");
                    }
                };
	}
	public boolean checkComponent(Component comp) {
	    if(comp.isShowing() && comp instanceof Window) {
		ComponentSearcher cs = new ComponentSearcher((Container)comp);
		cs.setOutput(JemmyProperties.getCurrentOutput().createErrorOutput());
		return(cs.findComponent(ppFinder)
		       != null);
	    }
	    return(false);
	}
	public String getDescription() {
            return("A tool tip window");
	}
    }

    class JToolTipFinder extends Finder {
	public JToolTipFinder(ComponentChooser sf) {
            super(JToolTip.class, sf);
	}
	public JToolTipFinder() {
            super(JToolTip.class);
	}
    }
}
