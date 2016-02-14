/*
 * Copyright 2016 tascape.
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
package com.tascape.qa.th.ios.model;

import com.tascape.qa.th.ios.comm.Instruments;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author linsong wang
 */
public class UIAElement {

    private int index = 0;

    private String name;

    private Rectangle2D.Float rect;

    private Point2D.Float center;

    private final List<UIAElement> elements = new ArrayList<>();

    private UIAElement parent;

    private Instruments instruments;

    public int index() {
        return index;
    }

    public String name() {
        return name;
    }

    public Point2D.Float hitpoint() throws UIAException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Rectangle2D.Float rect() {
        return rect;
    }

    public UIAButton[] buttons() {
        return elements.stream().filter(e -> e instanceof UIAButton).map(e -> (UIAButton) e)
            .collect(Collectors.toList()).toArray(new UIAButton[0]);
    }

    public UIACollectionView[] collectionViews() {
        return elements.stream().filter(e -> e instanceof UIACollectionView).map(e -> (UIACollectionView) e)
            .collect(Collectors.toList()).toArray(new UIACollectionView[0]);
    }

    public UIAElement[] elements() {
        return elements.toArray(new UIAElement[0]);
    }

    public UIAImage[] images() {
        return elements.stream().filter(e -> e instanceof UIAImage).map(e -> (UIAImage) e)
            .collect(Collectors.toList()).toArray(new UIAImage[0]);
    }

    public UIALink[] links() {
        return elements.stream().filter(e -> e instanceof UIALink).map(e -> (UIALink) e)
            .collect(Collectors.toList()).toArray(new UIALink[0]);
    }

    public UIAElement navigationBar() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public UIANavigationBar[] navigationBars() {
        return elements.stream().filter(e -> e instanceof UIANavigationBar).map(e -> (UIANavigationBar) e)
            .collect(Collectors.toList()).toArray(new UIANavigationBar[0]);
    }

    public UIAPageIndicator[] pageIndicators() {
        return elements.stream().filter(e -> e instanceof UIAPageIndicator).map(e -> (UIAPageIndicator) e)
            .collect(Collectors.toList()).toArray(new UIAPageIndicator[0]);
    }

    public UIAElement parent() {
        return parent;
    }

    public UIAPicker[] pickers() {
        return elements.stream().filter(e -> e instanceof UIAPicker).map(e -> (UIAPicker) e)
            .collect(Collectors.toList()).toArray(new UIAPicker[0]);
    }

    public UIAPopover popover() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public UIAProgressIndicator[] progressIndicators() {
        return elements.stream().filter(e -> e instanceof UIAProgressIndicator).map(e -> (UIAProgressIndicator) e)
            .collect(Collectors.toList()).toArray(new UIAProgressIndicator[0]);
    }

    public UIAScrollView[] scrollViews() {
        return elements.stream().filter(e -> e instanceof UIAScrollView).map(e -> (UIAScrollView) e)
            .collect(Collectors.toList()).toArray(new UIAScrollView[0]);
    }

    public UIASearchBar[] searchBars() {
        return elements.stream().filter(e -> e instanceof UIASearchBar).map(e -> (UIASearchBar) e)
            .collect(Collectors.toList()).toArray(new UIASearchBar[0]);
    }

    public UIASecureTextField[] secureTextFields() {
        return elements.stream().filter(e -> e instanceof UIASecureTextField).map(e -> (UIASecureTextField) e)
            .collect(Collectors.toList()).toArray(new UIASecureTextField[0]);
    }

    public UIASegmentedControl[] segmentedControls() {
        return elements.stream().filter(e -> e instanceof UIASegmentedControl).map(e -> (UIASegmentedControl) e)
            .collect(Collectors.toList()).toArray(new UIASegmentedControl[0]);
    }

    public UIASlider[] sliders() {
        return elements.stream().filter(e -> e instanceof UIASlider).map(e -> (UIASlider) e)
            .collect(Collectors.toList()).toArray(new UIASlider[0]);
    }

    public UIAStaticText[] staticTexts() {
        return elements.stream().filter(e -> e instanceof UIAStaticText).map(e -> (UIAStaticText) e)
            .collect(Collectors.toList()).toArray(new UIAStaticText[0]);
    }

    public UIASwitch[] switches() {
        return elements.stream().filter(e -> e instanceof UIASwitch).map(e -> (UIASwitch) e)
            .collect(Collectors.toList()).toArray(new UIASwitch[0]);
    }

