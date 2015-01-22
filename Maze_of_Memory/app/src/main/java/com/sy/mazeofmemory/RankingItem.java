package com.sy.mazeofmemory;

/**
 * Created by Jun on 2015-01-22.
 */
public class RankingItem {
    private String pictureUrl;
    private String nickname;
    private int starCnt;
    private int score;

    // constructor
    public RankingItem( String pictureUrl, String nickname, int starCnt, int score) {
        this.pictureUrl = pictureUrl;
        this.nickname = nickname;
        this.starCnt = starCnt;
        this.score = score;
    }

    public String getPictureUrl() {
        return this.pictureUrl;
    }

    public String getNickname() {
        return this.nickname;
    }

    public int getStarCnt() {
        return this.starCnt;
    }

    public int getScore() {
        return this.score;
    }
}
