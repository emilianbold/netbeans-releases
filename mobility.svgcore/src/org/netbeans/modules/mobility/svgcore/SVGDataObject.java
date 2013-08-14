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
 */package org.netbeans.modules.mobility.svgcore;

import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.netbeans.modules.mobility.svgcore.view.svg.SVGViewMultiViewElement;
import org.netbeans.modules.mobility.svgcore.view.source.SVGSourceMultiViewElement;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.*;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 *
 * @author Pavel Benes
 */
@MIMEResolver.ExtensionRegistration(
    mimeType="image/svg+xml",
    position=260,
    displayName="#SVGResolver",
    extension={ "svg", "SVG", "svgz", "SVGZ" }
)
@SuppressWarnings({"unchecked"})
public final class SVGDataObject extends XmlMultiViewDataObject {
    private static final long  serialVersionUID = 123471457562776148L;
    private static final Image SVGFILE_ICON     = ImageUtilities.loadImage("org/netbeans/modules/mobility/svgcore/resources/svg.png"); // NOI18N

    public static final int    XML_VIEW_INDEX   = 0;
    public static final int    SVG_VIEW_INDEX   = 1;
    public static final String PROP_EXT_CHANGE  = "external_change"; //NOI18N
    public static final String EXT_SVG          = "svg"; //NOI18N
    public static final String EXT_SVGZ         = "svgz"; //NOI18N
        
    private transient SVGFileModel m_model;
    private transient SceneManager m_sceneManager;
    private transient MultiViewElement m_activeElement = null;
    
    private final DataCache m_dataCache = new XmlMultiViewDataObject.DataCache() {
        @Override
        public void loadData(FileObject file, FileLock dataLock) throws IOException {
            if ( isSVGZ(file.getExt())) {
                file = new FileObjectGZIPDelegator(file);
            } 
            super.loadData(file, dataLock);
        }
    };

    private final class SVGEditorSupport extends XmlMultiViewEditorSupport {
        private static final long  serialVersionUID = 123471457562776148L;
        private Logger LOG = Logger.getLogger(SVGDataObject.SVGEditorSupport.class.getName());
        private String m_errorMessage = null;

        public SVGEditorSupport() {
            super(SVGDataObject.this);
        }

        @Override
        protected void notifyClosed() {
            super.notifyClosed();
            release();
        }

        @Override
        public void open() {
            if (getModel().getModel() != null || (getDocument() != null && getDocument().getLength() == 0)){
                super.open();
            } else {
                showErrorDialog(m_errorMessage);
            }
        }

        @Override
        public void edit() {
            if (getModel().getModel() != null || (getDocument() != null && getDocument().getLength() == 0)){
                super.edit();
            } else {
                showErrorDialog(m_errorMessage);
            }
        }

        @Override
        public StyledDocument openDocument() throws IOException {
            m_errorMessage = null;
            try{
                return super.openDocument();
            } catch (IOException io){
                m_errorMessage = "Could not open the document: " + io.getMessage();
                LOG.log(Level.WARNING, io.getMessage());
                throw io;
            }
        }

        @Override
        protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream)
            throws IOException, BadLocationException {
            FileObject fo = getPrimaryFile();
            
            if ( isSVGZ(fo.getExt())) {
                GZIPOutputStream gzipStream = new GZIPOutputStream(stream);            
                
                try {                    
                    kit.write(new OutputStreamWriter( gzipStream, getEncodingHelper().getEncoding()), doc, 0, doc.getLength());
                } finally {
                    gzipStream.close();
                }
            } else {
                kit.write(new OutputStreamWriter(stream, getEncodingHelper().getEncoding()), doc, 0, doc.getLength());                
            }
        }

        private void showErrorDialog(String message) {
            if (message != null) {
                NotifyDescriptor.Message e = new NotifyDescriptor.Message(
                        message,
                        NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(e);
            }
        }

    }
    
    private static final class VisualView extends DesignMultiViewDesc {
        private static final long serialVersionUID = 7526471457562776148L;
        //private static final  HelpCtx DEFAULT_HELP = new HelpCtx("MOBILITY.SVG.COMPOSER_VIEW"); // NOI18N
        private static final  HelpCtx DEFAULT_HELP = new HelpCtx(HelpCtx.class.getName() + ".DEFAULT_HELP"); // NOI18N
        
        VisualView(SVGDataObject dObj) {
            super(dObj, NbBundle.getMessage(SVGDataObject.class, "LBL_MULVIEW_TITLE_VIEW")); //NOI18N
        }
        
        public MultiViewElement createElement() {
            SVGDataObject dObj = (SVGDataObject)getDataObject();
            return new SVGViewMultiViewElement(dObj);
        }
        
        public Image getIcon() {
            return SVGFILE_ICON;
        }
        
        public String preferredID() {
            return "multiview_svgview"; //NOI18N
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return DEFAULT_HELP;
        }        
        
