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

import java.io.EOFException;

import java.util.*;

import javax.microedition.rms.*;


/**
 * <p>This class provides a wrapper class for the
 * <code>RecordStore</code> class.
 * It allows for easier addition and deletion as well as better searching and
 * updating of records.  The used recordIDs are kept in a <code>Vector</code>
 * which we use to access the indices of the records.  The last used recordID
 * is stored at the beginning of the database and when the database is opened,
 * each recordID up to the last one used is tested to see if a record exists in
 * that position and a new <code>Vector</code> of used recordIDs
 * is generated.</p>
 */
public abstract class Database {
    /**
     * The database storing all the records and the last
     * used recordID in position 1
     */
    protected volatile RecordStore database = null;

    /**
     * The <code>Vector</code> of used recordIDs that are in the database
     */
    protected volatile Vector recordIDs = null;

    /**
     * The last used ID in the database
     */
    protected int lastID = 1;

    /**
     * The object used to compare two records and see if they are equal
     */
    protected RecordComparator rc = null;

    /**
     * <p>Initializes the database and if it's not a new database, loads the
     * recordID of the last record out of the first position in the
     * <code>RecordStore</code>.  We have stored it there when we closed the
     * database, then checks each ID from 1 to lastID to see if they exist in
     * the database and then add the IDs that exist to the recordIDs
     * <code>Vector</code></p>
     *
     * @param fileName The name of the <code>RecordStore</code> to open
     * @throws <code>RecordStoreNotFoundException</code> is thrown if the
     *         <code>RecordStore</code> indicated with <code>fileName</code>
     *         cannot be found
     * @throws <code>RecordStoreException</code> is thrown when a general
     *         exception occurs in a <code>RecordStore</code> operation
     * @throws <code>RecordStoreFullException</code> is thrown when the
     *         storage system is is full
     */
    public void open(String fileName)
        throws RecordStoreNotFoundException, RecordStoreException, RecordStoreFullException {
        database = RecordStore.openRecordStore(fileName, true);
        recordIDs = new Vector();

        try {
            if (database.getNumRecords() != 0) {
                try {
                    lastID = Integer.valueOf(new String(database.getRecord(1))).intValue();

                    for (int i = 1; i <= lastID; i++) {
                        try {
                            database.getRecord(i);
                            recordIDs.addElement(new Integer(i));
                        } catch (RecordStoreException rs) {
                        }
                    }
                } catch (InvalidRecordIDException iri) {
                    throw new RecordStoreException(iri.getMessage());
                }
            }
        } catch (RecordStoreNotOpenException rsno) {
            throw new RecordStoreException(rsno.getMessage());
        }
    }

    /**
     * <p>Close the database and remove it from persistant
     * storage if it is empty</p>
     *
     * @throws <code>RecordStoreNotOpenException</code> is thrown when trying
     *         to close a <code>RecordStore</code> that is not open
     * @throws <code>RecordStoreException</code> is thrown when a general
     *         exception occurs in a <code>RecordStore</code> operation
     */
    public void close() throws RecordStoreNotOpenException, RecordStoreException {
        if (database.getNumRecords() == 0) {
            String fileName = database.getName();
            database.closeRecordStore();
            database.deleteRecordStore(fileName);
        } else {
            database.closeRecordStore();
        }
    }

    /**
     * <p>Remove the database from persistant storage</p>
     *
     * @param fileName the name of the <code>RecordStore</code> to remove
     */
    public void cleanUp(String fileName) throws RecordStoreNotFoundException, RecordStoreException {
        RecordStore.deleteRecordStore(fileName);
        open(fileName);
    }

    /**
     * <p>Add the record to the database<BR>
     * Add the recordID to our vector<BR>
     * Update the database's last ID counter</p>
     *
     * @param record The record data to be added to the database
     * @throws <code>RecordStoreNotOpenException</code> is thrown when
     *         trying to close a <code>RecordStore</code> that is not open
     * @throws <code>RecordStoreFullException</code> is thrown when the storage
     *         system is is full
     * @throws <code>RecordStoreException</code> is thrown when a general
     *         exception occurs in a <code>RecordStore</code> operation
     */
    public synchronized void add(String record)
        throws RecordStoreNotOpenException, RecordStoreFullException, RecordStoreException {
        if (database.getNumRecords() != 0) {
            database.addRecord(record.getBytes(), 0, record.getBytes().length);
            recordIDs.addElement(new Integer(++lastID));
            database.setRecord(1, (String.valueOf(lastID)).getBytes(), 0,
                (String.valueOf(lastID)).length());
        } else {
            recordIDs.addElement(new Integer(++lastID));
            database.addRecord((String.valueOf(lastID)).getBytes(), 0,
                (String.valueOf(lastID)).length());

            try {
                database.addRecord(record.getBytes(), 0, record.getBytes().length);
            } catch (RecordStoreException rs) {
                recordIDs.removeElement(new Integer(lastID--));
                database.setRecord(1, (String.valueOf(lastID)).getBytes(), 0,
                    (String.valueOf(lastID)).length());
                throw rs;
            }
        }
    }

