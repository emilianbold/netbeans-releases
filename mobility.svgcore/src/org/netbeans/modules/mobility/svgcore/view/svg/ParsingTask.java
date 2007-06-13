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
import java.io.StringBufferInputStream;
import javax.microedition.m2g.SVGImage;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.PerseusController;
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
    private final String              text;
    //private final String              svgElementText;

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
        text = dObj.getModel().writeToString();
      //  svgElementText = dObj.getModel().getSVGHeader();
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
        StringBufferInputStream in;
        try {
            in = new StringBufferInputStream(text);
            try {           
                final SVGImage svgImage = (SVGImage) PerseusController.createImage( in);
//                final SVGImage svgImage = (SVGImage) PerseusController.createImage( in, svgElementText);
//                final SVGImage svgImage = (SVGImage) ScalableImage.createImage( in, null);
                assert svgImage != null;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        svgView.showImage(svgImage);
                    }
                });  
            } catch (IOException ex) {
                showParsingError(text, ex);
            } finally {
                in.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
    private void showParsingError(String fileText, IOException perseusException) {            
        final StringBuffer saxErrors    = new StringBuffer();
        final StringBuffer perseusError = new StringBuffer(perseusException.getLocalizedMessage());
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
