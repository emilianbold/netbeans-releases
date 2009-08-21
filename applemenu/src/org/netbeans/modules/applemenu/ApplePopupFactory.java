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

package org.netbeans.modules.applemenu;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import org.openide.ErrorManager;



/**
 * Heavyweight popups created using PopupFactory are on mac-os will have a 
 * drop-shadow, which is exactly what we don't want for explorer tooltips that
 * are supposed to look like a seamless part of the component they appear
 * over.
 *
 * This class decides accurately if a heavyweight popup is needed, and if it
 * is, uses a background color hack (which alas, only works about 60% of the
 * time) to attempt to eliminate the drop shadow on the window it uses.
 *
 * Caveats:  1.  Heavyweight popups from ViewTooltips are currently simply
 * disabled by default.  Enable them by setting the system property
 * "nb.explorer.hw.completions" to "true".  This only affects macintosh.
 * They are off by default because sometimes the popup window will have a
 * drop shadow no matter what we do.
 *
 * If using a macintosh and "nb.explorer.hw.completions" is "true" then,
 * "nb.explorer.hw.cocoahack" can also be set true.  If it is true one of
 * two things will happen:  1. If System/Library/Java is on NetBeans' classpath
 * (so the cocoa java classes are accessible), then we will have heavyweight
 * popups that have no drop shadow 100% of the time.  2.  If the cocoa java
 * classes cannot be loaded, the standard Swing PopupFactory will be used,
 * which means the drop-shadow problem will be visible.
 * 
 * @see org.openide.explorer.view.ViewTooltips
 *
 * @author Tim Boudreau
 */
@org.openide.util.lookup.ServiceProvider(service=javax.swing.PopupFactory.class)
public class ApplePopupFactory extends PopupFactory {
    private static final boolean APPLE_HEAVYWEIGHT = 
            Boolean.getBoolean ("nb.explorer.hw.completions"); //NOI18N
    
    private static final boolean APPLE_COCOA_HACK = APPLE_HEAVYWEIGHT &&
            Boolean.getBoolean ("nb.explorer.hw.cocoahack"); //NOI18N
    
    private static Set<Reference<JWindow>> windowPool = new HashSet<Reference<JWindow>>();
    
    //As is, the background color hack in this class works about 60% of
    //the time to get rid of the drop shadow on heavyweight popups, and
    //this class will reliably prefer a lightweight popup wherever possible,
    //which Apple's implementation doesn't.  So it is useful without the
    //egregious hack...it will just work 100% with it.
    //To be continued...
    public ApplePopupFactory() {
    }
    
    public Popup getPopup(Component owner, Component contents,
                          int x, int y) throws IllegalArgumentException {
        assert owner instanceof JComponent;
        Dimension d = contents.getPreferredSize();
        Container c = ((JComponent) owner).getTopLevelAncestor();
        if (c == null) {
            throw new IllegalArgumentException ("Not onscreen: " + owner);
        }
        Point p = new Point (x, y);
        SwingUtilities.convertPointFromScreen(p, c);
        Rectangle r = new Rectangle (p.x, p.y, d.width, d.height);
        if (c.getBounds().contains(r)) {
            //XXX need API to determine if editor area comp is heavyweight,
            //and if so, return a "medium weight" popup of a java.awt.Component
            //that embeds the passed contents component
            return new LWPopup (owner, contents, x, y);
        } else {
            return APPLE_HEAVYWEIGHT ? 
                (Popup) new HWPopup (owner, contents, x, y) :
                (Popup) new NullPopup();
        }
    }
    
    private static final class NullPopup extends Popup {
        public void show() {}
        public void hide() {}
    }
    
    private static abstract class OurPopup extends Popup {
        protected Component owner = null;
        protected Component contents = null;
        protected int x = -1;
        protected int y = -1;
        public OurPopup (Component owner, Component contents, int x, int y) {
            configure (owner, contents, x, y);
        }
        
        final void configure (Component owner, Component contents, int x, int y) {
            this.owner = owner;
            this.contents = contents;
            this.x = x;
            this.y = y;
        }
        
