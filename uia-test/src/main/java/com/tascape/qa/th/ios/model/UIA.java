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
package com.tascape.qa.th.ios.model;

import java.awt.geom.Rectangle2D;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
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
        parseElements(window, lines.subList(1, lines.size() - 1));
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
            case "UIAPopover":
                return new UIAPopover();
            case "UIASearchBar":
                return new UIASearchBar();
            case "UIAScrollView":
                return new UIAScrollView();
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
        List<String> elementTree = IOUtils.readLines(new StringReader(TEST_TREE));
        UIAWindow w = parseElementTree(elementTree);
        LOG.debug("element tree\n{}", w);

        JSONObject json = w.toJson();
        LOG.debug("json\n{}", json.toString(4));

        try {
            UIAElement element = w.findElement(UIALink.class, "Lentil Soup");
            LOG.debug("{}", element.toJavaScript());
        } finally {
            System.exit(0);
        }
    }

    private static final String TEST_TREE
        = "2015-11-30 05:23:37 +0000 Debug: window.logElementTree();\n" + "\n"
        + "2015-11-30 05:23:38 +0000 logElementTree:\n"
        + "UIAWindow \"(null)\" {{0, 0}, {320, 568}}\n"
        + "elements: {\n"
        + "	UIAButton \"(null)\" {{0, 0}, {320, 64.5}}\n"
        + "	UIAButton \"URL\" {{9.5, 24.5}, {301, 29}}\n"
        + "	elements: {\n"
        + "		UIAElement \"URL\" {{11.5, 24.5}, {297, 29}}\n"
        + "		UIAButton \"ReloadButton\" {{281.5, 24.5}, {29, 29}}\n"
        + "	}\n"
        + "	UIAButton \"Cancel\" {{324.5, 24.5}, {56, 29}}\n"
        + "	elements: {\n"
        + "		UIAStaticText \"Cancel\" {{324.5, 28}, {56, 21.5}}\n"
        + "	}\n"
        + "	UIAScrollView \"(null)\" {{0, 0}, {320, 568}}\n"
        + "	elements: {\n"
        + "		UIAScrollView \"(null)\" {{0, 0}, {320, 568}}\n"
        + "		elements: {\n"
        + "			UIAWebView \"(null)\" {{0, 64}, {320, 4475}}\n"
        + "			elements: {\n"
        + "				UIALink \"(null)\" {{114, 80}, {92, 36}}\n"
        + "				elements: {\n"
        + "					UIALink \"(null)\" {{114, 80}, {92, 36}}\n"
        + "				}\n"
        + "				UIALink \"(null)\" {{17, 133}, {75, 26}}\n"
        + "				UIAElement \"Search\" {{8, 135}, {238, 26}}\n"
        + "				UIALink \"Clear Search\" {{246, 132}, {16, 31}}\n"
        + "				elements: {\n"
        + "					UIALink \"Clear Search\" {{246, 132}, {16, 31}}\n"
        + "					elements: {\n"
        + "						UIAStaticText \"Clear Search\" {{246, 132}, {16, 31}}\n"
        + "					}\n" + "				}\n"
        + "				UIAButton \"Google Search\" {{272, 128}, {40, 40}}\n"
        + "				UIAStaticText \"WEB\" {{24, 180}, {28, 15}}\n"
        + "				UIALink \"IMAGES\" {{83, 180}, {47, 15}}\n"
        + "				elements: {\n" + "					UIAStaticText \"IMAGES\" {{83, 180}, {47, 15}}\n"
        + "				}\n"
        + "				UIALink \"VIDEOS\" {{161, 180}, {45, 15}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"VIDEOS\" {{161, 180}, {45, 15}}\n"
        + "				}\n"
        + "				UIALink \"SHOPPING\" {{237, 180}, {64, 15}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"SHOPPING\" {{237, 180}, {64, 15}}\n"
        + "				}\n"
        + "				UIALink \"NEWS\" {{332, 180}, {36, 15}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"NEWS\" {{332, 180}, {36, 15}}\n"
        + "				}\n"
        + "				UIALink \"MAPS\" {{399, 180}, {36, 15}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"MAPS\" {{399, 180}, {36, 15}}\n"
        + "				}\n"
        + "				UIALink \"BOOKS\" {{466, 180}, {43, 15}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"BOOKS\" {{466, 180}, {43, 15}}\n"
        + "				}\n"
        + "				UIALink \"FLIGHTS\" {{540, 180}, {51, 15}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"FLIGHTS\" {{540, 180}, {51, 15}}\n"
        + "				}\n"
        + "				UIALink \"APPS\" {{622, 180}, {33, 15}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"APPS\" {{622, 180}, {33, 15}}\n"
        + "				}\n"
        + "				UIAButton \"SEARCH TOOLS\" {{671, 168}, {134, 40}}\n"
        + "				UIAStaticText \"Recipes\" {{24, 233}, {53, 18}}\n"
        + "				UIALink \"Lentil Soup\" {{40, 406}, {81, 20}}\n"
        + "				elements: {\n"
        + "					UIALink \"Lentil Soup\" {{40, 406}, {81, 20}}\n"
        + "					elements: {\n"
        + "						UIAStaticText \"Lentil Soup\" {{40, 406}, {81, 20}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"Food Network\" {{40, 429}, {89, 17}}\n"
        + "				UIAStaticText \"5.0\" {{40, 454}, {17, 15}}\n"
        + "				UIAStaticText \"335 reviews\" {{130, 454}, {65, 15}}\n"
        + "				UIAStaticText \"1 hr 15 min\" {{40, 475}, {61, 15}}\n"
        + "				UIALink \"Lentil Soup\" {{264, 263}, {232, 163}}\n"
        + "				elements: {\n"
        + "					UIALink \"(null)\" {{356, 304}, {48, 48}}\n"
        + "					elements: {\n"
        + "						UIALink \"Lentil Soup\" {{356, 304}, {48, 48}}\n"
        + "					}\n"
        + "					UIALink \"Lentil Soup\" {{280, 406}, {81, 20}}\n"
        + "					elements: {\n"
        + "						UIAStaticText \"Lentil Soup\" {{280, 406}, {81, 20}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"Allrecipes\" {{280, 429}, {61, 17}}\n"
        + "				UIAStaticText \"4.5\" {{280, 454}, {17, 15}}\n"
        + "				UIAStaticText \"1,410 reviews\" {{370, 454}, {75, 15}}\n"
        + "				UIAStaticText \"349 calories\" {{280, 475}, {65, 15}}\n"
        + "				UIALink \"Vegan Lentil Soup\" {{520, 406}, {130, 20}}\n"
        + "				elements: {\n"
        + "					UIALink \"Vegan Lentil Soup\" {{520, 406}, {130, 20}}\n"
        + "					elements: {\n"
        + "						UIAStaticText \"Vegan Lentil Soup\" {{520, 406}, {130, 20}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"Cookie and Kate\" {{520, 429}, {105, 17}}\n"
        + "				UIAStaticText \"5.0\" {{520, 454}, {17, 15}}\n"
        + "				UIAStaticText \"50 reviews\" {{610, 454}, {58, 15}}\n"
        + "				UIAStaticText \"55 min\" {{520, 475}, {37, 15}}\n"
        + "				UIALink \"Easy Lentil Soup\" {{760, 406}, {120, 20}}\n"
        + "				elements: {\n"
        + "					UIALink \"Easy Lentil Soup\" {{760, 406}, {120, 20}}\n"
        + "					elements: {\n"
        + "						UIAStaticText \"Easy Lentil Soup\" {{760, 406}, {120, 20}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"Chowhound.com\" {{760, 429}, {108, 17}}\n"
        + "				UIAStaticText \"4.5\" {{760, 454}, {17, 15}}\n"
        + "				UIAStaticText \"73 reviews\" {{850, 454}, {58, 15}}\n"
        + "				UIAStaticText \"1 hr\" {{760, 475}, {21, 15}}\n"
        + "				UIALink \"fresh cilantro\" {{1000, 406}, {93, 20}}\n"
        + "				elements: {\n"
        + "					UIALink \"fresh cilantro\" {{1000, 406}, {93, 20}}\n"
        + "					elements: {\n"
        + "						UIAStaticText \"fresh cilantro\" {{1000, 406}, {93, 20}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"Cooking - The New York Times\" {{1000, 429}, {194, 17}}\n"
        + "				UIAStaticText \"5.0\" {{1000, 454}, {17, 15}}\n"
        + "				UIAStaticText \"491 reviews\" {{1090, 454}, {65, 15}}\n"
        + "				UIAStaticText \"377 calories\" {{1000, 475}, {65, 15}}\n"
        + "				UIALink \"Lentil Soup Recipe\" {{1240, 406}, {136, 20}}\n"
        + "				elements: {\n"
        + "					UIALink \"Lentil Soup Recipe\" {{1240, 406}, {136, 20}}\n"
        + "					elements: {\n"
        + "						UIAStaticText \"Lentil Soup Recipe\" {{1240, 406}, {136, 20}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"Martha Stewart\" {{1240, 429}, {97, 17}}\n"
        + "				UIALink \"Red Lentil Soup\" {{1480, 406}, {115, 20}}\n"
        + "				elements: {\n"
        + "					UIALink \"Red Lentil Soup\" {{1480, 406}, {115, 20}}\n"
        + "					elements: {\n"
        + "						UIAStaticText \"Red Lentil Soup\" {{1480, 406}, {115, 20}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"The Kitchn\" {{1480, 429}, {68, 17}}\n"
        + "				UIAStaticText \"225 calories\" {{1480, 449}, {65, 15}}\n"
        + "				UIALink \"Fridge-Clearing Lentil Soup\" {{1704, 393}, {232, 33}}\n"
        + "				elements: {\n"
        + "					UIALink \"Fridge-Clearing Lentil, Soup\" {{1704, 393}, {232, 33}}\n"
        + "					elements: {\n"
        + "						UIAStaticText \"Fridge-Clearing Lentil\" {{1720, 406}, {159, 20}}\n"
        + "						UIAStaticText \"Soup\" {{1878, 406}, {39, 20}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"The Kitchn\" {{1720, 429}, {68, 17}}\n"
        + "				UIAStaticText \"314 calories\" {{1720, 449}, {65, 15}}\n"
        + "				UIALink \"Lentil Soup Recipe : Alton Brown : Food Network\" {{24, 541}, {246, 40}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Lentil Soup Recipe : Alton Brown : Food Network\" {{24, 541}, {246, 40}}\n"
        + "				}\n"
        + "				UIAStaticText \"Food Network › recipes › le...\" {{24, 580}, {180, 17}}\n"
        + "				UIALink \"lentil soup from www.foodnetwork.com\" {{13, 623}, {92, 70}}\n"
        + "				elements: {\n"
        + "					UIALink \"lentil soup from www.foodnetwork.com\" {{13, 623}, {92, 70}}\n"
        + "					elements: {\n"
        + "						UIAImage \"lentil soup from www.foodnetwork.com\" {{13, 623}, {92, 70}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"Rating: 4.8 - ‎335 reviews - ‎1 hr 15 min\" {{101, 620}, {174, 37}}\n"
        + "				UIAStaticText \"Mobile-friendly -\" {{101, 660}, {107, 17}}\n"
        + "				UIAStaticText \"Alton thinks\" {{207, 660}, {75, 17}}\n"
        + "				UIAStaticText \"lentils\" {{101, 680}, {40, 18}}\n"
        + "				UIAStaticText \"may be the best plant-borne\" {{101, 680}, {185, 37}}\n"
        + "				UIAStaticText \"soup\" {{141, 700}, {34, 18}}\n"
        + "				UIAStaticText \"base on planet Earth. ... Add the\" {{101, 700}, {170, 37}}\n"
        + "				UIAStaticText \"lentils\" {{210, 720}, {41, 18}}\n"
        + "				UIAStaticText \", tomatoes, broth, coriander, cumin and grains of paradise and stir to combine. ...\" {{101, 720}, {182, 77}}\n"
        + "				UIAStaticText \"Lentil Soup\" {{101, 780}, {182, 38}}\n"
        + "				UIAStaticText \"with Kale and Sausage.\" {{135, 800}, {152, 17}}\n"
        + "				UIAStaticText \"Lentil soup\" {{24, 853}, {79, 20}}\n"
        + "				UIAButton \"Image result for lentil soup\" {{24, 901}, {100, 144}}\n"
        + "				UIAButton \"Image result for lentil soup\" {{121, 901}, {216, 144}}\n"
        + "				UIAButton \"Image result for lentil soup\" {{332, 901}, {215, 144}}\n"
        + "				UIAButton \"Image result for lentil soup\" {{532, 901}, {215, 144}}\n"
        + "				UIAButton \"Image result for lentil soup\" {{727, 901}, {198, 144}}\n"
        + "				UIAButton \"Image result for lentil soup\" {{932, 901}, {192, 144}}\n"
        + "				UIAButton \"Image result for lentil soup\" {{1122, 901}, {217, 144}}\n"
        + "				UIAButton \"Image result for lentil soup\" {{1332, 901}, {144, 144}}\n"
        + "				UIAButton \"Image result for lentil soup\" {{1445, 901}, {246, 144}}\n"
        + "				UIAButton \"Image result for lentil soup\" {{1684, 901}, {192, 144}}\n"
        + "				UIAButton \"Image result for lentil soup\" {{1874, 901}, {228, 144}}\n"
        + "				UIALink \"View all\" {{2084, 901}, {150, 146}}\n"
        + "				elements: {\n"
        + "					UIAButton \"(null)\" {{2131, 931}, {56, 56}}\n"
        + "					UIALink \"View all\" {{2135, 999}, {48, 17}}\n"
        + "					elements: {\n"
        + "						UIAStaticText \"View all\" {{2135, 999}, {48, 17}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"Lentil soup refers to a variety of vegetarian and meat soups made with lentils. The soup may consist of green,\" {{24, 1081}, {266, 57}}\n"
        + "				UIAButton \"…  More\" {{193, 1121}, {51, 17}}\n"
        + "				UIAButton \"More about Lentil soup\" {{8, 1141}, {304, 72}}\n"
        + "				UIALink \"Lentil Soup Recipe - Allrecipes.com\" {{24, 1236}, {256, 20}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Lentil Soup Recipe - Allrecipes.com\" {{24, 1236}, {256, 20}}\n"
        + "				}\n"
        + "				UIAStaticText \"Allrecipes › recipe › lentil-s...\" {{24, 1255}, {176, 17}}\n"
        + "				UIALink \"lentil soup from allrecipes.com\" {{0, 1298}, {125, 70}}\n"
        + "				elements: {\n"
        + "					UIALink \"lentil soup from allrecipes.com\" {{0, 1298}, {125, 70}}\n"
        + "					elements: {\n"
        + "						UIAImage \"lentil soup from allrecipes.com\" {{0, 1298}, {125, 70}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"Rating: 4.5 - ‎1,410 reviews - ‎349 cal\" {{101, 1295}, {185, 37}}\n"
        + "				UIAStaticText \"Mobile-friendly -\" {{101, 1335}, {107, 17}}\n"
        + "				UIAStaticText \"Lentils are coupled with vegetables for this family-friendly\" {{101, 1335}, {173, 57}}\n"
        + "				UIAStaticText \"lentil soup\" {{218, 1375}, {70, 18}}\n"
        + "				UIAStaticText \". Topped with spinach and a splash of vinegar, this ...\" {{101, 1375}, {191, 57}}\n"
        + "				UIAStaticText \"(null)\" {{0, 64}, {0, 0}}\n"
        + "				UIALink \"Lentil Soup\" {{101, 1435}, {71, 17}}\n"
        + "				elements: {\n" + "					UIAStaticText \"Lentil Soup\" {{101, 1435}, {71, 17}}\n"
        + "				}\n"
        + "				UIAStaticText \"- ‎\" {{101, 1455}, {10, 17}}\n"
        + "				UIALink \"Vegan Red Lentil Soup\" {{110, 1455}, {143, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Vegan Red Lentil Soup\" {{110, 1455}, {143, 17}}\n"
        + "				}\n"
        + "				UIAStaticText \"- ‎\" {{101, 1475}, {10, 17}}\n"
        + "				UIALink \"87 Photos\" {{110, 1475}, {65, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"87 Photos\" {{110, 1475}, {65, 17}}\n"
        + "				}\n"
        + "				UIALink \"Best Lentil Soup Recipe - Cookie and Kate\" {{24, 1489}, {270, 40}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Best Lentil Soup Recipe - Cookie and Kate\" {{24, 1489}, {270, 40}}\n"
        + "				}\n"
        + "				UIAStaticText \"cookieandkate.com › vegan-...\" {{24, 1528}, {190, 17}}\n"
        + "				UIALink \"lentil soup from cookieandkate.com\" {{24, 1558}, {70, 96}}\n"
        + "				elements: {\n"
        + "					UIALink \"lentil soup from cookieandkate.com\" {{24, 1558}, {70, 96}}\n"
        + "					elements: {\n"
        + "						UIAImage \"lentil soup from cookieandkate.com\" {{24, 1558}, {70, 96}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"Rating: 4.9 - ‎50 votes - ‎55 min\" {{101, 1568}, {166, 37}}\n"
        + "				UIAStaticText \"Mobile-friendly - Feb 13, 2015 -\" {{101, 1608}, {189, 37}}\n"
        + "				UIAStaticText \"Spiced Vegan\" {{110, 1628}, {91, 17}}\n"
        + "				UIAStaticText \"Lentil Soup\" {{200, 1628}, {76, 18}}\n"
        + "				UIAStaticText \". ... My\" {{101, 1628}, {195, 37}}\n"
        + "				UIAStaticText \"lentil soup\" {{124, 1648}, {70, 18}}\n"
        + "				UIAStaticText \"is made with mostly pantry ingredients but includes hearty greens and a squeeze of lemon for bright, fresh flavor. ... This simple vegan\" {{101, 1648}, {183, 117}}\n"
        + "				UIAStaticText \"lentil soup\" {{142, 1748}, {70, 18}}\n"
        + "				UIAStaticText \"recipe comes together quickly with mostly pantry ingredients.\" {{101, 1748}, {177, 57}}\n"
        + "				UIALink \"French Lentil Soup recipe | Epicurious.com\" {{24, 1841}, {191, 40}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"French Lentil Soup recipe | Epicurious.com\" {{24, 1841}, {191, 40}}\n"
        + "				}\n"
        + "				UIAStaticText \"Epicurious › recipes › food › views › fre...\" {{24, 1880}, {249, 17}}\n"
        + "				UIAStaticText \"Rating: 4/4 - ‎265 reviews - ‎240 cal\" {{24, 1920}, {262, 37}}\n"
        + "				UIAStaticText \"Mobile-friendly -\" {{24, 1960}, {107, 17}}\n"
        + "				UIAStaticText \"French\" {{130, 1960}, {48, 17}}\n"
        + "				UIAStaticText \"Lentil Soup\" {{177, 1960}, {76, 18}}\n"
        + "				UIAStaticText \". Bon Appétit December 2006. 4/4 fork user rating. reviews ( 265). 93%. make it again. French Lentil ...\" {{24, 1960}, {262, 77}}\n"
        + "				UIALink \"Easy Lentil Soup - Chowhound\" {{24, 2073}, {222, 20}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Easy Lentil Soup - Chowhound\" {{24, 2073}, {222, 20}}\n"
        + "				}\n"
        + "				UIAStaticText \"www.chowhound.com › bas...\" {{24, 2092}, {186, 17}}\n"
        + "				UIALink \"lentil soup from www.chowhound.com\" {{7, 2135}, {105, 70}}\n"
        + "				elements: {\n"
        + "					UIALink \"lentil soup from www.chowhound.com\" {{7, 2135}, {105, 70}}\n"
        + "					elements: {\n"
        + "						UIAImage \"lentil soup from www.chowhound.com\" {{7, 2135}, {105, 70}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"Rating: 4.5 - ‎73 reviews - ‎1 hr\" {{101, 2132}, {166, 37}}\n"
        + "				UIAStaticText \"Mobile-friendly -\" {{101, 2172}, {107, 17}}\n"
        + "				UIAStaticText \"Lentil soup\" {{207, 2172}, {75, 18}}\n"
        + "				UIAStaticText \"recipes can range from superhearty to watery and tasteless. This soup is best of both worlds: filling and ...\" {{101, 2192}, {186, 77}}\n"
        + "				UIALink \"Lentil Soup Recipes - 7 Ways With Sausage, Bacon, Tomatoes, More\" {{24, 2305}, {248, 40}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Lentil Soup Recipes - 7 Ways With Sausage, Bacon, Tomatoes, More\" {{24, 2305}, {248, 40}}\n"
        + "				}\n"
        + "				UIAStaticText \"Mark Bittman › 7-ways-to-make-lentil-so...\" {{24, 2344}, {266, 17}}\n"
        + "				UIAStaticText \"Makes: 4 servings. Time: About 45 minutes.\" {{24, 2384}, {217, 37}}\n"
        + "				UIAStaticText \"Lentils\" {{81, 2404}, {45, 18}}\n"
        + "				UIAStaticText \"make\" {{125, 2404}, {43, 17}}\n"
        + "				UIAStaticText \"soup\" {{167, 2404}, {34, 18}}\n"
        + "				UIAStaticText \"making easy— they cook quickly and are incredibly tasty.\" {{24, 2404}, {260, 57}}\n"
        + "				UIALink \"Red Lentil Soup With Lemon Recipe - NYT Cooking\" {{24, 2497}, {270, 40}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Red Lentil Soup With Lemon Recipe - NYT Cooking\" {{24, 2497}, {270, 40}}\n"
        + "				}\n"
        + "				UIAStaticText \"cooking.nytimes.com › 101...\" {{24, 2536}, {181, 17}}\n"
        + "				UIALink \"lentil soup from cooking.nytimes.com\" {{7, 2579}, {105, 70}}\n"
        + "				elements: {\n"
        + "					UIALink \"lentil soup from cooking.nytimes.com\" {{7, 2579}, {105, 70}}\n"
        + "					elements: {\n"
        + "						UIAImage \"lentil soup from cooking.nytimes.com\" {{7, 2579}, {105, 70}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"Rating: 5 - ‎491 votes - ‎45 min - ‎377 cal\" {{101, 2576}, {162, 37}}\n"
        + "				UIAStaticText \"Mobile-friendly -\" {{101, 2616}, {107, 17}}\n"
        + "				UIAStaticText \"This is a\" {{207, 2616}, {56, 17}}\n"
        + "				UIAStaticText \"lentil soup\" {{101, 2616}, {194, 38}}\n"
        + "				UIAStaticText \"that defies expectations of what\" {{101, 2636}, {187, 37}}\n"
        + "				UIAStaticText \"lentil soup\" {{151, 2656}, {70, 18}}\n"
        + "				UIAStaticText \"can be It is light, spicy and a bold red color (no murky ...\" {{101, 2656}, {192, 57}}\n"
        + "				UIALink \"Lentil soup - Wikipedia, the free encyclopedia\" {{24, 2749}, {227, 40}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Lentil soup - Wikipedia, the free encyclopedia\" {{24, 2749}, {227, 40}}\n"
        + "				}\n"
        + "				UIAStaticText \"Wikipedia › wiki › Lentil_soup\" {{24, 2788}, {180, 17}}\n"
        + "				UIAStaticText \"Mobile-friendly -\" {{24, 2828}, {107, 17}}\n"
        + "				UIAStaticText \"Lentil soup\" {{130, 2828}, {75, 18}}\n"
        + "				UIAStaticText \"refers to a variety of vegetarian and meat soups made with lentils. The soup may consist of green, brown, ...\" {{24, 2828}, {271, 77}}\n"
        + "				UIALink \"Little marvels: Yotam Ottolenghi's lentil recipes | Life ...\" {{24, 2941}, {240, 40}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Little marvels: Yotam Ottolenghi's lentil recipes | Life ...\" {{24, 2941}, {240, 40}}\n"
        + "				}\n"
        + "				UIAStaticText \"www.theguardian.com › ...\" {{24, 2980}, {164, 17}}\n"
        + "				UIAStaticText \"‎ - The Guardian\" {{187, 2980}, {99, 17}}\n"
        + "				UIAStaticText \"Unlike red\" {{24, 3020}, {67, 17}}\n"
        + "				UIAStaticText \"lentils\" {{90, 3020}, {41, 18}}\n"
        + "				UIAStaticText \", which fall apart in the cooking (so making them perfect for\" {{24, 3020}, {248, 37}}\n"
        + "				UIAStaticText \"soups\" {{24, 3060}, {41, 18}}\n"
        + "				UIAStaticText \"), brown and green varieties hold their shape, making them a very good base on which to lay a roast ...\" {{24, 3060}, {271, 57}}\n"
        + "				UIALink \"Fifty of the City's Tastiest Soups\" {{24, 3153}, {230, 20}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Fifty of the City's Tastiest Soups\" {{24, 3153}, {230, 20}}\n"
        + "				}\n"
        + "				UIAStaticText \"nymag.com › feat...\" {{24, 3172}, {121, 17}}\n"
        + "				UIAStaticText \"‎ - New York Magazine\" {{144, 3172}, {138, 17}}\n"
        + "				UIAStaticText \"Everything at this storefront soup shack specializing in the silky rice noodles of China's ... There are two\" {{24, 3212}, {248, 57}}\n"
        + "				UIAStaticText \"lentil soups\" {{180, 3252}, {77, 18}}\n"
        + "				UIAStaticText \"on the menu at this Syrian-Lebanese spot, and it's nearly impossible ...\" {{24, 3252}, {253, 57}}\n"
        + "				UIALink \"Lentils, beyond soups and stews: Have you ever had ...\" {{24, 3345}, {240, 40}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Lentils, beyond soups and stews: Have you ever had ...\" {{24, 3345}, {240, 40}}\n"
        + "				}\n"
        + "				UIAStaticText \"www.washingtonpost.com › ...\" {{24, 3384}, {190, 17}}\n"
        + "				UIAStaticText \"‎ - The Washington Post\" {{213, 3384}, {81, 17}}\n"
        + "				UIAStaticText \"How old are\" {{24, 3424}, {80, 17}}\n"
        + "				UIAStaticText \"lentils\" {{103, 3424}, {40, 18}}\n"
        + "				UIAStaticText \"? Here's one clue: People who say\" {{24, 3424}, {233, 37}}\n"
        + "				UIAStaticText \"lentils\" {{127, 3444}, {40, 18}}\n"
        + "				UIAStaticText \"are shaped like lenses have the reference backwards. Turns out that the world's first lenses got that name because they ...\" {{24, 3444}, {255, 77}}\n"
        + "				UIAStaticText \"People also search for\" {{24, 3559}, {145, 18}}\n"
        + "				UIALink \"Lentil\" {{24, 3603}, {112, 112}}\n"
        + "				elements: {\n"
        + "					UIAImage \"Lentil\" {{24, 3603}, {112, 112}}\n"
        + "				}\n"
        + "				UIALink \"Lentil\" {{32, 3723}, {34, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Lentil\" {{32, 3723}, {34, 17}}\n"
        + "				}\n"
        + "				UIALink \"Ezogelin soup\" {{144, 3603}, {112, 112}}\n"
        + "				elements: {\n"
        + "					UIAImage \"Ezogelin soup\" {{144, 3603}, {112, 112}}\n"
        + "				}\n"
        + "				UIALink \"Ezogelin soup\" {{152, 3723}, {88, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Ezogelin soup\" {{152, 3723}, {88, 17}}\n"
        + "				}\n"
        + "				UIALink \"Yogurt soup\" {{264, 3603}, {112, 112}}\n"
        + "				elements: {\n"
        + "					UIAImage \"Yogurt soup\" {{264, 3603}, {112, 112}}\n"
        + "				}\n"
        + "				UIALink \"Yogurt soup\" {{272, 3723}, {76, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Yogurt soup\" {{272, 3723}, {76, 17}}\n"
        + "				}\n"
        + "				UIALink \"Pea soup\" {{384, 3603}, {112, 112}}\n"
        + "				elements: {\n"
        + "					UIAImage \"Pea soup\" {{384, 3603}, {112, 112}}\n"
        + "				}\n"
        + "				UIALink \"Pea soup\" {{392, 3723}, {60, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Pea soup\" {{392, 3723}, {60, 17}}\n"
        + "				}\n"
        + "				UIALink \"Tomato soup\" {{504, 3603}, {112, 112}}\n"
        + "				elements: {\n"
        + "					UIAImage \"Tomato soup\" {{504, 3603}, {112, 112}}\n"
        + "				}\n"
        + "				UIALink \"Tomato soup\" {{512, 3723}, {82, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Tomato soup\" {{512, 3723}, {82, 17}}\n"
        + "				}\n"
        + "				UIALink \"Dal\" {{624, 3603}, {112, 112}}\n"
        + "				elements: {\n"
        + "					UIAImage \"Dal\" {{624, 3603}, {112, 112}}\n"
        + "				}\n"
        + "				UIALink \"Dal\" {{632, 3723}, {21, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Dal\" {{632, 3723}, {21, 17}}\n"
        + "				}\n"
        + "				UIALink \"Kofta\" {{744, 3603}, {112, 112}}\n"
        + "				elements: {\n"
        + "					UIAImage \"Kofta\" {{744, 3603}, {112, 112}}\n"
        + "				}\n"
        + "				UIALink \"Kofta\" {{752, 3723}, {34, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Kofta\" {{752, 3723}, {34, 17}}\n"
        + "				}\n"
        + "				UIALink \"Minestrone\" {{864, 3603}, {112, 112}}\n"
        + "				elements: {\n"
        + "					UIAImage \"Minestrone\" {{864, 3603}, {112, 112}}\n"
        + "				}\n"
        + "				UIALink \"Minestrone\" {{872, 3723}, {70, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Minestrone\" {{872, 3723}, {70, 17}}\n"
        + "				}\n"
        + "				UIALink \"View all\" {{1004, 3633}, {56, 86}}\n"
        + "				elements: {\n"
        + "					UIAButton \"(null)\" {{1004, 3633}, {56, 56}}\n"
        + "					UIALink \"View all\" {{1008, 3701}, {48, 17}}\n"
        + "					elements: {\n"
        + "						UIAStaticText \"View all\" {{1008, 3701}, {48, 17}}\n"
        + "					}\n"
        + "				}\n"
        + "				UIAStaticText \"Related searches\" {{24, 3790}, {113, 18}}\n"
        + "				UIALink \"easy lentil soup\" {{8, 3819}, {304, 50}}\n"
        + "				UIALink \"middle eastern lentil soup\" {{8, 3870}, {304, 50}}\n"
        + "				UIALink \"lentil soup with ham\" {{8, 3921}, {304, 50}}\n"
        + "				UIALink \"lentil recipes\" {{8, 3972}, {304, 50}}\n"
        + "				UIALink \"indian lentil soup\" {{8, 4023}, {304, 50}}\n"
        + "				UIALink \"lentil soup sausage\" {{8, 4074}, {304, 50}}\n"
        + "				UIALink \"greek lentil soup\" {{8, 4125}, {304, 50}}\n"
        + "				UIALink \"red lentil soup\" {{8, 4176}, {304, 50}}\n"
        + "				UIALink \"Next page\" {{109, 4246}, {102, 39}}\n"
        + "				UIAStaticText \"San Jose, CA\" {{26, 4321}, {90, 18}}\n"
        + "				UIAStaticText \"-\" {{115, 4321}, {14, 17}}\n"
        + "				UIAStaticText \"From your Internet address\" {{128, 4321}, {171, 17}}\n"
        + "				UIAStaticText \"-\" {{298, 4321}, {10, 17}}\n"
        + "				UIALink \"Use precise location\" {{96, 4341}, {128, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Use precise location\" {{96, 4341}, {128, 17}}\n"
        + "				}\n"
        + "				UIAStaticText \"(null)\" {{112, 4361}, {13, 17}}\n"
        + "				UIALink \"Learn more\" {{124, 4361}, {72, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Learn more\" {{124, 4361}, {72, 17}}\n"
        + "				}\n"
        + "				UIAStaticText \"(null)\" {{195, 4361}, {13, 17}}\n"
        + "				UIALink \"Sign in\" {{138, 4397}, {44, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Sign in\" {{138, 4397}, {44, 17}}\n"
        + "				}\n"
        + "				UIALink \"Learn more about mobile-friendly pages\" {{34, 4431}, {252, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Learn more about mobile-friendly pages\" {{34, 4431}, {252, 17}}\n"
        + "				}\n"
        + "				UIALink \"Settings\" {{57, 4467}, {53, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Settings\" {{57, 4467}, {53, 17}}\n"
        + "				}\n"
        + "				UIALink \"Help\" {{140, 4467}, {30, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Help\" {{140, 4467}, {30, 17}}\n"
        + "				}\n"
        + "				UIALink \"Feedback\" {{200, 4467}, {63, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Feedback\" {{200, 4467}, {63, 17}}\n"
        + "				}\n"
        + "				UIALink \"Privacy\" {{102, 4502}, {47, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Privacy\" {{102, 4502}, {47, 17}}\n"
        + "				}\n"
        + "				UIALink \"Terms\" {{179, 4502}, {39, 17}}\n"
        + "				elements: {\n"
        + "					UIAStaticText \"Terms\" {{179, 4502}, {39, 17}}\n"
        + "				}\n"
        + "			}\n"
        + "			UIAImage \"(null)\" {{314.5, 67}, {2.5, 56.5}}\n"
        + "			UIAImage \"(null)\" {{3, 518.5}, {314, 2.5}}\n"
        + "		}\n"
        + "		UIAImage \"(null)\" {{0, 64}, {320, 493}}\n"
        + "	}\n"
        + "	UIAToolbar \"(null)\" {{0, 524}, {320, 44}}\n"
        + "	elements: {\n"
        + "		UIAImage \"(null)\" {{0, 524}, {320, 44}}\n"
        + "		UIAButton \"back\" {{0.5, 526}, {42, 40}}\n"
        + "		UIAButton \"Forward\" {{71, 526}, {42, 40}}\n"
        + "		UIAButton \"Share\" {{133.5, 525}, {51, 40}}\n"
        + "		UIAButton \"Show Bookmarks\" {{197, 526}, {57, 40}}\n"
        + "		UIAButton \"Pages\" {{263.5, 526}, {57, 40}}\n"
        + "	}\n"
        + "}\n"
        + "";

}
