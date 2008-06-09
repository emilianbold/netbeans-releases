package ims;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import org.openide.ServiceType;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class Foo extends ServiceType {
    private static final long serialVersionUID = 54629387456L;
    public transient ClassLoader loader;
    public transient String loaderToString;
    public transient URL resource;
    public transient String text;
    public Foo() {
        init();
    }
    private void init() {
        loader = Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null) {
            Thread.dumpStack();
            System.err.println("Lookup=" + Lookup.getDefault());
        }
        loaderToString = loader != null ? loader.toString() : null;
        resource = loader != null ? loader.getResource("ims/Bundle.properties") : null;
        text = NbBundle.getMessage(Foo.class, "foo");
        if (loader == null) throw new NullPointerException("no classloader");
        if (resource == null) throw new NullPointerException("no ims/Bundle.properties from " + loaderToString);
        System.err.println("loader=" + loaderToString + " resource=" + resource + " text=" + text);
    }
    public String getName() {
        return "foo";
    }
    public HelpCtx getHelpCtx() {
        return null;
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        //System.err.println("readObject");
        //Thread.dumpStack();
        init();
    }
}
