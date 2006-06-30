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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.fold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.spi.editor.fold.FoldManagerFactory;

/**
 * Fold manager factory provider that allows factories
 * to be registered explicitly.
 * <br>
 * It can be used for standalone editor.
 *
 * @author Miloslav Metelka, Martin Roskanin
 */
public class CustomProvider extends FoldManagerFactoryProvider {

    private static final String FOLDER_NAME = "FoldManager"; //NOI18N
    
    private final Map mime2factoryList = new HashMap();
    
    CustomProvider() {
    }
    
    /**
     * Register the fold manager factory so that it will be used for the code folding
     * for the given mime-type.
     *
     * @param mimeType mime-type for which the factory is being registered.
     * @param factory fold manager factory to be registered.
     *  <br>
     *  The factory added sooner will have higher priority regarding
     *  the fold appearance.
     */
    public void registerFactory(String mimeType, FoldManagerFactory factory) {
        assert (mimeType != null) && (factory != null);

        synchronized (mime2factoryList) {
            List factoryList = (List)mime2factoryList.get(mimeType);
            if (factoryList == null) {
                factoryList = new ArrayList();
                mime2factoryList.put(mimeType, factoryList);
            }
            factoryList.add(factory);
        }
    }
    
    /**
     * Register multiple factories for the given mime-type
     * in the order as they are present in the array.
     */
    public void registerFactories(String mimeType, FoldManagerFactory[] factories) {
        synchronized (mime2factoryList) {
            for (int i = 0; i < factories.length; i++) {
                registerFactory(mimeType, factories[i]);
            }
        }
    }
    
    public void removeAllFactories(String mimeType) {
        synchronized (mime2factoryList) {
            mime2factoryList.put(mimeType, null);
        }
    }

    public void removeAllFactories() {
        synchronized (mime2factoryList) {
            mime2factoryList.clear();
        }
    }

    public List getFactoryList(FoldHierarchy hierarchy) {
        List factoryList = null; // result
        JTextComponent editorComponent = hierarchy.getComponent();
        EditorKit kit = editorComponent.getUI().getEditorKit(editorComponent);
        if (kit != null) {
            String mimeType = kit.getContentType();
            if (mimeType != null) {
                synchronized (mime2factoryList) {
                    factoryList = (List)mime2factoryList.get(mimeType);
                }
            }
        }
        
        if (factoryList == null) {
            return Collections.EMPTY_LIST;
        }
        return factoryList;
    }
    
}
