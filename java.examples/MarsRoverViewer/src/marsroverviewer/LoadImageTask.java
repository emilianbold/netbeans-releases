/*
 * Copyright (c) 2007, Sun Microsystems, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems, Inc. nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */ 

package marsroverviewer;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;

/**
 * A Task that loads an image from a URL.  Loading and decoding 
 * progress is reported via the Task <code>progress</code> property
 * and messages are generated when the Task begins and when it
 * finishes.  If errors occur then warnings are logged and the 
 * Task's value is null.
 * <p>
 * Applications would typically use LoadImageTask by creating
 * a private subclass that overrode the <code>done</code> method.
 * See SingleFrameExample5.java for an example.
 * <p>
 * Resources for this class are in the 
 * <code>resources/LoadImageTask.properties</code> ResourceBundle.
 * 
 */
public class LoadImageTask extends Task<BufferedImage, Void> {
    private static Logger logger = Logger.getLogger(LoadImageTask.class.getName());
    private final URL url;
    private ImageReader imageReader = null;

    public LoadImageTask(Application app, URL url) {
	super(app, "LoadImageTask"); // init title/description/messages
	this.url = url;
    }

    protected final URL getImageURL() { 
	return url;
    }

    @Override protected BufferedImage doInBackground() {
	IIOReadProgressListener rpl = new IIOReadProgressAdapter() {
	    public void imageProgress(ImageReader r, float p) {
		setProgress(p, 0.0f, 100.0f);
	    }
	};
	message("startedLoadingImage", url);
	imageReader = findImageReader(url);
	return loadImage(imageReader, rpl);
    }

    private void completionMessage(String resourceKey) {
	message(resourceKey, url, getExecutionDuration(TimeUnit.MILLISECONDS));
    }

    @Override protected void cancelled() {
        if (imageReader != null) {
            imageReader.abort();
        }
	completionMessage("cancelledLoadingImage");
    }

    @Override protected void interrupted(InterruptedException e) {
        if (imageReader != null) {
            imageReader.abort();
        }
	completionMessage("cancelledLoadingImage");
    }

    @Override protected void succeeded(BufferedImage image) {
	completionMessage("finishedLoadingImage");
    }

    @Override protected void failed(Throwable e) {
	completionMessage("failedLoadingImage");
    }
    
    @Override protected void finished() {
	imageReader = null;
    }
    

    /* The methods below are what's required by the Java imaging
     * API to enable tracking the progress of an ImageIO read()
     * and optionally aborting it.  If we weren't interested in
     * tracking image-loading progress or supporting Task.cancel()
     * it would be enough to just use ImageIO.read().
     */
    private ImageReader findImageReader(URL url) {
	ImageInputStream input = null; 
	try {
	    input = ImageIO.createImageInputStream(url.openStream());
	}
	catch(IOException e) {
	    logger.log(Level.WARNING, "bad image URL " + url, e);
	}
	ImageReader reader = null;
	if (input != null) {
	    Iterator readers = ImageIO.getImageReaders(input);
	    while((reader == null) && (readers != null) && readers.hasNext()) {
		reader = (ImageReader)readers.next();
	    }
	    reader.setInput(input);
	}
	return reader;
    }

    private BufferedImage loadImage(ImageReader reader, IIOReadProgressListener listener) {
	BufferedImage image = null;
	try {
	    if (listener != null) {
		reader.addIIOReadProgressListener(listener);
	    }
	    int index = reader.getMinIndex();
	    image = reader.read(index);
	}
	catch (IOException e) { 
	    logger.log(Level.WARNING, "loadImage failed", e);
	}
	finally {
	    ImageInputStream input = (ImageInputStream)(reader.getInput());
	    if (input != null) {
		try { input.close(); } catch (IOException e) { }
	    }
	    if (reader != null) {
		reader.removeAllIIOReadProgressListeners();
		reader.dispose();
	    }
	}
	return image;
    }

    /* Makes creating an IIOReadProgressListener less horrible looking; see 
     * LoadImageTask.doInBackground() above.
     */
    private static class IIOReadProgressAdapter implements IIOReadProgressListener {
	public void imageStarted(ImageReader rdr, int imageIndex) { }
	public void imageProgress(ImageReader rdr, float percentageDone) { }
	public void imageComplete(ImageReader rdr) { }
	public void readAborted(ImageReader rdr) { }
	public void sequenceStarted(ImageReader rdr, int minIndex) { }
	public void sequenceComplete(ImageReader rdr) { }
	public void thumbnailStarted(ImageReader rdr, int imageIndex, int thumbIndex) {	}
	public void thumbnailProgress(ImageReader rdr, float percentageDone) { }
	public void thumbnailComplete(ImageReader rdr) { }
    }
}

