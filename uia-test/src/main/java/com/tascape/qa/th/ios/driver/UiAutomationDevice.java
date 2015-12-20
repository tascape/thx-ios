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

import com.tascape.qa.th.ios.model.UIA;
import org.libimobiledevice.ios.driver.binding.exceptions.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tascape.qa.th.SystemConfiguration;
import com.tascape.qa.th.Utils;
import com.tascape.qa.th.ios.comm.Instruments;
import com.tascape.qa.th.ios.model.DeviceOrientation;
import com.tascape.qa.th.ios.model.UIAAlert;
import com.tascape.qa.th.ios.model.UIAApplication;
import com.tascape.qa.th.ios.model.UIAElement;
import com.tascape.qa.th.ios.model.UIAException;
import com.tascape.qa.th.ios.model.UIATarget;
import com.tascape.qa.th.ios.model.UIAWindow;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author linsong wang
 */
public class UiAutomationDevice extends LibIMobileDevice implements UIATarget, UIAApplication {
    private static final Logger LOG = LoggerFactory.getLogger(UiAutomationDevice.class);

    public static final String SYSPROP_TIMEOUT_SECOND = "qa.th.comm.ios.TIMEOUT_SECOND";

    public static final String INSTRUMENTS_ERROR = "Error:";

    public static final String INSTRUMENTS_FAIL = "Fail: The target application appears to have died";

    public static final String TRACE_TEMPLATE = "/Applications/Xcode.app/Contents/Applications/Instruments.app/Contents"
        + "/PlugIns/AutomationInstrument.xrplugin/Contents/Resources/Automation.tracetemplate";

    public static final int TIMEOUT_SECOND
        = SystemConfiguration.getInstance().getIntProperty(SYSPROP_TIMEOUT_SECOND, 30);

    private Instruments instruments;

    private Dimension screenDimension;

    public UiAutomationDevice() throws SDKException {
        this(LibIMobileDevice.getAllUuids().get(0));
    }

    public UiAutomationDevice(String uuid) throws SDKException {
        super(uuid);
    }

    public void start(String appName, int delayMillis) throws Exception {
        LOG.info("Start app {} on {}", appName, this.getUuid());
        if (instruments != null) {
            instruments.disconnect();
        }
        instruments = new Instruments(getUuid(), appName);
        instruments.connect();
        Utils.sleep(delayMillis, "Wait for app to start");
        instruments.runJavaScript("window.logElement();").forEach(l -> LOG.debug(l));
    }

    public void stop() {
        if (instruments != null) {
            instruments.disconnect();
        }
    }

