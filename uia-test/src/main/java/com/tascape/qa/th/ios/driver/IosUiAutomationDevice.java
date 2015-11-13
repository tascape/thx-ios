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
package com.tascape.qa.th.ios.driver;

import com.tascape.qa.th.ios.comm.JavaScriptServer;
import com.tascape.qa.th.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.FileUtils;
import org.libimobiledevice.ios.driver.binding.exceptions.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.nailgun.NGServer;
import net.sf.lipermi.exception.LipeRMIException;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Server;
import com.tascape.qa.th.ios.comm.JavaScriptNail;
import java.util.concurrent.SynchronousQueue;

/**
 *
 * @author linsong wang
 */
public class IosUiAutomationDevice extends LibIMobileDevice implements JavaScriptServer {
    private static final Logger LOG = LoggerFactory.getLogger(IosUiAutomationDevice.class);

    private int ngPort;

    private int rmiPort;

    private String appName = "APP_NAME";

    private final SynchronousQueue<String> jsQueue = new SynchronousQueue<>();

    public IosUiAutomationDevice(String udid) throws SDKException, IOException {
        super(udid);
    }

    public void init() throws IOException, InterruptedException, LipeRMIException {
        this.startNailGunServer();
        this.startRmiServer();
        this.setupInstrumentsServer();
        Utils.sleep(2000, "wait for server");
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setJavaScript(String javaScript) throws InterruptedException {
        jsQueue.put(javaScript);
    }

    @Override
    public String getJavaScript() throws InterruptedException {
        return jsQueue.take();
    }

    private void startNailGunServer() throws InterruptedException {
        NGServer ngs = new NGServer(null, 0);
        new Thread(ngs).start();
        Utils.sleep(2000, "");
        this.ngPort = ngs.getPort();
        LOG.debug("ng port {}", this.ngPort);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                ngs.shutdown(false);
            }
        });
    }

    private void startRmiServer() throws IOException, LipeRMIException {
        Server server = new Server();
        CallHandler callHandler = new CallHandler();
        this.rmiPort = 8000;
        while (true) {
            try {
                server.bind(rmiPort, callHandler);
                break;
            } catch (IOException ex) {
                LOG.warn("port {} - {}", this.rmiPort, ex.getMessage());
                this.rmiPort += 7;
            }
        }
        LOG.debug("rmi port {}", this.rmiPort);
        callHandler.registerGlobal(JavaScriptServer.class, this);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                server.close();
            }
        });
    }

    private void setupInstrumentsServer() throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder()
            .append("while (1) {").append("\n")
            .append("  var target = UIATarget.localTarget();").append("\n")
            .append("  var host = target.host();").append("\n")
            .append("  var app = target.frontMostApp();").append("\n")
            .append("  var window = app.mainWindow();").append("\n")
            .append("  var js = host.performTaskWithPathArgumentsTimeout('").append(JavaScriptNail.NG_CLIENT)
            .append("', [").append("'--nailgun-port', '").append(ngPort).append("', '")
            .append(JavaScriptNail.class.getName()).append("', '").append(rmiPort).append("'], 10000);").append("\n")
            .append("  UIALogger.logDebug('request ' + js.stdout);").append("\n")
            .append("  try {").append("\n")
            .append("    var res = eval(js.stdout);").append("\n")
            .append("    UIALogger.logDebug(res);").append("\n")
            .append("  } catch(err) {").append("\n")
            .append("    UIALogger.logError(err);").append("\n")
            .append("  }").append("\n")
            .append("}");
        File js = File.createTempFile("instruments-", ".js");
        FileUtils.write(js, sb);
        LOG.debug("{}\n{}", js, sb);
        this.runServer(js);
    }

    private ExecuteWatchdog runServer(File javascript) throws IOException {
        CommandLine cmdLine = new CommandLine("instruments");
        cmdLine.addArgument("-t");
        cmdLine.addArgument("/Applications/Xcode.app/Contents/Applications/Instruments.app/Contents/PlugIns"
            + "/AutomationInstrument.xrplugin/Contents/Resources/Automation.tracetemplate");
        cmdLine.addArgument("-w");
        cmdLine.addArgument(this.getIosDevice().getUUID());
        cmdLine.addArgument(appName);
        cmdLine.addArgument("-e");
        cmdLine.addArgument("UIASCRIPT");
        cmdLine.addArgument(javascript.getAbsolutePath());
        cmdLine.addArgument("-e");
        cmdLine.addArgument("UIARESULTSPATH");
        cmdLine.addArgument(System.getProperty("user.home"));
        LOG.debug("{}", cmdLine.toString());
        ExecuteWatchdog watchdog = new ExecuteWatchdog(Long.MAX_VALUE);
        Executor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(new InstrumentsStreamToFileHandler(null));
        executor.execute(cmdLine, new DefaultExecuteResultHandler());
        return watchdog;
    }

    private class InstrumentsStreamToFileHandler implements ExecuteStreamHandler {
        File output;

        InstrumentsStreamToFileHandler(File output) {
            this.output = output;
        }

        @Override
        public void setProcessInputStream(OutputStream out) throws IOException {
            LOG.trace("setProcessInputStream");
        }

        @Override
        public void setProcessErrorStream(InputStream in) throws IOException {
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));
            do {
                String line = bis.readLine();
                if (line == null) {
                    break;
                }
                LOG.error(line);
            } while (true);
        }

        @Override
        public void setProcessOutputStream(InputStream in) throws IOException {
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));
            if (this.output == null) {
                while (true) {
                    String line = bis.readLine();
                    if (line == null) {
                        break;
                    }
                    LOG.debug(line);
                }

            } else {
                PrintWriter pw = new PrintWriter(this.output);
                LOG.debug("Log stdout to {}", this.output);
                while (true) {
                    String line = bis.readLine();
                    if (line == null) {
                        break;
                    }
                    pw.println(line);
                    pw.flush();
                }
            }
        }

        @Override
        public void start() throws IOException {
            LOG.trace("start");
        }

        @Override
        public void stop() {
            LOG.trace("stop");
        }
    }

    public static void main(String[] args) throws Exception {
        IosUiAutomationDevice d = new IosUiAutomationDevice("c73cd94b20897033b6462e1afef9531b524085c3");
        d.setAppName("Xinkaishi");
        d.init();

        int y = 200;
        for (int i = 0; i < 400; i++) {
            d.setJavaScript("target.tap({x:222, y:" + y++ + "})");
        }
    }
}
