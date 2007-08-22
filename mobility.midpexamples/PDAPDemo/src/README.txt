--------------------------------------------------------------------------------
      PDA Optional Packages for the J2ME Platform (JSR 75) Demonstration
--------------------------------------------------------------------------------

1. Introduction

    PDAPDemo shows how to use the PIM and FileConnection APIs that are part of 
    the JSR 75 specification.
    
2. Usage

    2.1 Browsing Files
        - To run the file browser, you'll need to give the MIDlet appropriate security 
          authorization, if you have not already done so. Choose Tools > Java Platforms, 
          select WTK emulator. On Tools&Extensions tab, choose Open Preferences. 
          Click on the Security tab. Change the Security 
          domain to maximum and press OK.
        - Now open and run the PDAPDemo project. Launch the FileBrowser MIDlet. 
          You see a directory listing, and you can browse through the available 
          directories and files. By default there is one directory, root1. 
        - Select the directory and press the select button to enter it.
        - Using the commands in the demonstration, you can view the file or see 
          its properties. Try selecting the file and choosing Properties or 
          View from the menu.
        - The actual files are located in WTK_HOME\appdb\DefaultColorPhone\filesystem, 
          assuming you are using the DefaultColorPhone emulator skin. You can add 
          files and root directories as you wish and they will be visible to 
          the JSR 75 File API
          
    2.2 The PIM API
        The JSR75 PIM APIs example demonstrates how to access personal information, 
        like contact lists, calendars, and to-do lists. 
        - After you launch the example, choose a type of list from the main menu.
        - In this example application, each type of list works the same way and 
          each list type contains a single list. For example, if you choose Contact 
          Lists, there is a single contact list called Contacts. Event Lists contains 
          a single list called Events, and To Do Lists contains a single list named To Do. 
        - Once you've selected a list type and chosen the specific list, you can 
          view all the items in the list. If this is the first time you've run 
          the example, the list is probably empty.
        - To add an item, choose New from the menu. The application prompts you 
          for a Formatted Name for the item. You can add more data fields to this 
          item using Add Field in the menu. You see a list of field names. Pick one, 
          then enter the value for the new field. 
        - To save the list item, choose Commit (option 3) from the menu.
        - You can return to the list by choosing the Back command. You'll see 
          the item you just created in the list.

        The items that you create are stored in standard vCard or vCalendar format 
        in the WTK_HOME\appdb\skin\pim directory. See Chapter 10 for more information.
        The PIM API allows for exporting contact, calender, and to-do items in 
        a standard format. The exact format depends on the list type. When you are 
        viewing an item in any list, the menu contains a command for viewing the 
        exported item.
        For example, when you are viewing a contact list item, the menu contains 
        Show vCard. When you choose this command, the exported item is shown on the screen. 
        Calendar items and to-do items both get exported as vCalendar. 


3. Required APIs
    
    JSR 30 - Connected Limited Device Configuration (CLDC) 1.0
    JSR 118 - Mobile Information Device Profile (MIDP) 2.0
    JSR 75 - PDA Optional Packages for the J2ME Platform
