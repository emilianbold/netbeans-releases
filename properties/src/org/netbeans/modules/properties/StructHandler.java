/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.properties;


import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;


/** Class for handling properties structure of one .properties file.
 *
 * @author Petr Hamernik, Petr Jiricka
 * @see PropertiesStructure
 */
public class StructHandler extends Object {

    /** Appropriate properties file entry. */
    private PropertiesFileEntry propFileEntry;

    /** Weak reference to parsing task. */
    private WeakReference parsingTaskWRef = new WeakReference(null);

    /** Soft reference to the underlying properties structure. */
    private SoftReference propStructureSRef = new SoftReference(null);

    /** Generated serialized version UID. */
    static final long serialVersionUID =-3367087822606643886L;

    
    /** Constructor. */
    public StructHandler(PropertiesFileEntry propFileEntry) {
        this.propFileEntry = propFileEntry;
    }

    
    /** Reparses file. */
    PropertiesStructure reparseNowBlocking() {
        try {
            PropertiesParser parser = new PropertiesParser(propFileEntry);

            PropertiesStructure propStructure = parser.parseFile();
            updatePropertiesStructure(propStructure);
            
            return propStructure;
        } catch (IOException e) {
            updatePropertiesStructure(null);
            
            return null;
        }
    }

    /** Getter for <code>propFileEntry</code> property. */
    public PropertiesFileEntry getEntry() {
        return propFileEntry;
    }

    /** Starts parsing task. Tries to cancel previous parsing task if it is not running yet. */
    void autoParse() {
        // Time to wait before start the parsing task.
        // If no parsing task running yet, set delay time to 0.
        int delayTime = 0;

        Task previousTask = (Task)parsingTaskWRef.get();
        if(previousTask != null) {
            if(!previousTask.cancel() && !previousTask.isFinished())
                // Previous task was not cancelled and is running currently -> next task delay time set to 500 milllis.
                delayTime = 500;
        }

        // Request parsing to start after 'delayTime' milliseconds.
        parsingTaskWRef = new WeakReference(
            RequestProcessor.postRequest(
                new Runnable() {
                    public void run() {
                        reparseNowBlocking();
                    }
                },
                delayTime
            )
        );
    }

    /** When parser finishes its job, it's called this method to set new values.
     *
     * @param newPropStructure new properties structure
     */
    private synchronized void updatePropertiesStructure(PropertiesStructure newPropStructure) {
        if(newPropStructure == null) {
            propStructureSRef = new SoftReference(null);
            return;
        }
        
        PropertiesStructure propStructure = (PropertiesStructure)propStructureSRef.get();

        if(propStructure == null) {
            // Set the parent.
            newPropStructure.setParent(this);
            propStructure = newPropStructure;
            propStructureSRef = new SoftReference(propStructure);
            propStructure.structureChanged();
        } else {
            // Update calls notification methods according to changes.
            propStructure.update(newPropStructure);
        }
    }

    /** Gets properties structure handled by this handler. */
    public PropertiesStructure getStructure() {
        PropertiesStructure propStructure = (PropertiesStructure)propStructureSRef.get();
        
        if(propStructure != null)
            return propStructure;

        // No data available -> reparse file.
        return reparseNowBlocking();
    }

}