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

import java.net.URI;
import java.net.URISyntaxException;

import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelAttributes.AttrType;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * @author ads This visitor checks for correctness of name attribute. At least -
 *         we check for absence of spaces in names.
 */
public class CheckNameService extends InnerEventDispatcherAdapter {

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
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
                Attribute attr = BpelAttributes.forName( ev.getName() );
                return ( attr instanceof BpelAttributes ) && 
                    (( BpelAttributes) attr).getAttributeType() != AttrType.STRING;
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
    public void preDispatch( ChangeEvent event ) throws VetoException {
        assert event instanceof PropertyUpdateEvent;

        PropertyUpdateEvent ev = (PropertyUpdateEvent) event;

        Attribute attr = BpelAttributes.forName( ev.getName() );
        AttrType attrType = ((BpelAttributes)attr).getAttributeType();
        
        if ( checkURI( ev, attrType) ){
            return;
        }

        if ( attrType != AttrType.NCNAME && attrType != AttrType.VARIABLE )
        {
            return;
        }
        
        checkNCNameAndVariable( ev, attrType );
    }

    private boolean checkURI( PropertyUpdateEvent ev, AttrType attrType ) 
        throws VetoException 
    {
        if (  attrType == AttrType.URI ){
            // in this case check should be performed over anyURI...
            String newValue = (String) ev.getNewValue();
            try {
                new URI(newValue);
            }
            catch (URISyntaxException e) {
                String str = Utils.getResourceString(
                        Utils.BAD_ATTRIBUTE_URI_VALUE, newValue, ev.getName());
                throw new VetoException(str, ev);
            }
            return true;
        }
        return false;
    }

    private void checkNCNameAndVariable( PropertyUpdateEvent ev,
            AttrType attrType ) throws VetoException
    {
        String newValue = (String) ev.getNewValue();
        // check NCName
        if (!Utils.checkNCName(newValue)) {
            throwVetoException(ev, newValue);
        }

        // check BpelVariable
        if (attrType == AttrType.VARIABLE && newValue.indexOf('.') != -1) {
            throwVetoException(ev, newValue);
        }
        
        checkVariableName( ev , newValue );
    }

    private void checkVariableName( PropertyUpdateEvent ev, String value )
            throws VetoException
    {
        if (ev.getParent() instanceof Variable
                && ev.getName().equals(NamedElement.NAME)
                && value.indexOf('.') != -1)
        {
            throwVetoException(ev, value);
        }
    }

    private void throwVetoException( PropertyUpdateEvent ev, String newValue )
            throws VetoException
    {
        String str = Utils.getResourceString(Utils.BAD_ATTRIBUTE_VALUE,
                newValue, ev.getName());
        throw new VetoException(str, ev);
    }
}
