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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.ui.editor.property;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.List;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public interface IPropertyGroup extends INode {

    public static final String VALID_ALL = "valid_all";

    /**
     * add a property in this gropu
     * 
     * @param property
     */
    public void addProperty(IProperty property);

    /**
     * add a property change listener
     * 
     * @param listener property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * add a vetoable change listener
     * 
     * @param listener vetoable change listener
     */
    public void addVetoableChangeListener(VetoableChangeListener listener);

    /**
     * get the position where this property should appear in the property sheet gui
     * 
     * @return position
     */
    public int getPosition();

    /**
     * get all the properties in this group
     * 
     * @return all the properties in this group
     */
    public List getProperties();

    /**
     * is valid value
     * 
     * @return valid value
     */
    public boolean isValid();

    /**
     * remove a property change listener
     * 
     * @param listener property change listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * remove a vetoable change listener
     * 
     * @param listener vetoable change listener
     */
    public void removeVetoableChangeListener(VetoableChangeListener listener);

    /**
     * set the position where this property should appear in the property sheet gui
     * 
     * @return position
     */
    public void setPosition(String position);

}
