/*
 * Copyright 2012 TranceCode
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.trancecode.web.html2xml;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.ccil.cowan.tagsoup.XMLWriter;
import org.trancecode.logging.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * @author Herve Quiroz
 */
public class Html2XmlServlet extends HttpServlet
{
    private static final String PROPERTY_LOGGING_LEVEL = "logging.level";

    private static final long serialVersionUID = 7343586650546951259L;

    private static Logger LOG = Logger.getLogger(Html2XmlServlet.class);

    @Override
    public void init()
    {
        org.apache.log4j.Logger.getRootLogger().removeAllAppenders();
        org.apache.log4j.Logger.getRootLogger().addAppender(
                new ConsoleAppender(new PatternLayout("%-5p %30.30c{2} %m%n")));
        final String levelName = System.getProperty(PROPERTY_LOGGING_LEVEL);
        final Level level;
        if (levelName == null)
        {
            level = Level.INFO;
        }
        else
        {
            level = Level.toLevel(levelName);
        }
        org.apache.log4j.Logger.getLogger("org.trancecode").setLevel(level);
        LOG.trace("{@method} servlet = {}", getServletName());
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
            IOException
    {
        LOG.trace("{@method} request = {}", request);

        final String requestUri = request.getRequestURI();
        if (!requestUri.matches("^/[^/]+/.+"))
        {
            response.sendError(500);
            return;
        }

        try
        {
            final StringBuilder pageUrlBuilder = new StringBuilder();
            pageUrlBuilder.append("http:/").append(requestUri);
            if (request.getQueryString() != null)
            {
                pageUrlBuilder.append("?").append(request.getQueryString());
            }
            final URL pageUrl = new URL(pageUrlBuilder.toString());
            LOG.debug("GET: {}", pageUrl);

            final XMLReader reader = new Parser();
            reader.setProperty(Parser.schemaProperty, new HTMLSchema());
            final ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            Writer writer = null;
            try
            {
                writer = new OutputStreamWriter(bytesOut);
                reader.setContentHandler(new XMLWriter(writer));
                final InputSource source = new InputSource();
                source.setByteStream(pageUrl.openStream());
                reader.parse(source);
            }
            finally
            {
                Closeables.closeQuietly(writer);
            }

            InputStream bytesIn = null;
            try
            {
                bytesIn = new ByteArrayInputStream(bytesOut.toByteArray());
                ByteStreams.copy(bytesIn, response.getOutputStream());
            }
            finally
            {
                Closeables.closeQuietly(bytesIn);
                Closeables.closeQuietly(response.getOutputStream());
            }
        }
        catch (final Exception e)
        {
            throw new IllegalStateException(request.getRequestURI(), e);
        }
    }
}
