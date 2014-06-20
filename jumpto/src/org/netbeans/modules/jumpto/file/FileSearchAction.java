/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
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
 *
 * Contributor(s): Andrei Badea
 *                 Petr Hrebejk
 *                 markiewb@netbeans.org
 */

package org.netbeans.modules.jumpto.file;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.JumpList;
import org.netbeans.modules.jumpto.EntitiesListCellRenderer;
import org.netbeans.modules.jumpto.common.Factory;
import org.netbeans.modules.jumpto.common.HighlightingNameFormatter;
import org.netbeans.modules.jumpto.common.Models;
import org.netbeans.modules.jumpto.common.Utils;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlRenderer;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
/**
 *
 * @author Andrei Badea, Petr Hrebejk, Tomas Zezula
 */
public class FileSearchAction extends AbstractAction implements FileSearchPanel.ContentProvider {

    /* package */ static final Logger LOGGER = Logger.getLogger(FileSearchAction.class.getName());
    private static final char LINE_NUMBER_SEPARATOR = ':';    //NOI18N
    private static final Pattern PATTERN_WITH_LINE_NUMBER = Pattern.compile("(.*)"+LINE_NUMBER_SEPARATOR+"(\\d*)");    //NOI18N

    private static final ListModel EMPTY_LIST_MODEL = new DefaultListModel();
    //Threading: Throughput 1 required due to inherent sequential code in Work.Request.exclude
    private static final RequestProcessor rp = new RequestProcessor ("FileSearchAction-RequestProcessor",1);
    //@GuardedBy("this")
    private Worker[] running;
    //@GuardedBy("this")
    private RequestProcessor.Task[] scheduledTasks;
    private Dialog dialog;
    private JButton openBtn;
    private FileSearchPanel panel;
    private Dimension initialDimension;

