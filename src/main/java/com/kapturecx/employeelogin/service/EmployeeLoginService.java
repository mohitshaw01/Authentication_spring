//package com.kapturecx.employeelogin.service;
//
//import com.kapturecx.employeelogin.Dao.EmployeeRepository;
//import com.kapturecx.employeelogin.dto.EmployeeLoginDto;
//import com.kapturecx.employeelogin.dto.EmployeeSignUpDto;
//import com.kapturecx.employeelogin.entity.EmployeeLogin;
//import com.kapturecx.employeelogin.util.EmployeeMapper;
//import com.kapturecx.employeelogin.util.EmployeeSignUpMapper;
//import com.kapturecx.employeelogin.validation.SignUpValidation;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmployeeLoginService {
//
//    @Autowired
//    private EmployeeRepository employeeRepository;
//    @Autowired
//    @Qualifier("employee")
//    ObjectMapper objectMapper;
//
//    public ResponseEntity<ObjectNode> login(EmployeeLoginDto employeeLoginDto) {
//        ObjectNode responseObject = objectMapper.createObjectNode();
//
//        EmployeeLogin employeeLogin = EmployeeMapper.dtoToEmployee(employeeLoginDto);
//        String username = employeeLogin.getUsername();
//        String password = employeeLogin.getPassword();
//        int client_id = employeeLogin.getClientId();
//        // verify the user data with validations here.
//        if(username == null || password == null || client_id <=0) {
//            responseObject.put("status" ,"Please enter email,client_id,and password");
//            return new ResponseEntity<>(responseObject, HttpStatus.BAD_REQUEST);
//        }
//
//        EmployeeLogin foundEmployee = employeeRepository.findByUsernameAndPassword(username, password, client_id);
//
//        if (foundEmployee == null) {
//            responseObject.put("result" , "Employee not found");
//            return new ResponseEntity<>(responseObject, HttpStatus.BAD_REQUEST);
//        }
//
//        foundEmployee.setActiveLogin(true);
//
//        //
//        if(employeeRepository.saveOrUpdateEmployee(foundEmployee))
//        {
//            return new ResponseEntity<>(responseObject, HttpStatus.OK);
//        }
//        return ResponseEntity.internalServerError().build();
//    }
//
//    public ResponseEntity<?> signup(EmployeeSignUpDto employeeSignUpDto) {
//        EmployeeLogin employeeLogin = EmployeeSignUpMapper.dtoToEmployee(employeeSignUpDto);
//        String username = employeeLogin.getUsername();
//        String password = employeeLogin.getPassword();
//        int client_id = employeeLogin.getClientId();
//        ObjectNode responseObject = objectMapper.createObjectNode();
//
//        if(SignUpValidation.validateSignUp(username, password, client_id)) {
//
//            if (employeeRepository.saveOrUpdateEmployee(employeeLogin)) {
//                responseObject.put("status",200);
//                responseObject.put("message","employee registered successfully");
//                return new ResponseEntity<>(responseObject,HttpStatus.OK);
//            }
//        }
//        return ResponseEntity.status(500).body("Enter Username or password or client_id");
//    }
//}

package com.kapturecx.employeelogin.service;

import com.kapturecx.employeelogin.dao.EmployeeRepository;
import com.kapturecx.employeelogin.dto.EmployeeLoginDto;
import com.kapturecx.employeelogin.dto.EmployeeSignUpDto;
import com.kapturecx.employeelogin.entity.EmployeeLogin;
import com.kapturecx.employeelogin.util.EmployeeMapper;
import com.kapturecx.employeelogin.util.EmployeeSignUpMapper;
import com.kapturecx.employeelogin.validation.SignUpValidation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.kapturecx.employeelogin.constant.Constants.*;

@Service
public class EmployeeLoginService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SessionService sessionService;

    @Autowired
    @Qualifier("employee")
    ObjectMapper objectMapper;

    public ResponseEntity<ObjectNode> login(EmployeeLoginDto employeeLoginDto, HttpServletRequest request, HttpServletResponse response) {
        ObjectNode responseObject = objectMapper.createObjectNode();
        EmployeeLogin employeeLogin = EmployeeMapper.dtoToEmployee(employeeLoginDto);
        String username = employeeLogin.getUsername();
        String password = employeeLogin.getPassword();
        int clientId = employeeLogin.getClientId();

        if (username.isEmpty() || password.isEmpty() || clientId <= 0) {
            responseObject.put("status", false);
            responseObject.put("message",INVALID_USERNAME_OR_PASSWORD);
            return new ResponseEntity<>(responseObject, HttpStatus.BAD_REQUEST);
        }

        //Checking user from db
        EmployeeLogin foundEmployee = employeeRepository.findByUsernameAndPassword(username, password, clientId);
        if (foundEmployee == null) {
            responseObject.put("status", false);
            responseObject.put("message",ERROR_USERNAME_NOT_FOUND);
            return new ResponseEntity<>(responseObject, HttpStatus.BAD_REQUEST);
        }

        boolean enable = foundEmployee.isEnable();
        if(!enable){
            responseObject.put("status",false);
            responseObject.put("message",USER_DISABLED);
            return new ResponseEntity<>(responseObject, HttpStatus.BAD_REQUEST);
        }
        try {
            responseObject.put("status", true);
            responseObject.put("message", SUCCESS_LOGIN);
            foundEmployee.setActiveLogin(true);
            int id = foundEmployee.getId();
            sessionService.createSession(request,response,clientId,username,password,id);
            if(employeeRepository.updateEmployee(foundEmployee))
                return new ResponseEntity<>(responseObject, HttpStatus.OK);
            else
                return new ResponseEntity<>(responseObject, HttpStatus.OK);
        } catch (Exception e) {
            responseObject.put("status", false);
            responseObject.put("message", INTERNAL_SERVER_ERROR);
            return ResponseEntity.internalServerError().body(responseObject);
        }
    }

    public ResponseEntity<ObjectNode> signup(EmployeeSignUpDto employeeSignUpDto) {
        ObjectNode responseObject = objectMapper.createObjectNode();
        EmployeeLogin employeeLogin = EmployeeSignUpMapper.dtoToEmployee(employeeSignUpDto);
        String username = employeeLogin.getUsername();
        String password = employeeLogin.getPassword();
        int clientIdd = employeeLogin.getClientId();

        if (!SignUpValidation.validateSignUp(username, password, clientIdd)) {
            responseObject.put("status", false);
            responseObject.put("message", ERROR_INVALID_CREDENTIALS);
            return new ResponseEntity<>(responseObject, HttpStatus.BAD_REQUEST);
        }

        try {
            if(!employeeRepository.saveEmployee(employeeLogin)){
                return new ResponseEntity<>(responseObject,HttpStatus.BAD_REQUEST);
            }
            responseObject.put("status", true);
            responseObject.put("message", SUCCESS_SIGNUP);
            return ResponseEntity.status(200).body(responseObject);
        } catch (Exception e) {
            responseObject.put("status", false);
            responseObject.put("message", ERROR_SIGNUP_FAILED);
            return ResponseEntity.status(500).body(responseObject);
        }
    }
}
