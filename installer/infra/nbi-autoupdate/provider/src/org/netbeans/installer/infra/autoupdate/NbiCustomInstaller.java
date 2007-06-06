/*
 * NbiCustomInstaller.java
 * 
 * Created on 04.06.2007, 15:41:54
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.installer.infra.autoupdate;

import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.installer.product.components.Product;
import org.netbeans.spi.autoupdate.CustomInstaller;

/**
 *
 * @author ks152834
 */
public class NbiCustomInstaller implements CustomInstaller {

    public NbiCustomInstaller(Product product) {
    }

    public boolean install(String arg0, String arg1, ProgressHandle arg2) throws OperationException {
        return true;
    }

}
