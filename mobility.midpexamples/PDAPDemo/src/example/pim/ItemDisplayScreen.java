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

import java.io.ByteArrayOutputStream;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.pim.Contact;
import javax.microedition.pim.Event;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.ToDo;


/**
 * Demonstrate the use of JSR 75 PIM APIs
 */
public class ItemDisplayScreen extends Form implements CommandListener, ItemCommandListener {
    private final Command editArrayCommand = new Command("Edit", Command.OK, 1);
    private final Command editBooleanCommand = new Command("Edit", Command.OK, 1);
    private final Command commitCommand = new Command("Commit", Command.OK, 2);
    private final Command backCommand = new Command("Back", Command.BACK, 1);
    private final Command showVCard = new Command("Show vCard", Command.SCREEN, 5);
    private final Command showVCalendar = new Command("Show vCalendar", Command.SCREEN, 5);
    private final Command addField = new Command("Add Field", Command.SCREEN, 2);
    private final Command removeField = new Command("Remove Field", Command.SCREEN, 3);
    private final PIMDemo midlet;
    private final ItemSelectionScreen caller;
    private final PIMItem item;
    private final Hashtable fieldTable = new Hashtable(); // maps field indices to items

    public ItemDisplayScreen(PIMDemo midlet, ItemSelectionScreen caller, PIMItem item)
        throws PIMException {
        super("PIM Item");
        this.midlet = midlet;
        this.caller = caller;
        this.item = item;

        populateForm();

        addCommand(backCommand);
        addCommand(commitCommand);
        setCommandListener(this);
    }

    private boolean isClassField(int field) {
        return (item instanceof Contact && (field == Contact.CLASS)) ||
        (item instanceof Event && (field == Event.CLASS)) ||
        (item instanceof ToDo && (field == ToDo.CLASS));
    }

    private void populateForm() throws PIMException {
        deleteAll();
        fieldTable.clear();

        int[] fields = item.getPIMList().getSupportedFields();
        boolean allFieldsUsed = true;

        for (int i = 0; i < fields.length; i++) {
            int field = fields[i];

            // exclude CLASS field
            if (isClassField(field)) {
                continue;
            }

            if (item.countValues(field) == 0) {
                allFieldsUsed = false;

                continue;
            }

            int dataType = item.getPIMList().getFieldDataType(field);
            String label = item.getPIMList().getFieldLabel(field);
            Item formItem = null;

            switch (dataType) {
            case PIMItem.STRING: {
                String sValue = item.getString(field, 0);

                if (sValue == null) {
                    sValue = "";
                }

                int style = TextField.ANY;

                // cater for specific field styles
                if (item instanceof Contact) {
                    switch (field) {
                    case Contact.EMAIL:
                        style = TextField.EMAILADDR;

                        break;

                    case Contact.TEL:
                        style = TextField.PHONENUMBER;

                        break;

                    case Contact.URL:
                        style = TextField.URL;

                        break;
                    }
                }

                try {
                    formItem = new TextField(label, sValue, 128, style);
                } catch (IllegalArgumentException e) {
                    formItem = new TextField(label, sValue, 128, TextField.ANY);
                }

                break;
            }

            case PIMItem.BOOLEAN: {
                formItem = new StringItem(label, item.getBoolean(field, 0) ? "yes" : "no");
                formItem.setDefaultCommand(editBooleanCommand);

                break;
            }

            case PIMItem.STRING_ARRAY: {
                String[] a = item.getStringArray(field, 0);

                if (a != null) {
                    formItem = new StringItem(label, joinStringArray(a));
                    formItem.setDefaultCommand(editArrayCommand);
                }

                break;
            }

            case PIMItem.DATE: {
                long time = item.getDate(field, 0);
                int style = DateField.DATE_TIME;

                // some fields are date only, without a time.
                // correct for these fields:
                if (item instanceof Contact) {
                    switch (field) {
                    case Contact.BIRTHDAY:
                        style = DateField.DATE;

                        break;
                    }
                }

                formItem = new DateField(label, style);
                ((DateField)formItem).setDate(new Date(time));

                break;
            }

            case PIMItem.INT: {
                formItem = new TextField(label, String.valueOf(item.getInt(field, 0)), 64,
                        TextField.DECIMAL);

                break;
            }

            case PIMItem.BINARY: {
                byte[] data = item.getBinary(field, 0);

                if (data != null) {
                    formItem = new StringItem(label, data.length + " bytes");
                }

                break;
            }
            }

            if (formItem != null) {
                append(formItem);
                fieldTable.put(formItem, new Integer(field));
                formItem.addCommand(removeField);
                formItem.setItemCommandListener(this);
            }
        }

        if (item instanceof Contact) {
            addCommand(showVCard);
        } else {
            addCommand(showVCalendar);
        }

        if (!allFieldsUsed) {
            addCommand(addField);
        } else {
            removeCommand(addField);
        }
    }

