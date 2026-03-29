package com.tunehive.musicplayer.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.tunehive.musicplayer.model.User;
import com.tunehive.musicplayer.repository.UserRepository;
import com.tunehive.musicplayer.storage.TempStorage;

import java.util.List;
import java.util.Random;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    // ─────────────────────────────────────────
    // HOME
    // ─────────────────────────────────────────
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        model.addAttribute("isLoggedIn", session.getAttribute("mobile") != null);
        return "index";
    }

    // ─────────────────────────────────────────
    // SIGNUP / OTP
    // ─────────────────────────────────────────
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("mobile") String mobile,
                          @RequestParam(value = "name",  required = false) String name,
                          @RequestParam(value = "email", required = false) String email,
                          HttpSession session,
                          Model model) {

        if (!mobile.matches("^[6-9]\\d{9}$")) {
            model.addAttribute("error", "Enter a valid 10-digit mobile number");
            return "signup";
        }

        int otp = 100000 + new Random().nextInt(900000);

        session.setAttribute("otp",     otp);
        session.setAttribute("mobile",  mobile);
        session.setAttribute("name",    name);
        session.setAttribute("email",   email);
        session.setAttribute("otpTime", System.currentTimeMillis());

        // FOR TESTING — remove in production
        System.out.println("OTP for " + mobile + ": " + otp);

        model.addAttribute("otp",    otp);   // show OTP on page during dev
        model.addAttribute("mobile", mobile);

        return "otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("userOtp") int userOtp,
                            HttpSession session,
                            Model model) {

        Integer sessionOtp = (Integer) session.getAttribute("otp");
        String  mobile     = (String)  session.getAttribute("mobile");
        String  name       = (String)  session.getAttribute("name");
        String  email      = (String)  session.getAttribute("email");
        Long    otpTime    = (Long)    session.getAttribute("otpTime");

        if (sessionOtp == null || mobile == null || otpTime == null) {
            model.addAttribute("error", "Session expired. Try again.");
            return "signup";
        }

        if (System.currentTimeMillis() - otpTime > 300000) { // 5 min
            model.addAttribute("error", "OTP expired. Request again.");
            return "otp";
        }

        if (userOtp != sessionOtp) {
            model.addAttribute("error", "Invalid OTP. Try again.");
            return "otp";
        }

        // OTP correct — register user and set session
        session.removeAttribute("otp");
        session.removeAttribute("otpTime");

        // Save to TempStorage
        if (!TempStorage.users.containsKey(mobile)) {
            User user = new User();
            user.setMobile(mobile);
            user.setEmail(email);
            user.setPremium(false);
            user.setPlan("FREE");
            TempStorage.users.put(mobile, user);
        }

        session.setAttribute("mobile",  mobile);
        session.setAttribute("premium", false);
        session.setAttribute("plan",    "FREE");
        session.setAttribute("email",   email);

        return "redirect:/dashboard";
    }

    // ─────────────────────────────────────────
    // LOGIN  ← KEY FIX: accepts any registered mobile
    // ─────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

   @PostMapping("/do-login")
