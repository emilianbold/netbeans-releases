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
package example.pim;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;
import javax.microedition.pim.ToDo;
import javax.microedition.pim.ToDoList;


/**
 * Demonstrate the use of JSR 75 PIM APIs
 */
public class ItemSelectionScreen extends List implements CommandListener {
    private final Command selectCommand = new Command("Select", Command.ITEM, 1);
    private final Command backCommand = new Command("Back", Command.BACK, 1);
    private final Command removeCommand = new Command("Delete", Command.SCREEN, 3);
    private final Command newCommand = new Command("New", Command.SCREEN, 2);
    private final PIMDemo midlet;
    private final Displayable caller;
    private final int listType;
    private final PIMList list;
    private final Vector itemList = new Vector();

    public ItemSelectionScreen(PIMDemo midlet, Displayable caller, int listType, PIMList list)
        throws PIMException {
        super("Select a PIM item", List.IMPLICIT);
        this.midlet = midlet;
        this.caller = caller;
        this.listType = listType;
        this.list = list;

        populateList();

        addCommand(backCommand);
        addCommand(newCommand);
        setCommandListener(this);
    }

    void populateList() {
        new Thread(new Runnable() {
                public void run() {
                    synchronized (ItemSelectionScreen.this) {
                        try {
                            deleteAll();
                            removeCommand(selectCommand);
                            removeCommand(removeCommand);
                            itemList.removeAllElements();

                            for (Enumeration items = list.items(); items.hasMoreElements();) {
                                PIMItem item = (PIMItem)items.nextElement();
                                int fieldCode = 0;

                                switch (listType) {
                                case PIM.CONTACT_LIST:
                                    fieldCode = Contact.FORMATTED_NAME;

                                    break;

                                case PIM.EVENT_LIST:
                                    fieldCode = Event.SUMMARY;

                                    break;

                                case PIM.TODO_LIST:
                                    fieldCode = ToDo.SUMMARY;

                                    break;
                                }

                                String label = getDisplayedField(item);

                                if (label == null) {
                                    label = "<Incomplete data>";
                                }

                                append(label, null);
                                itemList.addElement(item);
                            }

                            if (size() > 0) {
                                setSelectCommand(selectCommand);
                                addCommand(removeCommand);
                            }
                        } catch (PIMException e) {
                            midlet.reportException(e, ItemSelectionScreen.this);
                        }
                    }
                }
            }).start();
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == backCommand) {
            Display.getDisplay(midlet).setCurrent(caller);
        } else if (command == selectCommand) {
            try {
                PIMItem item = (PIMItem)itemList.elementAt(getSelectedIndex());
                Displayable screen = new ItemDisplayScreen(midlet, this, item);
                Display.getDisplay(midlet).setCurrent(screen);
            } catch (Exception e) {
                midlet.reportException(e, this);
            }
        } else if (command == removeCommand) {
            new Thread(new Runnable() {
                    public void run() {
                        synchronized (ItemSelectionScreen.this) {
                            try {
                                PIMItem item = (PIMItem)itemList.elementAt(getSelectedIndex());

                                switch (listType) {
                                case PIM.CONTACT_LIST:
                                    ((ContactList)list).removeContact((Contact)item);

                                    break;

                                case PIM.EVENT_LIST:
                                    ((EventList)list).removeEvent((Event)item);

                                    break;

                                case PIM.TODO_LIST:
                                    ((ToDoList)list).removeToDo((ToDo)item);

                                    break;
                                }
                            } catch (Exception e) {
                                midlet.reportException(e, ItemSelectionScreen.this);
                            }
                        }
                    }
                }).start();
            populateList();
        } else if (command == newCommand) {
            new Thread(new Runnable() {
                    public void run() {
                        try {
                            PIMItem item = null;

                            switch (listType) {
                            case PIM.CONTACT_LIST:
                                item = ((ContactList)list).createContact();

                                break;

                            case PIM.EVENT_LIST:
                                item = ((EventList)list).createEvent();

                                break;

                            case PIM.TODO_LIST:
                                item = ((ToDoList)list).createToDo();

                                break;
                            }

                            int fieldCode = getDisplayedFieldCode();
                            item.addString(fieldCode, PIMItem.ATTR_NONE, "");

                            Displayable screen =
                                new ItemDisplayScreen(midlet, ItemSelectionScreen.this, item);
                            Display.getDisplay(midlet).setCurrent(screen);
                        } catch (Exception e) {
                            midlet.reportException(e, ItemSelectionScreen.this);
                        }
                    }
                }).start();
        }
    }

    int getDisplayedFieldCode() {
        int fieldCode = 0;

        switch (listType) {
        case PIM.CONTACT_LIST:
            fieldCode = Contact.FORMATTED_NAME;

            break;

        case PIM.EVENT_LIST:
            fieldCode = Event.SUMMARY;

            break;

        case PIM.TODO_LIST:
            fieldCode = ToDo.SUMMARY;

            break;
        }

        return fieldCode;
    }

    void fixDisplayedField(PIMItem item) {
        int fieldCode = getDisplayedFieldCode();

        if (listType == PIM.CONTACT_LIST) {
            boolean defined = false;

            if (item.countValues(fieldCode) != 0) {
                String s = item.getString(fieldCode, 0);

                if ((s != null) && (s.trim().length() > 0)) {
                    defined = true;
                }
            }

            if (!defined) {
                // try to fill in the values from NAME
                if (item.countValues(Contact.NAME) != 0) {
                    String[] a = item.getStringArray(Contact.NAME, 0);

                    if (a != null) {
                        StringBuffer sb = new StringBuffer();

                        if (a[Contact.NAME_GIVEN] != null) {
                            sb.append(a[Contact.NAME_GIVEN]);
                        }

                        if (a[Contact.NAME_FAMILY] != null) {
                            if (sb.length() > 0) {
                                sb.append(" ");
                            }

                            sb.append(a[Contact.NAME_FAMILY]);
                        }

                        String s = sb.toString().trim();

                        if (s.length() > 0) {
                            if (item.countValues(fieldCode) == 0) {
                                item.addString(fieldCode, Contact.ATTR_NONE, s);
                            } else {
                                item.setString(fieldCode, 0, Contact.ATTR_NONE, s);
                            }
                        }
                    }
                }
            }
        }
    }

    String getDisplayedField(PIMItem item) {
        int fieldCode = getDisplayedFieldCode();
        fixDisplayedField(item);

        String fieldValue = null;

        if (item.countValues(fieldCode) != 0) {
            String s = item.getString(fieldCode, 0);

            if ((s != null) && (s.trim().length() != 0)) {
                fieldValue = s;
            }
        }

        return fieldValue;
    }
}