    public void commandAction(final Command command, Displayable displayable) {
        if (command == backCommand) {
            new Thread(new Runnable() {
                    public void run() {
                        try {
                            getUserData();
                        } catch (Exception e) {
                            // ignore; store what can be stored of the data
                        }

                        try {
                            caller.populateList();
                        } catch (Exception e) {
                            // ignore again; show what can be shown of the list
                        }

                        Display.getDisplay(midlet).setCurrent(caller);
                    }
                }).start();
        } else if (command == commitCommand) {
            commit();
        } else if (command == showVCard) {
            showItem("VCARD/2.1");
        } else if (command == showVCalendar) {
            showItem("VCALENDAR/1.0");
        } else if (command == addField) {
            addField();
        }
    }

    public void commandAction(final Command command, final Item formItem) {
        new Thread(new Runnable() {
                public void run() {
                    final int field = ((Integer)fieldTable.get(formItem)).intValue();

                    if (command == editBooleanCommand) {
                        boolean newValue = !item.getBoolean(field, 0);
                        item.setBoolean(field, 0, PIMItem.ATTR_NONE, newValue);
                        ((StringItem)formItem).setText(newValue ? "yes" : "no");
                    } else if (command == editArrayCommand) {
                        String label = item.getPIMList().getFieldLabel(field);
                        final String[] a = item.getStringArray(field, 0);
                        final TextField[] textFields = new TextField[a.length];

                        for (int i = 0; i < a.length; i++) {
                            String elementLabel = item.getPIMList().getArrayElementLabel(field, i);
                            textFields[i] = new TextField(elementLabel, a[i], 128, TextField.ANY);
                        }

                        Form form = new Form(label, textFields);
                        final Command okCommand = new Command("OK", Command.OK, 1);
                        final Command cancelCommand = new Command("Cancel", Command.CANCEL, 1);
                        form.addCommand(okCommand);
                        form.addCommand(cancelCommand);
                        form.setCommandListener(new CommandListener() {
                                public void commandAction(Command command, Displayable d) {
                                    if (command == okCommand) {
                                        for (int i = 0; i < textFields.length; i++) {
                                            a[i] = textFields[i].getString();
                                        }

                                        item.setStringArray(field, 0, item.getAttributes(field, 0),
                                            a);
                                        ((StringItem)formItem).setText(joinStringArray(a));
                                    }

                                    Display.getDisplay(midlet).setCurrent(ItemDisplayScreen.this);
                                }
                            });
                        Display.getDisplay(midlet).setCurrent(form);
                    } else if (command == removeField) {
                        try {
                            item.removeValue(field, 0);
                        } catch (IllegalArgumentException iae) {
                            System.out.println(iae.toString());
                        }

                        try {
                            populateForm();
                        } catch (PIMException e) {
                        }

                        /*
                        for (int i = size() - 1; i >=0; i--) {
                            if (get(i) == formItem) {
                                delete(i);
                                break;
                            }
                        }*/
                    }
                }
            }).start();
    }

    private void commit() {
        new Thread(new Runnable() {
                public void run() {
                    try {
                        getUserData();
                        item.commit();
                        populateForm();
                    } catch (Exception e) {
                        midlet.reportException(e, ItemDisplayScreen.this);
                    }
                }
            }).start();
    }

    private void getUserData() throws NumberFormatException {
        int itemCount = size();

        for (int i = 0; i < itemCount; i++) {
            Item formItem = get(i);
            int field = ((Integer)fieldTable.get(formItem)).intValue();

            if (item.countValues(field) < 1) {
                // No data in field. This can happen if, for example, a
                // value is adding to PUBLIC_KEY, causing values of
                // PUBLIC_KEY_STRING to be erased.
                continue;
            }

            int dataType = item.getPIMList().getFieldDataType(field);

            switch (dataType) {
            case PIMItem.STRING: {
                String s = ((TextField)formItem).getString();

                try {
                    item.setString(field, 0, PIMItem.ATTR_NONE, s);
                } catch (IllegalArgumentException e) {
                    // this was a read-only field (UID)
                }

                break;
            }

            case PIMItem.DATE: {
                long time = ((DateField)formItem).getDate().getTime();

                try {
                    item.setDate(field, 0, PIMItem.ATTR_NONE, time);
                } catch (IllegalArgumentException e) {
                    // this was a read-only field (REVISION)
                }

                break;
            }

            case PIMItem.INT: {
                String s = ((TextField)formItem).getString();
                int j = Integer.parseInt(s);
                item.setInt(field, 0, PIMItem.ATTR_NONE, j);

                break;
            }
            }
        }
    }

