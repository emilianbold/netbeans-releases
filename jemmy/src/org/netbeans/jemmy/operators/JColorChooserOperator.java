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
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.Timeouts;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JColorChooser;
import javax.swing.JTabbedPane;

import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;

import javax.swing.plaf.ColorChooserUI;

/**
 *
 * Class provides methods to cover main JColorChooser component functionality.
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JColorChooserOperator extends JComponentOperator 
    implements Outputable {

    private static final String RGB_TITLE = "RGB";

    private final static long WAIT_LIST_PAINTED_TIMEOUT = 60000;

    private TestOut output;
    private JTabbedPaneOperator tabbed;
    private JTextFieldOperator red;
    private JTextFieldOperator green;
    private JTextFieldOperator blue;

    /**
     * Constructor.
     */
    public JColorChooserOperator(JColorChooser comp) {
	super(comp);
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
	tabbed = new JTabbedPaneOperator(this);
    }

    public JColorChooserOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JColorChooser)cont.
             waitSubComponent(new JColorChooserFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    public JColorChooserOperator(ContainerOperator cont, ComponentChooser chooser) {
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
    public JColorChooserOperator(ContainerOperator cont, int index) {
	this((JColorChooser)
	     waitComponent(cont,
			   new JColorChooserFinder(),
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
    public JColorChooserOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JColorChooser in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JColorChooser instance or null if component was not found.
     */
    public static JColorChooser findJColorChooser(Container cont, ComponentChooser chooser, int index) {
	return((JColorChooser)findComponent(cont, new JColorChooserFinder(chooser), index));
    }

    /**
     * Searches 0'th JColorChooser in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JColorChooser instance or null if component was not found.
     */
    public static JColorChooser findJColorChooser(Container cont, ComponentChooser chooser) {
	return(findJColorChooser(cont, chooser, 0));
    }

    /**
     * Searches JColorChooser in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JColorChooser instance or null if component was not found.
     */
    public static JColorChooser findJColorChooser(Container cont, int index) {
	return(findJColorChooser(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JColorChooser instance"), index));
    }

    /**
     * Searches 0'th JColorChooser in container.
     * @param cont Container to search component in.
     * @return JColorChooser instance or null if component was not found.
     */
    public static JColorChooser findJColorChooser(Container cont) {
	return(findJColorChooser(cont, 0));
    }

    /**
     * Waits JColorChooser in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JColorChooser instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JColorChooser waitJColorChooser(Container cont, ComponentChooser chooser, int index)  {
	return((JColorChooser)waitComponent(cont, new JColorChooserFinder(chooser), index));
    }

    /**
     * Waits 0'th JColorChooser in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JColorChooser instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JColorChooser waitJColorChooser(Container cont, ComponentChooser chooser) {
	return(waitJColorChooser(cont, chooser, 0));
    }

    /**
     * Waits JColorChooser in container.
     * @param cont Container to search component in.
     * @param index Ordinal component index.
     * @return JColorChooser instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JColorChooser waitJColorChooser(Container cont, int index)  {
	return(waitJColorChooser(cont, ComponentSearcher.getTrueChooser(Integer.toString(index) + "'th JColorChooser instance"), index));
    }

    /**
     * Waits 0'th JColorChooser in container.
     * @param cont Container to search component in.
     * @return JColorChooser instance or null if component was not displayed.
     * @throws TimeoutExpiredException
     */
    public static JColorChooser waitJColorChooser(Container cont) {
	return(waitJColorChooser(cont, 0));
    }

    /**
     * Defines print output streams or writers.
     * @param out Identify the streams or writers used for print output.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     */
    public void setOutput(TestOut out) {
	output = out;
	super.setOutput(output.createErrorOutput());
    }

    /**
     * Returns print output streams or writers.
     * @return an object that contains references to objects for
     * printing to output and err streams.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     */
    public TestOut getOutput() {
	return(output);
    }

    /**
     * Switches tab to "RGB" page.
     */
    public void switchToRGB() {
	if(!tabbed.getTitleAt(tabbed.getSelectedIndex()).
	   equals(RGB_TITLE)) {
	    tabbed.selectPage(RGB_TITLE);
	}
	blue = new JTextFieldOperator(this, 2);
	green = new JTextFieldOperator(this, 1);
	red = new JTextFieldOperator(this, 0);
    }

    /**
     * Enters red color component value.
     * Switches to "RGB" page first.
     * @see #switchToRGB()
     * @see #enterColor(int, int, int)
     * @see #enterColor(java.awt.Color)
     * @see #enterColor(int)
     */
    public void enterRed(int value) {
	switchToRGB();
	red.setText(Integer.toString(value));
    }

    /**
     * Enters green color component value.
     * Switches to "RGB" page first.
     * @see #switchToRGB()
     * @see #enterColor(int, int, int)
     * @see #enterColor(java.awt.Color)
     * @see #enterColor(int)
     */
    public void enterGreen(int value) {
	switchToRGB();
	green.setText(Integer.toString(value));
    }

    /**
     * Enters blue color component value.
     * Switches to "RGB" page first.
     * @see #switchToRGB()
     * @see #enterColor(int, int, int)
     * @see #enterColor(java.awt.Color)
     * @see #enterColor(int)
     */
    public void enterBlue(int value) {
	switchToRGB();
	blue.setText(Integer.toString(value));
    }

    /**
     * Enters all color components values.
     * Switches to "RGB" page first.
     * @see #switchToRGB()
     * @see #enterColor(java.awt.Color)
     * @see #enterColor(int)
     */
    public void enterColor(int red, int green, int blue) {
	switchToRGB();
	enterRed(red);
	enterGreen(green);
	enterBlue(blue);
    }

    /**
     * Enters color.
     * Switches to "RGB" page first.
     * @see #switchToRGB()
     * @see #enterColor(int, int, int)
     * @see #enterColor(int)
     */
    public void enterColor(Color color) {
	enterColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Enters color.
     * Switches to "RGB" page first.
     * @see #switchToRGB()
     * @see #enterColor(int, int, int)
     * @see #enterColor(java.awt.Color)
     */
    public void enterColor(int color) {
	enterColor(new Color(color));
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put("Color", ((JColorChooser)getSource()).getColor().toString());
	JTabbedPane tb = (JTabbedPane)tabbed.getSource();
	result.put("Selected page", tb.getTitleAt(tb.getSelectedIndex()));
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JColorChooser.addChooserPanel(AbstractColorChooserPanel)</code> through queue*/
    public void addChooserPanel(final AbstractColorChooserPanel abstractColorChooserPanel) {
	runMapping(new MapVoidAction("addChooserPanel") {
		public void map() {
		    ((JColorChooser)getSource()).addChooserPanel(abstractColorChooserPanel);
		}});}

    /**Maps <code>JColorChooser.getChooserPanels()</code> through queue*/
    public AbstractColorChooserPanel[] getChooserPanels() {
	return((AbstractColorChooserPanel[])runMapping(new MapAction("getChooserPanels") {
		public Object map() {
		    return(((JColorChooser)getSource()).getChooserPanels());
		}}));}

    /**Maps <code>JColorChooser.getColor()</code> through queue*/
    public Color getColor() {
	return((Color)runMapping(new MapAction("getColor") {
		public Object map() {
		    return(((JColorChooser)getSource()).getColor());
		}}));}

    /**Maps <code>JColorChooser.getPreviewPanel()</code> through queue*/
    public JComponent getPreviewPanel() {
	return((JComponent)runMapping(new MapAction("getPreviewPanel") {
		public Object map() {
		    return(((JColorChooser)getSource()).getPreviewPanel());
		}}));}

    /**Maps <code>JColorChooser.getSelectionModel()</code> through queue*/
    public ColorSelectionModel getSelectionModel() {
	return((ColorSelectionModel)runMapping(new MapAction("getSelectionModel") {
		public Object map() {
		    return(((JColorChooser)getSource()).getSelectionModel());
		}}));}

    /**Maps <code>JColorChooser.getUI()</code> through queue*/
    public ColorChooserUI getUI() {
	return((ColorChooserUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JColorChooser)getSource()).getUI());
		}}));}

    /**Maps <code>JColorChooser.removeChooserPanel(AbstractColorChooserPanel)</code> through queue*/
    public AbstractColorChooserPanel removeChooserPanel(final AbstractColorChooserPanel abstractColorChooserPanel) {
	return((AbstractColorChooserPanel)runMapping(new MapAction("removeChooserPanel") {
		public Object map() {
		    return(((JColorChooser)getSource()).removeChooserPanel(abstractColorChooserPanel));
		}}));}

    /**Maps <code>JColorChooser.setChooserPanels(AbstractColorChooserPanel[])</code> through queue*/
    public void setChooserPanels(final AbstractColorChooserPanel[] abstractColorChooserPanel) {
	runMapping(new MapVoidAction("setChooserPanels") {
		public void map() {
		    ((JColorChooser)getSource()).setChooserPanels(abstractColorChooserPanel);
		}});}

    /**Maps <code>JColorChooser.setColor(int)</code> through queue*/
    public void setColor(final int i) {
	runMapping(new MapVoidAction("setColor") {
		public void map() {
		    ((JColorChooser)getSource()).setColor(i);
		}});}

    /**Maps <code>JColorChooser.setColor(int, int, int)</code> through queue*/
    public void setColor(final int i, final int i1, final int i2) {
	runMapping(new MapVoidAction("setColor") {
		public void map() {
		    ((JColorChooser)getSource()).setColor(i, i1, i2);
		}});}

    /**Maps <code>JColorChooser.setColor(Color)</code> through queue*/
    public void setColor(final Color color) {
	runMapping(new MapVoidAction("setColor") {
		public void map() {
		    ((JColorChooser)getSource()).setColor(color);
		}});}

    /**Maps <code>JColorChooser.setPreviewPanel(JComponent)</code> through queue*/
    public void setPreviewPanel(final JComponent jComponent) {
	runMapping(new MapVoidAction("setPreviewPanel") {
		public void map() {
		    ((JColorChooser)getSource()).setPreviewPanel(jComponent);
		}});}

    /**Maps <code>JColorChooser.setSelectionModel(ColorSelectionModel)</code> through queue*/
    public void setSelectionModel(final ColorSelectionModel colorSelectionModel) {
	runMapping(new MapVoidAction("setSelectionModel") {
		public void map() {
		    ((JColorChooser)getSource()).setSelectionModel(colorSelectionModel);
		}});}

    /**Maps <code>JColorChooser.setUI(ColorChooserUI)</code> through queue*/
    public void setUI(final ColorChooserUI colorChooserUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JColorChooser)getSource()).setUI(colorChooserUI);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    public static class JColorChooserFinder extends Finder {
	public JColorChooserFinder(ComponentChooser sf) {
            super(JColorChooser.class, sf);
	}
	public JColorChooserFinder() {
            super(JColorChooser.class);
	}
    }

}
