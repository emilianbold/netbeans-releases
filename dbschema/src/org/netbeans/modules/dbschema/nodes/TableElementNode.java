/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.nodes;

import java.text.MessageFormat;

import org.openide.util.NbBundle;
import org.openide.nodes.*;

import org.netbeans.modules.dbschema.*;

/** Node representing a database table.
 * @see TableElement
 */
public class TableElementNode extends DBElementNode {
    /** Return value of getIconAffectingProperties method. */
    private static final String[] ICON_AFFECTING_PROPERTIES = new String[] {
        PROP_TABLE_OR_VIEW
    };

    /** Create a new class node.
    * @param element class element to represent
    * @param children node children
    * @param writeable <code>true</code> to be writable
    */
    public TableElementNode(TableElement element, Children children, boolean writeable) {
        super(element, children, writeable);
        setDisplayName(MessageFormat.format((element.isTable() ? NbBundle.getBundle("org.netbeans.modules.dbschema.nodes.Bundle_noi18n").getString("SHORT_tableElement") : NbBundle.getBundle("org.netbeans.modules.dbschema.nodes.Bundle_noi18n").getString("SHORT_viewElement")), new Object[]{super.getDisplayName()})); //NOI18N
    }

    /* Resolve the current icon base.
    * @return icon base string.
    */
    protected String resolveIconBase() {
        return ((TableElement)element).isView() ? VIEW : TABLE;
    }

    /* This method is used for resolving the names of the properties,
    * which could affect the icon (such as "modifiers").
    * @return the appropriate array.
    */
    protected String[] getIconAffectingProperties() {
        return ICON_AFFECTING_PROPERTIES;
    }
  
    /* Creates property set for this node */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        ps.put(createNameProperty(writeable));
        ps.put(createTableOrViewProperty(writeable));

        return sheet;
    }

	/** Create a node property representing the element's name.
	 * @param canW if <code>false</code>, property will be read-only
	 * @return the property.
	 */
	protected Node.Property createNameProperty (boolean canW) {
		return new ElementProp(Node.PROP_NAME, String.class,canW) {
			/** Gets the value */
			public Object getValue () {
                String name = ((TableElement) element).getName().getFullName();
                int pos;
                    
                pos = name.lastIndexOf("."); //NOI18N
                if (pos != -1)
                    name = name.substring(pos + 1);
                
                return name;
			}
		};
	}

	/** Create a property for the table or view flag.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createTableOrViewProperty (boolean canW) {
		return new ElementProp(PROP_TABLE_OR_VIEW, String.class, canW) {
			/** Gets the value */
			public Object getValue () {
                if (((TableElement)element).isTableOrView())
                    return NbBundle.getMessage(TableElementNode.class, "Table"); //NOI18N
                else
                    return NbBundle.getMessage(TableElementNode.class, "View"); //NOI18N
			}
		};
	}
}
