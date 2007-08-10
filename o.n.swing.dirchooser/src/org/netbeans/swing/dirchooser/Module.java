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
 *
 * Contributor(s): Soot Phengsy
 */

package org.netbeans.swing.dirchooser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FileChooserUI;
import org.openide.modules.ModuleInstall;

/**
 * Registers the directory chooser in NetBeans.
 *
 * @author Soot Phengsy
 */
public class Module extends ModuleInstall {
    
    private static final String KEY = "FileChooserUI"; // NOI18N
    private static Class<? extends FileChooserUI> originalImpl;
    private static PropertyChangeListener pcl;
    
    private static final String QUICK_CHOOSER_NAME = 
            "org.netbeans.modules.quickfilechooser.ChooserComponentUI";
    
    private static final String FORCE_STANDARD_CHOOSER = "standard-file-chooser"; // NOI18N

    @Override public void restored() {
        install();
    }

    @Override public void uninstalled() {
        uninstall();
    }
        
    public static void install() {
        // don't install directory chooser if standard chooser is desired
        if (isStandardChooserForced()) {
            return;
        }
        final UIDefaults uid = UIManager.getDefaults();
        originalImpl = (Class<? extends FileChooserUI>) uid.getUIClass(KEY);
        Class impl = DelegatingChooserUI.class;
        final String val = impl.getName();
        // don't install dirchooser if quickfilechooser is present
        if (!isQuickFileChooser(uid.get(KEY))) {
            uid.put(KEY, val);
            // To make it work in NetBeans too:
            uid.put(val, impl);
        }
        // #61147: prevent NB from switching to a different UI later (under GTK):
        uid.addPropertyChangeListener(pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                Object className = uid.get(KEY);
                if ((name.equals(KEY) || name.equals("UIDefaults")) && !val.equals(className)
                        && !isQuickFileChooser(className)) {
                    uid.put(KEY, val);
                }
            }
        });
    }
    
    public static void uninstall() {
        if (isInstalled()) {
            assert pcl != null;
            UIDefaults uid = UIManager.getDefaults();
            uid.removePropertyChangeListener(pcl);
            pcl = null;
            String val = originalImpl.getName();
            uid.put(KEY, val);
            uid.put(val, originalImpl);
            originalImpl = null;
        }
    }
    
    public static boolean isInstalled() {
        return originalImpl != null;
    }
    
    static Class<? extends FileChooserUI> getOrigChooser () {
        return originalImpl;
    }
    
    private static boolean isQuickFileChooser (Object className) {
        return QUICK_CHOOSER_NAME.equals(className);
    }
    
    private static boolean isStandardChooserForced () {
        return Boolean.getBoolean(FORCE_STANDARD_CHOOSER);
    }
    
}
