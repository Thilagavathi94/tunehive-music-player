package com.tunehive.musicplayer.model;

import jakarta.persistence.*;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String mobile;
    private String plan;
    private boolean premium;

    // FIX: Added playCount to track plays server-side.
    // Previously this was only tracked in JS (localStorage / variable),
    // which resets on every page refresh — making the limit useless.
    @Column(columnDefinition = "integer default 0")
    private int playCount;

    public long getId() { return id; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public boolean isPremium() { return premium; }
    public void setPremium(boolean premium) { this.premium = premium; }

    public int getPlayCount() { return playCount; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }
}