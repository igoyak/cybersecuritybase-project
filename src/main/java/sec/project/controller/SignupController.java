package sec.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sec.project.domain.Account;
import sec.project.repository.SignupRepository;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Optional;

@Controller
public class SignupController {

    @Autowired
    private SignupRepository signupRepository;

    @RequestMapping("/")
    public String defaultMapping() {
        return "redirect:/login";
    }

    @RequestMapping("/**")
    @ResponseBody
    public ResponseEntity handle404(HttpServletRequest request) {
        /*
        Vulnerability 1: A3-Cross-Site Scripting (XSS)
         */
        String resp = "<h1>Not Found</h1><p>Sorry, could not find " + URLDecoder.decode(request.getRequestURI()) + "</p>";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);

    }

    @RequestMapping(value = "/createAccount", method = RequestMethod.POST)
    public String createAccount(@RequestParam String username, @RequestParam String password, HttpServletResponse response) {
        if (signupRepository.findAll()
                .stream()
                .anyMatch(a -> a.getUsername().equals(username))) {
            // Account already exists
            return "redirect:/login";
        }
        Account newAccount = signupRepository.save(new Account(username, password));
        response.addCookie(new Cookie("accountid", newAccount.getId().toString()));
        return "redirect:/account/";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(HttpServletResponse response, @RequestParam Optional<String> redirect) {
        if (redirect.isPresent()) {
            /*
            Vulnerability 5: A10-Unvalidated Redirects and Forwards
             */
            return "redirect:" + redirect.get();
        }
        response.addCookie(new Cookie("accountid", ""));
        return "login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String loginPost(@RequestParam String username, @RequestParam String password, HttpServletResponse response) {
        Optional<Account> account = signupRepository.findAll()
                .stream()
                .filter(a -> a.getUsername().equals(username) && a.getPassword().equals(password)).findFirst();
        System.out.println("login account: ");
        System.out.println(account);
        if (account.isPresent()) {
            System.out.println(account.get());
            System.out.println(account.get().getUsername());
            System.out.println(account.get().getPassword());
            Long id = account.get().getId();
            response.addCookie(new Cookie("accountid", id.toString()));

            return "redirect:/account/";
        }
        return "redirect:/login";
    }


    @RequestMapping(value = "/account", method = RequestMethod.GET)
    public String account(Model model, @CookieValue(value = "accountid", defaultValue = "") String accountIDString) {
        /*
        Vulnerability 3: A2-Broken Authentication and Session Management

        The authentication cookie is simply the account ID, which is easily guessable.
         */
        System.out.println("/account/ cookie is: " + accountIDString);
        try {
            Long accountID = Long.parseLong(accountIDString, 10);
            Account a = signupRepository.getOne(accountID);
            System.out.println(a);
            model.addAttribute("username", a.getUsername());
            model.addAttribute("password", a.getPassword());
            model.addAttribute("accounts", signupRepository.findAll());
            return "account";
        } catch (EntityNotFoundException | NumberFormatException e) {
            return "redirect:/login";
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String delete(Model model, @CookieValue(value = "accountid", defaultValue = "") String accountIDString, HttpServletResponse response) {
        /*
         Vulnerability 4: A8-Cross-Site Request Forgery (CSRF)
         */
        System.out.println("/delete/ cookie is: " + accountIDString);
        try {
            Long accountID = Long.parseLong(accountIDString, 10);
            Account a = signupRepository.getOne(accountID);
            signupRepository.delete(accountID);
            System.out.println("Deleted account: " + accountID.toString());
        } catch (EntityNotFoundException | NumberFormatException e) {
        }
        return "redirect:/login";
    }

    @RequestMapping(value = "/deleteall", method = RequestMethod.POST)
    public String deleteAll(Model model) {
        /*
         Vulnerability 2: A7-Missing Function Level Access Control

         This should only be available for Admin, but anyone with the URL can
         call this.
          */
        signupRepository.deleteAll();
        return "redirect:/login";
    }

}
