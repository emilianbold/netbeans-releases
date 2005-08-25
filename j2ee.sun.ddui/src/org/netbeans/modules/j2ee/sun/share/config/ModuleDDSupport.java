/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*

How to use this class to display ConfigBeans next to your deployment
descriptor fragments:

1.  Create the basebean
2.  Decide where you're going to potentially show sheets and customizers.
3.  Implement ModuleSupportCallback (minimal implementation fine for now).
4.  Create a ModuleDeploymentSupport instance.
5.  Get the standardDDBean back from the MDS.
6.  Query each of your plugins for their ConfigBeans.
7.  Give the ConfigBeans back to your MDS.
8.  When you want to show property sheets or components from the plugin,
    call add{sheet,customizer}listener.
9.  Receive the {Sheets,Customizers} and display them/remove them when
    pertinent
10. When ModuleSupportCallback.beanModified() is called, activate the
    save cookie on your DataObject.

 */
package org.netbeans.modules.j2ee.sun.share.config;

import java.util.*;
import java.beans.*;

import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.shared.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.WeakListeners;

import org.netbeans.modules.schema2beans.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;


/**
 */
public class ModuleDDSupport implements PropertyChangeListener {

    public static final String SEPARATOR = "/"; //NOI18N
    public static final String WEBSERVICES_XML = "webservices.xml"; //NOI18N
    private static Map filenameToPathMap = null;

    private Map rootMap = new HashMap(5); // DD location string -> DDRoot
    private Map configMap = new IdentityHashMap(5); // DD object -> ConfigBeanStorage
    private Map beanMap = Collections.synchronizedMap(new IdentityHashMap()); // BaseBean -> StandardDDImpl
    private Map leafMap = Collections.synchronizedMap(new IdentityHashMap()); // BaseProp -> StandardDDImpl
    private Set xpathListeners = new HashSet();
    private PropertyChangeListener weakListener;
    private J2eeModuleProvider provider;
    private DeploymentConfiguration config;
    
    private static Map moduleDDlocationMap = new HashMap(10); 

    static {
        moduleDDlocationMap.put(J2eeModule.EAR,
        new String[] {J2eeModule.APP_XML});
        moduleDDlocationMap.put(J2eeModule.WAR,
        new String[] {J2eeModule.WEB_XML,J2eeModule.WEBSERVICES_XML});
        moduleDDlocationMap.put(J2eeModule.EJB,
        new String[] {J2eeModule.EJBJAR_XML,J2eeModule.EJBSERVICES_XML});
        moduleDDlocationMap.put(J2eeModule.CONN,
        new String[] { J2eeModule.CONNECTOR_XML});
        moduleDDlocationMap.put(J2eeModule.CLIENT,
        new String[] { J2eeModule.CLIENT_XML});
    }
    
    public static String[] getDDPaths(Object type) {
        return (String[]) moduleDDlocationMap.get(type);
    }
    
    public ModuleDDSupport(J2eeModuleProvider provider, DeploymentConfiguration config) {
        this.provider = provider;
        this.config = config;
        String[] ddLocs = getDDPaths(provider.getJ2eeModule().getModuleType());
        for(int i = 0; i < ddLocs.length; i++) {
            createRoot(ddLocs[i]);
        }
    }

    private ModuleType getModuleType() {
        return (ModuleType) provider.getJ2eeModule().getModuleType();
    }
    
    private BaseBean getDeploymentDescriptor(String ddLoc) {
        return provider.getJ2eeModule().getDeploymentDescriptor(ddLoc);
    }
    
    private DDRoot createRoot(String ddLoc) {
        BaseBean bean = getDeploymentDescriptor(ddLoc);
        if (bean == null) { // no support for that descriptor
            return null;
        }
        while(!bean.isRoot()) {
            bean = bean.parent();
        }
        DDRoot root = new DDRoot(new DDNodeBean(null,bean,this));
        rootMap.put(ddLoc,root);

        beanMap.put(bean,root);
        weakListener = WeakListeners.propertyChange(this,root.proxy.bean);
        root.proxy.bean.addPropertyChangeListener(weakListener/* this*/);
        return root;
    }

    public DeployableObject getDeployableObject() {
        return config.getDeployableObject();
    }

