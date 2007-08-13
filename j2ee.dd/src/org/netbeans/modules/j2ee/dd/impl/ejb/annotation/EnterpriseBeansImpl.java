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
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner;

public class EnterpriseBeansImpl implements EnterpriseBeans {

    private final AnnotationModelHelper helper;

    public EnterpriseBeansImpl(AnnotationModelHelper helper) {
        this.helper = helper;
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
        final List<Session> result = new ArrayList<Session>();
        try {
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
        } catch (InterruptedException e) {
            return new Session[0];
        }
        return result.toArray(new Session[result.size()]);
    }

    public MessageDriven[] getMessageDriven() {
        final List<MessageDriven> result = new ArrayList<MessageDriven>();
        try {
            helper.getAnnotationScanner().findAnnotations("javax.ejb.MessageDriven", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() { // NOI18N
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    result.add(new MessageDrivenImpl(helper, type));
                }
            });
        } catch (InterruptedException e) {
            return new MessageDriven[0];
        }
        return result.toArray(new MessageDriven[result.size()]);
    }

    public Entity[] getEntity() {
        return new Entity[0];
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

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
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
