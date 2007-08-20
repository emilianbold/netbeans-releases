/*
 *
 * Copyright (c) 2007, Sun Microsystems, Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package example.mia.demo;

import java.util.Vector;

import javax.microedition.global.ResourceException;
import javax.microedition.global.ResourceManager;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;


/**
 * MicroLexicon midlet is an example for JSR238 (Mobile Internationalization API)
 * @see http://www.jcp.org/en/jsr/detail?id=238
 *
 * Example uses device resources of the emulator as well as application
 * resources bundled with the application.<br>
 * To see content of device resource files use manager
 * <i>WTK_HOME/bin/i18ntool.</i>. To see content of application resource
 * files use <i>i18n Resource Manager</i> from ktoolbar, look into menu project.
 * Application resources are placed under example's <project name>/res directory.
 * You will see how easy is to add your own language to the lexicon ;-)
 *
 *
 * @version 1.4
 */
public class MicroLexicon extends MIDlet implements CommandListener {
    /** constant saying that phrases resource ids start from 100 */
    private static final int PHRASES_OFFSET = 100;

    /** language choice item index */
    private static final int ITEM_LANG_CHOICE = 2;

    /** flag image item index*/
    private static final int ITEM_FLAG = 0;

    /** phrase list item index */
    private static final int ITEM_PHRASE = 2;

    /** resource id constants definitions for language names  */
    private static final int RES_LANG_EN_US = 201;
    private static final int RES_LANG_CS_CZ = 202;
    private static final int RES_LANG_SK_SK = 203;
    private static final int RES_LANG_HE_IL = 204;
    private static final int RES_LANG_ZH_CN = 205;
    private static final int RES_LANG_JA_JP = 206;
    private static final int RES_LANG_DE_DE = 207;
    private static final int RES_LANG_IT_IT = 208;
    private static final int RES_LANG_ES_ES = 209;

    /** resource id constant for flag image  */
    private static final int RES_FLAG = 101;

    /** resource id constant for welcome message*/
    private static final int RES_WELCOME_MSG = 1;

    /** resource id for company title */
    private static final int RES_COMPANY = 2;

    /** resource ids for screen titles */
    private static final int RES_TITLE_1 = 3;
    private static final int RES_TITLE_2 = 4;
    private static final int RES_TITLE_3 = 5;

    /** bundle names */
    private static final String COMMON_RESOURCE_NAME = "common";
    private static final String PHRASES_RESOURCE_NAME = "phrases";

    /**
     *  Resource ids for control buttons
     *  are read from device resources
     */
    private static final int RES_EXIT = 101;
    private static final int RES_NEXT = 102;
    private static final int RES_BACK = 103;

    /**
     *  Phrases are read from resources
     *  but the array is initialized with defaults (english)
     */
    static final String[] phrases =
        new String[] {
            "Hello", "Good morning", "How are you?", "See you", "I am fine", "I like wtk",
            "What is the time?", "How much is it?", "I want one beer", "I want next beer"
        };

    /** Resource Manager of target language */
    static ResourceManager targetPhrasesManager;

    /** resources manager for accessing phrases */
    static ResourceManager phrasesManager;

    /** resources manager for accessing ui resources */
    static ResourceManager uiManager;

    /** device resources manager */
    static ResourceManager devManager;

    /** languages available */
    Languages languages = null;

    /** currently visible screen */
    Displayable currentDisplay;

    /** screens lazy initialize */
    Form frmLangChooser = null;
    List lstPhraseChooser = null;
    Form frmTranslate = null;

    /** language selected */
    String language;

    /** flag selected */
    Image flag;

    /** phrase selected */
    String phrase;
    int phraseId;

    /** commands definitions */
    Command exitCommand;
    Command nextCommand;
    Command backCommand;

