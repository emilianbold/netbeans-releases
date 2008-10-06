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
package org.netbeans.modules.websvc.jaxrpc.wsdlmodel;

import com.sun.xml.rpc.processor.ProcessorOptions;
import com.sun.xml.rpc.processor.config.Configuration;
import com.sun.xml.rpc.processor.config.WSDLModelInfo;
import com.sun.xml.rpc.processor.model.Model;
import com.sun.xml.rpc.processor.modeler.wsdl.WSDLModelerBase;
import com.sun.xml.rpc.processor.util.ClientProcessorEnvironment;
import com.sun.xml.rpc.util.JAXRPCClassFactory;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.openide.util.RequestProcessor;

/**
 *
 * @author rico
 */
public class WsdlModeler {

    private WsdlModel wsdlModel;
    private URL wsdlUrl;
    private String packageName;
    RequestProcessor.Task task, task1;
    int listenersSize;
    protected Properties properties;
    private List<WsdlModelListener> modelListeners;

    /** Creates a new instance of WsdlModeler */
    public WsdlModeler(URL wsdlUrl, String packageName) {
        this.wsdlUrl = wsdlUrl;
        this.packageName = packageName;
        modelListeners = Collections.synchronizedList(new ArrayList<WsdlModelListener>());
        task = RequestProcessor.getDefault().create(new Runnable() {

            public void run() {
                generateWsdlModel();
                fireModelCreated(wsdlModel);
            }
        }, true);

    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    void setWsdlUrl(URL url) {
        wsdlUrl = url;
    }

    public URL getWsdlUrl() {
        return wsdlUrl;
    }

    public WsdlModel getWsdlModel() {
        return wsdlModel;
    }

    public WsdlModel getAndWaitForWsdlModel() {
        if (getWsdlModel() == null) {
            generateWsdlModel();
        }
        return wsdlModel;
    }

    public void generateWsdlModel(WsdlModelListener listener) {
        RequestProcessor.Task task = RequestProcessor.getDefault().create(new Runnable() {

            public void run() {
                generateWsdlModel();
                fireModelCreated(wsdlModel);
            }
        }, true);
        addWsdlModelListener(listener);
        task.run();
    }

    public void generateWsdlModel(WsdlModelListener listener, boolean forceReload) {

        if (forceReload) {
            try {
                task.waitFinished(10000);
            } catch (InterruptedException ex) {
            }
            addWsdlModelListener(listener);
            task.schedule(0);
        } else {
            addWsdlModelListener(listener);
            if (task.isFinished()) {
                task.schedule(0);
            }
        }
    }

    private synchronized void addWsdlModelListener(WsdlModelListener listener) {
        // adding listener
        if (listener != null) {
            modelListeners.add(listener);
        }
    }

    private void fireModelCreated(WsdlModel model) {
        synchronized (modelListeners) {
            Iterator<WsdlModelListener> modelIter = modelListeners.iterator();
            while (modelIter.hasNext()) {
                WsdlModelListener l = modelIter.next();
                l.modelCreated(model);
            }
        }
        // Removing all listeners
        synchronized (this) {
            modelListeners.clear();
        }
    }

    private void generateWsdlModel() {
        WSDLModelInfo modelInfo = new WSDLModelInfo();
        modelInfo.setJavaPackageName(getPackageName());
        ClientProcessorEnvironment env = new ClientProcessorEnvironment(new ByteArrayOutputStream(), null, null);
        Configuration config = new Configuration(env);
        config.setModelInfo(modelInfo);
        modelInfo.setParent(config);

        modelInfo.setLocation(getWsdlUrl().toExternalForm());
        Properties options = new Properties();
        options.put(ProcessorOptions.VALIDATE_WSDL_PROPERTY, "true");
        options.put(ProcessorOptions.SEARCH_SCHEMA_FOR_SUBTYPES, "true");
        options.put(ProcessorOptions.USE_WSI_BASIC_PROFILE, "false");

        WSDLModelerBase modeler = JAXRPCClassFactory.newInstance().createWSDLModeler(modelInfo, options);

        /**
         * Now that we have the modeler, we need to build a model.
         */
        Model tmpModel = null;
        try {
            tmpModel = modeler.buildModel();
        } catch (Exception me) {
        }
        wsdlModel = new WsdlModel(tmpModel);

    }
}


