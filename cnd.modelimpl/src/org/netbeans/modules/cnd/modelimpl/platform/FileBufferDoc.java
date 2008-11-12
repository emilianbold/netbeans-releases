/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.cnd.modelimpl.platform;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.EventListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.modelimpl.csm.core.AbstractFileBuffer;

/**
 * FileBuffer implementation
 * @author Vladimir Kvashin
 */
public class FileBufferDoc extends AbstractFileBuffer {
    
    private Document doc;
    private EventListenerList listeners = new EventListenerList();
    private DocumentListener docListener;
    private long lastModified;
    
    public FileBufferDoc(File file, Document doc) {
        super(file);
        this.doc = doc;
	resetLastModified();
    }
    
    private void resetLastModified() {
	this.lastModified = System.currentTimeMillis();
    }
    
    private void fireDocumentChanged() {
	resetLastModified();
        EventListener[] list = listeners.getListeners(ChangeListener.class);
        if( list.length > 0 ) {
            ChangeEvent ev = new ChangeEvent(this);
            for( int i = 0; i < list.length; i++ ) {
                ((ChangeListener) list[i]).stateChanged(ev);
            }
        }
        // TODO: think over when do invalidate? before informing listeners or after
        APTDriver.getInstance().invalidateAPT(this);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        if (listeners.getListenerCount() == 0)
        {
            docListener = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    fireDocumentChanged();
                }
                public void removeUpdate(DocumentEvent e) {
                    fireDocumentChanged();
                }
                public void changedUpdate(DocumentEvent e) {
                    // Add/remove annotation shouldn't result in reparse.
                    //fireDocumentChanged();
                }
            };
            doc.addDocumentListener(docListener);
        }
        listeners.add(ChangeListener.class, listener);
    }
    
    @Override
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(ChangeListener.class, listener);
        if (listeners.getListenerCount() == 0)
        {
            doc.removeDocumentListener(docListener);
            docListener = null;
        }
    }
    
    //public boolean isSaved() {
    //}
    
    private IOException convert(BadLocationException e) {
        IOException ioe = new  IOException(e.getMessage());
        ioe.setStackTrace(e.getStackTrace());
        return ioe;
    }

    public InputStream getInputStream() throws IOException {
        
        try { 
            String text = doc.getText(0, doc.getLength());
            return new ByteArrayInputStream(text.getBytes());
        }
        catch( BadLocationException e ) {
            throw convert(e);
        }
    }
    
    public String getText() throws IOException {
        try {
            return doc.getText(0, doc.getLength());
        }
        catch( BadLocationException e ) {
            //e.printStackTrace(System.err);
            throw convert(e);
        }
    }
    
    public String getText(int start, int end) throws IOException {
        try {
            return doc.getText(start, end - start);
        }
        catch( BadLocationException e ) {
            //e.printStackTrace(System.err);
            throw convert(e);
        }
    }
    
    public int getLength() {
        return doc.getLength();
    }

    public boolean isFileBased() {
        return false;
    }

    public long lastModified() {
	return lastModified;
    }
    
}
