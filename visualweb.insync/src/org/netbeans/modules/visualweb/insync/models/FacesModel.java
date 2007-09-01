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
package org.netbeans.modules.visualweb.insync.models;

import java.beans.MethodDescriptor;
import org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.netbeans.modules.visualweb.insync.InSyncServiceProvider;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
import org.netbeans.modules.visualweb.insync.java.JavaUnit;

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.visualweb.insync.java.Method;
import org.openide.ErrorManager;
import org.openide.cookies.CloseCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.OperationEvent;
import org.openide.loaders.OperationListener;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.api.insync.JsfJavaDataObjectMarker;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProperty;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.ModelSet;
import org.netbeans.modules.visualweb.insync.ParserAnnotation;
import org.netbeans.modules.visualweb.insync.ResultHandler;
import org.netbeans.modules.visualweb.insync.SourceUnit;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.UndoManager;
import org.netbeans.modules.visualweb.insync.Unit;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.beans.BeanStructureScanner;
import org.netbeans.modules.visualweb.insync.beans.BeansUnit;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;
import org.netbeans.modules.visualweb.insync.faces.FacesUnit;
import org.netbeans.modules.visualweb.insync.faces.ReefFacesBeanStructureScanner;
import org.netbeans.modules.visualweb.insync.faces.ThresherFacesApplicationBeanStructureScanner;
import org.netbeans.modules.visualweb.insync.faces.ThresherFacesPageBeanStructureScanner;
import org.netbeans.modules.visualweb.insync.faces.ThresherFacesRequestBeanStructureScanner;
import org.netbeans.modules.visualweb.insync.faces.ThresherFacesSessionBeanStructureScanner;
import org.netbeans.modules.visualweb.insync.faces.ThresherFacesFragmentBeanStructureScanner;
import org.netbeans.modules.visualweb.insync.live.BeansDesignEvent;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.live.LiveUnitWrapper;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.text.Line;

/**
 * Representation of a complete JSF design-time model, including the composited insync units and
 * netbeans wiring.
 *
 * @author  Tor Norbye
 * @author  Carl Quinn
 */
public class FacesModel extends Model {

    public static final FacesModel[] EMPTY_ARRAY = {};

    //The following arrays are used to ensure we are working with the
    //managed beans insync understands. There is one-one relationship
    //between these three arrays
    public static String managedBeanNames[] = {
        "com.sun.jsfcl.app.AbstractPageBean",       // NOI18N
        "com.sun.jsfcl.app.AbstractRequestBean",    // NOI18N
        "com.sun.jsfcl.app.AbstractSessionBean",    // NOI18N
        "com.sun.jsfcl.app.AbstractApplicationBean", // NOI18N
        //TODO: Refactor this array into specific to R1 and R2 page
        "com.sun.rave.web.ui.appbase.AbstractPageBean",       // NOI18N
        "com.sun.rave.web.ui.appbase.AbstractRequestBean",    // NOI18N
        "com.sun.rave.web.ui.appbase.AbstractSessionBean",    // NOI18N
        "com.sun.rave.web.ui.appbase.AbstractApplicationBean", // NOI18N
        "com.sun.rave.web.ui.appbase.AbstractFragmentBean" // NOI18N
    };
    
    public static ManagedBean.Scope managedBeanScopes[] = {
        ManagedBean.Scope.REQUEST,
        ManagedBean.Scope.REQUEST,
        ManagedBean.Scope.SESSION,
        ManagedBean.Scope.APPLICATION,
        //TODO: Refactor this array into specific to R1 and R2 page
        ManagedBean.Scope.REQUEST,
        ManagedBean.Scope.REQUEST,
        ManagedBean.Scope.SESSION,
        ManagedBean.Scope.APPLICATION,
        ManagedBean.Scope.REQUEST
    };
    
    public static boolean managedBeanIsPage[] = {
        true,
        false,
        false,
        false,
        //TODO: Refactor this array into specific to R1 and R2 page
        true,
        false,
        false,
        false,
        true
    };
    
    public static Class managedBeanScannerTypes[] = {
        ReefFacesBeanStructureScanner.class, // com.sun.rave.web.ui.appbase.AbstractPageBean
        BeanStructureScanner.class, // com.sun.jsfcl.app.AbstractRequestBean
        BeanStructureScanner.class, // com.sun.jsfcl.app.AbstractSessionBean
        BeanStructureScanner.class, // com.sun.jsfcl.app.AbstractApplicationBean
        ThresherFacesPageBeanStructureScanner.class, // com.sun.rave.web.ui.appbase.AbstractPageBean
        ThresherFacesRequestBeanStructureScanner.class, // com.sun.rave.web.ui.appbase.AbstractRequestBean
        ThresherFacesSessionBeanStructureScanner.class, // com.sun.rave.web.ui.appbase.AbstractSessionBean
        ThresherFacesApplicationBeanStructureScanner.class, // com.sun.rave.web.ui.appbase.AbstractApplicationBean
        ThresherFacesFragmentBeanStructureScanner.class // com.sun.rave.web.ui.appbase.AbstractFragmentBean
     };

    public static FacesModel getInstance(FileObject file) {
        return (FacesModel) Model.getInstance(file, FacesModelSet.class, false);
    }
    
    public static FacesModel getInstance(FileObject file, Class ofType) {
        return (FacesModel) Model.getInstance(file, ofType, false);
    }
    
    public static FacesModel getFacesModel(FileObject file) {
        return (FacesModel)Model.getModel(file);
    }
    
    /*
     * Project JSP and JAVA file maping methods map between the following.
     * The java tree rooted at srcFolder + backingFolder mirrors the jsp tree rooted at webFolder
     *
     * Examples:
     *      beanName        JSP             Java
     *      ~~~~~~~~        ~~~             ~~~~
     *      Page1           Page1.jsp       webapplication1/Page1.java
     *      foo/Page2       foo/Page2.jsp   webapplication1/foo/Page2.java
     */

    /**
     * Return the project item for the jsp half of a jsp/java pair given a java file object
     */
    public static final FileObject getJspForJava(FileObject javaFile) {
        // !EAT TODO MOVE this back INTO InSync where it BELONGS :(
        return JsfProjectUtils.getJspForJava(javaFile);
    }

    /**
     * Return the relative path and filename base given a java project item
     */
    static final String getBasePathForJava(FileObject javaFile) {
        return JsfProjectUtils.getBasePathForJava(javaFile);
    }

    /**
     * Return the logical bean name of a jsp/java pair given a java project item
     */
    public static final String getBeanNameForJava(FileObject javaFile) {
        String path = getBasePathForJava(javaFile);
        if (path != null) {
            //Remove the path separator character if found at the beginning
            if (path.startsWith("/"))
                path = path.substring(1);        
            return FacesUnit.fixPossiblyImplicitBeanName(path.replace('/', '$'));
        }
        return null;
    }

    /**
     * Return the project item for the java half of a jsp/java pair given a jsp project item
     */
    public static FileObject getJavaForJsp(FileObject file) {
        // !EAT TODO MOVE this back INTO InSync where it BELONGS :(
        return JsfProjectUtils.getJavaForJsp(file);
    }
    
    /**
     * Return the relative path and filename base given a jsp project item
     */
    static final String getBasePathForJsp(FileObject jspFile) {
        return JsfProjectUtils.getBasePathForJsp(jspFile);
    }

