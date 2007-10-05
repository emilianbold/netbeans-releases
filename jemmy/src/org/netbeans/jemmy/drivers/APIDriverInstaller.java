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

package org.netbeans.jemmy.drivers;

import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;

import org.netbeans.jemmy.drivers.buttons.ButtonMouseDriver;

import org.netbeans.jemmy.drivers.focus.APIFocusDriver;
import org.netbeans.jemmy.drivers.focus.MouseFocusDriver;

import org.netbeans.jemmy.drivers.lists.ChoiceDriver;
import org.netbeans.jemmy.drivers.lists.JComboMouseDriver;
import org.netbeans.jemmy.drivers.lists.JTableHeaderDriver;
import org.netbeans.jemmy.drivers.lists.JTabAPIDriver;
import org.netbeans.jemmy.drivers.lists.JListMouseDriver;
import org.netbeans.jemmy.drivers.lists.ListKeyboardDriver;
import org.netbeans.jemmy.drivers.menus.AppleMenuDriver;

import org.netbeans.jemmy.drivers.menus.DefaultJMenuDriver;
import org.netbeans.jemmy.drivers.menus.QueueJMenuDriver;

import org.netbeans.jemmy.drivers.scrolling.JScrollBarAPIDriver;
import org.netbeans.jemmy.drivers.scrolling.JSliderAPIDriver;
import org.netbeans.jemmy.drivers.scrolling.JSplitPaneDriver;
import org.netbeans.jemmy.drivers.scrolling.ScrollbarDriver;
import org.netbeans.jemmy.drivers.scrolling.ScrollPaneDriver;

import org.netbeans.jemmy.drivers.tables.JTableMouseDriver;

import org.netbeans.jemmy.drivers.trees.JTreeAPIDriver;

import org.netbeans.jemmy.drivers.text.AWTTextKeyboardDriver;
import org.netbeans.jemmy.drivers.text.SwingTextKeyboardDriver;

import org.netbeans.jemmy.drivers.windows.DefaultFrameDriver;
import org.netbeans.jemmy.drivers.windows.DefaultInternalFrameDriver;
import org.netbeans.jemmy.drivers.windows.DefaultWindowDriver;

/**
 * Installs all necessary drivers for Jemmy operators except
 * low-level drivers which are installed by 
 * <a href="InputDriverInstaller.java">InputDriverInstaller</a>.
 * 
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */

public class APIDriverInstaller extends ArrayDriverInstaller {

    /**
     * Constructs a DefaultDriverInstaller object.
     * @param shortcutEvents Signals whether shortcut mode is used.
     */
    public APIDriverInstaller(boolean shortcutEvents) {
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
	      DriverManager.MENU_DRIVER_ID,
	      DriverManager.ORDEREDLIST_DRIVER_ID},
	      new Object[] {
	      new JTreeAPIDriver(),
              new JTreeAPIDriver(),
	      new JTreeAPIDriver(),
	      new AWTTextKeyboardDriver(),
	      new SwingTextKeyboardDriver(),
	      new ScrollbarDriver(),
	      new ScrollPaneDriver(),
	      new JScrollBarAPIDriver(),
	      new JSplitPaneDriver(),
	      new JSliderAPIDriver(),
	      createSpinnerDriver(),
	      new ButtonMouseDriver(),
	      new JTabAPIDriver(),
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
              ((System.getProperty("apple.laf.useScreenMenuBar") != null && 
                      System.getProperty("apple.laf.useScreenMenuBar").equals("true")) ? new AppleMenuDriver() :
                  (shortcutEvents ? ((Object)new QueueJMenuDriver()) : ((Object)new DefaultJMenuDriver()))),
	      new JTableHeaderDriver()});
    }

    /**
     * Constructs a DefaultDriverInstaller object with shortcut mode flag
     * taken from <code>JemmyProperties</code>.
     */
    public APIDriverInstaller() {
        this((JemmyProperties.getCurrentDispatchingModel() &
              JemmyProperties.SHORTCUT_MODEL_MASK) != 0);
    }

    private static LightDriver createSpinnerDriver() {
        if(System.getProperty("java.specification.version").compareTo("1.3") > 0) {
            try {
                return((LightDriver)new ClassReference("org.netbeans.jemmy.drivers.scrolling.JSpinnerDriver").
                       newInstance(null, null));
            } catch(ClassNotFoundException e) {
                JemmyProperties.getCurrentOutput().
                    printErrLine("ATTENTION! you are using Jemmy built by Java earlier then 1.4, under " +
                                 "Java 1.4. \nImpossible to create JSpinnerDriver");
                return(createEmptyDriver());
            } catch(Exception e) {
                throw(new JemmyException("Impossible to create JSpinnerDriver although java version is " +
                                         System.getProperty("java.version"),
                                         e));
            }
        } else {
            return(createEmptyDriver());
        }
    }

    private static LightDriver createEmptyDriver() {
        return(new LightDriver() {
                public String[] getSupported() {
                    return(new String[] {Object.class.getName()});
                }});
    }
}
