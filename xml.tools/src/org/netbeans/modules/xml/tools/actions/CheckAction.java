/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.actions;

import java.util.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.loaders.*;

import org.netbeans.tax.*;
import org.netbeans.modules.xml.core.*;
import org.netbeans.modules.xml.core.actions.*;

import org.netbeans.api.xml.cookies.*;
import org.openide.util.RequestProcessor;

/**
 * Checks well-formess of XML file sending results to output window.
 *
 * @author  Petr Kuzel
 * @version 1.0
 * @deprecated To be eliminated once a API CheckXMLAction will be intorduces
 */
public class CheckAction extends CookieAction implements CollectXMLAction.XMLAction {

    /** Serial Version UID */
    private static final long serialVersionUID = -4617456591768900199L;

    /** Be hooked on XMLDataObjectLook narking XML nodes. */
    protected Class[] cookieClasses () {
        return new Class[] { CheckXMLCookie.class };
    }

    /** All selected nodes must be XML one to allow this action */
    protected int mode () {
        return MODE_ALL;
    }

    /** Check all selected nodes. */
    protected void performAction (Node[] nodes) {

        if (nodes == null) return;
        
        RequestProcessor.postRequest(
               new CheckAction.RunAction (nodes));
    }

    /** Human presentable name. */
    public String getName() {
        return Util.THIS.getString("NAME_Check_XML");
    }

    /** Do not slow by any icon. */
    protected String iconResource () {
        return "org/netbeans/modules/xml/tools/resources/check_xml.png";      // NOI18N
    }

    /** Provide accurate help. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (CheckAction.class);
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
            console.message(Util.THIS.getString("MSG_XML_check_start"));
            console.moveToFront();
            for (int i = 0; i<nodes.length; i++) {
                Node node = nodes[i];
                CheckXMLCookie cake = (CheckXMLCookie) node.getCookie(CheckXMLCookie.class);
                if (cake == null) continue;
                console.setNode(node); //??? how can console determine which editor to highlight
                cake.checkXML(console);
            }

            console.message(Util.THIS.getString("MSG_XML_check_end"));
            console.moveToFront(true);
       }
    }
}
