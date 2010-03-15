/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.spi.support;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author mt154047
 */
public final class DLightToolConfigurationProviderFactory {


    static DLightToolConfigurationProvider create(final Map map) {
        DLightToolConfigurationProvider result = new DLightToolConfigurationProvider() {

            public DLightToolConfiguration create() {
                try {
                    FileObject rootFolder = FileUtil.getConfigRoot();
                    FileObject fo = rootFolder.getFileObject(getStringValue(map, "provider"));
                    DataObject dob = DataObject.find(fo);
                    InstanceCookie c = dob.getCookie(InstanceCookie.class);
                    if (c != null) {
                        Object o = c.instanceCreate();
                        if (o instanceof DLightToolConfiguration) {
                            DLightToolConfiguration result = (DLightToolConfiguration) o;
                            return result;
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(DLightToolConfigurationProviderFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(DLightToolConfigurationProviderFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };
        return result;
    }

    private static String getStringValue(Map map, String key) {
        return (String) map.get(key);

    }
}
