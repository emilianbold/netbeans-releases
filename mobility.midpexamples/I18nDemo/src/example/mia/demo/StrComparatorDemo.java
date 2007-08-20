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

import java.util.Random;

import javax.microedition.global.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;


/**
 * StrComparatorDemo MIDlet demonstrates locale dependent
 * sorting of strings.
 * Slovak cities are sorted using default comparator or comparator
 * initialized with slovak locale.
 *
 * @version
 */
public final class StrComparatorDemo extends MIDlet implements CommandListener {
    private static final Random random = new Random();
    private final Command exitCommand;
    private final Command shuffleCommand;
    private final Command sortDefCommand;
    private final Command sortSkyCommand;
    private final Form mainForm;
    private StringComparator defComparator;
    private StringComparator skyComparator;
    private String[] cities =
        new String[] {
            "Horovce", "Chorv\u00e1tsky Grob", "Z\u00e1kamenn\u00e9", "\u0160trba", "Zalaba",
            "Str\u00e1\u017eske", "Chorv\u00e1ty", "Hoste", "Stre\u010dno", "\u017dakovce",
            "Z\u00e1kop\u010die", "\u017dakarovce"
        };

    public StrComparatorDemo() {
        mainForm = new Form("String Comparator");

        updateStrings();

        try {
            defComparator = new StringComparator(null, StringComparator.IDENTICAL);
            skyComparator = new StringComparator("sk", StringComparator.IDENTICAL);
        } catch (UnsupportedLocaleException e) {
        }

        exitCommand = new Command("Exit", Command.EXIT, 1);
        shuffleCommand = new Command("Shuffle", Command.SCREEN, 1);
        sortDefCommand = new Command("Sort - default", Command.SCREEN, 2);
        sortSkyCommand = new Command("Sort - slovak", Command.SCREEN, 2);

        mainForm.addCommand(exitCommand);
        mainForm.addCommand(shuffleCommand);
        mainForm.addCommand(sortDefCommand);
        mainForm.addCommand(sortSkyCommand);

        mainForm.setCommandListener(this);
    }

    private void updateStrings() {
        mainForm.deleteAll();

        for (int i = 0; i < cities.length; ++i) {
            mainForm.append(cities[i] + "\n");
        }
    }

    private static void sort(String[] array, StringComparator comparator) {
        boolean changed;
        changed = true;

        for (int j = array.length - 1; (j > 0) && changed; --j) {
            changed = false;

            for (int i = 0; i < j; ++i) {
                if (comparator.compare(array[i], array[i + 1]) > 0) {
                    String tmp = array[i + 1];
                    array[i + 1] = array[i];
                    array[i] = tmp;
                    changed = true;
                }
            }
        }
    }

    private static void shuffle(String[] array2, String[] array, int step) {
        int k = 0;

        for (int i = step - 1; i >= 0; --i) {
            for (int j = i; j < array.length; j += step) {
                array2[k++] = array[j];
            }
        }
    }

    private static void shuffle(String[] array) {
        String[] array2 = new String[array.length];

        for (int i = 0; i < 10; ++i) {
            shuffle(array2, array, random.nextInt(array.length) + 1);

            String[] tmpArray = array;
            array = array2;
            array2 = tmpArray;
        }
    }

    protected void startApp() {
        Display.getDisplay(this).setCurrent(mainForm);
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command c, Displayable d) {
        if (c == shuffleCommand) {
            shuffle(cities);
            updateStrings();
        } else if (c == sortDefCommand) {
            sort(cities, defComparator);
            updateStrings();
        } else if (c == sortSkyCommand) {
            sort(cities, skyComparator);
            updateStrings();
        } else if (c == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        }
    }
}
