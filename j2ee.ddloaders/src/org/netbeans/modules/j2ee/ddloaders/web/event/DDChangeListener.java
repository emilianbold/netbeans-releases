/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web.event;


/** DDChangeEvent can be fired whenever an object that affacts deployment is updated.
 *  You can register to listen on these changes to be notified.
 *
 * @author  Radim Kubacki
 */
public interface DDChangeListener extends java.util.EventListener {
    
    /** This methods gets called when object is changed
     * @param evt - object that describes the change.
     */
    public void deploymentChange (DDChangeEvent evt);
}
