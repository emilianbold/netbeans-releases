/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.loaders;

import java.util.ResourceBundle;
import javax.swing.Action;

import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;

/**
 *  A base class for C/C++/Fortran (C-C-F) nodes. The functionality from
 *  this base class is the renaming of the PROP_name property to show an extension.
 */
public class CndDataNode extends DataNode {

    /** The name property */
    //private static final String PROP_NAME = "name"; // NOI18N

    /** Cache the bundle */
    private static ResourceBundle bundle = NbBundle.getBundle(CndDataNode.class);

    /** Primary File */
    private FileObject primary;


    /** Constructor for this class */
    public CndDataNode(DataObject obj, Children ch) {
	super(obj, ch);
	primary = getDataObject().getPrimaryFile();
    }

    public CndDataNode(DataObject obj, Children ch, String icon) {
	super(obj, ch);
	setIconBaseWithExtension(icon);
    }

    /**
     *  Overrides default action from DataNode.
     *  Instantiate a template, if isTemplate() returns true.
     *  Opens otherwise.
     */
    public Action getPreferredAction() {
	Action result = super.getPreferredAction();
	return result == null ? SystemAction.get(OpenAction.class) : result;
    }

    /** Getter for bundle strings */
    protected static String getString(String prop) {
	return bundle.getString(prop);
    }

    public HelpCtx getHelpCtx() {
	return new HelpCtx("Welcome_cpp_home"); // NOI18N
    }
}
