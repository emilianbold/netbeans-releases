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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.sun.ddloaders;

import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.appclient.SunAppClientOverviewMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.EnvironmentMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.SecurityRoleMappingMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.EjbMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.ServletMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.web.SunWebOverviewMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.SunEjbOverviewMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.JmsMultiViewElement;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.WebServiceMultiViewElement;
import org.netbeans.modules.j2ee.sun.share.configbean.J2EEBaseVersion;
import org.netbeans.modules.j2ee.sun.share.configbean.J2EEVersion;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Peter Williams
 */
public abstract class DDViewFactory implements Serializable {
    
    private static final long serialVersionUID = -8759598009819101743L;
    
    // View names
    public static final String OVERVIEW = "Overview"; // NOI18N
    public static final String SERVLET = "Servlet"; // NOI18N
    public static final String EJB = "EJB"; // NOI18N
    public static final String SECURITY = "Security"; // NOI18N
    public static final String WSSERVICE = "WSService"; // NOI18N
    public static final String JMS = "JMS"; // NOI18N
    public static final String ENVIRONMENT = "Environment"; // NOI18N
    
    private static transient Map<DDType, DDViewFactory> factoryMap = new HashMap<DDType, DDViewFactory>();
    
    public static DDViewFactory getViewFactory(DDType type) {
        DDViewFactory factory = null;
        
        synchronized (factoryMap) {
            factory = factoryMap.get(type);
            if(factory == null) {
                factory = type.createViewFactory();
                factoryMap.put(type, factory);
            }
        }
        
        return factory;
    }
    
    public abstract DesignMultiViewDesc[] getMultiViewDesc(SunDescriptorDataObject dataObject);
    
