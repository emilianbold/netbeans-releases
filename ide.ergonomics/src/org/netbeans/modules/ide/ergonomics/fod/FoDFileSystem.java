/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ide.ergonomics.fod;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfoAccessor.Internal;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.xml.sax.SAXException;

/**
 *
 * @author Jirka Rechtacek
 */
@ServiceProvider(service=FileSystem.class)
public class FoDFileSystem extends MultiFileSystem 
implements Runnable {
    private static FoDFileSystem INSTANCE;
    final static Logger LOG = Logger.getLogger (FoDFileSystem.class.getPackage ().toString ());
    private static RequestProcessor RP = new RequestProcessor("Ergonomics"); // NOI18N
    private RequestProcessor.Task refresh = RP.create(this);

    public FoDFileSystem() {
        assert INSTANCE == null;
        INSTANCE = this;
        setPropagateMasks(true);
        refresh();
    }

    public static synchronized FoDFileSystem getInstance() {
        if (INSTANCE == null) {
            while (INSTANCE == null) {
                INSTANCE = Lookup.getDefault().lookup(FoDFileSystem.class);
            }
        }
        return INSTANCE;
    }
    
    public void refresh() {
        refresh.schedule(0);
        refresh.waitFinished();
    }
    
    public void run() {
        Lookup.Result<FeatureInfo> result = Feature2LayerMapping.featureTypesLookup().lookupResult(FeatureInfo.class);
        boolean empty = true;
        
        List<XMLFileSystem> delegate = new ArrayList<XMLFileSystem>();
        for (FeatureInfo info : result.allInstances ()) {
            Internal internal = FeatureInfoAccessor.DEFAULT.getInternal(info);
            if (!internal.isEnabled()) {
                delegate.add(internal.getXMLFileSystem());
            } else {
                empty = false;
            }
        }
        if (empty) {
            try {
                delegate.add(0, new XMLFileSystem(FoDFileSystem.class.getResource("default.xml"))); // NOI18N
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        setDelegates(delegate.toArray(new FileSystem[0]));
    }

    public FeatureInfo whichProvides(FileObject template) {
        Lookup.Result<FeatureInfo> result = Feature2LayerMapping.featureTypesLookup().lookupResult(FeatureInfo.class);

        String path = template.getPath();
        for (FeatureInfo info : result.allInstances()) {
            Internal internal = FeatureInfoAccessor.DEFAULT.getInternal(info);
            XMLFileSystem fs = internal.getXMLFileSystem();
            if (fs.findResource(path) != null) {
                return info;
            }
        }
        return null;
    }
    
    public URL getDelegateFileSystem(FileObject template) {
        Lookup.Result<FeatureInfo> result = Feature2LayerMapping.featureTypesLookup().lookupResult(FeatureInfo.class);
        
        String path = template.getPath();
        for (FeatureInfo pt2m : result.allInstances ()) {
            Internal internal = FeatureInfoAccessor.DEFAULT.getInternal(pt2m);
            XMLFileSystem fs = internal.getXMLFileSystem();
            if (fs.findResource(path) != null) {
                return fs.getXmlUrl();
            }
        }
        return null;
    }

}
