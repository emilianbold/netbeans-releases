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


package org.netbeans.modules.j2ee.sun.share.config;

import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.schema2beans.*;
import org.openide.ErrorManager;
import java.io.Writer;
import java.io.StringWriter;
import java.util.*;

abstract public class DDCommon implements DDBean {
    
    StandardDDImpl container;
    DDCommon parent = null;
    final BaseBean bean;
    final String xpath;
    final String dtdname;
    final ModuleDDSupport support;
    final Set configBeans = new HashSet();
    final Set childBeans = new HashSet();
    
    // must know about its STandardDDImpl to pass to ConfigBeanStorage
    DDCommon(DDCommon copy) {
        this(copy.parent,copy.bean,copy.support,copy.xpath);
        configBeans.addAll(copy.configBeans);
    }
    
    DDCommon(DDCommon parent, BaseBean bean, ModuleDDSupport support, String dtdname) {
        this.parent = parent;
        this.bean = bean;
        this.dtdname = dtdname;
        this.xpath = ((parent == null) ? "" : parent.xpath) + "/" + dtdname; // NOI18N
        this.support = support;
        if(parent != null) {
            parent.addChild(this);
        }
    }
    
    void addChild(DDCommon bean) {
        childBeans.add(bean);
    }
    
    void removeChild(DDCommon bean) {
        childBeans.remove(bean);
    }
    
    // find the DDBean which is a child of this DDBean that has the
    // given corresponding BaseBean.  If there is no such DDBean, return
    // null
    DDCommon findChild(BaseBean bean) {
        for(Iterator i = childBeans.iterator();i.hasNext(); ) {
            DDCommon child = (DDCommon) i.next();
            if(child.bean == bean) {
                return child;
            }
        }
        return null;
    }
    
    final public String getXpath() {
        return xpath;
    }
    
    final public DDBeanRoot getRoot() {
        DDCommon root = this;
        while(root.parent != null) {
            root = root.parent;
        }
        return (DDRoot) support.getBean(root.bean);
    }
    
    final public DDBean[] getChildBean(String xpath) {
        return getChildrenImpl(xpath);
    }
    
    public String getText() {
        Writer w = new StringWriter();
        try {
            bean.writeNode(w);
        } catch (Exception e) {
        }
        return w.toString();
    }
    
    final public String[] getText(String xpath) {
        StandardDDImpl[] dds = getChildrenImpl(xpath);
        if(dds == null) {
            return null;
        }
        String[] ret = new String[dds.length];
        for(int i = 0; i < dds.length; i++) {
            ret[i] = dds[i].proxy.getText();
        }
        return ret;
    }
    
    final private StandardDDImpl[] getChildrenImpl(String xpath) {
        //        System.out.println("Starting search with " + xpath);
        xpath = ModuleDDSupport.normalizePath(xpath);
        //        System.out.println("Now " + xpath);
        DDCommon searchRoot = this;
        if(xpath == null || xpath.equals("") || xpath.equals(".")) // NOI18N
            return new StandardDDImpl[] { container };
            if(xpath.startsWith("/")) { // NOI18N
                searchRoot = ((DDRoot)getRoot()).proxy;
                xpath = xpath.substring(xpath.indexOf("/") + 1); // NOI18N
            } else if (xpath.equals("..")) {
                if(parent == null) return null;
                else return new StandardDDImpl[] { parent.container };
            } else while (xpath.startsWith("../") && searchRoot != null) {
                searchRoot = searchRoot.parent;
                xpath = xpath.substring(3);
            }
            
            Collection ret = searchRoot.search(xpath,true);
            
            StandardDDImpl[] arr = new StandardDDImpl[ret.size()];
            ret.toArray(arr);
            return arr;
    }
    
