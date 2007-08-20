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
package example.mms;

import java.io.InputStream;

import javax.microedition.lcdui.*;

import javax.wireless.messaging.*;


public class PartsDialog implements CommandListener {
    private static final Command CMD_BACK = new Command("Back", Command.BACK, 1);
    private static final Command CMD_NEXT = new Command("Next", Command.OK, 1);
    private static final Command CMD_OK = new Command("OK", Command.OK, 1);
    private static final Command CMD_CANCEL = new Command("Cancel", Command.CANCEL, 1);

    /** current display. */
    private MMSSend mmsSend;
    private List typeList;
    public int counter = 0;

    /** Creates a new instance of PartsDialog */
    public PartsDialog(MMSSend mmsSend) {
        this.mmsSend = mmsSend;

        String[] stringArray = { "Text", "Image" };

        typeList = new List("Add Part: Type", Choice.EXCLUSIVE, stringArray, null);
        typeList.addCommand(CMD_BACK);
        typeList.addCommand(CMD_NEXT);
        typeList.setCommandListener(this);
    }

    public void show() {
        mmsSend.getDisplay().setCurrent(typeList);
    }

    /**
     * Respond to commands, including exit
     * @param c user interface command requested
     * @param s screen object initiating the request
     */
    public void commandAction(Command c, Displayable s) {
        try {
            if (c == CMD_BACK) {
                mmsSend.show();
            } else if (c == CMD_NEXT) {
                if (typeList.getSelectedIndex() == 0) {
                    mmsSend.getDisplay().setCurrent(new TextDialog());
                } else {
                    mmsSend.getDisplay().setCurrent(new ImageDialog());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private class TextDialog extends Form implements CommandListener {
        private Displayable mainForm;
        private TextField text;
        private String mimeType = "text/plain";

        public TextDialog() {
            super("Add Text");

            text = new TextField("Text: ", null, 256, TextField.ANY);
            append(text);
            append("MIME-Type: " + mimeType);

            addCommand(CMD_OK);
            addCommand(CMD_CANCEL);
            setCommandListener(this);
        }

        public void commandAction(Command c, Displayable s) {
            try {
                if (c == CMD_OK) {
                    String encoding = "UTF-8";
                    byte[] contents = text.getString().getBytes(encoding);
                    mmsSend.getMessage()
                           .addPart(new MessagePart(contents, 0, contents.length, mimeType,
                            "id" + counter, "contentLocation", encoding));
                    counter++;
                    mmsSend.show();
                } else if (c == CMD_CANCEL) {
                    mmsSend.show();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class ImageDialog extends Form implements CommandListener {
        private Displayable mainForm;
        private ChoiceGroup cg;
        private String mimeType = "image/png";
        private String[] resouces = { "/Duke1.png", "/Duke2.png" };
        private String[] imagesNames = { "Duke 1", "Duke 2" };

        public ImageDialog() {
            super("Add Image");

            cg = new ChoiceGroup("Select Image", Choice.EXCLUSIVE, imagesNames, null);
            append(cg);

            append("MIME-Type: " + mimeType);

            addCommand(CMD_OK);
            addCommand(CMD_CANCEL);
            setCommandListener(this);
        }

        public void commandAction(Command c, Displayable s) {
            try {
                if (c == CMD_OK) {
                    int index = cg.getSelectedIndex();
                    String resouce = resouces[index];
                    InputStream is = getClass().getResourceAsStream(resouce);
                    byte[] contents = new byte[is.available()];
                    is.read(contents);

                    String contentLocation = imagesNames[index];
                    mmsSend.getMessage()
                           .addPart(new MessagePart(contents, 0, contents.length, mimeType,
                            "id" + counter, contentLocation, null));
                    counter++;
                    mmsSend.show();
                } else if (c == CMD_CANCEL) {
                    mmsSend.show();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