    public UIAElement tabBar() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public UIATabBar[] tabBars() {
        return elements.stream().filter(e -> e instanceof UIATabBar).map(e -> (UIATabBar) e)
            .collect(Collectors.toList()).toArray(new UIATabBar[0]);
    }

    public UIATableView[] tableViews() {
        return elements.stream().filter(e -> e instanceof UIATableView).map(e -> (UIATableView) e)
            .collect(Collectors.toList()).toArray(new UIATableView[0]);
    }

    public UIATableCell[] cells() {
        return elements.stream().filter(e -> e instanceof UIATableCell).map(e -> (UIATableCell) e)
            .collect(Collectors.toList()).toArray(new UIATableCell[0]);
    }

    public UIATextField[] textFields() {
        return elements.stream().filter(e -> e instanceof UIATextField).map(e -> (UIATextField) e)
            .collect(Collectors.toList()).toArray(new UIATextField[0]);
    }

    public UIATextView[] textViews() {
        return elements.stream().filter(e -> e instanceof UIATextView).map(e -> (UIATextView) e)
            .collect(Collectors.toList()).toArray(new UIATextView[0]);
    }

    public UIAElement toolbar() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public UIAToolbar[] toolbars() {
        return elements.stream().filter(e -> e instanceof UIAToolbar).map(e -> (UIAToolbar) e)
            .collect(Collectors.toList()).toArray(new UIAToolbar[0]);
    }

    public UIAWebView[] webViews() {
        return elements.stream().filter(e -> e instanceof UIAWebView).map(e -> (UIAWebView) e)
            .collect(Collectors.toList()).toArray(new UIAWebView[0]);
    }

    public void doubleTap() throws UIAException {
        instruments.runJavaScript(toJavaScript() + ".doubleTap();");
    }

    public void scrollToVisible() throws UIAException {
        instruments.runJavaScript(toJavaScript() + ".scrollToVisible();");
    }

    public void touchAndHold(int duration) throws UIAException {
        instruments.runJavaScript(toJavaScript() + ".touchAndHold(" + duration + ");");
    }

    public void twoFingerTap() throws UIAException {
        instruments.runJavaScript(toJavaScript() + ".twoFingerTap();");
    }

    public boolean checkIsValid() throws UIAException {
        String v = Instruments.getLogMessage(instruments.runJavaScript("var e = " + toJavaScript()
            + "; UIALogger.logMessage(e.checkIsValid() + '');"));
        if (null != v) {
            switch (v) {
                case "true":
                    return true;
                case "false":
                    return false;
            }
        }
        throw new UIAException("Unknown status of " + v);
    }

    public int hasKeyboardFocus() throws UIAException {
        String v = Instruments.getLogMessage(instruments.runJavaScript("var e = " + toJavaScript()
            + "; UIALogger.logMessage(e.hasKeyboardFocus() + '');"));
        if (null != v) {
            switch (v) {
                case "1":
                    return 1;
                case "0":
                    return 0;
            }
        }
        throw new UIAException("Unknown status of " + v);
    }

    public int isEnabled() throws UIAException {
        String v = Instruments.getLogMessage(instruments.runJavaScript("var e = " + toJavaScript()
            + "; UIALogger.logMessage(e.isEnabled() + '');"));
        if (null != v) {
            switch (v) {
                case "1":
                    return 1;
                case "0":
                    return 0;
            }
        }
        throw new UIAException("Unknown status of " + v);
    }

    public boolean isValid() throws UIAException {
        String v = Instruments.getLogMessage(instruments.runJavaScript("var e = " + toJavaScript()
            + "; UIALogger.logMessage(e.isValid() + '');"));
        if (null != v) {
            switch (v) {
                case "true":
                    return true;
                case "false":
                    return false;
            }
        }
        throw new UIAException("Unknown status of " + v);
    }

    public int isVisible() throws UIAException {
        String v = Instruments.getLogMessage(instruments.runJavaScript("var e = " + toJavaScript()
            + "; UIALogger.logMessage(e.isVisible() + '');"));
        if (null != v) {
            switch (v) {
                case "1":
                    return 1;
                case "0":
                    return 0;
            }
        }
        throw new UIAException("Unknown status of " + v);
    }

