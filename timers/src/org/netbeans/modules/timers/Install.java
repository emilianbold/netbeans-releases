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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.timers;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.editor.Registry;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInstall;

/**
 *
 * @author nenik
 */
public class Install extends  ModuleInstall {
    static Logger logger;
    private static Handler timers = new TimerHandler();
    private static ChangeListener docTracker = new ActivatedDocumentListener();

    private static String INSTANCES = "Important instances";
    
    public void restored() {
        Logger log = Logger.getLogger("TIMER"); // NOI18N
        log.setUseParentHandlers(false);
        log.setLevel(Level.FINE);
        log.addHandler(timers);
        
        Registry.addChangeListener(docTracker);
    }
    
    private static class TimerHandler extends Handler {
        TimerHandler() {}
    
        public void publish(LogRecord rec) {
            String message = rec.getMessage();
            Object[] args = rec.getParameters();
            if (args == null || args[0] == null) return;
            
            if (args.length == 1) { // simplified instance logging
                TimesCollectorPeer.getDefault().reportReference(
                        INSTANCES, message, message, args[0]);
                return;
            }
            
            if (args.length != 2) return;
            
            Object key = args[0];

            if (args[1] instanceof Number) { // time
                TimesCollectorPeer.getDefault().reportTime(
                        key, message, message, ((Number)args[1]).longValue());
            } else if (args[1] instanceof Boolean) { // start/stop logic
                // XXX - start/stop support
            } else {
                String txt = message.startsWith("[M]") ? message : "[M] " + message;
                TimesCollectorPeer.getDefault().reportReference(
                        key, message, txt, args[1]);
            }
        }
    
        public void flush() {}
        public void close() throws SecurityException {}
    }

    /**
     *
     * @author Jan Lahoda
     */
    private static class ActivatedDocumentListener implements ChangeListener {
        ActivatedDocumentListener() {}

        public synchronized void stateChanged(ChangeEvent e) {
            Document active = Registry.getMostActiveDocument();
            if (active == null) return;

            Object sourceProperty = active.getProperty(Document.StreamDescriptionProperty);
            if (!(sourceProperty instanceof DataObject)) return;

            FileObject activeFile = ((DataObject)sourceProperty).getPrimaryFile();
            TimesCollectorPeer.getDefault().select(activeFile);
        }
    }
   
}
