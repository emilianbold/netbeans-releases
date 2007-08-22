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

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;

public class EnterpriseBeansImpl implements EnterpriseBeans {

    private final AnnotationModelHelper helper;
    private PropertyChangeSupport propChangeSupport;
    private PersistentObjectManager<SessionImpl> sessionManager;
    private PersistentObjectManager<MessageDrivenImpl> messageDrivenManager;

    private EnterpriseBeansImpl(AnnotationModelHelper helper) {
        this.helper = helper;
    }
    
    public static EnterpriseBeansImpl create(AnnotationModelHelper helper) {
        EnterpriseBeansImpl instance = new EnterpriseBeansImpl(helper);
        instance.initialize();
        return instance;
    }
    
    /**
     * Initializing outside the constructor to avoid escaping "this" from
     * the constructor.
     */
    private void initialize() {
        sessionManager = helper.createPersistentObjectManager(new SessionProvider());
        messageDrivenManager = helper.createPersistentObjectManager(new MessageDrivenProvider());
        propChangeSupport = new PropertyChangeSupport(this);
        sessionManager.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                // TODO: which path should be used? is it needed at all?
                propChangeSupport.firePropertyChange("/EnterpriseBeans/Session", null, null); // NOI18N
            }
        });
        messageDrivenManager.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                // TODO: which path should be used? is it needed at all?
                propChangeSupport.firePropertyChange("/EnterpriseBeans/MessageDriven", null, null); // NOI18N
            }
        });
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propChangeSupport.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propChangeSupport.removePropertyChangeListener(pcl);
    }

    // <editor-fold desc="Model implementation">

    public Ejb[] getEjbs() {
        Session[] sessions = getSession();
        MessageDriven[] messageDrivens = getMessageDriven();
        Ejb[] result = new Ejb[sessions.length + messageDrivens.length];
        System.arraycopy(sessions, 0, result, 0, sessions.length);
        System.arraycopy(messageDrivens, 0, result, sessions.length, messageDrivens.length);
        return result;
    }

    public Session[] getSession() {
        Collection<SessionImpl> sessions = sessionManager.getObjects();
        return sessions.toArray(new Session[sessions.size()]);
    }

    public MessageDriven[] getMessageDriven() {
        Collection<MessageDrivenImpl> messageDrivens = messageDrivenManager.getObjects();
        return messageDrivens.toArray(new MessageDriven[messageDrivens.size()]);
    }

    public Entity[] getEntity() {
        return new Entity[0];
    }

    private final class SessionProvider implements ObjectProvider<SessionImpl> {

        public List<SessionImpl> createInitialObjects() throws InterruptedException {
            final List<SessionImpl> result = new ArrayList<SessionImpl>();
            helper.getAnnotationScanner().findAnnotations("javax.ejb.Stateless", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() { // NOI18N
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    result.add(new SessionImpl(SessionImpl.Kind.STATELESS, helper, type));
                }
            });
            helper.getAnnotationScanner().findAnnotations("javax.ejb.Stateful", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() { // NOI18N
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    result.add(new SessionImpl(SessionImpl.Kind.STATEFUL, helper, type));
                }
            });
            return result;
        }

        public List<SessionImpl> createObjects(TypeElement type) {
            final List<SessionImpl> result = new ArrayList<SessionImpl>();
            if (helper.hasAnnotation(type.getAnnotationMirrors(), "javax.ejb.Stateless")) { // NOI18N
                result.add(new SessionImpl(SessionImpl.Kind.STATELESS, helper, type));
            }
            if (helper.hasAnnotation(type.getAnnotationMirrors(), "javax.ejb.Stateful")) { // NOI18N
                result.add(new SessionImpl(SessionImpl.Kind.STATEFUL, helper, type));
            }
            return result;
        }

        public boolean modifyObjects(TypeElement type, List<SessionImpl> objects) {
            assert objects.size() == 1;
            SessionImpl session = objects.get(0);
            if (!session.refresh(type)) {
                objects.remove(0);
                return true;
            }
            return false;
        }
    }

    private final class MessageDrivenProvider implements ObjectProvider<MessageDrivenImpl> {

        public List<MessageDrivenImpl> createInitialObjects() throws InterruptedException {
            final List<MessageDrivenImpl> result = new ArrayList<MessageDrivenImpl>();
            helper.getAnnotationScanner().findAnnotations("javax.ejb.MessageDriven", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() { // NOI18N
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    result.add(new MessageDrivenImpl(helper, type));
                }
            });
            return result;
        }

        public List<MessageDrivenImpl> createObjects(TypeElement type) {
            final List<MessageDrivenImpl> result = new ArrayList<MessageDrivenImpl>();
            if (helper.hasAnnotation(type.getAnnotationMirrors(), "javax.ejb.MessageDriven")) { // NOI18N
                result.add(new MessageDrivenImpl(helper, type));
            }
            return result;
        }

        public boolean modifyObjects(TypeElement type, List<MessageDrivenImpl> objects) {
            assert objects.size() == 1;
            MessageDrivenImpl messageDriven = objects.get(0);
            if (!messageDriven.refresh(type)) {
                objects.remove(0);
                return true;
            }
            return false;
        }
    }

// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">
    
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setSession(int index, Session value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSession(Session[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Session getSession(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addSession(Session value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeSession(Session value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeSession() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Session newSession() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEntity(int index, Entity value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEntity(Entity[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Entity getEntity(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeEntity(Entity value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addEntity(Entity value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeEntity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Entity newEntity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDriven(int index, MessageDriven value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDriven getMessageDriven(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDriven(MessageDriven[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addMessageDriven(MessageDriven value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeMessageDriven() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeMessageDriven(MessageDriven value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDriven newMessageDriven() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeEjb(Ejb value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean findBeanByName(String beanName, String propertyName,
                                       String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // </editor-fold>

}
