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
