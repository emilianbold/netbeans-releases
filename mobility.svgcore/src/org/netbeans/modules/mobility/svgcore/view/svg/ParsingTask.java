/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * ParsingTask.java
 * Created on May 25, 2007, 9:11 AM
 */

package org.netbeans.modules.mobility.svgcore.view.svg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import javax.microedition.m2g.SVGImage;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.model.ElementMapping;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.openide.util.NbBundle;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Pavel Benes
 */
final class ParsingTask extends Thread { 
    private final JPanel              panel;
    private final JTextArea           textArea;
    private final SVGViewTopComponent svgView;
    private final SVGFileModel        m_fileModel;
    private final DocumentModel       m_docModel;

    public ParsingTask(SVGDataObject dObj, SVGViewTopComponent svgView) throws Exception {
        this.svgView = svgView;
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        String errorMsg = NbBundle.getMessage(SVGViewTopComponent.class, "MSG_Parsing");
        textArea = new JTextArea(errorMsg); //NOI18N
        textArea.setBackground(Color.WHITE);
        Font font = textArea.getFont();
        textArea.setFont(font.deriveFont(16.0f));
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        setPriority( Thread.MIN_PRIORITY);
        m_fileModel = dObj.getModel();
        // ensure that model is valid
        m_docModel  = m_fileModel._getModel();
    }

    public JComponent getPanel() {
        return panel;
    }

    public void cancel() {
        interrupt();
    }

    //TODO use alternative to StringBufferInputStream
    @SuppressWarnings({"deprecation"})
    public void run() {
        try {
            try {
                final SVGImage svgImage = m_fileModel.parseSVGImage();
                assert svgImage != null;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        svgView.showImage(svgImage);
                    }
                });  
            } catch (IOException e) {
                Document doc = m_docModel.getDocument();
                showParsingError(doc.getText(0, doc.getLength()), e);
            } catch(Exception e) {
                e.printStackTrace();
                Document doc = m_docModel.getDocument();
                showParsingError(doc.getText(0, doc.getLength()), e);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private String composeMessageText( StringBuffer perseusError,
                                       StringBuffer saxErrors, int [] errNum) {
        StringBuilder sb = new StringBuilder(NbBundle.getMessage(SVGViewTopComponent.class, "ERR_NotSvgTiny"));
        if (perseusError.length() > 0) {
            sb.append("\n");
            sb.append( perseusError);
        }
        sb.append(saxErrors);
        sb.append("\n");
        if ( errNum == null) {
            sb.append(NbBundle.getMessage(SVGViewTopComponent.class, "ERR_Additional"));                            
        } else {
            sb.append(NbBundle.getMessage(SVGViewTopComponent.class, "ERR_ErrorWarnings",
                    Integer.toString(errNum[0]), Integer.toString(errNum[1]), Integer.toString(errNum[2]))); 
        }
        return sb.toString();
    }

    @SuppressWarnings({"deprecation"})
    private void showParsingError(String fileText, Exception perseusException) {            
        final StringBuffer saxErrors    = new StringBuffer();
        String errorDescr = perseusException.getLocalizedMessage();
        if (errorDescr == null) {
            errorDescr = perseusException.getClass().getName();
        }
        final StringBuffer perseusError = new StringBuffer(errorDescr);
        updateText( composeMessageText(perseusError, saxErrors, null));
        final int [] errorCount = new int[3];

        ErrorHandler errorHandler = new ErrorHandler() {                
            public void warning(SAXParseException e) throws SAXException {
                errorCount[2]++;
                addError("Warning", e);
            }

            public void fatalError(SAXParseException e) throws SAXException {
                errorCount[0]++;
                addError("Fatal Error", e);
            }

            public void error(SAXParseException e) throws SAXException {
                errorCount[1]++;
                addError("Error", e);
            }

            private void addError(String errorType, SAXParseException e) {
                String msg = e.getLocalizedMessage();
                if ( msg.equals(perseusError.toString())) {
                    perseusError.setLength(0);
                }
                saxErrors.append("\n");
                saxErrors.append(errorType);
                saxErrors.append( ": ");
                saxErrors.append( e.getLineNumber());
                saxErrors.append( ",");
                saxErrors.append( e.getColumnNumber());
                saxErrors.append( ":"); 
                saxErrors.append( msg); 
                updateText(composeMessageText(perseusError, saxErrors, null));
            }
        };

        java.io.StringBufferInputStream in = new java.io.StringBufferInputStream(fileText);
        try { 
            InputSource isource = new InputSource(in);
            System.out.println("Parsing XML document");
            XMLUtil.parse( isource, true, true, errorHandler, EntityCatalog.getDefault());
        } catch (SAXParseException e) {
            //Not interested in these errors ...
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + " " + e.getLocalizedMessage());
            e.printStackTrace();
            //Not interested in these errors ...
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (errorCount[0] == 0 && errorCount[1] == 0) {
                errorCount[0] = 1;
            }
            updateText(composeMessageText(perseusError, saxErrors, errorCount));
        }
    }

    private void updateText(final String errorMsg) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                textArea.setText(errorMsg);
                textArea.validate();
                textArea.repaint();
            }
        });
    }
}
