package org.netbeans.modules.iep.editor.designer.cookies;

import org.netbeans.modules.iep.editor.PlanDataObject;
import org.netbeans.api.xml.cookies.CookieObserver;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.modules.xml.validation.ui.Output;
import org.netbeans.modules.xml.xam.Model;

public class IEPValidateXMLCookie implements ValidateXMLCookie {
    private PlanDataObject dataObject;

    /**
     * Creates a new instance of IEPValidateXMLCookie.
     */
    public IEPValidateXMLCookie(PlanDataObject dobj) {
        dataObject = dobj;
    }

    public boolean validateXML(CookieObserver cookieObserver) {
        new Output().validate(getModel());
        return true;
    }

    protected Model getModel() {
        return dataObject.getPlanEditorSupport().getModel();
    }
}
