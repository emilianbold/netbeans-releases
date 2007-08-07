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
 */
package org.netbeans.modules.mobility.svgcore;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
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
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Pavel Benes
 */
@SuppressWarnings({"unchecked"})
public class SVGDataObject extends XmlMultiViewDataObject {
    private static final long serialVersionUID = 123471457562776148L;
    
    public static final int    XML_VIEW_INDEX   = 0;
    public static final int    SVG_VIEW_INDEX   = 1;
    public static final String PROP_EXT_CHANGE  = "external_change"; //NOI18N
    
    private static final Image SVGFILE_ICON = org.openide.util.Utilities.loadImage("org/netbeans/modules/mobility/svgcore/resources/svg.png"); // NOI18N
    
    private transient SVGFileModel m_model;
    private transient SceneManager m_sceneManager;
    private transient boolean      m_wasSaved = false;
    
    private final DataCache m_dataCache = new XmlMultiViewDataObject.DataCache() {
        public void loadData(FileObject file, FileLock dataLock) throws IOException {
            if ( isSVGZ(file.getExt())) {
                file = new FileObjectGZIPDelegator(file);
            } 
            super.loadData(file, dataLock);
        }
    };

    private class SVGEditorSupport extends XmlMultiViewEditorSupport {
        public SVGEditorSupport() {
            super(SVGDataObject.this);
            env.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange( PropertyChangeEvent evt) {
                    if ( CloneableEditorSupport.Env.PROP_TIME.equals( evt.getPropertyName())) {
                        if ( !m_wasSaved) {
                            m_wasSaved = true;
                            fireContentChanged();
                        }
                    }
                }
            });
        }

        protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream)
        throws IOException, BadLocationException {
            FileObject fo = getPrimaryFile();
            
            m_wasSaved = true;
            
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
    }
    
    private static class VisualView extends DesignMultiViewDesc {
        private static final long serialVersionUID = 7526471457562776148L;
        
        VisualView(SVGDataObject dObj) {
            super(dObj, NbBundle.getMessage(SVGDataObject.class, "LBL_MULVIEW_TITLE_VIEW")); //NOI18N
        }
        
        public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
            SVGDataObject dObj = (SVGDataObject)getDataObject();
            return new SVGViewMultiViewElement(dObj);
        }
        
        public java.awt.Image getIcon() {
            return SVGFILE_ICON;
        }
        
        public String preferredID() {
            return "multiview_svgview"; //NOI18N
        }
        
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ONLY_OPENED;
        }
    }
    
    private static class XMLTextView extends DesignMultiViewDesc {
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
        
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ONLY_OPENED;
        }
    }
    
    public SVGDataObject(FileObject pf, SVGDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        //System.out.println("> SVGDataObject()");
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
    }
        
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
    
    public synchronized SVGFileModel getModel() {
        if (m_model == null) {
            m_model = new SVGFileModel( getEditorSupport());
        }
        return m_model;
    }
    
    public synchronized SceneManager getSceneManager() {
        if (m_sceneManager == null) {
            m_sceneManager = new SceneManager();
            m_sceneManager.initialize(this);
        }
        return m_sceneManager;        
    }
       
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
    
    protected Node createNodeDelegate() {
        return new SVGDataNode(this);
    }
    
    protected String getPrefixMark() {
        return null;
    }   

    public static boolean isSVGZ(String fileExt) {
        return "svgz".equals(fileExt.toLowerCase()); //NOI18N
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
                java.io.InputStream in = m_delegate.getInputStream();
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
