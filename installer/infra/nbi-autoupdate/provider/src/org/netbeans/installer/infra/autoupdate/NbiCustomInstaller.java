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
import org.netbeans.api.autoupdate.OperationException.ERROR_TYPE;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.progress.ProgressListener;
import org.netbeans.spi.autoupdate.CustomInstaller;

/**
 *
 * @author ks152834
 */
public class NbiCustomInstaller implements CustomInstaller, ProgressListener {
    private Product product;
    
    public NbiCustomInstaller(
            final Product product) {
        this.product = product;
    }
    
    public boolean install(
            final String name,
            final String version,
            final ProgressHandle progressHandle) throws OperationException {
        final CompositeProgress composite =
                new CompositeProgress(new ProgressHandleAdapter(progressHandle));
        
        final Progress logicProgress = new Progress();
        final Progress dataProgress = new Progress();
        final Progress installProgress = new Progress();
        
        composite.addChild(logicProgress, 10);
        composite.addChild(dataProgress, 60);
        composite.addChild(installProgress, 30);
        
        try {
            product.downloadLogic(logicProgress);
            product.downloadData(dataProgress);
            product.install(installProgress);
        } catch (DownloadException e) {
            throw new OperationException(ERROR_TYPE.INSTALL, e);
        } catch (InstallationException e) {
            throw new OperationException(ERROR_TYPE.INSTALL, e);
        }
        
        return true;
    }
    
    public void progressUpdated(Progress arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
