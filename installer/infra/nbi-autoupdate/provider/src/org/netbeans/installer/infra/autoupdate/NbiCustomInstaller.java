/*
 * NbiCustomInstaller.java
 *
 * Created on 04.06.2007, 15:41:54
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.installer.infra.autoupdate;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationException.ERROR_TYPE;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.helper.FinishHandler;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.sequences.ProductWizardSequence;
import org.netbeans.installer.wizard.components.actions.CacheEngineAction;
import org.netbeans.spi.autoupdate.CustomInstaller;

/**
 *
 * @author ks152834
 */
public class NbiCustomInstaller implements CustomInstaller {
    
    private Product product;
    
    public NbiCustomInstaller(final Product product) {
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
            final List<WizardComponent> components = new LinkedList<WizardComponent>();
            
            components.add(new CacheEngineAction());
            components.add(new ProductWizardSequence(product));
            
            final Wizard wizard = new Wizard(null, components, -1);
            wizard.setFinishHandler(new FinishHandler() {
                public void cancel() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                
                public void finish() {
                    wizard.close();
                }
                
                public void criticalExit() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
             });
            wizard.openBlocking();
            
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
}
