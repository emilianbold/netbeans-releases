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

package threaddemo.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Really, a file.
 * Mutator methods (rename, delete, create*Phadhail) cannot be called
 * from within a listener callback.
 * You *can* add/remove listeners from within a listener callback however.
 * @author Jesse Glick
 */
public interface Phadhail {
    
    /** will be simple file name */
    String getName();
    
    /** will be full path */
    String getPath();
    
    /** rename (within parent) */
    void rename(String nue) throws IOException;
    
    /** will be true if a directory */
    boolean hasChildren();
    
    /**
     * will be child files
     * element type = Phadhail
     * caller cannot mutate list
     * implementor cannot change list after creation (i.e. size & identity of elements)
     * it is expected that once the list is obtained, asking for elements is fast and nonblocking
     */
    List getChildren();
    
    /** delete this phadhail (must not have children) */
    void delete() throws IOException;
    
    /** make a new phadhail without children */
    Phadhail createLeafPhadhail(String name) throws IOException;
    
    /** make a new phadhail with children */
    Phadhail createContainerPhadhail(String name) throws IOException;
    
    /** read */
    InputStream getInputStream() throws IOException;
    
    /** write (note: in this simple model, no locks here) */
    OutputStream getOutputStream() throws IOException;
    
    /** add a listener */
    void addPhadhailListener(PhadhailListener l);
    
    /** remove a listener */
    void removePhadhailListener(PhadhailListener l);
    
}
