/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.downloader;

import java.net.URL;
import org.netbeans.installer.utils.helper.Pair;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.downloader.Pumping.Section;

/**
 *
 * @author Danila_Dugurov
 */
public class DownloadProgress implements DownloadListener{
    private Progress progress;
    private URL      targetUrl;
    
    public DownloadProgress(Progress progress, URL targetUrl) {
        this.progress = progress;
        this.targetUrl = targetUrl;
    }
    
    public void pumpingUpdate(String id) {
        final Pumping pumping = DownloadManager.instance.queue().getById(id);
        
        if ((progress == null) || !targetUrl.equals(pumping.declaredURL())) {
            return;
        }
        
        progress.setDetail("Downloading from " + pumping.declaredURL());
        if (pumping.length() > 0) {
            final long length = pumping.length();
            long per = 0;
            for (Section section: pumping.getSections()) {
                final Pair<Long, Long> pair = section.getRange();
                per += section.offset() - pair.getFirst();
            }
            
            progress.setPercentage((int) (per * Progress.COMPLETE / length));
        }
    }
    
    public void pumpingStateChange(String id) {
        final Pumping pumping = DownloadManager.instance.queue().getById(id);
        if (progress == null) return;
        progress.setDetail(pumping.state().toString().toLowerCase() +": "
                + pumping.declaredURL());
        
    }
    
    public void pumpingAdd(String id) {
    }
    
    public void pumpingDelete(String id) {
    }
    
    public void queueReset() {
    }
    
    public void pumpsInvoke() {
        if (progress == null) return;
        progress.setDetail("downloader invoked.");
    }
    
    public void pumpsTerminate() {
        if (progress == null) return;
        progress.setDetail("ups downloader was switched off.");
    }
}
