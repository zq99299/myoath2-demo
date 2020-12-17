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
     * 使用密匙解析 JWT 令牌
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

    static String publicKey = "QgkAQIDAQAB";
    static String privateKey = "hellooauth";


    public static PublicKey getPublicKey() {
        try {
//            byte[] keyBytes = Base64.getDecoder().decode(publicKey.getBytes());
            byte[] keyBytes = (new BASE64Decoder()).decodeBuffer(publicKey); // 正确方式

            RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
//            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
//            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return pubKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey getPrivateKey() {

        try {
//            byte[] keyBytes = Base64.getDecoder().decode(privateKey.getBytes());
            byte[] keyBytes = (new BASE64Decoder()).decodeBuffer(privateKey); // 正确方式

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
