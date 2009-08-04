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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;
import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.AnotherVersionBpelProcess;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.events.ChangeEventSupport;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.RefCacheSupport;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.model.impl.events.TreeCreatedEvent;
import org.netbeans.modules.bpel.model.spi.EntityFactory;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class BpelModelImpl extends AbstractDocumentModel<BpelEntity> implements BpelModel {
    
    public BpelModelImpl( Document doc, Lookup lookup  ) {
        // TODO this is temporary constructor and this will be removed later.
        this(getOrCreateModelSource(doc, lookup));
    }
    
    public BpelModelImpl( ModelSource source){
        super( source );
        init( );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelModel#getProcess()
     */
    public ProcessImpl getProcess() {
        readLock();
        try {
            return (ProcessImpl) getRootComponent();
        }
        finally {
            readUnlock();
        }
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelModel#getBuilder()
     */
    public BpelBuilderImpl getBuilder() {
        return myBuilder;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelModel#getEntity(org.netbeans.modules.soa.model.bpel20.api.support.UniqueId)
     */
    public BpelEntity getEntity( UniqueId id ) {
        readLock();
        try {
            BpelEntityImpl impl = ((UniqueIdImpl) id).getEntity();
            if ( impl.isDeleted() ){
                return null;
            }
            return impl;
        }
        finally {
            readUnlock();
        }
    }

    //==========================================================================
    //===========   need to override default impl in AbstractModel =============
    //==========================================================================
    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl){
        myPropertyChangeSupport.removePropertyChangeListener(pcl);
    }
    
    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        myPropertyChangeSupport.addPropertyChangeListener(pcl);
    }
    
    @Override
    public void removeComponentListener(ComponentListener cl) {
        myComponentListeners.remove(ComponentListener.class, cl);
    }

    @Override
    public void addComponentListener(ComponentListener cl) {
        myComponentListeners.add(ComponentListener.class, cl);
    }
    //==========================================================================

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelModel#addPropertyChangeListener(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEventListener)
     */
    public void addEntityChangeListener( ChangeEventListener listener ) {
        mySupport.addChangeEventListener(listener);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelModel#removePropertyChangeListener(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEventListener)
     */
    public void removeEntityChangeListener( ChangeEventListener listener ) {
        mySupport.removeChangeEventListener(listener);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.Model#addUndoableEditListener(javax.swing.event.UndoableEditListener)
     */
    @Override
    public void addUndoableEditListener( UndoableEditListener listener ) {
        myUndoSupport.addUndoableEditListener(listener);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.Model#removeUndoableEditListener(javax.swing.event.UndoableEditListener)
     */
    @Override
    public void removeUndoableEditListener( UndoableEditListener listener ) {
        myUndoSupport.removeUndoableEditListener(listener);
    }
    
    //=============================================================================
    
    @Override
    public synchronized void addUndoableRefactorListener(
            UndoableEditListener listener) 
    {
        mySavedUndoableEditListeners = myUndoSupport.getUndoableEditListeners();
        if (mySavedUndoableEditListeners != null) {
            for (UndoableEditListener saved : mySavedUndoableEditListeners) {
                if (saved instanceof UndoManager) {
                    ((UndoManager)saved).discardAllEdits();
                }
            }
        }
        myUndoSupport = new BpelModelUndoableEditSupport();
        myUndoSupport.addUndoableEditListener(listener);
    }
    
    @Override
    public synchronized void removeUndoableRefactorListener(
            UndoableEditListener listener)
    {
        myUndoSupport.removeUndoableEditListener(listener);
        myUndoSupport = new BpelModelUndoableEditSupport();
        if (mySavedUndoableEditListeners != null) {
            for (UndoableEditListener saved : mySavedUndoableEditListeners) {
                myUndoSupport.addUndoableEditListener(saved);
            }
            mySavedUndoableEditListeners = null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelModel#invoke(java.util.concurrent.Callable, java.lang.Object)
     */
    public <V> V invoke( Callable<V> action, Object source ) throws Exception {
        /*
         * This just start transcation.
         * It could be started for safe OM packet reading.
         * In this case muatation will not appear.
         * So we set flag in writeLock in true.
         * Any muation method will call writeLock()
         * with flag equals to false, so we can check 
         * trying to mutate OM when it should not be possible.
         */
        writeLock( true , false ); 
        try {
            // mySource contains source of event.
            assert myTransaction != null;
            myTransaction.setSource(source);
            V v = action.call();

            return v;
        }
        finally {
            writeUnlock();
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelModel#invoke(java.lang.Runnable)
     */
    public void invoke( Runnable run ) {
        readLock();
        try {
            myReadLockObtained.set( true );
            run.run();
        }
        finally {
            myReadLockObtained.set( false );
            readUnlock();
        }
        
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.AbstractModel#createRootComponent(org.w3c.dom.Element)
     */
    @Override
    public BpelEntity createRootComponent( Element root )
    {
        String namespace = root.getNamespaceURI();
        if ( BpelEntity.BUSINESS_PROCESS_NS_URI.equals(namespace) &&
               BpelElements.PROCESS.getName().equals( root.getLocalName() ) ) 
        {
            myProcess = new ProcessImpl(this, root);
            myAnotherRoot = null;
            return myProcess;
        } 
        else {
            myAnotherRoot = root;
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.Model#getRootComponent()
     */
    public BpelEntity getRootComponent() {
        return myProcess;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.AbstractModel#validateWrite()
     */
    @Override
    public void validateWrite()
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.AbstractModel#firePropertyChangeEvent(java.beans.PropertyChangeEvent)
     */
    @Override
    public void firePropertyChangeEvent( PropertyChangeEvent event )
    {
        /*
         *  We don't collect events for firing them in the end of transaction.
         *  We fire each event right away. May be this is subject for change
         *  appropriate to firing model specific events.
         *  
         *  We suppose actually that clients of model will use normal BPEL OM
         *  specific events. So this is just for compatibility. 
         */ 
        myPropertyChangeSupport.firePropertyChange( event );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.AbstractModel#fireComponentChangedEvent(org.netbeans.modules.xml.xam.ComponentEvent)
     */
    @Override
    public void fireComponentChangedEvent( ComponentEvent evt )
    {
        /*
         *  We don't collect events for firing them in the end of transaction.
         *  We fire each event right away. May be this is subject for change
         *  appropriate to firing model specific events.
         *  
         *  We suppose actually that clients of model will use normal BPEL OM
         *  specific events. So this is just for compatibility. 
         */ 
        ComponentListener[] listeners = 
            myComponentListeners.getListeners(ComponentListener.class);
        for (ComponentListener listener : listeners) {
            evt.getEventType().fireEvent(evt,listener);
        }
        
        if ( inSync() && 
                evt.getEventType().equals(ComponentEvent.EventType.VALUE_CHANGED)) 
        { 
            /*
             * we care here only about events that appear from source editor
             * and only attribute events.
             */ 
            
            Object obj = evt.getSource(); 
            if ( !(obj instanceof AbstractDocumentComponent)){
                return;
            }
            
            AbstractDocumentComponent entity = ( AbstractDocumentComponent ) obj; 
            Element element =entity.getPeer();
            
            assert myTransaction!= null;
            int i ;
            while( (i = myTransaction.remove( element ))>=0 ) {
                Node oldAttr = myTransaction.removeOldAttribute( i );
                Node newAttr = myTransaction.removeNewAttribute( i );
                
                if ( entity instanceof BpelEntityImpl ){
                    ((BpelEntityImpl) entity).handleAttributeChange( oldAttr , 
                            newAttr);
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.Model#sync()
     */
    @Override
    public void sync() throws IOException
    {
        writeLock( true , true );
        ProcessImpl process = null;
        State oldState = null;
        try {
            if ( myLock.getWriteHoldCount() >1 ) {
                // Fix for #IZ80104
                return ;
            }
            oldState = getState();
            process = getProcess();

            preTreeCreated(process);
            super.sync();
            postTreeCreated(process, getProcess());
        }
        finally {
            try {
                if ( oldState!=getState() ) {
                    PropertyUpdateEvent event = new PropertyUpdateEvent( getSource(),
                            process , STATE , oldState , getState() );
                    fireChangeEvent(event);
                }
            }
            finally {
                writeUnlock( true );
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.UndoableEditListener#undoableEditHappened(javax.swing.event.UndoableEditEvent)
     */
    @Override
    public void undoableEditHappened( UndoableEditEvent e )
    {
        if (!inUndoRedo()) {    // Fix for #77785, #80584
            assert myLock.getWriteHoldCount() > 0;
            if (!myUndoSupport.editInProgress()) {
                myUndoSupport.beginUpdate();    
            }
        }
        myUndoSupport.postEdit(e.getEdit());
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.AbstractModel#getQNames()
     */
    @Override
    public Set<QName> getQNames() {
        return getEntityRegistry().getAllQNames();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.DocumentModel#createComponent(C, org.w3c.dom.Element)
     */
    public BpelEntity createComponent( BpelEntity parent, Element element ) {
        if ( ! (parent instanceof BpelContainer )) {
            return null;
        }
        return getChildBuilder().create( element , (BpelContainer)parent );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelModel#findElement(int)
     */
    public BpelEntity findElement( int i ) {
        readLock();
        try {
            Component comp = findComponent(i);

            assert comp == null || comp instanceof BpelEntity;
            return (BpelEntity) comp;
        }
        finally {
            readUnlock();
        }
    }
    
    @Override
    public boolean isIntransaction() {
        return myLock.getWriteHoldCount()>0;
    }
    
    @Override
    public boolean startTransaction()
    {
        writeLock( true , false );
        isXamTransaction = true;
        return super.startTransaction();
    }
    
    @Override
    public void endTransaction() {
        try {
            super.endTransaction();
        }
        finally {
            if ( isXamTransaction ){
                isXamTransaction = false;
                writeUnlock();
            }
        }
    }

    @Override
    public void rollbackTransaction() {
        try {
          myUndoSupport.abortUpdate();
          finishTransaction();
        }
        finally {
            if ( isXamTransaction ){
                isXamTransaction = false;
                writeUnlock();
            }
        }
    }
    
    public boolean isSupportedExpension( String uri ) {
        Collection<EntityFactory> factories = getEntityRegistry()
                .getFactories();
        for (EntityFactory factory : factories) {
            if (factory.isApplicable(uri)) {
                return true;
            }
        }
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.bpel.model.api.BpelModel#getAnotherVersionProcess()
     */
    public AnotherVersionBpelProcess getAnotherVersionProcess() {
        if (getState() == State.VALID) {
            return null;
        }
        else {
            Element element = null;
            if (getProcess() == null) { // in the case when xml was invalid from very beggining
                org.w3c.dom.Document doc = getDocument();
                if ( doc == null ) {
                    return null; 
                }
                element = doc.getDocumentElement();
                if (element == null) {
                    return null;
                }
            }
            else { // in the case when xml was originally good BPEL but was changed in incorrect way
                if ( myAnotherRoot ==null ) {
                    return null;
                }
                element = myAnotherRoot;
            }
            return new AnotherVersionBpelProcessImpl(this, element);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelModel#getAnotherVersionProcess()
     */
    public boolean canExtend(ExtensibleElements extensible, Class<? extends ExtensionEntity> extensionType) {
        Collection<EntityFactory> factories = getEntityRegistry().getFactories();
        for (EntityFactory factory : factories) {
            if (factory.canExtend(extensible, extensionType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * It is mainly intended to be used by JUnit tests.
     * @return
     */
    public RefCacheSupport getRefCacheSupport() {
        return mRefCacheSupport;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.xml.xam.xdm.AbstractXDMModel#getComponentUpdater()
     */
    @Override
    protected ComponentUpdater<BpelEntity> getComponentUpdater()
    {
        if ( mySyncUpdateVisitor == null ){
            mySyncUpdateVisitor = new SyncUpdateVisitor(); 
        }
        return mySyncUpdateVisitor;
    }

    protected void readLock() {
        readLock.lock();
    }

    protected void readUnlock() {
        readLock.unlock();
    }
    
    protected void writeLock() {
        writeLock( false , false );
    }

    protected void writeUnlock() {
        writeUnlock( false );
    }
    
    protected Object getSource(){
        assert myTransaction!= null;
        return myTransaction.getSource();
    }
    
    EntityFactoryRegistry getEntityRegistry(){
        return EntityFactoryRegistry.getInstance();
    }

    long getNextID() {
        return myNextID.incrementAndGet();
    }
    
    final boolean isInEventsFiring() {
        if ( myTransaction == null ){
            return false;
        }
        return myTransaction.isCommit;
    }
    
    
    /**
     * This method will be called before OM change was performed and we possibly
     * need to collect some information.
     * 
     * @throws could
     *             throw VetoException. It means that some visitor reject
     *             change.
     */
    final void preInnerEventNotify( ChangeEvent event ) throws VetoException {
        InnerEventDispatcher[] dispatchers = getInnerDispatchers();
        int executed = 0;
        try {
            for (InnerEventDispatcher dispatcher : dispatchers) {
                if (dispatcher.isApplicable(event)) {
                    dispatcher.preDispatch(event);
                    executed++;
                }
            }
        }
        catch (VetoException e) {
            int i=0;
            for (InnerEventDispatcher dispatcher : dispatchers) {
                if ( i > executed ) {
                    break;
                }
                if (dispatcher.isApplicable(event)) {
                    dispatcher.reset(event);
                    i++;
                }
            }
            throw e;
        }
    }

    /**
     * This method will be called right after OM change was performed and we
     * possibly need to change other elements in OM accordingly this change.
     */
    final void postInnerEventNotify( ChangeEvent event ) {
        InnerEventDispatcher[] dispatchers = getInnerDispatchers();
        for (InnerEventDispatcher dispatcher : dispatchers) {
            if (dispatcher.isApplicable(event)) {
                dispatcher.postDispatch(event);
            }
        }
    }

    /**
     * This method should be visible only for model impl elements.
     * 
     * @param event
     *            event for firing.
     */
    final void fireChangeEvent( ChangeEvent event ) {
        /*if ( event.getParent() instanceof PartnerLink ) {
            try {
                throw new Exception();
            }
            catch( Exception e ) {
            e.printStackTrace();
            }
        }*/
        myTransaction.add( event );
    }
    
    BpelChildEntitiesBuilder getChildBuilder(){
        return myChildBuilder;
    }
    
    private void writeLock( boolean allowMutation , boolean addMergeListener) {
        writeLock.lock();
        if ( myReadLockObtained.get() != null && myReadLockObtained.get() ){
            throw new IllegalStateException("You are trying to mutate OM "  // NOI18N
                    + "while read lock is obtianed.");                      // NOI18N
        }
        if ( !inSync() && !allowMutation  && 
                getState().equals( State.NOT_WELL_FORMED )) 
        {
            throw new IllegalStateException("Xml file is broken." +         // NOI18N
            " One cannot mutate model in broken state.");                   // NOI18N
        }
        if ( addMergeListener && myLock.getWriteHoldCount() == 1) {
            getAccess().addMergeEventHandler( myXDMListener );
        }
        if (myTransaction == null) {
            myTransaction = new Transaction();
        }
        myTransaction.start();
    }
    
    private void writeUnlock( boolean inSync ) {
        try { // just want to be sure that unlock will happen anyway.
            assert myTransaction != null;
            
            if (inSync && myLock.getWriteHoldCount() == 1) {
                getAccess().removeMergeEventHandler(myXDMListener);
                setInSync(true);
            }
            
            myTransaction.end();
        }
        finally {
            writeLock.unlock();
        }
    }
    
    private static ModelSource getOrCreateModelSource(Document doc, Lookup lookup )
    {
        Lookup lkup = Lookups.fixed(new Object[] { doc });
        return new ModelSource(lkup, true);
    }

    
    /**
     * 
     * I need the way to access to other OM ( Schema and WSDL ) 
     * and files . Currently for this purpose I use lookup.
     * Possibly this will be redone. 
     * 
     */
    private void init( ) {
        mySupport = new ChangeEventSupport();
        myUndoSupport = new BpelModelUndoableEditSupport();
        myPropertyChangeSupport = new PropertyChangeSupport( this );
        myComponentListeners = new EventListenerList();
        //getXDMAccess().getReferenceModel().setPretty(true);
        myXDMListener = new XDMListener();  
        myChildBuilder = new BpelChildEntitiesBuilder( this );
        mRefCacheSupport = new RefCacheSupport(this);
        
        getAccess().setAutoSync(true);
    }

    private void postTreeCreated( Process old, Process newProcess ) {
        TreeCreatedEvent<Process> newEvent = new TreeCreatedEvent<Process>(
                newProcess);
        if (old != newProcess) {
            postInnerEventNotify(newEvent);
        }
    }

    /**
     * @param process
     */
    private void preTreeCreated( Process process ) {
        TreeCreatedEvent<Process> event = new TreeCreatedEvent<Process>(process);
        try {
            if (process == null) {
                preInnerEventNotify(event);
            }
        }
        catch (VetoException e) {
            assert false;
        }
    }

    @SuppressWarnings("unchecked")
    private InnerEventDispatcher[] getInnerDispatchers()
    {
        if (myDispatchers == null) {
            if (null == innerDispatcherResult) {
                innerDispatcherResult = Lookup.getDefault().lookup(
                        new Lookup.Template(InnerEventDispatcher.class));
            }
            myDispatchers = (InnerEventDispatcher[]) innerDispatcherResult
                    .allInstances().toArray(
                            new InnerEventDispatcher[innerDispatcherResult
                                    .allInstances().size()]);
        }
        return myDispatchers;
    }
    

    protected class BpelModelUndoableEditSupport extends UndoableEditSupport {

        @Override
        public synchronized void beginUpdate()
        {
            super.beginUpdate();
            inProgress = true;
        }

        @Override
        public synchronized void endUpdate()
        {
            inProgress = false;
            super.endUpdate();
        }

        @Override
        protected CompoundEdit createCompoundEdit()
        {
            return new BpelModelUndoableEdit();
        }

        protected boolean editInProgress() {
            return inProgress;
        }

        protected void abortUpdate() {
            ModelUndoableEdit mue = (ModelUndoableEdit) compoundEdit;
            mue.justUndo();
            super.compoundEdit = createCompoundEdit();
        }

        private boolean inProgress;
    }

    protected class BpelModelUndoableEdit extends ModelUndoableEdit {

        private static final long serialVersionUID = 3771336438002454367L;

        @Override
        public void redo()
        {
            writeLock( true , true );
            try {
                super.redo();
                setInUndoRedo( true );
            }
            finally {
                writeUnlock( true );
            }
        }

        @Override
        public void undo()
        {
            writeLock( true , true );
            try {
                super.undo();
                setInUndoRedo( true );
            }
            finally {
                writeUnlock( true );
            }
        }
    }
    
    private final class Transaction {
        
        private Transaction (){
            myFullTransactionEventSet = new LinkedList<ChangeEvent>();
        }
       
        /**
         * Notify transaction about start.
         * Realy transaction could be already started. 
         * We just notify it that one more lock is entered. 
         */
        void start(){
            // ?? in previous version there was counter for locks. 
            // Now I get this counter dirrectly from myLock.
            // Keep this method for possible rewriting. May be it needs to be removed.....
        }
        
        
        /**
         * Notify transaction about end.
         * Realy it just notify about lock was released. 
         */
        void end(){
            if (myLock.getWriteHoldCount() == 1) {
                try {
                    commitEvents();
                    if (!inUndoRedo() && myUndoSupport.editInProgress()) {
                            myUndoSupport.endUpdate();
                    }
                }
                finally {
                    if ( inSync() ) {
                        setInUndoRedo(false);
                        setInSync(false);
                    }
                    setSource(null);
                    cleanEvents();
                    cleanAttributeChanges();
                }
            }
        }

        private void cleanEvents() {
            myFullTransactionEventSet.clear();
        }
        
        /**
         * Add the event into chain if events.
         */
        void add( ChangeEvent event ){
            assert myLock.getWriteHoldCount() > 0;
            if ( isCommit ){
                throw new IllegalStateException(
                        "Model should not be changed inside " +// NOI18N
                        "event listener.");// NOI18N
            }
            myFullTransactionEventSet.offer( event  );
        }
        
        Object getSource() {
            if (mySource == null) {
                return Thread.currentThread();
            }
            return mySource;
        }

        void setSource( Object obj ) {
            mySource = obj;
        }
        
        void addAttributeChange( Node parent , Node old , Node newValue ){
            myParents.add( parent );
            myOldAttrs.add( old );
            myNewAttrs.add( newValue );
        }
        
        int remove( org.w3c.dom.Node parent ){
            assert parent instanceof Node;
            int i=0;
            int ret = -1; 
            for ( Node node : myParents) {
               if ( areSameNodes( node , (Node) parent) ){
                   ret = i;
               }
               i++;
            }
            if ( ret>=0 ) {
                myParents.remove( ret );
            }
            return ret;
        }
        
        Node removeOldAttribute( int i ){
            return myOldAttrs.remove( i );
        }
        
        Node removeNewAttribute( int i ){
            return myNewAttrs.remove( i );
        }
        
        private void commitEvents() {
            isCommit = true;
            ChangeEvent event;
            try {
                boolean eventsExist = false;
                while ((event = myFullTransactionEventSet.poll()) != null) {
                    if (myFullTransactionEventSet.size() > 0) {
                        event.setNotLast();
                    }
                    else {
                        eventsExist = true;
                        event.setLast();
                    }
                    mySupport.fireChangeEvent(event);
                }
                if (!inSync() && eventsExist) { 
                    // inSync flag tell us that update was performed
                    // via editor
                    getAccess().flush();
                }
            }
            finally {
                isCommit = false;
            }
        }
        
        private void cleanAttributeChanges(){
            if ( !inSync() ){
                return;
            }
            myParents.clear();
            myOldAttrs.clear();
            myNewAttrs.clear();
        }
        
        private Queue<ChangeEvent> myFullTransactionEventSet;
        
        private boolean isCommit;
        
        private Object mySource;
        
        private List<Node> myParents = new ArrayList<Node>();
        
        private List<Node> myOldAttrs = new ArrayList<Node>();
        
        private List<Node> myNewAttrs = new ArrayList<Node>();
    }
    
    private class XDMListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent event) {
            //System.out.println("CALLED !");
            
            Node old = getAccess().getOldEventNode(event);
            Node now = getAccess().getNewEventNode(event);
            
            Node notNull = old==null?now:old;
            if ( notNull == null ){
                return;
            }
            
            Node parent = old==null?
                getAccess().getNewEventParentNode(event) : 
                getAccess().getOldEventParentNode(event);
            
            if ( notNull.getNodeType() == Node.ATTRIBUTE_NODE ){
                assert myTransaction!= null;
                myTransaction.addAttributeChange( parent , old, now );
            }
            
            //if ( notNull.getNodeType() == Node.TEXT_NODE){
            if ( notNull instanceof Text ){
                handleChangeInText(old, now, parent);
            }
            
        }

        private void handleChangeInText( Node old, Node now, Node parent ) {
            //if ( parent instanceof org.netbeans.modules.xml.xdm.nodes.Element ){
            if ( parent instanceof Element ){
                Component component = findComponent(
                        getAccess().getPathFromRoot(getDocument(), 
                                (Element) parent));
                if ( component instanceof ContentElement ){
                    String oldValue = null;
                    String value = null;
                    if ( old!= null ){
                        oldValue = old.getNodeValue();
                    }
                    if ( now!= null ){
                        value = now.getNodeValue();
                    }
                    PropertyUpdateEvent ev = new PropertyUpdateEvent( getSource(),
                            (BpelEntity)component, ContentElement.CONTENT_PROPERTY, 
                            oldValue, value);
                    
                    assert myTransaction != null;
                    myTransaction.add( ev );
                }
            }
        }

    }

    private ChangeEventSupport mySupport;

    private Lookup.Result innerDispatcherResult;

    private InnerEventDispatcher[] myDispatchers;

    private final BpelBuilderImpl myBuilder = new BpelBuilderImpl(this);

    private ProcessImpl myProcess;

    private AtomicLong myNextID = new AtomicLong();

    private final ReentrantReadWriteLock myLock = new ReentrantReadWriteLock();

    private final Lock readLock = myLock.readLock();

    private final Lock writeLock = myLock.writeLock();

    private BpelModelUndoableEditSupport myUndoSupport;
    
    private SyncUpdateVisitor mySyncUpdateVisitor;
    
    private XDMListener myXDMListener;
    
    private Transaction myTransaction;
    
    private PropertyChangeSupport myPropertyChangeSupport;
    
    private EventListenerList myComponentListeners; 
    
    private boolean isXamTransaction;
    
    private UndoableEditListener[] mySavedUndoableEditListeners;
    
    private ThreadLocal<Boolean> myReadLockObtained = new ThreadLocal<Boolean>();
    
    private BpelChildEntitiesBuilder myChildBuilder;
    
    private Element myAnotherRoot;

    private RefCacheSupport mRefCacheSupport;


}