    public boolean waitForInvalid() throws UIAException {
        String v = Instruments.getLogMessage(instruments.runJavaScript("var e = " + toJavaScript()
            + "; UIALogger.logMessage(e.waitForInvalid() + '');"));
        if (null != v) {
            switch (v) {
                case "true":
                    return true;
                case "false":
                    return false;
            }
        }
        throw new UIAException("Unknown status of " + v);
    }

    public String label() throws UIAException {
        String js = "var e = " + toJavaScript() + "; UIALogger.logMessage(e.label());";
        return Instruments.getLogMessage(instruments.runJavaScript(js));
    }

    public String value() throws UIAException {
        String js = "var e = " + toJavaScript() + "; UIALogger.logMessage(e.value());";
        return Instruments.getLogMessage(instruments.runJavaScript(js));
    }

    public UIAElement withName(String name) {
        return elements.stream().filter(e -> name.equals(e.name())).findFirst().orElse((UIAElement) null);
    }

    public void tap() throws UIAException {
        instruments.runJavaScript(toJavaScript() + ".tap()");
    }

    /**
     * Taps on screen at element's center coordinates.
     *
     * @throws UIAException in case of Instruments error
     */
    public void tapOn() throws UIAException {
        instruments.runJavaScript("target.tap({x:" + center.x + ", y:" + center.y + "})");
    }

    public void drag(float x, float y) throws UIAException {
        this.drag(x, y, 1);
    }

    public void drag(float x, float y, int duration) throws UIAException {
        Point2D.Float end = new Point2D.Float(center.x + x, center.y + y);
        instruments.runJavaScript("target.dragFromToForDuration(" + toCGString(center) + ", " + toCGString(end)
            + ", " + duration + ");");
    }

    public String toJavaScript() {
        List<String> list = new ArrayList<>();
        list.add(0, "elements()[" + index + "]");
        UIAElement element = this.parent();
        while (!(element instanceof UIAWindow)) {
            list.add(0, "elements()[" + element.index() + "]");
            element = element.parent();
        }
        list.add(0, "window");
        return StringUtils.join(list, ".");
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject().put(this.getClass().getSimpleName(), new JSONObject()
            .put("index", index)
            .put("name", name)
            .put("x", rect.x)
            .put("y", rect.y)
            .put("w", rect.width)
            .put("h", rect.height));
        if (!elements.isEmpty()) {
            JSONArray jarr = new JSONArray();
            json.put("elements", jarr);
            elements.forEach(e -> {
                jarr.put(e.toJson());
            });
        }
        return json;
    }

    public List<String> logElement() {
        List<String> lines = new ArrayList<>();
        lines.add(String.format("%s %d \"%s\" [x=%s,y=%s,w=%s,h=%s]", getClass().getSimpleName(), index, name,
            rect.x, rect.y, rect.width, rect.height));
        if (!elements.isEmpty()) {
            lines.add("elements: (" + elements.size() + ") {");
            elements.forEach((e) -> {
                e.logElement().forEach((l) -> {
                    lines.add("    " + l);
                });
            });
            lines.add("}");
        }
        return lines;
    }

    @Override
    public String toString() {
        return StringUtils.join(logElement(), "\n");
    }

    Instruments getInstruments() {
        return instruments;
    }

    void setInstruments(Instruments instruments) {
        this.instruments = instruments;
        this.elements.forEach((UIAElement e) -> e.setInstruments(instruments));
    }

    <T extends UIAElement> T findElement(Class<T> type, String name) {
        if (type.equals(this.getClass()) && this.name().equals(name)) {
            return type.cast(this);
        }
        for (UIAElement element : elements) {
            UIAElement e = element.findElement(type, name);
            if (e != null) {
                return type.cast(e);
            }
        }
        return type.cast(null);
    }

    void setIndex(int index) {
        this.index = index;
    }

    void setName(String name) {
        this.name = name;
    }

    void setRect(Rectangle2D.Float rect) {
        this.rect = rect;
        this.center = new Point2D.Float(rect.x + rect.width / 2, rect.y + rect.height / 2);
    }

    void addElement(UIAElement element) {
        element.setIndex(elements.size());
        element.setParent(this);
        elements.add(element);
    }

    void setParent(UIAElement parent) {
        this.parent = parent;
    }

    String toCGString(Point2D.Float point) {
        return String.format("{x:%f, y:%f}", point.x, point.y);
    }
}
