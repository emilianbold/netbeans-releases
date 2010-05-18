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

package org.netbeans.modules.bpel.model.impl;

import java.util.List;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterImport;
import org.netbeans.modules.bpel.model.xam.AfterSources;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class VariableContainerImpl extends ExtensibleElementsImpl implements
        VariableContainer, AfterImport, AfterSources
{

    VariableContainerImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    VariableContainerImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.VARIABLES.getName() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableContainer#getVariables()
     */
    public Variable[] getVariables() {
        readLock();
        try {
            List<Variable> list = getChildren(Variable.class);
            return list.toArray(new Variable[list.size()]);
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableContainer#getVariable(int)
     */
    public Variable getVariable( int i ) {
        return getChild(Variable.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableContainer#setVariables(org.netbeans.modules.soa.model.bpel20.api.Variable[])
     */
    public void setVariables( Variable[] variables ) {
        setArrayBefore(variables, Variable.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableContainer#removeVariable(int)
     */
    public void removeVariable( int i ) {
        removeChild(Variable.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableContainer#setVariable(org.netbeans.modules.soa.model.bpel20.api.Variable,
     *      int)
     */
    public void setVariable( Variable variable, int i ) {
        setChildAtIndex(variable, Variable.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableContainer#addVariable(org.netbeans.modules.soa.model.bpel20.api.Variable)
     */
    public void addVariable( Variable variable ) {
        addChild(variable, Variable.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableContainer#insertVariable(org.netbeans.modules.soa.model.bpel20.api.Variable,
     *      int)
     */
    public void insertVariable( Variable variable, int i ) {
        insertAtIndex(variable, Variable.class, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.VariableContainer#sizeOfVariable()
     */
    public int sizeOfVariable() {
        readLock();
        try {
            return getChildren(Variable.class).size();
        }
        finally {
            readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return VariableContainer.class;
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#canPaste(org.netbeans.modules.xml.xam.Component)
     */
    @Override
    public boolean canPaste( Component child )
    {
        boolean flag = super.canPaste(child);
        if ( flag && child instanceof Variable ){
            String name = ((Variable) child).getName();
            if ( name == null ){
                return flag;
            }
            Variable[] variables = getVariables();
            for (Variable variable : variables) {
                if ( name.equals( variable.getName()) ){
                    return false;
                }
            }
        }
        return flag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.impl.BpelContainerImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        if (BpelElements.VARIABLE.getName().equals(element.getLocalName())) {
            return new VariableImpl(getModel(), element);
        }
        return super.create( element );
    }
    
}
