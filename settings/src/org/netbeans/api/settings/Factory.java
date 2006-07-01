/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
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
