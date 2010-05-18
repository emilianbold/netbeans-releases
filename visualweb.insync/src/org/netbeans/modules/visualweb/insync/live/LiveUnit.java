/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.visualweb.insync.live;

import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.ContextMethod;
import com.sun.rave.designtime.Customizer2;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.event.DesignContextListener;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.ResolveResult;
import org.netbeans.modules.visualweb.insync.ParserAnnotation;
import org.netbeans.modules.visualweb.insync.ResultHandler;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.Unit;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.beans.Naming;
import org.netbeans.modules.visualweb.insync.beans.BeansUnit;
import org.netbeans.modules.visualweb.insync.faces.FacesBean;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;
import org.netbeans.modules.visualweb.insync.faces.HtmlBean;
import org.netbeans.modules.visualweb.insync.faces.MarkupBean;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.visualweb.jsfsupport.container.RaveFacesContext;
//import com.sun.rave.project.model.GenericItem;
//import com.sun.rave.project.model.WebAppProject;
import org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;

/**
 * @author Carl Quinn
 */
public class LiveUnit implements Unit, DesignContext, FacesDesignContext {

    protected FacesModel model;
    protected BeansUnit sourceUnit;
//    protected GenericItem projItem;
    protected FileObject file;

    protected final IdentityHashMap liveBeanHash = new IdentityHashMap();  // instance => lbean map
    protected final ArrayList liveBeanList = new ArrayList();      // lbean list, no specific order
    protected final SourceLiveRoot rootContainer;

    protected boolean itemAffected;  // flag to signal an item change event is needed after

