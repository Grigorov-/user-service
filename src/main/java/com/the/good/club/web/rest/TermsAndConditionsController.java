package com.the.good.club.web.rest;

import com.the.good.club.core.data.User;
import com.the.good.club.core.service.TermsAndConditionsService;
import com.the.good.club.core.service.UserService;
import com.the.good.club.web.rest.assembler.UserResourceAssembler;
import com.the.good.club.web.rest.resource.TermsAndConditionsResource;
import com.the.good.club.web.rest.resource.UserResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
public class TermsAndConditionsController {

    private final TermsAndConditionsService service;

    public TermsAndConditionsController(TermsAndConditionsService service) {
        this.service = service;
    }

    @PostMapping("/termsAndConditions")
    public ResponseEntity<TermsAndConditionsResource> registerUser(@RequestBody TermsAndConditionsResource requestResource) throws Exception {
        String status = service.registerTermsAndConditions(requestResource.getUrl());

        TermsAndConditionsResource response = requestResource.toBuilder().status(status).build();
        return new ResponseEntity<>(response, OK);
    }

}
