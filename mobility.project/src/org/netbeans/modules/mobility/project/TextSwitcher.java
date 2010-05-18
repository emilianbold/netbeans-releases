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

package org.netbeans.modules.mobility.project;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import javax.swing.text.StyledDocument;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor.Destination;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor.Source;
import org.netbeans.modules.mobility.project.bridge.J2MEProjectUtilitiesProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author  Adam Sotona, gc149856
 */
public class TextSwitcher implements PropertyChangeListener {
    
    public static final String TEXT_SWITCH_SUPPORT = "TEXT_SWITCH_SUPPORT"; //NOI18N
    
    public static final String SKIP_DUCUMENT_CHANGES = "SKIP_DUCUMENT_CHANGES"; //NOI18N
    
    protected final Project p;
    protected final AntProjectHelper h;
    protected ProjectConfigurationsHelper pch;
    
    protected static RequestProcessor switchProcessor = new RequestProcessor(TEXT_SWITCH_SUPPORT);
    protected static Object lockObserver = new Object();
    
    /** Creates a new instance of TextSwitcher */
    public TextSwitcher(Project p, AntProjectHelper h) {
        this.p = p;
        this.h = h;
    }
    
    public void propertyChange(final PropertyChangeEvent evt) {
        final String changed = evt.getPropertyName();
        if (pch == null) pch = p.getLookup().lookup(ProjectConfigurationsHelper.class);
        if (pch != null && pch.isPreprocessorOn() &&
               (ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE.equals(changed) ||
                ProjectConfigurationsHelper.PROJECT_PROPERTIES.equals(changed))) {
            
            new Runner(((ProjectConfiguration)evt.getNewValue())).start();
            
        }
    }
    
    
    
    private class Runner implements Runnable {
        
        
        private final ProjectConfiguration selectedConfig;
        private HashSet<DataObject> processed;
        
        public Runner(ProjectConfiguration config) {
            this.selectedConfig = config;
        }
        
        public synchronized void start() {
            if (selectedConfig == null) return;
            switchProcessor.post(this);
        }
        
        public void run() {
            synchronized(lockObserver) {
                
                processed = new HashSet<DataObject>();
                final Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
                for (int i=0;i<nodes.length;i++) {
                    final DataObject dob = (DataObject) nodes[i].getCookie(DataObject.class);
                    if (doSwitch(dob, false)) processed.add(dob);
                }
                
                if (processed == null) return;
                final String srcDir = h.getStandardPropertyEvaluator().getProperty("src.dir"); //NOI18N
                if (srcDir == null) return;
                final FileObject src = h.resolveFileObject(srcDir);
                if (src == null || !src.isFolder()) return;
                final Enumeration ch = DataFolder.findFolder(src).children(true);
                while (ch.hasMoreElements()) {
                    final DataObject dob = (DataObject)ch.nextElement();
                    if (!processed.contains(dob)) {
                        doSwitch(dob,false);
                    }
                }
            }
        }
        
        private synchronized boolean doSwitch(final DataObject dob, final boolean force) {
            
            if (dob == null)  return false;
            final FileObject fo = dob.getPrimaryFile();
            
            if (!fo.getExt().equals("java") || !p.equals(FileOwnerQuery.getOwner(fo))) return false; //NOI18N
            final EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
            if (ec == null) return false;
            try {
                final StyledDocument doc = force ? ec.openDocument() : ec.getDocument();
                J2MEProjectUtilitiesProvider utilProvider = Lookup.getDefault().lookup(J2MEProjectUtilitiesProvider.class);
                if (utilProvider == null) return false; //we do not run in full NetBeans
                if (!utilProvider.isBaseDocument(doc)) return false;
                
                final Source ppSrc = utilProvider.createPPDocumentSource(doc);
                final Destination ppDest = utilProvider.createPPDocumentDestination(doc);
                
                final HashMap<String,String> identifiers=new HashMap<String,String>(pch.getActiveAbilities());
                identifiers.put(pch.getActiveConfiguration().getDisplayName(),null);
                final CommentingPreProcessor cpp =new CommentingPreProcessor(ppSrc, ppDest, identifiers) ;
                
//                final MDRepository rep = JavaModel.getJavaRepository();
//                rep.beginTrans(false);
                try {
                    doc.putProperty(TextSwitcher.SKIP_DUCUMENT_CHANGES, TextSwitcher.SKIP_DUCUMENT_CHANGES);
                    NbDocument.runAtomic(doc, cpp);
                } finally {
                    doc.putProperty(TextSwitcher.SKIP_DUCUMENT_CHANGES, null);
//                    rep.endTrans();
                }
                
                return true;
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            return false;
        }
        
        
    }
    
}
