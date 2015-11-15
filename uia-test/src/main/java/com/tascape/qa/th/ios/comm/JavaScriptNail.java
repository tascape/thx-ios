/*
 * Copyright 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tascape.qa.th.ios.comm;

import com.martiansoftware.nailgun.NGContext;
import com.tascape.qa.th.SystemConfiguration;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Client;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class JavaScriptNail {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JavaScriptNail.class);

    public static final String SYSPROP_NG_CLIENT = "qa.th.comm.NG_CLIENT";

    public static final String NG_CLIENT = SystemConfiguration.getInstance().getProperty(SYSPROP_NG_CLIENT,
        "/usr/local/bin/ng");

    public static void nailMain(NGContext context) throws Exception {
        int port = Integer.parseInt(context.getArgs()[0]);
        CallHandler callHandler = new CallHandler();
        Client client = new Client("localhost", port, callHandler);
        JavaScriptServer jss = JavaScriptServer.class.cast(client.getGlobal(JavaScriptServer.class));
        String js = jss.retrieveJavaScript();
        System.out.println(js);
    }
}
