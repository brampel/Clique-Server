package com.fordexplorer.clique.controller;

import com.fordexplorer.clique.auth.JwtTokenManager;
import com.fordexplorer.clique.data.Group;
import com.fordexplorer.clique.data.Location;
import com.fordexplorer.clique.data.Person;
import com.fordexplorer.clique.db.GroupRepository;
import com.fordexplorer.clique.db.PersonRepository;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class APIController {
    @Autowired
    private GroupRepository groupRepository;
    private PersonRepository personRepository;
    private JwtTokenManager jwtTokenManager;

    /* Account management */

    //Create user
    @PostMapping("/registerUser")
    public ResponseEntity<String> registerUser(@RequestBody Person person){
        personRepository.save(person);
        String token = String.format("{token: %s}", jwtTokenManager.createToken(person.getUsername()));
        return new ResponseEntity<>(token,HttpStatus.OK);
    }
    //Login
    //login(credentials) -> JWT Token
    @PostMapping("/login")
    public ResponseEntity<String> login(String username, String password){
        Person person = personRepository.findPersonByUsername(username);
        if(person == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if(person.getPassword().equals(password)){
            String token = String.format("{token: %s}", jwtTokenManager.createToken(username));
            return new ResponseEntity<>(token,HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    //Get user profile
    @GetMapping("/getProfile/{username}")
    public Person getProfile(@PathVariable String username) {
        return personRepository.findPersonByUsername(username);
    }

    /* Group management */

    //Create group
    @PostMapping("/createGroup")
    public void createGroup(@RequestBody String username){
        Group group = new Group();
        //Add the creater of the group
        group.addMember(personRepository.findPersonByUsername(username));
        groupRepository.save(group);
    }
    //Get groups near user
    @GetMapping("/getGroups")
    public List<Group> getGroups(@RequestBody Location location){
        List<Group> result = new ArrayList<>();
        for(Group g : groupRepository.findAll()){
            //if group is within 1 mile of specified location
            if(g.getLocation().distanceTo(location) < 1){
                result.add(g);
            }
        }
        return result;
    }
    //Get group info, including people wanting to join group
    @GetMapping("/getGroup/{id}")
    public Group getGroup(@RequestParam Long id){
        if(!groupRepository.findById(id).isPresent()) return null;
        return groupRepository.findById(id).get();
    }
}
