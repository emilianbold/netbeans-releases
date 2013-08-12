package org.netbeans.modules.ko4j.debugging;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.spi.Factory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * 
 * @author Jan Stola
 */
public class Server {
    private static final Server INSTANCE = new Server();
    private boolean started = false;
    private ServerSocket socket;

    public static Server getInstance() {
        return INSTANCE;
    }

    public void acceptClient() {
        ensureStarted();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket client = socket.accept();
                    MessageDispatcherImpl dispatcher = new MessageDispatcherImpl();
                    Transport transport = new Transport(client, dispatcher);
                    WebKitDebugging webKit = Factory.createWebKitDebugging(transport);
                    Lookup context = Lookups.fixed(transport, webKit, dispatcher);
                    PageInspector.getDefault().inspectPage(context);
                } catch (IOException ioex) {
                    Exceptions.printStackTrace(ioex);
                }
            }
        });
        t.start();
    }

    private synchronized void ensureStarted() {
        if (started) {
            return;
        }
        try {
            socket = new ServerSocket(9876);
            started = true;
        } catch (IOException ioex) {
            Exceptions.printStackTrace(ioex);
        }
    }

}
