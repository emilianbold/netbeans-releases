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
 */

package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import org.netbeans.modules.search.types.TextDetail;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 * Fetches text from an Item off the event thread and passes it to a
 * TextReceiever on the event thread.
 *
 * @author Tim Boudreau
 */
final class TextFetcher implements Runnable {
    
    private final Item source;
    private final TextDisplayer textDisplayer;
    private final RequestProcessor.Task task;
    
    /** */
    private TextDetail location;                    //accessed only from the event DT
    /** */
    private boolean done = false;              //accessed only from the event DT
    /** */
    private boolean cancelled = false;         //accessed only from the event DT
    
    /** */
    private volatile String text = null;
    
    /**
     */
    TextFetcher(Item source,
                TextDisplayer receiver,
                RequestProcessor rp) {
        assert EventQueue.isDispatchThread();
        
        this.source = source;
        this.textDisplayer = receiver;
        this.location = source.getLocation();
        task = rp.post(this, 50);
    }

    
    void cancel() {
        assert EventQueue.isDispatchThread();
        
        cancelled = true;
        task.cancel();
    }

    public void run() {
        if (EventQueue.isDispatchThread()) {
            if (cancelled) {
                return;
            }
            
            FileObject fob = FileUtil.toFileObject(
                                                source.matchingObj.getFile());
            String mimeType = fob.getMIMEType();
            //We don't want the swing html editor kit, and even if we 
            //do get it, it will frequently throw a random NPE 
            //in StyleSheet.removeHTMLTags that appears to be a swing bug
            if ("text/html".equals(mimeType)) {                         //NOI18N
                mimeType = "text/plain";                                //NOI18N
            }
            textDisplayer.setText(text,
                                  fob.getMIMEType(),
                                  getLocation());
            done = true;
        }  else {
            
            /* called from the request processor's thread */
            
            if (Thread.interrupted()) {
                return;
            }
            
            String invalidityDescription
                    = source.matchingObj.getInvalidityDescription();
            if (invalidityDescription != null) {
                text = invalidityDescription;
            } else {
                try {
                    text = source.matchingObj.getText();
                } catch (ClosedByInterruptException cbie) {
                    cancelled = true;
                    return;
                } catch (IOException ioe) {
                    text = ioe.getLocalizedMessage();
                    
    //                cancel();
                }
            }
            
            if (Thread.interrupted()) {
                return;
            }
            
            EventQueue.invokeLater(this);
        }
    }
    
    /**
     * If a new request comes to display the same file, just possibly at a
     * different location, simply change the location we're scheduled to
     * display and return true, else return false (in which case we'll be
     * cancelled and a new request will be scheduled).
     * 
     * @param  item  item to be shown
     * @param  receiver  displayer that will actually show the item in the UI
     * @return  {@code true} if the previous item has not been shown yet
     *          and we are about to show the same file, just at a possible
     *          different location;
     *          {@code false} otherwise
     */
    boolean replaceLocation(Item item, TextDisplayer textDisplayer) {
        assert EventQueue.isDispatchThread();
        
        if (done || (textDisplayer != this.textDisplayer)) {
            return false;
        }
        
        boolean result = source.matchingObj.getFile()
                         .equals(item.matchingObj.getFile());
        if (result) {
            setLocation(item.getLocation());
            task.schedule(50);
        }
        return result;
    }

    private synchronized void setLocation(TextDetail location) {
        assert EventQueue.isDispatchThread();
        
        this.location = location;
    }

    private synchronized TextDetail getLocation() {
        assert EventQueue.isDispatchThread();
        
        return location;
    }
}