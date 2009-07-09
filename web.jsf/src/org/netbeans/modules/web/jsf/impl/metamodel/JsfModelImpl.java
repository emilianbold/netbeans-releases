/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.web.jsf.impl.metamodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModelFactory;
import org.netbeans.modules.web.jsf.api.metamodel.Behavior;
import org.netbeans.modules.web.jsf.api.metamodel.Component;
import org.netbeans.modules.web.jsf.api.metamodel.FacesConverter;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModelElement;
import org.netbeans.modules.web.jsf.api.metamodel.ModelUnit;
import org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener;
import org.netbeans.modules.web.jsf.api.metamodel.Validator;
import org.netbeans.modules.web.jsf.impl.facesmodel.AnnotationBehaviorRenderer;
import org.netbeans.modules.web.jsf.impl.facesmodel.AnnotationRenderer;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/**
 * @author ads
 *
 */
public class JsfModelImpl extends JsfModelManagers implements JsfModel {
    
    private static final String SUFFIX = "."+ModelUnit.FACES_CONFIG; // NOI18N 

    JsfModelImpl( ModelUnit unit, AnnotationModelHelper helper ) {
        super( helper );
        myUnit = unit;
        mySupport = new PropertyChangeSupport( this );
        //myModifiedModels = new CopyOnWriteArrayList<JSFConfigModel>();
        myModels = new LinkedList<JSFConfigModel>( );
        myFacesConfigs = new CopyOnWriteArrayList<FacesConfig>();
        registerChangeListeners();
        initModels();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#getElement(java.lang.Class)
     */
    public <T extends JsfModelElement> List<T> getElements( Class<T> clazz ) {
        refreshModels();
        ElementFinder<T> finder = getFinder(clazz);
        Class<? extends JSFConfigComponent> type = 
            (finder == null )? (Class)clazz : finder.getConfigType();
        List<T> result = new LinkedList<T>();
        for ( FacesConfig config : myFacesConfigs ){
            if ( config != null ){
                List<? extends JSFConfigComponent> children = config.getChildren(type);
                result.addAll( (List)children );
            }
        }
        
        
        JSFConfigModel model = myMainModel;
        boolean metadataComplete = false;
        if ( model!= null){
            FacesConfig config = model.getRootComponent();
            if ( config!= null ){
                Boolean isComplete = config.isMetaDataComplete();
                if ( isComplete!= null ){
                    metadataComplete = isComplete;
                }
            }
        }
        if ( finder != null && !metadataComplete )
        {
            result.addAll( finder.getAnnotations( this  ));
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#getFacesConfigs()
     */
    public List<FacesConfig> getFacesConfigs() {
        refreshModels();
        return Collections.unmodifiableList( myFacesConfigs );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#getMainConfig()
     */
    public FacesConfig getMainConfig() {
        refreshModels();
        JSFConfigModel model = myMainModel;
        return model!= null ? model.getRootComponent() : null ;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#getModels()
     */
    public List<JSFConfigModel> getModels() {
        synchronized ( myModels ){
            refreshModels();
            return Collections.unmodifiableList( myModels );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        getChangeSupport().addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener( PropertyChangeListener listener )
    {
        getChangeSupport().removePropertyChangeListener(listener);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.impl.facesmodel.AbstractJsfModel#getClientBehaviorRenderer(java.lang.String)
     */
    @Override
    protected List<AnnotationBehaviorRenderer> getClientBehaviorRenderers(
            String renderKitId )
    {
        FacesConfig config = getMainConfig();
        if ( config!= null ){
            Boolean complete = config.isMetaDataComplete();
            if ( complete != null && complete ){
                return Collections.emptyList();
            }
        }
        Collection<ClientBehaviorRendererImpl> collection =  
            getClientBehaviorManager().getObjects();
        List<AnnotationBehaviorRenderer> result = 
            new ArrayList<AnnotationBehaviorRenderer>( collection.size());
        for ( ClientBehaviorRendererImpl renderer : collection ){
            String id = renderer.getRenderKitId();
            if ( renderKitId.equals( id )){
                result.add( renderer );
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.impl.facesmodel.AbstractJsfModel#getRenderers(java.lang.String)
     */
    @Override
    protected List<AnnotationRenderer> getRenderers( String renderKitId ) {
        FacesConfig config = getMainConfig();
        if ( config!= null ){
            Boolean complete = config.isMetaDataComplete();
            if ( complete != null && complete ){
                return Collections.emptyList();
            }
        }
        Collection<RendererImpl> collection =  
            getRendererManager().getObjects();
        List<AnnotationRenderer> result = 
            new ArrayList<AnnotationRenderer>( collection.size());
        for ( RendererImpl renderer : collection ){
            String id = renderer.getRenderKitId();
            if ( renderKitId.equals( id )){
                result.add( renderer );
            }
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.impl.facesmodel.AbstractJsfModel#getSystemEventListeners()
     */
    @Override
    protected List<SystemEventListener> getSystemEventListeners() {
        // Notion : one don't need to check metadata-complete attribute. 
        // Listeners annotations works in any case. 
        Collection<SystemEventListenerImpl> collection =  
            getSystemEventManager().getObjects();
        List<SystemEventListener> listeners = ObjectProviders.
                    findApplicationSystemEventListeners( getHelper() );
        List<SystemEventListener> result = new ArrayList<SystemEventListener>(
                collection.size() + listeners.size());
        result.addAll( collection );
        result.addAll( listeners );
        return result;
    }

    
    private PropertyChangeSupport getChangeSupport(){
        return mySupport;
    }
    
    private void refreshModels(){
        /*
         * sync() method checks if document is modified, so modified 
         * list is not needed 
         * 
         * List<JSFConfigModel> list  = new ArrayList<JSFConfigModel>(myModifiedModels);
        myModifiedModels.clear();*/
        synchronized (myModels) {
            for (JSFConfigModel model : myModels) {
                try {
                    boolean nullRoot = model.getRootComponent() == null;
                    model.sync();
                    if (nullRoot) {
                        if (model.getRootComponent() != null) {
                            myFacesConfigs.add(model.getRootComponent());
                        }
                    }
                }
                catch (IOException e) {
                    LOG.log(Level.SEVERE,
                            "Error during faces-config.xml parsing! "
                                    + "File: "
                                    + model.getModelSource().getLookup()
                                            .lookup(FileObject.class), e);
                }
            }
        }
    }
    
    private void initModels() {
        List<FacesConfig> list;
        synchronized (myModels) {
            List<JSFConfigModel> models = new LinkedList<JSFConfigModel>();
            if (getUnit().getMainFacesConfig() != null) {
                myMainModel = JSFConfigModelFactory.getInstance().getModel(
                        getModelSource(getUnit().getMainFacesConfig(), true));
            }

            for (FileObject fileObject : getUnit().getConfigFiles()) {
                if (fileObject.equals(myMainModel)) {
                    models.add(myMainModel);
                }
                else {
                    models.add(JSFConfigModelFactory.getInstance().getModel(
                            getModelSource(fileObject, true)));
                }
            }

            compileRoots = new HashSet<FileObject>();
            FileObject[] roots;
            synchronized (compileRoots) {
                roots = getUnit().getCompilePath().getRoots();
                for (FileObject fileObject : roots) {
                    compileRoots.add(fileObject);
                }
            }

            for (FileObject fileObject : roots) {
                if (FileUtil.isArchiveFile(fileObject)) {
                    fileObject = FileUtil.getArchiveRoot(fileObject);
                }
                collectLibraryModels(models, fileObject);
            }
            
            myModels.addAll( models );

            list = new ArrayList<FacesConfig>(myModels.size());
            for (JSFConfigModel model : myModels) {
                if (model.getRootComponent() != null) {
                    list.add(model.getRootComponent());
                }
            }
        }
        myFacesConfigs.addAll( list );
    }

    private void collectLibraryModels( List<JSFConfigModel> models,FileObject fileObject )
    {
        FileObject metaInf = fileObject.getFileObject( ModelUnit.META_INF);
        if ( metaInf != null ){
            FileObject[] children = metaInf.getChildren();
            for (FileObject child : children) {
                String name = child.getNameExt();
                if ( name.equals( ModelUnit.FACES_CONFIG) || name.endsWith(SUFFIX )){
                    models.add( JSFConfigModelFactory.getInstance().getModel( 
                            getLibModelSource(child, false)) );
                }
            }
        }
    }
    
    private ModelUnit getUnit(){
        return myUnit;
    }
    
    private ModelSource getModelSource( FileObject fileObject , 
            boolean isEditable )
    {
        try {
            ModelSource source = Utilities.createModelSource( fileObject,isEditable);
            Lookup lookup = source.getLookup();
            lookup = new ProxyLookup( lookup , Lookups.singleton(this));
            return new ModelSource( lookup , isEditable );
        } catch (CatalogModelException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                ex.getMessage(), ex);
        }
        return null;
    }
    
    private ModelSource getLibModelSource( FileObject fileObject , 
            boolean isEditable )
    {
        try {
            ModelSource source = Utilities.createModelSource( fileObject,isEditable);
            Lookup lookup = source.getLookup();
            lookup = new ProxyLookup( lookup , Lookups.fixed(this, LIB));
            return new ModelSource( lookup , isEditable );
        } catch (CatalogModelException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                ex.getMessage(), ex);
        }
        return null;
    }
    
    private <T extends JsfModelElement> ElementFinder<T> getFinder( 
            Class<T> clazz)
    {
        return (ElementFinder<T>)FINDERS.get( clazz);
    }
    
    private void registerChangeListeners() {
        
        ClassPath compile = getUnit().getCompilePath();
        compile.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange( PropertyChangeEvent event ) {
                if (!event.getPropertyName().equals(ClassPath.PROP_ENTRIES)) {
                    return;
                }
                List<JSFConfigModel> newModels;
                FileObject[] roots = getUnit().getCompilePath().getRoots();
                List<FileObject> added = new LinkedList<FileObject>();
                synchronized (myModels) {
                    Set<JSFConfigModel> current = new HashSet<JSFConfigModel>();
                    Set<JSFConfigModel> allLib = new HashSet<JSFConfigModel>();
                    for (JSFConfigModel model : myModels) {
                        if ( model.getModelSource().getLookup().lookup( 
                                LibMarker.class) == null)
                        {
                            continue;
                        }
                        // we care here only about models in compile class path
                        allLib.add( model);
                    }
                    for (FileObject root : roots) {
                        boolean toAdd = true;
                        for (JSFConfigModel model : allLib) {
                            if ( model.getModelSource().getLookup().lookup( 
                                    LibMarker.class) == null)
                            {
                                continue;
                            }
                            FileObject fileObject = model.getModelSource()
                                .getLookup().lookup(FileObject.class);
                            if (FileUtil.isArchiveFile(root)) {
                                root = FileUtil.getArchiveRoot(root);
                            }
                            if (FileUtil.isParentOf(root, fileObject)) {
                                current.add(model);
                                toAdd = false;
                            }
                        }
                        if ( toAdd ){
                            added.add( root );
                        }
                    }
                    
                    allLib.removeAll( current );

                    for (JSFConfigModel model : allLib ) {
                        myModels.remove(model);
                        // myModifiedModels.remove( model );
                        myFacesConfigs.remove(model.getRootComponent());
                        // TODO : notify via property change event about model
                        // removal
                    }
                    newModels = new ArrayList<JSFConfigModel>(added.size());
                    for (FileObject root : added) {
                        collectLibraryModels(newModels, root);
                    }
                    myModels.addAll(newModels);
                    // myModifiedModels.addAll( newModels );
                    // TODO : notify via property change event about model
                    // creation

                }
                for (JSFConfigModel model : newModels) {
                    if (model.getRootComponent() != null) {
                        myFacesConfigs.add(model.getRootComponent());
                    }
                }
            }

        });
        
        myListener = new FileChangeListener(){

            public void fileAttributeChanged( FileAttributeEvent arg0 ) {
            }

            public void fileChanged( FileEvent event ) {
                /*
                 * sync() method checks if document is modified so there is no need
                 * in myModifiedModels
                 * 
                 * FileObject file = event.getFile();
                if ( !checkConfigFile(file)){
                    return;
                }
                JSFConfigModel model = null;
                for ( JSFConfigModel mod : myModels ){
                    FileObject fileObject = mod.getModelSource().getLookup().
                        lookup( FileObject.class);
                    if ( fileObject.equals( event.getFile())){
                        model = mod;
                        break;
                    }
                }
                if  ( model != null ){
                    myModifiedModels.add( model );
                }*/
            }

            public void fileDataCreated( FileEvent event ) {
                FileObject file = event.getFile();
                if ( !checkConfigFile(file)){
                    return;
                }
                ModelSource source=  getModelSource(file,  true );
                if (  source!= null ){
                    JSFConfigModel model = JSFConfigModelFactory.getInstance().
                        getModel( source );
                    boolean existed = false;
                    synchronized( myModels ){
                        if ( myModels.contains( model )){
                            existed = true;
                        }
                        else {
                            myModels.add( model );
                        }
                    }
                    if ( file.equals( getUnit().getMainFacesConfig())){
                        myMainModel = model;
                    }
                    //myModifiedModels.add( model );
                    if ( !existed && model.getRootComponent() != null ){
                        myFacesConfigs.add( model.getRootComponent() );
                    }
                    // TODO : notify via property listener event about model creation
                }
            }

            public void fileDeleted( FileEvent event ) {
                FileObject file = event.getFile();
                if ( !wasConfigFile(file)){
                    return;
                }
                JSFConfigModel model = null;
                synchronized (myModels) {
                    for (JSFConfigModel mod : myModels) {
                        FileObject fileObject = mod.getModelSource()
                                .getLookup().lookup(FileObject.class);
                        if (fileObject.equals(event.getFile())) {
                            model = mod;
                            break;
                        }
                    }
                    if (model != null) {
                        myModels.remove(model);
                    }

                }
                if (model != null) {
                    if ( myMainModel == model ){
                        myMainModel = null;
                    }
                    //myModifiedModels.remove(model);
                    if ( model.getRootComponent() != null ){
                        myFacesConfigs.remove(model.getRootComponent());
                    }
                    // TOFO : notify via property change event about model removal
                }
            }

            public void fileFolderCreated( FileEvent arg0 ) {
            }

            public void fileRenamed( FileRenameEvent arg0 ) {
                // TODO ??
                
            }
            
            private boolean checkConfigFile( FileObject fileObject ){
                if ( fileObject == null){
                    return false;
                }
                if ( fileObject.equals( getUnit().getMainFacesConfig())){
                    return true;
                }
                for( FileObject object : getUnit().getConfigFiles()){
                    if ( fileObject.equals( object)){
                        return true;
                    }
                }
                return false;
            }
            
            private boolean wasConfigFile( FileObject fileObject ){
                if ( fileObject == null){
                    return false;
                }
                if ( fileObject.equals( getUnit().getMainFacesConfig())){
                    return true;
                }
                String name = fileObject.getNameExt();
                if ( name.equals( ModelUnit.FACES_CONFIG) || name.endsWith(
                        "." + ModelUnit.FACES_CONFIG))
                {
                    FileObject parent = fileObject.getParent();
                    if ( !parent.getName().equals( ModelUnit.META_INF )){
                        return false;
                    }
                    for ( FileObject root : getUnit().getSourcePath().getRoots()){
                        if ( parent.equals(root.getFileObject(ModelUnit.META_INF))){
                            return true;
                        }
                    }
                }
                return false;
            }
            
        };
        FileUtil.addFileChangeListener( myListener );
    }
    
    private static final class LibMarker{
    }
    
    private final static Map<Class<? extends JsfModelElement>, 
        ElementFinder<? extends JsfModelElement>> FINDERS = 
            new HashMap<Class<? extends JsfModelElement>, 
                ElementFinder<? extends JsfModelElement>>();
    
    static {
        FINDERS.put( Behavior.class , new BehaviorFinder() );
        FINDERS.put( Component.class , new ComponentFinder());
        FINDERS.put( FacesConverter.class , new ConverterFinder() );
        FINDERS.put( FacesManagedBean.class,  new ManagedBeanFinder( ));
        FINDERS.put( Validator.class ,  new ValidatorFinder());
    }
    
    private static final LibMarker LIB = new LibMarker();
    private final PropertyChangeSupport mySupport;
    private ModelUnit myUnit;
    private List<JSFConfigModel> myModels ;
    volatile private JSFConfigModel myMainModel;
    private List<FacesConfig> myFacesConfigs;
    //private List<JSFConfigModel> myModifiedModels;
    private FileChangeListener myListener;
    private Set<FileObject> compileRoots;
    private static final Logger LOG = Logger.getLogger(JsfModelImpl.class.getName());

}
