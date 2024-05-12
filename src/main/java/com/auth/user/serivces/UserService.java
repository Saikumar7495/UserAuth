package com.auth.user.serivces;

import com.auth.user.models.Token;
import com.auth.user.models.User;
import com.auth.user.repositories.TokenRepository;
import com.auth.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public User signUp(String name,String email,String password){
        Optional<User> optionalUser=userRepository.findByEmail(email);
        if(optionalUser.isPresent()){
            //throw user already present
        }
        User user=new User();
        user.setEmail(email);
        user.setName(name);
        user.setHashedPassword(bCryptPasswordEncoder.encode(password));

        return userRepository.save(user);
    }

    public Token login(String email, String password){
        Optional<User> optionalUser=userRepository.findByEmail(email);
        if(optionalUser.isEmpty()){
            return null;
        }

        User user=optionalUser.get();
        if(!bCryptPasswordEncoder.matches(password, user.getHashedPassword())){
            return null;
        }
        Token token=new Token();
        token.setUser(user);
        token.setExpirydate(get30DaysLaterDate());
        token.setValue(UUID.randomUUID().toString());

        return tokenRepository.save(token);
    }

    private Date get30DaysLaterDate() {
        Date date=new Date();

        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);

        //add 30 days
        calendar.add(Calendar.DAY_OF_MONTH,30);

        //extract date from calender
        return calendar.getTime();
    }

    public void logout(String token){
        Optional<Token> optionalToken=
                tokenRepository.findByValueAndIsDeletedEquals(token,false);

        if(optionalToken.isEmpty()){
            return;
        }

        Token updatedToken=optionalToken.get();
        updatedToken.setDeleted(true);
        tokenRepository.save(updatedToken);
    }

    public boolean validateToken(String token){
        /*
        1. Check if the token is present in db
        2. Check if the token is not deleted
        3. Check if the token is not expired
         */
        Optional<Token> tokenOptional =
                tokenRepository.findByValueAndIsDeletedEqualsAndExpirydateGreaterThan(
                        token, false, new Date());

        return tokenOptional.isPresent();
    }
}
