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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.impl.common.DDProviderDataObject;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.common.TransactionSupport;
import org.netbeans.modules.j2ee.common.Transaction;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.lang.ref.WeakReference;

/**
 * @author pfiala
 */
public abstract class DDMultiViewDataObject extends XmlMultiViewDataObject
        implements DDProviderDataObject, TransactionSupport {


    private WeakReference transactionReference = null;
    private static final int HANDLE_UNPARSABLE_TIMEOUT = 2000;
    private DDMultiViewDataObject.ModelSynchronizer modelSynchronizer;

    public DDMultiViewDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        modelSynchronizer = new ModelSynchronizer(this);
    }

    public void modelUpdatedFromUI() {
        modelSynchronizer.requestUpdateData();
    }

    public XmlMultiViewDataSynchronizer getModelSynchronizer() {
        return modelSynchronizer;
    }

    public void checkParseable() {
        if (!isDocumentParseable()) {
            NotifyDescriptor desc = new org.openide.NotifyDescriptor.Message(
                    NbBundle.getMessage(XmlMultiViewDataObject.class, "TXT_DocumentUnparsable",
                            getPrimaryFile().getNameExt()), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            if (!isModelCreated()) {
                goToXmlView();
            }
        }
    }

    public InputStream createInputStream() {
        return getDataCache().createInputStream();
    }

    public void writeModel(RootInterface model) throws IOException {
        if (transactionReference != null && transactionReference.get() != null) {
            return;
        }
        FileLock dataLock = waitForLock();
        if (dataLock == null) {
            return;
        }
        try {
            if (((ModelSynchronizer) getModelSynchronizer()).mayUpdateData(true)) {
                writeModel(model, dataLock);
            }
        } finally {
            dataLock.releaseLock();
        }
    }

    public void writeModel(RootInterface model, FileLock dataLock) {
        ModelSynchronizer synchronizer = (ModelSynchronizer) getModelSynchronizer();
        ((RootInterface) synchronizer.getModel()).merge(model, RootInterface.MERGE_UPDATE);
        synchronizer.updateData(dataLock, false);
    }

    public FileLock getDataLock() {
        try {
            return getModelSynchronizer().takeLock();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }

    /**
     * Used to detect if data model has already been created or not.
     * Method is called before switching to the design view from XML view when the document isn't parseable.
     */
    protected abstract boolean isModelCreated();

    /**
     * @throws IOException
     */
    protected abstract void parseDocument() throws IOException;

    /**
     * @throws IOException
     */
    protected abstract void validateDocument() throws IOException;

    /**
     * Update text document from data model. Called when something is changed in visual editor.
     * @param model
     */
    protected String generateDocumentFromModel(RootInterface model) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            model.write(out);
            out.close();
            return out.toString("UTF8"); //NOI18N
        } catch (IOException e) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
        } catch (IllegalStateException e) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
        }
        return out.toString ();
    }

    /**
     * Returns model of the deployment descriptor
     * @return the model
     */
    protected abstract RootInterface getDDModel();

    /**
     * Returns true if xml file is parseable(data model can be created),
     * Method is called before switching to the design view from XML view when the document isn't parseable.
     */
    protected abstract boolean isDocumentParseable();

    public Transaction openTransaction() {
        final XmlMultiViewDataSynchronizer.Transaction synchronizerTransaction = getModelSynchronizer().openTransaction();
        if (synchronizerTransaction == null) {
            return null;
        } else {
            Transaction transaction = new Transaction() {
                public void rollback() {
                    synchronizerTransaction.rollback();
                    transactionReference = null;
                }

                public void commit() throws IOException {
                    synchronizerTransaction.commit();
                    transactionReference = null;
                }
            };
            transactionReference = new WeakReference(transaction);
            return transaction;
        }
    }

    private class ModelSynchronizer extends XmlMultiViewDataSynchronizer {
        private long handleUnparseableTimeout = 0;
        private Boolean overwriteUnparseable = Boolean.TRUE;

        public ModelSynchronizer(XmlMultiViewDataObject dataObject) {
            super(dataObject, 300);
            handleUnparseableTimeout = 0;
            overwriteUnparseable = Boolean.TRUE;
        }

        protected boolean mayUpdateData(boolean allowDialog) {
            if (isDocumentParseable()) {
                return true;
            }
            if (!allowDialog) {
                return false;
            }
            if (handleUnparseableTimeout != -1) {
                long time = new Date().getTime();
                if (time > handleUnparseableTimeout) {
                    handleUnparseableTimeout = -1;
                    org.netbeans.modules.xml.multiview.Utils.runInAwtDispatchThread(new Runnable() {
                        public void run() {
                            String message = NbBundle.getMessage(XmlMultiViewDataObject.class,
                                    "TXT_OverwriteUnparsableDocument", getPrimaryFile().getNameExt());
                            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message,
                                    NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                            overwriteUnparseable = Boolean.valueOf(desc.getValue() == NotifyDescriptor.YES_OPTION);
                            handleUnparseableTimeout = new Date().getTime() + HANDLE_UNPARSABLE_TIMEOUT;
                        }
                    });
                }
            }
            return overwriteUnparseable.booleanValue();
        }

        public void updateData(FileLock dataLock, boolean modify) {
            super.updateData(dataLock, modify);
            try {
                validateDocument();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
        }

        protected void updateDataFromModel(Object model, FileLock lock, boolean modify) {
            String newDocument = generateDocumentFromModel((RootInterface) model);
            try {
                getDataCache().setData(lock, newDocument.getBytes(), modify);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }

        protected Object getModel() {
            return getDDModel();
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