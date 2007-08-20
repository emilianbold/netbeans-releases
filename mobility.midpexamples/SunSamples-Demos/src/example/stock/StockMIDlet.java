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
package example.stock;

import java.io.*;

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;


/**
 * <p>The MIDlet application class that we'll run for the Stock Demo.</p>
 *
 */
public class StockMIDlet extends MIDlet {
    /**
     * Since there is no support in MIDP/CLDC for floating point numbers,
     * and all of the stock data comes in the form ##.### (usually) then we
     * have to find a way to store the decimals as well as the integer part
     * of the quote data.  Multiplying by the OFFSET will shift the decimal
     * place far enough over to make the number an integer which we can
     * store.  Every time we retrieve quote data from our
     * <code>RecordStore</code> we
     * must make sure that we divide by the OFFSET to correctly display and
     * use the value we actually want.
     */
    private static int OFFSET = 10000; // each 0 is one decimal place

    /**
     * Back <code>Command</code>
     */
    private static final Command BACK_COMMAND = new Command("Back", Command.BACK, 0);

    /**
     * Main Menu <code>Command</code>
     */
    private static final Command MAIN_MENU_COMMAND = new Command("Main", Command.SCREEN, 1);

    /**
     * Done <code>Command</code>
     */
    private static final Command DONE_COMMAND = new Command("Done", Command.OK, 2);

    /**
     * Set <code>Command</code>
     */
    private static final Command SET_COMMAND = new Command("Set", Command.OK, 4);

    /**
     * Exit <code>Command</code>
     */
    private static final Command EXIT_COMMAND = new Command("Exit", Command.STOP, 5);

    /**
     * Calc <code>Command</code>
     */
    private static final Command CALC_COMMAND = new Command("Calc", Command.OK, 6);

    /**
     * The <code>MIDlet</code>'s display object
     */
    Display display = null;

    /**
     * The <code>Ticker</code> that scrolls along the top of the screen
     */
    private Ticker stockTicker = null;

    /**
     * A <code>List</code> of stocks
     */
    private List choose = null;

    /**
     * The Stock Tracker menu
     */
    private List view = null;

    /**
     * The Main menu
     */
    private List menu = null;

    /**
     * The Alert menu
     */
    private List alertList = null;

    /**
     * The Settings menu
     */
    private List settingsList = null;

    /**
     * The 'What If?' <code>Form</code> upon which we enter our query data
     */
    private Form whatif = null;

    /**
     * The <code>Form</code> to display the different update intervals settings
     */
    private Form updatesForm = null;

    /**
     * Used to input a stock symbol
     */
    private TextBox stockSymbolBox = null;

    /**
     * Used to enter the price at which the user wishes to be alerted to the
     * stocks's value
     */
    private TextBox alertPriceBox = null;

    /**
     * The original price the user purchased the stock at, used on the What If?
     * <code>Form</code>
     */
    private TextField origPurchPriceField = null;

    /**
     * The number of shares the users wishes to sell, used on the What If?
     * <code>Form</code>
     */
    private TextField numSharesField = null;

    /**
     * The radio buttons for the update interval time
     */
    private ChoiceGroup updatesChoices = null;

    /**
     * A textual reference to the current menu that is displayed
     * onscreen to
     * allow the <code>StockCommandListener</code> to decide what
     * action to perform upon
     * execution of a command
     */
    private String currentMenu = null;

    /**
     * A reference to the stock that has been chosen from a list of stocks
     * onscreen.  Using this we can extract the correct stock's data from
     * the <code>StockDatabase</code>
     */
    private String stockSymbol = null;

    /**
     * The reference to the <code>StockDatabase</code> that stores
     * the stock data
     */
    private StockDatabase stocks = null;

    /**
     * The reference to the <code>AlertDatabase</code> that stores the alerts
     */
    private AlertDatabase alerts = null;

    /**
     * The server from which the quotes are downloaded
     *
     * NOTE: Currently, only the quote.yahoo.com server is supported
     */
    private String quoteServerURL = "http://quote.yahoo.com/d/quotes.csv?s=";

    /**
     * The format parameter for the quote server to retrieve stock data
     *
     * NOTE: Currently, only this format is supported
     */
    private String quoteFormat = "&f=slc1wop";

    /**
     * The proxy server that must be negotiated
     *
     * NOTE: Proxy server is optional and a blank string indicates that
     *       no proxy should be used
     */

    // Initial default but read from settings file on subsequent runs
    private String proxyURL = null;

    /**
     * The <code>Timer</code> object that refreshes the stock
     * quotes periodically
     */
    private Timer stockRefresh = null;

    /**
     * The <code>TimerTask</code> that the <code>Timer</code>
     * performs periodically.
     * It refreshes the stock quotes
     */
    private StockRefreshTask stockRefreshTask = null;

    /**
     * How often are the stocks' data updated off of the server?
     */

    // Initial default but read from settings file on subsequent runs
    private int refresh_interval = 900000; // 1000 = 1 second
    private boolean firstTime;

