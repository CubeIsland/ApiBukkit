package de.codeinfection.quickwango.ApiBukkit.Net;

import static de.codeinfection.quickwango.ApiBukkit.ApiBukkit.log;
import static de.codeinfection.quickwango.ApiBukkit.ApiBukkit.debug;
import de.codeinfection.quickwango.ApiBukkit.ApiConfiguration;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class WebServer
{
    private final List<Request> requests;

    private int httpServerPort;
    private ServerSocket httpServerSocket;
    private Thread httpServerThread;
    protected final ApiConfiguration config;
    private String charset;

    public WebServer(ApiConfiguration config)
    {
        this.config = config;
        this.requests = Collections.synchronizedList(new ArrayList<Request>());
        this.charset = "UTF-8";
    }

    public String getCharset()
    {
        return this.charset;
    }

    public WebServer setCharset(String charset)
    {
        this.charset = charset;
        return this;
    }

    /**
     * Starts a HTTP server on given port.
     * 
     * @throws IOException if the socket is already in use
     */
    public void start(int port, int maxSessions) throws IOException
    {
        this.httpServerThread = new Thread(new HttpServerThread(maxSessions));
        this.httpServerThread.setDaemon(true);
        this.httpServerPort = port;
        this.httpServerSocket = new ServerSocket(this.httpServerPort);
        this.httpServerSocket.setPerformancePreferences(2, 1, 0);
        this.httpServerThread.start();
    }

    /**
     * Closes all sessions and stops the server.
     */
    public void stop()
    {
        try
        {
            this.httpServerSocket.close();
            this.httpServerThread.join(500);
            for (Request session : this.requests)
            {
                session.close();
            }
            this.requests.clear();
        }
        catch (Throwable t)
        {}
    }

    /**
     * Generates the response out of the information given by the request
     *
     * @param url url-decoded request path
     * @param method the request method used by the client
     * @param params the parsed querystring as a object tree
     * @param header url-decoded headers
     * @return response, see class Response for details
     */
    public abstract Response processRequest(String uri, InetAddress remoteIp, String method, Map<String, String> headers, Parameters params);


    
    private class HttpServerThread implements Runnable
    {
        protected int maxSessions;

        public HttpServerThread(int maxSessions)
        {
            this.maxSessions = maxSessions;
        }

        public void run()
        {
            try
            {
                Request session;
                while (true)
                {
                    session = new Request(httpServerSocket.accept());
                    String IP = session.socket.getInetAddress().getHostAddress();

                    // default: IP is allowed
                    boolean IPAllowed = true;
                    if (config.whitelistEnabled && !config.whitelist.contains(IP))
                    {
                        // whitelisting: IP rejected if not on the whitelist
                        IPAllowed = false;
                    }
                    if (config.blacklistEnabled && config.blacklist.contains(IP))
                    {
                        // blacklisting: IP rejected when on the blacklist
                        IPAllowed = false;
                    }

                    if (!IPAllowed)
                    {
                        log("IP \"" + IP + "\" rejected!");
                    }

                    if (IPAllowed && requests.size() < this.maxSessions)
                    {
                        session.start();
                        requests.add(session);
                    }
                    else
                    {
                        session.close();
                    }
                }
            }
            catch (IOException ioe)
            {}
        }
    }

    private class Request implements Runnable
    {
        private Socket socket;
        private Thread thread;
        
        public Request(Socket socket)
        {
            this.socket = socket;
            this.thread = null;
        }

        public void start()
        {
            this.thread = new Thread(this);
            this.thread.setDaemon(true);
            this.thread.start();
        }

        public void close()
        {
            try
            {
                this.socket.shutdownInput();
                this.socket.shutdownOutput();
                if (!this.socket.isClosed())
                {
                    this.socket.close();
                }
            }
            catch (Throwable t)
            {}
        }

        public void run()
        {
            try
            {
                InputStream inputStream = this.socket.getInputStream();
                if (inputStream == null)
                {
                    return;
                }

                // Read the first 8192 bytes.
                // The full header should fit in here.
                // Apache's default header limit is 8KB.
                final int BUFFER_SIZE = 8192;
                byte[] buffer = new byte[BUFFER_SIZE];
                int receivedBytes = inputStream.read(buffer, 0, BUFFER_SIZE);
                if (receivedBytes <= 0)
                {
                    return;
                }

                // Create a BufferedReader for parsing the header.
                ByteArrayInputStream input = new ByteArrayInputStream(buffer, 0, receivedBytes);
                BufferedReader readr = new BufferedReader(new InputStreamReader(input));
                Map<String, String> pre = new HashMap<String, String>();
                Parameters params = new Parameters();
                Map<String, String> headers = new HashMap<String, String>();

                // Decode the header into parms and header java properties
                parseHeader(readr, pre, params, headers);
                String method = pre.get("method");
                String uri = pre.get("uri");

                long size = 0x7FFFFFFFFFFFFFFFl;
                String contentLength = headers.get("content-length");
                if (contentLength != null)
                {
                    try
                    {
                        size = Integer.parseInt(contentLength);
                    }
                    catch (NumberFormatException ex)
                    {}
                }

                // We are looking for the byte separating header from body.
                // It must be the last byte of the first two sequential new lines.
                int splitbyte = 0;
                boolean sbfound = false;
                while (splitbyte < receivedBytes)
                {
                    if (buffer[splitbyte] == '\r' && buffer[++splitbyte] == '\n' && buffer[++splitbyte] == '\r' && buffer[++splitbyte] == '\n')
                    {
                        sbfound = true;
                        break;
                    }
                    splitbyte++;
                }
                splitbyte++;

                // Write the part of body already read to ByteArrayOutputStream f
                ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                if (splitbyte < receivedBytes)
                {
                    byteArrayStream.write(buffer, splitbyte, receivedBytes-splitbyte);
                }

                // While Firefox sends on the first read all the data fitting
                // our buffer, Chrome and Opera sends only the headers even if
                // there is data for the body. So we do some magic here to find
                // out whether we have already consumed part of body, if we
                // have reached the end of the data to be sent or we should
                // expect the first byte of the body at the next read.
                if (splitbyte < receivedBytes)
                {
                    size -= receivedBytes - splitbyte + 1;
                }
                else if (!sbfound || size == 0x7FFFFFFFFFFFFFFFl)
                {
                    size = 0;
                }

                // Now read all the body and write it to f
                buffer = new byte[512];
                while (receivedBytes >= 0 && size > 0)
                {
                    receivedBytes = inputStream.read(buffer, 0, 512);
                    size -= receivedBytes;
                    if (receivedBytes > 0)
                    {
                        byteArrayStream.write(buffer, 0, receivedBytes);
                    }
                }

                // Get the raw body as a byte []
                byte[] fbuf = byteArrayStream.toByteArray();

                // Create a BufferedReader for easily reading it as string.
                ByteArrayInputStream bin = new ByteArrayInputStream(fbuf);
                BufferedReader reader = new BufferedReader(new InputStreamReader(bin));

                // If the method is POST, there may be parameters
                // in data section, too, read it:
                if (method.equalsIgnoreCase("POST"))
                {
                    String contentType = "";
                    String contentTypeHeader = headers.get("content-type");
                    StringTokenizer st = new StringTokenizer(contentTypeHeader , "; ");
                    if (st.hasMoreTokens())
                    {
                        contentType = st.nextToken();
                    }

                    if (contentType.equalsIgnoreCase("multipart/form-data"))
                    {
                        sendError(Status.BADREQUEST, "Multipart requests are not supported!");
                    }
                    else
                    {
                        // Handle application/x-www-form-urlencoded
                        String postLine = "";
                        char pbuf[] = new char[512];
                        int read = reader.read(pbuf);
                        while (read >= 0 && !postLine.endsWith("\r\n"))
                        {
                            postLine += String.valueOf(pbuf, 0, read);
                            read = reader.read(pbuf);
                        }
                        postLine = postLine.trim();
                        parseQueryString(postLine, params);
                    }
                }

                // generate the response
                Response response = processRequest(uri, socket.getInetAddress(), method, headers, params);
                if (response == null)
                {
                    sendError(Status.INTERNALERROR, "No content to serve!");
                }
                else
                {
                    sendResponse(response.status, response.mimeType, response.header, response.data);
                }

                reader.close();
                inputStream.close();
                this.close();
                requests.remove(this);
            }
            catch (IOException e)
            {
                try
                {
                    sendError(Status.INTERNALERROR, "IOException: " + e.getLocalizedMessage());
                }
                catch (Throwable t)
                {}
            }
            catch (InterruptedException e)
            {}
        }

        /**
         * Decodes the sent headers and loads the data into
         * java Properties' key - value pairs
         **/
        private void parseHeader(BufferedReader in, Map<String, String> pre, Parameters params, Map<String, String> headers) throws InterruptedException
        {
            try
            {
                // Read the request line
                String inLine = in.readLine();
                if (inLine == null)
                {
                    return;
                }
                StringTokenizer tokenizer = new StringTokenizer(inLine);
                if (!tokenizer.hasMoreTokens())
                {
                    sendError(Status.BADREQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
                }

                String method = tokenizer.nextToken();
                pre.put("method", method.toUpperCase());

                if (!tokenizer.hasMoreTokens())
                {
                    sendError(Status.BADREQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
                }


                String uri = tokenizer.nextToken();

                // Decode parameters from the URI
                int queryDelimIndex = uri.indexOf('?');
                if (queryDelimIndex > -1)
                {
                    parseQueryString(uri.substring(queryDelimIndex + 1), params);
                    uri = urlDecode(uri.substring(0, queryDelimIndex));
                }
                else
                {
                    uri = urlDecode(uri);
                }

                // If there's another token, it's protocol version,
                // followed by HTTP headers. Ignore version but parse headers.
                // NOTE: this now forces header names lowercase since they are
                // case insensitive and vary by client.
                if (tokenizer.hasMoreTokens())
                {
                    String line = in.readLine();
                    while (line != null && line.trim().length() > 0)
                    {
                        int p = line.indexOf( ':' );
                        if (p >= 0)
                        {
                            headers.put( line.substring(0,p).trim().toLowerCase(), line.substring(p+1).trim());
                        }
                        line = in.readLine();
                    }
                }

                pre.put("uri", uri);
            }
            catch (IOException e)
            {
                sendError(Status.INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + e.getMessage());
            }
        }

        /**
         * Decodes the percent encoding scheme. <br/>
         * For example: "an+example%20string" -> "an example string"
         */
        private String urlDecode(String string)
        {
            try
            {
                return URLDecoder.decode(string, "UTF-8");
            }
            catch (Exception e)
            {
                return string;
            }
        }

        /**
         * parses a querystring
         */
        private void parseQueryString(String queryString, Parameters params)
        {
            this.parseQueryString(queryString, params, "&");
        }

        /**
         * parses a querystring
         */
        private void parseQueryString(String queryString, Parameters params, String pairDelim)
        {
            this.parseQueryString(queryString, params, pairDelim, "=");
        }

        /**
         * parses a querystring
         */
        private void parseQueryString(String queryString, Parameters params, String pairDelim, String valueDelim)
        {
            if (queryString == null || queryString.length() == 0)
            {
                return;
            }

            StringTokenizer tokenizer = new StringTokenizer(queryString, pairDelim);
            while (tokenizer.hasMoreTokens())
            {
                this.parseKeyValuePair(tokenizer.nextToken(), params, valueDelim);
            }
        }

        private void parseKeyValuePair(String keyValuePair, Parameters params, String valueDelim)
        {
            int delimPosition = keyValuePair.indexOf(valueDelim);
            if (delimPosition > -1)
            {
                String key = keyValuePair.substring(0, delimPosition);
                String value = urlDecode(keyValuePair.substring(delimPosition + 1));
                
                params.put(this.parseKey(key), value);
            }
            else
            {
                List<String> path = this.parseKey(keyValuePair);
                if (!params.containsKey(path))
                {
                    params.put(path, null);
                }
            }
        }

        private List<String> parseKey(String key)
        {
            List<String> path = new ArrayList<String>();
            int firstOpenBracketPosition = key.indexOf("[");
            if (firstOpenBracketPosition > -1)
            {
                String indicesString = key.substring(firstOpenBracketPosition);
                int lastCloseBracketPosition = indicesString.lastIndexOf("]");
                if (lastCloseBracketPosition == indicesString.length() - 1)
                {
                    key = urlDecode(key.substring(0, firstOpenBracketPosition));
                    String delimitedIndices = indicesString.substring(1, lastCloseBracketPosition);

                    path.add(key);
                    for (String token : this.tokenize(delimitedIndices, "]["))
                    {
                        debug("Token: >" + token + "<");
                        if (token.length() == 0)
                        {
                            path.add(null);
                        }
                        else
                        {
                            path.add(urlDecode(token));
                        }
                    }
                    return path;
                }
            }

            path.add(urlDecode(key));
            return path;
        }

        private List<String> tokenize(String string, String delim)
        {
            int pos = 0, offset = 0, delimLen = delim.length();
            List<String> tokens = new ArrayList<String>();

            while ((pos = string.indexOf(delim, offset)) > -1)
            {
                tokens.add(string.substring(offset, pos));
                offset = pos + delimLen;
            }

            return tokens;
        }

        /**
         * Returns an error message as a HTTP response and
         * throws InterruptedException to stop further request processing.
         */
        private void sendError(Status status, String msg) throws InterruptedException
        {
            sendResponse(status, MimeType.PLAIN, null, new ByteArrayInputStream(msg.getBytes()));
            throw new InterruptedException();
        }

        /**
         * Sends given response to the socket.
         */
        private void sendResponse(Status status, MimeType mimeType, HashMap<String, String> header, InputStream data)
        {
            try
            {
                if (status == null)
                {
                    throw new IllegalArgumentException("Status can't be null.");
                }

                OutputStream out = this.socket.getOutputStream();
                PrintWriter writer = new PrintWriter(out);
                writer.print("HTTP/1.0 " + status + " \r\n");

                if (mimeType != null)
                {
                    writer.print("Content-Type: " + mimeType + "; charset=" + charset + "\r\n");
                }

                if (header != null)
                {
                    for (Map.Entry<String, String> entry : header.entrySet())
                    {
                        writer.print(entry.getKey() + ": " + entry.getValue() + "\r\n");
                    }
                }

                writer.print("\r\n");
                writer.flush();

                if (data != null)
                {
                    byte[] buffer = new byte[2048];
                    while (true)
                    {
                        int read = data.read(buffer, 0, 2048);
                        if (read <= 0)
                        {
                            break;
                        }
                        out.write(buffer, 0, read);
                    }
                    data.close();
                }
                out.flush();
                out.close();
            }
            catch (IOException ioe)
            {
                // close the socket on write failure.
                try
                {
                    this.socket.close();
                }
                catch (Throwable t)
                {}
            }
        }
    }
}
