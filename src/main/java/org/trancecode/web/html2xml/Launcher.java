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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author Herve Quiroz
 */
public final class Launcher
{
    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final String ENV_HTTP_PORT = "PORT";
    private static final String PROPERTY_HTTP_PORT = "http.port";

    public static void main(final String[] args) throws Exception
    {
        final String portString = System.getProperty(PROPERTY_HTTP_PORT, System.getenv(ENV_HTTP_PORT));
        final int port = portString != null ? Integer.valueOf(portString) : DEFAULT_HTTP_PORT;
        final Server server = new Server(port);
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        final ServletHolder holder = new ServletHolder(new Html2XmlServlet());
        context.addServlet(holder, "/*");
        server.start();
        server.join();
    }
}
