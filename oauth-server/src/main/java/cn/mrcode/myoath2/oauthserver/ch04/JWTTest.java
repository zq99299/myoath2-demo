package cn.mrcode.myoath2.oauthserver.ch04;

import org.junit.Test;

import sun.misc.BASE64Decoder;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JWTTest {
    static String sharedTokenSecret = "hellooauthhellooauthhellooauthhellooauth";

    /**
     * 使用密匙生成 JWT 令牌
     */
    @Test
    public void buildJewTest() {
        String jwt = buildJwt();
        System.out.println("jwt:");
        System.out.println(jwt);
    }

    private String buildJwt() {
        Key key = new SecretKeySpec(sharedTokenSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("typ", "JWT");
        headerMap.put("alg", "HS256");

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("iss", "http://localhost:8081/");
        payloadMap.put("sub", "XIAOMINGTEST");
        payloadMap.put("aud", "APPID_RABBIT");
        payloadMap.put("exp", 1584105790703L);
        payloadMap.put("iat", 1584105948372L);

        // 生成 Jwt 令牌
        String jwt = Jwts.builder().setHeaderParams(headerMap).setClaims(payloadMap).signWith(key, SignatureAlgorithm.HS256).compact();
        return jwt;
    }

    /**
     * 使用正确的密匙解析 JWT 令牌
     */
    @Test
    public void parserJwt() {
        Key key = new SecretKeySpec(sharedTokenSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        String jwt = buildJwt();

        Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt);

        JwsHeader header = claimsJws.getHeader();
        Claims body = claimsJws.getBody();

        System.out.println("jwt header:" + header);
        System.out.println("jwt body:" + body);
        System.out.println("jwt sub:" + body.getSubject());
        System.out.println("jwt aud:" + body.getAudience());
        System.out.println("jwt iss:" + body.getIssuer());
        System.out.println("jwt exp:" + body.getExpiration());
        System.out.println("jwt iat:" + body.getIssuedAt());
    }

    /**
     * 使用错误的的密匙解析 JWT 令牌
     */
    @Test
    public void parserJwtError() {
        Key key = new SecretKeySpec("hellooauthhellooauthhellooauthhellooau12".getBytes(), SignatureAlgorithm.HS256.getJcaName());
        String jwt = buildJwt();

        // JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.
        Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt);

        JwsHeader header = claimsJws.getHeader();
        Claims body = claimsJws.getBody();

        System.out.println("jwt header:" + header);
        System.out.println("jwt body:" + body);
        System.out.println("jwt sub:" + body.getSubject());
        System.out.println("jwt aud:" + body.getAudience());
        System.out.println("jwt iss:" + body.getIssuer());
        System.out.println("jwt exp:" + body.getExpiration());
        System.out.println("jwt iat:" + body.getIssuedAt());
    }
}
