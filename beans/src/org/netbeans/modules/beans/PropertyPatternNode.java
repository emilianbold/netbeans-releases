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
import java.text.Format;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.nodes.*;
import org.openide.util.Utilities;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.modules.java.ui.nodes.SourceNodes;

import javax.jmi.reflect.JmiException;

/** Node representing a field (variable).
* @see PropertyPattern
* @author Petr Hrebejk
*/
public class PropertyPatternNode extends PatternNode implements IconBases {

    /** Create a new pattern node.
    * @param pattern pattern to represent
    * @param writeable <code>true</code> to be writable
    */
    public PropertyPatternNode( PropertyPattern pattern, boolean writeable) {
        super(pattern, Children.LEAF, writeable);
        superSetName( pattern.getName() );
    }

    /* Resolve the current icon base.
    * @return icon base string.
    */
    protected String resolveIconBase() {
        switch (((PropertyPattern)pattern).getMode()) {
        case PropertyPattern.READ_WRITE:
            return PROPERTY_RW;
        case PropertyPattern.READ_ONLY:
            return PROPERTY_RO;
        case PropertyPattern.WRITE_ONLY:
            return PROPERTY_WO;
        default:
            return null;
        }
    }

    /** Gets the localized string name of property pattern type i.e.
     * "Indexed Property", "Property".
     */
    String getTypeForHint() {
        return PatternNode.getString( "HINT_Property" );
    }


    /* Gets the short description of this node.
    * @return A localized short description associated with this node.
    */
    public String getShortDescription() {
        String mode;

        switch( ((PropertyPattern)pattern).getMode() ) {
        case PropertyPattern.READ_WRITE:
            mode = PatternNode.getString("HINT_ReadWriteProperty") ;
            break;
        case PropertyPattern.READ_ONLY:
            mode = PatternNode.getString("HINT_ReadOnlyProperty");
            break;
        case PropertyPattern.WRITE_ONLY:
            mode = PatternNode.getString("HINT_WriteOnlyProperty");
            break;
        default:
            mode = ""; // NOI18N
            break;
        }
        return mode + " " + getTypeForHint() + " : " + getName(); // NOI18N
    }


    /** Creates property set for this node
     */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

        ps.put(createNameProperty( writeable ));
        ps.put(createTypeProperty( writeable ));
        ps.put(createModeProperty( writeable ));
        ps.put(createGetterProperty( false ));
        ps.put(createSetterProperty( false ));
        ps.put(createFieldProperty(false));

        return sheet;
    }

    /** Overrides the default implementation of clone node
     */
    public Node cloneNode() {
        return new PropertyPatternNode((PropertyPattern)pattern, writeable);
    }

    /** Sets the name of pattern
     */
    protected void setPatternName( String name ) throws JmiException {
        
        if ( pattern.getName().equals( name ) ) {
            return;
        }
        
        if (testNameValidity(name)) {
            ((PropertyPattern)pattern).setName( name );
        }
    }

    /** Tests if the given string is valid name for associated pattern and if not, notifies
    * the user.
    * @return true if it is ok.
    */
    boolean testNameValidity( String name ) {

        if (! Utilities.isJavaIdentifier( name ) ) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(PatternNode.getString("MSG_Not_Valid_Identifier"),
                                             NotifyDescriptor.ERROR_MESSAGE) );
            return false;
        }

        return true;
    }

    /** Create a property for the field type.
     * @param canW <code>false</code> to force property to be read-only
     * @return the property
     */
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
                       return new org.netbeans.modules.beans.PropertyTypeEditor();
                   }
               };
    }

    void fire () {
        firePropertyChange( null, null, null );
    }

    /** Create a property for the mode of property pattern.
     * @param canW <code>false</code> to force property to be read-only
     * @return the property
     */
    protected Node.Property createModeProperty(boolean canW) {
        return new PatternPropertySupport(PROP_MODE, int.class, canW) {

                   /** Gets the value */

                   public Object getValue () {
                       return new Integer( ((PropertyPattern)pattern).getMode() );
                   }

                   /** Sets the value */
                   public void setValue(Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);
                       if (!(val instanceof Integer))
                           throw new IllegalArgumentException();

                       pattern.patternAnalyser.setIgnore( true );
                       try {
                           ((PropertyPattern)pattern).setMode(((Integer)val).intValue());
                       } catch (JmiException e) {
                           throw new InvocationTargetException(e);
                       } catch (GenerateBeanException e) {
                           throw new InvocationTargetException(e);
                       } finally {
                           pattern.patternAnalyser.setIgnore( false );
                       }
                       setIconBase( resolveIconBase() );

                   }

                   /** Define property editor for this property. */

                   public PropertyEditor getPropertyEditor () {
                       return new org.netbeans.modules.beans.ModePropertyEditor();
                   }

               };
    }

    /** Create a property for the getter method.
     * @param canW <code>false</code> to force property to be read-only
     * @return the property
     */
    protected Node.Property createGetterProperty(boolean canW) {
        return new PatternPropertySupport(PROP_GETTER, String.class, canW) {

                   public Object getValue () {
                       Method method = ((PropertyPattern) pattern).getGetterMethod();
                       return getFormattedMethodName(method);
                   }
               };
    }

    /** Create a property for the getter method.
     * @param canW <code>false</code> to force property to be read-only
     * @return the property
     */
    protected Node.Property createSetterProperty(boolean canW) {
        return new PatternPropertySupport(PROP_SETTER, String.class, canW) {

                   public Object getValue () {
                       Method method = ((PropertyPattern) pattern).getSetterMethod();
                       return getFormattedMethodName(method);
                   }
               };
    }

    /** Create a property for the estimated filed.
     * @param canW <code>false</code> to force property to be read-only
     * @return the property
     */

    protected Node.Property createFieldProperty(boolean canW) {
        return new PatternPropertySupport(PROP_ESTIMATEDFIELD, String.class, canW) {

                   /** Gets the value */

                   public Object getValue () {
                       Format fmt = SourceNodes.createElementFormat("{t} {n}"); // NOI18N
                       Field field = ((PropertyPattern) pattern).getEstimatedField();
                       String name = null;
                       try {
                           if (field != null) {
                               name = fmt.format(field);
                           }
                       } catch (IllegalArgumentException e) {
                           ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                       }

                       return name != null? name: PatternNode.getString("LAB_NoField"); // NOI18N
                   }
               };
    }
}

