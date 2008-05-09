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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.source;

import java.awt.Dialog;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.BpelSourcesRegistry;
import org.netbeans.modules.bpel.debugger.api.SessionCookie;
import org.netbeans.modules.bpel.debugger.api.SourcePath;
import org.netbeans.modules.bpel.debugger.spi.SourcePathSelectionProvider;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.bpel.model.api.Process;

/**
 *
 * @author Alexander Zgursky
 */
public class SourcePathImpl implements SourcePath {
    
    private final ContextProvider          myLookupProvider;
    private final BpelDebugger             myDebugger;
    private final Object myProcessingLock = new Object();
    private final RequestProcessor myRequestProcessor =
            new RequestProcessor("BpelSourcesLocator"); //NOI18N
    private final BpelSourcesRegistry mySourcesRegistry;
    private final PropertyChangeSupport myPcs =
            new PropertyChangeSupport(this);
    
    private RequestProcessor.Task myCurrentTask;
    
    private Set<String> myUpdatedSources =
            new HashSet<String>();
    
    private Set<String> myAvailableSources =
            new TreeSet<String>();
    
    private Set<String> mySelectedSources =
            new HashSet<String>();
    
    private Map<QName, Set<String>> myAvailableSourcesByQName =
            new HashMap<QName, Set<String>>();
    
    private Map<String, QName> myQNames =
            new HashMap<String, QName>();
    
    private Map<QName, String>       mySourceMap =
            new HashMap<QName, String>();

    private SessionCookie mySessionCookie;

    private SourcePathSelectionProvider mySelectionProvider;

    /** Creates new instance of SourcePath.
     *
     * @param lookupProvider debugger context
     */
    public SourcePathImpl(ContextProvider lookupProvider) {
        myLookupProvider = lookupProvider;
        myDebugger = lookupProvider.lookupFirst(null, BpelDebugger.class);
        mySourcesRegistry = Lookup.getDefault().lookup(BpelSourcesRegistry.class);
        updateAvailableSources();
    }
    
    public QName getProcessQName(String path) {
        while (true) {
            RequestProcessor.Task task;
            synchronized (myProcessingLock) {
                if (myCurrentTask == null) {
                    return myQNames.get(path);
                } else {
                    task = myCurrentTask;
                }
            }
            task.waitFinished();
        }
    }
    
    public String getSourcePath(QName processQName) {
        while (true) {
            RequestProcessor.Task task;
            synchronized (myProcessingLock) {
                if (myCurrentTask == null) {
                    Set<String> sources = new HashSet<String>();
                    sources.addAll(myAvailableSourcesByQName.get(processQName));
                    sources.retainAll(mySelectedSources);
                    if (sources.isEmpty()) {
                        return null;
                    } else if (sources.size() == 1) {
                        return sources.iterator().next();
                    } else {
                        return selectSource(processQName, sources);
                    }
                } else {
                    task = myCurrentTask;
                }
            }
            task.waitFinished();
        }
//        return mySourceMap.get(processQName);
    }

    public String[] getAvailableSources() {
        while (true) {
            RequestProcessor.Task task;
            synchronized (myProcessingLock) {
                if (myCurrentTask == null) {
                    return myAvailableSources.toArray(new String[myAvailableSources.size()]);
                } else {
                    task = myCurrentTask;
                }
            }
            task.waitFinished();
        }
    }

    public String[] getSelectedSources() {
        while (true) {
            RequestProcessor.Task task;
            synchronized (myProcessingLock) {
                if (myCurrentTask == null) {
                    return mySelectedSources.toArray(new String[mySelectedSources.size()]);
                } else {
                    task = myCurrentTask;
                }
            }
            task.waitFinished();
        }
    }

