/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.visualizers.api.impl;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.visualizers.SourceSupportProvider;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mt154047
 */
public final class OpenFunctionInEditorActionProvider {
    private static  OpenFunctionInEditorActionProvider instance = null;

    private final SourceSupportProvider sourceSupportProvider= Lookup.getDefault().lookup(SourceSupportProvider.class);


    public static OpenFunctionInEditorActionProvider getInstance(){
        if (instance == null){
            instance = new OpenFunctionInEditorActionProvider();
        }
        return instance;
    }

    public final void openFunction(String functionName){
        if (sourceSupportProvider == null || functionName == null){
            return;
        }

        Collection<? extends SourceFileInfoProvider> sourceInforFileProviders =
            Lookup.getDefault().lookupAll(SourceFileInfoProvider.class);
        
        if (sourceInforFileProviders.isEmpty()){
            return;
        }
        Iterator<? extends SourceFileInfoProvider> iterator = sourceInforFileProviders.iterator();
        while (iterator.hasNext()){
            SourceFileInfoProvider provider = iterator.next();
            try{
                final SourceFileInfo lineInfo = provider.fileName(functionName);
                if (lineInfo != null && lineInfo.isSourceKnown()){
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            sourceSupportProvider.showSource(lineInfo);
                        }
                    });
                    return;
                }
            }catch(SourceFileInfoProvider.SourceFileInfoCannotBeProvided e){                
            }
        }

    }


    

}



    