        @Override
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ONLY_OPENED;
        }
    }
    
    private static final class XMLTextView extends DesignMultiViewDesc {
        private static final long serialVersionUID = 7526471457562776147L;
        
        XMLTextView(SVGDataObject dObj) {
            super( dObj, NbBundle.getMessage(SVGDataObject.class, "LBL_MULVIEW_TITLE_SOURCE")); //NOI18N
        }
        
        public MultiViewElement createElement() {
            return new SVGSourceMultiViewElement( (SVGDataObject) getDataObject());
        }
        
        public java.awt.Image getIcon() {
            return ((SVGDataObject) getDataObject()).getXmlViewIcon();
        }
        
        public String preferredID() {
            return "multiview_xml"; //NOI18N
        }
        
        @Override
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ONLY_OPENED;
        }
    }
    
    public SVGDataObject(FileObject pf, SVGDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        XmlMultiViewEditorSupport edSup = getEditorSupport();
        edSup.setSuppressXmlView(true);
        setLastOpenView( SVG_VIEW_INDEX);
        //call the method getMultiViewDescriptions() to
        //recalculate the xmlMultiViewIndex member after default
        //XML view has been suppressed.
        edSup.getMultiViewDescriptions();
        getCookieSet().assign( SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs(FileObject folder, String fileName) throws IOException {
                getEditorSupport().saveAs( folder, fileName );
            }
        });  
        SceneManager.log(Level.INFO, "SVGDataObject created for " + pf.getPath()); //NOI18N
    }
        
    @Override
    public DataCache getDataCache() {
        return m_dataCache;
    }

    public void fireContentChanged() {
        firePropertyChange(PROP_EXT_CHANGE, null, null);    
    }
    
    public TopComponent getMTVC() {
        getCookieSet();
        return getEditorSupport().getMVTC();        
    }
    
    public MultiViewElement getActiveElement() {
        return m_activeElement;
    }
    
    public void setMultiViewElement(MultiViewElement active) {
        m_activeElement = active;
    }
    
    public synchronized SVGFileModel getModel() {
        if (m_model == null) {
            m_model = new SVGFileModel( getEditorSupport());
            SceneManager.log(Level.INFO, "Model created for " + getPrimaryFile().getPath());//NOI18N
        }
        return m_model;
    }
    
    public synchronized SceneManager getSceneManager() {
        if (m_sceneManager == null) {
            m_sceneManager = new SceneManager();
            SceneManager.log(Level.INFO, "SceneManager created for " + getPrimaryFile().getPath()); //NOI18N
            m_sceneManager.initialize(this);
        }
        return m_sceneManager;        
    }
    
    public static boolean isSVGZ(String fileExt) {
        return EXT_SVGZ.equals(fileExt.toLowerCase()); 
    }

    private synchronized void release() {
        if (m_model != null) {
            m_model.detachDocument();
        }
        m_model = null;
        m_sceneManager = null; 
        SceneManager.log(Level.INFO, "SVGDataObject released for " + getPrimaryFile().getPath()); //NOI18N
    }
    
    @Override
    protected synchronized XmlMultiViewEditorSupport getEditorSupport() {
        if(editorSupport == null) {
            editorSupport = new SVGEditorSupport();
            editorSupport.getMultiViewDescriptions();
        }
        return editorSupport;
    }

    protected DesignMultiViewDesc[] getMultiViewDesc() {
        return new DesignMultiViewDesc[]{
            new XMLTextView(this),
            new VisualView(this)
        };
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new SVGDataNode(this);
    }
    
    protected String getPrefixMark() {
        return null;
    }   
    
    public static boolean isSVGFile( File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith('.' + EXT_SVG) || name.endsWith( '.' + EXT_SVGZ);
    }
    
    public static SVGDataObject getActiveDataObject(java.awt.Container comp) {
        while( comp != null) {
            if ( comp instanceof CloneableTopComponent) {
                SVGDataObject dObj = ((CloneableTopComponent) comp).getLookup().lookup(SVGDataObject.class);
                if ( dObj != null) {
                    return dObj;
                } 
            }
            comp = comp.getParent();
        }
        return null;
    }
    
    @SuppressWarnings({"deprecation"})
    private static final class FileObjectGZIPDelegator extends FileObject {
        private final FileObject m_delegate;
        
        public FileObjectGZIPDelegator(FileObject delegate) {
            m_delegate = delegate;
        }
        
        public String getName() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public String getExt() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public void rename(FileLock lock, String name, String ext) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public FileSystem getFileSystem() throws FileStateInvalidException {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public FileObject getParent() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public boolean isFolder() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public Date lastModified() {
            return m_delegate.lastModified();
        }

        public boolean isRoot() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public boolean isData() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public boolean isValid() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public void delete(FileLock lock) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public Object getAttribute(String attrName) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public void setAttribute(String attrName, Object value) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public Enumeration<String> getAttributes() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public void addFileChangeListener(FileChangeListener fcl) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public void removeFileChangeListener(FileChangeListener fcl) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public long getSize() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public InputStream getInputStream() throws FileNotFoundException {
            try {
                InputStream in = m_delegate.getInputStream();
                return new GZIPInputStream(in);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        public OutputStream getOutputStream(FileLock lock) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public FileLock lock() throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        @SuppressWarnings({"deprecation"})
        public void setImportant(boolean b) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public FileObject[] getChildren() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public FileObject getFileObject(String name, String ext) {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public FileObject createFolder(String name) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public FileObject createData(String name, String ext) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        
        @SuppressWarnings({"deprecation"})
        public boolean isReadOnly() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }        
    }    
}
