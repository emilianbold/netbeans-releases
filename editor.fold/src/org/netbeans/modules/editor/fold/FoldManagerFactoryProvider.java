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

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.openide.ErrorManager;

/**
 * Provides list of fold factories that produce fold managers
 * for the given fold hierarchy.
 *
 * <p>
 * The default implementation <code>NbFoldManagerFactoryProvider</code>
 * in fact first obtains a mime-type by using
 * <code>hierarchySpi.getComponent().getEditorKit().getContentType()</code>
 * and then inspects the contents of the following folder in the system FS:<pre>
 *     Editors/<mime-type>/FoldManager
 * </pre>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class FoldManagerFactoryProvider {
    
    private static FoldManagerFactoryProvider defaultProvider;
    
    private static FoldManagerFactoryProvider emptyProvider;
    
    private static boolean forceCustom;
    
    /**
     * Get the default provider used to produce the managers.
     * <br>
     * This method gets called by <code>FoldHierarchyExecution</code>
     * when rebuilding the managers.
     */
    public static synchronized FoldManagerFactoryProvider getDefault() {
        if (defaultProvider == null) {
            defaultProvider = findDefault();
        }
        
        return defaultProvider;
    }
    
    /**
     * Return provider that provides empty list of factories.
     * <br>
     * This method may be used e.g. by <code>FoldHierarchyExecution</code>
     * if the code folding is disabled in editor options.
     */
    public static FoldManagerFactoryProvider getEmpty() {
        if (emptyProvider == null) {
            // Multiple EmptyProvider can be created as method is not synced
            // but should be no harm
            emptyProvider = new EmptyProvider();
        }
        return emptyProvider;
    }
    
    /**
     * This method enforces the use of custom provider
     * instead of the default layer-based provider.
     * <br>
     * It can be used e.g. for testing purposes.
     *
     * @param forceCustomProvider whether the instance
     *  of the {@link CustomProvider} should be used forcibly.
     */
    public static synchronized void setForceCustomProvider(boolean forceCustomProvider) {
        if (!forceCustom) {
            defaultProvider = null;
        }
        forceCustom = forceCustomProvider;
    }
    
    private static FoldManagerFactoryProvider findDefault() {
        FoldManagerFactoryProvider provider = null;

        // By default use layer-based fold manager factory registrations.
        // In case of standalone editor the custom provider
        // will be used allowing custom fold manager factories registrations
        // (public packages restrictions should not apply).
        if (!forceCustom) {
            try {
                org.openide.filesystems.Repository repository
                    = org.openide.filesystems.Repository.getDefault();
                if (repository != null && repository.getDefaultFileSystem() != null) {
                    provider = new LayerProvider();
                }
            } catch (Throwable t) {
                // FileObject class not found -> use layer
            }
        }

        if (provider == null) {
            provider = new CustomProvider();
        }
        
        return provider;
    }
    
    /**
     * Get fold managers appropriate for the given fold hierarchy.
     *
     * @param hierarchy fold hierarchy for which the fold managers
     *  are being created.
     * @return list of <code>FoldManagerFactory</code>s to be used
     *  for the given hierarchy.
     *  <br>
     *  The order of the factories in the returned list defines
     *  priority of the folds produced by the corresponding manager
     *  (manager produced by the factory being first in the list
     *  produces the most important folds).
     *  <br>
     *  The list must not be modified by the clients.
     */
    public abstract List getFactoryList(FoldHierarchy hierarchy);

    
    /**
     * Provider giving empty list of factories.
     */
    private static final class EmptyProvider extends FoldManagerFactoryProvider {
        
        public List getFactoryList(FoldHierarchy hierarchy) {
            return java.util.Collections.EMPTY_LIST;
        }
        
    }
    
}
