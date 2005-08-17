/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.db.explorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.db.explorer.actions.AddDriverAction;
import org.netbeans.modules.db.explorer.driver.JDBCDriverConvertor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * This class manages the list of JDBC drivers registered in the Database Explorer.
 */
public final class JDBCDriverManager {
    
    /**
     * The JDBCDriverManager singleton instance.
     */
    private static JDBCDriverManager DEFAULT = null;
    
    private Lookup.Result result = getLookupResult();
    
    /**
     * The list of listeners.
     */
    private List/*<JDBCDriverListener>*/ listeners = new ArrayList(1);
    
    /**
     * 
     * Gets the JDBCDriverManager singleton instance.
     * 
     * @return the JDBCDriverManager singleton instance.
     */
    public static synchronized JDBCDriverManager getDefault() {
        if (DEFAULT == null) {
            JDBCDriverConvertor.importOldDrivers();
            DEFAULT = new JDBCDriverManager();
        }
        return DEFAULT;
    }

    /**
     * Private constructor.
     */
    private JDBCDriverManager() {
        result.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent e) {
                fireListeners();
            }
        });
    }
    
    /** 
     * Gets the registered JDBC drivers.
     *
     * @return a non-null array of JDBCDriver instances.
     */
    public JDBCDriver[] getDrivers() {
        Collection drivers = result.allInstances();
        return (JDBCDriver[])drivers.toArray(new JDBCDriver[drivers.size()]);
    }
    
    /**
     * Gets the registered JDBC drivers with the specified class name.
     *
     * @param drvClass driver class name; must not be null.
     *
     * @return a non-null array of JDBCDriver instances with the specified class name.
     *
     * @throws NullPointerException if the specified class name is null.
     */
    public JDBCDriver[] getDrivers(String drvClass) {
        if (drvClass == null) {
            throw new NullPointerException();
        }
        LinkedList result = new LinkedList();
        JDBCDriver[] drvs = getDrivers();
        for (int i = 0; i < drvs.length; i++) {
            if (drvClass.equals(drvs[i].getClassName())) {
                result.add(drvs[i]);
            }
        }
        return (JDBCDriver[])result.toArray(new JDBCDriver[result.size()]);
    }

    /**
     * Adds a new JDBC driver.
     * 
     * @param driver the JDBCDriver instance describing the driver to be added;
     * must not be null.
     *
     * @throws NullPointerException if the specified driver is null.
     *         DatabaseException if an error occured while adding the driver.
     */
    public void addDriver(JDBCDriver driver) throws DatabaseException {
        if (driver == null) {
            throw new NullPointerException();
        }
        try {
            JDBCDriverConvertor.create(driver);
        } catch (IOException ioe) {
            throw new DatabaseException(ioe);
        }
    }
    
    /**
     * Removes a JDBC driver.
     * 
     * @param driver the JDBCDriver instance to be removed.
     *
     * @throws DatabaseException if an error occured while adding the driver.
     */
    public void removeDriver(JDBCDriver driver) throws DatabaseException {
        try {
            JDBCDriverConvertor.remove(driver);
        } catch (IOException ioe) {
            throw new DatabaseException(ioe);
        }
    }
    
    /**
     * Shows the Add Driver dialog, allowing the user to add a new JDBC driver.
     */
    public void showAddDriverDialog() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new AddDriverAction.AddDriverDialogDisplayer().showDialog();
                }
            });
        } else {
            new AddDriverAction.AddDriverDialogDisplayer().showDialog();
        }
    }
    
    /**
     * Registers a JDBCDriverListener.
     */
    public void addDriverListener(JDBCDriverListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Unregisters the specified JDBCDriverListener.
     */
    public void removeDriverListener(JDBCDriverListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private void fireListeners() {
        List/*<JDBCDriverListener>*/ listenersCopy;
        
        synchronized (listeners) {
            listenersCopy = new ArrayList(listeners);
        }
        
        for (Iterator i= listeners.iterator(); i.hasNext();) {
            JDBCDriverListener listener = (JDBCDriverListener)i.next();
            listener.driversChanged();
        }
    }
    
    private synchronized Lookup.Result getLookupResult() {
        if (result == null) {
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(JDBCDriverConvertor.DRIVERS_PATH);
            DataFolder folder = DataFolder.findFolder(fo);
            result = new FolderLookup(folder).getLookup().lookup(new Lookup.Template(JDBCDriver.class));
        }
        return result;
    }
}
