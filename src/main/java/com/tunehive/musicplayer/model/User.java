package com.tunehive.musicplayer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Core fields ──────────────────────────────────
    @Column(nullable = false, unique = true)
    private String mobile;

    @Column
    private String email;           // set by user in settings

    // ── Subscription ─────────────────────────────────
    @Column(nullable = false)
    private boolean premium = false;

    @Column
    private String plan = "FREE";   // "FREE" or "PRO"

    // ── Constructors ─────────────────────────────────
    public User() {}

    public User(String mobile) {
        this.mobile  = mobile;
        this.premium = false;
        this.plan    = "FREE";
    }

    // ── Getters & Setters ────────────────────────────
    public Long getId()               { return id; }
    public void setId(Long id)        { this.id = id; }

    public String getMobile()              { return mobile; }
    public void   setMobile(String mobile) { this.mobile = mobile; }

    public String getEmail()             { return email; }
    public void   setEmail(String email) { this.email = email; }

    public boolean isPremium()                { return premium; }
    public void    setPremium(boolean premium) { this.premium = premium; }

    public String getPlan()            { return plan; }
    public void   setPlan(String plan) { this.plan = plan; }
}