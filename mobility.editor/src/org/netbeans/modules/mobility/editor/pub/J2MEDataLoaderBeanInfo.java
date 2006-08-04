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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.editor.pub;
import org.openide.ErrorManager;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Utilities;

import java.awt.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.SimpleBeanInfo;

/** BeanInfo for java source loader.
 *
 * @author Petr Suchomel
 */
public final class J2MEDataLoaderBeanInfo extends SimpleBeanInfo {
    
    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo(MultiFileLoader.class) };
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            return null;
        }
    }
    
    /** @param type Desired type of the icon
     * @return returns the Java loader's icon
     */
    public Image getIcon(@SuppressWarnings("unused")
	final int type) {
        return Utilities.loadImage( "org/netbeans/modules/mobility/editor/resources/class.gif"); // NOI18N
    }
    
}
