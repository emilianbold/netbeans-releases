/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.ui;

import java.io.IOException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class Utilities {
    private static Logger logger = Logger.getLogger(Utilities.class.getName());     
    private static final String UNSORTED_CATEGORY = NbBundle.getMessage (Utilities.class, "Utilities_Unsorted_Category");
    private static final String LIBRARIES_CATEGORY = NbBundle.getMessage (Utilities.class, "Utilities_Libraries_Category");
    private static final String BRIDGES_CATEGORY = NbBundle.getMessage (Utilities.class, "Utilities_Bridges_Category");
    
    @SuppressWarnings ("deprecation")
    public static List<UnitCategory> makeInstalledCategories (List<UpdateUnit> units) {
        //units = filterUneditable(units);
            List<UnitCategory> res = new ArrayList<UnitCategory> ();
            List<String> names = new ArrayList<String> ();
            for (UpdateUnit u : units) {
                UpdateElement el = u.getInstalled();
                if (el != null) {
                    String catName = el.getCategory();
                    if (u.isAutoload () || u.isFixed ()) {
                        catName = LIBRARIES_CATEGORY;
                    } else if (u.isEager ()) {
                        catName = BRIDGES_CATEGORY;
                    } else if (catName == null || catName.length () == 0) {
                        catName = UNSORTED_CATEGORY;
                    }
                    Unit.Installed i = new Unit.Installed (u);
                    if (names.contains(catName)) {
                        UnitCategory cat = res.get(names.indexOf(catName));
                        cat.addUnit (i);
                    } else {
                        UnitCategory cat = new UnitCategory(catName);
                        cat.addUnit (i);
                        res.add(cat);
                        names.add(catName);
                    }
                }
            }
            sort (res);                   
            logger.log(Level.FINER, "makeInstalledCategories (" + units.size() + ") returns " + res.size());
            return res;
        };

    public static List<UnitCategory> makeUpdateCategories (final List<UpdateUnit> units, boolean isNbms) {
        List<UnitCategory> res = new ArrayList<UnitCategory> ();
        List<String> names = new ArrayList<String> ();
        for (UpdateUnit u : units) {
            UpdateElement el = u.getInstalled ();
            if (el != null) {
                List<UpdateElement> updates = u.getAvailableUpdates ();
                if (updates.isEmpty()) {
                    continue;
                }
                String catName = el.getCategory();
//                if (u.isAutoload () || u.isFixed ()) {
//                    catName = LIBRARIES_CATEGORY;
//                } else if (u.isEager ()) {
//                    catName = BRIDGES_CATEGORY;
//                } else if (catName == null || catName.length () == 0) {
//                    catName = UNSORTED_CATEGORY;
//                }
                if (catName == null || catName.length () == 0) {
                    catName = UNSORTED_CATEGORY;
                }
                if (names.contains (catName)) {
                    UnitCategory cat = res.get (names.indexOf (catName));
                    cat.addUnit (new Unit.Update (u, isNbms));
                } else {
                    UnitCategory cat = new UnitCategory (catName);
                    cat.addUnit (new Unit.Update (u, isNbms));
                    res.add (cat);
                    names.add (catName);
                }
            }
        }
        sort (res);                           
        logger.log(Level.FINER, "makeUpdateCategories (" + units.size () + ") returns " + res.size ());
        return res;
        };

    public static List<UnitCategory> makeAvailableCategories (final List<UpdateUnit> units, boolean isNbms) {
        List<UnitCategory> res = new ArrayList<UnitCategory> ();
        List<String> names = new ArrayList<String> ();
        for (UpdateUnit u : units) {
            UpdateElement el = u.getInstalled ();
            if (el == null) {
                List<UpdateElement> updates = u.getAvailableUpdates ();
                if (updates == null || updates.size() == 0) {
                    continue;
                }
                UpdateElement upEl = updates.get (0);
                String catName = upEl.getCategory();
//                if (u.isAutoload () || u.isFixed ()) {
//                    catName = LIBRARIES_CATEGORY;
//                } else if (u.isEager ()) {
//                    catName = BRIDGES_CATEGORY;
//                } else if (catName == null || catName.length () == 0) {
//                    catName = UNSORTED_CATEGORY;
//                }
                if (catName == null || catName.length () == 0) {
                    catName = UNSORTED_CATEGORY;
                }
                if (names.contains (catName)) {
                    UnitCategory cat = res.get (names.indexOf (catName));
                    cat.addUnit (new Unit.Available (u, isNbms));
                } else {
                    UnitCategory cat = new UnitCategory (catName);
                    cat.addUnit (new Unit.Available (u, isNbms));
                    res.add (cat);
                    names.add (catName);
                }
            }
        }
        sort (res);
        logger.log(Level.FINER, "makeAvailableCategories (" + units.size () + ") returns " + res.size ());

        return res;
        };

        //candidate to delete
    /*public static List<UnitCategory> makeLocalCategories (final List<UpdateUnit> units) {
        List<UnitCategory> res = new ArrayList<UnitCategory> ();
        List<String> names = new ArrayList<String> ();
        for (UpdateUnit u : units) {
            List<UpdateElement> updates = u.getAvailableUpdates ();
            UpdateElement upEl = updates.get (0);
            String catName = upEl.getCategory ();
            if (names.contains (catName)) {
                UnitCategory cat = res.get (names.indexOf (catName));
                cat.addUnit (new Unit.Available (u));
            } else {
                UnitCategory cat = new UnitCategory (catName);
                cat.addUnit (new Unit.Available (u));
                res.add (cat);
                names.add (catName);
            }
        }
        sort (res);
        logger.log(Level.FINER, "makeAvailableCategories (" + units.size () + ") returns " + res.size ());

        return res;
        };*/

    public static void showURL (URL href) {
        HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
        assert displayer != null : "HtmlBrowser.URLDisplayer found.";
        if (displayer != null) {
            displayer.showURL (href);
        } else {
            logger.log (Level.INFO, "No URLDisplayer found.");
        }
    }
    
    public static List<UpdateElement> getRequiredElements(UpdateUnit unit, UpdateElement el, OperationContainer<OperationSupport> container) {
        List<UpdateElement> reqs = Collections.emptyList();
        if (container.canBeAdded(unit, el)) {
            OperationInfo<OperationSupport> info = container.add (unit,el);
            reqs = new LinkedList<UpdateElement> (info.getRequiredElements());
        }
        return reqs;
    }        
    
    private static void sort (List<UnitCategory> res) {
        final Collator collator = Collator.getInstance ();
        final Comparator<String> comparator = new Comparator<String> () {
            public int compare (String o1, String o2) {
                    // Libraries always put in the last place.
                    if (LIBRARIES_CATEGORY == o1) {
                        if (LIBRARIES_CATEGORY == o2) {
                            return 0;
                        } else {
                            return 1;
                        }
                    } else {
                        if (o2 == LIBRARIES_CATEGORY) {
                            return -1;
                        }
                        // Eager modules come between categories and libraries.
                        if (BRIDGES_CATEGORY == o1) {
                            if (BRIDGES_CATEGORY == o2) {
                                return 0;
                            } else {
                                return 1;
                            }
                        } else {
                            if (BRIDGES_CATEGORY == o2) {
                                return -1;
                            }
                            // Eager modules come between categories and libraries.
                            if (UNSORTED_CATEGORY == o1) {
                                if (UNSORTED_CATEGORY == o2) {
                                    return 0;
                                } else {
                                    return 1;
                                }
                            } else {
                                if (UNSORTED_CATEGORY == o2) {
                                    return -1;
                                }
                            }

                            return collator.compare (o1, o2);
                        }
                    }
            }
        };
        
        Collections.sort (res, new Comparator<UnitCategory> () {
            public int compare (UnitCategory o1, UnitCategory o2) {
                return comparator.compare (o1.getCategoryName (), o2.getCategoryName ());
            }
        });
            
        for (UnitCategory unitCategory : res) {
            Collections.sort (unitCategory.units, new Comparator<Unit> () {
                public int compare (Unit u1, Unit u2) {
                    return collator.compare (u1.getDisplayName (), u2.getDisplayName ());
                }
            });
        }
    }
    
    public static boolean isGtk () {
        return "GTK".equals (UIManager.getLookAndFeel ().getID ()); // NOI18N
    }
    
    public static String getDownloadSizeAsString (int size) {
        int gbSize = size / (1024 * 1024 * 1024);
        if (gbSize > 0) {
            return gbSize + getBundle ("Utilities_DownloadSize_GB");
        }
        int mbSize = size / (1024 * 1024);
        if (mbSize > 0) {
            return mbSize + getBundle ("Utilities_DownloadSize_MB");
        }
        int kbSize = size / 1024;
        if (kbSize > 0) {
            return kbSize + getBundle ("Utilities_DownloadSize_kB");
        }
        return size + getBundle ("Utilities_DownloadSize_B");
    }
    
    private static String getBundle (String key) {
        return NbBundle.getMessage (Utilities.class, key);
    }
    
    // Call PluginManagerUI.updateUnitsChanged() after refresh to reflect change in model
    public static void presentRefreshProvider (UpdateUnitProvider provider, PluginManagerUI manager, boolean force) {
        assert ! SwingUtilities.isEventDispatchThread () : "Don't presentRefreshProvider() call in EQ!";
        doRefreshProviders (Collections.singleton (provider), manager, force);
    }
    
    // Call PluginManagerUI.updateUnitsChanged() after refresh to reflect change in model
    public static void presentRefreshProviders (Collection<UpdateUnitProvider> providers, PluginManagerUI manager, boolean force) {
        assert ! SwingUtilities.isEventDispatchThread () : "Don't presentRefreshProvider() call in EQ!";
        doRefreshProviders (providers, manager, force);
    }
    
    // Call PluginManagerUI.updateUnitsChanged() after refresh to reflect change in model
    public static void presentRefreshProviders (PluginManagerUI manager, boolean force) {
        assert ! SwingUtilities.isEventDispatchThread () : "Don't presentRefreshProviders() call in EQ!";
        doRefreshProviders (UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (true), manager, force);
    }
    
    private static void doRefreshProviders (Collection<UpdateUnitProvider> providers, PluginManagerUI manager, boolean force) {
        ProgressHandle handle = ProgressHandleFactory.createHandle ("refresh-providers-handle"); // NOI18N
        JComponent progressComp = ProgressHandleFactory.createProgressComponent (handle);
        JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
        detailLabel.setHorizontalAlignment (SwingConstants.RIGHT);
        try {
            manager.setProgressComponent (detailLabel, progressComp);
            handle.setInitialDelay (0);
            handle.start ();
            for (UpdateUnitProvider p : providers) {
                p.refresh (handle, force);
            }
        } catch (IOException ioe) {
            logger.log (Level.FINE, ioe.getMessage(), ioe);
            if (handle != null) {
                handle.finish ();
            }
            NetworkProblemPanel.showNetworkProblemDialog();
        } finally {
            if (handle != null) {
                handle.finish ();
            }
            // XXX: Avoid NPE when called refresh providers on selected units
            // #101836: OperationContainer.contains() sometimes fails
            Containers.initNotify ();
            manager.unsetProgressComponent (detailLabel, progressComp);
        }
    }
}