    public FileSearchAction() {
        super( NbBundle.getMessage(FileSearchAction.class, "CTL_FileSearchAction") );
        // XXX this should be in initialize()?
        putValue("PopupMenuText", NbBundle.getBundle(FileSearchAction.class).getString("editor-popup-CTL_FileSearchAction")); // NOI18N
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public boolean isEnabled() {
        return OpenProjects.getDefault().getOpenProjects().length > 0;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        FileDescriptor[] typeDescriptors = getSelectedFiles();
        if (typeDescriptors != null) {
            JumpList.checkAddEntry();
            for(FileDescriptor td: typeDescriptors){
                td.open();
            }
        }
    }

    // Implementation of content provider --------------------------------------


    @Override
    public ListCellRenderer getListCellRenderer(
            @NonNull final JList list,
            @NonNull final Document nameDocument,
            @NonNull final ButtonModel caseSensitive) {
        Parameters.notNull("list", list);   //NOI18N
        Parameters.notNull("nameDocument", nameDocument);   //NOI18N
        Parameters.notNull("caseSensitive", caseSensitive); //NOI18N
        return new Renderer(list, nameDocument, caseSensitive);
    }


    @Override
    public void setListModel(final FileSearchPanel panel, String text ) {
        if (openBtn != null) {
            openBtn.setEnabled (false);
        }

        cancel();

        if ( text == null ) {
            panel.setModel(EMPTY_LIST_MODEL, true);
            return;
        }
        boolean exact = text.endsWith(" "); // NOI18N
        text = text.trim();
        if ( text.length() == 0) {
            panel.setModel(EMPTY_LIST_MODEL, true);
            return;
        }

        int wildcard = Utils.containsWildCard(text);
        QuerySupport.Kind nameKind;

        if (exact) {
            //nameKind = panel.isCaseSensitive() ? QuerySupport.Kind.EXACT : QuerySupport.Kind.CASE_INSENSITIVE_EXACT;
            nameKind = QuerySupport.Kind.EXACT;
        }
        else if (wildcard != -1) {
            nameKind = panel.isCaseSensitive() ? QuerySupport.Kind.REGEXP : QuerySupport.Kind.CASE_INSENSITIVE_REGEXP;
            text = Utils.removeNonNeededWildCards(text);
        }
        else if ((Utils.isAllUpper(text) && text.length() > 1) || Utils.isCamelCase(text)) {
            nameKind = QuerySupport.Kind.CAMEL_CASE;
        }
        else {
            nameKind = panel.isCaseSensitive() ? QuerySupport.Kind.PREFIX : QuerySupport.Kind.CASE_INSENSITIVE_PREFIX;
        }

        //Extract linenumber from search text
        //Pattern is like 'My*Object.java:123'
        final Matcher matcher = PATTERN_WITH_LINE_NUMBER.matcher(text);
        int lineNr;
        if (matcher.matches()) {
            text = matcher.group(1);
            try {
                lineNr = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException numberFormatException) {
                //prevent non convertable numbers
                lineNr=-1;
            }
        } else {
            lineNr = -1;
        }

        // Compute in other thread
        synchronized(this) {
            final Models.MutableListModel baseListModel = Models.mutable(
                    new FileComarator(
                        panel.isPreferedProject(),
                        panel.isCaseSensitive()));
            panel.setModel(Models.refreshable(
                    baseListModel,
                    new Factory<FileDescriptor, Pair<FileDescriptor,Runnable>>() {
                        @Override
                        public FileDescriptor create(@NonNull final Pair<FileDescriptor,Runnable> param) {
                            return new AsyncFileDescriptor(param.first(), param.second());
                        }
                    }),
                    false);
            final Worker.Request request = Worker.newRequest(
                text,
                nameKind,
                panel.getCurrentProject(),
                lineNr);
            final Worker.Collector collector = Worker.newCollector(
                baseListModel,
                new Runnable(){
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                panel.searchProgress();
                                if (openBtn != null && baseListModel.getSize() > 0) {
                                    openBtn.setEnabled (true);
                                }
                            }
                        });
                    }
                },
                new Runnable(){
                    @Override
                    public void run() {
                        panel.searchCompleted();
                    }
                },
                panel.time);
            final Worker.Type[] wts = Worker.Type.values();
            final Worker[] workers = new Worker[wts.length];
            //Threading: All workers need to be created before they are scheduled
            for (int i = 0; i < wts.length; i++) {
                workers[i] = Worker.newWorker(request, collector, wts[i]);
            }
            running = workers;
            final RequestProcessor.Task[] tasks = new RequestProcessor.Task[workers.length];
            for (int i = 0; i < workers.length; i++) {
                tasks[i] = rp.post(workers[i], 220);
            }
            scheduledTasks = tasks;
            if ( panel.time != -1 ) {
                LOGGER.log(
                    Level.FINE,
                    "Worker posted after {0} ms.",  //NOI18N
                    System.currentTimeMillis() - panel.time );
            }
        }
    }

    @Override
    public void closeDialog() {
        dialog.setVisible( false );
        cleanup();
    }

    @Override
    public boolean hasValidContent () {
        return this.openBtn != null && this.openBtn.isEnabled();
    }

    // Private methods ---------------------------------------------------------

    private FileDescriptor[] getSelectedFiles() {
        FileDescriptor[] result = null;
        panel = new FileSearchPanel(this, findCurrentProject());
        dialog = createDialog(panel);

        Node[] arr = TopComponent.getRegistry ().getActivatedNodes();
        if (arr.length > 0) {
            EditorCookie ec = arr[0].getCookie (EditorCookie.class);
            if (ec != null) {
                JEditorPane recentPane = NbDocument.findRecentEditorPane(ec);
                if (recentPane != null) {
                    String initSearchText = null;
                    if (org.netbeans.editor.Utilities.isSelectionShowing(recentPane.getCaret())) {
                        initSearchText = recentPane.getSelectedText();
                    }
                    if (initSearchText != null) {
                        if (initSearchText.length() > 256) {
                            initSearchText = initSearchText.substring(0, 256);
                        }
                        panel.setInitialText(initSearchText);
                    } else {
                        FileObject fo = arr[0].getLookup().lookup(FileObject.class);
                        if (fo != null) {
                            panel.setInitialText(fo.getNameExt());
                        }
                    }
                }
            }
        }

        dialog.setVisible(true);
        result = panel.getSelectedFiles();
        return result;
    }

   private Dialog createDialog( final FileSearchPanel panel) {
        openBtn = new JButton();
        Mnemonics.setLocalizedText(openBtn, NbBundle.getMessage(FileSearchAction.class, "CTL_Open"));
        openBtn.getAccessibleContext().setAccessibleDescription(openBtn.getText());
        openBtn.setEnabled( false );
        
        final Object[] buttons = new Object[] { openBtn, DialogDescriptor.CANCEL_OPTION };
        
        String title = NbBundle.getMessage(FileSearchAction.class, "MSG_FileSearchDlgTitle");
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                panel,
                title,
                true,
                buttons,
                openBtn,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP, 
                new DialogButtonListener(panel));
        dialogDescriptor.setClosingOptions(buttons);

        Dialog d = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        d.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FileSearchAction.class, "AN_FileSearchDialog"));
        d.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSearchAction.class, "AD_FileSearchDialog"));
                
        // Set size
        d.setPreferredSize( new Dimension(  FileSearchOptions.getWidth(),
                                                 FileSearchOptions.getHeight() ) );
        
        // Center the dialog after the size changed.
        Rectangle r = Utilities.getUsableScreenBounds();
        int maxW = (r.width * 9) / 10;
        int maxH = (r.height * 9) / 10;
        Dimension dim = d.getPreferredSize();
        dim.width = Math.min(dim.width, maxW);
        dim.height = Math.min(dim.height, maxH);
        initialDimension = dim;
        d.setBounds(Utilities.findCenterBounds(dim));
        d.addWindowListener(new WindowAdapter() {
            public @Override void windowClosed(WindowEvent e) {
                cleanup();
            }
        });

        return d;
    }
    
    /** For original of this code look at:
     *  org.netbeans.modules.project.ui.actions.ActionsUtil
     */
    private static Project findCurrentProject( ) {
        Lookup lookup = Utilities.actionsGlobalContext();

        // Maybe the project is in the lookup
        for (Project p : lookup.lookupAll(Project.class)) {
            return p;
        }
        // Now try to guess the project from dataobjects
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null ) {
                return p;
            }
        }

       return OpenProjects.getDefault().getMainProject();
    }

    private void cleanup() {
        cancel();
        if ( dialog != null ) { // Closing event for some reson sent twice
            // Save dialog size only when changed
            final int currentWidth = dialog.getWidth();
            final int currentHeight = dialog.getHeight();
            if (initialDimension != null && (initialDimension.width != currentWidth || initialDimension.height != currentHeight)) {
                FileSearchOptions.setHeight(currentHeight);
                FileSearchOptions.setWidth(currentWidth);
            }
            initialDimension = null;
            // Clean caches
            dialog.dispose();
            this.dialog = null;
            FileSearchOptions.flush();
        }
    }

    private void cancel() {
        synchronized (this) {
            if ( running != null ) {
                for (Worker w : running) {
                    w.cancel();
                }
                for (RequestProcessor.Task t : scheduledTasks) {
                    t.cancel();
                }
                running = null;
                scheduledTasks = null;
            }
        }
    }

    // Private classes ---------------------------------------------------------
    private class DialogButtonListener implements ActionListener {
        
        private FileSearchPanel panel;
        
        public DialogButtonListener(FileSearchPanel panel) {
            this.panel = panel;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {       
            if ( e.getSource() == openBtn) {
                panel.setSelectedFile();
            }
        }
    }

    //Inner classes
    private static class RendererComponent extends JPanel {
	private FileDescriptor fd;

	void setDescription(FileDescriptor fd) {
	    this.fd = fd;
	    putClientProperty(TOOL_TIP_TEXT_KEY, null);
	}

	@Override
	public String getToolTipText() {
	    String text = (String) getClientProperty(TOOL_TIP_TEXT_KEY);
	    if( text == null ) {
                if( fd != null) {
                    text = fd.getFileDisplayPath();
                }
                putClientProperty(TOOL_TIP_TEXT_KEY, text);
	    }
	    return text;
	}
    }

    private static final class AsyncFileDescriptor extends FileDescriptor implements Runnable {

        @StaticResource
        private static final String UNKNOWN_ICON_PATH = "org/netbeans/modules/jumpto/resources/unknown.gif";    //NOI18N
        private static final Icon UNKNOWN_ICON = ImageUtilities.loadImageIcon(UNKNOWN_ICON_PATH, false);
        private static final RequestProcessor LOAD_ICON_RP = new RequestProcessor(AsyncFileDescriptor.class.getName(), 1, false, false);

        private final FileDescriptor delegate;
        private final Runnable refreshCallback;
        private volatile Icon icon;

        AsyncFileDescriptor(
            @NonNull final FileDescriptor delegate,
            @NonNull final Runnable refreshCallback) {
            Parameters.notNull("delegate", delegate);   //NOI18N
            Parameters.notNull("refreshCallback", refreshCallback); //NOI18N
            this.delegate = delegate;
            this.refreshCallback = refreshCallback;
            FileProviderAccessor.getInstance().setFromCurrentProject(
                this,
                FileProviderAccessor.getInstance().isFromCurrentProject(delegate));
        }

        @Override
        public String getFileName() {
           return delegate.getFileName();
        }

        @Override
        public String getOwnerPath() {
            return delegate.getOwnerPath();
        }

        @Override
        public Icon getIcon() {
            if (icon != null) {
                return icon;
            }
            LOAD_ICON_RP.execute(this);
            return UNKNOWN_ICON;
        }

        @Override
        public String getProjectName() {
            return delegate.getProjectName();
        }

        @Override
        public Icon getProjectIcon() {
            return delegate.getProjectIcon();
        }

        @Override
        public void open() {
            delegate.open();
        }

        @Override
        public FileObject getFileObject() {
            final FileObject res = delegate.getFileObject();
            if (res == null) {
                LOGGER.log(
                    Level.FINE,
                    "FileDescriptor: {0} : {1} returned null from getFile", //NOI18N
                    new Object[]{
                        delegate,
                        delegate.getClass()
                    });
            }
            return res;
        }

        @Override
        public String getFileDisplayPath() {
            return delegate.getFileDisplayPath();
        }

        @Override
        public void run() {
            icon = delegate.getIcon();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    refreshCallback.run();
                }
            });
        }

    }

    public static class Renderer extends EntitiesListCellRenderer implements ActionListener, DocumentListener {

        public  static Icon WAIT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/jumpto/resources/wait.gif", false); // NOI18N

        private final HighlightingNameFormatter fileNameFormatter;

        private RendererComponent rendererComponent;
        private JLabel jlName = HtmlRenderer.createLabel();
        private JLabel jlPath = new JLabel();
        private JLabel jlPrj = new JLabel();
        private int DARKER_COLOR_COMPONENT = 5;
        private int LIGHTER_COLOR_COMPONENT = 80;
        private Color fgColor;
        private Color fgColorLighter;
        private Color bgColor;
        private Color bgColorDarker;
        private Color bgSelectionColor;
        private Color fgSelectionColor;
        private Color bgColorGreener;
        private Color bgColorDarkerGreener;

        private String textToFind = "";   //NOI18N
        private boolean caseSensitive;
        private JList jList;

        private boolean colorPrefered;

        public Renderer(
                @NonNull final JList list,
                @NonNull final Document nameDocument,
                @NonNull final ButtonModel caseSensitive) {
            jList = list;
            this.caseSensitive = caseSensitive.isSelected();
            resetName();
            Container container = list.getParent();
            if ( container instanceof JViewport ) {
                ((JViewport)container).addChangeListener(this);
                stateChanged(new ChangeEvent(container));
            }

            rendererComponent = new RendererComponent();
            rendererComponent.setLayout(new BorderLayout());
            rendererComponent.add( jlName, BorderLayout.WEST );
            rendererComponent.add( jlPath, BorderLayout.CENTER);
            rendererComponent.add( jlPrj, BorderLayout.EAST );


            jlPath.setOpaque(false);
            jlPrj.setOpaque(false);
            jlPath.setFont(list.getFont());
            jlPrj.setFont(list.getFont());


            jlPrj.setHorizontalAlignment(RIGHT);
            jlPrj.setHorizontalTextPosition(LEFT);

            // setFont( list.getFont() );
            fgColor = list.getForeground();
            fgColorLighter = new Color(
                                   Math.min( 255, fgColor.getRed() + LIGHTER_COLOR_COMPONENT),
                                   Math.min( 255, fgColor.getGreen() + LIGHTER_COLOR_COMPONENT),
                                   Math.min( 255, fgColor.getBlue() + LIGHTER_COLOR_COMPONENT)
                                  );

            bgColor = new Color( list.getBackground().getRGB() );
            bgColorDarker = new Color(
                                    Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT)
                            );
            bgSelectionColor = list.getSelectionBackground();
            fgSelectionColor = list.getSelectionForeground();


            bgColorGreener = new Color(
                                    Math.abs(bgColor.getRed() - 20),
                                    Math.min(255, bgColor.getGreen() + 10 ),
                                    Math.abs(bgColor.getBlue() - 20) );


            bgColorDarkerGreener = new Color(
                                    Math.abs(bgColorDarker.getRed() - 35),
                                    Math.min(255, bgColorDarker.getGreen() + 5 ),
                                    Math.abs(bgColorDarker.getBlue() - 35) );
            fileNameFormatter = HighlightingNameFormatter.createBoldFormatter();
            nameDocument.addDocumentListener(this);
            caseSensitive.addActionListener(this);
            jlName.setOpaque(true);
        }

        public @Override Component getListCellRendererComponent( JList list,
                                                       Object value,
                                                       int index,
                                                       boolean isSelected,
                                                       boolean hasFocus) {

            // System.out.println("Renderer for index " + index );

            int height = list.getFixedCellHeight();
            int width = list.getFixedCellWidth() - 1;

            width = width < 200 ? 200 : width;

            // System.out.println("w, h " + width + ", " + height );

            Dimension size = new Dimension( width, height );
            rendererComponent.setMaximumSize(size);
            rendererComponent.setPreferredSize(size);
            resetName();
            if ( isSelected ) {
                jlName.setForeground(fgSelectionColor);
                jlName.setBackground( bgSelectionColor );
                jlPath.setForeground(fgSelectionColor);
                jlPrj.setForeground(fgSelectionColor);
                rendererComponent.setBackground(bgSelectionColor);
            }
            else {
                jlName.setForeground(fgColor);
                jlName.setBackground(bgColor);
                jlPath.setForeground(fgColorLighter);
                jlPrj.setForeground(fgColor);
                rendererComponent.setBackground( index % 2 == 0 ? bgColor : bgColorDarker );
            }

            if ( value instanceof FileDescriptor ) {
                FileDescriptor fd = (FileDescriptor)value;
                jlName.setIcon(fd.getIcon());
                final String formattedFileName = fileNameFormatter.formatName(
                    fd.getFileName(),
                    textToFind,
                    caseSensitive,
                    isSelected? fgSelectionColor : fgColor);
                jlName.setText(formattedFileName);
                jlPath.setIcon(null);
                jlPath.setHorizontalAlignment(SwingConstants.LEFT);
                jlPath.setText(fd.getOwnerPath().length() > 0 ? " (" + fd.getOwnerPath() + ")" : " ()"); //NOI18N
                setProjectName(jlPrj, fd.getProjectName());
                jlPrj.setIcon(fd.getProjectIcon());
                if (!isSelected) {
                    final boolean cprj = FileProviderAccessor.getInstance().isFromCurrentProject(fd) && colorPrefered;
                    final Color bgc =  index % 2 == 0 ?
                        (cprj ? bgColorGreener : bgColor ) :
                        (cprj ? bgColorDarkerGreener : bgColorDarker );
                    jlName.setBackground(bgc);  //Html does not support transparent bg
                    rendererComponent.setBackground(bgc);
                }
                rendererComponent.setDescription(fd);
            }
            else {
                jlName.setText( "" ); // NOI18M
                jlName.setIcon(null);
                jlPath.setIcon(Renderer.WAIT_ICON);
                jlPath.setHorizontalAlignment(SwingConstants.CENTER);
                jlPath.setText( value.toString() );
                jlPrj.setIcon(null);
                jlPrj.setText( "" ); // NOI18N
            }

            return rendererComponent;
        }

        @Override
        public void stateChanged(ChangeEvent event) {

            JViewport jv = (JViewport)event.getSource();

            jlName.setText( "Sample" ); // NOI18N
            jlName.setIcon( new ImageIcon() );

            jList.setFixedCellHeight(jlName.getPreferredSize().height);
            jList.setFixedCellWidth(jv.getExtentSize().width);
        }

        public void setColorPrefered( boolean colorPrefered ) {
            this.colorPrefered = colorPrefered;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            caseSensitive = ((ButtonModel)e.getSource()).isSelected();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            try {
                textToFind = e.getDocument().getText(0, e.getDocument().getLength());
            } catch (BadLocationException ex) {
                textToFind = "";    //NOI18N
            }
        }

        private void resetName() {
            ((HtmlRenderer.Renderer)jlName).reset();
            jlName.setFont(jList.getFont());
            jlName.setOpaque(true);
            ((HtmlRenderer.Renderer)jlName).setHtml(true);
            ((HtmlRenderer.Renderer)jlName).setRenderStyle(HtmlRenderer.STYLE_TRUNCATE);
        }

     }
}
