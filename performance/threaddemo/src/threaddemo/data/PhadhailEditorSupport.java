/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.data;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.*;
import java.util.Date;
import org.openide.cookies.*;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.windows.CloneableOpenSupport;
import threaddemo.model.Phadhail;

/**
 * An editor for phadhails.
 * @author Jesse Glick
 */
final class PhadhailEditorSupport extends CloneableEditorSupport implements OpenCookie, CloseCookie, EditorCookie.Observable, PrintCookie {
    
    private final Phadhail ph;
    private SaveCookie modified = null;
    
    public PhadhailEditorSupport(Phadhail ph) {
        this(ph, new PhadhailEnv(ph));
    }
    
    private PhadhailEditorSupport(Phadhail ph, PhadhailEnv env) {
        super(env);
        if (ph.hasChildren()) throw new IllegalArgumentException();
        this.ph = ph;
        env.associate(this);
    }
    
    protected String messageName() {
        // XXX should listen for PhadhailNameEvent too
        String n = ph.getName();
        if (modified != null) {
            n += " *";
        }
        return n;
    }
    
    protected String messageOpened() {
        return "Opened " + ph.getPath() + ".";
    }
    
    protected String messageOpening() {
        return "Opening " + ph.getPath() + "...";
    }
    
    protected String messageSave() {
        return "Saved " + ph.getPath() + ".";
    }
    
    protected String messageToolTip() {
        return ph.getPath();
    }
    
    private static final class PhadhailEnv implements CloneableEditorSupport.Env {
        
        private final Phadhail ph;
        private PhadhailEditorSupport supp;
        private final long createTime;
        private boolean modified;
        
        public PhadhailEnv(Phadhail ph) {
            this.ph = ph;
            createTime = System.currentTimeMillis();
        }
        
        void associate(PhadhailEditorSupport supp) {
            this.supp = supp;
        }
        
        public CloneableOpenSupport findCloneableOpenSupport() {
            return supp;
        }
        
        public String getMimeType() {
            return "text/plain";
        }
        
        public Date getTime() {
            return new Date(createTime);
        }
        
        public InputStream inputStream() throws IOException {
            // XXX does this need to take a lock?
            return ph.getInputStream();
        }
        
        public OutputStream outputStream() throws IOException {
            // XXX ditto?
            return ph.getOutputStream();
        }
        
        public boolean isModified() {
            return supp.modified != null;
        }
        
        public boolean isValid() {
            // XXX a better model for Phadhail would have an isValid method
            // and deleting it would set it to false...
            return true;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void addVetoableChangeListener(VetoableChangeListener l) {
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void removeVetoableChangeListener(VetoableChangeListener l) {
        }
        
        public void markModified() throws IOException {
            // For some mysterious reason this can be called repeatedly?
            //assert supp.modified == null;
            if (supp.modified == null) {
                supp.modified = new Save();
                PhadhailLookups.modified(ph, supp.modified);
            }
        }
        
        private final class Save implements SaveCookie {
            
            public void save() throws IOException {
                supp.saveDocument();
            }
            
            public String toString() {
                return "Save[" + ph + "]";
            }
            
        }
        
        public void unmarkModified() {
            assert supp.modified != null;
            PhadhailLookups.saved(ph, supp.modified);
            supp.modified = null;
        }
        
    }
    
}
