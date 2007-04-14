/*
 * HudsonChangeListener.java
 *
 * Created on Apr 14, 2007, 9:38:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.hudson.api;

/**
 *
 * @author marigan
 */
public interface HudsonChangeListener {
    
    public void stateChanged();
    
    public void contentChanged();
}