        protected abstract void prepareResources();
        protected abstract void doShow();
        public abstract boolean isShowing();
        protected abstract void doHide();
        
        public final void show() {
            prepareResources();
            doShow();
        }
        
        public final void hide() {
            doHide();
        }
        
        void dispose() {
            owner = null;
            contents = null;
            x = -1;
            y = -1;
        }
        
        private boolean canReuse = false;
        public final void clear() {
            canReuse = true;
            dispose();
        }
        
        boolean isInUse() {
            return canReuse;
        }
    }
    
    private static class LWPopup extends OurPopup {
        public LWPopup (Component owner, Component contents, int x, int y) {
            super (owner, contents, x, y);
        }

        private Rectangle bounds = null;
        protected void prepareResources() {
            JComponent jc = (JComponent) owner;
            Container w = jc.getTopLevelAncestor();
            JComponent pane = null;
            if (w instanceof JFrame) {
                pane = (JComponent) ((JFrame) w).getGlassPane();
            } else if (w instanceof JDialog) {
                pane = (JComponent) ((JDialog) w).getGlassPane();
            } else if (w instanceof JWindow) {
                pane = (JComponent) ((JWindow) w).getGlassPane();
            }
            if (w == null) {
                throw new IllegalArgumentException ("Not a JFrame/" + //NOI18N
                        "JWindow/JDialog: " + owner); //NOI18N
            }
            Point p = new Point (x, y);
            SwingUtilities.convertPointFromScreen(p, pane);
            if (pane.getLayout() != null) {
                pane.setLayout (null);
            }
            pane.setVisible(true);
            contents.setVisible (false);
            Dimension d = contents.getPreferredSize();
            pane.add (contents);
            bounds = new Rectangle (p.x, p.y, d.width, d.height);
            contents.setBounds (p.x, p.y, d.width, d.height);
        }
        
        protected void doShow() {
            contents.setVisible (true);
        }
        
        public boolean isShowing() {
            return contents != null && contents.isShowing();
        }
        
        protected void doHide() {
            Container parent = contents.getParent();
            if (parent != null) {
                contents.getParent().remove (contents);
                parent.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
            //If doShow() was never called, we've modified the visibility
            //of the contents component, which could cause problems elsewhere
            contents.setVisible (true);
        }
    }
    
    private static class HWPopup extends OurPopup {
        private JWindow window = null;
        public HWPopup (Component owner, Component contents, int x, int y) {
            super (owner, contents, x, y);
        }
        
        public boolean isShowing() {
            return window != null && window.isShowing();
        }
        
        void dispose() {
            if (window != null) {
                checkInWindow (window);
                window = null;
            }
            super.dispose();
        }
        
        protected void prepareResources() {
            window = checkOutWindow();
            window.getContentPane().add (contents);
            window.setLocation (new Point (x, y));
            window.pack();
            window.setBackground (new java.awt.Color (255, 255, 255, 0));
        }
        
        protected void doShow() {
            window.setVisible(true);
        }
        
        protected void doHide() {
            if (window != null) {
                window.setVisible(false);
                window.getContentPane().remove (contents);
                //Try to force a reset
                dispose();
            }
        }
    }
    
    private static JWindow checkOutWindow() {
        if (windowPool != null) {
            if (!windowPool.isEmpty()) {
                for (Iterator<Reference<JWindow>> i=windowPool.iterator(); i.hasNext();) {
                    Reference<JWindow> ref = i.next();
                    JWindow win = ref.get();
                    i.remove();
                    if (win != null) {
                        assert !win.isShowing();
                        win.setBounds (0, 0, 1, 1);
                        win.getContentPane().removeAll();
                        win.setBackground (new java.awt.Color (255, 255, 255, 0));
                        return win;
                    }
                }
            }
        }
        JWindow nue = APPLE_COCOA_HACK ? (JWindow) new HackedJWindow() : new JWindow();
        
        nue.setBackground (new java.awt.Color (255, 255, 255, 0));
        return nue;
    }
    
