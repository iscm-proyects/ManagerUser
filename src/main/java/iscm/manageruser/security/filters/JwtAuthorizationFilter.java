package iscm.manageruser.security.filters;

import io.jsonwebtoken.Claims;
import iscm.manageruser.security.jwt.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {


    private final JwtUtils jwtUtils;
    public JwtAuthorizationFilter(JwtUtils jwtUtils)
    {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String tokenHeader = request.getHeader("Authorization");

        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);

            if (jwtUtils.isTokenValid(token)) {
                String username = jwtUtils.getUsernameFromToken(token);
                Claims claims = jwtUtils.extractAllClaims(token);


                List<String> rolesFromClaims = claims.get("roles", List.class);
                Collection<SimpleGrantedAuthority> authorities = rolesFromClaims.stream()
                        // añadir el prefijo "ROLE_" si tus @PreAuthorize lo esperan (y sí lo hacen).
                        .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                        .collect(Collectors.toList());

                /*List<String> roles = claims.get("roles", List.class);

                Collection<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                 */
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    public Collection<SimpleGrantedAuthority> extracRole(String roles)
    {
        roles=roles.replace("[","");
        roles=roles.replace("]","");
        roles=roles.replace(" ","");
        ArrayList<String> rolesextaidos = new ArrayList<>();
        while (roles.contains(","))
        {
            int p = roles.indexOf(",");
            rolesextaidos.add(roles.substring(0,p));
            roles = roles.substring(p+1);
        }
        rolesextaidos.add(roles);

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        int c=0,size=rolesextaidos.size();
        while(c<size)
        {
            authorities.add(new SimpleGrantedAuthority(rolesextaidos.get(c)));
            c++;
        }
        return authorities;
    }
}