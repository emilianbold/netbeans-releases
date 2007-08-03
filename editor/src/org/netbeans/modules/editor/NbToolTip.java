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

package org.netbeans.modules.editor;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.text.Annotation;
import org.openide.text.Line;
import java.io.IOException;
import java.util.HashMap;
import org.netbeans.editor.ext.ToolTipSupport;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtUtilities;
import java.beans.PropertyChangeListener;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.cookies.InstanceCookie;
import java.util.Enumeration;
import java.util.ArrayList;
import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import javax.swing.plaf.TextUI;
import org.netbeans.editor.BaseTextUI;
import org.openide.filesystems.Repository;
import org.openide.util.RequestProcessor;

/**
* ToolTip annotations reading and refreshing
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbToolTip extends FileChangeAdapter {
    
    private static final boolean debug = Boolean.getBoolean("netbeans.debug.editor.tooltip");
    
    private static final HashMap<String,WeakReference<NbToolTip>> mime2tip = new HashMap();
    
    private static int lastRequestId;
    
    private String mimeType;
    
    private Annotation[] tipAnnotations;
    
    private RequestProcessor toolTipRP = new RequestProcessor("ToolTip-Evaluator", 1); // NOI18N
    
    static synchronized void buildToolTip(JTextComponent target) {
        String mimeType = NbEditorUtilities.getMimeType(target.getDocument());
        NbToolTip tip = getTip(mimeType);
        tip.buildTip(target);
    }
    
    private static int newRequestId() {
        return ++lastRequestId;
    }
    
    private static int getLastRequestId() {
        return lastRequestId;
    }
    
    
    private NbToolTip(String mimeType) {
        this.mimeType = mimeType;
    }
    
    private static NbToolTip getTip(String mimeType) {
        WeakReference<NbToolTip> nttWr = mime2tip.get(mimeType);
        NbToolTip tip = nttWr == null ? null : nttWr.get();
        if (tip == null) {
            tip = new NbToolTip(mimeType);
            mime2tip.put(mimeType, new WeakReference<NbToolTip>(tip));
        }
        return tip;
    }

    private Annotation[] getTipAnnotations() {
        Annotation[] annos;
        synchronized (NbToolTip.class) {
            annos = tipAnnotations;
        }
        
        if (annos == null) {

            if (debug) {
                System.err.println("Searching for tooltip annotations for mimeType=" + mimeType);
            }

            FileObject annoFolder = Repository.getDefault().getDefaultFileSystem().
            findResource("Editors/" + mimeType + "/ToolTips"); //NOI18N
        
            if (debug) {
                System.err.println("tooltip annotation folder=" + annoFolder);
            }

            if (annoFolder != null) {
                ArrayList al = new ArrayList();
                Enumeration en = annoFolder.getChildren(false);
                while (en.hasMoreElements()) {
                    FileObject fo = (FileObject)en.nextElement();
                    
                    if (debug) {
                        System.err.println("tooltip annotation fileobject=" + fo);
                    }

                    try {
                        DataObject dob = DataObject.find(fo);
                        InstanceCookie ic = (InstanceCookie)dob.getCookie(InstanceCookie.class);

                        if (debug) {
                            System.err.println("tooltip annotation instanceCookie=" + ic);
                        }

                        if (ic != null) {
                            Object a = ic.instanceCreate();

                            if (debug) {
                                System.err.println("tooltip annotation instance=" + a);
                            }

                            if (a instanceof Annotation) {

                                if (debug) {
                                    System.err.println("Found tooltip annotation=" + a
                                        + ", class " + a.getClass() // NOI18N
                                        + " for mimeType=" + mimeType // NOI18N
                                    );
                                }
                                
                                al.add(a);
                            }
                        }
                    } catch (DataObjectNotFoundException e) {
                    } catch (IOException e) {
                    } catch (ClassNotFoundException e) {
                    }
                }
                
                annos = (Annotation[])al.toArray(new Annotation[al.size()]);
                synchronized (NbToolTip.class) {
                    tipAnnotations = annos;
                }
                
                annoFolder.addFileChangeListener(this);
            }
        }
        
        return annos;
    }
     
    private void buildTip(JTextComponent target) {

        TextUI textUI = target.getUI();
        if (textUI!=null && textUI instanceof BaseTextUI){
            BaseTextUI btui = (BaseTextUI)textUI;
            ExtEditorUI editorUI = (ExtEditorUI)btui.getEditorUI();
            ToolTipSupport tts = editorUI.getToolTipSupport();
            String toolTipText = btui.getToolTipText(target, tts.getLastMouseEvent().getPoint());
            if (toolTipText!=null){
                return;
            }
        }
        
        Annotation[] annos = getTipAnnotations();
        if (annos != null) {
            ExtEditorUI ui = ExtUtilities.getExtEditorUI(target);
            if (ui != null) {
                ToolTipSupport tts = ui.getToolTipSupport();
                if (tts != null) {
                    BaseDocument doc = Utilities.getDocument(target);
                    if (doc != null) {
                        DataObject dob = NbEditorUtilities.getDataObject(doc);
                        if (dob != null && dob.isValid()) {
                            EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
                            if (ec != null) {
                                StyledDocument openedDoc;
                                try {
                                    openedDoc = ec.openDocument();
                                } catch (IOException e) {
                                    openedDoc = null; // should return in next if stmt
                                }

                                if (openedDoc != doc) { // doc has changed in meantime
                                    return;
                                }

                                // partial fix of #33165 - read-locking of the document added
                                doc.readLock();
                                try {
                                    int offset = target.viewToModel(tts.getLastMouseEvent().getPoint());
                                    if (offset >= 0) {
                                        try {
                                            int line = Utilities.getLineOffset(doc, offset);
                                            int col = offset - Utilities.getRowStart(target, offset);
                                            Line.Set ls = ec.getLineSet();
                                            if (ls != null) {
                                                Line l = ls.getCurrent(line);
                                                if (l != null) {
                                                    Line.Part lp = l.createPart(col, 0);
                                                    if (lp != null) {
                                                        AnnotationDesc annoDesc = doc.getAnnotations().getActiveAnnotation(line);
                                                        if (annoDesc != null && ((offset < annoDesc.getOffset() || offset >= annoDesc.getOffset() + annoDesc.getLength()))) {
                                                            annoDesc = null;
                                                        }
                                                        org.netbeans.editor.BaseKit kit = org.netbeans.editor.Utilities.getKit(target);
                                                        if (kit instanceof NbEditorKit) {
                                                            int requestId = newRequestId();
                                                            toolTipRP.post(new Request(annoDesc, annos, lp, tts, doc, (NbEditorKit)kit, requestId));
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (BadLocationException e) {
                                        }
                                    }
                                } finally {
                                    doc.readUnlock();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
        
    private static class Request implements Runnable, PropertyChangeListener, DocumentListener {
        
        private ToolTipSupport tts;
        
        private Annotation[] annos;
        
        private AnnotationDesc annoDesc;
        
        private Line.Part linePart;
        
        private AbstractDocument doc;
        
        private NbEditorKit kit;
        
        private int requestId;
        
        private boolean documentModified;
        
        Request(AnnotationDesc annoDesc, Annotation[] annos, Line.Part lp,
        ToolTipSupport tts, AbstractDocument doc, NbEditorKit kit, int requestId) {
            this.annoDesc = annoDesc;
            this.annos = annos;
            this.linePart = lp;
            this.tts = tts;
            this.doc = doc;
            this.kit = kit;
            this.requestId = requestId;
        }
        
        public void run() {
            if (tts == null) return;
            
            if (tts == null || tts.getStatus() == ToolTipSupport.STATUS_HIDDEN) {
                return; // do nothing
            }
            if (!isRequestValid()) {
                return;
            }

            if (tts!=null) tts.addPropertyChangeListener(this);
            
            kit.toolTipAnnotationsLock(doc);
            try {
                doc.readLock();
                try {

                    if (!isRequestValid()) {
                        return;
                    }

                    // Attach tooltip annotations
                    for (int i = 0; i < annos.length; i++) {
                        annos[i].attach(linePart);
                    }

                    if (annoDesc != null && tts != null) {
                        tts.setToolTipText(annoDesc.getShortDescription());
                        annoDesc.addPropertyChangeListener(this);
                    } else {
                        for (int i = 0; i < annos.length; i++) {
                            String desc = annos[i].getShortDescription();
                            if (desc != null && tts != null) {
                                tts.setToolTipText(desc);
                            }
                            annos[i].addPropertyChangeListener(this);
                        }
                    }
                } finally {
                    doc.readUnlock();
                }
            } finally {
                kit.toolTipAnnotationsUnlock(doc);
            }
        }
          
        private boolean isRequestValid() {
            return (getLastRequestId() == this.requestId)
                && !documentModified
                && isDocumentValid();
        }

        private boolean isDocumentValid() {
            DataObject dob = NbEditorUtilities.getDataObject(doc);
            if (dob != null) {
                EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
                if (ec != null) {
                    StyledDocument openedDoc;
                    try {
                        openedDoc = ec.openDocument();
                    } catch (IOException e) {
                        openedDoc = null; // should return in next if stmt
                    }
                    
                    return (openedDoc == doc);
                }
            }
            return false;
        }

        private void dismiss() {
            if (tts !=null) tts.removePropertyChangeListener(this);
            tts = null; // signal that support no longer valid

            if (annoDesc != null) {
                annoDesc.removePropertyChangeListener(this);
            } else {
                for (int i = 0; i < annos.length; i++) {
                    annos[i].removePropertyChangeListener(this);
                    annos[i].detach();
                }
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (Annotation.PROP_SHORT_DESCRIPTION.equals(propName) || AnnotationDesc.PROP_SHORT_DESCRIPTION.equals(propName)) {
                if (evt.getNewValue() != null) {
                    final String tipText = (String)evt.getNewValue();
                    Utilities.runInEventDispatchThread( // ensure to run in AWT thread
                        new Runnable() {
                            public void run() {
                                if (tts != null) {
                                    tts.setToolTipText(tipText);
                                }
                            }
                        }
                    );
                }
                
            } else if (ToolTipSupport.PROP_STATUS.equals(propName)) {
                if (((Integer)evt.getNewValue()).intValue() == ToolTipSupport.STATUS_HIDDEN) {
                    dismiss();
                }
            }
        }
        
        public void insertUpdate(DocumentEvent evt) {
            documentModified = true;
        }
        
        public void removeUpdate(DocumentEvent evt) {
            documentModified = true;
        }
        
        public void changedUpdate(DocumentEvent evt) {
        }

    }

}
