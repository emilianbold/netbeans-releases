/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.myorg.feedreader;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * A top component with an embedded JEditorPane based HTML browser.
 */
final class BrowserTopComponent extends TopComponent {

    /** The cache of opened browser components. */
    private static Map browserComponents = new HashMap();

    private final JScrollPane scrollPane;
    private final JEditorPane editorPane;
    
    private final String title;
    private String url;
    
    private BrowserTopComponent(String title) {
        this.title = title;
        setName(title);
        setToolTipText(NbBundle.getMessage(BrowserTopComponent.class, "HINT_BrowserTopComponent"));
        
        scrollPane = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();
        
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        
        setLayout(new java.awt.BorderLayout());
        scrollPane.setViewportView(editorPane);
        add(scrollPane, java.awt.BorderLayout.CENTER);
    }
    
    
    public static synchronized BrowserTopComponent getBrowserComponent(String title) {
        BrowserTopComponent win = (BrowserTopComponent) browserComponents.get(title);
        if (win == null) {
            win = new BrowserTopComponent(title);
            browserComponents.put(title, win);
        }
        return win;
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    public void componentOpened() {
        // TODO add custom code on component opening
    }
    
    public synchronized void componentClosed() {
        browserComponents.remove(title);
    }
    
    public void setPage(String url) {
        this.url = url;
        try {
            editorPane.setPage(new URL(this.url));
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
}
