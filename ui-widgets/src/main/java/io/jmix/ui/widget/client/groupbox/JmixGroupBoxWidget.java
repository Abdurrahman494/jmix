/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.ui.widget.client.groupbox;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasEnabled;
import io.jmix.ui.widget.client.Tools;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ui.VPanel;
import com.vaadin.shared.ui.MarginInfo;

import java.util.function.Consumer;

public class JmixGroupBoxWidget extends VPanel implements HasEnabled {

    public static final String CLASSNAME = "jmix-groupbox";

    protected boolean expanded = true;
    protected boolean collapsable = false;
    protected boolean showAsPanel = false;

    protected boolean enabled = true;

    protected ExpandHandler expandHandler;
    protected MarginInfo marginInfo;

    public Element captionWrap;
    public Element expander = DOM.createSpan();

    protected Element contextHelpIcon;
    protected Consumer<Event> contextHelpClickHandler;

    protected Element requiredIcon;

    public Element captionStartDeco = DOM.createDiv();
    public Element captionEndDeco = DOM.createDiv();
    public Element captionTextNode;

    public JmixGroupBoxWidget(String primaryStyleName) {
        setStylePrimaryName(primaryStyleName);
        setStyleName(primaryStyleName);

        captionWrap = captionNode.getParentElement().cast();
        captionTextNode = (Element) captionNode.getChild(0);
    }

    public void init() {
        if (!isShowAsPanel()) {
            captionNode.setClassName(getStylePrimaryName() + "-caption");
            contentNode.setClassName(getStylePrimaryName() + "-content");
            bottomDecoration.setClassName(getStylePrimaryName() + "-deco");
            expander.setClassName(getStylePrimaryName() + "-expander");

            contentNode.getStyle().clearPosition();

            setExpanded(true);

            captionStartDeco.appendChild(DOM.createDiv());
            captionStartDeco.setClassName(getStylePrimaryName() + "-caption-start-deco");
            captionWrap.insertFirst(captionStartDeco);

            captionEndDeco.appendChild(DOM.createDiv());
            captionEndDeco.setClassName(getStylePrimaryName() + "-caption-end-deco");
            captionWrap.appendChild(captionEndDeco);

            captionNode.insertFirst(expander);

            captionTextNode.setClassName(getStylePrimaryName() + "-caption-text");

            DOM.sinkEvents(expander, Event.ONCLICK);
            DOM.sinkEvents(captionTextNode, Event.ONCLICK);
        }
    }

    public JmixGroupBoxWidget() {
        this(CLASSNAME);
    }

    @Override
    public void setCaption(String text, boolean captionAsHtml) {
        if (text == null || text.equals("")) {
            addStyleDependentName("nocaption");
        } else {
            removeStyleDependentName("nocaption");
        }

        super.setCaption(text, captionAsHtml);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        if (expanded) {
            expander.addClassName("expanded");
            getElement().removeClassName("collapsed");
            getElement().addClassName("expanded");
        } else {
            expander.removeClassName("expanded");
            getElement().removeClassName("expanded");
            getElement().addClassName("collapsed");
        }

        this.expanded = expanded;
    }

    public boolean isCollapsable() {
        return collapsable;
    }

    public void setCollapsable(boolean collapsable) {
        Style expanderStyle = expander.getStyle();
        if (collapsable) {
            expanderStyle.clearProperty("display");
            removeStyleDependentName("nocollapsable");
        } else {
            addStyleDependentName("nocollapsable");
            expanderStyle.setDisplay(Style.Display.NONE);
        }

        Tools.textSelectionEnable(captionNode, !collapsable);

        this.collapsable = collapsable;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (isEnabled() != enabled) {
            this.enabled = enabled;
        }
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (DOM.eventGetType(event) == Event.ONCLICK && isEnabled()) {
            if (collapsable
                    && (DOM.eventGetTarget(event) == expander
                    || DOM.eventGetTarget(event) == captionNode.getChild(1))) {
                toggleExpanded(event);
            } else if (DOM.eventGetTarget(event) == contextHelpIcon
                    && contextHelpClickHandler != null) {
                contextHelpClickHandler.accept(event);
            } else {
                super.onBrowserEvent(event);
            }
        } else {
            super.onBrowserEvent(event);
        }
    }

    protected void toggleExpanded(Event event) {
        setExpanded(!expanded);

        if (collapsable && expandHandler != null) {
            expandHandler.expandStateChanged(expanded);
        }
        DOM.eventCancelBubble(event, true);
    }

    public void setShowAsPanel(boolean showAsPanel) {
        if (isShowAsPanel() != showAsPanel) {
            this.showAsPanel = showAsPanel;
        }
    }

    public boolean isShowAsPanel() {
        return showAsPanel;
    }

    public interface ExpandHandler {
        void expandStateChanged(boolean expanded);
    }

    @Override
    public void setIconUri(String iconUri, ApplicationConnection client) {
        if (icon != null) {
            captionNode.removeChild(icon.getElement());
        }
        icon = client.getIcon(iconUri);
        if (icon != null) {
            DOM.insertChild(captionNode, icon.getElement(), 1);
        }
    }

    public boolean hasAnyOuterMargin() {
        return marginInfo != null
                && marginInfo.hashCode() != 0;
    }

    public MarginInfo getMarginInfo() {
        return marginInfo;
    }

    public void setOuterMargin(MarginInfo marginInfo) {
        this.marginInfo = marginInfo;
        if (marginInfo != null) {
            setStyleName("jmix-outer-margin-top", marginInfo.hasTop());
            setStyleName("jmix-outer-margin-right", marginInfo.hasRight());
            setStyleName("jmix-outer-margin-bottom", marginInfo.hasBottom());
            setStyleName("jmix-outer-margin-left", marginInfo.hasLeft());
        }
    }
}
