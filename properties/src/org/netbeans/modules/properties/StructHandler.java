/*
 * SourceElementImpl.java -- synopsis. 
 *
 *
 * <<Date>>
 * <<Revision>>
 *
 * SUN PROPRIETARY/CONFIDENTIAL:  INTERNAL USE ONLY.
 *
 * Copyright © 1997-1999 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package org.netbeans.modules.properties;

import java.io.*;
import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.HashMap;

import javax.swing.text.BadLocationException;

import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.util.*;
import org.openide.nodes.Children;
import org.openide.text.PositionRef;
import org.openide.text.PositionBounds;

/* Handling of properties structure files
*
* @author Petr Hamernik, Petr Jiricka
*/
public class StructHandler extends Element /*implements TaskListener*/ {

    public static final String PROP_PARSE = "parse";

    /** Appropriate properties file entry. */
    private PropertiesFileEntry pfe;

    /** If the parsing is in progress this variable is set
    * to the parsing task.
    */
    Task parsingTask;

    /** Soft reference to the data */
    SoftReference dataRef;
    /** SoftReference is GC-ed too often, I wonder if I should keep a hard reference */
    DataRef hardReference;

    // ======================== Public part ====================================

    static final long serialVersionUID =-3367087822606643886L;
    /** Constructs the implementation of source element for the given
    * java data object.
    */
    public StructHandler(PropertiesFileEntry pfe) {
        super(null);
        this.pfe = pfe;
    }

    /** Getter for the current status of the SourceElement implementation.
    * @return the status.
    */
    public boolean getStatus() {
        return getReferenceData() != null;
    }

    /** runs something under Children.MUTEX.writeAccess and then reparses the structure. */
    /*  public Object doUpdate(Mutex.Action action) {
      }*/

    /** If necessary parses the file, blocks until the thing is finished */
    private synchronized void getParsedDataBlocking() {
        if (getReferenceData() == null) {
            reparseNowBlocking();
        }
    }

    synchronized void reparseNowBlocking() {
        try {
            PropertiesParser parser = new PropertiesParser(pfe);
            parser.parseFile();
        }
        catch (IOException e) {
            setPropertiesStructure(null);
        }
    }

    /** Entry for use in this package */
    PropertiesFileEntry getEntry() {
        return pfe;
    }

    /** Method that instructs the implementation of the source element
    * to prepare the element. It is non blocking method that returns
    * task that can be used to control if the operation finished or not.
    *
    * @return task to control the preparation of the elemement
    */
    /*  public Task prepare () {
        return (Task) Children.MUTEX.writeAccess(new Mutex.Action() {
          public Object run() {
            if (parsingTask == null) {
              DataRef d = getReferenceData();
              if (d != null) {
                return new DataTask(d);
              }  
              
              parsingTask = createParsingTask(Thread.MAX_PRIORITY);
            }
            return parsingTask;
          }
        });
      }*/

    /** Get a string representation of the element for printing.
    * @return the string
    */
    public String printString() {
        try {
            return getData().ps.printString();
        }
        catch (PropertiesException e) {
            // PENDING - handle it
            return "";
        }
    }

    // ======================== Package private part ================================

    /** Starts the parsing if the this class is 'dirty' and status is true
    * and parsing is not running yet.
    */
    void autoParse() {
        if (getStatus())
            reparseNowBlocking();
    }

    /** When parser finishes its job, it has to call this method to inform
    * everyone about the result. Must be called under mutex.writeaccess
    *
    * @param res resultant structure
    */
    synchronized void setPropertiesStructure(final PropertiesStructure res) {
        if (res == null)
            return;

        PropertiesStructure result = res;
        // effectively getReferenceData, but we're under writeAccess, so no readAccess
        DataRef data = (dataRef != null) ? (DataRef) dataRef.get() : null;

        if (data == null) {
            // set the parent
            res.setParent(this);
            data = new DataRef(pfe, res);
            dataRef = new SoftReference(data);
            hardReference = data;
            data.ps.structureChanged();
        }
        else {
            // update calls notification methods according to changes
            data.ps.update(res);
        }
    }

    /** Gets the referenced object from the dataRef
    */
    private DataRef getReferenceData() {
        return (DataRef) Children.MUTEX.readAccess(new Mutex.Action() {
                    public Object run() {
                        return (dataRef != null) ? (DataRef) dataRef.get() : null;
                    }
                });
    }

    /** Returns the structure */
    public PropertiesStructure getStructure() {
        try {
            return getData().ps;
        }
        catch (PropertiesException e) {
            // PENDING
            return null;
        }
    }

    /**
    *
    * @return the DataRef object holding the parsing information
    * @exception SourceException if parsing failed.
    */
    private DataRef getData() throws PropertiesException {
        DataRef d = getReferenceData();
        if (d != null)
            return d;

        /*    Task t = prepare();
            t.waitFinished();*/
        getParsedDataBlocking();

        d = getReferenceData();
        if (d != null)
            return d;

        throw new PropertiesException("Document cannot be modified. Impossible to parse it.");
    }

    /** Informs the SourceElement about releasing data (classes, imports,...)
    * from the memory. This method gets as the parameter DataRef which will be
    * garbage collected and should swap them to the disk.
    */
    private void dataRefReleased(DataRef data) {
        Object oldValue = Children.MUTEX.writeAccess(new Mutex.Action() {
                              public Object run() {
                                  dataRef = null;
                                  return new Boolean(true);
                              }
                          });
        // PENDING
        //firePropertyChange (PROP_STATUS, null, null);
    }

    // ======================== The real data holder ==========================

    /** Class which is used for holding the parsed information.
    * It is serializable and could be swapped to the disk.
    * The struct handler holds only soft reference to this object.
    */
    private static class DataRef extends Object {
        /** A serial version UID */
        //static final long serialVersionUID = 697350931687937673L;

        /** Appropriate file entry. */
        PropertiesFileEntry pfe;

        // --------------- Data -------------------

        /** The structure holding the data */
        PropertiesStructure ps;

        /** Creates new data holder. */
        DataRef(PropertiesFileEntry pfe, PropertiesStructure ps) {
            this.pfe = pfe;
            this.ps  = ps;
        }

        /** Informs the SourceElementImpl about the releasing
        * of this class from the memory.
        */
        public void finalize() throws Throwable {
            if (pfe != null) {
                pfe.getHandler().dataRefReleased(this);
            }
            super.finalize();
        }
    }
}