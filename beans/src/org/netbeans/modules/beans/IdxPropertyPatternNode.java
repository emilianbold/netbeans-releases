/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;

import org.openide.nodes.*;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.Method;

import javax.jmi.reflect.JmiException;

/** Node representing a indexed property.
* @see IdxPropertyPattern
* @author Petr Hrebejk
*/
public final class IdxPropertyPatternNode extends PropertyPatternNode  {
    /** Create a new pattern node.
    * @param pattern field element to represent
    * @param writeable <code>true</code> to be writable
    */
    public IdxPropertyPatternNode( IdxPropertyPattern pattern, boolean writeable) {
        super(pattern, writeable);
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
        return PatternNode.getString( "HINT_IndexedProperty" );
    }

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
                       } catch (JmiException e) {
                           throw new InvocationTargetException(e);
                       } finally {
                           pattern.patternAnalyser.setIgnore( false );
                       }

                   }

                   /** Define property editor for this property. */
                   public PropertyEditor getPropertyEditor () {
                       return new PropertyTypeEditor();
                   }
               };
    }

    protected Node.Property createTypeProperty(boolean canW) {
        return new PatternPropertySupport(PROP_TYPE, Type.class, canW) {

                   /** Gets the value */

                   public Object getValue () {
                       return ((PropertyPattern)pattern).getType();
                   }

                   /** Sets the value */
                   public void setValue(Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);
                       if (!(val instanceof Type))
                           throw new IllegalArgumentException();


                       try {
                           pattern.patternAnalyser.setIgnore( true );
                           ((PropertyPattern)pattern).setType((Type)val);
                       } catch (JmiException e) {
                           throw new InvocationTargetException(e);
                       } finally {
                           pattern.patternAnalyser.setIgnore( false );
                       }

                   }

                   public PropertyEditor getPropertyEditor () {
                       return new IdxPropertyTypeEditor();
                   }
               };
    }


    /** Create a property for the getter method.
     * @param canW <code>false</code> to force property to be read-only
     * @return the property
     */
    protected Node.Property createIndexGetterProperty(boolean canW) {
        return new PatternPropertySupport(PROP_INDEXEDGETTER, String.class, canW) {

                   public Object getValue () {
                       Method method = ((IdxPropertyPattern) pattern).getIndexedGetterMethod();
                       return getFormattedMethodName(method);
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
                       Method method = ((IdxPropertyPattern) pattern).getIndexedSetterMethod();
                       return getFormattedMethodName(method);
                   }
               };
    }

}

