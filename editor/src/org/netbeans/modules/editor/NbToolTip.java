/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import org.openide.TopManager;
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
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtUtilities;
import java.beans.PropertyChangeListener;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.cookies.InstanceCookie;
import java.util.Enumeration;
import java.util.ArrayList;
import java.beans.PropertyChangeEvent;

/**
* ToolTip annotations reading and refreshing
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbToolTip extends FileChangeAdapter {
    
    private static final HashMap mime2tip = new HashMap();
    
    private String mimeType;
    
    private Annotation[] tipAnnotations;
    
    static synchronized void buildToolTip(JTextComponent target) {
        String mimeType = NbEditorUtilities.getMimeType(target.getDocument());
        NbToolTip tip = getTip(mimeType);
        tip.buildTip(target);
    }
    
    private NbToolTip(String mimeType) {
        this.mimeType = mimeType;
    }
    
    private static NbToolTip getTip(String mimeType) {
        NbToolTip tip = (NbToolTip)mime2tip.get(mimeType);
        if (tip == null) {
            tip = new NbToolTip(mimeType);
            mime2tip.put(mimeType, tip);
        }
        
        return tip;
    }

    private Annotation[] getTipAnnotations() {
        Annotation[] annos;
        synchronized (NbToolTip.class) {
            annos = tipAnnotations;
        }
        
        if (annos == null) {
            FileObject annoFolder = TopManager.getDefault().getRepository().getDefaultFileSystem().
            findResource("Editors/" + mimeType + "/ToolTips"); //NOI18N
        
            if (annoFolder != null) {
                ArrayList al = new ArrayList();
                Enumeration en = annoFolder.getChildren(false);
                while (en.hasMoreElements()) {
                    FileObject fo = (FileObject)en.nextElement();
                    try {
                        DataObject dob = DataObject.find(fo);
                        InstanceCookie ic = (InstanceCookie)dob.getCookie(InstanceCookie.class);
                        if (ic != null) {
                            Object a = ic.instanceCreate();
                            if (a instanceof Annotation) {
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
        Annotation[] annos = getTipAnnotations();
        if (annos != null) {
            ExtEditorUI ui = ExtUtilities.getExtEditorUI(target);
            if (ui != null) {
                ToolTipSupport tts = ui.getToolTipSupport();
                if (tts != null) {
                    BaseDocument doc = Utilities.getDocument(target);
                    if (doc != null) {
                        int offset = target.viewToModel(tts.getLastMouseEvent().getPoint());
                        if (offset >= 0) {
                            try {
                                int line = Utilities.getLineOffset(doc, offset);
                                int col = offset - Utilities.getRowStart(target, offset);

                                DataObject dob = NbEditorUtilities.getDataObject(doc);
                                if (dob != null) {
                                    EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
                                    if (ec != null) {
                                        Line.Set ls = ec.getLineSet();
                                        if (ls != null) {
                                            Line l = ls.getCurrent(line);
                                            if (l != null) {
                                                Line.Part lp = l.createPart(col, 0);
                                                if (lp != null) {
                                                    new Request(annos, lp, tts).run();
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (BadLocationException e) {
                            }
                        }
                    }
                }
            }
        }
    }
        
    private static class Request implements Runnable, PropertyChangeListener {
        
        private ToolTipSupport tts;
        
        private Annotation[] annos;
        
        Request(Annotation[] annos, Line.Part lp, ToolTipSupport tts) {
            this.annos = annos;
            this.tts = tts;
            
            for (int i = 0; i < annos.length; i++) {
                annos[i].attach(lp);
            }
            
            tts.addPropertyChangeListener(this);
        }
        
        public void run() {
            for (int i = 0; i < annos.length; i++) {
                String desc = annos[i].getShortDescription();
                if (desc != null) {
                    tts.setToolTipText(desc);
                }
                annos[i].addPropertyChangeListener(this);
            }
        }
        
        private void dismiss() {
            tts.removePropertyChangeListener(this);

            for (int i = 0; i < annos.length; i++) {
                annos[i].removePropertyChangeListener(this);
                annos[i].detach();
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (Annotation.PROP_SHORT_DESCRIPTION.equals(propName)) {
                String tipText = (String)evt.getNewValue();
                if (tipText != null) {
                    tts.setToolTipText(tipText);
                }
                
            } else if (ToolTipSupport.PROP_STATUS.equals(propName)) {
                if (((Integer)evt.getNewValue()).intValue() == ToolTipSupport.STATUS_HIDDEN) {
                    dismiss();
                }
            }
        }

    }

}
