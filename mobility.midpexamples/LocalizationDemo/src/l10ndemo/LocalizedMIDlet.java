/*
 * Copyright (c) 2010, Oracle.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Oracle nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