    /**
     * Return the logical bean name of a jsp/java pair given a jsp project item
     */
    public static final String getBeanNameForJsp(FileObject jspFile) {
        String beanName = getBasePathForJsp(jspFile);
        if (beanName == null)
            return null;
        if (beanName.length() == 0)
            return beanName;
        if (beanName.charAt(0) == '/')
            beanName = beanName.substring(1);
        beanName = beanName.replace('/', '$');
        beanName = FacesUnit.fixPossiblyImplicitBeanName(beanName);
        return beanName;
    }

    //--------------------------------------------------------------------------------- FacesFactory

    /**
     * A Model.Factory that knows how and when to make faces page FacesModels from file objects.
     */
    public static class FacesFactory implements Model.Factory {
//        static final String[] mimes = DesignerService.getDefault().getMimeTypes();
        static final String[] mimes = InSyncServiceProvider.get().getMimeTypes();

        static boolean isModelMime(String mime) {
            for (int i = 0; i < mimes.length; i++) {
                if (mimes[i].equals(mime))
                    return true;
            }
            return false;
        }

        public Model newInstance(ModelSet set, FileObject file) {
            boolean create = false;
            String mime = file.getMIMEType();
            FileObject jspFile = null;
            if(isModelMime(mime)) {
                jspFile = file;
                if(FacesModel.getJavaForJsp(file) != null) {
                    create = true;
                }
            }else if(BeansFactory.isModelMime(mime)) {
                //If .java file is in conflict during CVS update, the file gets 
                //renamed and a new file by same name is created which has the
                //the merged changes. In this scenario, model is deleted when the 
                //file is renamed and therefore when the new file gets created, 
                //it is necessary to create the model by pairing the new .java 
                //file with the already existing .jsp file
                
                //There is already logic in ModelCreateVisitor to check for the
                //existence of Model for a given .jsp file but not for a .java file
                jspFile = FacesModel.getJspForJava(file);
                if(jspFile != null && set.getModel(jspFile) == null) {
                    create = true;
                }
            }
            if(create) {
                return new FacesModel((FacesModelSet)set, jspFile);
            }
            
            return null;
        }
    }

    //--------------------------------------------------------------------------------- BeansFactory

    /**
     * A Model.Factory that knows how and when to make simple bean FacesModels from file objects.
     */
    public static class BeansFactory implements Model.Factory {
        static final String[] mimes = { "text/x-java" };

        static boolean isModelMime(String mime) {
            for (int i = 0; i < mimes.length; i++) {
                if (mimes[i].equals(mime))
                    return true;
            }
            return false;
        }

        public Model newInstance(ModelSet set, FileObject file) {
            String mime = file.getMIMEType();
            return isModelMime(mime) && FacesModel.getJspForJava(file) == null
            ? new FacesModel((FacesModelSet)set, file) : null;
        }
    }

    //------------------------------------------------------------------------------ Instance Fields

    protected FacesModelSet facesModelSet;  // Our down-casted containing model set

    /** Project item, FileObject representing the jsp markup iff this is a page model  */
    private FileObject markupFile;
    /** Unit representing the jsp markup iff this is a page model */
    private MarkupUnit markupUnit;

    /** Project item and FileObject representing the bean java file */
    private FileObject javaFile;
    /** Unit representing the bean java file */
    private JavaUnit javaUnit;

    /** JavaBean synthetic layer unit--always at least a plain bean. */
    private BeansUnit beansUnit;
    /** JavaBean synthetic layer also downcasted when a faces page */
    private FacesPageUnit facesUnit;

    protected LiveUnitWrapper liveUnitWrapper;
    
    private OperationListener operationListener = new ModelOperationListener();
    
    private String beanName;
    
//    private final FacesDnDSupport dndSupport = new FacesDnDSupport(this);
    

    //--------------------------------------------------------------------------------- Construction