    /**
     * <p>Default constructor that is called by the application
     * manager to create a new instance of this <code>MIDlet</code> after
     * which the <code>MIDlet</code> enters the <code>Paused</code> state.</p>
     */
    public StockMIDlet() throws MIDletStateChangeException {
        synchronized (this) {
            // Open the Stocks file
            stocks = new StockDatabase();

            try {
                stocks.open("Stocks");
            } catch (Exception e) {
                try {
                    stocks.cleanUp("Stocks");
                } catch (Exception e2) {
                }
            }

            // Open the Alerts file
            alerts = new AlertDatabase();

            try {
                alerts.open("Alerts");
            } catch (Exception e) {
                try {
                    alerts.cleanUp("Alerts");
                } catch (Exception e2) {
                }
            }

            // Create all the menus and forms
            origPurchPriceField = new TextField("Original Purchase Price:", "", 5, TextField.NUMERIC);
            numSharesField = new TextField("Number Of Shares:", "", 9, TextField.NUMERIC);

            menu = new List("Stock Menu", Choice.IMPLICIT);
            menu.append("Stock Tracker", null);
            menu.append("What If?", null);
            menu.append("Alerts", null);
            menu.append("Settings", null);
            menu.addCommand(EXIT_COMMAND);
            menu.setCommandListener(new StockCommandListener());
            //menu.setTicker(stockTicker);
            whatif = new Form("What If?");
            //whatif.setTicker(stockTicker);
            whatif.append(origPurchPriceField);
            whatif.append(numSharesField);
            whatif.addCommand(BACK_COMMAND);
            whatif.addCommand(CALC_COMMAND);
            whatif.setCommandListener(new StockCommandListener());

            alertList = new List("Alert Menu", Choice.IMPLICIT);
            //alertList.setTicker(stockTicker);
            alertList.append("Add", null);
            alertList.append("Remove", null);
            alertList.addCommand(BACK_COMMAND);
            alertList.setCommandListener(new StockCommandListener());

            settingsList = new List("Settings", Choice.IMPLICIT);
            //settingsList.setTicker(stockTicker);
            settingsList.append("Updates", null);
            settingsList.append("Add Stock", null);
            settingsList.append("Remove Stock", null);
            settingsList.addCommand(BACK_COMMAND);
            settingsList.setCommandListener(new StockCommandListener());

            alertPriceBox = new TextBox("Alert me when stock reaches:", "", 9, TextField.NUMERIC);
            //alertPriceBox.setTicker(stockTicker);
            alertPriceBox.addCommand(DONE_COMMAND);
            alertPriceBox.addCommand(BACK_COMMAND);
            alertPriceBox.setCommandListener(new StockCommandListener());

            updatesForm = new Form("Updates");
            updatesChoices = new ChoiceGroup("Update Interval:", Choice.EXCLUSIVE);
            updatesChoices.append("Continuous", null); // will be 30 seconds
            updatesChoices.append("15 minutes", null); // default for JavaONE
            updatesChoices.append("30 minutes", null);
            updatesChoices.append("1 hour", null);
            updatesChoices.append("3 hours", null);

            switch (refresh_interval) {
            case 30000:
                updatesChoices.setSelectedIndex(0, true);

                break;

            case 1800000:
                updatesChoices.setSelectedIndex(2, true);

                break;

            case 3600000:
                updatesChoices.setSelectedIndex(3, true);

                break;

            case 10800000:
                updatesChoices.setSelectedIndex(4, true);

                break;

            case 900000:default:
                updatesChoices.setSelectedIndex(1, true);

                break;
            }

            //updatesForm.setTicker(stockTicker);
            updatesForm.append(updatesChoices);
            updatesForm.addCommand(BACK_COMMAND);
            updatesForm.addCommand(DONE_COMMAND);
            updatesForm.setCommandListener(new StockCommandListener());

            stockSymbolBox = new TextBox("Enter a Stock Symbol:", "", 5, TextField.ANY);
            //stockSymbolBox.setTicker(stockTicker);
            stockSymbolBox.addCommand(DONE_COMMAND);
            stockSymbolBox.addCommand(BACK_COMMAND);
            stockSymbolBox.setCommandListener(new StockCommandListener());

            // Open the Settings file
            try {
                RecordStore settings = RecordStore.openRecordStore("Settings", true);
                refresh_interval = Integer.valueOf(new String(settings.getRecord(1))).intValue();
                settings.closeRecordStore();

                // No settings file existed
            } catch (Exception e) {
                refresh_interval = 900000;
            }
        } // synchronized

        firstTime = true;
    }

