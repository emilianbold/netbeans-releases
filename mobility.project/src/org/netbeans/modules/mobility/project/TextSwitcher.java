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

package org.netbeans.modules.mobility.project;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import javax.swing.text.StyledDocument;

import org.netbeans.api.mdr.MDRepository;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.modules.mobility.project.preprocessor.PPDocumentDestination;
import org.netbeans.modules.mobility.project.preprocessor.PPDocumentSource;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
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
    
    protected static RequestProcessor switchProcessor = new RequestProcessor(TEXT_SWITCH_SUPPORT);
    protected static Object lockObserver = new Object();
    
    /** Creates a new instance of TextSwitcher */
    public TextSwitcher(Project p, AntProjectHelper h) {
        this.p = p;
        this.h = h;
    }
    
    public void propertyChange(final PropertyChangeEvent evt) {
        final String changed = evt.getPropertyName();
        
        if (ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE.equals(changed) ||
                ProjectConfigurationsHelper.PROJECT_PROPERTIES.equals(changed)) {
            
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
                if (!(doc instanceof BaseDocument)) return false;
                
                final PPDocumentSource ppSrc = new PPDocumentSource(doc);
                final PPDocumentDestination ppDest = new PPDocumentDestination((BaseDocument)doc);
                
                final ProjectConfigurationsHelper pch = p.getLookup().lookup(ProjectConfigurationsHelper.class);
                
                final HashMap<String,String> identifiers=new HashMap<String,String>(pch.getActiveAbilities());
                identifiers.put(pch.getActiveConfiguration().getDisplayName(),null);
                final CommentingPreProcessor cpp =new CommentingPreProcessor(ppSrc, ppDest, identifiers) ;
                
                final MDRepository rep = JavaModel.getJavaRepository();
                rep.beginTrans(false);
                try {
                    doc.putProperty(TextSwitcher.SKIP_DUCUMENT_CHANGES, TextSwitcher.SKIP_DUCUMENT_CHANGES);
                    NbDocument.runAtomic(doc, cpp);
                } finally {
                    doc.putProperty(TextSwitcher.SKIP_DUCUMENT_CHANGES, null);
                    rep.endTrans();
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
