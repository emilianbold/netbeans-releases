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

package org.netbeans.beaninfo.editors;

import java.awt.Image;
import java.awt.Color;
import java.awt.FontMetrics;
import java.beans.PropertyEditorSupport;
import java.beans.FeatureDescriptor;

import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.nodes.Node;

import org.netbeans.core.projects.SettingChildren;

/** Editor for property of enumerated integers, each integer should
 * have associated image displayed as a property value. It's possible
 * to associate descriptions for each value which is then shown in combobox
 * when property is edited.
 *
 * @author  Vitezslav Stejskal
 */
public class ListImageEditor extends PropertyEditorSupport implements ExPropertyEditor {

    public static final String PROP_IMAGES = "images"; //NOI18N
    public static final String PROP_VALUES = "values"; //NOI18N
    public static final String PROP_DESCRIPTIONS = "descriptions"; //NOI18N
    
    private PropertyEnv env = null;
    private boolean canWrite = true;
    private boolean canRead = true;

    private Image [] images = null;
    private Integer [] values = null;
    private String [] descriptions = null;

    /** Creates new ListEditor */
    public ListImageEditor () {
        super ();
    }

    public void attachEnv (PropertyEnv env) {
        this.env = env;
        FeatureDescriptor d = env.getFeatureDescriptor ();
        if (d instanceof Node.Property) {
            canWrite = ((Node.Property)d).canWrite ();
            canRead = ((Node.Property)d).canRead ();
        }
        
        Image imgs [] = (Image [])d.getValue (PROP_IMAGES);
        Integer vals [] = (Integer [])d.getValue (PROP_VALUES);
        String descs [] = (String [])d.getValue (PROP_DESCRIPTIONS);
        
        int length = imgs.length < vals.length ? imgs.length : vals.length;
        if (descs != null && descs.length < length)
            length = descs.length;
        
        images = new Image [length];
        values = new Integer [length];
        descriptions = new String [length];

        for (int i = 0; i < length; i++) {
            images [i] = imgs [i];
            values [i] = vals [i];
            descriptions [i] = descs == null ? vals [i].toString () : descs [i];
        }
    }
    
    public boolean isEditable () {
        return canWrite;
    }
    
    public String getAsText () {
        int i = findIndex (values, getValue ());
        return (String) findObject (descriptions, i);
    }
    
    public void setAsText (String str) throws java.lang.IllegalArgumentException {
        int i = findIndex (descriptions, str);
        if (i == -1)
            throw new IllegalArgumentException ();
        
        setValue (findObject (values, i));
    }
    
    public String[] getTags () {
        return descriptions;
    }

    public boolean isPaintable () {
        return true;
    }
    
    public void paintValue (java.awt.Graphics g, java.awt.Rectangle rectangle) {
        int px;
        Image img = (Image) findObject (images, findIndex (values, getValue ()));
        
        g.drawImage (img,
            rectangle.x + (rectangle.width - img.getWidth (null))/ 2,
            rectangle.y + (rectangle.height - img.getHeight (null))/ 2, 
            img.getWidth (null),
            img.getHeight (null),
            null);
    }
    
    public String getJavaInitializationString () {
        return "new Integer(" + getValue () + ")";
    }
    
    public java.awt.Component getCustomEditor () {
        return null;
    }
    
    public boolean supportsCustomEditor () {
        return false;
    }

    private Object findObject (Object [] objs, int i) {
        if (objs == null || i < 0 || i >= objs.length)
            return null;
        
        return objs[i];
    }
    
    private int findIndex (Object [] objs, Object obj) {
        if (objs != null) {
            for ( int i = 0; i < objs.length; i++) {
                if (objs[i].equals (obj))
                    return i;
            }
        }
        return -1;
    }
}
