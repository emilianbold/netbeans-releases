/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ConfigBeanNode.java
 *
 * Created on August 15, 2001, 4:29 PM
 */

package org.netbeans.modules.j2ee.sun.share.config.ui;

import java.util.*;
import java.beans.*;
import java.awt.Image;
import java.awt.Component;

import javax.enterprise.deploy.spi.DConfigBean;

import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.share.config.ConfigBeanStorage;
import org.netbeans.modules.j2ee.sun.share.configbean.Base;
import org.netbeans.modules.j2ee.sun.share.configbean.DConfigBeanProperties;


/**
 *
 * @author  gfink
 * @author  Jeri Lockhart
 * @version
 */
public class ConfigBeanNode extends AbstractNode {
    
    final ConfigBeanStorage bean;
    final BeanInfo info;
    final DConfigBeanProperties extraProps;
    
    public ConfigBeanNode(ConfigBeanStorage bean) {
        super(new ConfigChildren(bean));
        this.bean = bean;
        info = ConfigUtils.createBeanInfo(bean.getConfigBean());

//        extraProps = bean.getStorage().getServer().getDConfigBeanProperties(bean.getConfigBean());
        DConfigBean dcb = bean.getConfigBean();
        if(dcb instanceof Base) {
            extraProps = ((Base) dcb).getUICustomization(dcb);
        } else {
            extraProps = null;
        }
    }
    
    public String getDisplayName() {
        if(extraProps != null) return extraProps.getDisplayName();
        if(info == null) return bean.getClass().toString();
        return info.getBeanDescriptor().getDisplayName();
    }
    
    public Node.Cookie getCookie(Class type) {
        if (SaveCookie.class.isAssignableFrom(type)) {
            return bean.getStorage().getPrimaryDataObject().getCookie(type);
        }
        return super.getCookie(type);
    }
    
    private static final javax.swing.Action[] EMPTY_ACTIONS = new javax.swing.Action[0];
    public javax.swing.Action[] getActions(boolean context) {
        return EMPTY_ACTIONS;
    }
    
    public HelpCtx getHelpCtx() {
        if(extraProps != null) {
            String helpId = extraProps.getHelpId();
            if(helpId != null) return new HelpCtx(helpId);
        }
        return HelpCtx.DEFAULT_HELP;
    }
    
    public Image getIcon(int type) {
        if(info != null) {
            Image icon = info.getIcon(type);
            if(icon != null) return icon;
        }
        return super.getIcon(type);
    }
    
    public Image getOpenedIcon(int type) {
        if(info != null) {
            Image icon = info.getIcon(type);
            if(icon != null) return icon;
        }
        return super.getOpenedIcon(type);
    }
    
    public Sheet createSheet() {
        Sheet ret = new Sheet();
        Sheet.Set set = ConfigUtils.createSheet(bean);
        set.setName(getDisplayName());
        ret.put(set);
        return ret;
    }
    
    public DConfigBean getBean() {
        return bean.getConfigBean();
    }
    
    /** Get the customizer.
     * @return <code>null</code> in the default implementation
     *
     */
    public Component getCustomizer() {
        Component comp = null;
        if (!hasCustomizer()) {
            return null;
        }
        try {
            // If there isn't such a customizer, try the default constructor
            return (java.awt.Component) info.getBeanDescriptor().getCustomizerClass().newInstance();
        }catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return comp;
    }
    
    /** Does this node have a customizer?
     * @return <CODE>false</CODE>
     *
     */
    public boolean hasCustomizer() {
        return info.getBeanDescriptor().getCustomizerClass() != null;
    }
    
    public static class ConfigChildren extends Children.Keys implements ConfigBeanStorage.ChildrenChangeListener {
        ConfigBeanStorage bean;
        ConfigChildren(ConfigBeanStorage bean) {
            this.bean = bean;
        }

        protected void addNotify() {
            updateKeys();
            bean.addChildrenChangeListener(this);
        }
         
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            bean.removeChildrenChangeListener(this);
        }

        void updateKeys() {
            setKeys(bean.getChildMap().keySet());
        }
        
        void updateKey(Object key) {
            refreshKey(key);
        }
        
        protected Node[] createNodes(Object key) {
            Collection nodeSet = (Collection) bean.getChildMap().get(key);
            ArrayList ret = new ArrayList();
            Iterator i = nodeSet.iterator();
            for(int c = 0; i.hasNext(); c++) {
                ConfigBeanStorage cbs = (ConfigBeanStorage) i.next();
                Node node = cbs.getNode();
                if (node != null) {
                    ret.add(node);
                }
            }
            return (Node[]) ret.toArray(new Node[ret.size()]);
        }
        
        public void childBeanAdded(ConfigBeanStorage childStorage) {
            updateKeys();
            refreshKey(childStorage.getConfigBean().getDDBean().getXpath());
        }
        
        public void childBeanRemoved(ConfigBeanStorage childStorage) {
            updateKey(childStorage.getConfigBean().getDDBean().getXpath());
        }
    }
}
