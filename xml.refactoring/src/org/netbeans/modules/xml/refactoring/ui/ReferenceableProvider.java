/*
 * ReferenceableProvider.java
 *
 * Created on June 30, 2006, 11:37 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.refactoring.ui;

import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.nodes.Node;

/**
 *
 * @author Jeri Lockhart
 */
public interface ReferenceableProvider extends Node.Cookie {
    
    /**
     * @returns Referenceable to be used by Refactoring Find Usage, Safe Delete, 
     *          and Rename
     *
     */
    public Referenceable getReferenceable();
    
}
