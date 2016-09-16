package org.sunflower.webservice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

    @RequestMapping(value = "/")
    public String home() {
        System.out.println("HomeController: Passing through home...");
        //return "WEB-INF/views/bootstrap.jsp";
        return "home";
    }
    
    @RequestMapping(value = "compare/")
    public String bootstrap() {
        System.out.println("HomeController: Passing through compare...");
        return "compare";
    }
    
    @RequestMapping(value = "tagprofile/")
    public String tagprofile() {
        System.out.println("HomeController: Passing through tagprofile...");
        return "tagprofile";
    }
    
    @RequestMapping(value = "test/")
    @ResponseBody
    public String test() {
        
        return "current time is: "+System.currentTimeMillis();
    }
}
