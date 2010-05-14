package org.netbeans.modules.iep.project.anttasks.cli;

import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.ReadOnlyAccess;

public class IEPReadOnlyAccess extends ReadOnlyAccess {

    public IEPReadOnlyAccess(AbstractDocumentModel model) {
        super(model);
    }

    @Override
    public String normalizeUndefinedAttributeValue(String value) {
        //see issue http://www.netbeans.org/issues/show_bug.cgi?id=152447
        //return value in 
        //ReadOnlyAccess the behaviour is different
        //from org.netbeans.modules.xml.xdm.xam.XDMAccess
        //which is used in design time
        //so we get null pointer exception in command line 
        //build which uses ReadOnlyAccess
        //so here we make sure behaviour is same as XDMAccess
        return value;
    }
}
