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
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.netbeans.editor.AnnotationTypes;
import java.lang.Boolean;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/** Node representing the Annotation Types in Options window.
 *
 * @author  David Konecny
 * @since 07/2001
 */
public class AnnotationTypesNode extends AbstractNode {

    private static final String HELP_ID = "editing.annotationtypes"; // !!! NOI18N
    private static final String ICON_BASE = "/org/netbeans/modules/editor/resources/annotationtypes"; // NOI18N
    
    private ResourceBundle bundle;
    
    /** Creates new AnnotationTypesNode */
    public AnnotationTypesNode() {
        super(new AnnotationTypesSubnodes ());
        setName("annotationtypes"); // NOI18N
        bundle = NbBundle.getBundle(AnnotationTypesNode.class);
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

    /** Create properties sheet */
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        
	Sheet.Set ps = sheet.get (Sheet.PROPERTIES);
	if (ps == null) {
	    ps = Sheet.createPropertiesSet ();
	}
        
        ps.put(createProperty(AnnotationTypes.PROP_BACKGROUND_DRAWING, boolean.class)); //NOI18N
        ps.put(createProperty(AnnotationTypes.PROP_BACKGROUND_GLYPH_ALPHA, int.class)); //NOI18N
        ps.put(createProperty(AnnotationTypes.PROP_COMBINE_GLYPHS, boolean.class));    //NOI18N
        ps.put(createProperty(AnnotationTypes.PROP_GLYPHS_OVER_LINE_NUMBERS, boolean.class));    //NOI18N
        sheet.put(ps);
        
        return sheet;
    }
    
    /** Create PropertySupport for given property name and class */
    private PropertySupport createProperty(final String name, final Class clazz) {
        return new PropertySupport.ReadWrite(name, clazz,
            bundle.getString("PROP_" + name),    //NOI18N
            bundle.getString("HINT_" + name)) {  //NOI18N
            public Object getValue() {
                return getProperty(name);
            }
            public void setValue(Object value) {
                setProperty(name, value);
            }
            public boolean supportsDefaultValue() {
                return false;
            }
        };
    }

    /** General setter */
    private void setProperty(String property, Object value) {
        if (property.equals(AnnotationTypes.PROP_BACKGROUND_DRAWING))
            AnnotationTypes.getTypes().setBackgroundDrawing((Boolean)value);
        if (property.equals(AnnotationTypes.PROP_BACKGROUND_GLYPH_ALPHA))
            AnnotationTypes.getTypes().setBackgroundGlyphAlpha(((Integer)value).intValue());
        if (property.equals(AnnotationTypes.PROP_COMBINE_GLYPHS))
            AnnotationTypes.getTypes().setCombineGlyphs((Boolean)value);
        if (property.equals(AnnotationTypes.PROP_GLYPHS_OVER_LINE_NUMBERS))
            AnnotationTypes.getTypes().setGlyphsOverLineNumbers((Boolean)value);
    }

    /** General getter*/
    private Object getProperty(String property) {
        if (property.equals(AnnotationTypes.PROP_BACKGROUND_DRAWING))
            return AnnotationTypes.getTypes().isBackgroundDrawing();
        if (property.equals(AnnotationTypes.PROP_BACKGROUND_GLYPH_ALPHA))
            return AnnotationTypes.getTypes().getBackgroundGlyphAlpha();
        if (property.equals(AnnotationTypes.PROP_COMBINE_GLYPHS))
            return AnnotationTypes.getTypes().isCombineGlyphs();
        if (property.equals(AnnotationTypes.PROP_GLYPHS_OVER_LINE_NUMBERS))
            return AnnotationTypes.getTypes().isGlyphsOverLineNumbers();
        
        return null;
    }
    
    /** Class representing subnodes of AnnotationType node.*/
    private static class AnnotationTypesSubnodes extends Children.Array {

        /** Listener on add/remove of annotation type. */
        private PropertyChangeListener listener;
        
        public AnnotationTypesSubnodes() {
            super();
            AnnotationTypes.getTypes().addPropertyChangeListener( listener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName() == AnnotationTypes.PROP_ANNOTATION_TYPES) {
                        AnnotationTypesSubnodes.this.nodes = initCollection();
                        refresh();
                    }
                }
            });
        }
        
        /** Initialize the collection with results of parsing of "Editors/AnnotationTypes" directory */
        protected java.util.Collection initCollection() {
            
            AnnotationTypesFolder folder = AnnotationTypesFolder.getAnnotationTypesFolder();

            Iterator types = AnnotationTypes.getTypes().getAnnotationTypeNames();

            java.util.List list = new java.util.LinkedList();

            BeanNode bn;
            String icon;

            for( ; types.hasNext(); ) {
                String name = (String)types.next();
                AnnotationType type = AnnotationTypes.getTypes().getType(name);
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
