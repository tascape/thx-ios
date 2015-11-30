/*
 * Copyright 2015 tascape.
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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.FileUtils;
import org.libimobiledevice.ios.driver.binding.exceptions.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.martiansoftware.nailgun.NGServer;
import com.tascape.qa.th.SystemConfiguration;
import net.sf.lipermi.exception.LipeRMIException;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Server;
import com.tascape.qa.th.ios.comm.JavaScriptNail;
import com.tascape.qa.th.ios.model.DeviceOrientation;
import com.tascape.qa.th.ios.model.UIA;
import com.tascape.qa.th.ios.model.UIAAlert;
import com.tascape.qa.th.ios.model.UIAApplication;
import com.tascape.qa.th.ios.model.UIAElement;
import com.tascape.qa.th.ios.model.UIAException;
import com.tascape.qa.th.ios.model.UIATarget;
import com.tascape.qa.th.ios.model.UIAWindow;
import com.tascape.qa.th.libx.DefaultExecutor;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author linsong wang
 */
public class UiAutomationDevice extends LibIMobileDevice implements UIATarget, UIAApplication, JavaScriptServer,
    Observer {
    private static final Logger LOG = LoggerFactory.getLogger(UiAutomationDevice.class);

    public static final String SYSPROP_TIMEOUT_SECOND = "qa.th.driver.ios.TIMEOUT_SECOND";

    public static final String INSTRUMENTS_ERROR = "Error:";

    public static final String FAIL = "Fail: The target application appears to have died";

    public static final String TRACE_TEMPLATE = "/Applications/Xcode.app/Contents/Applications/Instruments.app/Contents"
        + "/PlugIns/AutomationInstrument.xrplugin/Contents/Resources/Automation.tracetemplate";

    public static final int JAVASCRIPT_TIMEOUT_SECOND
        = SystemConfiguration.getInstance().getIntProperty(SYSPROP_TIMEOUT_SECOND, 30);

    private final SynchronousQueue<String> javaScriptQueue = new SynchronousQueue<>();

    private final BlockingQueue<String> responseQueue = new ArrayBlockingQueue<>(5000);

    private int ngPort;

    private int rmiPort;

    private NGServer ngServer;

    private Server rmiServer;

    private ExecuteWatchdog instrumentsDog;

    private ESH instrumentsStreamHandler;

    public UiAutomationDevice(String uuid) throws SDKException {
        super(uuid);
    }

    public void start(String appName) throws Exception {
        LOG.info("Start server");
        ngServer = this.startNailGunServer();
        rmiServer = this.startRmiServer();
        instrumentsDog = this.startInstrumentsServer(appName);
        addInstrumentsStreamObserver(this);
        sendJavaScript("window.logElement();").forEach(l -> LOG.debug(l));
    }

    public void stop() {
        LOG.info("Stop server");
        if (ngServer != null) {
            ngServer.shutdown(false);
        }
        if (rmiServer != null) {
            rmiServer.close();
        }
        if (instrumentsDog != null) {
            instrumentsDog.stop();
            instrumentsDog.killedProcess();
        }
    }

    /**
     * Gets the screen size in points.
     * http://www.paintcodeapp.com/news/ultimate-guide-to-iphone-resolutions
     *
     * @return the screen size in points
     *
     * @throws UIAException in case of any issue
     */
    public Dimension getDisplaySize() throws UIAException {
        List<String> lines = this.sendJavaScript("window.logElement();");
        Dimension dimension = new Dimension();
        String line = lines.stream().filter((l) -> (l.startsWith("UIAWindow"))).findFirst().get();
        if (StringUtils.isNotEmpty(line)) {
            String s = line.split("\\{", 2)[1].replaceAll("\\{", "").replaceAll("\\}", "");
            String[] ds = s.split(",");
            dimension.setSize(Integer.parseInt(ds[2].trim()), Integer.parseInt(ds[3].trim()));
        }
        return dimension;
    }

    public List<String> logElementTree() throws UIAException {
        return this.sendJavaScript("window.logElementTree();");
    }

    /**
     * Checks if an element exists on current UI, based on element type.
     *
     * @param <T>        sub-class of UIAElement
     * @param javaScript such as "window.tabBars()['MainTabBar']"
     * @param type       type of uia element, such as UIATabBar
     *
     * @return true if element identified by javascript exists
     *
     * @throws UIAException in case of any issue
     */
    public <T extends UIAElement> boolean doesElementExist(String javaScript, Class<T> type) throws UIAException {
        return doesElementExist(javaScript, type, null);
    }

    /**
     * Checks if an element exists on current UI, based on element type and text.
     *
     * @param <T>        sub-class of UIAElement
     * @param javaScript the javascript that uniquely identify the element, such as "window.tabBars()['MainTabBar']",
     *                   or "window.elements()[1].buttons()[0]"
     * @param type       type of uia element, such as UIATabBar
     * @param text       text of an element, such as "MainTabBar"
     *
     * @return true if element identified by javascript exists
     *
     * @throws UIAException in case of any issue
     */
    public <T extends UIAElement> boolean doesElementExist(String javaScript, Class<T> type, String text) throws
        UIAException {
        String js = "var e = " + javaScript + "; e.logElement();";
        return sendJavaScript(js).stream()
            .filter(line -> line.contains(type.getSimpleName()))
            .filter(line -> StringUtils.isEmpty(text) ? true : line.contains(text))
            .findFirst().isPresent();
    }

    public List<String> sendJavaScript(String javaScript) throws UIAException {
        String reqId = UUID.randomUUID().toString();
        LOG.trace("sending js {}", javaScript);
        try {
            javaScriptQueue.offer("UIALogger.logMessage('" + reqId + " start');", JAVASCRIPT_TIMEOUT_SECOND,
                TimeUnit.SECONDS);
            javaScriptQueue.offer(javaScript, JAVASCRIPT_TIMEOUT_SECOND, TimeUnit.SECONDS);
            javaScriptQueue
                .offer("UIALogger.logMessage('" + reqId + " stop');", JAVASCRIPT_TIMEOUT_SECOND, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw new UIAException("Interrupted", ex);
        }
        while (true) {
            String res;
            try {
                res = this.responseQueue.poll(JAVASCRIPT_TIMEOUT_SECOND, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                throw new UIAException("Interrupted", ex);
            }
            if (res == null) {
                throw new UIAException("no response from device");
            }
            LOG.trace(res);
            if (res.contains(FAIL)) {
                throw new UIAException(res);
            }
            if (res.contains(reqId + " start")) {
                break;
            }
        }
        List<String> lines = new ArrayList<>();
        while (true) {
            String res;
            try {
                res = this.responseQueue.poll(JAVASCRIPT_TIMEOUT_SECOND, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                throw new UIAException("Interrupted", ex);
            }
            if (res == null) {
                throw new UIAException("no response from device");
            }
            LOG.trace(res);
            if (res.contains(FAIL)) {
                throw new UIAException(res);
            }
            if (res.contains(reqId + " start")) {
                continue;
            }
            if (res.contains(reqId + " stop")) {
                break;
            } else {
                lines.add(res);
            }
        }
        javaScriptQueue.clear();
        lines.forEach(l -> {
            if (l.contains(INSTRUMENTS_ERROR)) {
                LOG.warn(l);
            } else {
                LOG.debug(l);
            }
        });
        if (lines.stream().filter(l -> l.contains(INSTRUMENTS_ERROR)).findAny().isPresent()) {
            throw new UIAException("instruments error");
        }
        return lines;
    }

    @Override
    public String retrieveJavaScript() throws InterruptedException {
        String js = javaScriptQueue.take();
        LOG.trace("got js {}", js);
        return js;
    }

    public boolean addInstrumentsStreamObserver(Observer observer) {
        if (this.instrumentsStreamHandler != null) {
            this.instrumentsStreamHandler.addObserver(observer);
            return true;
        }
        return false;
    }

    @Override
    public void update(Observable o, Object arg) {
        String res = arg.toString();
        try {
            responseQueue.put(res);
        } catch (InterruptedException ex) {
            LOG.error("Cannot save instruments response");
        }
    }

    public UIAWindow mainWindow() throws UIAException {
        List<String> lines = logElementTree();
        return UIA.parseElementTree(lines);
    }

    @Override
    public void deactivateAppForDuration(int duration) throws UIAException {
        sendJavaScript("UIALogger.logMessage(target.deactivateAppForDuration (" + duration + "));");
    }

    @Override
    public String model() throws UIAException {
        return getMessage(sendJavaScript("UIALogger.logMessage(target.model());"));
    }

    @Override
    public String name() throws UIAException {
        return getMessage(sendJavaScript("UIALogger.logMessage(target.name());"));
    }

    @Override
    public Rectangle2D.Float rect() throws UIAException {
        List<String> lines = sendJavaScript("UIALogger.logMessage(target.rect());");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String systemName() throws UIAException {
        return getMessage(sendJavaScript("UIALogger.logMessage(target.systemName());"));
    }

    @Override
    public String systemVersion() throws UIAException {
        return getMessage(sendJavaScript("UIALogger.logMessage(target.systemVersion());"));
    }

    @Override
    public DeviceOrientation deviceOrientation() throws UIAException {
        sendJavaScript("UIALogger.logMessage(target.deviceOrientation());");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDeviceOrientation(DeviceOrientation orientation) throws UIAException {
        sendJavaScript("target.setDeviceOrientation(" + orientation.ordinal() + ");");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLocation(double latitude, double longitude) throws UIAException {
        sendJavaScript("target.setLocation({latitude:" + latitude + ", longitude:" + longitude + "});");
    }

    @Override
    public void clickVolumeDown() throws UIAException {
        this.sendJavaScript("target.clickVolumeDown();");
    }

    @Override
    public void clickVolumeUp() throws UIAException {
        this.sendJavaScript("target.clickVolumeUp();");
    }

    @Override
    public void holdVolumeDown(int duration) throws UIAException {
        this.sendJavaScript("target.holdVolumeDown(" + duration + ");");
    }

    @Override
    public void holdVolumeUp(int duration) throws UIAException {
        this.sendJavaScript("target.holdVolumeUp(" + duration + ");");
    }

    @Override
    public void lockForDuration(int duration) throws UIAException {
        this.sendJavaScript("target.lockForDuration(" + duration + ");");
    }

    @Override
    public void shake() throws UIAException {
        this.sendJavaScript("target.shake();");
    }

    @Override
    public void dragFromToForDuration(Point2D.Float from, Point2D.Float to, int duration) throws UIAException {
        this.sendJavaScript("target.dragFromToForDuration(" + toCGString(from) + ", "
            + toCGString(to) + ", " + duration + ");");
    }

    @Override
    public void dragFromToForDuration(String fromJavaScript, String toJavaScript, int duration) throws UIAException {
        this.sendJavaScript("var e1 = " + fromJavaScript + "; var e2 = " + toJavaScript + "; "
            + "target.dragFromToForDuration(e1, e2, " + duration + ");");
    }

    @Override
    public void doubleTap(float x, float y) throws UIAException {
        this.sendJavaScript("target.doubleTap(" + toCGString(x, y) + ");");
    }

    @Override
    public void doubleTap(String javaScript) throws UIAException {
        this.sendJavaScript("var e = " + javaScript + "; target.doubleTap(e);");
    }

    @Override
    public void flickFromTo(Point2D.Float from, Point2D.Float to, int duration) throws UIAException {
        this.sendJavaScript("target.flickFromTo(" + toCGString(from) + ", " + toCGString(to)
            + ", " + duration + ");");
    }

    @Override
    public void flickFromTo(String fromJavaScript, String toJavaScript, int duration) throws UIAException {
        this.sendJavaScript("var e1 = " + fromJavaScript + "; var e2 = " + toJavaScript + "; "
            + "target.flickFromTo(e1, e2, " + duration + ");");
    }

    @Override
    public void pinchCloseFromToForDuration(Point2D.Float from, Point2D.Float to, int duration) throws UIAException {
        this.sendJavaScript("target.pinchCloseFromToForDuration(" + toCGString(from) + ", "
            + toCGString(to) + ", " + duration + ");");
    }

    @Override
    public void pinchCloseFromToForDuration(String fromJavaScript, String toJavaScript, int duration) throws
        UIAException {
        this.sendJavaScript("var e1 = " + fromJavaScript + "; var e2 = " + toJavaScript + "; "
            + "target.pinchCloseFromToForDuration(e1, e2, " + duration + ");");
    }

    @Override
    public void pinchOpenFromToForDuration(Point2D.Float from, Point2D.Float to, int duration) throws UIAException {
        this.sendJavaScript("target.pinchOpenFromToForDuration(" + toCGString(from) + ", "
            + toCGString(to) + ", " + duration + ");");
    }

    @Override
    public void pinchOpenFromToForDuration(String fromJavaScript, String toJavaScript, int duration) throws UIAException {
        this.sendJavaScript("var e1 = " + fromJavaScript + "; var e2 = " + toJavaScript + "; "
            + "target.pinchOpenFromToForDuration(e1, e2, " + duration + ");");
    }

    @Override
    public void tap(float x, float y) throws UIAException {
        this.sendJavaScript("target.tap(" + toCGString(x, y) + ");");
    }

    @Override
    public void tap(String javaScript) throws UIAException {
        this.sendJavaScript("var e = " + javaScript + "; target.touchAndHold(e);");
    }

    @Override
    public void touchAndHold(Point2D.Float point, int duration) throws UIAException {
        this.sendJavaScript("target.touchAndHold(" + toCGString(point) + ", " + duration + ");");
    }

    @Override
    public void touchAndHold(String javaScript, int duration) throws UIAException {
        this.sendJavaScript("var e = " + javaScript + "; target.touchAndHold(e, " + duration + ");");
    }

    @Override
    public void popTimeout() throws UIAException {
        this.sendJavaScript("target.popTimeout();");
    }

    @Override
    public void pushTimeout(int timeoutValue) throws UIAException {
        this.sendJavaScript("target.pushTimeout(" + timeoutValue + ");");
    }

    @Override
    public void setTimeout(int timeout) throws UIAException {
        this.sendJavaScript("target.setTimeout(" + timeout + ");");
    }

    @Override
    public int timeout() throws UIAException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void delay(int timeInterval) throws UIAException {
        this.sendJavaScript("target.delay(" + timeInterval + ");");
    }

    @Override
    public boolean onAlert(UIAAlert alert) throws UIAException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private NGServer startNailGunServer() throws InterruptedException {
        NGServer ngs = new NGServer(null, 0);
        new Thread(ngs).start();
        Thread.sleep(1000);
        this.ngPort = ngs.getPort();
        LOG.trace("ng port {}", this.ngPort);
        return ngs;
    }

    private Server startRmiServer() throws IOException, LipeRMIException {
        Server rmis = new Server();
        CallHandler callHandler = new CallHandler();
        this.rmiPort = 8000;
        while (true) {
            try {
                rmis.bind(rmiPort, callHandler);
                break;
            } catch (IOException ex) {
                LOG.trace("rmi port {} - {}", this.rmiPort, ex.getMessage());
                this.rmiPort += 7;
            }
        }
        LOG.trace("rmi port {}", this.rmiPort);
        callHandler.registerGlobal(JavaScriptServer.class, this);
        return rmis;
    }

    private ExecuteWatchdog startInstrumentsServer(String appName) throws IOException {
        StringBuilder sb = new StringBuilder()
            .append("while (1) {\n")
            .append("  var target = UIATarget.localTarget();\n")
            .append("  var host = target.host();\n")
            .append("  var app = target.frontMostApp();\n")
            .append("  var window = app.mainWindow();\n")
            .append("  var js = host.performTaskWithPathArgumentsTimeout('").append(JavaScriptNail.NG_CLIENT)
            .append("', ['--nailgun-port', '").append(ngPort).append("', '").append(JavaScriptNail.class.getName())
            .append("', '").append(rmiPort).append("'], 10000);\n")
            .append("  UIALogger.logDebug(js.stdout);\n")
            .append("  try {\n")
            .append("    var res = eval(js.stdout);\n")
            .append("  } catch(err) {\n")
            .append("    UIALogger.logError(err);\n")
            .append("  }\n")
            .append("}\n");
        File js = File.createTempFile("instruments-", ".js");
        FileUtils.write(js, sb);
        LOG.trace("{}\n{}", js, sb);

        CommandLine cmdLine = new CommandLine("instruments");
        cmdLine.addArgument("-t");
        cmdLine.addArgument(TRACE_TEMPLATE);
        cmdLine.addArgument("-w");
        cmdLine.addArgument(this.getIosDevice().getUUID());
        cmdLine.addArgument(appName);
        cmdLine.addArgument("-e");
        cmdLine.addArgument("UIASCRIPT");
        cmdLine.addArgument(js.getAbsolutePath());
        cmdLine.addArgument("-e");
        cmdLine.addArgument("UIARESULTSPATH");
        cmdLine.addArgument(Paths.get(System.getProperty("java.io.tmpdir")).toFile().getAbsolutePath());
        LOG.trace("{}", cmdLine.toString());
        ExecuteWatchdog watchdog = new ExecuteWatchdog(Long.MAX_VALUE);
        Executor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        instrumentsStreamHandler = new ESH();
        instrumentsStreamHandler.addObserver(this);
        executor.setStreamHandler(instrumentsStreamHandler);
        executor.execute(cmdLine, new DefaultExecuteResultHandler());
        return watchdog;
    }

    private class ESH extends Observable implements ExecuteStreamHandler {
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
                this.notifyObserversX("ERROR " + line);
            } while (true);
        }

        @Override
        public void setProcessOutputStream(InputStream in) throws IOException {
            BufferedReader bis = new BufferedReader(new InputStreamReader(in));
            while (true) {
                String line = bis.readLine();
                if (line == null) {
                    break;
                }
                LOG.trace(line);
                this.notifyObserversX(line);
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

        private void notifyObserversX(String line) {
            this.setChanged();
            this.notifyObservers(line);
            this.clearChanged();
        }
    }

    private String getMessage(List<String> lines) {
        String line = lines.stream().filter(l -> StringUtils.contains(l, "Default:")).findFirst().get();
        return line.substring(line.indexOf("Default: ") + 9);
    }

    public static void main(String[] args) throws SDKException {
        UiAutomationDevice d = new UiAutomationDevice(LibIMobileDevice.getAllUuids().get(0));
        try {
            d.start("Jumblify");

            LOG.debug("model {}", d.model());
        } catch (Throwable t) {
            LOG.error("", t);
        } finally {
            d.stop();
            System.exit(0);
        }
    }
}
