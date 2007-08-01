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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.beans.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class FormLAF {
    private static final String SWING_NOXP = "swing.noxp"; // NOI18N
    /** Determines whether the FormLAF has been initialized (e.g. DelegatingDefaults has been installed). */
    private static boolean initialized = false;
    /** Determines whether we already are in LAF block. */
    private static boolean lafBlockEntered;
    /** DelegatingDefaults installed in UIManager.FormLAF */
    private static DelegatingDefaults delDefaults;
    /** User UIDefaults of the IDE. */
    private static Map netbeansDefaults = new HashMap();
    /** User UIDefaults of components */
    private static Map userDefaults = new HashMap(UIManager.getDefaults());
    /** Maps LAF class to its theme. */
    private static Map<Class, MetalTheme> lafToTheme = new HashMap<Class, MetalTheme>();
    /** Determines whether the IDE LAF is subclass of MetalLookAndFeel. */
    private static boolean ideLafIsMetal;
    private static boolean preview;
    private static Class previewLaf;

    private FormLAF() {
    }
    
    static UIDefaults initPreviewLaf(Class lafClass) {
        try {
            boolean previewLafIsMetal = MetalLookAndFeel.class.isAssignableFrom(lafClass);
            if (!ideLafIsMetal && previewLafIsMetal &&
                !MetalLookAndFeel.class.equals(lafClass) && (lafToTheme.get(MetalLookAndFeel.class) == null)) {
                lafToTheme.put(MetalLookAndFeel.class, MetalLookAndFeel.getCurrentTheme());
            }
            LookAndFeel previewLookAndFeel = (LookAndFeel)lafClass.newInstance();
            if (previewLafIsMetal) {
                MetalTheme theme = lafToTheme.get(lafClass);
                if (theme == null) {
                    lafToTheme.put(lafClass, MetalLookAndFeel.getCurrentTheme());
                } else {
                    MetalLookAndFeel.setCurrentTheme(theme);
                }
            }

            String noxp = null;
            boolean classic = isClassicWinLAF(lafClass.getName());
            if (classic) {
                noxp = System.getProperty(SWING_NOXP);
                System.setProperty(SWING_NOXP, "y"); // NOI18N
            }
            UIDefaults previewDefaults = null;
            try {
                previewLookAndFeel.initialize();
                previewDefaults = previewLookAndFeel.getDefaults();
            } finally {
                if (classic) {
                    if (noxp == null) {
                        System.getProperties().remove(SWING_NOXP);
                    } else {
                        System.setProperty(SWING_NOXP, noxp);
                    }
                }
            }

            if (previewLafIsMetal && ideLafIsMetal) {
                LookAndFeel ideLaf = UIManager.getLookAndFeel();
                MetalTheme theme = lafToTheme.get(ideLaf.getClass());
                MetalLookAndFeel.setCurrentTheme(theme);
            }

            ClassLoader classLoader = lafClass.getClassLoader();
            if (classLoader != null) previewDefaults.put("ClassLoader", classLoader); // NOI18N

            // Force switch of the LayoutStyle
            if (previewDefaults.get("LayoutStyle.instance") == null) { // NOI18N
                previewDefaults.put("LayoutStyle.instance", // NOI18N
                    createLayoutStyle(previewLookAndFeel.getID())); 
            }

            previewDefaults.putAll(delDefaults.getCustomizedUIDefaults());

            return previewDefaults;
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (LinkageError ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }

    private static boolean isClassicWinLAF(String className) {
        return "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel".equals(className); // NOI18N
    }

    private static void invalidateXPStyle() {
        try {
            Class xpStyle = Class.forName("com.sun.java.swing.plaf.windows.XPStyle"); // NOI18N
            java.lang.reflect.Method method = xpStyle.getDeclaredMethod("invalidateStyle", null); // NOI18N
            method.setAccessible(true);
            method.invoke(null);
        } catch (Exception ex) {
            Logger.getLogger(FormLAF.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    private static void initialize() throws Exception {
        initialized = true;
        UIManager.getDefaults(); // Force initialization

        LookAndFeel laf = UIManager.getLookAndFeel();
        ideLafIsMetal = laf instanceof MetalLookAndFeel;
        if (ideLafIsMetal) {
            lafToTheme.put(laf.getClass(), MetalLookAndFeel.getCurrentTheme());
        }
        LookAndFeel original = laf;
        try {
            original = laf.getClass().newInstance();
        } catch (Exception ex) {
            Logger.getLogger(FormLAF.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }

        java.lang.reflect.Method method = UIManager.class.getDeclaredMethod("getLAFState", new Class[0]); // NOI18N
        method.setAccessible(true);
        Object lafState = method.invoke(null, new Object[0]);
        method = lafState.getClass().getDeclaredMethod("setLookAndFeelDefaults", new Class[] {UIDefaults.class}); // NOI18N
        method.setAccessible(true);

        UIDefaults ide = UIManager.getLookAndFeelDefaults();
        assert !(ide instanceof DelegatingDefaults);

        delDefaults = new DelegatingDefaults(null, original.getDefaults(), ide);
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
            try {
                initialize();
            } catch (Exception ex) {
                Logger.getLogger(FormLAF.class.getName()).log(Level.INFO, ex.getMessage(), ex);
            }
        }
        UIDefaults defaults = UIManager.getDefaults();
        netbeansDefaults.clear();
        netbeansDefaults.putAll(defaults);
        netbeansDefaults.keySet().removeAll(userDefaults.keySet());
        defaults.keySet().removeAll(netbeansDefaults.keySet());

        if (!preview) {
            setUseDesignerDefaults(true);
        } else if (MetalLookAndFeel.class.isAssignableFrom(previewLaf)) {
            MetalLookAndFeel.setCurrentTheme(lafToTheme.get(previewLaf));
        }
    }

    private static void useIDELookAndFeel() {
        userDefaults.clear();
        userDefaults.putAll(UIManager.getDefaults());
        UIManager.getDefaults().putAll(netbeansDefaults);

        if (!preview) {
            setUseDesignerDefaults(false);
        } else if (ideLafIsMetal) {
            MetalLookAndFeel.setCurrentTheme(lafToTheme.get(UIManager.getLookAndFeel().getClass()));
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
            Logger.getLogger(FormLAF.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        return layoutStyle;
    }

    static LayoutStyle getDesignerLayoutStyle() {
        return LayoutStyle.getSharedInstance();
    }

    static void setUseDesignerDefaults(boolean designerDefaults) {
        delDefaults.setDelegating(designerDefaults);
    }

    static String oldNoXP;
    static void setUsePreviewDefaults(boolean previewing, Class previewLAF, UIDefaults uiDefaults) {
        boolean classic = (previewLAF == null)
            ? ((previewLaf == null) ? false : isClassicWinLAF(previewLaf.getName()))
            : isClassicWinLAF(previewLAF.getName());
        preview = previewing;
        previewLaf = previewLAF;
        if (previewing) {
            if (classic) {
                oldNoXP = System.getProperty(SWING_NOXP);
                System.setProperty(SWING_NOXP, "y"); // NOI18N
                invalidateXPStyle();
            }
            delDefaults.setPreviewDefaults(uiDefaults);
        } else {
            if (classic) {
                if (oldNoXP == null) {
                    System.getProperties().remove(SWING_NOXP);
                } else {
                    System.setProperty(SWING_NOXP, oldNoXP);
                }
                invalidateXPStyle();
            }
            oldNoXP = null;
        }
        delDefaults.setPreviewing(previewing);
    }

    public static boolean getUsePreviewDefaults() {
        return preview && !delDefaults.isDelegating();
    }

    static void setCustomizingUIClasses(boolean customizing) {
        if (delDefaults != null)
            delDefaults.setCustomizingUIClasses(customizing);
    }

    /**
     * Implementation of UIDefaults that delegates requests between two
     * UIDefaults based on some rule.
     */
    static class DelegatingDefaults extends UIDefaults {
        /** UIDefaults used for preview. */
        private UIDefaults preview;
        /** The designer UIDefaults. */
        private UIDefaults original;
        /** IDE UIDefaults. */
        private UIDefaults ide;
        /** If true, then the designer map is used. */
        private boolean delegating;
        /** If true, then the preview map is used. */
        private boolean previewing;     
        /** If true, then new UI components may install their defaults. */
        private boolean customizingUI;
        /** Map of defaults of new UI components. */
        private Map customizedUIDefaults = new HashMap();
        
        DelegatingDefaults(UIDefaults preview, UIDefaults original, UIDefaults ide) {
            this.preview = preview;
            this.original = original;
            this.ide = ide;
        }

        public void setPreviewDefaults(UIDefaults preview) {
            this.preview = preview;
        }

        public void setDelegating(boolean delegating){
            this.delegating = delegating;
        }

        public boolean isDelegating() {
            return delegating;
        }

        public void setPreviewing(boolean previewing){
            this.previewing = previewing;
        }

        public boolean isPreviewing() {
            return previewing;
        }

        public void setCustomizingUIClasses(boolean customizing) {
            customizingUI = customizing;
        }

        public Map getCustomizedUIDefaults() {
            return customizedUIDefaults;
        }

        // Delegated methods

        private UIDefaults getCurrentDefaults() {
            return delegating ? original : (previewing ? preview : ide);
        }

        @Override
        public Object get(Object key) {
            return delegating ? original.get(key) : (previewing ? preview.get(key) : ide.get(key));
        }

        @Override
        public Object put(Object key, Object value) {
            if (delegating) {
                if (customizingUI) {
                    customizedUIDefaults.put(key, value);
                }
                return original.put(key, value);
            } else {
                return (previewing ? preview.put(key, value) : ide.put(key, value));
            }
        }

        @Override
        public void putDefaults(Object[] keyValueList) {
            getCurrentDefaults().putDefaults(keyValueList);
        }

        @Override
        public Object get(Object key, Locale l) {
            return getCurrentDefaults().get(key, l);
        }

        @Override
        public synchronized void addResourceBundle(String bundleName) {
            getCurrentDefaults().addResourceBundle(bundleName);
        }

        @Override
        public synchronized void removeResourceBundle(String bundleName) {
            getCurrentDefaults().removeResourceBundle(bundleName);
        }

        @Override
        public void setDefaultLocale(Locale l) {
            getCurrentDefaults().setDefaultLocale(l);
        }

        @Override
        public Locale getDefaultLocale() {
            return getCurrentDefaults().getDefaultLocale();
        }

        @Override
        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            getCurrentDefaults().addPropertyChangeListener(listener);
        }

        @Override
        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            getCurrentDefaults().removePropertyChangeListener(listener);
        }

        @Override
        public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
            return getCurrentDefaults().getPropertyChangeListeners();
        }

        @Override
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "FormLAF.firePropertyChange called, but not implemented."); // NOI18N
        }

        @Override
        public synchronized Enumeration<Object> keys() {
            return getCurrentDefaults().keys();
        }

    }

}
