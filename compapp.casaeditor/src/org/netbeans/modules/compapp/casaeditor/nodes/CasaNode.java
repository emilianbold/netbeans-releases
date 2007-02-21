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
package org.netbeans.modules.compapp.casaeditor.nodes;

import java.awt.Image;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.NodeDeleteAction;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * This class represents the base class for Casa related nodes.
 *
 * @author Josh Sandusky
 */
public abstract class CasaNode<T> extends AbstractNode
{
    private WeakReference mDataReference;
    private static Map<Object, Image> mImageMap = new HashMap<Object, Image>();

    
    public CasaNode(Object data, Lookup lookup) {
        this(data, Children.LEAF, lookup);
    }
    
    public CasaNode(Object data, Children children, Lookup lookup) {
        super(children, lookup);
        mDataReference = new WeakReference(data);
    }
    
    /**
     * Looks for the Properties Set by the Group enum.
     * If the group isn't
     */
    protected Sheet.Set getPropertySet(
            Sheet sheet, 
            PropertyUtils.PropertiesGroups group)
    {
        Sheet.Set propSet = sheet.get(group.getDisplayName());
        if (propSet == null) {
            propSet = new Sheet.Set();
            propSet.setName(group.getDisplayName());
            sheet.put(propSet);
        }
        return propSet;
    }
    
    public Object getData() {
        if (mDataReference != null) {
            Object ref = mDataReference.get();
            if (ref instanceof CasaComponent) {
                if (!((CasaComponent) ref).isInDocumentModel()) {
                    return null;
                }
            }
            return ref;
        }
        return null;
    }
    
    public CasaWrapperModel getModel() {
        return (CasaWrapperModel) getLookup().lookup(CasaWrapperModel.class);
    }
    
    public boolean isEditable(String propertyType) {
        return false;
    }
    
    public boolean isDeletable() {
        return false;
    }
    
    public Action[] getActions(boolean context) {
        List actions = new ArrayList();
        Action[] parentActions = super.getActions(context);
        for (Action parentAction : parentActions) {
            actions.add(parentAction);
        }
        
        if (isDeletable()) {
            actions.add(new NodeDeleteAction(this));
        }
        
        return (Action[]) actions.toArray(new Action[actions.size()]);
    }
    
    protected String getBadName() {
        return NbBundle.getMessage(PropertyUtils.class, "PROP_ERROR_VALUE");
    }
    
    protected final Sheet createSheet() {
        Sheet sheet = super.createSheet();
        try {
            setupPropertySheet(sheet);
        } catch (Throwable t) {
            // The user should be informed of any failure
            // during intialization of properties.
            ErrorManager.getDefault().notify(t);
        }
        return sheet;
    }
    
    protected void setupPropertySheet(Sheet sheet) {
        // Subclasses can intialize the sheet if desired.
    }
}
