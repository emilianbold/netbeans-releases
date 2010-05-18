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
 * Utilities_RAVE.java
 *
 * Created on August 24, 2004, 10:58 AM
 */

package org.netbeans.modules.visualweb.extension.openide.util;


import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.util.HashSet;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import org.openide.ErrorManager;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 *
 * @author  Peter Zavadsky
 */
public final class Utilities_RAVE {


    /** Creates a new instance of Utilities_RAVE */
    private Utilities_RAVE() {
    }



// <rave>
    /** Showing/hiding busy cursor, before this funcionality was in Rave winsys,
     * the code is copied from that module.
     * It needs to be called from event-dispatching thread to work synch,
     * otherwise it is scheduled into that thread. */
    public static void showBusyCursor(final boolean busy) {
        if(SwingUtilities.isEventDispatchThread()) {
            doShowBusyCursor(busy);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doShowBusyCursor(busy);
                }
            });
        }
    }

    private static void doShowBusyCursor(boolean busy) {
        JFrame mainWindow = (JFrame)WindowManager.getDefault().getMainWindow();
        if(busy){
            RepaintManager.currentManager(mainWindow).paintDirtyRegions();
            mainWindow.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            mainWindow.getGlassPane().setVisible(true);
            mainWindow.repaint();
        } else {
            mainWindow.getGlassPane().setVisible(false);
            mainWindow.getGlassPane().setCursor(null);
            mainWindow.repaint();
        }
    }
// </rave>

// <RAVE>
// Some rave code uses this method (designer, navigation),
// revise.. and try to get rid of it.
    public static javax.swing.JPopupMenu actionsToPopup (
        Action[] actions, Component component,
        javax.swing.JPopupMenu menu
    ) {
        // TEMP>> The next lines are copied from #atcionsToPopup(Action[], Component);
        Lookup context = null;
        for (Component c = component; c != null; c = c.getParent()) {
            if (c instanceof Lookup.Provider) {
                context = ((Lookup.Provider)c).getLookup ();
                if (context != null) {
                    break;
                }
            }
        }

        if(context == null) {
            // Fallback to composite action map, even it is questionable,
            // whether we should support component which is not (nor
            // none of its parents) lookup provider.
            Object map = createUtilitiesCompositeMap(component);
            if(map != null) {
                context = org.openide.util.lookup.Lookups.singleton(map);
            } else {
                // If the above fails, use the copy as a fallback.
                context = org.openide.util.lookup.Lookups.singleton(new UtilitiesCompositeActionMap(component));
            }
        }

        // TEMP>> The next lines are copied from #atcionsToPopup(Action[], Lookup);
        // keeps actions for which was menu item created already
        HashSet counted = new HashSet ();
        boolean canSep = false;
        for (int i = 0; i < actions.length; i++) {
            boolean addSep = true;

            Action action = actions[i];

            if (action != null) {
                // if this action has menu item already, skip to next iteration
                if (counted.contains (action))
                    continue;

                counted.add (action);

                // switch to replacement action if there is some
                if(action instanceof ContextAwareAction) {
                    action = ((ContextAwareAction)action).createContextAwareInstance(context);
                }

                addSep = false;
                canSep = true;
                javax.swing.JMenuItem item;
                if (action instanceof org.openide.util.actions.Presenter.Popup) {
                    item = ((org.openide.util.actions.Presenter.Popup)action).getPopupPresenter ();
                    if (item == null) {
                        NullPointerException npe = new NullPointerException(
                            "findContextMenuImpl, getPopupPresenter returning null for " + action); // NOI18N
                        ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, npe);
                    }
                    menu.add (item);
                } else {
                    // We need to correctly handle mnemonics with '&' etc.
                    // TODO: Since org.netbeans.modules.openide.util.AWTBridge is not a public API, using the default
                    // implementation instead; this may not handle '&' correctly
                    JMenuItem mi = new javax.swing.JMenuItem (action);
                    menu.add(mi);
                }
            }

            if (addSep && canSep) {
                menu.addSeparator ();
                canSep = false;
            }
        }
        
        return menu;
    }
    
    private static Object createUtilitiesCompositeMap(Component component) {
        try {
            // XXX Reflection usage, again better then modifying NB code.
            ClassLoader cl = org.openide.util.Utilities.class.getClassLoader();
            Class utilitiesCompositeMapClass = Class.forName("org.openide.util.UtilitiesCompositeMap", true, cl);
            java.lang.reflect.Constructor constructor = utilitiesCompositeMapClass.getDeclaredConstructor(new Class[] {Component.class});
            constructor.setAccessible(true);
            return constructor.newInstance(new Object[] {component});
        } catch(ClassNotFoundException cnfe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, cnfe);
        } catch(NoSuchMethodException nsme) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nsme);
        } catch(InstantiationException ie) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ie);
        } catch(IllegalAccessException iae) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, iae);
        } catch(java.lang.reflect.InvocationTargetException ite) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ite);
        }
        
        return null;
    }
// </RAVE>

    // Copy of openide/src/org/openide/util/UtilitiesCompositeActionMap as a fallback.
    private static class UtilitiesCompositeActionMap extends ActionMap {
        private Component component;

        public UtilitiesCompositeActionMap(Component c) {
            this.component = c;
        }

        public int size() {
            return keys ().length;
        }

        public Action get(Object key) {
            Component c = component;
            for (;;) {
                if (c instanceof JComponent) {
                    javax.swing.ActionMap m = ((JComponent)c).getActionMap ();
                    if (m != null) {
                        Action a = m.get (key);
                        if (a != null) {
                            return a;
                        }
                    }
                }

                if (c instanceof Lookup.Provider) {
                    break;
                }

                c = c.getParent();

                if (c == null) {
                    break;
                }
            }

            return null;
        }

        public Object[] allKeys() {
            return keys (true);
        }

        public Object[] keys() {
            return keys (false);
        }


        private Object[] keys(boolean all) {
            java.util.HashSet keys = new java.util.HashSet ();

            Component c = component;
            for (;;) {
                if (c instanceof JComponent) {
                    javax.swing.ActionMap m = ((JComponent)c).getActionMap ();
                    if (m != null) {
                        java.util.List l;

                        if (all) {
                            l = java.util.Arrays.asList (m.allKeys ());
                        } else {
                            l = java.util.Arrays.asList (m.keys ());
                        }

                        keys.addAll (l);
                    }
                }

                if (c instanceof Lookup.Provider) {
                    break;
                }

                c = c.getParent();

                if (c == null) {
                    break;
                }
            }

            return keys.toArray ();
        }

        // 
        // Not implemented
        //

        public void remove(Object key) {
        }        

        public void setParent(ActionMap map) {
        }

        public void clear() {
        }

        public void put(Object key, Action action) {
        }

        public ActionMap getParent() {
            return null;
        }

    } // End of UtilitiesCompositeActionMap.

}
