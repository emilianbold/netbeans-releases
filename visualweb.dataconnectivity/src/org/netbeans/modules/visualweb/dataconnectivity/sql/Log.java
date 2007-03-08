/* * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package org.netbeans.modules.visualweb.dataconnectivity.sql;

import org.netbeans.modules.visualweb.dataconnectivity.naming.LogBase;
import java.util.logging.Logger;

/**
 * A Log Utility for Creator's design time sql package (this package)
 *
 * @author John Kline
 *
 */
class Log extends LogBase {

    static Log log = null;

    Log(String packageName) {
        super(packageName);
    }

    static Logger getLogger() {
        if (log == null) {
            log = new Log(Log.class.getPackage().getName());
        }
        return log.getPackageLogger();
    }
}
