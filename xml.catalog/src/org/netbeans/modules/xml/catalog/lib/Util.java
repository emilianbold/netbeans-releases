/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.catalog.lib;

import java.beans.*;
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle;

import org.netbeans.modules.xml.core.lib.AbstractUtil;

/**
 * Utility methods.
 *
 * @author  Petr Kuzel
 * @author  Libor Kramolis
 * @version 0.2
 */
public class Util extends AbstractUtil {
 

    /** Default and only one instance of this class. */
    public static final Util THIS = new Util();

    /** Nobody can create instance of it, just me. */
    private Util () {
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
     * @param extensions takes a list of file extensions
     * @return filename or null if operation was cancelled.
     */
    public static File selectCatalogFile(final String extensions) {
        return selectFile(extensions, Util.THIS.getString("TITLE_select_catalog"), Util.THIS.getString("PROP_catalog_mask"));
    }
    
    /**
     * Prompts user for a file.
     * @param extensions takes a list of file extensions
     * @param dialogTitle dialog title
     * @param maskTitle title for filter mask
     * @return filename or null if operation was cancelled
     */
    public static File selectFile(final String extensions, String dialogTitle, final String maskTitle) {
        JFileChooser chooser = new JFileChooser();

        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                StringTokenizer token = new StringTokenizer(extensions, " ");  // NOI18N
                while (token.hasMoreElements()) {
                    if (f.getName().endsWith(token.nextToken())) return true;
                }
                return false;
            }
            public String getDescription() {
                return maskTitle; // NOI18N
            }
        });

        if (lastDirectory != null) {
            chooser.setCurrentDirectory(lastDirectory);
        }

        chooser.setDialogTitle(dialogTitle);
        while (chooser.showDialog(WindowManager.getDefault().getMainWindow(),
                               Util.THIS.getString("PROP_select_button"))
               == JFileChooser.APPROVE_OPTION)
        {
            File f = chooser.getSelectedFile();
            lastDirectory = chooser.getCurrentDirectory();
            if (f != null && f.isFile()) {
                StringTokenizer token = new StringTokenizer(extensions, " ");  // NOI18N
                while (token.hasMoreElements()) {
                    if (f.getName().endsWith(token.nextToken())) return f;
                }
            }

            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                Util.THIS.getString("MSG_inValidFile"), NotifyDescriptor.WARNING_MESSAGE));
        }
        return null;
    } 
    
}
