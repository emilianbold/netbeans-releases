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

package org.netbeans.jemmy.drivers;

import org.netbeans.jemmy.JemmyProperties;

import org.netbeans.jemmy.drivers.buttons.ButtonMouseDriver;

import org.netbeans.jemmy.drivers.focus.APIFocusDriver;
import org.netbeans.jemmy.drivers.focus.MouseFocusDriver;

import org.netbeans.jemmy.drivers.lists.ChoiceDriver;
import org.netbeans.jemmy.drivers.lists.JComboMouseDriver;
import org.netbeans.jemmy.drivers.lists.JTableHeaderDriver;
import org.netbeans.jemmy.drivers.lists.JTabMouseDriver;
import org.netbeans.jemmy.drivers.lists.JListMouseDriver;
import org.netbeans.jemmy.drivers.lists.ListKeyboardDriver;

import org.netbeans.jemmy.drivers.menus.DefaultJMenuDriver;
import org.netbeans.jemmy.drivers.menus.QueueJMenuDriver;

import org.netbeans.jemmy.drivers.scrolling.JScrollBarDriver;
import org.netbeans.jemmy.drivers.scrolling.ScrollbarDriver;
import org.netbeans.jemmy.drivers.scrolling.ScrollPaneDriver;
import org.netbeans.jemmy.drivers.scrolling.JSplitPaneDriver;

import org.netbeans.jemmy.drivers.tables.JTableMouseDriver;

import org.netbeans.jemmy.drivers.trees.JTreeMouseDriver;

import org.netbeans.jemmy.drivers.text.AWTTextKeyboardDriver;
import org.netbeans.jemmy.drivers.text.SwingTextKeyboardDriver;

import org.netbeans.jemmy.drivers.windows.DefaultFrameDriver;
import org.netbeans.jemmy.drivers.windows.DefaultInternalFrameDriver;
import org.netbeans.jemmy.drivers.windows.DefaultWindowDriver;

public class DefaultDriverInstaller extends ArrayDriverInstaller {
    public DefaultDriverInstaller(boolean shortcutEvents) {
	super(new String[] {
	      DriverManager.LIST_DRIVER_ID,
	      DriverManager.MULTISELLIST_DRIVER_ID,
	      DriverManager.TREE_DRIVER_ID,
	      DriverManager.TEXT_DRIVER_ID,
	      DriverManager.TEXT_DRIVER_ID,
	      DriverManager.SCROLL_DRIVER_ID,
	      DriverManager.SCROLL_DRIVER_ID,
	      DriverManager.SCROLL_DRIVER_ID,
	      DriverManager.SCROLL_DRIVER_ID,
	      DriverManager.BUTTON_DRIVER_ID,
	      DriverManager.LIST_DRIVER_ID,
	      DriverManager.LIST_DRIVER_ID,
	      DriverManager.MULTISELLIST_DRIVER_ID,
	      DriverManager.LIST_DRIVER_ID,
	      DriverManager.LIST_DRIVER_ID,
	      DriverManager.MULTISELLIST_DRIVER_ID,
	      DriverManager.TABLE_DRIVER_ID,
	      DriverManager.LIST_DRIVER_ID,
	      DriverManager.FRAME_DRIVER_ID,
	      DriverManager.WINDOW_DRIVER_ID,
	      DriverManager.FRAME_DRIVER_ID,
	      DriverManager.INTERNAL_FRAME_DRIVER_ID,
	      DriverManager.WINDOW_DRIVER_ID,
	      DriverManager.FOCUS_DRIVER_ID,
	      DriverManager.FOCUS_DRIVER_ID,
	      DriverManager.MENU_DRIVER_ID,
	      DriverManager.ORDEREDLIST_DRIVER_ID},
	      new Object[] {
	      new JTreeMouseDriver(),
              new JTreeMouseDriver(),
	      new JTreeMouseDriver(),
	      new AWTTextKeyboardDriver(),
	      new SwingTextKeyboardDriver(),
	      new ScrollbarDriver(),
	      new ScrollPaneDriver(),
	      new JScrollBarDriver(),
	      new JSplitPaneDriver(),
	      new ButtonMouseDriver(),
	      new JTabMouseDriver(),
	      new ListKeyboardDriver(),
	      new ListKeyboardDriver(),
	      new JComboMouseDriver(),
	      new JListMouseDriver(),
	      new JListMouseDriver(),
	      new JTableMouseDriver(),
	      new ChoiceDriver(),
	      new DefaultFrameDriver(),
	      new DefaultWindowDriver(),
	      new DefaultInternalFrameDriver(),
	      new DefaultInternalFrameDriver(),
	      new DefaultInternalFrameDriver(),
	      new APIFocusDriver(),
	      new MouseFocusDriver(),
              (shortcutEvents ? ((Object)new QueueJMenuDriver()) : ((Object)new DefaultJMenuDriver())),
	      new JTableHeaderDriver()});
    }
    public DefaultDriverInstaller() {
        this((JemmyProperties.getCurrentDispatchingModel() &
              JemmyProperties.SHORTCUT_MODEL_MASK) != 0);
    }
}
