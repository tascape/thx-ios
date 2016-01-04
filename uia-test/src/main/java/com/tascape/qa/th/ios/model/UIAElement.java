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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
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

    private final List<UIAElement> elements = new ArrayList<>();

    private UIAElement parent;

    private Instruments instruments;

    public int index() {
        return index;
    }

    public String name() {
        return name;
    }

    public Rectangle2D.Float rect() {
        return rect;
    }

    public UIAElement[] elements() {
        return elements.toArray(new UIAElement[0]);
    }

    public UIATextField[] textFields() {
        List<UIATextField> es = new ArrayList<>();
        elements.stream()
            .filter((e) -> (e instanceof UIATextField))
            .forEach((e) -> {
                es.add(UIATextField.class.cast(e));
            });
        return es.toArray(new UIATextField[0]);
    }

    public UIAElement parent() {
        return parent;
    }

    public String value() throws UIAException {
        String js = "var e = " + toJavaScript() + "; UIALogger.logMessage(e.value());";
        return Instruments.getLogMessage(instruments.runJavaScript(js));
    }

    public void tap() throws UIAException {
        instruments.runJavaScript(toJavaScript() + ".tap()");
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
    }

    void addElement(UIAElement element) {
        element.setIndex(elements.size());
        element.setParent(this);
        elements.add(element);
    }

    void setParent(UIAElement parent) {
        this.parent = parent;
    }
}
