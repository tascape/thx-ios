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
package tascape.qa.th.ios.driver;

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

/**
 *
 * @author linsong wang
 */
public class IosUiAutomationDevice extends LibIMobileDevice {
    private static final Logger LOG = LoggerFactory.getLogger(IosUiAutomationDevice.class);

    private final File lock;

    private final File js;

    private final File res;

    private final File out;

    public IosUiAutomationDevice(String udid) throws SDKException, IOException {
        super(udid);
        lock = File.createTempFile("uia-lock-", ".lock");
        js = File.createTempFile("uia-", ".js");
        res = File.createTempFile("uia-", ".res");
        out = File.createTempFile("out-", ".txt");
    }

    public void init() throws IOException, InterruptedException {
        this.setupInstrumentsServer();
        Utils.sleep(2000, "wait for server");
    }

    protected synchronized void sendJavascript(String javascript) throws IOException {
        if (!lock.exists()) {
            if (!lock.createNewFile()) {
                throw new RuntimeException("Cannot create lock file " + lock);
            }
        }
        FileUtils.write(lock, javascript);
        if (!lock.delete()) {
            throw new RuntimeException("Cannot delete lock file " + lock);
        }
    }

    protected synchronized String readResponse() throws IOException {
        if (!res.exists()) {
            return null;
        }
        try {
            return FileUtils.readFileToString(res);
        } finally {
            if (!res.delete()) {
                throw new RuntimeException("Cannot delete response file " + lock);
            }
        }
    }

    private void setupInstrumentsServer() throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder()
            .append("while (1) {").append("\n")
            .append("  var target = UIATarget.localTarget();").append("\n")
            .append("  var host = target.host();").append("\n")
            .append("  var app = target.frontMostApp();").append("\n")
            .append("  var window = app.mainWindow();").append("\n")
            .append("  var js = host.performTaskWithPathArgumentsTimeout('").append("/bin/cat").append("', ['")
            .append("/tmp/ui/uia.js").append("'], 3);").append("\n")
            .append("  ").append("\n")
            .append("  window.logElementTree();").append("\n")
            .append("  UIALogger.logDebug(js.stdout);").append("\n")
            .append("  try {").append("\n")
            .append("    var res = eval(js.stdout);").append("\n")
            .append("    UIALogger.logDebug(res);").append("\n")
            .append("  } catch(err) {").append("\n")
            .append("    UIALogger.logError(err);").append("\n")
            .append("  }").append("\n")
            .append("  target.delay(1);").append("\n")
            .append("}");
        File js = File.createTempFile("instruments-", ".js");
        FileUtils.write(js, sb);
        LOG.debug("{}", sb);

        this.runServer(js);
    }

    public ExecuteWatchdog runServer(File javascript) throws IOException {
        CommandLine cmdLine = new CommandLine("instruments");
        cmdLine.addArgument("-t");
        cmdLine.addArgument("/Applications/Xcode.app/Contents/Applications/Instruments.app/Contents/PlugIns"
            + "/AutomationInstrument.xrplugin/Contents/Resources/Automation.tracetemplate");
        cmdLine.addArgument("-w");
        cmdLine.addArgument(this.getIosDevice().getUUID());
        cmdLine.addArgument("Xinkaishi");
        cmdLine.addArgument("-e");
        cmdLine.addArgument("UIASCRIPT");
        cmdLine.addArgument(javascript.getAbsolutePath());
        cmdLine.addArgument("-e");
        cmdLine.addArgument("UIARESULTSPATH");
        cmdLine.addArgument("/tmp/ui");
        LOG.debug("{}", cmdLine.toString());
        ExecuteWatchdog watchdog = new ExecuteWatchdog(Long.MAX_VALUE);
        Executor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(new AdbStreamToFileHandler(null));
        executor.execute(cmdLine, new DefaultExecuteResultHandler());
        return watchdog;
    }

    private class AdbStreamToFileHandler implements ExecuteStreamHandler {
        File output;

        AdbStreamToFileHandler(File output) {
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
                String line = "";
                while (line != null) {
                    LOG.debug(line);
                    line = bis.readLine();
                }

            } else {
                PrintWriter pw = new PrintWriter(this.output);
                LOG.debug("Log stdout to {}", this.output);
                String line = "";
                try {
                    while (line != null) {
                        pw.println(line);
                        pw.flush();
                        line = bis.readLine();
                    }
                } finally {
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
        d.init();
    }
}
