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

import java.util.Calendar;

import javax.microedition.global.Formatter;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;


/**
 * FormatterDemo MIDlet shows usage of formatting in JSR238
 * @see http://www.jcp.org/en/jsr/detail?id=238
 *
 * MIDlet uses device resources to load number and date/time
 * formatting rules and symbols. Date/time and numbers are formatted according
 * locale used by Formatter.
 *
 * @version 1.3
 */
public class FormatterDemo extends MIDlet implements CommandListener {
    private static final String[] LOCALES =
        { "en-US", "sk-SK", "cs-CZ", "he-IL", "zh-CN", "ja-JP" };
    private static final String[] CURRENCY_CODES = { "USD", "SKK", "CZK", "ILS", "CNY", "JPY" };
    private static Formatter defaultFormatter = null;
    private static Formatter[] formatters = null;
    private Form dateForm;
    private Form numberForm1;
    private Form numberForm2;
    private Form numberForm3;
    Command exitCommand = new Command("Exit", Command.EXIT, 1);
    Command nextCommand = new Command("Next", Command.SCREEN, 1);

    public FormatterDemo() {
        defaultFormatter = new Formatter();
        formatters = new Formatter[LOCALES.length];

        for (int i = 0; i < LOCALES.length; ++i) {
            formatters[i] = new Formatter(LOCALES[i]);
        }

        dateForm = new Form("Date/time formatting by locales");
        dateForm.addCommand(nextCommand);
        dateForm.setCommandListener(this);

        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < LOCALES.length; ++i) {
            dateForm.append(new StringItem(LOCALES[i] + ":",
                    formatters[i].formatDateTime(calendar, Formatter.DATETIME_LONG)));
        }

        numberForm1 = new Form("Number formatting by locales");
        numberForm1.addCommand(nextCommand);
        numberForm1.setCommandListener(this);

        double number = -1234.5678;
        numberForm1.append("Show number with 2 decimals:");

        for (int i = 0; i < LOCALES.length; ++i) {
            numberForm1.append(new StringItem(LOCALES[i] + ":",
                    formatters[i].formatNumber(number, 2)));
        }

        numberForm2 = new Form("Percentage formatting by locales");
        numberForm2.addCommand(nextCommand);
        numberForm2.setCommandListener(this);

        numberForm2.append("Show number with 1 decimal:");

        for (int i = 0; i < LOCALES.length; ++i) {
            numberForm2.append(new StringItem(LOCALES[i] + ":",
                    formatters[i].formatPercentage((float)number, 1)));
        }

        numberForm3 = new Form("Currency formatting");
        numberForm3.addCommand(nextCommand);
        numberForm3.addCommand(exitCommand);
        numberForm3.setCommandListener(this);

        numberForm3.append("Show number as currency:");

        for (int i = 0; i < CURRENCY_CODES.length; ++i) {
            numberForm3.append(new StringItem(CURRENCY_CODES[i] + ":",
                    defaultFormatter.formatCurrency(number, CURRENCY_CODES[i])));
        }
    }

    public void startApp() {
        Display.getDisplay(this).setCurrent(dateForm);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command c, Displayable d) {
        if (c == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        } else if ((c == nextCommand) && (d == dateForm)) {
            Display.getDisplay(this).setCurrent(numberForm1);
        } else if ((c == nextCommand) && (d == numberForm1)) {
            Display.getDisplay(this).setCurrent(numberForm2);
        } else if ((c == nextCommand) && (d == numberForm2)) {
            Display.getDisplay(this).setCurrent(numberForm3);
        } else if ((c == nextCommand) && (d == numberForm3)) {
            Display.getDisplay(this).setCurrent(dateForm);
        }
    }
}
