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
import org.netbeans.jemmy.JemmyInputException;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;

import org.netbeans.jemmy.util.EmptyVisualizer;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.MultiSelListDriver;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import javax.swing.event.ListSelectionListener;

import javax.swing.plaf.ListUI;

/**
 * <BR><BR>Timeouts used: <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 * ComponentOperator.WaitStateTimeout - time to wait for item, and for item to be selected <BR>
 * JScrollBarOperator.OneScrollClickTimeout - time for one scroll click <BR>
 * JScrollBarOperator.WholeScrollTimeout - time for the whole scrolling <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JListOperator extends JComponentOperator
    implements Outputable {

    /**
     * Identifier for a "item" properties.
     * @see #getDump
     */
    public static final String ITEM_PREFIX_DPROP = "Item";

    /**
     * Identifier for a "selected item" property.
     * @see #getDump
     */
    public static final String SELECTED_ITEM_PREFIX_DPROP = "SelectedItem";

    private TestOut output;
    private MultiSelListDriver driver;

    /**
     * Constructor.
     * @param b a component
     */
    public JListOperator(JList b) {
	super(b);
	driver = DriverManager.getMultiSelListDriver(getClass());
    }

    /**
     * Constructs a JListOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JListOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JList)cont.
             waitSubComponent(new JListFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JListOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JListOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits item text first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param text Text of item which is currently selected. 
     * @param itemIndex Item index.
     * @param index Ordinal component index.
     * 
     */
    public JListOperator(ContainerOperator cont, String text, int itemIndex, int index) {
	this((JList)waitComponent(cont, 
				  new JListByItemFinder(text, itemIndex,
							cont.getComparator()),
				  index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component by selected item text first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param text Text of item which is currently selected. 
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * 
     */
    public JListOperator(ContainerOperator cont, String text, int index) {
	this(cont, text, -1, index);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param text Text of item which is currently selected. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * 
     */
    public JListOperator(ContainerOperator cont, String text) {
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
    public JListOperator(ContainerOperator cont, int index) {
	this((JList)
	     waitComponent(cont, 
			   new JListFinder(),
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
    public JListOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JList in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JList instance or null if component was not found.
     */
    public static JList findJList(Container cont, ComponentChooser chooser, int index) {
	return((JList)findComponent(cont, new JListFinder(chooser), index));
    }

    /**
     * Searches 0'th JList in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JList instance or null if component was not found.
     */
    public static JList findJList(Container cont, ComponentChooser chooser) {
	return(findJList(cont, chooser, 0));
    }

    /**
     * Searches JList by item.
     * @param cont Container to search component in.
     * @param text Item text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param itemIndex Index of item to compare text. If -1, selected item is checked.
     * @param index Ordinal component index.
     * @return JList instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JList findJList(Container cont, String text, boolean ce, boolean ccs, int itemIndex, int index) {
	return(findJList(cont, new JListByItemFinder(text, itemIndex, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Searches JList by item.
     * @param cont Container to search component in.
     * @param text Item text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param itemIndex Index of item to compare text. If -1, selected item is checked.
     * @return JList instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JList findJList(Container cont, String text, boolean ce, boolean ccs, int itemIndex) {
	return(findJList(cont, text, ce, ccs, itemIndex, 0));
    }

    /**
     * Waits JList in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JList instance or null if component was not found.
     * 
     */
    public static JList waitJList(Container cont, ComponentChooser chooser, int index) {
	return((JList)waitComponent(cont, new JListFinder(chooser), index));
    }

    /**
     * Waits 0'th JList in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JList instance or null if component was not found.
     * 
     */
    public static JList waitJList(Container cont, ComponentChooser chooser) {
	return(waitJList(cont, chooser, 0));
    }

    /**
     * Waits JList by item.
     * @param cont Container to search component in.
     * @param text Item text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param itemIndex Index of item to compare text. If -1, selected item is checked.
     * @param index Ordinal component index.
     * @return JList instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * 
     */
    public static JList waitJList(Container cont, String text, boolean ce, boolean ccs, int itemIndex, int index) {
	return(waitJList(cont, new JListByItemFinder(text, itemIndex, new DefaultStringComparator(ce, ccs)), index));
    }

    /**
     * Waits JList by item.
     * @param cont Container to search component in.
     * @param text Item text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param itemIndex Index of item to compare text. If -1, selected item is checked.
     * @return JList instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * 
     */
    public static JList waitJList(Container cont, String text, boolean ce, boolean ccs, int itemIndex) {
	return(waitJList(cont, text, ce, ccs, itemIndex, 0));
    }

    public void setOutput(TestOut output) {
	super.setOutput(output.createErrorOutput());
	this.output = output;
    }

    public TestOut getOutput() {
	return(output);
    }

    public void copyEnvironment(Operator anotherOperator) {
	super.copyEnvironment(anotherOperator);
	driver = 
	    (MultiSelListDriver)DriverManager.
	    getDriver(DriverManager.MULTISELLIST_DRIVER_ID,
		      getClass(), 
		      anotherOperator.getProperties());
    }

    /**
     * Gets point to click on itemIndex'th item.
     * @param itemIndex an index of an item to click.
     * @return a Point in component's coordinate system.
     */
    public Point getClickPoint(int itemIndex) {
	Rectangle rect = getCellBounds(itemIndex, itemIndex);
	return(new Point(rect.x + rect.width / 2,
			 rect.y + rect.height / 2));
    }

    /**
     * Ask renderer for component to be displayed.
     * @param itemIndex Item index.
     * @param isSelected True if the specified cell was selected.
     * @param cellHasFocus True if the specified cell has the focus.
     * @return Component to be displayed.
     */
    public Component getRenderedComponent(int itemIndex, boolean isSelected, boolean cellHasFocus) {
	return(getCellRenderer().
	       getListCellRendererComponent((JList)getSource(),
					    getModel().getElementAt(itemIndex),
					    itemIndex,
					    isSelected,
					    cellHasFocus));
    }

    /**
     * Ask renderer for component to be displayed.
     * Uses isSelectedIndex(itemIndex) to determine whether item is selected.
     * Supposes item do not have focus.
     * @param itemIndex Item index.
     * @return Component to be displayed.
     */
    public Component getRenderedComponent(int itemIndex) {
	return(getRenderedComponent(itemIndex, isSelectedIndex(itemIndex), false));
    }

    /**
     * Searches for index'th item good from chooser's point of view.
     * @param chooser Item verifying object.
     * @param index Ordinal item index.
     * @return Item index or -1 if search was insuccessful.
     */
    public int findItemIndex(ListItemChooser chooser, int index) {
	ListModel model = getModel();
	int count = 0;
	for(int i = 0; i < model.getSize(); i++) {
	    if(chooser.checkItem(this, i)) {
		if(count == index) {
		    return(i);
		} else {
		    count++;
		}
	    }
	}
	return(-1);
    }

    /**
     * Searches for an item good from chooser's point of view.
     * @param chooser Item verifying object.
     * @return Item index or -1 if serch was insuccessful.
     * @see #findItemIndex(JListOperator.ListItemChooser, int)
     * @see #findItemIndex(String, boolean, boolean)
     */
    public int findItemIndex(ListItemChooser chooser) {
	return(findItemIndex(chooser, 0));
    }

    /**
     * Searches for an item good from chooser's point of view.
     * @param item a text pattern
     * @param comparator a string comparision algorithm
     * @param index Ordinal item index.
     * @return Item index or -1 if serch was insuccessful.
     * @see #findItemIndex(JListOperator.ListItemChooser, int)
     * @see #findItemIndex(String, boolean, boolean)
     */
    public int findItemIndex(String item, StringComparator comparator, int index){
	return(findItemIndex(new BySubStringListItemChooser(item, comparator), index));
    }

    /**
     * Searched for index'th item by text.
     * @param item a text pattern
     * @param ce Compare text exactly.
     * @param cc Compare text case sensitively.
     * @param index Ordinal item index.
     * @return Item index or -1 if serch was insuccessful.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @deprecated Use findItemIndex(String, int) or findItemIndex(String, StringComparator, int)
     */
    public int findItemIndex(String item, boolean ce, boolean cc, int index){
	return(findItemIndex(item, new DefaultStringComparator(ce, cc), index));
    }
    
    /**
     * Searched for index'th item by text.
     * Uses StringComparator assigned to this object.
     * @param item a text pattern
     * @param index Ordinal item index.
     * @return Item index or -1 if search was insuccessful.
     */
    public int findItemIndex(String item, int index){
	return(findItemIndex(item, getComparator(), index));
    }
    
    /**
     * Searches for an item good from chooser's point of view.
     * @param item a text pattern
     * @param comparator a string comparision algorithm
     * @return Item index or -1 if serch was insuccessful.
     * @see #findItemIndex(JListOperator.ListItemChooser, int)
     * @see #findItemIndex(String, boolean, boolean)
     */
    public int findItemIndex(String item, StringComparator comparator){
	return(findItemIndex(item, comparator, 0));
    }

    /**
     * Searched  item by text.
     * @param item a text pattern
     * @param ce Compare text exactly.
     * @param cc Compare text case sensitively.
     * @return Item index or -1 if search was insuccessful.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @deprecated Use findItemIndex(String) or findItemIndex(String, StringComparator)
     */
    public int findItemIndex(String item, boolean ce, boolean cc) {
	return(findItemIndex(item, ce, cc, 0));
    }
    
    /**
     * Searched for first item by text.
     * Uses StringComparator assigned to this object.
     * @param item a text pattern
     * @return Item index or -1 if search was insuccessful.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public int findItemIndex(String item){
	return(findItemIndex(item, 0));
    }
    
    /**
     * Searches for index'th item by rendered component.
     * @param chooser Component verifying object.
     * @param index Ordinal item index.
     * @return Item index or -1 if serch was insuccessful.
     * @see #getRenderedComponent(int, boolean, boolean)
     */
    public int findItemIndex(ComponentChooser chooser, int index) {
	return(findItemIndex(new ByRenderedComponentListItemChooser(chooser), index));
    }
    
    /**
     * Searches for an item by rendered component.
     * @param chooser Component verifying object.
     * @return Item index or -1 if serch was insuccessful.
     * @see #getRenderedComponent(int, boolean, boolean)
     */
    public int findItemIndex(ComponentChooser chooser) {
	return(findItemIndex(chooser, 0));
    }
    
    /**
     * Clicks on item by item index.
     * @param itemIndex Item index.
     * @param clickCount count click.
     * @return Click point or null if list does not contains itemIndex'th item.
     * @throws NoSuchItemException
     */
    public Object clickOnItem(final int itemIndex, final int clickCount) {
	output.printLine("Click " + Integer.toString(clickCount) +
			 " times on JList\n    : " + toStringSource());
	output.printGolden("Click " + Integer.toString(clickCount) +
			 " times on JList");
        checkIndex(itemIndex);
	try {
	    scrollToItem(itemIndex);
	} catch(TimeoutExpiredException e) {
	    output.printStackTrace(e);
	}
	if(((JList)getSource()).getModel().getSize() <= itemIndex) {
	    output.printErrLine("JList " + toStringSource() + " does not contain " +
				Integer.toString(itemIndex) + "'th item");
	    return(null);
	}
	if(((JList)getSource()).getAutoscrolls()) {
	    ((JList)getSource()).ensureIndexIsVisible(itemIndex);
	}
        return(getQueueTool().invokeSmoothly(new QueueTool.QueueAction("Path selecting") {
                public Object launch() {
                    Rectangle rect = getCellBounds(itemIndex, itemIndex);
                    if(rect == null) {
                        output.printErrLine("Impossible to determine click point for " +
                                            Integer.toString(itemIndex) + "'th item");
                        return(null);
                    }
                    Point point = new Point((int)(rect.getX() + rect.getWidth() / 2),
                                            (int)(rect.getY() + rect.getHeight() / 2));
                    Object result = getModel().getElementAt(itemIndex);
                    clickMouse(point.x, point.y, clickCount);
                    return(result);
                }
            }));
    }
    
    /**
     * Finds item by item text, and do mouse click on it.
     * @param item Item text.
     * @param comparator a string comparision algorithm
     * @param clickCount count click.
     * @return Click point or null if list does not contains itemIndex'th item.
     * @throws NoSuchItemException
     */
    public Object clickOnItem(final String item, final StringComparator comparator, final int clickCount) {
        scrollToItem(findItemIndex(item, comparator, 0));
        return(getQueueTool().invokeSmoothly(new QueueTool.QueueAction("Path selecting") {
                public Object launch() {
                    int index = findItemIndex(item, comparator, 0);
                    if(index != -1) {
                        return(clickOnItem(index, clickCount));
                    } else {
                        throw(new NoSuchItemException(item));
                    }
                }
            }));
    }
    
    /**
     * Finds item by item text, and do mouse click on it.
     * @param item Item text.
     * @param ce Compare exactly.
     * @param cc Compare case sensitively.
     * @param clickCount count click.
     * @return Click point or null if list does not contains itemIndex'th item.
     * @throws NoSuchItemException
     * @deprecated Use clickOnItem(String, int) or clickOnItem(String, StringComparator, int)
     */
    public Object clickOnItem(String item, boolean ce, boolean cc, int clickCount) {
	return(clickOnItem(item, new DefaultStringComparator(ce, cc), clickCount));
    }

    /**
     * Finds item by item text, and do mouse click on it.
     * Uses StringComparator assigned to this object.
     * @param item Item text.
     * @param clickCount count click.
     * @return Click point or null if list does not contains itemIndex'th item.
     * @throws NoSuchItemException
     */
    public Object clickOnItem(String item, int clickCount) {
	return(clickOnItem(item, getComparator(), clickCount));
    }

    /**
     * Finds item by item text, and do simple mouse click on it.
     * Uses StringComparator assigned to this object.
     * @param item Item text.
     * @param comparator a string comparision algorithm
     * @return Click point or null if list does not contains itemIndex'th item.
     * @throws NoSuchItemException
     */
    public Object clickOnItem(String item, StringComparator comparator) {
	return(clickOnItem(item, comparator, 1));
    }

    /**
     * Finds item by item text, and do simple mouse click on it.
     * @param item Item text.
     * @param ce Compare exactly.
     * @param cc Compare case sensitively.
     * @return Click point or null if list does not contains itemIndex'th item.
     * @throws NoSuchItemException
     * @deprecated Use clickOnItem(String) or clickOnItem(String, StringComparator)
     */
    public Object clickOnItem(String item, boolean ce, boolean cc) {
	return(clickOnItem(item, ce, cc, 1));
    }

    /**
     * Finds item by item text, and do simple mouse click on it.
     * Uses StringComparator assigned to this object.
     * @param item Item text.
     * @return Click point or null if list does not contains itemIndex'th item.
     * @throws NoSuchItemException
     */
    public Object clickOnItem(String item) {
	return(clickOnItem(item, 0));
    }

    /**
     * Scrolls to an item if the list is on a JScrollPane component.
     * @param itemIndex an item index.
     * @see #scrollToItem(String, boolean, boolean)
     * 
     * @throws NoSuchItemException
     */
    public void scrollToItem(int itemIndex) {
	output.printTrace("Scroll JList to " + Integer.toString(itemIndex) + "'th item\n    : " +
			  toStringSource());
	output.printGolden("Scroll JList to " + Integer.toString(itemIndex) + "'th item");
        checkIndex(itemIndex);
	makeComponentVisible();
	//try to find JScrollPane under.
	JScrollPane scroll = (JScrollPane)getContainer(new JScrollPaneOperator.
						       JScrollPaneFinder(ComponentSearcher.
									 getTrueChooser("JScrollPane")));
	if(scroll == null) {
	    return;
	}
	JScrollPaneOperator scroller = new JScrollPaneOperator(scroll);
	scroller.copyEnvironment(this);
	scroller.setVisualizer(new EmptyVisualizer());
	Rectangle rect = getCellBounds(itemIndex, itemIndex);
	scroller.scrollToComponentRectangle(getSource(), 
					    (int)rect.getX(),
					    (int)rect.getY(),
					    (int)rect.getWidth(),
					    (int)rect.getHeight());
    }

    /**
     * Scrolls to an item if the list is on a JScrollPane component.
     * @param item Item text
     * @param comparator a string comparision algorithm
     * @see #scrollToItem(String, boolean, boolean)
     * 
     */
    public void scrollToItem(String item, StringComparator comparator) {
	scrollToItem(findItemIndex(item, comparator));
    }

    /**
     * Scrolls to an item if the list is on a JScrollPane component.
     * @param item Item text
     * @param ce Compare exactly.
     * @param cc Compare case sensitively.
     * @see #scrollToItem(String, boolean, boolean)
     * 
     * @deprecated Use scrollToItem(String) or scrollToItem(String, StringComparator)
     */
    public void scrollToItem(String item, boolean ce, boolean cc) {
	scrollToItem(findItemIndex(item, ce, cc));
    }

    /**
     * Selects an item by index.
     * @param index an item index.
     */
    public void selectItem(int index) {
        checkIndex(index);
        driver.selectItem(this, index);
	if(getVerification()) {
            waitItemSelection(index, true);
        }
    }

    /**
     * Selects an item by text.
     * @param item an item text.
     */
    public void selectItem(final String item) {
        scrollToItem(findItemIndex(item));
        getQueueTool().invokeSmoothly(new QueueTool.QueueAction("Path selecting") {
                public Object launch() {
                    driver.selectItem(JListOperator.this, findItemIndex(item));
                    return(null);
                }
            });
    }

    /**
     * Selects items by indices.
     * @param indices item indices.
     */
    public void selectItems(int[] indices) {
        checkIndices(indices);
	driver.selectItems(this, indices);
	if(getVerification()) {
            waitItemsSelection(indices, true);
        }
    }

    /**
     * Selects items by texts.
     * @param items item texts.
     */
    public void selectItem(String[] items) {
	int[] indices = new int[items.length];
	for(int i = 0; i < items.length; i++) {
	    indices[i] = findItemIndex(items[i]);
	}
	selectItems(indices);
    }

    /**
     * Waits for items to be selected.
     * @param itemIndices item indices to be selected
     * @param selected Selected (true) or unselected (false).
     */
    public void waitItemsSelection(final int[] itemIndices, final boolean selected) {
	getOutput().printLine("Wait items to be " +
			      (selected ? "" : "un") + "selected in component \n    : "+
			      toStringSource());
	getOutput().printGolden("Wait items to be " +
				(selected ? "" : "un") + "selected");
	waitState(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
                    int[] indices = getSelectedIndices();
                    for(int i = 0; i < indices.length; i++) {
                        if(indices[i] != itemIndices[i]) {
                            return(false);
                        }
                    }
                    return(true);
		}
		public String getDescription() {
		    return("Item has been " + 
			   (selected ? "" : "un") + "selected");
		}
	    });
    }

    /**
     * Waits for item to be selected.
     * @param itemIndex an item needs to be selected
     * @param selected Selected (true) or unselected (false).
     */
    public void waitItemSelection(final int itemIndex, final boolean selected) {
        waitItemsSelection(new int[] {itemIndex}, selected);
    }

    /**
     * Waits for item. Uses getComparator() comparator.
     * @param item an item text
     * @param itemIndex Index of item to check or -1 to check selected item.
     */
    public void waitItem(String item, int itemIndex) {
	getOutput().printLine("Wait \"" + item + "\" at the " + Integer.toString(itemIndex) + 
			      " position in component \n    : "+
			      toStringSource());
	getOutput().printGolden("Wait \"" + item + "\" at the " + Integer.toString(itemIndex) + 
				" position");
	waitState(new JListByItemFinder(item, itemIndex, getComparator()));
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	String[] items = new String[((JList)getSource()).getModel().getSize()];
	for(int i = 0; i < ((JList)getSource()).getModel().getSize(); i++) {
	    items[i] = ((JList)getSource()).getModel().getElementAt(i).toString();
	}
	int[] selectedIndices = ((JList)getSource()).getSelectedIndices();
	String[] selectedItems = new String[selectedIndices.length];
	for(int i = 0; i < selectedIndices.length; i++) {
	    selectedItems[i] = items[selectedIndices[i]];
	}
	addToDump(result, ITEM_PREFIX_DPROP, items);
	addToDump(result, SELECTED_ITEM_PREFIX_DPROP, selectedItems);
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JList.addListSelectionListener(ListSelectionListener)</code> through queue*/
    public void addListSelectionListener(final ListSelectionListener listSelectionListener) {
	runMapping(new MapVoidAction("addListSelectionListener") {
		public void map() {
		    ((JList)getSource()).addListSelectionListener(listSelectionListener);
		}});}

    /**Maps <code>JList.addSelectionInterval(int, int)</code> through queue*/
    public void addSelectionInterval(final int i, final int i1) {
	runMapping(new MapVoidAction("addSelectionInterval") {
		public void map() {
		    ((JList)getSource()).addSelectionInterval(i, i1);
		}});}

    /**Maps <code>JList.clearSelection()</code> through queue*/
    public void clearSelection() {
	runMapping(new MapVoidAction("clearSelection") {
		public void map() {
		    ((JList)getSource()).clearSelection();
		}});}

    /**Maps <code>JList.ensureIndexIsVisible(int)</code> through queue*/
    public void ensureIndexIsVisible(final int i) {
	runMapping(new MapVoidAction("ensureIndexIsVisible") {
		public void map() {
		    ((JList)getSource()).ensureIndexIsVisible(i);
		}});}

    /**Maps <code>JList.getAnchorSelectionIndex()</code> through queue*/
    public int getAnchorSelectionIndex() {
	return(runMapping(new MapIntegerAction("getAnchorSelectionIndex") {
		public int map() {
		    return(((JList)getSource()).getAnchorSelectionIndex());
		}}));}

    /**Maps <code>JList.getCellBounds(int, int)</code> through queue*/
    public Rectangle getCellBounds(final int i, final int i1) {
	return((Rectangle)runMapping(new MapAction("getCellBounds") {
		public Object map() {
		    return(((JList)getSource()).getCellBounds(i, i1));
		}}));}

    /**Maps <code>JList.getCellRenderer()</code> through queue*/
    public ListCellRenderer getCellRenderer() {
	return((ListCellRenderer)runMapping(new MapAction("getCellRenderer") {
		public Object map() {
		    return(((JList)getSource()).getCellRenderer());
		}}));}

    /**Maps <code>JList.getFirstVisibleIndex()</code> through queue*/
    public int getFirstVisibleIndex() {
	return(runMapping(new MapIntegerAction("getFirstVisibleIndex") {
		public int map() {
		    return(((JList)getSource()).getFirstVisibleIndex());
		}}));}

    /**Maps <code>JList.getFixedCellHeight()</code> through queue*/
    public int getFixedCellHeight() {
	return(runMapping(new MapIntegerAction("getFixedCellHeight") {
		public int map() {
		    return(((JList)getSource()).getFixedCellHeight());
		}}));}

    /**Maps <code>JList.getFixedCellWidth()</code> through queue*/
    public int getFixedCellWidth() {
	return(runMapping(new MapIntegerAction("getFixedCellWidth") {
		public int map() {
		    return(((JList)getSource()).getFixedCellWidth());
		}}));}

    /**Maps <code>JList.getLastVisibleIndex()</code> through queue*/
    public int getLastVisibleIndex() {
	return(runMapping(new MapIntegerAction("getLastVisibleIndex") {
		public int map() {
		    return(((JList)getSource()).getLastVisibleIndex());
		}}));}

    /**Maps <code>JList.getLeadSelectionIndex()</code> through queue*/
    public int getLeadSelectionIndex() {
	return(runMapping(new MapIntegerAction("getLeadSelectionIndex") {
		public int map() {
		    return(((JList)getSource()).getLeadSelectionIndex());
		}}));}

    /**Maps <code>JList.getMaxSelectionIndex()</code> through queue*/
    public int getMaxSelectionIndex() {
	return(runMapping(new MapIntegerAction("getMaxSelectionIndex") {
		public int map() {
		    return(((JList)getSource()).getMaxSelectionIndex());
		}}));}

    /**Maps <code>JList.getMinSelectionIndex()</code> through queue*/
    public int getMinSelectionIndex() {
	return(runMapping(new MapIntegerAction("getMinSelectionIndex") {
		public int map() {
		    return(((JList)getSource()).getMinSelectionIndex());
		}}));}

    /**Maps <code>JList.getModel()</code> through queue*/
    public ListModel getModel() {
	return((ListModel)runMapping(new MapAction("getModel") {
		public Object map() {
		    return(((JList)getSource()).getModel());
		}}));}

    /**Maps <code>JList.getPreferredScrollableViewportSize()</code> through queue*/
    public Dimension getPreferredScrollableViewportSize() {
	return((Dimension)runMapping(new MapAction("getPreferredScrollableViewportSize") {
		public Object map() {
		    return(((JList)getSource()).getPreferredScrollableViewportSize());
		}}));}

    /**Maps <code>JList.getPrototypeCellValue()</code> through queue*/
    public Object getPrototypeCellValue() {
	return((Object)runMapping(new MapAction("getPrototypeCellValue") {
		public Object map() {
		    return(((JList)getSource()).getPrototypeCellValue());
		}}));}

    /**Maps <code>JList.getScrollableBlockIncrement(Rectangle, int, int)</code> through queue*/
    public int getScrollableBlockIncrement(final Rectangle rectangle, final int i, final int i1) {
	return(runMapping(new MapIntegerAction("getScrollableBlockIncrement") {
		public int map() {
		    return(((JList)getSource()).getScrollableBlockIncrement(rectangle, i, i1));
		}}));}

    /**Maps <code>JList.getScrollableTracksViewportHeight()</code> through queue*/
    public boolean getScrollableTracksViewportHeight() {
	return(runMapping(new MapBooleanAction("getScrollableTracksViewportHeight") {
		public boolean map() {
		    return(((JList)getSource()).getScrollableTracksViewportHeight());
		}}));}

    /**Maps <code>JList.getScrollableTracksViewportWidth()</code> through queue*/
    public boolean getScrollableTracksViewportWidth() {
	return(runMapping(new MapBooleanAction("getScrollableTracksViewportWidth") {
		public boolean map() {
		    return(((JList)getSource()).getScrollableTracksViewportWidth());
		}}));}

    /**Maps <code>JList.getScrollableUnitIncrement(Rectangle, int, int)</code> through queue*/
    public int getScrollableUnitIncrement(final Rectangle rectangle, final int i, final int i1) {
	return(runMapping(new MapIntegerAction("getScrollableUnitIncrement") {
		public int map() {
		    return(((JList)getSource()).getScrollableUnitIncrement(rectangle, i, i1));
		}}));}

    /**Maps <code>JList.getSelectedIndex()</code> through queue*/
    public int getSelectedIndex() {
	return(runMapping(new MapIntegerAction("getSelectedIndex") {
		public int map() {
		    return(((JList)getSource()).getSelectedIndex());
		}}));}

    /**Maps <code>JList.getSelectedIndices()</code> through queue*/
    public int[] getSelectedIndices() {
	return((int[])runMapping(new MapAction("getSelectedIndices") {
		public Object map() {
		    return(((JList)getSource()).getSelectedIndices());
		}}));}

    /**Maps <code>JList.getSelectedValue()</code> through queue*/
    public Object getSelectedValue() {
	return((Object)runMapping(new MapAction("getSelectedValue") {
		public Object map() {
		    return(((JList)getSource()).getSelectedValue());
		}}));}

    /**Maps <code>JList.getSelectedValues()</code> through queue*/
    public Object[] getSelectedValues() {
	return((Object[])runMapping(new MapAction("getSelectedValues") {
		public Object map() {
		    return(((JList)getSource()).getSelectedValues());
		}}));}

    /**Maps <code>JList.getSelectionBackground()</code> through queue*/
    public Color getSelectionBackground() {
	return((Color)runMapping(new MapAction("getSelectionBackground") {
		public Object map() {
		    return(((JList)getSource()).getSelectionBackground());
		}}));}

    /**Maps <code>JList.getSelectionForeground()</code> through queue*/
    public Color getSelectionForeground() {
	return((Color)runMapping(new MapAction("getSelectionForeground") {
		public Object map() {
		    return(((JList)getSource()).getSelectionForeground());
		}}));}

    /**Maps <code>JList.getSelectionMode()</code> through queue*/
    public int getSelectionMode() {
	return(runMapping(new MapIntegerAction("getSelectionMode") {
		public int map() {
		    return(((JList)getSource()).getSelectionMode());
		}}));}

    /**Maps <code>JList.getSelectionModel()</code> through queue*/
    public ListSelectionModel getSelectionModel() {
	return((ListSelectionModel)runMapping(new MapAction("getSelectionModel") {
		public Object map() {
		    return(((JList)getSource()).getSelectionModel());
		}}));}

    /**Maps <code>JList.getUI()</code> through queue*/
    public ListUI getUI() {
	return((ListUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JList)getSource()).getUI());
		}}));}

    /**Maps <code>JList.getValueIsAdjusting()</code> through queue*/
    public boolean getValueIsAdjusting() {
	return(runMapping(new MapBooleanAction("getValueIsAdjusting") {
		public boolean map() {
		    return(((JList)getSource()).getValueIsAdjusting());
		}}));}

    /**Maps <code>JList.getVisibleRowCount()</code> through queue*/
    public int getVisibleRowCount() {
	return(runMapping(new MapIntegerAction("getVisibleRowCount") {
		public int map() {
		    return(((JList)getSource()).getVisibleRowCount());
		}}));}

    /**Maps <code>JList.indexToLocation(int)</code> through queue*/
    public Point indexToLocation(final int i) {
	return((Point)runMapping(new MapAction("indexToLocation") {
		public Object map() {
		    return(((JList)getSource()).indexToLocation(i));
		}}));}

    /**Maps <code>JList.isSelectedIndex(int)</code> through queue*/
    public boolean isSelectedIndex(final int i) {
	return(runMapping(new MapBooleanAction("isSelectedIndex") {
		public boolean map() {
		    return(((JList)getSource()).isSelectedIndex(i));
		}}));}

    /**Maps <code>JList.isSelectionEmpty()</code> through queue*/
    public boolean isSelectionEmpty() {
	return(runMapping(new MapBooleanAction("isSelectionEmpty") {
		public boolean map() {
		    return(((JList)getSource()).isSelectionEmpty());
		}}));}

    /**Maps <code>JList.locationToIndex(Point)</code> through queue*/
    public int locationToIndex(final Point point) {
	return(runMapping(new MapIntegerAction("locationToIndex") {
		public int map() {
		    return(((JList)getSource()).locationToIndex(point));
		}}));}

    /**Maps <code>JList.removeListSelectionListener(ListSelectionListener)</code> through queue*/
    public void removeListSelectionListener(final ListSelectionListener listSelectionListener) {
	runMapping(new MapVoidAction("removeListSelectionListener") {
		public void map() {
		    ((JList)getSource()).removeListSelectionListener(listSelectionListener);
		}});}

    /**Maps <code>JList.removeSelectionInterval(int, int)</code> through queue*/
    public void removeSelectionInterval(final int i, final int i1) {
	runMapping(new MapVoidAction("removeSelectionInterval") {
		public void map() {
		    ((JList)getSource()).removeSelectionInterval(i, i1);
		}});}

    /**Maps <code>JList.setCellRenderer(ListCellRenderer)</code> through queue*/
    public void setCellRenderer(final ListCellRenderer listCellRenderer) {
	runMapping(new MapVoidAction("setCellRenderer") {
		public void map() {
		    ((JList)getSource()).setCellRenderer(listCellRenderer);
		}});}

    /**Maps <code>JList.setFixedCellHeight(int)</code> through queue*/
    public void setFixedCellHeight(final int i) {
	runMapping(new MapVoidAction("setFixedCellHeight") {
		public void map() {
		    ((JList)getSource()).setFixedCellHeight(i);
		}});}

    /**Maps <code>JList.setFixedCellWidth(int)</code> through queue*/
    public void setFixedCellWidth(final int i) {
	runMapping(new MapVoidAction("setFixedCellWidth") {
		public void map() {
		    ((JList)getSource()).setFixedCellWidth(i);
		}});}

    /**Maps <code>JList.setListData(Vector)</code> through queue*/
    public void setListData(final Vector vector) {
	runMapping(new MapVoidAction("setListData") {
		public void map() {
		    ((JList)getSource()).setListData(vector);
		}});}

    /**Maps <code>JList.setListData(Object[])</code> through queue*/
    public void setListData(final Object[] object) {
	runMapping(new MapVoidAction("setListData") {
		public void map() {
		    ((JList)getSource()).setListData(object);
		}});}

    /**Maps <code>JList.setModel(ListModel)</code> through queue*/
    public void setModel(final ListModel listModel) {
	runMapping(new MapVoidAction("setModel") {
		public void map() {
		    ((JList)getSource()).setModel(listModel);
		}});}

    /**Maps <code>JList.setPrototypeCellValue(Object)</code> through queue*/
    public void setPrototypeCellValue(final Object object) {
	runMapping(new MapVoidAction("setPrototypeCellValue") {
		public void map() {
		    ((JList)getSource()).setPrototypeCellValue(object);
		}});}

    /**Maps <code>JList.setSelectedIndex(int)</code> through queue*/
    public void setSelectedIndex(final int i) {
	runMapping(new MapVoidAction("setSelectedIndex") {
		public void map() {
		    ((JList)getSource()).setSelectedIndex(i);
		}});}

    /**Maps <code>JList.setSelectedIndices(int[])</code> through queue*/
    public void setSelectedIndices(final int[] i) {
	runMapping(new MapVoidAction("setSelectedIndices") {
		public void map() {
		    ((JList)getSource()).setSelectedIndices(i);
		}});}

    /**Maps <code>JList.setSelectedValue(Object, boolean)</code> through queue*/
    public void setSelectedValue(final Object object, final boolean b) {
	runMapping(new MapVoidAction("setSelectedValue") {
		public void map() {
		    ((JList)getSource()).setSelectedValue(object, b);
		}});}

    /**Maps <code>JList.setSelectionBackground(Color)</code> through queue*/
    public void setSelectionBackground(final Color color) {
	runMapping(new MapVoidAction("setSelectionBackground") {
		public void map() {
		    ((JList)getSource()).setSelectionBackground(color);
		}});}

    /**Maps <code>JList.setSelectionForeground(Color)</code> through queue*/
    public void setSelectionForeground(final Color color) {
	runMapping(new MapVoidAction("setSelectionForeground") {
		public void map() {
		    ((JList)getSource()).setSelectionForeground(color);
		}});}

    /**Maps <code>JList.setSelectionInterval(int, int)</code> through queue*/
    public void setSelectionInterval(final int i, final int i1) {
	runMapping(new MapVoidAction("setSelectionInterval") {
		public void map() {
		    ((JList)getSource()).setSelectionInterval(i, i1);
		}});}

    /**Maps <code>JList.setSelectionMode(int)</code> through queue*/
    public void setSelectionMode(final int i) {
	runMapping(new MapVoidAction("setSelectionMode") {
		public void map() {
		    ((JList)getSource()).setSelectionMode(i);
		}});}

    /**Maps <code>JList.setSelectionModel(ListSelectionModel)</code> through queue*/
    public void setSelectionModel(final ListSelectionModel listSelectionModel) {
	runMapping(new MapVoidAction("setSelectionModel") {
		public void map() {
		    ((JList)getSource()).setSelectionModel(listSelectionModel);
		}});}

    /**Maps <code>JList.setUI(ListUI)</code> through queue*/
    public void setUI(final ListUI listUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JList)getSource()).setUI(listUI);
		}});}

    /**Maps <code>JList.setValueIsAdjusting(boolean)</code> through queue*/
    public void setValueIsAdjusting(final boolean b) {
	runMapping(new MapVoidAction("setValueIsAdjusting") {
		public void map() {
		    ((JList)getSource()).setValueIsAdjusting(b);
		}});}

    /**Maps <code>JList.setVisibleRowCount(int)</code> through queue*/
    public void setVisibleRowCount(final int i) {
	runMapping(new MapVoidAction("setVisibleRowCount") {
		public void map() {
		    ((JList)getSource()).setVisibleRowCount(i);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    private void checkIndex(int index) {
        if(index < 0 ||
           index >= getModel().getSize()) {
            throw(new NoSuchItemException(index));
        }
    }
    private void checkIndices(int[] indices) {
        for(int i = 0; i < indices.length; i++) {
            checkIndex(indices[i]);
        }
    }
    /**
     * Iterface to choose list item.
     */
    public interface ListItemChooser {
	/**
	 * Should be true if item is good.
	 * @param oper Operator used to search item.
	 * @param index Index of an item be checked.
         * @return true if the item fits the criteria
	 */
	public boolean checkItem(JListOperator oper, int index);

	/**
	 * Item description.
         * @return a description.
	 */
	public String getDescription();
    }

    /**
     * Can be throught during item selecting if list does not have
     * item requested.
     */
    public class NoSuchItemException extends JemmyInputException {
	/**
	 * Constructor.
         * @param item an item's text
	 */
	public NoSuchItemException(String item) {
	    super("No such item as \"" + item + "\"", getSource());
	}
	/**
	 * Constructor.
         * @param index an item's index
	 */
	public NoSuchItemException(int index) {
	    super("List does not contain " + index + "'th item", getSource());
	}
    }

    private class BySubStringListItemChooser implements ListItemChooser {
	String subString;
	StringComparator comparator;

	public BySubStringListItemChooser(String subString, StringComparator comparator) {
	    this.subString = subString;
	    this.comparator = comparator;
	}

	public boolean checkItem(JListOperator oper, int index) {
	    return(comparator.equals(oper.getModel().getElementAt(index).toString(), 
				     subString));
	}

	public String getDescription() {
	    return("Item containing \"" + subString + "\" string");
	}
    }

    private class ByRenderedComponentListItemChooser implements ListItemChooser {
	ComponentChooser chooser;
	public ByRenderedComponentListItemChooser(ComponentChooser chooser) {
	    this.chooser = chooser;
	}
	public boolean checkItem(JListOperator oper, int index) {
	    return(chooser.checkComponent(oper.getRenderedComponent(index)));
	}
	public String getDescription() {
	    return(chooser.getDescription());
	}
    }

    /**
     * Allows to find component by an item.
     */
    public static class JListByItemFinder implements ComponentChooser {
	String label;
	int itemIndex;
	StringComparator comparator;
        /**
         * Constructs JListByItemFinder.
         * @param lb a text pattern
         * @param ii item index to check. If equal to -1, selected item is checked.
         * @param comparator specifies string comparision algorithm.
         */
	public JListByItemFinder(String lb, int ii, StringComparator comparator) {
	    label = lb;
	    itemIndex = ii;
	    this.comparator = comparator;
	}
        /**
         * Constructs JListByItemFinder.
         * @param lb a text pattern
         * @param ii item index to check. If equal to -1, selected item is checked.
         */
	public JListByItemFinder(String lb, int ii) {
            this(lb, ii, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JList) {
                if(label == null) {
                    return(true);
                }
		if(((JList)comp).getModel().getSize() > itemIndex) {
		    int ii = itemIndex;
		    if(ii == -1) {
                        ii = ((JList)comp).getSelectedIndex();
                        if(ii == -1) {
                            return(false);
                        }
		    }
		    return(comparator.equals(((JList)comp).getModel().getElementAt(ii).toString(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("JList with text \"" + label + "\" in " + 
		   (new Integer(itemIndex)).toString() + "'th item");
	}
    }

    /**
     * Checks component type.
     */
    public static class JListFinder extends Finder {
        /**
         * Constructs JListFinder.
         * @param sf other searching criteria.
         */
	public JListFinder(ComponentChooser sf) {
            super(JList.class, sf);
	}
        /**
         * Constructs JListFinder.
         */
	public JListFinder() {
            super(JList.class);
	}
    }
}
