/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.mapper.tree.spi;

import java.util.Iterator;

/**
 * The iterator which can be used multiple times. 
 * 
 * @author nk160297
 */
public interface RestartableIterator<T> extends Iterator<T> {
    
    /**
     * Set iterator to initial state. 
     */
    void restart();

}
