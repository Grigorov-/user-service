package com.the.good.club.web.rest;

import com.the.good.club.core.data.User;
import com.the.good.club.core.data.UserStatus;
import com.the.good.club.core.service.UserService;
import com.the.good.club.web.rest.assembler.UserResourceAssembler;
import com.the.good.club.web.rest.resource.UserResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
public class UsersController {

    private final UserService userService;
    private final UserResourceAssembler userResourceAssembler;

    public UsersController(UserService userService, UserResourceAssembler userResourceAssembler) {
        this.userService = userService;
        this.userResourceAssembler = userResourceAssembler;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResource> registerUser(@RequestBody UserResource request) {
        User requestUser = userResourceAssembler.toUser(request, UserStatus.PENDING_CORRELATION);

        User user = userService.requestCorrelationWithUser(requestUser);

        return new ResponseEntity<>(userResourceAssembler.toResource(user), OK);
    }

    @GetMapping("users/{id}")
    public ResponseEntity<UserResource> getUser(@PathVariable String id) {
        Optional<User> user = userService.getById(id);

        return user.map(u -> new ResponseEntity<>(userResourceAssembler.toResource(u), OK))
        .orElseGet(() -> ResponseEntity.status(NOT_FOUND).build());
    }

    @GetMapping("users")
    public ResponseEntity<List<UserResource>> getUsers(@RequestParam String status,
                                                       @RequestParam String company,
                                                       @RequestParam(required = false) String startDate,
                                                       @RequestParam(required = false) String endDate) {
        List<User> users = userService.getUsersByFilters(status, company, startDate, endDate);

        return new ResponseEntity<>(users.stream().map(userResourceAssembler::toResource).toList(), OK);
    }

}
