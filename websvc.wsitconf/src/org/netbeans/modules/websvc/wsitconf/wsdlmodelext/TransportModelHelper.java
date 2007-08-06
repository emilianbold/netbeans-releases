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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxwsruntimemodel.JavaWsdlMapper;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.mtom.MtomQName;
import org.netbeans.modules.websvc.wsitmodelext.mtom.OptimizedMimeSerialization;
import org.netbeans.modules.websvc.wsitmodelext.transport.AutomaticallySelectFastInfoset;
import org.netbeans.modules.websvc.wsitmodelext.transport.AutomaticallySelectOptimalTransport;
import org.netbeans.modules.websvc.wsitmodelext.transport.FIQName;
import org.netbeans.modules.websvc.wsitmodelext.transport.OptimizedFastInfosetSerialization;
import org.netbeans.modules.websvc.wsitmodelext.transport.OptimizedTCPTransport;
import org.netbeans.modules.websvc.wsitmodelext.transport.TCPQName;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.xam.Model;

/**
 *
 * @author Martin Grebac
 */
public class TransportModelHelper {

    private static final String TCP_NONJSR109 = "com.sun.xml.ws.transport.tcp.server.glassfish.WSStartupServlet";   //NOI18N
            
    /**
     * Creates a new instance of TransportModelHelper
     */
    public TransportModelHelper() {
    }
    
    public static OptimizedMimeSerialization getOptimizedMimeSerialization(Policy p) {
        return PolicyModelHelper.getTopLevelElement(p, OptimizedMimeSerialization.class);        
    }
    
