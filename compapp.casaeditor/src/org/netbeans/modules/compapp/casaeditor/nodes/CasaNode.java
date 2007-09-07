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
import java.awt.Point;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.NodeDeleteAction;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * This class represents the base class for Casa related nodes.
 *
 * @author Josh Sandusky
 */
public abstract class CasaNode extends AbstractNode
{
    private WeakReference mDataReference;
    private static Map<Object, Image> mImageMap = new HashMap<Object, Image>();

    public CasaNode() {
        super(Children.LEAF);
    }

    public CasaNode(Object data, Children children, CasaNodeFactory factory) {
        this(data, children, factory.createInstanceContent());
    }
    
    private CasaNode(Object data, Children children, InstanceContent content) {
        super(children, new AbstractLookup(content));
        mDataReference = new WeakReference<Object>(data);
        content.add(new SaveCookieDelegate());
    }
    
    
    /**
     * Looks for the Properties Set by the Group enum.
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
    
    public CasaDataObject getDataObject() {
        return getLookup().lookup(CasaDataObject.class);
    }
        
    public CasaWrapperModel getModel() {
        return getLookup().lookup(CasaWrapperModel.class);
    }
    
    public boolean isEditable(String propertyType) {
        return false;
    }
    
    public boolean isDeletable() {
        return false;
    }
    
    protected void addCustomActions(List<Action> actions) {
        // Subclasses can override this to provide custom actions.
    }
    
    public final Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        
        addCustomActions(actions);
        if (actions.size() > 0) {
            actions.add(null);
        }
        
        if (isDeletable()) {
            actions.add(new NodeDeleteAction(this));
            actions.add(null);
        }
        
        Action[] parentActions = super.getActions(context);
        for (Action parentAction : parentActions) {
            actions.add(parentAction);
        }
        
        return (Action[]) actions.toArray(new Action[actions.size()]);
    }
    
    /**
     * If this action is invoked from the graph, this method will be called to determine
     * if the graph location in context is valid for the given action.
     */
    public boolean isValidSceneActionForLocation(Action action, Widget widget, Point sceneLocation) {
        return true;
    }
    
    protected String getBadName() {
        return NbBundle.getMessage(PropertyUtils.class, "PROP_ERROR_VALUE");    // NOI18N
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
    
    class SaveCookieDelegate implements SaveCookie {
        public void save() throws IOException {
            DataObject dobj = getDataObject();
            // May be null if component was removed from the model.
            if (dobj != null) {
                SaveCookie cookie = (SaveCookie) dobj.getCookie(SaveCookie.class);
                if (cookie != null) {
                    cookie.save();
                }
            }
        }
    }
}
