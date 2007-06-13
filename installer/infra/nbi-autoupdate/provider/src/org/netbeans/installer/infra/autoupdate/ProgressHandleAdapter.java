package org.netbeans.installer.infra.autoupdate;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.progress.ProgressListener;

/**
 *
 * @author ks152834
 */
public class ProgressHandleAdapter implements ProgressListener {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private ProgressHandle progressHandle;
    
    public ProgressHandleAdapter(
            final ProgressHandle progressHandle) {
        this.progressHandle = progressHandle;
        
        this.progressHandle.start(Progress.COMPLETE);
    }
    
    // progresslistener /////////////////////////////////////////////////////////////
    public void progressUpdated(
            final Progress progress) {
        progressHandle.setDisplayName(
                progress.getTitle());
        progressHandle.progress(
                progress.getDetail(), 
                progress.getPercentage());
    }
}
