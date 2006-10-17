/*
 * DumpController.java
 *
 * Created on October 17, 2005, 11:52 AM
 *
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