    /**
     * Creates a new instance of FacesModel
     *
     * @param owner The owning ModelSet for this Model.
     * @param file The FileObject for either a page.jsp or a bean.java that is being modeled
     */
    FacesModel(FacesModelSet owner, FileObject file) {
        super(owner, file);
        assert Trace.trace("insync.models", "LFM.FacesModel: file:" + file);  //NOI18N

        this.facesModelSet = owner;

        // Given a java file, grab all the java related info
        if (BeansFactory.isModelMime(file.getMIMEType())) {
            javaFile = file;
        }
        // Given a markup file, grab all the markup related info
        else if (FacesFactory.isModelMime(file.getMIMEType())) {
            markupFile = file;
        }
        
        DataLoaderPool.getDefault().addOperationListener(operationListener);
        //fireModelOpened(this);
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Model#destroy()
     */
    public void destroy() {
//        // Let the designer know that this model is being destroyed, so it can clean up.
//        // In the future, the designer should listen to the DesignProject property notification
//        // and react there instead.
//        if (markupFile != null) {
//            DesignerServiceHack.getDefault().destroyWebFormForFileObject(markupFile);
//        }
       if (operationListener != null) {
           DataLoaderPool.getDefault().removeOperationListener(operationListener);
           operationListener = null;
       }
       DataObject javaDataObject = javaUnit == null ? null : javaUnit.getDataObject(); 
        
        // invoke fireContextDeleted() to let viewers know our context is dead
        if (liveUnitWrapper != null) {
            if (facesModelSet != null && liveUnitWrapper.isLiveUnitInstantiated())
                facesModelSet.fireContextClosed(getLiveUnit());
            liveUnitWrapper.destroy();
            liveUnitWrapper = null;
        }

        if (facesUnit != null) {
            facesUnit.destroy();
            facesUnit = null;
        }
        if (beansUnit != null) {
            beansUnit.destroy();
            beansUnit = null;
        }
        if (javaUnit != null) {
            javaUnit.removeListener(this);
            javaUnit.destroy();
            javaUnit = null;
        }
        if (markupUnit != null) {
            markupUnit.removeListener(this);
            markupUnit.destroy();
            markupUnit = null;
        }
        if (facesModelSet != null){
            facesModelSet.removeFromModelsToSync(this);
            facesModelSet = null;
        }
        
// <separation of models>
        html = null;
        body = null;
// </separation of models>
        
        // Keep javaFile so we can report properly on error of isModelledManagedBean
//        javaFile = null;
        super.destroy();        
        
       // XXX #6478973, #6335072 Assuring there are no opened components left after
       // model destroyal.
       if (javaDataObject != null) {
           CloseCookie cc = (CloseCookie)javaDataObject.getCookie(CloseCookie.class);
           if (cc != null) {
               cc.close();
           }
       } 
    }

    /**
     * Called from ModelSet to let us know when the project class loader changes. Pass the new
     * loader down to the java and bean units if we have them.
     * @param cl The new classloader.
     */
    void updateClassLoader(ClassLoader cl) {
        if (beansUnit != null)
            beansUnit.setClassLoader(cl);
    }

    //------------------------------------------------------------------------------------ Accessors

    /*
     * @see org.netbeans.modules.visualweb.insync.Model#getUndoManager()
     */
    public UndoManager getUndoManager() {
        return undoManager;
    }

    /**
     * Return the FacesModelSet associated with this Model. This is a typed
     * version of getOwner();
     */
    public FacesModelSet getFacesModelSet() {
        return facesModelSet;
    }

    /**
     * Retrieve the FileObject for the markup portion of this model.
     *
     * @return The FileObject for the markup portion of this model.
     */
    public FileObject getMarkupFile() {
        return markupFile;
    }

    /**
     * Get the markup source Unit for this model.
     * @return The markup source Unit for this model.
     */
    public MarkupUnit getMarkupUnit() {
        return markupUnit;
    }

    /**
     * Returns a URI object representing the project resource for the page I represent.
     * web/Page1.jsp
     */
    public URI getMarkupResourceRelativeUri() {
        if (getMarkupUnit() == null)
            return null;
        FileObject file = getMarkupFile();
        URI uri = ((FacesModelSet) getOwner()).relativize(file);
        return uri;
    }
    
    /**
     * Get the FileObject for the Java file corresponding to this model.
     *
     * @return The Java file's FileObject.
     */
    public FileObject getJavaFile() {
        return javaFile;
    }

    /**
     * Get the java Unit for this model.
     * @return The java Unit for this model.
     */
    public JavaUnit getJavaUnit() {
        return javaUnit;
    }

    /**
     * Returns a URI object representing the project resource for the page I represent.
     * web/Page1.jsp
     */
    public URI getJavaResourceRelativeUri() {
        if (getJavaFile() == null)
            return null;
        FileObject file = getJavaFile();
        URI uri = ((FacesModelSet) getOwner()).relativize(file);
        return uri;
    }

    /**
     * Get the logical bean name for this model.
     *
     * @return The logical bean name for this model.
     */
    public String getBeanName() {
        if (beanName == null) {
            beanName = getBeanNameForJava(javaFile);
        }
        return beanName;
    }
                    

    /**
     * Get the beans Unit for this model.
     *
     * @return The beans Unit for this model.
     */
    public BeansUnit getBeansUnit() {
        return beansUnit;
    }

    /**
     * Get the faces Unit for this model.
     *
     * @return The faces Unit for this model.
     */
    public FacesPageUnit getFacesUnit() {
        return facesUnit;
    }

    /**
     * Get the live Unit for this model.
     *
     * @return The live Unit for this model.
     */
    public synchronized LiveUnit getLiveUnit() {
        if (liveUnitWrapper == null)
            return null;
        return liveUnitWrapper.getLiveUnit();
    }

    /**
     * Get the topmost Unit for this model
     *
     * @return
     */
    public Unit getTopmostUnit() {
        return liveUnitWrapper == null ? markupUnit: (Unit) liveUnitWrapper;
    }

    /**
     * Return whether or not this model has errors
     *
     * @return
     */
    public boolean isBusted() {
        // Catch a case whereby the refactoring is done, and a bunch of events are being fired off,
        // some of the callers of this get notified of changes/removal's of models that are in invalid
        // state.  My units are put into states that are not in sync.
        if (!isValid())
            return true;
        if (reportMustBeAbstractPageSubclassError) {
            return true;
        }
        // isBusted needs to take into account the two source units that derive the live unit, as
        // well as any possible errors contained in the live unit itself
        MarkupUnit markup = getMarkupUnit();
        if (markup != null && markup.getState().isBusted())
            return true;
        JavaUnit java = getJavaUnit();
        if (java != null && java.getState().isBusted())
            return true;
        if (liveUnitWrapper != null && liveUnitWrapper.getState().isBusted())
            return true;
        // if neither of my units are not busted or neither is initialized, then I am not busted
        return false;
    }

    /**
     * Return the errors that are present on this model
     *
     * @return
     */
    public ParserAnnotation[] getErrors() {
        ParserAnnotation errors[];
        // if the live unit is initialized, it will incorporate the errors from my source files as well, so dont ask the source files
        // if I have a live unit
        if (liveUnitWrapper == null) {
            // Return the collection of errors from all my source units
            ParserAnnotation markupErrors[] = getMarkupUnit() == null?ParserAnnotation.EMPTY_ARRAY:getMarkupUnit().getErrors();
            ParserAnnotation javaErrors[] = getJavaUnit() == null?ParserAnnotation.EMPTY_ARRAY:getJavaUnit().getErrors();
            int errorCount = markupErrors.length + javaErrors.length + (reportMustBeAbstractPageSubclassError?1:0);
            if (errorCount == 0)
                return ParserAnnotation.EMPTY_ARRAY;
            errors = new ParserAnnotation[errorCount];
            int index =0;
            System.arraycopy(markupErrors, 0, errors, 0, markupErrors.length);
            index += markupErrors.length;
            System.arraycopy(javaErrors, 0, errors, index, javaErrors.length);
            index += javaErrors.length;
            if (reportMustBeAbstractPageSubclassError) {
                StringBuffer buffer = new StringBuffer();
                boolean didFirst = false;
                // Only report page subclasses
                for (int i=0; i < managedBeanNames.length; i++) {
                    if (managedBeanIsPage[i]) {
                        if (didFirst) {
                            buffer.append(", "); // NOI18N
                        } else {
                            didFirst = true;
                        }
                        buffer.append(managedBeanNames[i]);
                    }
                }
                ParserAnnotation annotation = new ParserAnnotation(NbBundle.getMessage(FacesModel.class, "ERR_JavaMustBeDirectDescendent", buffer.toString()), getJavaFile(), 1, 1); // NOI18N
                errors[index] = annotation;
                index++;
            }
        } else {
            errors = liveUnitWrapper.getErrors();
        }
        return errors;
    }

    /**
     * Retrieve the DesignBeanContainer that holds the non-visual "tray" beans
     *
     * @return
     */
    public DesignBean getRootBean() {
        if (liveUnitWrapper  == null)
            return null;
        DesignBean rootbean = getLiveUnit().getRootContainer();
        //assert Trace.trace("insync.models", "LFM.getTrayRoot: rootbean:" + rootbean);
        return rootbean;
    }

    //------------------------------------------------------------------------------- Internal Setup

    /**
     * Open the insync unit for the given web page's markup source
     */
    private void openMarkupUnit() {
        if (markupFile == null) {
            return;
        }
        assert Trace.trace("insync.models", "LFM.openMarkupUnit(" + markupFile + ")");  //NOI18N

        // Jsp/Markup file
        markupUnit = new MarkupUnit(markupFile, MarkupUnit.ALLOW_XML, true, undoManager);
        markupUnit.addListener(this);
    }

    /**
     * Scan a DOM tree looking for an element with a binding="#{Name.foo}" attr & return Name
     *
     * @param n
     * @return Variable name part of first binding attr found
     */
    private static String findBoundBeanName(org.w3c.dom.Node n) {
        if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            String binding = ((Element)n).getAttribute("binding");  //NOI18N
            if (binding != null && binding.length() > 0
                    && binding.startsWith("#{") && binding.endsWith("}")) {  //NOI18N
                int dot = binding.indexOf('.');
                int end = dot > 0 ? dot : binding.length()-1;
                return binding.substring(2, end);
            }
        }
        if (n.hasChildNodes()) {
            NodeList nl = n.getChildNodes();
            int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                String name = findBoundBeanName(nl.item(i));
                if (name != null)
                    return name;
            }
        }
        return null;
    }

    protected boolean reportMustBeAbstractPageSubclassError = false;
    
    /**
     * Open the insync units for the given web page's java backing file.
     */
    private void openJavaUnits() {
        assert Trace.trace("insync.models", "LFM.openJavaUnits()");  //NOI18N

        FileObject sourceFolder = null;
        // Item is not part of the project - user probably browsed a mounted filesystem -> user is
        // probably a Rave developer....
        Project project = getProject();
        if (project == null)
            return;

        // figure out our logical bean name based on file name
        String beanName;
        String javaPackage = null;

        if (javaFile == null) {
            beanName = getBeanNameForJsp(markupFile);
        }
        else {
            beanName = getBeanNameForJava(javaFile);
        }

        // see if it is registered with MBM
        FacesConfigModel facesConfigModel = facesModelSet.getFacesConfigModel();
        ManagedBean mb = facesConfigModel.getManagedBean(beanName);

        // if so then get our package & classname from there if needed
        if (mb != null) {
            if (javaFile == null) {
                // PROJECTTODO2: cleanup
                // This ugly blob of code needs to go somewhere
                String javaFileName = mb.getManagedBeanClass().replace('.', '/') + ".java";  //NOI18N;
                Sources sources = ProjectUtils.getSources(getProject());
                // !EAT TODO: replace "java" with org.netbeans.api.java.project.JavaProjectConstants.SOURCES_TYPE_JAVA
                SourceGroup groups[] = sources.getSourceGroups("java");
                for (int i=0; i < groups.length; i++) {
                    SourceGroup group = groups[i];
                    sourceFolder = group.getRootFolder();
                    javaFile = sourceFolder.getFileObject(javaFileName);
                    if (javaFile != null)
                        break;
                }
                sourceFolder = null;
            }
            if (javaPackage == null)
                javaPackage = FacesConfigModel.getPackageName(mb);
        }
        // if not then get it using the formula, & update MBM later below
        else {
            if (javaFile == null) {
                javaFile = getJavaForJsp(markupFile);
            }
        }
        // if no java file, then abort setting up the java units
        if (javaFile == null) {
            ErrorManager.getDefault().log("No java file found for " + markupFile);  //NOI18N
            return;
        }

        // Assemble complete unit tree
        URLClassLoader cl = facesModelSet.getProjectClassLoader();
        javaUnit = new JavaUnit(javaFile, cl, undoManager);
        javaUnit.addListener(this);
        // PROJECTTODO2: iffy
        if (sourceFolder == null)
            // getPageBeanRoot() includes the defaultPackage, ie the default package folder
            sourceFolder = JsfProjectUtils.getPageBeanRoot(project).getParent();
        File sourceFolderFile = FileUtil.toFile(sourceFolder);
        if(!sourceFolderFile.exists()) {
            ErrorManager.getDefault().log("Error - The Source folder does not exist!"); //NOI18N
            return;
        }

        //In case we are reacting to .java file creation but before the file is
        //well formed, we may not be able to get the public class
        JavaClass javaClass = javaUnit.getJavaClass();
        if(javaClass == null) {
            javaUnit.setBusted();
            return;
        }
        
        // TODO Handle case where the class specified is not defined or is missing
        //Check if we are working with our known managed beans
        boolean isModelledManagedBean = false;
        for(int i=0; i<managedBeanNames.length; i++) {
            if(javaClass.isSubTypeOf(managedBeanNames[i])) {
                isModelledManagedBean = true;
                break;
            }
        }
        reportMustBeAbstractPageSubclassError = false;
        // Abort model creation if the class is not one of ours
        if(!isModelledManagedBean) {
            try {
                DataObject dataObject = DataObject.find(javaFile);
                if (dataObject instanceof JsfJavaDataObjectMarker) {
                    reportMustBeAbstractPageSubclassError = true;
                }
            } catch (DataObjectNotFoundException e) {
            }
            return;
        }
        
        javaUnit.sync();
        if (javaUnit.getState() != Unit.State.CLEAN) {
            return;
        }
        
        if (javaPackage == null)
            javaPackage = javaUnit.getPackageName();
        
        String rootPackage = JsfProjectUtils.getProjectProperty(project, JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE);
        if (markupUnit != null) {
            beansUnit = facesUnit = new FacesPageUnit(javaUnit, cl, javaPackage, this, rootPackage,
                                                      facesModelSet.getFacesContainer(), markupUnit);
            liveUnitWrapper = new LiveUnitWrapper(this, facesUnit, markupFile);

            facesUnit.setDefaultSrcEncoding(JsfProjectUtils.getSourceEncoding(project));
            facesUnit.setDefaultEncoding(JsfProjectUtils.getDefaultEncoding(project));
            facesUnit.setDefaultLanguage(JsfProjectUtils.getDefaultLocale(project));
        }
        else {
            beansUnit = new FacesUnit(javaUnit, cl, javaPackage, this, rootPackage, facesModelSet.getFacesContainer());
            facesUnit = null;
            liveUnitWrapper = new LiveUnitWrapper(this, beansUnit, javaFile);
        }

        // now do a sync to force an update of the missing MB entry in the MBM
        //if (mb == null)
        //    sync();

        assert Trace.trace("insync.models", "  markupObj:" + markupFile);  //NOI18N
        assert Trace.trace("insync.models", "  project:" + project);  //NOI18N
        assert Trace.trace("insync.models", "  javaObj:" + javaFile);  //NOI18N
        assert Trace.trace("insync.models", "  javaPackage:" + javaPackage);  //NOI18N
        return;
    }
    
    /**
     * When the file is renamed, the java class and other things may have changed
     * in an unopened file. So it has to be synced when renamed. Needed when the
     * document is not opened.
     */
    public void fileRenamed(String oldName, String newName, String ext, FileObject fo, boolean remove) {
        //Remove the model if any of the it's file is marked non sharable
        //(for example if there are conflicts during CVS update)
        if(remove && ((javaUnit != null && javaUnit.getFileObject() == fo) ||
           (markupUnit != null && markupUnit.getFileObject() == fo))) {
            facesModelSet.removeModel(this);
            return;
        }
        
        // The following computaion ensures that we react to a file rename only
        // after all files of a FacesModel are renamed e.g. .java and .jsp
        boolean doSync = false;
        if (markupUnit != null) {
            if(javaUnit.getFileObject() == fo && markupUnit.getFileObject().getName().equals(fo.getName())) {
                doSync = true;
            } else if(file == fo && javaUnit.getFileObject().getName().equals(fo.getName())) {
                doSync = true;
            }
        } else if (file == fo){
            doSync = true;
        }
        
        if(doSync){
            if (javaUnit != null){
                javaUnit.setSourceDirty();
            }
            if (markupUnit != null){
                markupUnit.setSourceDirty();
            }
            //bean name is not valid anymore
            beanName = null;
            
            // We could call syncAll here, since via setSourceDirty() we have added
            // only this model to ModelSet.modlesToSync. So sync happens only to
            // this model. However, I can not call syncAll() as it is protected
            //facesModelSet.syncAll();
            
            // This sync will reset the Source Dirty flag.
            sync();
            
            // Remove the model from the modlesToSync (set by setSourceDirty()),
            // as we have already synced.
            facesModelSet.removeFromModelsToSync(this);
        }
    }
    /**
     *
     */
    private void ensureManagedBeansEntry() {
        String beanName = getBeanName();
        // see if it is registered with MBM
        FacesConfigModel facesConfigModel = facesModelSet.getFacesConfigModel();
        if(facesConfigModel.isBusted()) {
            return;
        }
        ManagedBean mb = facesConfigModel.getManagedBean(beanName);
        // update the missing MB entry in the MBM, getting scope based on the superclass of the bean
        //!CQ consider fixing broken entry settings in some cases
        if (mb == null) {
            JavaClass javaClass = beansUnit.getThisClass();
            for(int i=0; i<managedBeanNames.length; i++) {
                if(beansUnit.getBaseBeanClassName().equals(managedBeanNames[i])) {
                    facesConfigModel.ensureManagedBean(beanName, javaClass.getName(), managedBeanScopes[i]);
                    return;
                }
            }
        }
    }

    public ManagedBean.Scope getManagedBeanEntryScope() {
        FacesConfigModel facesConfigModel = getFacesModelSet().getFacesConfigModel();
        ManagedBean mb = facesConfigModel.getManagedBean(getBeanName());
        if (mb != null)
            return mb.getManagedBeanScope();
        return null;
    }

    public ManagedBean.Scope getScope() {
        if (isBusted())
            return null;
        if (getBeansUnit() == null)
            return null;
        return getScope(getBeansUnit().getThisClass());
    }

    public ManagedBean.Scope getScope(JavaClass type) {
        if (type == null)
            return null;
        for(int i=0; i<managedBeanNames.length; i++) {
            if(beansUnit.getBaseBeanClassName().equals(managedBeanNames[i])) {
                return managedBeanScopes[i];
            }
        }
        return null;
    }

    //----------------------------------------------------------------------------- Unit Interaction

    /*
     * @see org.netbeans.modules.visualweb.insync.Model#writeLock(java.lang.String)
     */
    public UndoEvent writeLock(String description) {
        UndoEvent event = null;
        
        if(!isWriteLocked()) {
            //make sure the source is not dirty before modifying the model
            sync();
        }

        if (liveUnitWrapper != null) {
            event = liveUnitWrapper.isWriteLocked()
                ? undoManager.getCurrentEvent()
                : undoManager.startUndoableTask(description, this);

                liveUnitWrapper.writeLock(event);
        }
        else if (markupUnit != null)
            markupUnit.writeLock(event); // no undo events without a LiveUnit
        return event;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Model#isWriteLocked()
     */
    public boolean isWriteLocked() {
        if (liveUnitWrapper != null)
            return liveUnitWrapper.isWriteLocked();
        else if (markupUnit != null)
            return markupUnit.isWriteLocked();
        else
            return false;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Model#writeUnlock(org.netbeans.modules.visualweb.insync.UndoEvent)
     */
    public void writeUnlock(UndoEvent event) {
        if (liveUnitWrapper != null)
            liveUnitWrapper.writeUnlock(event);
        else if (markupUnit != null)
            markupUnit.writeUnlock(event);

        // If still write locked we're not done with the task
        if (liveUnitWrapper != null && !liveUnitWrapper.isWriteLocked())
            undoManager.finishUndoableTask(event);
    }

    protected boolean firstSyncCompleted;
    /**
     * General, high-level synchronizing of this model with any potential changes in the document(s)
     * and updating related models.
     * @see org.netbeans.modules.visualweb.insync.Model#sync()
     */
    protected synchronized void syncImpl() {
        if (!needSyncing) {
            return;
        }
        needSyncing = false;
        assert Trace.trace("insync.models", "LFM.sync markupFile:" + markupFile);  //NOI18N
    
        // Initial opening of units done on first sync
        boolean opened = false;
        // EAT: It used to do an &&, BUT it seems that if the files are added or moved
        // piece-meal, we will not get a complete picture.  We should really do a better
        // job of this when items are added and removed from a ModelSet, which is
        // where this SHOULD be.
        // This DOES remove the need for the order of files added to project :)
        // TODO XXX
        if (liveUnitWrapper == null || markupUnit == null) {
    		boolean hadLiveUnit = liveUnitWrapper != null;
    		boolean hadMarkupUnit = markupUnit != null;
            if (!hadMarkupUnit)
                openMarkupUnit();
            if (!hadLiveUnit)
                openJavaUnits();
            opened = (!hadLiveUnit && liveUnitWrapper != null) || (!hadMarkupUnit && markupUnit != null);
        }
        Unit unit = getTopmostUnit();
    
        // abort creation if the units did not open
        if (unit == null) {
            ErrorManager.getDefault().log("insync unit would not open: skipping read");  //NOI18N
            destroy();  // set will remove this model after scan
            return;
        }
    
        // XXX - this can happen when the project is closed while syncing is in progress.
        // Prevent NPE
        if (facesModelSet == null) {
        	return;
        }
        // Prevent NPE
        if (facesModelSet.getFacesContainer() == null) {
        	return;
        }
        
        // main unit synchronizing
        facesModelSet.getFacesContainer().getFacesContext();  // make sure the context is available to components via thread lookup
        boolean synced = unit.sync();
    
        if (liveUnitWrapper != null) {
            // Only do this if the unit was not busted on sync
            // update MBM as needed and add the xref accessors
            if (!unit.getState().isBusted() && synced) {
                ensureManagedBeansEntry();
                Object newProject = beansUnit.getModel().getProject().getProjectDirectory().getAttribute("NewProject"); //NOI18N
                if(!(newProject instanceof Boolean && (Boolean)newProject)) {
                    Object newFile = getFile().getAttribute("NewFile"); //NOI18N
                    if(newFile instanceof Boolean && (Boolean)newFile) {
                        FacesModel model = (FacesModel) beansUnit.getModel();
                        model.addXRefAccessors();
                    }
                }
            }
    
            // on first open, invoke fireContextCreated() to let viewers know of our new context
            if (opened && facesModelSet.hasDesignProjectListeners())
                facesModelSet.fireContextOpened(getLiveUnit());
        }else {
            needSyncing = true;
        }
        if (synced)
            fireModelChanged();
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Model#flush()
     */
    public void flushImpl() {
        assert Trace.trace("insync.models", "LFM.flush(" + markupFile + ")");  //NOI18N
        // if necessary, flush model to document(s) by locking & unlocking top-most unit
        Unit unit = getTopmostUnit();
        if (unit != null && unit.getState() == Unit.State.MODELDIRTY) {
            unit.writeLock(null);
            unit.writeUnlock(null);
        }
    }

    /**
     * Should only be used if you are certain this will not cause a problem.
     * At moment this is only used by refactoring mechanism.
     * See the caller in ModelSet.plannedChange for more information.
     *
     */
    public void flushNonJavaUnitsImpl() {
        Unit unit = getMarkupUnit();
        if (unit != null && unit.getState() == Unit.State.MODELDIRTY) {
            unit.writeLock(null);
            unit.writeUnlock(null);
        }
    }
    
    public boolean isValid() {
        if (!super.isValid())
            return false;
        if (getMarkupFile() != null && !getMarkupFile().isValid())
            return false;
        if (getJavaFile() != null && !getJavaFile().isValid())
            return false;
        return true;
    }

    public void saveUnits(){
        /*
         * Start a transaction here to deal with the fact that during a refactoring, at the end a savaAll happens,
         * but we also perform a save on the same documents, with the tx we prevent from the two operating
         * at same time.
         * Now, there could be a problem where one writes a file and the other does as well, but they are both
         * working with the same document, and the modifications to the document are guarded in this fashion
         * as well, and therefore we should be safe.
         */
        beginMdrTransation();
        try {
            if(javaUnit != null)
                javaUnit.save();
            if(markupUnit != null)
                markupUnit.save();
        } finally {
            endMdrTransaction();
        }
    }
    
    
        /** TODO - move to DesignerUtils!
     * Give the bean's DesignInfo a chance to annotate the bean after it has been created. This
     * should be called after the user has added the component to the webform, such as upon a drag
     * and drop or double click gesture; it should not be called when code is creating beans such
     * as on a webform resurrect.
     *
     * @param lbean The recently created bean to be annotated
     */
    public void beanCreated(DesignBean dbean) {
        // Annotate creation - give components a chance to update the DOLFM. For example, when you
        // drop a data table, beanCreatedSetup will go and add some default columns as well.
        DesignInfo di = dbean.getDesignInfo();
        if (di != null) {
            Result r = di.beanCreatedSetup(dbean);
            ResultHandler.handleResult(r, this);
        }
    }

    /**
     * Give the bean's DesignInfo a chance to annotate the bean after it has been pasted. This
     * should be called after the user has pasted the component intto the webform.
     *
     * @param lbean The recently created bean to be annotated
     */
    public void beanPasted(DesignBean dbean) {
        // Annotate creation - give components a chance to update the DOLFM. For example, when you
        // drop a data table, beanCreatedSetup will go and add some default columns as well.
        DesignInfo di = dbean.getDesignInfo();
        if (di != null) {
            Result r = di.beanPastedSetup(dbean);
            ResultHandler.handleResult(r, this);
        }
    }

    /**
     * Link the given bean to the given target bean. It is assumed that the bean has agreed to the
     * link in advance via DesignInfo.canLink().
     *
     * @param target The bean to be linked to
     * @param bean The bean to link
     */
    public void linkBeans(DesignBean target, DesignBean bean) {
        DesignInfo dbi = target.getDesignInfo();
        if (dbi != null) {
            Result r = dbi.linkBeans(target, bean);
            ResultHandler.handleResult(r, this);
        }
    }

    /**
     * Return the event index for the first event in an eventSet identifed by name
     *
     * @param eventSets
     * @param eventSetName
     * @return
     */
    static int getEventIndex(EventSetDescriptor[] eventSets, String eventSetName) {
        int offset = 0;
        for (int i = 0; i < eventSets.length; i++) {
            if (eventSets[i].getName().equals(eventSetName))
                return offset;
            offset += eventSets[i].getListenerMethodDescriptors().length;
        }
        return -1;
    }

    static String[] defaultEventSetNames = {
        "action", "item", "valueChange",  // event set names  //NOI18N
    };

    /**
     * Return the default DesignEvent for the given bean
     *
     * @param lbean The bean to look up event handler names for
     * @return
     */
    public static DesignEvent getDefaultEvent(DesignBean lbean) {
        // get the bean info, descriptors & see if there are any events
        BeanInfo bi = lbean.getBeanInfo();
        EventSetDescriptor[] eventSets = bi.getEventSetDescriptors();
        if (eventSets.length == 0) { // Show bean's declaration if there are no events
            assert Trace.trace("insync.models", "TODO - show bean's declaration");  //NOI18N
            return null;
        }

        // get flat list of all events
        DesignEvent[] events = lbean.getEvents();

        // now figure out the best default event index to use
        // if defEvent is -1, then we need to scan for a reasonable event
        int defEvent = bi.getDefaultEventIndex();
        if (defEvent >= events.length)
            defEvent = -1;

        for (int i = 0; defEvent < 0 && i < defaultEventSetNames.length; i++) {
            defEvent = getEventIndex(eventSets, defaultEventSetNames[i]);
            if (defEvent != -1 && (events[defEvent].getEventDescriptor().isHidden() || events[defEvent].getEventDescriptor().getEventSetDescriptor().isHidden())) {
                defEvent = -1;
            }
        }
        if (defEvent < 0)
            defEvent = 0;
        if (events[defEvent].getEventDescriptor().isHidden() || events[defEvent].getEventDescriptor().getEventSetDescriptor().isHidden()) {
            return null;
        } else {
            return events[defEvent];
        }
    }

    /**
     * Return an Array with intermixed DesignEvent, String entries.
     * Any hidden events will not be included.
     *
     * @param lbean The bean to look up event handler method names for
     * @return
     */
    public static ArrayList getVisibleEventsWithHandlerNames(DesignBean lbean) {
        assert Trace.trace("insync.models", "LFM.getEventHandlerNames(" + lbean + ")");  //NOI18N

        // walk through all the events and get the best text string for each
        DesignEvent[] events = lbean.getEvents();
        ArrayList result = new ArrayList(events.length);
        for (int i = 0; i < events.length; i++) {
            DesignEvent e = events[i];
            if (e.getEventDescriptor().isHidden() || e.getEventDescriptor().getEventSetDescriptor().isHidden()) {
                continue;
            }
            String dname = e.getEventDescriptor().getDisplayName();
            if (dname == null)
                dname = e.getEventDescriptor().getName();
            if (e.isHandled())
                dname += "=>" + e.getHandlerName() + "()";
            result.add(e);
            result.add(dname);
        }
        return result;
    }

    /**
     * Open the default "handler" for the unit itself. This is invoked for example when the user
     * double clicks on the document itself, not on any of the components on the page.
     **/
    public void openDefaultHandler() {
        Method m = facesUnit.getInitializerMethod();
        positionTheCursor(m, false);
    }

    /**
     * Open the default "handler" for a given bean. If no event handler is available for this bean,
     * it will open the default handler for a parent instead. If no such parent is found, it will
     * show the declaration for the given bean.
     *
     * @param lbean The bean to open a handler for
     */
    public void openDefaultHandler(DesignBean lbean) {
        assert Trace.trace("insync.models", "LFM.openDefaultHandler(" + lbean + ")");  //NOI18N
        for ( ; lbean != null; lbean = lbean.getBeanParent()) {
            DesignEvent event = getDefaultEvent(lbean);
            if (event != null) {
                openEventHandler(event);
                return;
            }
        }
        assert Trace.trace("insync.models", "TODO - show bean's declaration");  //NOI18N
    }
 
    public void openEventHandler(DesignEvent event) {
        boolean inserted = createEventHandler(event);
        String handlerName = event.getHandlerName();
        MethodDescriptor md = event.getEventDescriptor().getListenerMethodDescriptor();
        
        // now navigate the editor to the body of the newly created method
        Method m = beansUnit.getEventMethod(handlerName, md);
        positionTheCursor(m, inserted);
    }
    /**
     * Open the handler method for a given event in the editor, generating the default if needed.
     *
     * @param event
     */
    public boolean createEventHandler(DesignEvent event) {
        assert Trace.trace("insync.models", "LFM.openEventHandler(" + event + ")");  //NOI18N

        if (getLiveUnit() == null || beansUnit == null) {
            ErrorManager.getDefault().log("openEventHandler: a Java unit was null--skipping insync");  //NOI18N
            return false;
        }

        int linedelta = 0;
        boolean inserted = false;

        if (!event.isHandled()) {
            UndoEvent undo = null;
            String staticnav = null;
            try {
                String eventName = event.getEventDescriptor().getName();
                String description = NbBundle.getMessage(FacesModel.class, "OpenEventHandler", eventName); // NOI18N
                undo = writeLock(description);

                // transfer static navigation string to return value of handler
                if (eventName.equals("action")) {  //NOI18N
                    DesignBean lbean = event.getDesignBean();
                    DesignProperty lp = lbean.getProperty("action");  //NOI18N
                    if (lp != null) {
                        String source = lp.getValueSource();
                        if(source != null && !source.startsWith("#{"))
                            staticnav = source;   
                    }}

                event.setHandlerName(null);  // let the live event generate its own name

                if (staticnav != null) {
                    if (event instanceof BeansDesignEvent) {
                        ((BeansDesignEvent) event).updateReturnStrings(null, staticnav);
                    } else {
                        event.setHandlerMethodSource("\n        return \"" + staticnav + "\";");  //NOI18N
                    }
                }
                inserted = true;
            }
            finally {
                writeUnlock(undo);
            }

            // force a re-sync() (later) to get the model up to date with above source when
            if (staticnav instanceof String) {
                javaUnit.setSourceDirty();
            }
        }
        return inserted;
    }
    
    /**
     * Position the cursor in a blank line after the comment if the method is
     * newly inserted. Otherwise cursor is placed at the beginning of the first
     * statement 
     *
     * @param Method 
     * @param boolean indicates if the method is newly added
     */    
    void positionTheCursor(Method m, boolean inserted) {
        try {
            int[] pos = m.getCursorPosition(inserted);
            int lineNo = pos[0];
            int col = pos[1];

            assert Trace.trace("insync.models", "lineno=" + lineNo + " col=" + col);
            
            // Make sure that lineno is showing and the caret is position at line+col
            LineCookie lc = (LineCookie)Util.getCookie(javaFile, LineCookie.class);
            if (lc != null) {
                Line.Set ls = lc.getLineSet();
                if (ls != null) {
                    Line line = ls.getCurrent(lineNo);
                    line.show(Line.SHOW_GOTO, col);
                }
            }

            // Explicitly open the editor pane and request focus
            EditorCookie editorCookie = (EditorCookie)Util.getCookie(javaFile, EditorCookie.class);
            if (editorCookie != null) {
                editorCookie.open();
                javax.swing.JEditorPane[] panes = editorCookie.getOpenedPanes();

                // Make sure that the editor has focus
                if (panes != null && panes.length > 0) {
                    final javax.swing.JEditorPane editorPane = panes[0];
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                editorPane.requestFocus();
                            }
                        });
                }
            }
        }
        catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }

    //--------------------------------------------------------------------------- ModelSet Utilities

    /**
     * @param oldname
     * @param newname
     */
    public void updateAllBeanElReferences(String oldname, String newname) {
        facesModelSet.updateBeanElReferences(oldname, newname);
    }

    /**
     * @param oldname
     */
    public void removeAllBeanElReferences(String oldname) {
        facesModelSet.removeBeanElReferences(oldname);
    }

    //--------------------------------------------------------------------------------- Model Events

    /* Unused

    public interface LifeListener {
        public void modelOpened(FacesModel model);
        public void modelClosed(FacesModel model);
    }

    private static ArrayList lifeListeners = new ArrayList();

    public static void addModelLifeListener(Listener listener) {
        lifeListeners.add(listener);
    }

    public static void removeModelLifeListener(Listener listener) {
        lifeListeners.remove(listener);
    }

    public static LifeListener[] getModelLifeListeners() {
        return (LifeListener[])lifeListeners.toArray(new LifeListener[lifeListeners.size()]);
    }

    protected static void fireModelOpened(FacesModel model) {
        int n = lifeListeners.size();
        for (int i = 0; i < n; i++) {
            ((LifeListener)lifeListeners.get(i)).modelOpened(model);
        }
    }

    protected static void fireModelClosed(FacesModel model) {
        int n = lifeListeners.size();
        for (int i = 0; i < n; i++) {
            ((LifeListener)lifeListeners.get(i)).modelClosed(model);
        }
    }
    */

    /** Activate/deactive the DesignContext for this faces model. */
    public void setActivated(boolean activated) {
        if (this.activated != activated) {
            this.activated = activated;
            if (liveUnitWrapper != null) {
	            if (activated)
	                getLiveUnit().fireContextActivated();
	            else
	                getLiveUnit().fireContextDeactivated();
            }
        }
    }

    /** Return whether the given unit is activated */
    public boolean isActivated() {
        return activated;
    }

    private boolean activated;
    
    public void sourceUnitSaved(final SourceUnit unit) {
        // TODO !EAT: need to remove this when we get notification of saving instead of saved
        // We MUST queue this up, since there Cookie is removed after, and we need to find
        // out if there is a cookie after the save is done
/*
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // This is part of fact that this is a hack, getting notified of attribute changing
                // when file is being deleted
                if (getMarkupFile() != null && getMarkupFile().isValid()) {
                    sync();
                    DataObject dataObject = unit.getDataObject();
                    SaveCookie cookie = (SaveCookie) dataObject.getCookie(SaveCookie.class);
                    if (cookie != null)
                        try {
                            cookie.save();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                }
            }
        });
*/
    }
    
    public boolean isInRequestScope() {
        return getScope() == ManagedBean.Scope.REQUEST;
    }
    
    public boolean isPageBean() {
        if(getBeansUnit() != null)
            return getBeansUnit().isPageBean();
        return false;
    }
    
    public void resetOwner() {
        // We need to keep owner and facesModelSet in lock step, since facesModelSet is intended to be a type safe rendition of owner
        super.resetOwner();
        facesModelSet = null;
    }

    