    Collection search(String xpath,boolean addCurrent) {
        
        Collection ret = new LinkedList();
        
        int index = xpath.indexOf("/"); // NOI18N
        String fragment = index < 0 ? xpath : xpath.substring(0,index);
        
        if(isProxy()) {
            // find all children manually
            BeanProp prop = bean.beanProp(fragment);
            if(prop != null) {
                String remainder = index < 0 ? "" : xpath.substring(index); // NOI18N
                if(prop.isIndexed()) {
                    Object[] values = prop.getValues();
                    for(int i = 0; i < values.length; i++) {
                        DDCommon ddc = prop.isBean() ? support.getBean((BaseBean) values[i]).proxy
                        : support.getBean(prop,i).proxy;
                        ret.addAll(ddc.search(remainder,true));
                    }
                } else {
                    DDCommon ddc = prop.isBean() ? support.getBean(prop.getBean()).proxy
                    : support.getBean(prop,-1).proxy;
                    ret.addAll(ddc.search(remainder,true));
                }
            }
        } else if(addCurrent) {
            DDParser parser = new DDParser(bean,xpath);
            
            while(parser.hasNext()) {
                Object current = parser.next();
                DDParser.DDLocation location = parser.getLocation();
                if(location.isNode()) {
                    BaseBean currentBean = (BaseBean) current;
                    ret.add(support.getBean(currentBean));
                }
                else {
                    ret.add(support.getBean(
                    location.getRoot().getProperty(location.getName()),
                    location.getIndex()));
                }
            }
        }
        
        if(index < 0) return ret;
        
        // PENDING optimization - keep a semaphore recording whether
        // or not there are any children proxies, if not you can
        // skip this loop
        for(Iterator i = childBeans.iterator(); i.hasNext() ; ) {
            DDCommon ddc = (DDCommon) i.next();
            if(ddc.dtdname.equals(fragment))
                ret.addAll(ddc.search(xpath.substring(index),false));
        }
        
        return ret;
        
    }
    
    boolean isProxy() {
        return false;
    }
    
    public void addXpathListener(String xpath,XpathListener listener) {
        support.addXpathListener(this,xpath,listener);
    }
    
    public void removeXpathListener(String xpath,XpathListener listener) {
        support.removeXpathListener(this,xpath,listener);
    }
    
    public int hashCode() {
        return bean.hashCode();
    }
    
    /* Must be overridden in subclasses, and super.equals(o) must be
       part of the computation. */
    public boolean equals(Object o) {
        if(o instanceof DDCommon)
            return ((DDCommon)o).bean == bean;
        return false;
    }
    
    void fireEvent(XpathEvent xe) {
        //        System.out.println("Got event " + xe + " at " + this);
        if(xe.isChangeEvent()) {
            notifyChange(xe); 
        } else {
            // xpath is a prefix of eventXpath
            //            System.out.println("Xpath is " + xpath);
            String eventXpath = xe.getBean().getXpath();
            //            System.out.println("eventXpath is " + eventXpath);
            // take away xpath
            String relPath = getRelativePath(eventXpath, xpath);
            ConfigBeanStorage[] confBeans = getConfigBeans();
            for (int i = 0; i < confBeans.length;  i++) {
                try {
                    confBeans[i].fireEvent(relPath,xe);
                } catch (ConfigurationException e) {
                    // PENDING need to do something better here with the CE?
                    ErrorManager.getDefault().log(ErrorManager.WARNING, e.getMessage());
                }
            }
        }
        if(parent != null) {
            parent.fireEvent(xe);
        }
    }
    public static String getRelativePath(String child, String parent) {
        String relPath = child.substring(parent.length());
        if (relPath.startsWith("/")) 
            relPath = relPath.substring(1); // NOI18N    
        return relPath;
    }
    void notifyChange(XpathEvent event) {
        ConfigBeanStorage[] confBeans = getConfigBeans();
        for (int i = 0; i < confBeans.length;  i++) {
            confBeans[i].bean.notifyDDChange(event);
        }
    }
    
    // PENDING move the fireCustomizerListeners to this class as well.
    public void addConfigBean(ConfigBeanStorage cbs) {
        configBeans.add(cbs);
    }
    
    public void removeConfigBean(ConfigBeanStorage cbs) {
        configBeans.remove(cbs);
    }
    
    public ConfigBeanStorage[] getConfigBeans() {
        ConfigBeanStorage[] ret = new ConfigBeanStorage[configBeans.size()];
        configBeans.toArray(ret);
        return ret;
    }
    
    public String[] getAttributeNames() {
        return null;
    }
    
    public String getAttributeValue(String name) {
        return null;
    }
    
    public String getId() {
        return null;
    }
    
    public J2eeModuleProvider getModuleProvider() {
        return support.getProvider();
    }
}

