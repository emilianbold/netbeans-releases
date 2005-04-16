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

/**
 * WebServicePersistenceManager.java
 * @author  Winston Prakash
 */

package org.netbeans.modules.websvc.registry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Set;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.beans.ExceptionListener;

import org.openide.ErrorManager;

import org.netbeans.modules.websvc.registry.model.WebServiceData;
import org.netbeans.modules.websvc.registry.model.WebServiceDataPersistenceDelegate;
import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import org.netbeans.modules.websvc.registry.netbeans.SJSASVersion;

public class WebServicePersistenceManager implements ExceptionListener, org.netbeans.modules.websvc.registry.netbeans.PersistenceManagerInterface {

//	// Appserver version strings.  We do not differ between minor versions (e.g. 8.0.0.1 is the same as 8.0)
//	private static final String APPSERVER_VERSION_8_0 = "8.0"; // NOI18N
//	private static final String APPSERVER_VERSION_8_1_BETA = "8.1 beta"; // NOI18N	// AKA SJSAS 8.1 2004Q4
//	private static final String APPSERVER_VERSION_8_1 = "8.1"; // NOI18N  // AKA SJSAS 8.1 2005Q1
//	private static final String APPSERVER_VERSION_UNKNOWN = "unknown"; // NOI18N

	private static final String SAXParserFactory_PROP = "javax.xml.parsers.SAXParserFactory"; // NOI18N

	private File websvcDir = new File(System.getProperty("netbeans.user"), "websvc"); // NOI18N
	private File websvcRefFile = new File(websvcDir, "websvc_ref.xml"); // NOI18N
	

	public WebServicePersistenceManager() {
	}
	
	public void load(ClassLoader cl) {
            //Thread.dumpStack();
            // System.out.println("WebServicePersistenceManager load called");
                
             WebServiceListModel wsListModel = WebServiceListModel.getInstance();
		if(websvcRefFile.exists()) {
			ClassLoader origClassLoader = null;
			XMLDecoder decoder = null;

			try {
				SJSASVersion appServerVersion = SJSASVersion.getSJSAppServerVersion();
				System.getProperties().put(SAXParserFactory_PROP, appServerVersion.getSaxParserImplClass());

				origClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(cl);
				
				decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(websvcRefFile)));
				int wsDataNums = ((Integer)decoder.readObject()).intValue();

				for(int i = 0; i< wsDataNums; i++) {
					try {
						WebServiceData wsData = (WebServiceData) decoder.readObject();
						wsListModel.addWebService(wsData);
					} catch(Exception exc) {
						ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, exc);
					}
				}

				int wsGroups = ((Integer)decoder.readObject()).intValue();
				for(int i = 0; i< wsGroups; i++) {
					try {
						WebServiceGroup group = (WebServiceGroup) decoder.readObject();
						wsListModel.addWebServiceGroup(group);
					} catch(Exception ex) {
						ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
					}
				}
			} catch(Throwable thrown) {
				ErrorManager.getDefault().notify(ErrorManager.ERROR, thrown);
			} finally {
				// Restore the SAXParserFactor property that was changed, restore
				// this threads context classloader and close the decoder stream 
				// if it was opened.
				System.getProperties().put(SAXParserFactory_PROP, "org.netbeans.core.xml.SAXFactoryImpl"); // NOI18N
				
				if(origClassLoader != null) {
					Thread.currentThread().setContextClassLoader(origClassLoader);
				}

				if(decoder != null) {
					decoder.close();
				}
			}
		}
	}

	public void save(ClassLoader cl) {
		//System.out.println("WebServicePersistenceManager save called");
		//System.out.println("No webservices " + WSListModel.getInstance().getWSList().size());
             WebServiceListModel wsListModel = WebServiceListModel.getInstance();
		ClassLoader origClassLoader = null;
		XMLEncoder encoder = null;
		
		try {
			if(!websvcDir.exists()) {
				websvcDir.mkdirs();
			}

			if(websvcRefFile.exists()) {
				websvcRefFile.delete();
			}

			encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(websvcRefFile)));
			encoder.setExceptionListener(this);
			WebServiceDataPersistenceDelegate wsDelegate = new WebServiceDataPersistenceDelegate();
			encoder.setPersistenceDelegate(Class.forName("javax.xml.namespace.QName", false, cl), 
				new WebServiceDataPersistenceDelegate()); // NOI18N
			encoder.setPersistenceDelegate(Class.forName("com.sun.xml.rpc.wsdl.document.soap.SOAPStyle", false, cl), 
				new WebServiceDataPersistenceDelegate()); // NOI18N
			encoder.setPersistenceDelegate(Class.forName("com.sun.xml.rpc.wsdl.document.soap.SOAPUse", false, cl), 
				new WebServiceDataPersistenceDelegate()); // NOI18N
			Set wsDataSet = wsListModel.getWebServiceSet();
			encoder.writeObject(new Integer(wsDataSet.size()));
			Iterator iter = wsDataSet.iterator();
			
			origClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(cl);
			
			while(iter.hasNext()) {
				WebServiceData wsData = (WebServiceData) iter.next();
				encoder.writeObject(wsData);
			}
			
			Set wsGroupSet =  wsListModel.getWebServiceGroupSet();
			encoder.writeObject(new Integer(wsGroupSet.size()));
			iter = wsGroupSet.iterator();

			while(iter.hasNext()) {
				WebServiceGroup group = (WebServiceGroup) iter.next();
				encoder.writeObject(group);
			}
		} catch(Throwable thrown) {
			ErrorManager.getDefault().notify(ErrorManager.ERROR, thrown);
		} finally {
			// Restore this threads context classloader and close the encoder
			// stream if it was opened.
			if(origClassLoader != null) {
				Thread.currentThread().setContextClassLoader(origClassLoader);
			}
			
			if(encoder != null) {
				encoder.close();//was encoder
				encoder.flush();
			}
		}
	}

	public void exceptionThrown(Exception e) {
		e.printStackTrace();
	}

// <pbuzek>
//        the j2ee/platform has been removed.
//        If needed the code to access a ws platform can be rewritten to something like this:
//
//		String version = APPSERVER_VERSION_UNKNOWN;	// NOI18N
//                String serverInstanceIDs[] = Deployment.getDefault().getServerInstanceIDs ();
//                J2eePlatform platform = null;
//                for (int i = 0; i < serverInstanceIDs.length(); i++) {
//                    J2eePlatform p = Deployment.getDefault().getJ2eePlatform (serverInstanceIDs [i]);
//                    if (p.isToolSupported ("wscompile")) {
//                        platform = p;
//                        break;
//                    }
//                }
// </pbuzek>       
//	/** Attempt to discern the application server version we're running against.
//	 *
//	 * 8.0 uses sun-domain_1_0.dtd
//	 * 8.1 uses sun-domain_1_1.dtd (also includes the 1_0 version for backwards compatibility)
//	 *
//	 */
//	public String getAppServerVersion() {
//		String version = APPSERVER_VERSION_UNKNOWN;	// NOI18N
//		File asInstallRoot = PlatformProvider.getDefault().getLocation();
//
//		if(asInstallRoot != null && asInstallRoot.exists()) {
//			File sunDomain11Dtd = new File(asInstallRoot, "lib/dtds/sun-domain_1_1.dtd"); // NOI18N
//			if(sunDomain11Dtd.exists()) {
//				// !PW FIXME put in detection for SJSAS 8.1 2005Q1 (release candidate)
//				version = APPSERVER_VERSION_8_1_BETA;
//			} else {
//				version = APPSERVER_VERSION_8_0;
//			}
//		}
//
//		return version;
//	}
}
