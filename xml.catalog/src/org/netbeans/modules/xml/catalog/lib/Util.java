/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.catalog.lib;

import java.beans.*;
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openide.util.NbBundle;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.xml.catalog.AbstractUtil;

/**
 * Utility methods.
 *
 * @author  Petr Kuzel
 * @version 
 */
public class Util extends AbstractUtil {

    /** Get localized string.
     * @param key key of localized value.
     * @return localized value.
     */
    static final String getString (String key) {
	return NbBundle.getMessage (Util.class, key);
    }
        
    /** Get localized string by passing parameter.
     * @param key key of localized value.
     * @param param argument to use when formating the message
     * @return localized value.
     */
    static final String getString (String key, Object param) {
	return NbBundle.getMessage (Util.class, key, param);
    }
    
    /** Get localized character. Usually used on mnemonic.
     * @param key key of localized value.
     * @return localized value.
     */
    static final char getChar (String key) {
	return NbBundle.getMessage (Util.class, key).charAt (0);
    }
    

    /** 
     * Disabling it all debug infornation reported using this
     * class will be disabled. Performance may be decreased.
     * Suitable for intermediate builds.
     * <p>
     * In final release disable all such fields in all classes.
     */
    private static final boolean DEBUG = false;
    
    /** Creates new Util */
    public Util() {
    }

    
    public static final void trace (String message) {
        if ( DEBUG ) {
            debug (message);
        }
    }
    
    /**
     * Should be rewritten for fallback Properties customizer
     * @return customizer of given Class     
     */
    public static Customizer getProviderCustomizer(Class clazz) {
        try {
            Class customizer =
                Introspector.getBeanInfo(clazz).getBeanDescriptor().getCustomizerClass();
            
            return (Customizer) customizer.newInstance();
            
        } catch (InstantiationException ex) {
            return null;
        } catch (IntrospectionException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            return null;
        }
    }
    

    /**
     * Create new instance of given provider.
     */
    public static Object createProvider(Class clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            return null;
        }
    }

    // last catalog directory
    private static File lastDirectory;
    
    /**
     * Prompts user for a catalog file.
     * @param takes a list of file extensions
     * @return filename or null if operation was cancelled.
     */
    public static File selectCatalogFile(final String enum) {
        JFileChooser chooser = new JFileChooser();

        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                StringTokenizer token = new StringTokenizer(enum, " ");  // NOI18N
                while (token.hasMoreElements()) {
                    if (f.getName().endsWith(token.nextToken())) return true;
                }
                return false;
            }
            public String getDescription() {
                return Util.getString("PROP_catalog_mask"); // NOI18N
            }
        });

        if (lastDirectory != null) {
            chooser.setCurrentDirectory(lastDirectory);
        }

        chooser.setDialogTitle(Util.getString("TITLE_select_catalog"));
        while (chooser.showDialog(TopManager.getDefault().getWindowManager().getMainWindow(),
                               Util.getString("PROP_select_button"))
               == JFileChooser.APPROVE_OPTION)
        {
            File f = chooser.getSelectedFile();
            lastDirectory = chooser.getCurrentDirectory();
            if (f != null && f.isFile()) {
                StringTokenizer token = new StringTokenizer(enum, " ");  // NOI18N
                while (token.hasMoreElements()) {
                    if (f.getName().endsWith(token.nextToken())) return f;
                }
            }

            TopManager.getDefault().notify(new NotifyDescriptor.Message(
                Util.getString("MSG_inValidFile"), NotifyDescriptor.WARNING_MESSAGE));
        }
        return null;
    }    
    
}
