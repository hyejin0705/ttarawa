package com.jsdckj.ttarawa.jwt;

import com.jsdckj.ttarawa.oauth.UserDetailCustom;
import com.jsdckj.ttarawa.users.dto.res.UserResDto;
import com.jsdckj.ttarawa.users.entity.Users;
import com.jsdckj.ttarawa.users.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

  private final Key key;
  private final UserRepository userRepository;

//  public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
//    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//    this.key = Keys.hmacShaKeyFor(keyBytes);
//  }


  public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, UserRepository userRepository) {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.userRepository = userRepository;
  }

  // 유저 정보를 가지고 AccessToken, RefreshToken을 생성하는 메소드
  public UserResDto.TokenInfo generateToken(Authentication authentication) {
    String authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));

    long now = (new Date()).getTime();
//    String[] authoritiesSplit = authorities.split(",");
//    Optional<Users> user = userRepository.findByEmailAndProvider(authoritiesSplit[0], authoritiesSplit[1]);

    // AccessToken 생성
    Date accessTokenExpiresIn = new Date(now + JwtProperties.ACCESS_TOKEN_EXPIRE_TIME);
    String accessToken = Jwts.builder()
        .setSubject(authentication.getName())
        .claim("sub", authentication.getName()) // userId 담기
        .claim(JwtProperties.AUTHORITIES_KEY, authorities)
        .setExpiration(accessTokenExpiresIn)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();

    System.out.println("access -- "+accessToken);

    // Refresh Token 생성
    String refreshToken = Jwts.builder()
        .setExpiration(new Date(now + JwtProperties.REFRESH_TOKEN_EXPIRE_TIME))
        .claim("sub", authentication.getName())
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();

    System.out.println("refresh -- "+refreshToken);


    return UserResDto.TokenInfo.builder()
        .grantType(JwtProperties.BEARER_TYPE)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .refreshTokenExpirationTime(JwtProperties.REFRESH_TOKEN_EXPIRE_TIME)
        .build();
  }

  // JWT 토큰을 복호화 하여 토큰에 들어있는 정보를 꺼내는 메소드
  public Authentication getAuthentication(String accessToken) {

    System.out.println("sout getAuthentication");

    // 토큰 복호화
    Claims claims = parseClaims(accessToken);

    if(claims.get(JwtProperties.AUTHORITIES_KEY) == null){
      throw new RuntimeException("권한 정보가 없는 토큰입니다.");
    }

    // 클레임에서 권한 정보 가져오기
    Collection<? extends  GrantedAuthority> authorities =
        Arrays.stream(claims.get(JwtProperties.AUTHORITIES_KEY).toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    System.out.println("sout dd"+authorities);

    // UserDetails 객체를 만들어서 Authentication 리턴
    Users user = userRepository.findById(Long.parseLong(claims.get("sub").toString())).orElseThrow();
    UserDetailCustom principal = new UserDetailCustom(user);

//    UserDetailCustom principal = new User(claims.getSubject(),"", authorities);


    return new UsernamePasswordAuthenticationToken(principal, "", authorities);
  }

  // 토큰 정보를 검증하는 메서드
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJwt(token);
      return true;
    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
      log.info("Invalid JWT Token", e);
    } catch (ExpiredJwtException e) {
      log.info("Expired JWT Token", e);
    } catch (UnsupportedJwtException e) {
      log.info("Unsupported JWT Token", e);
    } catch (IllegalArgumentException e) {
      log.info("JWT claims string is empty.", e);
    }
    return false;
  }

  private Claims parseClaims(String accessToken) {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }

  public Long getExpiration(String accessToken) {
    // access token 남은 시간
    Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody().getExpiration();
    // 현재 시간
    Long now = new Date().getTime();
    return (expiration.getTime() - now);
  }
}
