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

package org.netbeans.core.execution.beaninfo.editors;

import java.awt.*;
import java.beans.*;
import org.openide.execution.NbClassPath;
import org.openide.explorer.propertysheet.ExPropertyEditor; 
import org.openide.explorer.propertysheet.PropertyEnv;
import java.beans.FeatureDescriptor;
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
