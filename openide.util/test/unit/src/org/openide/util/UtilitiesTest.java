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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.openide.util.AWTBridge;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 * @author Jiri Rechtacek et al.
 */
public class UtilitiesTest extends TestCase {

    public UtilitiesTest (String testName) {
        super (testName);
    }
    
    private String originalOsName;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Utilities.resetOperatingSystem ();
        originalOsName = System.getProperty("os.name");
    }
    
    @Override
    protected void tearDown() throws Exception {
        System.setProperty("os.name", originalOsName);
        super.tearDown();
    }

    public void testGetOperatingSystemWinNT () {
        System.setProperty ("os.name", "Windows NT");
        //assertEquals ("System.getProperty (os.name) returns Windows NT", "Windows NT", System.getProperty ("os.name"));
        assertEquals ("Windows NT recognized as OS_WINNT", Utilities.OS_WINNT, Utilities.getOperatingSystem ());
    }

    public void testGetOperatingSystemFreebsd () {
        System.setProperty ("os.name", "FreeBSD");
        assertEquals ("System.getProperty (os.name) returns FreeBSD", "FreeBSD", System.getProperty ("os.name"));
        assertEquals ("System.getProperty (os.name) returns freebsd", "freebsd", System.getProperty ("os.name").toLowerCase (Locale.US));
        assertEquals ("FreeBSD recognized as OS_FREEBSD", Utilities.OS_FREEBSD, Utilities.getOperatingSystem ());
    }

    public void testGetOperatingSystemFreeBSDLowerCase () {
        System.setProperty ("os.name", "freebsd");
        assertEquals ("FreeBSD recognized as OS_FREEBSD", Utilities.OS_FREEBSD, Utilities.getOperatingSystem ());
    }

    public void testGetUnknownOperatingSystem () {
        System.setProperty ("os.name", "Unknown");
        if (File.pathSeparatorChar == ':') {
            assertTrue("Unknown os.name should be recognized as Unix.", Utilities.isUnix());
        } else {
            assertEquals("Unknown os.name not OS_OTHER.", Utilities.OS_OTHER, Utilities.getOperatingSystem());
        }
    }

    public void testWhatIsWinXP () {
        System.setProperty ("os.name", "Windows XP");
        assertTrue ("Windows XP isWindows", Utilities.isWindows ());
        assertFalse ("Windows XP not isUnix", Utilities.isUnix ());
    }

    public void testWhatIsLinux () {
        System.setProperty ("os.name", "Linux");
        assertFalse ("Linux not isWindows", Utilities.isWindows ());
        assertTrue ("Linux isUnix", Utilities.isUnix ());
    }

    public void testWhatIsMac () {
        System.setProperty ("os.name", "Mac OS X");
        assertFalse ("Mac not isWindows", Utilities.isWindows ());
        assertTrue ("Mac isMac", Utilities.isMac ());
    }

    public void testWhatIsFreeBSD () {
        System.setProperty ("os.name", "freebsd");
        assertFalse ("freebsd is not isWindows", Utilities.isWindows ());
        assertTrue ("freebsd isUnix", Utilities.isUnix ());
    }

    // XXX sorry, but NoCustomCursorToolkit does not compile on JDK 6:
    // org.openide.util.UtilitiesTest.NoCustomCursorToolkit is not abstract and does not override abstract method isModalExclusionTypeSupported(java.awt.Dialog.ModalExclusionType) in java.awt.Toolkit
    // and since Toolkit is not an interface, we can't use java.lang.reflect.Proxy to solve the problem...
    // Filed as #6313637.
    /*
    public void testCustomCursorNotSupported() {
        NoCustomCursorToolkit toolkit = new NoCustomCursorToolkit();
        CustomToolkitComponent c = new CustomToolkitComponent( toolkit );
        Image icon = new BufferedImage( 16, 16, BufferedImage.TYPE_BYTE_BINARY );
        Cursor cursor = Utilities.createCustomCursor( c, icon, "junittest" );
        assertTrue( "fallback to wait cursor", Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ).equals( cursor ) );
        assertTrue( "getBestCursorSize was called", toolkit.getBestCursorSizeCalled );
        assertFalse( "no custom cursor created", toolkit.createCustomCursorCalled );
    }
     */

    public void testKeyConversions() throws Exception {
        assertEquals("CS-F1", Utilities.keyToString(KeyStroke.getKeyStroke(KeyEvent.VK_F1, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK)));
        assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.ALT_MASK), Utilities.stringToKey("A-EQUALS"));
        // XXX stringToKeys, Mac support, various more exotic conditions...
    }

    public void testSpecialKeyworksOn14AsWell15 () throws Exception {
        KeyStroke ks = Utilities.stringToKey("C-CONTEXT_MENU");
        assertNotNull ("key stroke created", ks);
        KeyStroke alt = KeyStroke.getKeyStroke(ks.getKeyCode(), KeyEvent.ALT_MASK);
        String s = Utilities.keyToString(alt);
        assertEquals ("Correctly converted", "A-CONTEXT_MENU", s);    
    }
    
    public void testSpecialKeyworksOn14AsWell15WithoutModificators () throws Exception {
        KeyStroke ks = Utilities.stringToKey("CONTEXT_MENU");
        assertNotNull ("key stroke created", ks);
        String s = Utilities.keyToString(ks);
        assertEquals ("Correctly converted", "CONTEXT_MENU", s);
    }
    
    public void testActionsToPopupWithLookup() throws Exception {
        MockServices.setServices(AwtBridgeImpl.class);
        final List<String> commands = new ArrayList<String>();
        class BasicAction extends AbstractAction {
            public BasicAction(String name) {
                super(name);
            }
            public void actionPerformed(ActionEvent e) {
                commands.add((String) getValue(Action.NAME));
            }
        }
        class ContextAction extends BasicAction implements ContextAwareAction {
            public ContextAction(String name) {
                super(name);
            }
            public Action createContextAwareInstance(final Lookup actionContext) {
                return new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        commands.add(ContextAction.this.getValue(Action.NAME) + "/" + actionContext.lookup(String.class));
                    }
                };
            }
        }
        class SpecialMenuAction extends BasicAction implements Presenter.Popup {
            public SpecialMenuAction(String name) {
                super(name);
            }
            public JMenuItem getPopupPresenter() {
                JMenuItem item = new JMenuItem((String) getValue(Action.NAME));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        commands.add(((String) getValue(Action.NAME)) + "/popup");
                    }
                });
                return item;
            }
        }
        Action duplicated = new BasicAction("duplicated");
        Action[] actions = new Action[] {
            null,
            null,
            new BasicAction("first"),
            duplicated,
            null,
            null,
            new BasicAction("second"),
            duplicated,
            null,
            new ContextAction("context"),
            new SpecialMenuAction("presenter"),
            null,
            new BasicAction("top"),
            new BasicAction("HIDDEN"),
            null,
            new BasicAction("bottom"),
            null,
            null,
        };
        Lookup l = Lookups.singleton("thing");
        JPopupMenu menu = Utilities.actionsToPopup(actions, l);
        for (Component element : menu.getComponents()) { // including separators
            if (element instanceof AbstractButton) {
                ((AbstractButton) element).doClick();
            } else {
                commands.add(null);
            }
        }
        String[] expectedCommands = new String[] {
            // leading separators must be stripped
            "first",
            "duplicated",
            null, // adjacent separators must be collapsed
            "second",
            // do not add the same action twice
            null,
            "context/thing", // ContextAwareAction was checked for
            "presenter/popup", // Presenter.Popup was checked for
            null,
            "top",
            // exclude HIDDEN because of AwtBridgeImpl.convertComponents
            // separator should however remain
            null,
            "bottom",
            // trailing separators must be stripped
        };
        assertEquals("correct generated menu", Arrays.asList(expectedCommands), commands);
    }

    /*
    private static class CustomToolkitComponent extends Component {
        private Toolkit customToolkit;
        
        public CustomToolkitComponent( Toolkit t ) {
            this.customToolkit = t;
        }
        
        public Toolkit getToolkit() {
            return customToolkit;
        }
    }
    
    private static class NoCustomCursorToolkit extends Toolkit {
        public FontMetrics getFontMetrics(Font font) {
            return Toolkit.getDefaultToolkit().getFontMetrics( font );
        }

        protected TextFieldPeer createTextField(TextField target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        protected ListPeer createList(java.awt.List target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        protected MenuBarPeer createMenuBar(MenuBar target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException {
            return Toolkit.getDefaultToolkit().createDragSourceContextPeer( dge );
        }

        public boolean prepareImage(Image image, int width, int height, ImageObserver observer) {
            return Toolkit.getDefaultToolkit().prepareImage( image, width, height, observer );
        }

        public int checkImage(Image image, int width, int height, ImageObserver observer) {
            return Toolkit.getDefaultToolkit().checkImage( image, width, height, observer );
        }

        protected PopupMenuPeer createPopupMenu(PopupMenu target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        public PrintJob getPrintJob(Frame frame, String jobtitle, Properties props) {
            return Toolkit.getDefaultToolkit().getPrintJob( frame, jobtitle, props );
        }

        protected ButtonPeer createButton(Button target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        public Image createImage(ImageProducer producer) {
            return Toolkit.getDefaultToolkit().createImage( producer );
        }

        protected CanvasPeer createCanvas(Canvas target) {
            throw new IllegalStateException("Method not implemented");
        }

        protected ScrollbarPeer createScrollbar(Scrollbar target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        public Image getImage(String filename) {
            return Toolkit.getDefaultToolkit().getImage( filename );
        }

        public Image createImage(String filename) {
            return Toolkit.getDefaultToolkit().createImage( filename );
        }

        protected MenuPeer createMenu(Menu target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        protected MenuItemPeer createMenuItem(MenuItem target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        public Map mapInputMethodHighlight(InputMethodHighlight highlight) throws HeadlessException {
            return Toolkit.getDefaultToolkit().mapInputMethodHighlight( highlight );
        }

        public Image createImage(byte[] imagedata, int imageoffset, int imagelength) {
            return Toolkit.getDefaultToolkit().createImage( imagedata, imageoffset, imagelength );
        }

        public Image getImage(URL url) {
            return Toolkit.getDefaultToolkit().getImage( url );
        }

        protected CheckboxPeer createCheckbox(Checkbox target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        public Image createImage(URL url) {
            return Toolkit.getDefaultToolkit().createImage( url );
        }

        protected TextAreaPeer createTextArea(TextArea target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        protected FileDialogPeer createFileDialog(FileDialog target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        protected ScrollPanePeer createScrollPane(ScrollPane target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        protected DialogPeer createDialog(Dialog target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        protected PanelPeer createPanel(Panel target) {
            throw new IllegalStateException("Method not implemented");
        }

        protected ChoicePeer createChoice(Choice target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        protected FramePeer createFrame(Frame target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        protected LabelPeer createLabel(Label target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        protected FontPeer getFontPeer(String name, int style) {
            throw new IllegalStateException("Method not implemented");
        }

        protected CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        protected WindowPeer createWindow(Window target) throws HeadlessException {
            throw new IllegalStateException("Method not implemented");
        }

        public void sync() {
            Toolkit.getDefaultToolkit().sync();
        }

        protected EventQueue getSystemEventQueueImpl() {
            return Toolkit.getDefaultToolkit().getSystemEventQueue();
        }

        public Clipboard getSystemClipboard() throws HeadlessException {
            return Toolkit.getDefaultToolkit().getSystemClipboard();
        }

        public Dimension getScreenSize() throws HeadlessException {
            return Toolkit.getDefaultToolkit().getScreenSize();
        }

        public int getScreenResolution() throws HeadlessException {
            return Toolkit.getDefaultToolkit().getScreenResolution();
        }

        public String[] getFontList() {
            return Toolkit.getDefaultToolkit().getFontList();
        }

        public ColorModel getColorModel() throws HeadlessException {
            return Toolkit.getDefaultToolkit().getColorModel();
        }

        public void beep() {
            Toolkit.getDefaultToolkit().beep();
        }

        boolean createCustomCursorCalled = false;
        public Cursor createCustomCursor(Image cursor, Point hotSpot, String name) throws IndexOutOfBoundsException, HeadlessException {

            createCustomCursorCalled = true;
            return Toolkit.getDefaultToolkit().createCustomCursor(cursor, hotSpot, name);
        }

        boolean getBestCursorSizeCalled = false;
        public Dimension getBestCursorSize(int preferredWidth, int preferredHeight) throws HeadlessException {
            getBestCursorSizeCalled = true;
            return new Dimension(0,0);
        }
    }
     */

    public static final class AwtBridgeImpl extends AWTBridge {
        public JPopupMenu createEmptyPopup() {
            return new JPopupMenu();
        }
        public JMenuItem createMenuPresenter(Action action) {
            return new JMenuItem(action);
        }
        public JMenuItem createPopupPresenter(Action action) {
            return new JMenuItem(action);
        }
        public Component createToolbarPresenter(Action action) {
            return new JButton(action);
        }
        public Component[] convertComponents(Component comp) {
            if (comp instanceof JMenuItem && "HIDDEN".equals(((JMenuItem) comp).getText())) {
                return new Component[0];
            } else {
                return new Component[] {comp};
            }
        }
    }
    
}
