/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.dbgp.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.logging.Level;

import java.util.logging.Logger;
import javax.swing.JToolTip;

import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.ModelNode;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.modules.php.dbgp.UnsufficientValueException;
import org.netbeans.modules.php.dbgp.models.nodes.AbstractModelNode;
import org.netbeans.modules.php.dbgp.models.nodes.VariableNode;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.netbeans.modules.php.dbgp.packets.PropertyGetCommand;
import org.netbeans.modules.php.dbgp.packets.PropertySetCommand;
import org.netbeans.modules.php.dbgp.packets.PropertyValueCommand;
import org.netbeans.modules.php.dbgp.packets.ContextNamesResponse.Context;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

public class VariablesModel extends ViewModelSupport
        implements TreeModel, TableModel, NodeModel
{

    private static final String EVALUATING    = "TXT_Evaluating";       // NOI18N

    static final String GET_SHORT_DESCRIPTION = "getShortDescription";  // NOI18N

    static final String NULL                  = "null";                 // NOI18N
    //@GuardedBy("this")
    private DebugSession debugSession;

    public VariablesModel(final ContextProvider contextProvider) {
        myContextProvider = contextProvider;
        myNodes = new LinkedList<>();
    }

    @Override
    public void clearModel() {
        myWritelock.lock();
        try {
            myNodes.clear();
        }
        finally {
            myWritelock.unlock();
        }
        fireTreeChanged();
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModel#getRoot()
     */
    @Override
    public Object getRoot() {
        return ROOT; // ROOT is defined by TreeModel
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModel#getChildren(java.lang.Object, int, int)
     */
    @Override
    public Object[] getChildren(Object parent, int from, int to)
        throws UnknownTypeException
    {
        myReadlock.lock();
        try {
            // Should be only two cases -- ROOT or a node from our tree
            ModelNode usedParent = null;
            if (parent == ROOT) {
                List<ModelNode> list = getTopLevelElements();
                if ( from >= list.size() ) {
                    return new Object[0];
                }
                int end = Math.min( list.size() , to);
                List<ModelNode> contexts = list.subList(from, end);
                //Collections.sort( contexts, COMPARATOR );

                return contexts.toArray(new Object[contexts.size()]);
            }
            else if (parent instanceof ModelNode) {
                usedParent = (ModelNode)parent;
            }
            if ( usedParent != null ){
                int size = ((ModelNode) parent).getChildrenSize();
                if ( from >= size ) {
                    return new Object[0];
                }
                int end = Math.min( size , to);
                return ((ModelNode) parent).getChildren(from, end);
            }
        }
        finally {
            myReadlock.unlock();
        }

        throw new UnknownTypeException(parent + " " + parent.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModel#isLeaf(java.lang.Object)
     */
    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == null) {
            return true;
        }
        else if (node == ROOT) {
            return myNodes.isEmpty();
        }
        else if (node instanceof ModelNode) {
            ModelNode modelNode = (ModelNode)node;
            DebugSession session = getSession();
            if (session != null) {
                childrenRequest(modelNode, session);
                fillChildrenList( modelNode , session);
            }
            return modelNode.isLeaf();
        }

        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModel#getChildrenCount(java.lang.Object)
     */
    @Override
    public int getChildrenCount( Object node ) throws UnknownTypeException {
        myReadlock.lock();
        try {
            if (node == ROOT) {
                //return myNodes.size();
                return getTopLevelElements().size();
            }
            else if (node instanceof ModelNode) {
                return ((ModelNode) node).getChildrenSize();
            }

            throw new UnknownTypeException(node);
        }
        finally {
            myReadlock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TableModel#getValueAt(java.lang.Object, java.lang.String)
     */
    @Override
    public Object getValueAt(Object node, String columnID)
        throws UnknownTypeException
    {
        if ( node instanceof JToolTip ) {
            return getTooltip( ((JToolTip) node), columnID);
        }
        String result = ""; // default is blank
        switch (columnID) {
            case Constants.LOCALS_TYPE_COLUMN_ID:
                if (node instanceof ModelNode) {
                    String type = ((ModelNode) node).getType();
                    assert type != null;
                    result = type;
                } else {
                    result = (node != null) ? node.getClass().getName() : "";
                }
                break;
            case Constants.LOCALS_VALUE_COLUMN_ID:
                if (node instanceof ModelNode) {
                    ModelNode modelNode = (ModelNode) node;
                    try {
                        result = modelNode.getValue();
                    } catch (UnsufficientValueException e) {
                        sendValueCommand( modelNode );
                        return NbBundle.getMessage( VariablesModel.class ,
                                EVALUATING);
                    }
                } else if (node == null) {
                    result = "";
                }
                break;
            default:
                //no-op
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TableModel#isReadOnly(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isReadOnly(Object node, String string)
        throws UnknownTypeException
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TableModel#setValueAt(java.lang.Object, java.lang.String, java.lang.Object)
     */
    @Override
    public void setValueAt(Object node, String columnID, Object value)
        throws UnknownTypeException
    {
        assert value instanceof String;

        if (Constants.LOCALS_VALUE_COLUMN_ID.equals(columnID)) {
            if (!(node instanceof VariableNode)) {
                throw new UnknownTypeException(node);
            }

            ModelNode modelNode = (ModelNode)node;

            if ( modelNode.isReadOnly()) {
                throw new UnknownTypeException(node);
            }

            DebugSession session = getSession();
            if ( session == null ){
                // TODO : need signal to user about inability to set value
                return;
            }
            PropertySetCommand command = new PropertySetCommand(
                    session.getTransactionId() );
            command.setData( (String)value );
            assert node instanceof AbstractVariableNode;
            ((AbstractVariableNode)node).setupCommand( command );
            session.sendCommandLater(command);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getDisplayName(java.lang.Object)
     */
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        String retval = null;
        if(node == ROOT) {
            retval = ROOT;
        } else if(node instanceof ModelNode) {
            retval =  ((ModelNode) node).getName();
        } else if (node != null) {
            throw new UnknownTypeException(node);
        }
        if (retval == null && node != null) {
            Logger.getLogger(VariablesModel.class.getName()).log(Level.WARNING, "display name isn''t expected to be null: {0}", node.getClass().getName());
        }
        return (retval != null) ? retval : NULL;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getIconBase(java.lang.Object)
     */
    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        if(node == null || node == ROOT) {
            return VariableNode.LOCAL_VARIABLE_ICON;
        } else if(node instanceof ModelNode) {
            return ((ModelNode) node).getIconBase();
        }
        throw new UnknownTypeException(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeModel#getShortDescription(java.lang.Object)
     */
    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if(node == null || node == ROOT) {
            return null;
        } else if(node instanceof ModelNode) {
            return ((ModelNode) node).getShortDescription();
        }
        throw new UnknownTypeException(node);
    }

    public void updateContext( ContextNode node) {
        myWritelock.lock();
        try {
            if (myNodes.isEmpty()) {
                myNodes.add(node);
            }
            else {
                boolean found = false;
                for (ModelNode child : myNodes) {
                    if (!(child instanceof ContextNode)) {
                        continue;
                    }
                    if (node.equalsTo((ContextNode) child)) {
                        updateContext((ContextNode) child, node);
                        found = true;
                    }
                }
                if (!found) {
                    myNodes.add(node);
                }
            }
            fireTreeChanged();
        }
        finally {
            myWritelock.unlock();
        }
    }

    public void updateProperty( Property property ) {
        myWritelock.lock();
        try {
            for( ModelNode node: myNodes ){
                if ( updateVariable( node , property ) ){
                    return;
                }
            }
        }
        finally {
            myWritelock.unlock();
        }
    }

    private boolean updateVariable( ModelNode node, Property property ) {
        if ( node instanceof AbstractVariableNode ){
            AbstractVariableNode var = (AbstractVariableNode)node;
            String name = var.getFullName();
            final String propertyFullName = property.getFullName();
            String propertyName = property.getName();
            if ((propertyFullName != null  && propertyFullName.equals(name)) || propertyName.equals(name)){
                Collection<ModelEvent> events = new ArrayList<>();
                var.collectUpdates( this , AbstractModelNode.
                        createVariable( property , var.getParent()), events);
                fireTableUpdate(events);
                return true;
            }
        }
        for( ModelNode child : node.getChildren( 0, node.getChildrenSize())){
            if ( updateVariable( child , property) ){
                return true;
            }
        }
        return false;
    }

    private List<ModelNode> getTopLevelElements(){
        List<ModelNode> result = new LinkedList<>();
        for ( ModelNode node :myNodes ){
            if ( node instanceof ContextNode && !((ContextNode) node).isGlobal()){
                result.addAll( Arrays.asList(
                        node.getChildren( 0 , node.getChildrenSize())));
            }
            else {
                result.add( 0, node );
            }
        }
        return result;
    }

    /*
     * This is how tooltips are implemented in the debugger views.
     */
    private String getTooltip( JToolTip tooltip, String columnId )
            throws UnknownTypeException
    {
        Object row = tooltip.getClientProperty(
                VariablesModel.GET_SHORT_DESCRIPTION);
        // TODO
        if ( row instanceof ModelNode ) {
            return getValueAt(row, columnId).toString();
        }
        throw new UnknownTypeException( tooltip );
    }

    private void sendValueCommand( ModelNode modelNode ) {
        if (modelNode instanceof AbstractVariableNode) {
            AbstractVariableNode node = (AbstractVariableNode) modelNode;
            DebugSession session = getSession();
            PropertyValueCommand command = new PropertyValueCommand(session
                    .getTransactionId());
            node.setupCommand( command );
            session.sendCommandLater(command);
        }
    }

    private void updateContext( ContextNode old , ContextNode node ) {
        Collection<ModelEvent> events = new LinkedList<>();
        old.collectUpdates(  this , node , events);
        fireTableUpdate(events);
    }

    private void fireTreeChanged() {
        refresh();
    }

    private void fireTableUpdate(Collection<ModelEvent> events) {
        fireChangeEvents(events);
    }

    private synchronized DebugSession getSession(){
        if (debugSession == null) {
            ContextProvider provider = getContextProvider();
            if ( provider != null ){
                SessionId id = (SessionId)provider.lookupFirst( null , SessionId.class );
                if ( id != null ){
                    debugSession = SessionManager.getInstance().getSession(id);
                }
            }
        }
        return debugSession;
    }

    private ContextProvider getContextProvider() {
        return myContextProvider;
    }

    /**
     * This method should check children availability and request them
     * is they absent originally ( it relates to max_depth option in
     * debugger engine ).
     */
    private void childrenRequest( ModelNode modelNode, DebugSession session ) {
        int size = modelNode.getChildrenSize();
        if ( !modelNode.isLeaf() && size == 0 ) {
            assert modelNode instanceof AbstractVariableNode;
            PropertyGetCommand getCommand = new PropertyGetCommand(
                    session.getTransactionId() );
            ((AbstractVariableNode)modelNode).setupCommand( getCommand );
            session.sendCommandLater( getCommand );
        }
    }


    /**
     * This method should check quantity of retrieved children.
     * It retrieve next page of children if necessarily.
     * This relates to max_children option in debugger engine.
     */
    private void fillChildrenList( ModelNode modelNode, DebugSession session ) {
        if ( !( modelNode instanceof AbstractVariableNode )){
            return;
        }
        AbstractVariableNode var = (AbstractVariableNode) modelNode;
        if (session != null && !var.isChildrenFilled()) {
            PropertyGetCommand command =
                new PropertyGetCommand( session.getTransactionId());
            var.setupFillChildrenCommand( command );
            session.sendCommandLater(command);
        }
    }


    /*private static class ContextOrder implements Comparator<ModelNode> {

        public int compare( ModelNode nodeOne, ModelNode nodeTwo ) {
            assert nodeOne instanceof ContextNode && nodeTwo instanceof ContextNode;
            ContextNode one = (ContextNode) nodeOne;
            ContextNode two = (ContextNode) nodeTwo;
            return one.getIndex()-two.getIndex();
        }

    }*/

    public static class ContextNode extends
        org.netbeans.modules.php.dbgp.models.nodes.ContextNode
    {
        public ContextNode( Context ctx, List<Property> properties ) {
            super(ctx, properties);
        }

        void collectUpdates( VariablesModel variablesModel,
                ContextNode node, Collection<ModelEvent> events )
        {
            boolean hasChanged = false;

            if ( ( getVariables()== null || getVariables().isEmpty() )
                    && node.getVariables() != null)
            {
                setVars( node.getVariables() );
                hasChanged = true;
            }
            else if (getVariables() != null) {
                hasChanged = updateExistedChildren(variablesModel, node, events);

                hasChanged = addAbsentChildren(node) || hasChanged ;
            }

            if (hasChanged) {
                events.add(new ModelEvent.NodeChanged( variablesModel, this));
            }

        }
    }

    public static abstract class AbstractVariableNode extends
        org.netbeans.modules.php.dbgp.models.nodes.AbstractVariableNode
    {
        protected AbstractVariableNode( Property property,
                AbstractModelNode parent )
        {
            super(property, parent);
        }

        @Override
        protected void collectUpdates( VariablesModel variablesModel,
                VariableNode node, Collection<ModelEvent> events )
        {
            AbstractVariableNode newNode = (AbstractVariableNode)node;
            boolean hasChanged = false;
            /*
             * Always update property.
             */
            setProperty(newNode.getProperty());

            if( updatePage( newNode ) ){
                if ( newNode.getChildrenSize() >0 ){
                    events.add(new ModelEvent.NodeChanged( variablesModel, this));
                }
                return;
            }

            if ( !Property.equals(getProperty(), newNode.getProperty() )){
                hasChanged = true;
            }

            if ( (getVariables() == null || getVariables().isEmpty())
                    && newNode.getVariables() != null)
            {
                initVariables( newNode.getProperty().getChildren() );
                hasChanged = true;
            }
            else if (getVariables() != null) {
                hasChanged = updateExistedChildren(variablesModel,
                        newNode, events) || hasChanged;

                hasChanged = addAbsentChildren(newNode) || hasChanged ;
            }

            if (hasChanged) {
                events.add(new ModelEvent.NodeChanged( variablesModel, this));
            }
        }

    }

    //private static final ContextOrder COMPARATOR    = new ContextOrder();

    private final ContextProvider myContextProvider;

    private List<ModelNode> myNodes;

    private ReentrantReadWriteLock myLock = new ReentrantReadWriteLock();

    private ReadLock myReadlock = myLock.readLock();

    private WriteLock myWritelock = myLock.writeLock();

}
