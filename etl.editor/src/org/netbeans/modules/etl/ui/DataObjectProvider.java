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

package org.netbeans.modules.etl.ui;

import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;


/**
 * This Class uses the Lookup to find the data object corresponding 
 * to the active top component.
 * @author ks161616
 */
public class DataObjectProvider {
    
    /* singleton instance of the top component provider */
    private static DataObjectProvider instance;
    
    public static ETLDataObject activeDataObject;
    
    /**
     * Creates a new instance of DataObjectProvider
     */
    private DataObjectProvider() {        
    }
    
    /**
     * Gets the singleton instance of the provider.
     *    
     */
    public static DataObjectProvider getProvider() {
        if(instance == null) {
            instance = new DataObjectProvider();
        }
        return instance;
    }
    
    /**
     * Gets the active ETL data object.
     *
     */
    public ETLDataObject getActiveDataObject() {
        Object obj = TopComponent.getRegistry().getActivated().getLookup().lookup(DataObject.class);
        if(obj instanceof ETLDataObject) {
            activeDataObject = (ETLDataObject)obj;
        }
        // If no active data object is found, returns the previously active data object.
        // check if any other ways exists to do this.
        return activeDataObject;
    }
}