    /**
     * <p>Delete the record from the database and remove that recordID from the
     * vector of used recordIDs</p>
     *
     * @param s The name of the record to delete from the database
     * @throws <code>RecordStoreNotOpenException</code> is thrown when trying
     *         to close a <code>RecordStore</code> that is not open
     * @throws <code>RecordStoreException</code> is thrown when a general
     *         exception occurs in a <code>RecordStore</code> operation
     */
    public synchronized void delete(String s)
        throws RecordStoreNotOpenException, RecordStoreException {
        action(s, null, 0);
    }

    /**
     * <p>Find and return a record</p>
     *
     * @return The record that we're looking for or
     * <code>null</code> if not found
     * @param s The name of the record to search for
     * @throws <code>RecordStoreNotOpenException</code> is thrown when trying
     *         to close a <code>RecordStore</code> that is not open
     * @throws <code>RecordStoreException</code> is thrown when a general
     *         exception occurs in a <code>RecordStore</code> operation
     */
    public synchronized String search(String s)
        throws RecordStoreNotOpenException, RecordStoreException {
        return (String)action(s, null, 1);
    }

    /**
     * <p>Update the record with the name <code>s</code> with the data
     * in the byte[] array</p>
     *
     * @param s The name of the record to update
     * @param data the new data to update the record with
     * @throws <code>RecordStoreNotOpenException</code> is thrown when trying
     *         to close a <code>RecordStore</code> that is not open
     * @throws <code>RecordStoreFullException</code> is thrown when the storage
     *         system is is full
     * @throws <code>RecordStoreException</code> is thrown when a general
     *         exception occurs in a <code>RecordStore</code> operation
     */
    public synchronized void update(String s, byte[] data)
        throws RecordStoreNotOpenException, RecordStoreFullException, RecordStoreException {
        action(s, data, 2);
    }

    /**
     * <p>Go to the index of the record specified by <code>s</code> and perform
     * an action.  Either an update, search or deletion.  This method is for
     * code compaction as the process for updating, searching and
     * deleting varies only slightly.</p>
     *
     * @param s The name of the record to perform the action on
     * @param data Data to use in the action
     * @param action What to do. 0 = delete, 1 = search, 2 = update
     * @throws <code>RecordStoreNotOpenException</code> is thrown when trying
     *         to close a <code>RecordStore</code> that is not open
     * @throws <code>RecordStoreFullException</code> is thrown when the storage
     *         system is is full
     * @throws <code>RecordStoreException</code> is thrown when a general
     *         exception occurs in a <code>RecordStore</code> operation
     */
    private synchronized Object action(String s, byte[] data, int action)
        throws RecordStoreNotOpenException, RecordStoreFullException, RecordStoreException {
        if ((action != 1) && (recordIDs.size() == 0)) {
            throw new RecordStoreException();
        }

        Enumeration IDs = recordIDs.elements();

        while (IDs.hasMoreElements()) {
            int index = ((Integer)IDs.nextElement()).intValue();

            try {
                if (rc.compare(database.getRecord(index), s.getBytes()) == RecordComparator.EQUIVALENT) {
                    switch (action) {
                    case 0:
                        database.deleteRecord(index);
                        recordIDs.removeElement(new Integer(index));

                        return null;

                    case 1:
                        return new String(database.getRecord(index));

                    case 2:
                        database.setRecord(index, data, 0, data.length);

                        return null;

                    default:
                        break;
                    }
                }
            } catch (InvalidRecordIDException iri) {
                throw new RecordStoreException(iri.getMessage());
            }
        }

        return null;
    }

    /**
     * <p>Return the number of records in the database</p>
     *
     * @return the number of records in the database
     * @throws <code>RecordStoreNotOpenException</code> is thrown when trying
     *         to close a <code>RecordStore</code> that is not open
     */
    public int getNumRecords() throws RecordStoreNotOpenException {
        return database.getNumRecords();
    }
}
