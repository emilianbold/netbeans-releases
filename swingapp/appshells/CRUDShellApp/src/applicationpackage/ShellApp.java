/*
 * ShellApp.java
 */

package applicationpackage;

import application.Application;
import application.ApplicationContext;
import application.SingleFrameApplication;
import application.View;
import java.awt.Window;

/**
 * The main class of the application.
 */
public class ShellApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
       show(new ShellView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of ShellApp
     */
    public static ShellApp getApplication() {
        return Application.getInstance(ShellApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(ShellApp.class, args);
    }
}