    /**
     * <p>This method is invoked when the <code>MIDlet</code>
     * is ready to run and
     * starts/resumes execution after being in the <code>Paused</code>
     * state. The
     * <code>MIDlet</code> acquires any resources it needs,
     * enters the
     * <code>Active</code> state and begins to perform its service,
     * which in this
     * case means it displays the main menu on the screen.</p>
     *
     * <p>The method proceeds like so:</p>
     *  <li> open the <code>StockDatabase</code> and the
     * <code>AlertDatabase</code></li>
     *  <li> read the settings data from the settings
     * <code>RecordStore</code></li>
     *  <li> create the string to be scrolled across the <code>Ticker</code> on
     *       the top of the screens and instantiate the Ticker object using that
     *       string.  That string will be constructed of the names and prices of
     *       the stocks in our database</li>
     *  <li> get and store the <code>MIDlet</code>'s
     *  <code>Display</code> object</li>
     *  <li> create and show the main menu</li>
     *  <li> instantiate the <code>TimerTask</code> and <code>Timer</code> and
     *       associate the two setting the refresh interval to the value of
     *       the refresh_interval variable</li>
     *
     * @throws <code>MIDletStateChangeException</code> is thrown if the
     *         <code>MIDlet</code> cannot start now but might be able to
     *         start at a later time.
     * @see javax.microedition.midlet.MIDlet
     */
    public void startApp() {
        // Make the ticker
        stockTicker = new Ticker(makeTickerString());
        // set the ticker to all forms
        menu.setTicker(stockTicker);
        whatif.setTicker(stockTicker);
        alertList.setTicker(stockTicker);
        settingsList.setTicker(stockTicker);
        alertPriceBox.setTicker(stockTicker);
        updatesForm.setTicker(stockTicker);
        stockSymbolBox.setTicker(stockTicker);

        display = Display.getDisplay(this);

        if (firstTime) {
            mainMenu();
            firstTime = false;
        }

        // Set up and start the timer to refresh the stock quotes
        stockRefreshTask = new StockRefreshTask();
        stockRefresh = new Timer();
        stockRefresh.schedule(stockRefreshTask, 0, refresh_interval);
    }

    /**
     * <p>This method is invoked by the application management software when
     * the <code>MIDlet</code> no longer needs to be active.  It is a stop
     * signal for the <code>MIDlet</code> upon which the <code>MIDlet</code>
     * should release any resources which can be re-acquired through the
     * <code>startApp<code> method which will be called upon re-activation of
     * the <code>MIDlet</code>. The <code>MIDlet</code> enters
     * the <code>Paused</code>
     * state upon completion of this method.</p>
     *
     * @see javax.microedition.midlet.MIDlet
     */
    public void pauseApp() {
        synchronized (this) {
            // free memory used by these objects
            display = null;
            stockTicker = null;
            stockRefresh.cancel();
            stockRefresh = null;
            stockRefreshTask = null;
        } // synchronized
    }

