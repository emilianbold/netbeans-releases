/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.netbeans.feedreader;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.BeanNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class RssNode extends FilterNode {
    
    /** Declaring the children of the root RSS node */
    public RssNode(Node folderNode) {
        super(folderNode, new RssFolderChildren(folderNode));
    }
    
    /** Declaring the Add Feed action and Add Folder action */
    @Override
    public Action[] getActions(boolean popup) {
        DataFolder df = getLookup().lookup(DataFolder.class);
        return new Action[] {
            new AddRssAction(df),
            new AddFolderAction(df)
        };
    }
    
    /** Getting the root node */
    public static class RootRssNode extends RssNode {
        public RootRssNode() throws DataObjectNotFoundException {
            super(DataObject.find(
                    FileUtil.getConfigFile("RssFeeds")).getNodeDelegate());
        }
        
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(RssNode.class, "FN_title");
        }
    }
    
    /** Getting the children of the root node */
    private static class RssFolderChildren extends FilterNode.Children {
        RssFolderChildren(Node rssFolderNode) {
            super(rssFolderNode);
        }
        
        @Override
        protected Node[] createNodes(Node n) {
            if (n.getLookup().lookup(DataFolder.class) != null) {
                return new Node[] {new RssNode(n)};
            } else {
                Feed feed = getFeed(n);
                if (feed != null) {
                    try {
                        return new Node[] {new OneFeedNode(n, feed.getSyndFeed())};
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            }
            // best effort
            return new Node[] {new FilterNode(n)};
        }
    }
    
    /** Getting the feed node and wrapping it in a FilterNode */
    private static class OneFeedNode extends FilterNode {
        
        OneFeedNode(Node feedFileNode, SyndFeed feed) {
            super(feedFileNode,
                    new FeedChildren(feed),
                    new ProxyLookup(new Lookup[] {
                Lookups.fixed(new Object[] {feed}),
                feedFileNode.getLookup()
            }));
        }
        
        @Override
        public String getDisplayName() {
            SyndFeed feed = getLookup().lookup(SyndFeed.class);
            return feed.getTitle();
        }
        
        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage("org/netbeans/feedreader/rss16.gif");
        }
        
        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
        @Override
        public Action[] getActions(boolean context) {
            return new Action[] { SystemAction.get(DeleteAction.class) };
        }
        
    }
    
    /** Defining the children of a feed node */
    private static class FeedChildren extends Children.Keys<SyndEntry> {
        private final SyndFeed feed;
        public FeedChildren(SyndFeed feed) {
            this.feed = feed;
        }
        
        @Override
        protected void addNotify() {
            setKeys(NbCollections.checkedListByCopy(feed.getEntries(), SyndEntry.class, true));
        }
        
        public Node[] createNodes(SyndEntry entry) {
            try {
                return new Node[] {new EntryBeanNode(entry)};
            } catch (IntrospectionException ex) {
                assert false : ex;
                return new Node[0];
            }
        }
    }
    
    /** Wrapping the children in a FilterNode */
    private static class EntryBeanNode extends FilterNode {
        
        private final SyndEntry entry;
        
        public EntryBeanNode(SyndEntry entry) throws IntrospectionException {
            super(new BeanNode<SyndEntry>(entry), Children.LEAF, Lookups.fixed(entry, new EntryOpenCookie(entry)));
            this.entry = entry;
        }
        
        /** Using HtmlDisplayName ensures any HTML in RSS entry titles are properly handled, escaped, entities resolved, etc. */
        @Override
        public String getHtmlDisplayName() {
            return entry.getTitle();
        }
        
        /** Making a tooltip out of the entry's description */
        @Override
        public String getShortDescription() {
            StringBuffer sb = new StringBuffer();
            sb.append("Author: " + entry.getAuthor() + "; ");
            if (entry.getPublishedDate() != null) {
                sb.append("Published: ").append(entry.getPublishedDate().toString());
            }
            return sb.toString();
        }
        
        /** Providing the Open action on a feed entry */
        @Override
        public Action[] getActions(boolean popup) {
            return new Action[] { SystemAction.get(OpenAction.class) };
        }
        
        @Override
        public Action getPreferredAction() {
            return getActions(false)[0];
        }
        
    }
    
    /** Specifying what should happen when the user invokes the Open action */
    private static class EntryOpenCookie implements OpenCookie {
        
        private final SyndEntry entry;
        
        EntryOpenCookie(SyndEntry entry) {
            this.entry = entry;
        }
        
        public void open() {
            BrowserTopComponent btc = BrowserTopComponent.getBrowserComponent(entry);
            btc.open();
            btc.requestActive();
        }
        
    }
    
    /** Looking up a feed */
    private static Feed getFeed(Node node) {
        InstanceCookie ck = node.getLookup().lookup(InstanceCookie.class);
        if (ck == null) {
            throw new IllegalStateException("Bogus file in feeds folder: " + node.getLookup().lookup(FileObject.class));
        }
        try {
            return (Feed) ck.instanceCreate();
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    /** An action for adding a folder to organize feeds into groups */
    private static class AddFolderAction extends AbstractAction {
        
        private final DataFolder folder;
        
        public AddFolderAction(DataFolder df) {
            super(NbBundle.getMessage(RssNode.class, "FN_addfolderbutton"));
            folder = df;
        }
        
        public void actionPerformed(ActionEvent ae) {
            NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                    NbBundle.getMessage(RssNode.class, "FN_askfolder_msg"),
                    NbBundle.getMessage(RssNode.class, "FN_askfolder_title"),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.PLAIN_MESSAGE);
            
            Object result = DialogDisplayer.getDefault().notify(nd);
            
            if (result.equals(NotifyDescriptor.OK_OPTION)) {
                final String folderString = nd.getInputText();
                try {
                    DataFolder.create(folder, folderString);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    /** An action for adding a feed */
    private static class AddRssAction extends AbstractAction {
        
        private final DataFolder folder;
        
        public AddRssAction(DataFolder df) {
            super(NbBundle.getMessage(RssNode.class, "FN_addbutton"));
            folder = df;
        }
        
        public void actionPerformed(ActionEvent ae) {
            NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                    NbBundle.getMessage(RssNode.class, "FN_askurl_msg"),
                    NbBundle.getMessage(RssNode.class, "FN_askurl_title"),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.PLAIN_MESSAGE);
            
            Object result = DialogDisplayer.getDefault().notify(nd);
            
            if (result.equals(NotifyDescriptor.OK_OPTION)) {
                String urlString = nd.getInputText();
                URL url;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException e) {
                    String message = NbBundle.getMessage(RssNode.class, "FN_askurl_err", urlString);
                    Exceptions.attachLocalizedMessage(e, message);
                    Exceptions.printStackTrace(e);
                    return;
                }
                try {
                    checkConnection(url);
                } catch (IOException e) {
                    String message = NbBundle.getMessage(RssNode.class, "FN_cannotConnect_err", urlString);
                    Exceptions.attachLocalizedMessage(e, message);
                    Exceptions.printStackTrace(e);
                    return;
                }
                Feed f = new Feed(url);
                FileObject fld = folder.getPrimaryFile();
                String baseName = "RssFeed";
                int ix = 1;
                while (fld.getFileObject(baseName + ix, "ser") != null) {
                    ix++;
                }
                try {
                    FileObject writeTo = fld.createData(baseName + ix, "ser");
                    FileLock lock = writeTo.lock();
                    try {
                        ObjectOutputStream str = new ObjectOutputStream(writeTo.getOutputStream(lock));
                        try {
                            str.writeObject(f);
                        } finally {
                            str.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        
        private static void checkConnection(final URL url) throws IOException {
            InputStream is = url.openStream();
            is.close();
        }
        
    }
    
}
