/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.hints.analyzer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.hints.infrastructure.HintsTask;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.modules.java.hints.analyzer.ui.AnalyzerTopComponent;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Lahoda
 */
public class Analyzer implements Runnable {

    private final Lookup context;
    private final AtomicBoolean cancel;
    private final ProgressHandle handle;
    private final Dialog d;
    private final Map<String, Preferences> preferencesOverlay;

    public Analyzer(Lookup context, AtomicBoolean cancel, ProgressHandle handle, Dialog d, Map<String, Preferences> preferencesOverlay) {
        this.context = context;
        this.cancel = cancel;
        this.handle = handle;
        this.d = d;
        this.preferencesOverlay = preferencesOverlay;
    }

    public void run() {
        handle.start();
        
        List<FileObject> toProcess = new LinkedList<FileObject>();
        Queue<FileObject> q = new LinkedList<FileObject>();
        
        q.addAll(toAnalyze(context));
        
        while (!q.isEmpty()) {
            FileObject f = q.poll();
            
            if (f.isData() && "text/x-java".equals(FileUtil.getMIMEType(f))) {
                toProcess.add(f);
            }
            
            if (f.isFolder()) {
                q.addAll(Arrays.asList(f.getChildren()));
            }
        }
        
        final List<ErrorDescription> eds = new LinkedList<ErrorDescription>();
        
        if (!toProcess.isEmpty()) {
            handle.switchToDeterminate(toProcess.size());
            
            ClasspathInfo cpInfo = ClasspathInfo.create(toProcess.get(0));
            JavaSource js = JavaSource.create(cpInfo, toProcess);
            final AtomicInteger f = new AtomicInteger();
            
            try {
                js.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController cc) throws Exception {
                        HintsSettings.setPreferencesOverride(preferencesOverlay);
                        
                        DataObject d = DataObject.find(cc.getFileObject());
                        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
                        Document doc = ec.openDocument();
                        
                        try {
                            handle.progress(FileUtil.getFileDisplayName(cc.getFileObject()));

                            if (cc.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
                                return;
                            }

                            handle.progress(f.incrementAndGet());

                            eds.addAll(new HintsTask().computeHints(cc));
                        } finally {
                            HintsSettings.setPreferencesOverride(null);
                        }
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        handle.finish();
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                AnalyzerTopComponent win = AnalyzerTopComponent.findInstance();
                win.open();
                win.requestActive();
                win.setData(context, preferencesOverlay, eds);
                
                d.setVisible(false);
            }
        });
    }
    
    //@AWT
    public static void process(Lookup context, Map<String, Preferences> preferencesOverlay) {
        ProgressHandle h = ProgressHandleFactory.createHandle("Processing Hints");
        JButton cancel = new JButton("Cancel");
        final AtomicBoolean abCancel = new AtomicBoolean();
        DialogDescriptor dd = new DialogDescriptor(ProgressHandleFactory.createProgressComponent(h), "Processing Hints", true, new Object[]{cancel}, cancel, DialogDescriptor.DEFAULT_ALIGN, null, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abCancel.set(true);
            }
        });
        
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);

        RequestProcessor.getDefault().post(new Analyzer(context, abCancel, h, d, preferencesOverlay));

        d.setVisible(true);
    }
    
    public static Lookup normalizeLookup(Lookup l) {
        if (!l.lookupAll(Project.class).isEmpty()) {
            return Lookups.fixed(l.lookupAll(Project.class).toArray(new Object[0]));
        }
        
        Collection<? extends FileObject> files = toAnalyze(l);
        
        if (!files.isEmpty()) {
            return Lookups.fixed(files.toArray(new Object[0]));
        }
        
        return null;
    }
    
    private static Collection<? extends FileObject> toAnalyze(Lookup l) {
        Collection<? extends FileObject> fos = l.lookupAll(FileObject.class);
        
        if (!fos.isEmpty()) {
            return fos;
        }

        Collection<? extends DataObject> dos = l.lookupAll(DataObject.class);
        
        if (!dos.isEmpty()) {
            Collection<FileObject> result = new LinkedList<FileObject>();
            
            for (DataObject od : dos) {
                result.add(od.getPrimaryFile());
            }
            
            return result;
        }
        
        return Collections.emptyList();
    }
}
