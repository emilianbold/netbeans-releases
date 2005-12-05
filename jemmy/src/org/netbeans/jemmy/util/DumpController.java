/*
 * DumpController.java
 *
 * Created on October 17, 2005, 11:52 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.jemmy.util;

import java.awt.Component;

/**
 *
 * @author shura
 */
public interface DumpController {

    public boolean onComponentDump(Component comp);
    public boolean onPropertyDump(Component comp, String name, String value);
    
}
