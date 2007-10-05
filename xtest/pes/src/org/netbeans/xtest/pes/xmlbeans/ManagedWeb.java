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


/*
 * ManagedWeb.java
 *
 * Created on May 27, 2002, 9:49 PM
 */

package org.netbeans.xtest.pes.xmlbeans;

import java.io.*;
import org.netbeans.xtest.pe.xmlbeans.*;

/**
 *
 * @author  breh
 */
public class ManagedWeb extends XMLBean {

    /** Creates a new instance of ManagedWeb */
    public ManagedWeb() {
    }

    // elements
    public ManagedGroup[] xmlel_ManagedGroup;

    // private stuff
    private File webFile;

    private PESWeb pesWeb = null;

    // business methods

    public static ManagedWeb getDefault(PESWeb web) {
        ManagedWeb mw = new ManagedWeb();
        mw.pesWeb = web;
        mw.xmlel_ManagedGroup = new ManagedGroup[0];
        return mw;
    }
    
    
    public static ManagedWeb loadManagedWeb(PESWeb web) {
        File webData = null;        
        try {
            webData = new File(web.getDataDir(),ManagedWeb.getDataFilename());
            if (webData.isFile()) {
                XMLBean aBean = null;
                try {
                    aBean = XMLBean.loadXMLBean(webData);
                } catch (ClassNotFoundException cnfe) {
                }
                if ((aBean == null) | !(aBean instanceof ManagedWeb)) {
                    ManagedWeb mw =  ManagedWeb.getDefault(web);                    
                    mw.webFile = webData;
                    return mw;
                } else {
                    ManagedWeb mw =  (ManagedWeb) aBean;
                    mw.webFile = webData;
                    mw.pesWeb = web;
                    return mw;
                }
            }
        } catch (IOException ioe) {            
            // exception ? ok, load the default web
        }
        ManagedWeb mw =  ManagedWeb.getDefault(web);
        mw.webFile = webData;
        return mw;
    }
    
    /*
    public void readPESWeb(PESWeb pesWeb) {
        this.xmlat_description = pesWeb.xmlat_description;
        this.xmlat_truncate = pesWeb.xmlat_truncate;
        this.xmlat_webroot = pesWeb.xmlat_webroot;
        this.xmlat_type = pesWeb.xmlat_type;
    }
    */
    
    public void saveManagedWeb(int depth) throws IOException {
        if (webFile != null) {            
            this.saveXMLBean(webFile, depth);
        } else {
            throw new IOException("web file is not specified - cannot save");
        }
    }
    
    
    // get filename of xml file with ManagedReports for this group
    public static String getDataFilename() {
        return "webdata.xml";        
    }
    
}