    public void setSelectedSources(String[] roots) {
        while (true) {
            RequestProcessor.Task task;
            synchronized (myProcessingLock) {
                if (myCurrentTask == null) {
                    mySelectedSources.clear();
                    for (String path : roots) {
                        mySelectedSources.add(path);
                    }
                    return;
                } else {
                    task = myCurrentTask;
                }
            }
            task.waitFinished();
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        myPcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        myPcs.removePropertyChangeListener(listener);
    }
    
    protected void addAvailableSource(String path) {
        synchronized (myProcessingLock) {
            myUpdatedSources.add(path);
            if (myCurrentTask == null) {
                myCurrentTask = myRequestProcessor.post(new MyProcessingRunnable());
            }
        }
    }
    
    private void updateAvailableSources() {
        String[] sourceRoots = mySourcesRegistry.getSourceRoots();
        for (String sourceRoot : sourceRoots) {
            addAvailableSource(sourceRoot);
        }
    }
    
    private String selectSource(QName processQName, Set<String> sources) {
        final SeveralSourceFilesWarning panel = new SeveralSourceFilesWarning(
                processQName,
                sources.toArray(new String[sources.size()]));
        final Object[] options = new Object[] {DialogDescriptor.OK_OPTION};
        final DialogDescriptor desc = new DialogDescriptor(
                panel,
                NbBundle.getMessage(
                SeveralSourceFilesWarning.class, "CTL_MORE_THAN_ONE_SOURCE_WARNING_TITLE" // NOI18N
                ),
                true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null
                );
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        
        // ugly hack -- we need to disable the option to just close the dialog,
        // see #95898
        final JDialog jDlg = (JDialog) dlg;
        jDlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        jDlg.getRootPane().getInputMap(
                JRootPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        jDlg.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourcePathImpl.class, "ACS_Select_Source_File_Dialog"));
        dlg.setVisible(true);
        
        final String path = panel.getSelectedInstance();
        
        sources.remove(path);
        mySelectedSources.removeAll(sources);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                myPcs.firePropertyChange(null, null, null);
            }
        });
        dlg.dispose();
        return path;
    }
    
    private synchronized SessionCookie getSessionCookie() {
        if (mySessionCookie == null) {
            mySessionCookie = myLookupProvider.lookupFirst(null, SessionCookie.class);
        }
        return mySessionCookie;
    }
    
    private synchronized SourcePathSelectionProvider getSelectionProvider() {
        if (mySelectionProvider == null) {
            mySelectionProvider = myLookupProvider.lookupFirst(null, SourcePathSelectionProvider.class);
        }
        return mySelectionProvider;
    }
    
    private class MyProcessingRunnable implements Runnable {
        public void run() {
            try {
                while (true) {
                    String nextSource;
                    synchronized(myProcessingLock) {
                        if (myUpdatedSources.isEmpty()) {
                            myCurrentTask = null;
                            break;
                        }
                        Iterator<String> iter = myUpdatedSources.iterator();
                        nextSource = iter.next();
                        iter.remove();
                    }
                    processSingleSource(nextSource);
                }
            } finally {
                //just for a back up
                myCurrentTask = null;
            }
        }
        
        private void processSingleSource(String path) {
            FileObject fo = FileUtil.toFileObject(new File(path));
            if (fo == null) {
                return;
            }
            registerFileObject(fo);
        }
        
        private void registerFileObject(FileObject fo) {
            if (fo.isFolder()) {
                //fo.addFileChangeListener(myFolderListener);
                for (FileObject child : fo.getChildren()) {
                    registerFileObject(child);
                }
            } else {
                DataObject dataObject;
                try {
                    dataObject = DataObject.find(fo);
                } catch (DataObjectNotFoundException ex) {
                    return;
                }

                BpelModel bpelModel = EditorUtil.getBpelModel(dataObject);
                if (bpelModel == null) {
                    return;
                }
                
                Process process = bpelModel.getProcess();
                if (process == null) {
                    return;
                }
                
                String name = process.getName();
                if (name == null || name.trim().equals("")) {
                    return;
                }
                
                File file = FileUtil.toFile(fo);
                if (fo == null) {
                    return;
                }
                QName processQName = new QName(process.getTargetNamespace(), name);
                register(file.getPath(), processQName);
            }
        }
        
        private void register(String path, QName processQName) {
            myQNames.put(path, processQName);
            myAvailableSources.add(path);
            if (getSelectionProvider() != null) {
                if (getSelectionProvider().isSelected(path)) {
                    mySelectedSources.add(path);
                }
            } else {
                mySelectedSources.add(path);
            }

            Set<String> availableForQName =
                    myAvailableSourcesByQName.get(processQName);
            if (availableForQName == null) {
                availableForQName = new HashSet<String>();
                myAvailableSourcesByQName.put(processQName, availableForQName);
            }
            availableForQName.add(path);
        }
    }
}
