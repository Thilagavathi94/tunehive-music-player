package com.tunehive.musicplayer.controller;


import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.tunehive.musicplayer.model.User;
import com.tunehive.musicplayer.repository.UserRepository;
 
import java.util.List;
import java.util.Random;
 
@Controller
public class AuthController {
 
    @Autowired
    private UserRepository userRepo;
 
    // ─── HOME ────────────────────────────────────────────────────────────────
    @GetMapping("/")
    public String home() {
        return "index";
    }
 
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }
 
    // ─── STEP 1: Submit mobile number ────────────────────────────────────────
    // FIX 1: Do NOT save to DB here. Just send OTP and store mobile in session.
    // Previously the code was skipping OTP entirely for new users and
    // redirecting straight to /player for existing users without verifying.
    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("mobile") String mobile,
                          HttpSession session,
                          Model model) {
 
        // Validate: must be 10 digits
        if (mobile == null || !mobile.matches("\\d{10}")) {
            model.addAttribute("error", "Enter a valid 10-digit mobile number");
            return "signup";
        }
 
        // Generate OTP and store in session (NOT in DB yet)
        int otp = 100000 + new Random().nextInt(900000);
        System.out.println("OTP for " + mobile + ": " + otp); // replace with real SMS service
 
        session.setAttribute("pendingMobile", mobile);
        session.setAttribute("otp", otp);
 
        model.addAttribute("mobile", mobile);
        model.addAttribute("otp", otp); // remove this line in production!
        return "otp";
    }
 
    // ─── STEP 2: Verify OTP ──────────────────────────────────────────────────
    // FIX 2: Save to DB ONLY after OTP is verified.
    // FIX 3: Existing users also go through OTP — never skip verification.
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("userOtp") String userOtp,
                            HttpSession session,
                            Model model) {
 
        Integer sessionOtp = (Integer) session.getAttribute("otp");
        String mobile = (String) session.getAttribute("pendingMobile");
 
        if (sessionOtp == null || mobile == null) {
            return "redirect:/signup";
        }
 
        if (!userOtp.matches("\\d{6}")) {
            model.addAttribute("error", "Enter a valid 6-digit OTP");
            model.addAttribute("mobile", mobile);
            return "otp";
        }
 
        if (Integer.parseInt(userOtp) != sessionOtp) {
            model.addAttribute("error", "Invalid OTP. Try again.");
            model.addAttribute("mobile", mobile);
            return "otp";
        }
 
        // OTP correct — clear OTP from session
        session.removeAttribute("otp");
        session.removeAttribute("pendingMobile");
        session.setAttribute("mobile", mobile);
 
        // Check if user already exists
        List<User> existing = userRepo.findAllByMobile(mobile);
 
        if (!existing.isEmpty()) {
            // Returning user — just log them in
            User user = existing.get(0);
            session.setAttribute("premium", user.isPremium());
            session.setAttribute("userId", user.getId());
            return "redirect:/player";
        }
 
        // New user — save to DB NOW (after OTP verified)
        User user = new User();
        user.setMobile(mobile);
        user.setPremium(false);
        user.setPlan("FREE");
        user.setPlayCount(0); // FIX 4: track play count in DB
        userRepo.save(user);
 
        session.setAttribute("premium", false);
        session.setAttribute("userId", user.getId());
 
        return "redirect:/dashboard";
    }
 
    // ─── DASHBOARD ───────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String mobile = (String) session.getAttribute("mobile");
        if (mobile == null) return "redirect:/signup";
 
        List<User> users = userRepo.findAllByMobile(mobile);
        if (!users.isEmpty()) {
            model.addAttribute("user", users.get(0));
        }
        return "dashboard";
    }
 
    // ─── PLAYER ──────────────────────────────────────────────────────────────
    // FIX 5: Load isPremium fresh from DB on every player page load,
    // not just from session (session can be stale after payment).
    @GetMapping("/player")
    public String playerPage(HttpSession session, Model model) {
        String mobile = (String) session.getAttribute("mobile");
        if (mobile == null) return "redirect:/signup";
 
        List<User> users = userRepo.findAllByMobile(mobile);
        if (users.isEmpty()) return "redirect:/signup";
 
        User user = users.get(0);
 
        // Always sync session premium status from DB
        session.setAttribute("premium", user.isPremium());
        model.addAttribute("isPremium", user.isPremium());
        model.addAttribute("playCount", user.getPlayCount());
 
        return "player";
    }
 
    // ─── PLAY SONG (server-side play count) ──────────────────────────────────
    // FIX 6: Play limit enforced server-side, not just in JS.
    // JS check in player.html is still useful for UX, but this is the real gate.
    @PostMapping("/play-song")
    @ResponseBody
    public String playSong(HttpSession session) {
        String mobile = (String) session.getAttribute("mobile");
        if (mobile == null) return "UNAUTHORIZED";
 
        List<User> users = userRepo.findAllByMobile(mobile);
        if (users.isEmpty()) return "UNAUTHORIZED";
 
        User user = users.get(0);
 
        if (user.isPremium()) return "OK";
 
        if (user.getPlayCount() >= 1) return "LIMIT_REACHED";
 
        // Increment and save
        user.setPlayCount(user.getPlayCount() + 1);
        userRepo.save(user);
 
        return "OK";
    }
 
    // ─── PLANS ───────────────────────────────────────────────────────────────
    @GetMapping("/plans")
    public String plansPage() {
        return "plans";
    }
 
    // ─── PAYMENT ─────────────────────────────────────────────────────────────
    @GetMapping("/payment")
    public String paymentPage(@RequestParam("plan") String plan, Model model) {
        model.addAttribute("plan", plan);
        return "payment";
    }
 
    // ─── PAYMENT SUCCESS ─────────────────────────────────────────────────────
    // FIX 7: payment.html uses a plain GET form — anyone can hit /payment-success
    // directly without paying. For a real app, verify with Razorpay/Stripe API.
    // Minimum fix: guard with session check + idempotent premium update.
    @GetMapping("/payment-success")
    public String paymentSuccess(HttpSession session, Model model) {
        String mobile = (String) session.getAttribute("mobile");
        if (mobile == null) return "redirect:/signup";
 
        List<User> users = userRepo.findAllByMobile(mobile);
        if (users.isEmpty()) return "redirect:/signup";
 
        User user = users.get(0);
        user.setPremium(true);
        user.setPlan("PRO");
        user.setPlayCount(0); // reset count after upgrade
        userRepo.save(user);
 
        // Sync session
        session.setAttribute("premium", true);
 
        model.addAttribute("plan", user.getPlan());
        model.addAttribute("amount", "₹99");
 
        return "success";
    }
 
    // ─── MISC PAGES ──────────────────────────────────────────────────────────
    @GetMapping("/check-user")
    public String checkUser(HttpSession session) {
        return session.getAttribute("mobile") != null ? "redirect:/player" : "redirect:/signup";
    }
 
    @GetMapping("/podcasts")
    public String podcastsPage() { return "podcasts"; }
 
    @GetMapping("/history")
    public String historyPage() { return "history"; }
 
    @GetMapping("/wishlist")
    public String wishlistPage() { return "wishlist"; }
 
    @GetMapping("/albums")
    public String albumsPage() { return "albums"; }
 
    @GetMapping("/artists")
    public String artistsPage() { return "artists"; }
 
    @GetMapping("/playlist")
    public String playlistPage() { return "playlist"; }
 
    @GetMapping("/playlist-details")
    public String playlistDetails() { return "playlist-details"; }
 
    @GetMapping("/account")
    public String accountPage() { return "account"; }
 
    @GetMapping("/settings")
    public String settingsPage() { return "settings"; }
 
    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String mobile, HttpSession session) {
        String oldMobile = (String) session.getAttribute("mobile");
        if (oldMobile == null) return "redirect:/signup";
 
        List<User> users = userRepo.findAllByMobile(oldMobile);
        if (users.isEmpty()) return "redirect:/signup";
 
        User user = users.get(0);
        user.setMobile(mobile);
        userRepo.save(user);
        session.setAttribute("mobile", mobile);
 
        return "redirect:/account";
    }
 
    @GetMapping("/custom-logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "logout";
    }
}