package org.netbeans.modules.iep.editor.designer.cookies;

import org.netbeans.modules.iep.editor.PlanDataObject;
import org.netbeans.modules.xml.validation.ValidateXMLCookieImpl;
import org.netbeans.modules.xml.xam.Model;

public class IEPValidateXMLCookie extends ValidateXMLCookieImpl {
    private PlanDataObject dataObject;

    /**
     * Creates a new instance of IEPValidateXMLCookie.
     */
    public IEPValidateXMLCookie(PlanDataObject dobj) {
        dataObject = dobj;
    }

    @Override
    protected Model getModel() {
        return dataObject.getPlanEditorSupport().getModel();
    }
}
