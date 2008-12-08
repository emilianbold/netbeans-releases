/*
 * ThreadListChangeListener.java
 *
 * Created on February 14, 2006, 5:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.python.debugger.backend;

/**
 * Just used to populate change in thread list back
 * @author jean-yves
 */
public interface DebuggerContextChangeListener 
{

    /** notify for change => caller will have to call back to get change infos */
    public void fireContextChanged () ;
       
    
}
