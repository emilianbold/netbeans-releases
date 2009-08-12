/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.visualizers.api.impl;

import java.util.Collection;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.spi.SourceSupportProvider;
import org.openide.util.Lookup;

/**
 *
 * @author mt154047
 */
public final class OpenFunctionInEditorActionProvider {

    private static OpenFunctionInEditorActionProvider instance = null;
    private final SourceSupportProvider sourceSupportProvider = Lookup.getDefault().lookup(SourceSupportProvider.class);

    public static OpenFunctionInEditorActionProvider getInstance() {
        if (instance == null) {
            instance = new OpenFunctionInEditorActionProvider();
        }
        return instance;
    }

    public final void openFunction(String functionName) {
        if (sourceSupportProvider == null || functionName == null) {
            return;
        }

        Collection<? extends SourceFileInfoProvider> sourceInfoProviders =
                Lookup.getDefault().lookupAll(SourceFileInfoProvider.class);

        for (SourceFileInfoProvider provider : sourceInfoProviders) {
            final SourceFileInfo sourceInfo = provider.fileName(functionName, 0, 0, null);
            if (sourceInfo != null && sourceInfo.isSourceKnown()) {
                DLightExecutorService.submit(new Runnable() {

                    public void run() {
                        sourceSupportProvider.showSource(sourceInfo);
                    }
                }, "Show source " + sourceInfo.toString()); // NOI18N
                return;
            }
        }

    }
}



    