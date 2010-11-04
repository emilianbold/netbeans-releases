package org.netbeans.test.java.hints;

import java.io.IOException;
import java.net.MalformedURLException;

public class Multicatch {
    private void t() {
        try {
            e();
        } catch (MalformedURLException | IOException ex) {
        }
    }
    private void e() throws MalformedURLException, IOException {}
}
