/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.properties;

import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.bpel.core.BPELCatalog;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class WsAddressingImportHelper {

    private static final String WS_ADDRESSING_FILE_NAME = "addressing"; // NOI18N
    private static final String SCHEMA_EXT = "xsd"; // NOI18N

    private final BpelModel myModel;
    private final ImportRegistrationHelper myImportHelper;

    private WsAddressingImportHelper(BpelModel bpelModel,
            ImportRegistrationHelper importHelper)
    {
        myModel = bpelModel;
        myImportHelper = importHelper;
    }

    public boolean isAccepted(String namespace) {
        return BPELCatalog.WS_ADDRESSING.equals(namespace);
    }

    public static WsAddressingImportHelper getInstance(BpelModel model,
            ImportRegistrationHelper importHelper)
    {
        return new WsAddressingImportHelper(model, importHelper);
    }

    public BpelModel getModel() {
        return myModel;
    }

    public ImportRegistrationHelper getImportHelper() {
        return myImportHelper;
    }

    // temporary fix to provide correct wsaddressing schema to runtime
    public Import createWsAddressingImport(String namespace) {
        if (!isAccepted(namespace)) {
            return null;
        }

        BPELCatalog bpelCatalog = BPELCatalog.getDefault();
        try {
            InputSource inSource = bpelCatalog.
                resolveEntity(BPELCatalog.WS_ADDRESSING_ID, namespace);
            FileObject wsAddrFo = createLocalWsAddressing(inSource);
            return wsAddrFo != null ? 
                getImportHelper().createImport(wsAddrFo) : null;
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch(SAXException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return null;
    }

    private FileObject createLocalWsAddressing(InputSource inSource) throws IOException {
        if (inSource == null) {
            return null;
        }
        String unUsedName = WS_ADDRESSING_FILE_NAME;
        FileObject sourceFo =  ResolverUtility.getProjectSource(myModel);
        if ( sourceFo == null) {
            throw new IOException(
                    NbBundle.getMessage(WsAddressingImportHelper.class, "Err_Msg_UnknownSource"));// NOI18N
        }

        FileObject addressingFo = getExistFo( sourceFo, unUsedName );

        if (addressingFo != null && addressingFo.isValid() 
                && !addressingFo.isVirtual()) 
        {
            if (isWsAddressingModel(addressingFo)) {
                return addressingFo;
            }
            unUsedName = FileUtil.findFreeFileName(sourceFo, unUsedName, SCHEMA_EXT);

            if (unUsedName == null) {
                return null;
            }
        }

        FileObject newAddressingFo = sourceFo.createData(unUsedName, SCHEMA_EXT);
        if (newAddressingFo == null) {
            return null;
        }

        FileLock fLock = null;
        OutputStream ouStream = null;
        try {
            fLock = newAddressingFo.lock();
            ouStream = newAddressingFo.getOutputStream(fLock);
            Document doc = XMLUtil.parse(inSource, false, true, null, null);
            XMLUtil.write(doc, ouStream, unUsedName+"."+SCHEMA_EXT);
            
        } catch (SAXException ex) {
            ErrorManager.getDefault().notify(ex);
        } finally {
            if (ouStream != null) {
                ouStream.close();
            }
            
            if (fLock != null && fLock.isValid()) {
                fLock.releaseLock();
            }
        }

        newAddressingFo = sourceFo.getFileObject(unUsedName,SCHEMA_EXT);
        return !newAddressingFo.isVirtual() && newAddressingFo.isValid()
                ? newAddressingFo : null;
    }

    private boolean isWsAddressingModel(FileObject fo) {
        ModelSource modelSource = Utilities.getModelSource(
                fo, false);
        if (modelSource != null) {
            SchemaModel schemaModel = SchemaModelFactory.getDefault().
                    getModel(modelSource);
            Schema schema = schemaModel != null ? schemaModel.getSchema() : null;
            return BPELCatalog.WS_ADDRESSING.equals(schema.getTargetNamespace());
        }
        return false;
    }

    private FileObject getExistFo(FileObject sourceFo, String schemaName) {
        assert schemaName != null && schemaName.length() > 0;
        if (sourceFo == null) {
            return null;
        }
        return sourceFo.getFileObject(schemaName, SCHEMA_EXT);
    }
}
