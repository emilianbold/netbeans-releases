/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
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
import org.netbeans.modules.xml.core.actions.CollectDTDAction;

/**
 * checks DTD file sending results to output window.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class CheckDTDAction extends CookieAction implements CollectDTDAction.DTDAction {

    /** serialVersionUID */
    private static final long serialVersionUID = -8772119268950444992L;

    /** */
    protected Class[] cookieClasses () {
        return new Class[] { DTDDataObject.class };
    }

    /** All selected nodes must be XML one to allow this action */
    protected int mode () {
        return MODE_ALL;
    }

    /** Check all selected nodes. */
    protected void performAction (Node[] nodes) {

        if (nodes == null) return;

        XMLDisplayer output = new XMLDisplayer();
        XMLCompiler comp = new XMLCompiler(output);
        comp.parseDTD(nodes);

        output.display(Util.getString("MSG_DTD_valid_end"), true);
    }

    /** Human presentable name. */
    public String getName() {
        return Util.getString("NAME_Validate_DTD");
    }

    /** Do not slow by any icon. */
    protected String iconResource () {
        return null;
    }

    /** Provide accurate help. */
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx (CheckActionAction.class);
    }

}
