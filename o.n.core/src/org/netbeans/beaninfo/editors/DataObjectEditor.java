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

package org.netbeans.beaninfo.editors;

import java.beans.*;

import org.openide.TopManager;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataFilter;
import org.openide.explorer.propertysheet.*;

/**
 * Property editor for org.openide.loaders.DataObject.
 * Uses class DataObjectPanel as custom property editor.
 * @author David Strupl
 */
public class DataObjectEditor extends PropertyEditorSupport implements ExPropertyEditor {

    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_CURRENT_FOLDER = "currentFolder"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_ROOT_FOLDER = "rootFolder"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_COOKIES = "cookies"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_DATA_FILTER = "dataFilter"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_FOLDER_FILTER = "folderFilter"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_NODE_ACCEPTOR = "nodeAcceptor"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_LABEL = "label"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_TITLE = "title"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_INSET = "inset"; // NOI18N

    /** This gets lazy initialized in getDataObjectPanel*/
    private DataObjectPanel customEditor;
   
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private DataFolder rootFolder;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private DataFolder currentFolder;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private Class[] cookies;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private DataFilter dataFilter;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private DataFilter folderFilter;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private NodeAcceptor nodeAcceptor;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private String label;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private String title;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private int insets;
    
    private PropertyChangeSupport supp = new PropertyChangeSupport(this);

    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     */
    public void attachEnv(PropertyEnv env) {
        Object newObj = env.getFeatureDescriptor().getValue(PROPERTY_CURRENT_FOLDER);
        if (newObj instanceof DataFolder) {
            currentFolder = (DataFolder)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_ROOT_FOLDER);
        if (newObj instanceof DataFolder) {
            rootFolder = (DataFolder)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_COOKIES);
        if (newObj instanceof Class[]) {
            cookies = (Class[])newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_DATA_FILTER);
        if (newObj instanceof DataFilter) {
            dataFilter = (DataFilter)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_FOLDER_FILTER);
        if (newObj instanceof DataFilter) {
            folderFilter = (DataFilter)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_NODE_ACCEPTOR);
        if (newObj instanceof NodeAcceptor) {
            nodeAcceptor = (NodeAcceptor)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_LABEL);
        if (newObj instanceof String) {
            label = (String)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_TITLE);
        if (newObj instanceof String) {
            title = (String)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_INSET);
        if (newObj instanceof Integer) {
            insets = ((Integer)newObj).intValue();
        }
    }    
    
    /**
     * Calls lazy initialization in getDataObjectpanel().
     * @return an instanceof DataObjectPanel
     */
    public java.awt.Component getCustomEditor() {
        return getDataObjectPanel();
    }
    
    void setOkButtonEnabled(boolean state) {
        if (supp == null) {
            supp = new PropertyChangeSupport(this);
        }
        supp.firePropertyChange(ExPropertyEditor.PROP_VALUE_VALID, null, state?Boolean.TRUE:Boolean.FALSE);
    }
    
    /**
     * Lazy initializes customEditor (DataObjectPanel).
     * Passes all parameters gathered in method attachEnv.
     */
    private DataObjectPanel getDataObjectPanel() {
        if (customEditor == null) {
            // lazy init ...
            customEditor = new DataObjectPanel(this);
        }
        if (cookies != null) {
            customEditor.setDataFilter(new CookieFilter(cookies, dataFilter));
        } else {
            customEditor.setDataFilter(dataFilter);
        }
        if (currentFolder != null) {
            customEditor.setDataObject(currentFolder);
        }
        if (label != null) {
            customEditor.setText(label);
        }
        if (title != null) {
            customEditor.putClientProperty("title", title); // NOI18N
        }
        if (nodeAcceptor != null) {
            customEditor.setNodeFilter(nodeAcceptor);
        }
        if (folderFilter != null) {
            customEditor.setFolderFilter(folderFilter);
        }
        if (rootFolder != null) {
            customEditor.setRootObject(rootFolder);
        }
        customEditor.setInsetValue(insets);
        return customEditor;
    }
    
    /**
     * Determines whether the propertyEditor can provide a custom editor.
     * @return  true.
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Adds the listener also to private support supp.*/
     public void addPropertyChangeListener(PropertyChangeListener l) {
         super.addPropertyChangeListener(l);
         supp.addPropertyChangeListener(l);
     }

    /** Removes the listener also from private support supp.*/
     public void removePropertyChangeListener(PropertyChangeListener l) {
         super.removePropertyChangeListener(l);
         supp.removePropertyChangeListener(l);
     }
    
    /** CookieFilter allows you to filter DataObjects
     * based on presence of specified cookies.
     */
    private static class CookieFilter implements DataFilter {
        private Class[] cookieArray;
        private DataFilter originalFilter;

        /** Just remember the cookie array and original filter.*/
        public CookieFilter(Class[] cookieArray, DataFilter originalFilter) {
            this.cookieArray = cookieArray;
            this.originalFilter = originalFilter;
        }
        /** Should the data object be displayed or not? This implementation
         * combines the originalFilter with set of cookies supplied
         * in cookieArray.
         * @param obj the data object
         * @return <CODE>true</CODE> if the object should be displayed,
         *    <CODE>false</CODE> otherwise
         */
        public boolean acceptDataObject (DataObject obj) {
            if (cookieArray == null) {
                if (originalFilter != null) {
                    return originalFilter.acceptDataObject(obj);
                } else {
                    return true;
                }
            }
            for (int i = 0; i < cookieArray.length; i++) {
                if (obj.getCookie(cookieArray[i]) == null) {
                    return false;
                }
            }
            if (originalFilter != null) {
                return originalFilter.acceptDataObject(obj);
            } else {
                return true;
            }
        }
    }
}
