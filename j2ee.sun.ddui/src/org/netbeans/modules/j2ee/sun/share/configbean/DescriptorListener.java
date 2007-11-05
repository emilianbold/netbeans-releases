/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.PortComponentRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;


/** Listens for changes made in the standard and webservice descriptor files.
 * 
 *  Responses configure certain key default properties in the corresponding
 *  SJSAS descriptor file as needed for zero-configuration.
 * 
 *  For example:
 *    JNDI names for J2EE 1.4 EJB's with remote interfaces
 *    Endpoint URI for EJB hosted web services.
 *
 *  This system also monitors the event stream for certain delete/create event
 *  pairs that actually represent beans being renamed or otherwise changed in
 *  some way.
 *
 * @author Peter Williams
 */
public class DescriptorListener implements PropertyChangeListener {

    private final SunONEDeploymentConfiguration config;
    private RootInterface stdRootDD = null;
    private PropertyChangeListener stdRootDDWeakListener = null;
    private RootInterface wsRootDD = null;
    private PropertyChangeListener wsRootDDWeakListener = null;
    
    private static final int EVENT_DELAY = 100;
    private PropertyChangeEvent lastEvent = null;
    private Object lastEventMonitor = new Object();
    private final RequestProcessor.Task lastEventTask = 
            RequestProcessor.getDefault().create(new Runnable() {
                public void run() {
                    synchronized (lastEventMonitor) {
                        if(lastEvent != null) {
                            processEvent(lastEvent);
                            lastEvent = null;
                        }
                    }
                }
            }, true);
            
    
    public DescriptorListener(final SunONEDeploymentConfiguration sdc) {
        config = sdc;
    }
    
    public void addListener(final RootInterface rootDD) {
        PropertyChangeListener weakListener = WeakListeners.propertyChange(this, rootDD);
        
        if(rootDD instanceof Webservices) {
            if(wsRootDD != null && wsRootDDWeakListener != null) {
                wsRootDD.removePropertyChangeListener(wsRootDDWeakListener);
            }
            wsRootDD = rootDD;
            wsRootDDWeakListener = weakListener;
        } else {
            if(stdRootDD != null && stdRootDDWeakListener != null) {
                stdRootDD.removePropertyChangeListener(stdRootDDWeakListener);
            }
            stdRootDD = rootDD;
            stdRootDDWeakListener = weakListener;
        }
        
        rootDD.addPropertyChangeListener(weakListener);
    }
    
    public void removeListener(final RootInterface rootDD) {
        if(wsRootDD == rootDD) {
            wsRootDD.removePropertyChangeListener(wsRootDDWeakListener);
            wsRootDDWeakListener = null;
            wsRootDD = null;
        } else if(stdRootDD == rootDD) {
            stdRootDD.removePropertyChangeListener(stdRootDDWeakListener);
            stdRootDDWeakListener = null;
            stdRootDD = null;
        }
    }
    
