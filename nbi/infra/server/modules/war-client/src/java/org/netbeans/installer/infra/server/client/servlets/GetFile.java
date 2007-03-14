package org.netbeans.installer.infra.server.client.servlets;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.Manager;
import org.netbeans.installer.infra.server.ejb.ManagerException;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.StringUtils;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class GetFile extends HttpServlet {
    @EJB
    private Manager manager;
    
    @Override
    protected void doGet(
            final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException, IOException {
        try {
            System.out.println("request headers:");
            Enumeration en = request.getHeaderNames();
            while (en.hasMoreElements()) {
                Object name = en.nextElement();
                System.out.println("" + name + ": " + request.getHeader(name.toString()));
            }
            
            final String registry = request.getParameter("registry");
            final String path = request.getParameter("file");
            
            final File file = manager.getFile(registry, path);
            
            response.setContentType(
                    "application/octet-stream");
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=" + file.getName());
            response.setHeader(
                    "Last-Modified",
                    StringUtils.httpFormat(new Date(file.lastModified())));
            response.setHeader(
                    "Accept-Ranges",
                    "bytes");
            
            RandomAccessFile input = null;
            OutputStream output = null;
            try {
                output = response.getOutputStream();
                input = new RandomAccessFile(file, "r");
                
                final String range = request.getHeader("Range");
                if (range == null) {
                    System.out.println("range NOT specified");
                    response.setStatus(HttpServletResponse.SC_OK);
                    
                    input.seek(0);
                    
                    response.setHeader(
                            "Content-Length",
                            Long.toString(file.length()));
                    
                    StreamUtils.transferData(input, output);
                } else {
                    Matcher matcher = Pattern.compile("^bytes=([0-9]*)-([0-9]*)$").matcher(range);
                    
                    if (!matcher.find()) {
                        System.out.println("range specified but INVALID");
                        response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    } else {
                        long start = -1;
                        long finish = -1;
                        
                        if (!matcher.group(1).equals("")) {
                            start = Long.parseLong(matcher.group(1));
                        }
                        
                        if (!matcher.group(2).equals("")) {
                            finish = Long.parseLong(matcher.group(2));
                        }
                        
                        if ((start != -1) && 
                                (finish != -1) && 
                                ((start > finish) || (finish > file.length()))) {
                            System.out.println("range specified but is not CORRECT");
                            response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                            return;
                        }
                        
                        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                        
                        if (start == -1) {
                            start = file.length() - finish;
                            finish = -1;
                        }
                        
                        System.out.println("range start: " + start);
                        System.out.println("range end: " + finish);
                        
                        input.seek(start);
                        
                        long length = (finish == -1 ? file.length() - start : finish - start) + 1;
                        
                        response.setHeader("Content-Length", Long.toString(length));
                        response.setHeader("Content-Range", "bytes " + start + "-" + (finish == -1 ? file.length() - 1 : finish) + "/" + file.length());
                        
                        if (finish == -1) {
                            System.out.println("transferring fully");
                            StreamUtils.transferData(input, output);
                        } else {
                            System.out.println("transferring partial");
                            StreamUtils.transferData(input, output, finish - start + 1);
                        }
                        System.out.println("finished transferring");
                    }
                }
            } catch (IOException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                e.printStackTrace();
                e.printStackTrace(response.getWriter());
            } finally {
                if (input != null) {
                    System.out.println("closing input");
                    input.close();
                }
                if (output != null) {
                    System.out.println("closing output");
                    output.close();
                }
            }
        } catch (ManagerException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            e.printStackTrace(response.getWriter());
        }
    }
}
