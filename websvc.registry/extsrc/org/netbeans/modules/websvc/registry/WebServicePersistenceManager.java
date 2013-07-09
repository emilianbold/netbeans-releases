/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.ErrorManager;

import org.netbeans.modules.websvc.registry.model.WebServiceData;
import org.netbeans.modules.websvc.registry.model.WebServiceDataPersistenceDelegate;
import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;

public class WebServicePersistenceManager implements ExceptionListener, org.netbeans.modules.websvc.registry.netbeans.PersistenceManagerInterface {
	
        private File websvcDir = new File(System.getProperty("netbeans.user"), "websvc"); // NOI18N
	private File websvcRefFile = new File(websvcDir, "websvc_ref.xml"); // NOI18N	

	public WebServicePersistenceManager() {
	}
	
	public void load(ClassLoader cl) {
            //Thread.dumpStack();
            // System.out.println("WebServicePersistenceManager load called");
                
             WebServiceListModel wsListModel = WebServiceListModel.getInstance();
		if(websvcRefFile.exists()) {
                        //String originalParserFactory = System.getProperty(SAXParserFactory_PROP);
			ClassLoader origClassLoader = null;
			XMLDecoder decoder = null;

			try {
				origClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(cl);
				
				decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(websvcRefFile)));
                                Object o = decoder.readObject();
                                int wsDataNums = 0;
                                
                                if (o instanceof Integer) {
                                    wsDataNums = ((Integer)o).intValue();
                                } else {
                                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while loading in WS registry: " + o);
                                    return;
                                }

				for(int i = 0; i< wsDataNums; i++) {
					try {
						WebServiceData wsData = (WebServiceData) decoder.readObject();
						wsListModel.addWebService(wsData);
					} catch(Exception exc) {
                                            Logger.getLogger(WebServicePersistenceManager.class.getName()).log(Level.FINE, "WS Persistance Manager: cannot read WebServiceData", exc);
					}
				}

                                o = decoder.readObject();
                                int wsGroups = 0;
                                if (o instanceof Integer) {
                                    wsGroups = ((Integer)o).intValue();
                                } else {
                                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while loading in WS registry: " + o);
                                    return;
                                }
				
                                for(int i = 0; i< wsGroups; i++) {
					try {
						WebServiceGroup group = (WebServiceGroup) decoder.readObject();
						wsListModel.addWebServiceGroup(group);
					} catch(Exception ex) {
						ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
					}
				}
			} catch(Throwable thrown) {
                            Logger.getLogger(WebServicePersistenceManager.class.getName()).log(Level.FINE, "WS Persistance Manager loading failed", thrown);
			} finally {
                            
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
}
