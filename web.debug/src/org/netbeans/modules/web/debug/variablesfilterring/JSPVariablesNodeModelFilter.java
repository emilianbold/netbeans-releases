/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.debug.variablesfilterring;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.web.debug.variablesfilterring.JSPVariablesFilter.AttributeMap;
import org.netbeans.modules.web.debug.variablesfilterring.JSPVariablesFilter.ImplicitLocals;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 *
 * @author Libor Kotouc
 */
public class JSPVariablesNodeModelFilter implements NodeModelFilter {
    
    /** Creates a new instance of JSPVariablesNodeModelFilter */
    public JSPVariablesNodeModelFilter() {
    }

    /**
     * Returns filterred display name for given node. You should not 
     * throw UnknownTypeException directly from this method!
     *
     * @throws  ComputingException if the display name resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getDisplayName (...)</code> method call only!
     * @return  display name for given node
     */
    public String getDisplayName (NodeModel original, Object node) 
    throws UnknownTypeException 
    {
        
        String dn = "";
        if (node instanceof ImplicitLocals)
            dn =  NbBundle.getMessage(JSPVariablesFilter.class, "LBL_IMPLICIT_LOCALS");
        else if (node instanceof AttributeMap) {
            String resIcon = "";
            String ownerName = ((AttributeMap)node).getOwnerName();
            if (ownerName.equals("request"))
                resIcon = "LBL_REQUEST_ATTRIBUTES";
            else if (ownerName.equals("session"))
                resIcon = "LBL_SESSION_ATTRIBUTES";
            else if (ownerName.equals("application"))
                resIcon = "LBL_APPLICATION_ATTRIBUTES";
            
            dn = NbBundle.getMessage(JSPVariablesFilter.class, resIcon);
        }
        else if (node instanceof AttributeMap.Attribute)
            dn = ((AttributeMap.Attribute)node).getName();
        else
            dn = original.getDisplayName(node);
        
        return dn;
    }
    
    /**
     * Returns filterred icon for given node. You should not throw 
     * UnknownTypeException directly from this method!
     *
     * @throws  ComputingException if the icon resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getIconBase (...)</code> method call only!
     * @return  icon for given node
     */
    public String getIconBase (NodeModel original, Object node)
    throws UnknownTypeException 
    {
        String ib = "";
        if (node instanceof ImplicitLocals)
            ib = NbBundle.getMessage(JSPVariablesFilter.class, "RES_IMPLICIT_LOCALS_GROUP");
        else if (node instanceof AttributeMap)
            ib = NbBundle.getMessage(JSPVariablesFilter.class, "RES_ATTRIBUTES_GROUP");
        else if (node instanceof AttributeMap.Attribute)
            ib = NbBundle.getMessage(JSPVariablesFilter.class, "RES_ATTRIBUTE_VALUE");
        else
            ib = original.getIconBase(node);
                
        return ib;
    }
    
    /**
     * Returns filterred tooltip for given node. You should not throw 
     * UnknownTypeException directly from this method!
     *
     * @throws  ComputingException if the tooltip resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getShortDescription (...)</code> method call only!
     * @return  tooltip for given node
     */
    public String getShortDescription (NodeModel original, Object node)
    throws UnknownTypeException 
    {
        String sd = "";
        if (node instanceof ImplicitLocals)
            sd = NbBundle.getMessage(JSPVariablesFilter.class, "TLT_IMPLICIT_LOCALS");
        else if (node instanceof AttributeMap) {
            String tltAttributes = "";
            String ownerName = ((AttributeMap)node).getOwnerName();
            if (ownerName.equals("request"))
                tltAttributes = "TLT_REQUEST_ATTRIBUTES";
            else if (ownerName.equals("session"))
                tltAttributes = "TLT_SESSION_ATTRIBUTES";
            else if (ownerName.equals("application"))
                tltAttributes = "TLT_APPLICATION_ATTRIBUTES";
            
            sd = NbBundle.getMessage(JSPVariablesFilter.class, "TLT_REQUEST_ATTRIBUTES");
        }
        else if (node instanceof AttributeMap.Attribute) {
            Variable attributeValue = ((AttributeMap.Attribute)node).getValue();
            String type = attributeValue.getType ();
            try {
                String stringValue = attributeValue.getValue();
                if (attributeValue instanceof ObjectVariable)
                    stringValue = ((ObjectVariable)attributeValue).getToStringValue();
                sd = "(" + type + ") " + stringValue;
            } catch (InvalidExpressionException iee) {
                sd = iee.getLocalizedMessage();
            }
        }
        else
            sd = original.getShortDescription(node);
                
        return sd;
    }

    /**
     * 
     * Unregisters given listener.
     * 
     * @param l the listener to remove
     */
    public void removeModelListener(org.netbeans.spi.viewmodel.ModelListener l) {
    }

    /**
     * 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener(org.netbeans.spi.viewmodel.ModelListener l) {
    }

    
}