    /**
     * Start application.
     * By default initialized with form for choosing languages
     */
    public void startApp() {
        if (!initResources()) {
            Form errFrm =
                new Form("Error", new Item[] { new StringItem(null, "Cannot load resources!") });
            exitCommand = new Command("Exit", Command.EXIT, 1);
            errFrm.addCommand(exitCommand);
            errFrm.setCommandListener(this);
            setDisplay(errFrm);
        } else {
            initCommands();
            currentDisplay = getLangChooserForm();
            setDisplay(getLangChooserForm());
            initPhrases();
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == exitCommand) { // immediately exit application
            destroyApp(true);
            notifyDestroyed();
        } else if (command == nextCommand) {
            if (currentDisplay == frmLangChooser) {
                // get the chosen language and go to phrases
                ChoiceGroup grp = (ChoiceGroup)getLangChooserForm().get(ITEM_LANG_CHOICE);
                int idx = grp.getSelectedIndex();
                language = grp.getString(idx);
                flag = languages.getFlagForLanguage(language);
                setDisplay(getPhraseChooserList());
            } else if (currentDisplay == lstPhraseChooser) {
                setDisplay(getTranslatedForm());
            }
        } else if (command == backCommand) {
            if (currentDisplay == lstPhraseChooser) {
                setDisplay(getLangChooserForm());
            } else if (currentDisplay == frmTranslate) {
                setDisplay(getPhraseChooserList());
            }
        } else if (command == List.SELECT_COMMAND) {
            if (currentDisplay == lstPhraseChooser) {
                //get chosen phrase and go to translation page
                int idx = lstPhraseChooser.getSelectedIndex();
                phrase = lstPhraseChooser.getString(idx);
                phraseId = idx;
                setDisplay(getTranslatedForm());
            }
        }
    }

    private boolean initResources() {
        try {
            phrasesManager = ResourceManager.getManager("phrases");
            uiManager = ResourceManager.getManager("common");
            devManager = ResourceManager.getManager("");
        } catch (ResourceException re) {
            re.printStackTrace();

            return false;
        }

        return true;
    }

    private void initCommands() {
        exitCommand = new Command(devManager.getString(RES_EXIT), Command.EXIT, 1);
        nextCommand = new Command(devManager.getString(RES_NEXT), Command.SCREEN, 1);
        backCommand = new Command(devManager.getString(RES_BACK), Command.BACK, 1);
    }

    /**
     * Load phrases into array.
     */
    private void initPhrases() {
        int resourceId = 0;
        ResourceManager rm = phrasesManager;

        for (int i = 0; i < phrases.length; i++) {
            try {
                resourceId = PHRASES_OFFSET + i;
                phrases[i] = rm.getString(resourceId);
            } catch (ResourceException re) {
                System.err.println("Resource with id " + resourceId + "not found");
            }
        }
    }

    /**
     * display another screen
     */
    private void setDisplay(Displayable d) {
        Display.getDisplay(this).setCurrent(d);
        currentDisplay = d;
    }

    /**
     * Form definition for choosing languages.
     * Contains welcome message and ChoiceGroup of languages.
     */
    private Form getLangChooserForm() {
        if (frmLangChooser == null) {
            String title = uiManager.getString(RES_COMPANY);
            frmLangChooser = new Form(title,
                    new Item[] {
                        new StringItem(null,
                            "Running in locale: " + System.getProperty("microedition.locale")),
                        new StringItem(null, uiManager.getString(RES_WELCOME_MSG)),
                        new ChoiceGroup(uiManager.getString(RES_TITLE_1) + ":", Choice.POPUP,
                            getLanguages().getNames(), null)
                    });
            frmLangChooser.addCommand(exitCommand);
            frmLangChooser.addCommand(nextCommand);
            frmLangChooser.setCommandListener(this);
            language = getLanguages().getNames()[0];
        }

        return frmLangChooser;
    }

    /**
     * Screen for choosing phrases contains list
     * filled with phrases.
     */
    private List getPhraseChooserList() {
        if (lstPhraseChooser == null) {
            lstPhraseChooser = new List(uiManager.getString(RES_TITLE_2) + ":", Choice.IMPLICIT,
                    phrases, null);
            lstPhraseChooser.setFitPolicy(Choice.TEXT_WRAP_ON);
            lstPhraseChooser.addCommand(exitCommand);
            lstPhraseChooser.addCommand(backCommand);
            lstPhraseChooser.setCommandListener(this);
            phrase = phrases[0];
            phraseId = 0;
        }

        String code = languages.getCodeForLanguage(language);
        // depending on language switch to another resource file
        targetPhrasesManager = ResourceManager.getManager("phrases", code);

        return lstPhraseChooser;
    }

