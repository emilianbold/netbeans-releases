/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */


/*
 * DiagramElementAction.java
 *
 * Created on February 10, 2005, 2:13 PM
 *
 * This Class is derived from jelly DiagramElementAction.java
 */

package org.netbeans.test.umllib.actions;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.JellyVersion;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.test.umllib.actions.Actionable;

/**
 * This is basic class for all diagram actions. Derived from 
 * corresponding jelly <CODE>Action</CODE> class
 * @author Alexei Mokeev
 */
public class DiagramElementAction {
    /** through menu action performing mode */
    public static final int MENU_MODE = 0;
    /** through popup menu action performing mode */
    public static final int POPUP_MODE = 1;
    /** through API action performing mode */
    public static final int API_MODE = 2;
    /** through shortcut action performing mode */
    public static final int SHORTCUT_MODE = 3;
    
    /** sleep time between elements selection and action execution */
    protected static final long SELECTION_WAIT_TIME = 300;
    /** sleep time after action execution */
    protected static final long AFTER_ACTION_WAIT_TIME = 0;
    /** sleep time between sequence of shortcuts */
    protected static final long WAIT_AFTER_SHORTCUT_TIMEOUT = 0;
    
    private static final int sequence[][] = {{MENU_MODE, POPUP_MODE, SHORTCUT_MODE, API_MODE},
    {POPUP_MODE, MENU_MODE, SHORTCUT_MODE, API_MODE},
    {API_MODE, POPUP_MODE, MENU_MODE, SHORTCUT_MODE},
    {SHORTCUT_MODE, POPUP_MODE, MENU_MODE, API_MODE}};
    
    /** menu path of current action or null when MENU_MODE is not supported */
    protected String menuPath;
    /** popup menu path of current action or null when POPUP_MODE is not supported */
    protected String popupPath;
    /** SystemDiagramElementAction class of current action or null when API_MODE is not supported */
    protected Class systemActionClass;
    /** array of shortcuts of current action or null when SHORTCUT_MODE is not supported */
    protected Shortcut[] shortcuts;
    
    /** Comparator used as default for all DiagramElementAction instances. It is set in static clause. */
    private static StringComparator defaultComparator;
    /** Comparator used for this action instance. */
    private StringComparator comparator;
    
    /** creates new DiagramElementAction instance without API_MODE and SHORTCUT_MODE support
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported) */
    public DiagramElementAction(String menuPath, String popupPath) {
        this(menuPath, popupPath, null, (Shortcut[])null);
    }
    
    /** creates new DiagramElementAction instance without SHORTCUT_MODE support
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemDiagramElementAction (use null value if API mode is not supported) */
    public DiagramElementAction(String menuPath, String popupPath, String systemActionClass) {
        this(menuPath, popupPath, systemActionClass, (Shortcut[])null);
    }
    
    /** creates new DiagramElementAction instance without API_MODE support
     * @param shortcuts array of Shortcut instances (use null value if shorcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported) */
    public DiagramElementAction(String menuPath, String popupPath, Shortcut[] shortcuts) {
        this(menuPath, popupPath, null, shortcuts);
    }
    
    /** creates new DiagramElementAction instance without API_MODE support
     * @param shortcut Shortcut (use null value if menu mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported) */
    public DiagramElementAction(String menuPath, String popupPath, Shortcut shortcut) {
        this(menuPath, popupPath, null, new Shortcut[] {shortcut});
    }
    
    /** creates new DiagramElementAction instance
     * @param shortcuts array of Shortcut instances (use null value if shortcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemDiagramElementAction (use null value if API mode is not supported) */
    public DiagramElementAction(String menuPath, String popupPath, String systemActionClass, Shortcut[] shortcuts) {
        this.menuPath = menuPath;
        this.popupPath = popupPath;
        if (systemActionClass==null) {
            this.systemActionClass = null;
        } else try {
            this.systemActionClass = Class.forName(systemActionClass);
        } catch (ClassNotFoundException e) {
            this.systemActionClass = null;
        }
        this.shortcuts = shortcuts;
    }
    
    /** creates new DiagramElementAction instance
     * @param shortcut Shortcut String (use null value if menu mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemDiagramElementAction (use null value if API mode is not supported) */
    public DiagramElementAction(String menuPath, String popupPath, String systemActionClass, Shortcut shortcut) {
        this(menuPath, popupPath, systemActionClass, new Shortcut[] {shortcut});
    }
    
    static {
        // Checks if you run on correct jemmy version. Writes message to jemmy log if not.
        JellyVersion.checkJemmyVersion();
        
        if (JemmyProperties.getCurrentProperty("DiagramElementAction.DefaultMode")==null)
            JemmyProperties.setCurrentProperty("DiagramElementAction.DefaultMode", new Integer(POPUP_MODE));
        Timeouts.initDefault("DiagramElementAction.WaitAfterShortcutTimeout", WAIT_AFTER_SHORTCUT_TIMEOUT);
        // Set case sensitive comparator as default because of
        // very often clash between Cut and Execute menu items.
        // Substring criterion is set according to default string comparator
        boolean compareExactly = !Operator.getDefaultStringComparator().equals("abc", "a"); // NOI18N
        defaultComparator = new DefaultStringComparator(compareExactly, true);
    }
    
    private void perform(int mode) {
        switch (mode) {
            // case POPUP_MODE: performPopup(); break;
            case MENU_MODE: performMenu(); break;
            //   case API_MODE: performAPI(); break;
            // case SHORTCUT_MODE: performShortcut(); break;
            default: throw new IllegalArgumentException("Wrong DiagramElementAction.MODE");
        }
    }
    /** Creates a new instance of DiagramElementAction */
    public DiagramElementAction() {
    }
    
