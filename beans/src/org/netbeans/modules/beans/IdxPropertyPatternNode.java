/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans;

import java.beans.*;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.src.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Node representing a indexed property.
* @see FieldElement
* @author Petr Hrebejk
*/
public class IdxPropertyPatternNode extends PropertyPatternNode  {
    /** Create a new field node.
    * @param element field element to represent
    * @param writeable <code>true</code> to be writable
    */
    public IdxPropertyPatternNode( IdxPropertyPattern pattern, boolean writeable) {
        super(pattern, writeable);
        //BHM
        //setElementFormat (sourceOptions.getFieldElementFormat());
    }

    /* Resolve the current icon base.
    * @return icon base string.
    */
    protected String resolveIconBase() {

        switch (((PropertyPattern)pattern).getMode()) {
        case PropertyPattern.READ_WRITE:
            return IDXPROPERTY_RW;
        case PropertyPattern.READ_ONLY:
            return IDXPROPERTY_RO;
        case PropertyPattern.WRITE_ONLY:
            return IDXPROPERTY_WO;
        default:
            return null;
        }
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (IdxPropertyPatternNode.class);
    }

    /* Creates property set for this node */
    protected Sheet createSheet () {

        Sheet sheet = super.createSheet();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

        ps.put(createIndexedTypeProperty( writeable ));
        ps.put(createIndexGetterProperty( false ));
        ps.put(createIndexSetterProperty( false ));

        return sheet;
    }

    /** Gets the localized string name of property pattern type i.e.
     * "Indexed Property", "Property".
     */
    String getTypeForHint() {
        return bundle.getString( "HINT_IndexedProperty" );
    }

    /* Removes the element from the class and calls superclass.
    *
    * @exception IOException if SourceException is thrown
    *            from the underlayed Element.
    */
    /*
    public void destroy() throws IOException {
      /
      try {
        FieldElement el = (FieldElement) element;
        el.getDeclaringClass().removeField(el);
      }
      catch (SourceException e) {
        throw new IOException(e.getMessage());
      }
      *
      System.out.println ("Pattern should be removed");
      super.destroy();
}
    */
    /** Overrides the default implementation of clone node
    */
    public Node cloneNode() {
        return new IdxPropertyPatternNode((IdxPropertyPattern)pattern, writeable);
    }

    /** Create a property for the indexed property type.
      * @param canW <code>false</code> to force property to be read-only
      * @return the property
      */

    protected Node.Property createIndexedTypeProperty(boolean canW) {
        return new PatternPropertySupport(PROP_INDEXEDTYPE, Type.class, canW) {

                   /** Gets the value */

                   public Object getValue () {
                       return ((IdxPropertyPattern)pattern).getIndexedType();
                   }

                   /** Sets the value */
                   public void setValue(Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);
                       if (!(val instanceof Type))
                           throw new IllegalArgumentException();

                       try {
                           pattern.patternAnalyser.setIgnore( true );
                           ((IdxPropertyPattern)pattern).setIndexedType((Type)val);
                           pattern.patternAnalyser.setIgnore( false );
                       }
                       catch (SourceException e) {
                           throw new InvocationTargetException(e);
                       }

                   }

                   /** Define property editor for this property. */
                   public PropertyEditor getPropertyEditor () {
                       return new org.openide.explorer.propertysheet.editors.TypeEditor();
                   }
               };
    }





    /** Create a property for the getter method.
     * @param canW <code>false</code> to force property to be read-only
     * @return the property
     */

    protected Node.Property createIndexGetterProperty(boolean canW) {
        return new PatternPropertySupport(PROP_INDEXEDGETTER, String.class, canW) {

                   /** Gets the value */

                   public Object getValue () {
                       ElementFormat fmt = new ElementFormat ("{n} ({p})"); // NOI18N
                       MethodElement method = ((IdxPropertyPattern)pattern).getIndexedGetterMethod();
                       if ( method == null )
                           return bundle.getString("LAB_NoMethod");
                       else
                           return (fmt.format (method));
                   }
               };
    }

    /** Create a property for the getter method.
     * @param canW <code>false</code> to force property to be read-only
     * @return the property
     */

    protected Node.Property createIndexSetterProperty(boolean canW) {
        return new PatternPropertySupport(PROP_INDEXEDSETTER, String.class, canW) {

                   /** Gets the value */

                   public Object getValue () {
                       ElementFormat fmt = new ElementFormat ("{n} ({p})"); // NOI18N
                       MethodElement method = ((IdxPropertyPattern)pattern).getIndexedSetterMethod();
                       if ( method == null )
                           return bundle.getString("LAB_NoMethod");
                       else
                           return (fmt.format (method));
                   }
               };
    }

}

/*
* Log
*  6    Gandalf   1.5         1/12/00  Petr Hrebejk    i18n  
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         7/26/99  Petr Hrebejk    Better implementation of 
*       patterns resolving
*  3    Gandalf   1.2         7/8/99   Jesse Glick     Context help.
*  2    Gandalf   1.1         6/30/99  Ian Formanek    Reflecting package 
*       changes of some property editors
*  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
* $
*/
