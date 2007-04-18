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

package org.netbeans.modules.compapp.javaee.sunresources.tool.cmap;

import java.util.ArrayList;

import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;

import org.netbeans.modules.compapp.javaee.sunresources.tool.annotation.EJBAnnotation;
import org.netbeans.modules.compapp.javaee.sunresources.tool.annotation.JavaEEAnnotationProcessor;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.EJBInterface.EJBInterfaceType;


/**
 * EJBNode could represent a Stateless, Stateful, or WebService node
 * 
 * @author echou
 *
 */
public class EJBNode extends CMapNode {
    
    private ArrayList<EJBInterface> implementedIntfs = 
        new ArrayList<EJBInterface> ();
    
    private boolean isWebService = false;
    
    /**
     * This constructor is invoked from EJB 2.1 style DD processing
     * 
     */
    public EJBNode() {
        super();
    }
    
    public boolean isWebService() {
        return this.isWebService;
    }
    
    public void setIsWebService(boolean isWebService) {
        this.isWebService = isWebService;
    }
    
    /** 
     * This constructor is invoked from EJB 3.0 style annotation processing
     * 
     * @param cls
     * @param type
     */
    /*
    public EJBNode(Class<?> cls, CMapNodeType type) {
        super(cls.getSimpleName(), cls.getName(), type);
        EJBInterfaceType intfType = 
            cls.isAnnotationPresent(Remote.class) ? EJBInterfaceType.REMOTE : EJBInterfaceType.LOCAL;
        ArrayList<Class> aList = EJBAnnotation.filterExcludedEJBIntf(cls.getInterfaces());
        for (int i = 0; i < aList.size(); i++) {
            implementedIntfs.add(new EJBInterface(aList.get(i).getName(), intfType));
        }
    }
     **/
    
    public EJBNode(ClassFile cls, CMapNodeType type) {
        super(cls.getName().getSimpleName(), cls.getName().getExternalName(), type);
        EJBInterfaceType intfType = 
            cls.isAnnotationPresent(ClassName.getClassName(JavaEEAnnotationProcessor.REMOTE_CLASSTYPE)) ? EJBInterfaceType.REMOTE : EJBInterfaceType.LOCAL;
        ArrayList<String> aList = EJBAnnotation.filterExcludedEJBIntf(cls.getInterfaces());
        for (int i = 0; i < aList.size(); i++) {
            implementedIntfs.add(new EJBInterface(aList.get(i), intfType));
        }
    }
    
    public ArrayList<EJBInterface> getImplementedIntfs() {
        return implementedIntfs;
    }
    
    public void addImplementedIntfs(EJBInterface intf) {
        implementedIntfs.add(intf);
    }
    
    public boolean implementsInterface(String intfName) {
        for (int i = 0; i < implementedIntfs.size(); i++) {
            EJBInterface ejbIntf = implementedIntfs.get(i);
            if (ejbIntf.getIntf().equals(intfName)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append("\tisWebService=" + isWebService + "\n"); // NOI18N
        sb.append("\timplementedIntfs=" + implementedIntfs + "\n"); // NOI18N
        return sb.toString();
    }

}
