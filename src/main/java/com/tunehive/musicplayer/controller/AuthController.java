package com.tunehive.musicplayer.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tunehive.musicplayer.model.User;
import com.tunehive.musicplayer.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class AuthController {
    
@Autowired
private UserRepository userRepo;
@GetMapping("/")
public String home(HttpSession session){
    return "index";   // ✅ always allow home
}// 👈 instead of signup


@PostMapping("/send-otp")
public String sendOtp(@RequestParam("mobile") String mobile,
                      HttpSession session,
                      Model model) {

    List<User> users = new ArrayList<>();

    try {
        users = userRepo.findAllByMobile(mobile);
    } catch (Exception e) {
        System.out.println("DB Error: " + e.getMessage());
    }

    // ✅ USER EXISTS → DIRECT LOGIN
    if(users != null && !users.isEmpty()){
        User user = users.get(0);

        session.setAttribute("mobile", mobile);
        session.setAttribute("premium", user.isPremium());

        return "redirect:/player";
    }

    // ❌ NEW USER → OTP
    int otp = 100000 + new Random().nextInt(900000);

    System.out.println("OTP: " + otp);

    session.setAttribute("otp", otp);
    session.setAttribute("mobile", mobile);

    model.addAttribute("mobile", mobile);
    model.addAttribute("otp", otp);   // ✅ ADD THIS LINE

    return "otp";
}
@PostMapping("/verify-otp")
public String verifyOtp(@RequestParam("userOtp") String userOtp,
                        HttpSession session,
                        Model model) {

    Integer sessionOtp = (Integer) session.getAttribute("otp");
    String mobile = (String) session.getAttribute("mobile");

   if (sessionOtp == null || mobile == null) {
    return "redirect:/signup";   // ✅ NOT "signup"
}

    try {
        int enteredOtp = Integer.parseInt(userOtp);

        if (enteredOtp == sessionOtp) {

            List<User> users = userRepo.findAllByMobile(mobile);

            if (users.isEmpty()) {
                User user = new User();
                user.setMobile(mobile);
                user.setPremium(false);
                user.setPlan("FREE");

                userRepo.save(user);

                session.setAttribute("premium", false);

                return "redirect:/dashboard";
            }

            session.setAttribute("premium", users.get(0).isPremium());

            return "redirect:/player";
        }

    } catch (Exception e) {
        model.addAttribute("error", "Invalid OTP format");
         model.addAttribute("mobile", mobile); 
        return "otp";
    }

    model.addAttribute("error", "Invalid OTP");
    model.addAttribute("mobile", mobile);
    return "otp";
}
 @GetMapping("/dashboard")
public String dashboard(HttpSession session, Model model) {

    String mobile = (String) session.getAttribute("mobile");

    if(mobile == null){
        return "redirect:/signup";
    }

    // 🔥 Get user from DB
    List<User> users = userRepo.findAllByMobile(mobile);

    if(!users.isEmpty()){
        model.addAttribute("user", users.get(0));
    }

    return "dashboard";
}
    @GetMapping("/plans")
public String plansPage() {
    return "plans";
}
@GetMapping("/payment")
public String paymentPage(@RequestParam("plan") String plan, Model model) {
    model.addAttribute("plan", plan);
    return "payment";
}
@GetMapping("/payment-success")
public String paymentSuccess(HttpSession session, Model model) {

    String mobile = (String) session.getAttribute("mobile");

    if(mobile == null){
        return "redirect:/signup"; // 🔥 FIX
    }

    List<User> users = userRepo.findAllByMobile(mobile);

    if(users.isEmpty()){
        return "redirect:/signup"; // 🔥 FIX
    }

    User user = users.get(0);

    if(!user.isPremium()){
        user.setPremium(true);
        user.setPlan("PRO");
        userRepo.save(user);
    }

    session.setAttribute("premium", true);

    model.addAttribute("plan", user.getPlan());
    model.addAttribute("amount", "₹99");

    return "success";
}
  @GetMapping("/signup")
    public String signupPage(){
        return "signup"; // 👉 loads signup.html
    }
    @GetMapping("/player")
public String playerPage(HttpSession session) {

    // ✅ check login
    if(session.getAttribute("mobile") == null){
        return "redirect:/signup";
    }

    return "player"; // opens player.html
}
@GetMapping("/check-user")
public String checkUser1(HttpSession session) {

    String mobile = (String) session.getAttribute("mobile");

    // ✅ already logged in
    if(mobile != null){
        return "redirect:/player";
    }

    // ❌ not logged in → go signup
    return "redirect:/signup";
}


@GetMapping("/podcasts")
public String podcastsPage(){
    return "podcasts";
}
@GetMapping("/history")
public String historyPage(){
    return "history";
}
@GetMapping("/wishlist")
public String wishlistPage(){
    return "wishlist";
}
@GetMapping("/albums")
public String albumsPage(){
    return "albums";
}
@GetMapping("/artists")
public String artistsPage(){
    return "artists";
}
@GetMapping("/playlist")
public String playlistPage(){
    return "playlist";
}

@GetMapping("/playlist-details")
public String playlistDetails(){
    return "playlist-details";
}
// ACCOUNT PAGE
@GetMapping("/account")
public String accountPage(){

   

    return "account";
}


// SETTINGS PAGE
@GetMapping("/settings")
public String settingsPage(){

   
    return "settings";
}


// UPDATE PROFILE
@PostMapping("/update-profile")
public String updateProfile(@RequestParam String mobile, HttpSession session){

    String oldMobile = (String) session.getAttribute("mobile");

    User user = userRepo.findAllByMobile(oldMobile).get(0);

    user.setMobile(mobile);
    userRepo.save(user);

    session.setAttribute("mobile", mobile);

    return "redirect:/account";
}


// LOGOUT
@GetMapping("/custom-logout")
public String logout(HttpSession session){

    session.invalidate(); // clear session

    return "logout"; // 👈 show logout.html
}
}
