package org.netbeans.modules.xml.multiview.test;

import org.netbeans.modules.xml.multiview.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.ErrorManager;

import java.io.IOException;
import java.io.OutputStream;

import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.spi.xml.cookies.*;

import org.netbeans.modules.xml.multiview.test.bookmodel.*;

/**
 *
 * @author mkuchtiak
 */
public class BookDataObject extends XmlMultiViewDataObject {

    private ModelSynchronizer modelSynchronizer;

    private static final int TYPE_TOOLBAR = 0;
    private static final int TYPE_TREEPANEL = 1;
    Book book;
    
    /** Creates a new instance of BookDataObject */  
    public BookDataObject (FileObject pf, BookDataLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        modelSynchronizer = new ModelSynchronizer(this);
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        try {
            parseDocument();
        } catch (IOException ex) {
            System.out.println("ex="+ex);
        }
    }
    /**
     *
     * @throws IOException
     */
    protected void parseDocument() throws IOException {
        if (book==null) {
            book = getBook();
        } else {
            java.io.InputStream is = getEditorSupport().getInputStream();
            Book newBook = null;
            try {
                newBook = Book.createGraph(is);
            } catch (RuntimeException ex) {
                System.out.println("runtime error "+ex);
            }
            if (newBook!=null) {
                book.merge(newBook, org.netbeans.modules.schema2beans.BaseBean.MERGE_UPDATE);
            }
        }
    }
    
    public Book getBook() throws IOException {
        if (book==null) book = Book.createGraph(FileUtil.toFile(getPrimaryFile()));
        return book;
    }

    /** Update text document from data model. Called when something is changed in visual editor.
    */
    protected String generateDocumentFromModel() {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        try {
            getBook().write(out);
            out.close();
            return out.toString("UTF8"); //NOI18N
        }
        catch (IOException e) {
            org.openide.ErrorManager.getDefault ().notify(org.openide.ErrorManager.INFORMATIONAL, e);
        }
	return out.toString ();
    }
    
    protected DesignMultiViewDesc[] getMultiViewDesc() {
        return new DesignMultiViewDesc[]{new DesignView(this,TYPE_TOOLBAR),new DesignView(this,TYPE_TREEPANEL)};
    }

    private static class DesignView extends DesignMultiViewDesc {
        private int type;
        DesignView(BookDataObject dObj, int type) {
            super(dObj, "Design"+String.valueOf(type));
            this.type=type;
        }

        public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
            BookDataObject dObj = (BookDataObject)getDataObject();
            if (type==TYPE_TOOLBAR) return new BookToolBarMVElement(dObj);
            else return new BookTreePanelMVElement(dObj);
        }

        public java.awt.Image getIcon() {
            return org.openide.util.Utilities.loadImage("org/netbeans/modules/j2ee/ddloaders/resources/DDDataIcon.gif"); //NOI18N
        }

        public String preferredID() {
            return "book_multiview_design"+String.valueOf(type);
        }
    }
    
    /** Enable to focus specific object in Multiview Editor
     *  The default implementation opens the XML View
     */
    public void showElement(Object element) {
        Object target=null;
        if (element instanceof Chapter) {
            openView(0);
            target=element;
        }
        if (target!=null) {
            final Object key=target;
            org.netbeans.modules.xml.multiview.Utils.runInAwtDispatchThread(new Runnable() {
                public void run() {
                    getActiveMultiViewElement0().getSectionView().openPanel(key);
                }
            });
        }
    }

    protected String getPrefixMark() {
        return null;
    }

    /** Enable to get active MultiViewElement object
     */
    public ToolBarMultiViewElement getActiveMultiViewElement0() {
        return (ToolBarMultiViewElement)super.getActiveMultiViewElement();
    }
    
    public void modelUpdatedFromUI() {
        modelSynchronizer.requestUpdateData();
    }

    private class ModelSynchronizer extends XmlMultiViewDataSynchronizer {

        public ModelSynchronizer(XmlMultiViewDataObject dataObject) {
            super(dataObject, 500);
        }

        protected boolean mayUpdateData(boolean allowDialog) {
            return true;
        }

        protected void updateDataFromModel(FileLock lock, boolean modify) {
            try {
                OutputStream outputStream = getDataCache().createOutputStream(lock, modify);
                try {
                    outputStream.write(generateDocumentFromModel().getBytes());
                } finally {
                    outputStream.close();
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        protected void reloadModelFromData() {
            try {
                parseDocument();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
}
