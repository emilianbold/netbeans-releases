package org.netbeans.modules.editor.fold;

import javax.swing.JEditorPane;
import javax.swing.text.AbstractDocument;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.spi.editor.fold.FoldManagerFactory;

/*
 * FoldHierarchyExecutionTest.java
 * JUnit based test
 *
 * Created on June 27, 2004, 1:03 AM
 */


/**
 *
 * @author mmetelka
 */
class FoldHierarchyTestEnv {
    
    private JEditorPane pane;
    
    FoldHierarchyTestEnv(FoldManagerFactory factory) {
        this(new FoldManagerFactory[] { factory });
    }

    FoldHierarchyTestEnv(FoldManagerFactory[] factories) {
        pane = new JEditorPane();
        assert (getMimeType() != null);

        FoldManagerFactoryProvider.setForceCustomProvider(true);
        FoldManagerFactoryProvider provider = FoldManagerFactoryProvider.getDefault();
        assert (provider instanceof CustomProvider)
            : "setForceCustomProvider(true) did not ensure CustomProvider use"; // NOI18N

        CustomProvider customProvider = (CustomProvider)provider;
        customProvider.removeAllFactories(); // cleanup all registered factories
        customProvider.registerFactories(getMimeType(), factories);
    }

    public JEditorPane getPane() {
        return pane;
    }
    
    public AbstractDocument getDocument() {
        return (AbstractDocument)getPane().getDocument();
    }
    
    public String getMimeType() {
        return pane.getEditorKit().getContentType();
    }
    
    public FoldHierarchy getHierarchy() {
        FoldHierarchy hierarchy = FoldHierarchy.get(getPane());
        assert (hierarchy != null);
        return hierarchy;
    }
    
}
