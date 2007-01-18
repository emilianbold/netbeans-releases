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


package org.netbeans.modules.properties;


import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import org.openide.util.RequestProcessor.Task;


/**
 * Class for handling structure of a single <tt>.properties</tt> file.
 *
 * @author Petr Hamernik, Petr Jiricka
 * @see PropertiesStructure
 */
public class StructHandler {

    /** Appropriate properties file entry. */
    private PropertiesFileEntry propFileEntry;

    /** Weak reference to parsing task. */
    private WeakReference<Task> parsingTaskWRef
            = new WeakReference<Task>(null);

    /** Soft reference to the underlying properties structure. */
    private SoftReference<PropertiesStructure> propStructureSRef
            = new SoftReference<PropertiesStructure>(null);

    /** Parser performing actual parsing task. */
    private WeakReference<PropertiesParser> parserWRef
            = new WeakReference<PropertiesParser>(null);
    
    /** Flag indicating if parsing isAllowed. */
    private boolean parsingAllowed = true;

    /** Generated serialized version UID. */
    static final long serialVersionUID = -3367087822606643886L;

    
    /**
     * Creates a new handler for a given data object entry.
     *
     * @param  propFileEntry  entry to create a handler for
     */
    public StructHandler(PropertiesFileEntry propFileEntry) {
        this.propFileEntry = propFileEntry;
    }

    
    /** Reparses file. Fires changes. */
    PropertiesStructure reparseNowBlocking() {
        return reparseNowBlocking(true);
    }
    
    /**
     * Reparses file. 
     *
     * @param  fire  true if should fire changes
     */
    private synchronized PropertiesStructure reparseNowBlocking(boolean fire) {
        if (!parsingAllowed) {
            return null;
        }
        
        PropertiesParser parser = new PropertiesParser(propFileEntry);

        try {
            parserWRef = new WeakReference<PropertiesParser>(parser);

            parser.initParser();
            PropertiesStructure propStructure = parser.parseFile();
            
            updatePropertiesStructure(propStructure, fire);
            
            return propStructure;
        } catch (IOException ioe) {
            updatePropertiesStructure(null, fire);
            
            return null;
        } finally {
            parser.clean();
        }
    }
    
    /**
     * Stops parsing and prevent any other sheduled ones.
     * File object is going to be deleted. Due actual delete or move operation.
     */
    synchronized void stopParsing() {
        parsingAllowed = false;
        
        PropertiesParser parser = parserWRef.get();
        
        if (parser != null) {
            parser.stop();
        }
    }
    
    /**
     * Allows parsing when error during deleting or moving occured and
     * the operation didn't succed.
     */
    synchronized void allowParsing() {
        parsingAllowed = true;
    }

    /** Getter for <code>propFileEntry</code> property. */
    public PropertiesFileEntry getEntry() {
        return propFileEntry;
    }

    /**
     * Starts parsing task. Tries to cancel previous parsing task if
     * it is not running yet. Never create new structure if it does not
     * exist.
     */
    void autoParse() {

        if (false == isStructureLoaded()) {
            return;
        }
        Task previousTask = parsingTaskWRef.get();
        if (previousTask != null) {
            // There was previous task already, reschedule it 500 ms later.
            previousTask.schedule(500);
        } else {
            // Create a new task, and schedule it immediatelly.
            parsingTaskWRef = new WeakReference<Task>(
                PropertiesRequestProcessor.getInstance().post(
                    new Runnable() {
                        public void run() {
                            reparseNowBlocking();
                        }
                    }
                )
            );
        }
    }

    /**
     * When the parser finishes its job, it calls this method to set new values.
     *
     * @param newPropStructure new properties structure
     * @param fire if should fire change when structure created anew
     */
    private void updatePropertiesStructure(PropertiesStructure newPropStructure,
                                           boolean fire) {
        if (newPropStructure == null) {
            propStructureSRef = new SoftReference<PropertiesStructure>(null);
            return;
        }
        
        PropertiesStructure propStructure = propStructureSRef.get();

        if (propStructure == null) {
            // Set the parent.
            newPropStructure.setParent(this);
            propStructure = newPropStructure;
            propStructureSRef = new SoftReference<PropertiesStructure>(propStructure);
            
            if (fire) {
                propStructure.structureChanged();
            }
        } else {
            // Update calls notification methods according to changes.
            propStructure.update(newPropStructure);
        }
    }

    /** Gets properties structure handled by this handler. */
    public PropertiesStructure getStructure() {
        PropertiesStructure propStructure = propStructureSRef.get();
        
        if (propStructure != null) {
            return propStructure;
        }
        // No data available -> reparse file.
        // PENDING don't send change event when requesting data only.
        // They could be garbaged before so no fire changes.
        return reparseNowBlocking(false); 
    }

    /**
     * Determine wheteher somebody have already asked for the model.
     */
    private boolean isStructureLoaded() {
        return propStructureSRef.get() != null;
    }
}