    public MultiViewElement createElement(SunDescriptorDataObject dataObject, final String name) {
        if(name.equals(SECURITY)) {
            return new SecurityRoleMappingMultiViewElement(dataObject);
        } else if(name.equals(ENVIRONMENT)) {
            return new EnvironmentMultiViewElement(dataObject);
        } else if(name.equals(WSSERVICE)) {
            return new WebServiceMultiViewElement(dataObject);
        } else if(name.equals(JMS)) {
            return new JmsMultiViewElement(dataObject);
        }
        return null;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
    
    /** View factory for sun-web.xml specific views
     */
    public static class SunWebDDViewFactory extends DDViewFactory {
        
        private static final long serialVersionUID = -8759598009819101745L;
        
        public DesignMultiViewDesc[] getMultiViewDesc(SunDescriptorDataObject dataObject) {
            ASDDVersion asDDVersion = dataObject.getASDDVersion();
            
            List<DDView> views = new ArrayList<DDView>(8);
            views.add(new DDView(dataObject, OVERVIEW));
            views.add(new DDView(dataObject, SERVLET));
            views.add(new DDView(dataObject, SECURITY));
            if(ASDDVersion.SUN_APPSERVER_8_0.compareTo(asDDVersion) <= 0) {
                J2EEBaseVersion j2eeVersion = dataObject.getJ2eeModuleVersion();
                if(j2eeVersion == null || j2eeVersion.compareSpecification(J2EEVersion.J2EE_1_4) >= 0) {
                    views.add(new DDView(dataObject, WSSERVICE));
                    views.add(new DDView(dataObject, JMS));
                }
            }
            views.add(new DDView(dataObject, ENVIRONMENT));
            return views.toArray(new DDView[views.size()]);
        }
        
        @Override
        public MultiViewElement createElement(SunDescriptorDataObject dataObject, final String name) {
            if(name.equals(OVERVIEW)) {
                return new SunWebOverviewMultiViewElement(dataObject);
            } else if(name.equals(SERVLET)) {
                return new ServletMultiViewElement(dataObject);
            }
            
            return super.createElement(dataObject, name);
        }
        
    }
    
    
    /** View factory for sun-ejb-jar.xml specific views
     */
    public static class SunEjbJarDDViewFactory extends DDViewFactory {
        
        private static final long serialVersionUID = -8759598009819101747L;
        
        public DesignMultiViewDesc[] getMultiViewDesc(SunDescriptorDataObject dataObject) {
            // TODO complete set of sun-ejb-jar.xml multiview panels.
            ASDDVersion asDDVersion = dataObject.getASDDVersion();
            
            List<DDView> views = new ArrayList<DDView>(6);
            views.add(new DDView(dataObject, OVERVIEW));
            views.add(new DDView(dataObject, EJB));
            views.add(new DDView(dataObject, SECURITY));
            if(ASDDVersion.SUN_APPSERVER_8_0.compareTo(asDDVersion) <= 0) {
                J2EEBaseVersion j2eeVersion = dataObject.getJ2eeModuleVersion();
                if(j2eeVersion == null || j2eeVersion.compareSpecification(J2EEVersion.J2EE_1_4) >= 0) {
                    views.add(new DDView(dataObject, WSSERVICE));
                    views.add(new DDView(dataObject, JMS));
                }
            }
            return views.toArray(new DDView[views.size()]);
        }
        
        @Override
        public MultiViewElement createElement(SunDescriptorDataObject dataObject, final String name) {
            if(name.equals(OVERVIEW)) {
                return new SunEjbOverviewMultiViewElement(dataObject);
            } else if(name.equals(EJB)) {
                return new EjbMultiViewElement(dataObject);
            }
            
            return super.createElement(dataObject, name);
        }
        
    }
    
    
    /** View factory for sun-application.xml specific views
     */
    public static class SunApplicationDDViewFactory extends DDViewFactory {
        
        private static final long serialVersionUID = -8759598009819101749L;
        
        public DesignMultiViewDesc[] getMultiViewDesc(SunDescriptorDataObject dataObject) {
            // TODO complete set of sun-application.xml multiview panels.
            return new DDView[] {
                new DDView(dataObject, SECURITY)
            };
        }
        
        @Override
        public MultiViewElement createElement(SunDescriptorDataObject dataObject, final String name) {
//            if(name.equals(OVERVIEW)) {
//                return new SunApplicationGeneralMultiViewElement(dataObject);
//            }
            
            return super.createElement(dataObject, name);
        }
        
    }
    
    
    /** View factory for sun-application-client.xml specific views
     */
    public static class SunAppClientDDViewFactory extends DDViewFactory {
        
        private static final long serialVersionUID = -8759598009819101751L;
        
        public DesignMultiViewDesc[] getMultiViewDesc(SunDescriptorDataObject dataObject) {
            ASDDVersion asDDVersion = dataObject.getASDDVersion();
            
            List<DDView> views = new ArrayList<DDView>();
            if(ASDDVersion.SUN_APPSERVER_9_0.compareTo(asDDVersion) <= 0) {
                views.add(new DDView(dataObject, OVERVIEW));
            }
            views.add(new DDView(dataObject, ENVIRONMENT));
            if(ASDDVersion.SUN_APPSERVER_8_0.compareTo(asDDVersion) <= 0) {
                J2EEBaseVersion j2eeVersion = dataObject.getJ2eeModuleVersion();
                if(j2eeVersion == null || j2eeVersion.compareSpecification(J2EEVersion.J2EE_1_4) >= 0) {
                    views.add(new DDView(dataObject, WSSERVICE));
                    views.add(new DDView(dataObject, JMS));
                }
            }
            return views.toArray(new DDView[views.size()]);
        }
        
        @Override
        public MultiViewElement createElement(SunDescriptorDataObject dataObject, final String name) {
            if(name.equals(OVERVIEW)) {
                return new SunAppClientOverviewMultiViewElement(dataObject);
            }
            
            return super.createElement(dataObject, name);
        }
        
    }
    
    
    /** View factory for sun-cmp-mappings.xml specific views
     */
    public static class SunCmpMappingsDDViewFactory extends DDViewFactory {
        
        private static final long serialVersionUID = -8759598009819101753L;
        
        public DesignMultiViewDesc[] getMultiViewDesc(SunDescriptorDataObject dataObject) {
// TODO complete set of sun-cmp-mappings.xml multiview panels.
//            return new DDView[] {
//                new DDView(dataObject, OVERVIEW)
//            };
            return new DDView[0];
        }
        
        @Override
        public MultiViewElement createElement(SunDescriptorDataObject dataObject, final String name) {
//            if(name.equals(RELATIONSHIPS)) {
//                return new SunCmpRelationshipsMultiViewElement(dataObject);
//            }
            
            return super.createElement(dataObject, name);
        }
        
    }
    
    /** View factory for sun-resource.xml specific views
     */
    public static class SunResourceDDViewFactory extends DDViewFactory {
        
        private static final long serialVersionUID = -8759598009819101753L;
        
        public DesignMultiViewDesc[] getMultiViewDesc(SunDescriptorDataObject dataObject) {
            return new DDView[0];
        }
        
        @Override
        public MultiViewElement createElement(SunDescriptorDataObject dataObject, final String name) {
            return super.createElement(dataObject, name);
        }
        
    }
    
    /** Common DDView class that represents a top level tab in the multiview page.
     *  Parameterized by it's name (TODO and what else ???)
     *
     *  Delegates to descriptor specific view factory for creating child elements.
     */
    class DDView extends DesignMultiViewDesc implements java.io.Serializable {
        
        private static final long serialVersionUID = -8759598009819101741L;
        
        private String name;
        
        DDView(SunDescriptorDataObject dataObject, String name) {
            super(dataObject, name);
            this.name = name;
        }
        
        public MultiViewElement createElement() {
            SunDescriptorDataObject dataObject = (SunDescriptorDataObject) getDataObject();
            return DDViewFactory.this.createElement(dataObject, name);
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            final SunDescriptorDataObject dataObject = (SunDescriptorDataObject) getDataObject();
            return new HelpCtx(dataObject.getActiveMVElement().getSectionView().getClass());
        }
        
        public Image getIcon() {
            return Utilities.loadImage(Utils.ICON_BASE_DD_VALID + ".gif"); //NOI18N
        }
        
        public String preferredID() {
            return "sundd_multiview_" + name; //NOI18N
        }
        
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(DDViewFactory.class, "LBL_" + name); //NOI18N
        }
        
    }
    
}
