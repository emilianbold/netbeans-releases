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

package  org.netbeans.modules.web.taglib;

import org.openide.loaders.*;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;

/** TLD Node
 * @author  mk115033
 */

public class TLDNode extends DataNode {

    private static final boolean debug = false;

    public static final int TLD_NODE        = 0;
    public static final int TAG_NODE        = 2;
    public static final int ATTRIBUTES_NODE = 3;
    public static final int ATTRIBUTE_NODE  = 4;
    public static final int TEI_NODE        = 5;
    public static final int VARIABLE_NODE   = 6;

    ///
    ///   ICON specifications for the various node types.
    ///
    public static final String ICON_BASE_CLASSES = 
        "org/netbeans/modules/web/taglib/default";     //NOI18N
    public static final String ICON_BASE_DEFAULT = 
        "org/netbeans/modules/web/taglib/default";  // NOI18N
    
    public static final String ICON_BASE_TLD = 
        "org/netbeans/modules/web/taglib/resources/tags";        //NOI18N
    public static final String ICON_BASE_TAG = 
        "org/netbeans/modules/web/taglib/resources/tag";         //NOI18N
    public static final String ICON_BASE_ATTRIBUTES = 
        "org/netbeans/modules/web/taglib/resources/attributes";  //NOI18N
    public static final String ICON_BASE_ATTRIBUTE = 
        "org/netbeans/modules/web/taglib/resources/attribute";   //NOI18N
    public static final String ICON_BASE_TEI = 
        "org/netbeans/modules/web/taglib/resources/variables";   //NOI18N
    public static final String ICON_BASE_VARIABLES = 
        "org/netbeans/modules/web/taglib/resources/variables";   //NOI18N
    public static final String ICON_BASE_VARIABLE = 
        "org/netbeans/modules/web/taglib/resources/variable";    //NOI18N

    public TLDNode (final TLDDataObject dataObject) {
	super(dataObject,Children.LEAF);
        setIconBase(ICON_BASE_TLD);
    }
    
    //
    // We return null in createActions to signal that actions should be gotten from getActions(),
    // so that we can vary the actions depending on the state of node.
    //
    
    protected SystemAction[] createActions () {
	return null;
    }

    protected String getIconBase() {
	return getIconBase(TLD_NODE);
    }

    public static String getIconBase(int type) {
	switch (type) {
	case TLD_NODE:
	    return ICON_BASE_TLD;
	case TAG_NODE:
	    return ICON_BASE_TAG;
	case ATTRIBUTES_NODE:
	    return ICON_BASE_ATTRIBUTES;
	case ATTRIBUTE_NODE:
	    return ICON_BASE_ATTRIBUTE;
	case TEI_NODE:
	    return ICON_BASE_TEI;
	case VARIABLE_NODE:
	    return ICON_BASE_VARIABLE;
	    
	}    
	return ICON_BASE_DEFAULT;
    }

    public String getDisplayName() {
	String dn = super.getDisplayName();
	return dn; 
    }

    // test to see if we can use DeleteAction
    public boolean canDestroy() {
	return true;
    }
   
}
