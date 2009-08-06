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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.jellytools.actions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JSplitPane;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.openide.util.Exceptions;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** Used to attach a window to a new position by IDE API.
 * It also defines constants used for attaching.
 * <p>
 * Usage:<br>
 * <pre>
        TopComponentOperator tco = new TopComponentOperator("Runtime");
        // attach Runtime top component right to Execution View
        new AttachWindowAction("Execution View", AttachWindowAction.RIGHT).perform(tco);
        Thread.sleep(2000);
        // attach Runtime top component back (next to Filesystems as the last tab)
        new AttachWindowAction("Filesystems", AttachWindowAction.AS_LAST_TAB).perform(tco);
 * </pre>
 * @see Action
 * @see org.netbeans.jellytools.TopComponentOperator
 * @author Jiri.Skrivanek@sun.com
 */
public class AttachWindowAction extends Action {
    
    public static final String TOP    = JSplitPane.TOP;
    public static final String BOTTOM = JSplitPane.BOTTOM;
    public static final String LEFT   = JSplitPane.LEFT;
    public static final String RIGHT  = JSplitPane.RIGHT;
    public static final String AS_LAST_TAB = "As a Last Tab";
    
    /** "Window" main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle",
                                                                     "Menu/Window");

    /** Parameter used in API operations. */
    private String sideConstant;

    // at least one of variables is defined in constructor
    private String targetTopComponentName;
    private TopComponentOperator targetTopComponentOperator;
    
    /** Create new AttachWindowAction instance.
     * @param targetTopComponentName name of top component defining a position
     * where to attach top component
     * @param side side where to attach top component ({@link #LEFT}, 
     * {@link #RIGHT}, {@link #TOP}, {@link #BOTTOM}, {@link #AS_LAST_TAB})
     */
    public AttachWindowAction(String targetTopComponentName, String side) {
        super(null, null);
        if(targetTopComponentName == null) {
            throw new IllegalArgumentException("targetTopComponentName cannot be null.");
        }
        this.targetTopComponentName = targetTopComponentName;
        mapSide(side);
    }
    
    /** Create new AttachWindowAction instance.
     * @param targetTopComponentOperator operator of top component defining a position
     * where to attach top component
     * @param side side where to attach top component ({@link #LEFT}, 
     * {@link #RIGHT}, {@link #TOP}, {@link #BOTTOM}, {@link #AS_LAST_TAB})
     */
    public AttachWindowAction(TopComponentOperator targetTopComponentOperator, String side) {
        super(null, null);
        if(targetTopComponentOperator == null) {
            throw new IllegalArgumentException("targetTopComponentOperator cannot be null.");
        }
        this.targetTopComponentOperator = targetTopComponentOperator;
        mapSide(side);
    }
    
    private String getTargetTopComponentName() {
        if(targetTopComponentName == null) {
            // it is guaranteed targetTopComponentOperator != null
            targetTopComponentName = targetTopComponentOperator.getName();
        }
        return targetTopComponentName;
    }

    private TopComponentOperator getTargetTopComponentOperator() {
        if(targetTopComponentOperator == null) {
            // it is guaranteed targetTopComponentName != null
            targetTopComponentOperator = new TopComponentOperator(targetTopComponentName);
        }
        return targetTopComponentOperator;
    }

    /** Set sideItem and sideConstant from given parameter.
     */
    private void mapSide(String side) {
        if(side == null || side.equals(AS_LAST_TAB)) {
            sideConstant = AS_LAST_TAB;
        } else if(side.equals(RIGHT)) {
            sideConstant = RIGHT;
        } else if(side.equals(LEFT)) {
            sideConstant = LEFT;
        } else if(side.equals(TOP)) {
            sideConstant = TOP;
        } else if(side.equals(BOTTOM)) {
            sideConstant = BOTTOM;
        } else {
            throw new JemmyException("Cannot attach to position \""+side+"\".");
        }
    }

    /** Perform popup action on given TopComponent's tab.
     * @param tco TopComponentOperator instance on which perform
     * the action
     */
    /* Currently not enabled on top components tab. 
    public void performPopup(TopComponentOperator tco) {
        // TODO no idea what this empty todo means
        // Attach "SourceTC" to|taargetTC|Top"
        String popupPath = Bundle.getString("org.netbeans.core.windows.actions.Bundle",
        "CTL_AttachWindowAction", new String[] {tco.getName()});
        menuPath += "|"+getTargetTopComponentName()+"|"+sideItem;
        getTargetTopComponentOperator().makeComponentVisible();
        tco.pushMenuOnTab(popupPath);
    }
     */

    /** Attach given TopComponentOperator to position specified in constructor
     * of action. It also waints until given TopComponent is showing in the 
     * new position.
     * @param compOperator TopComponentOperator which should be attached to desired
     * position
     */
    public void performAPI(ComponentOperator compOperator) {
        if(compOperator instanceof TopComponentOperator) {
            performAPI((TopComponentOperator)compOperator);
        } else {
            throw new UnsupportedOperationException(
                "AttachWindowAction can only be called on TopComponentOperator.");
        }
    }
        
    /** Attaches given TopComponentOperator to position specified in constructor
     * of action. It also waints until given TopComponent is showing in the 
     * new position.
     * @param tco TopComponentOperator which should be attached to desired
     * position
     */
    public void performAPI(TopComponentOperator tco) {
        final TopComponent sourceTc = (TopComponent)tco.getSource();
        final TopComponent targetTc = (TopComponent)getTargetTopComponentOperator().getSource();
        // run in dispatch thread
        tco.getQueueTool().invokeSmoothly(new Runnable() {
            public void run() {
                Mode mode = (Mode)WindowManager.getDefault().findMode(targetTc);
                if(sideConstant == AS_LAST_TAB) {
                    mode.dockInto(sourceTc);
                    sourceTc.open();
                    sourceTc.requestActive();
                } else {
                    callWindowManager("attachTopComponentToSide", sourceTc, mode, sideConstant);
                }
            }
        });
        
        // wait until TopComponent is in new location, i.e. is showing
        try {
            new Waiter(new Waitable() {
                public Object actionProduced(Object tc) {
                    return ((TopComponent)tc).isShowing() ? Boolean.TRUE : null;
                }
                public String getDescription() {
                    return("TopComponent is showing."); // NOI18N
                }
            }).waitAction(sourceTc);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e); // NOI18N
        }
    }

    static Object callWindowManager(String method, Object... args) {
        return callWindowManager(WindowManager.getDefault().getClass(), method, args);
    }

    private static Object callWindowManager(Class clazz, String method, Object... args) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (!method.equals(m.getName())) {
                continue;
            }
            if (args == null && m.getParameterTypes().length > 0 ||
                    args != null && m.getParameterTypes().length != args.length) {
                continue;
            }
            try {
                return m.invoke(WindowManager.getDefault(), args);
            } catch (Exception ex) {
                throw (IllegalStateException)new IllegalStateException("Cannot execute " + method).initCause(ex);
            }
        }
        return callWindowManager(clazz.getSuperclass(), method, args);
    }


    
    /** Throws UnsupportedOperationException because AttachWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
            "AttachWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because AttachWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
            "AttachWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because AttachWindowAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
            "AttachWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because AttachWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
            "AttachWindowAction doesn't have popup representation on nodes.");
    }
}
