/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.parsing.impl;

import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.impl.indexing.lucene.DocumentBasedIndexManager;
import org.netbeans.modules.parsing.impl.indexing.lucene.LuceneIndexFactory;
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored () {
        super.restored();
        RepositoryUpdater.getDefault().start(false);

        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run () {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        Schedulers.init();
                    }
                });
            }
        });
    }

    @Override
    public boolean closing () {
        final boolean ret = super.closing();
        RepositoryUpdater.getDefault().stop();
        LuceneIndexFactory.getDefault().close();
        DocumentBasedIndexManager.getDefault().close();
        return ret;
    }

}
