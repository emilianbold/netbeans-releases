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
