package sec.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sec.project.domain.Signup;
import sec.project.repository.SignupRepository;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;

@Controller
public class SignupController {

    @Autowired
    private SignupRepository signupRepository;

    @RequestMapping("/")
    public String defaultMapping() {
        return "redirect:/form";
    }


    @RequestMapping("/**")
    @ResponseBody
    public String handle404(HttpServletRequest request) {
        /*
        Vulnerability 1: Reflected XSS
         */
        return "<h1>Not Found</h1><p>Sorry, could not find " + URLDecoder.decode(request.getRequestURI()) + "</p>";

    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String loadForm() {
        return "form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public String submitForm(@RequestParam String name, @RequestParam String address) {
        signupRepository.save(new Signup(name, address));
        return "done";
    }

}