    private void addField() {
        int[] allFields = item.getPIMList().getSupportedFields();
        final Vector unusedFields = new Vector();

        for (int i = 0; i < allFields.length; i++) {
            if ((item.countValues(allFields[i]) == 0) && !isClassField(allFields[i])) {
                unusedFields.addElement(new Integer(allFields[i]));
            }
        }

        final List fieldList = new List("Select a field to add", List.IMPLICIT);

        for (Enumeration e = unusedFields.elements(); e.hasMoreElements();) {
            int field = ((Integer)e.nextElement()).intValue();
            fieldList.append(item.getPIMList().getFieldLabel(field), null);
        }

        fieldList.addCommand(new Command("Cancel", Command.CANCEL, 1));
        fieldList.setSelectCommand(new Command("Add", Command.OK, 1));
        fieldList.setCommandListener(new CommandListener() {
                public void commandAction(final Command c, Displayable d) {
                    new Thread(new Runnable() {
                            public void run() {
                                if (c.getCommandType() == Command.OK) {
                                    try {
                                        int index = fieldList.getSelectedIndex();
                                        int field =
                                            ((Integer)unusedFields.elementAt(index)).intValue();
                                        addField(field);
                                    } catch (IllegalArgumentException iae) {
                                        midlet.reportException(iae, ItemDisplayScreen.this);
                                    }

                                    try {
                                        getUserData();
                                        populateForm();
                                    } catch (Exception e) {
                                        midlet.reportException(e, ItemDisplayScreen.this);
                                    }
                                }

                                Display.getDisplay(midlet).setCurrent(ItemDisplayScreen.this);
                            }
                        }).start();
                }
            });
        Display.getDisplay(midlet).setCurrent(fieldList);
    }

    private void addField(int field) {
        switch (item.getPIMList().getFieldDataType(field)) {
        case PIMItem.STRING:
            item.addString(field, PIMItem.ATTR_NONE, "");

            break;

        case PIMItem.STRING_ARRAY: {
            int[] supportedElements = item.getPIMList().getSupportedArrayElements(field);
            int arraySize = 0;

            for (int i = 0; i < supportedElements.length; i++) {
                arraySize = Math.max(arraySize, supportedElements[i] + 1);
            }

            String[] a = new String[arraySize];

            for (int i = 0; i < a.length; i++) {
                a[i] = "";
            }

            item.addStringArray(field, PIMItem.ATTR_NONE, a);

            break;
        }

        case PIMItem.BINARY:
            item.addBinary(field, PIMItem.ATTR_NONE, new byte[16], 0, 16);

            break;

        case PIMItem.BOOLEAN:
            item.addBoolean(field, PIMItem.ATTR_NONE, false);

            break;

        case PIMItem.DATE:
            item.addDate(field, PIMItem.ATTR_NONE, new Date().getTime());

            break;

        case PIMItem.INT:
            item.addInt(field, PIMItem.ATTR_NONE, 0);
        }
    }

    private void showItem(final String format) {
        new Thread(new Runnable() {
                public void run() {
                    try {
                        getUserData();
                        populateForm();

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PIM.getInstance().toSerialFormat(item, baos, "UTF-8", format);

                        String s = new String(baos.toByteArray(), "UTF-8");
                        Alert a = new Alert(format, s, null, AlertType.INFO);
                        a.setTimeout(Alert.FOREVER);
                        Display.getDisplay(midlet).setCurrent(a, ItemDisplayScreen.this);
                    } catch (Exception e) {
                        midlet.reportException(e, ItemDisplayScreen.this);
                    }
                }
            }).start();
    }

    private String joinStringArray(String[] a) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < a.length; i++) {
            if ((a[i] != null) && (a[i].length() > 0)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }

                sb.append(a[i]);
            }
        }

        return sb.toString();
    }
}
