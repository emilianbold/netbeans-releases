package org.netbeans.modules.iep.project.anttasks.cli;

import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.netbeans.modules.xml.xam.dom.ReadOnlyAccess;

public class IEPReadOnlyAccessProvider extends ReadOnlyAccess.Provider {

    @Override
    public DocumentModelAccess createModelAccess(AbstractDocumentModel model) {
        return new IEPReadOnlyAccess(model);
    }
}
