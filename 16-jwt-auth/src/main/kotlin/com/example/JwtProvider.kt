package com.example

import org.springframework.stereotype.Component
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.exceptions.*
import java.time.Instant

@Component
class JwtProvider(
    val props: JwtConfig
){
    fun createToken(
        sub: String, 
        roles: List<String>, 
        now: Instant = Instant.now(), 
        ttlSeconds: Long = props.ttlSeconds) : String{
        try {
            val algorithm = Algorithm.HMAC256(props.secret)
            return JWT.create()
                .withIssuer(props.issuer)
                .withAudience(props.audience)
                .withIssuedAt(now)
                .withExpiresAt(now.plusSeconds(ttlSeconds))
                .withClaim("roles", roles)
                .withSubject(sub)
                .sign(algorithm);
        } catch (e: JWTCreationException){
            throw RuntimeException("can not create token", e)
        }
    }

    fun verifyToken(token: String): DecodedJWT{
        try {
            val algorithm = Algorithm.HMAC256(props.secret)
            val verifier = JWT.require(algorithm)
                .withIssuer(props.issuer)
                .withAudience(props.audience)
                .build();
                
            return verifier.verify(token);
        } catch (e: JWTVerificationException){
            throw RuntimeException("can not verify token", e)
        }
    }
}