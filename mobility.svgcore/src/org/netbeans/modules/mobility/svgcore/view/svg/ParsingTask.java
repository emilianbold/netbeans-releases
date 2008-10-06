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

package org.netbeans.modules.mobility.svgcore.view.svg;

import com.sun.perseus.platform.ThreadSupport;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.m2g.SVGImage;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.PerseusController;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.model.EncodingInputStream;
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
    private static final String HTML_BEGIN  = "<html><body><font face=\"Monospaced\" size=\"4\" color=\"black\">"; //NOI18N
    private static final String HTML_END    = "</font></body><html>"; //NOI18N
    private static final String PARSE_TOKEN = "parse"; //NOI18N
    private static final Logger LOG = Logger.getLogger(ParsingTask.class.getName());

    private static final class ErrorDescription {      
        private static final int SEVERITY_FATAL   = 0;
        private static final int SEVERITY_ERROR   = 1;
        private static final int SEVERITY_WARNING = 2;

        private static final String [] SEVERITIES = new String[] {
            "Fatal Error", "Error", "Warning" }; //NOI18N

        final String m_text;
        final int    m_severity;
              int    m_line;
              int    m_column;
        
        public ErrorDescription(SAXParseException e, int severity) {
            assert e != null;
            m_text     = getMessage(e);
            m_severity = severity;
            m_line     = e.getLineNumber();
            m_column   = e.getColumnNumber();
        }
        
        public ErrorDescription(Exception e) {
            m_text     = getMessage(e);
            m_severity = SEVERITY_FATAL;
            m_line = m_column = -1;
        }
        
        public String getSeverity() {
            return SEVERITIES[m_severity];
        }
        
        public void fillNumbers(SVGFileModel fileModel) {
            if ( m_line == -1) {
                int p1 = m_text.indexOf('"');
                if (p1 != -1 && m_text.indexOf("\" is missing on element") == -1) { //NOI18N
                    int p2 = m_text.lastIndexOf('"');
                    if ( p2 > p1) {
                        String invalidValue = m_text.substring(p1, p2+1);
                        p2 = fileModel.firstIndexOf(invalidValue);
                        if ( p2 != -1) {
                            int [] pos = fileModel.getPositionByOffset(p2);
                            if (pos != null) {
                                m_line   = pos[0];
                                m_column = pos[1];
                            }
                        }
                    }
                }
            }
        }
        
        private static String getMessage( Exception e) {
            String msg = e.getLocalizedMessage();
            if (msg == null) {
                msg = e.getClass().getName();
            }
            return msg;
        }
    }

    private final SVGDataObject       m_dObj;
    private final JPanel              m_panel;
    private final JEditorPane         m_textPane;
    private final SVGViewTopComponent m_svgView;

    public ParsingTask(SVGDataObject dObj, SVGViewTopComponent svgView) throws Exception {
        m_dObj = dObj;
        m_svgView = svgView;
        m_panel = new javax.swing.JPanel();
        m_panel.setLayout(new java.awt.BorderLayout());
        m_panel.setBackground(Color.WHITE);
        StringBuilder sb = new StringBuilder(HTML_BEGIN);
        sb.append(NbBundle.getMessage(SVGViewTopComponent.class, "MSG_Parsing")); //NOI18N
        sb.append( HTML_END);
        m_textPane = new JEditorPane("text/html", sb.toString()); //NOI18N
        m_textPane.setBackground(Color.WHITE);
        m_textPane.setEditable(false);
        
        m_textPane.addHyperlinkListener( this);
        m_panel.add(new JScrollPane(m_textPane), BorderLayout.CENTER);
        setPriority( Thread.MIN_PRIORITY);
        setDaemon(true);
        
        if ( m_svgView.getBasePanel().getComponentCount() == 0) {
            initializeReportView();
            m_svgView.repaint();
        }
    }

    public JComponent getPanel() {
        return m_panel;
    }

    public void cancel() {
        interrupt();
    }

    public void run() {
        try {
            SVGFileModel fileModel = m_dObj.getModel();
            try {
                m_dObj.getSceneManager().setBusyState(PARSE_TOKEN, true);
                System.setErr( new PrintStream(new OutputStream() {
                    public void write(int b) throws IOException {
                    }
                }));
                if (!Thread.currentThread().isInterrupted()) {
                    final SVGImage svgImage = fileModel.parseSVGImage();
                    assert svgImage != null;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            m_svgView.showImage(svgImage);
                            m_svgView.enableImageContext();
                        }
                    });
                } else {
                    throw new InterruptedException();
                }
            } catch(InterruptedException ie){
                LOG.log(Level.INFO, null, ie);
            } catch(Exception e) {
                showParsingErrors(fileModel.getModel().getDocument(), e);
            } finally {
                m_dObj.getSceneManager().setBusyState(PARSE_TOKEN, false);
                System.setErr(System.err);
            }
        } catch(Exception e) {
            LOG.log(Level.WARNING, null, e);
        }
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED &&
             e.getDescription() != null) {            
            int [] position = string2position(e.getDescription());
            if (position != null) {
                SVGFileModel fileModel = m_dObj.getModel();
                int offset = fileModel.getOffsetByPosition(position[0], position[1]);
                SVGSourceMultiViewElement.selectPosition(m_dObj,offset, true);
            }
        }        
    }
          
    private String composeMessageText( List<ErrorDescription> errors) {
        StringBuilder sb = new StringBuilder(HTML_BEGIN);
        
        sb.append(NbBundle.getMessage(SVGViewTopComponent.class, "ERR_NotSvgTiny")); //NOI18N
        sb.append( "<font size=\"4\" color=\"blue\">"); //NOI18N
        int [] errNum = new int[3];
        
        for (ErrorDescription error : errors) {
            errNum[error.m_severity]++;
            
            sb.append("<br>"); //NOI18N
            if ( error.m_line != -1) {
                sb.append("<a href=\""); //NOI18N
                sb.append( position2string(error.m_line, error.m_column));
                sb.append( "\">"); //NOI18N
                sb.append( error.getSeverity());
                sb.append( ": "); //NOI18N
                sb.append( error.m_line);
                sb.append( ","); //NOI18N
                sb.append( error.m_column);
                sb.append( ":"); //NOI18N
                sb.append( error.m_text); 
                sb.append("</a>"); //NOI18N
            } else {
                sb.append( "<font color=\"black\">"); //NOI18N                
                sb.append(error.getSeverity());
                sb.append( ": "); //NOI18N
                sb.append( error.m_text); 
                sb.append("</font>"); //NOI18N
            }
        }
        sb.append("</font>"); //NOI18N
        sb.append("<br>"); //NOI18N
        sb.append(NbBundle.getMessage(SVGViewTopComponent.class, "ERR_ErrorWarnings", //NOI18N
                  Integer.toString(errNum[0]), Integer.toString(errNum[1]), Integer.toString(errNum[2]))); 
        sb.append(HTML_END);
        return sb.toString();
    }

    private void showParsingErrors(Document doc, Exception perseusException) {
        final List<ErrorDescription> errors = new ArrayList<ErrorDescription>();
        
        errors.add( new ErrorDescription(perseusException));

        ErrorHandler errorHandler = new ErrorHandler() {                
            public void warning(SAXParseException e) throws SAXException {
                addError( new ErrorDescription( e, ErrorDescription.SEVERITY_WARNING));
            }

            public void fatalError(SAXParseException e) throws SAXException {
                addError( new ErrorDescription( e, ErrorDescription.SEVERITY_FATAL));
            }

            public void error(SAXParseException e) throws SAXException {
                addError( new ErrorDescription( e, ErrorDescription.SEVERITY_ERROR));
            }
            
            private void addError( ErrorDescription errDesc) {
                for ( ErrorDescription ed : errors) {
                    if (ed.m_text.equals(errDesc.m_text)) {
                        if ( ed.m_line == -1 && errDesc.m_line != -1) {
                            ed.m_line   = errDesc.m_line;
                            ed.m_column = errDesc.m_column;
                        }
                        return;
                    }
                }
                errors.add(errDesc);
            }
        };

        InputStream in = new EncodingInputStream( (BaseDocument) doc, m_dObj.getEncodingHelper().getEncoding());
        try { 
            InputSource isource = new InputSource(in);
            XMLUtil.parse( isource, true, true, errorHandler, EntityCatalog.getDefault());
        } catch( SAXException e) { 
            //Not interested in these errors ...
        }
        // Fix for IZ#145734 .
        catch ( ConnectException e ){
            /*
             * Ignore this exception . It can be caused by connection 
             * error from entity resolver.
             */
        }    
        catch (Exception e) {
            SceneManager.error("Error during document parse", e); //NOI18N
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                SceneManager.error("Could not close stream", ex); //NOI18N
            }
        }
        
        for ( ErrorDescription ed : errors) {
            ed.fillNumbers( m_dObj.getModel());
        }
        
        updateText(composeMessageText( errors));
    }

    private void initializeReportView() {
        JPanel basePanel = m_svgView.getBasePanel();
        if ( basePanel.getComponentCount() > 0) {
            if ( basePanel.getComponent(0) == m_panel) {
                return;
            } else {
                basePanel.removeAll();
            }
        }
        basePanel.add( m_panel, BorderLayout.CENTER);
        basePanel.invalidate();
    }
    
    private void updateText(final String errorMsg) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                initializeReportView();
                m_textPane.setText(errorMsg);
                m_textPane.invalidate();
                m_svgView.validate();
                m_svgView.repaint();
            }
        });
    }
    
    private static String position2string(int lineNum, int colNumber) {
        return lineNum + "_" + colNumber; //NOI18N
    }
    
    private static int [] string2position(String str) {
        int [] position = null;
        String [] parts = str.split("_"); //NOI18N
        
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
