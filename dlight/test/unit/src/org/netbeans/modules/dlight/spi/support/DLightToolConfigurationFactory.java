/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.spi.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author mt154047
 */
public class DLightToolConfigurationFactory {

    private static final String DLIGHT_TOOL_ID = "id";//NOI18N
    private static final String DLIGHT_TOOL_NAME = "name";//NOI18N
    private static final String DLIGHT_TOOL_DISPLAYED_NAME = "displayName";//NOI18N
    private static final String DLIGHT_ICON_BASE = "iconBase";//NOI18N
    private static final String DLIGHT_COLLECTORS = "collector.config";//NOI18N
    private static final String DLIGHT_IDPS = "idp.config";//NOI18N
    private static final String DLIGHT_DATATABLE = "datatable";
    private static final String DLIGHT_DESCRIPTION = "description";

    static DLightToolConfiguration create(Map map) {
        return DLightToolConfigurationFactory.createInstance(map);
    }

    private static DLightToolConfiguration createInstance(Map map) {
        DLightToolConfiguration toolConfiguration =
                new DLightToolConfiguration(getStringValue(map, DLIGHT_TOOL_ID), getStringValue(map, DLIGHT_TOOL_NAME));
        toolConfiguration.setLongName(getStringValue(map, DLIGHT_TOOL_DISPLAYED_NAME));
        toolConfiguration.setIcon(getStringValue(map, DLIGHT_ICON_BASE));
        toolConfiguration.setDescription(getStringValue(map, DLIGHT_DESCRIPTION));
        try {
            FileObject rootFolder = FileUtil.getConfigRoot();
            FileObject fo = rootFolder.getFileObject(getStringValue(map, DLIGHT_COLLECTORS));
            DataObject dob = DataObject.find(fo);
            InstanceCookie c = dob.getCookie(InstanceCookie.class);
            if (c != null) {
                Object o = c.instanceCreate();
                Collection<DataCollectorConfiguration> result = (Collection<DataCollectorConfiguration>) o;
                for (DataCollectorConfiguration collector : result){
                    System.out.println("I AM HEREEEE");
                    toolConfiguration.addDataCollectorConfiguration(collector);
                }
            }else{
                System.out.println("Canot het cookie for the dob=" + dob.getName());
            }
        } catch (IOException ex) {
            Logger.getLogger(DLightToolConfigurationProviderFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DLightToolConfigurationProviderFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return        toolConfiguration;
    }

    static Collection<DataCollectorConfiguration> createCollectors(Map map) {
        Collection<DataCollectorConfiguration> result = new ArrayList<DataCollectorConfiguration>();
        FileObject rootFolder = FileUtil.getConfigRoot();
        FileObject collectorsListFolder = rootFolder.getFileObject(getStringValue(map, "collectors"));
        for (FileObject collectorFO : collectorsListFolder.getChildren()){
            try {
                System.out.println("Collector is " + collectorFO);
                DataObject dob = DataObject.find(collectorFO);
                InstanceCookie c = dob.getCookie(InstanceCookie.class);
                if (c != null) {
                    Object o = c.instanceCreate();
                    if (o != null){
                        DataCollectorConfiguration dcc = (DataCollectorConfiguration)o;
                        System.out.println("add dcc: " + dcc) ;
                        result.add(dcc);
                    }

                }
            } catch (IOException ex) {
                Logger.getLogger(DLightToolConfigurationProviderFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DLightToolConfigurationProviderFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    static Collection<IndicatorDataProviderConfiguration> createIdps(Map map) {
        return null;
    }

    private static String getStringValue(Map map, String key) {
        return (String) map.get(key);
    }
}
