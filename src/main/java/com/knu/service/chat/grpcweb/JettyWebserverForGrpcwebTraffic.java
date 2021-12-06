/*
 * Copyright 2020  Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.knu.service.chat.grpcweb;

import java.lang.invoke.MethodHandles;
import java.util.EnumSet;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import jakarta.servlet.DispatcherType;

public class JettyWebserverForGrpcwebTraffic {
  private static final Logger LOG =
      Logger.getLogger(JettyWebserverForGrpcwebTraffic.class.getName());

  private final int mGrpcwebPort;

  public JettyWebserverForGrpcwebTraffic(int grpcwebPort) {
    mGrpcwebPort = grpcwebPort;
  }

  public org.eclipse.jetty.server.Server start() {
    // Start a jetty server to listen on the grpc-web port#
    Server jServer = new Server(mGrpcwebPort);
    ServletHandler handler = new ServletHandler();
    jServer.setHandler(handler);
    handler.addFilterWithMapping(CorsFilter.class, "/*",
            EnumSet.of(DispatcherType.REQUEST));
    handler.addServletWithMapping(GrpcWebTrafficServlet.class, "/*");
    try {
      jServer.start();
    } catch (Exception e) {
      LOG.warning("Jetty Server couldn't be started. " + e.getLocalizedMessage());
      e.printStackTrace();
      return null;
    }
    LOG.info("****  started gRPC-web Service on port# " + mGrpcwebPort);
    jServer.setStopAtShutdown(true);
    return jServer;
  }
}
