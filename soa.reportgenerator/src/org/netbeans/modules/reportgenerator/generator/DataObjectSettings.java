/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.reportgenerator.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.reportgenerator.api.ReportCustomizationOptions;
import org.openide.loaders.DataObject;

/**
 *
 * @author radval
 */
public class DataObjectSettings {

    private static Map<DataObject, ReportCustomizationOptions> mDataObjectToOptionsMap = new WeakHashMap<DataObject, ReportCustomizationOptions>();
    
    public static synchronized ReportCustomizationOptions getOrStoreOptions(DataObject dobj) {
        ReportCustomizationOptions option = mDataObjectToOptionsMap.get(dobj);
        if(option == null) {
            option = new ReportCustomizationOptions();
            mDataObjectToOptionsMap.put(dobj, option);
        }
        
        return option;
    }
    
}
