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
package org.netbeans.modules.visualweb.insync.beans;

import java.lang.reflect.Modifier;
import org.netbeans.modules.visualweb.insync.java.EventMethod;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
import org.netbeans.modules.visualweb.insync.java.Method;
import org.netbeans.modules.visualweb.insync.java.Statement;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import com.sun.rave.designtime.Position;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.ParserAnnotation;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.Unit;
import org.netbeans.modules.visualweb.insync.java.JavaUnit;
import org.netbeans.modules.visualweb.insync.java.ClassUtil;

/**
 * An abstract compilation Unit that represents a single outer (or 'this' or 'root') JavaBean class
 * under construction. Within this Unit will be instance Beans, as well as event handling methods.
 *
 * @author cquinn
 */
public class BeansUnit implements Unit {

    protected final List beans = new ArrayList();
    protected final Map nameCounters = new HashMap();  // type-mapped integer counters

    protected JavaUnit junit;     // underlying java source unit
    protected ClassLoader classLoader;  // classloader to use for loading bean and beaninfo classes

    protected String packageName;  // package this unit resides in

    protected JavaClass javaClass;

    protected ParserAnnotation error;

    protected Model model; // Not sure this should be here but I need it for some code. revisit. EAT TODO XXX

    // We need to force a sync in case I was constructed with units that we're already synced
    // If my sub units are already synced, I will not realize I myself need to be sync'ed.  This flag
    // is true once at least one sync has been completed.
    protected boolean syncedSinceConstructed;

    private boolean isPageBean = false;

    // Hold onto my scanner
    protected BeanStructureScanner beanStructureScanner;
    
    private String baseClassName;

    //--------------------------------------------------------------------------------- Construction

    /**
     * Set up the beans design-time flags
     */
    {
        java.beans.Beans.setDesignTime(true);
        java.beans.Beans.setGuiAvailable(true);
    }