// <separation of models>  moved from designer/WebForm.
    /**
     * Get the document associated with this webform.
     */
    public org.w3c.dom.Document getJspDom() {
        MarkupUnit unit = getMarkupUnit();

        if (unit == null) { // possible when project has closed
            return null;
        }

        return unit.getSourceDom();
    }
    
    public org.w3c.dom.Document getHtmlDom() {
        MarkupUnit unit = getMarkupUnit();

        if (unit == null) { // possible when project has closed
            return null;
        }

        return unit.getRenderedDom();
    }
    
    private DocumentFragment html;
    private Element body;
    /**
     * Return the HTML DOM associated with the source JSPX DOM
     * returned by {@link getDom}.
     * @return A DocumentFragment which represents an HTML rendered,
     *   JavaScript mutated, &lt;f:verbatim&gt;/&lt;ui:tag&gt; expanded
     *   view of the source DOM.
     */
    public DocumentFragment getHtmlDomFragment() {
        if (html == null) {
//            // XXX TODO There is not needed webform here.
//            FileObject markupFile = this.getModel().getMarkupFile();
////            html = FacesSupport.renderHtml(markupFile, null, !CssBox.noBoxPersistence);
//            html = InSyncService.getProvider().renderHtml(markupFile, null, !CssBox.noBoxPersistence);
//            // XXX FIXME Is this correct here?
//            FacesSupport.updateErrorsInComponent(this);
            html = FacesPageUnit.renderHtml(this, null);
        }

        return html;
    }

    /**
     * Return the &lt;body&gt; element associated with the rendered HTML
     * document
     */
    public Element getHtmlBody() {
        if (body == null) {
            body = findHtmlBody();
        }

        return body;
    }
    
    public void clearHtml() {
        this.html = null;
        this.body = null; // force new search
    }
    
    private Element findHtmlBody() {
        DocumentFragment htmlFragment = getHtmlDomFragment();
        Element bodyElement = null;
        if (htmlFragment != null) {
            // Is this a page fragment?
            FileObject markupFile = getMarkupFile();
            // XXX
            boolean isFragment = markupFile != null && "jspf".equals(markupFile.getExt()); // NOI18N
            boolean isPortlet = this.getFacesModelSet().getFacesContainer().isPortletContainer();
            if (isFragment || isPortlet) {
                // Just use the first element
                NodeList nl = htmlFragment.getChildNodes();
                for (int i = 0, n = nl.getLength(); i < n; i++) {
                    Node node = nl.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        bodyElement = (Element)node;
// <removing set/getRoot from RaveDocument>
//                            getJspDom().setRoot(bodyElement);
// </removing set/getRoot from RaveDocument>
                        break;
                    }
                }

//                    WebForm page = getContextPage();
                // XXX Get rid of this, why fragment keeps ref to one of the context pages??
                FileObject contextFile = DesignerServiceHack.getDefault().getContextFileForFragmentFile(markupFile);
                FacesModel page;
                if (contextFile == null) {
                    page = null;
                } else {
                    page = FacesModel.getInstance(contextFile);
                }

                if (page != null) {
                    // XXX Force sync first??
                    Element surroundingBody = page.getHtmlBody();

                    if (surroundingBody != null) {
//                            RaveElement.setStyleParent(bodyElement, surroundingBody);
                        CssProvider.getEngineService().setStyleParentForElement(bodyElement, surroundingBody);

                        // Make sure styles inherit right into the included content
//                            ((RaveDocument)getJspDom()).setCssEngine(page.getJspDom().getCssEngine());
//                        CssProvider.getEngineService().reuseCssEngineForDocument(getJspDom(), page.getJspDom());
                        CssProvider.getEngineService().reuseCssEngineForDocument(getHtmlDom(), page.getHtmlDom());

//                            XhtmlCssEngine engine = CssLookup.getCssEngine(bodyElement);
//                            XhtmlCssEngine engine = (XhtmlCssEngine)((CSSStylableElement)bodyElement).getEngine();
//
//                            if (engine != null) {
//                                engine.clearTransientStyleSheetNodes();
//                            }
                        CssProvider.getEngineService().clearTransientStyleSheetNodesForDocument(getJspDom());
                    }
                }
            } else {
                bodyElement = null;
            }

            if (bodyElement == null) {
                bodyElement = findBodyElement(htmlFragment);

// <removing set/getRoot from RaveDocument>
//                  //if (bodyElement == null) {
//                    //    // Insert one! Is this going to cause locking problems??? I
//                    //    // need to do this under a write lock...
//                    //}
//                    NodeList nl = html.getChildNodes();
//                    for (int i = 0, n = nl.getLength(); i < n; i++) {
//                        Node node = nl.item(i);
//                        if (node.getNodeType() == Node.ELEMENT_NODE) {
//                            getJspDom().setRoot((RaveElement)node);
//                            break;
//                        }
//                    }
// </removing set/getRoot from RaveDocument>
            }
        }
        
        return bodyElement;
    }

    private static Element findBodyElement(Node node) {
        Element bodyElement = Util.findDescendant(HtmlTag.BODY.name, node);

        if (bodyElement == null) {
            bodyElement = Util.findDescendant(HtmlTag.FRAMESET.name, node);
        }

        // TODO -- make sure body is lowercase tag. If not offer to tidy it!
        return bodyElement;
    }
