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

package org.netbeans.modules.editor.options;

import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Children;
import org.netbeans.modules.editor.options.AnnotationTypesFolder;
import org.netbeans.editor.AnnotationType;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Node;
import java.util.Iterator;
import java.beans.IntrospectionException;
import org.openide.util.NbBundle;
import java.util.ResourceBundle;

/** Node representing the Annotation Types in Options window.
 *
 * @author  David Konecny
 * @since 07/2001
 */
public class AnnotationTypesNode extends AbstractNode {

    private static final String HELP_ID = "editing.annotationtypes"; // !!! NOI18N
    private static final String ICON_BASE = "/org/netbeans/modules/editor/resources/annotationtypes"; // NOI18N
    
    /** Creates new AnnotationTypesNode */
    public AnnotationTypesNode() {
        super(new AnnotationTypesSubnodes ());
        setName("annotationtypes"); // NOI18N
        ResourceBundle bundle = NbBundle.getBundle(AnnotationTypesNode.class);
        setDisplayName(bundle.getString("ATN_AnnotationTypesNode_Name"));
        setShortDescription (bundle.getString ("ATN_AnnotationTypesNode_Description"));
        setIconBase (ICON_BASE);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
    }
    
    protected SystemAction[] createActions () {
        return new SystemAction[] {
                   SystemAction.get (PropertiesAction.class),
               };
    }
    
    /** Class representing subnodes of AnnotationType node.*/
    private static class AnnotationTypesSubnodes extends Children.Array {

        /** Initialize the collection with results of parsing of "Editors/AnnotationTypes" directory */
        protected java.util.Collection initCollection() {
            
            AnnotationTypesFolder folder = AnnotationTypesFolder.getAnnotationTypesFolder();

            Iterator types = AnnotationType.getAnnotationTypeNames();

            java.util.List list = new java.util.LinkedList();

            BeanNode bn;
            String icon;

            for( ; types.hasNext(); ) {
                String name = (String)types.next();
                AnnotationType type = AnnotationType.getType(name);
                if (!type.isVisible())
                    continue;
                try {
                    bn = new BeanNode(new AnnotationTypeOptions(type));
                } catch (IntrospectionException e) {
                    continue;
                }
                list.add(bn);
                bn.setName(type.getDescription());
                icon = type.getGlyph().getFile();
                icon = icon.substring(0, icon.lastIndexOf('.'));
                bn.setIconBase(icon);
            }

            return list;
        }
        
    }

}
