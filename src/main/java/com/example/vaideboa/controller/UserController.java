package com.example.vaideboa.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.vaideboa.Dtos.UserDto;
import com.example.vaideboa.Dtos.UserRetornoDto;
import com.example.vaideboa.model.User;
import com.example.vaideboa.service.UserService;



@RestController
@RequestMapping("/user")
public class UserController {
    private UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarUser(@RequestBody UserDto userDto){
        boolean retorno = userService.cadastrarUser(userDto);
        if(retorno)
        {
            return ResponseEntity.ok("Usuário criado com sucesso");
        }
        return ResponseEntity.badRequest().body("Erro ao criar Usuário"); 
    }  
    
    @GetMapping("/me")
    public ResponseEntity<UserRetornoDto> buscarUser(@AuthenticationPrincipal Jwt jwt){
        String username = jwt.getSubject();
        return ResponseEntity.ok(userService.buscarUserPorId(username));
    }

}
