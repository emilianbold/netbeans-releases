/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.common;

import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.j2ee.ddloaders.common.xmlutils.XMLJ2eeDataObject;
import org.netbeans.modules.j2ee.ddloaders.common.xmlutils.XMLJ2eeUtils;

import javax.swing.*;
import javax.swing.text.Document;
import java.io.IOException;

/** Represents a DD2beansDataObject in the Repository.
 *
 * @author  mkuchtiak
 */
public abstract class DD2beansDataObject extends XMLJ2eeDataObject implements org.openide.nodes.CookieSet.Factory{

    private static final int DELAY_FOR_TIMER=200;
    /** Private request processor for parsing and text generating tasks */
    protected final static RequestProcessor RP = new RequestProcessor("XML Parsing"); //NOI18N
    private final RequestProcessor.Task generationTask;
    // constructor settings
    private String prefixMark;

    private static final long serialVersionUID = -5363900668319174348L;

    public DD2beansDataObject(FileObject pf, MultiFileLoader loader)
        throws org.openide.loaders.DataObjectExistsException {
        this (pf, loader,true);
    }

    public DD2beansDataObject(FileObject pf, MultiFileLoader loader, final boolean saveAfterNodeChanges)
        throws org.openide.loaders.DataObjectExistsException {
        super (pf, loader);

        generationTask = RP.create(new Runnable() {
            int numberOfStartedGens;
            public void run() {
                numberOfStartedGens++;
                final String newDoc = generateDocument();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            Document doc = getEditorSupport().openDocument();
                            XMLJ2eeUtils.replaceDocument(doc, newDoc, prefixMark);
                            setDocumentValid(true);
                            if (saveAfterNodeChanges) {
                                SaveCookie savec = (SaveCookie) getCookie(SaveCookie.class);
                                if (savec != null) {
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
                            synchronized (generationTask) {
                                numberOfStartedGens--;
                                if (numberOfStartedGens == 0) {
                                    nodeDirty = false;
                                }
                            }
                        }
                    }
                });
            }
        });
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
                restartGen();
            }
        }
    }

    public RequestProcessor.Task getGenerationTask(){
        return generationTask;
    }

    protected void restartGen() {
        generationTask.schedule(DELAY_FOR_TIMER);
    }
}
