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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome.content;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class RSSFeed extends JScrollPane implements Constants, PropertyChangeListener {
    
    protected static final int NEWS_COUNT = 10;
    
    private String url;
    
    private boolean showProxyButton = true;

    private RequestProcessor.Task reloadTimer;
    private long lastReload = 0;

    public static final String FEED_CONTENT_PROPERTY = "feedContent";
    
    private static DateFormat parsingDateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH ); // NOI18N
    private static DateFormat parsingDateFormatShort = new SimpleDateFormat( "EEE, dd MMM yyyy", Locale.ENGLISH ); // NOI18N
    private static DateFormat printingDateFormat = DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT );
    private static DateFormat printingDateFormatShort = DateFormat.getDateInstance( DateFormat.SHORT );

    public RSSFeed( String url, boolean showProxyButton ) {
        this.url = url;
        this.showProxyButton = showProxyButton;
        setBorder(null);
        setOpaque(false);

        setBackground( Utils.getColor(DEFAULT_BACKGROUND_COLOR) );
        getViewport().setBackground( Utils.getColor(DEFAULT_BACKGROUND_COLOR) );
        setViewportView( buildContentLoadingLabel() );
        
        setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

        HttpProxySettings.getDefault().addPropertyChangeListener( WeakListeners.propertyChange( this, HttpProxySettings.getDefault() ) );
    }
    
    public RSSFeed( boolean showProxyButton ) {
        this( null, showProxyButton );
    }
    
    public void setContent( Component content ) {
        setViewportView( content );
        setCursor( Cursor.getDefaultCursor() );
        firePropertyChange( FEED_CONTENT_PROPERTY, null, content );
    }

    public Component getContent() {
        return getViewport().getView();
    }

    public void reload() {
        new Reload().start();
    }

    protected ArrayList/*<FeedItem>*/ buildItemList() throws SAXException, ParserConfigurationException, IOException {
        XMLReader reader = XMLUtil.createXMLReader( false, true );
        FeedHandler handler = new FeedHandler();
        reader.setContentHandler( handler );
        reader.setEntityResolver( org.openide.xml.EntityCatalog.getDefault() );
        reader.setErrorHandler( new ErrorCatcher() );

        reader.parse( new InputSource(url) );

        return handler.getItemList();
    }

        /** Inner class error catcher for handling SAXParseExceptions */
    static class ErrorCatcher implements org.xml.sax.ErrorHandler {
        private void message(Level level, org.xml.sax.SAXParseException e) {
            Logger l = Logger.getLogger(RSSFeed.class.getName());
            l.log(level, "Line number:"+e.getLineNumber()); //NOI18N
            l.log(level, "Column number:"+e.getColumnNumber()); //NOI18N
            l.log(level, "Public ID:"+e.getPublicId()); //NOI18N
            l.log(level, "System ID:"+e.getSystemId()); //NOI18N
            l.log(level, "Error message:"+e.getMessage()); //NOI18N
        }
        
        public void error(org.xml.sax.SAXParseException e) {
            message(Level.SEVERE, e); //NOI18N
        }
        
        public void warning(org.xml.sax.SAXParseException e) {
            message(Level.WARNING,e); //NOI18N
        }
        
        public void fatalError(org.xml.sax.SAXParseException e) {
            message(Level.SEVERE,e); //NOI18N
        }
    } //end of inner class ErrorCatcher

    private class Reload extends Thread {
        public void run() {
            try {
                lastReload = System.currentTimeMillis();

                setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
                
                ArrayList itemList = buildItemList();
                final JPanel contentPanel = new NoHorizontalScrollPanel();
                contentPanel.setOpaque( false );
                int contentRow = 0;

                Component header = getContentHeader();
                if( null != header ) {
                    contentPanel.add( header, new GridBagConstraints(0,contentRow++,1,1,0.0,0.0,
                                GridBagConstraints.CENTER,GridBagConstraints.BOTH,
                                new Insets(0,0,0,0),0,0 ) );
                }

                for( int i=0; i<Math.min(itemList.size(), NEWS_COUNT); i++ ) {
                    FeedItem item = (FeedItem)itemList.get(i);

                    if( null != item.title && null != item.link ) {
                        JPanel panel = new JPanel( new GridBagLayout() );
                        panel.setOpaque( false );
                        int row = 0;
                        if( item.dateTime != null) {
                            JLabel label = new JLabel();
                            label.setFont( RSS_DESCRIPTION_FONT );
                            label.setForeground( Utils.getColor(RSS_DATETIME_COLOR) );
                            label.setText( formatDateTime( item.dateTime ) );
                            panel.add( label, new GridBagConstraints(0,row++,1,1,0.0,0.0,
                                    GridBagConstraints.WEST,GridBagConstraints.NONE,
                                    new Insets(0,TEXT_INSETS_LEFT+5,2,TEXT_INSETS_RIGHT),0,0 ) );
                        }

                        WebLink linkButton = new WebLink( item.title, item.link, true );
                        linkButton.getAccessibleContext().setAccessibleName( 
                                BundleSupport.getAccessibilityName( "WebLink", item.title ) ); //NOI18N
                        linkButton.getAccessibleContext().setAccessibleDescription( 
                                BundleSupport.getAccessibilityDescription( "WebLink", item.link ) ); //NOI18N
                        linkButton.setFont( HEADER_FONT );
                        panel.add( linkButton, new GridBagConstraints(0,row++,1,1,1.0,1.0,
                                GridBagConstraints.WEST,GridBagConstraints.NONE,
                                new Insets(0,5,2,TEXT_INSETS_RIGHT),0,0 ) );


                        if (item.description != null) {
                            JLabel label = new JLabel();
                            label.setFont( RSS_DESCRIPTION_FONT );
                            label.setText( "<html>"+trimHtml( item.description )  ); // NOI18N
                            panel.add( label, new GridBagConstraints(0,row++,1,1,1.0,1.0,
                                    GridBagConstraints.WEST,GridBagConstraints.BOTH,
                                    new Insets(0,TEXT_INSETS_LEFT+5,0,TEXT_INSETS_RIGHT),0,0 ) );
                        }

                        contentPanel.add( panel, new GridBagConstraints(0,contentRow++,1,1,1.0,1.0,
                                GridBagConstraints.NORTHWEST,GridBagConstraints.BOTH,
                                new Insets(contentRow==1 ? UNDER_HEADER_MARGIN : 0,0,16,0),0,0 ) );
                    }
                }

                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        setContent( contentPanel );
                    }
                });

                //schedule feed reload
                reloadTimer = RequestProcessor.getDefault().post( this, RSS_FEED_TIMER_RELOAD_MILLIS );

            } catch( UnknownHostException uhE ) {
                setCursor( Cursor.getDefaultCursor() );
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        setContent( buildProxyPanel() );
                    }
                });
            } catch( SocketException sE ) {
                setCursor( Cursor.getDefaultCursor() );
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        setContent( buildProxyPanel() );
                    }
                });
            } catch( Exception e ) {
                setCursor( Cursor.getDefaultCursor() );
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        setContent( buildErrorLabel() );
                    }
                });
                ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
            }
        }
    }
    
    protected static String getTextContent(Node node) {
        Node child = node.getFirstChild();
        if( null == child )
            return null;
        
        return child.getNodeValue();
    }

    protected String formatDateTime( String strDateTime ) {
        try {
            Date date = parsingDateFormat.parse( strDateTime );
            return printingDateFormat.format( date );
        } catch( NumberFormatException nfE ) {
            //ignore
        } catch( ParseException pE ) {
            try {
                Date date = parsingDateFormatShort.parse( strDateTime );
                return printingDateFormatShort.format( date );
            } catch( NumberFormatException nfE ) {
                //ignore
            } catch( ParseException otherPE ) {
                //ignore
            }
        }
        return strDateTime;
    }
    
    private static final long serialVersionUID = 1L; 

    public Dimension getPreferredSize() {
        Dimension retValue = super.getPreferredSize();
        retValue.width = 1;
        retValue.height = 1;
        return retValue;
    }
    
    public void removeNotify() {
        //cancel reload timer
        if( null != reloadTimer ) {
            reloadTimer.cancel();
            reloadTimer = null;
        }
        super.removeNotify();
    }

    public void addNotify() {
        super.addNotify();
        if( null == reloadTimer && !Boolean.getBoolean("netbeans.full.hack")) {
            if( System.currentTimeMillis() - lastReload >= RSS_FEED_TIMER_RELOAD_MILLIS ) {
                reload();
            } else {
                reloadTimer = RequestProcessor.getDefault().post( new Reload(),
                        (int)(RSS_FEED_TIMER_RELOAD_MILLIS - (System.currentTimeMillis() - lastReload)) );
            }
        }
    }

    private String trimHtml( String htmlSnippet ) {
        String res = htmlSnippet.replaceAll( "<[^>]*>", "" ); // NOI18N // NOI18N
        res = res.replaceAll( "&nbsp;", " " ); // NOI18N // NOI18N
        res = res.trim();
        int maxLen = getMaxDecsriptionLength();
        if( res.length() > maxLen ) {
            res = res.substring( 0, maxLen ) + "..."; // NOI18N
        }
        return res;
    }
    
    protected int getMaxDecsriptionLength() {
        int verticalSize = Toolkit.getDefaultToolkit().getScreenSize().height;
        if( verticalSize >= 1200 )
            return 350;
        if( verticalSize >= 1024 )
            return 220;
        return 140;
    }

    protected Component getContentHeader() {
        return null;
    }

    private JComponent buildProxyPanel() {
        Component header = getContentHeader();
        JPanel panel = null == header ? new JPanel(new GridBagLayout()) : new NoHorizontalScrollPanel();
        panel.setOpaque( false );

        int row = 0;
        if( null != header ) {
            panel.add( header,  new GridBagConstraints(0,row++,1,1,1.0,0.0,
                    GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0 ) );
        }

        panel.add( new JLabel(BundleSupport.getLabel("ErrCannotConnect")),  // NOI18N
                new GridBagConstraints(0,row++,1,1,0.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,10,10,5),0,0 ) );
        if( showProxyButton ) {
            JButton button = new JButton();
            Mnemonics.setLocalizedText( button, BundleSupport.getLabel( "ProxyConfig" ) );  // NOI18N
            button.setOpaque( false );
            button.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    HttpProxySettings.getDefault().showConfigurationDialog();
                }
            });
            panel.add( button, new GridBagConstraints(0,row++,1,1,0.0,0.0,
                    GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,10,10,5),0,0 ) );
        }
        return panel;
    }

    private JComponent buildContentLoadingLabel() {
        JLabel label = new JLabel( BundleSupport.getLabel( "ContentLoading" ) ); // NOI18N
        label.setHorizontalAlignment( JLabel.CENTER );
        label.setVerticalAlignment( JLabel.CENTER );
        label.setForeground( Utils.getColor(DEFAULT_TEXT_COLOR) );
        label.setBackground( Utils.getColor(DEFAULT_BACKGROUND_COLOR) );
        label.setOpaque( false );
        Component header = getContentHeader();
        if( null != header ) {
            JPanel panel = new NoHorizontalScrollPanel();
            panel.setOpaque( false );
            panel.add( header, new GridBagConstraints(0,0,1,1,1.0,1.0,
                GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0 ) );
            panel.add( label, new GridBagConstraints(0,1,1,1,1.0,1.0,
                GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0 ) );
            return panel;
        }
        return label;
    }

    private JComponent buildErrorLabel() {
        Component header = getContentHeader();
        JPanel panel = null == header ? new JPanel(new GridBagLayout()) : new NoHorizontalScrollPanel();
        panel.setOpaque( false );

        int row = 0;
        if( null != header ) {
            panel.add( header,  new GridBagConstraints(0,row++,1,1,1.0,0.0,
                    GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0 ) );
        }

        panel.add( new JLabel(BundleSupport.getLabel("ErrLoadingFeed")),  // NOI18N
                new GridBagConstraints(0,row++,1,1,0.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,10,10,5),0,0 ) );
        JButton button = new JButton();
        Mnemonics.setLocalizedText( button, BundleSupport.getLabel( "Reload" ) );  // NOI18N
        button.setOpaque( false );
        button.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reload();
            }
        });
        panel.add( button, new GridBagConstraints(0,row++,1,1,0.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,10,10,5),0,0 ) );
        return panel;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if( HttpProxySettings.PROXY_SETTINGS.equals( evt.getPropertyName() ) ) {
            setViewportView( buildContentLoadingLabel() );
            reload();
        }
    }

    static class FeedHandler implements ContentHandler {
        private FeedItem currentItem;
        private StringBuffer textBuffer;
        private ArrayList<FeedItem> itemList = new ArrayList<FeedItem>( 10 );

        public void setDocumentLocator(Locator locator) {
        }

        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        public void endPrefixMapping(String prefix) throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if( itemList.size() < NEWS_COUNT ) {
                if( "item".equals( localName ) ) { // NOI18N
                    currentItem = new FeedItem();
                } else if( "link".equals( localName ) // NOI18N
                        || "pubDate".equals( localName ) // NOI18N
                        || "date".equals( localName ) // NOI18N
                        || "description".equals( localName ) // NOI18N
                        || "title".equals( localName ) ) { // NOI18N
                    textBuffer = new StringBuffer( 110 );
                }
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if( itemList.size() < NEWS_COUNT ) {
                if( "item".equals( localName ) ) { // NOI18N
                    if( null != currentItem && currentItem.isValid() ) {
                        itemList.add( currentItem );
                    }
                    currentItem = null;
                } else if( null != currentItem && null != textBuffer ) {
                    String text = textBuffer.toString().trim();
                    textBuffer = null;
                    if( 0 == text.length() )
                        text = null;

                    if( "link".equals( localName ) ) { // NOI18N
                        currentItem.link = text;
                    } else if( "pubDate".equals( localName ) // NOI18N
                            || "date".equals( localName ) ) { // NOI18N
                        currentItem.dateTime = text;
                    } else if( "title".equals( localName ) ) { // NOI18N
                        currentItem.title = text;
                    } else if( "description".equals( localName ) ) { // NOI18N
                        currentItem.description = text;
                    }
                }
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if( null != textBuffer )
                textBuffer.append( ch, start, length );
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        public void processingInstruction(String target, String data) throws SAXException {
        }

        public void skippedEntity(String name) throws SAXException {
        }

        public ArrayList<FeedItem> getItemList() {
            return itemList;
        }
    }

    static class FeedItem {
        String title;
        String link;
        String description;
        String dateTime;

        boolean isValid() {
            return null != title && null != link;
        }
    }
}
