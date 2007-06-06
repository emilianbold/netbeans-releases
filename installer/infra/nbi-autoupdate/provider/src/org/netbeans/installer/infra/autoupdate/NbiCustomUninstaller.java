/*
 * NbiCustomUninstaller.java
 * 
 * Created on 04.06.2007, 18:04:11
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.installer.infra.autoupdate;

import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.installer.product.components.Product;
import org.netbeans.spi.autoupdate.CustomUninstaller;

/**
 *
 * @author ks152834
 */
public class NbiCustomUninstaller implements CustomUninstaller {

    public NbiCustomUninstaller(Product product) {
    }

    public boolean uninstall(String arg0, String arg1, ProgressHandle arg2) throws OperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
