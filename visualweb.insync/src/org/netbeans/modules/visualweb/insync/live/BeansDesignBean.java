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

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;

import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.EventDescriptor;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignProperty;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.beans.Naming;
import org.netbeans.modules.visualweb.insync.faces.FacesBean;
import java.util.Iterator;

/**
 * A live bean implementation that uses a beans.Bean (or faces.FacesBean) to persist the bean's state.
 * Knows about both Java and markup based persistance.
 *
 * @author Carl Quinn
 * @version 1.0
 */
public class BeansDesignBean extends SourceDesignBean {

    protected final Bean bean;

    /**
     * @param unit
     * @param beanInfo
     * @param liveBeanInfo
     * @param parent
     * @param instance
     * @param bean
     */
    public BeansDesignBean(LiveUnit unit, BeanInfo beanInfo, DesignInfo liveBeanInfo,
                         SourceDesignBean parent, Object instance, Bean bean) {
        super(unit, beanInfo, liveBeanInfo, parent, instance);
        this.bean = bean;

        if (bean.isParentCapable()) {
            setChildCapable();
        }
    }

    public Bean getBean() {
        return bean;
    }

    /**
     *
     */
    public void invokeCleanupMethod() {
        if (instance != null) {
            String cmn = bean.getCleanupMethod();
            if (cmn != null) {
                try {
                    Method cm = instance.getClass().getMethod(cmn, new Class[] {});
                    cm.invoke(instance);
                }
                catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    //------------------------------------------------------------------------------- SourceDesignBean

    public ClipImage getClipImage() {
        ClipImage image = super.getClipImage();
        image.facetName = getFacetName();
        return image;
    }

    /**
     * @return true iff a property given by descriptor should be handled in markup
     */
    protected boolean isMarkupProperty(PropertyDescriptor pd) {
        return bean.isMarkupProperty(pd);
    }

    /*
     * Create the right kind of Property handler depending on the type of the property
     */
    protected SourceDesignProperty newDesignProperty(PropertyDescriptor pd) {
        if (bean instanceof FacesBean && isMarkupProperty(pd)) {
            if (MethodBindDesignProperty.isMethodBindingProperty(pd))
                return new MethodBindDesignProperty(pd, this);
            if (FacesDesignProperty.isBindingProperty(pd))
                return new FacesDesignProperty(pd, this);
        }
        return new BeansDesignProperty(pd, this);
    }

    /*
     * Create the right kind of Event handler--a Java source based one or a MethodBound one
     */
    protected SourceDesignEvent newDesignEvent(EventDescriptor ed) {
        if (bean instanceof FacesBean) {
            Object pdO = ed.getEventSetDescriptor().getValue(Constants.EventSetDescriptor.BINDING_PROPERTY);
            if (pdO instanceof PropertyDescriptor &&
                    MethodBindDesignProperty.isMethodBindingProperty((PropertyDescriptor)pdO)) {
                DesignProperty prop = getProperty(((PropertyDescriptor)pdO).getName());
                if (prop instanceof MethodBindDesignProperty)
                    return new MethodBindDesignEvent(ed, this, (MethodBindDesignProperty)prop);
            }
        }
        return new BeansDesignEvent(ed, this);
    }

    public String getInstanceName() {
        return bean.getName();
    }

    public boolean canSetInstanceName() {
        return bean.canSetName();
    }

    public boolean setInstanceName(String name, boolean autoNumber) {
        if(Naming.isJavaKeyWord(name)){
            String msg = NbBundle.getMessage(BeansDesignBean.class, "IdIsJavaKeyWord", //NOI18N
                    new Object[]{name, getInstanceName()});
            StatusDisplayer.getDefault().setStatusText(msg);
            return false;
        }
        
        // If asked to name the bean the existing name, just say we did
        String beanname = bean.getName();
        if (name != null && name.equals(beanname))
            return true;

        // make sure we can get a good name
        String goodName = Naming.makeValidIdentifier(name);
        if (goodName == null)
            return false;

        // Grab the old name for the instanceNameChanged event
        String oldName = bean.getName();

        // now bang it in
        UndoEvent event = null;
        try {
            String description = NbBundle.getMessage(BeansDesignProperty.class, "SetInstanceName", beanname); // NOI18N
            event = unit.model.writeLock(description);
            String oldname = bean.getName();
            String newname = bean.setName(goodName, autoNumber, this);
            if (newname == null)
                return false;  // oh, bad name. wasn't changed.
            // fix event handler methods that are still in the form <oldname>_event
            Iterator ei = (events != null) ? events.iterator() : null;
            while(ei != null && ei.hasNext()) {
                BeansDesignEvent bde = (BeansDesignEvent)ei.next();
                String hname = bde.getHandlerName();
                if(hname != null) {
                    int delim = hname.indexOf("_");
                    if (delim > 0) {
                        String nameLeft = hname.substring(0, delim);
                        String nameRight = hname.substring(delim + 1);
                        if (nameLeft.equals(oldname)) {
                            bde.setHandlerName(newname + "_" + nameRight);
                            //Update the property sheet
                            fireDesignEventChanged(bde);
                        }
                    }
                }
            }
            //org.netbeans.modules.visualweb.insync.java.Statement holds on to bean name because of bug #96387
            //Until that bug is fixed, this is a workaround to fix #103122
            Iterator pi = (properties != null) ? properties.iterator() : null;
            while(ei != null && pi.hasNext()) {
                BeansDesignProperty bdp = (BeansDesignProperty)pi.next();
                if(bdp.property != null && bdp.getDesignBean().getInstanceName().equals(newname)) {
                    bdp.property.setBeanName(newname);
                }
            }
            
            if (!newname.equals(oldname)) {
                String scope = unit.getBeansUnit().getBeanName() + ".";
                unit.model.updateAllBeanElReferences(scope + oldname, scope + newname);
            }
        }
        finally {
            unit.model.writeUnlock(event);
        }
        
        fireDesignBeanInstanceNameChanged(oldName);
        fireDesignBeanChanged();
        return true;
    }

    /**
     * @return
     */
    public String getFacetName() {
        return (bean instanceof FacesBean) ? ((FacesBean)bean).getFacetName() : null;
    }

    //--------------------------------------------------------------------------------------- Object

    public void toString(StringBuffer sb) {
        sb.append(" instanceName:");
        sb.append(getInstanceName());
        super.toString(sb);
    }
}
