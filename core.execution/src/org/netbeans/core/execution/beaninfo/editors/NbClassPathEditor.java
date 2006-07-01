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

package org.netbeans.core.execution.beaninfo.editors;

import java.awt.*;
import java.beans.*;
import org.openide.execution.NbClassPath;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;

/** A property editor for NbClassPath.
* @author  Jaroslav Tulach
*/
public class NbClassPathEditor extends Object implements ExPropertyEditor {
    private NbClassPath pd;
    private PropertyChangeSupport support;
    private boolean editable = true;

    public NbClassPathEditor () {
        support = new PropertyChangeSupport (this);
    }

    public Object getValue () {
        return pd;
    }

    public void setValue (Object value) {
        Object old = pd;
        pd = (NbClassPath) value;
        support.firePropertyChange ("value", old, pd); // NOI18N
    }

    public String getAsText () {
        if ( pd != null )
            return pd.getClassPath ();
        else
            return "null"; // NOI18N
    }

    public void setAsText (String string) {
        if ( ! "null".equals( string ) )
            setValue (new NbClassPath (string));
    }

    public String getJavaInitializationString () {
        return "new NbClassPath (" + getAsText () + ")"; // NOI18N
    }

    public String[] getTags () {
        return null;
    }

    public boolean isPaintable () {
        return false;
    }

    public void paintValue (Graphics g, Rectangle rectangle) {
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public Component getCustomEditor () {
        return new NbClassPathCustomEditor (this);
    }

    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }

    /** gets information if the text in editor should be editable or not */
    public boolean isEditable(){
        return editable;
    }
    
    public void attachEnv(PropertyEnv env) {
        FeatureDescriptor desc = env.getFeatureDescriptor();
        if (desc instanceof Node.Property){
            Node.Property prop = (Node.Property)desc;
            editable = prop.canWrite();
        }
    }
}
