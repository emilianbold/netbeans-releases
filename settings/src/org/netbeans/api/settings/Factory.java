/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.settings;

/** The factory produces the environment provider for .settings files.
 *
 * @author  Jan Pokorsky
 */
final class Factory {
    
    private Factory() {
    }
    
    /** create a convertor for .settings stored in the xml properties format
     * @param fo the provider registration
     * @return the environment provider
     */
    private static Object properties(org.openide.filesystems.FileObject fo) {
        return org.netbeans.modules.settings.convertors.XMLPropertiesConvertor.create(fo);
    }
    
    /** create a provider for .settings stored in the custom format
     * @param fo the provider registration providing custom convertor
     *  as settings.convertor attribute.
     * @return the environment provider
     */
    private static Object create(org.openide.filesystems.FileObject fo) {
        return org.netbeans.modules.settings.Env.create(fo);
    }
    
}
