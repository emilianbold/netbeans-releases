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
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyInputException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.MultiSelListDriver;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.List;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import java.util.Hashtable;
import java.util.Vector;

/**
 * <BR><BR>Timeouts used: <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait component enabled <BR>.
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class ListOperator extends ComponentOperator
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
    public ListOperator(List b) {
	super(b);
	driver = DriverManager.getMultiSelListDriver(getClass());
    }

    /**
     * Constructs a ListOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public ListOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((List)cont.
             waitSubComponent(new ListFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a ListOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public ListOperator(ContainerOperator cont, ComponentChooser chooser) {
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
     * @throws TimeoutExpiredException
     */
    public ListOperator(ContainerOperator cont, String text, int itemIndex, int index) {
	this((List)waitComponent(cont, 
				  new ListByItemFinder(text, itemIndex,
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
     * @throws TimeoutExpiredException
     */
    public ListOperator(ContainerOperator cont, String text, int index) {
	this(cont, text, -1, index);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont a container
     * @param text Text of item which is currently selected. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public ListOperator(ContainerOperator cont, String text) {
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
    public ListOperator(ContainerOperator cont, int index) {
	this((List)
	     waitComponent(cont, 
			   new ListFinder(),
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
    public ListOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches List in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return List instance or null if component was not found.
     */
    public static List findList(Container cont, ComponentChooser chooser, int index) {
	return((List)findComponent(cont, new ListFinder(chooser), index));
    }

    /**
     * Searches 0'th List in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return List instance or null if component was not found.
     */
    public static List findList(Container cont, ComponentChooser chooser) {
	return(findList(cont, chooser, 0));
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

    private int findItemIndex(String item, StringComparator comparator, int index){
	int count = 0;
	for(int i = 0; i < getItemCount(); i++) {
	    if(comparator.equals(getItem(i), item)) {
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
     * Searches an item index. 
     * @param item item text.
     * @param index an ordinal index between appropriate ones.
     * @return an index.
     */
    public int findItemIndex(String item, int index){
	return(findItemIndex(item, getComparator(), index));
    }
    
    /**
     * Searches an item index. 
     * @param item item text.
     * @return an index.
     */
    public int findItemIndex(String item){
	return(findItemIndex(item, 0));
    }

    private void selectItem(String item, StringComparator comparator, int index) {
	selectItem(findItemIndex(item, comparator, index));
    }

    /**
     * Selects an item. 
     * @param item item text.
     * @param index an ordinal index between appropriate ones.
     */
    public void selectItem(String item, int index) {
	selectItem(item, getComparator(), index);
    }

    /**
     * Selects an item. 
     * @param item item text.
     */
    public void selectItem(String item) {
	selectItem(item, 0);
    }

    /**
     * Selects an item. 
     * @param index an item index.
     */
    public void selectItem(int index) {
	output.printLine("Select " + Integer.toString(index) + "`th item in list\n    : " +
			 toStringSource());
	output.printGolden("Select " + Integer.toString(index) + "`th item in list");
	driver.selectItem(this, index);
	if(getVerification()) {
            waitItemSelection(index, true);
        }
    }

    /**
     * Selects some items. 
     * @param from start selection index.
     * @param to end selection index.
     */
    public void selectItems(int from, int to) {
	output.printLine("Select items from " + Integer.toString(from) + 
			 "`th to " + Integer.toString(from) + "'th in list\n    : " +
			 toStringSource());
	output.printGolden("Select items from " + Integer.toString(from) + 
			 "`th to " + Integer.toString(from) + "'th");
	driver.selectItems(this, new int[] {from, to});
	if(getVerification()) {
            waitItemsSelection(from, to, true);
        }
    }
    
    /**
     * Waits for items to be selected.
     * @param from Start selection inex
     * @param to End selection inex
     * @param selected Selected (true) or unselected (false).
     */
    public void waitItemsSelection(final int from, final int to, final boolean selected) {
	getOutput().printLine("Wait items to be " +
			      (selected ? "" : "un") + "selected in component \n    : "+
			      toStringSource());
	getOutput().printGolden("Wait items to be " +
				(selected ? "" : "un") + "selected");
	waitState(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
                    int[] indices = getSelectedIndexes();
                    for(int i = 0; i < indices.length; i++) {
                        if(indices[i] < from ||
                           indices[i] > to) {
                            return(false);
                        }
                    }
                    return(true);
		}
		public String getDescription() {
		    return("Items has been " + 
			   (selected ? "" : "un") + "selected");
		}
	    });
    }

    /**
     * Waits for item to be selected.
     * @param itemIndex an item index to be selected.
     * @param selected Selected (true) or unselected (false).
     */
    public void waitItemSelection(final int itemIndex, final boolean selected) {
        waitItemsSelection(itemIndex, itemIndex, selected);
    }

    public Hashtable getDump() {
	Hashtable result = super.getDump();
	addToDump(result, ITEM_PREFIX_DPROP, ((List)getSource()).getItems());
	addToDump(result, SELECTED_ITEM_PREFIX_DPROP, ((List)getSource()).getSelectedItems());
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>List.addActionListener(ActionListener)</code> through queue*/
    public void addActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("addActionListener") {
		public void map() {
		    ((List)getSource()).addActionListener(actionListener);
		}});}

    /**Maps <code>List.addItemListener(ItemListener)</code> through queue*/
    public void addItemListener(final ItemListener itemListener) {
	runMapping(new MapVoidAction("addItemListener") {
		public void map() {
		    ((List)getSource()).addItemListener(itemListener);
		}});}

    /**Maps <code>List.deselect(int)</code> through queue*/
    public void deselect(final int i) {
	runMapping(new MapVoidAction("deselect") {
		public void map() {
		    ((List)getSource()).deselect(i);
		}});}

    /**Maps <code>List.getItem(int)</code> through queue*/
    public String getItem(final int i) {
	return((String)runMapping(new MapAction("getItem") {
		public Object map() {
		    return(((List)getSource()).getItem(i));
		}}));}

    /**Maps <code>List.getItemCount()</code> through queue*/
    public int getItemCount() {
	return(runMapping(new MapIntegerAction("getItemCount") {
		public int map() {
		    return(((List)getSource()).getItemCount());
		}}));}

    /**Maps <code>List.getItems()</code> through queue*/
    public String[] getItems() {
	return((String[])runMapping(new MapAction("getItems") {
		public Object map() {
		    return(((List)getSource()).getItems());
		}}));}

    /**Maps <code>List.getMinimumSize(int)</code> through queue*/
    public Dimension getMinimumSize(final int i) {
	return((Dimension)runMapping(new MapAction("getMinimumSize") {
		public Object map() {
		    return(((List)getSource()).getMinimumSize(i));
		}}));}

    /**Maps <code>List.getPreferredSize(int)</code> through queue*/
    public Dimension getPreferredSize(final int i) {
	return((Dimension)runMapping(new MapAction("getPreferredSize") {
		public Object map() {
		    return(((List)getSource()).getPreferredSize(i));
		}}));}

    /**Maps <code>List.getRows()</code> through queue*/
    public int getRows() {
	return(runMapping(new MapIntegerAction("getRows") {
		public int map() {
		    return(((List)getSource()).getRows());
		}}));}

    /**Maps <code>List.getSelectedIndex()</code> through queue*/
    public int getSelectedIndex() {
	return(runMapping(new MapIntegerAction("getSelectedIndex") {
		public int map() {
		    return(((List)getSource()).getSelectedIndex());
		}}));}

    /**Maps <code>List.getSelectedIndexes()</code> through queue*/
    public int[] getSelectedIndexes() {
	return((int[])runMapping(new MapAction("getSelectedIndexes") {
		public Object map() {
		    return(((List)getSource()).getSelectedIndexes());
		}}));}

    /**Maps <code>List.getSelectedItem()</code> through queue*/
    public String getSelectedItem() {
	return((String)runMapping(new MapAction("getSelectedItem") {
		public Object map() {
		    return(((List)getSource()).getSelectedItem());
		}}));}

    /**Maps <code>List.getSelectedItems()</code> through queue*/
    public String[] getSelectedItems() {
	return((String[])runMapping(new MapAction("getSelectedItems") {
		public Object map() {
		    return(((List)getSource()).getSelectedItems());
		}}));}

    /**Maps <code>List.getSelectedObjects()</code> through queue*/
    public Object[] getSelectedObjects() {
	return((Object[])runMapping(new MapAction("getSelectedObjects") {
		public Object map() {
		    return(((List)getSource()).getSelectedObjects());
		}}));}

    /**Maps <code>List.getVisibleIndex()</code> through queue*/
    public int getVisibleIndex() {
	return(runMapping(new MapIntegerAction("getVisibleIndex") {
		public int map() {
		    return(((List)getSource()).getVisibleIndex());
		}}));}

    /**Maps <code>List.isIndexSelected(int)</code> through queue*/
    public boolean isIndexSelected(final int i) {
	return(runMapping(new MapBooleanAction("isIndexSelected") {
		public boolean map() {
		    return(((List)getSource()).isIndexSelected(i));
		}}));}

    /**Maps <code>List.isMultipleMode()</code> through queue*/
    public boolean isMultipleMode() {
	return(runMapping(new MapBooleanAction("isMultipleMode") {
		public boolean map() {
		    return(((List)getSource()).isMultipleMode());
		}}));}

    /**Maps <code>List.makeVisible(int)</code> through queue*/
    public void makeVisible(final int i) {
	runMapping(new MapVoidAction("makeVisible") {
		public void map() {
		    ((List)getSource()).makeVisible(i);
		}});}

    /**Maps <code>List.remove(int)</code> through queue*/
    public void remove(final int i) {
	runMapping(new MapVoidAction("remove") {
		public void map() {
		    ((List)getSource()).remove(i);
		}});}

    /**Maps <code>List.remove(String)</code> through queue*/
    public void remove(final String string) {
	runMapping(new MapVoidAction("remove") {
		public void map() {
		    ((List)getSource()).remove(string);
		}});}

    /**Maps <code>List.removeActionListener(ActionListener)</code> through queue*/
    public void removeActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("removeActionListener") {
		public void map() {
		    ((List)getSource()).removeActionListener(actionListener);
		}});}

    /**Maps <code>List.removeAll()</code> through queue*/
    public void removeAll() {
	runMapping(new MapVoidAction("removeAll") {
		public void map() {
		    ((List)getSource()).removeAll();
		}});}

    /**Maps <code>List.removeItemListener(ItemListener)</code> through queue*/
    public void removeItemListener(final ItemListener itemListener) {
	runMapping(new MapVoidAction("removeItemListener") {
		public void map() {
		    ((List)getSource()).removeItemListener(itemListener);
		}});}

    /**Maps <code>List.replaceItem(String, int)</code> through queue*/
    public void replaceItem(final String string, final int i) {
	runMapping(new MapVoidAction("replaceItem") {
		public void map() {
		    ((List)getSource()).replaceItem(string, i);
		}});}

    /**Maps <code>List.select(int)</code> through queue*/
    public void select(final int i) {
	runMapping(new MapVoidAction("select") {
		public void map() {
		    ((List)getSource()).select(i);
		}});}

    /**Maps <code>List.setMultipleMode(boolean)</code> through queue*/
    public void setMultipleMode(final boolean b) {
	runMapping(new MapVoidAction("setMultipleMode") {
		public void map() {
		    ((List)getSource()).setMultipleMode(b);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Allows to find component by item text.
     */
    public static class ListByItemFinder implements ComponentChooser {
	String label;
	int itemIndex;
	StringComparator comparator;
        /**
         * Constructs ListByItemFinder.
         * @param lb a text pattern
         * @param ii item index to check. If equal to -1, selected item is checked.
         * @param comparator specifies string comparision algorithm.
         */
	public ListByItemFinder(String lb, int ii, StringComparator comparator) {
	    label = lb;
	    itemIndex = ii;
	    this.comparator = comparator;
	}
        /**
         * Constructs ListByItemFinder.
         * @param lb a text pattern
         * @param ii item index to check. If equal to -1, selected item is checked.
         */
	public ListByItemFinder(String lb, int ii) {
            this(lb, ii, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof List) {
		if(label == null) {
		    return(true);
		}
		if(((List)comp).getItemCount() > itemIndex) {
		    int ii = itemIndex;
		    if(ii == -1) {
			ii = ((List)comp).getSelectedIndex();
			if(ii == -1) {
			    return(false);
			}
		    }
		    return(comparator.equals(((List)comp).getItem(ii),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("List with text \"" + label + "\" in " + 
		   (new Integer(itemIndex)).toString() + "'th item");
	}
    }

    /**
     * Checks component type.
     */
    public static class ListFinder extends Finder {
        /**
         * Constructs ListFinder.
         * @param sf other searching criteria.
         */
	public ListFinder(ComponentChooser sf) {
            super(List.class, sf);
	}
        /**
         * Constructs ListFinder.
         */
	public ListFinder() {
            super(List.class);
	}
    }
}
