/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.parsing.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
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
            public void run () {
                RequestProcessor.getDefault().post(new Runnable() {
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
        return ret;
    }

    @Override
    public void validate() throws IllegalStateException {
        super.validate();

        long s = System.currentTimeMillis();
        try {
            List<MemoryPoolMXBean> pools = null;
            pools = ManagementFactory.getMemoryPoolMXBeans();
            for (MemoryPoolMXBean pool : pools) {
                if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {    //NOI18N
                    return ;
                }
            }

            IllegalStateException e = new IllegalStateException("Cannot listen on usage threshold");

            throw Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(Installer.class, "ERR_NoUsageThreshold"));
        } finally {
            Logger log = Logger.getLogger(Installer.class.getName());

            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "threshold supported check took: {0}", System.currentTimeMillis() - s);
            }
        }
    }

}
