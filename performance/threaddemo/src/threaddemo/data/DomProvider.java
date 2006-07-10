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

package threaddemo.data;

import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.w3c.dom.Document;
import threaddemo.locking.RWLock;

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
    RWLock lock();
    
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
