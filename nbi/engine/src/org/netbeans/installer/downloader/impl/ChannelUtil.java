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
 * 
 * $Id$
 */
package org.netbeans.installer.downloader.impl;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.helper.MutualHashMap;
import org.netbeans.installer.utils.helper.MutualMap;

/**
 * @author Danila_Dugurov
 */
public class ChannelUtil {

  private static final int BUFFER_SIZE = 64 * 1024;

  private static final Map<FileChannel, Integer> channel2ClientsCount = new HashMap<FileChannel, Integer>();
  private static final MutualMap<File, FileChannel> file2Channel = new MutualHashMap<File, FileChannel>();

  //so synchronization on file save me from closing channel when
  //concurrently another thread try to get fragment.
  //No synchronization in methods signiture becouse
  //I don't want to block threads wich deal with another resourses(files)

  public static OutputStream channelFragmentAsStream(final File file, final SectionImpl pumpSection) throws FileNotFoundException {
    if (file == null || pumpSection == null) throw new IllegalArgumentException();
    synchronized (file) {
      if (!file.exists()) throw new FileNotFoundException();
      FileChannel channel;
      if (!file2Channel.containsKey(file)) {
        channel = new RandomAccessFile(file, "rw").getChannel();
        file2Channel.put(file, channel);
        channel2ClientsCount.put(channel, 1);
      } else {
        channel = file2Channel.get(file);
        int count = channel2ClientsCount.get(channel);
        channel2ClientsCount.put(channel, ++count);
      }
    }
    return new OutputStream() {
      final FileChannel channel = file2Channel.get(file);
      final SectionImpl section = pumpSection;
      long position = pumpSection.offset();
      long barier = section.length() > 0 ? section.offset() + section.length() : Long.MAX_VALUE;

      ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

      public synchronized void write(int b) throws IOException {
        if (position + buffer.position() >= barier) return;
        if (buffer.remaining() == 0) flush();
        buffer.put((byte) b);
      }
      
      public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (b == null) throw new NullPointerException();
        if (off < 0 || off > b.length || (off + len > b.length)) throw new IndexOutOfBoundsException();
        while (len > 0) {
          int length = len <= buffer.remaining() ? len: buffer.remaining();
          final long remaining = barier - position - buffer.position();
          if (remaining == 0) break;
          length = length <= remaining  ? length: (int)remaining;
          buffer.put(b, off, length);
          if (buffer.remaining() == 0) flush();
          len -= length;
          off += length;
        }
      }

      // close may be invoked asynchroniously so synchronized modifer really need
      public synchronized void flush() throws IOException {
        final int written = this.channel.write((ByteBuffer) buffer.flip(), position);
        position += written;
        section.shiftOffset(written);
        buffer.rewind();
      }

      //on close() thread release channel in any case of exceptions
      public void close() throws IOException {
        try {
          flush();
        } finally {
          releaseFile(channel);
        }
      }
    };
  }

  private static void releaseFile(final FileChannel channel) {
    final File file = file2Channel.reversedGet(channel);
    if (file == null) return;
    synchronized (file) {
      Integer count = channel2ClientsCount.get(channel);
      if (count == null) return;//already removed
      if (count > 1) {
        channel2ClientsCount.put(channel, --count);
      } else {
        channel2ClientsCount.remove(channel);
        file2Channel.reversedRemove(channel);
        try {
          channel.close();
        } catch (IOException ex) {
          LogManager.log("can't close channel", ex);
        }
      }
    }
  }
}
