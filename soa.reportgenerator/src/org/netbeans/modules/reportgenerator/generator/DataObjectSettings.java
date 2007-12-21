/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.reportgenerator.generator;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.reportgenerator.api.ReportCustomizationOptions;
import org.openide.loaders.DataObject;

/**
 *
 * @author radval
 */
public class DataObjectSettings {

    private static Map<DataObject, ReportCustomizationOptions> mDataObjectToOptionsMap = new HashMap<DataObject, ReportCustomizationOptions>();
    
    public static ReportCustomizationOptions getOrStoreOptions(DataObject dobj) {
        ReportCustomizationOptions option = mDataObjectToOptionsMap.get(dobj);
        if(option == null) {
            option = new ReportCustomizationOptions();
            mDataObjectToOptionsMap.put(dobj, option);
        }
        
        return option;
    }
    
}
