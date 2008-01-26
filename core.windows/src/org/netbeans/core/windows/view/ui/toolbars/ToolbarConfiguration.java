/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.windows.view.ui.toolbars;

import java.util.logging.Logger;
import org.netbeans.core.NbPlaces;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.XMLDataObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.xml.sax.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.logging.Level;
import org.netbeans.core.windows.view.ui.MainWindow;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.windows.WindowManager;

/** Toolbar configuration.
 * It can load configuration from DOM Document, store configuration int XML file. 
 * Toolbar configuration contains list of all correct toolbars (toolbars which are
 * represented int ToolbarPool too), waiting toolbars (toolbars which was described
 * [it's position, visibility] but there is representation int ToolbarPool).
 * There is list of rows (ToolbarRow) and map of invisible toolbars.
 *
 * @author Libor Kramolis
 */
public final class ToolbarConfiguration extends Object 
implements ToolbarPool.Configuration, PropertyChangeListener {
    /** location outside the IDE */
    protected static final String TOOLBAR_DTD_WEB           =
        "http://www.netbeans.org/dtds/toolbar.dtd"; // NOI18N
    /** toolbar dtd public id */
    protected static final String TOOLBAR_DTD_PUBLIC_ID     =
        "-//NetBeans IDE//DTD toolbar//EN"; // NOI18N
    /** toolbar prcessor class */
    protected static final Class  TOOLBAR_PROCESSOR_CLASS   = ToolbarProcessor.class;
    /** toolbar icon base */
    protected static final String TOOLBAR_ICON_BASE         =
        "/org/netbeans/core/windows/toolbars/xmlToolbars"; // NOI18N
    
    /** error manager */
    private static Logger ERR = Logger.getLogger("org.netbeans.core.windows.toolbars"); // NOI18N

    /** last time the document has been reloaded */
    private volatile long lastReload;
    
    /** xml extension */
    protected static final String EXT_XML                   = "xml"; // NOI18N
//      /** xmlinfo extension */
//      protected static final String EXT_XMLINFO               = "xmlinfo"; // NOI18N

    /** xml element for configuration (root element) */
    protected static final String TAG_CONFIG                = "Configuration"; // NOI18N
    /** xml element for row */
    protected static final String TAG_ROW                   = "Row"; // NOI18N
    /** xml element for toolbar */
    protected static final String TAG_TOOLBAR               = "Toolbar"; // NOI18N
    /** xml attribute for toolbar name */
    protected static final String ATT_TOOLBAR_NAME          = "name"; // NOI18N
    /** xml attribute for toolbar position */
    protected static final String ATT_TOOLBAR_POSITION      = "position"; // NOI18N
    /** xml attribute for toolbar visible */
    protected static final String ATT_TOOLBAR_VISIBLE       = "visible"; // NOI18N

    /** standard panel for all configurations */
    private static JPanel  toolbarPanel;
    /** mapping from configuration instances to their names */
    private static WeakHashMap<ToolbarConfiguration, String> confs2Names = 
            new WeakHashMap<ToolbarConfiguration, String>(10);
    
    /** toolbar layout manager for this configuration */
    private        ToolbarLayout toolbarLayout;
    /** toolbar drag and drop listener */
    private   ToolbarDnDListener toolbarListener;

    /** All toolbars which are represented in ToolbarPool too. */
    private WeakHashMap<String, ToolbarConstraints> allToolbars;
    /** List of visible toolbar rows. */
    private Vector<ToolbarRow> toolbarRows;
    /** All invisible toolbars (visibility==false || tb.isCorrect==false). */
    private HashMap<ToolbarConstraints,Integer>     invisibleToolbars;
    
    /** Toolbar menu is global so it is static. It it the same for all toolbar
     configurations. */
    private static JMenu toolbarMenu;
    
    /** Toolbars which was described in DOM Document,
	but which aren't represented in ToolbarPool.
	For exapmle ComponentPalette and first start of IDE. */
    private WeakHashMap<String, ToolbarConstraints> waitingToolbars;
    /** Name of configuration. */
    private String      configName;
    /** Display name of configuration. */
    private String      configDisplayName;
    /** Cached preferred width. */
    private int         prefWidth;
    /** true during toggling big/small toolbar buttons */
    private boolean togglingIconSize = false;
    /** variable to signal that we are just writing the content of configuration
     * and we should ignore all changes. In such case set to Boolean.TRUE
     */
    private final ThreadLocal<Boolean> WRITE_IN_PROGRESS = new ThreadLocal<Boolean> ();

   // private static final ResourceBundle bundle = NbBundle.getBundle (ToolbarConfiguration.class);

    /** Creates new empty toolbar configuration for specific name.
     * @param name new configuration name
     */
    public ToolbarConfiguration (String name, String displayName) {
        configName = name;
        configDisplayName = displayName;
        // fix #44537 - just doing the simple thing of hacking the extension out of the display name.. node.getDisplayName is too unpredictable.
        if (configDisplayName.endsWith(".xml")) {
            configDisplayName = configDisplayName.substring(0, configDisplayName.length() - ".xml".length());
        }
        initInstance ();
        // asociate name and configuration instance
        confs2Names.put(this, name);
    }

    /** Creates new toolbar configuration for specific name and from specific XMLDataObject
     * @param xml XMLDataObject representing a toolbar configuration
     */
    public ToolbarConfiguration(XMLDataObject xml) throws IOException {
        this(xml.getNodeDelegate().getName(), xml.getNodeDelegate().getDisplayName());
        readConfig(xml);
    }

    private void readConfig(XMLDataObject xml) throws IOException {
        Parser parser = xml.createParser();
        

        ToolbarParser handler = new ToolbarParser();
        parser.setEntityResolver(handler);
        parser.setDocumentHandler(handler);
     
        InputStream is = null;
        try {
            is = xml.getPrimaryFile().getInputStream();
            parser.parse(new InputSource(is));
        } catch (Exception saxe) {
            throw (IOException) new IOException(saxe.toString()).initCause(saxe);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException exc) {
                Logger.getLogger(ToolbarConfiguration.class.getName()).log(Level.WARNING, null, exc);
            }
        }
        checkToolbarRows();
    }
    
    private class ToolbarParser extends HandlerBase implements EntityResolver {
        private ToolbarRow currentRow = null;
        private int toolbarIndex = 0;
        
        public void startElement(String name, AttributeList amap) throws SAXException {
            if (TAG_ROW.equals(name)) {
                toolbarIndex = 0;
                currentRow = new ToolbarRow(ToolbarConfiguration.this);
                addRow(currentRow);
            }
            else if (currentRow != null && TAG_TOOLBAR.equals(name)) {
                String tbname = amap.getValue(ATT_TOOLBAR_NAME);
                if (tbname == null || tbname.equals("")) // NOI18N
                    return;
                
                String  posStr = amap.getValue(ATT_TOOLBAR_POSITION);
                Integer pos = null;
                if (posStr != null)
                    pos = new Integer(posStr);
                
                String visStr = amap.getValue(ATT_TOOLBAR_VISIBLE);
                Boolean vis;
                if (visStr != null)
                    vis = Boolean.valueOf(visStr);
                else
                    vis = Boolean.TRUE;
                
                addToolbar(currentRow, checkToolbarConstraints (tbname, pos, vis, toolbarIndex++));
            }
        }
        
        @Override
        public void endElement(String name) throws SAXException {
            if (TAG_ROW.equals(name)) {
                currentRow = null;
            }
        }
        
        @Override
        public InputSource resolveEntity(String pubid, String sysid) {
            return new InputSource(new java.io.ByteArrayInputStream(new byte[0]));
        }
    };
    /** Clean all the configuration parameters.
     */
    private void initInstance () {
        allToolbars = new WeakHashMap<String, ToolbarConstraints>();
        waitingToolbars = new WeakHashMap<String, ToolbarConstraints>();
        toolbarRows = new Vector<ToolbarRow>();
        invisibleToolbars = new HashMap<ToolbarConstraints, Integer>();
        toolbarListener = new ToolbarDnDListener (this);
    }
    
    /** @return returns string from bundle for given string pattern */
    static final String getBundleString (String bundleStr) {
        return NbBundle.getMessage(ToolbarConfiguration.class, bundleStr);
    }
    
    /** Finds toolbar configuration which has given name.
     * @return toolbar configuration instance which ID is given name or null
     * if no such configuration can be found */
    public static final ToolbarConfiguration findConfiguration (String name) {
        Map.Entry curEntry = null;
        for (Iterator iter = confs2Names.entrySet().iterator(); iter.hasNext(); ) {
            curEntry = (Map.Entry)iter.next();
            if (name.equals((String)curEntry.getValue())) {
                return (ToolbarConfiguration)curEntry.getKey();
            }
        }
        // no luck
        return null;
    }

    /** Add toolbar to list of all toolbars.
     * If specified toolbar constraints represents visible component 
     * it is added to specified toolbar row.
     * Othewise toolbar constraints is added to invisible toolbars.
     *
     * @param row toolbar row of new toolbar is part
     * @param tc added toolbar represented by ToolbarConstraints
     */
    void addToolbar (ToolbarRow row, ToolbarConstraints tc) {
        if (tc == null)
            return;

        if (tc.isVisible())
            row.addToolbar (tc);
        else {
            int rI;
            if (row == null)
                rI = toolbarRows.size();
            else
                rI = toolbarRows.indexOf (row);
            invisibleToolbars.put (tc, Integer.valueOf(rI));
        }
        allToolbars.put (tc.getName(), tc);
    }

    /** Remove toolbar from list of all toolbars.
     * This could mean that toolbar is represented only in DOM document.
     *
     * @param name name of removed toolbar
     */
    ToolbarConstraints removeToolbar (String name) {
        ToolbarConstraints tc = allToolbars.remove (name);
        if (tc.destroy())
            checkToolbarRows();
        return tc;
    }

    /** Add toolbar row as last row.
     * @param row added toolbar row
     */
    void addRow (ToolbarRow row) {
        addRow (row, toolbarRows.size());
    }

    /** Add toolbar row to specific index.
     * @param row added toolbar row
     * @param index specified index of toolbar position
     */
    void addRow (ToolbarRow row, int index) {
	/* It is important to recompute row neighbournhood. */
        ToolbarRow prev = null;
        ToolbarRow next = null;
        int rowCount = toolbarRows.size();
        if( index > 0 && index <= rowCount )
            prev = toolbarRows.elementAt( index - 1 );
        if( index >= 0 && index < rowCount )
            next = toolbarRows.elementAt (index);

        if (prev != null)
            prev.setNextRow (row);
        row.setPrevRow (prev);
        row.setNextRow (next);
        if (next != null)
            next.setPrevRow (row);

        toolbarRows.insertElementAt (row, index);
        updateBounds (row);
    }

    /** Remove toolbar row from list of all rows.
     * @param row removed toolbar row
     */
    void removeRow (ToolbarRow row) {
	/* It is important to recompute row neighbournhood. */
        ToolbarRow prev = row.getPrevRow();
        ToolbarRow next = row.getNextRow();
        if (prev != null) {
            prev.setNextRow (next);
        }
        if (next != null) {
            next.setPrevRow (prev);
        }

        toolbarRows.removeElement (row);
        updateBounds (next);
        revalidateWindow();
    }

    /** Update toolbar row cached bounds.
     * @param row updated row
     */
    void updateBounds (ToolbarRow row) {
        while (row != null) {
            row.updateBounds();
            row = row.getNextRow();
        }
    }
    
    private static final ToolbarPool toolbarPool () {
        return ToolbarPool.getDefault ();
    }

    /** Revalidates toolbar pool window.
     * It is important for change height when number of rows is changed.
     */
    void revalidateWindow () {
        // PENDING
        toolbarPanel().revalidate();
        // #15930. Always replane even we are in AWT thread already.
//        SwingUtilities.invokeLater(new Runnable () {
//            public void run () {
//                doRevalidateWindow();
//            }
//        });
    }
    
