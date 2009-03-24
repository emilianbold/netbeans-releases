package test.pkg.not.in.junit;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.netbeans.junit.internal.NbModuleLogHandler;
import org.openide.util.Lookup;

public class NbModuleSuiteException extends TestCase {

    public NbModuleSuiteException(String t) {
        super(t);
    }

    public void testGenerateMsgOrException() throws IOException {
        boolean ok = false;
        for (Handler h : Lookup.getDefault().lookupAll(Handler.class)) {
            if (h.getClass().equals(NbModuleLogHandler.class)) {
                ok = true;
                break;
            }
        }
        assertTrue("Our loader found", ok);

        if (Boolean.getBoolean("generate.msg")) {
            Logger.getLogger("my.own.logger").warning("msg");
        }
        if (Boolean.getBoolean("generate.exc")) {
            Logger.getLogger("my.own.logger").log(Level.INFO, "msg", new Exception());
        }
    }
}
