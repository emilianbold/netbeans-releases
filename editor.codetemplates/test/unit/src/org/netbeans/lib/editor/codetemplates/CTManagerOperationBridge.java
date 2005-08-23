/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory;

/**
 * Bridge to CodeTemplateManagerOperation to load explicit templates.
 *
 * @author mmetelka
 */
public class CTManagerOperationBridge {
    
    private static final Document staticDoc = new PlainDocument();
    static {
        staticDoc.putProperty("mimeType", "text/fake");
    }
    
    private static final CodeTemplateManager staticManager
            = CodeTemplateManagerOperation.getManager(staticDoc);
    
    private static final JTextComponent staticComponent = new JEditorPane();
    static {
        staticComponent.setDocument(staticDoc);
    }

    public static void test(String parametrizedText, CTProcessor processor) {
        CodeTemplateApiPackageAccessor.get().getOperation(staticManager).testInstallProcessorFactory(new CTPFactory(processor));
        CodeTemplate template = staticManager.createTemporary(parametrizedText);
        template.insert(staticComponent);
    }

    private static final class CTPFactory implements CodeTemplateProcessorFactory {
        
        private CTProcessor processor;
        
        CTPFactory(CTProcessor processor) {
            this.processor = processor;
        }

        public CodeTemplateProcessor createProcessor(CodeTemplateInsertRequest request) {
            assert (request != null);
            processor.setRequest(request);
            return processor;
        }
        
    }
}
