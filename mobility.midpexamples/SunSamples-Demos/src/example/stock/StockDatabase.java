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

import javax.microedition.rms.*;


/**
 * <p>This class provides an implementation for the <code>Database</code>
 * class specific to stock records.</p>
 */
public class StockDatabase extends Database {
    /**
     * Default Constructor
     */
    public StockDatabase() {
        rc = new StockComparator();
    }

    /**
     * <p>Get a <code>RecordEnumeration</code> of records in the database who
     * match the <code>StockFilter</code> -- which just filters out the first
     * entry in the database (ie. the lastID record)</p>
     *
     * @return <code>RecordEnumeration</code> of all stock records (ie.
     *         excluding the lastID record)
     * @throws <code>RecordStoreNotOpenException</code> is thrown when trying
     *         to close a <code>RecordStore</code> that is not open
     */
    public synchronized RecordEnumeration enumerateRecords()
        throws RecordStoreNotOpenException {
        return database.enumerateRecords(new StockFilter(), null, false);
    }

    /**
     * <p>Filters out the lastID record</p>
     *
     * @see javax.microedition.rms.RecordFilter
     */
    private class StockFilter implements RecordFilter {
        /**
         * Returns true if the candidate is less than 5 characters
         */
        public boolean matches(byte[] candidate) {
            return ((candidate.length > 5) ? true : false);
        }
    }

    /**
     *  <p>Class to compare two records and see if they are equal</p>
     *
     * @see javax.microedition.rms.RecordComparator
     */
    private class StockComparator implements RecordComparator {
        /**
         * Checks to see if rec1 matches rec2
         *
         * @return  RecordComparator.PRECEDES if the name of
         * rec2 comes before the name of rec1 alphabetically,
         * RecordComparator.EQUIVALENT
         * if the records match,
         * RecordComparator.FOLLOWS if the name of
         * rec2 follows the name of rec1 alphabetically
         *
         * @param rec1 the data to compare against
         * @param rec2 the data to compare with
         */
        public int compare(byte[] rec1, byte[] rec2) {
            String name1 = Stock.getName(new String(rec1));
            String name2 = Stock.getName(new String(rec2));
            int result = name1.compareTo(name2);

            if (result < 0) {
                return RecordComparator.PRECEDES;
            } else if (result == 0) {
                return RecordComparator.EQUIVALENT;
            } else {
                return RecordComparator.FOLLOWS;
            }
        }
    }
}