    /**
     * <p>When the application management software has determined that the
     * <code>MIDlet</code> is no longer needed, or perhaps needs to make room
     * for a higher priority application in memory, is signals
     * the <code>MIDlet</code>
     * that it is a candidate to be destroyed by invoking the
     * <code>destroyApp(boolean)</code> method. In this case, we need to destroy
     * the <code>RecordEnumeration</code>s so that we don't waste their memory
     * and close the open <code>RecordStore</code>s.  If the
     * <code>RecordStore</code>s
     * are empty, then we do not need them to be stored as
     * they will be recreated
     * on the next invokation of the <code>MIDlet</code> so
     * we should delete them.
     * At the end of our clean up, we must call <code>notifyDestroyed</code>
     * which will inform the application management software that we are done
     * cleaning up and have finished our execution and can
     * now be safely terminated and
     * enters the <code>Destroyed</code> state.</p>
     *
     * @param unconditional If true when this method is called,
     * the <code>MIDlet</code> must
     * cleanup and release all resources. If false the <code>MIDlet</code>
     * may throw <code>MIDletStateChangeException</code> to indicate it
     * does not want to be destroyed at this time.
     * @throws <code>MIDletStateChangeException</code>
     *         is thrown if the <code>MIDlet</code> wishes to continue to
     *         execute (Not enter the <code>Destroyed</code> state). This
     *         exception is ignored if <code>unconditional</code> is equal
     *         to true.
     * @see javax.microedition.midlet.MIDlet
     */
    public void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        // If there is no criteria that will keep us from terminating
        if (unconditional) {
            synchronized (this) {
                if (display == null) {
                    // If display == null, we are not initialized and
                    // we have nothing to destroy
                    return;
                }

                stockRefresh.cancel();

                try {
                    stocks.close();
                    alerts.close();

                    RecordStore settings = RecordStore.openRecordStore("Settings", true);

                    try {
                        settings.setRecord(1, String.valueOf(refresh_interval).getBytes(), 0,
                            String.valueOf(refresh_interval).length());

                        // First time writing to the settings file
                    } catch (RecordStoreException rse) {
                        settings.addRecord(String.valueOf(refresh_interval).getBytes(), 0,
                            String.valueOf(refresh_interval).length());
                    }

                    settings.closeRecordStore();
                } catch (Exception e) {
                    // Ignore exception there is no place to report it
                }

                notifyDestroyed();

                // Something might make us not want to exit so check it
                // here before terminating
            } // synchronized
        }
    }

    /**
     * <p>Calculate the profits in a What If? scenario by the formula:</p>
     * <p><type>  Profit = (CurrentPrice - OriginalPurchasePrice)
     * * NumberOfShares</type></p>
     * <p>First we retrieve the current price of the stock. Then parse the
     * original purchase price that the user enters to format it to an
     * integer.  Next, retrieve the number of shares from the form that
     * the user filled in and then calculate the profits and display a nice
     * message (with the result) to the user onscreen.</p>
     */
    private void calc() {
        try {
            String s = stocks.search(stockSymbol);
            int currPrice = Stock.getPrice(s);
            int opp = Stock.makeInt(origPurchPriceField.getString());
            int numShares = Integer.valueOf(numSharesField.getString()).intValue();
            int profit = ((currPrice - opp) * numShares);

            Form answerForm = new Form(Stock.getName(s) + " " + Stock.getStringPrice(s));
            StringBuffer sb =
                new StringBuffer().append("Net profit (loss) is ").append((profit >= 0) ? "$" : "($")
                                  .append((profit >= 0) ? Stock.convert(profit)
                                                        : ("-" + Stock.convert(profit)))
                                  .append((profit >= 0) ? "" : ")").append(" when selling ")
                                  .append(String.valueOf(numShares)).append(" shares at $")
                                  .append(Stock.convert(currPrice)).append(" per share.");
            answerForm.append(sb.toString());
            answerForm.addCommand(BACK_COMMAND);
            answerForm.addCommand(MAIN_MENU_COMMAND);
            answerForm.setCommandListener(new StockCommandListener());
            display.setCurrent(answerForm);
            currentMenu = "AnswerForm";
        } catch (Exception e) {
            error("Calculation Failed", 2000);
        }
    }

    /**
     * <p>Set an alert for the selected stock at the specified price</p>
     *
     * @param Sprice String representation of the price of the stock that
     *               the user would like an alert for
     */
    private void setAlert(String Sprice) {
        try {
            alerts.add((new StringBuffer().append(Stock.getName(stocks.search(stockSymbol)))
                                          .append(';').append(Stock.makeInt(Sprice))).toString());
        } catch (Exception e) {
            error("Failed to add alert", 2000);
        }
    }

    /**
     * <p>Generate a string (which concatenates all of the stock names and
     * prices) which will be used for the <code>Ticker</code>.</p>
     *
     * @return The ticker string which concatenates all of the stock symbols
     *         and prices
     */
    private String makeTickerString() {
        // the ticker tape string
        StringBuffer tickerTape = new StringBuffer();

        try {
            RecordEnumeration re = stocks.enumerateRecords();

            while (re.hasNextElement()) {
                String theStock = new String(re.nextRecord());
                tickerTape.append(Stock.getName(theStock)).append(" @ ")
                          .append(Stock.getStringPrice(theStock)).append("   ");
            }
        } catch (Exception e) {
            return "Error Accessing Database";
        }

        return tickerTape.toString();
    }

    /**
     * <p>Display a message onscreen for a specified period of time</p>
     *
     * @param message The message to be displayed
     * @param time The delay before the message disappears
     */
    private void error(String message, int time) {
        if (!(display.getCurrent() instanceof Alert)) {
            Alert a = new Alert("Error", message, null, AlertType.ERROR);
            a.setTimeout(time);
            display.setCurrent(a, display.getCurrent());
        }
    }

    /**
     * <p>Check the alerts to see if any are registered for tkrSymbol at the
     * tkrSymbol's current price</p>
     *
     * @param tkrSymbol The name of the stock to check for alerts on
     */
    private void checkAlerts(String tkrSymbol) {
        try {
            int current_price = Stock.getPrice(stocks.search(tkrSymbol));
            RecordEnumeration re = alerts.enumerateRecords(tkrSymbol, current_price);

            while (re.hasNextElement()) {
                String alert_string = new String(re.nextRecord());
                int my_price =
                    Integer.valueOf(alert_string.substring(alert_string.indexOf(';') + 1,
                            alert_string.length())).intValue();
                Alert a = new Alert(tkrSymbol, "", null, AlertType.ALARM);
                StringBuffer sb =
                    new StringBuffer().append(tkrSymbol).append(" has reached your price point of $")
                                      .append(Stock.convert(my_price))
                                      .append(" and currently is trading at $")
                                      .append(Stock.getStringPrice(stocks.search(tkrSymbol)));
                a.setString(sb.toString());
                a.setTimeout(Alert.FOREVER);
                display.setCurrent(a);
                alerts.delete(alert_string);
            }
        } catch (RecordStoreNotOpenException rsno) {
        } catch (RecordStoreException rs) {
        }
    }

    /**
     * <p>Display the main menu of the program</p>
     */
    private void mainMenu() {
        display.setCurrent(menu);
        currentMenu = "Main";
    }

    /**
     * <p>Show the list of stocks to pick from and set the menu type to indicate
     * to the Listener what to do with the list choice</p>
     *
     * @param reload   Indicates whether the list should be reloaded
     * which should
     * only happen if it is possible that the stocks have changed
     * since it was last shown
     * @param menuType Which menu is this list representing
     * @param type     Type of Choice to display
     * @param prices   Indicates whether or not to show prices on the list
     */
    private void chooseStock(boolean reload, String menuType, int type, boolean prices) {
        if (reload) {
            choose = new List("Choose Stocks", type);
            choose.setTicker(stockTicker);
            choose.addCommand(BACK_COMMAND);

            if (menuType.equals("RemoveStock")) {
                choose.addCommand(DONE_COMMAND);
            } else if (menuType.equals("WhatChoose") || menuType.equals("AddAlert")) {
                choose.addCommand(MAIN_MENU_COMMAND);
            }

            choose.setCommandListener(new StockCommandListener());

            try {
                RecordEnumeration re = stocks.enumerateRecords();

                while (re.hasNextElement()) {
                    String theStock = new String(re.nextRecord());

                    if (prices) {
                        choose.append(Stock.getName(theStock) + " @ " +
                            Stock.getStringPrice(theStock), null);
                    } else {
                        choose.append(Stock.getName(theStock), null);
                    }
                }
            } catch (RecordStoreNotOpenException rsno) {
            } catch (RecordStoreException rs) {
            }
        }

        display.setCurrent(choose);
        currentMenu = menuType;
    }

    /**
     * <p>Display the stock selected on the View menu with all its
     * attributes shown (ie. Name, Price, etc.)</p>
     *
     * @param tkrSymbol The name of the stock to be deleted
     */
    private void displayStock(String tkrSymbol) {
        try {
            String theStock = stocks.search(tkrSymbol);
            Form stockInfo = new Form(Stock.getName(theStock));
            stockInfo.setTicker(stockTicker);

            StringBuffer sb =
                new StringBuffer().append("Last Trade:\n         ").append(Stock.getTime(theStock))
                                  .append("\n         ").append(Stock.getStringPrice(theStock))
                                  .append("\nChange:  ").append(Stock.getStringChange(theStock))
                                  .append("\nHigh:  ").append(Stock.getStringHigh(theStock))
                                  .append("\nLow:  ").append(Stock.getStringLow(theStock))
                                  .append("\nOpen:  ").append(Stock.getStringOpen(theStock))
                                  .append("\nPrev:  ").append(Stock.getStringPrevious(theStock));
            stockInfo.append(sb.toString());
            stockInfo.addCommand(BACK_COMMAND);
            stockInfo.addCommand(MAIN_MENU_COMMAND);
            stockInfo.setCommandListener(new StockCommandListener());
            display.setCurrent(stockInfo);
            currentMenu = "stockInfo";
        } catch (RecordStoreNotOpenException rsno) {
            error("Could not display stock.  ", 2000);
        } catch (RecordStoreException rs) {
            error("Could not display stock.  ", 2000);
        } catch (NullPointerException npe) {
            error("Could not display stock.  ", 2000);
        }
    }

    /**
     * <p>Show the What If? form to investigate a hypothetical stock deal</p>
     *
     * @param tkrSymbol The name of the stock to perform the query with
     */
    private void whatIfForm(String tkrSymbol) {
        display.setCurrent(whatif);
        currentMenu = "WhatIfForm";
        stockSymbol = tkrSymbol;
    }

    /**
     * <p>Show the alert management menu</p>
     *
     * @param showAlert Indicate whether we should show an alert to indicate
     *                  that we have just successfully added an alert
     */
    private void alertMenu(boolean showAlert) {
        display.setCurrent(alertList);
        currentMenu = "AlertMenu";

        if (showAlert) {
            Alert a = new Alert("", "\n\n\n   Saved!", null, null);
            a.setTimeout(2000);
            display.setCurrent(a, alertList);
        }
    }

    /**
     * <p>Show a list of all active alerts</p>
     */
    private void viewAlerts() {
        choose = new List("Current Alerts", Choice.MULTIPLE);
        choose.setTicker(stockTicker);

        try {
            // Get all the Alert records
            RecordEnumeration re = alerts.enumerateRecords("", 0);

            while (re.hasNextElement()) {
                String a = new String(re.nextRecord());
                String price =
                    Stock.convert(Integer.valueOf(a.substring(a.indexOf(';') + 1, a.length()))
                                         .intValue());
                choose.append(a.substring(0, a.indexOf(';')) + " @ $" + price, null);
            }
        } catch (Exception e) {
            error("Error reading alerts", 2500);
        }

        choose.addCommand(BACK_COMMAND);
        choose.addCommand(DONE_COMMAND);
        choose.setCommandListener(new StockCommandListener());
        display.setCurrent(choose);
        currentMenu = "RemoveAlert";
    }

    /**
     * <p>Show the form to add an alert</p>
     *
     * @param tkrSymbol The ticker symbol of the stock we're adding an alert to
     */
    private void alertForm(String tkrSymbol) {
        display.setCurrent(alertPriceBox);
        currentMenu = "AlertForm";
        stockSymbol = tkrSymbol;
    }

    /**
     * <p>Remove the alert from our RecordStore referenced by index</p>
     *
     * @param choose_data A string with the symbol and price of the alert
     *                    to remove in it
     */
    private void removeAlert(String choose_data) {
        try {
            // Separate the symbol and price from the data
            String symbol = choose_data.substring(0, choose_data.indexOf('@') - 1);
            int sPrice =
                Stock.makeInt(choose_data.substring(choose_data.indexOf('@') + 3,
                        choose_data.length()));
            System.out.println("Remove Alert: " + symbol + ";" + sPrice);
            // Remove the alert
            alerts.delete(symbol + ";" + sPrice);
        } catch (Exception e) {
            error("Failed to remove alert", 2000);
        }
    }

    /**
     * <p>Show the settings menu</p>
     *
     * @param showAlert Indicate whether we should show an alert to indicate
     *                  that we have just successfully saved changes to the
     *                  settings
     */
    private void settings(boolean showAlert) {
        display.setCurrent(settingsList);
        currentMenu = "Settings";

        if (showAlert) {
            Alert a = new Alert("", "\n\n\n   Saved!", null, null);
            a.setTimeout(1500);
            display.setCurrent(a, settingsList);
        }
    }

    /**
     * <p>Show the updates choices</p>
     */
    private void updates() {
        display.setCurrent(updatesForm);
        currentMenu = "Updates";
    }

    /**
     * <p>Show the screen to add a stock</p>
     */
    private void addStock() {
        stockSymbolBox.setString("");
        display.setCurrent(stockSymbolBox);
        currentMenu = "AddStock";
    }

    /**
     * <p>Add the stock to the database</p>
     *  <li> first contact the quote server to get the stock quote</li>
     *  <li> if the stock doesn't not exist, alert the user and return to
     *       the add screen</li>
     *  <li> if the stock exists then add it to our list</li>
     *  <li> if the addition of the stock was successful, return true
     *       otherwise, return false</li>
     * <BR>
     * <p>This is the format returned by the quote.yahoo.com server in
     * the format
     * specified in the instance variable:</p>
     * <pre>
     * NAME    TIME         PRICE        CHANGE   LOW       HIGH    OPEN PREV
     * "SUNW","11:24AM - <b>79.0625</b>",-3.0625,"26.9375 - 106.75",80.5,82.125
     * </pre>
     * <BR>
     * <p>This is what is returned if the stock is not found:</p>
     * <pre>
     * "NAME" ,"N/A - <b>0.00</b>"       ,N/A    ,"N/A - N/A"       ,N/A ,N/A
     * </pre>
     *
     * @return whether or not the addition of the stock was successful
     * @param tkrSymbol The ticker symbol of the stock to add
     */
    private boolean addNewStock(String tkrSymbol) {
        try {
            // When stocks.search() returns null, the stock doesn't yet exist
            if (stocks.search(tkrSymbol) == null) {
                try {
                    stocks.add(getStockQuote(tkrSymbol));
                    stockTicker.setString(makeTickerString());
                } catch (RecordStoreFullException rsf) {
                    error("Database is full.", 2000);

                    return false;
                } catch (RecordStoreException rs) {
                    error("Failed to add " + tkrSymbol, 2000);

                    return false;
                } catch (IOException ioe) {
                    error("Failed to download stock quote for \"" + tkrSymbol + "\"", 2000);

                    return false;
                } catch (NumberFormatException nfe) {
                    error("\"" + tkrSymbol + "\" not found on server, or invalid data " +
                        "received from server", 2000);

                    return false;
                } catch (Exception e) {
                    error(e.toString(), 2000);

                    return false;
                }

                // The stock already exists so we'll just update it
            } else {
                try {
                    stocks.update(tkrSymbol, getStockQuote(tkrSymbol).getBytes());
                    stockTicker.setString(makeTickerString());
                } catch (RecordStoreFullException rsf) {
                    error("Database is full.", 2000);

                    return false;
                } catch (RecordStoreException rs) {
                    error("Failed to update " + tkrSymbol, 2000);

                    return false;
                } catch (IOException ioe) {
                    error("Failed to download stock quote for " + tkrSymbol, 2000);

                    return false;
                } catch (Exception e) {
                    error(e.toString(), 2000);

                    return false;
                }
            }
        } catch (RecordStoreException rs) {
            error("Error accessing database.", 2000);

            return false;
        }

        return true;
    }

    /**
     * <p>This method actually contacts the server, downloads and returns
     * the stock quote.</p>
     *
     * NOTE: If PROXY support is added to HttpConnection then switch the code
     *       over to that as it will be pure MIDP.
     *
     * @return the stock quote
     * @param tkrSymbol The Stock to be requested from the server
     * @throws <code>IOException</code> is thrown if there is a problem
     *         negotiating a connection with the server
     * @throws <code>NumberFormatException</code> is thrown if trashed data
     *         is received from the server (or the Stock could not be found)
     */
    private String getStockQuote(String tkrSymbol) throws IOException, NumberFormatException {
        String quoteURL = quoteServerURL + tkrSymbol + quoteFormat;

        StreamConnection c = (StreamConnection)Connector.open(quoteURL, Connector.READ_WRITE);
        InputStream is = c.openInputStream();
        int ch;
        StringBuffer sb = new StringBuffer();

        while ((ch = is.read()) != -1) {
            sb.append((char)ch);
        }

        Stock.parse(sb.toString());
        is.close();
        c.close();

        return sb.toString();
    }

    /**
     * <p>Remove the stock from the <code>StockDatabase</code></p>
     *
     * @param tkrSymbol The ticker symbol of the stock to delete
     */
    private void deleteStock(String tkrSymbol) {
        try {
            stocks.delete(tkrSymbol);
            alerts.removeUselessAlerts(tkrSymbol);
            stockTicker.setString(makeTickerString());
        } catch (RecordStoreException rs) {
            error("Failed to delete " + tkrSymbol, 2000);
        }
    }

    /**
     * <p>This class is the Listener for ALL of the events that
     * take place during
     * life span of the <code>MIDlet</code>.  It handles <code>Command</code>
     * events and list selections which are the only events that this
     * <code>MIDlet</code> will generate.  In order to determine what to do,
     * the Listener checks the name of the command and the currently displayed
     * menu/list and matches them up to execute the appropriate action.</p>
     * <BR>
     * NOTE: The parseCommandString(String) is only there because the getXXX
     *       methods are missing from the Command class.  When they are added,
     *       this method can be removed and the Command.getLabel() can be used
     */
    private class StockCommandListener implements CommandListener, Runnable {
        /** Current command to process. */
        private Command currentCommand;

        /** Displayable of the current command to process. */
        private Displayable currentDisplayable;

        /** The current command processing thread. */
        private Thread commandThread;

        /**
         * <p>The method to determine what action to take</p>
         *
         * @param c The <code>Command</code> object that has been activated
         * @param d The <code>Displayable</code> object that the command was
         *          associated with
         */
        public void commandAction(Command c, Displayable d) {
            synchronized (this) {
                if (commandThread != null) {
                    // process only one command at a time
                    return;
                }

                currentCommand = c;
                currentDisplayable = d;
                commandThread = new Thread(this);
                commandThread.start();
            }
        }

        /**
         * Perform the current command set by the method commandAction.
         */
        public void run() {
            String type = currentCommand.getLabel();

            // Main command executed, always show Main Menu regardless of
            // which screen is showing
            if (type.equals("Main")) {
                mainMenu();

                // Back command executed, check which screen is and move to
                // the previous one
            } else if (type.equals("Back")) {
                // Screens off the Main Menu
                if (currentMenu.equals("View") || currentMenu.equals("WhatChoose") ||
                        currentMenu.equals("AlertMenu") || currentMenu.equals("Settings")) {
                    mainMenu();

                    // Screens off the Settings menu
                } else if (currentMenu.equals("Add") || currentMenu.equals("Updates") ||
                        currentMenu.equals("RemoveStock")) {
                    settings(false);
                } else if (currentMenu.equals("stockInfo")) {
                    chooseStock(false, "View", Choice.IMPLICIT, true);
                } else if (currentMenu.equals("WhatIfForm")) {
                    chooseStock(false, "WhatChoose", Choice.IMPLICIT, false);
                } else if (currentMenu.equals("AlertForm")) {
                    chooseStock(false, "AddAlert", Choice.IMPLICIT, false);
                } else if (currentMenu.equals("AnswerForm")) {
                    whatIfForm(stockSymbol);

                    // Screens off the Alerts menu
                } else if (currentMenu.equals("RemoveAlert") || currentMenu.equals("AddAlert")) {
                    alertMenu(false);
                } else if (currentMenu.equals("AddStock")) {
                    settings(false);
                }

                /*
                 * OK command executed, perform different actions
                 * depending on which screen is showing
                 */
            } else if (type.equals("Done")) {
                if (currentMenu.equals("AddStock")) {
                    if ((!stockSymbolBox.getString().trim().equals("")) &&
                            (addNewStock(stockSymbolBox.getString().trim()))) {
                        settings(true);
                    }
                } else if (currentMenu.equals("AlertForm")) {
                    setAlert(((TextBox)currentDisplayable).getString());
                    alertMenu(true);

                    // Remove an alert
                } else if (currentMenu.equals("RemoveAlert")) {
                    boolean[] chosen = new boolean[choose.size()];
                    choose.getSelectedFlags(chosen);

                    for (int i = 0; i < chosen.length; i++) {
                        if (chosen[i]) {
                            removeAlert(choose.getString(i));
                        }
                    }

                    alertMenu(true);

                    // Remove a Stock
                } else if (currentMenu.equals("RemoveStock")) {
                    boolean[] chosen = new boolean[choose.size()];
                    choose.getSelectedFlags(chosen);

                    for (int i = 0; i < chosen.length; i++) {
                        if (chosen[i]) {
                            deleteStock(choose.getString(i));
                        }
                    }

                    chosen = null;
                    settings(true);

                    // Set the quote update interval
                } else if (currentMenu.equals("Updates")) {
                    switch (updatesChoices.getSelectedIndex()) {
                    case 0:
                        refresh_interval = 30000;

                        break;

                    case 1:
                        refresh_interval = 900000;

                        break;

                    case 2:
                        refresh_interval = 1800000;

                        break;

                    case 3:
                        refresh_interval = 3600000;

                        break;

                    case 4:
                        refresh_interval = 10800000;

                        break;

                    default:
                        break;
                    }

                    stockRefreshTask.cancel();
                    stockRefreshTask = new StockRefreshTask();
                    stockRefresh.schedule(stockRefreshTask, 0, refresh_interval);
                    settings(true);
                }

                // Exit command executed
            } else if (type.equals("Exit")) {
                try {
                    destroyApp(true);
                } catch (MIDletStateChangeException msce) {
                    mainMenu();
                }

                // Calc command executed
            } else if (type.equals("Calc")) {
                if (origPurchPriceField.size() == 0) {
                    error("You must enter the price you originally " + "purchased the stock at.",
                        2000);
                } else {
                    if (numSharesField.size() == 0) {
                        error("You must specify the number of shares" + " to calculate with.", 2000);
                    } else {
                        calc();
                    }
                }

                /*
                 * No command button was pressed but a list selection
                 * was made
                 */
            } else {
                List shown = (List)display.getCurrent();

                /*
                 * if it's a menu not a list of stocks then we'll
                 * use a switch to select which action to perform
                 */
                if (currentMenu.equals("Main") || currentMenu.equals("Settings") ||
                        currentMenu.equals("AlertMenu")) {
                    switch (shown.getSelectedIndex()) {
                    case 0:

                        // View Stocks
                        if (currentMenu.equals("Main")) {
                            chooseStock(true, "View", Choice.IMPLICIT, true);

                            // Updates
                        } else if (currentMenu.equals("Settings")) {
                            updates();

                            // Add Alert
                        } else {
                            chooseStock(true, "AddAlert", Choice.IMPLICIT, false);
                        }

                        break;

                    case 1:

                        // What If?
                        if (currentMenu.equals("Main")) {
                            chooseStock(true, "WhatChoose", Choice.IMPLICIT, false);

                            // Add Stock
                        } else if (currentMenu.equals("Settings")) {
                            addStock();

                            // Remove Alert
                        } else {
                            viewAlerts();
                        }

                        break;

                    case 2:

                        // Alerts
                        if (currentMenu.equals("Main")) {
                            alertMenu(false);

                            // Remove Stock
                        } else if (currentMenu.equals("Settings")) {
                            chooseStock(true, "RemoveStock", Choice.MULTIPLE, false);
                        }

                        break;

                    case 3:

                        // Settings
                        if (currentMenu.equals("Main")) {
                            settings(false);
                        }

                        break;

                    default:
                        break;
                    }

                    /*
                     * we've now determined that it is a menu of stocks
                     * so we have to either show the stock info (from the
                     * View menu), add an alert (if Alert Choose screen is
                     * showing) or perform a What If? (if the What If?
                     * Choose screen is showing)
                     */
                } else {
                    if (currentMenu.equals("View")) {
                        displayStock(choose.getString(choose.getSelectedIndex())
                                           .substring(0,
                                choose.getString(choose.getSelectedIndex()).indexOf('@') - 1));
                    } else if (currentMenu.equals("WhatChoose")) {
                        if (choose.getSelectedIndex() >= 0) {
                            whatIfForm(choose.getString(choose.getSelectedIndex()));
                        }
                    } else if (currentMenu.equals("AddAlert")) {
                        if (choose.getSelectedIndex() >= 0) {
                            alertForm(choose.getString(choose.getSelectedIndex()));
                        }
                    }
                }
            }

            synchronized (this) {
                // signal that another command can be processed
                commandThread = null;
            }
        }
    }

    /**
     * <p>This is an extension of the <code>TimerTask</code> class which runs
     * when called by <code>Timer</code>.  It refreshes the stock info for
     * each stock from the quote server and checks to see if any of the alerts
     * should be fired.</p>
     *
     * @see java.util.TimerTask
     */
    private class StockRefreshTask extends TimerTask {
        /**
         * <p>Execute the Timer's Task</p>
         */
        public void run() {
            try {
                // Just return if the database is empty
                if (stocks.getNumRecords() == 0) {
                    return;
                }

                // Get all the records
                RecordEnumeration re = stocks.enumerateRecords();

                while (re.hasNextElement()) {
                    String tkrSymbol = Stock.getName(new String(re.nextRecord()));

                    try {
                        byte[] rec = getStockQuote(tkrSymbol).getBytes();

                        // Update the record and check for any alerts that
                        // may need to be executed
                        stocks.update(tkrSymbol, rec);
                        checkAlerts(tkrSymbol);
                    } catch (NumberFormatException nfe) {
                        error("\"" + tkrSymbol + "\" not found on server, or invalid data " +
                            "received from server", 2000);
                    }
                }
            } catch (Exception e) {
                error("Update Failed\n\nStocks were not updated", 2000);
            }
        }
    }
}
