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
