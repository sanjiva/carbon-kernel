/*
 * Copyright 2005,2013 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.base.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wso2.carbon.base.Utils;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.base.api.ServerConfigurationService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Activator of the {@link org.wso2.carbon.base} bundle
 */
public class CarbonBaseActivator implements BundleActivator {
    private static Log log = LogFactory.getLog(CarbonBaseActivator.class);
    private ServiceRegistration registration;

    @Override
    public void start(BundleContext bundleContext) throws Exception {

        ServerConfiguration carbonServerConfiguration = ServerConfiguration.getInstance();
        initServerConfiguration(carbonServerConfiguration);
        String portOffset = System.getProperty("portOffset", carbonServerConfiguration.
                getFirstProperty("Ports.Offset"));
        //setting the the retrieved ports.offset value as a system property, in case it was not defined.
        //NIO transport make use of this system property
        System.setProperty("portOffset", portOffset);
        //register carbon server confg as an OSGi service
        registration = bundleContext.registerService(ServerConfigurationService.class.getName(),
                                                     carbonServerConfiguration, null);

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        registration.unregister();
    }

    private void initServerConfiguration(ServerConfiguration carbonServerConfiguration) {
        File carbonXML = new File(Utils.getServerXml());
        InputStream in = null;
        try {
            in = new FileInputStream(carbonXML);
            carbonServerConfiguration.forceInit(in);
        } catch (ServerConfigurationException e) {
            String msg = "Could not initialize server configuration";
            log.fatal(msg);
        } catch (FileNotFoundException e) {
            log.error("Could not initialize server configuration", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn("Cannot close FileInputStream of file " + carbonXML.getAbsolutePath());
                }
            }
        }
    }

}
