package test.pkg.not.in.junit;

import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

public class NbModuleSuiteException extends TestCase {

    public NbModuleSuiteException(String t) {
        super(t);
    }

    public void testGenerateMsgOrException() {
        if (Boolean.getBoolean("generate.msg")) {
            Logger.getLogger("my.own.logger").warning("msg");
        }
        if (Boolean.getBoolean("generate.exc")) {
            Logger.getLogger("my.own.logger").log(Level.INFO, "msg", new Exception());
        }
    }
}
