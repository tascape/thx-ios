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
package com.tascape.qa.th.ios.model;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author linsong wang
 */
public interface UIATarget {

    boolean deactivateAppForDuration (int duration) throws UIAException;

    String model() throws UIAException;

    String name() throws UIAException;

    Rectangle2D.Float rect() throws UIAException;

    String systemName() throws UIAException;

    String systemVersion() throws UIAException;

    DeviceOrientation deviceOrientation() throws UIAException;

    void setDeviceOrientation(DeviceOrientation orientation) throws UIAException;

    boolean setLocation(double latitude, double longitude) throws UIAException;

    void clickVolumeDown() throws UIAException;

    void clickVolumeUp() throws UIAException;

    void holdVolumeDown() throws UIAException;

    void holdVolumeUp() throws UIAException;

    void lockForDuration(int duration) throws UIAException;

    void lock() throws UIAException;

    void shake() throws UIAException;

    void unlock() throws UIAException;

    void dragFromToForDuration(Point2D.Float from, Point2D.Float to, int duration) throws UIAException;

    void dragFromToForDuration(Rectangle2D.Float from, Rectangle2D.Float to, int duration) throws UIAException;

    void dragFromToForDuration(String fromJavaScript, String toJavaScript, int duration) throws UIAException;

    void doubleTap(float x, float y) throws UIAException;

    void doubleTap(Rectangle2D.Float rectangle) throws UIAException;

    void doubleTap(String javaScript) throws UIAException;

    void flickFromTo(Point2D.Float from, Point2D.Float to, int duration) throws UIAException;

    void flickFromTo(Rectangle2D.Float from, Rectangle2D.Float to, int duration) throws UIAException;

    void flickFromTo(String fromJavaScript, String toJavaScript, int duration) throws UIAException;

    void pinchCloseFromToForDuration(Point2D.Float from, Point2D.Float to, int duration) throws UIAException;

    void pinchCloseFromToForDuration(Rectangle2D.Float from, Rectangle2D.Float to, int duration) throws UIAException;

    void pinchCloseFromToForDuration(String fromJavaScript, String toJavaScript, int duration) throws UIAException;

    void pinchOpenFromToForDuration(Point2D.Float from, Point2D.Float to, int duration) throws UIAException;

    void pinchOpenFromToForDuration(Rectangle2D.Float from, Rectangle2D.Float to, int duration) throws UIAException;

    void pinchOpenFromToForDuration(String fromJavaScript, String toJavaScript, int duration) throws UIAException;

    void tap(float x, float y) throws UIAException;

    void tap(Rectangle2D.Float rectangle) throws UIAException;

    void tap(String javaScript) throws UIAException;

    void touchAndHold(Point2D.Float point, int duration) throws UIAException;

    void touchAndHold(Rectangle2D.Float point, int duration) throws UIAException;

    void touchAndHold(String javaScript, int duration) throws UIAException;

    void popTimeout() throws UIAException;

    void pushTimeout(int timeoutValue) throws UIAException;

    void setTimeout(int timeout) throws UIAException;

    int timeout() throws UIAException;

    void delay(int timeInterval) throws UIAException;

    boolean onAlert(UIAAlert alert) throws UIAException;

    default String toCGString(Rectangle2D.Float rect) {
        return String.format("{{%f, %f}, {%f, %f}}", rect.x, rect.y, rect.width, rect.height);
    }

    default String toCGString(Point2D.Float point) {
        return String.format("{x:%f, y:%f}", point.x, point.y);
    }

    default String toCGString(float x, float y) {
        return String.format("{x:%f, y:%f}", x, y);
    }
}
