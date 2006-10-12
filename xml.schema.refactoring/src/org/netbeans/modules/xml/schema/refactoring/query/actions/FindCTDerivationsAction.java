/*
 * FindUnusedAction.java
 *
 * Created on July 12, 2006, 3:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query.actions;

import java.io.IOException;
import org.netbeans.modules.xml.schema.refactoring.query.QueryComplexTypeDerivations;
import org.netbeans.modules.xml.schema.refactoring.ui.QueryPanel;
import org.netbeans.modules.xml.schema.ui.basic.SchemaModelCookie;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Jeri Lockhart
 */
public class FindCTDerivationsAction extends CookieAction {
    
    protected void performAction(Node[] node) {
        try {
            assert node.length==1:
                "Length of nodes array should be 1";
            DataObject dobj = (DataObject)node[0].getCookie(DataObject.class);
            SchemaModelCookie modelCookie = (SchemaModelCookie)dobj.getCookie(SchemaModelCookie.class);
            QueryComplexTypeDerivations query = new QueryComplexTypeDerivations(modelCookie.getModel());
            new QueryPanel(query);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
    }

    public String getName() {
        return NbBundle.getMessage(FindCTDerivationsAction.class,"LBL_Find_CTDerivations");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }

    protected int mode() {
	return CookieAction.MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses() {
	return new Class[] {SchemaModelCookie.class};
    }

}
