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
package org.netbeans.modules.xml.tools.generator;

import java.util.*;
import java.beans.*;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.xml.core.actions.CollectXMLAction;
import org.netbeans.modules.xml.core.actions.CollectDTDAction;

import org.netbeans.modules.xml.core.lib.GuiUtil;

public abstract class XMLGenerateAction extends CookieAction {

    /** Stream serialVersionUID as of Build1099j. */
    protected static final long serialVersionUID = -6614874187800576344L;
    
    /* @return the mode of action. */
    protected int mode() {
        return MODE_ALL;
    }

    /* Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public abstract String getName();

    protected Class[] cookieClasses () {
        return new Class[] { XMLGenerateCookie.class };
    }

    protected Class getOwnCookieClass () {
        return XMLGenerateCookie.class;
    }
    
    protected boolean asynchronous() {
        return false;
    }

    /*
     * This code is called from a "Module-actions" thread.
     */
    protected void performAction (final Node[] activatedNodes) {
        try {
            for (int i = 0; i < activatedNodes.length; i++) {
                Class cake = getOwnCookieClass();
                XMLGenerateCookie gc = (XMLGenerateCookie)activatedNodes[i].getCookie (cake);
                if (gc != null) {
                    gc.generate ();
                } else {
                    throw new IllegalStateException("Missing cookie " + cake);
                }
            }
        } catch (RuntimeException ex) {
            String msg = Util.THIS.getString("MSG_action_failed");  //NOI18N
            GuiUtil.notifyException(msg, ex);
        }
    }


    //////////////////////////
    // class GenerateDTDAction
    public static class GenerateDTDAction extends XMLGenerateAction implements CollectXMLAction.XMLAction {
        /** generated Serialized Version UID */
        private static final long serialVersionUID = 8532990650127561962L;

        /* Human presentable name of the action. This should be
         * presented as an item in a menu.
         * @return the name of the action
         */
        public String getName () {
            return Util.THIS.getString ("PROP_GenerateDTD");
        }

        /* Help context where to find more about the action.
         * @return the help context for this action
         */
        public HelpCtx getHelpCtx () {
            return new HelpCtx (GenerateDTDAction.class);
        }

        protected Class getOwnCookieClass () {
            return GenerateDTDSupport.class;
        }
    } // end of inner class GenerateDTDAction

    //////////////////////////////////////
    // class GenerateDocumentHandlerAction
    public static class GenerateDocumentHandlerAction extends XMLGenerateAction implements CollectDTDAction.DTDAction {
        /** generated Serialized Version UID */
        private static final long serialVersionUID = 1342753912956042368L;

        /* Human presentable name of the action. This should be
         * presented as an item in a menu.
         * @return the name of the action
         */
        public String getName () {
            return Util.THIS.getString ("PROP_GenerateSAXHandler");
        }

        /* Help context where to find more about the action.
         * @return the help context for this action
         */
        public HelpCtx getHelpCtx () {
            return new HelpCtx (GenerateDocumentHandlerAction.class);
        }

        protected Class getOwnCookieClass () {
            return SAXGeneratorSupport.class;
        }
    } // end of inner class GenerateDocumentHandlerAction

    /////////////////////////////////
    // class GenerateDOMScannerAction
    public static class GenerateDOMScannerAction extends XMLGenerateAction implements CollectDTDAction.DTDAction {
        /** generated Serialized Version UID */
        private static final long serialVersionUID = 2567846356902367312L;

        /* Human presentable name of the action. This should be
         * presented as an item in a menu.
         * @return the name of the action
         */
        public String getName () {
            return Util.THIS.getString ("PROP_GenerateDOMScanner");
        }

        /* Help context where to find more about the action.
         * @return the help context for this action
         */
        public HelpCtx getHelpCtx () {
            return new HelpCtx (GenerateDOMScannerAction.class);
        }

        protected Class getOwnCookieClass () {
            return GenerateDOMScannerSupport.class;
        }
    } // end of inner class GenerateDOMScannerAction
}
