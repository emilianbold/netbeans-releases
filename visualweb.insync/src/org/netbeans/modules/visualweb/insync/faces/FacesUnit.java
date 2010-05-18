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
package org.netbeans.modules.visualweb.insync.faces;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import com.sun.rave.designtime.Constants;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.beans.BeanStructureScanner;
import org.netbeans.modules.visualweb.insync.beans.BeansUnit;
import org.netbeans.modules.visualweb.insync.java.JavaUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.jsfsupport.container.FacesContainer;

/**
 * An extended BeansUnit that is aware of JavaServer Faces features and designtime requirements and
 * support. Provides access to the FacesContainer support provided in jsfsupport, as well as
 * handling JSF related metadata queries.
 *
 * General JSF managed beans can be designed using an instance of this class. For page backing beans
 * @see FacesPageUnit
 *
 * @author cquinn
 */
public class FacesUnit extends BeansUnit {

    // FACES_IMPLICIT_OBJECTS var definition copied from com.sun.faces.el.ValueBindingImpl
    //  Not optimal, but their var is not visible to me, so I get I clone it for now.  Will need to clean
    // up as well as update as JSF updates this list :(
    // TODO
    // Array of faces implicit objects
    protected static final String[] FACES_IMPLICIT_OBJECTS = {
        "applicationScope",
        "sessionScope",
        "requestScope",
        "facesContext",
        "cookies",
        "header",
        "headerValues",
        "initParam",
        "param",
        "paramValues",
        "view"
    };

    protected static HashMap facesImplicitNamesMap;

    static {
        facesImplicitNamesMap = new HashMap(FACES_IMPLICIT_OBJECTS.length);
        for (int i=0; i < FACES_IMPLICIT_OBJECTS.length; i++) {
            facesImplicitNamesMap.put(FACES_IMPLICIT_OBJECTS[i], "_" + FACES_IMPLICIT_OBJECTS[i]);
        }
    }

    public static boolean isImplicitBeanName(String beanName) {
        String implicitNameReplacement = (String) facesImplicitNamesMap.get(beanName);
        return implicitNameReplacement != null;
    }

    public static String fixPossiblyImplicitBeanName(String beanName) {
        String implicitNameReplacement = (String) facesImplicitNamesMap.get(beanName);
        if (implicitNameReplacement == null) {
            return beanName;
        }
        return implicitNameReplacement;
    }

    protected final FacesContainer container;
    protected final String rootPackage;

    /**
     * @param junit
     * @param cl
     * @param packageName
     * @param rootPackage
     * @param container
     */
    public FacesUnit(JavaUnit junit, ClassLoader cl, String packageName, Model model, String rootPackage,
                     FacesContainer container) {
        super(junit, cl, packageName, model);
        this.rootPackage = rootPackage;
        this.container = container;
    }

    //------------------------------------------------------------------------------------ BeansUnit

    /**
     * Return whether a bean described by a BeanInto is a faces component bean (UIComponent)
     *
     * @param beanInfo
     * @return
     */
    public static final boolean isFacesBean(BeanInfo beanInfo) {
        return UIComponent.class.isAssignableFrom(beanInfo.getBeanDescriptor().getBeanClass());
    }

    /**
     * Return whether a bean described by a BeanInto is an html bean
     *
     * @param beanInfo
     * @return
     */
    public static final boolean isHtmlBean(BeanInfo beanInfo) {
        return beanInfo.getBeanDescriptor().getBeanClass().getName().startsWith(HtmlBean.PACKAGE);
    }

    /**
     * Get a given BeanDescriptor value by name from a BeanInfo
     *
     * @param bi
     * @param name
     * @return
     */
    public static final String getBeanDescriptorValue(BeanInfo bi, String name) {
        BeanDescriptor bd = bi.getBeanDescriptor();
        if (bd != null) {
            Object value = bd.getValue(name);
            if (value instanceof String)
                return (String)value;
        }
        return null;
    }

