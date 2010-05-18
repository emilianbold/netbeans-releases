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
package org.netbeans.modules.bpel.model.impl.services;


import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.bpel.model.ext.ExtBpelAttribute;
import org.netbeans.modules.bpel.model.ext.ExtBpelAttribute.ExtBpelAttributeMeta;
import org.netbeans.modules.bpel.model.xam.BpelAttributes.AttrType;

/**
 * @author ads This visitor checks for correctness of name attribute. At least -
 *         we check for absence of spaces in names.
 */
public class CheckExtBpelAttrValueService extends InnerEventDispatcherAdapter {

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    @Override
    public boolean isApplicable( ChangeEvent event ) {
        if (event instanceof PropertyUpdateEvent) {
            PropertyUpdateEvent ev = (PropertyUpdateEvent) event;
            if ( ev.getParent() == null || ev.getParent().getModel().inSync() ){
                return false;
            }
            Object newValue = ev.getNewValue();
            if ( newValue == null ){
                return false;
            }
            // we check only Strings values, but may be event String value will not be checked 
            if ( newValue instanceof String ) {
                ExtBpelAttributeMeta attrMeta = ExtBpelAttribute.forName( ev.getName(), ev.getParent());
                return attrMeta != null && AttrType.NON_NEGATIVE_INTEGER.
                        equals(attrMeta.getAttributeType());
            }
            else {
                return false;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.spi.InnerEventDispatcher#preDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    @Override
    public void preDispatch( ChangeEvent event ) throws VetoException {
        assert event instanceof PropertyUpdateEvent;

        PropertyUpdateEvent ev = (PropertyUpdateEvent) event;

        ExtBpelAttributeMeta attrMeta = ExtBpelAttribute.forName( ev.getName(), ev.getParent());
        AttrType attrType = attrMeta != null ? attrMeta.getAttributeType() : null;
        
        if ( checkNonNegativeInteger( ev, attrType) ){
            return;
        }
    }

    private boolean checkNonNegativeInteger( PropertyUpdateEvent ev, AttrType attrType ) 
        throws VetoException 
    {
        if (  attrType == AttrType.NON_NEGATIVE_INTEGER ){
            // in this case check should be performed over any nonNegativeInteger...
            String newValue = (String) ev.getNewValue();
            try {
                int intNewVal = Integer.parseInt(newValue);
                if (intNewVal < 0) {
                    String str = Utils.getResourceString(
                            Utils.BAD_ATTRIBUTE_INTEGER_NON_NEGATIVE_VALUE, newValue, ev.getName());
                    throw new VetoException(str, ev);
                }
            }
            catch (NumberFormatException e) {
                String str = Utils.getResourceString(
                        Utils.BAD_ATTRIBUTE_INTEGER_VALUE, newValue, ev.getName());
                throw new VetoException(str, ev);
            }
            return true;
        }
        return false;
    }

    private void throwVetoException( PropertyUpdateEvent ev, String newValue )
            throws VetoException
    {
        String str = Utils.getResourceString(Utils.BAD_ATTRIBUTE_VALUE,
                newValue, ev.getName());
        throw new VetoException(str, ev);
    }
}
