package com.kpi.controllers;

import com.kpi.dto.UserDTO;
import com.kpi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<Void> createUser(@RequestBody UserDTO user) throws Exception {
        return userService.createUser(user) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteUser(@RequestParam("username") String username) throws Exception {
        return userService.deleteUser(username) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = "/retrieve", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> getUser(@RequestParam("username") String username) throws Exception {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }
}