    /**
     * Get the tag name for a given bean type
     *
     * @param beanInfo
     * @return
     */
    public static final String getBeanTagName(BeanInfo beanInfo) {
        String btn = getBeanDescriptorValue(beanInfo, Constants.BeanDescriptor.TAG_NAME);
        if (btn == null && HtmlBean.isHtmlBean(beanInfo))  //!CQ TODO: should remove this when html beaninfos are avail
            return HtmlBean.getBeanTagName(beanInfo);
        return btn;
    }

    /**
     * Get the tag library URI for a given faces type
     *
     * @param beanInfo
     * @return
     */
    public static final String getBeanTaglibUri(BeanInfo beanInfo) {
        String btu = getBeanDescriptorValue(beanInfo, Constants.BeanDescriptor.TAGLIB_URI);
        if (btu == null && HtmlBean.isHtmlBean(beanInfo))  //!CQ TODO: should remove this when html beaninfos are avail
            return HtmlBean.getBeanTaglibUri(beanInfo);
        return btu;
    }

    /**
     * Get the recomended tag library prefix for a given faces type
     *
     * @param beanInfo
     * @return
     */
    public static final String getBeanTaglibPrefix(BeanInfo beanInfo) {
        String btp = getBeanDescriptorValue(beanInfo, Constants.BeanDescriptor.TAGLIB_PREFIX);
        if (btp == null && HtmlBean.isHtmlBean(beanInfo))  //!CQ TODO: should remove this when html beaninfos are avail
            return HtmlBean.getBeanTaglibPrefix(beanInfo);
        return btp;
    }

    /**
     * Get the tag name for a given faces type
     *
     * @param beanInfo
     * @return
     */
    public static final String getBeanMarkupSection(BeanInfo beanInfo) {
        String bms = getBeanDescriptorValue(beanInfo, Constants.BeanDescriptor.MARKUP_SECTION);
        //if (HtmlBean.isHtmlBean(beanInfo))  //!CQ TODO: should remove this when html beaninfos are avail
        //    return null;
        return bms;
    }

    /**
     * Get the one unique FacesContext for this unit
     *
     * @return
     */
    public FacesContext getFacesContext() {
        return container.getFacesContext();
    }

    /**
     * Get the one unique faces Application for this unit
     *
     * @return
     */
    public Application getFacesApplication() {
        return getFacesContext().getApplication();
    }

    /**
     * @return the name of the runtime faces managed-bean for this unit's outer bean
     */
    public String getBeanName() {
        String result = getBeanNameUnfixed();
        result = fixPossiblyImplicitBeanName(result);
        return result;
    }

    /**
     * @return the name of the runtime faces managed-bean for this unit's outer bean
     */
    public String getBeanNameUnfixed() {
        if (packageName != null && packageName.length() > 0) {
            String pkg = packageName;
            if (pkg.startsWith(rootPackage)) {
                pkg = pkg.substring(rootPackage.length());
                if (pkg.startsWith("."))
                    pkg = pkg.substring(1);
            }
            if (pkg.length() > 0)
                return fixPossiblyImplicitBeanName(pkg.replace('.', '$') + "$" + getThisClassName());
        }
        // just using the this-class name for no package, or a single level package
        return fixPossiblyImplicitBeanName(getThisClassName());
    }

    protected BeanStructureScanner getNewBeanStructureScanner() {
        Class scannerClass = null;
        for (int i=0; i < FacesModel.managedBeanNames.length; i++) {
            String name = FacesModel.managedBeanNames[i];
            if(getBaseBeanClassName().equals(name)) {
                scannerClass = FacesModel.managedBeanScannerTypes[i];
                break;
            }
        }
        
        Constructor constructor = null;
        // Get an instance of scanner based on my type or one of my ancestors,
        // get the first constructor specialized for an instance of me
        Class argumentType = getClass();
        while (argumentType != null) {
            try {
                constructor = scannerClass.getDeclaredConstructor(new Class[] {argumentType});
                break;
            } catch (NoSuchMethodException e1) {
                argumentType = argumentType.getSuperclass();
            }
        }
        BeanStructureScanner scanner;
        try {
            scanner = (BeanStructureScanner) constructor.newInstance(new Object[] {this});
        } catch (Exception e) {
            scanner = null;
        }
        if (scanner == null)
            // Always fall back to this
            scanner = new BeanStructureScanner(this);
        return scanner;
    }
}
