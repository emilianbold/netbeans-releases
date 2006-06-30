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

        RequestProcessor.getDefault().post(
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
