package org.foo.myapp;
import java.io.IOException;
import java.util.Set;
import org.openide.util.WeakSet;
import org.openide.util.io.NullInputStream;
public class MyApp {
    public static void main(String[] args) throws IOException {
        Set s = new WeakSet();
        s.add("hello");
        System.out.println("A WeakSet from lib1.jar: " + s);
        System.out.println("A NullInputStream.available from lib2.jar: " + new NullInputStream().available());
    }
}
