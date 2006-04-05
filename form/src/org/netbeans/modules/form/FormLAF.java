/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import org.jdesktop.layout.LayoutStyle;
import org.openide.util.*;
import org.openide.ErrorManager;

/**
 * Support for execution of tasks in another look and feel.
 *
 * @author Jan Stola, Tran Duc Trung
 */
class FormLAF {
    /** Determines whether the FormLAF has been initialized (e.g. DelegatingDefaults has been installed). */
    private static boolean initialized = false;
    /** Determines whether we already are in LAF block. */
    private static boolean lafBlockEntered;
    /** DelegatingDefaults installed in UIManager.FormLAF */
    private static DelegatingDefaults delDefaults;
    /** User UIDefaults of the IDE. */
    private static Map netbeansDefaults = new HashMap();
    /** Maps LAF class to its theme. */
    private static Map lafToTheme = new HashMap();
    /** Determines whether the designer LAF is subclass of MetalLookAndFeel. */
    private static boolean designerLafIsMetal;
    /** Determines whether the IDE LAF is subclass of MetalLookAndFeel. */
    private static boolean ideLafIsMetal;

    private FormLAF() {
    }

    static void updateLAF() {
        try {
            if (!initialized) {
                initialized = true;
                initialize();
            }
            Class lafClass = LAFSelector.getDefault().getDesignerLAF();
            designerLafIsMetal = MetalLookAndFeel.class.isAssignableFrom(lafClass);
            if (!ideLafIsMetal && designerLafIsMetal &&
                !MetalLookAndFeel.class.equals(lafClass) && (lafToTheme.get(MetalLookAndFeel.class) == null)) {
                lafToTheme.put(MetalLookAndFeel.class, MetalLookAndFeel.getCurrentTheme());
            }
            LookAndFeel designerLookAndFeel = (LookAndFeel)lafClass.newInstance();
            if (designerLafIsMetal) {
                MetalTheme theme = (MetalTheme)lafToTheme.get(lafClass);
                if (theme == null) {
                    lafToTheme.put(lafClass, MetalLookAndFeel.getCurrentTheme());
                } else {
                    MetalLookAndFeel.setCurrentTheme(theme);
                }
            }
            designerLookAndFeel.initialize();

            UIDefaults designerDefaults = designerLookAndFeel.getDefaults();

            if (designerLafIsMetal && ideLafIsMetal) {
                LookAndFeel ideLaf = UIManager.getLookAndFeel();
                MetalTheme theme = (MetalTheme)lafToTheme.get(ideLaf.getClass());
                MetalLookAndFeel.setCurrentTheme(theme);
            }

            ClassLoader classLoader = lafClass.getClassLoader();
            if (classLoader != null) designerDefaults.put("ClassLoader", classLoader); // NOI18N

            // Force switch of the LayoutStyle
            if (designerDefaults.get("LayoutStyle.instance") == null) { // NOI18N
                designerDefaults.put("LayoutStyle.instance", // NOI18N
                    createLayoutStyle(designerLookAndFeel.getID())); 
            }

            if ("Metal".equals(designerLookAndFeel.getID())) { // NOI18N
                designerDefaults.put("InternalFrameUI", "org.netbeans.modules.form.FormLAF$FormMetalInternalFrameUI"); // NOI18N
            }

            delDefaults.setDelegate(designerDefaults);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (LinkageError ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    private static void initialize() throws Exception {
        UIManager.getDefaults(); // Force initialization

        LookAndFeel laf = UIManager.getLookAndFeel();
        ideLafIsMetal = laf instanceof MetalLookAndFeel;
        if (ideLafIsMetal) {
            lafToTheme.put(laf.getClass(), MetalLookAndFeel.getCurrentTheme());
        }

        java.lang.reflect.Method method = UIManager.class.getDeclaredMethod("getLAFState", new Class[0]); // NOI18N
        method.setAccessible(true);
        Object lafState = method.invoke(null, new Object[0]);
        method = lafState.getClass().getDeclaredMethod("setLookAndFeelDefaults", new Class[] {UIDefaults.class}); // NOI18N
        method.setAccessible(true);

        UIDefaults original = UIManager.getLookAndFeelDefaults();
        assert !(original instanceof DelegatingDefaults);

        delDefaults = new DelegatingDefaults(null, original);
        method.invoke(lafState, new Object[] {delDefaults});
    }

    static Object executeWithLookAndFeel(final Mutex.ExceptionAction act)
        throws Exception
    {
        try {
            return Mutex.EVENT.readAccess(new Mutex.ExceptionAction() {
                public Object run() throws Exception {
                    // FIXME(-ttran) needs to hold a lock on UIDefaults to
                    // prevent other threads from creating Swing components
                    // in the mean time
                    synchronized (UIManager.getDefaults()) {
                        boolean restoreAfter = true;
                        try {
                            if (lafBlockEntered)
                                restoreAfter = false;
                            else {
                                lafBlockEntered = true;
                                useDesignerLookAndFeel();
                                restoreAfter = true;
                            }
                            return act.run();
                        }
                        finally {
                            if (restoreAfter) {
                                useIDELookAndFeel();
                                lafBlockEntered = false;
                            }
                        }
                    }
                }
            });
        }
        catch (MutexException ex) {
            throw ex.getException();
        }
    }

    static void executeWithLookAndFeel(final Runnable run) {
        Mutex.EVENT.readAccess(new Mutex.Action() {
            public Object run() {
                // FIXME(-ttran) needs to hold a lock on UIDefaults to
                // prevent other threads from creating Swing components
                // in the mean time
                synchronized (UIManager.getDefaults()) {
                    boolean restoreAfter = true;
                    try {
                        if (lafBlockEntered)
                            restoreAfter = false;
                        else {
                            lafBlockEntered = true;
                            useDesignerLookAndFeel();
                            restoreAfter = true;
                        }
                        run.run();
                    }
                    finally {
                        if (restoreAfter) {
                            useIDELookAndFeel();
                            lafBlockEntered = false;
                        }
                    }
                }
                return null;
            }
        });
    }

    private static void useDesignerLookAndFeel() {
        if (!initialized) {
            updateLAF();
        }
        UIDefaults defaults = UIManager.getDefaults();
        netbeansDefaults.clear();
        netbeansDefaults.putAll(defaults);
        Iterator iter = netbeansDefaults.keySet().iterator();
        while (iter.hasNext()) defaults.remove(iter.next());

        setUseDesignerDefaults(true);

        if (designerLafIsMetal) {
            try {
                Class designerLAF = LAFSelector.getDefault().getDesignerLAF();
                MetalLookAndFeel.setCurrentTheme((MetalTheme)lafToTheme.get(designerLAF));
            } catch (ClassNotFoundException cnfex) {
                cnfex.printStackTrace(); // Should not happen
            }
        }
    }

    private static void useIDELookAndFeel() {
        UIManager.getDefaults().putAll(netbeansDefaults);

        setUseDesignerDefaults(false);

        if (ideLafIsMetal) {
            MetalLookAndFeel.setCurrentTheme((MetalTheme)lafToTheme.get(UIManager.getLookAndFeel().getClass()));
        }
    }

    /**
     * HACK - creates a LayoutStyle that corresponds to the given LAF.
     * LayoutStyle is created according to UIManager.getLookAndFeel()
     * which is not affected by our LAF switch => we have to create
     * the new LayoutStyle manually.
     */
    private static LayoutStyle createLayoutStyle(String lafID) {
        boolean useCoreLayoutStyle = false;
        try {
            Class.forName("javax.swing.LayoutStyle"); // NOI18N
            useCoreLayoutStyle = true;
        } catch (ClassNotFoundException cnfex) {}
        String layoutStyleClass;
        if (useCoreLayoutStyle) {
            layoutStyleClass = "Swing"; // NOI18N
        } else if ("Metal" == lafID) { // NOI18N
            layoutStyleClass = "Metal"; // NOI18N
        }
        else if ("Windows" == lafID) { // NOI18N
            layoutStyleClass = "Windows"; // NOI18N
        }
        else if ("GTK" == lafID) { // NOI18N
            layoutStyleClass = "Gnome"; // NOI18N
        }
        else if ("Aqua" == lafID) { // NOI18N
            layoutStyleClass = "Aqua"; // NOI18N
        } else {
            layoutStyleClass = ""; // NOI18N
        }
        layoutStyleClass = "org.jdesktop.layout." + layoutStyleClass + "LayoutStyle"; // NOI18N
        LayoutStyle layoutStyle = null;
        try {
            Class clazz = Class.forName(layoutStyleClass);
            java.lang.reflect.Constructor constr = clazz.getDeclaredConstructor(new Class[0]);
            constr.setAccessible(true);
            layoutStyle = (LayoutStyle)constr.newInstance(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return layoutStyle;
    }

    static LayoutStyle getDesignerLayoutStyle() {
        return (LayoutStyle)delDefaults.getDelegate().get("LayoutStyle.instance"); // NOI18N
    }

    static void setUseDesignerDefaults(boolean designerDefaults) {
        delDefaults.setDelegating(designerDefaults);
    }

    /**
     * Implementation of UIDefaults that delegates requests between two
     * UIDefaults based on some rule.
     */
    static class DelegatingDefaults extends UIDefaults {
        /** UIDefaults to use for delegation. */
        private UIDefaults delegate;
        /** The original UIDefaults. */
        private UIDefaults original;
        /** If true, then the delegate map is always used. */
        private boolean delegating;
        
        DelegatingDefaults(UIDefaults delegate, UIDefaults original) {
            this.delegate = delegate;
            this.original = original;
        }

        public UIDefaults getDelegate() {
            return delegate;
        }

        public void setDelegate(UIDefaults delegate) {
            this.delegate = delegate;
        }

        public void setDelegating(boolean delegating){
            this.delegating = delegating;
        }

        public boolean isDelegating() {
            return delegating;
        }

        // Delegated methods

        public Object get(Object key) {
            return delegating ? delegate.get(key) : original.get(key);
        }

        public Object put(Object key, Object value) {
            return delegating ? delegate.put(key, value) : original.put(key, value);
        }

        public void putDefaults(Object[] keyValueList) {
            if (delegating) delegate.putDefaults(keyValueList); else original.putDefaults(keyValueList);
        }

        public Object get(Object key, Locale l) {
            return delegating ? delegate.get(key, l) : original.get(key, l);
        }

        public synchronized void addResourceBundle(String bundleName) {
            if (delegating) delegate.addResourceBundle(bundleName); else original.addResourceBundle(bundleName);
        }

        public synchronized void removeResourceBundle(String bundleName) {
            if (delegating) delegate.removeResourceBundle(bundleName); else original.removeResourceBundle(bundleName);
        }

        public void setDefaultLocale(Locale l) {
            if (delegating) delegate.setDefaultLocale(l); else original.setDefaultLocale(l);
        }

        public Locale getDefaultLocale() {
            return delegating ? delegate.getDefaultLocale() : original.getDefaultLocale();
        }

        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            if (delegating) delegate.addPropertyChangeListener(listener); else original.addPropertyChangeListener(listener);
        }

        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            if (delegating) delegate.removePropertyChangeListener(listener); else original.removePropertyChangeListener(listener);
        }

        public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
            return (delegating) ? delegate.getPropertyChangeListeners() : original.getPropertyChangeListeners();
        }

        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            System.out.println("Warning: FormLAF.firePropertyChange called, but not implemented."); // NOI18N
        }

    }

    // Workaround for issue 4969308
    public static class FormMetalInternalFrameUI extends MetalInternalFrameUI {
        public FormMetalInternalFrameUI(JInternalFrame b)   {
            super(b);
        }

        protected JComponent createNorthPane(JInternalFrame w) {
            return new FormMetalInternalFrameTitlePane(w);
        }

        public static javax.swing.plaf.ComponentUI createUI(JComponent c)    {
            return new FormMetalInternalFrameUI((JInternalFrame)c);
        }

        static class FormMetalInternalFrameTitlePane extends MetalInternalFrameTitlePane {
            FormMetalInternalFrameTitlePane(JInternalFrame w) {
                super(w);
            }

            public void paintComponent(java.awt.Graphics g)  {
                if (delDefaults.isDelegating()) {
                    super.paintComponent(g);
                } else {
                    // RepaintManager.paintDirtyRegions for a previewed frame
                    // is sometimes able not to use the correct L&F
                    try {
                        delDefaults.setDelegating(true);
                        super.paintComponent(g);
                    } finally {
                        delDefaults.setDelegating(false);
                    }
                }
            }
        }
    }

}