    /**
     * Initialize form displaying translated phrase.
     * Form contains country flag and phrase translation.
     */
    private Form getTranslatedForm() {
        if (frmTranslate == null) {
            frmTranslate = new Form(uiManager.getString(RES_TITLE_3) + ":",
                    new Item[] {
                        new ImageItem(null, Image.createImage(1, 1),
                            Item.LAYOUT_LEFT | Item.LAYOUT_TOP, ""), new Spacer(10, 10),
                        new StringItem("", "")
                    });
            frmTranslate.addCommand(exitCommand);
            frmTranslate.addCommand(backCommand);
            frmTranslate.setCommandListener(this);
        }

        ImageItem iimage = (ImageItem)frmTranslate.get(ITEM_FLAG);
        iimage.setImage(flag);

        StringItem istring = (StringItem)frmTranslate.get(ITEM_PHRASE);
        istring.setText(translate(phraseId, phrase));

        return frmTranslate;
    }

    /**
     * Translation just retrieves string resource of
     * given resource id.
     */
    private String translate(int id, String text) {
        try {
            return targetPhrasesManager.getString(PHRASES_OFFSET + id);
        } catch (ResourceException re) {
            return "Phrase translation not found";
        }
    }

    /**
     * Initialize languages class
     */
    private Languages getLanguages() {
        if (languages == null) {
            languages = Languages.getInstance();
        }

        return languages;
    }

    /**
     * Helper class holds language names, locale codes and flags all in one.
     */
    static class Languages {
        static Languages ls = null;
        private Object[] langlist;

        private Languages() {
            init();
        }

        private void init() {
            ResourceManager rm = uiManager;

            //offsets of objects in common resources
            int lang_offset = 200;
            int flag_offset = 100;

            //initialize language names and flags
            String[] availableLocales = ResourceManager.getSupportedLocales(COMMON_RESOURCE_NAME);
            int localesCount = availableLocales.length;

            Vector languagesV = new Vector();
            Vector flagsV = new Vector();
            Vector localesV = new Vector();
            int idx = 1;

            for (int i = 1; i < localesCount; i++) {
                try {
                    String language = rm.getString(lang_offset + idx);

                    if ((language != null) && (language.length() > 0)) {
                        byte[] idata = rm.getData(flag_offset + idx);
                        Image flag = Image.createImage(idata, 0, idata.length);
                        languagesV.addElement(language);
                        flagsV.addElement(flag);
                        localesV.addElement(availableLocales[idx]);
                        idx++;
                    }
                } catch (ResourceException e) {
                }
            }

            String[] languages = new String[languagesV.size()];
            String[] locales = new String[localesV.size()];
            Image[] flags = new Image[flagsV.size()];
            languagesV.copyInto(languages);
            localesV.copyInto(locales);
            flagsV.copyInto(flags);
            langlist = new Object[] { languages, locales, flags };

            localesV = null;
            languagesV = null;
            flagsV = null;
        }

        static Languages getInstance() {
            if (ls == null) {
                ls = new Languages();
            }

            return ls;
        }

        /** @return all language names */
        String[] getNames() {
            return (String[])langlist[0];
        }

        /** @return all locale codes */
        String[] getCodes() {
            return (String[])langlist[1];
        }

        /** @return all country flags */
        Image[] getFlags() {
            return (Image[])langlist[2];
        }

        /** @return flag for given language */
        Image getFlagForLanguage(String lang) {
            String[] languages = getNames();

            for (int i = 0; i < languages.length; i++) {
                if (languages[i].equals(lang)) {
                    return getFlags()[i];
                }
            }

            return Image.createImage(10, 10);
        }

        /** @return locale code for given language */
        String getCodeForLanguage(String lang) {
            String[] languages = getNames();

            for (int i = 0; i < languages.length; i++) {
                if (languages[i].equals(lang)) {
                    return getCodes()[i];
                }
            }

            return "";
        }
    }
}