//    /** Performs revalidating work */
//    private void doRevalidateWindow () {
//        toolbarPanel().revalidate();
//        java.awt.Window w = SwingUtilities.windowForComponent (toolbarPool ());
//        if (w != null) {
//            w.validate ();
//        }
//    } // PENDING

    /** 
     * @param row specified toolbar row
     * @return index of toolbar row
     */
    int rowIndex (ToolbarRow row) {
        return toolbarRows.indexOf (row);
    }

    /** Updates cached preferred width of toolbar configuration.
     */
    void updatePrefWidth () {
        prefWidth = 0;
        for (ToolbarRow tr: toolbarRows) {
            prefWidth = Math.max (prefWidth, tr.getPrefWidth());
        }
    }

    /**
     * @return configuration preferred width
     */
    int getPrefWidth () {
        return prefWidth;
    }

    /**
     * @return configuration preferred height, sum of preferred heights of rows.
     * If there is no row, preferred height is 0.
     */
    int getPrefHeight () {
        if (getRowCount() == 0) return 0;
        ToolbarRow lastRow = toolbarRows.lastElement();
        return getRowVertLocation(lastRow) + lastRow.getPreferredHeight();
    }

    /** Checks toolbar rows. If there is some empty row it is removed.
     */
    void checkToolbarRows () {
        Object[] rows = toolbarRows.toArray();
        ToolbarRow row;

        for (int i = rows.length - 1; i >= 0; i--) {
            row = (ToolbarRow)rows[i];
            if (row.isEmpty())
                removeRow (row);
        }
    }

    /**
     * @return number of rows.
     */
    int getRowCount () {
        return toolbarRows.size();
    }

    /**
     * @param name toolbar constraints name
     * @return toolbar constraints of specified name
     */
    ToolbarConstraints getToolbarConstraints (String name) {
        return allToolbars.get (name);
    }

    /** Checks toolbars constraints if there is some of specific name.
     * If isn't then is created new toolbar constraints. Othewise is old
     * toolbar constraints confronted with new values (position, visibility).
     * @param name name of checked toolbar
     * @param position position of toolbar
     * @param visible visibility of toolbar
     * @param toolbarIndex index of the toolbar as defined by the order of 
     * declarations in layers
     * @return toolbar constraints for specifed toolbar name
     */
    ToolbarConstraints checkToolbarConstraints (String name, Integer position, Boolean visible, int toolbarIndex) {
        ToolbarConstraints tc = allToolbars.get (name);
        if (tc == null)
            tc = new ToolbarConstraints (this, name, position, visible, toolbarIndex);
        else
            tc.checkNextPosition (position, visible);
        return tc;
    }

    /** Checks whole toolbar configuration.
     * It confronts list of all toolbars and waiting toolbars
     * with toolbars represented by ToolbarPool.
     *
     * @return true if there is some change and is important another check.
     */
    boolean checkConfigurationOver () {
        boolean change = false;
        String name;
        String[] waNas = waitingToolbars.keySet().toArray(new String[0]);
        String[] names = allToolbars.keySet().toArray(new String[0]);
        
        /* Checks ToolbarPool with waiting list. */
        for (int i = 0; i < waNas.length; i++) {
            name = waNas[i];
            if (toolbarPool ().findToolbar (name) != null) {  /* If there is new toolbar in the pool
							      which was sometimes described ... */
                ToolbarConstraints tc = waitingToolbars.remove(name);
		                                           /* ... it's removed from waiting ... */
                allToolbars.put (name, tc);                /* ... so it's added to correct toolbars ... */
                addVisible (tc);                         /* ... and added to visible toolbars. */
                change = true;
            }
        }

        /* Checks ToolbarPool with list of all toolbars ... reverse process than previous for. */
        for (int i = 0; i < names.length; i++) {
            name = names[i];
            if (toolbarPool ().findToolbar (name) == null) {  /* If there is toolbar which is not represented int pool ... */
                ToolbarConstraints tc = removeToolbar (name);  /* ... so let's remove toolbar from all toolbars ... */
                waitingToolbars.put (name, tc);                /* ... and add to waiting list. */
                invisibleToolbars.put (tc, Integer.valueOf(tc.rowIndex()));
                change = true;
            }
        }
        if (change || Utilities.arrayHashCode(toolbarPool().getConfigurations()) != lastConfigurationHash) {
            rebuildMenu();
        }
        return change;
    }

    void refresh() {
        // #102450 - don't allow row rearrangement during icon size toggle 
        togglingIconSize = true;
        rebuildPanel();
        togglingIconSize = false;
        rebuildMenu();
    }
    
    private int lastConfigurationHash = -1;
    public void rebuildMenu() {
        if (toolbarMenu != null) {
            toolbarMenu.removeAll();
            fillToolbarsMenu(toolbarMenu, false);
            revalidateWindow();
        }
    }
    
    /** Removes toolbar from visible toolbars.
     * @param tc specified toolbar
     */
    private void removeVisible (ToolbarConstraints tc) {
        invisibleToolbars.put (tc, Integer.valueOf (tc.rowIndex()));
        if (tc.destroy())
            checkToolbarRows();
        tc.setVisible (false);

        //reflectChanges();
    }

    /** Adds toolbar from list of invisible to visible toolbars.
     * @param tc specified toolbar
     */
    private void addVisible (ToolbarConstraints tc) {
        int rC = toolbarRows.size();
        int pos = invisibleToolbars.remove (tc).intValue();
        tc.setVisible (true);
        for (int i = pos; i < pos + tc.getRowCount(); i++) {
            getRow (i).addToolbar (tc, tc.getPosition());
        }

        if (rC != toolbarRows.size())
            revalidateWindow();
        //reflectChanges();
    }

    /**
     * @param rI index of required row
     * @return toolbar row of specified index.
     * If rI is out of bounds then new row is created.
     */
    ToolbarRow getRow (int rI) {
        ToolbarRow row;
        int s = toolbarRows.size();
        if (rI < 0) {
            row = new ToolbarRow (this);
            addRow (row, 0);
        } else if (rI >= s) {
            row = new ToolbarRow (this);
            addRow (row);
        } else {
            row = toolbarRows.elementAt(rI);
        }
        return row;
    }

    /**
     * @return toolbar row at last row position.
     */
    ToolbarRow createLastRow () {
        return getRow (toolbarRows.size());
    }

    /** Reactivate toolbar panel.
     * All components are removed and again added using ToolbarPool's list of correct toolbars.
     *
     * @param someBarRemoved if some toolbar was previously removed and is important to reflect changes
     * @param writeAtAll if false the content of disk will not be updated at all
     */
    void reactivatePanel (boolean someBarRemoved, boolean writeAtAll) {
        toolbarPanel().removeAll();
        prefWidth = 0;

        Toolbar tbs[] = toolbarPool ().getToolbars();
        Toolbar tb;
        ToolbarConstraints tc;
        String name;
        ToolbarRow lastRow = null;

        for (int i = 0; i < tbs.length; i++) {
            tb = tbs[i];
            name = tb.getName();
            tc = allToolbars.get(name);
            if (tc == null) { /* If there is no toolbar constraints description defined yet ... */
                if (lastRow == null) {
                    if( toolbarRows.isEmpty() )
                        lastRow = createLastRow();
                    else
                        lastRow = getRow( toolbarRows.size()-1 );
                }
                tc = new ToolbarConstraints (this, name, null, Boolean.TRUE); /* ... there is created a new constraints. */
                addToolbar (lastRow, tc);
            }
            toolbarPanel().add (tb, tc);
        }
        
        revalidateWindow();

    }
    
    /** Rebuild toolbar panel when size of icons is changed.
     * All components are removed and again added using ToolbarPool's list of correct toolbars.
     */
    private void rebuildPanel () {
        toolbarPanel().removeAll();
        prefWidth = 0;

        Toolbar tbs[] = toolbarPool ().getToolbars();
        Toolbar tb;
        ToolbarConstraints tc;
        String name;
        ToolbarRow newRow = null;
        boolean smallToolbarIcons = (ToolbarPool.getDefault().getPreferredIconSize() == 16);
        for (int i = 0; i < tbs.length; i++) {
            tb = tbs[i];
            name = tb.getName();
            Component [] comps = tb.getComponents();
            for (int j = 0; j < comps.length; j++) {
                if (comps[j] instanceof JComponent) {
                    if (smallToolbarIcons) {
                        ((JComponent) comps[j]).putClientProperty("PreferredIconSize",null); //NOI18N
                    } else {
                        ((JComponent) comps[j]).putClientProperty("PreferredIconSize",Integer.valueOf(24)); //NOI18N
                    }
                }
            }
            tc = allToolbars.get(name);
            if (tc == null) { /* If there is no toolbar constraints description defined yet ... */
                if (newRow == null) {
                    newRow = createLastRow();
                }
                tc = new ToolbarConstraints (this, name, null, Boolean.TRUE);  /* ... there is created a new constraints. */
                addToolbar (newRow, tc);
            }
            toolbarPanel().add (tb, tc);
        }
        revalidateWindow();
    }
    
    /**
     * @return true if if important reactivate component.
     */
    boolean isImportantActivateComponent () {
        Object[] names = allToolbars.keySet().toArray();
        Toolbar[] toolbars = toolbarPool ().getToolbars();

	/* Is number of toolbars int local list and toolbar pool list different? */
        if (names.length != toolbars.length)
            return true;

	/* Is name of current configuration differrent of last toolbar pool configuration? */
        if (! configName.equals (toolbarPool ().getConfiguration()))
            return true;

        return false;
    }

    /** Reflects configuration changes ... write it to document.
     */
    void reflectChanges () {
        try {
            writeDocument();
        } catch (IOException e) { /* ??? */ }
    }

    /////////////////////////////////
    // from ToolbarPool.Configuration

    /** Activates the configuration and returns right
     * component that can display the configuration.
     * @return representation component
     */
    public Component activate () {
        return activate (isImportantActivateComponent (), true);
    }
        
        
    /** Activate.
     * @param isImportant is the change of structure important
     * @param writeAtAll write changes to disk or not?
     */
    private Component activate (boolean isImportant, boolean writeAtAll) {
        toolbarPool().setToolbarsListener (toolbarListener);

        boolean someBarRemoved = checkConfigurationOver();

        if (isImportant || someBarRemoved) {
            toolbarLayout = new ToolbarLayout (this);
            toolbarPanel().setLayout (toolbarLayout);
            reactivatePanel (someBarRemoved, writeAtAll);
            rebuildMenu();
        }

        return toolbarPanel();
    }

    /** Name of the configuration.
     * @return the name
     */
    public String getName () {
        return configName;
    }
    
    public String getDisplayName () {
        return configDisplayName;
    }

    /** Popup menu that should be displayed when the users presses
     * right mouse button on the panel. This menu can contain
     * contains list of possible configurations, additional actions, etc.
     *
     * @return popup menu to be displayed
     */
    public JPopupMenu getContextMenu () {
        JPopupMenu menu = new JPopupMenu();
        fillToolbarsMenu(menu, true);
        return menu;
    }

    /** Fills given menu with toolbars and configurations items and returns
     * filled menu. */ 
    public JMenu getToolbarsMenu (JMenu menu) {
        fillToolbarsMenu(menu, false);
        toolbarMenu = menu;
        return menu;
    }
    
    public static void resetToolbarIconSize() {
        ToolbarPool.getDefault().setPreferredIconSize(24);
        //Rebuild toolbar panel
        String name = ToolbarPool.getDefault().getConfiguration();
        ToolbarConfiguration tbConf = findConfiguration(name);
        if (tbConf != null) {
            tbConf.refresh();
        }
    }
    
    /** Fills given menu instance with list of toolbars and configurations */
    private void fillToolbarsMenu (JComponent menu, boolean isContextMenu) {
        MainWindow frame = (MainWindow)WindowManager.getDefault().getMainWindow();
        boolean fullScreen = frame.isFullScreenMode();
        
        lastConfigurationHash = Utilities.arrayHashCode(ToolbarPool.getDefault().getConfigurations());
        // generate list of available toolbars
        Iterator it = Arrays.asList (ToolbarPool.getDefault ().getToolbars ()).iterator ();
        while (it.hasNext()) {
            final Toolbar tb = (Toolbar)it.next();
            final String tbName = tb.getName();
            ToolbarConstraints tc = allToolbars.get(tb.getName());
            if (tc == null || tb == null) {
                //a toolbar configuration has been renamed (for whatever reason,
                //we permit this - I'm sure it's a popular feature).
                checkConfigurationOver();
            }

            
            if (tc != null && tb != null) {
                //May be null if a toolbar has been renamed
                JCheckBoxMenuItem mi = new JCheckBoxMenuItem (
                    tb.getDisplayName(), tc.isVisible()
                );
                mi.putClientProperty("ToolbarName", tbName); //NOI18N
                mi.addActionListener (new ActionListener () {
                    public void actionPerformed (ActionEvent ae) {
                        // #39741 fix
                        // for some reason (unknown to me - mkleint) the menu gets recreated repeatedly, which 
                        // can cause the formerly final ToolbarConstraints instance to be obsolete.
                        // that's why we each time look up the current instance on the allToolbars map.
                        ToolbarConstraints tc = allToolbars.get(tbName);
                        setToolbarVisible(tb, !tc.isVisible());
                    }
                });
                mi.setEnabled( !fullScreen );
                menu.add (mi);
            }
        }
        menu.add (new JPopupMenu.Separator());
        
        //Bigger toolbar icons
        boolean smallToolbarIcons = (ToolbarPool.getDefault().getPreferredIconSize() == 16);
        
        JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem (
            getBundleString("PROP_smallToolbarIcons"), smallToolbarIcons
        );
        cbmi.addActionListener (new ActionListener () {
              public void actionPerformed (ActionEvent ev) {
                  if (ev.getSource() instanceof JCheckBoxMenuItem) {
                      JCheckBoxMenuItem cb = (JCheckBoxMenuItem) ev.getSource();
                      // toggle big/small icons
                      boolean state = cb.getState();
                      if (state) {
                          ToolbarPool.getDefault().setPreferredIconSize(16);
                      } else {
                          ToolbarPool.getDefault().setPreferredIconSize(24);
                      }
                      //Rebuild toolbar panel
                      //#43652: Find current toolbar configuration
                      String name = ToolbarPool.getDefault().getConfiguration();
                      ToolbarConfiguration tbConf = findConfiguration(name);
                      if (tbConf != null) {
                          tbConf.refresh();
                      }
                  }
              }
        });
        cbmi.setEnabled( !fullScreen );
        menu.add (cbmi);
        
        menu.add( new JPopupMenu.Separator() );

        JMenuItem menuItem = new JMenuItem( new ResetToolbarsAction() );
        menuItem.setEnabled( !fullScreen );
        menu.add( menuItem );
        
        menuItem = new JMenuItem(getBundleString( "CTL_CustomizeToolbars" ) );
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                ConfigureToolbarPanel.showConfigureDialog();
            }
        });
        menuItem.setEnabled( !fullScreen );
        menu.add( menuItem );
        
        for( Component c : menu instanceof JPopupMenu 
                ? menu.getComponents() 
                : ((JMenu)menu).getPopupMenu().getComponents()) {
            if( c instanceof AbstractButton ) {
                AbstractButton b = (AbstractButton)c;

                if( isContextMenu ) {
                    b.setText( Actions.cutAmpersand(b.getText()) );
                } else {
                    Mnemonics.setLocalizedText( b, b.getText() );
                }
            }
        }
    } // getContextMenu
    
    boolean isTogglingIconSize () {
        return togglingIconSize;
    }

    /** Make toolbar visible/invisible in this configuration
     * @param tb toolbar
     * @param b true to make toolbar visible
     */
    public void setToolbarVisible(Toolbar tb, boolean b) {
        ToolbarConstraints tc = getToolbarConstraints(tb.getName());
        if (b) {
            addVisible(tc);
        } else {
            removeVisible(tc);
        }
        if (toolbarMenu != null) {
            //#39808 - somoewhat bruteforce approach, but works and is simple enough.
            // assumes the toolbar selection is always processed through the setToolbarVisible() method.
            //correct selection of the toolbar checkboxes in the main menu..
            Component[] elements = toolbarMenu.getMenuComponents();
            for (int i = 0; i < elements.length; i++) {
                JComponent component = (JComponent)elements[i];
                String tcmenu  = (String)component.getClientProperty("ToolbarName"); //NOI18N
                if (tcmenu != null && tcmenu.equals(tb.getName())) {
                    ((JCheckBoxMenuItem)component).setSelected(b);
                    break;
                }
            }
        }
        tb.setVisible(b);
        reflectChanges();
        firePropertyChange();
    }
    
    /** Returns true if the toolbar is visible in this configuration
     * @param tb toolbar
     * @return true if the toolbar is visible
     */
    public boolean isToolbarVisible(Toolbar tb) {
        ToolbarConstraints tc = getToolbarConstraints(tb.getName());
        return tc.isVisible();
    }
    
    PropertyChangeSupport pcs;
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null) {
            pcs.removePropertyChangeListener(l);
        }
    }
    
    private void firePropertyChange() {
        if (pcs != null) {
            pcs.firePropertyChange("constraints", null, null); // some constraints have changed
        }
    }

    //// writting

    /** Write actual toolbar configuration. */
    public void writeDocument () throws IOException {
        writeDocument (configName);
    }

    /** Write toolbar configuration for specified file name to xml.
     * @param cn configuration file name
     */
    private void writeDocument (final String cn) throws IOException {
        ERR.fine("writeDocument: " + cn); // NOI18N
        WritableToolbarConfiguration wtc = new WritableToolbarConfiguration (toolbarRows, invisibleToolbars);
        final StringBuffer sb = new StringBuffer ("<?xml version=\"1.0\"?>\n\n"); // NOI18N
        sb.append ("<!DOCTYPE ").append (TAG_CONFIG).append (" PUBLIC \""). // NOI18N
        append (TOOLBAR_DTD_PUBLIC_ID).append ("\" \"").append (TOOLBAR_DTD_WEB).append ("\">\n\n").append (wtc.toString()); // NOI18N

        final FileObject tbFO = NbPlaces.getDefault().toolbars().getPrimaryFile();
        final FileSystem tbFS = tbFO.getFileSystem();

        Boolean prev = WRITE_IN_PROGRESS.get ();
        try {
            WRITE_IN_PROGRESS.set (Boolean.TRUE);
            tbFS.runAtomicAction (new FileSystem.AtomicAction () {
		public void run () throws IOException {
		    FileLock lock = null;
		    OutputStream os = null;
		    FileObject xmlFO = tbFO.getFileObject(cn, EXT_XML);
		    if (xmlFO == null)
			xmlFO = tbFO.createData (cn, EXT_XML);
		    try {
			lock = xmlFO.lock ();
			os = xmlFO.getOutputStream (lock);
			
			Writer writer = new OutputStreamWriter(os, "UTF-8"); // NOI18N
			writer.write(sb.toString());
			writer.close();
		    } finally {
                        lastReload = System.currentTimeMillis ();
                        ERR.fine("Setting last reload: " + lastReload); // NOI18N
                        
			if (os != null)
			    os.close ();
			if (lock != null)
			    lock.releaseLock ();
		    }
		}
	    });
        } finally {
            WRITE_IN_PROGRESS.set (prev);
        }
        ERR.fine("writeDocument finished"); // NOI18N
    }

    /** lazy init of toolbar panel */
    private static final synchronized JPanel toolbarPanel () {
        if (toolbarPanel == null) {
            toolbarPanel = new JPanel();
            toolbarPanel.setLayout(new FlowLayout (FlowLayout.LEFT));
        }
        return toolbarPanel;
    }

    /** Listens on changes in the document.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        if (!XMLDataObject.PROP_DOCUMENT.equals(ev.getPropertyName ())) {
            // interested only in PROP_DOCUMENT properties
            return;
        }
        if (Boolean.TRUE.equals (WRITE_IN_PROGRESS.get ())) {
            return;
        }
        
        updateConfiguration((XMLDataObject)ev.getSource());
    }

    /** Updates configuration and also 'configuration over'.
     * @see #readConfig 
     * @see #checkConfigurationOver */
    void updateConfiguration(final XMLDataObject xmlDataObject) {
        long mod = xmlDataObject.getPrimaryFile ().lastModified().getTime ();
        ERR.fine("Checking modified: " + lastReload); // NOI18N
        //Bugfix #10196, this condition commented to make sure that all changes
        //will be applied.
        /*if (lastReload >= mod) {
            // not changed since last refresh
            return;
        }*/
        
        // [dafe] - code below demonstrates data integrity problem that occurs
        // in current toolbar impl. data in toolbar pool and data in toolbar
        // rows are not maintained centrally, and because of this invoke later,
        // they are inconsistent for a while (toolbar rows have older data, while
        // toolbar pool has already new content)
        // needs architecture redesign IMO
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    initInstance();
                    readConfig(xmlDataObject);
                    checkConfigurationOver();
                    if (configName.equals(toolbarPool().getConfiguration())) {
                        ERR.fine("Activating the configuration");
                        // 1st argument is true because the change is important
                        // 2nd argument is false, because it should prevent the system
                        // to write anything do
                        activate(true, false);
                    }
                }
                catch (IOException ex) {
                    Logger.getLogger(ToolbarConfiguration.class.getName()).log(Level.WARNING, null, ex);
                }
            }
        });
    }    
    
    /** @return upper vertical location of specified row
     */
    int getRowVertLocation (ToolbarRow row) {
        int index = rowIndex(row);
        int vertLocation = index * ToolbarLayout.VGAP;
        Iterator iter = toolbarRows.iterator();
        for (int i = 0; i < index; i++) {
            vertLocation += ((ToolbarRow)iter.next()).getPreferredHeight();
        }
        return vertLocation;
    }

    // class WritableToolbarConfiguration
    static class WritableToolbarConfiguration {
	/** List of rows. */
        Vector<ToolbarRow.WritableToolbarRow> rows;

	/** Create new WritableToolbarConfiguration.
	 * @param rs list of rows
	 * @param iv map of invisible toolbars
	 */
        public WritableToolbarConfiguration (Vector<ToolbarRow> rs, Map iv) {
            initRows (rs);
            initInvisible (iv);
            removeEmptyRows();
        }

        /** Init list of writable rows.
	 * @param rs list of rows
	 */
        void initRows (Vector<ToolbarRow> rs) {
            rows = new Vector<ToolbarRow.WritableToolbarRow>();
            for (ToolbarRow r: rs) {
                rows.addElement (new ToolbarRow.WritableToolbarRow (r));
            }
        }

	/** Init invisible toolbars in toolbar rows.
	 * @param iv map of invisible toolbars
	 */
        void initInvisible (Map iv) {
            Iterator it = iv.keySet().iterator();
            ToolbarConstraints tc;
            int row;
            while (it.hasNext()) {
                tc = (ToolbarConstraints)it.next();
                row = ((Integer)iv.get (tc)).intValue();
                for (int i = row; i < row + tc.getRowCount(); i++) {
                    getRow (i).addToolbar (tc);
                }
            }
        }

	/** Removes empty rows. */
        void removeEmptyRows () {
            ToolbarRow.WritableToolbarRow row;
            for (int i = rows.size() - 1; i >= 0; i--) {
                row = rows.elementAt(i);
                if (row.isEmpty())
                    rows.removeElement (row);
            }
        }

	/**
	 * @param r row index
	 * @return WritableToolbarRow for specified row index
	 */
        ToolbarRow.WritableToolbarRow getRow (int r) {
            try {
                return rows.elementAt (r);
            } catch (ArrayIndexOutOfBoundsException e) {
                rows.addElement (new ToolbarRow.WritableToolbarRow ());
                return getRow (r);
            }
        }

        /** @return ToolbarConfiguration in xml format. */
        @Override
        public String toString () {
            StringBuffer sb = new StringBuffer();

            sb.append ("<").append (TAG_CONFIG).append (">\n"); // NOI18N
            Iterator it = rows.iterator();
            while (it.hasNext()) {
                sb.append (it.next().toString());
            }
            sb.append ("</").append (TAG_CONFIG).append (">\n"); // NOI18N

            return sb.toString();
        }
    } // end of class WritableToolbarConfiguration
} // end of class Configuration
