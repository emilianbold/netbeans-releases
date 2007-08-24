/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package loginscreenexample;

import java.io.IOException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Ticker;
import javax.microedition.midlet.*;
import org.netbeans.microedition.lcdui.LoginScreen;
import org.netbeans.microedition.lcdui.SplashScreen;
import org.netbeans.microedition.lcdui.WaitScreen;
import org.netbeans.microedition.util.SimpleCancellableTask;


/**
 * @author devil
 */
public class LoginScreenExample extends MIDlet implements CommandListener {

    private boolean midletPaused = false;
    private boolean login = false;
    //<editor-fold defaultstate="collapsed" desc=" Generated Fields ">//GEN-BEGIN:|fields|0|
    private WaitScreen waitScreen;
    private SplashScreen splashScreen;
    private LoginScreen loginScreen;
    private Alert alertSuccess;
    private Alert alertFailure;
    private Command exitCommand;
    private SimpleCancellableTask task;
    private Ticker ticker;
    //</editor-fold>//GEN-END:|fields|0|

    /**
     * The LoginScreenExample constructor.
     */
    public LoginScreenExample() {
    }
    //<editor-fold defaultstate="collapsed" desc=" Generated Methods ">//GEN-BEGIN:|methods|0|
    //</editor-fold>//GEN-END:|methods|0|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: initialize ">//GEN-BEGIN:|0-initialize|0|0-preInitialize
    /**
     * Initilizes the application.
     * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.
     */
    private void initialize() {//GEN-END:|0-initialize|0|0-preInitialize
        // write pre-initialize user code here
//GEN-LINE:|0-initialize|1|0-postInitialize
        // write post-initialize user code here
    }//GEN-BEGIN:|0-initialize|2|
    //</editor-fold>//GEN-END:|0-initialize|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: startMIDlet ">//GEN-BEGIN:|3-startMIDlet|0|3-preAction
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Started point.
     */
    public void startMIDlet() {//GEN-END:|3-startMIDlet|0|3-preAction
        // write pre-action user code here
        switchDisplayable(null, getSplashScreen());//GEN-LINE:|3-startMIDlet|1|3-postAction
        // write post-action user code here
    }//GEN-BEGIN:|3-startMIDlet|2|
    //</editor-fold>//GEN-END:|3-startMIDlet|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: resumeMIDlet ">//GEN-BEGIN:|4-resumeMIDlet|0|4-preAction
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
     */
    public void resumeMIDlet() {//GEN-END:|4-resumeMIDlet|0|4-preAction
        // write pre-action user code here
//GEN-LINE:|4-resumeMIDlet|1|4-postAction
        // write post-action user code here
    }//GEN-BEGIN:|4-resumeMIDlet|2|
    //</editor-fold>//GEN-END:|4-resumeMIDlet|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: switchDisplayable ">//GEN-BEGIN:|5-switchDisplayable|0|5-preSwitch
    /**
     * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
     * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
     * @param nextDisplayable the Displayable to be set
     */
    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {//GEN-END:|5-switchDisplayable|0|5-preSwitch
        // write pre-switch user code here
        Display display = getDisplay();//GEN-BEGIN:|5-switchDisplayable|1|5-postSwitch
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }//GEN-END:|5-switchDisplayable|1|5-postSwitch
        // write post-switch user code here
    }//GEN-BEGIN:|5-switchDisplayable|2|
    //</editor-fold>//GEN-END:|5-switchDisplayable|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: task ">//GEN-BEGIN:|18-getter|0|18-preInit
    /**
     * Returns an initiliazed instance of task component.
     * @return the initialized component instance
     */
    public SimpleCancellableTask getTask() {
        if (task == null) {//GEN-END:|18-getter|0|18-preInit
            // write pre-init user code here
            task = new SimpleCancellableTask();//GEN-BEGIN:|18-getter|1|18-execute
            task.setExecutable(new org.netbeans.microedition.util.Executable() {
                public void execute() throws Exception {//GEN-END:|18-getter|1|18-execute
                    
                }//GEN-BEGIN:|18-getter|2|18-postInit
            });//GEN-END:|18-getter|2|18-postInit
            // write post-init user code here
        }//GEN-BEGIN:|18-getter|3|
        return task;
    }
    //</editor-fold>//GEN-END:|18-getter|3|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: commandAction for Displayables ">//GEN-BEGIN:|7-commandAction|0|7-preCommandAction
    /**
     * Called by a system to indicated that a command has been invoked on a particular displayable.
     * @param command the Command that was invoked
     * @param displayable the Displayable where the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {//GEN-END:|7-commandAction|0|7-preCommandAction
        // write pre-action user code here
        if (displayable == loginScreen) {//GEN-BEGIN:|7-commandAction|1|31-preAction
            if (command == LoginScreen.LOGIN_COMMAND) {//GEN-END:|7-commandAction|1|31-preAction
                // write pre-action user code here
                switchDisplayable(null, getWaitScreen());//GEN-LINE:|7-commandAction|2|31-postAction
                // write post-action user code here
            } else if (command == exitCommand) {//GEN-LINE:|7-commandAction|3|34-preAction
                // write pre-action user code here
                exitMIDlet();//GEN-LINE:|7-commandAction|4|34-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|5|27-preAction
        } else if (displayable == splashScreen) {
            if (command == SplashScreen.DISMISS_COMMAND) {//GEN-END:|7-commandAction|5|27-preAction
                // write pre-action user code here
                switchDisplayable(null, getLoginScreen());//GEN-LINE:|7-commandAction|6|27-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|7|17-preAction
        } else if (displayable == waitScreen) {
            if (command == WaitScreen.FAILURE_COMMAND) {//GEN-END:|7-commandAction|7|17-preAction
                // write pre-action user code here
                switchDisplayable(getAlertFailure(), getLoginScreen());//GEN-LINE:|7-commandAction|8|17-postAction
                // write post-action user code here
            } else if (command == WaitScreen.SUCCESS_COMMAND) {//GEN-LINE:|7-commandAction|9|16-preAction
                // write pre-action user code here
                switchDisplayable(getAlertSuccess(), getLoginScreen());//GEN-LINE:|7-commandAction|10|16-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|11|7-postCommandAction
        }//GEN-END:|7-commandAction|11|7-postCommandAction
        // write post-action user code here
    }//GEN-BEGIN:|7-commandAction|12|
    //</editor-fold>//GEN-END:|7-commandAction|12|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: waitScreen ">//GEN-BEGIN:|13-getter|0|13-preInit
    /**
     * Returns an initiliazed instance of waitScreen component.
     * @return the initialized component instance
     */
    public WaitScreen getWaitScreen() {
        if (waitScreen == null) {//GEN-END:|13-getter|0|13-preInit
            // write pre-init user code here
            waitScreen = new WaitScreen(getDisplay());//GEN-BEGIN:|13-getter|1|13-postInit
            waitScreen.setTitle("waitScreen");
            waitScreen.setCommandListener(this);
            waitScreen.setText("Please Wait ...");
            waitScreen.setTask(getTask());//GEN-END:|13-getter|1|13-postInit
            // write post-init user code here
        }//GEN-BEGIN:|13-getter|2|
        return waitScreen;
    }
    //</editor-fold>//GEN-END:|13-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: splashScreen ">//GEN-BEGIN:|25-getter|0|25-preInit
    /**
     * Returns an initiliazed instance of splashScreen component.
     * @return the initialized component instance
     */
    public SplashScreen getSplashScreen() {
        if (splashScreen == null) {//GEN-END:|25-getter|0|25-preInit
            // write pre-init user code here
            splashScreen = new SplashScreen(getDisplay());//GEN-BEGIN:|25-getter|1|25-postInit
            splashScreen.setTitle("splashScreen");
            splashScreen.setCommandListener(this);
            splashScreen.setText(" Login Screen Example");//GEN-END:|25-getter|1|25-postInit
            // write post-init user code here
        }//GEN-BEGIN:|25-getter|2|
        return splashScreen;
    }
    //</editor-fold>//GEN-END:|25-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand ">//GEN-BEGIN:|33-getter|0|33-preInit
    /**
     * Returns an initiliazed instance of exitCommand component.
     * @return the initialized component instance
     */
    public Command getExitCommand() {
        if (exitCommand == null) {//GEN-END:|33-getter|0|33-preInit
            // write pre-init user code here
            exitCommand = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|33-getter|1|33-postInit
            // write post-init user code here
        }//GEN-BEGIN:|33-getter|2|
        return exitCommand;
    }
    //</editor-fold>//GEN-END:|33-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: loginScreen ">//GEN-BEGIN:|29-getter|0|29-preInit
    /**
     * Returns an initiliazed instance of loginScreen component.
     * @return the initialized component instance
     */
    public LoginScreen getLoginScreen() {
        if (loginScreen == null) {//GEN-END:|29-getter|0|29-preInit
            // write pre-init user code here
            loginScreen = new LoginScreen(getDisplay());//GEN-BEGIN:|29-getter|1|29-postInit
            loginScreen.setLabelTexts("Username:", "Password:");
            loginScreen.setTitle("loginScreen");
            loginScreen.setTicker(getTicker());
            loginScreen.addCommand(LoginScreen.LOGIN_COMMAND);
            loginScreen.addCommand(getExitCommand());
            loginScreen.setCommandListener(this);
            loginScreen.setBGColor(-3355444);
            loginScreen.setFGColor(0);
            loginScreen.setUseLoginButton(false);//GEN-END:|29-getter|1|29-postInit
            // write post-init user code here
        }//GEN-BEGIN:|29-getter|2|
        return loginScreen;
    }
    //</editor-fold>//GEN-END:|29-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: alertFailure ">//GEN-BEGIN:|37-getter|0|37-preInit
    /**
     * Returns an initiliazed instance of alertFailure component.
     * @return the initialized component instance
     */
    public Alert getAlertFailure() {
        if (alertFailure == null) {//GEN-END:|37-getter|0|37-preInit
            // write pre-init user code here
            alertFailure = new Alert("alert", "Error", null, null);//GEN-BEGIN:|37-getter|1|37-postInit
            alertFailure.setTimeout(Alert.FOREVER);//GEN-END:|37-getter|1|37-postInit
            // write post-init user code here
        }//GEN-BEGIN:|37-getter|2|
        return alertFailure;
    }
    //</editor-fold>//GEN-END:|37-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: alertSuccess ">//GEN-BEGIN:|39-getter|0|39-preInit
    /**
     * Returns an initiliazed instance of alertSuccess component.
     * @return the initialized component instance
     */
    public Alert getAlertSuccess() {
        if (alertSuccess == null) {//GEN-END:|39-getter|0|39-preInit
            // write pre-init user code here
            alertSuccess = new Alert("Logged In", "Successfuly logged in", null, null);//GEN-BEGIN:|39-getter|1|39-postInit
            alertSuccess.setTimeout(Alert.FOREVER);//GEN-END:|39-getter|1|39-postInit
            // write post-init user code here
        }//GEN-BEGIN:|39-getter|2|
        return alertSuccess;
    }
    //</editor-fold>//GEN-END:|39-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: ticker ">//GEN-BEGIN:|41-getter|0|41-preInit
    /**
     * Returns an initiliazed instance of ticker component.
     * @return the initialized component instance
     */
    public Ticker getTicker() {
        if (ticker == null) {//GEN-END:|41-getter|0|41-preInit
            // write pre-init user code here
            ticker = new Ticker("");//GEN-LINE:|41-getter|1|41-postInit
            // write post-init user code here
            //#ifdef LoginScreenServletExample
//#             ticker.setString("The sample is in online mode. Use username/password john/peanuts for successfull connection.");
            //#else
            ticker.setString("The sample is in offline mode. Use username/password test/test for successfull connection.");
            //#endif
            
            
        }//GEN-BEGIN:|41-getter|2|
        return ticker;
    }
    //</editor-fold>//GEN-END:|41-getter|2|

    /**
     * Returns a display instance.
     * @return the display instance.
     */
    public Display getDisplay() {
        return Display.getDisplay(this);
    }

    /**
     * Exits MIDlet.
     */
    public void exitMIDlet() {
        switchDisplayable(null, null);
        destroyApp(true);
        notifyDestroyed();
    }

    /**
     * Called when MIDlet is started.
     * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
     */
    public void startApp() {
        if (midletPaused) {
            resumeMIDlet();
        } else {
            initialize();
            startMIDlet();
        }
        midletPaused = false;
    }

    /**
     * Called when MIDlet is paused.
     */
    public void pauseApp() {
        midletPaused = true;
    }

    /**
     * Called to signal the MIDlet to terminate.
     * @param unconditional if true, then the MIDlet has to be unconditionally terminated and all resources has to be released.
     */
    public void destroyApp(boolean unconditional) {
    }

    private void login() throws IOException {
        //#ifdef LoginScreenServletExample
//#         //URL
//#         String url = "http://localhost:8080/LoginScreenExample/" + "?username=" + getLoginScreen().getUsername() + "&password=" + getLoginScreen().getPassword();
        //#endif

        //Clean up alertSuccess
        getAlertSuccess().setString("");

        //#ifdef LoginScreenServletExample
//#         //Connect to the server
//#         HttpConnection hc = (HttpConnection) Connector.open(url);
//#         //Authentication
//#         if (hc.getResponseCode() == HttpConnection.HTTP_OK) {
//#             login = true;
//#         }
//#         //Closing time...
//#         hc.close();
//#         //Take action based on login value
        //#endif

        //#ifndef LoginScreenServletExample
        if(getLoginScreen().getUsername().startsWith("test") && getLoginScreen().getPassword().startsWith("test")) {
            login = true;            
        }
        //#endif
        
        if (login) {
            getAlertSuccess().setString("Login Succesfull");
        } else {
            getAlertSuccess().setString("Wrong Username or Password");
        }
        login = false;
    }
}