// </separation of models>


    // >>> JSF support (DnD, refresh etc.)
//    public FacesDnDSupport getDnDSupport() {
//        return dndSupport;
//    }
    public JsfSupport getJsfSupport() {
        JsfSupportProvider jsfSupportProvider = Lookup.getDefault().lookup(JsfSupportProvider.class);
        if (jsfSupportProvider == null) {
            return new DummyJsfSupport();
        } else {
            return jsfSupportProvider.getDndSupport(this);
        }
    }
            
    public interface JsfSupport {
        public void moveBeans(DesignBean[] designBean, DesignBean liveBean);
        public void selectAndInlineEdit(DesignBean[] beans, DesignBean bean);
        public void refresh(boolean deep);
    } // End of JsfSupport.
    
    public interface JsfSupportProvider {
        public JsfSupport getDndSupport(FacesModel facesModel);
    } // End of JsfSupportProvider.
    
    public static class DummyJsfSupport implements JsfSupport {
        public void moveBeans(DesignBean[] designBean, DesignBean liveBean) {
        }

        public void selectAndInlineEdit(DesignBean[] beans, DesignBean bean) {
        }

        public void refresh(boolean deep) {
        }
    } // End of DummyJsfSupport.
    // <<< JSF support (DnD, refresh, etc.)
    
    
    /* Refresh and sync non page beans to update the outline
     * Workaround for bug#6468062
     */
    public void refreshAndSyncNonPageBeans(boolean deep){
    	// Bug Fix# 109681
    	// Prevent NPE. No need to refresh if this is a deleted FacesModel 
    	if (facesModelSet != null) {
	        refresh(deep);
			//The following call enables syncing of non page beans required
	        //to refresh outline
	        facesModelSet.findDesignContexts(new String[] {
	                "request", //NOI18N
	                "session", //NOI18N
	                "application" //NOI18N
	            });
    	}
    }
    
    /** XXX Moved from designer/WebForm#refresh, the insync part.
     * Refreshes the model
     * @deep If true, go all the way down to the insync markup unit
     *   and force a sync also
     */
    public void refresh(boolean deep) {
        if (deep) {
            MarkupUnit unit = getMarkupUnit();
            if (unit != null) {
                if (unit.getState() == Unit.State.MODELDIRTY) {
                    flush();
                }

                if (unit.getState() == Unit.State.CLEAN) {
                    unit.setSourceDirty();
                }

                if (unit.getState() == Unit.State.SOURCEDIRTY) {
                    sync();
                }
            }
        }

//        CssLookup.refreshEffectiveStyles(webform.getDom());
        CssProvider.getEngineService().refreshStylesForDocument(getJspDom());
        // XXX Should this be here too (or the above?).
        CssProvider.getEngineService().refreshStylesForDocument(getHtmlDom());
        
        // XXX
//        StyleSheetCache.getInstance().flush();
        CssProvider.getEngineService().flushStyleSheetCache();
        
        clearHtml();
    }
    
    private class ModelOperationListener implements OperationListener {
        public void operationPostCreate(OperationEvent ev) {}
        
        public void operationCopy(OperationEvent.Copy ev) {}
        
        public void operationMove(OperationEvent.Move ev) {
            FileObject fo = ev.getOriginalPrimaryFile();
            
            // The following computaion ensures that we react to a file rename only
            // after all files of a FacesModel are renamed e.g. .java and .jsp
            boolean doRemoveModel = false;
            if (markupUnit != null) {
                if(javaUnit.getFileObject() == fo && JsfProjectUtils.getJspForJava(ev.getObject().getPrimaryFile()) != null) {
                    doRemoveModel = true;
                } else if(file == fo && JsfProjectUtils.getJavaForJsp(ev.getObject().getPrimaryFile()) != null) {
                    doRemoveModel = true;
                }
            } else if (file == fo){
                doRemoveModel = true;
            }
            
            if(doRemoveModel){
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        getFacesModelSet().removeModel(FacesModel.this);
                    }
                });
            }
        }
        
        public void operationDelete(OperationEvent ev) {}
        
        public void operationRename(OperationEvent.Rename ev) {}
        
        public void operationCreateShadow(OperationEvent.Copy ev) {}
        
        public void operationCreateFromTemplate(OperationEvent.Copy ev) {}
    } // End of ModelSetOperationListener.
    
    public void addXRefAccessors() {
        FacesModelSet modelSet = getFacesModelSet();
        FacesConfigModel facesConfigModel = modelSet.getFacesConfigModel();
        ManagedBean managedBean = facesConfigModel.getManagedBean(beansUnit.getBeanName());
        ManagedBean.Scope scope = null;
        if (managedBean != null) {
            scope = managedBean.getManagedBeanScope();
        }
        if (scope == null) {
            scope = getScope(beansUnit.getThisClass());
        }
        Collection names = modelSet.getBeanNamesToXRef(scope, this);
        for (Iterator iterator=names.iterator(); iterator.hasNext(); ) {
            String name = (String) iterator.next();
            // Ignore adding a xref to myself
            if (!name.equals(beansUnit.getBeanName())) {
                managedBean = facesConfigModel.getManagedBean(name);
                beansUnit.addXRefAccessor(name, managedBean.getManagedBeanClass());
            }
        }
    }
}
