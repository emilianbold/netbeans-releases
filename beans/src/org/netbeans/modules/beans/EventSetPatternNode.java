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
import java.text.MessageFormat;

import org.openide.DialogDisplayer;

import org.openide.NotifyDescriptor;
import org.openide.nodes.*;
import org.openide.util.Utilities;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.Method;

import javax.jmi.reflect.JmiException;

/** Node representing a event set pattern.
* @see EventSetPattern
* @author Petr Hrebejk
*/
public final class EventSetPatternNode extends PatternNode implements IconBases {

    /** Create a new pattern node.
    * @param pattern pattern to represent
    * @param writeable <code>true</code> to be writable
    */
    public EventSetPatternNode( EventSetPattern pattern, boolean writeable) {
        super(pattern, Children.LEAF, writeable);
        superSetName( pattern.getName() );
    }

    protected void setPatternName( String name ) throws JmiException {
        
        if ( pattern.getName().equals( name ) ) {
            return;
        }
        
        if ( testNameValidity(name) ) {
            ((EventSetPattern)pattern).setName(name);
        }
    }

    /** Tests if the given string is valid name for associated pattern and if not, notifies
    * the user.
    * @return true if it is ok.
    */
    boolean testNameValidity( String name ) {

        if (! Utilities.isJavaIdentifier( name ) ) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(getString("MSG_Not_Valid_Identifier"),
                                             NotifyDescriptor.ERROR_MESSAGE) );
            return false;
        }

        if (name.indexOf( "Listener" ) <= 0 ) { // NOI18N
            String msg = MessageFormat.format( getString("FMT_InvalidEventSourceName"),
                                               new Object[] { name } );
            DialogDisplayer.getDefault().notify( new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE) );
            return false;
        }

        return true;
    }


    /** Resolve the current icon base.
    * @return icon base string.
    */
    protected String resolveIconBase() {
        if (((EventSetPattern)pattern).isUnicast() )
            return EVENTSET_UNICAST;
        else
            return EVENTSET_MULTICAST;
    }

    /** Gets the short description of this node.
    * @return A localized short description associated with this node.
    */
    public String getShortDescription() {
        return (((EventSetPattern)pattern).isUnicast () ?
                PatternNode.getString( "HINT_UnicastEventSet" ) :
                PatternNode.getString( "HINT_MulticastEventSet" ) )
               + " : " + getName(); // NOI18N
    }

    /** Creates property set for this node */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

        ps.put(createNameProperty( writeable ));
        ps.put(createTypeProperty( writeable ));
        ps.put(createIsUnicastProperty( writeable ));
        ps.put(createAddListenerProperty( false ));
        ps.put(createRemoveListenerProperty( false ));

        return sheet;
    }

    /** Overrides the default implementation of clone node
     */

    public Node cloneNode() {
        return new EventSetPatternNode((EventSetPattern)pattern, writeable );
    }


    /** Create a property for the field type.
     * @param canW <code>false</code> to force property to be read-only
     * @return the property
     */

    protected Node.Property createTypeProperty(boolean canW) {
        return new PatternPropertySupport(PROP_TYPE, Type.class, canW) {

                   /** Gets the value */

                   public Object getValue () {
                       return ((EventSetPattern)pattern).getType();
                   }

                   /** Sets the value */
                   public void setValue(Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);
                       if (!(val instanceof Type))
                           throw new IllegalArgumentException();

                       try {
                           pattern.patternAnalyser.setIgnore( true );
                           ((EventSetPattern)pattern).setType((Type)val);
                       } catch (JmiException e) {
                           throw new InvocationTargetException(e);
                       } finally {
                           pattern.patternAnalyser.setIgnore( false );
                       }
                   }

                   public PropertyEditor getPropertyEditor () {
                       return new org.netbeans.modules.beans.EventTypeEditor();
                   }
               };
    }


    /** Create a property for the field type.
     * @param canW <code>false</code> to force property to be read-only
     * @return the property
     */


    protected Node.Property createIsUnicastProperty(boolean canW) {
        return new PatternPropertySupport(PROP_ISUNICAST, boolean.class, canW) {

                   /** Gets the value */

                   public Object getValue () {
                       return ((EventSetPattern)pattern).isUnicast() ? Boolean.TRUE : Boolean.FALSE;
                   }

                   /** Sets the value */
                   public void setValue(Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);
                       if (!(val instanceof Boolean))
                           throw new IllegalArgumentException();

                       try {
                           try {
                               pattern.patternAnalyser.setIgnore( true );
                               ((EventSetPattern)pattern).setIsUnicast(((Boolean)val).booleanValue());
                               setIconBase( resolveIconBase() );
                           } finally {
                               pattern.patternAnalyser.setIgnore( false );
                           }
                       } catch (JmiException e) {
                           throw new InvocationTargetException(e);
                       }
                   }

               };
    }

    /** Create a property for the addListener method.
     * @param canW <code>false</code> to force property to be read-only
     * @return the property
     */

    protected Node.Property createAddListenerProperty(boolean canW) {
        return new PatternPropertySupport(PROP_ADDLISTENER, String.class, canW) {

                   public Object getValue () {
                       Method method = ((EventSetPattern) pattern).getAddListenerMethod();
                       return getFormattedMethodName(method);
                   }
               };
    }

    /** Create a property for the removeListener method.
     * @param canW <code>false</code> to force property to be read-only
     * @return the property
     */

    protected Node.Property createRemoveListenerProperty(boolean canW) {
        return new PatternPropertySupport(PROP_REMOVELISTENER, String.class, canW) {

                   public Object getValue () {
                       Method method = ((EventSetPattern) pattern).getRemoveListenerMethod();
                       return getFormattedMethodName(method);
                   }
               };
    }
}

