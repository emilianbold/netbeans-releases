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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mobility.svgcore;

import org.netbeans.modules.mobility.svgcore.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author breh
 */
public class ExternalEditAction extends CookieAction {

    public ExternalEditAction() {
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    protected Class<?>[] cookieClasses() {
        return new Class[] {
            SVGDataObject.class
        };
    }

    protected void performAction(Node[] activatedNodes) {
        SVGDataObject doj = (SVGDataObject) activatedNodes[0].getLookup().lookup(SVGDataObject.class);
        if (doj != null){            
            final FileObject primaryFile = doj.getPrimaryFile ();
            Util.launchExternalEditor(primaryFile);
        }
    }

    public String getName() {
         return NbBundle.getMessage(ExternalEditAction.class, "CTL_ExternalEditAction");  //NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
