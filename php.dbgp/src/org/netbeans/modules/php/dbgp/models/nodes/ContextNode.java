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
package org.netbeans.modules.php.dbgp.models.nodes;

import java.util.List;
import java.util.Set;

import org.netbeans.modules.php.dbgp.ModelNode;
import org.netbeans.modules.php.dbgp.models.VariablesModelFilter.FilterType;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.netbeans.modules.php.dbgp.packets.ContextNamesResponse.Context;


/**
 * Represent context which contains varaibles
 * ( VariableNodes ).
 * Could be Local, Superglobal,...
 * @author ads
 *
 */
public abstract class ContextNode extends AbstractModelNode implements ModelNode {
    
    private final static String SUPER_GLOBAL    = "Superglobals";            // NOI18N 
    
    private static final String SUPER_ICON      =
        "org/netbeans/modules/debugger/resources/watchesView/SuperVariable"; // NOI18N
    
    protected ContextNode(Context ctx , List<Property> properties) {
        super( null , properties );
        myName = ctx.getContext();
        myIndex = ctx.getId();
    }
    
    @Override
    public String getName() {
        return myName;
    }
    
    public int getIndex() {
        return myIndex;
    }
    
    public int getVaraibleSize() {
        return getVariables().size();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.ModelNode#getChildren(int, int)
     */
    @Override
    public ModelNode[] getChildren( int from, int to ) {
        List<AbstractVariableNode> subList = getVariables().subList(from, to);
        return subList.toArray( new ModelNode[ subList.size() ] );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.ModelNode#getChildrenSize()
     */
    @Override
    public int getChildrenSize() {
        return getVariables().size();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.ModelNode#getIconBase()
     */
    @Override
    public String getIconBase() {
        if ( isGlobal() ){
            return SUPER_ICON;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.ModelNode#getShortDescription()
     */
    @Override
    public String getShortDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.ModelNode#getType()
     */
    @Override
    public String getType() {
        return "";
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.ModelNode#getValue()
     */
    @Override
    public String getValue() {
        return "";
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.ModelNode#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.api.ModelNode#isLeaf()
     */
    @Override
    public boolean isLeaf() {
        return getChildrenSize() == 0;
    }

    public boolean equalsTo( ContextNode node ) {
        String name = node.myName;
        if ( name == null ) {
            return myName == null;
        }
        else {
            return name.equals( myName );
        }
    }
    
    public boolean isGlobal(){
        return SUPER_GLOBAL.equals(getDbgpName());
    }
    
    @Override
    protected boolean isTypeApplied( Set<FilterType> set ) {
        if ( !set.contains(FilterType.SUPERGLOBALS) ) {
            return !isGlobal();
        }
        return true;
    }
    
    private String getDbgpName() {
        return myName;
    }
    
    private final String myName;
    
    private final int myIndex;

}