    ContextMethodHelper contextMethodHelper; //Helper class which provides the ContextMethod API implementation
    protected boolean hasResurectedSinceInstantiated;


    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct an BeansUnit from an existing source file
     * @param model
     * @param sourceUnit
     * @param file
     */
    public LiveUnit(FacesModel model, BeansUnit sourceUnit, FileObject file) {
        this.model = model;
        this.sourceUnit = sourceUnit;
        this.rootContainer = new SourceLiveRoot(this);
        if (sourceUnit instanceof FacesPageUnit)
            this.rootContainer.setInstance(((FacesPageUnit)sourceUnit).getViewRoot());
        this.file = file;
        contextMethodHelper = new ContextMethodHelper(this);
        //Trace.enableTraceCategory("insync.live");
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#destroy()
     */
    public void destroy() {
        model = null;
        sourceUnit = null;
        this.rootContainer.setInstance(null);
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * @return
     */
    public BeansUnit getBeansUnit() {
        return sourceUnit;
    }

    /**
     * @return
     */
    public FacesModel getModel() {
        return model;
    }

    /**
     * Get the DesignBean that is managing a given java source bean.
     * Note: this method is specific to BeansDesignBean
     * @return the DesignBean that is managing a given java source bean.
     */
    public DesignBean getDesignBean(Bean bean) {
        assert Trace.trace("insync.live", "LU.getDesignBean:" + bean);
        ArrayList lbeans = getBeansList();
        for (int i = 0, n = lbeans.size(); i < n; i++) {
            Object o = lbeans.get(i);
            if (o instanceof BeansDesignBean) {
                BeansDesignBean lbean = (BeansDesignBean)o;
                //assert Trace.trace("insync.live", "  LU.getDesignBean checking:" + lbean);
                if (lbean.bean == bean)
                    return lbean;
            }
        }
        return null;
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#getBeanByName(java.lang.String)
     */
    public DesignBean getBeanByName(String name) {
        assert Trace.trace("insync.live", "LU.getDesignBean:" + name);
        int n = liveBeanList.size();
        //!CQ should use a hash here somehow... would need to flush or update on name changes
        for (int i = 0; i < n; i++) {
            DesignBean lb = (DesignBean)liveBeanList.get(i);
            if (lb.getInstanceName().equals(name))
                return lb;
        }
        return null;
    }

    /*
     * The live beans may have been replaced during a sync, since lookFor
     * was created.
     * So find the equivalent one to lookFor.
     */
    public DesignBean getBeanEquivalentTo(DesignBean lookFor) {
        if (lookFor == null) {
            return null;
        }
        
        // #102260 The bean is still alive, return it immediately.
        if (liveBeanList.contains(lookFor)) {
            return lookFor;
        }

        // XXX This is not fully working solution.
        // I-m using name here, but that may not be good enough.  What
        // if the bean is in a sub page ?  May need to do some hierarchy
        // based lookup, but for now this will do
        return getBeanByName(lookFor.getInstanceName());
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#addResource(java.net.URL, boolean)
     */
    public String addResource(URL resource, boolean copy) throws IOException {
        String updatedRes = null;
        updatedRes = JsfProjectUtils.addResource(file, resource, copy);
        return updatedRes;
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#resolveResource(java.lang.String)
     */
    public URL resolveResource(String resource) {
        try {
            URL fileUrl = file.getURL();
            return new URL(fileUrl, resource);
        } catch (java.net.MalformedURLException mue) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue);
        } catch(FileStateInvalidException fsie) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, fsie);
        }
        return null;
    }

    //---------------------------------------------------------------------------------- DisplayInfo

    /*
     * @see com.sun.rave.designtime.DisplayInfo#getDisplayName()
     */
    public String getDisplayName() {
        String beanName = sourceUnit.getBeanName();
        if(beanName != null) {
            return beanName.replace('$', '/');
        }
        return null;
    }

    /*
     * @see com.sun.rave.designtime.DisplayInfo#getDescription()
     */
    public String getDescription() {
        return getDisplayName();
    }

    /*
     * @see com.sun.rave.designtime.DisplayInfo#getLargeIcon()
     */
    public Image getLargeIcon() {
        return null;
    }

    /*
     * @see com.sun.rave.designtime.DisplayInfo#getSmallIcon()
     */
    public Image getSmallIcon() {
        return null;
    }

    /*
     * @see com.sun.rave.designtime.DisplayInfo#getHelpKey()
     */
    public String getHelpKey() {
        return null;
    }

    //----------------------------------------------------------------------------------- Unit Input

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#getState()
     */
    public State getState() {
        // As good a guess as any as to what the state should be ?
        if (sourceUnit == null) {
            return Unit.State.BUSTED;
        }
        return sourceUnit.getState();
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#getErrors()
     */
    public ParserAnnotation[] getErrors() {
        return sourceUnit.getErrors();
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#readLock()
     */
    public void readLock() {
        sourceUnit.readLock();
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#readUnlock()
     */
    public void readUnlock() {
        sourceUnit.readUnlock();
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#sync()
     */
    public boolean sync() {
        assert Trace.trace("insync.live", "LU.sync");
        boolean wasBusted = getState().isBusted();
        // rebuild the live instances only when the source and hence models below change
        boolean synced = sourceUnit.sync();
        if (!hasResurectedSinceInstantiated) {
            hasResurectedSinceInstantiated = true;
            synced = true;
        }
        if (synced) {
            resurrect();
        }
        if (synced || wasBusted != getState().isBusted()) {
            fireContextChanged();
        }
        return synced;
    }

    /**
     * Add a new live bean to our containers & parent hierarchy.
     *
     * @param slbean
     * @param slparent
     * @param pos
     */
    private void addDesignBean(SourceDesignBean slbean, SourceDesignBean slparent, Position pos) {
        liveBeanList.add(slbean);
        Object instance = slbean.getInstance();
        if (instance != null && instance != Boolean.TRUE && instance != Boolean.FALSE) {
            liveBeanHash.put(instance, slbean);
        }
        if (slparent != null)
            slparent.addLiveChild(slbean, pos);
//        registerProjectListener(slbean);
    }

    /**
     * Resurrect a live bean from its source bean, including all the child beans.
     *
     * @param slparent
     * @param bean
     */
    private void resurrectDesignBean(SourceDesignBean slparent, Bean bean) {
        try {
            // Get the class and beaninfo objects
            BeanInfo beanInfo = bean.getBeanInfo();
            Class beanClass = beanInfo.getBeanDescriptor().getBeanClass();
            DesignInfo liveBeanInfo = getDesignInfo(beanClass, getModel().getFacesModelSet().getProjectClassLoader());

            // Perform the instance creation and parenting
            // Eat any exceptions and continue without an instance... !CQ can we really?
            Object instance = sourceUnit.instantiateBean(beanClass);
            if (instance != null) {
                // immediate parent may NOT be the desired instance parent, so walk up until accepted
                for (DesignBean slp = slparent;
                     slp != null && !bean.performInstanceParenting(instance, slp.getInstance(), null);
                     slp = slp.getBeanParent())
                    ;
            }

            //!CQ this chunk is specific to BeansDesignBean
            SourceDesignBean slbean = instantiateDesignBean(beanInfo, liveBeanInfo, slparent, instance, bean);

            addDesignBean(slbean, slparent, null);
            if (slbean.isContainer()) {
                Bean[] beankids = bean.getChildren();
                for (int i = 0; i < beankids.length; i++) {
                    // Sanity check
                    assert getDesignBean(beankids[i]) == null :
                        "LU.resurrectBean " + bean +
                        " PROBLEM: child already resurrected:" + beankids[i];
                    resurrectDesignBean(slbean, beankids[i]);
                }
            }
            // No need to call recursive setReady() method here as the resurrectDesignBean() is
			// going through the beans recursively
            slbean.ready = true;
        }
        catch (Exception e) {
            assert Trace.trace("insync.live", "LU.read: Bean type uninstantiable: " + bean.getType());
            //assert Trace.trace("insync.live", e);
        }
    }
// static int count;
    /**
     * Rebuild the entire live bean tree from underlying source beans
     */
    void resurrect() {
// System.out.println(Thread.currentThread());
// System.out.println("count: " + count++);
// new Exception().printStackTrace();
        // run through any old beans and let them cleanup their instances
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getBeansUnit().getClassLoader());
            for (Iterator i = liveBeanList.iterator(); i.hasNext(); ) {
                SourceDesignBean slb = (SourceDesignBean)i.next();
    //            unregisterProjectListener(slb);
                slb.invokeCleanupMethod();
            }

            // start fresh
            liveBeanHash.clear();
            liveBeanList.clear();

            // always add our generic rootContainer, cleaning it up first.
            rootContainer.clearLiveChildren();
            // Make sure the faces context is initialized properly to allow resolvers to have access to the correct context
            setFacesContextCurrentInstance();
            try {
                // retrieve the instance to go with the root...
                rootContainer.setInstance(sourceUnit.getRootInstance());

                // Update rootContainer's bean?: unit.sourceUnit.getHostClass();
                addDesignBean(rootContainer, null, null);

                // scan source beans and create matching live wrappers and bind them
                //!CQ this chunk is specific to BeansDesignBean
                Bean[] beans = sourceUnit.getBeans();
                assert Trace.trace("insync.live", "LU.read: resurrecting " + beans.length + " beans");
                for (int i = 0; i < beans.length; i++) {
                    // skip if has a parent--will be done by parent when it is resurrected
                    if (beans[i].getParent() == null)
                        resurrectDesignBean(rootContainer, beans[i]);
                }

                // run accross each new live bean and load its properties & fire its created events
                for (Iterator i = liveBeanList.iterator(); i.hasNext(); ) {
                    SourceDesignBean slbean = (SourceDesignBean)i.next();
                    slbean.loadProperties();
                    //slbean.loadEvents();  //!CQ why not these too?
                    //fireDesignBeanCreated(slbean); //!CQ not really creating, just reconstructing
                }
                fireContextChanged();  //!CQ is this a good one for notifying of a reconstruct?
            }catch(Exception exc){
                exc.printStackTrace();
            }finally {
                resetFacesContextCurrentInstance();
            } 
        }catch(Exception exc){
            exc.printStackTrace();
        }finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
            resetFacesContextCurrentInstance();
        } 
    }

    //---------------------------------------------------------------------------------- Unit Output

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#writeLock(org.netbeans.modules.visualweb.insync.UndoEvent)
     */
    public void writeLock(UndoEvent event) {
        sourceUnit.writeLock(event);
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#writeUnlock(org.netbeans.modules.visualweb.insync.UndoEvent)
     */
    public boolean writeUnlock(UndoEvent event) {
        boolean unlocked = sourceUnit.writeUnlock(event);
        if (unlocked) {
            if (itemAffected) {
                itemAffected = false;
                getModel().fireModelChanged();
            }
            flushContextData();  //!CQ this may be getting called way too much
        }
        return unlocked;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#isWriteLocked()
     */
    public boolean isWriteLocked() {
        return sourceUnit.isWriteLocked();
    }

    /**
     *
     */
    public void dumpTo(PrintWriter w) {
        //TODO
    }

    //---------------------------------------------------------------------------------- DesignContext

    /*
     * @see com.sun.rave.designtime.DesignContext#canCreateBean(java.lang.String, com.sun.rave.designtime.DesignBeanContainer)
     */
    public boolean canCreateBean(String type, DesignBean parent, Position pos) {
    	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
    		Thread.currentThread().setContextClassLoader(getBeansUnit().getClassLoader());
	        Bean beanParent = parent instanceof BeansDesignBean ? ((BeansDesignBean)parent).getBean() : null;
	        BeanInfo bi = sourceUnit.getBeanInfo(type);
	        return bi != null && canCreateAsChildOf(parent, bi, type) &&
	                canCreateChild(parent, type) && sourceUnit.canCreateBean(bi, beanParent);
    	} finally {    		
    	    Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    	}
    }

    /**
     * Check whether the given type can be created as a child of the given parent.
     * That's true by default, unless either the parent or the child refuses (through
     * checks in their DesignInfos.)
     *
     * @param parent The parent DesignBean
     * @param type The class of the potential child
     * @return Whether the child can be adopted by this parent
     */
    private boolean canCreateChild(DesignBean parent, String type) {
        if (parent == null) {
            return true;
        }
        try {
            Class typeClass = sourceUnit.getBeanClass(type);
            DesignInfo parentInfo = parent.getDesignInfo();
            if (parentInfo != null && !parentInfo.acceptChild(parent, null, typeClass)) {
                return false;
            }
            DesignInfo childInfo = getDesignInfo(typeClass, getModel().getFacesModelSet().getProjectClassLoader());
            if (childInfo != null && !childInfo.acceptParent(parent, null, typeClass)) {
                return false;
            }
        } catch (ClassNotFoundException cnfe) {
            org.openide.ErrorManager.getDefault().log("Couldn't find class " + type);
        }
        // No evidence to the contrary
        return true;
    }

    /**
     * Check whether the given type can be created as a child of the given parent. That's true by
     * default, unless the child specifies a particular set of eligible parents. If so,
     * only subtypes of the specified parents will be accepted as a parent for this component.
     */
    private boolean canCreateAsChildOf(DesignBean parent, BeanInfo bi, String type) {
        if (bi == null)
            return true;

        // Check specific "allowed parent" list
        BeanDescriptor bd = bi.getBeanDescriptor();
        if (bd == null) {
            return true;
        }
        Object o = null; //bd.getValue(Constants.BeanDescriptor.REQUIRED_PARENT_TYPES);
        if (o != null && o instanceof String[]) {
            Class parentClass = UIComponent.class;
            if (parent == null) {
                if (sourceUnit instanceof FacesPageUnit) {
                    parent = getDesignBean(((FacesPageUnit)sourceUnit).getDefaultParent());
                }
            }
            if (parent != null) {
                parentClass = parent.getInstance().getClass();
            }
            String[] allowed = (String[])o;
            boolean found = false;
            for (int i = 0; i < allowed.length; i++) {
                try {
                    Class c = sourceUnit.getBeanClass(allowed[i]);
                    if (c.isAssignableFrom(parentClass)) {
                        found = true;
                        break;
                    }
                }
                catch (ClassNotFoundException cnfe) {
                    org.openide.ErrorManager.getDefault().log("Couldn't find allowed class " + allowed[i]);
                }
            }
            // Specified required parents but no eligible was found
            if (!found) {
                return false;
            }
        }
        return true;
    }

    /**
     * Perform the actual construction of the DesignBean implementation, including parent wiring for
     * both the DesignBean hierarchy as well as the instance hierarchy
     *
     * @param beanClass
     * @param beanInfo
     * @param liveBeanInfo
     * @param parent
     * @param facet
     * @param pos
     * @return
     */
    protected SourceDesignBean newDesignBean(Class beanClass, BeanInfo beanInfo, DesignInfo liveBeanInfo,
                                         DesignBean parent, String facet, Position pos, String instanceName) {
        if(instanceName == null) {                                 
            // Retrieve the optional suggested instance name base
            Object instanceNameO = beanInfo.getBeanDescriptor().getValue(Constants.BeanDescriptor.INSTANCE_NAME);
            instanceName = instanceNameO instanceof String ? (String)instanceNameO : null;
        }

        // Create the source representation for the live bean
        //!CQ this chunk is specific to BeansDesignBean
        Bean beanParent = parent instanceof BeansDesignBean ? ((BeansDesignBean)parent).getBean() : null;
        Bean bean = sourceUnit.addBean(beanInfo, beanParent, instanceName, facet, pos);

        // Readjust the parent if the source layer did. Especially filling in for a null.
        if (bean.getParent() != beanParent) {
            beanParent = bean.getParent();
            parent = getDesignBean(beanParent);
        }

        // Perform the instance creation and parenting
        // Eat any exceptions and continue without an instance...
        Object instance = null;
        if (parent == null)
            parent = rootContainer;
        try {
            instance = sourceUnit.instantiateBean(beanClass);
            if (instance != null)
                // immediate parent may NOT be the desired instance parent, so walk up until accepted
                for (DesignBean slp = parent;
                     slp != null && !bean.performInstanceParenting(instance, slp.getInstance(), pos);
                     slp = slp.getBeanParent())
                    ;
        }
        catch (Exception e) {
            assert Trace.trace("insync.live", "Caught " + e + " instantiating " + beanClass.getName());
            e.printStackTrace();
        }

        // Construct the actual DesignBean--either a leaf or a container implementation
        // Add it to our containers and wire up parentage
        //!CQ this chunk is specific to BeansDesignBean
        SourceDesignBean slparent = (SourceDesignBean)parent;
        SourceDesignBean slbean = instantiateDesignBean( beanInfo, liveBeanInfo, slparent, instance, bean);
        addDesignBean(slbean, slparent, pos);
        return slbean;
    }

    protected BeansDesignBean instantiateDesignBean(BeanInfo beanInfo, DesignInfo liveBeanInfo, SourceDesignBean parent, Object instance, Bean bean) {

        if (bean instanceof FacesBean) {
            return new FacesDesignBean(this, beanInfo, liveBeanInfo, parent, instance, (FacesBean) bean);
        }
        if (bean instanceof MarkupBean) {
            return new MarkupDesignBean(this, beanInfo, liveBeanInfo, parent, instance, (MarkupBean) bean);
        }
        if(bean.getTypeParameterNames() != null) {
            return new BeansDesignBeanExt(this, beanInfo, liveBeanInfo, parent, instance, bean);
        }
        
        return new BeansDesignBean(this, beanInfo, liveBeanInfo, parent, instance, bean);
    }

    /**
     * Perform the actual construction of the DesignBean implementation, including parent wiring for
     * both the DesignBean hierarchy as well as the instance hierarchy
     *
     * @param type
     * @param parent
     * @param facet
     * @param pos
     * @return
     * @throws ClassNotFoundException
     */
    protected SourceDesignBean newDesignBean(String type, DesignBean parent, String facet, Position pos, String instanceName) throws ClassNotFoundException {
        Class beanClass = sourceUnit.getBeanClass(type);
        if (beanClass != null) {
            BeanInfo beanInfo = BeansUnit.getBeanInfo(beanClass, getModel().getFacesModelSet().getProjectClassLoader());
            if (beanInfo != null) {
                DesignInfo liveBeanInfo = getDesignInfo(beanClass, getModel().getFacesModelSet().getProjectClassLoader());
                return newDesignBean(beanClass, beanInfo, liveBeanInfo, parent, facet, pos, instanceName);
            }
        }
        return null;
    }

    /**
     * Creates an array of DesignBeans given a clipboard image, parent and facet spec.
     *
     * @param image
     * @param parent
     * @param pos
     * @return
     */
    protected SourceDesignBean[] createDesignBeans(ClipImage image, DesignBean parent, Position pos) {
        // create all of the raw beans
        for (int i = 0; i < image.beans.length; i++)
            createDesignBean(image.beans[i], parent, pos);  //!CQ maybe skip nulls
        // now populate their properties
        for (int i = 0; i < image.beans.length; i++)
            if (image.beans[i].bean != null && image.beans[i].props != null)
                populateDesignBean(image.beans[i]);
        // count how many actually got created
        int count = 0;
        for (int i = 0; i < image.beans.length; i++)
            if (image.beans[i].bean != null)
                count++;
        // return an array of just the good ones
        SourceDesignBean[] slbs = new SourceDesignBean[count];
        for (int i = 0, c = 0; i < image.beans.length; i++)
            if (image.beans[i].bean != null)
                slbs[c++] = image.beans[i].bean;
        // strip away all of the temp bean refs in the image
        image.strip();
        //fireContextChanged();  //!CQ now firing beanCreated from paste
        return slbs;
    }

    /**
     * Creates a DesignBean given a clipboard image, parent and facet spec.
     *
     * @param image
     * @param parent
     * @param pos
     * @return
     */
    protected SourceDesignBean createDesignBean(SourceDesignBean.ClipImage image, DesignBean parent,
                                            Position pos) {
        try {
            String name = Naming.getBaseName(image.instanceName);
            image.bean = newDesignBean(image.type.getName(), parent, image.facetName, pos, name);
            if (image.bean != null) {
                // do children also...
                if (image.children != null) {
                    for (int j = 0; j < image.children.length; j++)
                        createDesignBean(image.children[j], image.bean, null);
                }
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return image.bean;
    }

    /**
     * Populate the properties of a DesignBean given a clipboard image
     *
     * @param image
     */
    protected void populateDesignBean(SourceDesignBean.ClipImage image) {
        if (image.props != null) {
            for (int j = 0; j < image.props.length; j++) {
                SourceDesignProperty.ClipImage pimage = image.props[j];
                SourceDesignProperty slp = (SourceDesignProperty)image.bean.getProperty(pimage.name);
                if (slp != null) {
                    Object val = pimage.value instanceof SourceDesignBean.ClipImage
                                     ? ((SourceDesignBean.ClipImage)pimage.value).bean
                                     : pimage.value;
                    if (val instanceof ValueBinding) {
                        try {
                            //val = ((ValueBinding)val).getValue(getFacesContext());
                            String valSrc = ((ValueBinding)val).getExpressionString();
                            slp.setValueSource(valSrc);
                        }
                        catch (Exception e) {
                            val = null;
                        }
                    }
                    else if (val != null)
                        slp.setValue(val);
                }
            }
        }
        
        if (image.events != null) {
            for (int j = 0; j < image.events.length; j++) {
                SourceDesignEvent.ClipImage eimage = image.events[j];
                SourceDesignEvent sle = (SourceDesignEvent)image.bean.getEvent(eimage.name);
                if (sle != null) {
                    Object val = eimage.handler;
                    if (val instanceof String) {
                        sle.setHandlerName((String)val);
                    }
                }
            }
        }    

        // do children also...
        if (image.children != null) {
            for (int j = 0; j < image.children.length; j++)
                populateDesignBean(image.children[j]);
        }
    }

    /**
     * Creates a DesignBean given type, parent and facet spec. Gathers needed info and calls
     * newDesignBean within a write lock to do the work.
     *
     * @param type
     * @param parent
     * @param facet
     * @param pos
     * @return
     */
    protected SourceDesignBean createBeanOrFacet(String type, DesignBean parent, String facet, Position pos) {
        if (!canCreateBean(type, parent, pos))
            return null;
        UndoEvent event = null;
        SourceDesignBean slbean = null;
        try {
            String description = NbBundle.getMessage(LiveUnit.class, "CreateBean");  //NOI18N
            event = model.writeLock(description);
            slbean = newDesignBean(type, parent, facet, pos, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            model.writeUnlock(event);
        }
        if (slbean != null) {
            fireBeanCreated(slbean);
        }
        return slbean;
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#createBean(java.lang.String, com.sun.rave.designtime.DesignBeanContainer)
     */
    public DesignBean createBean(String type, DesignBean parent, Position pos) {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getBeansUnit().getClassLoader());
            return createBeanOrFacet(type, parent, null, pos);
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#canMoveBean(com.sun.rave.designtime.DesignBean, com.sun.rave.designtime.DesignBean, com.sun.rave.designtime.Position)
     */
    public boolean canMoveBean(DesignBean lbean, DesignBean newParent, Position pos) {
    	// Prevent moving to root bean. The child beans can be added to the root bean only programatically. 
    	if (newParent instanceof SourceLiveRoot) {
    		return false;
    	}
    	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
    		Thread.currentThread().setContextClassLoader(getBeansUnit().getClassLoader());
	        // See if the target position is accepted by the involved components
	        Class typeClass = lbean.getInstance().getClass();
	        DesignInfo parentInfo = newParent.getDesignInfo();
	        if (parentInfo != null && !parentInfo.acceptChild(newParent, lbean, typeClass)) {
	            return false;
	        }
	        DesignInfo childInfo = lbean.getDesignInfo();
	        if (childInfo != null && !childInfo.acceptParent(newParent, lbean, typeClass)) {
	            return false;
	        }
	
	        return lbean instanceof SourceDesignBean && newParent.isContainer() &&
	               ((SourceDesignBean)lbean).unit == this && ((SourceDesignBean)newParent).unit == this;
	        //!CQ TODO more checks, & relax cross-unit moving
    	} finally {    		
    	    Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    	}
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#moveDesignBean(com.sun.rave.designtime.DesignBean, com.sun.rave.designtime.DesignBeanContainer)
     */
    public boolean moveBean(DesignBean lbean, DesignBean newParent, Position pos) {
        if (!canMoveBean(lbean, newParent, pos))
            return false;

        // Looks OK to move. Gather info
        SourceDesignBean slbean = (SourceDesignBean)lbean;
        Object instance = slbean.getInstance();
        Bean bean = ((BeansDesignBean)lbean).bean;  //!CQ assuming a Beans live bean for now
        SourceDesignBean oldparent = (SourceDesignBean)slbean.getBeanParent();

        UndoEvent event = null;
        try {
            String description = NbBundle.getMessage(LiveUnit.class, "MoveBean");  //NOI18N
            event = model.writeLock(description);

            // TODO - suppress firing of removes and additions here, should
            // only have a single "moved" at the end
            oldparent.removeLiveChild(slbean);
            if (instance != null)
                bean.performInstanceUnparenting(instance, oldparent.getInstance());
            sourceUnit.moveBean(bean, ((BeansDesignBean)newParent).bean, pos);
            ((SourceDesignBean)newParent).addLiveChild(slbean, pos);
            if (instance != null)
                bean.performInstanceParenting(instance, newParent.getInstance(), pos);
        }
        finally {
            model.writeUnlock(event);
        }
        fireBeanMoved(slbean, oldparent, pos);
        return true;
    }
    
    /*
     * Reorder the children beans.
     */
    public boolean reorderBeanChidren(DesignBean parentBean, int[] perm) {
        if (perm == null || perm.length <= 1) {
            return false;
        }
        
        SourceDesignBean parentSourceBean = (SourceDesignBean)parentBean;

        String description = NbBundle.getMessage(LiveUnit.class, "ReorderBeans");  //NOI18N
        UndoEvent event = model.writeLock(description);
        try {
			// Save the original order
            DesignBean[] sourceDesignBeans = parentSourceBean.getChildBeans();

            for (int i = 0; i < perm.length; i++) {
				// Move if needed
                if (i != perm[i]) {
                    moveBean(sourceDesignBeans[i], parentBean, new Position(perm[i]));
                }
            }          
        }
        finally {
            model.writeUnlock(event);
        }
        parentSourceBean.fireDesignBeanChanged();
        return true;
    }


    static final String FlavorMime = "application/x-creator-components;class=" +  //NOI18N
	                                 ClipImage.class.getName();
    
    /**
     * Clipboard image for a whole set of cut/copied SourceDesignBeans.
     * !TODO should merge this with BeanCreateInfo and BeanCreateInfoSet
     * @author cquinn
     */
    public static class ClipImage {

        SourceDesignBean.ClipImage[] beans;

        public static SourceDesignBean.ClipImage find(SourceDesignBean.ClipImage[] domain, Object bean) {
            for (int i = 0; i < domain.length; i++) {
                if (domain[i].bean.getInstance() == bean) {
                    return domain[i];
                }
                else if (domain[i].children != null) {
                    SourceDesignBean.ClipImage ci = find(domain[i].children, bean);
                    if (ci != null)
                        return ci;
                }
            }
            return null;
        }

        public String[] getTypes() {
            String[] types = new String[beans.length];
            for (int i = 0; i < beans.length; i++)
                types[i] = beans[i].type.getName();
            return types;
        }

        /**
         * Walk target bean list and point property cross references back into image
         */
        private void fixup(SourceDesignBean.ClipImage[] targtbeans) {
            for (int i = 0; i < targtbeans.length; i++) {
                SourceDesignBean.ClipImage beanim = targtbeans[i];
                if (beanim.props != null) {
                    for (int j = 0; j < beanim.props.length; j++) {
                        SourceDesignProperty.ClipImage propim = beanim.props[j];
                        SourceDesignBean.ClipImage ci = find(beans, propim.value);
                        if (ci != null)
                            propim.value = ci;
                    }
                }
                if (beanim.children != null)
                    fixup(beanim.children);
            }
        }

        /**
         * Walk target bean list and strip temporary live bean pointers
         */
        private static void strip(SourceDesignBean.ClipImage[] targtbeans) {
            for (int i = 0; i < targtbeans.length; i++) {
                targtbeans[i].bean = null;
                if (targtbeans[i].children != null)
                    strip(targtbeans[i].children);
            }
        }

        /**
         *
         */
        void fixup() {
            fixup(beans);
        }

        /**
         *
         */
        void strip() {
            strip(beans);
        }

        /*
         * @see java.lang.Object#toString()
         */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("[Context.ClipImage beans=");
            for (int i = 0; i < beans.length; i++)
                beans[i].toString(sb);
            sb.append("]");
            return sb.toString();
        }
    }
    
    static final DataFlavor flavor =
        new DataFlavor(FlavorMime,
                       NbBundle.getMessage(LiveUnit.class, "Components"));  //NOI18N
                       // can pass in ClassLoader too if useful    
    /*
     * @see com.sun.rave.designtime.DesignContext#copyBeans(com.sun.rave.designtime.DesignBean[])
     */
    public Transferable copyBeans(DesignBean[] beans) {

        final ClipImage clip = new ClipImage();
        clip.beans = new SourceDesignBean.ClipImage[beans.length];
        for (int i = 0; i < beans.length; i++)
            clip.beans[i] = ((SourceDesignBean)beans[i]).getClipImage();
        clip.fixup();
        clip.strip();

        Transferable transferable = new ExTransferable.Single(flavor) {
            public Object getData() {
                return clip;
            }
        };
        return transferable;
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#pasteBeans(java.awt.datatransfer.Transferable, com.sun.rave.designtime.DesignBean, com.sun.rave.designtime.Position)
     */
    public DesignBean[] pasteBeans(Transferable persistData, DesignBean parent, Position pos) {
        UndoEvent event = null;
        DesignBean[] lbeans = null;
        try {
            String description = NbBundle.getMessage(LiveUnit.class, "PasteBean");  //NOI18N
            event = model.writeLock(description);

            if (persistData != null) {
                DataFlavor[] df = persistData.getTransferDataFlavors();
                if (df == null)
                    return null;
                try {
                    for (int dfi = 0; dfi < df.length; dfi++) {
                        //Log.err.log("Flavor " + i + " is " + df[i]);
                        //Log.err.log("mimetype is " + df[i].getMimeType());
                        if (df[dfi].isMimeTypeEqual(FlavorMime)) {  //NOI18N
                            Object data = persistData.getTransferData(df[dfi]);
                            if (data instanceof ClipImage)
                                // perform the actual paste operation
                                lbeans = createDesignBeans((ClipImage)data, parent, pos);
                            break;  // done: either got it, or clipboard was funky
                        }
                    }
                }
                catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
        }
        }
        finally {
            model.writeUnlock(event);
        }
        if (lbeans != null) {
            for (int i = 0; i < lbeans.length; i++) {
                fireBeanCreated(lbeans[i]);
            }
            for (int i = 0; i < lbeans.length; i++) {
                getModel().beanPasted(lbeans[i]);
            }
        }
        return lbeans;
    }

    /**
     * Called during deletion to remove the bean from its parent & remove all the bean's persistence
     * source
     *
     * @param lbean The bean to remove from all containers.
     */
    protected void removeBean(DesignBean lbean) {
        assert Trace.trace("insync.live", "LU.removeDesignBean " + lbean);
        Object instance = lbean.getInstance();
        liveBeanHash.remove(instance);
        liveBeanList.remove(lbean);
        SourceDesignBean lparent = (SourceDesignBean)lbean.getBeanParent();
        lparent.removeLiveChild((SourceDesignBean)lbean);

        String name = lbean.getInstanceName();
        Bean bean = ((BeansDesignBean)lbean).bean;
        if (instance != null)
            bean.performInstanceUnparenting(instance, lparent.getInstance());
        sourceUnit.removeBean(bean);
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#deleteBean(com.sun.rave.designtime.DesignBean)
     */
    public boolean deleteBean(DesignBean lbean) {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getBeansUnit().getClassLoader());
            if (lbean == rootContainer || lbean == null || !(lbean instanceof SourceDesignBean))
                return false;
            UndoEvent event = null;
            try {
                String description = NbBundle.getMessage(LiveUnit.class, "DeleteBean");  //NOI18N
                event = model.writeLock(description);
                SourceDesignBean slbean = (SourceDesignBean)lbean;
                if (slbean.isContainer()) {
                    DesignBean[] lbeans = slbean.getChildBeans();
                    for (int i = 0; i < lbeans.length; i++) {
                        boolean ok = deleteBean(lbeans[i]);
                        if (!ok)
                            return false;
                    }
                }
//              unregisterProjectListener(lbean);
                slbean.invokeCleanupMethod();
                removeBean(lbean);
            }
            finally {
                model.writeUnlock(event);
            }
            fireBeanDeleted(lbean);
            return true;
        } finally {         
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }

//    /** DesignBeans that implement DesignProjectListener should be
//     * registered as project listeners when created, and unregistered
//     * when tossed away.
//     */
//     private void unregisterProjectListener(DesignBean bean) {
//         if (bean.getDesignInfo() instanceof DesignProjectListener) {
//             DesignProjectListener listener = (DesignProjectListener)bean.getDesignInfo();
//             if (model.isActivated()) {
//                 listener.contextDeactivated(this);
//             }
//             model.getFacesModelSet().removeDesignProjectListener(listener);
//         }
//     }
//
//    /** DesignBeans that implement DesignProjectListener should be
//     * registered as project listeners when created, and unregistered
//     * when tossed away.
//     */
//     private void registerProjectListener(DesignBean bean) {
//         if (bean.getDesignInfo() instanceof DesignProjectListener) {
//             DesignProjectListener listener = (DesignProjectListener)bean.getDesignInfo();
//             if (model.isActivated()) {
//                 listener.contextActivated(this);
//             }
//             model.getFacesModelSet().addDesignProjectListener(listener);
//         }
//     }

    /*
     * @see com.sun.rave.designtime.DesignContext#getBean(java.lang.Object)
     */
    public DesignBean getBeanForInstance(Object instance) {
        return (DesignBean)liveBeanHash.get(instance);
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#getRootContainer()
     */
    public DesignBean getRootContainer() {
        return rootContainer;
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#getBeans()
     */
    public DesignBean[] getBeans() {
        return (DesignBean[])liveBeanList.toArray(new DesignBean[liveBeanList.size()]);
    }

    /**
     * Return a list of the DesignBeans. NOTE: This returns the original list
     * used internally - do <b>NOT</b> muck with it! It is intended for internal
     * functions needing to iterate over the array contents quickly.
     */
    public ArrayList getBeansList() {
        return liveBeanList;
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#getBeansOfType(java.lang.Class)
     */
    public DesignBean[] getBeansOfType(Class type) {
        ArrayList lbeans = getBeansList();
        ArrayList bst = new ArrayList();
        for (int i = 0, n = lbeans.size(); i < n; i++) {
            DesignBean b = (DesignBean)lbeans.get(i);
            Object instance = b.getInstance();
            Class beanType;
            if (instance != null ) {
                beanType = instance.getClass();
            } else {
                beanType = b.getBeanInfo().getBeanDescriptor().getBeanClass();
            }
            if(type.isAssignableFrom(beanType)) {
                bst.add(b);
            }
        }
        return (DesignBean[])bst.toArray(new DesignBean[bst.size()]);
    }

    /**
     * User data managed and persisted by this context.
     */
    HashMap userData = new HashMap();

    /*
     * @see com.sun.rave.designtime.DesignContext#setContextData(java.lang.String, java.lang.Object)
     */
    public void setContextData(String key, Object data) {
        userData.put(key, data);
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#getContextData(java.lang.String)
     */
    public Object getContextData(String key) {
        if (key.equals(Constants.ContextData.SCOPE)) {
            ManagedBean.Scope scope = model.getManagedBeanEntryScope();
            if (scope == null)
                return null;
            return scope.toString();
        } else if (key.equals(Constants.ContextData.CLASS_NAME)) {
            //Return the full class name (String) for the backing file
            return sourceUnit.getThisClass().getName();
        } else if (key.equals(Constants.ContextData.BASE_CLASS)) {
            //Return the base class (Class) object for the backing file
            return sourceUnit.getBaseBeanClass();
        } else if (key.equals(Constants.ContextData.PAGE_RESOURCE_URI)) {
            // return the page resource URI (java.net.URI) for the jsp file or null (web/Page1.jsp)
            return getModel().getMarkupResourceRelativeUri();
        } else if (key.equals(Constants.ContextData.JAVA_RESOURCE_URI)) {
            // return the backing bean resource URI (java.net.URI) for the backing source file (src/webapplication1/Page1.java)
            return getModel().getJavaResourceRelativeUri();
        } else if (key.equals(Constants.ContextData.INIT_METHOD) ||
                   key.equals(Constants.ContextData.PREPROCESS_METHOD) ||
                   key.equals(Constants.ContextData.PRERENDER_METHOD) ||
                   key.equals(Constants.ContextData.DESTROY_METHOD)) {
            //Extract the method name
            int index = key.lastIndexOf("Method");
            if(index > 0) {
                String methodName = key.substring(0, index);
                //Return the asked ContextMethod
                return contextMethodHelper.getContextMethod(methodName);
            }
            return null;
        } else if (key.equals(Constants.ContextData.CSS_STYLE_CLASS_DESCRIPTORS)) {
            MarkupUnit munit = model.getMarkupUnit();
            if (munit != null) {
//                return munit.getCssEngine().getStyleClasses().toString();
                // XXX Changed on Gregory's request to rather provide an array of Objects
                // then a String representing entire collection, however it is still
                // not according the API doc (see Constants.ConstextData.CSS_STYLE_CLASS_DESCRIPTORS).
//                Collection col = CssProvider.getEngineService().getCssStyleClassesForDocument(munit.getSourceDom());
                Collection col = CssProvider.getEngineService().getCssStyleClassesForDocument(munit.getRenderedDom());
                        
                List styleClasses = new ArrayList();
                for (Iterator it = col.iterator(); it.hasNext(); ) {
                    // XXX There should be some known type (but not the batik's type).
                    Object styleClass = it.next();
                    if (styleClass != null) {
                        styleClasses.add(styleClass);
                    }
                }
                return styleClasses.toArray(new Object[styleClasses.size()]);
            }
            return null;
        } else if (Constants.ContextData.DATASOURCE_NAMES.equals(key)) {
            ArrayList beans = getBeansList();
            StringBuffer names = new StringBuffer();
            boolean doneFirst = false;
            for (int i = 0, n = beans.size(); i < n; i++) {
                DesignBean bean = (DesignBean)beans.get(i);
                Class beanClass = bean.getBeanInfo().getBeanDescriptor().getBeanClass();
                if (javax.sql.RowSet.class.isAssignableFrom(beanClass)) {
                    DesignProperty[] lps = bean.getProperties();
                    for (int j = 0; j < lps.length; j++) {
                        if (lps[j].getPropertyDescriptor().getName().equals("dataSourceName")) {  //NOI18N
                            Object v = lps[j].getValue();
                            if (v instanceof String) {
                                if (doneFirst)
                                    names.append(",");
                                else
                                    doneFirst = true;
                                names.append((String)v);
                            }
                        }
                    }
                }
            }
            return names.toString();
        } else {
            Object ud = userData.get(key);
            if (ud == null) {
                // PROJECTTODO2: cleanup - should I be using attributes on file ?
                ud = file.getAttribute(key);
                if (ud == null)
                    ud = "";
                userData.put(key, ud);
            }
            return ud;
        }
    }

    /**
     * Flush our user data map to our underlying project item.
     */
    protected void flushContextData() {
        for (Iterator i = userData.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry)i.next();
            Object data = entry.getValue();
            String string = data != null ? data.toString() : null;
            // PROJECTTODO2: Should I be storing data on the file object or not ?
            Util.updateItemProperty(file, (String)entry.getKey(), string);
        }
        // PROJECTTODO2: cleanup
        // How to fire off these events on data source names that are dynamically computed
        // Datasource is the only thing that MAY need this, AND should not needed once it uses the ModelSetListener stuff
        if (file == null)
            return;
        String key = Constants.ContextData.DATASOURCE_NAMES;
        String value = (String) getContextData(key);
        Util.updateItemProperty(file, key, value);
    }

    //------------------------------------------------------------------------ ContextMethod Methods

    /**
     * Returns a set of {@link ContextMethod} objects describing the public methods declared on this
     * DesignContext (source file).
     *
     * @return An array of {@link ContextMethod} objects, describing the public methods declared on
     *         this DesignContext (source file)
     */
    public ContextMethod[] getContextMethods() {
        return contextMethodHelper.getContextMethods();
    }

    /*
     * Returns the owned beansunit
     */
    public BeansUnit getSourceUnit() {
        return sourceUnit;
    }

    /**
     * Returns a {@link ContextMethod} object describing the public method with the specified name
     * and parameter types.  Returns <code>null</code> if no public method exists on this
     * DesignContext with the specified name and parameter types.
     *
     * @param methodName The method name of the desired context method
     * @param parameterTypes The parameter types of the desired context method
     * @return A ContextMethod object describing the requested method, or <code>null</code> if no
     *         method exists with the specified name and parameter types
     */
    public ContextMethod getContextMethod(String methodName, Class[] parameterTypes) {
        return contextMethodHelper.getContextMethod(methodName, parameterTypes);
    }

    /**
     * <p>Creates a new public method in the source code for this DesignContext.  The passed
     * ContextMethod <strong>must</strong> specify at least the designContext and methodName, and
     * <strong>must not</strong> describe a method that already exists in the DesignContext source.
     * To update an existing method, use the <code>updateContextMethod()</code> method.  These
     * methods are separated to help prevent accidental method overwriting.  The following table
     * details how the specified ContextMethod is used for this method:</p>
     *
     * <p><table border="1">
     * <tr><th>designContext <td><strong>REQUIRED.</strong> Must match the DesignContext that is
     *         being called.  This is essentially a safety precaution to help prevent accidental
     *         method overwriting.
     * <tr><th>methodName <td><strong>REQUIRED.</strong> Defines the method name.
     * <tr><th>parameterTypes <td>Defines the parameter types.  If <code>null</code> or an empty
     *         array is specified, the created method will have no arguments.
     * <tr><th>parameterNames <td>Defines the parameter names.  If <code>null</code> or an empty
     *         array is specified (or an array shorter than the parameterTypes array), default
     *         argument names will be used.
     * <tr><th>returnType <td>Defines the return type.  If <code>null</code> is specified, the
     *         created method will have a <code>void</code> return type.
     * <tr><th>throwsTypes <td>Defines the throws clause types.  If <code>null</code> is specified,
     *         the created method will have no throws clause.
     * <tr><th>bodySource <td>Defines the method body Java source code.  If <code>null</code> is
     *         specified, the method will have an empty body.  If the value is non-null, this must
     *         represent valid (compilable) Java source code.
     * <tr><th>commentText <td>Defines the comment text above the newly created method.  If
     *         <code>null</code> is specified, no comment text will be included.
     * </table></p>
     *
     * @param method A ContextMethod object representing the desired public method.
     * @return <code>true</code> if the method was created successfully, or <code>false</code> if
     *         it was not.
     * @throws IllegalArgumentException If there was a syntax error in any of the ContextMethod
     *         settings, or if the ContextMethod represents a method that already exists on this
     *         DesignContext (<code>updateContextMethod()</code> must be used in this case to avoid
     *         accidental method overwriting)
     */
    public boolean createContextMethod(ContextMethod method) throws IllegalArgumentException {
        return contextMethodHelper.createContextMethod(method);
    }

    /**
     * <p>Updates an existing public method in the source code for this DesignContext.  The passed
     * ContextMethod will be used to locate the desired public method to update using the
     * designContext, methodName, and parameterTypes.  This method may only be used to update the
     * parameterNames, returnType, throwsTypes, bodySource, or commentText.  Any other changes
     * actually constitute the creation of a new method, as they alter the method signature.  To
     * create a new method, the <code>createContextMethod()</code> method should be used.  These
     * operations are separated to help prevent accidental method overwriting.  The following table
     * details how the specified ContextMethod is used for this method:</p>
     *
     * <p><table border="1">
     * <tr><th>designContext <td><strong>REQUIRED.</strong> Must match the DesignContext that is
     *         being called.  This is essentially a safety precaution to help prevent accidental
     *         method overwriting.
     * <tr><th>methodName <td><strong>REQUIRED.</strong> Specifies the desired method name.
     * <tr><th>parameterTypes <td><strong>REQUIRED.</strong> Specifies the desired method's
     *         parameter types (if it has any).  If <code>null</code> or an empty array is
     *         specified, the desired method is assumed to have zero arguments.
     * <tr><th>parameterNames <td>Defines the parameter names.  If <code>null</code> or an empty
     *         array is specified (or an array shorter than the parameterTypes array), default
     *         argument names will be used.
     * <tr><th>returnType <td>Defines the method's return type.  If <code>null</code> is specified,
     *         the method is assumed to have a <code>void</code> return type.
     * <tr><th>throwsTypes <td>Defines the throws clause types.  If <code>null</code> is specified,
     *         the resulting method will have no throws clause.
     * <tr><th>bodySource <td>Defines the method body Java source code.  If <code>null</code> is
     *         specified, the resulting method body will be empty.  If the value is non-null, this
     *         must represent valid (compilable) Java source code.  Note that a method with a
     *         non-void return type <strong>must</strong> return a value.
     * <tr><th>commentText <td>Defines the comment text above the newly created method.  If
     *         <code>null</code> is specified, no comment text will be included.
     * </table></p>
     *
     * @param method The desired ContextMethod representing the method to be updated
     * @return The resulting ContextMethod object (including any updates from the process)
     * @throws IllegalArgumentException If there was a syntax error in any of the ContextMethod
     *         settings, or if the ContextMethod does not exist in this DesignContext.
     */
    public ContextMethod updateContextMethod(ContextMethod method) throws IllegalArgumentException {
        return contextMethodHelper.updateContextMethod(method);
    }

    /**
     * <p>Removes an existing method from the source code for this DesignContext.  The passed
     * ContextMethod will be used to locate the desired method to remove using the designContext,
     * methodName, and parameterTypes.  No other portions of the ContextMethod are used.  The
     * following table details how the specified ContextMethod is used for this method:</p>
     *
     * <p><table border="1">
     * <tr><th>designContext <td><strong>REQUIRED.</strong> Must match the DesignContext that is
     *         being called.  This is essentially a safety precaution to help prevent accidental
     *         method removal.
     * <tr><th>methodName <td><strong>REQUIRED.</strong> Specifies the desired method name.
     * <tr><th>parameterTypes <td><strong>REQUIRED.</strong> Specifies the desired method's
     *         parameter types (if it has any).  If <code>null</code> or an empty array is
     *         specified, the desired method is assumed to have zero arguments.
     * <tr><th>parameterNames <td>Ignored.
     * <tr><th>returnType <td>Ignored.
     * <tr><th>throwsTypes <td>Ignored.
     * <tr><th>bodySource <td>Ignored.
     * <tr><th>commentText <td>Ignored.
     * </table></p>
     *
     * @param method A ContextMethod object defining the method to be removed
     * @return <code>true</code> if the method was successfully removed
     * @exception IllegalArgumentException if the specified ContextMethod does not exist or is not
     *            public on this DesignContext
     */
    public boolean removeContextMethod(ContextMethod method) {
        return contextMethodHelper.removeContextMethod(method);
    }

    //-------------------------------------------------------------------------- MarkupDesignContext

    /*
     * @see com.sun.rave.designtime.markup.MarkupDesignContext#getCssPreviewImage
     */
    public Image getCssPreviewImage(String cssStyle, String[] cssStyleClasses, com.sun.rave.designtime.markup.MarkupDesignBean bean, int width, int height) {
        DesignerServiceHack designer = DesignerServiceHack.getDefault();
        if (designer != null) {
            return designer.getCssPreviewImage(cssStyle, cssStyleClasses, bean, width, height);
        }
        return null;
    }

    /*
     * @see com.sun.rave.designtime.markup.MarkupDesignContext#convertCssStyleToMap
     */
    public Map convertCssStyleToMap(String cssStyle) {
        // See if we have a MarkupUnit
        MarkupUnit munit = model.getMarkupUnit();
//        if (munit != null && munit.getCssEngine() != null) {
//            return munit.getCssEngine().styleToMap(cssStyle);
//        }
//        return new HashMap(0);
//        return CssProvider.getEngineService().getStyleMapFromStringForDocument(munit.getSourceDom(), cssStyle);
        return CssProvider.getEngineService().getStyleMapFromStringForDocument(munit.getRenderedDom(), cssStyle);
    }

    /*
     * @see com.sun.rave.designtime.markup.MarkupDesignContext#convertMapToCssStyle
     */
    public String convertMapToCssStyle(Map cssStyleMap) {
        MarkupUnit munit = model.getMarkupUnit();
//        if (munit != null && munit.getCssEngine() != null) {
//            return munit.getCssEngine().mapToStyle(cssStyleMap);
//        }
//        return "";
//        return CssProvider.getEngineService().getStringFromStyleMapForDocument(munit.getSourceDom(), cssStyleMap);
        return CssProvider.getEngineService().getStringFromStyleMapForDocument(munit.getRenderedDom(), cssStyleMap);
    }

    /*
     * @see com.sun.rave.designtime.faces.FacesDesignContext#getReferenceName()
     */
    public String getReferenceName() {     
        return getModel().getBeanName();
    }

    /*
     * @see com.sun.rave.designtime.faces.FacesDesignContext#canCreateFacet(java.lang.String, java.lang.String, com.sun.rave.designtime.DesignBean)
     */
    public boolean canCreateFacet(String facet, String type, DesignBean parent) {
        return canCreateBean(type, parent, null);  // , facet
    }

    /*
     * @see com.sun.rave.designtime.faces.FacesDesignContext#createFacet(java.lang.String, java.lang.String, com.sun.rave.designtime.DesignBean)
     */
    public DesignBean createFacet(String facet, String type, DesignBean parent) {
        return createBeanOrFacet(type, parent, facet, null);
    }

    /*
     * @see com.sun.rave.designtime.faces.FacesDesignContext#isValidBindingTarget(com.sun.rave.designtime.DesignBean)
     */
    public boolean isValidBindingTarget(DesignBean toBean) {
        if (!(toBean instanceof BeansDesignBean))
            return false;

        return ((BeansDesignBean)toBean).getBean().hasGetter();
    }

    /*
     * @see com.sun.rave.designtime.faces.FacesDesignContext#getBindingExpr(com.sun.rave.designtime.DesignBean, java.lang.String)
     */
    public String getBindingExpr(DesignBean toBean, String subExpr) {
        if (toBean == null)
            return null;

        // if toBean is an instance variable then the reference is just #{<this>.<toBean>}
        String ref = toBean.getInstanceName();
        if (toBean != rootContainer)
            ref = getReferenceName() + "." + ref;
        return "#{" + ref + subExpr + "}";
    }

    /*
     * @see com.sun.rave.designtime.faces.FacesDesignContext#getBindingExpr(com.sun.rave.designtime.DesignBean)
     */
    public String getBindingExpr(DesignBean toBean) {
        return getBindingExpr(toBean, "");
    }

    /*
     * @see com.sun.rave.designtime.faces.FacesDesignContext#resolveBindingExpr(java.lang.String)
     */
    public Object resolveBindingExpr(String expr) {
        ResolveResult result = resolveBindingExprToBean(expr);
        if (result.getRemainder() == null && result.getDesignBean() != null)
            return result.getDesignBean().getInstance();
        return null;
    }

    /*
     * @see com.sun.rave.designtime.faces.FacesDesignContext#resolveBindingExprToBean(java.lang.String)
     */
    public ResolveResult resolveBindingExprToBean(String expr) {
        if (expr.startsWith("#{") && expr.endsWith("}"))
            expr = expr.substring(2, expr.length() - 1);

        int lpos = 0;  // last token position
        int pos = expr.indexOf('.', lpos);  // delimiter position
        String name = pos >= 0 ? expr.substring(lpos, pos) : expr.substring(lpos);

        // match & gobble up the global bean reference for this root if there is one
        if (!name.equals(getReferenceName()))
            return new ResolveResult(null, expr.substring(lpos));  // last bean + remainder

        if (pos < 0)
            return new ResolveResult(rootContainer, null);  // exact match
        lpos = pos + 1;
        pos = expr.indexOf('.', lpos);
        name = pos >= 0 ? expr.substring(lpos, pos) : expr.substring(lpos);

        // match & gobble up the instance bean reference if there is one
        DesignBean lbean = getBeanByName(name);
        if (lbean == null)
            return new ResolveResult(rootContainer, expr.substring(lpos));  // last bean + remainder
        if (pos < 0)
            return new ResolveResult(lbean, null);  // exact match
        lpos = pos + 1;
        pos = expr.indexOf('.', 0);  // delimiter position
        //name = pos >= 0 ? expr.substring(lpos, pos) : expr.substring(lpos);

        // return the most recent bean found + the remainder
        return new ResolveResult(lbean, expr.substring(lpos));  // last bean + remainder
    }

    /*
     * @see com.sun.rave.designtime.faces.FacesDesignContext#getFacesContext()
     */
    public FacesContext getFacesContext() {
        // !EAT TODO HACK ???  Should resurrect do something smarter ?
        // This most likely is appropriate as we should not cause a cast exception on out unit
        if (sourceUnit instanceof org.netbeans.modules.visualweb.insync.faces.FacesUnit)
            return ((org.netbeans.modules.visualweb.insync.faces.FacesUnit)sourceUnit).getFacesContext();
        return null;
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#getProject()
     */
    public DesignProject getProject() {
        // XXX Possible NPE when model is null, invalid state?
        if (model == null) {
            return null;
        }
        return (FacesModelSet)model.getOwner();
    }

    //--------------------------------------------------------------------------- DesignContext Events

    private List listeners = new ArrayList();

    /*
     * @see com.sun.rave.designtime.DesignContext#addDesignContextListener(com.sun.rave.designtime.DesignContextListener)
     */
    public void addDesignContextListener(DesignContextListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /*
     * @see com.sun.rave.designtime.DesignContext#removeDesignContextListener(com.sun.rave.designtime.DesignContextListener)
     */
    public void removeDesignContextListener(DesignContextListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private List getDesignContextListenersList() {
        synchronized (listeners) {
            return new ArrayList(listeners);
        }
    }
        

    /*
     * @see com.sun.rave.designtime.DesignContext#getDesignContextListeners()
     */
    public DesignContextListener[] getDesignContextListeners() {
        List dcListeners = getDesignContextListenersList();
        return (DesignContextListener[])dcListeners.toArray(new DesignContextListener[dcListeners.size()]);
    }

    /**
     * Fire a context changed event to all of our listeners.
     */
    void fireContextChanged() {
        generation++;
        List dcListeners = getDesignContextListenersList();
        for (Iterator li = dcListeners.iterator(); li.hasNext(); )
            ((DesignContextListener)li.next()).contextChanged(this);
        getModel().fireModelChanged();
    }

    /**
     * Return the generation id for the current context.
     * Each contextChanged will create a new generation of
     * beans. ContextListeners can use this generation id
     * to coordinate changes; for example, two cooperating
     * views can tell each other about the contextChanged()
     * messages they receive to ensure that both have handled
     * this contextChange before doing some particular action. This
     * means that a listener may receive a contextChanged message
     * multiple times, and by looking at the generation (and remembering
     * which generation they most recently processed) they can avoid
     * processing the same update more than once.
     * @return The most recent generation we've notified ContextListeners
     *   of via contextChanged(DesignContext).
     */
    public long getContextGeneration() {
        return generation;
    }

    private long generation = 0;

    /**
     * Fire a context activated event to all of our listeners.
     * This is public so that FacesModel can call this
     */
    public void fireContextActivated() {
        List dcListeners = getDesignContextListenersList();
        for (Iterator li = dcListeners.iterator(); li.hasNext(); )
            ((DesignContextListener)li.next()).contextActivated(this);
        for (Iterator bi = liveBeanList.iterator(); bi.hasNext(); ) {
            DesignBean db = (DesignBean)bi.next();
            DesignInfo di = db.getDesignInfo();
            if (di != null) {
                di.beanContextActivated(db);
            }
        }
    }

    /**
     * Fire a context deactivated event to all of our listeners.
     * This is public so that FacesModel can call this
     */
    public void fireContextDeactivated() {
        List dcListeners = getDesignContextListenersList();
        for (Iterator li = dcListeners.iterator(); li.hasNext(); )
            ((DesignContextListener)li.next()).contextDeactivated(this);
        for (Iterator bi = liveBeanList.iterator(); bi.hasNext(); ) {
            DesignBean db = (DesignBean)bi.next();
            DesignInfo di = db.getDesignInfo();
            if (di != null) {
                di.beanContextDeactivated(db);
            }
        }
    }

    /**
     * Fire a bean created event to all of our listeners.
     *
     * @param bean The newly created bean.
     */
    protected void fireBeanCreated(DesignBean bean) {
        itemAffected = true;
        SourceDesignBean slbean = (SourceDesignBean)bean;
        setReady(slbean, true);
        List dcListeners = getDesignContextListenersList();
        for (Iterator li = dcListeners.iterator(); li.hasNext(); ) {
            try {
                ((DesignContextListener)li.next()).beanCreated(slbean);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Note: DI.beanCreatedSetup() is handled elsewhere...
    }

    /**
     * Fire a bean deleted event to all of our listeners.
     *
     * @param bean The recently deleted bean.
     */
    protected void fireBeanDeleted(DesignBean bean) {
        itemAffected = true;
        SourceDesignBean slbean = (SourceDesignBean)bean;
        if (slbean.ready) {
            List dcListeners = getDesignContextListenersList();
            for (Iterator li = dcListeners.iterator(); li.hasNext(); ) {
                try {
                    ((DesignContextListener)li.next()).beanDeleted(bean);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Recursively set the ready state
            setReady(slbean, false);
        }
        DesignInfo lbi = bean.getDesignInfo();
        if (lbi != null) {
            try {
                Result r = lbi.beanDeletedCleanup(bean);
                ResultHandler.handleResult(r, getModel());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        model.removeAllBeanElReferences(model.getBeanName() + "." + bean.getInstanceName());
    }

    /**
     * Fire a bean instance name changed event to all of our listeners.
     *
     * @param bean The recently renamed bean.
     * @param oldName The prior name of the bean.
     */
    protected void fireBeanInstanceNameChanged(DesignBean bean, String oldName) {
        SourceDesignBean slbean = (SourceDesignBean)bean;
        if (slbean.ready) {
            List dcListeners = getDesignContextListenersList();
            for (Iterator li = dcListeners.iterator(); li.hasNext(); ) {
                try {
                    ((DesignContextListener)li.next()).instanceNameChanged(bean, oldName);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        DesignInfo di = bean.getDesignInfo();
        if (di != null) {
            try {
                di.instanceNameChanged(bean, oldName);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire a bean changed event to all of our listeners.
     *
     * @param bean The recently changed bean.
     */
    protected void fireBeanChanged(DesignBean bean) {
        SourceDesignBean slbean = (SourceDesignBean)bean;
        if (slbean.ready) {
            List dcListeners = getDesignContextListenersList();
            for (Iterator li = dcListeners.iterator(); li.hasNext(); ) {
                try {
                    ((DesignContextListener)li.next()).beanChanged(bean);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        DesignInfo lbi = bean.getDesignInfo();
        if (lbi != null) {
            try {
                lbi.beanChanged(bean);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire a bean moved event to all of our listeners.
     *
     * @param bean The recently moved bean.
     * @param oldParent The bean's old parent.
     * @param pos The new position of the bean.
     */
    protected void fireBeanMoved(DesignBean bean, DesignBean oldParent, Position pos) {
        List dcListeners = getDesignContextListenersList();
        for (Iterator li = dcListeners.iterator(); li.hasNext(); ) {
            try {
                ((DesignContextListener)li.next()).beanMoved(bean, oldParent, pos);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*
        DesignInfo lbi = bean.getDesignInfo();
        if (lbi != null) {
            Result r = lbi.beanMoved(bean, oldParent, pos);
            ResultHandler.handleResult(r, getModel());
        }
        */
    }

    /**
     * Fire a property changed event to all of our listeners.
     *
     * @param prop The recently changed property.
     */
    protected void firePropertyChanged(DesignProperty prop, Object oldValue) {
        if (prop.getPropertyDescriptor().getName().equals("dataSourceName"))  //NOI18N
            itemAffected = true;

        SourceDesignBean slbean = (SourceDesignBean)prop.getDesignBean();
        if (slbean.ready) {
            List dcListeners = getDesignContextListenersList();
            for (Iterator li = dcListeners.iterator(); li.hasNext(); ) {
                DesignContextListener l = (DesignContextListener)li.next();
                assert Trace.trace("insync.live", "LU.fireDesignPropertyChanged prop:" + prop + " l:" + l);
                try {
                    l.propertyChanged(prop, oldValue);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        DesignInfo lbi = prop.getDesignBean().getDesignInfo();
        if (lbi != null) {
            try {
                lbi.propertyChanged(prop, oldValue);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fire a property changed event to all of our listeners.
     *
     * @param event The recently changed event.
     */
    protected void fireEventChanged(DesignEvent event) {
        SourceDesignBean slbean = (SourceDesignBean)event.getDesignBean();
        if (slbean.ready) {
            List dcListeners = getDesignContextListenersList();
            for (Iterator li = dcListeners.iterator(); li.hasNext(); ) {
                try {
                    ((DesignContextListener)li.next()).eventChanged(event);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        DesignInfo lbi = event.getDesignBean().getDesignInfo();
        if (lbi != null) {
            try {
                lbi.eventChanged(event);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //-------------------------------------------------------------------------------------- Utility
    
    // Recursively set the ready state
    private void setReady(SourceDesignBean bean, boolean ready) {
        bean.ready = ready;
        DesignBean[] childDesignBeans = bean.getChildBeans();
        if (childDesignBeans != null) {
            for (int i = 0; i < childDesignBeans.length; i++) {
                DesignBean childDesignBean = childDesignBeans[i];
                if (childDesignBean instanceof SourceDesignBean) {
                    setReady((SourceDesignBean) childDesignBean, ready);
                }
            }
        }
    }

    /**
     * TODO Now that we cache DesignInfos that we instantiate, we need to see how we
     * clean up and get rid of dependencies that MAY have been added to it.
     */
    private static Map designInfoCache = Collections.synchronizedMap(new WeakHashMap());
    
    private final String DESIGNINFO_SUFFIX = "DesignInfo";
    
    private String[] searchPath = Introspector.getBeanInfoSearchPath();
    
    /**
     * Marker object used to indicate that an entry in designInfos is null, since we cannot store
     * null into a map.
     */
    protected static final Object nullDesignInfoMarker = new Object();

    /**
     * Flush our design info cache, as well as the Introspector one.
     * Care must be taken when this is done, as their are dependencies on
     * the getDesignInfo() call to always return the same instance.
     * There is a slight disconnect here, that if the Introspector cache is
     * flushed, we may not be :(
     */
    public static void flushCaches() {
        designInfoCache.clear();
        Introspector.flushCaches();
    }

    /**
     * @see #flushCaches
     * 
     * @param clz  Class object to be flushed.
     * @throws NullPointerException If the Class object is null.
     */
    public static void flushFromCaches(Class clz) {
        if (clz == null) {
            throw new NullPointerException();
        }
        designInfoCache.remove(clz);
        Introspector.flushFromCaches(clz);
    }

    /**
     * Get the DesignInfo for a bean, returning the same instance for the same class, with
     * the exception that if a flushCaches() or flushFromCache() occurs.
     * The code here is very similar to java.beans.Introspector 
     */
    public DesignInfo getDesignInfo(Class beanClass, ClassLoader classLoder) {        
        BeanInfo bi = BeansUnit.getBeanInfo(beanClass, classLoder);
        if (bi instanceof DesignInfo){
            return (DesignInfo)bi;
        }

        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
            Thread.currentThread().setContextClassLoader(classLoder);
            String lbiClassName = beanClass.getName() + DESIGNINFO_SUFFIX;
            try {
                Object cacheEntry = designInfoCache.get(beanClass);
                if (cacheEntry == null) {
                    DesignInfo designInfo = (DesignInfo) instantiate(beanClass, lbiClassName, classLoder);
                    if (designInfo != null) {
                        designInfoCache.put(beanClass, designInfo);
                        return designInfo;
                    } else {
                        designInfoCache.put(beanClass, nullDesignInfoMarker);
                        return null;
                    }
                } else if (cacheEntry == nullDesignInfoMarker) {
                    return null;
                } else {
                    return (DesignInfo) cacheEntry;
                }
            } catch (ClassNotFoundException cnfe) {
                // Now try looking for <searchPath>.fooDesignInfo
                lbiClassName = lbiClassName.substring(lbiClassName.lastIndexOf('.') + 1);
                for (int i = 0; i < searchPath.length; i++) {
                    try {
                        String fullName = searchPath[i] + "." + lbiClassName;
                        DesignInfo designInfo = (DesignInfo) instantiate(beanClass, fullName, classLoder);
                        if (designInfo != null) {
                            if (designInfo.getBeanClass() == beanClass) {
                                designInfoCache.put(beanClass, designInfo);
                                return designInfo;
                            }
                        } else {
                            designInfoCache.put(beanClass, nullDesignInfoMarker);
                            return null;
                        }
                    } catch (ClassNotFoundException cnfe1) {
                        // Silently ignore, because we want to iterate over all path
                    } catch (InstantiationException ie) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ie);
                    } catch (IllegalAccessException iae) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, iae);
                    }
                }
            } catch (InstantiationException ie) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ie);
            } catch (IllegalAccessException iae) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, iae);
            }
            // It's normal to not find the DesignInfo and land in here
            designInfoCache.put(beanClass, nullDesignInfoMarker);
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }
    
    /** Copied from java.beans.Introspector
     * Try to create an instance of a named class.
     * First try the classloader of "sibling", then try the system
     * classloader then the class loader of the current Thread.
     */
    private Object instantiate(Class sibling, String className, ClassLoader classLoader)
    throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        // First check with sibling's classloader (if any).
        ClassLoader cl = sibling.getClassLoader();
        if (cl != null) {
            try {
                Class cls = cl.loadClass(className);
                return cls.newInstance();
            } catch (Exception ex) {
                // Just drop through and try the system classloader.
            }
        }
        
        // Now try the system classloader.
        try {
            cl = ClassLoader.getSystemClassLoader();
            if (cl != null) {
                Class cls = cl.loadClass(className);
                return cls.newInstance();
            }
        } catch (Exception ex) {
            // We're not allowed to access the system class loader or
            // the class creation failed.
            // Drop through.
        }
        
        // Use the classloader from the current Thread.
        cl = Thread.currentThread().getContextClassLoader();
        Class cls = cl.loadClass(className);
        return cls.newInstance();
    }

    /**
     * Return true iff the given component is visual. A nonvisual bean that is a UIComponent will
     * be still be rendered as part of the page (think of the stylesheet component for example),
     * but it will appear in the tray and can not be manipulated as a visual component in the
     * designer.
     *
     * @param bean The bean to be checked
     * @return true iff the bean is "visual"
     */
    public static boolean isVisualBean(DesignBean bean) {
        BeanInfo bi = bean.getBeanInfo();
        BeanDescriptor bd = null;
        if (bi != null) {
            bd = bi.getBeanDescriptor();
        }

        if (bd != null) {
            Object value = bd.getValue(Constants.BeanDescriptor.MARKUP_SECTION);
            if (value instanceof String) {
                String s = (String)value;
                if (s.equals("head"))  //NOI18N
                    return false;
            }
        }

        if (bean instanceof BeansDesignBean && ((BeansDesignBean)bean).getBean() instanceof HtmlBean)
            return true;
        if (!(bean.getInstance() instanceof UIComponent) || isTrayBean(bean))
            return false;

        if (bi == null)
            return false;  // No BeanInfo: not a visible component

        if (bd == null)
            return false;  // No BeanDescriptor: not a visible component

        // if the bean does not have a tag, then it can't be visual
        Object o = bd.getValue(com.sun.rave.designtime.Constants.BeanDescriptor.TAG_NAME);
        if (o == null)
            return false;

        return true;
    }

    /**
     * Return true iff the given component is positionable via CSS (e.g. using CSS2 CSS positioning
     * via a style attribute). If a component does not specify a positioning attribute, then it is
     * considered positionable iff it is a visual component (see isVisualBean());
     *
     * @see isVisualBean
     * @param bean The bean to be checked
     * @return true iff the bean is "positionable"
     */
    public static boolean isCssPositionable(DesignBean bean) {
        BeanInfo bi = bean.getBeanInfo();
        BeanDescriptor bd = null;
        if (bi != null) {
            bd = bi.getBeanDescriptor();

            if (bd != null) {
                // TODO: get constant for this, e.g.
                //   Constants.BeanDescriptor.CSS_POSITIONING);
                Object value = bd.getValue("cssPositioning"); // NOI18N
                if (value instanceof String) {
                    String s = (String)value;
                    return !s.equals("none");  // NOI18N
                }
            }
        }

        return isVisualBean(bean);
    }

    /**
     * Return true iff the given component should be shown in the tray.
     *
     * @param bean The bean to be checked
     * @return true iff the bean declares that it is a "tray" component
     *
     * !!CQ TODO: need a better name. What the heck is a tray?
     */
    public static boolean isTrayBean(DesignBean bean) {

        // Components declare themselves as tray components by setting the "trayComponent" flag to
        // Boolean.TRUE
        BeanInfo bi = bean.getBeanInfo();
        if (bi == null)
            return false;  // No BeanInfo: not a tray component

        BeanDescriptor bd = bi.getBeanDescriptor();
        if (bd == null)
            return false;  // No BeanDescriptor: not a tray component

        Object o = bd.getValue(com.sun.rave.designtime.Constants.BeanDescriptor.TRAY_COMPONENT);
        if (o instanceof Boolean) {
            return ((Boolean)o).booleanValue();
        }
        else if (o instanceof String) {
            try {
                return Boolean.getBoolean((String)o);
            }
            catch (Exception x) {
            }
        }

        return false;
    }

    //---------------------------------------------------------------------------------- Customizers

    private static HashMap customizers = new HashMap();

    /**
     * @param classname
     * @param lc
     */
    public static void registerCustomizer(String classname, Customizer2 lc) {
        customizers.put(classname, lc);
    }

    /**
     * @param classname
     * @return
     */
    public static Customizer2 getCustomizer(String classname) {
        return (Customizer2)customizers.get(classname);
    }

    //--------------------------------------------------------------------------------------- Object

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[LU sourceUnit:" + sourceUnit);
        sb.append(" root:" + rootContainer);
        sb.append("]");
        return sb.toString();
    }
    
    public void resetFacesContextCurrentInstance() {
        /* TODONOW
         * We need to fix issue where we do not set design context properly, we assume it was
         * set by something else, this is not good.  Reverting it back for now in order for me to
         * be able to commit and have sanity pass.  Will work on issue with Deva, Tor, Craig.
         */
        // TODO
        // We should really set the faces context to null, but there does seem to be any API to do that
        // Talk to Craig about it
        RaveFacesContext facesContext = (RaveFacesContext) getFacesContext();
        // Is there an actual faces context related to me that I need to init ?
        if (facesContext != null) {
//            facesContext.setDesignContext(null);
        }
    }

    public void setFacesContextCurrentInstance() {
        // Make sure the faces context is initialized properly to allow resolvers to have access to the correct context
        RaveFacesContext facesContext = (RaveFacesContext) getFacesContext();
        // Is there an actual faces context related to me that I need to init ?
        if (facesContext != null) {
            facesContext.setCurrentInstance();  // make sure the context is available to components via thread lookup
            facesContext.setDesignContext(this);  //!CQ HACK? to have to point its state to each lc all the time
        }
    }

}