    // checks if Mtom is enabled in the config wsdl on specified binding
    public static boolean isMtomEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            OptimizedMimeSerialization mtomAssertion = getOptimizedMimeSerialization(p);
            return (mtomAssertion != null);
        }
        return false;
    }
    
    // enables Mtom in the config wsdl on specified binding
    public static void enableMtom(Binding b) {
        All a = PolicyModelHelper.createPolicy(b, false);
        PolicyModelHelper.createElement(a, MtomQName.OPTIMIZEDMIMESERIALIZATION.getQName(), OptimizedMimeSerialization.class, false);
    }

    // disables Mtom in the config wsdl on specified binding
    public static void disableMtom(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        OptimizedMimeSerialization mtom = getOptimizedMimeSerialization(p);
        if (mtom != null) {
            PolicyModelHelper.removeElement(mtom);
        }
        PolicyModelHelper.cleanPolicies(b);        
    }

    public static OptimizedTCPTransport getOptimizedTCPTransport(Policy p) {
        return PolicyModelHelper.getTopLevelElement(p, OptimizedTCPTransport.class);
    }
    
    // checks if TCP is enabled in the config wsdl on specified binding
    public static boolean isTCPEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            OptimizedTCPTransport tcpAssertion = getOptimizedTCPTransport(p);
            if (tcpAssertion != null) {
                return tcpAssertion.isEnabled();
            }
        }
        return false;
    }
    
    // enables/disables TCP in the config wsdl on specified binding
    public static void enableTCP(Service s, boolean isFromJava, Binding b, Project p, boolean enable, boolean jsr109) {
        All a = PolicyModelHelper.createPolicy(b, false);
        OptimizedTCPTransport tcp = 
                PolicyModelHelper.createElement(a, TCPQName.OPTIMIZEDTCPTRANSPORT.getQName(), 
                OptimizedTCPTransport.class, false);
        
        // make sure the listener is there (in Web project and jsr109 deployment
        if (enable) {

            Model model = b.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }

            WebModule wm = WebModule.getWebModule(p.getProjectDirectory());
            if (wm != null) {
                try {
                    WebApp wApp = DDProvider.getDefault ().getDDRoot(wm.getDeploymentDescriptor());                    
                    if (jsr109) {
                        String servletClassName = s.getImplementationClass();       //NOI18N
                        Servlet servlet = getServlet(wApp, servletClassName);
                        if (servlet == null) {      //NOI18N
                            try {
                                String servletName = s.getName();
                                servlet = (Servlet)wApp.addBean("Servlet", new String[]{"ServletName","ServletClass"},    //NOI18N
                                        new Object[]{servletName,servletClassName}, "ServletName");                                 //NOI18N
                                servlet.setLoadOnStartup(new java.math.BigInteger("1"));                            //NOI18N
                                String serviceName = s.getServiceName();
                                if (serviceName == null) {
                                    serviceName = servletClassName.substring(servletClassName.lastIndexOf(".")+1) + JavaWsdlMapper.SERVICE;
                                }
                                wApp.addBean("ServletMapping", new String[]{"ServletName","UrlPattern"}, //NOI18N
                                        new Object[]{servletName, "/" + serviceName}, "ServletName");                               //NOI18N
                                wApp.write(wm.getDeploymentDescriptor());
                            } catch (NameAlreadyUsedException ex) {
                                ex.printStackTrace();
                            } catch (ClassNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            servlet.setLoadOnStartup(new java.math.BigInteger("1"));
                        }
                    } else {
                        if (!isTcpListener(wApp)) {
                            try {
                                Listener l = (Listener)wApp.addBean("Listener", new String[]{"ListenerClass"},  //NOI18N
                                        new Object[]{TCP_NONJSR109}, "ListenerClass");                          //NOI18N
                                wApp.write(wm.getDeploymentDescriptor());
                            } catch (NameAlreadyUsedException ex) {
                                ex.printStackTrace();
                            } catch (ClassNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            try {
                tcp.enable(enable);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        } else {
            removeTCP(b);
            PolicyModelHelper.cleanPolicies(b);
        }
    }

    private static boolean isTcpListener(WebApp wa) {
        Listener[] listeners = wa.getListener();
        for (Listener l : listeners) {
            if (TCP_NONJSR109.equals(l.getListenerClass())) {
                return true;
            }
        }
        return false;
    }

    private static Servlet getServlet(WebApp wa, String className) {
        Servlet[] servlets = wa.getServlet();
        for (Servlet s : servlets) {
            if (className.equals(s.getServletClass())) {
                return s;
            }
        }
        return null;
    }
    
   public static void removeTCP(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        OptimizedTCPTransport tcp = getOptimizedTCPTransport(p);
        if (tcp != null) {
            PolicyModelHelper.removeElement(tcp);
        }
    }
       
    public static OptimizedFastInfosetSerialization getOptimizedFastInfosetSerialization(Policy p) {
        return PolicyModelHelper.getTopLevelElement(p, OptimizedFastInfosetSerialization.class);
    }
    
    // checks if FI is enabled in the config wsdl on specified binding
    public static boolean isFIEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            OptimizedFastInfosetSerialization fiAssertion = getOptimizedFastInfosetSerialization(p);
            if (fiAssertion != null) {
                return fiAssertion.isEnabled();
            }
        }
        return true;
    }
    
    // enables/disables FI in the config wsdl on specified binding
    public static void enableFI(Binding b, boolean enable) {
        All a = PolicyModelHelper.createPolicy(b, false);
        OptimizedFastInfosetSerialization fi = 
                PolicyModelHelper.createElement(a, FIQName.OPTIMIZEDFASTINFOSETSERIALIZATION.getQName(), 
                OptimizedFastInfosetSerialization.class, false);
        Model model = b.getModel();
        boolean isTransaction = model.isIntransaction();
        if (enable) {
            removeFI(b);
            PolicyModelHelper.cleanPolicies(b);
        } else {
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                fi.enable(enable);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
        if (enable) {
            PolicyModelHelper.cleanPolicies(b);
        }
    }
    
    public static void removeFI(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        OptimizedFastInfosetSerialization fi = getOptimizedFastInfosetSerialization(p);
        if (fi != null) {
            PolicyModelHelper.removeElement(fi);
        }
    }
    
    public static AutomaticallySelectFastInfoset getAutoEncoding(Policy p) {
        return (AutomaticallySelectFastInfoset) PolicyModelHelper.getTopLevelElement(p, AutomaticallySelectFastInfoset.class);
    }
    
    public static boolean isAutoEncodingEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            AutomaticallySelectFastInfoset ae = getAutoEncoding(p);
            return (ae != null);
        }
        return false;
    }
    
    public static void setAutoEncoding(Binding b, boolean enable) {
        if (enable) {
            All a = PolicyModelHelper.createPolicy(b, false);
            PolicyModelHelper.createElement(a, FIQName.AUTOMATICALLYSELECTFASTINFOSET.getQName(), AutomaticallySelectFastInfoset.class, false);
        } else {
            Policy p = PolicyModelHelper.getPolicyForElement(b);
            AutomaticallySelectFastInfoset ae = getAutoEncoding(p);
            if (ae != null) {
                PolicyModelHelper.removeElement(ae);
            }
        }
    }
    
    public static AutomaticallySelectOptimalTransport getAutoTransport(Policy p) {
        return (AutomaticallySelectOptimalTransport) PolicyModelHelper.getTopLevelElement(p, AutomaticallySelectOptimalTransport.class);
    }
    
    public static boolean isAutoTransportEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            AutomaticallySelectOptimalTransport at = getAutoTransport(p);
            return (at != null);
        }
        return false;
    }
    
    public static void setAutoTransport(Binding b, boolean enable) {
        if (enable) {
            All a = PolicyModelHelper.createPolicy(b, false);
            PolicyModelHelper.createElement(a, 
                    TCPQName.AUTOMATICALLYSELECTOPTIMALTRANSPORT.getQName(), 
                    AutomaticallySelectOptimalTransport.class, false);
        } else {
            Policy p = PolicyModelHelper.getPolicyForElement(b);
            AutomaticallySelectOptimalTransport at = getAutoTransport(p);
            if (at != null) {
                PolicyModelHelper.removeElement(at);
            }
        }
    }

}