    /** performs action through main menu
     * @throws UnsupportedOperationException when action does not support menu mode */
    public void performMenu() {
        if (menuPath==null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define menu path");
        }
        // Need to wait here to be more reliable.
        // TBD - It can be removed after issue 23663 is solved.
        new EventTool().waitNoEvent(500);
        MainWindowOperator.getDefault().menuBar().pushMenu(menuPath, "|", getComparator());
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }
    
    public void performMenu(Actionable element) {
        performMenu(new Actionable[]{element});
    }
    
    /** performs action through main menu
     * @param elements elements to be action performed on
     * @throws UnsupportedOperationException when action does not support shortcut mode */
    public void performMenu(Actionable[] elements) {
        if (menuPath==null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define menu path");
        }
        //Need to check that all of them are from the same diagram
        elements[0].select();
        
        for (int i=1; i<elements.length; i++) {
            elements[i].addToSelection();
            try {
                Thread.sleep(SELECTION_WAIT_TIME);
            } catch (Exception e) {
                throw new JemmyException("Sleeping interrupted", e);
            }
        }
        
        new EventTool().waitNoEvent(500);
        performMenu();
    }
    
    
    
    
    
    
    
    /** performs action through popup menu
     * @param element node to be action performed on
     * @throws UnsupportedOperationException when action does not support popup mode */
    public void performPopup(Actionable element) {
        performPopup(new Actionable[]{element});
    }
    
    
    
    
    /** performs action through popup menu
     * @param elements elements to be action performed on
     * @throws UnsupportedOperationException when action does not support popup mode */
    public void performPopup(Actionable[] elements) {
        if (popupPath==null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define popup path");
        }
        //Need to check that all of them are from the same diagram
        elements[0].select();
        
        for (int i=1; i<elements.length; i++) {
            elements[i].addToSelection();
            try {
                Thread.sleep(SELECTION_WAIT_TIME);
            } catch (Exception e) {
                throw new JemmyException("Sleeping interrupted", e);
            }
        }
        
        
        new EventTool().waitNoEvent(500);
        
        JPopupMenuOperator popup = elements[0].getPopup();
        popup.pushMenu(popupPath, "|", getComparator());
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        
    }
    /** performs action through popup menu
     * @param element node to be action performed on
     * @throws UnsupportedOperationException when action does not support popup mode */
    public void performPopupNoBlock(Actionable element) {
        performPopupNoBlock(new Actionable[]{element});
    }
    
    
    
    
    /** performs action through popup menu
     * @param elements elements to be action performed on
     * @throws UnsupportedOperationException when action does not support popup mode */
    public void performPopupNoBlock(Actionable[] elements) {
        if (popupPath==null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define popup path");
        }
        //Need to check that all of them are from the same diagram
        elements[0].select();
        
        for (int i=1; i<elements.length; i++) {
            elements[i].addToSelection();
            try {
                Thread.sleep(SELECTION_WAIT_TIME);
            } catch (Exception e) {
                throw new JemmyException("Sleeping interrupted", e);
            }
        }
        
        
        new EventTool().waitNoEvent(500);
        
        JPopupMenuOperator popup = elements[0].getPopup();
        popup.pushMenuNoBlock(popupPath, "|", getComparator());
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        
    }
    
    
    
    /** performs action through shortcut
     * @throws UnsupportedOperationException if no shortcut is defined */
    public void performShortcut() {
        if (shortcuts == null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define shortcut");
        }
        for(int i=0; i<shortcuts.length; i++) {
            new KeyRobotDriver(new Timeout("autoDelay",100)).pushKey(null, shortcuts[i].getKeyCode(), shortcuts[i].getKeyModifiers(), JemmyProperties.getCurrentTimeouts().create("ComponentOperator.PushKeyTimeout"));
            JemmyProperties.getProperties().getTimeouts().sleep("DiagramElementAction.WaitAfterShortcutTimeout");
        }
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }
    
    /** performs action through shortcut
     * @param element node to be action performed on
     * @throws UnsupportedOperationException when action does not support shortcut mode */
    public void performShortcut(Actionable element) {
        performShortcut(new Actionable[]{element});
    }
    
    /** performs action through shortcut
     * @param elements elements to be action performed on
     * @throws UnsupportedOperationException when action does not support shortcut mode */
    public void performShortcut(Actionable[] elements) {
        if (shortcuts == null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define shortcut");
        }
        //Need to check that all of them are from the same diagram
        elements[0].select();
        
        for (int i=1; i<elements.length; i++) {
            elements[i].addToSelection();
            try {
                Thread.sleep(SELECTION_WAIT_TIME);
            } catch (Exception e) {
                throw new JemmyException("Sleeping interrupted", e);
            }
        }
        
        new EventTool().waitNoEvent(500);
        performShortcut();
    }
    
    
    
    
    
    /** Sets comparator fot this action. Comparator is used for all actions
     * after this method is called.
     * @param comparator new comparator to be set (e.g.
     *                   new Operator.DefaultStringComparator(true, true);
     *                   to search string item exactly and case sensitive)
     */
    public void setComparator(StringComparator comparator) {
        this.comparator = comparator;
    }
    
    /** Gets comparator set for this action instance.
     * @return comparator set for this action instance.
     */
    public StringComparator getComparator() {
        if(comparator == null) {
            comparator = defaultComparator;
        }
        return comparator;
    }
}
