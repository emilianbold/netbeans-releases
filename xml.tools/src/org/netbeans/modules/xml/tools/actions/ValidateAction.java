/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.tools.actions;

import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.modules.xml.core.actions.CollectXMLAction;
import org.netbeans.modules.xml.core.actions.InputOutputReporter;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;

/**
 * Validates XML file sending results to output window.
 *
 * @author  Petr Kuzel
 */
public class ValidateAction extends CookieAction implements CollectXMLAction.XMLAction {

    /** Serial Version UID */
    private static final long serialVersionUID = -8772119268950444993L;

    
    /** Be hooked on XMLDataObjectLook narking XML nodes. */
    protected Class[] cookieClasses () {
        return new Class[] { ValidateXMLCookie.class };
    }

    /** All selected nodes must be XML one to allow this action */
    protected int mode () {
        return MODE_ALL;
    }

    /** Check all selected nodes. */
    protected void performAction (Node[] nodes) {

        if (nodes == null) return;

        RequestProcessor.postRequest(
               new ValidateAction.RunAction (nodes)
        );
    }

    /** Human presentable name. */
    public String getName() {
        return Util.THIS.getString("NAME_Validate_XML");
    }

    protected String iconResource () {
        return "org/netbeans/modules/xml/tools/resources/validate_xml.png";   // NOI18N
    }

    /** Provide accurate help. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ValidateAction.class);
    }

    protected boolean asynchronous() {
        return false;
    }

    private class RunAction implements Runnable{
        private Node[] nodes;

        RunAction (Node[] nodes){
            this.nodes = nodes;
        }

        public void run() {
            InputOutputReporter console = new InputOutputReporter();
            console.message(Util.THIS.getString("MSG_XML_valid_start"));
            console.moveToFront();
            for (int i = 0; i<nodes.length; i++) {
                Node node = nodes[i];
                ValidateXMLCookie cake = (ValidateXMLCookie) node.getCookie(ValidateXMLCookie.class);
                if (cake == null) continue;
                console.setNode(node); //??? how can console determine which editor to highlight
                cake.validateXML(console);
            }

            console.message(Util.THIS.getString("MSG_XML_valid_end"));
            console.moveToFront(true);
       }
    }
}
