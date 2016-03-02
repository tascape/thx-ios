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

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class UIA {
    private static final Logger LOG = LoggerFactory.getLogger(UIA.class);

    private static final Pattern PATTERN_UIA = Pattern.compile("(UIA.+?) \"(.+?)\" (\\{\\{.+?\\}, \\{.+?\\}\\})");

    private static final String UIA = "UIA";

    private static final String ELEMENTS = "elements: {";

    public static UIAWindow parseElementTree(List<String> elementTree) throws UIAException {
        List<String> lines = elementTree.stream()
            .filter(l -> l.contains(UIA) || l.contains(ELEMENTS) || l.endsWith("}")).collect(Collectors.toList());
        UIAWindow window = null;
        while (lines.size() > 0) {
            String line = lines.remove(0);
            if (line.startsWith("UIAWindow")) {
                window = (UIAWindow) parseElement(line);
                break;
            }
        }
        if (window == null) {
            throw new UIAException("Cannot parse element tree, no UIAWindow found");
        }
        if (!lines.isEmpty()) {
            parseElements(window, lines.subList(0, lines.size() - 1));
        }
        return window;
    }

    public static UIAElement parseElement(String line) throws UIAException {
        Matcher m = PATTERN_UIA.matcher(line);
        if (m.matches()) {
            UIAElement e = newElement(m.group(1));
            e.setName(m.group(2));
            String[] r = m.group(3).replaceAll("\\{", "").replaceAll("\\}", "").split(",");
            e.setRect(new Rectangle2D.Float(Float.parseFloat(r[0]), Float.parseFloat(r[1]),
                Float.parseFloat(r[2]), Float.parseFloat(r[3])));
            return e;
        }
        throw new UIAException("Cannot parse " + line);
    }

    private static void parseElements(UIAElement root, List<String> elementTree) throws UIAException {
        List<String> lines = elementTree.stream().map(l -> StringUtils.replace(l, "\t", "", 1))
            .collect(Collectors.toList());
        UIAElement element = null;
        List<String> ls = new ArrayList<>();
        while (lines.size() > 0) {
            String line = lines.remove(0);
            if (line.startsWith(UIA)) {
                element = parseElement(line);
                root.addElement(element);

            } else if (line.startsWith(ELEMENTS)) {
                ls = new ArrayList<>();

            } else if (line.startsWith("\t")) {
                ls.add(line);

            } else if (line.startsWith("}")) {
                parseElements(element, ls);
            }
        }
    }

    private static UIAElement newElement(String uia) {
        switch (uia) {
            case "UIAAlert":
                return new UIAAlert();
            case "UIAButton":
                return new UIAButton();
            case "UIACollectionCell":
                return new UIACollectionCell();
            case "UIACollectionView":
                return new UIACollectionView();
            case "UIAElement":
                return new UIAElement();
            case "UIAImage":
                return new UIAImage();
            case "UIALink":
                return new UIALink();
            case "UIANavigationBar":
                return new UIANavigationBar();
            case "UIAPageIndicator":
                return new UIAPageIndicator();
            case "UIAPicker":
                return new UIAPicker();
            case "UIAPickerWheel":
                return new UIAPickerWheel();
            case "UIAPopover":
                return new UIAPopover();
            case "UIASearchBar":
                return new UIASearchBar();
            case "UIAScrollView":
                return new UIAScrollView();
            case "UIASecureTextField":
                return new UIASecureTextField();
            case "UIASegmentedControl":
                return new UIASegmentedControl();
            case "UIASlider":
                return new UIASlider();
            case "UIAStaticText":
                return new UIAStaticText();
            case "UIASwitch":
                return new UIASwitch();
            case "UIATabBar":
                return new UIATabBar();
            case "UIATableCell":
                return new UIATableCell();
            case "UIATableGroup":
                return new UIATableGroup();
            case "UIATableView":
                return new UIATableView();
            case "UIAToolbar":
                return new UIAToolbar();
            case "UIATextField":
                return new UIATextField();
            case "UIAWebView":
                return new UIAWebView();
            case "UIAWindow":
                return new UIAWindow();
            default:
                LOG.warn("Unkown element type {}, use UIAElement", uia);
                return new UIAElement();
        }
    }

    public static void main(String[] args) throws Exception {
        List<String> elementTree = IOUtils.readLines(UIA.class.getResourceAsStream("element-tree.txt"));
        UIAWindow w = parseElementTree(elementTree);
        LOG.debug("element tree\n{}", w);
        LOG.debug("json\n{}", w.toJson().toString(2));

        try {
            UIAElement element = w.findElement(UIAStaticText.class, "Recipes");
            LOG.debug("{}", element.toJavaScript());
            LOG.debug("{}", element.toJson().toString(2));
            LOG.debug("{}", element.toString());
        } catch (Exception ex) {
            LOG.error("", ex);
        }

        elementTree = IOUtils.readLines(UIA.class.getResourceAsStream("element-tree-0.txt"));
        w = parseElementTree(elementTree);
        LOG.debug(w.toJson().toString(2));
    }
}
