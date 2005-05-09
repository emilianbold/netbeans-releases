/*
 * TableRemoveListener.java
 *
 * Created on April 22, 2005, 10:43 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.jmx.mbeanwizard.listener;

import javax.swing.event.TableModelEvent;

/**
 *
 * @author an156382
 */
public interface TableRemoveListener {
    
    public void tableStateChanged(TableModelEvent e);
    
}
