/*
 * SynchronizedNode.java
 *
 * Created on 28 Èþëü 2006 ã., 10:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.nodes.synchronizer;

import org.netbeans.modules.xml.xam.Component;

/**
 *
 * @author Alexey
 */
public interface SynchronisationListener {
    void componentUpdated(Component component);
    void childrenUpdated(Component component);
    
}
