/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.syntax.spi.JSPColoringData;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.openide.loaders.DataObject;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Lahoda, Marek Fukala
 */
final class JspUpToDateStatusProvider extends UpToDateStatusProvider implements DocumentListener, PropertyChangeListener  {
    
    private UpToDateStatus upToDate;
    
    public static JspUpToDateStatusProvider get(Document doc) {
        JspUpToDateStatusProvider provider = (JspUpToDateStatusProvider) doc.getProperty(JspUpToDateStatusProvider.class);
        
        if (provider == null) {
            doc.putProperty(JspUpToDateStatusProvider.class, provider = new JspUpToDateStatusProvider(doc));
        }
        
        return provider;
    }
    
    /** Creates a new instance of AnnotationMarkProvider */
    private JspUpToDateStatusProvider(Document document) {
        upToDate = UpToDateStatus.UP_TO_DATE_OK;
        document.addDocumentListener(this);
        
        //listen to parser results
        DataObject documentDO = NbEditorUtilities.getDataObject(document);
        if(documentDO != null && documentDO.isValid()) {
            JSPColoringData jspcd = JspUtils.getJSPColoringData(document, documentDO.getPrimaryFile());
            //jspcd.addPropertyChangeListener(this);
            jspcd.addPropertyChangeListener(WeakListeners.propertyChange(this, jspcd));
        }
    }
    
    //the property changes are fired via JSPColoringData by TagLibParseSupport
    public void propertyChange(PropertyChangeEvent evt) {
        Boolean newValue = (Boolean)evt.getNewValue();
        if(JSPColoringData.PROP_PARSING_IN_PROGRESS.equals(evt.getPropertyName()) && newValue.booleanValue())
            setUpToDate(UpToDateStatus.UP_TO_DATE_PROCESSING);
        if(JSPColoringData.PROP_PARSING_SUCCESSFUL.equals(evt.getPropertyName()))
            setUpToDate(UpToDateStatus.UP_TO_DATE_OK);
    }
    
    public synchronized UpToDateStatus getUpToDate() {
        return upToDate;
    }
    
    private void setUpToDate(UpToDateStatus upToDate) {
        UpToDateStatus oldStatus = this.upToDate;
        if(oldStatus.equals(upToDate)) return ;
        this.upToDate = upToDate;
        firePropertyChange(PROP_UP_TO_DATE, oldStatus, upToDate);
    }
    
    public synchronized void removeUpdate(DocumentEvent e) {
        setUpToDate(UpToDateStatus.UP_TO_DATE_DIRTY);
    }
    
    public synchronized void insertUpdate(DocumentEvent e) {
        setUpToDate(UpToDateStatus.UP_TO_DATE_DIRTY);
    }
    
    public void changedUpdate(DocumentEvent e) {
    }
    
}
