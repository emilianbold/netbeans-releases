/*
 * Copyright (c) 2007-2008 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package org.netbeans.modules.html.validation;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import nu.validator.htmlparser.sax.HtmlSerializer;
import nu.validator.messages.MessageEmitter;
import nu.validator.messages.MessageTextHandler;
import nu.validator.messages.ResultHandler;
import nu.validator.messages.TextMessageTextHandler;
import nu.validator.messages.TextResultHandler;

import nu.validator.messages.types.MessageType;
import nu.validator.source.SourceHandler;
import org.netbeans.editor.ext.html.parser.api.ProblemDescription;
import org.xml.sax.ContentHandler;

import org.xml.sax.SAXException;

public class NbMessageEmitter extends MessageEmitter {

    private static final char[] COLON_SPACE = { ':', ' ' };

    private static final char[] PERIOD = { '.' };

    private static final char[] ON_LINE = "On line ".toCharArray();

    private static final char[] AT_LINE = "At line ".toCharArray();

    private static final char[] FROM_LINE = "From line ".toCharArray();

    private static final char[] TO_LINE = "; to line ".toCharArray();

    private static final char[] COLUMN = ", column ".toCharArray();

    private static final char[] IN_RESOURCE = " in resource ".toCharArray();

    private final Writer writer;

    private final TextMessageTextHandler messageTextHandler;

    private String systemId;

    private int oneBasedFirstLine;

    private int oneBasedFirstColumn;

    private int oneBasedLastLine;

    private int oneBasedLastColumn;

    private boolean textEmitted;

    private final HtmlSerializer contentHandler;

    private ProblemsHandler problemsHandler;

    private int problemType;

    private LinesMapper linesMapper;
    
    
//    private static Writer newOutputStreamWriter(OutputStream out) {
//        CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();
//        enc.onMalformedInput(CodingErrorAction.REPLACE);
//        enc.onUnmappableCharacter(CodingErrorAction.REPLACE);
//        return new OutputStreamWriter(out, enc);
//    }

    public NbMessageEmitter(ProblemsHandler problemsHandler, LinesMapper linesMapper, boolean asciiQuotes) {
        this.problemsHandler = problemsHandler;
        this.linesMapper = linesMapper;
        this.writer = new StringWriter();
        this.messageTextHandler = new TextMessageTextHandler(writer, asciiQuotes);
        this.contentHandler = new HtmlSerializer(writer);
    }

    private void emitErrorLevel(char[] level) throws IOException {
        writer.write(level, 0, level.length);
    }

    private void maybeEmitLocation() throws IOException {
        if (oneBasedLastLine == -1 && systemId == null) {
            return;
        }
        if (oneBasedLastLine == -1) {
            emitSystemId();
        } else if (oneBasedLastColumn == -1) {
            emitLineLocation();
        } else if (oneBasedFirstLine == -1
                || (oneBasedFirstLine == oneBasedLastLine && oneBasedFirstColumn == oneBasedLastColumn)) {
            emitSingleLocation();
        } else {
            emitRangeLocation();
        }
        writer.write('\n');
    }

    /**
     * @throws SAXException
     */
    private void maybeEmitInResource() throws IOException {
        if (systemId != null) {
            this.writer.write(IN_RESOURCE);
            emitSystemId();
        }
    }

    /**
     * @throws SAXException
     */
    private void emitSystemId() throws IOException {
        this.writer.write(systemId);
    }

    private void emitRangeLocation() throws IOException {
        this.writer.write(FROM_LINE);
        this.writer.write(Integer.toString(oneBasedFirstLine));
        this.writer.write(COLUMN);
        this.writer.write(Integer.toString(oneBasedFirstColumn));
        this.writer.write(TO_LINE);
        this.writer.write(Integer.toString(oneBasedLastLine));
        this.writer.write(COLUMN);
        this.writer.write(Integer.toString(oneBasedLastColumn));
        maybeEmitInResource();
    }

    private void emitSingleLocation() throws IOException {
        this.writer.write(AT_LINE);
        this.writer.write(Integer.toString(oneBasedLastLine));
        this.writer.write(COLUMN);
        this.writer.write(Integer.toString(oneBasedLastColumn));
        maybeEmitInResource();
    }

    private void emitLineLocation() throws IOException {
        this.writer.write(ON_LINE);
        this.writer.write(Integer.toString(oneBasedLastLine));
        maybeEmitInResource();
    }

    @Override
    public void startMessage(MessageType type, String systemId,
            int oneBasedFirstLine, int oneBasedFirstColumn,
            int oneBasedLastLine, int oneBasedLastColumn, boolean exact)
            throws SAXException {
        this.systemId = systemId;
        this.oneBasedFirstLine = oneBasedFirstLine;
        this.oneBasedFirstColumn = oneBasedFirstColumn;
        this.oneBasedLastLine = oneBasedLastLine;
        this.oneBasedLastColumn = oneBasedLastColumn;
        try {
            emitErrorLevel(type.getPresentationName());
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
        this.textEmitted = false;

        problemType = messageTypeToProblemType(type);
    }

    private static int messageTypeToProblemType(MessageType type) {
        if(type == MessageType.INFO) {
            return ProblemDescription.INFORMATION;
        } else if(type == MessageType.WARNING) {
            return ProblemDescription.WARNING;
        } else if(type == MessageType.FATAL) {
            return ProblemDescription.FATAL;
        } else {
            return ProblemDescription.ERROR;
        }
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startMessages(java.lang.String)
     */
    @Override
    public void startMessages(String documentUri, boolean willShowSource) throws SAXException {
        problemsHandler.startProblems();
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endMessages()
     */
    @Override
    public void endMessages() throws SAXException {
        problemsHandler.endProblems();
        
         try {
             writer.flush();
             writer.close();
         } catch (IOException e) {
         throw new SAXException(e.getMessage(), e);
         }
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endText()
     */
    @Override
    public void endText() throws SAXException {
        try {
            this.writer.write('\n');
            this.textEmitted = true;
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }

    }


    /**
     * @see nu.validator.messages.MessageEmitter#startText()
     */
    @Override
    public MessageTextHandler startText() throws SAXException {
        try {
            this.writer.write(COLON_SPACE);
            return messageTextHandler;
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void endMessage() throws SAXException {
        try {
            if (!textEmitted) {
                writer.write(PERIOD);
                writer.write('\n');
            }
            maybeEmitLocation();
            writer.write('\n');
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }

        int linefrom = -1, lineto = -1, columnfrom = -1, columnto = -1;
        if(oneBasedFirstLine != -1) {
            linefrom = oneBasedFirstLine;
            columnfrom = oneBasedFirstColumn == -1 ? 0 : oneBasedFirstColumn;
        }
        if(oneBasedLastLine != -1) {
            lineto = oneBasedLastLine;
            columnto = oneBasedLastColumn == -1 ? 0 : oneBasedLastColumn;
        } else {
            lineto = linefrom;
            columnto = 1;
        }

        int from = linesMapper.getSourceOffsetForLocation(linefrom, columnfrom);
        int to = linesMapper.getSourceOffsetForLocation(lineto, columnto);

        problemsHandler.addProblem(ProblemDescription.create("nu.validator.issue",
                writer.toString(),
                problemType,
                from,
                to));
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startResult()
     */
    @Override
    public ResultHandler startResult() throws SAXException {
        return new TextResultHandler(writer);
    }

    @Override
    public ContentHandler startElaboration() throws SAXException {
        return contentHandler;
    }
 

    @Override
    public void endResult() throws SAXException {
        super.endResult();
    }

    @Override
    public void endSource() throws SAXException {
        super.endSource();
    }


    @Override
    public SourceHandler startFullSource(int lineOffset) throws SAXException {
        return super.startFullSource(lineOffset);
    }

    @Override
    public SourceHandler startSource() throws SAXException {
        return super.startSource();
    }



}
