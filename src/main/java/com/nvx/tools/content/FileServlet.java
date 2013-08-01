package com.nvx.tools.content;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileServlet to handle static files not in the web-app directory.<br>
 * Most of the code taken from BalusC <br>
 * http://balusc.blogspot.sg/2009/02/fileservlet-supporting-resume-and.html
 */
public class FileServlet extends HttpServlet {

    private static Logger log = LoggerFactory.getLogger(FileServlet.class);

    public static final int DEFAULT_BUFFER_SIZE = 10240; // ..bytes = 10KB.
    public static final long DEFAULT_EXPIRE_TIME = 604800000L; // ..ms = 1 week.
    public static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
    
    private String basePath;
    
    @Override
    public void init() throws ServletException {
        this.basePath = getInitParameter("basePath");
        log.info("Initializing FileServlet with base path: " + this.basePath);

        if (StringUtils.isEmpty(this.basePath)) {
            throw new ServletException("StaticFilter init param 'basePath' is required.");
        } else {
            final File path = new File(this.basePath);
            if (!path.exists()) {
                throw new ServletException("Base path (" + this.basePath +") does not exist.");
            } else if (!path.isDirectory()) {
                throw new ServletException("Base path (" + this.basePath +") is not a directory.");
            } else if (!path.canRead()) {
                throw new ServletException("Base path (" + this.basePath +") cannot be read.");
            }
        }
    }
    
    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, false);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, true);
    }
    
    private void processRequest(HttpServletRequest req, HttpServletResponse resp, boolean content) throws IOException {
        // Check existence
        
        final String requestedFile = req.getPathInfo();
        if (requestedFile == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        final File file = new File(basePath, URLDecoder.decode(requestedFile, "UTF-8"));
        if (!file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Check if file is not modified
        
        final String filename = file.getName();
        final long length = file.length();
        final long lastModified = file.lastModified();
        final String etag = filename + "_" + length + "_" + lastModified;
        
        final String ifNoneMatch = req.getHeader("If-None-Match");
        if (ifNoneMatch != null && FileServeUtil.matches(ifNoneMatch, etag)) {
            resp.setHeader("ETag", etag);
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        
        final long ifModifiedSince = req.getDateHeader("If-Modified-Since");
        if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified) {
            resp.setHeader("ETag", etag);
            resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        
        // Validate request headers for resume
        
        final String ifMatch = req.getHeader("If-Match");
        if (ifMatch != null && !FileServeUtil.matches(ifMatch, etag)) {
            resp.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return;
        }

        final long ifUnmodifiedSince = req.getDateHeader("If-Unmodified-Since");
        if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified) {
            resp.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return;
        }
        
        // Do byte-range requests used in resume
        
        final Range full = new Range(0, length - 1, length);
        final List<Range> ranges = new ArrayList<Range>();

        final String range = req.getHeader("Range");
        if (range != null) {
            // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
            if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
                resp.setHeader("Content-Range", "bytes */" + length);
                resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }

            // If-Range header should either match ETag or be greater then LastModified. If not,
            // then return full file.
            final String ifRange = req.getHeader("If-Range");
            if (ifRange != null && !ifRange.equals(etag)) {
                try {
                    final long ifRangeTime = req.getDateHeader("If-Range"); // Throws IAE if invalid.
                    if (ifRangeTime != -1 && ifRangeTime + 1000 < lastModified) {
                        ranges.add(full);
                    }
                } catch (IllegalArgumentException ignore) {
                    ranges.add(full);
                }
            }

            // If any valid If-Range header, then process each part of byte range.
            if (ranges.isEmpty()) {
                for (String part : range.substring(6).split(",")) {
                    // Assuming a file with length of 100, the following examples returns bytes at:
                    // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                    long start = FileServeUtil.sublong(part, 0, part.indexOf("-"));
                    long end = FileServeUtil.sublong(part, part.indexOf("-") + 1, part.length());

                    if (start == -1) {
                        start = length - end;
                        end = length - 1;
                    } else if (end == -1 || end > length - 1) {
                        end = length - 1;
                    }

                    // Check if Range is syntactically valid. If not, then return 416.
                    if (start > end) {
                        resp.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                        resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }

                    // Add range.
                    ranges.add(new Range(start, end, length));
                }
            }
        }
        
        //Prepare and initialize response
        
        // Get content type by file name and set default GZIP support and content disposition.
        String contentType = getServletContext().getMimeType(filename);
        boolean acceptsGzip = false;
        String disposition = "inline";

        // If content type is unknown, then set the default value.
        // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // If content type is text, then determine whether GZIP content encoding is supported by
        // the browser and expand content type with the one and right character encoding.
        if (contentType.startsWith("text")) {
            String acceptEncoding = req.getHeader("Accept-Encoding");
            acceptsGzip = acceptEncoding != null && FileServeUtil.accepts(acceptEncoding, "gzip");
            contentType += ";charset=UTF-8";
        } 

        // Else, expect for images, determine content disposition. If content type is supported by
        // the browser, then set to inline, else attachment which will pop a 'save as' dialogue.
        else if (!contentType.startsWith("image")) {
            String accept = req.getHeader("Accept");
            disposition = accept != null && FileServeUtil.accepts(accept, contentType) ? "inline" : "attachment";
        }

        resp.reset();
        resp.setBufferSize(DEFAULT_BUFFER_SIZE);
        resp.setHeader("Content-Disposition", disposition + ";filename=\"" + filename + "\"");
        resp.setHeader("Accept-Ranges", "bytes");
        resp.setHeader("ETag", etag);
        resp.setDateHeader("Last-Modified", lastModified);
        resp.setDateHeader("Expires", System.currentTimeMillis() + DEFAULT_EXPIRE_TIME);
        
        // Send the requested file or parts to the client
        
        // Prepare streams.
        RandomAccessFile input = null;
        OutputStream output = null;

        try {
            // Open streams.
            input = new RandomAccessFile(file, "r");
            output = resp.getOutputStream();

            if (ranges.isEmpty() || ranges.get(0) == full) {
                // Return full file.
                Range r = full;
                resp.setContentType(contentType);
                resp.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);

                if (content) {
                    if (acceptsGzip) {
                        // The browser accepts GZIP, so GZIP the content.
                        resp.setHeader("Content-Encoding", "gzip");
                        output = new GZIPOutputStream(output, DEFAULT_BUFFER_SIZE);
                    } else {
                        // Content length is not directly predictable in case of GZIP.
                        // So only add it if there is no means of GZIP, else browser will hang.
                        resp.setHeader("Content-Length", String.valueOf(r.length));
                    }

                    // Copy full range.
                    FileServeUtil.copy(input, output, r.start, r.length, DEFAULT_BUFFER_SIZE);
                }
            } else if (ranges.size() == 1) {
                // Return single part of file.
                Range r = ranges.get(0);
                resp.setContentType(contentType);
                resp.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
                resp.setHeader("Content-Length", String.valueOf(r.length));
                resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                if (content) {
                    // Copy single part range.
                    FileServeUtil.copy(input, output, r.start, r.length, DEFAULT_BUFFER_SIZE);
                }
            } else {
                // Return multiple parts of file.
                resp.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
                resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                if (content) {
                    // Cast back to ServletOutputStream to get the easy println methods.
                    ServletOutputStream sos = (ServletOutputStream) output;

                    // Copy multi part range.
                    for (Range r : ranges) {
                        // Add multipart boundary and header fields for every range.
                        sos.println();
                        sos.println("--" + MULTIPART_BOUNDARY);
                        sos.println("Content-Type: " + contentType);
                        sos.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);

                        // Copy single part range of multi part range.
                        FileServeUtil.copy(input, output, r.start, r.length, DEFAULT_BUFFER_SIZE);
                    }

                    // End with multipart boundary.
                    sos.println();
                    sos.println("--" + MULTIPART_BOUNDARY + "--");
                }
            }
        } finally {
            // Gently close streams.
            FileServeUtil.close(output);
            FileServeUtil.close(input);
        }
    }
    
    /**
     * This class represents a byte range.
     */
    protected class Range {
        long start;
        long end;
        long length;
        long total;

        /**
         * Construct a byte range.
         * @param start Start of the byte range.
         * @param end End of the byte range.
         * @param total Total length of the byte source.
         */
        public Range(long start, long end, long total) {
            this.start = start;
            this.end = end;
            this.length = end - start + 1;
            this.total = total;
        }
    }
}
