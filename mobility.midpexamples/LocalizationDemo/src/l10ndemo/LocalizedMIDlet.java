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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * @(#)LocalizedMIDlet.java	1.0 04/05/18 @(#)
 */
package l10ndemo;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.Calendar;

/**
 *
 * @author  breh, lukas
 * @version
 */
public class LocalizedMIDlet extends MIDlet implements CommandListener {
    
    private List mainMenu;
    private Alert alert1, alert2, alert3, alert4;    
    
    private Command quitCommand;
    
    private Display d;
    
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

        
        d = Display.getDisplay(this);
        
        // prepare our simple gui
        prepareGUI();
        
        // set the menu as the current display
        d.setCurrent(mainMenu);
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {        
    }
    
    public void updateAlert1() {
        // get current time
        Calendar now = Calendar.getInstance();
        // create the localized sentence with time and timezone (it requires 3 parameters).
        alert1.setString(LocalizationSupport.getMessage("A1_TEXT", 
                 new Object[] { new Integer(now.get(Calendar.HOUR_OF_DAY)), 
                                new Integer(now.get(Calendar.MINUTE)),
                                now.getTimeZone().getID() }));        
    }
    
    private void prepareGUI() {
        // create main menu
        mainMenu = new List(LocalizationSupport.getMessage("MAIN_MENU_TITLE"), List.IMPLICIT);
        mainMenu.append(LocalizationSupport.getMessage("MENU_ITEM_1"),null);
        mainMenu.append(LocalizationSupport.getMessage("MENU_ITEM_2"),null);
        mainMenu.append(LocalizationSupport.getMessage("MENU_ITEM_3"),null);
        mainMenu.append(LocalizationSupport.getMessage("MENU_ITEM_4"),null);
        
        // create quit command and add it to main menu
        quitCommand = new Command(LocalizationSupport.getMessage("C_QUIT"), Command.EXIT, 1);
        mainMenu.addCommand(quitCommand);
        mainMenu.setCommandListener(this);
        
        // create an alert1 (called by 1st item from menu)
        // this only prepares the alert, the actual string will be set just
        // before it is shown to user     
        alert1 = new Alert(LocalizationSupport.getMessage("A1_TITLE"));
        alert1.setType(AlertType.INFO);
        alert1.setTimeout(Alert.FOREVER);
        
        // create alert2 (called by 2nd item from menu)
        // please note, the message requires one parameter, but it is
        // not supplied - see how it is handled by the support in runtime
        alert2 = new Alert(LocalizationSupport.getMessage("A2_TITLE"),
                           LocalizationSupport.getMessage("A2_TEXT"), 
                           null, AlertType.INFO);
        alert2.setTimeout(Alert.FOREVER);
        
        // create alert3 (called by 3rd item of menu)
        // the A3_TEXT key does not exists - see how it is handled by the support
        alert3 = new Alert(LocalizationSupport.getMessage("A3_TITLE"),
                           LocalizationSupport.getMessage("A3_TEXT"), 
                           null, AlertType.INFO);
        alert3.setTimeout(Alert.FOREVER);
        
        // create alert4 (called by 4rd item of menu)
        // just show the current locale - uses message with parameter
        alert4 = new Alert(LocalizationSupport.getMessage("A4_TITLE"),
                LocalizationSupport.getMessage("A4_TEXT", new Object[] { System.getProperty("microedition.locale")}),
                null, AlertType.INFO);
        alert4.setTimeout(Alert.FOREVER);
    }
    
    
    
    public void commandAction(Command c, Displayable displayable) {
        if (c == quitCommand) {
            destroyApp(false);
            notifyDestroyed();
        } else if ((c == List.SELECT_COMMAND) && (displayable == mainMenu)) {
            switch (mainMenu.getSelectedIndex()) {
                case 0: updateAlert1();
                        d.setCurrent(alert1,mainMenu);
                        break;
                case 1: d.setCurrent(alert2,mainMenu);
                        break;
                case 2: d.setCurrent(alert3,mainMenu);
                        break;
                case 3: d.setCurrent(alert4,mainMenu);
                        break;
                
            }
        }
        
    }
}
