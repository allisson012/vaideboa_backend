package com.example.vaideboa.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vaideboa.Dtos.ApiResponse;
import com.example.vaideboa.Dtos.EditarUserDto;
import com.example.vaideboa.Dtos.UserDto;
import com.example.vaideboa.Dtos.UserRetornoDto;

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
    public ResponseEntity<UserRetornoDto> buscarUser(Authentication authentication){
    String username = authentication.getName();
    return ResponseEntity.ok(userService.buscarUserPorUsername(username));
    }

    @DeleteMapping("/excluir")
    public ResponseEntity<?> excluirUsuario(Authentication auth){
        String username = auth.getName();
        boolean retorno = userService.excluirUsuario(username);
        if(retorno == false){
            return ResponseEntity.badRequest().body("Erro ao excluir usuário");
        }
        return ResponseEntity.ok("Usuário deletado com sucesso");
    }

    @PutMapping("/editar")
    public ResponseEntity<?> editarUser(@RequestBody EditarUserDto editarUserDto, Authentication auth){
        String username = auth.getName();
        ApiResponse resposta = userService.editarUsuario(editarUserDto, username);
        if(!resposta.isRetorno()){
            return ResponseEntity.badRequest().body(resposta.getMensagem());
        }
        return ResponseEntity.ok("Usuário editado com sucesso");
    } 

}
