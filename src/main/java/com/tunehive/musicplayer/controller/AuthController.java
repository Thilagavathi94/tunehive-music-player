package com.tunehive.musicplayer.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tunehive.musicplayer.model.User;
import com.tunehive.musicplayer.repository.UserRepository;

import java.util.List;
import java.util.Random;

@Controller
public class AuthController {
    
@Autowired
private UserRepository userRepo;
@GetMapping("/")
public String home(HttpSession session){
    if(session.getAttribute("mobile") != null){
        return "redirect:/player";
    }
    return "index";
} // 👈 instead of signup


 @PostMapping("/send-otp")
public String sendOtp(@RequestParam("mobile") String mobile,
                      HttpSession session,
                      Model model) {

    // 🔥 CHECK DB FIRST
    List<User> users = userRepo.findAllByMobile(mobile);

    // ✅ USER EXISTS → DIRECT LOGIN
    if(!users.isEmpty()){
        User user = users.get(0);

        session.setAttribute("mobile", mobile);
        session.setAttribute("premium", user.isPremium());

        return "redirect:/player"; // 🔥 IMPORTANT
    }

    // ❌ NEW USER → OTP
    int otp = 100000 + new Random().nextInt(900000);

    System.out.println("OTP: " + otp);

    session.setAttribute("otp", otp);
    session.setAttribute("mobile", mobile);

    model.addAttribute("mobile", mobile);

    return "otp";
}
 @PostMapping("/verify-otp")
public String verifyOtp(@RequestParam("userOtp") int userOtp,
                        HttpSession session) {

    int sessionOtp = (int) session.getAttribute("otp");
    String mobile = (String) session.getAttribute("mobile");

    if(userOtp == sessionOtp){

        // 🔥 CHECK AGAIN BEFORE SAVE
        List<User> users = userRepo.findAllByMobile(mobile);

        if(users.isEmpty()){
            User user = new User();
            user.setMobile(mobile);
            user.setPremium(false);
            user.setPlan("FREE");

            userRepo.save(user);
        }

        session.setAttribute("premium", false);

        return "redirect:/player"; // 🔥 change from dashboard
    }

    return "otp";
}
  @GetMapping("/dashboard")
public String dashboard(HttpSession session) {

    if(session.getAttribute("mobile") == null){
        return "redirect:/signup";
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
        return "redirect:/signup";
    }

    List<User> users = userRepo.findAllByMobile(mobile);
    User user = users.get(0);

    // update user
    user.setPremium(true);
    user.setPlan("PRO");
    userRepo.save(user);

    session.setAttribute("premium", true);

    // ✅ send data to UI
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
@GetMapping("/logout")
public String logout(HttpSession session){

    session.invalidate(); // 🔥 clear session

    return "redirect:/"; // go to home page
}
@GetMapping("/account")
public String accountPage(HttpSession session, Model model){

    String mobile = (String) session.getAttribute("mobile");

    if(mobile == null){
        return "redirect:/signup";
    }

    // get user from DB
    User user = userRepo.findAllByMobile(mobile).get(0);

    model.addAttribute("user", user);

    return "account";
}
@GetMapping("/podcasts")
public String podcastsPage(){
    return "podcasts";
}
@GetMapping("/history")
public String historyPage(HttpSession session){

    if(session.getAttribute("mobile") == null){
        return "redirect:/signup";
    }

    return "history";
}
}
