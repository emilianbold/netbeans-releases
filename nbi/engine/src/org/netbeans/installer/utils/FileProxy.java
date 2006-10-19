package org.netbeans.installer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import org.netbeans.installer.downloader.DownloadFilesBase;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.downloader.queue.EmptyQueueListener;
import org.netbeans.installer.downloader.queue.URLStatus;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Danila_Dugurov
 */
public class FileProxy {
  
  private final DownloadManager manager = DownloadManager.getInstance();
  
  MyListener listener = new MyListener();
  {
    manager.getURLQueue().addListener(listener);
  }
  
  URL currentURL;
  
  URLStatus currentURLStatus;
  
  Progress progress;
  
  public static final FileProxy proxy = new FileProxy();
  //TODO: not thread safe now!!!!!!!!!
  
  public static FileProxy getInstance() {
    return proxy;
  }
  
  public File getFile(URL url) throws DownloadException {
    return getFile(url, null, false);
  }
  public File getFile(String uri) throws DownloadException {
    return getFile(uri, null, null);
  }
  
  public File getFile(String uri, boolean deleteOnExit) throws DownloadException {
    return getFile(uri, null, null, deleteOnExit);
  }
  
  public File getFile(String uri, ClassLoader loader) throws DownloadException {
    return getFile(uri, null, loader);
  }
  
  public File getFile(URI uri, Progress progress)  throws DownloadException {
    return getFile(uri, progress, null, false);
  }
  
  public File getFile(String uri, Progress progress, ClassLoader loader) throws DownloadException{
    return getFile(uri, progress, loader, false);
  }
  
  public File getFile(String uri, Progress progress, ClassLoader loader, boolean deleteOnExit) throws DownloadException {
    final URI myUri;
    try {
      myUri = new URI(uri);
    } catch (URISyntaxException ex) {
      throw new DownloadException("uri:" + uri, ex);
    }
    return getFile(myUri, progress, loader, deleteOnExit);
  }
  
  public File getFile(URI uri, boolean deleteOnExit) throws DownloadException {
    return getFile(uri, null, null, deleteOnExit);
  }
  
  public File getFile(URI uri) throws DownloadException {
    return getFile(uri, null, null, false);
  }
  
  public File getFile(URI uri, Progress progress, ClassLoader loader, boolean deleteOnExit) throws DownloadException {
    if (uri.getScheme().equals("file")) {
      File file = new File(uri);
      if (!file.exists()) throw new DownloadException("file not exist: " + uri);
      return file;
    } else if (uri.getScheme().equals("resource")) {
      OutputStream out  = null;
      try {
        File dir = SystemUtils.getInstance().getTempDirectory();//TODO: is Dir exists;
        String path = uri.getSchemeSpecificPart();
        File file = new File(dir, path.substring(path.lastIndexOf('/')));
        file.createNewFile();
        final InputStream resource = (loader != null ? loader: getClass().getClassLoader()).getResourceAsStream(uri.getSchemeSpecificPart());
        out = new FileOutputStream(file);
        if (resource == null) throw new DownloadException("resource:" + uri + "not found");
        StreamUtil.transferData(resource, out);
        return file;
      } catch(IOException ex) {
        throw new DownloadException("I/O error has occures", ex);
      } finally {
        if (out != null)
          try {
            out.close();
          } catch (IOException ignord) {}
      }
    } else if (uri.getScheme().startsWith("http")) {
      try {
        return getFile(uri.toURL(), progress, deleteOnExit);
      } catch(MalformedURLException ex) {
        throw new UnexpectedExceptionError(ex);
      }
    }
    throw new DownloadException("unsupported sheme: " + uri.getScheme());
  }
  
  protected File getFile(URL url, Progress progress, boolean deleteOnExit) throws DownloadException {
    //   if (currentURL != null) throw new IllegalStateException("getFile not thread Safe! not allowed cuncurrency invokation!");
    currentURL = url;
    this.progress = progress;
    File file = DownloadFilesBase.getInstance().getFile(currentURL);
    if (file != null) return file;
    synchronized (this) {
      manager.getURLQueue().add(currentURL);
      try {
        System.out.println("sleep..");
        wait();
        System.out.println("un sleep..");
      } catch (InterruptedException ex) {
        //TODO: todo todo
        throw new UnexpectedExceptionError(ex);
      }
    }
    if (currentURLStatus != URLStatus.DOWNLOAD_FINISHED) {
      currentURL = null;
      this.progress = null;
      throw new DownloadException(currentURLStatus.toString());
    }
    currentURL = null;
    this.progress = null;
    if (deleteOnExit)
      DownloadFilesBase.getInstance().getFile(url).deleteOnExit();
    return DownloadFilesBase.getInstance().getFile(url);
  }
  
  private class MyListener extends EmptyQueueListener {
    
    public void URLStatusChanged(URL url) {
      System.out.println(url + " status " + DownloadManager.getInstance().getURLQueue().getStatus(url));
      System.out.println("time: " + new Date(System.currentTimeMillis()));
      if (!url.equals(currentURL)) return;
      URLStatus status = manager.getURLQueue().getStatus(currentURL);
      boolean shuldNotify = false;
      switch (status) {
        case DOWNLOAD_FINISHED: {
          currentURLStatus = status;
          shuldNotify = true;
          break;
        }
        case CONNECTION_FAILD:
        case DOWNLOAD_FAILD:
          
      }
      if (shuldNotify == true) {
        synchronized (FileProxy.this) {
          FileProxy.this.notify();
        }
      }
    }
    public void newURLAdded(URL url) {
      System.out.println("added  " + url);
    }
    
    public void chunkDownloaded(URL url, int length) {
      //       System.out.print("chunk downloaded - length" + length + ": " + url + " T:");
      //       System.out.println(Thread.currentThread().getName());
      if (progress == null) return;
      double per = (double)manager.getURLQueue().getCurrentSize(url) / manager.getURLQueue().getSize(url);
      progress.setPercentage(per);
    }
  }
}
