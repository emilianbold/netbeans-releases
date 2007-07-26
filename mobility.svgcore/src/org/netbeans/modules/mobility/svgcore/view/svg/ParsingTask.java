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
import java.io.IOException;
import javax.microedition.m2g.SVGImage;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.netbeans.modules.mobility.svgcore.view.source.SVGSourceMultiViewElement;
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
final class ParsingTask extends Thread implements HyperlinkListener { 
    private final JPanel              m_panel;
    private final JEditorPane         m_textPane;
    private final SVGViewTopComponent m_svgView;
    private final SVGFileModel        m_fileModel;
    private final DocumentModel       m_docModel;

    public ParsingTask(SVGDataObject dObj, SVGViewTopComponent svgView) throws Exception {
        m_svgView = svgView;
        m_panel = new javax.swing.JPanel();
        m_panel.setLayout(new java.awt.BorderLayout());
        m_panel.setBackground(Color.WHITE);
        StringBuilder sb = new StringBuilder(NbBundle.getMessage(SVGViewTopComponent.class, "MSG_Parsing"));
        wrapAsHtml(sb);
        m_textPane = new JEditorPane("text/html", sb.toString());
        m_textPane.setBackground(Color.WHITE);
        m_textPane.setEditable(false);
        
        m_textPane.addHyperlinkListener( this);
        //Font font = m_textArea.getFont();
        //m_textArea.setFont(font.deriveFont(16.0F));
        m_panel.add(new JScrollPane(m_textPane), BorderLayout.CENTER);
        setPriority( Thread.MIN_PRIORITY);
        m_fileModel = dObj.getModel();
        // ensure that model is valid
        m_docModel  = m_fileModel._getModel();
    }

    public JComponent getPanel() {
        return m_panel;
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
                        m_svgView.showImage(svgImage);
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

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED &&
             e.getDescription() != null) {            
            int [] position = string2position(e.getDescription());
            if (position != null) {
                int offset = m_fileModel.getOffsetByPosition(position[0], position[1]);
                SVGSourceMultiViewElement.selectPosition(m_fileModel.getDataObject(),
                                                         offset, true);
            }
        }        
    }
    
    private String composeMessageText( StringBuffer perseusError,
                                       StringBuffer saxErrors, int [] errNum) {
        StringBuilder sb = new StringBuilder();
        sb.append(NbBundle.getMessage(SVGViewTopComponent.class, "ERR_NotSvgTiny"));
        if (perseusError.length() > 0) {
            sb.append("<br>");
            sb.append( perseusError);
        }
        sb.append( "<font size=\"4\" color=\"blue\">");
        sb.append(saxErrors);
        sb.append( "</font>");
        sb.append("<br>");
        if ( errNum == null) {
            sb.append(NbBundle.getMessage(SVGViewTopComponent.class, "ERR_Additional"));                            
        } else {
            sb.append(NbBundle.getMessage(SVGViewTopComponent.class, "ERR_ErrorWarnings",
                    Integer.toString(errNum[0]), Integer.toString(errNum[1]), Integer.toString(errNum[2]))); 
        }
        wrapAsHtml(sb);
        return sb.toString();
    }

    private static void wrapAsHtml(StringBuilder sb) {
        sb.insert( 0, "<html><body><font face=\"Monospaced\" size=\"4\" color=\"black\">");
        sb.append("</font></body><html>");
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
                saxErrors.append("<br>");
                saxErrors.append("<a href=\"");
                saxErrors.append( position2string(e.getLineNumber(), e.getColumnNumber()));
                saxErrors.append( "\">");
                saxErrors.append(errorType);
                saxErrors.append( ": ");
                saxErrors.append( e.getLineNumber());
                saxErrors.append( ",");
                saxErrors.append( e.getColumnNumber());
                saxErrors.append( ":"); 
                saxErrors.append( msg); 
                saxErrors.append("</a>");
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
                m_textPane.setText(errorMsg);
                m_textPane.validate();
                m_textPane.repaint();
            }
        });
    }
    
    private static String position2string(int lineNum, int colNumber) {
        return lineNum + "_" + colNumber;
    }
    
    private static int [] string2position(String str) {
        int [] position = null;
        String [] parts = str.split("_");
        
        if (parts.length == 2) {
            try {
                position = new int[] { Integer.parseInt(parts[0]),
                                       Integer.parseInt(parts[1])};            
            } catch( NumberFormatException e) {
                e.printStackTrace();
            }
        }
        
        return position;
    }
}
