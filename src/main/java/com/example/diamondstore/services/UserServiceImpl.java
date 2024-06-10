package com.example.diamondstore.services;

import com.example.diamondstore.dto.PasswordResetDTO;
import com.example.diamondstore.dto.UpdateUser;
import com.example.diamondstore.dto.UserLoginResponse;
import com.example.diamondstore.entities.User;
import com.example.diamondstore.repositories.RoleRepository;
import com.example.diamondstore.repositories.UserRepository;
import com.example.diamondstore.services.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.print.attribute.standard.DateTimeAtCreation;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private static final long EXPIRE_TOKEN=30;

    @Override
    public User register(String fullName, String email, String password, String phone, String gender, Date dob) {
        User saveUser = userRepository.save(User.builder()
             .fullName(fullName)
             .email(email)
             .password(bCryptPasswordEncoder.encode(password))
             .phone(phone)
             .gender(gender)
             .dob(dob)
             .roleid(roleRepository.findRoleByRoleid(5))
             .status("active")
             .point(0)
             .typeLogin("system account")
             .createAt(Date.from(Instant.now()))
             .build());
        return saveUser;
    }

    @Override
    public boolean isEmailDuplicated(String email) {
        try {
            return (userRepository.findUserByEmail(email) != null);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public UserLoginResponse login(String email, String password){
        User loginUser = userRepository.findUserByEmail(email);
        if(loginUser == null || !bCryptPasswordEncoder.matches(password,userRepository.findUserByEmail(email).getPassword())){
            return null;
        }else{
            return new UserLoginResponse(loginUser.getUserId(), loginUser.getEmail());
        }
    }

    @Override
    public Optional<User> getUserId(int id) {
         return userRepository.findById(id);
    }

    @Override
    public User getUserById(int userid) {
        return userRepository.findUserByUserId(userid);
    }

    @Override
    public List<User> userList() {
       return userRepository.findAll();
    }

    @Override
    public User updateUser(UpdateUser updateUser, int id) {

        BCryptPasswordEncoder b = new BCryptPasswordEncoder();
        User saveUser = getUserId(id).get();
        saveUser.setEmail(updateUser.getEmail());
        saveUser.setPassword(b.encode(updateUser.getPassword()));
        saveUser.setPhone(updateUser.getPhone());
        saveUser.setGender(updateUser.getGender());
        saveUser.setFullName(updateUser.getFullName());
        saveUser.setDob(updateUser.getDob());
        saveUser.setUpdateAt(new Date());

        return userRepository.save(saveUser);
    }

    @Override
    public boolean updateStatusByUserid(String status, int userid) {
        return userRepository.updateStatusByUserId(status, userid) == 1;
    }

    @Override
    public String forgotPass(String email) {
        User user = userRepository.findUserByEmail(email);

        if(user == null){
            return "Invalid email.";
        }

        user.setTokenPassword(generateToken());
        user.setTokenCreateDate(new Date());

        user = userRepository.save(user);
        return user.getTokenPassword();
    }

    @Override
    public String resetPass(String token, PasswordResetDTO passwordResetDTO) {
        Optional<User> userOptional= Optional.ofNullable(userRepository.findUserByTokenPassword(token));

        if(!userOptional.isPresent()){
            return "Invalid token";
        }
        Date tokenCreationDate = userOptional.get().getTokenCreateDate();

        if (isTokenExpired(tokenCreationDate)) {
            return "Token expired.";

        }

        if(!passwordResetDTO.getPassword().equals(passwordResetDTO.getConfirm())){
            return "Confirm password not match!";
        }

        User user = userOptional.get();

        user.setPassword(bCryptPasswordEncoder.encode(passwordResetDTO.getPassword()));
        user.setTokenPassword(null);
        user.setTokenCreateDate(null);

        userRepository.save(user);

        return "Your password successfully updated.";
    }

    private String generateToken() {
        StringBuilder token = new StringBuilder();

        return token.append(UUID.randomUUID())
                .append(UUID.randomUUID()).toString();
    }

    private boolean isTokenExpired(Date tokenCreationDate) {
        Instant tokenCreationInstant = tokenCreationDate.toInstant();
        Instant nowInstant = Instant.now();

        Duration diff = Duration.between(tokenCreationInstant, nowInstant);

        return diff.toMinutes() >= EXPIRE_TOKEN;
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

//    @Override
//    public void deleteById(Long id) {
//        userRepository.deleteById(id);
//    }
}
