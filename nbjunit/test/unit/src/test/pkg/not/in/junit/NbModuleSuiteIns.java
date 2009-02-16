package test.pkg.not.in.junit;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.netbeans.insane.scanner.ObjectMap;
import org.netbeans.insane.scanner.Visitor;

public class NbModuleSuiteIns extends TestCase implements Visitor {
    private static Logger LOG = Logger.getLogger("test.logger");

    public NbModuleSuiteIns(String t) {
        super(t);
    }

    public void testOne() {
        try {
            Class<?> access = Class.forName("org.netbeans.insane.model.Support");
            System.setProperty("ins.one", "OK");
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Error loading class", ex);
        }
    }

    public void testFS() {
        try {
            ClassLoader l = NbModuleSuiteIns.class.getClassLoader();
            Class<?> access = l.loadClass("org.openide.filesystems.FileSystem");
            System.setProperty("ins.fs", "OK");
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Error loading class", ex);
        }
    }

    public void testWindowSystem() {
        try {
            ClassLoader l = NbModuleSuiteIns.class.getClassLoader();
            Class<?> access = l.loadClass("org.netbeans.api.java.platform.JavaPlatform");
            System.setProperty("ins.java", "OK");
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Error loading class", ex);
        }
    }

    public void testSecond() {
        System.setProperty("ins.two", "OK");
    }

    public void testThree() {
        System.setProperty("ins.three", "OK");
    }

    public void visitClass(Class cls) {
    }

    public void visitObject(ObjectMap map, Object object) {
    }

    public void visitObjectReference(ObjectMap map, Object from, Object to, Field ref) {
    }

    public void visitArrayReference(ObjectMap map, Object from, Object to, int index) {
    }

    public void visitStaticReference(ObjectMap map, Object to, Field ref) {
    }
}
