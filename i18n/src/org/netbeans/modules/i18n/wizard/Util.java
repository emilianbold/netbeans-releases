/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.i18n.wizard;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.i18n.FactoryRegistry;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.cookies.EditorCookie;

/**
 * Bundle access, ...
 *
 * @author  Petr Kuzel
 */
final class Util {
    
    public static String getString(String key) {
        return NbBundle.getMessage(Util.class, key);
    }
    
    public static char getChar(String key) {
        return getString(key).charAt(0);
    }

    // Settings ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
     * Create empty settings used in i18n wizards. 
     */
    public static Map createWizardSettings() {
        return new TreeMap(new DataObjectComparator());
    }
    
    /** 
     * Create settings based on selected nodes. Finds all accepted data objects. 
     * Used by actions to populate wizard.
     * @param activatedNodes selected nodes 
     * @return map with accepted data objects as keys or empty map if no such
     * data objec were found.
     */
    public static Map createWizardSettings(Node[] activatedNodes) {
        Map settings = createWizardSettings();
        
        if (activatedNodes != null && activatedNodes.length > 0) {
            for (int i = 0; i < activatedNodes.length; i++) {
                DataObject dataObject = (DataObject)activatedNodes[i].getCookie(DataObject.class);
                
                if (dataObject == null) continue;
                
                if (dataObject instanceof DataFolder) {
                    Iterator it = I18nUtil.getAcceptedDataObjects((DataFolder)dataObject).iterator();
                    
                    while(it.hasNext()) {
                        addSource(settings, (DataObject)it.next());
                    }
                } else if (FactoryRegistry.hasFactory(dataObject.getClass())) {
                    addSource(settings, dataObject);
                }
            }
        }
        
        return settings;
    }
    
    /** Adds source to source map (I18N wizard settings). If there is already no change is done.
     * If it's added anew then it is tried to find correspondin reousrce, i.e.
     * first resource from the same folder.
     * @param sourceMap settings where to add teh sources
     * @param source source to add */
    public static void addSource(Map sourceMap, DataObject source) {
        if(sourceMap.containsKey(source))
            return;
        
        DataFolder folder = source.getFolder();
        
        if(folder == null) {
            sourceMap.put(source, null);
            return;
        }
        
        DataObject[] children = folder.getChildren();
        
        for(int i = 0; i < children.length; i++) {
            if(children[i] instanceof PropertiesDataObject) { // PENDING 
                sourceMap.put(source, new SourceData(children[i]));
                return;
            }
        }
        
        // No resource found in the same folder.
        sourceMap.put(source, null);
    }

    /** Shared enableness logic. Either DataObject.Container or EditorCookie must be present on all nodes.*/
    static boolean wizardEnabled(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0) {
            return false;
        }

        for (int i = 0; i<activatedNodes.length; i++) {
            Node node = activatedNodes[i];
            Object container = node.getCookie(DataObject.Container.class);
            if (container != null) continue;
            if (node.getCookie(EditorCookie.class) == null) {
                return false;
            }
        }
        return true;
    }


    /**
     * Compare data objects according their package and name. 
     */
    private static class DataObjectComparator implements Comparator {

        /** Implements <code>Comparator</code> interface. */
        public int compare(Object o1, Object o2) {
            if(!(o1 instanceof DataObject) || !(o2 instanceof DataObject))
                return 0;
            
            DataObject d1 = (DataObject)o1;
            DataObject d2 = (DataObject)o2;
            
            if(d1 == d2)
                return 0;
            
            if(d1 == null)
                return -1;
            
            if(d2 == null)
                return 1;

            //return d1.getPrimaryFile().getPackageName('.').compareTo(d2.getPrimaryFile().getPackageName('.'));
            return d1.getPrimaryFile().getPath().compareTo( d2.getPrimaryFile().getPath() );
        }
        
        /** Implements <code>Comparator</code> interface method. */
        public boolean equals(Object obj) {
            if(this == obj)
                return true;
            else
                return false;
        }
    }

}
