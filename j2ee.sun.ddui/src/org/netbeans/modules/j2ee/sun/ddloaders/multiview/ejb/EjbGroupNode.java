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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class EjbGroupNode extends NamedBeanGroupNode {

    private SunEjbJar sunEjbJar;
    
    public EjbGroupNode(SectionNodeView sectionNodeView, SunEjbJar sunEjbJar, ASDDVersion version) {
        super(sectionNodeView, sunEjbJar, Ejb.EJB_NAME, 
                NbBundle.getMessage(EjbGroupNode.class, "LBL_EjbGroupHeader"), // NOI18N
                ICON_EJB_GROUP_NODE, version);
        
        this.sunEjbJar = sunEjbJar;
        enableAddAction(NbBundle.getMessage(EjbGroupNode.class, "LBL_AddEjb")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new EjbNode(getSectionNodeView(), binding, version);
    }
    
    protected CommonDDBean [] getBeansFromModel() {
        EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
        if(eb != null) {
            return eb.getEjb();
        }
        return null;
    }

    protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] getStandardBeansFromModel() {
        org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] emptyList = new org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [0];
        org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] stdBeans = emptyList;
        org.netbeans.modules.j2ee.dd.api.common.RootInterface stdRootDD = getStandardRootDD();
        
        if(stdRootDD instanceof org.netbeans.modules.j2ee.dd.api.ejb.EjbJar) {
            org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = (org.netbeans.modules.j2ee.dd.api.ejb.EjbJar) stdRootDD;
            org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans eb = ejbJar.getEnterpriseBeans();
            if(eb != null) {
                // !PW FIXME how to differentiate between session, mdb, and entity beans?
                org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] session = eb.getSession();
                session = (session != null) ? session : emptyList;
                org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] mdb = eb.getMessageDriven();
                mdb = (mdb != null) ? mdb : emptyList;
                org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] entity = eb.getEntity();
                entity = (entity != null) ? entity : emptyList;
                
                stdBeans = new org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [session.length + mdb.length + entity.length];
                System.arraycopy(session, 0, stdBeans, 0, session.length);
                System.arraycopy(mdb, 0, stdBeans, session.length, mdb.length);
                System.arraycopy(entity, 0, stdBeans, session.length + mdb.length, entity.length);
            }
        }
        return stdBeans;
    }
    
    protected CommonDDBean addNewBean() {
        Ejb newEjb = (Ejb) createBean();
        newEjb.setEjbName("ejb" + getNewBeanId()); // NOI18N
        return addBean(newEjb);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
        if(eb == null) {
            eb = sunEjbJar.newEnterpriseBeans();
            sunEjbJar.setEnterpriseBeans(eb);
        }
        eb.addEjb((Ejb) newBean);
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
        if(eb != null) {
            Ejb ejb = (Ejb) bean;
            eb.removeEjb(ejb);
            // TODO if eb is empty of all data now, we could remove it too.
        }
    }

    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    private volatile EnterpriseBeans ejbFactory = null;
    
    public CommonDDBean createBean() {
        if(ejbFactory == null) {
            ejbFactory = sunEjbJar.newEnterpriseBeans();
        }
        return ejbFactory.newEjb();
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((Ejb) sunBean).getEjbName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((Ejb) sunBean).setEjbName(newName);
    }

    public String getSunBeanNameProperty() {
        return Ejb.EJB_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.ejb.Ejb) standardBean).getEjbName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_EJB_NAME;
    }
    
//    // ------------------------------------------------------------------------
//    // Metadata access    
//    // ------------------------------------------------------------------------
//    private volatile List<String> xpathMap;
//    
//    @Override
//    public List<XPathNode> getAnnotationModel() {
//        List<XPathNode> result = null;
//        
//        try {
//            MetadataModel<EjbJarMetadata> ejbJarModel = getMetadataModel(EjbJarMetadata.class);
//            if(ejbJarModel != null) {
//                result = ejbJarModel.runReadAction(new EjbMetadataMgr());
//            }
//        } catch (MetadataModelException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        } catch (IOException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        }
//
//        return result;
//    }
    
//    public List<org.netbeans.modules.j2ee.dd.api.ejb.Ejb> getEjbMetadataModel() {
//        List<org.netbeans.modules.j2ee.dd.api.ejb.Ejb> result = Collections.EMPTY_LIST;
//
//        try {
//            MetadataModel<EjbJarMetadata> ejbJarModel = getMetadataModel(EjbJarMetadata.class);
//            if(ejbJarModel != null) {
//                result = ejbJarModel.runReadAction(new MetadataModelAction<EjbJarMetadata, List<org.netbeans.modules.j2ee.dd.api.ejb.Ejb>>() {
//                    public List<org.netbeans.modules.j2ee.dd.api.ejb.Ejb> run(EjbJarMetadata metadata) throws Exception {
//                        List<org.netbeans.modules.j2ee.dd.api.ejb.Ejb> ejbList = new ArrayList<org.netbeans.modules.j2ee.dd.api.ejb.Ejb>();
//                        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = metadata.getRoot();
//                        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans eb = ejbJar.getEnterpriseBeans();
//                        if(eb != null) {
//                            org.netbeans.modules.j2ee.dd.api.ejb.Session [] sessionBeans = eb.getSession();
//                            if(sessionBeans != null) {
//                                for(org.netbeans.modules.j2ee.dd.api.ejb.Session ejb : sessionBeans) {
//                                    if(Utils.notEmpty(ejb.getEjbName())) {
//                                        org.netbeans.modules.j2ee.dd.api.ejb.Session ejbCopy = 
//                                                (org.netbeans.modules.j2ee.dd.api.ejb.Session) ejbJar.createBean("Session");
//                                        ejbCopy.setEjbName(ejb.getEjbName());
//                                        ejbList.add(ejbCopy);
//                                    }
//                                }
//                            }
//                            org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven [] messageBeans = eb.getMessageDriven();
//                            if(messageBeans != null) {
//                                for(org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven ejb : messageBeans) {
//                                    if(Utils.notEmpty(ejb.getEjbName())) {
//                                        org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven ejbCopy = 
//                                                (org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven) ejbJar.createBean("MessageDriven");
//                                        ejbCopy.setEjbName(ejb.getEjbName());
//                                        ejbList.add(ejbCopy);
//                                    }
//                                }
//                            }
//                        }
//                        return ejbList;
//                    }
//                }); 
//            }
//        } catch (MetadataModelException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        } catch (IOException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        }
//
//        return result;
//    }
    
}