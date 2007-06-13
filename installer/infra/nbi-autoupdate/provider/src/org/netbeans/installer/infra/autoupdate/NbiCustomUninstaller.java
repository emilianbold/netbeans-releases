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
import org.netbeans.api.autoupdate.OperationException.ERROR_TYPE;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.spi.autoupdate.CustomUninstaller;

/**
 *
 * @author ks152834
 */
public class NbiCustomUninstaller implements CustomUninstaller {
    private Product product;
    
    public NbiCustomUninstaller(
            final Product product) {
        this.product = product;
    }

    public boolean uninstall(
            final String name, 
            final String version, 
            final ProgressHandle progressHandle) throws OperationException {
        final Progress uninstallProgress = 
                new Progress(new ProgressHandleAdapter(progressHandle));
        
        try {
            product.uninstall(uninstallProgress);
        } catch (UninstallationException e) {
            throw new OperationException(ERROR_TYPE.UNINSTALL, e);
        }
        
        return true;
    }

}
