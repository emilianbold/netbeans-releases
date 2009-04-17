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
 * WsdlUpdater.java
 *
 * Created on August 30, 2005, 10:29 AM
 *
 */
package org.netbeans.modules.mobility.jsr172.multiview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.jsr172.wizard.WsdlRetriever;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author suchys
 */
public class WsdlUpdater implements WsdlRetriever.MessageReceiver, Cancellable{
    
    static final String PROP_UPDATE_FINISHED = "updateFinished"; //NOI18N
    
    final private WsdlRetriever retriever;
    final private ProgressHandle handle;
    final private RequestProcessor updater;
    final private E2EDataObject doj;
    final private String toPackage;
    final private String fileName;
    
    /** Creates a new instance of WsdlUpdater */
    public WsdlUpdater(String url, String toPackage, String fileName, E2EDataObject doj) {
        this.doj = doj;
        this.toPackage = toPackage;
        this.fileName = fileName;
        
        retriever = new WsdlRetriever(this, url);
        handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(WsdlUpdater.class, "MSG_RefreshingWsdl"), this);
        handle.start();
        handle.switchToIndeterminate();
        handle.progress(NbBundle.getMessage(WsdlUpdater.class, "MSG_Refreshing"));
        updater = new RequestProcessor("wsdl updater"); //NOI18N
        updater.post(retriever);
    }
    
    public void setWsdlDownloadMessage(final String message) {
        handle.progress(message);
        if(retriever.getState() >= WsdlRetriever.STATUS_COMPLETE) {
            handle.finish();
            updater.stop();
            if (retriever.getState() == WsdlRetriever.STATUS_COMPLETE){
                final ByteArrayInputStream bais = new ByteArrayInputStream(retriever.getWsdl());
                try {
                    final String file = toPackage + '/' + fileName; //NOI18N
                    final Sources sources = doj.getClientProject().getLookup().lookup(Sources.class);
                    final SourceGroup sg = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)[0];
                    FileObject localFile = sg.getRootFolder().getFileObject(file);
                    // Fix for #162713 - NullPointerException at org.netbeans.modules.mobility.jsr172.multiview.WsdlUpdater.setWsdlDownloadMessage
                    boolean equals ;
                    if ( localFile == null){
                        equals = false;
                    }
                    else {
                        final InputStream is = localFile.getInputStream();
                        equals = FileContentComparator.equalFiles(bais, is);
                        is.close();
                    }
                    if (equals){
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(WsdlUpdater.class, "MSG_WSDL_Unchanged"));
                    } else {
                        final NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                                NbBundle.getMessage(WsdlUpdater.class, "MSG_WSDL_Changed"),
                                NbBundle.getMessage(WsdlUpdater.class, "MSG_WSDL_ChangedTitle"),
                                NotifyDescriptor.YES_NO_OPTION);
                        DialogDisplayer.getDefault().notify(nd);
                        if (nd.getValue() == NotifyDescriptor.YES_OPTION){
                            FileLock flck = null;
                            if (localFile == null){
                                localFile = sg.getRootFolder().getFileObject(toPackage); //get folder
                                localFile = localFile.createData(fileName); //create new local wsdl
                                assert localFile != null;
                            }
                            try {
                                flck = localFile.lock();
                                final BufferedOutputStream bos = new BufferedOutputStream(localFile.getOutputStream(flck));
                                bos.write(retriever.getWsdl());
                                bos.close();
                                doj.generate();
                            } catch (IOException e){
                                ErrorManager.getDefault().notify(e);
                            } finally {
                                if (flck != null){
                                    flck.releaseLock();
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                }
            } else if (retriever.getState() > WsdlRetriever.STATUS_COMPLETE){ //errors
                StatusDisplayer.getDefault().setStatusText(message);
            }
            firePropertyChangeListenerPropertyChange(
                    new PropertyChangeEvent(this, PROP_UPDATE_FINISHED, Boolean.FALSE, Boolean.TRUE));
        }
    }
    
    public boolean cancel() {
        retriever.stopRetrieval();
        //updater.stop();
        //handle.finish();
        setWsdlDownloadMessage(NbBundle.getMessage(WsdlUpdater.class, "MSG_Stopping"));
        return true;
    }
    
    /**
     * Utility field holding list of PropertyChangeListeners.
     */
    private transient java.util.ArrayList<PropertyChangeListener> propertyChangeListenerList;
    
    /**
     * Registers PropertyChangeListener to receive events.
     * @param listener The listener to register.
     */
    public synchronized void addPropertyChangeListener(final PropertyChangeListener listener) {
        if (propertyChangeListenerList == null ) {
            propertyChangeListenerList = new java.util.ArrayList<PropertyChangeListener>();
        }
        propertyChangeListenerList.add(listener);
    }
    
    /**
     * Removes PropertyChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
        if (propertyChangeListenerList != null ) {
            propertyChangeListenerList.remove(listener);
        }
    }
    
    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void firePropertyChangeListenerPropertyChange(final PropertyChangeEvent event) {
        PropertyChangeListener list[];
        synchronized (this) {
            if (propertyChangeListenerList == null) return;
            list = propertyChangeListenerList.toArray(new PropertyChangeListener[propertyChangeListenerList.size()]);
        }
        for (PropertyChangeListener pcl : list ) {
            pcl.propertyChange(event);
        }
    }
}
