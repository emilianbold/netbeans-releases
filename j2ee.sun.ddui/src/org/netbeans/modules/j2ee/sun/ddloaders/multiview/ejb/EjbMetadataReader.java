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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;

/**
 *
 * @author Peter Williams
 */
public class EjbMetadataReader implements MetadataModelAction<EjbJarMetadata, Map<String, Object>> {

    /** Entry point to generate map from standard descriptor
     */
    public static Map<String, Object> readDescriptor(EjbJar ejbJar) {
        return genProperties(ejbJar);
    }
    
    /** Entry point to generate map from annotation metadata
     */
    public Map<String, Object> run(EjbJarMetadata metadata) throws Exception {
        return genProperties(metadata.getRoot());
    }
    
    /** Maps interesting fields from ejb-jar descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    private static Map<String, Object> genProperties(EjbJar ejbJar) {
        Map<String, Object> data = new HashMap<String, Object>();
        EnterpriseBeans eb = ejbJar.getEnterpriseBeans();
        if(eb != null) {
            Session [] sessionBeans = eb.getSession();
            if(sessionBeans != null) {
                for(Session session: sessionBeans) {
                    String ejbName = session.getEjbName();
                    if(Utils.notEmpty(ejbName)) {
                        Map<String, Object> sessionMap = new HashMap<String, Object>();
                        data.put(ejbName, sessionMap);
                        sessionMap.put(DDBinding.PROP_NAME, ejbName);
                        
                        String sessionType = session.getSessionType();
                        if(sessionType != null && sessionType.length() > 0) {
                            sessionMap.put(DDBinding.PROP_SESSION_TYPE, sessionType);
                        }
                    }
                }
            }
            MessageDriven [] messageBeans = eb.getMessageDriven();
            if(messageBeans != null) {
                for(MessageDriven message: messageBeans) {
                    String ejbName = message.getEjbName();
                    if(Utils.notEmpty(ejbName)) {
                        Map<String, Object> messageMap = new HashMap<String, Object>();
                        data.put(ejbName, messageMap);
                        messageMap.put(DDBinding.PROP_NAME, ejbName);
                        
//                        ActivationConfig config = message.getActivationConfig();
//                        if(config != null) {
//                            ActivationConfigProperty [] properties = config.getActivationConfigProperty();
//                            if(properties != null) {
//                                for(ActivationConfigProperty property: properties) {
//                                    String name = property.getActivationConfigPropertyName();
//                                    if("destinationType".equals(name)) {
//                                        String destinationType = property.getActivationConfigPropertyValue();
//                                        messageMap.put("DestinationType", destinationType);
//                                    }
//                                }
//                            }
//                        }
                    }
                }
            }
            Entity [] entityBeans = eb.getEntity();
            if(entityBeans != null) {
                for(Entity entity: entityBeans) {
                    String ejbName = entity.getEjbName();
                    if(Utils.notEmpty(ejbName)) {
                        Map<String, Object> entityMap = new HashMap<String, Object>();
                        data.put(ejbName, entityMap);
                        entityMap.put(DDBinding.PROP_NAME, ejbName);
                        
                        String persistenceType = entity.getPersistenceType();
                        if(persistenceType != null && persistenceType.length() > 0) {
                            entityMap.put(DDBinding.PROP_PERSISTENCE_TYPE, persistenceType);
                        }
                    }
                }
            }
        }
        
        return data.size() > 0 ? data : null;
    }

}