    public void removeListeners() {
        if(stdRootDD != null) {
            removeListener(stdRootDD);
        }
        if(wsRootDD != null) {
            removeListener(wsRootDD);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        synchronized (lastEventMonitor) {
//            System.out.println("RAW EVENT: " + evt.getPropertyName() +
//                    ", old = " + evt.getOldValue() + ", new = " + evt.getNewValue() + 
//                    ", source = " + evt.getSource());

            if(lastEvent != null) {
                // Cancel scheduled task.  If this were to return false (already run/running)
                // then one of the following must be true:
                //
                // (a) the task finished, but then lastEvent == null and we can't get here in
                //     in the first place.
                //
                // (b) the task started, but this thread grabbed the lastEventMonitor first
                //     in which case the task will be stalled until this block completes
                //     and then lastEvent will be null in the task and the task will
                //     do nothing (so it doesn't matter that we couldn't cancel it.)
                //
                lastEventTask.cancel();
                
                if(!isCreateBeanEvent(evt) || !isVeiledRenameEvent(lastEvent, evt) ||
                        !processAsChangeNameEvent(lastEvent, evt)) {
                    processEvent(lastEvent);
                    processEvent(evt);
                }

                lastEvent = null;
            } else {
                if(isDeleteBeanEvent(evt)) {
                    // store delete bean event and return.
                    lastEvent = evt;
                    lastEventTask.schedule(EVENT_DELAY);
                } else {
                    processEvent(evt);
                }
            }
        }
    }
    
    private boolean isDeleteBeanEvent(PropertyChangeEvent evt) {
        return evt.getOldValue() instanceof CommonDDBean && evt.getNewValue() == null;
    }
    
    private boolean isCreateBeanEvent(PropertyChangeEvent evt) {
        return evt.getOldValue()== null && evt.getNewValue() instanceof CommonDDBean;
    }
    
    private boolean isVeiledRenameEvent(PropertyChangeEvent deleteEvent, PropertyChangeEvent createEvent) {
        // Check for different event source
        if(deleteEvent.getSource() != createEvent.getSource()) {
            return false;
        }
        
        // Check for different bean type
        if(!deleteEvent.getOldValue().getClass().equals(createEvent.getNewValue().getClass())) {
            return false;
        }
        
        // Check for different bean path
//        if(!makeXpath(deleteEvent.getPropertyName()).equals(makeXpath(createEvent.getPropertyName()))) {
        if(!xcompare(deleteEvent.getPropertyName(), createEvent.getPropertyName())) {
            return false;
        }
        
        // FIXME Should check bean properties (except for name).
        
        return true;
    }
    
    private boolean processAsChangeNameEvent(PropertyChangeEvent deleteEvent, PropertyChangeEvent createEvent) {
        boolean result = false;
        // swap old bean pointer for new bean.
        // change name field.
        CommonDDBean newBean = (CommonDDBean) createEvent.getNewValue();
        NameVisitor nameVisitor = getNameVisitor(newBean);
        if(nameVisitor != null) {
            CommonDDBean oldBean = (CommonDDBean) deleteEvent.getOldValue();
            String oldName = nameVisitor.getName(oldBean);
            String newName = nameVisitor.getName(newBean);
            
            // If names are not the same, assume this is a name change event.
            if(!Utils.strEquals(oldName, newName)) {
                PropertyChangeEvent changeEvent = new PropertyChangeEvent(newBean, createEvent.getPropertyName() + nameVisitor.getNameProperty(), oldName, newName);

//                System.out.println("processing delete/create sequence as change name event.");
                processEvent(changeEvent);
                result = true;
            }
        } else {
//            System.out.println("No support for delete/create sequence from type " + newBean.getClass().getSimpleName());
        }
        
        return result;
    }
    
    private void processEvent(PropertyChangeEvent evt) {
//        System.out.println("PROCESSED EVENT: " + evt.getPropertyName() + 
//                ", old = " + evt.getOldValue() + ", new = " + evt.getNewValue() + 
//                ", source = " + evt.getSource());

        String xpath = makeXpath(evt.getPropertyName());

        BeanVisitor visitor = handlerCache.get(xpath);
        if(visitor != null) {
            Object oldValue = evt.getOldValue();
            Object newValue = evt.getNewValue();

            if(oldValue == null) {
                if(newValue instanceof CommonDDBean) {
                    // !PW FIXME check type on getSource().
                    visitor.beanCreated(config, xpath, (CommonDDBean) evt.getSource(), (CommonDDBean) newValue);
                } else if(newValue != null) {
                    visitor.fieldCreated(config, xpath, (CommonDDBean) evt.getSource(), newValue);
                }
            } else if(newValue == null) {
                if(oldValue instanceof CommonDDBean) {
                    visitor.beanDeleted(config, xpath, (CommonDDBean) evt.getSource(), (CommonDDBean) oldValue);
                } else if(oldValue != null) {
                    visitor.fieldDeleted(config, xpath, (CommonDDBean) evt.getSource(), oldValue);
                }
            } else {
                if(oldValue instanceof CommonDDBean && newValue instanceof CommonDDBean) {
                    visitor.beanChanged(config, xpath, (CommonDDBean) evt.getSource(), (CommonDDBean) oldValue, (CommonDDBean) newValue);
                } else if(oldValue != null && newValue != null) {
                    visitor.fieldChanged(config, xpath, (CommonDDBean) evt.getSource(), oldValue, newValue);
                }
            }
        } 
//        else {
//            // FIXME performance could be better w/ some form of sorted set lookup.
//            int minKeyLength = xpath.length() + 1;
//            for(String key: handlerCache.keySet()) {
//                if(key.length() > minKeyLength && key.startsWith(xpath)) {
//                    // locate proper child bean(s) and fire correct event.
//                    String subKeyGroup = key.substring(minKeyLength);
//                    System.out.println("Child bean: " + subKeyGroup);
//                    
//                    String [] subKeys = subKeyGroup.split("/");
//
//                    if(subKeys != null && subKeys.length > 0) {
//                        try {
//                            
//                            Object oldValue = evt.getOldValue();
//                            Object newValue = evt.getNewValue();
//                            
//                            CommonDDBean sourceBean = null;
//                            boolean createEvent = true;
//                            if(oldValue == null && newValue instanceof CommonDDBean) {
//                                sourceBean = (CommonDDBean) newValue;
//                            } else if(newValue == null && oldValue instanceof CommonDDBean) {
//                                sourceBean = (CommonDDBean) oldValue;
//                                createEvent = false;
//                            }
//                            
//                            if(sourceBean != null) {
//                                for(String subKey: subKeys) {
//                                    Method subKeyGetter = sourceBean.getClass().getDeclaredMethod("get" + subKey, (Class) null);
//                                    if (subKeyGetter != null) {
//                                        Object beanCandidate = subKeyGetter.invoke(sourceBean, (Object) null);
////                                        if (beanCandidate instanceof CommonDDBean []) {
////                                            CommonDDBean [] beanArray = (CommonDDBean []) beanCandidate;
////                                            // handle each bean
////                                        } else if (beanCandidate instanceof CommonDDBean) {
////                                            CommonDDBean bean = (CommonDDBean) beanCandidate;
////                                        }
//                                        if (beanCandidate instanceof CommonDDBean) {
//                                            CommonDDBean [] tmp = new CommonDDBean[1];
//                                            tmp[0] = (CommonDDBean) beanCandidate;
//                                            beanCandidate = tmp;
//                                        }
//                                        
//                                        if (beanCandidate instanceof CommonDDBean []) {
//                                            CommonDDBean [] beanArray = (CommonDDBean []) beanCandidate;
//                                            // handle each bean
//                                            for(int i = 0; i < beanArray.length; i++) {
//                                                
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        } catch (IllegalAccessException ex) {
//                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                        } catch (IllegalArgumentException ex) {
//                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                        } catch (InvocationTargetException ex) {
//                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                        } catch (NoSuchMethodException ex) {
//                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                        } catch (SecurityException ex) {
//                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                        }
//                    }
//                }
//            }
//            
//        }
    }
    
    /** Convert a property event string into a pseudo absolute xpath.
     *  
     *  For example, 
     *      "/EjbJar/EnterpriseBeans/Session.12ei0" 
     *  is converted to 
     *      "/EjbJar/EnterpriseBeans/Session"
     * 
     *  The proper xpath would have been
     *      "/ejb-jar/enterprise-beans/session"
     * 
     *  However, by leaving the string mixed case with no hyphens, it's a little
     *  easier to convert back to property get/set methods, which we need to do
     *  for xpaths that don't have visitor mappings themselves, but contain
     *  children that do.
     * 
     *  For example:
     *      /Webservices contains /Webservices/WebserviceDescription/PortComponent
     */
    private String makeXpath(String ddpath) {
        StringBuilder xpathBuilder = new StringBuilder(ddpath.length());
        int len = ddpath.length();
        
        for(int i = 0; i < len; i++ ) {
            char c = ddpath.charAt(i);
            if(c == '.') {
                while(++i < len && (c = ddpath.charAt(i)) != '/') {
                    // skip these.
                }
            }
            if(i < len) {
                xpathBuilder.append(c);
            }
        }
        
        return xpathBuilder.toString();
    }
    
    /** Quickly compare two property event strings to see if they represent
     *  the same xpath -- requires ignoring any embedded object id's.
     * 
     *  For example, these two events should match, since they differ only by
     *  the id of the generating Session object.
     *    /EjbJar/EnterpriseBeans/Session.12ei0
     *    /EjbJar/EnterpriseBeans/Session.13f
     */
    private static boolean xcompare(String a, String b) {
        boolean result = true;
        int alen = a.length();
        int blen = b.length();
        int i = 0, j = 0;
        
        for(; i < alen && j < blen; i++, j++) {
            char aa = a.charAt(i);
            char bb = b.charAt(j);
            
            if(aa == bb) {
                if(aa == '.') {
                    // skip contents following dot for both strings.
                    boolean aslash = false, bslash = false;
                    
                    while(++i < alen) {
                        if(a.charAt(i) == '/') {
                            aslash = true;
                            break;
                        }
                    }
                    
                    while(++j < blen) {
                        if(b.charAt(j) == '/') {
                            bslash = true;
                            break;
                        }
                    }
                    
                    if(aslash != bslash) {
                        result = false;
                        break;
                    }
                }
            } else {
                result = false;
                break;
            }
        }
        
        if(result && (i < alen || j < blen)) {
            result = false;
        }
        
        return result;
    }    
    
    private static WeakHashMap<Class, WeakReference<NameVisitor>> visitorCache = 
            new WeakHashMap<Class, WeakReference<NameVisitor>>();
    
    public static synchronized NameVisitor getNameVisitor(CommonDDBean bean) {
        NameVisitor result = null;
        Class beanClass = bean.getClass();
        WeakReference<NameVisitor> ref = visitorCache.get(beanClass);
        if(ref != null) {
            result = ref.get();
        }
        if(result == null) {
            result = createNameVisitor(bean);
            if(result != null) {
                visitorCache.put(beanClass, new WeakReference<NameVisitor>(result));
            }
        }
        return result;
    }
    
    public static NameVisitor createNameVisitor(CommonDDBean bean) {
        NameVisitor result = null;
        if(bean instanceof Session) {
            result = new SessionBeanVisitor();
        } else if(bean instanceof MessageDriven) {
            result = new MDBeanVisitor();
        } else if(bean instanceof Entity) {
            result = new EntityBeanVisitor();
        } else if(bean instanceof CmpField) {
            result = new CmpFieldNameVisitor();
        } else if(bean instanceof EjbRef) {
            result = new EjbRefVisitor();
        } else if(bean instanceof MessageDestinationRef) {
            result = new MessageDestinationRefVisitor();
        } else if(bean instanceof ResourceEnvRef) {
            result = new ResourceEnvRefVisitor();
        } else if(bean instanceof ResourceRef) {
            result = new ResourceRefVisitor();
        } else if(bean instanceof ServiceRef) {
            result = new ServiceRefVisitor();
        } else if(bean instanceof MessageDestination) {
            result = new MessageDestinationVisitor();
        } else if(bean instanceof SecurityRole) {
            result = new SecurityRoleVisitor();
        } else if(bean instanceof PortComponent) {
            result = new PortComponentVisitor();
        } else if(bean instanceof PortComponentRef) {
            result = new PortComponentRefVisitor();
        }
        return result;
    }
    
    public static interface NameVisitor {
        public String getName(CommonDDBean bean);
        public String getNameProperty();
    }
    
    // Three types of ejbs
    public static class SessionBeanVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((Session) bean).getEjbName();
        }
        public String getNameProperty() {
            return "/" + Session.EJB_NAME;
        }
    }
    
    public static class MDBeanVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((MessageDriven) bean).getEjbName();
        }
        public String getNameProperty() {
            return "/" + MessageDriven.EJB_NAME;
        }
    }
    
    public static class EntityBeanVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((Entity) bean).getEjbName();
        }
        public String getNameProperty() {
            return "/" + Entity.EJB_NAME;
        }
    }
    
    public static class CmpFieldNameVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((CmpField) bean).getFieldName();
        }
        public String getNameProperty() {
            return "/" + CmpField.FIELD_NAME;
        }
    }

    // All the common reference types
    public static class EjbRefVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((EjbRef) bean).getEjbRefName();
        }
        public String getNameProperty() {
            return "/" + EjbRef.EJB_REF_NAME;
        }
    }
    
    public static class MessageDestinationRefVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((MessageDestinationRef) bean).getMessageDestinationRefName();
        }
        public String getNameProperty() {
            return "/" + MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME;
        }
    }
    
    public static class ResourceEnvRefVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((ResourceEnvRef) bean).getResourceEnvRefName();
        }
        public String getNameProperty() {
            return "/" + ResourceEnvRef.RESOURCE_ENV_REF_NAME;
        }
    }
    
    public static class ResourceRefVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((ResourceRef) bean).getResRefName();
        }
        public String getNameProperty() {
            return "/" + ResourceRef.RES_REF_NAME;
        }
    }
    
    public static class ServiceRefVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((ServiceRef) bean).getServiceRefName();
        }
        public String getNameProperty() {
            return "/" + ServiceRef.SERVICE_REF_NAME;
        }
    }
    
    public static class PortComponentRefVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((PortComponentRef) bean).getServiceEndpointInterface();
        }
        public String getNameProperty() {
            return "/" + PortComponentRef.SERVICE_ENDPOINT_INTERFACE;
        }
    }
    
    public static class PortComponentVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((PortComponent) bean).getPortComponentName();
        }
        public String getNameProperty() {
            return "/" + PortComponent.PORT_COMPONENT_NAME;
        }
    }
    
    // Message destination
    public static class MessageDestinationVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((MessageDestination) bean).getMessageDestinationName();
        }
        public String getNameProperty() {
            return "/" + MessageDestination.MESSAGE_DESTINATION_NAME;
        }
    }
    
    // Security
    public static class SecurityRoleVisitor implements NameVisitor {
        public String getName(CommonDDBean bean) {
            return ((SecurityRole) bean).getRoleName();
        }
        public String getNameProperty() {
            return "/" + SecurityRole.ROLE_NAME;
        }
    }

    private static Map<String, BeanVisitor> handlerCache = new HashMap<String, BeanVisitor>(37);
    
    static {
        EntityAndSessionVisitor entitySessionVistor = new EntityAndSessionVisitor();
        EntityVisitor entityVisitor = new EntityVisitor();
        CmpEntityVisitor cmpEntityVisitor = new CmpEntityVisitor();
        CmpFieldVisitor cmpFieldVisitor = new CmpFieldVisitor();
        EntityAndSessionRemoteVisitor entitySessionRemoteVistor = new EntityAndSessionRemoteVisitor();
        handlerCache.put("/EjbJar/EnterpriseBeans", entitySessionVistor);
        handlerCache.put("/EjbJar/EnterpriseBeans/Session", entitySessionVistor);
        handlerCache.put("/EjbJar/EnterpriseBeans/Session/Remote", entitySessionRemoteVistor);
        handlerCache.put("/EjbJar/EnterpriseBeans/Entity", entityVisitor);
        handlerCache.put("/EjbJar/EnterpriseBeans/Entity/EjbName", cmpEntityVisitor);
        handlerCache.put("/EjbJar/EnterpriseBeans/Entity/CmpField", cmpFieldVisitor);
        handlerCache.put("/EjbJar/EnterpriseBeans/Entity/CmpField/FieldName", cmpFieldVisitor);
        handlerCache.put("/EjbJar/EnterpriseBeans/Entity/Remote", entitySessionRemoteVistor);
//        handlerCache.put("/EjbJar/EnterpriseBeans/MessageDriven", new MessageDrivenVisitor());

        WebserviceDescriptionBeanVisitor wsDescVisitor = new WebserviceDescriptionBeanVisitor();
        handlerCache.put("/Webservices/WebserviceDescription", wsDescVisitor);
        handlerCache.put("/Webservices/WebserviceDescription/PortComponent", wsDescVisitor);
    }
    
    public static interface BeanVisitor {
        public void beanCreated(SunONEDeploymentConfiguration config, String xpath, 
                CommonDDBean sourceDD, CommonDDBean newDD);
        public void beanDeleted(SunONEDeploymentConfiguration config, String xpath, 
                CommonDDBean sourceDD, CommonDDBean oldDD);
        public void beanChanged(SunONEDeploymentConfiguration config, String xpath, 
                CommonDDBean sourceDD, CommonDDBean oldDD, CommonDDBean newDD);
        public void fieldCreated(SunONEDeploymentConfiguration config, String xpath, 
                Object sourceDD, Object newValue);
        public void fieldDeleted(SunONEDeploymentConfiguration config, String xpath, 
                Object sourceDD, Object oldValue);
        public void fieldChanged(SunONEDeploymentConfiguration config, String xpath, 
                Object sourceDD, Object oldValue, Object newValue);
    }
    
    public static abstract class AbstractBeanVisitor implements BeanVisitor {
        public void beanCreated(SunONEDeploymentConfiguration config, String xpath, 
                CommonDDBean sourceDD, CommonDDBean newDD) {
        }
        public void beanDeleted(SunONEDeploymentConfiguration config, String xpath, 
                CommonDDBean sourceDD, CommonDDBean oldDD) {
        }
        public void beanChanged(SunONEDeploymentConfiguration config, String xpath, 
                CommonDDBean sourceDD, CommonDDBean oldDD, CommonDDBean newDD) {
        }
        public void fieldCreated(SunONEDeploymentConfiguration config, String xpath, 
                Object sourceDD, Object newValue) {
        }
        public void fieldDeleted(SunONEDeploymentConfiguration config, String xpath, 
                Object sourceDD, Object oldValue) {
        }
        public void fieldChanged(SunONEDeploymentConfiguration config, String xpath, 
                Object sourceDD, Object oldValue, Object newValue) {
        }
    }

    public static final class WebserviceDescriptionBeanVisitor extends AbstractBeanVisitor {
        
        @Override
        public void beanCreated(final SunONEDeploymentConfiguration config, final String xpath, 
                final CommonDDBean sourceDD, final CommonDDBean newDD) {
            if(newDD instanceof WebserviceDescription) {
                webserviceDescriptionUpdated(config, (WebserviceDescription) newDD, 
                        SunONEDeploymentConfiguration.ChangeOperation.CREATE);
            } else if(newDD instanceof PortComponent) {
                portComponentUpdated(config, (PortComponent) newDD, 
                        SunONEDeploymentConfiguration.ChangeOperation.CREATE);
            }
        }

        @Override
        public void beanDeleted(final SunONEDeploymentConfiguration config, final String xpath, 
                final CommonDDBean sourceDD, final CommonDDBean oldDD) {
            if(oldDD instanceof WebserviceDescription) {
                webserviceDescriptionUpdated(config, (WebserviceDescription) oldDD, 
                        SunONEDeploymentConfiguration.ChangeOperation.DELETE);
            } else if(oldDD instanceof PortComponent) {
                portComponentUpdated(config, (PortComponent) oldDD, 
                        SunONEDeploymentConfiguration.ChangeOperation.DELETE);
            }
        }

        private void webserviceDescriptionUpdated(final SunONEDeploymentConfiguration config, 
                final WebserviceDescription wsDescDD, final SunONEDeploymentConfiguration.ChangeOperation operation) {
            PortComponent [] portDDs = wsDescDD.getPortComponent();
            
            if(portDDs != null && portDDs.length > 0) {
                for(PortComponent portDD : portDDs) {
                    if(portDD != null) {
                        portComponentUpdated(config, portDD, operation);
                    }
                }
            }
        }
        
        private void portComponentUpdated(final SunONEDeploymentConfiguration config, 
                final PortComponent portDD, final SunONEDeploymentConfiguration.ChangeOperation operation) {
            String portName = portDD.getPortComponentName();
            String linkName = getLinkName(portDD);

            if(Utils.notEmpty(portName) && Utils.notEmpty(linkName)) {
                config.updateDefaultEjbEndpointUri(linkName, portName, operation);
            }
        }        
        
        private String getLinkName(final PortComponent portDD) {
            String result = null;
            ServiceImplBean sib = portDD.getServiceImplBean();
            if(sib != null) {
                result = sib.getServletLink();
                if(result == null) {
                    result = sib.getEjbLink();
                }
            }
            return result;
        }
        
    }
    
    public static class EntityAndSessionVisitor extends AbstractBeanVisitor {

        @Override
        public void beanCreated(final SunONEDeploymentConfiguration config, final String xpath, 
                final CommonDDBean sourceDD, final CommonDDBean newDD) {
            if(newDD instanceof EntityAndSession) {
                entitySessionUpdated(config, (EntityAndSession) newDD, 
                        SunONEDeploymentConfiguration.ChangeOperation.CREATE);
            } else if(newDD instanceof EnterpriseBeans) {
                enterpriseBeansUpdated(config, (EnterpriseBeans) newDD, 
                        SunONEDeploymentConfiguration.ChangeOperation.CREATE);
            }
        }
        
        @Override
        public void beanDeleted(SunONEDeploymentConfiguration config, String xpath, CommonDDBean sourceDD, CommonDDBean oldDD) {
            if(oldDD instanceof EntityAndSession) {
                entitySessionUpdated(config, (EntityAndSession) oldDD, 
                        SunONEDeploymentConfiguration.ChangeOperation.DELETE);
            } else if(oldDD instanceof EnterpriseBeans) {
                enterpriseBeansUpdated(config, (EnterpriseBeans) oldDD, 
                        SunONEDeploymentConfiguration.ChangeOperation.DELETE);
            }
        }
        
        private void enterpriseBeansUpdated(final SunONEDeploymentConfiguration config, 
                final EnterpriseBeans ebDD, final SunONEDeploymentConfiguration.ChangeOperation operation) {
            Session [] sessionDDs = ebDD.getSession();
            if(sessionDDs != null && sessionDDs.length > 0) {
                for(Session sessionDD : sessionDDs) {
                    if(sessionDD != null) {
                        entitySessionUpdated(config, sessionDD, operation);
                    }
                }
            }
            Entity [] entityDDs = ebDD.getEntity();
            if(entityDDs != null && entityDDs.length > 0) {
                for(Entity entityDD : entityDDs) {
                    if(entityDD != null) {
                        entitySessionUpdated(config, entityDD, operation);
                    }
                }
            }
        }
        
        private void entitySessionUpdated(final SunONEDeploymentConfiguration config, 
                final EntityAndSession ejbDD, final SunONEDeploymentConfiguration.ChangeOperation operation) {
            String ejbName = ejbDD.getEjbName();
            String remote = ejbDD.getRemote();
            
            if(Utils.notEmpty(ejbName) && 
                    (operation == SunONEDeploymentConfiguration.ChangeOperation.DELETE || Utils.notEmpty(remote))) {
                config.updateDefaultEjbJndiName(ejbName, "ejb/", operation);
            }
         }

    }

    private static boolean isCMP(Object ddBean) {
        if(ddBean instanceof Entity) {
            Entity entity = (Entity)ddBean;
            if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(entity.getPersistenceType())) {
                return true;
            }
        }
        return false;
    }

    public static final class CmpFieldVisitor extends AbstractBeanVisitor {
        @Override
        public void beanDeleted(SunONEDeploymentConfiguration config, String xpath, CommonDDBean sourceDD, CommonDDBean oldDD) {
            super.beanDeleted(config, xpath, sourceDD, oldDD);
            if(isCMP(sourceDD)) {
                String ejbName = ((Entity)sourceDD).getEjbName();

                if (Utils.notEmpty(ejbName) && (oldDD instanceof CmpField)) {
                    String fieldName = ((CmpField)oldDD).getFieldName();
                    if (Utils.notEmpty(fieldName)) {
                        config.removeMappingForCmpField(ejbName, fieldName);
                    }
                }
            }
        }
        @Override
        public void fieldChanged(SunONEDeploymentConfiguration config, String xpath, 
                Object sourceDD, Object oldValue, Object newValue) {
            super.fieldChanged(config, xpath, sourceDD, oldValue, newValue);
            if(isCMP(sourceDD)) {
                String ejbName = ((Entity)sourceDD).getEjbName();
                String oldFieldName = oldValue.toString();
                String newFieldName = newValue.toString();

                if (Utils.notEmpty(oldFieldName) && Utils.notEmpty(newFieldName) && 
                        !(oldFieldName.equals(newFieldName))) {
                    config.renameMappingForCmpField(ejbName, oldFieldName, newFieldName);
                }
            }
        }
    }
    public static final class CmpEntityVisitor extends AbstractBeanVisitor {
        @Override
        public void fieldChanged(SunONEDeploymentConfiguration config, String xpath, 
                Object sourceDD, Object oldValue, Object newValue) {
            super.fieldChanged(config, xpath, sourceDD, oldValue, newValue);
            if(isCMP(sourceDD)) {
                String oldEjbName = oldValue.toString();
                String newEjbName = newValue.toString();

                if (Utils.notEmpty(oldEjbName) && Utils.notEmpty(newEjbName) && 
                        !(oldEjbName.equals(newEjbName))) {
                    config.renameMappingForCmp(oldEjbName, newEjbName);
                }
            }
        }
    }
    public static final class EntityVisitor extends EntityAndSessionVisitor {
        
        @Override
        public void beanDeleted(SunONEDeploymentConfiguration config, String xpath, CommonDDBean sourceDD, CommonDDBean oldDD) {
            super.beanDeleted(config, xpath, sourceDD, oldDD);
            if(isCMP(oldDD)) {
                String ejbName = ((Entity)oldDD).getEjbName();
                if(Utils.notEmpty(ejbName)) {
                    config.removeMappingForCmp(ejbName);
                }
            }
        }
    }

    public static final class EntityAndSessionRemoteVisitor extends AbstractBeanVisitor {

        @Override
        public void fieldCreated(SunONEDeploymentConfiguration config, String xpath, Object sourceDD, Object newValue) {
            remoteFieldUpdated(config, (EntityAndSession) sourceDD, (String) newValue, 
                    SunONEDeploymentConfiguration.ChangeOperation.CREATE);
        }

        @Override
        public void fieldDeleted(SunONEDeploymentConfiguration config, String xpath, Object sourceDD, Object newValue) {
            remoteFieldUpdated(config, (EntityAndSession) sourceDD, null, 
                    SunONEDeploymentConfiguration.ChangeOperation.DELETE);
        }
        
        private void remoteFieldUpdated(SunONEDeploymentConfiguration config, EntityAndSession ejbDD, 
                String remote, SunONEDeploymentConfiguration.ChangeOperation operation) {
            String ejbName = ejbDD.getEjbName();
            
            if(Utils.notEmpty(ejbName) && 
                    (operation == SunONEDeploymentConfiguration.ChangeOperation.DELETE || Utils.notEmpty(remote))) {
                config.updateDefaultEjbJndiName(ejbName, "ejb/", operation);
            }
        }
        
    }
    
    public static final class MessageDrivenVisitor extends AbstractBeanVisitor {
        
        @Override
        public void beanCreated(SunONEDeploymentConfiguration config, String xpath, CommonDDBean sourceDD, CommonDDBean newDD) {
            mdbUpdated(config, (MessageDriven) newDD, SunONEDeploymentConfiguration.ChangeOperation.CREATE);
        }

        @Override
        public void beanDeleted(SunONEDeploymentConfiguration config, String xpath, CommonDDBean sourceDD, CommonDDBean oldDD) {
            mdbUpdated(config, (MessageDriven) oldDD, SunONEDeploymentConfiguration.ChangeOperation.DELETE);
        }
        
        private void mdbUpdated(SunONEDeploymentConfiguration config, MessageDriven mdbDD, 
                SunONEDeploymentConfiguration.ChangeOperation operation) {
            String ejbName = mdbDD.getEjbName();
            
            if(Utils.notEmpty(ejbName)) {
                config.updateDefaultEjbJndiName(ejbName, "jms/", operation);
            }
        }
        
    }

}
