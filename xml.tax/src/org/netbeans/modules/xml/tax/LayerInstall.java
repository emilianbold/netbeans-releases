/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.tax;

import org.openide.loaders.DataObject;

import org.netbeans.modules.xml.core.DTDDataObject;
import org.netbeans.modules.xml.core.XMLDataObject;
import org.netbeans.modules.xml.core.XMLDataObjectLook;
import org.netbeans.modules.xml.core.cookies.CookieFactory;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookieImpl;


/**
 * Contains classes that are registered at module layer
 *
 * @author Petr Kuzel
 */
public class LayerInstall {

    public static final class TAXProvider
    implements XMLDataObject.XMLCookieFactoryCreator,
    DTDDataObject.DTDCookieFactoryCreator {

        /**
         */
        public CookieFactory createCookieFactory (DataObject obj) {
            return new TreeEditorCookieImpl.CookieFactoryImpl ((XMLDataObjectLook) obj);
        }

    }


}