    // This is broken in jsr88 that I even have to supply this
    public DDRoot getDDBeanRoot() {
        String loc = ((String[]) moduleDDlocationMap.get(getModuleType()))[0];
        return getDDBeanRoot(loc);
    }

    public DDRoot getDDBeanRoot(String loc) {
        DDRoot root = (DDRoot) rootMap.get(loc);
        
        // primary DD should be ready when this is called
        // so this updating is only for non-primary DD's
        if (root == null && ! isPrimaryDD(loc, getType())) {
            root = createRoot(loc);
            DDRoot proot = getPrimaryDD();
            ConfigBeanStorage configRoot = (ConfigBeanStorage) configMap.get(proot);
            
            if (root != null && configRoot != null) {
                DConfigBeanRoot cbroot = (DConfigBeanRoot) configRoot.getConfigBean();
                DConfigBean cb = cbroot.getDConfigBean(root);
                if (cb != null) {
                    try {
                        ConfigBeanStorage cbs = new ConfigBeanStorage(cb, null, configRoot.getStorage());
                        configMap.put(root, cbs);
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
        }
        return root;
    }

    public ModuleType getType() {
        return getModuleType();
    }

    public String getVersion() {
        return provider.getJ2eeModule().getModuleVersion();
    }

    /* PENDING get from CompilationUnit */
    public Class getClassFromScope(String cls) {
        return null; // provider.getClassFromScope(cls);
    }

    public Node[] getNodes() {
        String[] ddLocs = (String[]) moduleDDlocationMap.get(getModuleType());
        List ret = new ArrayList();
        for(int i = 0; i < ddLocs.length; i++) {
            Object dd = rootMap.get(ddLocs[i]);
            if(dd == null) {
                continue;
            }
            ConfigBeanStorage cbs = (ConfigBeanStorage) configMap.get(dd);
            if(cbs != null) {
                Node n = cbs.getNode();
                if (n != null) {
                    ret.add(n);
                } else {
                    throw new RuntimeException("CBS.getNode returned null"); //NOI18N
                }
            }
        }
        return (Node[]) ret.toArray(new Node[ret.size()]);
    }

    public void resetConfigCache() {
        configMap = new IdentityHashMap(5); // DD object -> ConfigBeanStorage
        xpathListeners = new HashSet();
    }

    public void createConfigs(ConfigurationStorage storage) throws ConfigurationException {
        String[] ddLocs = (String[]) moduleDDlocationMap.get(getModuleType());
        DDRoot root = (DDRoot) rootMap.get(ddLocs[0]);
        DConfigBeanRoot cbroot = config.getDConfigBeanRoot(root);
        ConfigBeanStorage cbs = new ConfigBeanStorage(cbroot, null, storage);
        configMap.put(root,cbs);

        for(Iterator it = rootMap.keySet().iterator(); it.hasNext() ;) {
            String ddLoc = (String) it.next();
            if (isPrimaryDD(ddLoc, getModuleType())) {
                continue;
            }
            root = (DDRoot) rootMap.get(ddLoc);
            DConfigBean cb = cbroot.getDConfigBean(root);
            if(cb == null) {
                continue;
            }
            ConfigBeanStorage cbStorage = new ConfigBeanStorage(cb, null, storage);
            configMap.put(root,cbs);
        }
    }

    static public boolean isPrimaryDD(String ddLocation, Object type) {
        String[] ddLocs = (String[]) moduleDDlocationMap.get(type);
        if (ddLocs.length < 1) {
            return false;
        }
        return ddLocs[0].equals(ddLocation);
    }
    
    static private Map filenameToPathMap() {
        if (filenameToPathMap == null) {
            filenameToPathMap = new HashMap();
            
            filenameToPathMap.put(filename(J2eeModule.APP_XML), J2eeModule.APP_XML);
            filenameToPathMap.put(filename(J2eeModule.WEB_XML), J2eeModule.WEB_XML);
            filenameToPathMap.put(filename(J2eeModule.EJBJAR_XML), J2eeModule.EJBJAR_XML);
            filenameToPathMap.put(filename(J2eeModule.CONNECTOR_XML), J2eeModule.CONNECTOR_XML);
            filenameToPathMap.put(filename(J2eeModule.CLIENT_XML), J2eeModule.CLIENT_XML);

            filenameToPathMap.put(J2eeModule.APP_XML, J2eeModule.APP_XML);
            filenameToPathMap.put(J2eeModule.WEB_XML, J2eeModule.WEB_XML);
            filenameToPathMap.put(J2eeModule.EJBJAR_XML, J2eeModule.EJBJAR_XML);
            filenameToPathMap.put(J2eeModule.CONNECTOR_XML, J2eeModule.CONNECTOR_XML);
            filenameToPathMap.put(J2eeModule.CLIENT_XML, J2eeModule.CLIENT_XML);
        }
        return filenameToPathMap;
    }

    static private String filename(String path) {
        int i = path.lastIndexOf(SEPARATOR);
        return path.substring(i+1);
    }
    
    static public String filenameToPath(String filename, Object type) {
        if (filename.endsWith(WEBSERVICES_XML)) {
            if (J2eeModule.EJB.equals(type)) {
                return J2eeModule.EJBSERVICES_XML;
            } else {
                return J2eeModule.WEBSERVICES_XML;
            }
        }
        String name = (String) filenameToPathMap().get(filename);
        if (name == null) {
            name = filename;
        }
        return name;
    }
    
    public DDRoot getPrimaryDD() {
        ModuleType type = this.getType();
        String[] ddLocs = (String[]) moduleDDlocationMap.get(type);
        if (ddLocs.length < 1) {
            return null;
        }
        return (DDRoot) rootMap.get(ddLocs[0]);
    }
    
    /* Called when the module/app is closed from the ide, clean up listeners and
     * references */
    public void cleanup() {
        // stop listening to DD changes
        for (Iterator i = rootMap.values().iterator(); i.hasNext();) {
            DDRoot root = (DDRoot)i.next();
            root.proxy.bean.removePropertyChangeListener(weakListener);
        }
        rootMap = null; 
        configMap = null; 
        beanMap = null; 
        xpathListeners = null; 
        leafMap = null;
        provider = null;
    }

    /* Called when the module is removed from the app. */
    public void dispose(DeploymentConfiguration config) {
        for(Iterator it = configMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            DDRoot root = (DDRoot) entry.getKey();
            ConfigBeanStorage cbs = (ConfigBeanStorage) entry.getValue();
            root.proxy.bean.removePropertyChangeListener(weakListener);
            try {
                config.removeDConfigBean((DConfigBeanRoot)cbs.bean);
            } catch (BeanNotFoundException bnfe) {
                // IGNORE
            }
        }
    }

    StandardDDImpl getBean(BaseBean bean) {
        //     System.out.println("Getting bean for " + bean);
        //     System.out.println(bean.fullName());
        //     System.out.println(bean.dtdName());
        if (bean == null) {
            return null;
        }
        
        StandardDDImpl ret = (StandardDDImpl) beanMap.get(bean);
        
        if (ret == null) {
            /*
            DDCommon base;
            //            System.out.println("Creating new bean");
            BaseBean bb = bean;
            while(!bb.isRoot()) {
                bb = bb.parent();
                if (bb== null) {
                    // We are in an unattached tree, we have expressed no prior
                    // interest in this Xpath so we just toss it.
                    // See: addTemporaryBean in this object
                    return null;
                }

            }
            if(bb == root.proxy.bean) base = new DDNodeBean(bean,this);
            else { // must build proxy tree
                if (bean.isRoot()) {
                    // PENDING This probably means that there is an error, can it legaly happen?
                    throw new IllegalStateException("Found a bean rooted in a tree not previously registered with Module Deployment Support. Bean = : " + bean + "@" +  Integer.toHexString(bean.hashCode())); //NO I18N
                }
                StandardDDImpl parent = getBean(bean.parent());
                base = new DDProxy(parent.proxy,bean,bean.dtdName(),this);
            }
             **/
	    if (bean.parent() != null) {
                ret = new StandardDDImpl(new DDNodeBean(bean,this));
                beanMap.put(bean,ret);
            }
        }
        return ret;
    }

    // for indexed leaf properties
    StandardDDImpl getBean(BaseProperty prop,int index) {

        if(index < 0) {
            return getBean(prop);
        }

        if (!leafMap.containsKey(prop)) {
            leafMap.put(prop, new StandardDDImpl[index + 1]);
        } else if (((StandardDDImpl[])leafMap.get(prop)).length <= index) {
            StandardDDImpl[] a = (StandardDDImpl[])leafMap.get(prop);
            StandardDDImpl[] b = new StandardDDImpl[index + 1];

            leafMap.put(prop, b);
            for (int i = 0; i < a.length; i++) {
                b[i] = a[i];
            }
        }

        StandardDDImpl[] arr = (StandardDDImpl[])leafMap.get(prop);
        StandardDDImpl elem = arr[index];

        if (elem == null) {
            elem = new StandardDDImpl(new DDLeafBean(prop, index, this));
            arr[index] = elem;
        }

        return elem;
     }

    // for non-indexed leaf properties
    StandardDDImpl getBean(BaseProperty prop) {
       StandardDDImpl elem = (StandardDDImpl) leafMap.get(prop);
       if(elem == null) {
          elem = new StandardDDImpl(new DDLeafBean(prop, this));
          leafMap.put(prop,elem);
       }
       return elem;
    }

    StandardDDImpl getBean(String name) {
        return getBean(name,getDDBeanRoot().proxy.bean);
    }

    StandardDDImpl getBean(String name,BaseBean rootBean) {
        Bean parent = GraphManager.getPropertyParent(rootBean, name);
        if (parent == null) {
            return getDDBeanRoot();
        }
        String shortName = GraphManager.getPropertyName(name);
        int index = GraphManager.getPropertyIndex(rootBean, name);
        //        System.out.println(name);
        //        System.out.println(index);

        BaseProperty prop = parent.getProperty(shortName);

        if(index < 0 && prop.isIndexed()) {
            index = 0;
        }

        StandardDDImpl ret;
        if(prop.isBean()) {
            if(prop.isIndexed()) {
                ret = getBean((BaseBean) parent.getValue(shortName,index));
            } else {
                ret = getBean((BaseBean) parent.getValue(shortName));
            }
        }
        else {
            if(prop.isIndexed()) {
                ret = getBean(prop,index);
            } else {
                ret = getBean(prop);
            }
        }
        //        System.out.println(ret.proxy.bean.fullName());
        //        System.out.println(((Object)ret.proxy.bean).toString());
        return ret;
    }

    void addXpathListener(DDCommon bean, String xpath, XpathListener listen) {
        xpathListeners.add(new XpathListenerStorage(bean,xpath,listen));
    }

    void removeXpathListener(DDCommon bean, String xpath, XpathListener listen) {
        xpathListeners.remove(new XpathListenerStorage(bean,xpath,listen));
    }

    /* functional spec for processing the PropertyChangeEvents:
     *
     * Ways in which listeners are added:
     * 1.  Customizer/Sheet Listeners
     * 2.  ConfigBean getChildBean()
     * 3.  ConfigBean associated Bean.
     * 4.  Xpath listeners (in all situations just fire XpathEvent)
     *
     * Types of Events:
     * 1.  Bean added
     * 2.  Bean removed
     * 3.  Bean changed
     *
     * 4.  Plugin added
     * 5.  Plugin removed
     * 6.  Listener added
     *
     * Location of Event:
     * 1.  Current bean
     * 2.  Descendant bean
     * 3.  Ancestor bean (removal only)
     *
     * Other event type:
     *
     * Case-by-case breakdown:
     *
     * Location of Event: Current Bean
     *
     *               Added              Removed              Changed
     *
     *  Listener:    N/A            remove listener           N/A
     *  getChild():  if matches,
     *          Call ConfigBean.getChild()
     *          Add it to parent property
     *          sheet if necessary.
     *                                 N/A                   N/A
     *  Bean:             fire notifyStandardDDBean changed.
     *
     * Location of Event: Descendant Bean
     *
     *               Added              Removed              Changed
     *
     *  Listener:                 N/A
     *  getChild():  see above           N/A                   N/A
     *  Bean:              fire notifyStandardDDBean changed
     *
     * Location of Event: Ancestor Bean
     *
     *               Added              Removed              Changed
     *
     *  Listener:                        N/A
     *  getChild():  check match         N/A                  N/A
     *  Bean:                         if removed is self,
     *                                call removeChildBean()
     *                                on parent
     *
     *  Other events:
     *            ListenerAdded       PluginAdded            PluginRemoved
     *  Listener:      N/A        Calculate display for new     remove display
     *                             plugin
     *  getChild(): Calculate display   Call all getChild()   remove listeners
     *              for associated      methods applicable
     *              ConfigBean
     *  bean:       as above              as above            remove listeners
     */

    public void propertyChange(PropertyChangeEvent event) {

        Object oldValue = event.getOldValue();
        //        System.out.println("Old value" + oldValue);
        Object newValue = event.getNewValue();
        //        System.out.println("New value" + newValue);
        String name = event.getPropertyName();

        //        System.out.println("Processing ddbeans event " + name);
        //        System.out.println("From source " + event.getSource());
        //        System.out.println(event.getSource().getClass());

        try {
            StandardDDImpl eventBean = null;
            if(newValue == null && oldValue instanceof BaseBean) {
                eventBean = getBean((BaseBean) oldValue);
            }
            else {
                Object eventObj = oldValue != null ? oldValue : newValue;
                if (!(eventObj instanceof BaseBean)) {
                    eventObj = event.getSource();
                }
                if (eventObj instanceof BaseBean) {
                    BaseBean root = (BaseBean) eventObj;
                    while (! root.isRoot()) {
                        root = root.parent();
                    }
                    //check if same root that we saw
                    boolean rootInCache = false;
                    for (Iterator ddRoots=rootMap.values().iterator(); ddRoots.hasNext();) {
                        DDRoot ddroot = (DDRoot) ddRoots.next();
                        if (ddroot.proxy != null && ddroot.proxy.bean == root) {
                            rootInCache = true;
                            break;
                        }
                    }
                    if (rootInCache) {
                        eventBean = getBean(name, root);
                    }
                } 
                if (eventBean == null) {
                    eventBean = getBean(name);
                }
            }

            //  this is the case where an array assignment is made
            //  too change a whole set of properties, and to make
            //  sense of the events.  Our UI only generates these
            //  array assignments if the oldvalue is non-null.
            if (eventBean == null && oldValue instanceof Object[]) {
                // process separate propertyChange events for each
                // array element.
                List newElements = new ArrayList();
                if(newValue != null) {
                    Object[] newValues = (Object[])newValue;
                    for (int i=0; i<newValues.length; i++) {
                        if (newValues[i] == null) {
                            continue;
                        }
                        newElements.add(newValues[i]);
                    }
                }
                Object[] values = (Object[]) oldValue;
                for(int i = 0; i < values.length; i++) {
                    Object value = values[i];
                    // PENDING tracking indicies of non-BaseBean
                    // properties does not work.
                    if(!(value instanceof BaseBean)) {
                        break;
                    }
                    // no change in this element
                    if(newElements.contains(value)) {
                        newElements.remove(value);
                        continue;
                    }
                    StandardDDImpl valueBean = getBean((BaseBean)value);
                    // I still don't know anything about this bean.
                    if(valueBean == null) {
                        continue;
                    }
                    // this element has been removed.
                    processEvent(value,null,valueBean.proxy,event);
                }
                /*for(Iterator i = newElements.iterator();i.hasNext();) {
                    i.next();
                    // PENDING ignore for now - these should have already
                    // generated events for adds?
                }*/
            }

            // swallow events we know nothing about.
            if (eventBean == null) {
                return;
            }

            if(oldValue == null && eventBean.proxy.isProxy()) {
                eventBean.setProxy(new DDNodeBean((DDProxy)eventBean.proxy));
                return; // swallow this event
            }

            processEvent(oldValue,newValue,eventBean.proxy,event);

        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    void processEvent(Object oldValue, Object newValue, DDCommon eventBean, PropertyChangeEvent event) {

        //        System.out.println("Processing event on " + eventBean);


        // Start with just XpathEvents.
        // 0.  Make the StandardDDBean for the Event, get its Xpath.
        // 1.  Iterate through all the listeners.  // optimize lookup later
        // 2.  for listener l
        // 3.  Make the listener's xpath.
        // 4.  If (3) is related to source, continue.
        // 5.  Find the real BB + xpath for the listener
        // 6.  Check the ancestry relationship between the Event's BB
        //     and the Listener's BB
        // 7.  If share ancestry, Construct XpathEvent, fire.

        // PENDING should get from source + property

        String eventDtdPath = eventBean.getXpath();

        Object type = XpathEvent.BEAN_CHANGED;
        if(oldValue == null) {
            type = XpathEvent.BEAN_ADDED;
        }
        if(newValue == null) {
            type = XpathEvent.BEAN_REMOVED;
        }
        XpathEvent xe = new XpathEvent(eventBean.container,type);
        xe.setChangeEvent(event);


        Object xpathListenerArray[] = xpathListeners.toArray();
        for (int i = 0; i < xpathListenerArray.length; i++) {
            XpathListenerStorage x = (XpathListenerStorage) xpathListenerArray[i];
            if (x.bean == null) {
                continue;
            }
            String xp = x.getNormalizedPath();

            //            System.out.println("Checking against listener " + xp);

            //PENDING - handle delete events on completely different code path?
            // need to get this code working for DDBean ancestry traversal.
            DDCommon leftBean,rightBean;
            if(eventDtdPath.startsWith(xp)) {
                //               System.out.println("Event dtd is smaller");
                leftBean = x.bean;
                rightBean = eventBean;
            } else if(xp.startsWith(eventDtdPath)) {
                //               System.out.println("Event dtd is bigger");
                leftBean = eventBean;
                rightBean = x.bean;
            } else {
                continue;
            }
            while (leftBean != rightBean && rightBean != null) {
                rightBean = rightBean.parent;
            }
            if(leftBean == rightBean) {
                x.listen.fireXpathEvent(xe);
            }
        }
        // should look through DDBeans we know about and check for
        // relative listeners that way.  This perhaps means we pop
        // up the event bean ancestor list and just look up the DDBean
        // directly to process events.
        eventBean.fireEvent(xe);
        // PENDING remove should remove the DDBean and any children from the cache.

    }

    private class XpathListenerStorage {
        private DDCommon bean = null;
        private String xpath;
        private boolean xpathRelative;
        private XpathListener listen;
        private String normal = null;

        XpathListenerStorage(DDCommon bean,String xpath,XpathListener listen) {
            this.bean = bean; 
            this.xpath = xpath; 
            this.listen = listen;
            xpathRelative = ! xpath.startsWith(SEPARATOR);
        }

        public String getNormalizedPath() {
            if(normal == null) {
                String base = xpath;
                if (xpathRelative) {
                    base = bean.getXpath() + SEPARATOR + base;
                }
                normal = normalizePath(base);
            }
            return normal;
        }

        public String toString() {
            return bean + " " + xpath + " " + listen;
        }

        public int hashCode() { return listen.hashCode(); }

        public boolean equals(Object o) {
            if(o instanceof XpathListenerStorage) {
                XpathListenerStorage x = (XpathListenerStorage) o;
                return (x.bean == bean) && (x.xpath == xpath) && (x.listen == listen);
            }
            return false;
        }

    }

    static String normalizePath(String path) {
        boolean absolute = path.startsWith(SEPARATOR);
        StringTokenizer tokens = new StringTokenizer(path, SEPARATOR, false);

        LinkedList l = new LinkedList();

        while(tokens.hasMoreElements()) {
            l.addLast(tokens.nextElement());
        }

        for(int i = 0 ; i < l.size(); ) {
            String tok = (String) l.get(i);
            if(tok.equals(".")) {
                l.remove(i);
            } else if(tok.equals("..") && i > 0 && !l.get(i-1).equals("..")) {
                l.remove(i);
                l.remove(i-1);
                i--;
            } else {
                i++;
            }
        }

        StringBuffer ret = new StringBuffer();

        for(int i = 0; i < l.size(); i++) {
            if(absolute || i > 0) {
                ret.append(SEPARATOR);
            }
            ret.append(l.get(i));
        }

        return ret.toString();

    }
    
    public J2eeModuleProvider getProvider() {
        return provider;
    }
}
