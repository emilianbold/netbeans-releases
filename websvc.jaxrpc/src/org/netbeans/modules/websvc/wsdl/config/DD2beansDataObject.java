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

package org.netbeans.modules.websvc.wsdl.config;

import java.awt.event.ActionListener;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.websvc.wsdl.xmlutils.XMLJ2eeDataObject;
import org.netbeans.modules.websvc.wsdl.xmlutils.XMLJ2eeUtils;

import java.io.*;
import javax.swing.Timer;
import org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.util.WeakListeners;

/** Represents a DD2beansDataObject in the Repository.
 *
 * @author  mkuchtiak
 */
public abstract class DD2beansDataObject extends XMLJ2eeDataObject implements org.openide.nodes.CookieSet.Factory{

    private static final int DELAY_FOR_TIMER=200;
    /** Private request processor for parsing and text generating tasks */
    protected final static RequestProcessor RP = new RequestProcessor("XML Parsing"); //NOI18N
    private RequestProcessor.Task generationTask;
    // constructor settings
    private boolean saveAfterNodeChanges;
    private Timer timer;
    private String prefixMark;
    private int numberOfStartedGens;
    ActionListener timerListener;

    private static final long serialVersionUID = -5363900668319174348L;

    public DD2beansDataObject(FileObject pf, MultiFileLoader loader)
        throws org.openide.loaders.DataObjectExistsException {
        this (pf, loader,true);
    }

    public DD2beansDataObject(FileObject pf, MultiFileLoader loader, boolean saveAfterNodeChanges)
        throws org.openide.loaders.DataObjectExistsException {
        super (pf, loader);
        //System.out.println("DD2beansDataObject() "+this+" "+pf.hashCode());
        //Thread.dumpStack();
        this.saveAfterNodeChanges=saveAfterNodeChanges;
        init();
    }

    private void init () {
        timerListener = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                //updatingDocumentFromNode=true;
                restartGen();
            }
        };
        // initialize timer
        timer = new Timer(0, null);
        timer.addActionListener ((ActionListener) WeakListeners.create (ActionListener.class, timerListener, timer));
        timer.setInitialDelay(DELAY_FOR_TIMER);
        timer.setRepeats(false);
        getCookieSet().assign(FileEncodingQueryImplementation.class, XmlFileEncodingQueryImpl.singleton());
    }

    private synchronized void restartTimer(){
        if (!timer.isRunning()){
            numberOfStartedGens++;
        }
        timer.restart();
    }

    /** Create document from the Node. This method is called after Node (Node properties)is changed.
    *  The document is generated from data modul (isDocumentGenerable=true)
    */
    protected abstract String generateDocument();

    /** setter for prefixMark. This is information, which prefix in xml document should be preserved
    * after replacing by new generated document (This is mainly for preserving comments at the beginning)
    * @param prefix prefixMark
    */
    protected final void setPrefixMark(String prefix) {
        this.prefixMark=prefix;
    }
    /** gettert for prefixMark
    * @return prefixMark
    */
    protected final String getPrefixMark() {
        return prefixMark;
    }

    /** Setter for property nodeDirty.
     * @param dirty New value of property nodeDirty.
     */
    public void setNodeDirty(boolean dirty){
        //System.out.println("setNodeDirty("+dirty+")");
        if (dirty) {
            synchronized (this) {
                nodeDirty=true;
                restartTimer();
            }
        }
    }

    public RequestProcessor.Task getGenerationTask(){
        return generationTask;
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    protected void restartGen() {
        //System.out.println("restart Gen");
        generationTask = null;
        generationTask = RP.postRequest(new Runnable() {
            public void run() {
                final String newDoc = generateDocument();
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            javax.swing.text.Document doc = getEditorSupport().openDocument();
                            XMLJ2eeUtils.replaceDocument(doc,newDoc,prefixMark);
                            setDocumentValid(true);
                            if (saveAfterNodeChanges) {
                                SaveCookie savec = (SaveCookie) getCookie(SaveCookie.class);
                                if (savec!=null) {
                                    savec.save();
                                }
                            }
                            // this is necessary for correct undo behaviour
                            getEditorSupport().getUndo().discardAllEdits();
                        } catch (javax.swing.text.BadLocationException e) {
                            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
                        } catch (IOException e) {
                            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
                        } finally {
                            synchronized (DD2beansDataObject.this) {
                                numberOfStartedGens--;
                                if (numberOfStartedGens==0) {
                                    nodeDirty=false;
                                }
                            }
                        }
                    }
                });
            }
        });
    }
}
