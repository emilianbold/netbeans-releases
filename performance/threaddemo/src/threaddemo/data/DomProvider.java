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

import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.w3c.dom.Document;
import threaddemo.locking.Lock;

/**
 * Cookie for an object with a DOM tree.
 * @author Jesse Glick
 */
public interface DomProvider {
    
    /**
     * Prepare for parsing. If the DOM tree is not already
     * available, parsing will be initiated. To receive notification
     * of completion, attach a listener.
     */
    void start();
    
    /**
     * Get the parsed document (blocking as needed).
     * @throws IOException if it cannot be read or parsed
     */
    Document getDocument() throws IOException;
    
    /**
     * Set the parsed document.
     * @throws IOException if it cannot be written
     */
    void setDocument(Document d) throws IOException;
    
    /**
     * True if the parse is finished and OK (does not block except for lock).
     */
    boolean isReady();
    
    /**
     * Listen for changes in status.
     */
    void addChangeListener(ChangeListener l);

    /**
     * Stop listening for changes in status.
     */
    void removeChangeListener(ChangeListener l);
    
    /**
     * Lock on which to lock while doing things.
     */
    Lock lock();
    
    /**
     * Do an isolated block of operations to the document (must be in the write lock).
     * During this block you may not call any other methods of this interface which
     * require the lock (in read or write mode), or this method itself; you may
     * only adjust the document using DOM mutations.
     * Changes will be fired, and any underlying storage recreated, only when the
     * block is finished (possibly with an error). Does not roll back partial blocks.
     */
    void isolatingChange(Runnable r);

}