public String doLogin(@RequestParam String mobile,
                      HttpSession session,
                      Model model) {

    if (mobile == null || mobile.isEmpty()) {
        model.addAttribute("error", "Enter mobile number");
        return "login";
    }

    session.setAttribute("mobile", mobile);

    return "redirect:/dashboard";  // ✅ VERY IMPORTANT
}
    // ─────────────────────────────────────────
    // DASHBOARD
    // ─────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String mobile = (String) session.getAttribute("mobile");
        if (mobile == null) return "redirect:/login";
        model.addAttribute("mobile",  mobile);
        model.addAttribute("premium", session.getAttribute("premium"));
        model.addAttribute("plan",    session.getAttribute("plan"));
        return "dashboard";
    }

    // ─────────────────────────────────────────
    // PLANS & PAYMENT
    // ─────────────────────────────────────────
    @GetMapping("/plans")
    public String plansPage(HttpSession session) {
        if (session.getAttribute("mobile") == null) return "redirect:/login";
        return "plans";
    }

    @GetMapping("/payment")
    public String paymentPage(@RequestParam("plan") String plan,
                              HttpSession session, Model model) {
        if (session.getAttribute("mobile") == null) return "redirect:/login";
        model.addAttribute("plan", plan);
        return "payment";
    }

    @GetMapping("/payment-success")
    public String paymentSuccess(HttpSession session, Model model) {
        String mobile = (String) session.getAttribute("mobile");
        if (mobile == null) return "redirect:/login";

        User user = TempStorage.users.get(mobile);
        if (user != null) {
            user.setPremium(true);
            user.setPlan("PRO");
        }

        session.setAttribute("premium", true);
        session.setAttribute("plan",    "PRO");

        model.addAttribute("plan",   "PRO");
        model.addAttribute("amount", "₹99");

        return "success";
    }

    // ─────────────────────────────────────────
    // PLAYER
    // ─────────────────────────────────────────
    @GetMapping("/player")
    public String playerPage(HttpSession session, Model model) {
        String mobile = (String) session.getAttribute("mobile");
        if (mobile == null) return "redirect:/login";
        model.addAttribute("mobile",  mobile);
        model.addAttribute("premium", session.getAttribute("premium"));
        return "player";
    }

    // ─────────────────────────────────────────
    // CHECK USER
    // ─────────────────────────────────────────
    @GetMapping("/check-user")
    public String checkUser(HttpSession session) {
        if (session.getAttribute("mobile") != null) return "redirect:/dashboard";
        return "redirect:/login";
    }

    // ─────────────────────────────────────────
    // LIBRARY PAGES — all require login
    // ─────────────────────────────────────────
    @GetMapping("/podcasts")
    public String podcastsPage(HttpSession session) {
        if (session.getAttribute("mobile") == null) return "redirect:/login";
        return "podcasts";
    }

    @GetMapping("/history")
    public String historyPage(HttpSession session) {
        if (session.getAttribute("mobile") == null) return "redirect:/login";
        return "history";
    }

    @GetMapping("/wishlist")
    public String wishlistPage(HttpSession session) {
        if (session.getAttribute("mobile") == null) return "redirect:/login";
        return "wishlist";
    }

    @GetMapping("/albums")
    public String albumsPage(HttpSession session) {
        if (session.getAttribute("mobile") == null) return "redirect:/login";
        return "albums";
    }

    @GetMapping("/artists")
    public String artistsPage(HttpSession session) {
        if (session.getAttribute("mobile") == null) return "redirect:/login";
        return "artists";
    }

    @GetMapping("/playlist")
    public String playlistPage(HttpSession session) {
        if (session.getAttribute("mobile") == null) return "redirect:/login";
        return "playlist";
    }

    @GetMapping("/playlist-details")
    public String playlistDetails(HttpSession session) {
        if (session.getAttribute("mobile") == null) return "redirect:/login";
        return "playlist-details";
    }

    // ─────────────────────────────────────────
    // ACCOUNT PAGE
    // ─────────────────────────────────────────
    @GetMapping("/account")
    public String accountPage(HttpSession session, Model model) {
        String mobile = (String) session.getAttribute("mobile");
        if (mobile == null) return "redirect:/login";

        User user = TempStorage.users.get(mobile);
        String email = null;
        String plan  = "FREE";

        if (user != null) {
            email = user.getEmail();
            plan  = user.getPlan() != null ? user.getPlan() : (user.isPremium() ? "PRO" : "FREE");
        }

        if ("FREE".equals(plan) && Boolean.TRUE.equals(session.getAttribute("premium"))) {
            plan = "PRO";
        }

        model.addAttribute("mobile", mobile);
        model.addAttribute("email",  email);
        model.addAttribute("plan",   plan);

        return "account";
    }

    // ─────────────────────────────────────────
    // SETTINGS
    // ─────────────────────────────────────────
    @GetMapping("/settings")
    public String settingsPage(HttpSession session) {
        if (session.getAttribute("mobile") == null) return "redirect:/login";
        return "settings";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String mobile,
                                @RequestParam(required = false) String email,
                                HttpSession session) {

        String oldMobile = (String) session.getAttribute("mobile");

        List<User> users = userRepo.findAllByMobile(oldMobile);
        if (!users.isEmpty()) {
            User user = users.get(0);
            user.setMobile(mobile);
            if (email != null && !email.isBlank()) {
                user.setEmail(email);
                session.setAttribute("email", email);
            }
            userRepo.save(user);
        }

        if (TempStorage.users.containsKey(oldMobile)) {
            User tempUser = TempStorage.users.remove(oldMobile);
            tempUser.setMobile(mobile);
            if (email != null && !email.isBlank()) tempUser.setEmail(email);
            TempStorage.users.put(mobile, tempUser);
        }

        session.setAttribute("mobile", mobile);
        return "redirect:/account";
    }

    // ─────────────────────────────────────────
    // HELP
    // ─────────────────────────────────────────
    @GetMapping("/help")
    public String helpPage() { return "help"; }

    // ─────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────
    @GetMapping("/custom-logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}