    public List<String> runJavaScript(String javaScript) throws UIAException {
        return instruments.runJavaScript(javaScript);
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
        if (screenDimension == null) {
            screenDimension = loadDisplaySize();
        }
        return screenDimension;
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
     * Checks if an element exists on current UI, based on element type and name.
     *
     * @param <T>        sub-class of UIAElement
     * @param javaScript the javascript that uniquely identify the element, such as "window.tabBars()['MainTabBar']",
     *                   or "window.elements()[1].buttons()[0]"
     * @param type       type of uia element, such as UIATabBar
     * @param name       name of an element, such as "MainTabBar"
     *
     * @return true if element identified by javascript exists
     *
     * @throws UIAException in case of any issue
     */
    public <T extends UIAElement> boolean doesElementExist(String javaScript, Class<T> type, String name) throws
        UIAException {
        String js = "var e = " + javaScript + "; e.logElement();";
        return instruments.runJavaScript(js).stream()
            .filter(line -> line.contains(type.getSimpleName()))
            .filter(line -> StringUtils.isEmpty(name) ? true : line.contains(name))
            .findFirst().isPresent();
    }

    /**
     * Checks if an element exists on current UI, based on element type and name. This method loads full element tree.
     *
     * @param <T>  sub-class of UIAElement
     * @param type type of uia element, such as UIATabBar
     * @param name name of an element, such as "MainTabBar"
     *
     * @return true if element identified by type and name exists, or false if timeout
     *
     * @throws UIAException in case of any issue
     */
    public <T extends UIAElement> boolean doesElementExist(Class<T> type, String name) throws UIAException {
        return mainWindow().findElement(type, name) != null;
    }

    /**
     * Waits for an element exists on current UI, based on element type.
     *
     * @param <T>        sub-class of UIAElement
     * @param javaScript the javascript that uniquely identify the element, such as "window.tabBars()['MainTabBar']",
     *                   or "window.elements()[1].buttons()[0]"
     * @param type       type of uia element, such as UIATabBar
     *
     * @return true if element identified by javascript exists, or false if timeout
     *
     * @throws UIAException                   in case of UIA issue
     * @throws java.lang.InterruptedException in case of interruption
     */
    public <T extends UIAElement> boolean waitForElement(String javaScript, Class<T> type)
        throws UIAException, InterruptedException {
        return this.waitForElement(javaScript, type, null);
    }

    /**
     * Waits for an element exists on current UI, based on element type and name.
     *
     * @param <T>        sub-class of UIAElement
     * @param javaScript the javascript that uniquely identify the element, such as "window.tabBars()['MainTabBar']",
     *                   or "window.elements()[1].buttons()[0]"
     * @param type       type of uia element, such as UIATabBar
     * @param name       name of an element, such as "MainTabBar"
     *
     * @return true if element identified by javascript exists, or false if timeout
     *
     * @throws UIAException                   in case of UIA issue
     * @throws java.lang.InterruptedException in case of interruption
     */
    public <T extends UIAElement> boolean waitForElement(String javaScript, Class<T> type, String name)
        throws UIAException, InterruptedException {
        long end = System.currentTimeMillis() + TIMEOUT_SECOND * 1000;
        while (System.currentTimeMillis() < end) {
            if (doesElementExist(javaScript, type, name)) {
                return true;
            }
            Utils.sleep(1000, "wait for " + type + "[" + name + "]");
        }
        return false;
    }

    /**
     * Waits for an element exists on current UI, based on element type and name. This method loads full element tree.
     *
     * @param <T>  sub-class of UIAElement
     * @param type type of uia element, such as UIATabBar
     * @param name name of an element, such as "MainTabBar"
     *
     * @return true if element identified by type and name exists, or false if timeout
     *
     * @throws UIAException                   in case of UIA issue
     * @throws java.lang.InterruptedException in case of interruption
     */
    public <T extends UIAElement> boolean waitForElement(Class<T> type, String name)
        throws UIAException, InterruptedException {
        long end = System.currentTimeMillis() + TIMEOUT_SECOND * 1000;
        while (System.currentTimeMillis() < end) {
            if (mainWindow().findElement(type, name) != null) {
                return true;
            }
            Utils.sleep(1000, "wait for " + type + "[" + name + "]");
        }
        return false;
    }

    public <T extends UIAElement> String getElementName(String javaScript, Class<T> type) throws UIAException {
        String js = "var e = " + javaScript + "; e.logElement();";
        String line = instruments.runJavaScript(js).stream().filter(l -> l.contains(type.getSimpleName())).findFirst()
            .get();
        return UIA.parseElement(line).name();
    }

    @Override
    public UIAWindow mainWindow() throws UIAException {
        List<String> lines = instruments.loadElementTree();
        return UIA.parseElementTree(lines);
    }

    @Override
    public void deactivateAppForDuration(int duration) throws UIAException {
        instruments.runJavaScript("UIALogger.logMessage(target.deactivateAppForDuration (" + duration + "));");
    }

    @Override
    public String model() throws UIAException {
        return Instruments.getLogMessage(instruments.runJavaScript("UIALogger.logMessage(target.model());"));
    }

    @Override
    public String name() throws UIAException {
        return Instruments.getLogMessage(instruments.runJavaScript("UIALogger.logMessage(target.name());"));
    }

    @Override
    public Rectangle2D.Float rect() throws UIAException {
        List<String> lines = instruments.runJavaScript("UIALogger.logMessage(target.rect());");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String systemName() throws UIAException {
        return Instruments.getLogMessage(instruments.runJavaScript("UIALogger.logMessage(target.systemName());"));
    }

    @Override
    public String systemVersion() throws UIAException {
        return Instruments.getLogMessage(instruments.runJavaScript("UIALogger.logMessage(target.systemVersion());"));
    }

    @Override
    public DeviceOrientation deviceOrientation() throws UIAException {
        instruments.runJavaScript("UIALogger.logMessage(target.deviceOrientation());");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDeviceOrientation(DeviceOrientation orientation) throws UIAException {
        instruments.runJavaScript("target.setDeviceOrientation(" + orientation.ordinal() + ");");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLocation(double latitude, double longitude) throws UIAException {
        instruments.runJavaScript("target.setLocation({latitude:" + latitude + ", longitude:" + longitude + "});");
    }

    @Override
    public void clickVolumeDown() throws UIAException {
        this.instruments.runJavaScript("target.clickVolumeDown();");
    }

    @Override
    public void clickVolumeUp() throws UIAException {
        this.instruments.runJavaScript("target.clickVolumeUp();");
    }

    @Override
    public void holdVolumeDown(int duration) throws UIAException {
        this.instruments.runJavaScript("target.holdVolumeDown(" + duration + ");");
    }

    @Override
    public void holdVolumeUp(int duration) throws UIAException {
        this.instruments.runJavaScript("target.holdVolumeUp(" + duration + ");");
    }

    @Override
    public void lockForDuration(int duration) throws UIAException {
        this.instruments.runJavaScript("target.lockForDuration(" + duration + ");");
    }

    @Override
    public void shake() throws UIAException {
        this.instruments.runJavaScript("target.shake();");
    }

    public void dragHalfScreenUp() throws UIAException {
        Dimension dimension = this.getDisplaySize();
        this.dragFromToForDuration(new Point2D.Float(dimension.width / 2, dimension.height / 2),
            new Point2D.Float(dimension.width / 2, 0), 1);
    }

    public void dragHalfScreenDown() throws UIAException {
        Dimension dimension = this.getDisplaySize();
        this.dragFromToForDuration(new Point2D.Float(dimension.width / 2, dimension.height / 2),
            new Point2D.Float(dimension.width / 2, dimension.height), 1);
    }

    @Override
    public void dragFromToForDuration(Point2D.Float from, Point2D.Float to, int duration) throws UIAException {
        this.instruments.runJavaScript("target.dragFromToForDuration(" + toCGString(from) + ", "
            + toCGString(to) + ", " + duration + ");");
    }

    @Override
    public void dragFromToForDuration(UIAElement fromElement, UIAElement toElement, int duration) throws UIAException {
        this.dragFromToForDuration(fromElement.toJavaScript(), toElement.toJavaScript(), duration);
    }

    @Override
    public void dragFromToForDuration(String fromJavaScript, String toJavaScript, int duration) throws UIAException {
        this.instruments.runJavaScript("var e1 = " + fromJavaScript + "; var e2 = " + toJavaScript + "; "
            + "target.dragFromToForDuration(e1, e2, " + duration + ");");
    }

    @Override
    public void doubleTap(float x, float y) throws UIAException {
        this.instruments.runJavaScript("target.doubleTap(" + toCGString(x, y) + ");");
    }

    @Override
    public void doubleTap(UIAElement element) throws UIAException {
        this.doubleTap(element.toJavaScript());
    }

    @Override
    public void doubleTap(String javaScript) throws UIAException {
        this.instruments.runJavaScript("var e = " + javaScript + "; e.doubleTap();");
    }

    @Override
    public void flickFromTo(Point2D.Float from, Point2D.Float to, int duration) throws UIAException {
        this.instruments.runJavaScript(
            "target.flickFromTo(" + toCGString(from) + ", " + toCGString(to) + ", " + duration + ");");
    }

    @Override
    public void flickFromTo(UIAElement fromElement, UIAElement toElement, int duration) throws UIAException {
        this.flickFromTo(fromElement.toJavaScript(), toElement.toJavaScript(), duration);
    }

    @Override
    public void flickFromTo(String fromJavaScript, String toJavaScript, int duration) throws UIAException {
        this.instruments.runJavaScript("var e1 = " + fromJavaScript + "; var e2 = " + toJavaScript + "; "
            + "target.flickFromTo(e1, e2, " + duration + ");");
    }

    @Override
    public void pinchCloseFromToForDuration(Point2D.Float from, Point2D.Float to, int duration) throws UIAException {
        this.instruments.runJavaScript("target.pinchCloseFromToForDuration(" + toCGString(from) + ", "
            + toCGString(to) + ", " + duration + ");");
    }

    @Override
    public void pinchCloseFromToForDuration(UIAElement fromElement, UIAElement toElement, int duration) throws
        UIAException {
        this.pinchCloseFromToForDuration(fromElement.toJavaScript(), toElement.toJavaScript(), duration);
    }

    @Override
    public void pinchCloseFromToForDuration(String fromJavaScript, String toJavaScript, int duration) throws
        UIAException {
        this.instruments.runJavaScript("var e1 = " + fromJavaScript + "; var e2 = " + toJavaScript + "; "
            + "target.pinchCloseFromToForDuration(e1, e2, " + duration + ");");
    }

    @Override
    public void pinchOpenFromToForDuration(Point2D.Float from, Point2D.Float to, int duration) throws UIAException {
        this.instruments.runJavaScript("target.pinchOpenFromToForDuration(" + toCGString(from) + ", "
            + toCGString(to) + ", " + duration + ");");
    }

    @Override
    public void pinchOpenFromToForDuration(UIAElement fromElement, UIAElement toElement, int duration) throws
        UIAException {
        this.pinchOpenFromToForDuration(fromElement.toJavaScript(), toElement.toJavaScript(), duration);
    }

    @Override
    public void pinchOpenFromToForDuration(String fromJavaScript, String toJavaScript, int duration) throws UIAException {
        this.instruments.runJavaScript("var e1 = " + fromJavaScript + "; var e2 = " + toJavaScript + "; "
            + "target.pinchOpenFromToForDuration(e1, e2, " + duration + ");");
    }

    @Override
    public void tap(float x, float y) throws UIAException {
        this.instruments.runJavaScript("target.tap(" + toCGString(x, y) + ");");
    }

    @Override
    public void tap(UIAElement element) throws UIAException {
        this.tap(element.toJavaScript());
    }

    @Override
    public void tap(String javaScript) throws UIAException {
        this.instruments.runJavaScript("var e = " + javaScript + "; e.tap();");
    }

    @Override
    public void touchAndHold(Point2D.Float point, int duration) throws UIAException {
        this.instruments.runJavaScript("target.touchAndHold(" + toCGString(point) + ", " + duration + ");");
    }

    @Override
    public void touchAndHold(UIAElement element, int duration) throws UIAException {
        this.instruments.runJavaScript("var e = " + element.toJavaScript() + "; e.touchAndHold(e, " + duration + ");");
    }

    @Override
    public void touchAndHold(String javaScript, int duration) throws UIAException {
        this.instruments.runJavaScript("var e = " + javaScript + "; target.touchAndHold(e, " + duration + ");");
    }

    @Override
    public void popTimeout() throws UIAException {
        this.instruments.runJavaScript("target.popTimeout();");
    }

    @Override
    public void pushTimeout(int timeoutValue) throws UIAException {
        this.instruments.runJavaScript("target.pushTimeout(" + timeoutValue + ");");
    }

    @Override
    public void setTimeout(int timeout) throws UIAException {
        this.instruments.runJavaScript("target.setTimeout(" + timeout + ");");
    }

    @Override
    public int timeout() throws UIAException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void delay(int timeInterval) throws UIAException {
        this.instruments.runJavaScript("target.delay(" + timeInterval + ");");
    }

    @Override
    public boolean onAlert(UIAAlert alert) throws UIAException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void logElementTree() throws UIAException {
        mainWindow().logElement().forEach(l -> LOG.debug(l));
    }

    public Instruments getInstruments() {
        return instruments;
    }

    private Dimension loadDisplaySize() throws UIAException {
        List<String> lines = this.instruments.runJavaScript("window.logElement();");
        Dimension dimension = new Dimension();
        String line = lines.stream().filter((l) -> (l.startsWith("UIAWindow"))).findFirst().get();
        if (StringUtils.isNotEmpty(line)) {
            String s = line.split("\\{", 2)[1].replaceAll("\\{", "").replaceAll("\\}", "");
            String[] ds = s.split(",");
            dimension.setSize(Integer.parseInt(ds[2].trim()), Integer.parseInt(ds[3].trim()));
        }
        return dimension;
    }

    public static void main(String[] args) throws SDKException {
        UiAutomationDevice d = new UiAutomationDevice();
        try {
            d.start("Movies", 5000);

            LOG.debug("model {}", d.model());
        } catch (Throwable t) {
            LOG.error("", t);
        } finally {
            d.stop();
            System.exit(0);
        }
    }
}
