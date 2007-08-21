/*
 * LocalizationVisualMIDlet.java
 * 
 * Created on Aug 20, 2007, 3:18:57 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package l10ndemo;

import java.util.Calendar;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * @author tester
 */
public class LocalizationVisualMIDlet extends MIDlet implements CommandListener {

    private boolean midletPaused = false;

    //<editor-fold defaultstate="collapsed" desc=" Generated Fields ">//GEN-BEGIN:|fields|0|
    private List list;
    private Alert alert;
    private Alert alert1;
    private Alert alert2;
    private Alert alert3;
    private Command exitCommand;
    //</editor-fold>//GEN-END:|fields|0|

    /**
     * The LocalizationVisualMIDlet constructor.
     */
    public LocalizationVisualMIDlet() {
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
        switchDisplayable(null, getList());//GEN-LINE:|3-startMIDlet|1|3-postAction
        // write post-action user code here
    }//GEN-BEGIN:|3-startMIDlet|2|
    //</editor-fold>//GEN-END:|3-startMIDlet|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: resumeMIDlet ">//GEN-BEGIN:|4-resumeMIDlet|0|4-preAction
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
     */
    public void resumeMIDlet() {//GEN-END:|4-resumeMIDlet|0|4-preAction
        // write pre-action user code here
        switchDisplayable(null, getList());//GEN-LINE:|4-resumeMIDlet|1|4-postAction
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

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: commandAction for Displayables ">//GEN-BEGIN:|7-commandAction|0|7-preCommandAction
    /**
     * Called by a system to indicated that a command has been invoked on a particular displayable.
     * @param command the Command that was invoked
     * @param displayable the Displayable where the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {//GEN-END:|7-commandAction|0|7-preCommandAction
        // write pre-action user code here
        if (displayable == list) {//GEN-BEGIN:|7-commandAction|1|15-preAction
            if (command == List.SELECT_COMMAND) {//GEN-END:|7-commandAction|1|15-preAction
                // write pre-action user code here
                listAction();//GEN-LINE:|7-commandAction|2|15-postAction
                // write post-action user code here
            } else if (command == exitCommand) {//GEN-LINE:|7-commandAction|3|31-preAction
                // write pre-action user code here
                exitMIDlet();//GEN-LINE:|7-commandAction|4|31-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|5|7-postCommandAction
        }//GEN-END:|7-commandAction|5|7-postCommandAction
        // write post-action user code here
    }//GEN-BEGIN:|7-commandAction|6|
    //</editor-fold>//GEN-END:|7-commandAction|6|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: list ">//GEN-BEGIN:|13-getter|0|13-preInit
    /**
     * Returns an initiliazed instance of list component.
     * @return the initialized component instance
     */
    public List getList() {
        if (list == null) {//GEN-END:|13-getter|0|13-preInit
            // write pre-init user code here
            list = new List("list", Choice.IMPLICIT);//GEN-BEGIN:|13-getter|1|13-postInit
            list.append(LocalizationSupport.getMessage("MENU_ITEM_1"), null);
            list.append(LocalizationSupport.getMessage("MENU_ITEM_2"), null);
            list.append(LocalizationSupport.getMessage("MENU_ITEM_3"), null);
            list.append(LocalizationSupport.getMessage("MENU_ITEM_4"), null);
            list.addCommand(getExitCommand());
            list.setCommandListener(this);
            list.setSelectedFlags(new boolean[] { false, false, false, false });//GEN-END:|13-getter|1|13-postInit
            // write post-init user code here
        }//GEN-BEGIN:|13-getter|2|
        return list;
    }
    //</editor-fold>//GEN-END:|13-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: listAction ">//GEN-BEGIN:|13-action|0|13-preAction
    /**
     * Performs an action assigned to the selected list element in the list component.
     */
    public void listAction() {//GEN-END:|13-action|0|13-preAction
        // enter pre-action user code here
        String __selectedString = getList().getString(getList().getSelectedIndex());//GEN-BEGIN:|13-action|1|18-preAction
        if (__selectedString != null) {
            if (__selectedString.equals(LocalizationSupport.getMessage("MENU_ITEM_1"))) {//GEN-END:|13-action|1|18-preAction
                // we need to update the time in the alert message
                updateAlertMessage();     
                switchDisplayable(getAlert(), getList());//GEN-LINE:|13-action|2|18-postAction
                // write post-action user code here
            } else if (__selectedString.equals(LocalizationSupport.getMessage("MENU_ITEM_2"))) {//GEN-LINE:|13-action|3|19-preAction
                // write pre-action user code here
                switchDisplayable(getAlert1(), getList());//GEN-LINE:|13-action|4|19-postAction
                // write post-action user code here
            } else if (__selectedString.equals(LocalizationSupport.getMessage("MENU_ITEM_3"))) {//GEN-LINE:|13-action|5|20-preAction
                // write pre-action user code here
                switchDisplayable(getAlert2(), getList());//GEN-LINE:|13-action|6|20-postAction
                // write post-action user code here
            } else if (__selectedString.equals(LocalizationSupport.getMessage("MENU_ITEM_4"))) {//GEN-LINE:|13-action|7|21-preAction
                // write pre-action user code here
                switchDisplayable(getAlert3(), getList());//GEN-LINE:|13-action|8|21-postAction
                // write post-action user code here
            }//GEN-BEGIN:|13-action|9|13-postAction
        }//GEN-END:|13-action|9|13-postAction
        // enter post-action user code here
    }//GEN-BEGIN:|13-action|10|
    //</editor-fold>//GEN-END:|13-action|10|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: alert ">//GEN-BEGIN:|22-getter|0|22-preInit
    /**
     * Returns an initiliazed instance of alert component.
     * @return the initialized component instance
     */
    public Alert getAlert() {
        if (alert == null) {//GEN-END:|22-getter|0|22-preInit
            // write pre-init user code here            
            alert = new Alert(LocalizationSupport.getMessage("A1_TITLE"), null, null, AlertType.INFO);//GEN-BEGIN:|22-getter|1|22-postInit
            alert.setTimeout(Alert.FOREVER);//GEN-END:|22-getter|1|22-postInit
            // write post-init user code here
        }//GEN-BEGIN:|22-getter|2|
        return alert;
    }
    //</editor-fold>//GEN-END:|22-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: alert1 ">//GEN-BEGIN:|23-getter|0|23-preInit
    /**
     * Returns an initiliazed instance of alert1 component.
     * @return the initialized component instance
     */
    public Alert getAlert1() {
        if (alert1 == null) {//GEN-END:|23-getter|0|23-preInit
            // write pre-init user code here
            alert1 = new Alert(LocalizationSupport.getMessage("A2_TITLE"), LocalizationSupport.getMessage("A2_TEXT"), null, AlertType.INFO);//GEN-BEGIN:|23-getter|1|23-postInit
            alert1.setTimeout(Alert.FOREVER);//GEN-END:|23-getter|1|23-postInit
            // write post-init user code here
        }//GEN-BEGIN:|23-getter|2|
        return alert1;
    }
    //</editor-fold>//GEN-END:|23-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: alert2 ">//GEN-BEGIN:|24-getter|0|24-preInit
    /**
     * Returns an initiliazed instance of alert2 component.
     * @return the initialized component instance
     */
    public Alert getAlert2() {
        if (alert2 == null) {//GEN-END:|24-getter|0|24-preInit
            // write pre-init user code here
            alert2 = new Alert(LocalizationSupport.getMessage("A3_TITLE"), LocalizationSupport.getMessage("A3_TITLE"), null, null);//GEN-BEGIN:|24-getter|1|24-postInit
            alert2.setTimeout(Alert.FOREVER);//GEN-END:|24-getter|1|24-postInit
            // write post-init user code here
        }//GEN-BEGIN:|24-getter|2|
        return alert2;
    }
    //</editor-fold>//GEN-END:|24-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: alert3 ">//GEN-BEGIN:|25-getter|0|25-preInit
    /**
     * Returns an initiliazed instance of alert3 component.
     * @return the initialized component instance
     */
    public Alert getAlert3() {
        if (alert3 == null) {//GEN-END:|25-getter|0|25-preInit
            // write pre-init user code here
            alert3 = new Alert(LocalizationSupport.getMessage("A4_TITLE"), LocalizationSupport.getMessage("A4_TEXT"), null, null);//GEN-BEGIN:|25-getter|1|25-postInit
            alert3.setTimeout(Alert.FOREVER);//GEN-END:|25-getter|1|25-postInit
            // write post-init user code here
        }//GEN-BEGIN:|25-getter|2|
        return alert3;
    }
    //</editor-fold>//GEN-END:|25-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand ">//GEN-BEGIN:|30-getter|0|30-preInit
    /**
     * Returns an initiliazed instance of exitCommand component.
     * @return the initialized component instance
     */
    public Command getExitCommand() {
        if (exitCommand == null) {//GEN-END:|30-getter|0|30-preInit
            // write pre-init user code here
            exitCommand = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|30-getter|1|30-postInit
            // write post-init user code here
        }//GEN-BEGIN:|30-getter|2|
        return exitCommand;
    }
    //</editor-fold>//GEN-END:|30-getter|2|

    /**
     * Returns a display instance.
     * @return the display instance.
     */
    public Display getDisplay () {
        return Display.getDisplay(this);
    }

    /**
     * Exits MIDlet.
     */
    public void exitMIDlet() {
        switchDisplayable (null, null);
        destroyApp(true);
        notifyDestroyed();
    }

    /**
     * Called when MIDlet is started.
     * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
     */
    public void startApp() {
//#ifdef Chinese
//#         /* This is used only for chinese configuration 
//#          * we want the application to run always in Chinese
//#          * no matter what the microedition.locale property is set.
//#          * Otherwise the localization support is initialized
//#          * when a getMessage() method is called for the first time.            
//#          */
//#         LocalizationSupport.initLocalizationSupport("zh_CN");
//#endif

//#ifdef Japanese
//#         /* This is used only for japanese configuration 
//#          * we want the application to run always in Japanese
//#          * no matter what the microedition.locale property is set.
//#          * Otherwise the localization support is initialized
//#          * when a getMessage() method is called for the first time.            
//#          */
//#         LocalizationSupport.initLocalizationSupport("ja_JP");
//#endif
        
//#ifdef Czech
//#         /* This is used only for czech configuration 
//#          * we want the application to run always in Czech
//#          * no matter what the microedition.locale property is set.
//#          * Otherwise the localization support is initialized
//#          * when a getMessage() method is called for the first time.            
//#          */
//#         LocalizationSupport.initLocalizationSupport("cs_CZ");
//#endif
        
//#ifdef Deutsch
//#         /* This is used only for german configuration 
//#          * we want the application to run always in German
//#          * no matter what the microedition.locale property is set.
//#          * Otherwise the localization support is initialized
//#          * when a getMessage() method is called for the first time.            
//#          */
//#         LocalizationSupport.initLocalizationSupport("de");
//#endif

//#ifdef Spanish
//#         /* This is used only for german configuration 
//#          * we want the application to run always in Spanish
//#          * no matter what the microedition.locale property is set.
//#          * Otherwise the localization support is initialized
//#          * when a getMessage() method is called for the first time.            
//#          */
//#         LocalizationSupport.initLocalizationSupport("es_MX");
//#endif        

        if (midletPaused) {
            resumeMIDlet ();
        } else {
            initialize ();
            startMIDlet ();
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
    
    public void updateAlertMessage() {
        // get current time
        Calendar now = Calendar.getInstance();
        // create the localized sentence with time and timezone (it requires 3 parameters).
        getAlert().setString(LocalizationSupport.getMessage("A1_TEXT", 
                 new Object[] { new Integer(now.get(Calendar.HOUR_OF_DAY)), 
                                new Integer(now.get(Calendar.MINUTE)),
                                now.getTimeZone().getID() }));        
    }
}
