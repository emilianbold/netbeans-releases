/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans;

import org.openide.src.ClassElement;
import org.openide.src.SourceException;
import org.openide.nodes.Node;

/** Base class for patterns object. These objects hold information
 * about progarammatic patterns i.e. Properties and Events in the source code
 * @author Petr Hrebejk
 */
public abstract class  Pattern extends Object {

    /** PatternAnalyser which created this pattern */
    PatternAnalyser patternAnalyser;

    /** Constructor of Pattern. The patternAnalyser is the only connetion
     * to class which created this pattern.
     * @param patternAnalyser The patern analayser which created this pattern.
     */
    public Pattern( PatternAnalyser patternAnalyser ) {
        this.patternAnalyser = patternAnalyser;
    }

    /** Gets the name of pattern.
     * @return Name of the pattern.
     */
    public abstract String getName();

    /** Sets the name of the pattern
     * @param name New name of the pattern.
     * @throws SourceException If the change of source code is not possible.
     */
    public abstract void setName( String name ) throws SourceException;

    /** Gets the class which declares this Pattern.
     * @return Class in which this pattern is defined.
     */
    public ClassElement getDeclaringClass() {
        return patternAnalyser.getClassElement();
    }

    /** Temporary implementation of getCookie
     * @param type Type of the Cookie.
     * @return The Cookie.
     */
    Node.Cookie getCookie( Class type ) {
        return null;
    }

    /** Default behavior for destroying pattern is to do nothing
     * @throws SourceException If the modification of source code is impossible.
     */
    public void destroy() throws SourceException {
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

/*
 * Log
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/13/99  Petr Hrebejk    Creating multiple 
 *       Properties/EventSet with the same name vorbiden. Forms made i18n
 *  3    Gandalf   1.2         7/26/99  Petr Hrebejk    Better implementation of
 *       patterns resolving
 *  2    Gandalf   1.1         7/20/99  Petr Hrebejk    
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 