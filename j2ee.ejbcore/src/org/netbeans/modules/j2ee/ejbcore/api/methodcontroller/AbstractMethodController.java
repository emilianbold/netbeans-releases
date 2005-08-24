/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ejbcore.api.methodcontroller;
import java.rmi.RemoteException;
import java.util.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.MultipartId;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.TypeReference;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.javacore.internalapi.JavaModelUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public abstract class AbstractMethodController extends EjbMethodController {
    private EntityAndSession model;
    private ClassPath cp;
    protected Set classesForSave;
    private int transactionLevel = 0;
    private boolean writeTransactionRollBack = false;

    public AbstractMethodController(EntityAndSession model, ClassPath cp) {
        this.model = model;
        this.cp = cp;
    }

    public interface GenerateFromImpl {
        void getInterfaceMethodFromImpl(MethodType t, JavaClass home, JavaClass component);
        JavaClass getDestinationInterface();
        Method getInterfaceMethod();
    }

    public interface GenerateFromIntf {
        void getInterfaceMethodFromImpl(MethodType t);
        Method getImplMethod();
        Method getSecondaryMethod();
    }

    public abstract GenerateFromImpl createGenerateFromImpl();
    public abstract GenerateFromIntf createGenerateFromIntf();

    public final void createAndAdd(Method clientView, boolean local, boolean isComponent) {
        JavaClass home = null;
        JavaClass component = null;
        boolean rollback = true;
        beginWriteJmiTransaction();
        try {
            if (local) {
                home = JMIUtils.findClass(model.getLocalHome(), cp);
                component = businessInterface(model.getLocal());
            } else {
                home = JMIUtils.findClass(model.getHome(), cp);
                component = businessInterface(model.getRemote());
            }
            if (isComponent) {
                registerClassForSave(component);
                TypeReference typeReference = JavaModelUtil.resolveImportsForType(component, clientView.getType());
                clientView.setTypeName(typeReference);
                component.getContents().add(clientView);
            } else {
                registerClassForSave(home);
                TypeReference typeReference = JavaModelUtil.resolveImportsForType(home, clientView.getType());
                clientView.setTypeName(typeReference);
                home.getContents().add(clientView);
            }
            JavaClass bc = JMIUtils.findClass(model.getEjbClass(), cp);
            if (hasJavaImplementation(clientView)) {
                List implMethods = getImplementationMethods(clientView);
                Iterator it = implMethods.iterator();
                while (it.hasNext()) {
                    Method me = (Method) it.next();
                    if (JMIUtils.findInClass(me,bc) == null) {
                        registerClassForSave(bc);
                        TypeReference typeReference = JavaModelUtil.resolveImportsForType(bc, me.getType());
                        me.setTypeName(typeReference);
                        bc.getContents().add(me);
                    }
                }
            }
            if (!local) {
                addExceptionIfNecessary(clientView, RemoteException.class.getName());
            }
            rollback = false;
        } finally {
            endWriteJmiTransaction(rollback);
        }
    }

    public final void createAndAddInterface(Method beanImpl, boolean local) {
        beginWriteJmiTransaction();
        boolean rollback = false;
        try {
            MethodType t = getMethodTypeFromImpl(beanImpl);
            GenerateFromImpl v = createGenerateFromImpl();
            JavaClass home = null;
            JavaClass component = null;
            if (local) {
                home = JMIUtils.findClass(model.getLocalHome(), cp);
                component = businessInterface(model.getLocal());
            } else {
                home = JMIUtils.findClass(model.getHome(), cp);
                component = businessInterface(model.getRemote());
            }
            registerClassForSave(home);
            registerClassForSave(component);
            v.getInterfaceMethodFromImpl(t,home,component);
            Method me = v.getInterfaceMethod();
            if (!local) {
                addExceptionIfNecessary(me, RemoteException.class.getName());
            }
            me.setModifiers(0);
            JavaClass destinationInterface = v.getDestinationInterface();
            registerClassForSave(destinationInterface);
            destinationInterface.getContents().add(me);
            rollback = false;
        } finally {
            endWriteJmiTransaction(rollback);
        }
    }

    public final void createAndAddImpl(Method intfView) {
        beginWriteJmiTransaction();
        boolean rollback = true;
        try {
            MethodType t = getMethodTypeFromInterface(intfView);
            GenerateFromIntf v = createGenerateFromIntf();
            v.getInterfaceMethodFromImpl(t);
            Method bcm = v.getImplMethod();
            JavaClass bc = JMIUtils.findClass(model.getEjbClass(), cp);
            registerClassForSave(bc);
            bc.getContents().add(bcm);
            rollback = false;
        } finally {
            endWriteJmiTransaction(rollback);
        }
    }

    private List getImplementationMethods(Method intfView) {
        MethodType t = getMethodTypeFromInterface(intfView);
        GenerateFromIntf v = createGenerateFromIntf();
        v.getInterfaceMethodFromImpl(t);
        Method primary = v.getImplMethod();
        Method secondary = v.getSecondaryMethod();
        List rv = null;
        if (secondary != null) {
            rv = Arrays.asList(new Method[] {primary,secondary});
        } else {
            rv = Collections.singletonList(primary);
        }
        return rv;
    }

    public final List getImplementation(Method intfView) {
        List methods = getImplementationMethods(intfView);
        List l = new ArrayList(methods.size());
        Iterator mIt = methods.iterator();
        while (mIt.hasNext()) {
            Method m = (Method) mIt.next();
            l.add(JMIUtils.findInClass(m, getBeanClass()));
        }
       return l;
    }

    public final Method getInterface(Method beanImpl, boolean local) {
        MethodType t = getMethodTypeFromImpl(beanImpl);
        assert t != null: "method cannot be used in interface";
        GenerateFromImpl v = createGenerateFromImpl();
        JavaClass home = null;
        JavaClass component = null;
        if (local) {
            home = JMIUtils.findClass(model.getLocalHome(), cp);
            component = businessInterface(model.getLocal());
        } else {
            home = JMIUtils.findClass(model.getHome(), cp);
            component = businessInterface(model.getRemote());
        }
        v.getInterfaceMethodFromImpl(t,home,component);
        return JMIUtils.findInClass(v.getInterfaceMethod(), v.getDestinationInterface());
    }


    /** Performs the check if the method is defined in apporpriate interface
     * @return false if the interface is found but does not contain matching method.
     */
    public boolean hasMethodInInterface(Method m, MethodType methodType, boolean local) {
        boolean result = false;
        JavaClass intf = null;
        String name = null;
        org.netbeans.jmi.javamodel.Type retT = null;
        // return type
        if (methodType instanceof MethodType.BusinessMethodType) {
            name = m.getName();
            retT = m.getType();
            intf = findBusinessInterface(local? model.getLocal(): model.getRemote());
        }
        else if (methodType instanceof MethodType.CreateMethodType) {
            name = chopAndUpper(m.getName(),"ejb"); //NOI18N
            retT = findJavaClass(local? model.getLocal(): model.getRemote());
            intf = findJavaClass(local? model.getLocalHome(): model.getHome());
        }
        if (name == null || intf == null || retT == null) {
            return true;
        }
        Iterator/*<Feature>*/ features = intf.getFeatures().iterator();
        while (features.hasNext()) {
            Feature f = (Feature)features.next();
            if (f instanceof Method) {
                Method method = (Method)f;
                if (name.equals(method.getName())
                    && retT.getName().equals(method.getType().getName())) {
                    // XXX check also parameters
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /** Find a @link{JavaClass} for given classname.
     */
    private JavaClass findJavaClass (String clzName) {
        FileObject clzFo = cp.findResource(clzName.replace('.', '/')+".java");
        Resource res = (clzFo != null)? JavaModel.getResource(clzFo): null;
        if (res != null) {
            List classes = res.getClassifiers();
            assert classes.size() == 1: "" + res + " should contain just one class";
            JavaClass beani = (JavaClass)classes.get(0);
            return beani;
        }
        return null;
    }

    private String chopAndUpper(String fullName, String chop) {
         StringBuffer sb = new StringBuffer(fullName);
         sb.delete(0, chop.length());
         sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
         return sb.toString();
    }

    private void addExceptionIfNecessary(Method me, String exceptionName) {
        // TODO: test this!
        JavaModelPackage javaModelPackage = (JavaModelPackage) me.refImmediatePackage();
        MultipartId exceptionMultipartId = javaModelPackage.getMultipartId().createMultipartId(exceptionName, null, null);

        if (!me.getExceptionNames().contains(exceptionMultipartId)) {
            JMIUtils.addException(me, exceptionName);
        }
    }

    private JavaClass businessInterface(String compInterfaceName) {
        JavaClass compInterface = JMIUtils.findClass(compInterfaceName, cp);
        JavaClass beanClass = JMIUtils.findClass(model.getEjbClass(), cp);
        if (compInterface == null || beanClass == null) {
            return null;
        }
        // get bean interfaces
        List beanInterfaces = buildInterfaces(beanClass.getInterfaces());

        // get method interfaces
        List compInterfaces = buildInterfaces(compInterface.getInterfaces());

        // look for common candidates
        compInterfaces.retainAll(beanInterfaces);

        if (compInterfaces.isEmpty()) {
            return compInterface;
        }

        JavaClass business = JMIUtils.findClass(compInterfaces.get(0).toString(), cp);
        return business == null ? compInterface : business;
    }

    private JavaClass findBusinessInterface(String compInterfaceName) {
        JavaClass compInterface = findJavaClass(compInterfaceName);
        JavaClass beanClass = findJavaClass(model.getEjbClass());
        if (compInterface == null || beanClass == null) {
            return null;
        }
        // get bean interfaces
        List/*<JavaClass>*/ beanInterfaces = beanClass.getInterfaces();
//        System.out.println("beanInterfaces "+beanInterfaces+", "+beanInterfaces.size()+", "+beanClass.getInterfaces().size());

        // get method interfaces
        List/*<JavaClass>*/ compInterfaces = compInterface.getInterfaces();
//        System.out.println("compInterfaces "+compInterfaces+", "+compInterfaces.size()+", "+compInterface.getInterfaces().size());

        // look for common candidates
        JavaClass business = null;
        for (int i=0; i<compInterfaces.size(); i++) {
            Object o = compInterfaces.get(i);
            if (beanInterfaces.contains(o)) {
                business = (JavaClass)o;
                break;
            }
        }

        return business == null ? compInterface : business;
    }

    private List buildInterfaces(List/*<JavaClass>*/ interfaces) {
        JavaClass[] interfacesArray = (JavaClass[]) interfaces.toArray(new JavaClass[interfaces.size()]);
        List l = new java.util.ArrayList(interfacesArray.length);
        for (int i = 0 ; i < interfacesArray.length; i++) {
            l.add(interfacesArray[i].getName());
        }
        return l;
    }

    public final JavaClass getBeanClass() {
        return model.getEjbClass() == null ? null : JMIUtils.findClass(model.getEjbClass(), cp);
    }
    
    public final Collection getLocalInterfaces() {
        if (!hasLocal()) {
            return Collections.EMPTY_LIST;
        }
        List l = new ArrayList(2);
        if (model.getLocalHome() != null) {
            l.add(JMIUtils.findClass(model.getLocalHome(), cp));
        }
        if (model.getLocal() != null) {
            l.add(businessInterface(model.getLocal()));
        }
        
        return l;
    }
    
    public final Collection getRemoteInterfaces() {
        if (!hasRemote()) {
            return Collections.EMPTY_LIST;
        }
        List l = new ArrayList(2);
        if (model.getHome() != null) {
            l.add(JMIUtils.findClass(model.getHome(), cp));
        }
        if (model.getRemote() != null) {
            l.add(businessInterface(model.getRemote()));
        }
        
        return l;
    }
    
    public final void delete(Method interfaceMethod, boolean local) {
        Collection impls = getImplementation(interfaceMethod);
        boolean checkOther = local?hasRemote():hasLocal();
        if (!impls.isEmpty()) {
            Iterator implIt = impls.iterator();
            while (implIt.hasNext()) {
                Method impl = (Method) implIt.next();
                if (impl != null) { // could be null here if the method is missing
                    if (((checkOther && getInterface(impl, !local) == null)) ||
                            !checkOther) {
                        impl.getDeclaringClass().getContents().remove(impl);
                    }
                }
            }
            interfaceMethod.getDeclaringClass().getContents().remove(interfaceMethod);
        }
    }
    
    public boolean hasRemote() {
        String intf = model.getHome();
        if (intf == null || findJavaClass(intf) == null) {
            return false;
        }
        intf = model.getRemote();
        if (intf == null || findBusinessInterface(intf) == null) {
            return false;
        }
        return true;
    }
    
    public boolean hasLocal() {
        String intf = model.getLocalHome();
        if (intf == null || findJavaClass(intf) == null) {
            return false;
        }
        intf = model.getLocal();
        if (intf == null || findBusinessInterface(intf) == null) {
            return false;
        }
        return true;
    }

    public Method getPrimaryImplementation(Method intfView) {
        List impls = getImplementation(intfView);
        return impls.isEmpty()?null:(Method)impls.get(0);
    }

    public String getRemote() {
        return model.getRemote();
    }

    public String getLocal() {
        return model.getLocal();
    }

    public final void addMethod(Method method, boolean local, boolean isComponent) {
        beginWriteJmiTransaction();
        boolean rollback = true;
        try {
            method = JMIUtils.duplicate(method);
            JavaClass javaClass = getBeanInterface(local, isComponent);
            assert javaClass != null;
            registerClassForSave(javaClass);
            javaClass.getContents().add(method);
            if (!local) {
                addExceptionIfNecessary(method, RemoteException.class.getName());
            }
            createBeanMethod(method);
            rollback = false;
        } finally {
            endWriteJmiTransaction(rollback);
        }
    }

    public JavaClass getBeanInterface(boolean local, boolean isComponent) {
        if (isComponent) {
            return businessInterface(local ? model.getLocal() : model.getRemote());
        } else {
            String className = local ? model.getLocalHome() : model.getHome();
            return JMIUtils.findClass(className, cp);
        }
    }

    private void createBeanMethod(Method method) {
        beginWriteJmiTransaction();
        boolean rollback = true;
        try {
            JavaClass beanClass = JMIUtils.findClass(model.getEjbClass(), cp);
            if (hasJavaImplementation(method)) {
                List implMethods = getImplementationMethods(method);
                Iterator it = implMethods.iterator();
                while (it.hasNext()) {
                    Method me = (Method) it.next();
                    if (JMIUtils.findInClass(me, beanClass) == null) {
                        beanClass.getContents().add(me);
                    }
                }
            }
            rollback = true;
        } finally {
            endWriteJmiTransaction(rollback);
        }
    }

    public final void removeMethod(Method method, boolean local, boolean isComponent) {
        beginWriteJmiTransaction();
        boolean rollback = true;
        try {
            JavaClass javaClass = getBeanInterface(local, isComponent);
            assert javaClass != null;
            registerClassForSave(javaClass);
            if (!local) {
                addExceptionIfNecessary(method, RemoteException.class.getName());
            }
            javaClass.getContents().remove(method);
            createBeanMethod(method);
            rollback = false;
        } finally {
            endWriteJmiTransaction(rollback);
        }
    }

    public final void updateMethod(Method method, boolean local, boolean isComponent, boolean shouldExist) {
        beginWriteJmiTransaction();
        boolean rollback = true;
        try {
            JavaClass javaClass = getBeanInterface(local, isComponent);
            assert javaClass != null;
            registerClassForSave(javaClass);
            Method m = JMIUtils.findInClass(method, javaClass);
            final List contents = javaClass.getContents();
            if (shouldExist) {
                if (m == null) {
                    if (!local) {
                        addExceptionIfNecessary(method, RemoteException.class.getName());
                    }
                    contents.add(method);
                }
            } else {
                if (m != null) {
                    contents.remove(m);
                }
            }
            rollback = false;
        } finally {
            endWriteJmiTransaction(rollback);
        }
    }

    public void beginWriteJmiTransaction() {
        if (transactionLevel++ == 0) {
            writeTransactionRollBack = false;
            classesForSave = new HashSet();
            JMIUtils.beginJmiTransaction(true);
        }
    }

    public void endWriteJmiTransaction(boolean rollback) {
        writeTransactionRollBack = writeTransactionRollBack || rollback;
        if (--transactionLevel == 0) {
            JMIUtils.endJmiTransaction(writeTransactionRollBack);
            if (!writeTransactionRollBack) {
                saveModifiedClasses();
            }
        }
    }

    public void registerClassForSave(JavaClass javaClass) {
        if (transactionLevel > 0 && javaClass != null && !Utils.isModified(javaClass)) {
            classesForSave.add(javaClass);
        }
    }

    private void saveModifiedClasses() {
        for (Iterator it = classesForSave.iterator(); it.hasNext();) {
            JavaClass javaClass = (JavaClass) it.next();
            if (Utils.isModified(javaClass)) {
                Utils.save(javaClass);
            }

        }
    }

}