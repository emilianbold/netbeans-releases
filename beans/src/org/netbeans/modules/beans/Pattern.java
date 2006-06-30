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

package org.netbeans.modules.beans;

import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.ErrorManager;
import org.netbeans.jmi.javamodel.JavaClass;

import javax.jmi.reflect.JmiException;

/** Base class for patterns object. These objects hold information
 * about progarammatic patterns i.e. Properties and Events in the source code
 * @author Petr Hrebejk
 */
public abstract class  Pattern extends Object {

    /** PatternAnalyser which created this pattern */
    PatternAnalyser patternAnalyser;
    private DataObject src;

    /** Constructor of Pattern. The patternAnalyser is the only connetion
     * to class which created this pattern.
     * @param patternAnalyser The patern analayser which created this pattern.
     */
    public Pattern( PatternAnalyser patternAnalyser ) {
        this.patternAnalyser = patternAnalyser;
        try {
            this.src = DataObject.find(patternAnalyser.findFileObject());
        } catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
    }

    /** Gets the name of pattern.
     * @return Name of the pattern.
     */
    public abstract String getName();

    /** Sets the name of the pattern
     * @param name New name of the pattern.
     */
    public abstract void setName( String name );

    /** Gets the class which declares this Pattern.
     * @return Class in which this pattern is defined.
     */
    public JavaClass getDeclaringClass() {
        return patternAnalyser.getClassElement();
    }

    /** Temporary implementation of getCookie
     * @param type Type of the Cookie.
     * @return The Cookie.
     */
    Node.Cookie getCookie( Class type ) {
        if (this.src != null && type.isAssignableFrom(this.src.getClass())) {
            return this.src; 
        }
        return null;
    }

    /** Default behavior for destroying pattern is to do nothing
     */
    public void destroy() throws JmiException {
    }

    // UTILITY METHODS ----------------------------------------------------------

    /** Utility method capitalizes the first letter of string, used to
     * generate method names for patterns
     * @param str The string for capitalization.
     * @return String with the first letter capitalized.
     */
    static String capitalizeFirstLetter( String str ) {
        if ( str == null || str.length() <= 0 )
            return str;

        char chars[] = str.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    // IMPLEMENTATION OF PropertyChangeSupport ----------------------------------

    /** Utility field used by bound properties. */
    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport( this );

    /** Add a PropertyChangeListener to the listener list.
     * @param l the listener to add. 
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener( l );
    }

    /** Removes a PropertyChangeListener from the listener list.
     * @param l the listener to remove. 
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener( l );
    }

    /** Fires the <CODE>PropertyChangeEvent</CODE> to listeners.
     * @param evt The event to fire.
     */
    protected void firePropertyChange( java.beans.PropertyChangeEvent evt ) {
        propertyChangeSupport.firePropertyChange( evt );
    }

}
