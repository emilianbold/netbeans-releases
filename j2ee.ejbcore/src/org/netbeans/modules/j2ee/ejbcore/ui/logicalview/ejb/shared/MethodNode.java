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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import java.awt.Image;
import org.openide.nodes.Node;
import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.util.actions.SystemAction;
import org.openide.util.Utilities;
import java.util.Collection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

// TODO: RETOUCHE listening on sources
public class MethodNode extends AbstractNode implements /*MDRChangeListener,*/ OpenCookie {

    private final JavaSource javaSource;
    private final String implBean;
    private final FileObject implBeanFO;
    private final MethodModel method;
    private final ComponentMethodViewStrategy cmvs;
    private final Collection interfaces;
    
    public MethodNode(JavaSource javaSource, MethodModel method, String implBean, Collection interfaces, ComponentMethodViewStrategy cmvs) {
        this(javaSource, method, implBean, interfaces, cmvs, new InstanceContent());
    }
    
    private MethodNode(JavaSource javaSource, MethodModel method, String implBean, Collection interfaces, ComponentMethodViewStrategy cmvs, InstanceContent ic) {
        
        super(Children.LEAF, new AbstractLookup(ic));
        
        ic.add(this);
        ic.add(method);
//        disableDelegation(FilterNode.DELEGATE_DESTROY);
        this.javaSource = javaSource;
        this.method = method;
        this.implBean = implBean;
        this.interfaces = interfaces;
        this.cmvs = cmvs;
        this.implBeanFO = getFileObject(javaSource, implBean);
        
        // TODO: listeners - WeakListener was used here before change to JMI, how to use it now?
        // unregister in appropriate point or play with ActiveQueue (openide utilities)
//        ((MDRChangeSource) method).addListener(this);  
    }
    
    public Image getIcon(int type) {
        Image badge = cmvs.getBadge(method, interfaces);
        Image icon = cmvs.getIcon(method, interfaces);
        if(badge != null){
            return Utilities.mergeImages(icon, badge, 7,7);
        }
        return icon;
    }

    @Override
    public String getName() {
        return method.getName();
    }
    
    public boolean canDestroy() {
        final boolean[] result = new boolean[] { false };
        try {
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    if ("findByPrimaryKey".equals(method.getName())) { //NOI18N
                        if (isEntityBeanMethod()) {
                            result[0] = false;
                        }
                    } else if (method.getModifiers().contains(Modifier.ABSTRACT) &&
                            (isGetter(method) || isSetter(method)) && isEntityBeanMethod()) {
                        result[0] = false;
                    }
                    result[0] = true;
                }
            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return result[0];
    }
    
    public void destroy() throws IOException {
//        ((MDRChangeSource) method).removeListener(this);
        
        if (implBeanFO != null) {
            cmvs.deleteImplMethod(method, implBean, implBeanFO, interfaces);
        }
        super.destroy();
    }

//    @Override
//    public void change(MDRChangeEvent e) {
//        // TODO: listeners - filtering of events is possible. Is it needed?
//        fireIconChange();
//    }
    
    public Action[] getActions(boolean context) {
//        List l = new ArrayList(Arrays.asList(getOriginal().getActions(context)));
//        return (Action[]) l.toArray(new Action[l.size()]);
        // XXX method node actions
        return new Action[0];
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    //implementation of OpenCookie
    public void open() {
        cmvs.openMethod(method, implBean, implBeanFO, interfaces);
    }

    private boolean isEntityBeanMethod() throws IOException {
        
        final boolean[] result = new boolean[] { false };
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Elements elements = controller.getElements();
                TypeElement entityBean = elements.getTypeElement("javax.ejb.EntityBean"); // NOI18N
                TypeElement implBeanElement = elements.getTypeElement(implBean);
                result[0] = controller.getTypes().isSubtype(implBeanElement.asType(), entityBean.asType());
            }
        }, true);
        return result[0];
        
    }

    private boolean isGetter(MethodModel method) {
        boolean isVoid = "void".equals(method.getReturnType());
        if (method.getName().indexOf("get") == 0 &&
            !isVoid &&
            method.getParameters().size() == 0) {
            return true;
        }
        return false;
    }
    
    private boolean isSetter(MethodModel method) {
        boolean isVoid = "void".equals(method.getReturnType());
        if (method.getName().indexOf("set") == 0 &&
            isVoid &&
            method.getParameters().size() == 1) {
            return true;
        }
        return false;
    }

    private static FileObject getFileObject(JavaSource javaSource, final String className) {
        final FileObject[] result = new FileObject[1];
        try {
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = controller.getElements().getTypeElement(className);
                    if (typeElement != null) {
                        result[0] = SourceUtils.getFile(ElementHandle.create(typeElement), controller.getClasspathInfo());
                    }
                }
            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return result[0];
    }
    
}

