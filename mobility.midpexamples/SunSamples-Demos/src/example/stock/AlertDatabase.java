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

import java.util.*;

import javax.microedition.rms.*;


/**
 * <p>This class provides an implementation of the <code>Database</code>
 * class specific to alert records.</p>
 */
public class AlertDatabase extends Database {
    /**
     * Default Constructor
     */
    public AlertDatabase() {
        rc = new AlertComparator();
    }

    /**
     * <p>This methods cleans out the database of all alerts that match the
     * <code>tkrSymbol</code> passed in.  An appropriate use would be when
     * removing a stock from the database, all alerts for that stock are no
     * longer valid, so call this method then.</p>
     *
     * @param tkrSymbol The name of the stock to match with alerts
     */
    public synchronized void removeUselessAlerts(String tkrSymbol) {
        Enumeration IDs = recordIDs.elements();

        while (IDs.hasMoreElements()) {
            int index = ((Integer)IDs.nextElement()).intValue();

            try {
                String data = new String(database.getRecord(index));
                data = data.substring(0, data.indexOf(';'));

                if (data.equals(tkrSymbol)) {
                    database.deleteRecord(index);
                    recordIDs.removeElement(new Integer(index));
                }
            } catch (RecordStoreException rse) {
                return;
            }
        }
    }

    /**
     * <p>Get a <code>RecordEnumeration</code> of records in the database who
     * match the <code>AlertFilter</code> conditions</p>
     *
     * @return <code>RecordEnumeration</code> of all stock records that match
     *         the <code>RecordFilter</code>
     * @param tkrSymbol The name of the stock to retrieve alerts for
     * @param price The price of the stock to retrieve alerts for
     * @throws <code>RecordStoreNotOpenException</code> is thrown when
     *         trying to close a <code>RecordStore</code> that is not open
     */
    public synchronized RecordEnumeration enumerateRecords(String tkrSymbol, int price)
        throws RecordStoreNotOpenException {
        return database.enumerateRecords(new AlertFilter(tkrSymbol, price), null, false);
    }

    /**
     * <p>Filters the records based on stock symbol and price.  If price is
     * passed as 0 then all records (excluding the first one that stores the
     * lastID) are selected</p>
     *
     * @see javax.microedition.rms.RecordFilter
     */
    private class AlertFilter implements RecordFilter {
        /**
         * <p>The stock symbol to filter with</p>
         */
        private String symbol = null;

        /**
         * <p>The price to filter with</p>
         */
        private int price = 0;

        /**
         * <p>Constructor for the <code>AlertFilter</code></p>
         *
         * @param tkrSymbol The name of the stock to filter for
         * @param matchPrice The price to match with the alert
         */
        public AlertFilter(String tkrSymbol, int matchPrice) {
            symbol = tkrSymbol;
            price = matchPrice;
        }

        /**
         * <p>Returns true if the candidate matches the symbol and price<p>
         *
         * @returns true if the candidate matches the criteria
         * @param candidate The data to check against the criteria
         */
        public boolean matches(byte[] candidate) {
            if (candidate.length > 4) {
                if (price == 0) {
                    return true;
                }

                String c = new String(candidate);

                if (symbol.equals(c.substring(0, c.indexOf(';')))) {
                    if (price >= Integer.valueOf(c.substring(c.indexOf(';') + 1, c.length()))
                                            .intValue()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    /**
     *  <p>Class to compare two records and see if they are equal</p>
     *
     * @see javax.microedition.rms.RecordComparator
     */
    private class AlertComparator implements RecordComparator {
        /**
         * <p>Checks to see if rec1 matches rec2</p>
         *
         * @returns <code>RecordComparator.EQUIVALENT</code> if the records
         *          match or <code>Integer.MAX_VALUE</code> if they don't
         * @param rec1 the data to compare against
         * @param rec2 the data to compare with
         */
        public int compare(byte[] rec1, byte[] rec2) {
            System.out.println(new String(rec1) + " ?==? " + new String(rec2));

            String record1 = new String(rec1);
            String record2 = new String(rec2);

            if (record1.equals(record2)) {
                return RecordComparator.EQUIVALENT;
            }

            return Integer.MAX_VALUE;
        }
    }
}
