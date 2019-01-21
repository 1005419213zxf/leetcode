package com.litb.bid.object.bing;

public enum BingLitbAdChannel {

    bing_search(40),
    bing_content(123),
    bing_pla(192),
    bing_brand(430),
    bing_display(3200);

    private int channelId;

    private BingLitbAdChannel(int channelId) {
        this.channelId = channelId;
    }

    public static BingLitbAdChannel getLitbAdChannel(int channelId) {
        switch (channelId) {
            case 40:
                return bing_search;
            case 123:
                return bing_content;
            case 192:
                return bing_pla;
            case 430:
                return bing_brand;
            case 3200:
                return bing_display;
            default:
                throw new IllegalArgumentException("Cannot deal with channel id: " + channelId);
        }
    }

    public int getChannelId() {
        return this.channelId;
    }
}
