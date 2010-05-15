package org.netbeans.modules.wsdlextensions.ldap.ldif;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class ImportFileAction extends CookieAction {
    
    protected void performAction(Node[] activatedNodes) {
        DataObject dataObject = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
        // TODO use dataObject
    }
    
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    public String getName() {
        return NbBundle.getMessage(ImportFileAction.class, "CTL_ImportFileAction");
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {
            DataObject.class
        };
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/wsdlextensions/ldap/resources/ldiffile16x16.png";
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
}

