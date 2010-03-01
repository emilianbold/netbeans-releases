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

import java.util.Collection;
import java.util.List;

import org.netbeans.modules.php.dbgp.ModelNode;
import org.netbeans.modules.php.dbgp.UnsufficientValueException;
import org.netbeans.modules.php.dbgp.models.VariablesModel;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.netbeans.modules.php.dbgp.packets.PropertyCommand;
import org.netbeans.modules.php.dbgp.packets.PropertyGetCommand;
import org.netbeans.modules.php.dbgp.packets.PropertySetCommand;
import org.netbeans.modules.php.dbgp.packets.PropertyValueCommand;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.openide.text.Line;


/**
 * @author ads
 *
 */
public abstract class AbstractVariableNode extends AbstractModelNode 
    implements VariableNode 
{

    protected static final String FIELD_ICON =
        "org/netbeans/modules/debugger/resources/watchesView/Field"; // NOI18N
    
    
    /**
     * <code>property</code> is not authority for this class.
     * It is used as information.
     * This class is initialized based on <code>property</code>.
     * And it could be updated later based on other property .
     * F.e. one can set  new property via {@link #setProperty(Property)}
     * but this doesn't mean that all  AbstractVariableNode should be 
     * reinitialized due property change.
     * AbstractVariableNode provides its own information that updates by 
     * ( basically children ) adding/removing children.
     * So children in current <code>property</code> and AbstractVariableNode
     * class could be different.    
     */
    protected AbstractVariableNode( Property property , 
            AbstractModelNode parent ) 
    {
        super( parent , property.getChildren() );
        myProperty = property;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.VariableNode#getFullName()
     */
    public String getFullName() {
        return getProperty().getFullName();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.VariableNode#getName()
     */
    public String getName() {
        Property property = getProperty();
        String propertyName = property != null ? property.getName()  : null;
        if ( getParent() instanceof ArrayVariableNode ) {
            StringBuilder builder = new StringBuilder("[");
            builder.append(propertyName);
            builder.append("]");
            return builder.toString();
        }
        return propertyName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.api.ModelNode#getChildren(int, int)
     */
    public ModelNode[] getChildren( int from, int to ) {
        List<AbstractVariableNode> subList = getVariables().subList(from, to);
        return subList.toArray( new ModelNode[ subList.size() ] );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.api.ModelNode#getChildrenSize()
     */
    public int getChildrenSize() {
        return getVariables().size();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.api.ModelNode#getIconBase()
     */
    public String getIconBase() {
        AbstractModelNode node = getParent();
        if ( node instanceof ObjectVariableNode ) {
            return FIELD_ICON;
        }
        return LOCAL_VARIABLE_ICON;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.api.ModelNode#getShortDescription()
     */
    public String getShortDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.api.ModelNode#getType()
     */
    public String getType() {
        return getProperty().getType();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.api.ModelNode#getValue()
     */
    public String getValue() throws UnsufficientValueException {
        return  getProperty().getStringValue();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.api.ModelNode#isReadOnly()
     */
    public boolean isReadOnly() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.VariableNode#findDeclarationLine()
     */
    public Line findDeclarationLine() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.api.ModelNode#isLeaf()
     */
    public boolean isLeaf() {
        return !getProperty().hasChildren();
    }
    
    public void setupCommand( PropertyValueCommand valueCommand ) {
        setupCommand( ( PropertyGetCommand) valueCommand);
        valueCommand.setMaxDataSize( getProperty().getSize() );
        // ? valueCommand.setAddress( getProperty().getAddress());
    }
    
    
    public void setupCommand( PropertyGetCommand getCommand ) {
        setupCommand( (PropertyCommand)getCommand);
        String key = getProperty().getKey();
        if ( key != null ) {
            getCommand.setKey(key);
        }
    }
    
    public void setupCommand( PropertySetCommand command ) {
        setupCommand( (PropertyCommand)command);
        // ? command.setAddress( getProperty().getAddress());
    }
    
    public void setupFillChildrenCommand( PropertyGetCommand getCommand  ){
        setupCommand( getCommand );
        int page = getProperty().getPage() +1;
        getCommand.setDataPage(page);    
    }
    
    public boolean isChildrenFilled(){
        int pageSize = getProperty().getPageSize();
        if ( pageSize == 0 ){
            return true;
        }
        int childrenSize = getProperty().getChildrenSize();
        int page = getProperty().getPage();
        return childrenSize <= (page+1)*pageSize;
    }
        
    public int getContext() {
        return getRootContext().getIndex();
    }

    protected void setProperty( Property property ) {
        Property old = getProperty();
        property.setName( old.getName() );
        myProperty = property;
    }
    
    protected abstract void collectUpdates( VariablesModel variablesModel,
            VariableNode node, Collection<ModelEvent> events );
    
    protected Property getProperty() {
        return myProperty;
    }
    
    private ContextNode getRootContext() {
        AbstractModelNode retval = this;
        while (retval != null && !( retval instanceof ContextNode)) {
            retval = retval.getParent();
        }
        assert retval instanceof ContextNode : retval;
        return (ContextNode)retval;
    }
    
    private void setupCommand(PropertyCommand command) {
        command.setName(getFullName());
        command.setContext( getContext() );
    }
    
    private Property myProperty; 
    
}
