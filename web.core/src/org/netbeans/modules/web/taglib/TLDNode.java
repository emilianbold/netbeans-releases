/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package  org.netbeans.modules.web.taglib;

import org.openide.loaders.*;
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