    private static void checkInWindow (JWindow win) {
        if (!APPLE_COCOA_HACK) {
            win.dispose();
        }
        windowPool.add (new SoftReference<JWindow> (win));
    }
    
    //A counter for unique window ids (used only if APPLE_COCOA_HACK is true)
    private static int ct = 0;  
    //A flag if our reflection-based hack doesn't work, so we don't try
    //again
    private static boolean hackBroken = false;
    //Make sure we've logged a warning
    private static boolean warned = false;  
    
    static boolean broken() {
        return hackBroken;
    }
    
    /**
     * A JWindow which can (maybe) look up the native cocoa window that
     * corresponds to it and hack its shadow property.  No guarantees
     * this will continue working, but once it fails it will fall back
     * gracefully.
     *
     * This class is ONLY used if both system properties, 
     * nb.explorer.hw.completions and nb.explorer.hw.cocoahack 
     * are intentionally set to true.  If the cocoa classes are not
     * available, it will log a warning and fail gracefully.
     *
     */
    private static final class HackedJWindow extends JWindow {
        private String title = "none";
        HackedJWindow() {}
        
        public void addNotify() {
            super.addNotify();
            hackTitle();
            hackNativeWindow();
        }
        
        private void hackTitle() {
            if (!hackBroken) {
                try {
                    //First we set a unique title on the peer - JWindow 
                    //doesn't have a title, but apple.awt.CWindow does.
                    //Later we will use it to identify the right window in
                    //the array of windows owned by the application.
                    //This ain't pretty.
                    @SuppressWarnings("deprecation")
                    Object o = getPeer();
                    if (o != null) {
                        Method m = o.getClass().getDeclaredMethod ("setTitle", 
                                new Class[] { String.class });
                        m.setAccessible(true);
                        title = "hw popup" + (ct++);
                        m.invoke (o, new Object[] { title });
                    }
                } catch (Exception e) {
                    warn(e);
                }
            }
        }
        
        private void hackNativeWindow() {
            if (!hackBroken) {
                try {
                    //First, lookup the global singleton NSApplication
                    Class<?> c = Class.forName ("com.apple.cocoa.application." +
                            "NSApplication");
                    
                    Method m = c.getDeclaredMethod ("sharedApplication");
                    Object nsapplication = m.invoke (null);
                    
                    //Now we'll get an NSArray array wrapper of NSWindow objects
                    m = nsapplication.getClass().getMethod ("windows");
                    Object nsarray_of_nswindows = m.invoke (nsapplication);
                    //Get the array size
                    m = nsarray_of_nswindows.getClass().getMethod("count");
                    int arrSize = ((Integer) m.invoke (nsarray_of_nswindows)).intValue();
                    
                    //Allocate an array to copy into
                    Object[] windows = new Object [arrSize];
                    m = nsarray_of_nswindows.getClass().getMethod(
                            "getObjects", new Class[] { Object[].class });
                            
                    //Gets us an NSWindow[]
                    m.invoke (nsarray_of_nswindows, new Object[] { windows });
                    if (windows.length > 0) {
                        //Lookup the methods we'll need first, to reduce
                        //overhead inside the loop
                        c = windows[0].getClass();
                        Method titleMethod = c.getMethod("title");
                        Method setHasShadowMethod = c.getMethod ("setHasShadow", 
                                new Class[] { Boolean.TYPE});
                                
                        for (int i=0; i < windows.length; i++) {
                            //Get the title
                            String ttl = (String) titleMethod.invoke (windows[i]);
                            
                            if (title.equals (ttl)) {
                                //We have the right method, set hasShadow to
                                //false
                                setHasShadowMethod.invoke (windows[i], 
                                        new Object[] { Boolean.FALSE });
                            }
                        }
                    }
                } catch (Exception e) {
                    warn(e);
                }
            }
        }
        
        private void warn(Exception e) {
            hackBroken = true;
            if (!warned) {
                warned = true;
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                        "Cannot turn off popup drop shadow, " +
                        "reverting to standard swing popup factory");
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                  e.printStackTrace();
            }
        }
    }    
}
