package com.ndmc.ndmc_record.utils;

import com.ndmc.ndmc_record.repository.AuthRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component

public class JwtUtil {

    private final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    //private static final String SECRET_KEY = "learn_programming_yourself";
    private static final String SECRET_KEY = "8enXN0M($ecrete#ey#or4e1h!";

    //5 Hours JWT Expiration time
    private static final int TOKEN_VALIDITY = 3600 * 5;

    @Autowired
    AuthRepository authRepository;

//    public String getUserIdFromRequest(HttpServletRequest request) {
//       String userName = getUsernameFromRequest(request);
//       logger.info("==== getUsernameFromRequest==="+userName);
//       UserModel userDetails =  authRepository.findById(Long.parseLong(userName));
//        //logger.info("==== userDetails==="+userDetails);
//     // String userId =  userDetails.getUserId().toString();
//       return userName;
//    }

    public String getUsernameFromRequest(HttpServletRequest request) {
        final String requestTokenHeader = request.getHeader("Authorization");
        String jwtToken = requestTokenHeader.substring(7);
        return getUsernameFromToken(jwtToken);
    }
    public String getUsernameFromToken(String token) {

        return getClaimFromToken(token, Claims::getSubject);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public String generateToken(UserDetails userDetails) throws Exception {

        Map<String, Object> claims = new HashMap<>();
        String ipAddress =  CommonUtil.getPublicIp();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }
}