    /**
     * Construct an BeansUnit from an existing source file
     *
     * @param junit  Underlying Java Unit
     * @param cl  Project-wide classloader
     * @param packageName  Package name for this class
     */
    public BeansUnit(JavaUnit junit, ClassLoader cl, String packageName, Model model) {
        this.junit = junit;
        this.packageName = packageName;
        this.model = model;
        setClassLoader(cl);
        //Trace.enableTraceCategory("insync.beans");
        this.syncedSinceConstructed = false;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#destroy()
     */
    public void destroy() {
        junit = null;
        beans.clear();
        nameCounters.clear();
        packageName = null;
        beanStructureScanner = null;
        javaClass = null;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#getState()
     */
    public State getState() {
        return junit.getState();
    }
    
    public boolean isPageBean() {
        return isPageBean;
    }

    public ParserAnnotation[] getErrors() {
        ParserAnnotation[] annotations = (junit == null)?ParserAnnotation.EMPTY_ARRAY:junit.getErrors();
        if (error != null) {
            System.arraycopy(annotations, 0, annotations = new ParserAnnotation[annotations.length+1], 0, annotations.length-1);
            annotations[annotations.length -1] = error;
        }
        return annotations;
    }

    /**
     * @return
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * @param cl
     */
    public void setClassLoader(ClassLoader cl) {
        classLoader = cl != null
            ? cl
            : (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
    }

    //----------------------------------------------------------------------------------- Unit Input

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#readLock()
     */
    public void readLock() {
        junit.readLock();
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#readUnlock()
     */
    public void readUnlock() {
        junit.readUnlock();
    }

    /**
     * Read the underlying java model and rebuild ourselves if it had changed or we are new.
     * @see org.netbeans.modules.visualweb.insync.Unit#sync()
     */
    public boolean sync() {
        boolean synced = syncSubUnits();
        //if (!synced && javaClass != null)
        //    return false;  // do nothing if read was a no-op and we are already inited
        if ((syncedSinceConstructed && !synced) || getState().isBusted())
            return false;
        error = null;
        scan();
        if (getState().isBusted())
            return false;
        bind();
        syncedSinceConstructed = true;
        return true;
    }
    
    /**
     * Since a unit can be composed of other units, this is where a subclass should add code
     * to sync there sub units.  In order for my sync to proceed, I must ensure that my sub
     * units are synced successfully.  Once they are sync'ed, I can go ahead and sync myself.
     * This method provides the capability of a subclass to add on additional sub-units and have
     * them synced when I am asked to sync.
     * NOTE: Make sure that if ANY of the child units did a sync, this method must return true
     * as I must assume something has changed in that case and continue the sync process.
     * @return
     */
    protected boolean syncSubUnits() {
        return junit.sync();
    }

    /**
     * Scan our underlying document to find or create our tracked items
     */
    protected void scan() {
        // If the JavaUnit does not have a class defined in it, then I will re-use the same scanner
        // so that a similar class is re-created.
        javaClass = getJavaUnit().getJavaClass();
        if (javaClass != null) {
            for(int i=0; i<FacesModel.managedBeanNames.length; i++) {
                if(javaClass.isSubTypeOf(FacesModel.managedBeanNames[i])) {
                    isPageBean = FacesModel.managedBeanIsPage[i];
                    baseClassName = FacesModel.managedBeanNames[i];
                    break;
                }
            }
            
            beanStructureScanner = getNewBeanStructureScanner();
        }
        // Handle the case where the JavaUnit never had a public class in it to begin with
        if (beanStructureScanner == null) {
            error = new ParserAnnotation("Must have a class defined.", getJavaUnit().getFileObject(), 1, 1);
            // TODO Should most likely add state to BeansUnit, but since error is source related we should be ok
            getJavaUnit().setBusted();
            return;
        }
        beanStructureScanner.scan();
    }

    /**
     * Return my currently active scanner, if a sync has been performed or is not in progress
     * then the result will be null.
     */
    public BeanStructureScanner getBeanStructureScanner() {
        return beanStructureScanner;
    }
    
    /**
     * Bind beans & their properties, events and parents
     */
    protected void bind() {
        bindBeans();
        List<Statement> stmts = getPropertiesInitStatements();
        bindProperties(stmts);
        bindEventSets(stmts);
        bindBeanParents();
    }

    //---------------------------------------------------------------------------------- Unit Output

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#writeLock(org.netbeans.modules.visualweb.insync.UndoEvent)
     */
    public void writeLock(UndoEvent event) {
        junit.writeLock(event);
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#writeUnlock(org.netbeans.modules.visualweb.insync.UndoEvent)
     */
    public boolean writeUnlock(UndoEvent event) {
        return junit.writeUnlock(event);
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#isWriteLocked()
     */
    public boolean isWriteLocked() {
        return junit.isWriteLocked();
    }

    //----------------------------------------------------------------------------------------- Guts

    /**
     * @param bean
     * @param type
     * @return
     */
    protected Bean firstBeanOfType(Bean bean, Class type) {
        if (type.isAssignableFrom(bean.getBeanInfo().getBeanDescriptor().getBeanClass()))
            return bean;
        Bean[] kids = bean.getChildren();
        return kids != null ? firstBeanOfType(kids, type) : null;
    }

    /**
     * @param beans
     * @param type
     * @return
     */
    protected Bean firstBeanOfType(Bean[] beans, Class type) {
        for (int i = 0; i < beans.length; i++) {
            Bean kb = firstBeanOfType(beans[i], type);
            if (kb != null)
                return kb;
        }
        return null;
    }

    /**
     * Update the names counter for a given type to be at least as large as a specific ordinal value
     */
    protected void updateName(String type, int index) {
        //Use short name for type to avoid name collision
        int dot = type.lastIndexOf('.');
        if (dot >= 0)
            type = type.substring(dot+1);
        Integer counter = (Integer)nameCounters.get(type);
        if (counter == null || counter.intValue() < index) {
            nameCounters.put(type, new Integer(index));
            assert Trace.trace("insync.beans", "BU.updateName type:" + type + " index:" + index);  //NOI18N
        }
    }

    /**
     * Examine a given type and name to adjust the max ordinal used.
     */
    protected void scanName(String type, String name) {
        String baseName = Naming.varName(type);
        if (name.startsWith(baseName)) {
            int tailPos = baseName.length();
            String tail = name.substring(tailPos);
            try {
                int index = Integer.parseInt(tail);
                updateName(type, index);
            }
            catch (NumberFormatException e) {
                // ignore--not using a generated index-based name
            }
        }
    }

    /**
     * Retrieve the next generated name for a type.
     * @param type The type name of type to gen name for
     * @return The next generated name for the type.
     */
    protected String nextNameForType(String type) {
        //Use short name for type to avoid name collision
        int dot = type.lastIndexOf('.');
        if (dot >= 0)
            type = type.substring(dot+1);
        Integer counter = (Integer)nameCounters.get(type);
        int index = counter == null ? 1 : counter.intValue() + 1;
        String name = null;
        do {
            name = Naming.varName(type) + Integer.toString(index);
            if (javaClass.getField(name) == null) {
                nameCounters.put(type, new Integer(index));
                assert Trace.trace("insync.beans", "BU.nextNameForType type:" + type + //NOI18N
                        " index:" + index + " name:" + name);  //NOI18N
                return name;
            }
            index++;
        } while (index <= 9999);
        return Naming.varName(type) + "_SuffixOverflow";  //NOI18N;
    }

    private boolean isBeanNameAvailable(Bean[] beans, String name, Bean bean) {
        for (int i = 0; i < beans.length; i++)
            if (name.equals(beans[i].getName()) && beans[i] != bean)
                return false;
        return true;
    }

    /**
     * @param name
     * @return whether or not a given name is available for use as a bean name
     */
    protected boolean isBeanNameAvailable(String name, Bean bean) {
        return isBeanNameAvailable(getBeans(), name, bean);
    }

    /**
     * Retrieve the next available name given a base
     * @param base The base name to use.
     * @param bean The bean that wants the name--used to ignore for autonumbering
     * @param alwaysNumber true to always include a number suffix, false to first attempt to use base
     * @return The next available name from base, returns base itself if no collisions
     */
    protected String nextAvailableName(String base, Bean bean, boolean alwaysNumber) {
        int suffix = 1;
        String name = alwaysNumber ? base + Integer.toString(suffix++) : base;
        base = Naming.getBaseName(base);
        Bean[] beans = getBeans();
        do {
            if (isBeanNameAvailable(beans, name, bean) && (javaClass.getField(name) == null))
                return name;
            name = base + Integer.toString(suffix++);
        }
        while (suffix <= 9999);
        return base + "_SuffixOverflow";  //NOI18N
    }

    /**
     * Return a new Bean instance bound to an existing field, getter & setter
     */
    protected Bean newBoundBean(BeanInfo beanInfo, String name, List<String> typeNames) {
         return new Bean(this, beanInfo, name, typeNames);
    }

    /**
     *
     */
    public boolean canCreateBean(BeanInfo bi, Bean parent) {
        // can't create any parented beans
        return parent == null && bi != null;
    }

    /**
     * Return a brand new created Bean instance
     * @param pos TODO
     */
    protected Bean newCreatedBean(BeanInfo beanInfo, Bean parent, String name, String facet, Position pos) {
         Bean b = new Bean(this, beanInfo, name);
         if (b != null) {
             int index = pos != null ? pos.getIndex() : -1;
             Bean after = (index > 0 && index <= beans.size()) ? (Bean)beans.get(index-1) : null;
             b.insertEntry(after);
             if (parent != null) {        // add this child to parent
                 beans.add(b);            // add at end, this pos doesn't matter
                 parent.addChild(b, pos);
             }
             else {
                 if (index > 0 && index <= beans.size())
                     beans.add(index, b);
                 else
                     beans.add(b);
             }
         }
         return b;
    }

    /**
     * Scan all the fields of the host class and attempt to bind each to a new bean, updating our
     * beans list as we go.
     * Run a second parent-child wiring pass
     */
    protected void bindBeans() {
        beans.clear();
        HashMap<String, List<String>> props = javaClass.getPropertiesNameAndTypes();
        for(String key : props.keySet()) {
            Bean bean = bindBean(key, props.get(key));
            if(bean != null) {
                beans.add(bean);
            }
        }
   }

    /**
     * Run a pass over the beans to see if they need to do parent-child wiring
     */
    protected void bindBeanParents() {
        for (Iterator i = beans.iterator(); i.hasNext(); ) {
            Bean b = (Bean)i.next();
            Bean parent = b.bindParent();
            if (parent != null)
                parent.addChild(b, null);
        }
    }
    
    protected Bean bindBean(String name, List<String> typeNames) {
        // Scan all type/name pairs for later name generation
        scanName(typeNames.get(0), name);
        // make sure we can obtain the bean's beaninfo
        BeanInfo bi = getBeanInfo(typeNames.get(0));
        if (bi == null) {
            return null;
        }
        return newBoundBean(bi, name, typeNames.subList(1, typeNames.size()));
    }
    
    /**
     * @param s
     * @return
     */
    protected Property newBoundProperty(Statement stmt) {
        return Property.newBoundInstance(this, stmt);
    }

    /**
     *
     */
    protected void bindProperties(List<Statement> stmts) {
        for(Statement stmt : stmts) {
            Property p = newBoundProperty(stmt);
            if (p != null) {
                Bean b = p.bean;
                b.properties.add(p);
            } else {
                assert Trace.trace("insync.beans", "BU.bindProperties: Stmnt was NOT a property setter:" + stmt);  //NOI18N
            }
        }
    }
    
    /**
     * Overridable hook to allow subclasses an opportunity to create different kinds of bound event
     * sets.
     *
     * @param s
     * @return
     */
    protected EventSet newBoundEventSet(Statement stmt) {
        return EventSet.newBoundInstance(this, stmt);
    }

    /**
     * Scan the init block statements and create matching EventSets and register with their beans
     */
    protected void bindEventSets(List<Statement> stmts) {
        for(Statement stmt : stmts) {
            EventSet es = newBoundEventSet(stmt);
            if (es != null) {
                es.bean.eventSets.add(es);
            } else {
                assert Trace.trace("insync.beans", "BU.bindEventSets: Stmnt was NOT an event adder:" + stmt); //NOI18N
            }        
        }
    }    

    //------------------------------------------------------------------------------------ Accessors

    /**
     *
     */
    /**
     * @return
     */
    public String getThisPackageName() {
        return packageName;  // same as: junit.getPackage().getName()
    }

    /**
     *
     */
    /**
     * @return
     */
    public String getThisClassName() {
        if (javaClass == null)
            return null;
        return javaClass.getShortName();
    }

    /**
     *
     */
    /**
     * @return
     */
    public String getBeanName() {
        return getThisClassName();
    }

    /**
     *
     */
    /**
     * @return
     */
    public JavaClass getThisClass() {
        return javaClass;
    }

    /**
     * To get the base bean class
     */
    /**
     * @return the base bean class if the bean is a known managed bean
     */
    public Class getBaseBeanClass() {
        try {
            return ClassUtil.getClass(getBaseBeanClassName(), classLoader);
        }catch(ClassNotFoundException e) {
        }
        return null;
    }    
    
    /**
     * @return the base bean class name
     */    
    public String getBaseBeanClassName() {
        return baseClassName;
    }
    
    
    /**
     * @return
     */
    public List<Statement>  getPropertiesInitStatements() {
        return getBeanStructureScanner().getPropertiesInitStatements();
    }

    /**
     * @return
     */
    public Method getPropertiesInitMethod() {
        return getBeanStructureScanner().getPropertiesInitMethod();
    }
    
    /**
     * @return
     */
    public Method getCleanupMethod() {
        return getBeanStructureScanner().getDestroyMethod();
    }

    /**
     *
     */
    /**
     * @return
     */
    public JavaUnit getJavaUnit() {
        return junit;
    }

    /**
     *
     */
    /**
     * @return
     */
    public Bean[] getBeans() {
        return (Bean[])beans.toArray(Bean.EMPTY_ARRAY);
    }

    /**
     * Get the root DOM element for the bean tree if applicable
     */
    /**
     * @return
     */
    public org.w3c.dom.Element getRootElement() {
        return null;
    }

    /**
     * Get the live instance (if any) for the root live container
     * @return the live root instance
     */
    public Object getRootInstance() {
        return null;
    }

    /**
     *
     */
    /**
     * @param name
     * @return
     */
    public Bean getBean(String name) {
        for (Iterator i = beans.iterator(); i.hasNext(); ) {
            Bean c = (Bean)i.next();
            if (c.getName().equals(name))
                return c;
        }
        return null;
    }

    /**
     * Use the junit method to enure that we have an import for this type so that 
     * the identifier can use its short form. By default explicit imports are ensured
     * 
     * @param type  fully-qualified type name 
     */
    public void ensureImportForType(String type) {
        getBeanStructureScanner().ensureImportForType(type);
    }

    /**
     * Create and add a new bean to an existing parent.
     *
     * @param beanInfo  The definition of the bean to create
     * @param parent  The parent for the new bean
     * @param name  The instance name for the new bean
     * @param facet  The (optional) facet to tag this child within the parent
     * @return The newly created bean
     */
    public final Bean addBean(BeanInfo beanInfo, Bean parent, String name, String facet, Position pos) {
        String type = beanInfo.getBeanDescriptor().getBeanClass().getName();
        name = name != null ? nextAvailableName(name, null, true) : nextNameForType(type);

        Bean b = newCreatedBean(beanInfo, parent, name, facet, pos);

        // ensure a package import for the bean's type if it has a Java source entry (is renamable)
        //if (b.canSetName())
        //    ensureImportForType(type);

        return b;
    }

    /**
     *
     */
    /**
     * @param bean
     * @param newparent
     * @param pos
     */
    public void moveBean(Bean bean, Bean newparent, Position pos) {
        Bean oldparent = bean.getParent();
        oldparent.removeChild(bean);
        newparent.addChild(bean, pos);
    }

    /**
     *
     */
    /**
     * @param bean
     */
    public final void removeBean(Bean bean) {
        beans.remove(bean);  // remove from unit list
        Bean parent = bean.getParent();
        if (parent != null)
            parent.removeChild(bean);  // remove from parent list
        bean.removeEntry();  // tell bean to remove all of its source entries
    }

    /**
     * Ensures that a cross-reference accessor to a sibling bean is in place. Accessor method is of
     * the form:
     *      public <type> get<Mname>() {
     *          return (<type>) getBean("<bname>");
     *      }
     *
     * @param bname
     * @param type
     */
    public void addXRefAccessor(String bname, String type) {
        getBeanStructureScanner().addXRefAccessor(bname, type);
    }

    //-------------------------------------------------------------------------------- Event Methods

    /**
     * Find an (event) method with a given name
     *
     * @param name
     * @return
     */
    public Method getEventMethod(String name, MethodDescriptor md) {
        Class[] pts = md.getMethod().getParameterTypes();
        assert Trace.trace("insync.beans", "BU.getEventMethod name:" + name + " null");  //NOI18N
        return javaClass.getMethod(name, pts);
    }

    /**
     * Find the initializer method for the page
     *
     * @param name
     * @return
     */
    public Method getInitializerMethod() {
        return javaClass.getMethod("init", new Class[0]);
    }
    
    /**
     * Add an event method with a given name and event type, and return type
     *
     * @param md
     * @param name
     * @return
     */
    public boolean hasEventMethod(MethodDescriptor md, String name) {
        Class[] pts = md.getMethod().getParameterTypes();
        return javaClass.getMethod(name, pts) != null;
    }

    /**
     * Add as needed an event method with a given name and event type, and return type. Do nothing
     * if the method is already present.
     *
     * @param md  The MethodDescriptor that identifies the method signature+return.
     * @param name  The name of the metod to find or create.
     * @return The existing or newly created method.
     */
    public EventMethod ensureEventMethod(MethodDescriptor md, String name, String defaultBody, 
             String[] parameterNames, String[] requiredImports) {
        return getBeanStructureScanner().ensureEventMethod(md, name, defaultBody, 
                parameterNames, requiredImports);
    }

    //-------------------------------------------------------------------------------------- Utility

    /**
     * Instantiate a bean (or any object really) given its Class. Handles primitives and arrays by
     * creating default values for them.
     *
     * @param cls  Class to instantiate.
     * @return  The new instance of the given Class.
     */
    public Object instantiateBean(Class cls) {
        // intercept Basic and Primitive types & just hand-construct them
        // Also, intercept classes that lack a null constructor
        if (cls == Boolean.TYPE || cls == Boolean.class)
            return Boolean.FALSE;
        else if (cls == Byte.TYPE || cls == Byte.class)
            return new Byte((byte)0);
        else if (cls == Character.TYPE || cls == Character.class)
            return new Character('\000');
        else if (cls == Double.TYPE || cls == Double.class)
            return new Double(0);
        else if (cls == Float.TYPE || cls == Float.class)
            return new Float(0);
        else if (cls == Integer.TYPE || cls == Integer.class)
            return new Integer(0);
        else if (cls == Long.TYPE || cls == Long.class)
            return new Long(0);
        else if (cls == Short.TYPE || cls == Short.class)
            return new Short((short)0);
        else if (cls == BigDecimal.class)
            return new BigDecimal("0");

        try {        
            if(cls.isArray()) {
                return Array.newInstance(cls.getComponentType(), 0);
            }else {
                if(!cls.isInterface() && !Modifier.isAbstract(cls.getModifiers())) {
                    return cls.newInstance();
                }                
            }
        } catch (Exception e) {
            // EAT: TODO XXX
            // Look into what we can do to handle beans that do not have a default constructor ?
            // Is there something in the Beans spec that could help ?
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return null;
    }

    /**
     * Get the actual class of a bean or supporting object, given its type. The type may be a
     * primitive and/or it may be an array.
     *
     * @param type  The type retrieve the Class for.
     * @return The Class that represents the given type.
     * @throws ClassNotFoundException
     */
    public Class getBeanClass(String type) throws ClassNotFoundException {
        return ClassUtil.getClass(type, classLoader);
    }

    /**
     * Get the BeanInfo given a class for a bean.
     *
     * @param cls  The Class of the bean.
     * @return The BeanInfo.
     */
    public static BeanInfo getBeanInfo(Class cls, ClassLoader classLoader) {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
            Thread.currentThread().setContextClassLoader(classLoader);
            try {
                if (cls == null)
                    return null;
                return Introspector.getBeanInfo(cls, Introspector.USE_ALL_BEANINFO);
            } catch (Exception e) {
                e.printStackTrace();
                assert Trace.trace("insync.beans", "Caught " + e + " in BU.getBeanInfo for class:" + cls.getName());  //NOI18N
            } catch (NoClassDefFoundError e) {
                throw e;
            }
            return null;
        } finally {
    		Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    	}
    }

    /**
     * Get the BeanInfo for a bean by type using this unit's class loader.
     *
     * @param type  The fully-qualified bean type name
     * @return The BeanInfo, or null if not found.
     */
    public BeanInfo getBeanInfo(String type) {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
            Thread.currentThread().setContextClassLoader(getClassLoader());
            try {
                return getBeanInfo(getBeanClass(type), getClassLoader());
            }
            catch (Exception e) {
                //System.err.println("Caught " + e + " in BU.getBeanInfo for type:" + type);  //NOI18N
                assert Trace.trace("insync.beans", "Caught " + e + " in BU.getBeanInfo for type:" + type);  //NOI18N
            }
            return null;
        } finally {
    		Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    	}
    }

    //--------------------------------------------------------------------------------------- Output

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#dumpTo(java.io.PrintWriter)
     */
    public void dumpTo(PrintWriter w) {
        w.println("BeansUnit pkg:" + packageName + " name:" + junit.getName() + " beans:");  //NOI18N
        Bean[] beans = getBeans();
        for (int bi = 0; bi < beans.length; bi++) {
            w.println("  Bean: " + beans[bi].getName());  //NOI18N
            Property[] props = beans[bi].getProperties();
            for (int pi = 0; pi < props.length; pi++)
                w.println("    PropSetting name:" + props[pi].getName() + " valSrc:" +   //NOI18N
                          props[pi].getValueSource());
        }
    }

    /*
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(30);
        sb.append("[BeansUnit pkg:" + getThisPackageName() + " cls:" + getThisClassName() +   //NOI18N
                  " name:" + junit.getName() + " beans:");  //NOI18N
        Bean[] beans = getBeans();
        for (int i = 0; i < beans.length; i++)
            sb.append(beans[i].toString());
        sb.append("]");  //NOI18N
        return sb.toString();
    }

    public Model getModel() {
        return model;
    }
    
    /**
     * Must guarantee to never return null;
     * 
     * @return
     */
    protected BeanStructureScanner getNewBeanStructureScanner() {
        return new BeanStructureScanner(this);
    }

}
