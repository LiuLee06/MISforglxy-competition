package com.sdjzuxg.collegemanagesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
@RequestMapping("/index")
public class CollegeManageSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(CollegeManageSystemApplication.class, args);
    }

  //GetMapping 包含 @RequestMapping
//    @GetMapping("/hello")
//    public String index() {
//        return "Get方法";
//    }
//
//    //restful传参风格
//    @GetMapping("/hello/{id}")
//    public String hello(@PathVariable String id) {
//        return "id�? + id;
//    }
//
//    //普通传�?
//    @GetMapping("/hello3")
//    public String hello1(@RequestParam String id,@RequestParam String name){
//        return id+name;
//    }
//
//    //post请求
//    @PostMapping
//    public String save(@RequestBody String id){
//        return id;
//    }

}
