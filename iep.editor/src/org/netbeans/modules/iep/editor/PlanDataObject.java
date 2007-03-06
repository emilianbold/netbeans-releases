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

package org.netbeans.modules.iep.editor;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.HelpCtx;

/**
 * Represents a Plan file.
 *
 * @author  Bing Lu
 */
public class PlanDataObject extends MultiDataObject {
    public PlanDataObject(FileObject fObj, MultiFileLoader loader)
        throws DataObjectExistsException {
        super(fObj, loader);

        // add support for viewing a prompt in the IDE
        // getCookieSet().add(new PlanOpenSupport(getPrimaryEntry()));
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you add context help, change to:
        // return new HelpCtx (MyDataObject.class);
    }

    protected Node createNodeDelegate() {
        return new PlanNode(this);
    }

    public void addCookie(Cookie cookie) {
        getCookieSet().add(cookie);
    }

    public void removeCookie(Cookie cookie) {
        getCookieSet().remove(cookie);
    }

    private static final long serialVersionUID = 6338889116068357651L;
}