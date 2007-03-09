/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;


/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class DuplicateElementRenameDescriptor extends DialogDescriptor
    implements PropertyChangeListener
{
    public DuplicateElementRenameDescriptor(
            DuplicateElementRenamePanel derPanel,
            String title,
            boolean modal,
            int buttonOptionType,
            Object defaultButton,
            int alignType,
            HelpCtx helpCtx, 
            ActionListener listener)
    {
        super(
            derPanel,
            title,
            modal,
            buttonOptionType, 
            defaultButton,
            alignType,
            helpCtx,
            listener);
        
        derPanel.addPropertyChangeListener(this);
    }
    
    private String newName = "";
    
    public final static String PROP_NEW_NAME = "NEW_NAME";
    
    public String getNewName()
    {
        return newName;
    }
    
    private void setNewName(String val)
    {
        newName = val;
    }
            
    
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(PROP_VALID))
        {
            setValid(((Boolean)evt.getNewValue()).booleanValue());
        }
        
        else if (evt.getPropertyName().equals(PROP_NEW_NAME))
        {
            setNewName(evt.getNewValue().toString());
        }
    }
}
